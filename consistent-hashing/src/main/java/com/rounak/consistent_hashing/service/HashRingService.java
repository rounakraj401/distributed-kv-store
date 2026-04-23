package com.rounak.consistent_hashing.service;

import com.rounak.consistent_hashing.model.DataEntity;
import com.rounak.consistent_hashing.model.Node;
import com.rounak.consistent_hashing.model.NodeEntity;
import com.rounak.consistent_hashing.repository.DataRepository;
import com.rounak.consistent_hashing.repository.NodeRepository;
import com.rounak.consistent_hashing.util.HashUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Service
public class HashRingService {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final TreeMap<Long, Node> ring = new TreeMap<>();
    private final Map<String, Boolean> nodeHealth = new ConcurrentHashMap<>();
    private final int VIRTUAL_NODES = 3;
    private final Set<String> keys = ConcurrentHashMap.newKeySet();
    private final ReadWriteLock ringLock = new ReentrantReadWriteLock();

    private final int REPLICATION_FACTOR = 3;
    private final int WRITE_QUORUM = (REPLICATION_FACTOR / 2) + 1;
    private final int READ_QUORUM = (REPLICATION_FACTOR / 2) + 1;

    @Scheduled(fixedRate = 5000)
    public void checkNodeHealth() {

        for (NodeEntity entity : nodeRepository.findAll()) {

            String nodeId = entity.getId();
            String url = entity.getUrl() + "/internal/health";

            boolean wasDown = !nodeHealth.getOrDefault(nodeId, true);

            try {
                restTemplate.getForObject(url, String.class);
                nodeHealth.put(nodeId, true);

                // RECOVERY DETECTED
                if (wasDown) {
                    System.out.println("Node RECOVERED: " + nodeId);
                    triggerRecovery(nodeId);
                }
            } catch (Exception e) {
                nodeHealth.put(nodeId, false);
            }
        }
    }

    @Scheduled(fixedRate = 60000) // every 60 sec
    @Async
    public void antiEntropyRepair() {

        System.out.println("Replicas sync started...");

        for (String key : keys) {

            List<Node> nodes = getNodesForKey(key);

            Map<String, Integer> valueCount = new HashMap<>();
            Map<Node, String> nodeResponses = new HashMap<>();

            for (Node node : nodes) {
                try {
                    String value = restTemplate.getForObject(
                            node.getUrl() + "/internal/data?key=" + key,
                            String.class
                    );

                    if (value != null && !value.equals("Not found")) {
                        nodeResponses.put(node, value);
                        valueCount.put(value,
                                valueCount.getOrDefault(value, 0) + 1);
                    }

                } catch (Exception ignored) {}
            }

            // find majority value
            String correctValue = null;
            int max = 0;

            for (Map.Entry<String, Integer> entry : valueCount.entrySet()) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                    correctValue = entry.getKey();
                }
            }

            if (correctValue == null) continue;

            // repair inconsistent nodes
            for (Map.Entry<Node, String> entry : nodeResponses.entrySet()) {

                if (!entry.getValue().equals(correctValue)) {

                    try {
                        System.out.println("Anti-entropy fixing node: " + entry.getKey().getId());

                        restTemplate.postForObject(
                                entry.getKey().getUrl() +
                                        "/internal/data?key=" + key + "&value=" + correctValue,
                                null,
                                String.class
                        );

                    } catch (Exception ignored) {}
                }
            }
        }

        System.out.println("Replicas sync completed");
    }

    @PostConstruct
    public void loadStates() {

        // Load nodes
        List<NodeEntity> nodes = nodeRepository.findAll();

        for (NodeEntity entity : nodes) {
            addNodeToRing(new Node(entity.getId(), entity.getUrl()));
            nodeHealth.put(entity.getId(), true);
        }

        System.out.println("Loaded nodes from DB: " + nodes.size());

        // Load keys
        List<DataEntity> data = dataRepository.findAll();

        for (DataEntity entity : data) {
            keys.add(entity.getKey());
        }

        System.out.println("Loaded keys from DB: " + data.size());
    }


    @Transactional
    public void addNode(Node node) {
        if (!nodeRepository.existsById(node.getId())) {
            nodeRepository.save(new NodeEntity(node.getId(), node.getUrl()));
        }
        nodeHealth.put(node.getId(), true);
        addNodeToRing(node);
        triggerRebalancing();
    }

    public void removeNodeById(String id) {

        NodeEntity entity = nodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Node not found"));

        Node node = new Node(entity.getId(), entity.getUrl());
        // remove from DB
        nodeRepository.deleteById(id);
        // remove from ring
        removeNodeFromRing(node);

        triggerRebalancing();
    }

    public void addNodeToRing(Node node) {
        ringLock.writeLock().lock();
        try {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                String virtualKey = node.getId() + "#VN" + i;
                long hash = HashUtil.hash(virtualKey);
                ring.put(hash, node);
            }
        } finally {
            ringLock.writeLock().unlock();
        }
    }

    public void removeNodeFromRing(Node node) {
        ringLock.writeLock().lock();
        try {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                String virtualKey = node.getId() + "#VN" + i;
                long hash = HashUtil.hash(virtualKey);
                ring.remove(hash);
            }
        } finally {
            ringLock.writeLock().unlock();
        }
    }

    public List<Node> getNodesForKey(String key) {

        ringLock.readLock().lock();
        try {
            List<Node> nodes = new ArrayList<>();

            if (ring.isEmpty()) return nodes;

            long hash = HashUtil.hash(key);

            // get first node (primary)
            Map.Entry<Long, Node> entry = ring.ceilingEntry(hash);

            if (entry == null) {
                entry = ring.firstEntry();
            }

            // iterate clockwise to get replicas
            Iterator<Map.Entry<Long, Node>> iterator =
                    ring.tailMap(entry.getKey(), true).entrySet().iterator();

            while (nodes.size() < REPLICATION_FACTOR && iterator.hasNext()) {
                Node node = iterator.next().getValue();

                //avoid dead nodes ( due to node failure )     USE THIS WHEN PERFORMANCE IMPROVEMENT NEEDED ->Skips node failure logs when used
//            if(nodeHealth.getOrDefault(node.getId(), true)) {
                // avoid duplicates (due to virtual nodes)
                if (!nodes.contains(node)) {
                    nodes.add(node);
                }
//            }
            }

            // wrap around if needed
            iterator = ring.entrySet().iterator();
            while (nodes.size() < REPLICATION_FACTOR && iterator.hasNext()) {
                Node node = iterator.next().getValue();

                //avoid dead nodes ( due to node failure )     USE THIS WHEN PERFORMANCE IMPROVEMENT NEEDED ->Skips node failure logs when used
//            if(nodeHealth.getOrDefault(node.getId(), true)) {
                // avoid duplicates (due to virtual nodes)
                if (!nodes.contains(node)) {
                    nodes.add(node);
                }
//            }
            }
            return nodes;
        }
        finally {
            ringLock.readLock().unlock();
        }
    }

    public String storeData(String key, String value) {

        List<Node> nodes = getNodesForKey(key);

        if (nodes.isEmpty()) {
            return "No nodes available";
        }

        List<Node> successNodes = new ArrayList<>();

        for (Node node : nodes) {
            try {
                String url = node.getUrl() + "/internal/data?key=" + key + "&value=" + value;

                executeWithRetry(() -> {
                    restTemplate.postForObject(url, null, String.class);
                    return null;
                }, node.getId());

                successNodes.add(node);

            } catch (Exception e) {
                System.out.println("Write failed on node: " + node.getId());
            }
        }

        // QUORUM CHECK
        if (nodes.size()>1 && successNodes.size() < WRITE_QUORUM) {

            System.out.println("Quorum not met. Rolling back...");

            for (Node node : successNodes) {
                try {
                    restTemplate.delete(
                            node.getUrl() + "/internal/data?key=" + key
                    );
                } catch (Exception ignored) {}
            }

            return "Write failed (quorum not met)";
        }

        if (!dataRepository.existsById(key)) {
            keys.add(key);
            dataRepository.save(new DataEntity(key));
        }

        return "Write successful on nodes: " +
                successNodes.stream().map(Node::getId).toList();
    }

    public String getData(String key) {

        List<Node> nodes = getNodesForKey(key);

        if (nodes.isEmpty()) {
            return "No nodes available";
        }

        Map<String, Integer> valueCount = new HashMap<>();
        Map<Node, String> nodeResponses = new HashMap<>();
        boolean dataFound = false;

        for (Node node : nodes) {
            try {
                String url = node.getUrl() + "/internal/data?key=" + key;

                String response = executeWithRetry(() ->
                                restTemplate.getForObject(url, String.class),
                        node.getId()
                );

                if (response != null && !response.equals("Not found")) {
                    dataFound = true;
                    nodeResponses.put(node, response);

                    valueCount.put(response,
                            valueCount.getOrDefault(response, 0) + 1);

                    //QUORUM READ HIT
                    if (nodes.size()>1 && valueCount.get(response) >= READ_QUORUM) {
                        return response + " (quorom met)";
                    }
                }

            } catch (Exception ignored) {}
        }

        //Data exists but quorum not met
        if (dataFound) {
            return "Read failed, quorum not met";
        }
        //No data anywhere
        return "Key not found";
    }

    public String removeData(String key) {

        List<Node> nodes = getNodesForKey(key);

        if (nodes.isEmpty()) {
            return "No nodes available";
        }

        int success = 0;

        for (Node node : nodes) {
            try {

                String url = node.getUrl() + "/internal/data?key=" + key;

                executeWithRetry(() -> {
                    restTemplate.delete(url);
                    return null;
                }, node.getId());

                success++;

            } catch (Exception e) {
                System.out.println("Delete failed on " + node.getId());
            }
        }

        if (success >= WRITE_QUORUM) {

            keys.remove(key);
            dataRepository.deleteById(key);

            return "Deleted successfully (quorum met)";
        }

        return "Delete failed (quorum not met)";
    }

    public String getFullMapping() {

        StringBuilder sb = new StringBuilder();

        sb.append("\n===== VIRTUAL NODES =====\n");

        for (Map.Entry<Long, Node> entry : ring.entrySet()) {
            sb.append("Hash: ")
                    .append(entry.getKey())
                    .append(" -> Node: ")
                    .append(entry.getValue().getId())
                    .append("\n");
        }

        sb.append("\n===== KEY MAPPING =====\n");
        if(keys.isEmpty()){
            sb.append("\nNo Keys in Application\n");
        }
        else {
            for (String key : keys) {

                long keyHash = HashUtil.hash(key);

                Map.Entry<Long, Node> entry = ring.ceilingEntry(keyHash);

                if (entry == null) {
                    entry = ring.firstEntry();
                }

                sb.append("Key: ").append(key)
                        .append(" | Hash: ").append(keyHash)
                        .append(" | Assigned Node: ").append(entry.getValue().getId())
                        .append("\n");
            }
        }

        return sb.toString();
    }

    @Async
    public void triggerRecovery(String recoveredNodeId) {

        System.out.println("Starting recovery for: " + recoveredNodeId);
        Node recoveredNode = getNodeById(recoveredNodeId);

        for (String key : keys) {

            // Nodes that SHOULD contain this key now
            List<Node> correctNodes = getNodesForKey(key);

            boolean shouldStore = correctNodes.stream()
                    .anyMatch(n -> n.getId().equals(recoveredNodeId));

            if (!shouldStore) {
                continue;
            }

            try {
                // STEP 1: Check if recovered node already has key
                String existing = restTemplate.getForObject(
                        recoveredNode.getUrl() +
                                "/internal/data?key=" + key,
                        String.class
                );

                if (existing != null &&
                        !existing.equals("Not found")) {

                    // already recovered locally (WAL / SSTable)
                    continue;
                }

            } catch (Exception ignored) {}

            // STEP 2: Fetch from healthy replica
            for (Node replica : correctNodes) {

                if (replica.getId().equals(recoveredNodeId)) {
                    continue;
                }

                try {

                    String value = restTemplate.getForObject(
                            replica.getUrl() +
                                    "/internal/data?key=" + key,
                            String.class
                    );

                    if (value != null &&
                            !value.equals("Not found")) {

                        // STEP 3: Copy to recovered node
                        restTemplate.postForObject(
                                recoveredNode.getUrl()
                                        + "/internal/data?key=" + key
                                        + "&value=" + value,
                                null,
                                String.class
                        );

                        System.out.println("Recovered key "
                                + key + " from "
                                + replica.getId());

                        break;
                    }

                } catch (Exception ignored) {}
            }
        }

        System.out.println("Recovery completed for: " + recoveredNodeId);
    }

    private Node getNodeById(String nodeId) {
        NodeEntity entity = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));

        return new Node(entity.getId(), entity.getUrl());
    }

    @Async
    public void triggerRebalancing() {

        System.out.println("Rebalancing started...");

        for (String key : keys) {

            // Nodes that SHOULD store this key (new ring)
            List<Node> correctNodes = getNodesForKey(key);

            //Ensure all correct nodes have data
            for (Node targetNode : correctNodes) {

                try {
                    //Check if already present
                    String existing = restTemplate.getForObject(
                            targetNode.getUrl() + "/internal/data?key=" + key,
                            String.class
                    );

                    if (existing != null && !existing.equals("Not found")) {
                        continue; // already correct
                    }

                    //Find source node (any node that has data)
                    String value = null;
                    Node sourceNodeUsed = null;

                    for (Node sourceNode : getUniqueNodes()) {

                        try {
                            String response = restTemplate.getForObject(
                                    sourceNode.getUrl() + "/internal/data?key=" + key,
                                    String.class
                            );

                            if (response != null && !response.equals("Not found")) {
                                value = response;
                                sourceNodeUsed = sourceNode;
                                break;
                            }

                        } catch (Exception ignored) {}
                    }

                    if (value == null) continue; // no source found

                    //COPY to target node
                    restTemplate.postForObject(
                            targetNode.getUrl() + "/internal/data?key=" + key + "&value=" + value,
                            null,
                            String.class
                    );

                    //VERIFY copy
                    String verify = restTemplate.getForObject(
                            targetNode.getUrl() + "/internal/data?key=" + key,
                            String.class
                    );

                    if (verify == null || verify.equals("Not found")) {
                        System.out.println("Verification failed for key: " + key +
                                " on node: " + targetNode.getId());
                        continue;
                    }

                    System.out.println("Copied " + key +
                            " data to node: " + targetNode.getId());

                } catch (Exception e) {
                    System.out.println("Failed processing target node: " + targetNode.getId());
                }
            }

            //DELETE from nodes that should NOT have data
            for (Node node : getUniqueNodes()){

                boolean shouldKeep = correctNodes.stream()
                        .anyMatch(n -> n.getId().equals(node.getId()));

                if (!shouldKeep) {

                    try {
                        // Check if node has data
                        String existing = restTemplate.getForObject(
                                node.getUrl() + "/internal/data?key=" + key,
                                String.class
                        );

                        if (existing != null && !existing.equals("Not found")) {

                            // DELETE
                            restTemplate.delete(
                                    node.getUrl() + "/internal/data?key=" + key
                            );

                            System.out.println("Deleted " + key +
                                    " data from previous node: " + node.getId());
                        }

                    } catch (Exception ignored) {}
                }
            }
        }
        System.out.println("Rebalancing completed");
    }

    private Set<Node> getUniqueNodes() {
        ringLock.readLock().lock();
        try {
            return new HashSet<>(ring.values());
        } finally {
            ringLock.readLock().unlock();
        }
    }

    private <T> T executeWithRetry(Supplier<T> action, String nodeId) {
        int retries = 3;

        for (int i = 0; i < retries; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                System.out.println("Retry " + (i + 1) + " failed for node: " + nodeId);
            }
        }
        nodeHealth.put(nodeId, false);
        throw new RuntimeException("All retries failed , Node failed!!!: " + nodeId);
    }
}
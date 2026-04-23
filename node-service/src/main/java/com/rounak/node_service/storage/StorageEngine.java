package com.rounak.node_service.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.TreeMap;

@Service
public class StorageEngine {

    private final TreeMap<String, String> memTable = new TreeMap<>();
    private static final String TOMBSTONE = "__DELETED__";

    @Autowired
    private WALManager walManager;

    @Autowired
    private SSTableManager ssTableManager;

    private static final int THRESHOLD = 5;

    @Value("${node.id}")
    private String nodeId;

    @PostConstruct
    public void recover() {

        ssTableManager.loadExistingFiles(); // detect existing SSTables

        File nodeDir = new File(
                System.getProperty("user.dir") + "/data/" + nodeId
        );

        File file = new File(nodeDir, "wal.log");

        if (!file.exists()) {
            System.out.println("No WAL found");
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(":");

                if (parts.length == 2) {
                    memTable.put(parts[0], parts[1]);
                }
            }
            System.out.println("Recovered " + memTable.size() + " entries from WAL");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void put(String key, String value) {

        // 1. WAL
        walManager.write(key, value);

        // 2. MemTable
        memTable.put(key, value);

        // 3. Flush
        if (memTable.size() >= THRESHOLD) {

            System.out.println("MemTable full , flushing data into SStable.");

            ssTableManager.flush(memTable);
            memTable.clear();
            walManager.clear();

            System.out.println("Flush completed. WAL cleared");

            if (ssTableManager.getFileCount() >= 3) {
                ssTableManager.compact();
            }
        }
    }

    public String get(String key) {
        // 1. Check MemTable
        if (memTable.containsKey(key)) {
            String value = memTable.get(key);
            return TOMBSTONE.equals(value) ? null : value;
        }
        // 2. Check SSTables
        String value = ssTableManager.read(key);

        if (TOMBSTONE.equals(value)) {
            return null;
        }
        return value;
    }

    public void remove(String key) {           //“In LSM, delete doesn’t remove data — it writes a tombstone”
        // 1. Write to WAL
        walManager.write(key, TOMBSTONE);

        // 2. Put tombstone in MemTable
        memTable.put(key, TOMBSTONE);

        // 3. Flush if needed
        if (memTable.size() >= THRESHOLD) {
            ssTableManager.flush(memTable);
            memTable.clear();
            walManager.clear();
        }

        if (ssTableManager.getFileCount() >= 3) {
            ssTableManager.compact();
        }
    }
}

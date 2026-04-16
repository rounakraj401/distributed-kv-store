package com.rounak.consistent_hashing.controller;

import com.rounak.consistent_hashing.model.Node;
import com.rounak.consistent_hashing.service.HashRingService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ConsistentHashController {

    @Autowired
    private HashRingService hashRingService;

    @PostConstruct
    public void init() {
        System.out.println("Controller loaded!");
    }

    @PostMapping("/node")
    public String addNode(@RequestParam String id,
                          @RequestParam String url) {

        hashRingService.addNode(new Node(id, url));
        return "Node added :" + id;
    }

    @DeleteMapping("/node")
    public String removeNode(@RequestParam String id) {
        System.out.println("node to be deleted: " + id);
        hashRingService.removeNodeById(id);
        return "Node removed: " + id;
    }

    @PostMapping("/data")
    public String put(@RequestParam String key,
                      @RequestParam String value) {
        return hashRingService.storeData(key, value);
    }

    @GetMapping("/data")
    public String get(@RequestParam String key) {
        return hashRingService.getData(key);
    }

    @GetMapping("/full-mapping")
    public String fullMapping() {
        return hashRingService.getFullMapping();
    }
}
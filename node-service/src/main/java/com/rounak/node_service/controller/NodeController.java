package com.rounak.node_service.controller;

import com.rounak.node_service.storage.StorageEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class NodeController {

    @Autowired
    private StorageEngine storageEngine;

    @PostMapping("/data")
    public String put(@RequestParam String key,
                      @RequestParam String value) {
        storageEngine.put(key, value);
        return "Stored";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    @GetMapping("/data")
    public String get(@RequestParam String key) {
        String value = storageEngine.get(key);
        return value != null ? value : "Not found";
    }

    @DeleteMapping("/data")
    public String delete(@RequestParam String key) {
        storageEngine.remove(key);
        return "Deleted";
    }
}
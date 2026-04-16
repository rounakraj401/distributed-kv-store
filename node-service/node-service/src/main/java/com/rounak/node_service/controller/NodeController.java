package com.rounak.node_service.controller;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class NodeController {

    private final Map<String, String> storage = new ConcurrentHashMap<>();

    @PostMapping("/data")
    public String put(@RequestParam String key,
                      @RequestParam String value) {
        storage.put(key, value);
        return "Stored";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    @GetMapping("/data")
    public String get(@RequestParam String key) {
        return storage.getOrDefault(key, "Not found");
    }

    @DeleteMapping("/data")
    public String delete(@RequestParam String key) {
        storage.remove(key);
        return "Deleted";
    }
}
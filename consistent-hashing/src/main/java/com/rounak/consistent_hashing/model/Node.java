package com.rounak.consistent_hashing.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//public class Node {
//
//    private final String id;
//    private final Map<String, String> storage = new ConcurrentHashMap<>();
//
//    public Node(String id) {
//        this.id = id;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public Map<String, String> getStorage() {
//        return storage;
//    }
//}
public class Node {

    private String id;
    private String url;

    public Node(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
}
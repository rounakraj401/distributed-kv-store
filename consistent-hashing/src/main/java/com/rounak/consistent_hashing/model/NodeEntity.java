package com.rounak.consistent_hashing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class NodeEntity {

    @Id
    private String id;

    private String url;

    public NodeEntity() {}

    public NodeEntity(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
}
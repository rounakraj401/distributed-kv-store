package com.rounak.consistent_hashing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DataEntity {

    @Id
    private String dataKey;

    private String dataValue;
    private String nodeId;

    public DataEntity() {}

    public DataEntity(String key, String value, String nodeId) {
        this.dataKey = key;
        this.dataValue = value;
        this.nodeId = nodeId;
    }

    public String getKey() { return dataKey; }
    public String getValue() { return dataValue; }
    public String getNodeId() { return nodeId; }
}
# 🚀 Distributed Key-Value Store with Consistent Hashing

A production-inspired **distributed key-value storage system** built using **Java + Spring Boot**, implementing core distributed system concepts like **consistent hashing, replication, quorum-based reads/writes, fault tolerance, and self-healing mechanisms**.

---

# 📌 Overview

This project simulates how modern distributed databases (like Cassandra/DynamoDB) work internally.

It consists of two main components:

* **Router Service (consistent-hashing)**
  Responsible for request routing, hashing, replication, and system coordination.

* **Node Service (node-service)**
  Represents individual storage nodes that store key-value data.

---

# 🧱 Architecture

```
                ┌──────────────────────────┐
                │        Client            │
                └────────────┬─────────────┘
                             │
                             ▼
                ┌──────────────────────────┐
                │  Consistent Hash Router  │
                │  (consistent-hashing)    │
                └────────────┬─────────────┘
                             │
        ┌───────────────┬────┴────┬───────────────┐
        ▼               ▼         ▼               ▼
 ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐
 │ Node A     │ │ Node B     │ │ Node C     │ │ Node D     │
 │ (8081)     │ │ (8082)     │ │ (8083)     │ │ (8084)     │
 └────────────┘ └────────────┘ └────────────┘ └────────────┘
```

---

# ⚙️ How It Works

### 🔹 Consistent Hashing

* Keys and nodes are mapped to a hash ring
* Each key is routed to the nearest clockwise node
* Supports **minimal data movement during scaling**

---

### 🔹 Virtual Nodes

* Each physical node is represented multiple times
* Ensures **better load distribution**

---

### 🔹 Replication

* Each key is stored on **multiple nodes**
* Provides **fault tolerance**

---

### 🔹 Quorum-based Reads/Writes

| Parameter | Meaning            |
| --------- | ------------------ |
| N         | Replication factor |
| W         | Write quorum       |
| R         | Read quorum        |

Condition:

```
R + W > N
```

Ensures **strong eventual consistency**

---

### 🔹 Write Flow

```
Client → Router → N nodes
        → success if ≥ W nodes succeed
        → else rollback
```

---

### 🔹 Read Flow

```
Client → Router → multiple replicas
        → majority/quorum decision
        → read repair if mismatch
```

---

# 🔁 Self-Healing Mechanisms

---

## 🟢 Read Repair

* Fixes inconsistent replicas during read
* Ensures **hot data remains consistent**

---

## 🟡 Anti-Entropy (Background Repair)

* Runs periodically
* Fixes inconsistencies across all nodes
* Ensures **eventual consistency**

---

## 🔵 Node Recovery

* When a node comes back UP:

  * Data is restored from replicas
  * Ensures **no data loss after downtime**

---

## 🔴 Rebalancing

* Triggered on:

  * Node addition
  * Node removal

Steps:

1. Copy data to correct nodes
2. Verify
3. Delete from incorrect nodes

---

# 🛡️ Fault Tolerance

* Handles node failures gracefully
* Retries failed requests
* Skips unhealthy nodes
* Supports failover to replicas

---

# ⚡ Features Implemented

* ✅ Consistent Hashing Ring
* ✅ Virtual Nodes
* ✅ Replication (N copies)
* ✅ Quorum Reads/Writes
* ✅ Retry Mechanism
* ✅ Node Health Check
* ✅ Failover Handling
* ✅ Data Rebalancing
* ✅ Read Repair
* ✅ Anti-Entropy Background Sync
* ✅ Node Recovery

---

# 🧠 Design Trade-offs

| Feature             | Choice                             |
| ------------------- | ---------------------------------- |
| Consistency         | Eventual consistency               |
| Availability        | High                               |
| Partition tolerance | Supported                          |

---

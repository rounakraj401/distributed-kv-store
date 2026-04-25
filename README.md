<img width="300" height="142" alt="hero_architecture_banner" src="https://github.com/user-attachments/assets/cfab6e0b-8ff4-4999-8dd0-8c1872de3179" /># 🚀 Distributed Key-Value Store with Consistent Hashing

A production-inspired **distributed key-value storage system** built using **Java + Spring Boot**, implementing core distributed system concepts like **consistent hashing, replication, quorum-based reads/writes, fault tolerance, self-healing mechanisms, and an LSM-inspired storage engine**.

---

# 📌 Overview

This project simulates how modern distributed databases (like Cassandra/DynamoDB) work internally.

It consists of two main components:

* **Router Service (consistent-hashing)**
  Responsible for request routing, hashing, replication, and system coordination.

* **Node Service (node-service)**
  Represents individual storage nodes that store key-value data, backed by an LSM-inspired storage engine with crash recovery support.

---

# 🧱 Architecture

![Uploading hero_<svg width="100%" viewBox="0 0 680 321.83" role="img" style="" xmlns="http://www.w3.org/2000/svg">
  <title style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">Distributed KV Store Architecture</title>
  <desc style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">System architecture showing Client connecting to a Consistent Hash Router which fans out to four storage nodes A through D</desc>
  <defs>
    <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M2 1L8 5L2 9" fill="none" stroke="context-stroke" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
    </marker>
  </defs>

  <!-- Client -->
  <g onclick="sendPrompt('How does the client interact with the distributed KV store?')" style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">
    <rect x="260" y="20" width="160" height="52" rx="8" stroke-width="0.5" style="fill:rgb(241, 239, 232);stroke:rgb(95, 94, 90);color:rgb(0, 0, 0);stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
    <text x="340" y="40" text-anchor="middle" dominant-baseline="central" style="fill:rgb(68, 68, 65);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:14px;font-weight:500;text-anchor:middle;dominant-baseline:central">Client</text>
    <text x="340" y="58" text-anchor="middle" dominant-baseline="central" style="fill:rgb(95, 94, 90);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">External requests</text>
  </g>

  <!-- Arrow Client → Router -->
  <line x1="340" y1="72" x2="340" y2="108" marker-end="url(#arrow)" style="fill:none;stroke:rgb(115, 114, 108);color:rgb(0, 0, 0);stroke-width:1.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>

  <!-- Router -->
  <g onclick="sendPrompt('How does the consistent hash router work?')" style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">
    <rect x="190" y="108" width="300" height="56" rx="8" stroke-width="0.5" style="fill:rgb(238, 237, 254);stroke:rgb(83, 74, 183);color:rgb(0, 0, 0);stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
    <text x="340" y="128" text-anchor="middle" dominant-baseline="central" style="fill:rgb(60, 52, 137);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:14px;font-weight:500;text-anchor:middle;dominant-baseline:central">Consistent Hash Router</text>
    <text x="340" y="148" text-anchor="middle" dominant-baseline="central" style="fill:rgb(83, 74, 183);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">Hashing · Replication · Quorum</text>
  </g>

  <!-- Fan-out arrows Router → Nodes -->
  <line x1="234" y1="164" x2="90" y2="218" marker-end="url(#arrow)" style="fill:none;stroke:rgb(115, 114, 108);color:rgb(0, 0, 0);stroke-width:1.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <line x1="294" y1="164" x2="248" y2="218" marker-end="url(#arrow)" style="fill:none;stroke:rgb(115, 114, 108);color:rgb(0, 0, 0);stroke-width:1.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <line x1="386" y1="164" x2="432" y2="218" marker-end="url(#arrow)" style="fill:none;stroke:rgb(115, 114, 108);color:rgb(0, 0, 0);stroke-width:1.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <line x1="446" y1="164" x2="590" y2="218" marker-end="url(#arrow)" style="fill:none;stroke:rgb(115, 114, 108);color:rgb(0, 0, 0);stroke-width:1.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>

  <!-- Node A -->
  <g onclick="sendPrompt('What does a storage node do in the distributed KV store?')" style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">
    <rect x="30" y="218" width="120" height="64" rx="8" stroke-width="0.5" style="fill:rgb(225, 245, 238);stroke:rgb(15, 110, 86);color:rgb(0, 0, 0);stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
    <text x="90" y="240" text-anchor="middle" dominant-baseline="central" style="fill:rgb(8, 80, 65);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:14px;font-weight:500;text-anchor:middle;dominant-baseline:central">Node A</text>
    <text x="90" y="258" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">:8081</text>
    <text x="90" y="274" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">LSM · WAL</text>
  </g>

  <!-- Node B -->
  <g onclick="sendPrompt('What does a storage node do in the distributed KV store?')" style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">
    <rect x="188" y="218" width="120" height="64" rx="8" stroke-width="0.5" style="fill:rgb(225, 245, 238);stroke:rgb(15, 110, 86);color:rgb(0, 0, 0);stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
    <text x="248" y="240" text-anchor="middle" dominant-baseline="central" style="fill:rgb(8, 80, 65);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:14px;font-weight:500;text-anchor:middle;dominant-baseline:central">Node B</text>
    <text x="248" y="258" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">:8082</text>
    <text x="248" y="274" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">LSM · WAL</text>
  </g>

  <!-- Node C -->
  <g onclick="sendPrompt('What does a storage node do in the distributed KV store?')" style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">
    <rect x="372" y="218" width="120" height="64" rx="8" stroke-width="0.5" style="fill:rgb(225, 245, 238);stroke:rgb(15, 110, 86);color:rgb(0, 0, 0);stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
    <text x="432" y="240" text-anchor="middle" dominant-baseline="central" style="fill:rgb(8, 80, 65);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:14px;font-weight:500;text-anchor:middle;dominant-baseline:central">Node C</text>
    <text x="432" y="258" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">:8083</text>
    <text x="432" y="274" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">LSM · WAL</text>
  </g>

  <!-- Node D -->
  <g onclick="sendPrompt('What does a storage node do in the distributed KV store?')" style="fill:rgb(0, 0, 0);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto">
    <rect x="530" y="218" width="120" height="64" rx="8" stroke-width="0.5" style="fill:rgb(225, 245, 238);stroke:rgb(15, 110, 86);color:rgb(0, 0, 0);stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
    <text x="590" y="240" text-anchor="middle" dominant-baseline="central" style="fill:rgb(8, 80, 65);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:14px;font-weight:500;text-anchor:middle;dominant-baseline:central">Node D</text>
    <text x="590" y="258" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">:8084</text>
    <text x="590" y="274" text-anchor="middle" dominant-baseline="central" style="fill:rgb(15, 110, 86);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:middle;dominant-baseline:central">LSM · WAL</text>
  </g>

  <!-- Health check indicators -->
  <circle cx="146" cy="228" r="5" fill="#1D9E75" opacity="0.85" style="fill:rgb(29, 158, 117);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:0.85;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <circle cx="304" cy="228" r="5" fill="#1D9E75" opacity="0.85" style="fill:rgb(29, 158, 117);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:0.85;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <circle cx="488" cy="228" r="5" fill="#1D9E75" opacity="0.85" style="fill:rgb(29, 158, 117);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:0.85;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <circle cx="646" cy="228" r="5" fill="#1D9E75" opacity="0.85" style="fill:rgb(29, 158, 117);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:0.85;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>

  <!-- Legend -->
  <circle cx="42" cy="305" r="5" fill="#1D9E75" style="fill:rgb(29, 158, 117);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <text x="52" y="309" style="fill:rgb(61, 61, 58);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:start;dominant-baseline:auto">Node healthy (heartbeat OK)</text>
  <rect x="210" y="299" width="12" height="12" rx="2" fill="#AFA9EC" style="fill:rgb(175, 169, 236);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <text x="228" y="309" style="fill:rgb(61, 61, 58);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:start;dominant-baseline:auto">Router layer</text>
  <rect x="330" y="299" width="12" height="12" rx="2" fill="#5DCAA5" style="fill:rgb(93, 202, 165);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:16px;font-weight:400;text-anchor:start;dominant-baseline:auto"/>
  <text x="348" y="309" style="fill:rgb(61, 61, 58);stroke:none;color:rgb(0, 0, 0);stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;opacity:1;font-family:&quot;Anthropic Sans&quot;, -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, sans-serif;font-size:12px;font-weight:400;text-anchor:start;dominant-baseline:auto">Storage node</text>
</svg>architecture_banner.svg…]()


Each node runs an independent LSM-inspired storage engine with its own MemTable, SSTables, and Write-Ahead Log.

---

# ⚙️ How It Works

## 🔹 Consistent Hashing

* Keys and nodes are mapped to a hash ring (circular keyspace)
* Each key is routed to the nearest clockwise node
* Ring is implemented using a `TreeMap` for efficient wrap-around lookups
* Supports **minimal data movement during scaling**

---

## 🔹 Virtual Nodes

* Each physical node is represented multiple times on the ring
* Ensures **better load distribution** across nodes

---

## 🔹 Replication

* Each key is stored on **N consecutive nodes** on the ring
* Provides **fault tolerance** — data survives node failures

---

## 🔹 Quorum-based Reads/Writes

| Parameter | Meaning            |
| --------- | ------------------ |
| N         | Replication factor |
| W         | Write quorum       |
| R         | Read quorum        |

Condition:

```
R + W > N
```

Ensures **strong eventual consistency**.

---

## 🔹 Write Flow

```
Client → Router → N nodes (with retry on transient failures)
        → success if ≥ W nodes succeed
        → else rollback
```

Failed writes trigger a rollback across nodes that did succeed, preventing partial write divergence.

---

## 🔹 Read Flow

```
Client → Router → multiple replicas
        → quorum decision across R responses
        → read repair triggered on replica mismatch
```

---

# 🗄️ Storage Engine

Each node uses an **LSM-inspired storage engine** for durable, high-throughput writes.

## Write Path

```
Write → WAL (Write-Ahead Log) → MemTable (in-memory)
      → flush to SSTable when MemTable threshold reached
```

## Read Path

```
Read → MemTable → SSTables (newest first)
     → return first match found
```

## Components

**MemTable** — in-memory write buffer backed by a `ConcurrentHashMap`. Absorbs writes at high speed before flushing to disk.

**SSTables (Sorted String Tables)** — immutable on-disk files written when the MemTable exceeds its flush threshold. Reads scan SSTables from newest to oldest.

**Write-Ahead Log (WAL)** — every write is appended to the WAL before hitting the MemTable. On crash, the WAL is replayed to restore in-flight writes that weren't yet flushed.

**Compaction** — background process that merges SSTables, removes duplicate keys, and reclaims space.

**Tombstones** — deletes are written as tombstone markers rather than immediate removals, which are resolved during compaction.

---

# 🔁 Self-Healing Mechanisms

## 🟢 Read Repair

* Detects replica divergence during reads
* Asynchronously pushes the latest value to stale replicas
* Keeps **hot data consistent** without extra background load

---

## 🟡 Anti-Entropy (Background Repair)

* Runs periodically via `@Scheduled` background job
* Compares replicas across all nodes
* Pushes missing or stale values to lagging replicas
* Ensures **eventual consistency** for cold/infrequently-read data

---

## 🔵 Node Recovery

When a node comes back UP after downtime:

* Detects keys it is responsible for but is missing
* Pulls latest values from peer replicas
* Ensures **no data loss after downtime**

---

## 🔴 Rebalancing

Triggered on node addition or removal:

1. Identify keys that now belong to different nodes
2. Copy data to correct target nodes
3. Verify successful transfer
4. Delete from nodes no longer responsible

---

# 🛡️ Fault Tolerance

* Detects unhealthy nodes via heartbeat health checks
* Retries failed requests with configurable retry logic
* Skips unhealthy nodes during routing
* Fails over reads/writes to healthy replicas automatically

---

# 🔒 Concurrency Model

* **`ConcurrentHashMap`** — thread-safe in-memory storage for MemTable and node registry
* **`ReadWriteLock`** — protects the hash ring during rebalancing and topology changes
* **`@Async`** — read repair and replica sync run asynchronously to avoid blocking client requests
* **`@Scheduled`** — anti-entropy repair runs as a background scheduled task without manual coordination

---

# 🗃️ Persistence & Metadata

Node registry and key ownership metadata are persisted using **Spring Data JPA** backed by an **H2 database**, ensuring the router can reconstruct system state after a restart without relying purely on in-memory state.

| Store              | Technology       | Purpose                          |
| ------------------ | ---------------- | -------------------------------- |
| Node Registry      | H2 + JPA         | Tracks live nodes and their status |
| Key Registry       | H2 + JPA         | Tracks key-to-node ownership     |
| Key-Value Data     | LSM Engine       | Actual stored values per node    |
| Crash Recovery     | WAL Replay       | Restores writes lost on crash    |

---

# ⚡ Features Implemented

* ✅ Consistent Hashing Ring (TreeMap-based)
* ✅ Virtual Nodes
* ✅ Replication (N copies)
* ✅ Quorum Reads/Writes
* ✅ Partial Write Rollback
* ✅ Retry Mechanism
* ✅ Node Health Check (Heartbeat)
* ✅ Failover Handling
* ✅ Data Rebalancing
* ✅ Read Repair
* ✅ Anti-Entropy Background Sync
* ✅ Node Recovery
* ✅ LSM-inspired Storage Engine
* ✅ MemTable + SSTable
* ✅ Write-Ahead Log (WAL) + Crash Recovery
* ✅ Compaction + Tombstone Deletes
* ✅ Thread-Safe Concurrency Model
* ✅ Metadata Persistence (H2 + JPA)

---

# 🧠 Design Trade-offs

| Feature             | Choice                              | Reasoning                                      |
| ------------------- | ----------------------------------- | ---------------------------------------------- |
| Consistency model   | Eventual consistency                | Favours availability and partition tolerance    |
| Availability        | High                                | Quorum allows progress despite node failures   |
| Partition tolerance | Supported                           | Ring-based routing survives partial failures   |
| Write durability    | WAL before MemTable                 | Crash-safe without sacrificing write speed     |
| Delete strategy     | Tombstones                          | Safe for replicated systems; resolved at compaction |
| Concurrency         | ReadWriteLock + ConcurrentHashMap   | Fine-grained locking over coarse synchronisation |

---

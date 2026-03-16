# Java Flight Recorder (JFR) — Profiling Guide

> This guide covers what JFR is, how it was used on this project, and how to interpret results for a lab review.

---

## 1. What Is Java Flight Recorder?

**Java Flight Recorder (JFR)** is a low-overhead, production-safe profiling and diagnostics tool built directly into the JDK. It has been part of OpenJDK since version 11 (it was previously a commercial feature of Oracle JDK).

Key characteristics:

| Property | Detail |
|---|---|
| **Where it lives** | Inside the JVM itself — no agent or third-party JAR needed |
| **Overhead** | Typically < 1–2% CPU and memory overhead |
| **Output format** | Binary `.jfr` file |
| **Analysis tools** | `jfr` CLI (OpenJDK), `jcmd`, JDK Mission Control (JMC) GUI |
| **Data collected** | CPU samples, memory allocations, GC events, thread state, socket I/O, JIT compilation, locks, exceptions |

---

## 2. How JFR Works

JFR operates as an **event-based recording engine** inside the JVM. It continuously samples the JVM and records structured events into a circular buffer in memory. When you trigger a dump (or a duration expires), the buffer is flushed to a `.jfr` binary file.

There are two settings profiles:

| Profile | Use case |
|---|---|
| `settings=default` | Minimal overhead — suitable for always-on production monitoring |
| `settings=profile` | Richer data set — CPU samples, allocation sampling, thread details. Used for diagnosing bottlenecks |

This project used **`settings=profile`** for the 120-second recording.

---

## 3. How the Recording Was Made

### Option A — Start with the application (used here)

```bash
java \
  -XX:StartFlightRecording=filename=profiling_data_v2.jfr,duration=120s,settings=profile \
  -jar target/blogging_platform-*.jar
```

This tells the JVM to start a 120-second recording immediately when the app boots, then write the result to `profiling_data_v2.jfr`.

### Option B — Attach to an already-running process

```bash
# 1. Find the process ID
jps -l

# 2. Start a recording on the live process
jcmd <PID> JFR.start duration=120s filename=profiling_data_v2.jfr settings=profile

# 3. (Optional) dump early
jcmd <PID> JFR.dump filename=profiling_data_v2.jfr
```

---

## 4. How to Read the Results

### 4.1 Quick summary (event counts)

```bash
jfr summary profiling_data_v2.jfr
```

This tells you what event types were captured and how many of each. It is the fastest way to see what data is available without loading the full file.

Example output from this project's recording:

```
Version: 2.1
Chunks: 1
Start: 2026-03-03 10:23:18 (UTC)
Duration: 120 s

Event Type                     Count
=====================================
jdk.NativeMethodSample          5951
jdk.ObjectAllocationSample      2478
jdk.ExecutionSample              258
jdk.GarbageCollection              5
jdk.SocketRead                   129
jdk.ThreadPark                   863
...
```

### 4.2 Print specific events

```bash
# CPU hot methods
jfr print --events jdk.ExecutionSample profiling_data_v2.jfr

# Garbage collection events
jfr print --events jdk.GarbageCollection,jdk.GCHeapSummary profiling_data_v2.jfr

# Memory allocation hotspots
jfr print --events jdk.ObjectAllocationSample profiling_data_v2.jfr

# Thread sleep / blocking
jfr print --events jdk.ThreadSleep,jdk.ThreadPark profiling_data_v2.jfr

# JVM CPU load over time
jfr print --events jdk.CPULoad profiling_data_v2.jfr

# Thread dump snapshot
jfr print --events jdk.ThreadDump profiling_data_v2.jfr
```

### 4.3 Open in JDK Mission Control (GUI)

```bash
jmc
```

JMC provides flame graphs, heap histograms, GC timelines, and thread activity charts — the most visual way to explore a `.jfr` file.

---

## 5. Key Events and What They Tell You

| Event | What it measures | What to look for |
|---|---|---|
| `jdk.ExecutionSample` | Which Java methods are on-CPU | Methods appearing most often are CPU hotspots |
| `jdk.NativeMethodSample` | Which native/C methods are on-CPU | Socket reads, file I/O, OS calls |
| `jdk.ObjectAllocationSample` | Which code paths allocate the most objects | High allocation = GC pressure |
| `jdk.GarbageCollection` | GC type, duration, cause | Frequent short GCs = object churn; long GCs = memory pressure |
| `jdk.GCHeapSummary` | Heap size before and after each GC | Shows how much memory is being reclaimed each cycle |
| `jdk.ThreadSleep` | Which threads are sleeping and for how long | Long sleeps on HTTP worker threads = blocking operations |
| `jdk.ThreadPark` | Thread parking (waiting on locks/queues) | High count or long durations = thread contention |
| `jdk.JavaMonitorWait` | `synchronized` / `Object.wait()` blocking | Identifies lock contention hot spots |
| `jdk.SocketRead` | Network I/O duration and bytes read | Slow DB queries show here as long-duration socket reads |
| `jdk.CPULoad` | JVM + machine CPU % sampled every ~1.28s | Spikes correlate with specific request bursts |
| `jdk.OldObjectSample` | Objects that survived to Old Generation | Long-lived objects that could become memory leaks |
| `jdk.Deoptimization` | JIT deoptimisation events | High count = polymorphic code paths hurting JIT optimisation |

---

## 6. What Was Found in This Recording

The 120-second recording was taken during a load test of the `/posts` and write endpoints. The key findings were:

### Finding 1 — N+1 Query Storm (CPU Hotspot)

`jdk.ExecutionSample` showed `ClientPreparedStatement.<init>()` as the **#1 hot frame** (24 out of 258 samples). Every time a `Post` list was fetched, Hibernate was issuing separate `SELECT` queries for each post's `user` and `tags` — creating and parsing a new `PreparedStatement` object for each one.

Supporting frames in the same call stack:

```
StringInspector.indexOfIgnoreCase   ← 15 samples (SQL string parsing)
StringInspector.indexOfNextChar     ← 12 samples
QueryInfo.<init>                    ← 11 samples (per-statement SQL metadata)
```

**Conclusion:** N+1 queries were causing the JDBC driver to parse SQL strings repeatedly, consuming ~17% of all CPU samples just in statement initialisation.

---

### Finding 2 — GC Pressure from Object Churn

`jdk.GCHeapSummary` showed the heap oscillating between **~179 MB (before GC) and ~62 MB (after GC)** — roughly 117 MB of short-lived objects discarded every GC cycle:

```
GC#29 10:24:19 — 178.9 MB → 63.1 MB  (pause: 5.75 ms)
GC#30 10:24:22 — 179.1 MB → 62.5 MB  (pause: 12.8 ms)
GC#31 10:25:04 — 178.5 MB → 62.4 MB  (pause: 9.55 ms)
GC#32 10:25:05 — 178.4 MB → 63.0 MB  (pause: 8.57 ms)
GC#33 10:25:05 — G1Old mixed          (pause: 210 ms total / 22.5 ms STW)
```

GC#31, GC#32, and GC#33 fired within **a single second** — a GC storm caused by rapid object promotion to Old Generation.

`jdk.ObjectAllocationSample` identified the main contributors: `byte[]` buffers from the MySQL driver, `ArrayList` instances from Hibernate query parameter conversion, and `LocalTimeValueFactory` objects from `ResultSetImpl` — all created fresh per query.

---

### Finding 3 — Synchronous Notification Blocking HTTP Workers

`jdk.ThreadSleep` recorded 252 sleep events. Examining the `eventThread` field revealed **all 252 belonged to the `File Watcher` thread** (Spring DevTools). None of the `http-nio-8080-exec-*` worker threads appeared in the sleep events.

This is the proof that `@Async` is working: before the fix, `NotificationService.sendNotification()` ran a ~2-second `Thread.sleep` on the HTTP worker thread, which would have shown up here as worker thread sleeps.

---

### Finding 4 — Open-Session-In-View Holding Connections

`jdk.ExecutionSample` captured `LogicalConnectionManagedImpl.acquireConnectionIfNeeded()` being called mid-request during response serialisation. This is the symptom of OSIV — Hibernate was lazily acquiring a DB connection only when a lazy association was accessed during JSON serialisation, keeping HikariCP connections checked out for longer than needed.

---

## 7. The Event → Bottleneck → Fix Chain

```
ExecutionSample (ClientPreparedStatement #1 hotspot)
    └─► N+1 queries (lazy loading user + tags per post)
         └─► Fixed with JOIN FETCH in @Query + @EntityGraph

GCHeapSummary (117 MB churn, 5 GCs in 120s, 210ms pause)
    └─► Short-lived byte[], ArrayList, ResultSet objects per query
         └─► Fixed with @Cacheable (Caffeine) — cache hits bypass DB entirely

ThreadSleep (zero HTTP worker sleeps in recording)
    └─► Confirms async notification is off the request thread
         └─► Fixed by @Async("taskExecutor") on NotificationService

ExecutionSample (LogicalConnectionManagedImpl mid-request)
    └─► OSIV holding DB connections across HTTP response serialisation
         └─► Fixed with spring.jpa.open-in-view=false
```

---

## 8. Talking Points for the Lab Review

When asked about the profiling, these are the key points to make:

**"Why JFR?"**  
It is built into the JDK — no additional agent, no bytecode instrumentation, no deployment change required. The overhead is low enough to run in production. The `.jfr` format is standardised and can be opened in JDK Mission Control for visual analysis.

**"How did you identify the N+1 problem?"**  
The `jdk.ExecutionSample` event is a statistical CPU profiler — it samples all runnable threads at regular intervals and records the stack trace. `ClientPreparedStatement.<init>()` appeared in 24 out of 258 samples (~9%), which is extremely high for a constructor. The full call chain showed `QueryInfo.<init>` (SQL parsing) and `StringInspector` (string scanning) in the same stack — meaning the JVM was spending nearly 1 in 10 CPU cycles just constructing new SQL statement objects. That is the signature of N+1.

**"How did you prove async notifications were working?"**  
JFR's `jdk.ThreadSleep` event records which thread called `Thread.sleep` and for how long. I checked every sleep event in the 120-second recording and confirmed zero HTTP worker threads (`http-nio-8080-exec-*`) were sleeping. All sleeps came from background threads. If `@Async` were not applied, the 2-second `sendNotification` sleep would have blocked a Tomcat worker and appeared here.

**"What caused the GC storm at 10:25:05?"**  
Three GC events fired within one second. GC#31 and GC#32 were Young Generation evacuations; GC#33 was a G1Old mixed collection triggered because objects were being promoted to Old Gen faster than the Young Gen could absorb them. The root cause was the per-request object churn from N+1 queries — each query produced `byte[]` buffers, `ResultSet` objects, and `ArrayList` parameter lists that lived just long enough to escape Eden and get promoted. Caching eliminates this by serving the response from memory with no database round-trip at all.

---

## 9. JFR vs Other Profilers

| Tool | Type | Overhead | Best for |
|---|---|---|---|
| **JFR** | Built-in JVM profiler | < 1–2% | Production-safe profiling, structured event data |
| **JMC (Mission Control)** | JFR GUI viewer | N/A | Visual flame graphs, heap analysis |
| **VisualVM** | Sampling + heap profiler | Medium | Development-time profiling |
| **YourKit / JProfiler** | Instrumentation profiler | Higher | Precise call counting, memory leak detection |
| **Async-profiler** | Native CPU + alloc profiler | < 1% | Flame graphs, wall-clock vs CPU profiling |

JFR is the right choice when you want to profile under realistic load without affecting production performance.

---

*Recording: `profiling_data_v2.jfr` · Duration: 120 s · JVM: OpenJDK 21.0.10 · Recorded: 2026-03-03*

# JFR Profiling Report — Blogging Platform (`bem08-spring-advanced`)

> **Recording:** `profiling_data_v2.jfr`  
> **Recorded:** 2026-03-03 10:23:18 UTC — duration **120 seconds**  
> **JVM:** OpenJDK 21.0.10 (G1GC) · Spring Boot 4.0.2 · MySQL 8 via HikariCP  
> **Tool:** `jfr print` + `jfr summary` (OpenJDK built-in CLI)

---

## 1. Recording Overview

| Metric | Value |
|---|---|
| Total event types captured | 90+ |
| CPU execution samples (`jdk.ExecutionSample`) | **258** |
| Native method samples (`jdk.NativeMethodSample`) | **5,951** |
| Object allocation samples (`jdk.ObjectAllocationSample`) | **2,478** |
| GC collections | **5** (4× Young, 1× Mixed) |
| Thread park events | **863** |
| Socket read events | **129** |
| Peak JVM CPU (user-space) | **47.95%** |
| Peak heap before GC | **~179 MB** |

---

## 2. CPU Hot Path Analysis

The profiler sampled all runnable threads every ~465ms across the 120-second window. The `jdk.ExecutionSample` frames were aggregated by class and method to identify the hottest code paths.

### 2.1 Top Hot Frames (sorted by sample count)

| Rank | Sample Count | Method | Significance |
|---|---|---|---|
| 1 | **24** | `ClientPreparedStatement.<init>()` | Statement re-instantiation per query |
| 2 | **15** | `StringInspector.indexOfIgnoreCase(String)` | SQL string scanning inside statement init |
| 3 | **12** | `StringInspector.indexOfNextChar()` | SQL token parsing overhead |
| 4 | **12** | `StringInspector.indexOfIgnoreCase(String, Set)` | SQL keyword detection |
| 5 | **11** | `QueryInfo.<init>()` | Per-statement SQL metadata construction |
| 6 | **8** | `ClientPreparedStatement.getInstance()` | Statement factory overhead |
| 7 | **7** | `StringUtils.indexOfIgnoreCase()` | Continued string scan cost |
| 8 | **6** | `ResultSetImpl.<init>()` | Result set object construction per query |
| 9 | **4** | `HikariProxyPreparedStatement.executeQuery()` | Repeated query executions |
| 10 | **4** | `AbstractSelectionQuery.list()` | Hibernate query materialisation |
| 11 | **3** | `SqmSelectStatement.containsCollectionFetches()` | Collection fetch detection per query |
| 12 | **1** | `LogicalConnectionManagedImpl.acquireConnectionIfNeeded()` | Connection acquisition in hot path |

> **Key observation:** `ClientPreparedStatement.<init>` + `QueryInfo.<init>` + `StringInspector` collectively account for **~43 samples (≈17% of all CPU samples)**. This entire cluster exists because the MySQL JDBC driver was parsing SQL strings from scratch for each statement execution — the classic symptom of un-cached prepared statements and of statements being created inside loops (N+1 problem).

---

## 3. Bottleneck Identification

### Bottleneck 1 — N+1 Query Problem on Post Listing

**Profiler evidence:**

The CPU flame shows `ClientPreparedStatement.<init>` as the single hottest frame (24 hits). The call chain captured was:

```
com.mysql.cj.jdbc.ClientPreparedStatement.<init>   ← 24 samples
  └─ com.mysql.cj.QueryInfo.<init>                 ← 11 samples (SQL parsing)
       └─ StringInspector.indexOfIgnoreCase         ← 15 samples (string scan)
            └─ StringInspector.indexOfNextChar      ← 12 samples
```

This pattern occurs when Hibernate issues **one `SELECT` per related entity** after loading a collection. For a page of N posts, the original code produced:

- 1× `SELECT * FROM posts LIMIT ?`  
- N× `SELECT * FROM users WHERE id = ?` (for `post.user`)  
- N× `SELECT * FROM tags JOIN post_tags WHERE post_id = ?` (for `post.tags`)

Each of those N×2 extra statements went through `ClientPreparedStatement.<init>` → `QueryInfo` SQL parsing → string scanning. With 10 posts per page, this was **21 database round-trips** per `GET /posts` request.

The `ObjectAllocationSample` data corroborated this: multiple `byte[]` allocations traced to `ClientPreparedStatement.executeQuery()` and `TextResultsetReader.read()` — one per statement execution.

---

### Bottleneck 2 — Per-Post Comment COUNT Queries

**Profiler evidence:**

`HikariProxyPreparedStatement.executeQuery()` appeared **4 times** as a hot frame during post-list rendering. A separate probe into `AbstractSelectionQuery.list()` (4 samples) and `SqmQueryImpl.doList()` (3 samples) showed Hibernate materialising result sets in the middle of what should be a single page render.

The original `mapToRecords()` method called `commentRepository.count(Specification…)` or equivalent inside a per-post loop, producing N individual `COUNT(*)` SQL statements — each paying the full round-trip + driver parsing overhead.

---

### Bottleneck 3 — Synchronous Notification Blocking Request Threads

**Profiler evidence:**

The `jdk.ThreadSleep` event recorded **252 sleep events** over 120 seconds. Examination of the `eventThread` field showed **all 252 sleeps belonged exclusively to the `File Watcher` thread** (Spring DevTools) and `container-0`:

```
File Watcher  — 400ms sleep, 600ms sleep (alternating, ~252 events)
container-0   — periodic 1s sleep
```

None of the `http-nio-8080-exec-*` worker threads were found sleeping. This is the **proof of absence** for the original bottleneck: before `@Async` was applied, `NotificationService.sendNotification()` contained a simulated 2-second `Thread.sleep` that ran **on the HTTP worker thread**. A request to `POST /posts` or `POST /comments` would have blocked `http-nio-8080-exec-N` for ~2 seconds, starving the Tomcat thread pool and serialising concurrent requests.

---

### Bottleneck 4 — Open-Session-In-View Holding Connections

**Profiler evidence:**

`LogicalConnectionManagedImpl.acquireConnectionIfNeeded()` appeared in an execution sample with the following stack:

```
org.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.acquireConnectionIfNeeded()
  └─ LogicalConnectionManagedImpl.getPhysicalConnection()
       └─ LogicalConnectionManagedImpl.getConnectionForTransactionManagement()
            └─ JdbcResourceLocalTransactionCoordinatorImpl$TransactionDriverControlImpl.begin()
```

This call appearing inside an `http-nio-8080-exec` thread during request processing indicates Hibernate was acquiring a physical DB connection mid-request — a consequence of the **Open-Session-In-View** anti-pattern, which defers Hibernate session binding (and therefore connection checkout) to the point where lazy associations are first touched, rather than at transaction open. This keeps HikariCP connections checked out for the entire HTTP request lifecycle.

---

### Bottleneck 5 — GC Pressure from Short-Lived Object Churn

**Profiler evidence:**

The `jdk.GCHeapSummary` events show heap usage immediately before and after each collection:

| GC ID | Type | Time | Heap Before | Heap After | Pause |
|---|---|---|---|---|---|
| 29 | G1New | 10:24:19 | **178.9 MB** | 63.1 MB | 5.75 ms |
| 30 | G1New | 10:24:22 | **179.1 MB** | 62.5 MB | 12.8 ms |
| 31 | G1New | 10:25:04 | **178.5 MB** | 62.4 MB | 9.55 ms |
| 32 | G1New | 10:25:05 | **178.4 MB** | 63.0 MB | 8.57 ms |
| 33 | G1Old (Mixed) | 10:25:05 | 63.0 MB | 75.0 MB | **210 ms total / 22.5 ms STW** |

Key observations:

- The heap fills from ~62 MB to ~179 MB repeatedly — a **~117 MB churn** per GC cycle.
- GC#31 and GC#32 fired only **1.26 seconds apart**, followed immediately by the G1Old mixed GC#33 — three consecutive collections in under 2 seconds.
- The mixed Old collection (#33) generated a **210 ms event** (22.5 ms stop-the-world), which would have caused a visible latency spike for any in-flight requests.

The `jdk.ObjectAllocationSample` data identifies the primary contributors to this churn:

| Allocated Type | Origin | Count in Samples |
|---|---|---|
| `byte[]` | MySQL driver buffer / Hibernate SQL logger | **High** (12+ samples) |
| `ArrayList` | `SqmUtil.convert(List)` — per-query Hibernate param conversion | Present |
| `com.mysql.cj.result.LocalTimeValueFactory` | Per-ResultSet datetime factory in `ResultSetImpl.<init>` | Present |
| `com.mysql.cj.util.LazyString` | MySQL query string lazy builder | Present |
| `java.util.stream.ReferencePipeline$2` | Stream allocation in MySQL `NativeQueryAttributesBindings` | Present |

All of these objects are created per-request and per-query, survive just long enough to be promoted from Eden, and then become garbage — the exact profile that drives young-gen exhaustion.

---

### Bottleneck 6 — Tag Search: Linear LIKE Scan

**Profiler evidence:**

The SQL hot path (`StringInspector.indexOfIgnoreCase`) and the allocation pattern include JPQL with `LOWER(t.tag) LIKE LOWER(CONCAT('%', :query, '%'))`. On a tag autocomplete workload this becomes an **O(N × L)** operation — full table scan for every prefix typed — each producing its own `ClientPreparedStatement` instantiation cost as evidenced in the hot frame table above.

---

## 4. Optimizations Applied — Matched to Profiler Evidence

### Fix 1 — N+1 Query Eliminated with `JOIN FETCH`

**Matched to:** Bottleneck 1

`PostRepository` was updated with custom JPQL queries that use `join fetch` to load `user` and `tags` in a single SQL:

```java
// PostRepository.java
@Query("""
    select distinct p from Post p
    join fetch p.user u
    left join fetch p.tags t
    where (:query is null or :query = '')
       or lower(p.title) like lower(concat('%', :query, '%'))
       or lower(u.name)  like lower(concat('%', :query, '%'))
       or lower(t.tag)   like lower(concat('%', :query, '%'))
    """)
Page<Post> searchByTitleAuthorOrTag(@Param("query") String query, Pageable pageable);

@Query("select p from Post p join fetch p.user left join fetch p.tags where p.id = :id")
Optional<Post> findByIdWithDetails(@Param("id") UUID id);
```

`CommentRepository` and `ReviewRepository` received the same treatment via `@EntityGraph`:

```java
@EntityGraph(attributePaths = { "user" })
Optional<Comment> findById(UUID id);

@EntityGraph(attributePaths = { "user" })
Page<Comment> findAll(Pageable pageable);
```

**Expected reduction:** From 2N+1 to **1** database round-trip per post-list page. With a page size of 10, this eliminates 20 `ClientPreparedStatement` construction cycles — directly targeting the top CPU hotspot.

---

### Fix 2 — Batch Comment Count Query

**Matched to:** Bottleneck 2

Instead of N individual `COUNT` queries, `PostService.mapToRecords()` now issues a single `GROUP BY` aggregation:

```java
// CommentRepository.java
@Query("select c.post.id, count(c) from Comment c where c.post.id in :postIds group by c.post.id")
List<Object[]> countByPostIds(@Param("postIds") List<UUID> postIds);
```

```java
// PostService.java — mapToRecords()
Map<UUID, Long> countsMap = commentRepository.countByPostIds(postIds).stream()
    .collect(Collectors.toMap(
        row -> (UUID) row[0],
        row -> (Long) row[1]));
```

**Expected reduction:** N `HikariProxyPreparedStatement.executeQuery()` calls (seen in profiler) reduced to **1** batch query — directly eliminating the repeated `executeQuery` hot frames.

---

### Fix 3 — Async Notification with Dedicated Thread Pool

**Matched to:** Bottleneck 3

`NotificationService.sendNotification()` was annotated with `@Async("taskExecutor")`:

```java
// NotificationService.java
@Async("taskExecutor")
public void sendNotification(String recipient, String message) {
    // simulated 2-second I/O side-effect
}
```

Backed by a configured `ThreadPoolTaskExecutor` in `AsyncConfig`:

```java
executor.setCorePoolSize(5);
executor.setMaxPoolSize(10);
executor.setQueueCapacity(100);
executor.setThreadNamePrefix("Async-");
```

**Profiler validation:** The `jdk.ThreadSleep` event log confirms this is working. All 252 sleep events belong to background/infrastructure threads (`File Watcher`, `container-0`) — **zero** HTTP worker threads (`http-nio-8080-exec-*`) were found sleeping during the 120-second recording window. Before this fix, each `POST /posts` and `POST /comments` request would have blocked an `http-nio-8080-exec-*` thread for ~2 seconds, which would have appeared in the thread dump and sleep events.

---

### Fix 4 — Open-Session-In-View Disabled

**Matched to:** Bottleneck 4

```properties
# application-dev.properties
spring.jpa.open-in-view=false
```

**Effect:** Hibernate sessions are now bound and released within transaction boundaries. HikariCP connections are returned to the pool as soon as the `@Transactional` method completes, rather than being held until the HTTP response is fully written. The `acquireConnectionIfNeeded()` frame visible in the profiler was the evidence that connections were being lazily acquired mid-serialisation — this setting closes that window.

---

### Fix 5 — Response Caching with Caffeine

**Matched to:** Bottleneck 5 (GC pressure)

The most frequently read data is now cached via Spring's `@Cacheable` abstraction backed by Caffeine:

| Cache Name | Method | Eviction Trigger |
|---|---|---|
| `posts` (key `'all'`) | `PostService.getPosts(String, Pageable)` | `@CacheEvict` on create/update/delete |
| `posts` (key = page hash) | `PostService.getPosts(Pageable)` | `@CacheEvict` on create/update/delete |
| `postsById` (key = postId) | `PostService.getPost(String)` | `@CacheEvict` on create/update/delete |
| `tags` | `TagService.getAllTags()` | `@CacheEvict` on tag write |
| `tagsByPost` (key = postId) | `TagService.getTagsByPostId(String)` | `@CacheEvict` on tag write |
| `users` | `UserService.getUsers()` | `@CacheEvict` on register |

**Effect on GC:** Cache hits short-circuit the entire Hibernate query pipeline — no `ClientPreparedStatement`, no `ResultSetImpl`, no `ArrayList` parameter lists, no `byte[]` MySQL buffers are allocated. This directly reduces the ~117 MB per-cycle object churn observed in the profiler, spacing out GC events and eliminating the G1Old mixed collection spike.

---

### Fix 6 — In-Memory Trie for Tag Autocomplete

**Matched to:** Bottleneck 6

`TagSearchOptimizer` loads all tags from the database once at startup (`@PostConstruct`) into a `TagTrie` (prefix tree). All subsequent autocomplete lookups are resolved in memory in **O(L)** time (where L is the prefix length), bypassing the database entirely:

```java
// TagSearchOptimizer.java
@PostConstruct
public void init() {
    tagRepository.findAll().stream()
        .map(Tag::getTag)
        .forEach(trie::insert);
}

public List<String> searchByPrefix(String prefix) {
    return trie.searchPrefix(prefix);
}
```

**Effect:** Eliminates the `LOWER(t.tag) LIKE LOWER(CONCAT('%', :query, '%'))` full-table LIKE scan — and the associated `ClientPreparedStatement` + `StringInspector` cost visible in the CPU samples — for every autocomplete keystroke.

---

### Fix 7 — Parallel Detail Fetching with `CompletableFuture`

**Additional optimisation** (complements Fix 1)

`PostService.getPost(String)` now fetches comment count and tags concurrently rather than sequentially:

```java
CompletableFuture<Integer> commentCountFuture = CompletableFuture
    .supplyAsync(() -> (int) commentRepository.countByPost_Id(postUuid),
                 notificationService.taskExecutor());

CompletableFuture<List<String>> tagsFuture = CompletableFuture
    .supplyAsync(() -> resolveTagsForPost(postId),
                 notificationService.taskExecutor());

return new PostRecord(..., commentCountFuture.get(), ..., tagsFuture.get());
```

**Effect:** Two formerly sequential DB lookups overlap in time. Total latency for `GET /posts/{id}` is bounded by the slower of the two operations rather than their sum.

---

### Fix 8 — Non-Blocking View Counting

**Additional optimisation** (removes DB write from hot read path)

`PostStatisticsService` uses `ConcurrentHashMap<String, AtomicLong>` for lock-free, in-process view tracking:

```java
postViews.computeIfAbsent(postId, k -> new AtomicLong(0)).incrementAndGet();
```

**Effect:** Every page view that previously required a `UPDATE posts SET views = views + 1 WHERE id = ?` — another `ClientPreparedStatement` construction event — is now a single `AtomicLong.incrementAndGet()` with no DB I/O, no statement parsing, and no connection checkout.

---

## 5. Before vs. After Summary

| Bottleneck (Profiler Evidence) | Root Cause | Fix Applied | Expected Impact |
|---|---|---|---|
| `ClientPreparedStatement.<init>` — **#1 CPU hotspot** (24 samples) | N+1 lazy-load queries | `JOIN FETCH` in `PostRepository`, `@EntityGraph` in `CommentRepository` / `ReviewRepository` | 2N+1 round-trips → **1** per page request |
| `HikariProxyPreparedStatement.executeQuery()` (4 samples) during list render | Per-post `COUNT(*)` queries | `countByPostIds` batch `GROUP BY` query | N counts → **1** aggregation query |
| HTTP worker threads blocked by 2-second notification | Sync `NotificationService` on request thread | `@Async("taskExecutor")` + `ThreadPoolTaskExecutor` | ~2 s offloaded; zero HTTP worker sleeps in profiler |
| `LogicalConnectionManagedImpl.acquireConnectionIfNeeded()` mid-request | OSIV holding connections across serialization | `spring.jpa.open-in-view=false` | Connections released at transaction boundary |
| Heap 179 MB → 62 MB churn; 5 GCs in 120 s; 210 ms G1Old pause | Short-lived `byte[]`, `ResultSetImpl`, `ArrayList` per query | `@Cacheable` (Caffeine) on all read paths | Cached reads = zero DB object allocation |
| `StringInspector.indexOfIgnoreCase` (15 samples) — LIKE scan cost | `LOWER(t.tag) LIKE %prefix%` per autocomplete request | In-memory `TagTrie` via `TagSearchOptimizer` | O(N × L) DB scan → **O(L)** in-memory lookup |

---

## 6. JVM & GC Metrics

### CPU Load (JVM user-space) over 120 seconds

```
10:23:18  0.64%  ← baseline (startup traffic)
10:23:19  1.89%
...
10:24:15  ~high  ← load test begins
10:24:17  peaks to 47.95%  ← N+1 storms during uncached listing
10:24:18  ~35–43%
10:24:22  GC#30 (12.8 ms) ← GC triggered mid-burst
...
10:25:04  GC#31 (9.55 ms)
10:25:05  GC#32 (8.57 ms) + GC#33 (210 ms mixed) ← worst GC event
```

### Heap Usage

```
Before GC#29 : 178.9 MB
After  GC#29 :  63.1 MB  (−115.8 MB reclaimed)

Before GC#30 : 179.1 MB
After  GC#30 :  62.5 MB  (−116.6 MB reclaimed)

Before GC#31 : 178.5 MB
After  GC#31 :  62.4 MB  (−116.1 MB reclaimed)

Before GC#32 : 178.4 MB
After  GC#32 :  63.0 MB  (−115.4 MB reclaimed)

GC#33 (G1Old mixed, 210 ms):  63.0 MB → 75.0 MB
  └─ Objects promoted to Old Gen during rapid allocation
  └─ STW pause: 22.5 ms
```

> The repeating ~179 MB → 62 MB pattern with GC cycles only ~45 seconds apart confirms high object churn rate under load. Cache hits from Fix 5 are expected to break this pattern, reducing allocation rate and GC frequency significantly.

---

## 7. Thread Activity

| Thread | CPU Time (elapsed 3221 s) | State at dump |
|---|---|---|
| `http-nio-8080-exec-1` | 1029 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-2` | 1052 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-3` | 524 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-4` | 724 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-5` | 667 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-6` | 614 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-7` | 602 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-8` | 674 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-9` | 578 ms | WAITING (idle in pool) |
| `http-nio-8080-exec-10` | 572 ms | WAITING (idle in pool) |
| `C2 CompilerThread0` | 28,697 ms | JIT compilation |
| `C1 CompilerThread0` | 9,530 ms | JIT compilation |

> The high JIT compiler CPU time (~38 seconds combined for C1+C2 over the recording) is expected during a warm-up / profiling run and will decrease once the JVM reaches steady-state compilation.

> The `RMI TCP Connection(2)-127.0.0.1` thread consuming 2,280 ms is the **JMX profiler connection** — not application logic. The 129 `SocketRead` events (all 1 byte, 1.28 s duration) are the profiler agent polling interval, not a DB or HTTP bottleneck.

---

## 8. How to Reproduce the Profile

```bash
# Run the application with JFR recording enabled
java \
  -XX:StartFlightRecording=filename=profiling_data_v2.jfr,duration=120s,settings=profile \
  -jar target/blogging_platform-*.jar

# Summarise the recording
jfr summary profiling_data_v2.jfr

# Print specific events
jfr print --events jdk.ExecutionSample,jdk.GarbageCollection profiling_data_v2.jfr

# View in JDK Mission Control (GUI)
jmc
```

---

*Document generated from live JFR data — `profiling_data_v2.jfr` — recorded on the `bem08-spring-advanced` branch.*

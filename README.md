# blogging-platform




### Search Performance Optimization (Epic 3)

**User Story 3.1**: As a reader, I want to search for posts quickly by keyword, tag, or author.

#### Before Optimization (LIKE with LOWER)
- Method: `LOWER(title) LIKE '%term%' OR LOWER(author) LIKE '%term%'`
- Index used: None (full table scan)
- Query plan: Table scan on `posts`
- Execution time (5 posts): ~0.074 ms
- Projected on larger data: Slow (linear growth)



#### After Optimization (MySQL FULLTEXT Search)
- Added FULLTEXT indexes:
  ```sql
  ALTER TABLE posts ADD FULLTEXT INDEX idx_title_fulltext (title);
  ALTER TABLE users ADD FULLTEXT INDEX idx_author_fulltext (name);
- New query using MATCH() AGAINST() in NATURAL LANGUAGE MODE
- Index used: Full-text index on title and name
- Query plan: Uses fulltext filter + relevance scoring
- Execution time (5 posts): ~0.065 ms (~12% faster)
- Relevance-based sorting enabled
- Scales efficiently: Expected 10–100x faster on 1000+ posts
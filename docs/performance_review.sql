EXPLAIN ANALYZE SELECT 
      BIN_TO_UUID(p.id) AS id,
      p.title,
      p.content,
      p.status,
      p.published_datetime,
      COALESCE(u.name, 'Unknown') AS author
   FROM posts p
   LEFT JOIN users u ON p.user_id = u.id
   WHERE p.status = 'PUBLISHED'
   AND (LOWER(p.title) LIKE 'Greatness is not gotten by'
      OR LOWER(u.name) LIKE 'Greatness is not gotten by') OR 'Greatness is not gotten by' IS NULL
   ORDER BY p.published_datetime DESC;

-> Filter: ((lower(p.title) like 'Greatness is not gotten by') or (lower(u.`name`) like 'Greatness is not gotten by'))  (cost=1.38 rows=4) (actual time=0.0747..0.0747 rows=0 loops=1)
    -> Nested loop left join  (cost=1.38 rows=4) (actual time=0.0653..0.0702 rows=4 loops=1)
        -> Sort: p.published_datetime DESC  (cost=0.65 rows=4) (actual time=0.0538..0.0548 rows=4 loops=1)
            -> Filter: (p.`status` = 'PUBLISHED')  (cost=0.65 rows=4) (actual time=0.0283..0.0364 rows=4 loops=1)
                -> Table scan on p  (cost=0.65 rows=4) (actual time=0.0261..0.0332 rows=5 loops=1)
        -> Single-row index lookup on u using PRIMARY (id=p.user_id)  (cost=0.325 rows=1) (actual time=0.00338..0.00341 rows=1 loops=4)




   EXPLAIN ANALYZE SELECT 
      BIN_TO_UUID(p.id) AS id,
      p.title,
      p.content,
      p.status,
      p.published_datetime,
      COALESCE(u.name, 'Unknown') AS author,
      MATCH(p.title) AGAINST ('Greatness is not gotten by' IN NATURAL LANGUAGE MODE) AS title_score,
      MATCH(u.name) AGAINST ('Greatness is not gotten by' IN NATURAL LANGUAGE MODE) AS author_score
   FROM posts p
   LEFT JOIN users u ON p.user_id = u.id
   WHERE p.status = 'PUBLISHED'
   AND (MATCH(p.title) AGAINST ('Greatness is not gotten by' IN NATURAL LANGUAGE MODE)  > 0 
      OR MATCH(u.name) AGAINST ('Greatness is not gotten by' IN NATURAL LANGUAGE MODE) > 0)
   ORDER BY (title_score + author_score) DESC;


   -> Sort: `(title_score + author_score)` DESC  (actual time=0.0654..0.0655 rows=1 loops=1)
    -> Stream results  (cost=1.33 rows=1.67) (actual time=0.0417..0.0551 rows=1 loops=1)
        -> Filter: (((match p.title against ('Greatness is not gotten by')) > 0) or ((match u.`name` against ('Greatness is not gotten by')) > 0))  (cost=1.33 rows=1.67) (actual time=0.0315..0.0445 rows=1 loops=1)
            -> Nested loop left join  (cost=1.33 rows=1.67) (actual time=0.023..0.0415 rows=4 loops=1)
                -> Filter: (p.`status` = 'PUBLISHED')  (cost=0.75 rows=1.67) (actual time=0.0142..0.0247 rows=4 loops=1)
                    -> Table scan on p  (cost=0.75 rows=5) (actual time=0.0123..0.0211 rows=5 loops=1)
                -> Single-row index lookup on u using PRIMARY (id=p.user_id)  (cost=0.31 rows=1) (actual time=0.00375..0.00379 rows=1 loops=4)

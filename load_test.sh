BASE="http://localhost:8080/api/v1"

# ── 1. Register users ──────────────────────────────────────────
curl -s -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Author","email":"alice@test.com","password":"password123","role":"AUTHOR"}' | jq .

curl -s -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob Reader","email":"bob@test.com","password":"password123","role":"READER"}' | jq .

# ── 2. Login and grab token ────────────────────────────────────
TOKEN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"password123"}' | jq -r '.data.accessToken')

echo "Token: $TOKEN"

# ── 3. Create posts (repeat to generate load) ──────────────────
for i in $(seq 1 10); do
  curl -s -X POST "$BASE/posts" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"title\":\"Post $i\",\"content\":\"Content for post $i\",\"status\":\"published\"}" | jq .status
done

# ── 4. Hit GET /posts repeatedly (this is the N+1 hotspot) ─────
for i in $(seq 1 20); do
  curl -s "$BASE/posts?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN" | jq '.message'
done

# ── 5. Get a single post ───────────────────────────────────────
POST_ID=$(curl -s "$BASE/posts" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.data[0].id')

curl -s "$BASE/posts/$POST_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .

# ── 6. Add comments ───────────────────────────────────────────
READER_TOKEN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@test.com","password":"password123"}' | jq -r '.data.accessToken')

for i in $(seq 1 5); do
  curl -s -X POST "$BASE/comments?postId=$POST_ID" \
    -H "Authorization: Bearer $READER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"comment\":\"Comment $i on this post\"}" | jq .status
done

# ── 7. Add a review ────────────────────────────────────────────
curl -s -X POST "$BASE/reviews?postId=$POST_ID" \
  -H "Authorization: Bearer $READER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"rating":4,"message":"Great post!"}' | jq .

# ── 8. Hit GET /posts again after data exists (heavier N+1) ───
for i in $(seq 1 20); do
  curl -s "$BASE/posts?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN" | jq '.message'
done
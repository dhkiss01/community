// Explicitly name a disposable local Elasticsearch container.
import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
const base = "http://localhost:18080";
const container = process.env.ELASTICSEARCH_CONTAINER;
if (!container)
  throw Error(
    "ELASTICSEARCH_CONTAINER에 로컬 테스트용 Elasticsearch 컨테이너 이름을 지정해주세요.",
  );
const suffix = Date.now().toString(36);
const email = `recovery-${suffix}@example.com`;
let token;
async function api(path, method = "GET", data, expected = 200) {
  const res = await fetch(base + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: data ? JSON.stringify(data) : undefined,
  });
  const body = await res.json();
  assert.equal(res.status, expected, JSON.stringify(body));
  return body.data;
}
await api(
  "/users",
  "POST",
  { email, nickname: "복구" + suffix, password: "Baseball1!" },
  201,
);
token = (await api("/auth", "POST", { email, password: "Baseball1!" })).token
  .accessToken;
let post;
try {
  execFileSync("docker", ["stop", container], { stdio: "pipe" });
  post = await api(
    "/posts",
    "POST",
    {
      title: `복구 검증 ${suffix}`,
      content: "검색 엔진이 멈춘 동안에도 글을 저장합니다.",
    },
    201,
  );
  await api(
    "/posts/search?keyword=" + encodeURIComponent("복구"),
    "GET",
    undefined,
    503,
  );
} finally {
  execFileSync("docker", ["start", container], { stdio: "pipe" });
}
try {
  let found = false;
  for (let i = 0; i < 60; i++) {
    try {
      found = (
        await api(
          "/posts/search?keyword=" + encodeURIComponent("복구") + "&size=100",
        )
      ).some((p) => p.id === post.id);
    } catch {}
    if (found) break;
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }
  assert.ok(found, "검색 엔진 복구 후 outbox의 게시글이 색인되어야 합니다.");
  console.log(
    "PASS: 검색 장애 중 글 저장, 검색 503, 검색 엔진 복구 후 outbox 자동 반영",
  );
} finally {
  if (post) await api(`/posts/${post.id}`, "DELETE");
}

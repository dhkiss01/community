import assert from "node:assert/strict";
const base = process.env.API_URL || "http://localhost:18080";
if (!["localhost", "127.0.0.1"].includes(new URL(base).hostname))
  throw Error("로컬 테스트 서버를 지정해주세요.");
const key = Date.now().toString(36);
const email = `auth-${key}@example.com`;
async function request(path, { method = "POST", data, cookie } = {}) {
  const res = await fetch(base + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(cookie ? { Cookie: cookie } : {}),
    },
    body: data ? JSON.stringify(data) : undefined,
  });
  const body = await res.json();
  const setCookie = res.headers.get("set-cookie");
  return {
    status: res.status,
    body,
    cookie: setCookie?.split(";")[0],
    setCookie,
  };
}
assert.equal(
  (
    await request("/users", {
      data: { email, password: "Baseball1!", nickname: "인증" + key },
    })
  ).status,
  201,
);
const credentials = { email, password: "Baseball1!" };
const login = await request("/auth", { data: credentials });
assert.equal(login.status, 200);
assert.equal(login.body.data.token.expiresIn, 600);
const concurrent = await Promise.all([
  request("/users/token/refresh", { cookie: login.cookie }),
  request("/users/token/refresh", { cookie: login.cookie }),
]);
assert.deepEqual(concurrent.map((r) => r.status).sort(), [200, 401]);
const rotated = concurrent.find((r) => r.status === 200);
assert.notEqual(rotated.cookie, login.cookie);
assert.equal(rotated.body.data.expiresIn, 600);
const logout = await request("/auth", {
  method: "DELETE",
  cookie: rotated.cookie,
});
assert.equal(logout.status, 200);
assert.match(logout.setCookie, /Max-Age=0/);
assert.equal(
  (await request("/users/token/refresh", { cookie: rotated.cookie })).status,
  401,
);
const logins = await Promise.all([
  request("/auth", { data: credentials }),
  request("/auth", { data: credentials }),
]);
assert.deepEqual(
  logins.map((r) => r.status),
  [200, 200],
);
const sessions = await Promise.all(
  logins.map((r) => request("/users/token/refresh", { cookie: r.cookie })),
);
assert.deepEqual(sessions.map((r) => r.status).sort(), [200, 401]);
await request("/auth", {
  method: "DELETE",
  cookie: sessions.find((r) => r.status === 200).cookie,
});
console.log(
  "PASS: 만료시간 단위, 동시 토큰 재발급, 이전 토큰 재사용 차단, 로그아웃, 동시 로그인",
);

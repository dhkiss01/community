import { test, expect } from "@playwright/test";

test("a fan can sign up, write, search, comment, edit and delete on desktop and mobile", async ({
  page,
}) => {
  await page.goto("/html/index.html");
  await expect(page).toHaveTitle("더그아웃");
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  const key = Date.now().toString(36);
  await page.goto("/html/index.html");
  await expect(page.getByRole("heading", { name: "전체글" })).toBeVisible();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBeTruthy();
  await page.getByRole("searchbox").fill("없는검색어" + key);
  await page.getByRole("button", { name: "검색", exact: true }).click();
  await expect(page.getByText("검색 결과가 없습니다.")).toBeVisible();
  await page.getByRole("button", { name: "초기화" }).click();
  await page.getByRole("link", { name: "글쓰기", exact: true }).click();
  await expect(page).toHaveURL(/login/);
  await page
    .locator("main")
    .getByRole("link", { name: "회원가입", exact: true })
    .click();
  await page.getByLabel("닉네임", { exact: false }).fill("팬" + key);
  await page
    .getByLabel("이메일", { exact: true })
    .fill(`fan-${key}@example.com`);
  await page.getByLabel("비밀번호", { exact: true }).fill("Baseball1!");
  await page.getByLabel("비밀번호 확인", { exact: true }).fill("Baseball1!");
  await page.getByRole("button", { name: "회원가입" }).click();
  await expect(page).toHaveURL(/index/);
  await page.getByRole("link", { name: "글쓰기", exact: true }).click();
  await page.getByLabel("제목", { exact: false }).fill("한화 직관 " + key);
  await page
    .getByLabel("내용", { exact: true })
    .fill("응원석에서 본 불펜 이야기. <script>alert(1)</script>");
  await page.getByRole("button", { name: "글 등록" }).click();
  await expect(
    page.getByRole("heading", { name: "한화 직관 " + key }),
  ).toBeVisible();
  await expect(page).toHaveTitle("더그아웃");
  expect(await page.locator("body").innerText()).not.toMatch(/[\u00b7\u318d\u2022]/);
  const postUrl = page.url();
  await page.getByRole("button", { name: /공감/ }).click();
  await expect(page.getByRole("button", { name: /공감/ })).toHaveText("공감 1");
  await page.getByLabel("댓글 내용").fill("멋진 경기였어요");
  await page.getByRole("button", { name: "댓글 등록" }).click();
  await expect(page.getByText("멋진 경기였어요")).toBeVisible();
  await page.getByRole("link", { name: "수정", exact: true }).click();
  await page.getByLabel("제목", { exact: false }).fill("한화 불펜 " + key);
  await page.getByRole("button", { name: "수정 완료" }).click();
  await expect(
    page.getByRole("heading", { name: "한화 불펜 " + key }),
  ).toBeVisible();
  // Poll the asynchronous search index through the UI.
  await expect(async () => {
    await page.goto("/html/index.html?q=" + encodeURIComponent("한화 불펜"));
    await expect(
      page.getByRole("link", { name: "한화 불펜 " + key, exact: true }),
    ).toBeVisible({ timeout: 1500 });
  }).toPass({ timeout: 15000 });
  await page.goto(postUrl);
  page.on("dialog", (dialog) => dialog.accept());
  await page.getByRole("button", { name: "삭제", exact: true }).first().click();
  await expect(page).toHaveURL(/index/);
  expect(errors).toEqual([]);
});

test("search errors are visible and retryable", async ({ page }) => {
  await page.route("**/api/posts**", (route) =>
    route.fulfill({
      status: 503,
      contentType: "application/json",
      body: JSON.stringify({ message: "검색을 잠시 사용할 수 없습니다." }),
    }),
  );
  await page.goto("/html/index.html?q=한화");
  await expect(page.getByText("게시글을 불러오지 못했습니다.")).toBeVisible();
  await expect(page.getByRole("button", { name: "다시 시도" })).toBeVisible();
});

test("board tabs filter saved categories without turning them into search terms", async ({
  page,
  request,
}) => {
  const key = Date.now().toString(36);
  const email = `board-${key}@example.com`;
  await request.post("/api/users", {
    data: { email, password: "Baseball1!", nickname: "탭" + key },
  });
  const login = await (
    await request.post("/api/auth", { data: { email, password: "Baseball1!" } })
  ).json();
  const headers = { Authorization: "Bearer " + login.data.token.accessToken };
  const ids = [];
  try {
    for (const category of ["REVIEW", "ANALYSIS"]) {
      const result = await request.post("/api/posts", {
        headers,
        data: {
          title: `분류검증 ${category} ${key}`,
          content: "제목에 게시판 검색어가 없는 글입니다.",
          category,
        },
      });
      expect(result.status()).toBe(201);
      ids.push((await result.json()).data.id);
    }
    await page.goto("/html/index.html");
    await page
      .getByRole("navigation", { name: "게시판" })
      .getByRole("link", { name: "직관 이야기" })
      .click();
    await expect(page).toHaveURL(/category=REVIEW/);
    expect(new URL(page.url()).searchParams.has("q")).toBeFalsy();
    await expect(
      page.getByRole("heading", { name: "직관 이야기", exact: true }),
    ).toBeVisible();
    await expect(
      page.getByRole("link", { name: `분류검증 REVIEW ${key}`, exact: true }),
    ).toBeVisible();
    await expect(
      page.getByRole("link", { name: `분류검증 ANALYSIS ${key}`, exact: true }),
    ).toHaveCount(0);
    await page
      .getByRole("navigation", { name: "게시판" })
      .getByRole("link", { name: "경기 분석" })
      .click();
    await expect(
      page.getByRole("link", { name: `분류검증 ANALYSIS ${key}`, exact: true }),
    ).toBeVisible();
    await expect(
      page.getByRole("link", { name: `분류검증 REVIEW ${key}`, exact: true }),
    ).toHaveCount(0);
    await expect
      .poll(async () => {
        const result = await request.get(
          "/api/posts/search?keyword=" +
            encodeURIComponent("분류검증") +
            "&category=ANALYSIS",
        );
        return (await result.json()).data?.map((post) => post.id) || [];
      })
      .toContain(ids[1]);
    await page.getByRole("searchbox").fill("분류검증");
    await page.getByRole("button", { name: "검색", exact: true }).click();
    await expect(
      page.getByRole("link", { name: `분류검증 ANALYSIS ${key}`, exact: true }),
    ).toBeVisible();
    await expect(
      page.getByRole("link", { name: `분류검증 REVIEW ${key}`, exact: true }),
    ).toHaveCount(0);
    await page.getByRole("button", { name: "초기화" }).click();
    await expect(page).toHaveURL(/category=ANALYSIS/);
    await expect(
      page.getByRole("heading", { name: "경기 분석", exact: true }),
    ).toBeVisible();
    expect(
      await page.evaluate(
        () => document.documentElement.scrollWidth <= innerWidth,
      ),
    ).toBeTruthy();
    // Choosing a board before composing carries the category into the form.
    await page.evaluate(
      (token) => localStorage.setItem("accessToken", token),
      login.data.token.accessToken,
    );
    await page.reload();
    await page.getByRole("link", { name: "글쓰기", exact: true }).click();
    await expect(
      page.getByRole("combobox", { name: "게시판", exact: true }),
    ).toHaveValue("ANALYSIS");
    await expect(
      page.getByRole("option", { name: "직관 이야기" }),
    ).toHaveAttribute("value", "REVIEW");
  } finally {
    for (const id of ids) await request.delete(`/api/posts/${id}`, { headers });
  }
});

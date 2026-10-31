import { test, expect } from "@playwright/test";

test("a fan can sign up, write, search, comment, edit and delete on desktop and mobile", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  const key = Date.now().toString(36);
  await page.goto("/html/index.html");
  await expect(
    page.getByRole("heading", { name: "우리들의 야구 이야기" }),
  ).toBeVisible();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBeTruthy();
  await page.getByRole("searchbox").fill("없는검색어" + key);
  await page.getByRole("button", { name: "검색", exact: true }).click();
  await expect(page.getByText("아직 이 이야기는 없네요.")).toBeVisible();
  await page.getByRole("button", { name: "초기화" }).click();
  await page.getByRole("link", { name: "글쓰기", exact: true }).click();
  await expect(page).toHaveURL(/login/);
  await page.getByRole("link", { name: "회원가입", exact: true }).click();
  await page.getByLabel("닉네임", { exact: false }).fill("팬" + key);
  await page
    .getByLabel("이메일", { exact: true })
    .fill(`fan-${key}@example.com`);
  await page.getByLabel("비밀번호", { exact: true }).fill("Baseball1!");
  await page.getByLabel("비밀번호 확인", { exact: true }).fill("Baseball1!");
  await page.getByRole("button", { name: "더그아웃 함께하기" }).click();
  await expect(page).toHaveURL(/index/);
  await page.getByRole("link", { name: "글쓰기", exact: true }).click();
  await page.getByLabel("제목", { exact: false }).fill("한화 직관 " + key);
  await page
    .getByLabel("내용", { exact: true })
    .fill("응원석에서 본 불펜 이야기. <script>alert(1)</script>");
  await page.getByRole("button", { name: "이야기 올리기" }).click();
  await expect(
    page.getByRole("heading", { name: "한화 직관 " + key }),
  ).toBeVisible();
  const postUrl = page.url();
  await page.getByRole("button", { name: /공감/ }).click();
  await expect(page.getByRole("button", { name: /공감/ })).toHaveText("공감 1");
  await page.getByLabel("댓글 내용").fill("멋진 경기였어요");
  await page.getByRole("button", { name: "댓글 남기기" }).click();
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
  await expect(page.getByText("이야기를 불러오지 못했어요.")).toBeVisible();
  await expect(page.getByRole("button", { name: "다시 시도" })).toBeVisible();
});

const API = window.__APP_CONFIG__?.API_BASE_URL || "/api";
const main = document.querySelector("#main");
const page = document.body.dataset.page;
const params = new URLSearchParams(location.search);
const boards = {
  FREE: "자유게시판",
  REVIEW: "직관 이야기",
  ANALYSIS: "경기 분석",
};
const boardUrl = (category) =>
  "/html/index.html" + (boards[category] ? "?category=" + category : "");
const selectedBoard = () =>
  boards[new URLSearchParams(location.search).get("category")]
    ? new URLSearchParams(location.search).get("category")
    : "";
const teams = [
  "LG",
  "한화",
  "삼성",
  "KIA",
  "SSG",
  "KT",
  "롯데",
  "NC",
  "두산",
  "키움",
];
let user = null;
let refreshPromise;
const escape = (value) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const icon = (name, size = 18) => {
  const paths = {
    pencil: '<path d="m15 4 5 5M4 20l4-1L20 7a2 2 0 0 0-3-3L5 16z"/>',
    image:
      '<rect x="3" y="4" width="18" height="16" rx="1"/><circle cx="8" cy="9" r="1.5"/><path d="m4 18 6-6 4 4 3-3 4 5"/>',
    search: '<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 4.5 4.5"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    ball: '<circle cx="12" cy="12" r="9"/><path d="M6 5c6 4 6 10 0 14M18 5c-6 4-6 10 0 14"/>',
    arrow: '<path d="m14 6-6 6 6 6"/>',
  };
  return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" aria-hidden="true">${paths[name] || paths.ball}</svg>`;
};
const date = (value) => {
  if (!value) return "";
  const d = new Date(value);
  return Number.isNaN(d.getTime())
    ? ""
    : new Intl.DateTimeFormat("ko-KR", {
        month: "2-digit",
        day: "2-digit",
      }).format(d);
};
const count = (n) => new Intl.NumberFormat("ko-KR").format(Number(n) || 0);
const avatar = (nickname) =>
  `<span class="avatar" aria-hidden="true">${escape((nickname || "팬").slice(0, 1))}</span>`;
const safeImage = (value) => {
  try {
    const url = new URL(value);
    return ["https:", "http:"].includes(url.protocol) ? escape(url.href) : "";
  } catch {
    return "";
  }
};
function toast(message) {
  const element = document.querySelector("#toast");
  element.textContent = message;
  element.classList.add("visible");
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => element.classList.remove("visible"), 3500);
}
async function request(
  path,
  { method = "GET", data, signal, retry = true } = {},
) {
  const token = localStorage.getItem("accessToken");
  const response = await fetch(API + path, {
    method,
    signal,
    credentials: "include",
    headers: {
      ...(data ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...(data ? { body: JSON.stringify(data) } : {}),
  });
  const body = await response.json().catch(() => null);
  if (response.status === 401 && token && retry) {
    refreshPromise ||= fetch(API + "/users/token/refresh", {
      method: "POST",
      credentials: "include",
    })
      .then(async (res) => {
        const body = await res.json();
        if (!res.ok) throw Error("다시 로그인해주세요.");
        localStorage.setItem("accessToken", body.data.accessToken);
      })
      .finally(() => {
        refreshPromise = null;
      });
    try {
      await refreshPromise;
      return request(path, { method, data, signal, retry: false });
    } catch {
      localStorage.removeItem("accessToken");
      user = null;
      renderHeader();
    }
  }
  if (!response.ok) {
    const error = new Error(
      body?.message ||
        (response.status === 401
          ? "로그인이 필요합니다."
          : "요청을 처리하지 못했습니다. 다시 시도해주세요."),
    );
    error.status = response.status;
    throw error;
  }
  return body?.data ?? body;
}
function renderHeader() {
  const links = [["", "전체글"], ...Object.entries(boards)];
  document.querySelector("#site-header").className = "site-header";
  document.querySelector("#site-header").innerHTML = `<div class="header-inner">
    <a class="brand" href="/" aria-label="더그아웃 홈"><img class="brand-wordmark" src="/public/dugout-wordmark.svg?v=20261031-4" alt="더그아웃" width="148" height="35"></a>
    <div class="account">${user ? `<a href="/html/modifyInfo.html">${escape(user.nickname)}</a><button id="logout" type="button">로그아웃</button>` : '<a href="/html/login.html">로그인</a><a href="/html/signup.html">회원가입</a>'}</div>
    </div><div class="nav-band"><nav class="top-nav" aria-label="게시판">${links.map(([value, label]) => `<a href="${boardUrl(value)}" ${page === "home" && selectedBoard() === value ? 'class="active" aria-current="page"' : ""}>${label}</a>`).join("")}</nav></div>`;
  document.querySelector("#logout")?.addEventListener("click", () => {
    localStorage.removeItem("accessToken");
    location.assign("/");
  });
}
async function loadUser() {
  if (!localStorage.getItem("accessToken")) return;
  try {
    const id = await request("/auth");
    user = await request(`/users/${id}`);
  } catch {
    localStorage.removeItem("accessToken");
  }
}
function loginUrl() {
  return (
    "/html/login.html?next=" +
    encodeURIComponent(location.pathname + location.search)
  );
}
function requireLogin() {
  if (user) return true;
  location.assign(loginUrl());
  return false;
}
function state(title, description, action = "") {
  return `<div class="list-state"><h3>${escape(title)}</h3><p>${escape(description)}</p>${action}</div>`;
}
function postRow(post) {
  return `<article class="post-row">
    <span class="post-id">${post.id}</span><span class="post-category">${boards[post.category] || boards.FREE}</span>
    <div class="post-title"><h3><a href="/html/board.html?id=${post.id}" aria-label="${escape(post.title)}">${escape(post.title)}</a></h3><a class="comment-count" href="/html/board.html?id=${post.id}#comments" aria-label="댓글 ${count(post.commentCount)}개">[${count(post.commentCount)}]</a>${post.image ? '<span class="attachment-mark" aria-label="첨부 이미지">' + icon("image", 13) + "</span>" : ""}</div>
    <div class="post-stats"><span class="post-author">${escape(post.nickname || "야구팬")}</span><time class="post-date" datetime="${escape(post.createdAt)}">${date(post.createdAt)}</time><span class="post-views"><span class="stat-label">조회 </span>${count(post.viewCount)}</span><span class="post-likes"><span class="stat-label">공감 </span>${count(post.likeCount)}</span></div>
  </article>`;
}
function forumSidebar(category) {
  const links = [["", "전체글"], ...Object.entries(boards)];
  return `<aside class="forum-sidebar" aria-label="게시판 이용 메뉴"><section class="member-panel">${user ? `<strong>${escape(user.nickname)}</strong><a class="side-profile" href="/html/modifyInfo.html">내 정보</a><a class="side-write" aria-label="새 글 작성" href="/html/board-write.html${category ? "?category=" + category : ""}">글쓰기</a>` : '<p>글과 댓글 작성은 로그인 후 이용할 수 있습니다.</p><div class="member-links"><a href="/html/login.html">로그인</a><a href="/html/signup.html">회원가입</a></div>'}</section><section class="side-boards"><h2>커뮤니티</h2>${links.map(([value, label]) => `<a href="${boardUrl(value)}" class="${category === value ? "selected" : ""}">${label}<span aria-hidden="true">›</span></a>`).join("")}</section><section class="side-info"><h2>게시판 이용 안내</h2><p>게시글에 맞는 게시판을 선택해주세요.<br>직관 후기와 좌석 정보는 직관 이야기,<br>경기와 선수 기록은 경기 분석에 남겨주세요.</p></section></aside>`;
}
async function home() {
  let category = selectedBoard();
  let keyword = params.get("q") || "";
  let sort = params.get("sort") === "recent" ? "recent" : "relevance";
  let currentPage = 0,
    loading = false,
    controller,
    generation = 0;
  main.className = "page";
  main.innerHTML = `<div class="forum-layout"><section class="board-section" aria-labelledby="feed-title">
    <div class="breadcrumb">커뮤니티 <span aria-hidden="true">/</span> <span id="board-crumb">${boards[category] || "전체글"}</span></div>
    <div class="section-title"><h1 id="feed-title">${boards[category] || "전체글"}</h1><a id="write-link" class="button primary" href="/html/board-write.html${category ? "?category=" + category : ""}">${icon("pencil", 14)} 글쓰기</a></div>
    <div class="team-row" aria-label="구단 이름으로 검색"><button class="chip" type="button" data-team="" aria-pressed="${!keyword}">전체</button>${teams.map((team) => `<button class="chip" type="button" data-team="${team}" aria-pressed="${keyword === team}">${team}</button>`).join("")}</div>
    <div class="list-toolbar"><div class="result-context"><h3 id="result-title"></h3><button id="clear-search" class="text-button" type="button" hidden>초기화</button><label><span class="sr-only">검색 결과 정렬</span><select id="sort" aria-label="검색 결과 정렬"><option value="relevance">관련도순</option><option value="recent">최신순</option></select></label></div>
    <form id="search-form" role="search" class="search"><input id="search-input" name="q" type="search" maxlength="100" placeholder="제목, 내용 검색" aria-label="게시글 검색" value="${escape(keyword)}"><button type="submit">검색</button></form></div>
    <div class="table-head" aria-hidden="true"><span>번호</span><span>게시판</span><span>제목</span><span>글쓴이</span><span>날짜</span><span>조회</span><span>공감</span></div>
    <div id="feed" aria-live="polite" aria-busy="true"></div><button id="more" class="load-more" type="button" hidden>게시글 더 보기</button><p id="end-note" class="end-note" hidden>마지막 게시글입니다.</p></section><div id="sidebar-slot">${forumSidebar(category)}</div></div>`;
  const feed = document.querySelector("#feed"),
    more = document.querySelector("#more");
  function context() {
    document.querySelector("#feed-title").textContent =
      boards[category] || "전체글";
    document.querySelector("#write-link").href =
      "/html/board-write.html" + (category ? "?category=" + category : "");
    document.title = "더그아웃";
    renderHeader();
    document.querySelector("#board-crumb").textContent =
      boards[category] || "전체글";
    document.querySelector("#sidebar-slot").innerHTML = forumSidebar(category);
    document.querySelector("#result-title").textContent = keyword
      ? `“${keyword}” 검색 결과`
      : "최신 게시글";
    document.querySelector("#clear-search").hidden = !keyword;
    document.querySelector("#sort").value = keyword ? sort : "recent";
    document.querySelector("#sort").disabled = !keyword;
    document
      .querySelectorAll("[data-team]")
      .forEach((button) =>
        button.setAttribute(
          "aria-pressed",
          String(button.dataset.team === keyword),
        ),
      );
  }
  async function load(reset = false) {
    if (loading && !reset) return;
    if (reset) {
      controller?.abort();
      generation++;
      currentPage = 0;
      feed.innerHTML =
        '<div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div>';
    }
    const run = generation;
    loading = true;
    controller = new AbortController();
    more.hidden = true;
    document.querySelector("#end-note").hidden = true;
    feed.setAttribute("aria-busy", "true");
    context();
    try {
      const query = new URLSearchParams({
        page: currentPage,
        size: 10,
        ...(category ? { category } : {}),
        ...(keyword ? { keyword, sort } : {}),
      });
      const posts = await request(
        `/posts${keyword ? "/search" : ""}?${query}`,
        { signal: controller.signal },
      );
      if (run !== generation) return;
      if (reset) feed.innerHTML = "";
      if (!posts.length && !currentPage)
        feed.innerHTML = state(
          keyword ? "검색 결과가 없습니다." : "등록된 게시글이 없습니다.",
          keyword
            ? "다른 검색어를 입력하거나 검색 조건을 초기화하세요."
            : "이 게시판에 첫 글을 작성해보세요.",
          `<a class="button small" href="${keyword ? boardUrl(category) : "/html/board-write.html" + (category ? "?category=" + category : "")}">${keyword ? "검색 초기화" : "글쓰기"}</a>`,
        );
      else feed.insertAdjacentHTML("beforeend", posts.map(postRow).join(""));
      currentPage++;
      more.hidden = posts.length < 10;
      document.querySelector("#end-note").hidden =
        (posts.length === 0 && currentPage === 1) || posts.length === 10;
    } catch (error) {
      if (error.name === "AbortError" || run !== generation) return;
      if (reset) feed.innerHTML = "";
      feed.insertAdjacentHTML(
        "beforeend",
        state(
          "게시글을 불러오지 못했습니다.",
          error.message,
          '<button id="retry" class="button small" type="button">다시 시도</button>',
        ),
      );
      document.querySelector("#retry").addEventListener("click", () => {
        feed.querySelector(".list-state")?.remove();
        load(currentPage === 0);
      });
    } finally {
      if (run === generation) {
        loading = false;
        feed.setAttribute("aria-busy", "false");
      }
    }
  }
  function search(value) {
    keyword = value.trim();
    document.querySelector("#search-input").value = keyword;
    const query = new URLSearchParams({
      ...(category ? { category } : {}),
      ...(keyword ? { q: keyword, sort } : {}),
    });
    history.pushState(
      null,
      "",
      location.pathname + (query.size ? "?" + query : ""),
    );
    load(true);
  }
  document.querySelector("#search-form").addEventListener("submit", (event) => {
    event.preventDefault();
    search(new FormData(event.currentTarget).get("q"));
  });
  document
    .querySelector("#clear-search")
    .addEventListener("click", () => search(""));
  document
    .querySelectorAll("[data-team]")
    .forEach((button) =>
      button.addEventListener("click", () => search(button.dataset.team)),
    );
  document.querySelector("#sort").addEventListener("change", (event) => {
    sort = event.target.value;
    search(keyword);
  });
  window.addEventListener("popstate", () => {
    const query = new URLSearchParams(location.search);
    category = boards[query.get("category")] ? query.get("category") : "";
    keyword = query.get("q") || "";
    sort = query.get("sort") || "relevance";
    document.querySelector("#search-input").value = keyword;
    load(true);
  });
  more.addEventListener("click", () => load());
  await load(true);
}
async function submitForm(form, action) {
  const error = form.querySelector('[role="alert"]');
  const button = form.querySelector('button[type="submit"]');
  error.textContent = "";
  button.disabled = true;
  try {
    await action(Object.fromEntries(new FormData(form)));
  } catch (e) {
    error.textContent = e.message;
    error.focus();
  } finally {
    button.disabled = false;
  }
}
async function auth(signup = false) {
  main.className = "auth-page";
  main.innerHTML = `<h1>${signup ? "회원가입" : "로그인"}</h1><section class="surface"><form id="auth-form" class="form-stack">${signup ? '<label>닉네임<input name="nickname" required maxlength="10" autocomplete="nickname" placeholder="닉네임을 입력해주세요"><small>공백 없이 10자까지 사용할 수 있어요.</small></label>' : ""}<label>이메일<input name="email" type="email" required maxlength="40" autocomplete="email" placeholder="you@example.com"></label><label>비밀번호<input name="password" aria-label="비밀번호" type="password" required minlength="8" maxlength="20" autocomplete="${signup ? "new-password" : "current-password"}" placeholder="비밀번호를 입력해주세요">${signup ? "<small>8~20자, 영문 대, 소문자, 숫자, 특수문자를 포함해주세요.</small>" : ""}</label>${signup ? '<label>비밀번호 확인<input name="confirmation" type="password" required autocomplete="new-password" placeholder="비밀번호를 한 번 더 입력해주세요"></label>' : ""}<p class="form-error" role="alert" tabindex="-1"></p><button class="button primary" type="submit">${signup ? "회원가입" : "로그인"}</button></form></section><p class="auth-switch">${signup ? '이미 계정이 있나요? <a href="/html/login.html">로그인</a>' : '아직 회원이 아닌가요? <a href="/html/signup.html">회원가입</a>'}</p>`;
  document.querySelector("#auth-form").addEventListener("submit", (event) => {
    event.preventDefault();
    submitForm(event.currentTarget, async (data) => {
      if (signup) {
        if (data.password !== data.confirmation)
          throw Error("비밀번호 확인이 일치하지 않습니다.");
        await request("/users", {
          method: "POST",
          data: {
            email: data.email,
            password: data.password,
            nickname: data.nickname,
          },
        });
      }
      const result = await request("/auth", {
        method: "POST",
        data: { email: data.email, password: data.password },
      });
      localStorage.setItem("accessToken", result.token.accessToken);
      const next = params.get("next");
      location.assign(
        next && next.startsWith("/html/") && !next.includes("\\")
          ? next
          : "/html/index.html",
      );
    });
  });
}
async function post() {
  const id = params.get("id");
  if (!/^\d+$/.test(id)) {
    main.innerHTML = state(
      "게시글을 찾을 수 없어요.",
      "목록에서 이야기를 다시 선택해주세요.",
      '<a class="button" href="/">목록으로</a>',
    );
    return;
  }
  main.className = "page narrow";
  main.innerHTML = '<div class="skeleton"></div>';
  try {
    const data = await request(`/posts/${id}`);
    document.title = "더그아웃";
    main.innerHTML = `<a class="back-link" href="/html/index.html">${icon("arrow", 14)} 전체글</a><article class="surface"><h1 class="post-heading">${escape(data.title)}</h1><div class="post-meta"><span class="post-author">${avatar(data.nickname)}${escape(data.nickname)}</span><time datetime="${escape(data.createdAt)}">${date(data.createdAt)}</time><span>조회 ${count(data.viewCount)}</span></div><div class="article-content">${escape(data.content)}</div>${safeImage(data.image) ? `<img class="article-image" src="${safeImage(data.image)}" alt="게시글 첨부 이미지" loading="lazy">` : ""}<div class="post-actions"><button class="button" id="like" type="button">공감 <span>${count(data.likeCount)}</span></button>${user?.id === data.userId ? `<div><a class="button small" href="/html/board-modify.html?id=${id}">수정</a><button class="button small danger" id="delete-post" type="button">삭제</button></div>` : '<a class="text-button" href="/html/index.html">목록으로</a>'}</div><section id="comments" class="comments"><h2>댓글 <span id="comment-total"></span></h2><div id="comment-list" aria-live="polite"></div>${user ? '<form id="comment-form" class="comment-form"><label for="comment-input" class="sr-only">댓글 내용</label><textarea id="comment-input" name="comment" required maxlength="1000" placeholder="댓글을 입력해주세요."></textarea><p class="form-error" role="alert" tabindex="-1"></p><div class="form-footer"><small>댓글은 최대 1,000자까지 작성할 수 있습니다.</small><button class="button primary small" type="submit">댓글 등록</button></div></form>' : `<div class="list-state"><p>댓글을 작성하려면 로그인해주세요.</p><a class="button small" href="${loginUrl()}">로그인</a></div>`}</section></article>`;
    async function comments() {
      const items = await request(`/posts/${id}/comments`);
      document.querySelector("#comment-total").textContent = items.length;
      document.querySelector("#comment-list").innerHTML = items.length
        ? items
            .map(
              (c) =>
                `<article class="comment"><header>${avatar(c.nickname)}<strong>${escape(c.nickname)}</strong><time>${date(c.createdAt)}</time>${user?.id === c.userId ? `<div class="comment-actions"><button class="text-button" data-edit-comment="${c.id}" type="button">수정</button><button class="text-button" data-delete-comment="${c.id}" type="button">삭제</button></div>` : ""}</header><p>${escape(c.comment)}</p></article>`,
            )
            .join("")
        : '<p class="end-note">등록된 댓글이 없습니다.</p>';
      document.querySelectorAll("[data-delete-comment]").forEach((button) =>
        button.addEventListener("click", async () => {
          if (!confirm("이 댓글을 삭제할까요?")) return;
          button.disabled = true;
          try {
            await request(
              `/posts/${id}/comments/${button.dataset.deleteComment}`,
              { method: "DELETE" },
            );
            await comments();
          } catch (e) {
            toast(e.message);
            button.disabled = false;
          }
        }),
      );
      document.querySelectorAll("[data-edit-comment]").forEach((button) =>
        button.addEventListener("click", async () => {
          const c = items.find(
            (c) => c.id === Number(button.dataset.editComment),
          );
          const text = prompt("댓글 수정", c.comment);
          if (text === null || text === c.comment) return;
          if (!text.trim() || text.length > 1000) {
            toast("댓글은 1~1,000자로 입력해주세요.");
            return;
          }
          try {
            await request(`/posts/${id}/comments/${c.id}`, {
              method: "PATCH",
              data: { comment: text },
            });
            await comments();
          } catch (e) {
            toast(e.message);
          }
        }),
      );
    }
    await comments();
    document
      .querySelector("#comment-form")
      ?.addEventListener("submit", (event) => {
        event.preventDefault();
        submitForm(event.currentTarget, async (values) => {
          await request(`/posts/${id}/comments`, {
            method: "POST",
            data: values,
          });
          event.target.reset();
          await comments();
          toast("댓글을 남겼어요.");
        });
      });
    // The server owns like state; use the conflict response to undo an existing like.
    document.querySelector("#like").addEventListener("click", async (event) => {
      if (!requireLogin()) return;
      const button = event.currentTarget;
      button.disabled = true;
      try {
        let likes;
        try {
          likes = await request(`/posts/${id}/likes`, { method: "POST" });
        } catch (e) {
          if (e.status !== 409) throw e;
          likes = await request(`/posts/${id}/likes`, { method: "DELETE" });
        }
        button.querySelector("span").textContent = count(likes);
      } catch (e) {
        toast(e.message);
      } finally {
        button.disabled = false;
      }
    });
    document
      .querySelector("#delete-post")
      ?.addEventListener("click", async () => {
        if (
          !confirm("게시글과 댓글을 삭제할까요? 삭제 후에는 되돌릴 수 없어요.")
        )
          return;
        try {
          await request(`/posts/${id}`, { method: "DELETE" });
          location.assign("/");
        } catch (e) {
          toast(e.message);
        }
      });
    if (location.hash === "#comments")
      document.querySelector("#comments").scrollIntoView();
  } catch (e) {
    main.innerHTML = state(
      "게시글을 열지 못했습니다.",
      e.message,
      '<a class="button small" href="/">전체글로</a>',
    );
  }
}
async function editor(edit = false) {
  if (!requireLogin()) return;
  const id = params.get("id");
  const existing = edit ? await request(`/posts/${id}`) : {};
  if (edit && existing.userId !== user.id)
    throw Error("작성자만 수정할 수 있습니다.");
  const draftKey = `dugout-draft-${user.id}`;
  let draft = {};
  if (!edit) {
    try {
      draft = JSON.parse(sessionStorage.getItem(draftKey)) || {};
    } catch {}
  }
  const values = edit ? existing : draft;
  main.className = "page narrow";
  main.innerHTML = `<a class="back-link" href="/html/index.html">${icon("arrow", 14)} 전체글</a><section class="surface editor"><h1>${edit ? "게시글 수정" : "글쓰기"}</h1><form id="editor" class="form-stack"><label>게시판<select name="category" aria-label="게시판" required>${Object.entries(
    boards,
  )
    .map(
      ([value, label]) =>
        `<option value="${value}" ${value === (values.category || selectedBoard() || "FREE") ? "selected" : ""}>${label}</option>`,
    )
    .join(
      "",
    )}</select></label><label>제목<input name="title" required maxlength="26" value="${escape(values.title)}" placeholder="제목을 입력해주세요"><small>최대 26자</small></label><label>내용<textarea name="content" required maxlength="20000" placeholder="내용을 입력해주세요.">${escape(values.content)}</textarea></label><label>사진 주소 <small>선택 사항 ,  공개된 http 또는 https 이미지 주소를 사용할 수 있어요.</small><input name="image" type="url" maxlength="200" value="${escape(values.image)}" placeholder="https://..."></label><p class="form-error" role="alert" tabindex="-1"></p><div class="form-footer"><small id="draft-status">${edit ? "변경 사항을 확인한 후 저장해주세요." : "작성 중인 내용은 이 탭에 임시 보관돼요."}</small><div><a class="button quiet" href="${edit ? "/html/board.html?id=" + id : "/"}">취소</a> <button class="button primary" type="submit">${edit ? "수정 완료" : "글 등록"}</button></div></div></form></section>`;
  let dirty = false;
  const form = document.querySelector("#editor");
  form.addEventListener("input", () => {
    dirty = true;
    if (!edit) {
      sessionStorage.setItem(
        draftKey,
        JSON.stringify(Object.fromEntries(new FormData(form))),
      );
      document.querySelector("#draft-status").textContent =
        "이 탭에 임시 보관했어요.";
    }
  });
  window.addEventListener("beforeunload", (event) => {
    if (dirty) {
      event.preventDefault();
      event.returnValue = "";
    }
  });
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    submitForm(form, async (data) => {
      if (!data.title.trim() || !data.content.trim())
        throw Error("제목과 내용을 입력해주세요.");
      if (data.image && !safeImage(data.image))
        throw Error("http 또는 https 이미지 주소를 입력해주세요.");
      const result = await request(edit ? `/posts/${id}` : "/posts", {
        method: edit ? "PATCH" : "POST",
        data,
      });
      dirty = false;
      if (!edit) sessionStorage.removeItem(draftKey);
      location.assign(`/html/board.html?id=${result.id}`);
    });
  });
}
async function profile() {
  if (!requireLogin()) return;
  main.className = "auth-page";
  main.innerHTML = `<h1>내 정보</h1><p class="intro">닉네임과 프로필 사진을 변경할 수 있습니다.</p><section class="surface"><form id="profile-form" class="form-stack"><label>이메일<input class="readonly" value="${escape(user.email)}" readonly></label><label>닉네임<input name="nickname" required maxlength="10" value="${escape(user.nickname)}"></label><label>프로필 사진 주소<input name="image" type="url" maxlength="200" value="${escape(user.image)}" placeholder="https://..."></label><p class="form-error" role="alert" tabindex="-1"></p><button class="button primary" type="submit">변경 사항 저장</button></form></section>`;
  document
    .querySelector("#profile-form")
    .addEventListener("submit", (event) => {
      event.preventDefault();
      submitForm(event.currentTarget, async (data) => {
        if (data.image && !safeImage(data.image))
          throw Error("http 또는 https 이미지 주소를 입력해주세요.");
        user = await request(`/users/${user.id}`, { method: "PATCH", data });
        renderHeader();
        toast("내 정보를 저장했어요.");
      });
    });
}
renderHeader();
await loadUser();
renderHeader();
try {
  await (
    {
      home,
      post,
      login: () => auth(),
      signup: () => auth(true),
      write: () => editor(),
      edit: () => editor(true),
      profile,
    }[page] || home
  )();
} catch (e) {
  main.className = "page narrow";
  main.innerHTML = state(
    "잠시 후 다시 시도해주세요.",
    e.message,
    '<a class="button" href="/">홈으로</a>',
  );
}

// Optional local fixtures. Does not run automatically or use real match results.
const base = process.env.API_URL || "http://localhost:18080";
if (!["localhost", "127.0.0.1"].includes(new URL(base).hostname))
  throw Error("샘플 데이터는 로컬에서만 넣을 수 있습니다.");
async function api(path, method, data, token) {
  const result = await fetch(base + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: data ? JSON.stringify(data) : undefined,
  });
  const body = await result.json();
  if (!result.ok) throw Error(body.message);
  return body.data;
}
const password = "DugoutDemo1!";
const email = "demo@dugout.local";
try {
  await api("/users", "POST", { email, password, nickname: "더그아웃지기" });
} catch (e) {
  if (!e.message.includes("중복")) throw e;
}
const { token, user } = await api("/auth", "POST", { email, password });
const legacyTitles = [
  "처음 직관 가는 친구에게 해주고 싶은 말",
  "한화 불펜, 어떤 기록부터 보면 좋을까요?",
  "유니폼에 남은 첫 야구장의 기억",
  "LG 응원석에서 만난 새로운 친구들",
  "야구 분석을 시작하는 나만의 방법",
  "롯데 원정 직관 준비물 같이 챙겨요",
];
const posts = [
  [
    "잠실 1루 내야석, 해가 많이 드나요?",
    "잠실은 처음 가보려고 합니다. 오후 경기 기준으로 1루 내야에서 그늘이 생기는 구역이 궁금합니다. 다녀오신 분들 좌석 추천 부탁드립니다.",
  ],
  [
    "한화 불펜 기록은 어떤 지표로 보시나요?",
    "평균자책점과 함께 볼넷, 탈삼진 비율을 보고 있습니다. 불펜 투수를 비교할 때 추가로 참고할 만한 지표가 있을까요?",
  ],
  [
    "유니폼 사이즈는 크게 사는 편인가요?",
    "온라인으로 처음 구매하려고 합니다. 안에 후드를 입을 생각이면 평소 티셔츠보다 한 치수 크게 고르는 게 나을까요?",
  ],
  [
    "LG 응원석 예매, 구역 추천 부탁해요",
    "응원단상 근처와 통로 쪽 사이에서 고민 중입니다. 시야와 이동 편의성 기준으로 추천해주시면 감사하겠습니다.",
  ],
  [
    "선발투수 비교할 때 어떤 기록 보세요?",
    "승패보다는 이닝과 탈삼진, 볼넷을 먼저 보려고 합니다. 표본이 적을 때 어떤 기록을 주의해서 봐야 하는지도 궁금합니다.",
  ],
  [
    "사직 원정은 지하철로 가는 게 낫나요?",
    "부산 원정을 계획하고 있습니다. 경기 끝나고 이동할 때 주차장보다 지하철이 편한지, 숙소는 어디 쪽을 잡는지 궁금합니다.",
  ],
];
const existing = await api("/posts?size=100", "GET");
const categories = [
  "REVIEW",
  "ANALYSIS",
  "FREE",
  "REVIEW",
  "ANALYSIS",
  "REVIEW",
];
for (const [index, [title, content]] of posts.entries()) {
  const category = categories[index];
  const current = existing.find(
    (p) => p.title === title || p.title === legacyTitles[index],
  );
  if (!current)
    await api(
      "/posts",
      "POST",
      { title, content, category },
      token.accessToken,
    );
  else if (
    current.userId === user.id &&
    (current.category !== category ||
      current.title !== title ||
      current.content !== content)
  )
    await api(
      `/posts/${current.id}`,
      "PATCH",
      { title, content, category },
      token.accessToken,
    );
}
console.log("로컬 샘플 게시글을 준비했습니다.");

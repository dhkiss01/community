// Optional local fixtures. Does not run automatically or use real match results.
const base = process.env.API_URL || 'http://localhost:18080';
if (!['localhost', '127.0.0.1'].includes(new URL(base).hostname)) throw Error('샘플 데이터는 로컬에서만 넣을 수 있습니다.');
async function api(path, method, data, token) {
    const result = await fetch(base + path, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, body: data ? JSON.stringify(data) : undefined });
    const body = await result.json();
    if (!result.ok) throw Error(body.message);
    return body.data;
}
const password = 'DugoutDemo1!';
const email = 'demo@dugout.local';
try { await api('/users', 'POST', { email, password, nickname: '더그아웃지기' }); } catch (e) { if (!e.message.includes('중복')) throw e; }
const { token, user } = await api('/auth', 'POST', { email, password });
const posts = [
    ['처음 직관 가는 친구에게 해주고 싶은 말', '경기 시작 전에 조금 일찍 가보세요. 구장 한 바퀴를 돌고 연습하는 선수들을 보는 것도 직관의 즐거움이에요. 여러분만의 직관 팁도 궁금합니다.'],
    ['한화 불펜, 어떤 기록부터 보면 좋을까요?', '평균자책점뿐 아니라 볼넷과 탈삼진 비율도 같이 보고 있어요. 숫자 뒤의 이야기를 함께 나눠보고 싶습니다.'],
    ['유니폼에 남은 첫 야구장의 기억', '옷장 속 오래된 유니폼을 꺼내보니 첫 직관의 응원 소리가 떠올랐어요. 가장 아끼는 야구 물건이 있나요?'],
    ['LG 응원석에서 만난 새로운 친구들', '같은 팀을 응원한다는 이유 하나로 금방 친해졌어요. 야구장에는 일상에서 찾기 어려운 반가움이 있는 것 같아요.'],
    ['야구 분석을 시작하는 나만의 방법', '경기를 보며 궁금했던 장면 하나를 적고, 기록을 찾아보는 것부터 시작했어요. 여러분은 어떤 지표를 즐겨 보나요?'],
    ['롯데 원정 직관 준비물 같이 챙겨요', '모바일 티켓과 보조 배터리는 꼭 챙기고 있어요. 계절마다 달라지는 준비물도 댓글로 알려주세요.'],
];
const existing = await api('/posts?size=100', 'GET');
const categories = ['REVIEW', 'ANALYSIS', 'FREE', 'REVIEW', 'ANALYSIS', 'REVIEW'];
for (const [index, [title, content]] of posts.entries()) {
  const category = categories[index];
  const current = existing.find(p => p.title === title);
  if (!current) await api('/posts', 'POST', {title, content, category}, token.accessToken);
  else if (current.userId === user.id && current.category !== category) await api(`/posts/${current.id}`, 'PATCH', {title, content, category}, token.accessToken);
}
console.log('로컬 샘플 게시글을 준비했습니다. demo@dugout.local / DugoutDemo1!');

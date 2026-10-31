import assert from 'node:assert/strict';
const base = process.env.API_URL || 'http://localhost:18080';
const suffix = Date.now().toString(36);
let token;
async function api(path, method = 'GET', data, expected = 200) {
    const res = await fetch(base + path, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, body: data ? JSON.stringify(data) : undefined });
    const body = await res.json();
    assert.equal(res.status, expected, JSON.stringify(body));
    return body.data;
}
async function eventually(check) {
    for (let i = 0; i < 40; i++) { if (await check()) return; await new Promise(resolve => setTimeout(resolve, 500)); }
    throw Error('검색 색인이 제한 시간 안에 동기화되지 않았습니다.');
}
const email = `integration-${suffix}@example.com`;
await api('/users', 'POST', { email, password: 'Baseball1!', nickname: '검증' + suffix }, 201);
token = (await api('/auth', 'POST', { email, password: 'Baseball1!' })).token.accessToken;
const post = await api('/posts', 'POST', { title: `LG 불펜 ${suffix}`, content: '엘지 투수 이야기와 직관 후기' }, 201);
try {
    await eventually(async () => (await api('/posts/search?keyword=' + encodeURIComponent('엘지 불펜') + '&size=100')).some(p => p.id === post.id));
    await api(`/posts/${post.id}`, 'PATCH', { title: `한화 직관 ${suffix}`, content: '이글스 응원 후기' });
    await eventually(async () => (await api('/posts/search?keyword=' + encodeURIComponent('이글스 직관') + '&sort=recent')).some(p => p.id === post.id));
    await eventually(async () => !(await api('/posts/search?keyword=' + encodeURIComponent('엘지 불펜') + '&size=100')).some(p => p.id === post.id));
    await Promise.all(Array.from({ length: 30 }, () => api(`/posts/${post.id}`)));
    await Promise.all(Array.from({ length: 8 }, (_, i) => api(`/posts/${post.id}/comments`, 'POST', { comment: `응원 ${i}` }, 201)));
    assert.equal((await api(`/posts/${post.id}/comments`)).length, 8);
    const latest = (await api('/posts?size=100')).find(p => p.id === post.id);
    assert.equal(latest.viewCount, 30, '동시 조회수 증가가 유실되면 안 됩니다.');
    assert.equal(latest.commentCount, 8, '동시 댓글 카운트 증가가 유실되면 안 됩니다.');
    await api(`/posts/${post.id}/likes`, 'POST');
    await api(`/posts/${post.id}/likes`, 'POST', undefined, 409);
} finally { await api(`/posts/${post.id}`, 'DELETE'); }
await eventually(async () => !(await api('/posts/search?keyword=' + encodeURIComponent('한화 직관') + '&size=100')).some(p => p.id === post.id));
console.log('PASS: Nori/구단 약칭 검색, 수정, 삭제 색인 동기화, 동시 조회수 30회, 댓글 8개, 중복 공감 방지');

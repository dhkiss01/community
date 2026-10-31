## 변경

기존 커뮤니티를 야구 팬을 위한 더그아웃으로 개편했습니다. 비회원도 게시글을 읽고 검색할 수 있고, 글, 댓글 작성 시 로그인합니다. 모바일 대응, 검색 조건 URL 유지, 빈 결과와 오류 안내, 글쓰기 임시 보관을 추가했습니다. 게시판을 자유게시판, 직관 이야기, 경기 분석으로 분리하고 선택한 게시판 안에서 검색합니다. 배너와 홍보 문구를 제거하고 남색, 파랑, 흰색 배색으로 정리했습니다.

Elasticsearch Nori 검색과 구단 약칭 동의어를 추가했습니다. 게시글 변경과 같은 트랜잭션에 outbox를 기록해 비동기로 색인을 갱신합니다. 검색, 댓글의 작성자 N+1, 조회수 갱신 유실, 회원 정보 수정 권한 누락, 댓글과 URL 게시글 불일치, 댓글이 있는 글의 삭제 실패도 수정했습니다.

## 검증

- Java 21 Gradle 테스트 및 bootJar
- JPA 목록, 검색, 댓글 조회가 각각 SQL 1회인지 검증
- 실제 MySQL 8.4 / Elasticsearch 8.18.5에서 한국어, 약칭 검색과 수정, 삭제 동기화, 동시 요청 검증
- 프런트엔드 서버 테스트 및 Playwright 데스크톱, 모바일 시나리오

자세한 결과: docs/testing.md

## 병합 전 적용

기존 DB에 community-BE/src/main/resources/db/의 search-outbox.sql, community-upgrade.sql, post-category.sql 적용 → 운영 Elasticsearch/Nori 준비 → search 프로필 및 환경변수 설정 → 초기 backfill 순으로 진행합니다. local 프로필은 운영에 사용하지 않습니다. main 병합 시 기존 배포 워크플로가 실행됩니다.

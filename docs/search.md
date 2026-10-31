# 검색 구성과 운영

MySQL이 원본 데이터입니다. Elasticsearch에는 게시글 ID, 제목, 내용, 작성 시각만 저장합니다. 검색 결과의 ID를 한 번의 fetch join으로 조회해서 최신 작성자 정보와 카운트를 반환하고, 검색 순서는 유지합니다.

## 준비 순서

1. `community-BE/src/main/resources/db/search-outbox.sql`과 `community-BE/src/main/resources/db/community-upgrade.sql`을 기존 DB에 적용합니다. 두 번째 파일은 본문과 댓글 저장 길이를 확장합니다. `post-category.sql`도 적용해 게시판 분류 컬럼을 추가합니다. 기존 게시글은 자유게시판(FREE)에 속합니다. 새 로컬 Compose DB에는 자동 적용됩니다. 이미 만들어진 볼륨에는 init SQL이 다시 실행되지 않습니다.
2. Nori 플러그인을 설치한 Elasticsearch 8.18.5를 준비합니다. 버전은 Spring Boot 3.5.5가 관리하는 Java 클라이언트에 맞췄습니다.
3. `search` 프로필을 활성화하고 다음 값을 설정합니다.

| 설정 | 의미 |
| --- | --- |
| `ELASTICSEARCH_URIS` | Elasticsearch 접속 주소 |
| `SPRING_ELASTICSEARCH_USERNAME`, `SPRING_ELASTICSEARCH_PASSWORD` | 인증을 사용하는 클러스터의 계정 |
| `POST_SEARCH_INDEX` | 기본 `community-posts-v2` |
| `POST_SEARCH_REINDEX` | 초기에는 `true`, backfill 후에는 `false` |

운영에서는 HTTPS와 신뢰할 수 있는 인증서를 설정하고 인덱스 생성, 조회, bulk 쓰기에 필요한 권한을 부여합니다. 기존 배포 설정이 `SPRING_CONFIG_LOCATION`으로 기본 설정을 대체한다면 `app.search.*`, `spring.elasticsearch.*` 설정도 함께 제공해야 합니다.

## 어떻게 동기화하나요?

게시글 생성, 수정, 삭제 트랜잭션 안에서 같은 DB의 `post_search_outbox`에 ID를 기록합니다. worker는 `FOR UPDATE SKIP LOCKED`로 최대 100개를 가져와 현재 게시글을 bulk 색인합니다. 이미 삭제된 글은 검색에서도 삭제합니다. 성공한 ID만 outbox에서 제거합니다.

외부 검색 요청은 HTTP 쓰기 트랜잭션에서 실행하지 않습니다. worker는 한 배치를 처리하는 동안 별도 DB 트랜잭션과 outbox 잠금을 유지합니다. 실패하면 롤백하고 5초 후 재시도합니다. 여러 인스턴스에서도 같은 outbox ID를 동시에 처리하지 않습니다.

검색 엔진이 멈춰도 글 작성은 계속할 수 있습니다. 검색은 503으로 실패하고, 화면에 재시도 안내를 표시합니다. 자동으로 SQL 검색에 전환하지는 않습니다. 검색 설정을 끄면 기존 SQL 검색을 사용하며, 이때는 최신순 부분 문자열 검색입니다. 꺼 둔 동안 변경한 글은 다시 켤 때 backfill하세요.

## 분석기와 검색 범위

제목과 내용을 Nori로 분석하고 제목에 더 높은 가중치를 줍니다. 구단의 한글, 영문 약칭과 팀 이름은 검색 동의어로 연결합니다. 자연어에 답을 생성하는 기능은 아닙니다. 여러 단어는 AND 조건으로 검색합니다.

`GET /posts/search?keyword=엘지&sort=relevance&page=0&size=10`

`sort`는 `relevance` 또는 `recent`입니다. 페이지 크기는 1~100, offset + size는 10,000 이하여야 합니다. 동점은 작성 시각과 ID로 정렬합니다. 목록도 작성 시각과 ID로 정렬해 동일 시각의 글이 흔들리지 않도록 했습니다.

## 재색인과 장애 복구

- 초기 기동 중 Elasticsearch에 연결하지 못하면 앱 기동이 실패합니다. 의존 서비스가 준비된 뒤 시작하세요. 기동 이후의 일시 장애는 worker가 재시도합니다.
- outbox 적체는 `SELECT COUNT(*), MIN(updated_at) FROM post_search_outbox;`로 확인합니다. worker 경고와 Elasticsearch bulk 오류도 함께 확인하세요.
- 인덱스를 실수로 지웠다면 앱을 다시 시작해 인덱스를 생성하고 `POST_SEARCH_REINDEX=true`로 기존 글을 다시 넣습니다. 인덱스 생성 시점에만 매핑을 적용합니다.
- 분석기를 변경할 때는 새 인덱스 이름을 사용하고 전체 글을 backfill합니다. 현재 구현은 무중단 alias 교체를 자동화하지 않습니다. 운영 변경은 쓰기를 잠시 중지한 유지보수 시간에 새 인덱스 반영을 확인하고 전환하세요.
- DB의 글을 SQL로 직접 수정, 삭제하면 애플리케이션 outbox를 거치지 않습니다. 직접 수정 후에는 해당 ID를 outbox에 넣으세요. 이미 검색에만 남은 삭제 문서는 전체 새 인덱스로 재구축해야 제거됩니다.

## 게시판 분류

`category=FREE|REVIEW|ANALYSIS`로 목록과 검색을 같은 게시판으로 제한합니다. 생략하면 전체글입니다. 제목에 “직관”이나 “분석”이 없어도 저장된 분류로 조회합니다. 글 수정 시 분류를 변경하면 outbox를 통해 검색 필터에도 반영됩니다.

분류는 v2 인덱스의 keyword 필드로 저장합니다. 새 인덱스를 처음 만들면 기존 글을 자동으로 backfill합니다. 기존 v1 인덱스를 쓰는 배포는 `POST_SEARCH_INDEX=community-posts-v2`로 바꾸고 새 인덱스를 준비하세요. 기존 인덱스를 재사용할 경우 category 매핑을 추가하고 명시적으로 전체 재색인해야 합니다.

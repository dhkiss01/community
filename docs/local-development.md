# 개발 환경 설정

앱은 Java 21과 Node.js 24 이상에서 실행합니다. MySQL과 Nori가 설치된 Elasticsearch는 직접 준비하거나 아래 Docker 명령으로 실행할 수 있습니다. 이미 해당 포트에서 서비스를 사용 중이라면 새 컨테이너를 만들 필요가 없습니다.

## MySQL

저장소 루트에서 실행합니다. 아래 계정 정보는 로컬 개발용이며 운영에서 사용하지 않습니다.

```bash
docker run -d --name community-mysql-local -p 127.0.0.1:13306:3306 -e MYSQL_DATABASE=community -e MYSQL_USER=community -e MYSQL_PASSWORD=local-community -e MYSQL_ROOT_PASSWORD=local-root -v community-mysql-data:/var/lib/mysql mysql:8.4
```

DB 초기화가 끝난 뒤 outbox 테이블을 만듭니다.

```bash
docker exec -i community-mysql-local mysql -uroot -plocal-root community < community-BE/src/main/resources/db/search-outbox.sql
```

새 로컬 DB의 나머지 테이블은 앱의 local 프로필이 생성합니다. 기존 DB를 사용한다면 [검색 운영 문서](search.md)의 마이그레이션 순서를 확인하세요.

## Elasticsearch

Spring Boot 3.5.5의 Java 클라이언트와 맞춘 8.18.5 이미지에 Nori를 설치합니다.

```bash
docker build -t community-elasticsearch-nori:8.18.5 community-BE/elasticsearch
docker run -d --name community-elasticsearch-local --memory=2g -p 127.0.0.1:19200:9200 -e discovery.type=single-node -e xpack.security.enabled=false -e 'ES_JAVA_OPTS=-Xms512m -Xmx512m' -v community-search-data:/usr/share/elasticsearch/data community-elasticsearch-nori:8.18.5
curl -fsS 'http://localhost:19200/_cluster/health?wait_for_status=yellow&timeout=30s'
```

포트는 루프백에만 공개합니다. 운영에서는 인증과 TLS를 구성해야 합니다.

## 앱 실행

백엔드:

```bash
cd community-BE
./gradlew bootRun --args='--spring.profiles.active=local,search --spring.elasticsearch.uris=http://localhost:19200 --server.port=18080'
```

다른 터미널에서 프런트엔드를 실행합니다.

```bash
cd community-FE
npm ci
API_UPSTREAM=http://localhost:18080 npm start
```

DB 접속 정보는 `DB_URL`, `DB_NAME`, `DB_PASSWORD`로 바꿀 수 있습니다. 이미지 업로드 API를 연결할 때는 별도의 AWS 설정이 필요합니다.

컨테이너를 종료할 때는 `docker stop community-mysql-local community-elasticsearch-local`을 사용합니다. 데이터는 이름 있는 볼륨에 남습니다.

## 프런트엔드 컨테이너

저장소 루트에서 이미지를 빌드하고 실행합니다.

```bash
docker build -t community-fe-local community-FE
docker run --rm --read-only --cap-drop=ALL --security-opt=no-new-privileges -p 127.0.0.1:3000:3000 -e API_UPSTREAM=http://host.docker.internal:18080 community-fe-local
```

`host.docker.internal`은 Docker Desktop에서 호스트의 백엔드에 연결하는 주소입니다. 다른 환경에서는 접근 가능한 백엔드 주소로 `API_UPSTREAM`을 지정하세요.

의존성은 별도 단계에서 lockfile 기준으로 설치하고, 실행 이미지에는 운영 의존성과 공개 파일만 복사합니다. 실행 이미지는 셸과 npm이 없는 Node.js 24 distroless이며 UID/GID 65532로 실행합니다. 앱 파일은 root 소유로 유지합니다. Kubernetes chart도 읽기 전용 파일시스템, 권한 상승 차단, 모든 Linux capability 제거를 적용합니다.

두 베이스 이미지는 멀티아키텍처 digest로 고정했습니다. 보안 업데이트 시 태그에 해당하는 새 digest를 확인해 Dockerfile을 갱신하고, 컨테이너 테스트를 다시 실행하세요. distroless는 Node.js를 entrypoint로 사용하므로 `docker run 이미지명 npm start`나 셸 접속 대신 `docker logs`와 `docker inspect`로 상태를 확인합니다.

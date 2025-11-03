# Service B: Secret Room

Inter Nos 프로젝트의 Service B (Secret Room) 마이크로서비스입니다.

## 개요

Service B는 비밀 방(Secret Room)의 생성, 관리, 열람(Solve) 로직을 처리하는 독립적인 마이크로서비스입니다.

### 주요 기능

- 방 CRUD API (`/rooms/*`)
- 공개 목록 및 랭킹 API (`/rooms/public`, `/rank/trending`)
- 열람(Solve) 관련 API (`/s/{id}/meta`, `/solve/*`)
- 이미지 업로드 API (`/upload/presign`)
- 헬스 체크 API (`/health/liveness`, `/health/readiness`)

## 기술 스택

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL 15
- Redis
- Google Cloud Storage SDK
- Gradle

## 실행 방법

### 로컬 실행

1. 환경 변수 설정:
```bash
export DB_URL_B=jdbc:postgresql://localhost:5432/secret
export DB_USER=postgres
export DB_PASS=postgres
export REDIS_HOST=localhost
export REDIS_PORT=6379
export STORAGE_BUCKET=internos-dev
export SIGNED_URL_TTL_SEC=300
export SOLVE_NONCE_TTL_SEC=60
export LOCKOUT_FAILS=5
export LOCKOUT_TTL_SEC=600
export IP_HASH_PEPPER=change-me-in-production
export SESSION_SECRET=change-me-in-production
```

2. PostgreSQL 및 Redis 실행 (Docker 사용):
```bash
cd ../../docker
./run.sh
```

또는 개별적으로:
```bash
# PostgreSQL
docker run -d --name internos-postgres-b -e POSTGRES_DB=secret -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5433:5432 postgres:15-alpine

# Redis
docker run -d --name internos-redis -p 6379:6379 redis:7-alpine
```

3. 애플리케이션 실행:
```bash
./gradlew bootRun
```

### Docker로 전체 실행

```bash
cd ../../docker
./run.sh
```

서비스는 `http://localhost:8081/b/v1`에서 실행됩니다.

### Docker 이미지 직접 빌드 및 실행

```bash
cd service-b-secret
docker build -t internos/service-b-secret:latest .
docker run -d \
  --name internos-service-b \
  --network internos-network \
  -e DB_URL_B=jdbc:postgresql://internos-postgres-b:5432/secret \
  -e DB_USER=postgres \
  -e DB_PASS=postgres \
  -e REDIS_HOST=internos-redis \
  -p 8081:8081 \
  internos/service-b-secret:latest
```

## API 문서

애플리케이션 실행 후 Swagger UI에 접근:
- URL: `http://localhost:8081/b/v1/swagger-ui.html`
- API Docs: `http://localhost:8081/b/v1/api-docs`

## 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `DB_URL_B` | PostgreSQL 데이터베이스 URL | `jdbc:postgresql://localhost:5432/secret` |
| `DB_USER` | 데이터베이스 사용자명 | `postgres` |
| `DB_PASS` | 데이터베이스 비밀번호 | `postgres` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `STORAGE_BUCKET` | GCS 버킷 이름 | `internos-dev` |
| `SIGNED_URL_TTL_SEC` | 서명 URL TTL (초) | `300` |
| `SOLVE_NONCE_TTL_SEC` | Nonce TTL (초) | `60` |
| `LOCKOUT_FAILS` | 락아웃 임계치 (실패 횟수) | `5` |
| `LOCKOUT_TTL_SEC` | 락아웃 지속 시간 (초) | `600` |
| `IP_HASH_PEPPER` | IP 해싱용 페퍼 | (필수) |
| `SESSION_SECRET` | 세션 암호화 키 | (필수) |

## 빌드

```bash
./gradlew build
```

## 테스트

```bash
./gradlew test
```

## 데이터베이스 마이그레이션

Flyway를 사용하여 데이터베이스 마이그레이션을 관리합니다:
- 마이그레이션 파일: `src/main/resources/db/migration/`
- 애플리케이션 시작 시 자동 실행


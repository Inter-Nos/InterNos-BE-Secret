# Docker Run Scripts

Docker Compose 대신 개별 Docker 명령어로 서비스를 실행하는 스크립트입니다.

## 사용 방법

### 모든 서비스 시작

```bash
./run.sh
```

이 스크립트는 다음을 수행합니다:
1. Docker 네트워크 생성 (`internos-network`)
2. PostgreSQL 컨테이너 시작
3. Redis 컨테이너 시작
4. Service B 이미지 빌드 (없는 경우)
5. Service B 컨테이너 시작

### 모든 서비스 중지 및 제거

```bash
./stop.sh
```

## 개별 Docker 명령어

### 1. 네트워크 생성

```bash
docker network create internos-network
```

### 2. PostgreSQL 실행

```bash
docker run -d \
    --name internos-postgres-b \
    --network internos-network \
    -e POSTGRES_DB=secret \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5433:5432 \
    -v postgres_b_data:/var/lib/postgresql/data \
    postgres:15-alpine
```

### 3. Redis 실행

```bash
docker run -d \
    --name internos-redis \
    --network internos-network \
    -p 6379:6379 \
    -v redis_data:/data \
    redis:7-alpine
```

### 4. Service B 이미지 빌드

```bash
cd ../service-b-secret
docker build -t internos/service-b-secret:latest .
cd ../docker
```

### 5. Service B 실행

```bash
docker run -d \
    --name internos-service-b \
    --network internos-network \
    -e SPRING_PROFILES_ACTIVE=local \
    -e DB_URL_B=jdbc:postgresql://internos-postgres-b:5432/secret \
    -e DB_USER=postgres \
    -e DB_PASS=postgres \
    -e REDIS_HOST=internos-redis \
    -e REDIS_PORT=6379 \
    -e STORAGE_BUCKET=internos-dev \
    -e SIGNED_URL_TTL_SEC=300 \
    -e SOLVE_NONCE_TTL_SEC=60 \
    -e LOCKOUT_FAILS=5 \
    -e LOCKOUT_TTL_SEC=600 \
    -e IP_HASH_PEPPER=change-me-in-production \
    -e SESSION_SECRET=change-me-in-production \
    -e SERVER_PORT=8081 \
    -p 8081:8081 \
    internos/service-b-secret:latest
```

## 로그 확인

```bash
# Service B 로그
docker logs -f internos-service-b

# PostgreSQL 로그
docker logs -f internos-postgres-b

# Redis 로그
docker logs -f internos-redis
```

## 접속 정보

- **PostgreSQL**: `localhost:5433`
- **Redis**: `localhost:6379`
- **Service B**: `http://localhost:8081/b/v1`
- **Swagger UI**: `http://localhost:8081/b/v1/swagger-ui.html`

## 데이터 볼륨

데이터는 Docker 볼륨에 저장됩니다:
- `postgres_b_data`: PostgreSQL 데이터
- `redis_data`: Redis 데이터

볼륨을 삭제하려면:
```bash
docker volume rm postgres_b_data redis_data
```


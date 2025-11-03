#!/bin/bash

# Create network if it doesn't exist
NETWORK_NAME="internos-network"
if [ -z "$(docker network ls | grep $NETWORK_NAME)" ]; then
    echo "Creating network: $NETWORK_NAME"
    docker network create $NETWORK_NAME
else
    echo "Network $NETWORK_NAME already exists"
fi

# Stop and remove existing containers if they exist
echo "Stopping existing containers..."
docker stop internos-postgres-b internos-redis internos-service-b 2>/dev/null
docker rm internos-postgres-b internos-redis internos-service-b 2>/dev/null

# Run PostgreSQL
echo "Starting PostgreSQL..."
docker run -d \
    --name internos-postgres-b \
    --network $NETWORK_NAME \
    -e POSTGRES_DB=secret \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5433:5432 \
    -v postgres_b_data:/var/lib/postgresql/data \
    postgres:15-alpine

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5
until docker exec internos-postgres-b pg_isready -U postgres > /dev/null 2>&1; do
    echo "Waiting for PostgreSQL..."
    sleep 1
done
echo "PostgreSQL is ready"

# Run Redis
echo "Starting Redis..."
docker run -d \
    --name internos-redis \
    --network $NETWORK_NAME \
    -p 6379:6379 \
    -v redis_data:/data \
    redis:7-alpine

# Wait for Redis to be ready
echo "Waiting for Redis to be ready..."
sleep 3
until docker exec internos-redis redis-cli ping > /dev/null 2>&1; do
    echo "Waiting for Redis..."
    sleep 1
done
echo "Redis is ready"

# Build Service B image if not exists
SERVICE_B_IMAGE="internos/service-b-secret:latest"
if [ -z "$(docker images | grep internos/service-b-secret)" ]; then
    echo "Building Service B image..."
    cd ../service-b-secret
    docker build -t $SERVICE_B_IMAGE .
    cd ../docker
else
    echo "Service B image already exists"
fi

# Run Service B
echo "Starting Service B..."
docker run -d \
    --name internos-service-b \
    --network $NETWORK_NAME \
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
    $SERVICE_B_IMAGE

echo ""
echo "All services are starting..."
echo "PostgreSQL: localhost:5433"
echo "Redis: localhost:6379"
echo "Service B: http://localhost:8081/b/v1"
echo ""
echo "To view logs:"
echo "  docker logs -f internos-service-b"
echo ""
echo "To stop all services:"
echo "  docker stop internos-postgres-b internos-redis internos-service-b"
echo ""
echo "To remove all containers:"
echo "  docker rm internos-postgres-b internos-redis internos-service-b"


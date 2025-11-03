#!/bin/bash

echo "Stopping all containers..."
docker stop internos-service-b internos-redis internos-postgres-b 2>/dev/null

echo "Removing containers..."
docker rm internos-service-b internos-redis internos-postgres-b 2>/dev/null

echo "Containers stopped and removed"


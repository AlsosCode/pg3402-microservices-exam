#!/bin/bash

# Test script to demonstrate load balancing across multiple catalog-service instances

# Set Docker path
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"

echo "========================================="
echo "Load Balancing Test for Catalog Service"
echo "========================================="
echo ""

# Check running instances
echo "1. Checking running catalog-service instances:"
docker ps --filter "name=catalog-service" --format "   - {{.Names}} ({{.Status}})"
echo ""

# Count instances
INSTANCE_COUNT=$(docker ps --filter "name=catalog-service" --format "{{.Names}}" | wc -l | tr -d ' ')
echo "   Total instances running: $INSTANCE_COUNT"
echo ""

# Send test requests
echo "2. Sending 20 test requests through API Gateway..."
for i in {1..20}; do
    curl -s http://localhost:8080/api/catalog/sets > /dev/null
    if [ $? -eq 0 ]; then
        echo -n "."
    else
        echo -n "X"
    fi
    sleep 0.2
done
echo ""
echo "   All requests sent!"
echo ""

# Wait a moment for logs to be written
sleep 2

# Count requests per instance
echo "3. Request distribution across instances:"
for container in $(docker ps --filter "name=catalog-service" --format "{{.Names}}" | sort); do
    count=$(docker logs $container 2>&1 | grep "GET.*sets" | wc -l | tr -d ' ')
    echo "   - $container: $count requests"
done
echo ""

echo "========================================="
echo "Load balancing test complete!"
echo ""
echo "Expected behavior: Requests should be distributed"
echo "evenly across all catalog-service instances using"
echo "Spring Cloud LoadBalancer with round-robin strategy."
echo "========================================="

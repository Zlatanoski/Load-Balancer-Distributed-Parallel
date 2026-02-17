#!/bin/bash

echo "=========================================="
echo "Starting Full Performance Test Suite"
echo "=========================================="
echo ""

# Rebuild everything first
echo "Building Docker images..."
docker compose build
echo ""

# Test with different numbers of workers
for workers in 1 2 4 6 8 10; do
  echo "######################################"
  echo "Testing with ${workers} worker(s)"
  echo "######################################"
  echo ""
  
  # Stop everything
  docker compose down

  # Start appropriate workers + proxy
  echo "Starting ${workers} worker(s)..."

  if [ $workers -eq 1 ]; then
    export WORKER_ADDRESSES="worker1:8081"
    docker compose up -d worker1 proxy
  elif [ $workers -eq 2 ]; then
    export WORKER_ADDRESSES="worker1:8081,worker2:8082"
    docker compose up -d worker1 worker2 proxy
  elif [ $workers -eq 4 ]; then
    export WORKER_ADDRESSES="worker1:8081,worker2:8082,worker3:8083,worker4:8084"
    docker compose up -d worker1 worker2 worker3 worker4 proxy
  elif [ $workers -eq 6 ]; then
    export WORKER_ADDRESSES="worker1:8081,worker2:8082,worker3:8083,worker4:8084,worker5:8085,worker6:8086"
    docker compose up -d worker1 worker2 worker3 worker4 worker5 worker6 proxy
  elif [ $workers -eq 8 ]; then
    export WORKER_ADDRESSES="worker1:8081,worker2:8082,worker3:8083,worker4:8084,worker5:8085,worker6:8086,worker7:8087,worker8:8088"
    docker compose up -d worker1 worker2 worker3 worker4 worker5 worker6 worker7 worker8 proxy
  else  # 10 workers
    export WORKER_ADDRESSES="worker1:8081,worker2:8082,worker3:8083,worker4:8084,worker5:8085,worker6:8086,worker7:8087,worker8:8088,worker9:8089,worker10:8090"
    docker compose up -d worker1 worker2 worker3 worker4 worker5 worker6 worker7 worker8 worker9 worker10 proxy
  fi
  
  # Wait for containers to be ready
  echo "Waiting for system to stabilize..."
  sleep 10
  
  # Run performance test
  ./performance_test.sh | tee results_${workers}workers.txt
  
  echo ""
  echo "Results saved to results_${workers}workers.txt"
  echo ""
done

# Cleanup
docker compose down

echo ""
echo "=========================================="
echo "All Tests Complete!"
echo "=========================================="
echo "Results files:"
ls -lh results_*workers.txt

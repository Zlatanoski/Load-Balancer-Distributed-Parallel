#!/bin/bash

echo "=== Load Balancer Performance Testing ==="
echo "Start time: $(date)"
echo ""

# Function to test with N concurrent clients
test_concurrency() {
  local concurrency=$1
  local total_requests=$((concurrency * 1000))
  
  echo "Concurrency=${concurrency} (${total_requests} total requests)"
  start=$(date +%s)
  
  # Launch N-1 clients in background
  for ((c=1; c<concurrency; c++)); do
    (for i in {1..1000}; do 
      curl -s http://localhost:8080/work > /dev/null 2>&1
    done) &
  done
  
  # Run last client in foreground
  for i in {1..1000}; do
    curl -s http://localhost:8080/work > /dev/null 2>&1
  done
  
  wait  # Wait for all background jobs
  
  end=$(date +%s)
  duration=$((end - start))
  
  # Avoid division by zero
  if [ $duration -eq 0 ]; then
    throughput="N/A (too fast)"
  else
    throughput=$((total_requests / duration))
  fi
  
  echo "Duration: ${duration}s | Throughput: ${throughput} req/s"
  echo ""
  
  # Check if test took longer than 10 minutes (600 seconds)
  if [ $duration -gt 600 ]; then
    echo "⚠️  Test exceeded 10 minutes - stopping concurrency scaling"
    return 1
  fi
  
  return 0
}

# Test sequential baseline
echo "Test: Sequential (1000 requests)"
start=$(date +%s)
for i in {1..1000}; do
  curl -s http://localhost:8080/work > /dev/null 2>&1
done
end=$(date +%s)
duration=$((end - start))
if [ $duration -eq 0 ]; then
  throughput="N/A"
else
  throughput=$((1000 / duration))
fi
echo "Duration: ${duration}s | Throughput: ${throughput} req/s"
echo ""

# Test increasing concurrency until one test takes 10+ minutes
for concurrency in 2 4 6 8 10 12 14 16 18 20 25 30 40 50; do
  test_concurrency $concurrency
  if [ $? -eq 1 ]; then
    break  # Stop if test took > 10 minutes
  fi
done

echo "=== Testing Complete ==="
echo "End time: $(date)"

# Load Balancer – Distributed & Parallel (Docker + Java)

A small educational project that demonstrates a **load balancer (proxy)** distributing requests to multiple **worker** services. It is designed to be run locally with **Docker Compose**, and includes scripts for building JARs and running basic performance tests.

## What’s inside

- **Proxy / Load Balancer**: receives client requests and forwards them to workers
- **Workers**: handle the actual work and respond to the proxy
- **Health checks**: proxy can periodically check if workers are alive (via `/health`)
- **Docker setup**: separate Dockerfiles for proxy and worker + `docker-compose.yml`
- **Benchmark scripts + results**: simple performance test script and saved results for different worker counts

## GET /api/owners/11

Environment
- Java 17
- Spring Boot 4
- PostgreSQL 18.3 via Docker Compose
- Local MacBook
- hey

Load
- Duration: 10 seconds
- Concurrency: 10
- runs: 3


Results
| Run | Requests/sec | Avg latency | p50 | p95 | p99 | Errors |
|---|---:|---:|---:|---:|---:|---:|
| 1 | 5429 | 1.8 ms | 1.8 ms | 2.3 ms | 3.2 ms | 0 |
| 2 | 5437 | 1.8 ms | 1.8 ms | 2.5 ms | 3.1 ms | 0 |
| 3 | 5360 | 1.9 ms | 1.8 ms | 2.6 ms | 3.2 ms | 0 |

Average throughput: ~5409 req/s  
Average latency: ~1.83 ms  
Average p95 latency: ~2.47 ms  
Average p99 latency: ~3.17 ms  
HTTP error rate: 0%

#!/bin/bash

set -e

URL="http://localhost:8080/api/owners"
REQUESTS=500
CONCURRENCY=10
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
OUTPUT="perf-baseline-$TIMESTAMP.txt"

echo "Performance baseline"
echo "URL=$URL"
echo "Requests=$REQUESTS"
echo "Concurrency=$CONCURRENCY"

hey -n "$REQUESTS" -c "$CONCURRENCY" "$URL" | tee "$OUTPUT"


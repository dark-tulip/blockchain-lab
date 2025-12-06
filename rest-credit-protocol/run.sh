#!/bin/bash

# Время в секундах, 0.183
TIMEFORMAT=%R

> blockchain_times.txt

for i in {1..50}; do
  { time curl -s -X POST http://localhost:8081/api/loans \
    -H "Content-Type: application/json" \
    -d "{\"id\":\"loanB$i\",\"accountId\":\"acc3\",\"principal\":500,\"outstanding\":0,\"status\":\"ACTIVE\"}" \
    > /dev/null; } 2>> blockchain_times.txt
done

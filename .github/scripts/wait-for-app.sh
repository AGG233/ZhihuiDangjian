#!/usr/bin/env bash

set -euo pipefail

url="${1:-http://127.0.0.1:9000/api/school/all}"
timeout_seconds="${2:-120}"
start_time="$(date +%s)"

until curl --silent --fail "$url" >/dev/null; do
  now="$(date +%s)"
  if (( now - start_time >= timeout_seconds )); then
    echo "Application did not become ready within ${timeout_seconds}s: ${url}" >&2
    exit 1
  fi
  sleep 2
done

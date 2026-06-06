#!/usr/bin/env bash

set -euo pipefail

url="${1:-http://127.0.0.1:9000/api/school/all}"
timeout_seconds="${2:-120}"
pid_file="${3:-server/build/api-smoke-app.pid}"
log_file="${4:-server/build/api-smoke-app.log}"
start_time="$(date +%s)"

app_pid=""
if [[ -f "$pid_file" ]]; then
  app_pid="$(tr -d '[:space:]' < "$pid_file")"
fi

print_app_log_tail() {
  if [[ -f "$log_file" ]]; then
    echo "Last application log lines:" >&2
    tail -n 120 "$log_file" >&2 || true
  fi
}

until curl --silent --fail "$url" >/dev/null; do
  if [[ -n "$app_pid" ]] && ! kill -0 "$app_pid" 2>/dev/null; then
    echo "Application process exited before becoming ready: pid=${app_pid}, url=${url}" >&2
    print_app_log_tail
    exit 1
  fi

  now="$(date +%s)"
  if (( now - start_time >= timeout_seconds )); then
    echo "Application did not become ready within ${timeout_seconds}s: ${url}" >&2
    print_app_log_tail
    exit 1
  fi
  sleep 2
done

#!/usr/bin/env bash
# Boots a Paper server with the built plugin and verifies that it enables cleanly.
# Usage: smoke-test.sh <server-dir> <plugin-jar> [server-port]
set -euo pipefail

SERVER_DIR="${1:?usage: smoke-test.sh <server-dir> <plugin-jar> [port]}"
JAR="${2:?usage: smoke-test.sh <server-dir> <plugin-jar> [port]}"
PORT="${3:-25601}"

mkdir -p "$SERVER_DIR/plugins"
cp "$JAR" "$SERVER_DIR/plugins/ActionsAndTriggers.jar"
echo "eula=true" > "$SERVER_DIR/eula.txt"

cat > "$SERVER_DIR/server.properties" <<EOF
online-mode=false
level-type=minecraft:flat
generate-structures=false
spawn-protection=0
view-distance=4
simulation-distance=4
max-players=5
server-port=${PORT}
motd=AAT smoke test
EOF

cd "$SERVER_DIR"
java -Xms512M -Xmx1G -jar paper.jar --nogui > server.log 2>&1 &
PID=$!
trap 'kill "$PID" 2>/dev/null || true' EXIT

for _ in $(seq 1 240); do
  if grep -q 'Done (' server.log 2>/dev/null; then
    break
  fi
  if ! kill -0 "$PID" 2>/dev/null; then
    echo "::error::Server exited before it finished starting"
    tail -n 120 server.log
    exit 1
  fi
  sleep 1
done

if ! grep -q 'Done (' server.log; then
  echo "::error::Server did not start within 240 seconds"
  tail -n 120 server.log
  exit 1
fi

fail=0
for expected in \
  'Actions&Triggers initialized successfully' \
  'core:command' \
  'core:message' \
  'core:cryo_freeze'
do
  if ! grep -q "$expected" server.log; then
    echo "::error::Missing expected log line: $expected"
    fail=1
  fi
done

if grep -qE 'Could not load|Failed to enable|Exception occurred while enabling' server.log; then
  echo "::error::Plugin failed to enable"
  grep -nE 'Could not load|Failed to enable|Exception occurred while enabling' server.log || true
  fail=1
fi

kill "$PID" 2>/dev/null || true
wait "$PID" 2>/dev/null || true
trap - EXIT

if [ "$fail" -ne 0 ]; then
  echo "Smoke test failed"
  tail -n 120 server.log
  exit 1
fi

echo "Smoke test passed: $(grep -m1 'This server is running Paper' server.log || echo 'Paper')"

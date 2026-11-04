#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

if ! pgrep -f "node server.js" > /dev/null; then
  echo "Starting Saif Vegetable Web Server on port 3000..."
  nohup node server.js >> /var/log/server.log 2>&1 &
  sleep 1
fi

# Verify port 3000 is listening
for i in {1..10}; do
  if curl -s http://127.0.0.1:3000/healthz > /dev/null 2>&1; then
    echo "Saif Vegetable Server is up and healthy on http://127.0.0.1:3000/"
    exit 0
  fi
  sleep 0.5
done

echo "Server started with PID $(pgrep -f 'node server.js')"

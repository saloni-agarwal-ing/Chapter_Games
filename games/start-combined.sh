#!/bin/bash
# Combined startup script for Render.com deployment
# Starts both Java game server and websockify proxy in the same container

set -e

echo "🚀 Starting Drop Game Combined Server on Render.com"

# Get the port from Render (this will be the external port, typically 10000 or similar)
PUBLIC_PORT="${PORT:-10000}"
JAVA_SERVER_PORT="8887"

echo "Public port: $PUBLIC_PORT"
echo "Java server port: $JAVA_SERVER_PORT"

# Start Java WebSocket server in background
echo "Starting Java WebSocket server..."
java -jar /app/drop-server.jar &
JAVA_PID=$!

# Wait a moment for Java server to start
sleep 5

# Check if Java server is running
if ! kill -0 $JAVA_PID 2>/dev/null; then
    echo "❌ Java server failed to start"
    exit 1
fi

echo "✅ Java server started (PID: $JAVA_PID)"

# Start websockify proxy in foreground
echo "Starting websockify proxy..."
echo "Proxying :$PUBLIC_PORT -> localhost:$JAVA_SERVER_PORT"

# Use exec so websockify becomes the main process (for proper signal handling)
exec websockify --web=./ --verbose "$PUBLIC_PORT" "localhost:$JAVA_SERVER_PORT"

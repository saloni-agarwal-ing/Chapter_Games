#!/bin/bash

echo "🚀 Drop Game Local Test Script"
echo "==============================="

# Function to check if port is open
check_port() {
    local port=$1
    local service_name=$2
    if nc -z localhost $port 2>/dev/null; then
        echo "✅ $service_name is running on port $port"
        return 0
    else
        echo "❌ $service_name is NOT running on port $port"
        return 1
    fi
}

# Function to test WebSocket connection
test_websocket() {
    local url=$1
    echo "🔍 Testing WebSocket connection to $url"

    # Use curl to test WebSocket upgrade
    response=$(curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Sec-WebSocket-Version: 13" -H "Sec-WebSocket-Key: test" "$url" 2>&1)

    if echo "$response" | grep -q "101 Switching Protocols"; then
        echo "✅ WebSocket upgrade successful"
        return 0
    else
        echo "❌ WebSocket upgrade failed"
        echo "Response: $response"
        return 1
    fi
}

echo ""
echo "1️⃣ Checking if services are running..."
check_port 8887 "Game Server (Java WebSocket)"
game_server_up=$?

check_port 10000 "Websockify Proxy"
websockify_up=$?

echo ""
echo "2️⃣ Testing connections..."

if [ $game_server_up -eq 0 ]; then
    echo "🔍 Testing direct connection to game server..."
    test_websocket "ws://localhost:8887"
fi

if [ $websockify_up -eq 0 ]; then
    echo "🔍 Testing websockify proxy connection..."
    test_websocket "ws://localhost:10000"

    echo "🌐 Testing web client access..."
    if curl -f http://localhost:10000 >/dev/null 2>&1; then
        echo "✅ Web client is accessible at http://localhost:10000"
    else
        echo "❌ Web client is not accessible"
    fi
fi

echo ""
echo "3️⃣ Setup Instructions:"
echo "======================"
echo ""
echo "To start the full stack:"
echo "  docker-compose up --build"
echo ""
echo "To start just the Java server:"
echo "  java -jar core/build/libs/drop-server-1.0.0-all.jar"
echo ""
echo "To start websockify manually:"
echo "  ./start-websockify.sh"
echo ""
echo "To test with the desktop client:"
echo "  ./gradlew :lwjgl3:run -PplayerName=TestPlayer"
echo ""
echo "Web client URL: http://localhost:10000"
echo "Direct WebSocket: ws://localhost:8887"
echo "Proxy WebSocket: ws://localhost:10000"

echo ""
echo "🎯 Quick troubleshooting:"
echo "========================"
echo ""
echo "If game server won't start:"
echo "  - Check if port 8887 is already in use: lsof -i :8887"
echo "  - Rebuild the JAR: ./gradlew :core:shadowJar"
echo ""
echo "If websockify won't start:"
echo "  - Install websockify: pip install websockify"
echo "  - Check if port 10000 is in use: lsof -i :10000"
echo ""
echo "If web client can't connect:"
echo "  - Make sure both services are running"
echo "  - Check browser console for WebSocket errors"
echo "  - Try direct connection to ws://localhost:10000"

#!/bin/bash

echo "🔍 Testing Drop Game Client Connection"
echo "====================================="

SERVER_URI="wss://chapter-games-websockify.onrender.com"
PLAYER_NAME="DebugPlayer"

echo "Server URI: $SERVER_URI"
echo "Player Name: $PLAYER_NAME"
echo ""

echo "1️⃣ Testing server connectivity first..."
echo "Testing HTTPS connection:"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\nTotal Time: %{time_total}s\n" "https://chapter-games-websockify.onrender.com"

echo ""
echo "2️⃣ Starting client with debug output..."
echo "Command: ./gradlew :lwjgl3:run -PplayerName=$PLAYER_NAME -DgameServerUri=$SERVER_URI"
echo ""

cd "/Users/yu69yj/Documents/workspace/Chapter_Games/games"

# Set Java system properties for debugging
export JAVA_OPTS="-DgameServerUri=$SERVER_URI -Djava.util.logging.level.websocket=ALL"

# Run the client
./gradlew :lwjgl3:run -PplayerName="$PLAYER_NAME" -DgameServerUri="$SERVER_URI" --info

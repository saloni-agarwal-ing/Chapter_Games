#!/bin/bash

echo "🧪 Testing Render.com Deployment"
echo "================================"

RENDER_URL="chapter-games-websockify.onrender.com"

echo ""
echo "1️⃣ Testing HTTPS connectivity (port 443)..."
if nc -z "$RENDER_URL" 443 2>/dev/null; then
    echo "✅ HTTPS port 443 is accessible"
else
    echo "❌ HTTPS port 443 is not accessible"
fi

echo ""
echo "2️⃣ Testing HTTP response..."
HTTP_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "https://$RENDER_URL" 2>/dev/null)
if [ "$HTTP_RESPONSE" = "200" ] || [ "$HTTP_RESPONSE" = "101" ]; then
    echo "✅ HTTP response: $HTTP_RESPONSE (Good)"
elif [ "$HTTP_RESPONSE" = "000" ]; then
    echo "❌ No HTTP response - service may be down"
else
    echo "⚠️  HTTP response: $HTTP_RESPONSE"
fi

echo ""
echo "3️⃣ Testing WebSocket upgrade..."
# Try to get WebSocket upgrade response
WS_TEST=$(curl -i -s -N \
    -H "Connection: Upgrade" \
    -H "Upgrade: websocket" \
    -H "Sec-WebSocket-Version: 13" \
    -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
    "https://$RENDER_URL" 2>&1 | head -5)

if echo "$WS_TEST" | grep -q "101 Switching Protocols"; then
    echo "✅ WebSocket upgrade successful"
elif echo "$WS_TEST" | grep -q "200 OK"; then
    echo "✅ HTTP connection successful (websockify may be serving web page)"
else
    echo "⚠️  WebSocket response:"
    echo "$WS_TEST"
fi

echo ""
echo "4️⃣ Testing the old port 10000 (should fail)..."
if nc -z "$RENDER_URL" 10000 2>/dev/null; then
    echo "⚠️  Port 10000 is accessible (unexpected)"
else
    echo "✅ Port 10000 correctly blocked (expected on Render.com)"
fi

echo ""
echo "📋 Summary & Next Steps:"
echo "========================"
echo ""
echo "✅ Correct connection URLs:"
echo "   Web browser: https://$RENDER_URL"
echo "   Desktop client: wss://$RENDER_URL"
echo "   Desktop command: ./gradlew :lwjgl3:run -PplayerName=YourName -DgameServerUri=wss://$RENDER_URL"
echo ""
echo "❌ Don't use these (will fail):"
echo "   ws://$RENDER_URL:10000"
echo "   wss://$RENDER_URL:10000"
echo ""
echo "🔧 If connections still fail:"
echo "   1. Check Render service logs for errors"
echo "   2. Ensure your service is using Dockerfile.combined"
echo "   3. Verify the service is in 'Running' status"
echo "   4. Try redeploying the service"

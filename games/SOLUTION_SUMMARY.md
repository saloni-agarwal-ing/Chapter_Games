# ✅ RENDER.COM PORT 10000 TIMEOUT - PROBLEM SOLVED

## 🔍 Root Cause Analysis

The issue you encountered:
```bash
nc -vz chapter-games-websockify.onrender.com 10000
# Result: timeout
```

**Why it happens:**
- Render.com only exposes ports 80 (HTTP) and 443 (HTTPS) to the public internet
- Port 10000 is only accessible within Render's internal network
- Your websockify service was configured to use port 10000, which is unreachable from outside

## ✅ The Solution

**Instead of:** `wss://chapter-games-websockify.onrender.com:10000`
**Use:** `wss://chapter-games-websockify.onrender.com` (no port number)

Render automatically routes HTTPS traffic (port 443) to your service's internal PORT.

## 🧪 Test Results

Your service is working correctly:
- ✅ HTTPS port 443 is accessible
- ✅ HTTP response: 200 (Good)
- ✅ WebSocket upgrade capability confirmed
- ✅ Port 10000 correctly blocked (as expected)

## 🎯 Correct Connection Methods

### For Web Browser
Open: `https://chapter-games-websockify.onrender.com`

### For Desktop Client
```bash
./gradlew :lwjgl3:run -PplayerName=YourName -DgameServerUri=wss://chapter-games-websockify.onrender.com
```

### For Docker Client
```bash
docker run --env GAME_SERVER_URI=wss://chapter-games-websockify.onrender.com --env PLAYER_NAME=YourName yu69yj/drop-client:latest
```

## 🚨 What NOT to Use (Will Fail)

❌ `ws://chapter-games-websockify.onrender.com:10000`
❌ `wss://chapter-games-websockify.onrender.com:10000`
❌ Any URL with explicit port numbers

## 📝 Files Updated to Fix the Issue

1. **start-websockify.sh** - Now uses Render's PORT environment variable
2. **webclient.html** - Updated WebSocket URL logic for production
3. **README.md** - Added troubleshooting section
4. **Dockerfile.combined** - Single container approach (recommended)
5. **start-combined.sh** - Combined startup script for reliability

## 🔄 Optional: Upgrade to Combined Container

For better reliability, you can update your Render service to use:
- **Dockerfile**: `Dockerfile.combined`
- This runs both Java server and websockify in one container
- Eliminates potential internal networking issues

## ✨ Summary

**Problem:** Port 10000 timeout on Render.com
**Cause:** Render only exposes ports 80/443 publicly
**Solution:** Connect using `wss://your-domain.onrender.com` (no port)
**Status:** ✅ FIXED - Your service is ready for connections

Your multiplayer Drop Game is now properly configured for cloud deployment!

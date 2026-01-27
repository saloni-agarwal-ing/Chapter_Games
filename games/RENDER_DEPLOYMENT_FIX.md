# Render.com Deployment Configuration

## The Problem
Render.com only exposes ports 80 (HTTP) and 443 (HTTPS) to the public internet. Your websockify service was trying to use port 10000, which is not accessible from outside.

## The Solution
We need to deploy TWO services on Render.com:

### Service 1: Java Game Server (Internal)
- Runs your Java WebSocket server
- Only accessible internally within Render's network
- Uses port 8887 internally

### Service 2: Websockify Proxy (Public-facing) 
- Accepts public WebSocket connections on port 443 (HTTPS)
- Forwards to the Java server internally
- Serves the web client HTML

## Deployment Steps

### 1. Create render.yaml for Multi-Service Deployment

Create this file in your project root to define both services:

```yaml
services:
  - type: web
    name: drop-game-server
    env: docker
    dockerfilePath: ./Dockerfile
    envVars:
      - key: PORT
        value: 8887
    # This service is internal-only, no public URL needed
    
  - type: web
    name: drop-game-websockify
    env: docker
    dockerfilePath: ./Dockerfile.websockify
    envVars:
      - key: TARGET_HOST
        value: drop-game-server  # Internal service name
      - key: TARGET_PORT
        value: 8887
    # This service gets the public URL
```

### 2. Alternative: Single Container Approach

If Render doesn't support internal networking well, you can run both in one container:

Create `Dockerfile.combined`:
```dockerfile
FROM eclipse-temurin:8-jre

# Install Python and websockify
RUN apt-get update && apt-get install -y python3 python3-pip && \
    pip3 install websockify && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Java server
COPY core/build/libs/drop-server-1.0.0-all.jar /app/drop-server.jar

# Copy web client and assets
COPY webclient.html /app/index.html
COPY assets/ /app/assets/

# Copy startup script
COPY start-combined.sh /app/start-combined.sh
RUN chmod +x /app/start-combined.sh

EXPOSE 8887
CMD ["/app/start-combined.sh"]
```

### 3. Client Connection URLs

**For web clients (browser):**
- Production: `https://your-app-name.onrender.com` (automatically uses WSS on port 443)
- Local: `http://localhost:10000`

**For desktop clients:**
```bash
# Production
./gradlew :lwjgl3:run -PplayerName=YourName -DgameServerUri=wss://your-app-name.onrender.com

# Local  
./gradlew :lwjgl3:run -PplayerName=YourName -DgameServerUri=ws://localhost:8887
```

## Testing the Fix

1. Test HTTPS connectivity:
   ```bash
   curl -v https://chapter-games-websockify.onrender.com
   ```

2. Test WebSocket connection:
   ```bash
   # This should work now (using standard HTTPS port)
   wscat -c wss://chapter-games-websockify.onrender.com
   ```

3. Open in browser:
   ```
   https://chapter-games-websockify.onrender.com
   ```

The key insight is that Render.com automatically routes external HTTPS traffic (port 443) to your container's PORT environment variable, so you don't need to specify port numbers in your URLs.

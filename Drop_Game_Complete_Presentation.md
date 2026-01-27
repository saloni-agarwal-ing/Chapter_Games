# Drop Game - Complete Project Overview
## Multiplayer WebSocket Implementation & Architecture

---

## Project Summary

**Drop Game** is a real-time multiplayer arcade game built with LibGDX and Java WebSockets. Players control buckets to catch falling raindrops while competing against each other in a shared game world.

**Key Technologies:**
- **LibGDX**: Cross-platform game framework
- **Java WebSockets**: Real-time multiplayer communication  
- **Docker**: Containerized deployment
- **Websockify**: WebSocket-to-TCP proxy for browser access
- **Cloud Deployment**: Render.com hosting

---

## Game Architecture Overview

```
┌─────────────────┐    WebSocket     ┌─────────────────┐
│   Game Client   │ ←──────────────→ │   Game Server   │
│   (LibGDX)      │  ws://host:8887  │  (Java WS)      │
└─────────────────┘                  └─────────────────┘
        │                                     │
   ┌────▼────┐                          ┌────▼────┐
   │ Input   │                          │ Game    │
   │Handler  │                          │ State   │
   │& Render │                          │Manager  │
   └─────────┘                          └─────────┘

For Browser Access:
┌─────────────────┐   WebSocket   ┌─────────────────┐   TCP   ┌─────────────────┐
│   Web Browser   │ ←────────────→ │   Websockify    │ ←──────→ │   Game Server   │
│   (HTML/JS)     │  wss://host:443│   Proxy         │ :8887   │   (Java WS)     │
└─────────────────┘                └─────────────────┘         └─────────────────┘
```

---

## How the Game Works

### Core Gameplay Mechanics

**1. Game World:**
- 8x5 unit coordinate system
- Raindrops spawn randomly at the top
- Players control buckets at the bottom
- Physics: gravity affects falling drops

**2. Player Actions:**
- Move bucket left/right
- Catch falling raindrops
- Score increases with each catch
- Real-time position updates to server

**3. Multiplayer Features:**
- Multiple players in same game world
- Real-time synchronization
- Shared scoreboard
- Player identification with colors

---

## WebSocket Implementation Details

### Server Side (WebGameServer.java)

**Key Components:**
```java
public class WebGameServer extends WebSocketServer {
    // Player management
    private Map<Integer, WebSocket> clients = new HashMap<>();
    private Map<Integer, String> playerNames = new HashMap<>();
    private Map<WebSocket, Integer> connectionToPlayerId = new HashMap<>();
    
    // Game state
    private Map<Integer, Float> bucketPositions = new HashMap<>();
    private List<Map<String, Float>> drops = new ArrayList<>();
    private Map<Integer, Integer> scoreboard = new HashMap<>();
}
```

**The onMessage() Method Explained:**

```java
public void onMessage(WebSocket conn, String message) {
    // 1. Parse incoming JSON message
    Map<String, Object> msg = gson.fromJson(message, 
        new TypeToken<Map<String, Object>>(){}.getType());
    String type = (String) msg.get("type");
    
    // 2. Handle different message types
    if ("join".equals(type)) {
        // New player joining - assign ID, add to game
        String playerName = (String) msg.get("playerName");
        int playerId = nextPlayerId++;
        clients.put(playerId, conn);
        playerNames.put(playerId, playerName);
        connectionToPlayerId.put(conn, playerId);
        // Send welcome message with player ID
        
    } else if ("move".equals(type)) {
        // Player movement - update bucket position
        String direction = (String) msg.get("direction");
        // Update bucket position based on direction
        
    } else if ("moveTo".equals(type)) {
        // Direct position update (for mouse/touch)
        Double x = (Double) msg.get("x");
        // Set bucket to specific x coordinate
    }
    
    // 3. Broadcast updated game state to all clients
    broadcastGameState();
}
```

**What onMessage() Does:**
1. **Receives** JSON messages from any connected client
2. **Parses** the message to determine action type
3. **Updates** game state based on the message
4. **Broadcasts** the new state to all connected players

---

## Client Side Implementation

### Main.java (LibGDX Client)

**WebSocket Client Setup:**
```java
public class Main extends ApplicationAdapter {
    private WebSocketClient wsClient;
    private int myPlayerId = -1;
    
    // Connect to server
    private void connectToServer(String serverUri, String playerName) {
        URI uri = URI.create(serverUri);
        wsClient = new WebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                // Handle server messages (game state updates)
                updateGameState(message);
            }
        };
        wsClient.connect();
        // Send join message
        sendJoinMessage(playerName);
    }
}
```

**Message Flow:**
1. Client sends movement commands to server
2. Server updates game state
3. Server broadcasts new state to all clients
4. Clients update their local display

---

## WebSocket Protocol Messages

### Message Types

**1. Join Message (Client → Server):**
```json
{
    "type": "join",
    "playerName": "Player1"
}
```

**2. Movement Message (Client → Server):**
```json
{
    "type": "move",
    "direction": "left"
}
```

**3. Position Message (Client → Server):**
```json
{
    "type": "moveTo",
    "x": 3.5
}
```

**4. Game State Message (Server → All Clients):**
```json
{
    "type": "gameState",
    "scoreboard": {"1": 5, "2": 3},
    "playerNames": {"1": "Alice", "2": "Bob"},
    "bucketPositions": {"1": 2.5, "2": 4.0},
    "drops": [{"x": 1.5, "y": 3.2}],
    "worldWidth": 8.0,
    "worldHeight": 5.0
}
```

---

## Deployment Architecture

### Local Development
```bash
# Start server locally
./gradlew :core:run --args='io.github.Games.Drop.WebGameServer'

# Start client locally  
./gradlew :lwjgl3:run -PplayerName=Player1
```

### Docker Deployment

**1. Server Container (Dockerfile):**
```dockerfile
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY games/core/build/libs/drop-server-1.0.0-all.jar /app/drop-server.jar
EXPOSE 8887
CMD ["java", "-cp", "drop-server.jar", "io.github.Games.Drop.WebGameServer"]
```

**2. Client Container (Dockerfile.client):**
```dockerfile
FROM eclipse-temurin:8-jre
# Copy client JAR and run LibGDX client
```

**3. Websockify Proxy (Dockerfile.websockify):**
```dockerfile
FROM python:3.11-slim
RUN pip install websockify
COPY games/start-websockify.sh /app/start-websockify.sh
CMD ["/app/start-websockify.sh"]
```

---

## Websockify Proxy Explained

### What is Websockify?

**Purpose:** Bridges WebSocket connections (browsers) with TCP connections (Java server)

**The start-websockify.sh Script:**
```bash
#!/bin/sh
# Use Render.com provided PORT or default to 10000
PUBLIC_PORT="${PORT:-10000}"
# Target host and port (Java server)
TARGET_HOST="${TARGET_HOST:-localhost}" 
TARGET_PORT="${TARGET_PORT:-8887}"

# Start websockify proxy
exec websockify --web=./ --verbose "$PUBLIC_PORT" "$TARGET_HOST:$TARGET_PORT"
```

**What It Does:**
1. **Listens** on public port (10000) for WebSocket connections
2. **Forwards** WebSocket messages to TCP port (8887) where Java server runs
3. **Converts** between WebSocket protocol and raw TCP
4. **Serves** static web files for browser clients

**Why Needed:**
- Browsers can only make WebSocket connections
- Java server uses TCP sockets
- Cloud platforms (Render.com) typically only expose ports 80/443
- Websockify translates between these protocols

---

## Cloud Deployment Process

### 1. GitHub Repository
- Code pushed to: https://github.com/saloni-agarwal-ing/Chapter_Games
- Contains all source code, Dockerfiles, and configuration

### 2. Render.com Deployment
- **Service URL:** https://chapter-games-websockify.onrender.com
- **Architecture:** Websockify proxy → Java game server
- **Port Configuration:** 443 (HTTPS) → 10000 (websockify) → 8887 (Java server)

### 3. Connection Testing
```bash
# Test HTTPS connectivity
nc -vz chapter-games-websockify.onrender.com 443
# Result: Connection succeeded!

# Test WebSocket proxy port  
nc -vz chapter-games-websockify.onrender.com 10000
# Result: Connection timeout (port not exposed externally)
```

### 4. Client Connection String
```bash
# For desktop clients
./gradlew :lwjgl3:run -PplayerName=Player1 \
  -DgameServerUri=wss://chapter-games-websockify.onrender.com

# For Docker clients
docker run --env GAME_SERVER_URI=wss://chapter-games-websockify.onrender.com \
  --env PLAYER_NAME=Player1 yu69yj/drop-client:latest
```

---

## Technical Challenges & Solutions

### 1. Network Restrictions
**Problem:** ING network blocked Docker Hub access
**Solution:** Deployed to public cloud (Render.com) for external access

### 2. Protocol Mismatch  
**Problem:** Browsers need WebSocket, Java server uses TCP
**Solution:** Websockify proxy to bridge protocols

### 3. Port Limitations
**Problem:** Cloud platforms limit exposed ports
**Solution:** Use standard HTTPS (443) with websockify routing

### 4. Real-time Synchronization
**Problem:** Multiple players need consistent game state  
**Solution:** Server authoritative model with broadcast updates

---

## Game Flow Summary

### 1. Player Joins Game
1. Client connects to WebSocket server
2. Sends "join" message with player name
3. Server assigns unique player ID
4. Server adds player to game world
5. Server broadcasts updated game state

### 2. Gameplay Loop  
1. Player moves bucket (keyboard/mouse input)
2. Client sends movement message to server
3. Server updates bucket position
4. Server runs game logic (drop movement, collision detection)
5. Server broadcasts new game state to all clients
6. Clients render updated positions

### 3. Scoring & Competition
1. Server detects drop-bucket collisions
2. Updates player score
3. Broadcasts scoreboard to all players
4. Real-time leaderboard display

---

## Key Features Demonstrated

### Real-time Multiplayer
- WebSocket bidirectional communication
- Server-authoritative game state
- Consistent synchronization across clients

### Scalable Architecture  
- Docker containerization
- Cloud deployment
- Protocol bridging (WebSocket ↔ TCP)

### Cross-platform Support
- LibGDX for desktop clients
- WebSocket for browser access
- Docker for deployment consistency

### Network Programming
- Java WebSocket server implementation
- Message serialization with JSON/Gson
- Connection management and error handling

---

## Conclusion

The Drop Game project demonstrates:

1. **Modern multiplayer game architecture** with real-time communication
2. **Practical WebSocket implementation** for game networking  
3. **Docker deployment strategies** for multiplayer games
4. **Protocol bridging techniques** for browser compatibility
5. **Cloud deployment solutions** for global accessibility

**Technical Stack Mastery:**
- Java WebSocket programming
- LibGDX game development
- Docker containerization 
- Cloud deployment (Render.com)
- Network protocol translation (websockify)

This project showcases full-stack game development from local prototyping to production cloud deployment.

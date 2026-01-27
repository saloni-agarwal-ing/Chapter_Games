# Drop Game - Multiplayer WebSocket Implementation
## Project Overview & Architecture

---

## Slide 1: Project Overview
### Drop Game - Multiplayer LibGDX Game

**What is Drop Game?**
- A multiplayer arcade-style game built with LibGDX
- Players control buckets to catch falling drops
- Real-time multiplayer gameplay using WebSocket communication
- Cross-platform deployment (Desktop, Docker, Cloud)

**Key Features:**
- Real-time synchronization between players
- WebSocket-based networking
- Docker containerization
- Cloud deployment with websockify proxy
- Player scoring and leaderboard

---

## Slide 2: Game Architecture Overview

```
┌─────────────────┐    WebSocket     ┌─────────────────┐
│   Game Client   │ ←──────────────→ │   Game Server   │
│   (LibGDX)      │    ws://host:8887│  (WebSocket)    │
└─────────────────┘                  └─────────────────┘
        │                                     │
        │                                     │
   ┌────▼────┐                          ┌────▼────┐
   │ Player  │                          │ Game    │
   │ Input   │                          │ Logic   │
   │Handler  │                          │ Engine  │
   └─────────┘                          └─────────┘
```

**Components:**
- **Client (Main.java)**: LibGDX game rendering and input handling
- **Server (WebGameServer.java)**: Game state management and WebSocket communication
- **WebSocket Protocol**: Real-time bidirectional communication
- **Docker Containers**: Deployment and scaling

---

## Slide 3: Game Mechanics - How Drop Game Works

**Core Gameplay Loop:**

1. **Drop Spawning**
   - Server spawns drops at random X positions every second
   - Drops fall from top (Y=5) to bottom (Y=0) at constant speed

2. **Player Movement**
   - Players control bucket position using keyboard (left/right) or mouse
   - Bucket position constrained within world boundaries (0 to 8 units)

3. **Collision Detection**
   - Server checks if falling drops intersect with player buckets
   - When collision detected: player score increases, drop disappears

4. **Game State Synchronization**
   - Server broadcasts game state to all clients 20 times per second
   - Includes: player positions, scores, active drops, world dimensions

**Game World:**
- World Size: 8 × 5 units
- Bucket Size: 1 × 1 units  
- Drop Size: 0.7 × 0.7 units
- Drop Speed: 2.5 units/second

---

## Slide 4: WebSocket Implementation - Client Side

**Client Connection (Main.java)**

```java
// WebSocket connection setup
URI uri = new URI("ws://localhost:8887");
wsClient = new WebSocketClient(uri) {
    @Override
    public void onOpen(ServerHandshake handshake) {
        // Send join message with player name
        Map<String, Object> join = new HashMap<>();
        join.put("type", "join");
        join.put("playerName", myName);
        wsClient.send(gson.toJson(join));
    }
    
    @Override
    public void onMessage(String message) {
        // Parse game state updates from server
        handleGameStateUpdate(message);
    }
};
```

**Message Types Sent by Client:**
- `join`: Initial connection with player name
- `move`: Keyboard-based movement (left/right)
- `moveTo`: Mouse-based absolute positioning

**Message Processing:**
- Receives `welcome` message with assigned player ID
- Processes `gameState` updates containing all game data
- Updates local game state for rendering

---

## Slide 5: WebSocket Implementation - Server Side

**Server Structure (WebGameServer.java)**

```java
public class WebGameServer extends WebSocketServer {
    private Map<Integer, WebSocket> clients = new HashMap<>();
    private Map<Integer, Integer> scoreboard = new HashMap<>();
    private Map<Integer, String> playerNames = new HashMap<>();
    private List<Map<String, Float>> drops = new ArrayList<>();
    
    // Game loop running at 20 FPS
    timer.scheduleAtFixedRate(() -> updateGameLogic(), 0, 50);
}
```

**Key Server Functions:**

1. **Connection Management**
   - Assigns unique player IDs
   - Tracks player connections and names
   - Handles disconnections gracefully

2. **Game Logic Processing**
   - Updates drop positions (physics simulation)
   - Collision detection between drops and buckets
   - Score calculation and tracking

3. **State Broadcasting**
   - Sends complete game state to all connected clients
   - Ensures synchronization across all players

---

## Slide 6: WebSocket Message Protocol

**Message Structure (JSON Format)**

**Client → Server Messages:**
```json
// Join game
{"type": "join", "playerName": "PlayerName"}

// Move bucket
{"type": "move", "direction": "left|right"}
{"type": "moveTo", "x": 3.5}
```

**Server → Client Messages:**
```json
// Welcome with player ID
{"type": "welcome", "playerId": 1}

// Complete game state
{
  "type": "gameState",
  "scoreboard": {"1": 5, "2": 3},
  "playerNames": {"1": "Alice", "2": "Bob"},
  "bucketPositions": {"1": 2.5, "2": 4.0},
  "drops": [{"x": 1.5, "y": 3.2}],
  "playerColors": {"1": "#FF5733", "2": "#33FF57"}
}
```

**Communication Pattern:**
- Client sends input events to server
- Server processes all game logic centrally
- Server broadcasts authoritative game state to all clients

---

## Slide 7: Project Structure & Build System

**Module Organization:**
```
games/
├── core/                    # Shared game logic
│   ├── Main.java           # LibGDX client implementation
│   └── WebGameServer.java  # WebSocket server
├── lwjgl3/                 # Desktop launcher
│   └── Lwjgl3Launcher.java # Main entry point
└── assets/                 # Game resources (images, sounds)
```

**Build System (Gradle):**
```groovy
// Multi-module project
// Core module: Contains shared game logic
// LWJGL3 module: Desktop platform launcher
// Shadow plugin: Creates fat JARs for deployment
```

**Dependencies:**
- **LibGDX**: Game framework for rendering and input
- **Java-WebSocket**: WebSocket client/server implementation
- **Gson**: JSON serialization/deserialization
- **LWJGL3**: OpenGL bindings for desktop

---

## Slide 8: Docker Containerization Strategy

**Three-Container Architecture:**

**1. Game Server Container (Dockerfile)**
```dockerfile
FROM eclipse-temurin:8-jre
COPY core/build/libs/drop-server-1.0.0-all.jar /app/
EXPOSE 8887
CMD ["java", "-cp", "drop-server.jar", "io.github.Games.Drop.WebGameServer"]
```

**2. Game Client Container (Dockerfile.client)**
```dockerfile
FROM eclipse-temurin:8-jre
COPY lwjgl3/build/libs/Games-1.0.0.jar /app/
COPY assets /app/assets
ENV GAME_SERVER_URI=wss://chapter-games-new.onrender.com
CMD ["java", "-jar", "Games-1.0.0.jar"]
```

**3. WebSockify Proxy Container (Dockerfile.websockify)**
```dockerfile
FROM python:3.11-slim
RUN pip install websockify
# Proxies WebSocket traffic for cloud deployment
CMD ["websockify", "10000", "localhost:8887"]
```

---

## Slide 9: Deployment Architecture

**Local Development:**
```
Client ←→ WebSocket (ws://localhost:8887) ←→ Server
```

**Docker Local Network:**
```
Client Container ←→ Server Container
    ↓                     ↓
Network Bridge (docker)
```

**Cloud Deployment (Render.com):**
```
Browser/Client ←→ HTTPS/WSS ←→ Websockify Proxy ←→ Game Server
    (Port 443)              (Port 10000)        (Port 8887)
```

**Why Websockify Proxy?**
- Cloud platforms typically only expose HTTP/HTTPS ports (80/443)
- Websockify translates WebSocket traffic to TCP for internal communication
- Enables browser-based clients to connect to TCP-based game servers
- Provides SSL termination for secure WebSocket connections (WSS)

---

## Slide 10: Networking & Communication Flow

**Complete Communication Flow:**

1. **Client Startup**
   ```java
   // Client connects to server URI
   String uri = System.getProperty("gameServerUri", "ws://localhost:8887");
   wsClient = new WebSocketClient(new URI(uri));
   ```

2. **Player Join Process**
   ```
   Client → Server: {"type": "join", "playerName": "Alice"}
   Server → Client: {"type": "welcome", "playerId": 1}
   Server → All: broadcasts updated game state
   ```

3. **Gameplay Loop**
   ```
   Input Event → Client processes → Send to Server
   Server processes all inputs → Updates game state
   Server → All Clients: broadcasts new game state
   Clients render updated state
   ```

4. **Real-time Synchronization**
   - Server runs at 20 FPS (50ms intervals)
   - All game logic processed server-side (authoritative)
   - Clients act as "thin clients" - rendering only

---

## Slide 11: Challenges & Solutions

**Challenge 1: Network Firewall Issues**
- **Problem**: Corporate networks block non-HTTP traffic
- **Solution**: Websockify proxy converts WebSocket to HTTPS
- **Implementation**: Deploy proxy container on cloud platform

**Challenge 2: Real-time Synchronization**
- **Problem**: Multiple clients need consistent game state
- **Solution**: Server-authoritative architecture
- **Implementation**: Server processes all logic, broadcasts state

**Challenge 3: Cross-platform Deployment**
- **Problem**: Different environments (local, Docker, cloud)
- **Solution**: Environment-based configuration
- **Implementation**: System properties and environment variables

```java
// Flexible server URI configuration
String uri = System.getProperty("gameServerUri");
if (uri == null) uri = System.getenv("GAME_SERVER_URI");
if (uri == null) uri = "ws://localhost:8887"; // default
```

---

## Slide 12: Cloud Deployment Details

**Render.com Deployment Process:**

1. **Repository Setup**
   - Push code to GitHub: `github.com/saloni-agarwal-ing/Chapter_Games`
   - Connect Render.com to GitHub repository

2. **Service Configuration**
   - **Web Service**: Runs websockify proxy
   - **Background Service**: Could run game server (if needed)
   - **Environment Variables**: Configure target host/port

3. **Websockify Configuration**
   ```bash
   # start-websockify.sh
   PUBLIC_PORT="${PORT:-10000}"
   TARGET_HOST="${TARGET_HOST:-localhost}"  
   TARGET_PORT="${TARGET_PORT:-8887}"
   websockify "$PUBLIC_PORT" "$TARGET_HOST:$TARGET_PORT"
   ```

4. **Client Connection**
   ```
   wss://chapter-games-websockify.onrender.com
   ```

**Current Status:**
- ✅ HTTPS connectivity (port 443): Working
- ❌ WebSocket proxy (port 10000): Timeout issue
- **Next Steps**: Debug Render.com port configuration

---

## Slide 13: Running the Application

**Local Development:**
```bash
# Terminal 1: Start the server
./gradlew :core:run --args='io.github.Games.Drop.WebGameServer'

# Terminal 2: Start client
./gradlew :lwjgl3:run -PplayerName=Alice

# Terminal 3: Start another client  
./gradlew :lwjgl3:run -PplayerName=Bob
```

**Docker Deployment:**
```bash
# Build images
docker build -t drop-server -f Dockerfile .
docker build -t drop-client -f Dockerfile.client .

# Run server
docker run -p 8887:8887 drop-server

# Run clients (on different machines)
docker run --env SERVER_IP=192.168.1.100 --env PLAYER_NAME=Alice drop-client
```

**Cloud Connection:**
```bash
./gradlew :lwjgl3:run -PplayerName=Alice -DgameServerUri=wss://chapter-games-websockify.onrender.com
```

---

## Slide 14: Technical Highlights

**Real-time Game Features:**

1. **Smooth Movement**
   - Client-side prediction for responsive controls
   - Server validation and correction
   - Support for both keyboard and mouse input

2. **Visual Features**
   - Each player assigned unique color
   - Real-time scoreboard display
   - Smooth drop physics animation

3. **Scalable Architecture**
   - WebSocket handles multiple concurrent players
   - Stateless server design (could be horizontally scaled)
   - Efficient JSON message protocol

**Performance Optimizations:**
- Fixed tick rate (20 FPS) for consistent gameplay
- Batched state updates (not per-action)
- Efficient collision detection (AABB)
- Memory management for drop objects

---

## Slide 15: Future Enhancements

**Potential Improvements:**

1. **Gameplay Features**
   - Power-ups and special drops
   - Different game modes (timed, survival)
   - Player levels and progression
   - Spectator mode

2. **Technical Enhancements**
   - WebRTC for peer-to-peer gameplay
   - Redis for session persistence
   - Load balancing for multiple server instances
   - Mobile client (LibGDX Android)

3. **User Experience**
   - Web-based client (HTML5/WebGL)
   - Lobby system for match-making
   - Replay system
   - Tournament mode

4. **Infrastructure**
   - Kubernetes deployment
   - Auto-scaling based on player count
   - Global CDN for assets
   - Analytics and monitoring

---

## Slide 16: Conclusion

**Project Summary:**
- Successfully implemented multiplayer Drop game using LibGDX + WebSocket
- Demonstrated real-time networking with authoritative server architecture
- Achieved cross-platform deployment (local, Docker, cloud)
- Learned containerization and cloud deployment strategies

**Key Technical Achievements:**
✅ WebSocket communication protocol design  
✅ Real-time game state synchronization  
✅ Docker containerization strategy  
✅ Cloud deployment with proxy configuration  
✅ Multi-platform client support  

**Lessons Learned:**
- WebSocket provides excellent foundation for real-time multiplayer games
- Server-authoritative architecture prevents cheating and ensures consistency
- Docker simplifies deployment across different environments
- Cloud deployment requires careful networking configuration
- LibGDX offers powerful cross-platform game development capabilities

**Next Steps:**
- Resolve Render.com WebSocket proxy configuration
- Implement web-based client for broader accessibility
- Add more engaging gameplay mechanics
- Scale for larger player counts

---

## Thank You!
### Questions & Discussion

**Repository**: https://github.com/saloni-agarwal-ing/Chapter_Games  
**Live Demo**: https://chapter-games-websockify.onrender.com  

**Technologies Used:**
- Java 8 + LibGDX
- WebSocket (Java-WebSocket library)
- Docker & Docker Hub
- Render.com Cloud Platform
- Websockify Proxy
- Gradle Build System

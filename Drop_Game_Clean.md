---
title: "Drop Game - Multiplayer WebSocket Implementation"
subtitle: "Project Overview & Architecture"
author: "Your Name"
date: "January 2026"
---

# Project Overview

## What is Drop Game?

- A multiplayer arcade-style game built with LibGDX
- Players control buckets to catch falling drops
- Real-time multiplayer gameplay using WebSocket communication
- Cross-platform deployment (Desktop, Docker, Cloud)

## Key Features

- Real-time synchronization between players
- WebSocket-based networking
- Docker containerization
- Cloud deployment with websockify proxy
- Player scoring and leaderboard

# Game Architecture

## System Architecture

```
┌─────────────────┐    WebSocket     ┌─────────────────┐
│   Game Client   │ ←──────────────→ │   Game Server   │
│   (LibGDX)      │    ws://host:8887│  (WebSocket)    │
└─────────────────┘                  └─────────────────┘
```

## Components

- **Client (Main.java)**: LibGDX game rendering and input handling
- **Server (WebGameServer.java)**: Game state management and WebSocket communication
- **WebSocket Protocol**: Real-time bidirectional communication
- **Docker Containers**: Deployment and scaling

# Game Mechanics

## Core Gameplay Loop

1. **Drop Spawning**: Server spawns drops at random positions every second
2. **Player Movement**: Players control bucket using keyboard/mouse
3. **Collision Detection**: Server checks drop-bucket intersections
4. **State Synchronization**: Server broadcasts game state 20 times per second

## Game World Specifications

- World Size: 8 × 5 units
- Bucket Size: 1 × 1 units
- Drop Size: 0.7 × 0.7 units
- Drop Speed: 2.5 units/second

# WebSocket Implementation - Client

## Connection Setup

```java
URI uri = new URI("ws://localhost:8887");
wsClient = new WebSocketClient(uri) {
    @Override
    public void onOpen(ServerHandshake handshake) {
        Map<String, Object> join = new HashMap<>();
        join.put("type", "join");
        join.put("playerName", myName);
        wsClient.send(gson.toJson(join));
    }
};
```

## Message Types

- `join`: Initial connection with player name
- `move`: Keyboard-based movement
- `moveTo`: Mouse-based positioning

# WebSocket Implementation - Server

## Server Structure

```java
public class WebGameServer extends WebSocketServer {
    private Map<Integer, WebSocket> clients = new HashMap<>();
    private Map<Integer, Integer> scoreboard = new HashMap<>();
    private List<Map<String, Float>> drops = new ArrayList<>();
    
    // Game loop at 20 FPS
    timer.scheduleAtFixedRate(() -> updateGameLogic(), 0, 50);
}
```

## Key Functions

1. **Connection Management**: Player IDs and tracking
2. **Game Logic Processing**: Physics and collision detection
3. **State Broadcasting**: Synchronization across clients

# Docker Deployment

## Three-Container Architecture

### Game Server Container

```dockerfile
FROM eclipse-temurin:8-jre
COPY core/build/libs/drop-server-1.0.0-all.jar /app/
EXPOSE 8887
CMD ["java", "-cp", "drop-server.jar", 
     "io.github.Games.Drop.WebGameServer"]
```

### Websockify Proxy Container

```dockerfile
FROM python:3.11-slim
RUN pip install websockify
CMD ["websockify", "10000", "localhost:8887"]
```

# Deployment Architecture

## Local vs Cloud Deployment

- **Local**: `Client ←→ WebSocket ←→ Server`
- **Cloud**: `Browser ←→ HTTPS/WSS ←→ Websockify ←→ Server`

## Why Websockify?

- Cloud platforms expose HTTP/HTTPS ports only
- Translates WebSocket to TCP for internal communication
- Provides SSL termination for WSS connections

# Running the Application

## Local Development

```bash
# Start server
./gradlew :core:run --args='io.github.Games.Drop.WebGameServer'

# Start client
./gradlew :lwjgl3:run -PplayerName=Alice
```

## Docker Deployment

```bash
docker build -t drop-server -f Dockerfile .
docker run -p 8887:8887 drop-server
```

# Technical Highlights

## Real-time Features

- Client-side prediction for responsive controls
- Server validation and correction
- Unique player colors and real-time scoreboard
- Smooth physics animation

## Performance Optimizations

- Fixed 20 FPS tick rate
- Batched state updates
- Efficient collision detection (AABB)
- Memory management for game objects

# Challenges & Solutions

## Network Firewall Issues

- **Problem**: Corporate networks block non-HTTP traffic
- **Solution**: Websockify proxy converts WebSocket to HTTPS
- **Implementation**: Deploy proxy container on cloud platform

## Real-time Synchronization

- **Problem**: Multiple clients need consistent game state
- **Solution**: Server-authoritative architecture
- **Implementation**: Server processes all logic, broadcasts state

# Cloud Deployment Details

## Render.com Deployment Process

1. **Repository Setup**: Push to GitHub
2. **Service Configuration**: Web service runs websockify proxy
3. **Environment Variables**: Configure target host/port
4. **Client Connection**: `wss://chapter-games-websockify.onrender.com`

## Current Status

- ✅ HTTPS connectivity (port 443): Working
- ❌ WebSocket proxy (port 10000): Timeout issue
- **Next Steps**: Debug Render.com port configuration

# Future Enhancements

## Potential Improvements

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

# Conclusion

## Key Achievements

✅ WebSocket communication protocol design  
✅ Real-time game state synchronization  
✅ Docker containerization strategy  
✅ Cloud deployment with proxy configuration  
✅ Multi-platform client support  

## Lessons Learned

- WebSocket excellent for real-time multiplayer games
- Server-authoritative architecture prevents cheating
- Docker simplifies cross-environment deployment
- Cloud deployment requires careful network configuration

# Thank You

## Questions & Discussion

**Repository**: https://github.com/saloni-agarwal-ing/Chapter_Games  
**Technologies**: Java 8, LibGDX, WebSocket, Docker, Render.com

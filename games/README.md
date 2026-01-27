# Drop Game - Multiplayer WebSocket Game

A real-time multiplayer arcade game built with LibGDX and Java WebSockets. Players control buckets to catch falling raindrops while competing against each other.

## 🎮 Live Demo

**Play now:** https://chapter-games-1.onrender.com

## 🚀 Features

- **Real-time multiplayer** - Multiple players in the same game world
- **Cross-platform** - Desktop client and web browser support  
- **WebSocket networking** - Smooth, responsive gameplay
- **Player customization** - Unique colors for each player
- **Live scoreboard** - Real-time score tracking
- **Cloud deployment** - Hosted on Render.com

## 🎯 How to Play

### Web Browser
1. Visit https://chapter-games-1.onrender.com
2. Enter your player name
3. Use arrow keys (←→) or A/D keys to move your bucket
4. Catch falling raindrops to score points!

### Desktop Client
```bash
./gradlew :lwjgl3:run -PplayerName=YourName -DgameServerUri=wss://chapter-games-1.onrender.com
```

## 🏗 Architecture

```
Web Browser → WebSocket → Websockify Proxy → Java Game Server
Desktop Client → WebSocket → Java Game Server
```

- **Java WebSocket Server** (`WebGameServer.java`) - Game logic and state management
- **LibGDX Client** (`Main.java`) - Desktop game rendering  
- **Web Client** (`webclient.html`) - Browser-based gameplay
- **Websockify Proxy** - Bridges browser WebSockets with Java server

## 🛠 Development

### Local Development
```bash
# Start server locally
java -jar core/build/libs/drop-server-1.0.0-all.jar

# Start desktop client
./gradlew :lwjgl3:run -PplayerName=Player1 -DgameServerUri=ws://localhost:8887
```

### Build JAR
```bash
./gradlew :core:shadowJar
```

### Docker Deployment
Uses `Dockerfile.final` with combined Java server + websockify proxy setup.

## 📁 Project Structure

```
games/
├── Dockerfile.final          # Production deployment
├── webclient.html            # Web browser client
├── core/src/main/java/
│   └── WebGameServer.java    # Server-side game logic
│   └── Main.java            # Desktop client
├── assets/                   # Game sprites and sounds
└── build.gradle             # Build configuration
```

## 🎯 Game Mechanics

- **World**: 8x5 unit coordinate system
- **Physics**: Gravity affects falling drops (2.5 units/second)
- **Movement**: Bucket speed 2.5 units/second
- **Scoring**: +1 point per caught raindrop
- **Spawn Rate**: New raindrop every 1 second

## 🌐 Deployment

Currently deployed on Render.com using:
- **Service URL**: https://chapter-games-1.onrender.com  
- **Docker Configuration**: `Dockerfile.final`
- **Port Setup**: Websockify proxy on public port → Java server on port 8887

---

**Built with:** LibGDX • Java WebSockets • Docker • Render.com

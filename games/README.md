# Drop

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

Build the project:
./gradlew clean :core:build

Run the command:
cd '/Users/yu69yj/Documents/workspace/Chapter_Games/games'

# find your local IP address:
ifconfig | grep inet
# Look for an IP address like 192.168.x.x or 10.x.x.x.
# Home wifi ip address is 192.168.1.248


# To run the server locally. This runs the web server on the port 8887
./gradlew :core:run --args='io.github.Games.Drop.WebGameServer'

# To run the client locally 
./gradlew :lwjgl3:run -PplayerName=Erish

# Multiplayer Drop Game with Docker

This guide explains how to run the Drop Game in server-client mode using Docker containers, so multiple players can play on different machines.

---

## 1. Build Docker Images

### Build the Server Image
```
docker build -t drop-server -f Dockerfile.server .
```

### Build the Client Image
```
docker build -t drop-client -f Dockerfile.client .
```

---

## 2. Share Docker Images

### Push to Docker Hub
1. Log in to Docker Hub:
   ```
   docker login
   ```
2. Tag and push your images:
   ```
   docker tag drop-client yu69yj/drop-client:latest
   docker push yu69yj/drop-client:latest
   ```
   Repeat for `drop-server` if needed.

### Pull from Docker Hub (on another machine)
```
docker pull yu69yj/drop-client:latest
```

---
## 3. Find Your Server IP Address

Run:
```
ifconfig | grep inet
```
Look for an IP address like `192.168.x.x` or `10.x.x.x` (not `127.0.0.1`).
**Note:** This IP may change if you connect to a different WiFi/network.

---

## 4. Run the Server Container

Expose the game port (e.g., 8887):
```
docker run -p 8887:8887 <your-dockerhub-username>/drop-server:latest
```

---

## 5. Run the Client Container

To connect to the server, pass the server IP and player name as environment variables or arguments (update Dockerfile/client code if needed):
```
docker run --env SERVER_IP=<server-ip> --env PLAYER_NAME=<your-name> <your-dockerhub-username>/drop-client:latest
```
Or, if your client expects command-line arguments:
```
docker run <your-dockerhub-username>/drop-client:latest <server-ip> <your-name>
```

---

## 6. Example Usage
- Server runs on host with IP `192.168.1.248`:
  ```
  docker run -p 8887:8887 yu69yj/drop-server:latest
  ```
- Client on another machine:
  ```
  docker run --env SERVER_IP=192.168.1.248 --env PLAYER_NAME=Erish yu69yj/drop-client:latest
  ```

---

## 7. Troubleshooting
- **Class not found:** Ensure your JAR contains the correct main class and is copied in Dockerfile.
- **Port conflicts:** Make sure port 8887 is not used by other apps.
- **Network issues:** Both machines must be on the same network, and firewalls must allow traffic on the game port.
- **Cannot ping server IP:**
    - Check your computer's firewall settings and allow incoming connections for Docker and the game server port (e.g., 8887).
    - Ensure both devices are on the same WiFi or LAN and not on isolated guest networks or VPNs.
    - Verify Docker port mapping (`-p 8887:8887`) and that the server is running and listening on the correct IP.
    - Check router settings for "AP Isolation" or "Client Isolation" and disable if enabled.
    - Confirm you are using the correct local IP address (not 127.0.0.1 or localhost).
    - On Linux, use `sudo ufw allow 8887/tcp` to open the port if using a firewall.
    - Test connectivity with `ping <server-ip>` and `telnet <server-ip> 8887` from the client machine.
    - Restart your router or devices if network issues persist.
- **IP changes:** If server switches WiFi, re-check IP and inform clients.

---

## 8. Advanced: Customizing Client Connection
If your client needs to specify server IP or player name via command-line or environment, update your Dockerfile/client code to accept these.

---

## 9. Local Run (for reference)
```
./gradlew :core:run --args='io.github.Games.Drop.WebGameServer'
./gradlew :lwjgl3:run -PplayerName=Erish
```

---

## 10. How to Push Your Project to GitHub

1. Create a new repository on [GitHub](https://github.com/new).
2. Open a terminal in your project directory.
3. Initialize git (if not already done):
   ```
   git init
   ```
4. Add all files:
   ```
   git add .
   ```
5. Commit your files:
   ```
   git commit -m "Initial commit"
   ```
6. Add the remote repository (replace `<your-username>` and `<repo-name>`):
   ```
   git remote add origin https://github.com/<your-username>/<repo-name>.git
   ```
   If you see a 403 error (e.g., `fatal: unable to access ... 403`), you do not have permission to access that repository. Remove the remote and add a remote for a repository you own:
   ```
   git remote remove origin
   git remote add origin https://github.com/<your-username>/<your-new-repo>.git
   ```
   Make sure you use a repository you own or have collaborator access to. Do not use someone else's repository unless you have explicit permission.
   If you can access a repository in your browser but get a 403 error when pushing, you likely only have read access. In this case, fork the repository to your own GitHub account:
   1. Go to the repository page in your browser and click **Fork**.
   2. This creates a copy under your account (e.g., https://github.com/<your-username>/DropGame).
   3. Change your git remote to your fork:
      ```
      git remote remove origin
      git remote add origin https://github.com/<your-username>/DropGame.git
      ```
   4. Push to your fork using your personal access token.
7. Push to GitHub:
   ```
   git push -u origin master
   ```
   (If your default branch is `main`, use `main` instead of `master`.)

   **Note:** GitHub no longer supports password authentication for Git operations. You must use a [personal access token (PAT)](https://github.com/settings/tokens) as your password when prompted. Generate a token with `repo` scope, copy it, and use it in place of your password. You can cache your credentials with:
   - macOS: `git config --global credential.helper osxkeychain`
   - Windows: `git config --global credential.helper wincred`
   - Linux: `git config --global credential.helper cache`


---


# Connectivity Test (Cloud Deployment)

To test HTTPS connectivity to your deployed service:
```
nc -vz chapter-games-websockify.onrender.com 443
```

To test WebSocket connectivity (proxy):
```
nc -vz chapter-games-websockify.onrender.com 10000
```

If port 10000 times out:
- Check Render service settings to ensure port 10000 is exposed.
- Verify the websockify process is running and listening on port 10000.
- Check for firewall or network restrictions on Render or your client.
- See Render docs: https://render.com/docs/networking#ports

You can also test HTTPS with:
```
curl -v https://chapter-games-websockify.onrender.com
```

Update your client connection string:
```
./gradlew :lwjgl3:run -PplayerName=Erish -DgameServerUri=wss://chapter-games-websockify.onrender.com
```
Or, if specifying the port:
```
./gradlew :lwjgl3:run -PplayerName=Erish -DgameServerUri=wss://chapter-games-websockify.onrender.com:10000
```

If you are running the client in Docker:
```
docker run --env GAME_SERVER_URI=wss://chapter-games-websockify.onrender.com:10000 --env PLAYER_NAME=Erish yu69yj/drop-client:latest
```



1) To run the server and client locally , run the below commands
   ./gradlew :core:run --args='io.github.Games.Drop.WebGameServer'
   ./gradlew :lwjgl3:run -PplayerName=Erish

2) I tried to build and deploy the image of server and client on docker , so that other players can also run the client on their local machine and we can play mutiple player game .
   a- the server runs fine on docker - can see message on docker WebGameServer started on port 8887
   b- the client on my local machine also works fine.
   c- But it was not possible for another person to pull the client image locally in their machine. It gave firewall network/infra/access related error since within ING network it is restricted.


3) I pushed my repo in the github so that i can deploy my repo on public cloud.
   a- Repo is available on github - https://github.com/saloni-agarwal-ing/Chapter_Games
   b- Then i used render.com for public cloud -  https://dashboard.render.com/project/prj-d4s36o24d50c73b7k920
   c- Used websocket to build the image on cloud so that other players can access it via browser.
      *   You want to expose a TCP service (e.g., a game server) to browsers via WebSockets.
      *   You deploy this container to a platform (like Render.com) that only exposes certain ports (usually 80/443).
      * The container listens on the platform’s assigned port and proxies WebSocket traffic to your backend service.
      d- the application is deployed successfully on public - https://dashboard.render.com/web/srv-d4td5a2li9vc73alhrs0
      e- nc -vz chapter-games-websockify.onrender.com 443 - shows succeeded "Connection to chapter-games-websockify.onrender.com port 443 [tcp/https] succeeded!"
      f- nc -vz chapter-games-websockify.onrender.com 10000 - shows timeout "nc: connect to chapter-games-websockify.onrender.com port 10000 (tcp) timed out: Operation now in progress"
      
## ✅ FIX FOR RENDER.COM PORT 10000 TIMEOUT

**Problem**: Render.com only exposes ports 80 (HTTP) and 443 (HTTPS) to the public internet. Port 10000 is not accessible externally.

**Solution**: Use the combined Docker approach or connect through standard HTTPS (port 443).

### Option 1: Deploy Combined Container (Recommended)

1. **Update your Render service** to use the combined Dockerfile:
   - In your Render dashboard, go to your service settings
   - Change the Dockerfile path to: `Dockerfile.combined`
   - Redeploy the service

2. **Test the connection** (should work now):
   ```bash
   # Test HTTPS connectivity (this should work)
   curl -v https://chapter-games-websockify.onrender.com
   
   # Test with browser
   # Open: https://chapter-games-websockify.onrender.com
   ```

3. **Connect desktop client**:
   ```bash
   # Use WSS (secure WebSocket) without specifying port
   ./gradlew :lwjgl3:run -PplayerName=Erish -DgameServerUri=wss://chapter-games-websockify.onrender.com
   ```

### Option 2: Fix Your Current Deployment

If you want to keep your current setup, ensure your websockify service uses Render's PORT environment variable:

1. **Verify your start-websockify.sh** uses `$PORT` (already fixed in the repository)
2. **Connect using HTTPS** instead of specifying port 10000:
   - ✅ Correct: `wss://chapter-games-websockify.onrender.com`  
   - ❌ Wrong: `wss://chapter-games-websockify.onrender.com:10000`

### Testing Commands That Should Work:

```bash
# Test basic connectivity (should succeed)
nc -vz chapter-games-websockify.onrender.com 443

# Test HTTPS (should return HTML or websockify page)
curl -v https://chapter-games-websockify.onrender.com

# Connect desktop client (use this format)
./gradlew :lwjgl3:run -PplayerName=Erish -DgameServerUri=wss://chapter-games-websockify.onrender.com

# Docker client connection
docker run --env GAME_SERVER_URI=wss://chapter-games-websockify.onrender.com --env PLAYER_NAME=Erish yu69yj/drop-client:latest
```

**Key Point**: Never specify port numbers when connecting to Render.com services. Use the bare domain name and let Render handle the port routing automatically.

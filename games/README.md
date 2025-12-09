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

## 11. Deploying the Drop Game Server on Railway (No Card Required)

1. Sign up at [Railway](https://railway.app/) with your GitHub account.
2. Click “New Project” and select “Deploy from GitHub repo”.
3. Connect your GitHub repository (created above).
4. Railway will detect your Dockerfile. If needed, set the start command (e.g., `java -jar /app/core-1.0.0.jar`).
5. Set the port to `8887` in Railway’s service settings.
6. Click “Deploy” and wait for the build to finish.
7. Railway will provide a public domain and port (e.g., `your-app.up.railway.app:12345`).
8. Share this address and port with your players. Clients should use this address to connect.

---

## How to Remove git from the Project

If you want to completely remove git tracking and history from this project:

1. Open a terminal in your project directory.
2. Run:
   ```
   rm -rf .git
   ```
   This deletes all git history and configuration.
3. Optionally, remove `.gitignore` and `.gitattributes` files:
   ```
   rm -f .gitignore .gitattributes
   ```
4. To verify removal, run:
   ```
   git status
   ```
   You should see: `fatal: not a git repository (or any of the parent directories): .git`

You can re-initialize git later with `git init` if needed.

# Testing Instructions

## Changes Made

### Bug Fix: Y-Coordinate Inversion
**Problem**: Drops and buckets were not appearing on the canvas because the Y-coordinate wasn't being inverted properly.

**Solution**: 
- Canvas has Y=0 at the TOP and increases downward
- Game world has Y=0 at the BOTTOM and increases upward
- Fixed the rendering code to invert Y coordinates: `y = (worldHeight - gameY) * scaleY`

### Debug Improvements
1. Added connection status indicator on the page
2. Added detailed console logging for all render operations
3. Added server status check display

## How to Test Locally

### Option 1: Test with local server
```bash
cd /Users/yu69yj/Documents/workspace/Chapter_Games/games

# Build the server JAR
./gradlew clean shadowJar

# Start the Java WebSocket server
java -jar core/build/libs/drop-server-1.0.0-all.jar

# In another terminal, start the web server
python3 start_webserver.py

# Open browser to http://localhost:10000
```

### Option 2: Quick test without building
If you just want to see the rendering fix without running the server:

1. Open webclient.html in a browser
2. Open browser console (F12)
3. The detailed logging will show you what's happening

## What You Should See

When you click "Join Game":

1. **Connection Status** should change from "Not connected" → "Connecting..." → "Connected" → "Joined as Player X"
2. **Console logs** should show:
   - WebSocket connection opened
   - Welcome message received
   - Game state messages arriving every 50ms
   - Details of drops and buckets being rendered

3. **On the canvas**:
   - Blue drops falling from top to bottom
   - Colored bucket(s) at the bottom
   - Player name above each bucket

## Deploy to Render

Once local testing is successful:

```bash
git add games/webclient.html
git commit -m "Fix Y-coordinate inversion for drops and buckets rendering"
git push origin main
```

Render will automatically redeploy when you push to main.

## Troubleshooting

### "No drops to draw" in console
- Drops spawn every 1 second
- Wait a few seconds after joining

### "No buckets to draw" in console  
- This means the WebSocket connection isn't working
- Check the connection status indicator
- Look for error messages in console

### Bucket appears but doesn't move
- Check that keyboard events are working
- Try the "Move Left" and "Move Right" buttons instead

### Server connection fails on Render
- Check Render logs for Java server startup
- Verify nginx is proxying WebSocket connections properly
- Make sure the PORT environment variable is set correctly


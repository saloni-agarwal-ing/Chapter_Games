package io.github.Games.Drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite; // Declare a new Sprite variable
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    private WebSocketClient wsClient;
    private Gson gson = new Gson();
    private int myPlayerId = -1;
    private String myName = "Player";
    private Map<Integer, Float> bucketPositions = new HashMap<>();
    private Map<Integer, String> playerNames = new HashMap<>();
    private Map<Integer, Integer> scoreboard = new HashMap<>();
    private List<Map<String, Float>> drops = new CopyOnWriteArrayList<>();
    private float worldWidth = 8f;
    private float worldHeight = 5f;
    private float bucketWidth = 1f;
    private float bucketHeight = 1f;
    private float dropWidth = 0.7f;
    private float dropHeight = 0.7f;
    BitmapFont font;
    private Map<Integer, String> playerColors = new HashMap<>();
    private long lastMoveTime = 0;
    private String lastDirectionSent = null;

    @Override
    public void create() {
        touchPos = new Vector2();

        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        music.setLooping(true);
        music.setVolume(.5f);
        music.play();

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        bucketSprite = new Sprite(bucketTexture); // Initialize the sprite based on the texture
        bucketSprite.setSize(1, 1); // Define the size of the sprite
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        // Prompt for player name (or use default)
        myName = System.getProperty("playerName", "Player");
        System.out.println("playerName property: " + System.getProperty("playerName"));
        System.out.println("myName: " + myName);
        connectWebSocket();
        font = new BitmapFont(Gdx.files.internal("ui/font.fnt"));
        font.getData().setScale(2f); // Increase font size for visibility
    }

    private String getServerUri() {
        // Try system property first, then environment variable, then default
        String uri = System.getProperty("gameServerUri");
        if (uri != null && !uri.isEmpty()) return uri;
        uri = System.getenv("GAME_SERVER_URI");
        if (uri != null && !uri.isEmpty()) return uri;
        return "ws://localhost:8887";
    }

    private void connectWebSocket() {
        try {

            URI uri = new URI(getServerUri());

            wsClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Map<String, Object> join = new HashMap<>();
                    join.put("type", "join");
                    join.put("playerName", myName);
                    wsClient.send(gson.toJson(join));
                }
                @Override
                public void onMessage(String message) {
                    Map<String, Object> msg = gson.fromJson(message, new TypeToken<Map<String, Object>>(){}.getType());
                    String type = (String) msg.get("type");
                    if ("welcome".equals(type)) {
                        myPlayerId = ((Double) msg.get("playerId")).intValue();
                    } else if ("gameState".equals(type)) {
                        // Update game state
                        // Parse scoreboard
                        Map<String, Double> rawScoreboard = (Map<String, Double>) msg.get("scoreboard");
                        scoreboard = new HashMap<>();
                        if (rawScoreboard != null) {
                            for (Map.Entry<String, Double> entry : rawScoreboard.entrySet()) {
                                scoreboard.put(Integer.parseInt(entry.getKey()), entry.getValue().intValue());
                            }
                        }
                        // Parse playerNames
                        Map<String, String> rawPlayerNames = (Map<String, String>) msg.get("playerNames");
                        playerNames = new HashMap<>();
                        if (rawPlayerNames != null) {
                            for (Map.Entry<String, String> entry : rawPlayerNames.entrySet()) {
                                playerNames.put(Integer.parseInt(entry.getKey()), entry.getValue());
                            }
                        }
                        System.out.println("Parsed playerNames: " + playerNames); // Debug log
                        // Parse bucketPositions
                        Map<String, Double> rawBucketPositions = (Map<String, Double>) msg.get("bucketPositions");
                        bucketPositions = new HashMap<>();
                        if (rawBucketPositions != null) {
                            for (Map.Entry<String, Double> entry : rawBucketPositions.entrySet()) {
                                bucketPositions.put(Integer.parseInt(entry.getKey()), entry.getValue().floatValue());
                            }
                        }
                        // Parse drops
                        List<Map<String, Object>> rawDrops = (List<Map<String, Object>>) msg.get("drops");
                        drops = new CopyOnWriteArrayList<>();
                        if (rawDrops != null) {
                            for (Map<String, Object> drop : rawDrops) {
                                Map<String, Float> parsedDrop = new HashMap<>();
                                for (Map.Entry<String, Object> entry : drop.entrySet()) {
                                    parsedDrop.put(entry.getKey(), ((Double) entry.getValue()).floatValue());
                                }
                                drops.add(parsedDrop);
                            }
                        }
                        // Parse world/game dimensions
                        worldWidth = ((Double) msg.get("worldWidth")).floatValue();
                        worldHeight = ((Double) msg.get("worldHeight")).floatValue();
                        bucketWidth = ((Double) msg.get("bucketWidth")).floatValue();
                        bucketHeight = ((Double) msg.get("bucketHeight")).floatValue();
                        dropWidth = ((Double) msg.get("dropWidth")).floatValue();
                        dropHeight = ((Double) msg.get("dropHeight")).floatValue();
                        // Parse playerColors
                        Map<String, String> rawPlayerColors = (Map<String, String>) msg.get("playerColors");
                        playerColors = new HashMap<>();
                        if (rawPlayerColors != null) {
                            for (Map.Entry<String, String> entry : rawPlayerColors.entrySet()) {
                                playerColors.put(Integer.parseInt(entry.getKey()), entry.getValue());
                            }
                        }
                    }
                }
                @Override
                public void onClose(int code, String reason, boolean remote) {}
                @Override
                public void onError(Exception ex) { ex.printStackTrace(); }
            };

            if ("wss".equalsIgnoreCase(uri.getScheme())) {
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                wsClient.setSocketFactory(sslContext.getSocketFactory());
            }

            wsClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
        input();
        draw();
    }

    private void input() {
        if (myPlayerId == -1) return;
        boolean moved = false;
        String direction = null;
        boolean mouseMoved = false;
        float mouseTargetX = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            direction = "left";
            moved = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            direction = "right";
            moved = true;
        }
        // Mouse/touchpad movement
        if (Gdx.input.isTouched()) {
            float mouseX = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY())).x;
            mouseTargetX = MathUtils.clamp(mouseX - bucketWidth / 2f, 0, worldWidth - bucketWidth);
            mouseMoved = true;
        }
        long now = System.currentTimeMillis();
        if (mouseMoved) {
            if (now - lastMoveTime > 50) {
                sendMoveTo(mouseTargetX);
                lastMoveTime = now;
            }
            lastDirectionSent = null;
        } else if (moved && direction != null) {
            if (!direction.equals(lastDirectionSent) || now - lastMoveTime > 50) {
                sendMove(direction);
                lastMoveTime = now;
                lastDirectionSent = direction;
            }
        } else {
            lastDirectionSent = null;
        }
    }

    private void sendMoveTo(float x) {
        if (wsClient != null && wsClient.isOpen() && myPlayerId != -1) {
            Map<String, Object> moveTo = new HashMap<>();
            moveTo.put("type", "moveTo");
            moveTo.put("playerId", myPlayerId);
            moveTo.put("x", x);
            wsClient.send(gson.toJson(moveTo));
        }
    }

    private void sendMove(String direction) {
        if (wsClient != null && wsClient.isOpen() && myPlayerId != -1) {
            Map<String, Object> move = new HashMap<>();
            move.put("type", "move");
            move.put("playerId", myPlayerId);
            move.put("direction", direction);
            wsClient.send(gson.toJson(move));
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        // Debug: Print playerNames and bucketPositions
        System.out.println("Drawing with playerNames: " + playerNames);
        System.out.println("bucketPositions keys: " + bucketPositions.keySet());
        System.out.println("playerNames keys: " + playerNames.keySet());
        // TEST: Draw a hardcoded string to verify font rendering
        font.setColor(Color.RED);
        font.draw(spriteBatch, "TEST FONT", 0.5f, 1.0f);
        // Draw all buckets and names/scores
        for (Map.Entry<Integer, Float> entry : bucketPositions.entrySet()) {
            int id = entry.getKey();
            float x = entry.getValue();
            Sprite bucket = new Sprite(bucketTexture);
            bucket.setSize(bucketWidth, bucketHeight);
            bucket.setPosition(x, 0);
            // Use assigned color for each player
            String colorHex = playerColors.getOrDefault(id, "#888888");
            Color color = Color.valueOf(colorHex);
            bucket.setColor(color);
            bucket.draw(spriteBatch);
            bucket.setColor(Color.WHITE); // Reset color for next draw
            // Draw player name and score above bucket, centered
            String name = playerNames.getOrDefault(id, "?");
            int score = scoreboard.getOrDefault(id, 0);
            String label = name + ": " + score;
            GlyphLayout layout = new GlyphLayout(font, label);
            float labelX = Math.max(0, x + bucketWidth / 2f - layout.width / 2f);
            float labelY = bucketHeight + 0.5f; // Lowered for visibility
            System.out.println("Drawing label: '" + label + "' at x=" + labelX + ", y=" + labelY);
            font.setColor(Color.WHITE); // Use white for visibility
            font.draw(spriteBatch, label, labelX, labelY);
        }
        // Draw global scoreboard at top left
        float scoreboardY = worldHeight - 0.2f;
        int scoreboardIndex = 0;
        for (Map.Entry<Integer, String> entry : playerNames.entrySet()) {
            int id = entry.getKey();
            String name = entry.getValue();
            int score = scoreboard.getOrDefault(id, 0);
            font.setColor(id == myPlayerId ? Color.BLUE : Color.GRAY);
            font.draw(spriteBatch, name + ": " + score, 0.2f, scoreboardY - scoreboardIndex * 0.5f);
            scoreboardIndex++;
        }
        // Draw all drops
        for (Map<String, Float> drop : drops) {
            float x = drop.get("x");
            float y = drop.get("y");
            Sprite dropSprite = new Sprite(dropTexture);
            dropSprite.setSize(dropWidth, dropHeight);
            dropSprite.setPosition(x, y);
            dropSprite.draw(spriteBatch);
        }
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

}

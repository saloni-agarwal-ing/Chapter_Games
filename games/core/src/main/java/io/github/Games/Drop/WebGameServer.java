package io.github.Games.Drop;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.InetSocketAddress;
import java.util.*;

public class WebGameServer extends WebSocketServer {
    private Map<Integer, WebSocket> clients = new HashMap<>();
    private Map<Integer, Integer> scoreboard = new HashMap<>();
    private Map<Integer, String> playerNames = new HashMap<>();
    private Map<WebSocket, Integer> connectionToPlayerId = new HashMap<>();
    private int nextPlayerId = 1;
    private Gson gson = new Gson();
    private Map<Integer, Float> bucketPositions = new HashMap<>(); // playerId -> bucket x
    private List<Map<String, Float>> drops = new ArrayList<>(); // each drop: {"x":..., "y":...}
    private Timer timer = new Timer();
    private final float WORLD_WIDTH = 8f;
    private final float WORLD_HEIGHT = 5f;
    private final float BUCKET_WIDTH = 1f;
    private final float BUCKET_HEIGHT = 1f;
    private final float DROP_WIDTH = 0.7f;
    private final float DROP_HEIGHT = 0.7f;
    private final float DROP_SPEED = 2.5f;
    private final float BUCKET_SPEED = 2.5f;
    private final float DROP_SPAWN_INTERVAL = 1.0f; // seconds
    private Random random = new Random();
    private Map<Integer, String> pendingNames = new HashMap<>();
    private Map<Integer, String> playerColors = new HashMap<>(); // playerId -> color hex string

    public WebGameServer(int port) {
        super(new InetSocketAddress(port));
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateGameLogic();
            }
        }, 0, 50); // 20 FPS
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                spawnDrop();
            }
        }, 0, (int)(DROP_SPAWN_INTERVAL * 1000));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Connection opened: " + conn.getRemoteSocketAddress());
        // Wait for join message before assigning playerId
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message from " + conn.getRemoteSocketAddress() + ": " + message);
        Map<String, Object> msg = gson.fromJson(message, new TypeToken<Map<String, Object>>(){}.getType());
        String type = (String) msg.get("type");
        if ("join".equals(type)) {
            if (connectionToPlayerId.containsKey(conn)) {
                System.out.println("Duplicate join message ignored for connection: " + conn.getRemoteSocketAddress());
                return;
            }
            String playerName = (String) msg.get("playerName");
            int playerId = nextPlayerId++;
            clients.put(playerId, conn);
            scoreboard.put(playerId, 0);
            playerNames.put(playerId, playerName);
            connectionToPlayerId.put(conn, playerId);
            bucketPositions.put(playerId, WORLD_WIDTH / 2 - BUCKET_WIDTH / 2);
            // Assign a random color (hex string)
            String color = String.format("#%06X", random.nextInt(0xFFFFFF + 1));
            playerColors.put(playerId, color);
            Map<String, Object> welcome = new HashMap<>();
            welcome.put("type", "welcome");
            welcome.put("playerId", playerId);
            conn.send(gson.toJson(welcome));
            System.out.println("Player joined: id=" + playerId + ", name=" + playerName);
            broadcastGameState();
        } else if ("move".equals(type)) {
            Integer playerId = connectionToPlayerId.get(conn);
            if (playerId == null) return; // Ignore if not mapped
            String direction = (String) msg.get("direction");
            float x = bucketPositions.getOrDefault(playerId, WORLD_WIDTH / 2 - BUCKET_WIDTH / 2);
            float delta = BUCKET_SPEED * 0.05f; // 50ms tick
            if ("left".equals(direction)) x -= delta;
            if ("right".equals(direction)) x += delta;
            x = Math.max(0, Math.min(WORLD_WIDTH - BUCKET_WIDTH, x));
            bucketPositions.put(playerId, x);
        } else if ("moveTo".equals(type)) {
            Integer playerId = connectionToPlayerId.get(conn);
            if (playerId == null) return;
            Double xObj = (Double) msg.get("x");
            if (xObj == null) return;
            float x = xObj.floatValue();
            x = Math.max(0, Math.min(WORLD_WIDTH - BUCKET_WIDTH, x));
            bucketPositions.put(playerId, x);
        }
    }


    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
        Integer playerId = connectionToPlayerId.get(conn);
        if (playerId != null) {
            System.out.println("Player disconnected: id=" + playerId + ", name=" + playerNames.get(playerId));
            clients.remove(playerId);
            scoreboard.remove(playerId);
            playerNames.remove(playerId);
            bucketPositions.remove(playerId);
            playerColors.remove(playerId);
            connectionToPlayerId.remove(conn);
            broadcastGameState();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebGameServer started on port " + getPort());
    }

    private void updateGameLogic() {
        // Move drops
        for (Map<String, Float> drop : drops) {
            drop.put("y", drop.get("y") - DROP_SPEED * 0.05f);
        }
        // Check for collisions and remove drops
        Iterator<Map<String, Float>> it = drops.iterator();
        while (it.hasNext()) {
            Map<String, Float> drop = it.next();
            float dropX = drop.get("x");
            float dropY = drop.get("y");
            boolean caught = false;
            for (Map.Entry<Integer, Float> entry : bucketPositions.entrySet()) {
                int playerId = entry.getKey();
                float bucketX = entry.getValue();
                if (dropY <= 0 + BUCKET_HEIGHT && dropX + DROP_WIDTH > bucketX && dropX < bucketX + BUCKET_WIDTH) {
                    scoreboard.put(playerId, scoreboard.getOrDefault(playerId, 0) + 1);
                    caught = true;
                }
            }
            if (dropY < -DROP_HEIGHT || caught) {
                it.remove();
            }
        }
        broadcastGameState();
    }

    private void spawnDrop() {
        float x = random.nextFloat() * (WORLD_WIDTH - DROP_WIDTH);
        Map<String, Float> drop = new HashMap<>();
        drop.put("x", x);
        drop.put("y", WORLD_HEIGHT);
        drops.add(drop);
    }

    private void broadcastGameState() {
        Map<String, Object> state = new HashMap<>();
        state.put("type", "gameState");
        state.put("scoreboard", scoreboard);
        state.put("playerNames", playerNames);
        state.put("bucketPositions", bucketPositions);
        state.put("playerColors", playerColors);
        state.put("drops", drops);
        state.put("worldWidth", WORLD_WIDTH);
        state.put("worldHeight", WORLD_HEIGHT);
        state.put("bucketWidth", BUCKET_WIDTH);
        state.put("bucketHeight", BUCKET_HEIGHT);
        state.put("dropWidth", DROP_WIDTH);
        state.put("dropHeight", DROP_HEIGHT);
        String json = gson.toJson(state);
        for (WebSocket client : clients.values()) {
            client.send(json);
        }
    }

    public static void main(String[] args) {

        String host = "0.0.0.0";
        int port = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8887"));
        WebGameServer server = new WebGameServer(port);
        server.start();
        System.out.printf("WebGameServer started on %s:%d%n", host, port);

    }
}

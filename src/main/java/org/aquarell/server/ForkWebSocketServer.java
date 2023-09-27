package org.aquarell.server;

import com.google.gson.Gson;
import org.aquarell.forks.Fork;
import org.aquarell.forks.ForksRegistry;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ForkWebSocketServer extends WebSocketServer {
    protected final Logger logger = LoggerFactory.getLogger(ForkWebSocketServer.class);
    private final ForksRegistry forksRegistry;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Set<WebSocket> subscribers = new HashSet<>();
    private final boolean onlyActive;

    public ForkWebSocketServer(ForksRegistry forksRegistry, int port, boolean onlyActive) {
        super(new InetSocketAddress(port));
        forksRegistry.start();
        this.forksRegistry = forksRegistry;
        this.onlyActive = onlyActive;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        webSocket.send("{\"type\":\"ping\", \"message\": \"pong\"}");
        logger.info("New connection established: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        subscribers.remove(webSocket);
        logger.info("Connection closed: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        if (message.equals("subscribe")) {
            subscribers.add(webSocket);
            logger.info("New subscription");
        } else if (message.equals("unsubscribe")) {
            subscribers.remove(webSocket);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.error(e.getMessage());
    }

    @Override
    public void onStart() {
        logger.info("Server successfully started");
    }

    private void sendUpdates() {
        JSONArray forks = new JSONArray();

        forksRegistry.getForks().forEach((key, value) -> {
            if (onlyActive) {
                forks.putAll(value.stream().filter(Fork::isActive).map(Fork::toJson).toList());
                return;
            }
            forks.putAll(value.stream().map(Fork::toJson).toList());
        });

        String message = String.format("{\"type\": \"forks\", \"message\": %s}", forks);

        subscribers.forEach(subscriber -> {
            subscriber.send(message);
        });
    }

    public void startUpdates() {
        scheduler.scheduleAtFixedRate(this::sendUpdates, 0, 200, TimeUnit.MILLISECONDS);
    }
}

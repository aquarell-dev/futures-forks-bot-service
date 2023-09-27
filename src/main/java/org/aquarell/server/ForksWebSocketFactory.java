package org.aquarell.server;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

public class ForksWebSocketFactory extends DefaultWebSocketServerFactory {
    public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d, ClientHandshake h) {
        WebSocketImpl webSocket = super.createWebSocket(a, d);
        webSocket.setAttachment(h.getFieldValue("Origin"));
        return webSocket;
    }
}

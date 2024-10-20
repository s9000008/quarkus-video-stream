package my.project.endpoint;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.PathParam;

@ServerEndpoint("/sync/{roomId}")
@ApplicationScoped
public class SyncEndpoint {
	
	Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") String roomId) {
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("roomId") String roomId) {
        rooms.getOrDefault(roomId, ConcurrentHashMap.newKeySet()).remove(session);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("roomId") String roomId) {
        broadcastToRoom(roomId, message);
    }

    private void broadcastToRoom(String roomId, String message) {
        Set<Session> sessions = rooms.get(roomId);
        if (sessions != null) {
            for (Session session : sessions) {
                session.getAsyncRemote().sendText(message);
            }
        }
    }
}

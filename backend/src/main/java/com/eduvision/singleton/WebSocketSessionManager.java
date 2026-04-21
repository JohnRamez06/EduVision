package com.eduvision.singleton;

import jakarta.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SINGLETON PATTERN
 *
 * Tracks connected WebSocket principals and provides send helpers.
 * Spring creates and injects this bean; @PostConstruct exposes it
 * statically so non-Spring code can call getInstance() if needed.
 *
 * Thread-safe via ConcurrentHashMap — safe under concurrent CONNECT/DISCONNECT events.
 */
@Component
public class WebSocketSessionManager {

    // ── Static singleton reference (set after Spring construction) ─────────
    private static volatile WebSocketSessionManager instance;

    // ── Spring-injected dependency ─────────────────────────────────────────
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * userId (email/subject from JWT) → set of STOMP session IDs.
     * A user can have multiple browser tabs open simultaneously.
     */
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    // ── Constructor (Spring DI) ────────────────────────────────────────────
    public WebSocketSessionManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** Called by Spring after construction — wires up the static reference. */
    @PostConstruct
    private void init() {
        instance = this;
    }

    /** Non-Spring access point (matches existing codebase style). */
    public static WebSocketSessionManager getInstance() {
        return instance;
    }

    // ── Session lifecycle ──────────────────────────────────────────────────

    /**
     * Called on STOMP CONNECT / SessionConnectedEvent.
     * @param userId    email or UUID of the authenticated user
     * @param sessionId STOMP session ID from StompHeaderAccessor
     */
    public void addSession(String userId, String sessionId) {
        userSessions
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
    }

    /**
     * Called on STOMP DISCONNECT / SessionDisconnectEvent.
     */
    public void removeSession(String userId, String sessionId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    /** @return true if the user has at least one live WebSocket session. */
    public boolean isConnected(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /** Snapshot of all connected user IDs. */
    public Set<String> getConnectedUsers() {
        return Collections.unmodifiableSet(userSessions.keySet());
    }

    /** Total number of active STOMP sessions across all users. */
    public int getTotalSessionCount() {
        return userSessions.values().stream().mapToInt(Set::size).sum();
    }

    // ── Messaging helpers ──────────────────────────────────────────────────

    /**
     * Sends a message to a specific user's private queue.
     * Destination becomes: /user/{userId}/queue/{destination}
     *
     * @param userId      recipient's email / JWT subject
     * @param destination e.g. "alerts", "notifications"
     * @param payload     any serialisable DTO
     */
    public void sendToUser(String userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/" + destination, payload);
    }

    /**
     * Broadcasts to a shared topic — all subscribers receive it.
     * Destination becomes: /topic/{destination}
     *
     * @param destination e.g. "session/abc123/mood"
     * @param payload     any serialisable DTO
     */
    public void broadcast(String destination, Object payload) {
        messagingTemplate.convertAndSend("/topic/" + destination, payload);
    }
}
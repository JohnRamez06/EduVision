package com.eduvision.singleton;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class LectureSessionRegistry {

    private static volatile LectureSessionRegistry instance;
    private final ConcurrentMap<String, String> activeSessions = new ConcurrentHashMap<>();

    private LectureSessionRegistry() {
    }

    public static LectureSessionRegistry getInstance() {
        if (instance == null) {
            synchronized (LectureSessionRegistry.class) {
                if (instance == null) {
                    instance = new LectureSessionRegistry();
                }
            }
        }
        return instance;
    }

    public void register(String courseId, String sessionId) {
        activeSessions.put(courseId, sessionId);
    }

    public String get(String courseId) {
        return activeSessions.get(courseId);
    }

    public void remove(String courseId) {
        activeSessions.remove(courseId);
    }

    public boolean hasActive(String courseId) {
        return activeSessions.containsKey(courseId);
    }
}

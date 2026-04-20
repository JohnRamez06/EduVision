package com.eduvision.singleton;
import org.springframework.stereotype.Component;
@Component
public class LectureSessionRegistry {
    private static volatile LectureSessionRegistry instance;
    private LectureSessionRegistry() {}
    public static LectureSessionRegistry getInstance() {
        if (instance == null) { synchronized (LectureSessionRegistry.class) { if (instance == null) instance = new LectureSessionRegistry(); } }
        return instance;
    }
    // TODO: track active sessions mapped to course IDs
}

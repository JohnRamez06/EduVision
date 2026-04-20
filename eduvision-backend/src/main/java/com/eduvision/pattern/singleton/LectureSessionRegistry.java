package com.eduvision.pattern.singleton;
public class LectureSessionRegistry {
    private static LectureSessionRegistry instance;
    private LectureSessionRegistry() {}
    public static synchronized LectureSessionRegistry getInstance() {
        if (instance == null) instance = new LectureSessionRegistry();
        return instance;
    }
}

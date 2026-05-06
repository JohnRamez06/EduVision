package com.eduvision.factory;

public interface VideoStreamReader {
    boolean connect();
    byte[] readFrame();
    void disconnect();
    String getStreamUrl();
}
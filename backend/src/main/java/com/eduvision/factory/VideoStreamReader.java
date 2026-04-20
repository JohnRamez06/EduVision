package com.eduvision.factory;
public interface VideoStreamReader {
    void connect();
    byte[] readFrame();
    void disconnect();
}

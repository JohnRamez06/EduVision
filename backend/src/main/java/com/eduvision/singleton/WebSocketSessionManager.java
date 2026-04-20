package com.eduvision.singleton;
import org.springframework.stereotype.Component;
@Component
public class WebSocketSessionManager {
    private static volatile WebSocketSessionManager instance;
    private WebSocketSessionManager() {}
    public static WebSocketSessionManager getInstance() { if(instance==null){synchronized(WebSocketSessionManager.class){if(instance==null)instance=new WebSocketSessionManager();}} return instance; }
}

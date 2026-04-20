package com.eduvision.singleton;
import org.springframework.stereotype.Component;
@Component
public class PermissionCache {
    private static volatile PermissionCache instance;
    private PermissionCache() {}
    public static PermissionCache getInstance() { if(instance==null){synchronized(PermissionCache.class){if(instance==null)instance=new PermissionCache();}} return instance; }
}

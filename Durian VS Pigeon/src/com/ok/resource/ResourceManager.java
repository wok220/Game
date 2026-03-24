package com.ok.resource;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private static ResourceManager instance;
    private Map<String, Object> resources;
    
    private ResourceManager() {
        resources = new HashMap<>();
    }
    
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    public void loadResource(String key, Object resource) {
        resources.put(key, resource);
    }
    
    public Object getResource(String key) {
        return resources.get(key);
    }
    
    public void unloadResource(String key) {
        resources.remove(key);
    }
    
    public void clearAll() {
        resources.clear();
    }
}
package io.swagger.integration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SwaggerContextLocator {

    private static SwaggerContextLocator instance;

    private ConcurrentMap<String, SwaggerContext> map = new ConcurrentHashMap<String, SwaggerContext>();

    private SwaggerContextLocator() {
    }

    public static SwaggerContextLocator getInstance() {
        if (instance == null) {
            instance = new SwaggerContextLocator();
        }
        return instance;
    }

    public SwaggerContext getSwaggerContext(String id) {
        return map.get(id);
    }

    public void putSwaggerContext(String id, SwaggerContext swaggerContext) {
        map.put(id, swaggerContext);
    }
}

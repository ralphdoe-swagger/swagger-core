package io.swagger.integration;

import io.swagger.models.Swagger;

import java.util.Map;

public interface SwaggerContext {

    static final String SWAGGER_CONTEXT_ID_KEY = "swagger.context.id";
    static final String SWAGGER_CONTEXT_ID_PREFIX = SWAGGER_CONTEXT_ID_KEY + ".";
    static final String SWAGGER_CONTEXT_ID_DEFAULT = SWAGGER_CONTEXT_ID_PREFIX + "default";

    Map<String, SwaggerProcessor> getSwaggerProcessors();

    String getId();

    SwaggerContext init();

    Swagger read();

    SwaggerConfiguration getSwaggerConfiguration();

    String getConfigLocation();
}

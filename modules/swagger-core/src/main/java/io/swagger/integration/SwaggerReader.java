package io.swagger.integration;

import io.swagger.models.Swagger;

import java.util.Map;
import java.util.Set;

public interface SwaggerReader {

    Swagger read(Set<Class<?>> classes, Map<String, Object> resources);
}

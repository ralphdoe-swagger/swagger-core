package io.swagger.integration;

import java.util.Map;
import java.util.Set;

public interface SwaggerScanner {

    Set<Class<?>> classes();

    Map<String, Object> resources();
}

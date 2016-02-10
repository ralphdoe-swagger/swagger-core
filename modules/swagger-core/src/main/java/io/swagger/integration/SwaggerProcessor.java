package io.swagger.integration;

import io.swagger.models.Swagger;

public interface SwaggerProcessor {

    String getBasePath();

    SwaggerProcessor init();

    Swagger read();

    void setSwaggerScanner(SwaggerScanner swaggerScanner);

    void setSwaggerReader(SwaggerReader swaggerReader);

    SwaggerConfiguration getSwaggerConfiguration();

}

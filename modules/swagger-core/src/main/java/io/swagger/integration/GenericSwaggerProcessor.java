package io.swagger.integration;

import io.swagger.models.Swagger;

import java.util.Map;
import java.util.Set;

public class GenericSwaggerProcessor implements SwaggerProcessor {

    private SwaggerReader swaggerReader;
    private SwaggerScanner swaggerScanner;

    public SwaggerReader getSwaggerReader() {
        return swaggerReader;
    }

    public void setSwaggerReader(SwaggerReader swaggerReader) {
        this.swaggerReader = swaggerReader;
    }

    public SwaggerScanner getSwaggerScanner() {
        return swaggerScanner;
    }

    public void setSwaggerScanner(SwaggerScanner swaggerScanner) {
        this.swaggerScanner = swaggerScanner;
    }

    @Override
    public SwaggerConfiguration getSwaggerConfiguration() {
        return swaggerConfiguration;
    }

    public void setSwaggerConfiguration(SwaggerConfiguration swaggerConfiguration) {
        this.swaggerConfiguration = swaggerConfiguration;
    }

    private SwaggerConfiguration swaggerConfiguration;

    @Override
    public String getBasePath() {
        if (swaggerConfiguration !=  null) {
            return swaggerConfiguration.getBasePath();
        }
        return "/";
    }

    public final GenericSwaggerProcessor withSwaggerReader(SwaggerReader swaggerReader) {
        this.swaggerReader = swaggerReader;
        return this;
    }

    public final GenericSwaggerProcessor withSwaggerScanner(SwaggerScanner swaggerScanner) {
        this.swaggerScanner = swaggerScanner;
        return this;
    }

    public final GenericSwaggerProcessor withSwaggerConfiguration(SwaggerConfiguration swaggerConfiguration) {
        this.swaggerConfiguration = swaggerConfiguration;
        return this;
    }

    public final GenericSwaggerProcessor withSwaggerConfigurationFromMap(Map<String, String> properties) {
        this.swaggerConfiguration = new SwaggerConfiguration().withProperties(properties);
        return this;
    }
    @Override
    public SwaggerProcessor init() {

        if (swaggerConfiguration == null) {
            swaggerConfiguration = new SwaggerConfiguration();
        }
        if (swaggerScanner == null) {
            swaggerScanner = new GenericSwaggerScanner(swaggerConfiguration);
        }
        if (swaggerReader == null) {
            // TODO use a real generic swagger reader only reading swagger annotations..
            swaggerReader = new SwaggerReader() {
                @Override
                public Swagger read(Set<Class<?>> classes, Map<String, Object> resources) {
                    swaggerConfiguration.getBasePath();
                    Swagger swagger = swaggerConfiguration.toSwagger(null);
                    return swagger;

                }
            };
        }
        return this;
    }

    @Override
    public Swagger read() {
        return getSwaggerReader().read(getSwaggerScanner().classes(), getSwaggerScanner().resources());
    }

}

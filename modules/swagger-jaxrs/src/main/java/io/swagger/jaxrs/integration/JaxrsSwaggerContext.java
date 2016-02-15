package io.swagger.jaxrs.integration;

import io.swagger.integration.GenericSwaggerContext;
import io.swagger.integration.SwaggerConfiguration;
import io.swagger.integration.SwaggerContext;
import io.swagger.integration.SwaggerReader;
import io.swagger.integration.SwaggerScanner;
import io.swagger.jaxrs.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;

public class JaxrsSwaggerContext<T extends JaxrsSwaggerContext> extends GenericSwaggerContext<JaxrsSwaggerContext> implements SwaggerContext {
    Logger LOGGER = LoggerFactory.getLogger(JaxrsSwaggerContext.class);

    private Application app;

    public T withApp(Application app) {
        this.app = app;
        return (T)this;
    }


    @Override
    protected SwaggerReader buildReader(String path, SwaggerConfiguration swaggerConfiguration) throws Exception {
        LOGGER.trace("buildReader");
        return new Reader(swaggerConfiguration.getSwagger());
    }

    @Override
    protected SwaggerScanner buildScanner(String path, SwaggerConfiguration swaggerConfiguration) throws Exception {
        LOGGER.trace("buildscanner");
        return new AnnotationJaxrsScanner().withSwaggerConfiguration(swaggerConfiguration);
    }

}

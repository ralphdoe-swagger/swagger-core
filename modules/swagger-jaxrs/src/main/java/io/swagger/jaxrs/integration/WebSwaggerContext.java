package io.swagger.jaxrs.integration;

import io.swagger.integration.SwaggerContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public interface WebSwaggerContext extends SwaggerContext{

    ServletContext getServletContext();
    ServletConfig getServletConfig();


}

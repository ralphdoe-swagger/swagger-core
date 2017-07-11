package io.swagger.jaxrs.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class XmlWebSwaggerContext<T extends XmlWebSwaggerContext<T>> extends JaxrsSwaggerContext<T> implements WebSwaggerContext{

    private ServletContext servletContext;
    private ServletConfig servletConfig;

    public static final String SWAGGER_CONFIGURATION_RESOURCEPACKAGE_KEY = "swagger.configuration.resourcePackage";
    public static final String SWAGGER_CONFIGURATION_BASEPATH_KEY = "swagger.configuration.basePath";
    public static final String SWAGGER_CONFIGURATION_LOCATION_KEY = "swagger.configuration.location";
    public static final String JERSEY1_PACKAGE_KEY = "com.sun.jersey.config.property.packages";
    public static final String JERSEY2_PACKAGE_KEY = "jersey.config.server.provider.packages";

    Logger LOGGER = LoggerFactory.getLogger(XmlWebSwaggerContext.class);

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public T withServletConfig(ServletConfig servletConfig) {

        if (servletConfig == null) return (T)this;
        this.servletConfig = servletConfig;
        this.servletContext = servletConfig.getServletContext();
        withId(SWAGGER_CONTEXT_ID_PREFIX + "servlet." + servletConfig.getServletName());
        String location = getInitParam (servletConfig, SWAGGER_CONFIGURATION_LOCATION_KEY);
        if (location != null) {
            withConfigLocation(location);
        }
        resolveResourcePackage(servletConfig);
        String basePath = getInitParam (servletConfig, SWAGGER_CONFIGURATION_BASEPATH_KEY);
        if (basePath != null) {
            withBasePath(basePath);
        }
        return (T)this;
    }

    private void resolveResourcePackage (ServletConfig servletConfig) {
        String resourcePackage = getInitParam (servletConfig, SWAGGER_CONFIGURATION_RESOURCEPACKAGE_KEY);
        if (resourcePackage == null) {
            // jersey 1
            resourcePackage = getInitParam (servletConfig, JERSEY1_PACKAGE_KEY);
            resourcePackage = resourcePackage.replace(';', ',');
        }
        if (resourcePackage == null) {
            // jersey 2
            resourcePackage = getInitParam (servletConfig, JERSEY2_PACKAGE_KEY);
        }
        if (resourcePackage != null) {
            withResourcePackage(resourcePackage);
        }
    }

    private String getInitParam(ServletConfig sc, String paramKey) {
        return servletConfig.getInitParameter(paramKey) == null?
                servletContext.getInitParameter(paramKey) :
                servletConfig.getInitParameter(paramKey);
    }


    // TODO DRAFT and move logic to generic for what possible (classpath etc, known locations)
/*
    protected SwaggerConfiguration loadConfiguration() {

        // also sysprops

        // check if configurationFilePath param
        // check if configurationFileFormat param

        // if no configurationFilePath, load config from file in standard location:
        //  /WEB-INF/swagger/swaggerconfig.properties
        //  /WEB-INF/swagger/swaggerconfig.json
        //  /WEB-INF/swagger/swaggerconfig.yaml
        //  /WEB-INF/swagger/swaggerconfig...

        // then try to find in classpath..

        // otherwise when we have configFilePath, load from there

        return SwaggerConfiguration.fromUri(location, "props");

    }
*/
}

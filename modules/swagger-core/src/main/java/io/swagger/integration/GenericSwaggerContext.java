package io.swagger.integration;

import io.swagger.models.Swagger;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GenericSwaggerContext<T extends GenericSwaggerContext> implements SwaggerContext {

    private SwaggerConfiguration swaggerConfiguration;

    private String resourcePackage;

    private String basePath = "/";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }


    public final T withBasePath(String basePath) {
        this.basePath = basePath;
        return (T) this;
    }


    public T withSwaggerConfiguration(SwaggerConfiguration swaggerConfiguration) {
        this.swaggerConfiguration = swaggerConfiguration;
        return (T) this;
    }

    public void setSwaggerConfiguration(SwaggerConfiguration swaggerConfiguration) {
        this.swaggerConfiguration = swaggerConfiguration;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    private String configLocation;

    public final T withConfigLocation(String configLocation) {
        this.configLocation = configLocation;
        return (T) this;
    }

    public void setSwaggerProcessors(Map<String, SwaggerProcessor> swaggerProcessors) {
        this.swaggerProcessors = swaggerProcessors;
    }

    public void setId(String id) {
        this.id = id;
    }

    private Map<String, SwaggerProcessor> swaggerProcessors = new HashMap<String, SwaggerProcessor>();
    protected String id = SWAGGER_CONTEXT_ID_DEFAULT;

    protected void register() {

        SwaggerContextLocator.getInstance().putSwaggerContext(id, this);

    }

    @Override
    public String getId() {
        return this.id;
    }

    public final T withId(String id) {
        this.id = id;
        return (T) this;
    }

    @Override
    public Map<String, SwaggerProcessor> getSwaggerProcessors() {
        return swaggerProcessors;
    }

    public GenericSwaggerContext addSwaggerProcessor(SwaggerProcessor swaggerProcessor) {
        if (StringUtils.isEmpty(swaggerProcessor.getBasePath())) {
            swaggerProcessor.getSwaggerConfiguration().getSwagger().basePath(basePath);
        }
        swaggerProcessors.put(swaggerProcessor.getBasePath(), swaggerProcessor);
        return this;
    }

    protected SwaggerProcessor buildProcessor(String path, final SwaggerConfiguration swaggerConfiguration) throws Exception {
        SwaggerProcessor processor;
        if (StringUtils.isNotBlank(swaggerConfiguration.getProcessorClass())) {
            Class cls = getClass().getClassLoader().loadClass(swaggerConfiguration.getProcessorClass());
            processor = (SwaggerProcessor) cls.newInstance();
        } else {
            processor = new GenericSwaggerProcessor().withSwaggerConfiguration(swaggerConfiguration);
        }
        processor.setSwaggerScanner(buildScanner(path, swaggerConfiguration));
        processor.setSwaggerReader(buildReader(path, swaggerConfiguration));
        return processor;
    }

    protected SwaggerReader buildReader(String path, final SwaggerConfiguration swaggerConfiguration) throws Exception {
        SwaggerReader reader;
        if (StringUtils.isNotBlank(swaggerConfiguration.getReaderClass())) {
            Class cls = getClass().getClassLoader().loadClass(swaggerConfiguration.getReaderClass());
            // TODO instantiate with configuration
            reader = (SwaggerReader) cls.newInstance();
        } else {
            reader = new SwaggerReader() {
                @Override
                public Swagger read(Set<Class<?>> classes, Map<String, Object> resources) {
                    Swagger swagger = swaggerConfiguration.getSwagger();
                    return swagger;

                }
            };
        }
        return reader;
    }

    protected SwaggerScanner buildScanner(String path, final SwaggerConfiguration swaggerConfiguration) throws Exception {
        SwaggerScanner scanner;
        if (StringUtils.isNotBlank(swaggerConfiguration.getScannerClass())) {
            Class cls = getClass().getClassLoader().loadClass(swaggerConfiguration.getScannerClass());
            // TODO instantiate with configuration
            scanner = (SwaggerScanner) cls.newInstance();
        } else {
            scanner = new GenericSwaggerScanner(swaggerConfiguration);
        }
        return scanner;
    }

    // TODO implement in subclass, also handle classpath
    protected URL buildConfigLocationURL(String configLocation) {
        // TODO
        configLocation = "file://" + configLocation;
        try {
            return new URL(configLocation);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public SwaggerContext init() {
        if (StringUtils.isNotEmpty(configLocation)) {
            // TODO handle urls and stuff, also use loadConfiguration protected now in WebXmlContext..
            Map<String, SwaggerConfiguration> configurations = SwaggerConfiguration.fromUrl(buildConfigLocationURL(configLocation), null, basePath);
            for (String path : configurations.keySet()) {
                try {
                    swaggerProcessors.put(path, buildProcessor(path, configurations.get(path)));
                } catch (Exception e) {
                    // TODO
                    e.printStackTrace();
                }

            }
        }

        // TODO here try with openApiController? and replace SwaggerConfiguration.fromUrl

        if (swaggerProcessors.isEmpty()) {
            try {
                if (swaggerConfiguration == null) {
                    swaggerConfiguration = new SwaggerConfiguration().withResourcePackage(resourcePackage);
                    swaggerConfiguration.getSwagger().basePath(basePath);
                }
                swaggerProcessors.put(basePath, buildProcessor(basePath, swaggerConfiguration));
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
        }
        for (SwaggerProcessor p : swaggerProcessors.values()) {
            p.init();
        }
        register();
        return this;
    }

    @Override
    public Swagger read() {
        if (swaggerProcessors.isEmpty()) {
            return null;
        }
        return swaggerProcessors.get(basePath).read();
    }

    @Override
    public SwaggerConfiguration getSwaggerConfiguration() {
        if (swaggerConfiguration != null) {
            return swaggerConfiguration;
        }
        if (!swaggerProcessors.isEmpty()) {
            if (swaggerProcessors.get(basePath) != null) {
                return swaggerProcessors.get(basePath).getSwaggerConfiguration();
            }
        }
        return null;
    }

    public String getResourcePackage() {
        return resourcePackage;
    }

    public T withResourcePackage(String resourcePackage) {
        this.resourcePackage = resourcePackage;
        return (T) this;
    }
}

package io.swagger.integration;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenericSwaggerScanner implements SwaggerScanner {

    SwaggerConfiguration swaggerConfiguration;

    public GenericSwaggerScanner(SwaggerConfiguration swaggerConfiguration) {
        this.swaggerConfiguration = swaggerConfiguration;
    }

    @Override
    public Set<Class<?>> classes() {
        ConfigurationBuilder config = new ConfigurationBuilder();
        Set<String> acceptablePackages = new HashSet<String>();

        boolean allowAllPackages = false;

        if (swaggerConfiguration.getResourcePackage() != null && !"".equals(swaggerConfiguration.getResourcePackage())) {
            String[] parts = swaggerConfiguration.getResourcePackage().split(",");
            for (String pkg : parts) {
                if (!"".equals(pkg)) {
                    acceptablePackages.add(pkg);
                    config.addUrls(ClasspathHelper.forPackage(pkg));
                }
            }
        } else {
            allowAllPackages = true;
        }

        config.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());

        final Reflections reflections = new Reflections(config);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Api.class);
        classes.addAll(reflections.getTypesAnnotatedWith(SwaggerDefinition.class));

        Set<Class<?>> output = new HashSet<Class<?>>();
        for (Class<?> cls : classes) {
            if (allowAllPackages) {
                output.add(cls);
            } else {
                for (String pkg : acceptablePackages) {
                    if (cls.getPackage().getName().startsWith(pkg)) {
                        output.add(cls);
                    }
                }
            }
        }
        return output;
    }

    @Override
    public Map<String, Object> resources() {
        return new HashMap<String, Object>();
    }
}

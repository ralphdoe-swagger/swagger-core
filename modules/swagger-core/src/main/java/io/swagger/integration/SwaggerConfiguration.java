package io.swagger.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.util.Json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SwaggerConfiguration {

    Map<String, String> rawProperties = new ConcurrentHashMap<String, String>();
    private boolean basePathAsKey;

    private String resourcePackage;
    private String[] schemes;
    private String title;
    private String version;
    private String description;
    private String termsOfServiceUrl;
    private String contact;
    private String license;
    private String licenseUrl;
    private String filterClass;
    private Info info;
    private String host;
    private String basePath = "/";

    private String readerClass;
    private String scannerClass;
    private String processorClass;
    private boolean pathAsProcessorKey;

    public static Map<String, SwaggerConfiguration> fromUrl(URL location, String format) {

        // get file as string (for the moment, TODO use commons config)
        // load from classpath etc, look from file..
        try {
            Map<String, SwaggerConfiguration> configurationMap = new HashMap<String, SwaggerConfiguration>();

            String configAsString = readUrl(location);

            List<SwaggerConfiguration> configurations = Json.mapper().readValue(configAsString, new TypeReference<List<SwaggerConfiguration>>() {
            });
            for (SwaggerConfiguration config : configurations) {
                configurationMap.put(config.basePath, config);
            }
            return configurationMap;

        } catch (Exception e) {
            // TODO
            e.printStackTrace();
            throw new RuntimeException("exception reading config", e);
        }

    }

    private static Properties loadProperties(URI uri) throws IOException {
        FileReader reader = new FileReader(new File(uri));
        Properties props = new Properties();
        props.load(reader);
        reader.close();
        return props;
    }

    private static String readUrl(URL url) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine).append("\n");
        }
        in.close();
        return sb.toString();
    }

    public boolean isPathAsProcessorKey() {
        return pathAsProcessorKey;
    }

    public void setPathAsProcessorKey(boolean pathAsProcessorKey) {
        this.pathAsProcessorKey = pathAsProcessorKey;
    }

    public SwaggerConfiguration withtPathAsProcessorKey(boolean pathAsProcessorKey) {
        this.pathAsProcessorKey = pathAsProcessorKey;
        return this;
    }

    public String getReaderClass() {
        return readerClass;
    }

    public void setReaderClass(String readerClass) {
        this.readerClass = readerClass;
    }

    public String getScannerClass() {
        return scannerClass;
    }

    public void setScannerClass(String scannerClass) {
        this.scannerClass = scannerClass;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(String processorClass) {
        this.processorClass = processorClass;
    }

    public Map<String, String> getRawProperties() {
        return rawProperties;
    }

    public void setRawProperties(Map<String, String> rawProperties) {
        this.rawProperties = rawProperties;
    }

    public SwaggerConfiguration withScannerClass(String scannerClass) {
        this.scannerClass = scannerClass;
        return this;
    }

    public SwaggerConfiguration withReaderClass(String readerClass) {
        this.readerClass = readerClass;
        return this;
    }

    public SwaggerConfiguration withProcessorClass(String processorClass) {
        this.processorClass = processorClass;
        return this;
    }

    public SwaggerConfiguration withProperties(Map<String, String> properties) {
        if (properties != null) {
            rawProperties.putAll(properties);
            loadRawProperties();
        }
        return this;
    }

    public Swagger toSwagger(Swagger swagger) {
        // TODO
        if (swagger == null) {
            swagger = new Swagger();
        }
        swagger.setBasePath(basePath);
        //swagger.setInfo(...);
        return swagger;
    }

    // TODO ENUM ETC
    private void loadRawProperties() {
        for (String key : rawProperties.keySet()) {
            switch (key.charAt(0)) {
/*                case (RESOURCE_PACKAGE_KEY):
                    resourcePackage = rawProperties.get(key);
                    break;*/
                default:
                    resourcePackage = rawProperties.get(key);
            }
        }

    }

    public boolean isBasePathAsKey() {
        return basePathAsKey;
    }

    public void setBasePathAsKey(boolean basePathAsKey) {
        this.basePathAsKey = basePathAsKey;
    }

    public SwaggerConfiguration withBasePathAsKey(boolean basePathAsKey) {
        this.basePathAsKey = basePathAsKey;
        return this;
    }

    public String getResourcePackage() {
        return resourcePackage;
    }

    public void setResourcePackage(String resourcePackage) {
        this.resourcePackage = resourcePackage;
    }

    public SwaggerConfiguration withResourcePackage(String resourcePackage) {
        this.resourcePackage = resourcePackage;
        return this;
    }

    public String[] getSchemes() {
        return schemes;
    }

    public void setSchemes(String[] schemes) {
        this.schemes = schemes;
    }

    public SwaggerConfiguration withSchemes(String[] schemes) {
        this.schemes = schemes;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SwaggerConfiguration withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SwaggerConfiguration withVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SwaggerConfiguration withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public SwaggerConfiguration withTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
        return this;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public SwaggerConfiguration withContact(String contact) {
        this.contact = contact;
        return this;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public SwaggerConfiguration withLicense(String license) {
        this.license = license;
        return this;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public SwaggerConfiguration withLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
        return this;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public SwaggerConfiguration withFilterClass(String filterClass) {
        this.filterClass = filterClass;
        return this;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public SwaggerConfiguration withInfo(Info info) {
        this.info = info;
        return this;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public SwaggerConfiguration withHost(String host) {
        this.host = host;
        return this;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public SwaggerConfiguration withBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public SwaggerConfiguration withJavaProperties(Properties properties) {
        if (properties != null) {
            Map<String, String> map = (Map) properties;
            return withProperties(map);
        }
        return this;
    }

    public enum CONFIG_FILE_FORMAT {PROPERTIES, INI, JSON, YAML, XML}
}

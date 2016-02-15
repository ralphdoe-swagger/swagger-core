package io.swagger.jaxrs.listing;

import io.swagger.annotations.ApiOperation;
import io.swagger.config.FilterFactory;
import io.swagger.config.Scanner;
import io.swagger.config.SwaggerConfig;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.integration.SwaggerContext;
import io.swagger.integration.SwaggerContextLocator;
import io.swagger.integration.SwaggerProcessor;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.integration.XmlWebSwaggerContext;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.config.ReaderConfigUtils;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Path("/")
public class ApiListingResource {
    private static volatile boolean initialized = false;

    private static volatile ConcurrentMap<String, Boolean> initializedScanner = new ConcurrentHashMap<String, Boolean>();
    private static volatile ConcurrentMap<String, Boolean> initializedConfig = new ConcurrentHashMap<String, Boolean>();

    Logger LOGGER = LoggerFactory.getLogger(ApiListingResource.class);

    @Context
    ServletContext context;

    private String configLocation;
    
    public String getConfigLocation() {
        return configLocation;
    }
    public void setConfigLocation (String configLocation) {
        this.configLocation = configLocation;
    }

    public ApiListingResource configLocation(String configLocation) {
        setConfigLocation(configLocation);
        return this;
    }

    private String basePath;

    public String getBasePath() {
        return basePath;
    }
    public void setBasePath (String basePath) {
        this.basePath = basePath;
    }

    public ApiListingResource basePath(String basePath) {
        setBasePath(basePath);
        return this;
    }

    private String resourcePackage;

    public String getResourcePackage() {
        return resourcePackage;
    }
    public void setResourcePackage (String resourcePackage) {
        this.resourcePackage = resourcePackage;
    }

    public ApiListingResource resourcePackage(String resourcePackage) {
        setResourcePackage(resourcePackage);
        return this;
    }
    
    protected synchronized Swagger scan(Application app, ServletConfig sc) {
        Swagger swagger = null;
        SwaggerContextService ctxService = new SwaggerContextService().withServletConfig(sc);
        Scanner scanner = ctxService.getScanner();
        if (scanner != null) {
            SwaggerSerializers.setPrettyPrint(scanner.getPrettyPrint());
            swagger = new SwaggerContextService().withServletConfig(sc).getSwagger();
            Set<Class<?>> classes;
            if (scanner instanceof JaxrsScanner) {
                JaxrsScanner jaxrsScanner = (JaxrsScanner) scanner;
                classes = jaxrsScanner.classesFromContext(app, sc);
            } else {
                classes = scanner.classes();
            }
            if (classes != null) {
                Reader reader = new Reader(swagger, ReaderConfigUtils.getReaderConfig(context));
                swagger = reader.read(classes);
                if (scanner instanceof SwaggerConfig) {
                    swagger = ((SwaggerConfig) scanner).configure(swagger);
                } else {
                    SwaggerConfig swaggerConfig = ctxService.getConfig();
                    if (swaggerConfig != null) {
                        LOGGER.debug("configuring swagger with " + swaggerConfig);
                        swaggerConfig.configure(swagger);
                    } else {
                        LOGGER.debug("no configurator");
                    }
                }
                new SwaggerContextService().withServletConfig(sc).updateSwagger(swagger);
            }
        }
        if (SwaggerContextService.isScannerIdInitParamDefined(sc)) {
            initializedScanner.put(sc.getServletName() + "_" + SwaggerContextService.getScannerIdFromInitParam(sc), true);
        } else if (SwaggerContextService.isConfigIdInitParamDefined(sc)) {
            initializedConfig.put(sc.getServletName() + "_" + SwaggerContextService.getConfigIdFromInitParam(sc), true);
        } else {
            initialized = true;
        }

        return swagger;
    }

    private Swagger process(
            Application app,
            ServletConfig sc,
            HttpHeaders headers,
            UriInfo uriInfo) {
        Swagger swagger = new SwaggerContextService().withServletConfig(sc).getSwagger();
        synchronized (ApiListingResource.class) {
            if (SwaggerContextService.isScannerIdInitParamDefined(sc)) {
                if (!initializedScanner.containsKey(sc.getServletName() + "_" + SwaggerContextService.getScannerIdFromInitParam(sc))) {
                    swagger = scan(app, sc);
                }
            } else {
                if (SwaggerContextService.isConfigIdInitParamDefined(sc)) {
                    if (!initializedConfig.containsKey(sc.getServletName() + "_" + SwaggerContextService.getConfigIdFromInitParam(sc))) {
                        swagger = scan(app, sc);
                    }
                } else {
                    if (!initialized) {
                        swagger = scan(app, sc);
                    }
                }
            }
        }
        if (swagger != null) {
            SwaggerSpecFilter filterImpl = FilterFactory.getFilter();
            if (filterImpl != null) {
                SpecFilter f = new SpecFilter();
                swagger = f.filter(swagger, filterImpl, getQueryParams(uriInfo.getQueryParameters()), getCookies(headers),
                        getHeaders(headers));
            }
        }
        return swagger;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    @ApiOperation(value = "The swagger definition in either JSON or YAML", hidden = true)
    @Path("/swagger.{type:json|yaml}")
    public Response getListing(
            @Context Application app,
            @Context ServletConfig sc,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            @PathParam("type") String type) {
        if (StringUtils.isNotBlank(type) && type.trim().equalsIgnoreCase("yaml")) {
            return getListingYaml(app, sc, headers, uriInfo);
        } else {
            return getListingJson2(app, sc, headers, uriInfo);
        }
    }


    private SwaggerContext getOrBuildContext(String ctxId, Application app, ServletConfig sc) {
        SwaggerContext ctx = SwaggerContextLocator.getInstance().getSwaggerContext(ctxId);
        String rootId = SwaggerContext.SWAGGER_CONTEXT_ID_DEFAULT;
        if (ctx == null) {
            // get root
            ctx = SwaggerContextLocator.getInstance().getSwaggerContext(rootId);
            if (ctx == null) {
                ctx = new XmlWebSwaggerContext()
                        .withServletConfig(sc)
                        .withApp(app)
                        .withId(ctxId);
                if (ctx.getConfigLocation() == null && configLocation != null) {
                    ((XmlWebSwaggerContext)ctx).withConfigLocation(configLocation);
                }
                if (basePath != null) {
                    ((XmlWebSwaggerContext)ctx).withBasePath(basePath);
                }
                if (resourcePackage != null) {
                    ((XmlWebSwaggerContext)ctx).withResourcePackage(resourcePackage);
                }
                ctx.init();
            } else {
                SwaggerContextLocator.getInstance().putSwaggerContext(ctxId, ctx);
            }
        }
        return ctx;
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/api-docs/swaggers")
    @ApiOperation(value = "The swagger definition in JSON", hidden = true)
    public Response getListingJsonBase(
            @Context Application app,
            @Context ServletConfig sc) {
        return getListingJson(app, sc, "/");

    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/api-docs/{basePath}/swaggers")
    @ApiOperation(value = "The swagger definition in JSON", hidden = true)
    public Response getListingJson(
            @Context Application app,
            @Context ServletConfig sc,
            @PathParam("basePath") String basePath) {
        // get context for this servlet e.g. for part of path:
        String ctxId;
        final String rootCtxId = SwaggerContext.SWAGGER_CONTEXT_ID_DEFAULT;
        SwaggerContext ctx;
        if (sc != null) {
            ctxId =SwaggerContext.SWAGGER_CONTEXT_ID_PREFIX + "servlet." + sc.getServletName();
            ctx = getOrBuildContext(ctxId, app, sc);
        } else {
            ctx = getOrBuildContext(rootCtxId, app, sc);
        }
        String processorKey = "/";
        if (!basePath.startsWith("/")) basePath = "/" + basePath;
        processorKey = basePath;
        SwaggerProcessor p = ctx.getSwaggerProcessors().get(processorKey);
        if (p == null) {
            // default to root processor
            // TODO DO WE WANT THIS OR WE THROW ERROR
            //p = ctx.getSwaggerProcessors().get("/");
        }
        Swagger swagger = p.read();
        if (swagger != null) {
            return Response.ok().entity(swagger).build();
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/swagger")
    @ApiOperation(value = "The swagger definition in JSON", hidden = true)
    public Response getListingJson2(
            @Context Application app,
            @Context ServletConfig sc,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {
        Swagger swagger = process(app, sc, headers, uriInfo);

        if (swagger != null) {
            return Response.ok().entity(swagger).build();
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Produces("application/yaml")
    @Path("/swagger")
    @ApiOperation(value = "The swagger definition in YAML", hidden = true)
    public Response getListingYaml(
            @Context Application app,
            @Context ServletConfig sc,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {
        Swagger swagger = process(app, sc, headers, uriInfo);
        try {
            if (swagger != null) {
                String yaml = Yaml.mapper().writeValueAsString(swagger);
                StringBuilder b = new StringBuilder();
                String[] parts = yaml.split("\n");
                for (String part : parts) {
                    b.append(part);
                    b.append("\n");
                }
                return Response.ok().entity(b.toString()).type("application/yaml").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(404).build();
    }

    protected Map<String, List<String>> getQueryParams(MultivaluedMap<String, String> params) {
        Map<String, List<String>> output = new HashMap<String, List<String>>();
        if (params != null) {
            for (String key : params.keySet()) {
                List<String> values = params.get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    protected Map<String, String> getCookies(HttpHeaders headers) {
        Map<String, String> output = new HashMap<String, String>();
        if (headers != null) {
            for (String key : headers.getCookies().keySet()) {
                Cookie cookie = headers.getCookies().get(key);
                output.put(key, cookie.getValue());
            }
        }
        return output;
    }

    protected Map<String, List<String>> getHeaders(HttpHeaders headers) {
        Map<String, List<String>> output = new HashMap<String, List<String>>();
        if (headers != null) {
            for (String key : headers.getRequestHeaders().keySet()) {
                List<String> values = headers.getRequestHeaders().get(key);
                output.put(key, values);
            }
        }
        return output;
    }
}

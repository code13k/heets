package org.code13k.heets.service.set;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.code13k.heets.business.ClusteredCache;
import org.code13k.heets.config.AppConfig;
import org.code13k.heets.lib.Util;
import org.code13k.heets.model.CacheData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class SetHttpServer extends AbstractVerticle {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(SetHttpServer.class);

    // Const
    public static final int PORT = AppConfig.getInstance().getPort().getSetHttp();
    public static final int DEFAULT_EXPIRES = AppConfig.getInstance().getCache().getDefaultExpires();


    /**
     * start()
     */
    @Override
    public void start() throws Exception {
        mLogger.trace("start()");

        // Init
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setCompressionSupported(true);
        httpServerOptions.setPort(PORT);
        httpServerOptions.setIdleTimeout(3); // seconds
        HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

        // Routing
        Router router = Router.router(vertx);
        setRouter(router);

        // Listen
        httpServer.requestHandler(router::accept).listen();

        // End
        logging(httpServerOptions, router);
    }

    /**
     * Logging
     */
    private void logging(HttpServerOptions httpServerOptions, Router router) {
        synchronized (mLogger) {
            // Begin
            mLogger.info("------------------------------------------------------------------------");
            mLogger.info("API HTTP Server");
            mLogger.info("------------------------------------------------------------------------");

            // Vert.x
            mLogger.info("Vert.x clustered = " + getVertx().isClustered());
            mLogger.info("Vert.x deployment ID = " + deploymentID());

            // Http Server Options
            mLogger.info("Port = " + httpServerOptions.getPort());
            mLogger.info("Idle timeout (second) = " + httpServerOptions.getIdleTimeout());
            mLogger.info("Compression supported = " + httpServerOptions.isCompressionSupported());
            mLogger.info("Compression level = " + httpServerOptions.getCompressionLevel());

            // Route
            router.getRoutes().forEach(r -> {
                mLogger.info("Routing path = " + r.getPath());
            });

            // End
            mLogger.info("------------------------------------------------------------------------");
        }
    }


    /**
     * Set app router
     */
    private void setRouter(Router router) {
        // POST /*
        router.route().method(HttpMethod.POST).path("/*").handler(routingContext -> {
            final String headerContentType = routingContext.request().getHeader(HttpHeaderNames.CONTENT_TYPE);
            mLogger.trace("content-type # " + headerContentType);

            // application/json
            if (HttpHeaderValues.APPLICATION_JSON.contentEqualsIgnoreCase(headerContentType)) {
                routingContext.request().bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        // Json
                        JsonObject jsonObject = null;
                        try {
                            String body = event.toString();
                            jsonObject = new GsonBuilder().create().fromJson(body, JsonObject.class);
                        } catch (Exception e) {
                            response(routingContext, 400, "Bad Request (Invalid Json)");
                            return;
                        }

                        // Key
                        final String path = routingContext.request().uri();
                        if (StringUtils.isEmpty(path) == true) {
                            response(routingContext, 400, "Bad Request (Invalid Path)");
                            return;
                        }
                        final String key = path;

                        // Value
                        String value = null;
                        try {
                            value = jsonObject.get("value").getAsString();
                        } catch (Exception e) {
                            response(routingContext, 400, "Bad Request (Invalid Value)");
                            return;
                        }

                        // ContentType
                        String contentType = "";
                        try {
                            contentType = jsonObject.get("content_type").getAsString();
                        } catch (Exception e) {
                            // Nothing
                        }

                        // Expires
                        int expires = DEFAULT_EXPIRES;
                        try {
                            expires = jsonObject.get("expires").getAsInt();
                        } catch (Exception e) {
                            // Nothing
                        }

                        // Handle
                        handleRequest(routingContext, key, value, contentType, expires);
                    }
                });
            }

            // application/x-www-form-urlencoded
            else if (HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.contentEqualsIgnoreCase(headerContentType)) {
                routingContext.request().bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        // Parameter
                        Map<String, String> param = null;
                        try {
                            String body = event.toString();
                            param = Util.splitQuery(body);
                        } catch (Exception e) {
                            response(routingContext, 400, "Bad Request (Invalid Query)");
                            return;
                        }

                        // Key
                        final String path = routingContext.request().uri();
                        if (StringUtils.isEmpty(path) == true) {
                            response(routingContext, 400, "Bad Request (Invalid Path)");
                            return;
                        }
                        final String key = path;

                        // Value
                        String value = null;
                        try {
                            value = param.get("value");
                        } catch (Exception e) {
                            response(routingContext, 400, "Bad Request (Invalid Value)");
                            return;
                        }

                        // ContentType
                        String contentType = "";
                        try {
                            contentType = param.get("content_type");
                        } catch (Exception e) {
                            // Nothing
                        }

                        // Expires
                        int expires = DEFAULT_EXPIRES;
                        try {
                            String temp = param.get("expires");
                            expires = Integer.parseInt(temp);
                        } catch (Exception e) {
                            // Nothing
                        }

                        // Handle
                        handleRequest(routingContext, key, value, contentType, expires);
                    }
                });
            }

            // multipart/form-data
            else {
                String[] headerArray = StringUtils.split(headerContentType, ";");
                if (headerArray.length > 1) {
                    String headerString = StringUtils.trim(headerArray[0]);
                    if (HttpHeaderValues.MULTIPART_FORM_DATA.contentEqualsIgnoreCase(headerString) == true) {
                        routingContext.request().setExpectMultipart(true);
                        routingContext.request().endHandler(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                // Form
                                MultiMap form = null;
                                try {
                                    form = routingContext.request().formAttributes();
                                } catch (Exception e) {
                                    response(routingContext, 400, "Bad Request (Invalid Form Data)");
                                    return;
                                }

                                // Key
                                final String path = routingContext.request().uri();
                                if (StringUtils.isEmpty(path) == true) {
                                    response(routingContext, 400, "Bad Request (Invalid Path)");
                                    return;
                                }
                                final String key = path;

                                // Value
                                String value = null;
                                try {
                                    value = form.get("value");
                                } catch (Exception e) {
                                    response(routingContext, 400, "Bad Request (Invalid Value)");
                                    return;
                                }

                                // ContentType
                                String contentType = "";
                                try {
                                    contentType = form.get("content_type");
                                } catch (Exception e) {
                                    // Nothing
                                }

                                // Expires
                                int expires = DEFAULT_EXPIRES;
                                try {
                                    String temp = form.get("expires");
                                    expires = Integer.parseInt(temp);
                                } catch (Exception e) {
                                    // Nothing
                                }

                                // Handle
                                handleRequest(routingContext, key, value, contentType, expires);
                            }
                        });
                    } else {
                        response(routingContext, 400, "Bad Request (Invalid Content-Type)");
                    }
                } else {
                    response(routingContext, 400, "Bad Request (Invalid Content-Type)");
                }
            }
        });
    }

    /**
     * Handle request
     */
    private void handleRequest(RoutingContext routingContext, String key, String value, String contentType, int expires) {
        // Log
        mLogger.trace("key = " + key);
        mLogger.trace("value = " + value);
        mLogger.trace("contentType = " + contentType);
        mLogger.trace("expires = " + expires);

        // Process
        CacheData cacheData = new CacheData(value, contentType, expires);
        ClusteredCache.getInstance().set(key, cacheData, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean result) {
                if (result == true) {
                    response(routingContext, 200, "OK");
                } else {
                    response(routingContext, 500, "Internal Server Error");
                }
            }
        });
    }

    /**
     * Response HTTP error status
     */
    private void response(RoutingContext routingContext, int statusCode, String message) {
        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
        response.setStatusCode(statusCode);
        response.setStatusMessage(message);
        response.end(message);
        response.close();
    }
}
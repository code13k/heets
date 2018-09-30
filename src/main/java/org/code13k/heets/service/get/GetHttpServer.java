package org.code13k.heets.service.get;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.code13k.heets.business.ClusteredCache;
import org.code13k.heets.config.AppConfig;
import org.code13k.heets.model.CacheData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;

public class GetHttpServer extends AbstractVerticle {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(GetHttpServer.class);

    // Const
    public static final int PORT = AppConfig.getInstance().getPort().getGetHttp();
    private static String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

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
        // GET /*
        router.route().method(HttpMethod.GET).path("/*").handler(routingContext -> {
            routingContext.request().endHandler(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                    // Key
                    final String path = routingContext.request().uri();
                    if (StringUtils.isEmpty(path) == true) {
                        response(routingContext, 400, "Bad Request (Invalid Path)");
                        return;
                    }
                    final String key = path;

                    // Log
                    mLogger.trace("key = " + key);

                    // Process
                    ClusteredCache.getInstance().get(key, new Consumer<CacheData>() {
                        @Override
                        public void accept(CacheData cacheData) {
                            if (cacheData == null) {
                                response(routingContext, 404, "Not Found");
                            } else {
                                sendData(routingContext, cacheData);
                            }
                        }
                    });
                }
            });
        });
    }

    /**
     * Response HTTP status
     */
    private void response(RoutingContext routingContext, int statusCode, String message) {
        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
        response.setStatusCode(statusCode);
        response.setStatusMessage(message);
        response.end(message);
        response.close();
    }

    /**
     * Response HTTP data
     */
    private void sendData(RoutingContext routingContext, CacheData cacheData) {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        int lastModifiedTime = (int) (cacheData.getModified() / 1000);
        int expiresSeconds = cacheData.getExpires();
        int pastSeconds = currentTime - lastModifiedTime;
        int maxAgeSeconds = Math.max(0, expiresSeconds - pastSeconds);
        int expiresTime = currentTime + maxAgeSeconds;

        // Log
        mLogger.trace("currentTime = " + currentTime);
        mLogger.trace("lastModifiedTime = " + lastModifiedTime);
        mLogger.trace("expiresSeconds = " + expiresSeconds);
        mLogger.trace("pastSeconds = " + pastSeconds);
        mLogger.trace("maxAgeSeconds = " + maxAgeSeconds);
        mLogger.trace("expiresTime = " + expiresTime);

        // Check to modify
        boolean isModified = false;
        final String headerIfModifiedSince = routingContext.request().headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (StringUtils.isEmpty(headerIfModifiedSince)) {
            isModified = true;
        } else {
            try {
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
                Date date = format.parse(headerIfModifiedSince);
                long ifModifiedSince = date.getTime();
                mLogger.debug("ifModifiedSince : " + ifModifiedSince + ", lastModified : " + cacheData.getModified());
                isModified = (cacheData.getModified() > ifModifiedSince);
            } catch (Exception e) {
                isModified = true;
            }
        }

        // Cache-Control
        routingContext.response().putHeader(HttpHeaderNames.CACHE_CONTROL, "public, max-age=" + maxAgeSeconds);

        // Expires
        SimpleDateFormat expiresFormat = new SimpleDateFormat(DATE_FORMAT);
        expiresFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String expiresString = expiresFormat.format(expiresTime * 1000L);
        routingContext.response().putHeader(HttpHeaderNames.EXPIRES, expiresString);

        // Last-Modified
        if (isModified == true) {
            SimpleDateFormat lastModifiedFormat = new SimpleDateFormat(DATE_FORMAT);
            lastModifiedFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String lastModifiedString = lastModifiedFormat.format(lastModifiedTime * 1000L);
            routingContext.response().putHeader(HttpHeaderNames.LAST_MODIFIED, lastModifiedString);
        }

        // Content-Type
        if (StringUtils.isEmpty(cacheData.getContentType()) == false) {
            routingContext.response().putHeader(HttpHeaderNames.CONTENT_TYPE, cacheData.getContentType());
        }

        // Date
        SimpleDateFormat currentDateFormat = new SimpleDateFormat(DATE_FORMAT);
        currentDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String currentDateString = currentDateFormat.format(currentTime * 1000L);
        routingContext.response().putHeader(HttpHeaderNames.DATE, currentDateString);

        // Not Modified (304)
        if (isModified == false) {
            String statusMessage = "Not Modified";
            routingContext.response().setStatusCode(304).setStatusMessage(statusMessage).end(statusMessage);
        }

        // OK (200)
        else {
            routingContext.response().putHeader(HttpHeaderNames.ACCEPT_RANGES, "bytes");
            routingContext.response().putHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            routingContext.response().end(cacheData.getValue());
        }
    }
}
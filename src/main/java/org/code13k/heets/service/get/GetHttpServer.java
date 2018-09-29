package org.code13k.heets.service.get;

import io.netty.handler.codec.http.HttpHeaderNames;
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
import org.code13k.heets.model.CacheData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
            routingContext.request().bodyHandler(new Handler<Buffer>() {
                @Override
                public void handle(Buffer event) {
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
        /**
         * Content Type
         */
        if (StringUtils.isEmpty(cacheData.getContentType()) == false) {
            routingContext.response().putHeader(HttpHeaderNames.CONTENT_TYPE, cacheData.getContentType());
        }

        /**
         * Check to modify
         */
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

        /**
         * Not Modified (304)
         */
        if (isModified == false) {
            routingContext.response().setStatusCode(304).end();
            return;
        }

        /**
         * Modified
         */
        SimpleDateFormat expiresFormat = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat lastModifiedFormat = new SimpleDateFormat(DATE_FORMAT);
        expiresFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        lastModifiedFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, cacheData.getBrowserCacheExpiration());
        String expires = expiresFormat.format(calendar.getTime());
        String lastModified = lastModifiedFormat.format(cacheData.getModified());

        // Put Header
        routingContext.response().putHeader(HttpHeaderNames.CACHE_CONTROL, "public, max-age=" + cacheData.getBrowserCacheExpiration());
        routingContext.response().putHeader(HttpHeaderNames.EXPIRES, expires);
        routingContext.response().putHeader(HttpHeaderNames.LAST_MODIFIED, lastModified);

        /**
         * END
         */
        routingContext.response().end(cacheData.getValue());
    }
}
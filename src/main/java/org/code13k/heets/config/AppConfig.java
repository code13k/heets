package org.code13k.heets.config;

import org.code13k.heets.lib.Util;
import org.code13k.heets.model.config.app.CacheInfo;
import org.code13k.heets.model.config.app.ClusterInfo;
import org.code13k.heets.model.config.app.PortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class AppConfig extends BasicConfig {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(AppConfig.class);

    // Data
    private PortInfo mPortInfo = new PortInfo();
    private CacheInfo mCacheInfo = new CacheInfo();
    private ClusterInfo mClusterInfo = new ClusterInfo();

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final AppConfig INSTANCE = new AppConfig();
    }

    public static AppConfig getInstance() {
        return AppConfig.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private AppConfig() {
        mLogger.trace("AppConfig()");
    }

    @Override
    protected String getDefaultConfigFilename() {
        return "default_app_config.yml";
    }

    @Override
    protected String getConfigFilename() {
        return "app_config.yaml";
    }

    @Override
    protected boolean loadConfig(final String content, final String filePath) {
        try {
            Yaml yaml = new Yaml();
            LinkedHashMap yamlObject = yaml.load(content);
            mLogger.trace("yamlObject class name = " + yamlObject.getClass().getName());
            mLogger.trace("yamlObject = " + yamlObject);

            // PortInfo
            LinkedHashMap portObject = (LinkedHashMap) yamlObject.get("port");
            mLogger.trace("portObject class name = " + portObject.getClass().getName());
            mLogger.trace("portObject = " + portObject);
            Integer portGetHttp = (Integer) portObject.get("get_http");
            if (Util.isValidPortNumber(portGetHttp) == false) {
                mLogger.error("Invalid get_http of port : " + portGetHttp);
                return false;
            }
            Integer portSetHttp = (Integer) portObject.get("set_http");
            if (Util.isValidPortNumber(portSetHttp) == false) {
                mLogger.error("Invalid set_http of port : " + portSetHttp);
                return false;
            }
            Integer portApiHttp = (Integer) portObject.get("api_http");
            if (Util.isValidPortNumber(portApiHttp) == false) {
                mLogger.error("Invalid api_http of port : " + portApiHttp);
                return false;
            }
            mPortInfo.setGetHttp(portGetHttp);
            mPortInfo.setSetHttp(portSetHttp);
            mPortInfo.setApiHttp(portApiHttp);

            // CacheInfo
            LinkedHashMap cacheObject = (LinkedHashMap) yamlObject.get("cache");
            Integer cacheDefaultBrowserExpiration = (Integer) cacheObject.get("default_browser_cache_expiration");
            if (cacheDefaultBrowserExpiration < 0) {
                mLogger.error("Invalid default_browser_cache_expiration of cache : " + cacheDefaultBrowserExpiration);
                return false;
            }
            Integer cacheDefaultTtl = (Integer) cacheObject.get("default_ttl");
            if (cacheDefaultTtl <= 0) {
                mLogger.error("Invalid default_ttl of cache : " + cacheDefaultTtl);
                return false;
            }
            mCacheInfo.setDefaultBrowserCacheExpiration(cacheDefaultBrowserExpiration);
            mCacheInfo.setDefaultTtl(cacheDefaultTtl);

            // ClusterInfo
            LinkedHashMap clusterObject = (LinkedHashMap) yamlObject.get("cluster");
            if (clusterObject != null) {
                mLogger.trace("portObject class name = " + portObject.getClass().getName());
                mLogger.trace("portObject = " + portObject);
                Integer clusterPort = (Integer) clusterObject.get("port");
                if (Util.isValidPortNumber(clusterPort) == false) {
                    mLogger.error("Invalid port of cluster : " + clusterPort);
                    return false;
                }
                ArrayList<String> clusterNodes = (ArrayList<String>) clusterObject.get("nodes");
                mClusterInfo.setPort(clusterPort);
                mClusterInfo.setNodes(clusterNodes);
            }
        } catch (Exception e) {
            mLogger.error("Failed to load config file", e);
            return false;
        }
        return true;
    }

    @Override
    public void logging() {
        // Begin
        mLogger.info("------------------------------------------------------------------------");
        mLogger.info("Application Configuration");
        mLogger.info("------------------------------------------------------------------------");

        // Config File Path
        mLogger.info("Config file path = " + getConfigFilename());

        // PortInfo
        mLogger.info("get_http of PortInfo = " + mPortInfo.getGetHttp());
        mLogger.info("set_http of PortInfo = " + mPortInfo.getSetHttp());
        mLogger.info("api_http of PortInfo = " + mPortInfo.getApiHttp());

        // CacheInfo
        mLogger.info("default_browser_cache_expiration of CacheInfo = " + mCacheInfo.getDefaultBrowserCacheExpiration());
        mLogger.info("default_ttl of CacheInfo = " + mCacheInfo.getDefaultTtl());

        // ClusterInfo
        mLogger.info("port of ClusterInfo = " + mClusterInfo.getPort());
        mLogger.info("nodes of ClusterInfo = " + mClusterInfo.getNodes());

        // End
        mLogger.info("------------------------------------------------------------------------");
    }

    /**
     * Get port
     */
    public PortInfo getPort() {
        return mPortInfo;
    }

    /**
     * Get cache
     */
    public CacheInfo getCache() {
        return mCacheInfo;
    }

    /**
     * Get cluster
     */
    public ClusterInfo getCluster() {
        return mClusterInfo;
    }
}

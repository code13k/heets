package org.code13k.heets.model.config.app;

public class CacheInfo {
    private int defaultBrowserCacheExpiration;
    private int defaultTtl;

    public int getDefaultBrowserCacheExpiration() {
        return defaultBrowserCacheExpiration;
    }

    public void setDefaultBrowserCacheExpiration(int defaultBrowserCacheExpiration) {
        this.defaultBrowserCacheExpiration = defaultBrowserCacheExpiration;
    }

    public int getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(int defaultTtl) {
        this.defaultTtl = defaultTtl;
    }
}

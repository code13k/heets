package org.code13k.heets.model;

public class CacheData extends BasicModel {
    private String value;
    private String contentType;
    private int browserCacheExpiration;
    private long modified;


    public CacheData(String value, String contentType, int browserCacheExpiration){
        setValue(value);
        setContentType(contentType);
        setBrowserCacheExpiration(browserCacheExpiration);
        updateModified();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getBrowserCacheExpiration() {
        return browserCacheExpiration;
    }

    public void setBrowserCacheExpiration(int browserCacheExpiration) {
        this.browserCacheExpiration = browserCacheExpiration;
    }

    public void updateModified(){
        this.modified = System.currentTimeMillis() / 1000 * 1000;
    }

    public long getModified(){
        return this.modified;
    }
}

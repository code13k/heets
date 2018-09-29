package org.code13k.heets.model;

public class CacheData extends BasicModel {
    private String value;
    private String contentType;
    private int expires;
    private long modified;


    public CacheData(String value, String contentType, int expires){
        setValue(value);
        setContentType(contentType);
        setExpires(expires);
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

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public void updateModified(){
        this.modified = System.currentTimeMillis() / 1000 * 1000;
    }

    public long getModified(){
        return this.modified;
    }
}

package org.code13k.heets.model.config.app;

import org.code13k.heets.model.BasicModel;

public class CacheInfo extends BasicModel {
    private int defaultExpires;

    public int getDefaultExpires() {
        return defaultExpires;
    }

    public void setDefaultExpires(int defaultExpires) {
        this.defaultExpires = defaultExpires;
    }
}

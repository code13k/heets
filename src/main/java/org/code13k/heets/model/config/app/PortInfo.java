package org.code13k.heets.model.config.app;

import org.code13k.heets.model.BasicModel;

public class PortInfo extends BasicModel {
    private int getHttp;
    private int setHttp;
    private int apiHttp;

    public int getGetHttp() {
        return getHttp;
    }

    public void setGetHttp(int getHttp) {
        this.getHttp = getHttp;
    }

    public int getSetHttp() {
        return setHttp;
    }

    public void setSetHttp(int setHttp) {
        this.setHttp = setHttp;
    }

    public int getApiHttp() {
        return apiHttp;
    }

    public void setApiHttp(int apiHttp) {
        this.apiHttp = apiHttp;
    }
}

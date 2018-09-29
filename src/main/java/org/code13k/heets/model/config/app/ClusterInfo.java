package org.code13k.heets.model.config.app;

import java.util.ArrayList;

public class ClusterInfo {
    private int port;
    private ArrayList<String> nodes;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList<String> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<String> nodes) {
        this.nodes = nodes;
    }
}

package org.code13k.heets.service.api.controller;

import org.code13k.heets.app.Cluster;

public class ClusterAPI extends BasicAPI {
    /**
     * Environment
     */
    public String status() {
        return toResultJsonString(Cluster.getInstance().values());
    }
}

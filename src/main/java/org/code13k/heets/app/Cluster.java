package org.code13k.heets.app;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.*;
import org.code13k.heets.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Cluster {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(Cluster.class);

    // Data
    private HazelcastInstance mHazelcastInstance = null;

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final Cluster INSTANCE = new Cluster();
    }

    public static Cluster getInstance() {
        return Cluster.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private Cluster() {
        mLogger.trace("Cluster()");
    }

    /**
     * Initialize
     */
    public void init() {
        // Config
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(AppConfig.getInstance().getCluster().getPort());
        networkConfig.setPortCount(3);

        // JoinConfig
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        // TcpIpConfig
        TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
        ArrayList<String> nodes = AppConfig.getInstance().getCluster().getNodes();
        tcpIpConfig.setMembers(nodes);
        tcpIpConfig.setRequiredMember(null);
        tcpIpConfig.setEnabled(true);

        // Instance
        mHazelcastInstance = Hazelcast.newHazelcastInstance(config);
        mHazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                mLogger.info("Cluster # Member added : " + membershipEvent.toString());
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                mLogger.error("Cluster # Member removed : " + membershipEvent.toString());
            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
                mLogger.trace("Cluster # Member attribute changed : " + memberAttributeEvent.toString());
            }
        });
    }

    /**
     * Get Hazelcast Instance
     */
    public HazelcastInstance getHazelcastInstance() {
        return mHazelcastInstance;
    }

    /**
     * Get clustered member count
     */
    public int getMemberCount() {
        try {
            return mHazelcastInstance.getCluster().getMembers().size();
        } catch (Exception e) {
            return 1;
        }
    }
}















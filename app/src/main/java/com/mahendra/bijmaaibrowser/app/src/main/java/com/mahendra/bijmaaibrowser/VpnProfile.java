
package com.mahendra.bijmaaibrowser;

public class VpnProfile {

    private final String serverId;
    private final String serverName;
    private final String protocol;
    private final String serverAddress;
    private final int port;
    private final String configuration;
    private final boolean enabled;

    public VpnProfile(
            String serverId,
            String serverName,
            String protocol,
            String serverAddress,
            int port,
            String configuration,
            boolean enabled) {

        this.serverId = serverId;
        this.serverName = serverName;
        this.protocol = protocol;
        this.serverAddress = serverAddress;
        this.port = port;
        this.configuration = configuration;
        this.enabled = enabled;
    }

    public String getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getPort() {
        return port;
    }

    public String getConfiguration() {
        return configuration;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isValid() {
        return serverAddress != null
                && !serverAddress.trim().isEmpty()
                && port > 0
                && port <= 65535
                && protocol != null
                && !protocol.trim().isEmpty()
                && configuration != null
                && !configuration.trim().isEmpty();
    }
}

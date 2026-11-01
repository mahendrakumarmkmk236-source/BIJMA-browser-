
package com.mahendra.bijmaaibrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VpnServerConfig {

    public final String id;
    public final String name;
    public final String country;
    public final String flag;
    public final String city;
    public final String protocol;
    public final String description;

    // असली सर्वर की जानकारी मिलने तक false रहेगा।
    public final boolean configured;

    private VpnServerConfig(
            String id,
            String name,
            String country,
            String flag,
            String city,
            String protocol,
            String description,
            boolean configured) {

        this.id = id;
        this.name = name;
        this.country = country;
        this.flag = flag;
        this.city = city;
        this.protocol = protocol;
        this.description = description;
        this.configured = configured;
    }

    public static List<VpnServerConfig> getServers() {

        List<VpnServerConfig> servers = new ArrayList<>();

        servers.add(new VpnServerConfig(
                "india-mumbai",
                "India - Mumbai",
                "India",
                "🇮🇳",
                "Mumbai",
                "WireGuard",
                "मुंबई सर्वर",
                false
        ));

        servers.add(new VpnServerConfig(
                "india-delhi",
                "India - Delhi",
                "India",
                "🇮🇳",
                "New Delhi",
                "WireGuard",
                "दिल्ली सर्वर",
                false
        ));

        servers.add(new VpnServerConfig(
                "singapore",
                "Singapore",
                "Singapore",
                "🇸🇬",
                "Singapore",
                "WireGuard",
                "एशिया सर्वर",
                false
        ));

        servers.add(new VpnServerConfig(
                "germany",
                "Germany",
                "Germany",
                "🇩🇪",
                "Frankfurt",
                "WireGuard",
                "यूरोप सर्वर",
                false
        ));

        servers.add(new VpnServerConfig(
                "usa",
                "United States",
                "USA",
                "🇺🇸",
                "New York",
                "WireGuard",
                "अमेरिका सर्वर",
                false
        ));

        servers.add(new VpnServerConfig(
                "japan",
                "Japan",
                "Japan",
                "🇯🇵",
                "Tokyo",
                "WireGuard",
                "जापान सर्वर",
                false
        ));

        return Collections.unmodifiableList(servers);
    }

    public boolean isReadyToConnect() {
        return configured;
    }

    public static VpnServerConfig findById(String id) {
        if (id == null) return null;

        for (VpnServerConfig server : getServers()) {
            if (server.id.equals(id)) {
                return server;
            }
        }

        return null;
    }
}

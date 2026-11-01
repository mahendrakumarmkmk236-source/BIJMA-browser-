
package com.mahendra.bijmaaibrowser;

import android.content.Context;
import android.content.SharedPreferences;

public class VpnServiceManager {

    private static final String PREFS_NAME = "bijma_vpn_settings";
    private static final String KEY_SELECTED_SERVER = "selected_server";
    private static final String KEY_CONNECTED = "connected";

    private final SharedPreferences preferences;
    private VpnServerConfig selectedServer;
    private VpnProfile activeProfile;

    public VpnServiceManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String savedId = preferences.getString(
                KEY_SELECTED_SERVER, "india-mumbai");

        selectedServer = VpnServerConfig.findById(savedId);
    }

    public synchronized boolean selectServer(String serverId) {
        VpnServerConfig server =
                VpnServerConfig.findById(serverId);

        if (server == null) {
            return false;
        }

        selectedServer = server;

        preferences.edit()
                .putString(KEY_SELECTED_SERVER, serverId)
                .apply();

        return true;
    }

    public synchronized VpnServerConfig getSelectedServer() {
        return selectedServer;
    }

    public synchronized void setProfile(VpnProfile profile) {
        activeProfile = profile;
    }

    public synchronized VpnProfile getProfile() {
        return activeProfile;
    }

    public synchronized boolean isReadyToConnect() {
        return selectedServer != null
                && selectedServer.isReadyToConnect()
                && activeProfile != null
                && activeProfile.isValid()
                && selectedServer.id.equals(
                        activeProfile.getServerId());
    }

    public synchronized boolean isConnected() {
        // वास्तविक VPN सेवा कनेक्शन की पुष्टि करेगी।
        return false;
    }

    public synchronized String getConnectionStatus() {
        if (isConnected()) {
            return "Connected";
        }

        if (selectedServer == null) {
            return "No server selected";
        }

        if (!isReadyToConnect()) {
            return "Server configuration required";
        }

        return "Ready for VPN service";
    }

    public synchronized void clearProfile() {
        activeProfile = null;
    }

    public synchronized void disconnectStatus() {
        preferences.edit()
                .putBoolean(KEY_CONNECTED, false)
                .apply();
    }
}

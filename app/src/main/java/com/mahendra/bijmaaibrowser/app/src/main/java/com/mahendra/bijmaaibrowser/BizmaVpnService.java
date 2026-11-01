
package com.mahendra.bijmaaibrowser;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class BizmaVpnService extends VpnService {

    public static final String ACTION_START =
            "com.mahendra.bijmaaibrowser.VPN_START";

    public static final String ACTION_STOP =
            "com.mahendra.bijmaaibrowser.VPN_STOP";

    public static final String ACTION_STATUS =
            "com.mahendra.bijmaaibrowser.VPN_STATUS";

    private static final String TAG = "BizmaVpnService";

    private ParcelFileDescriptor vpnInterface;
    private boolean connected = false;

    public static Intent getPermissionIntent(Context context) {
        return VpnService.prepare(context);
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, BizmaVpnService.class);
        intent.setAction(ACTION_START);

        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, BizmaVpnService.class);
        intent.setAction(ACTION_STOP);

        context.startService(intent);
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        if (intent == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_STOP.equals(action)) {
            disconnectVpn();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(action)) {
            if (VpnService.prepare(this) != null) {
                Log.w(TAG, "VPN permission is required.");
                sendStatus("Permission required");
                stopSelf();
                return START_NOT_STICKY;
            }

            Log.w(TAG,
                    "VPN server configuration and tunnel engine are required.");

            sendStatus("Server configuration required");
        }

        return START_NOT_STICKY;
    }

    private void disconnectVpn() {
        connected = false;

        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (Exception error) {
                Log.e(TAG, "VPN close error", error);
            }

            vpnInterface = null;
        }

        sendStatus("Disconnected");
    }

    private void sendStatus(String status) {
        Intent intent = new Intent(ACTION_STATUS);
        intent.setPackage(getPackageName());
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    public boolean isVpnConnected() {
        return connected;
    }

    @Override
    public void onRevoke() {
        disconnectVpn();
        stopSelf();
        super.onRevoke();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        disconnectVpn();
        super.onDestroy();
    }
}

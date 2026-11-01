
package com.mahendra.bijmaaibrowser;

import android.os.SystemClock;
import java.util.Locale;

public class VpnSessionStats {

    private long startTime;
    private long lastStopTime;
    private long uploadedBytes;
    private long downloadedBytes;
    private boolean connected;

    public VpnSessionStats() {
        reset();
    }

    public synchronized void startSession() {
        startTime = SystemClock.elapsedRealtime();
        lastStopTime = 0;
        uploadedBytes = 0;
        downloadedBytes = 0;
        connected = true;
    }

    public synchronized void stopSession() {
        if (connected) {
            lastStopTime = SystemClock.elapsedRealtime();
            connected = false;
        }
    }

    public synchronized void reset() {
        startTime = 0;
        lastStopTime = 0;
        uploadedBytes = 0;
        downloadedBytes = 0;
        connected = false;
    }

    public synchronized void addUploadedBytes(long bytes) {
        if (connected && bytes > 0) {
            uploadedBytes += bytes;
        }
    }

    public synchronized void addDownloadedBytes(long bytes) {
        if (connected && bytes > 0) {
            downloadedBytes += bytes;
        }
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    public synchronized long getUploadedBytes() {
        return uploadedBytes;
    }

    public synchronized long getDownloadedBytes() {
        return downloadedBytes;
    }

    public synchronized long getSessionDurationSeconds() {
        if (startTime == 0) return 0;

        long endTime = connected
                ? SystemClock.elapsedRealtime()
                : lastStopTime;

        return Math.max(0, (endTime - startTime) / 1000);
    }

    public synchronized String getFormattedDuration() {
        long seconds = getSessionDurationSeconds();

        return String.format(
                Locale.ROOT,
                "%02d:%02d:%02d",
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60
        );
    }

    public synchronized String getFormattedUpload() {
        return formatBytes(uploadedBytes);
    }

    public synchronized String getFormattedDownload() {
        return formatBytes(downloadedBytes);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";

        if (bytes < 1024L * 1024L) {
            return String.format(Locale.ROOT,
                    "%.2f KB", bytes / 1024.0);
        }

        if (bytes < 1024L * 1024L * 1024L) {
            return String.format(Locale.ROOT,
                    "%.2f MB", bytes / (1024.0 * 1024.0));
        }

        return String.format(Locale.ROOT,
                "%.2f GB",
                bytes / (1024.0 * 1024.0 * 1024.0));
    }
}

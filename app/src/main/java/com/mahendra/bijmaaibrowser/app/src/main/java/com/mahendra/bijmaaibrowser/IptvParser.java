
package com.mahendra.bijmaaibrowser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IptvParser {

    private IptvParser() {}

    public static class Channel {
        public final String name;
        public final String url;
        public final String logo;
        public final String group;
        public final String tvgId;

        public Channel(String name, String url,
                       String logo, String group, String tvgId) {
            this.name = name;
            this.url = url;
            this.logo = logo;
            this.group = group;
            this.tvgId = tvgId;
        }
    }

    public static List<Channel> parse(String content) {
        List<Channel> channels = new ArrayList<>();

        if (content == null || content.trim().isEmpty()) {
            return channels;
        }

        String name = null;
        String logo = null;
        String group = null;
        String tvgId = null;

        String[] lines = content.split("\\r?\\n");

        for (String raw : lines) {
            String line = raw.trim();

            if (line.isEmpty()) continue;

            if (line.startsWith("#EXTINF:")) {
                tvgId = getAttribute(line, "tvg-id");
                logo = getAttribute(line, "tvg-logo");
                group = getAttribute(line, "group-title");

                int comma = line.lastIndexOf(',');
                if (comma >= 0 && comma < line.length() - 1) {
                    name = line.substring(comma + 1).trim();
                }

                if (name == null || name.isEmpty()) {
                    name = getAttribute(line, "tvg-name");
                }

            } else if (line.startsWith("#EXTGRP:")) {
                group = line.substring(8).trim();

            } else if (!line.startsWith("#")
                    && isValidUrl(line)) {

                if (name == null || name.isEmpty()) {
                    name = "TV Channel " + (channels.size() + 1);
                }

                if (group == null || group.isEmpty()) {
                    group = "General";
                }

                channels.add(new Channel(
                        name, line, logo, group, tvgId
                ));

                name = null;
                logo = null;
                group = null;
                tvgId = null;
            }
        }

        return channels;
    }

    private static String getAttribute(String line, String key) {
        Pattern pattern = Pattern.compile(
                "(?:^|\\s)" + Pattern.quote(key) + "=\"([^\"]*)\""
        );

        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    public static boolean isValidUrl(String url) {
        if (url == null) return false;

        String value = url.trim().toLowerCase();

        return value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("rtmp://")
                || value.startsWith("rtsp://");
    }

    public static List<Channel> filterByGroup(
            List<Channel> channels, String group) {

        List<Channel> result = new ArrayList<>();

        if (channels == null || group == null) {
            return result;
        }

        for (Channel channel : channels) {
            if (group.equalsIgnoreCase(channel.group)) {
                result.add(channel);
            }
        }

        return result;
    }
}

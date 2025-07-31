package com.assetcare360.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {
    public static String extractJsonValue(String json, String key) {
        String pattern = String.format("\"%s\"\\s*:\\s*\"([^\"]*)\"", key);
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}

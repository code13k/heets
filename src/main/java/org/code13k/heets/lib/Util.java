package org.code13k.heets.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Util {

    /**
     * Check if the given number is valid port number.
     */
    public static boolean isValidPortNumber(Integer portNumber) {
        if (portNumber == null || portNumber < 1 || portNumber > 65535) {
            return false;
        }
        return true;
    }

    /**
     * splitQuery()
     */
    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}

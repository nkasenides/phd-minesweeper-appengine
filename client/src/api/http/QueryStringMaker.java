package api.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class QueryStringMaker {

    public static String makeQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }
        }
        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

}

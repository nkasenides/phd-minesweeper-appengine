package api.http;

import java.util.HashMap;

public class ParameterMap {

    private HashMap<String, String> params = new HashMap<>();

    public void add(String key, String value) {
        params.put(key, value);
    }

    public HashMap<String, String> get() {
        return params;
    }

}

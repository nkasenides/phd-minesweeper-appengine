package api.http;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public abstract class SyncHTTP {

    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 5000;

    private final String url;
    private final HashMap<String, String> params;
    private final RequestMethod requestMethod;
    private MimeType mimeType = MimeType.CONTENT_TYPE_JSON;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    public SyncHTTP(String url, ParameterMap params, RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        this.params = params.get();
        try {
            if (requestMethod == RequestMethod.GET) {
                this.url = url + "?" + QueryStringMaker.makeQueryString(this.params);
            } else {
                this.url = url;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        setConnectTimeout(10000);
        setReadTimeout(10000);
    }

    public void setContentType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void execute() {

        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod(requestMethod.toString());
            con.setRequestProperty(MimeType.CONTENT_TYPE_KEY, MimeType.CONTENT_TYPE_JSON.getText());
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.setDoOutput(true);

            if (requestMethod == RequestMethod.POST) {
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes(QueryStringMaker.makeQueryString(params));
                out.flush();
                out.close();
            }

            int statusCode = con.getResponseCode();

            Reader streamReader = null;
            if (statusCode > 299) {
                streamReader = new InputStreamReader(con.getErrorStream());
            } else {
                streamReader = new InputStreamReader(con.getInputStream());
            }

            BufferedReader in = new BufferedReader(streamReader);
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            onResponseReceived(content.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void onResponseReceived(String response);

}

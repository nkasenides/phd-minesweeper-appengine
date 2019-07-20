package api.http;

import api.AsyncTask;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

//TODO Improve based on: https://www.baeldung.com/java-http-request
public abstract class HTTPAsyncTask extends AsyncTask {

    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT = "text/*";
    private static final String CONTENT_TYPE_STREAM = "application/xml";
    private static final String CONTENT_TYPE_BMP = "image/bmp";
    private static final String CONTENT_TYPE_CSS = "text/css";
    private static final String CONTENT_TYPE_CSV = "text/csv";
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String CONTENT_TYPE_JPG = "image/jpeg";
    private static final String CONTENT_TYPE_MP3 = "audio.mpeg";
    private static final String CONTENT_TYPE_PNG = "image/png";
    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 5000;
    //TODO All MIME types as listed here: https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types

    private final String url;
    private final HashMap<String, String> params;
    private final RequestMethod requestMethod;
    private String contentType = CONTENT_TYPE_JSON;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    public HTTPAsyncTask(String url, ParameterMap params, RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        this.params = params.get();
        try {
            if (requestMethod == RequestMethod.GET) {
                this.url = url + "?" + makeQueryString(this.params);
            } else {
                this.url = url;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    protected void onPreExecute() { }

    @Override
    protected void doInBackground() {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod(requestMethod.toString());
            con.setRequestProperty(CONTENT_TYPE_KEY, contentType);
            con.setConnectTimeout(connectTimeout);
            con.setReadTimeout(readTimeout);
            con.setDoOutput(true);

            if (requestMethod == RequestMethod.POST) {
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes(makeQueryString(params));
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
            throw new RuntimeException(e);
        }
    }

    protected abstract void onResponseReceived(String response);

    @Override
    protected void onPostExecute() { }

    private static String makeQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }
        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

}

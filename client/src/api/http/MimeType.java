package api.http;

public enum MimeType {

    CONTENT_TYPE_JSON("application/json"),
    CONTENT_TYPE_TEXT("text/*"),
    CONTENT_TYPE_STREAM("application/xml"),
    CONTENT_TYPE_BMP("image/bmp"),
    CONTENT_TYPE_CSS("text/css"),
    CONTENT_TYPE_CSV("text/csv"),
    CONTENT_TYPE_HTML("text/html"),
    CONTENT_TYPE_JPG("image/jpeg"),
    CONTENT_TYPE_MP3("audio.mpeg"),
    CONTENT_TYPE_PNG("image/png"),

    ;

    public static final String CONTENT_TYPE_KEY = "Content-Type";

    private final String text;

    MimeType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}

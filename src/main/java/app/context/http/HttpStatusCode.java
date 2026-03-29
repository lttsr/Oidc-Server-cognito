package app.context.http;

public final class HttpStatusCode {
    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String NO_CONTENT = "204";

    public static final String MULTIPLE_CHOICES = "300";
    public static final String MOVED_PERMANENTLY = "301";
    public static final String FOUND = "302";
    public static final String SEE_OTHER = "303";
    public static final String NOT_MODIFIED = "304";
    public static final String TEMPORARY_REDIRECT = "307";
    public static final String PERMANENT_REDIRECT = "308";

    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String CONFLICT = "409";
    public static final String UNPROCESSABLE_ENTITY = "422";
    public static final String TOO_MANY_REQUESTS = "429";

    public static final String INTERNAL_SERVER_ERROR = "500";
    public static final String BAD_GATEWAY = "502";
    public static final String SERVICE_UNAVAILABLE = "503";
    public static final String GATEWAY_TIMEOUT = "504";

    private HttpStatusCode() {
    }
}

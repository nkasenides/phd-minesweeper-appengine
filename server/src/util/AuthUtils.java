package util;

import respondx.ErrorResponse;

public class AuthUtils {

    public static final String ADMIN_PASSWORD = "1234";
    public static final ErrorResponse SECURITY_RESPONSE = new ErrorResponse("Access denied", "You are not authorized to access this service.");
    public static final ErrorResponse MISING_PASSWORD_RESPONSE = new ErrorResponse("Password missing", "Please provide a password.");
    public static final ErrorResponse MISSING_SESSION_ID_RESPONSE = new ErrorResponse("Missing session ID", "Please provide a session ID.");

}

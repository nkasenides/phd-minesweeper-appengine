package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validatePassword(String password) {
        if (password.length() < 6 || password.length() > 255) {
            return false;
        }
        return true;
    }

    public static boolean validateEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    public static boolean validateUsername(String username) {
        if (!username.matches("[a-zA-Z0-9]+") || username.length() > 20 || username.length() < 3) {
            return false;
        }
        return true;
    }

    public static boolean validateAppName(String appName) {
        if (!appName.matches("[a-zA-Z0-9]+") || appName.length() > 20 || appName.length() < 5) {
            return false;
        }
        return true;
    }

    public static boolean validateStringAlNumOnly(String text) {
        if (!text.matches("[a-zA-Z0-9]+")) {
            return false;
        }
        return true;
    }

    public static boolean validateStringAlNumSpaceOnly(String text) {
        if (!text.matches("[a-zA-Z0-9\\s]+")) {
            return false;
        }
        return true;
    }


}

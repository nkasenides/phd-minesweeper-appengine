package util;

public class TimeUtils {

    public static String formatMilliseconds(long ms) {
        long minutes = (ms / 1000) / 60;
        long seconds = (ms / 1000) % 60;
        return String.format("%d minutes and %d seconds", minutes, seconds);
    }

}

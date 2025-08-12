package com.modernac.util;

public final class TimeUtil {
    private TimeUtil() {}

    public static long parseTime(String input) {
        if (input == null || input.isEmpty()) return 0L;
        try {
            long value = Long.parseLong(input.substring(0, input.length() - 1));
            char unit = input.charAt(input.length() - 1);
            switch (unit) {
                case 'h':
                case 'H':
                    return value * 60L * 60L * 1000L;
                case 'm':
                case 'M':
                    return value * 60L * 1000L;
                case 's':
                case 'S':
                    return value * 1000L;
                default:
                    return value * 1000L;
            }
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}

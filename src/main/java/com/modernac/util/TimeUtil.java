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

  public static String formatDuration(long ms) {
    if (ms <= 0) return "0s";
    long sec = ms / 1000;
    long h = sec / 3600;
    long m = (sec % 3600) / 60;
    long s = sec % 60;
    StringBuilder sb = new StringBuilder();
    if (h > 0) sb.append(h).append("h");
    if (m > 0) sb.append(m).append("m");
    if (s > 0 || sb.length() == 0) sb.append(s).append("s");
    return sb.toString();
  }
}

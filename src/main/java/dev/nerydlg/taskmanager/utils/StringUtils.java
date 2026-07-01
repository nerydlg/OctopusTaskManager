package dev.nerydlg.taskmanager.utils;

public class StringUtils {

  public static void removeBlankSpace(StringBuilder sb) {
    int j = 0;
    for (int i = 0; i < sb.length(); i++) {
      if (!Character.isWhitespace(sb.charAt(i))) {
        sb.setCharAt(j++, sb.charAt(i));
      }
    }
    sb.delete(j, sb.length());
  }

  public static void removeTrailingSpace(StringBuilder sb) {
    int j = sb.length() - 1;
    while (j >= 0 && Character.isWhitespace(sb.charAt(j))) {
      j--;
    }
    sb.delete(j + 1, sb.length());
  }
}

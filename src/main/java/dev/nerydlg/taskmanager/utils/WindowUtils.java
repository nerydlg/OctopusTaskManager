package dev.nerydlg.taskmanager.utils;

import javax.swing.JFrame;
import java.io.File;

public class WindowUtils {

  public static final String APP_NAME = "Octopus Task Manager";

  /**
   * Shows the task file currently in use next to the application name, so the open
   * file stays visible after it is switched from the File menu.
   */
  public static void setStorageTitle(JFrame frame, String storagePath) {
    if (storagePath == null || storagePath.isBlank()) {
      frame.setTitle(APP_NAME);
      return;
    }
    frame.setTitle(APP_NAME + " - " + new File(storagePath).getName());
  }
}

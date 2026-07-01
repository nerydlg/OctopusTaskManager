package dev.nerydlg.taskmanager.utils;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public class ImageUtils {

  public static ImageIcon createImageIcon(String path, int width, int height) {
    URL url = getResource(path);
    if (url == null) {
      throw new IllegalArgumentException("Icon not found on classpath: " + path);
    }
    Image img = new ImageIcon(url).getImage()
        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
    return new ImageIcon(img);
  }

  public static URL getResource(String path) {
    return ImageUtils.class.getResource(path);
  }
}

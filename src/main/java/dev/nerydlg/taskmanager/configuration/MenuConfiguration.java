package dev.nerydlg.taskmanager.configuration;

import dev.nerydlg.taskmanager.window.menu.AppMenuBar;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.util.List;

public class MenuConfiguration {

  public JMenu createFileMenu() {
    JMenu fileMenu = new JMenu("File");
    List<JMenuItem> fileMenuItems = List.of(
        new JMenuItem("Open Task File"),
        new JMenuItem("Create New Task File")
    );
    for (JMenuItem menuItem : fileMenuItems) {
      fileMenu.add(menuItem);
    }
    fileMenu.addSeparator();
    fileMenu.add(new JMenuItem("Exit"));
    return fileMenu;
  }

  public List<JMenu> createMenuItems() {
    return List.of(createFileMenu());
  }

  public JMenuBar createJMenuBar() {
    return new JMenuBar();
  }

  public AppMenuBar createAppMenuBar() {
    return new AppMenuBar(createJMenuBar(), createMenuItems());
  }

}

package dev.nerydlg.taskmanager.window.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class AppMenuBar implements ActionListener, ItemListener {

  private final JMenuBar menuBar;
  private final List<JMenu> menuItems;

  public AppMenuBar(JMenuBar menuBar, List<JMenu> menuItems) {
    this.menuBar = menuBar;
    this.menuItems = menuItems;
  }

  public void init() {
    for (JMenuItem menuItem : menuItems) {
      menuBar.add(menuItem);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }

  @Override
  public void itemStateChanged(ItemEvent e) {

  }

  public JMenuBar getMenuBar() {
    return menuBar;
  }
}

package dev.nerydlg.taskmanager.components;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.FlowLayout;
import java.awt.Font;


public class ButtonTabComponent extends JPanel {

  private final JButton button;
  private final JTabbedPane parent;
  private final String title;

  public ButtonTabComponent(String title, JTabbedPane parent, JButton button) {
    super(new FlowLayout(FlowLayout.LEFT, 5, 0));
    this.parent = parent;
    this.title = title;
    this.button = button;
    JLabel label = new JLabel(title);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    label.setOpaque(false);
    if (!title.isEmpty()) {
      add(label);
    }
    add(button);
  }
}

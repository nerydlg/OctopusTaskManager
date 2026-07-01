package dev.nerydlg.taskmanager.components;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static dev.nerydlg.taskmanager.utils.ImageUtils.createImageIcon;

public class ButtonCloseTab extends JButton implements ActionListener {

  public JTabbedPane parent;
  public JFrame frame;

  public ButtonCloseTab(JFrame frame, JTabbedPane parent) {
    int size = 15;
    setIcon(createImageIcon("/ico/icons8-close-30.png", 10, 10));
    setUI(new BasicButtonUI());
    setPreferredSize(new Dimension(size, size));
    setBorderPainted(false);
    setContentAreaFilled(false);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    setFocusPainted(true);
    addActionListener(this);
    this.parent = parent;
    this.frame = frame;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // getParent() is the ButtonTabComponent panel that holds this button,
    // i.e. the component installed via setTabComponentAt(...).
    int i = parent.indexOfTabComponent(getParent());
    if (i > 0) {
      ConfirmationDialog confirmationDialog = new ConfirmationDialog(frame, "Are you sure?");
      confirmationDialog.setVisible(true);
      if (confirmationDialog.isConfirmed()) {
        parent.remove(i);
      }
    }
    if (i == 0) {
      JDialog dialog = new AlertDialog(frame, "Dashboard is not closable");
      dialog.setVisible(true);
    }
  }
}

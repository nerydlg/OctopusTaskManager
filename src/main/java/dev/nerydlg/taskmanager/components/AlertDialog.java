package dev.nerydlg.taskmanager.components;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import static dev.nerydlg.taskmanager.utils.ImageUtils.createImageIcon;

public class AlertDialog extends JDialog {

  private final JFrame parent;

  public AlertDialog(JFrame parent, String message) {
    super(parent, message, true);
    this.parent = parent;
    setTitle("Warning!");
    ImageIcon icon = createImageIcon("/ico/icons8-alert-48.png", 30, 30);
    JPanel panel = new JPanel();
    JLabel iconLabel = new JLabel(icon);
    panel.add(iconLabel);
    panel.add(new JLabel(message));

    JButton ok = new JButton("ok");

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    buttonPanel.add(ok);

    ok.addActionListener(e -> setVisible(false));

    getContentPane().add(panel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    // Enter in the text field confirms.
    getRootPane().setDefaultButton(ok);

    pack();
    setLocationRelativeTo(parent);
  }
}

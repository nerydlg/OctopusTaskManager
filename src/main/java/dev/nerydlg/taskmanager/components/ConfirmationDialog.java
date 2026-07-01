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

public class ConfirmationDialog extends JDialog {

  private final JFrame parent;
  private boolean confirmed = false;

  public ConfirmationDialog(JFrame parent, String message) {
    super(parent, "Confirmation", true);
    this.parent = parent;

    ImageIcon icon = createImageIcon("/ico/icons8-information-50.png", 30, 30);
    JLabel iconLabel = new JLabel(icon);
    JLabel label = new JLabel(message);
    JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

    messagePanel.add(iconLabel);
    messagePanel.add(label);

    JButton ok = new JButton("Yes");
    JButton cancel = new JButton("No");

    ok.addActionListener(e -> {
      confirmed = true;
      setVisible(false);
    });

    cancel.addActionListener(e -> setVisible(false));

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    getContentPane().add(messagePanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(parent);
  }

  public boolean isConfirmed() {
    return confirmed;
  }
}

package dev.nerydlg.taskmanager.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;

public class ProjectDialog extends JDialog {

  private static final Logger log = LogManager.getLogger(ProjectDialog.class);

  private final JTabbedPane parent;
  private final JTextField textField;
  private boolean confirmed = false;

  public ProjectDialog(JFrame owner, JTabbedPane parent) {
    super(owner, "New Project", true);
    this.parent = parent;

    JLabel label = new JLabel("Project name: ");
    textField = new JTextField(15);

    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    inputPanel.add(label);
    inputPanel.add(textField);

    JButton ok = new JButton("OK");
    JButton cancel = new JButton("Cancel");
    ok.addActionListener(e -> {
      confirmed = true;

      setVisible(false);
    });
    cancel.addActionListener(e -> cleanAndHide());

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    getContentPane().add(inputPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    // Enter in the text field confirms.
    getRootPane().setDefaultButton(ok);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(java.awt.event.ComponentEvent e) {
        textField.requestFocusInWindow();
      }
    });

    pack();
    setLocationRelativeTo(owner);
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getText() {
    return textField.getText();
  }

  public void cleanAndHide() {
    textField.setText("");
    setVisible(false);
  }

}

package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.time.LocalDateTime;

public class ProjectDialog extends JDialog {

  private static final Logger log = LogManager.getLogger(ProjectDialog.class);

  private final JTabbedPane parent;
  private boolean confirmed = false;
  private Project project;

  public ProjectDialog(JFrame owner, JTabbedPane parent, Project project) {
    super(owner, project == null ? "Create Project" : "Edit Project", true);
    this.parent = parent;

    JLabel label = new JLabel("Project name: ");
    JTextField textField = new JTextField(15);

    JLabel comboLabel = new JLabel("Status: ");
    String[] values = new String[]{ "Open", "Closed", "Finalized", "Deleted"};
    JComboBox<String> status = new JComboBox<>(values);

    if(project != null) {
      textField.setText(project.name());
      status.setSelectedIndex(project.status());
    }

    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    inputPanel.add(label);
    inputPanel.add(textField);

    if(project != null) {
      inputPanel.add(comboLabel);
      inputPanel.add(status);
    }

    JButton ok = new JButton("OK");
    JButton cancel = new JButton("Cancel");
    ok.addActionListener(e -> {
      confirmed = true;
      if(textField.getText().isEmpty()) {
        JOptionPane.showMessageDialog(ProjectDialog.this, "Please enter a project name", "Alert", JOptionPane.WARNING_MESSAGE);
        return;
      }
      if(project != null) {
        this.project = new Project(project.id(), textField.getText(), status.getSelectedIndex(), project.createdAt(), LocalDateTime.now());
      } else {
        this.project = new Project(null, textField.getText(), status.getSelectedIndex(), LocalDateTime.now(), LocalDateTime.now());
      }
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

  public ProjectDialog(JFrame owner, JTabbedPane parent) {
    this(owner, parent, null);
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public Project getProject() {
    return project;
  }

  public void cleanAndHide() {
    project = null;
    setVisible(false);
  }

}

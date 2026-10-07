package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Project;
import dev.nerydlg.taskmanager.window.TabManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class ProjectManagementDialog extends JDialog {
  private final JFrame owner;
  private final TabManager tabs;
  private final ProjectManagementTable  pmt;

  public ProjectManagementDialog(JFrame owner, TabManager tabs) {
    super(owner, "Project Management", true);
    this.owner = owner;
    this.tabs = tabs;

    JPanel contentPanel = new JPanel(new GridBagLayout());
    contentPanel.setBorder(BorderFactory.createEmptyBorder(13, 13, 13, 13));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    try {
      List<Project> projects = this.tabs.getProjectRepository().findAll();
      pmt = new ProjectManagementTable(projects);
      JButton remove = new JButton("Remove Project");
      remove.addActionListener(this::removeProjectAction);
      JButton edit = new JButton("Edit Project");
      edit.addActionListener(this::openEditDialog);
      c.gridwidth = 3;
      c.weightx = 1;
      c.gridx = 0;
      c.gridy = 0;
      c.ipadx = 10;
      c.ipady = 10;
      contentPanel.add(pmt, c);
      c.gridwidth = 1;
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0.5;
      contentPanel.add(remove, c);
      c.gridwidth = 1;
      c.gridx = 2;
      c.gridy = 1;
      c.weightx = 0.5;
      contentPanel.add(edit, c);
      setLocationRelativeTo(owner);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    setContentPane(contentPanel);
    pack();
  }

  private void openEditDialog(ActionEvent actionEvent) {
    Project project = this.pmt.getSelectedProject();
    if (project == null) {
      JOptionPane.showMessageDialog(ProjectManagementDialog.this, "Please select a project", "Alert", JOptionPane.WARNING_MESSAGE);
      return;
    }

    ProjectDialog projectDialog = new ProjectDialog(owner, (JTabbedPane) this.tabs.getTabPanel(), project);
    projectDialog.setVisible(true);

    if (projectDialog.isConfirmed()) {
      try {
        Project newProject = projectDialog.getProject();
        this.tabs.getProjectRepository().update(newProject);
        pmt.addUpdatedProject(project, newProject);
      } catch (SQLException e) {
        JOptionPane.showMessageDialog(ProjectManagementDialog.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void removeProjectAction(ActionEvent evt) {
    Project project = pmt.getSelectedProject();
    if(project == null) {
      JOptionPane.showMessageDialog(ProjectManagementDialog.this, "Please select a project", "Alert", JOptionPane.WARNING_MESSAGE);
      return;
    }
    int confirm = JOptionPane.showConfirmDialog(
        ProjectManagementDialog.this,
        "Are you sure you want to remove '" + project.name() +"' project?");
    if(confirm == JOptionPane.YES_OPTION) {
      try {
        this.tabs.getProjectRepository().markAsDeleted(project);
        pmt.removeProject(project);
      } catch (SQLException e) {
        JOptionPane.showMessageDialog(ProjectManagementDialog.this, "failed to update project");
      }
    }
  }
}

package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Project;
import dev.nerydlg.taskmanager.repository.ProjectRepository;
import dev.nerydlg.taskmanager.repository.TaskRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import static dev.nerydlg.taskmanager.utils.ImageUtils.createImageIcon;

public class ButtonAddTab extends JButton implements ActionListener {

  public static final Logger log = LogManager.getLogger(ButtonAddTab.class);

  public final JTabbedPane parent;
  public final JFrame frame;
  public final ProjectRepository projectRepository;
  public final TaskRepository taskRepository;

  public ButtonAddTab(JFrame frame, JTabbedPane parent, ProjectRepository projectRepository,
                      TaskRepository taskRepository) {
    int size = 15;
    setPreferredSize(new Dimension(size, size));
    setIcon(createImageIcon("/ico/icons8-plus-50.png", 10, 10));
    setUI(new BasicButtonUI());
    setBorderPainted(false);
    setContentAreaFilled(false);
    setOpaque(false);
    setFocusPainted(true);
    addActionListener(this);
    this.parent = parent;
    this.frame = frame;
    this.projectRepository = projectRepository;
    this.taskRepository = taskRepository;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ProjectDialog project = new ProjectDialog(frame, parent);
    project.setVisible(true);
    if (!project.isConfirmed()) {
      return;
    }
    String projectName = project.getText();
    if (projectName == null || projectName.isBlank()) {
      return;
    }

    Project savedProject;
    try {
      savedProject = projectRepository.save(new Project(projectName));
    } catch (SQLException ex) {
      log.error("Failed to save project", ex);
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Insert the new project before the "+" tab (the last tab, which hosts this button).
    int index = parent.getTabCount() - 1;
    JComponent content = new ProjectTaskPanel(frame, savedProject, taskRepository);
    parent.insertTab(projectName, null, content, projectName, index);

    ButtonCloseTab closeTab = new ButtonCloseTab(frame, parent);
    parent.setTabComponentAt(index, new ButtonTabComponent(projectName, parent, closeTab));
    parent.setSelectedIndex(index);
  }
}

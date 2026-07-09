package dev.nerydlg.taskmanager.window;

import dev.nerydlg.taskmanager.components.ButtonAddTab;
import dev.nerydlg.taskmanager.components.ButtonCloseTab;
import dev.nerydlg.taskmanager.components.ButtonTabComponent;
import dev.nerydlg.taskmanager.components.Dashboard;
import dev.nerydlg.taskmanager.components.ProjectTaskPanel;
import dev.nerydlg.taskmanager.entity.Project;
import dev.nerydlg.taskmanager.repository.ProjectRepository;
import dev.nerydlg.taskmanager.repository.TaskRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.sql.SQLException;
import java.util.List;

public class TabManager {

  private static final Logger log = LogManager.getLogger(TabManager.class);

  private final JFrame frame;
  private final JTabbedPane tabPane;
  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;

  public TabManager(JFrame frame, JTabbedPane tabPane, ProjectRepository projectRepository,
                    TaskRepository taskRepository) {
    this.frame = frame;
    this.tabPane = tabPane;
    this.projectRepository = projectRepository;
    this.taskRepository = taskRepository;
  }

  public void init() {
    log.debug("Initializing tab manager");

    // Dashboard tab: real content in the body, custom header carrying a close button.
    Dashboard dashboardContent = new Dashboard(projectRepository, taskRepository);
    addTab("Dashboard", dashboardContent);
    int dashboardIndex = tabPane.indexOfComponent(dashboardContent);
    ButtonCloseTab buttonCloseTab = new ButtonCloseTab(frame, tabPane);
    tabPane.setTabComponentAt(dashboardIndex,
        new ButtonTabComponent("Dashboard", tabPane, buttonCloseTab));

    // Open projects tabs: real content in the body, custom header carrying a close button.
    openProjects();

    // "+" tab: header carries the add button that creates new project tabs.
    JComponent addContent = new JPanel();
    addTab("Add tab", addContent);
    int addIndex = tabPane.indexOfComponent(addContent);
    ButtonAddTab buttonAddTab = new ButtonAddTab(frame, tabPane, projectRepository, taskRepository);
    tabPane.setTabComponentAt(addIndex,
        new ButtonTabComponent("", tabPane, buttonAddTab));
  }

  private void openProjects() {
    try {
      List<Project> projectList = projectRepository.findAll();
      for (Project project : projectList) {
        JComponent projectContent = new ProjectTaskPanel(frame, project, taskRepository);
        addTab(project.name(), projectContent);
        int addIndex = tabPane.indexOfComponent(projectContent);
        ButtonCloseTab buttonCloseTab = new ButtonCloseTab(frame, tabPane);
        tabPane.setTabComponentAt(addIndex,
            new ButtonTabComponent(project.name(), tabPane, buttonCloseTab));
      }
    } catch (SQLException e) {
      log.error("Failed to open projects", e);
    }
  }

  public void addTab(String title, ImageIcon icon, JComponent panel) {
    tabPane.addTab(title, icon, panel, title);
  }

  public void addTab(String title, JComponent component) {
    tabPane.addTab(title, component);
  }

  public Component getTabPanel() {
    return tabPane;
  }
}

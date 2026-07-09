package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.TaskStatus;
import dev.nerydlg.taskmanager.repository.ProjectRepository;
import dev.nerydlg.taskmanager.repository.TaskRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Dashboard extends JComponent {

  private static final Logger log = LogManager.getLogger(Dashboard.class);
  private final TaskRepository taskRepository;
  private final ProjectRepository projectRepository;
  private final JPanel content;

  public Dashboard(ProjectRepository projectRepository, TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
    this.projectRepository = projectRepository;
    this.content = new JPanel();
    setLayout(new BorderLayout());
    content.setAutoscrolls(true);
    try {
      load();
    }catch (SQLException ex) {
      log.error("Failed to load dashboard", ex);
    }
    add(content, BorderLayout.CENTER);
  }

  private void refresh() throws SQLException{
    // I want to refresh the graph
    content.removeAll();
    load();
  }

  private void load() throws SQLException {
    // Tasks per project
    Map<String,Integer> tasksPerProject = taskRepository.getNumOfTaskPerProjectByStatus(TaskStatus.NEW);
    List<Color> colorsB = List.of(
        new Color(100, 140,100),
        new Color(90, 120,150),
        new Color(80, 120,150),
        new Color(70, 110,150),
        new Color(60, 100,160),
        new Color(50, 90, 170)
        );
    CircleGraph pendingTasksGraph = new CircleGraph("Pending Tasks", 300, 300, tasksPerProject, colorsB);

    // Task Done per project
    Map<String,Integer> tasksDonePerProject = taskRepository.getNumOfTaskPerProjectByStatus(TaskStatus.DONE);
    List<Color> colorsG = List.of(
        new Color(100, 140,220),
        new Color(90, 160,200),
        new Color(80, 180,180),
        new Color(70, 190,160),
        new Color(60, 210,140),
        new Color(50, 220, 130)
    );
    CircleGraph doneTaskGraph = new CircleGraph("Tasks Done", 300, 300, tasksDonePerProject, colorsG);

    //
    // add to component
    content.add(pendingTasksGraph);
    content.add(doneTaskGraph);
  }


}

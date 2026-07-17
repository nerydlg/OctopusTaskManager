package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Project;
import dev.nerydlg.taskmanager.entity.Task;
import dev.nerydlg.taskmanager.entity.TaskStatus;
import dev.nerydlg.taskmanager.repository.TaskRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The body of a project tab: a toolbar plus a scrollable {@link TaskTable}
 * showing the tasks that belong to a single {@link Project}.
 *
 * <p>Reusable both when a project tab is created on the fly (see
 * {@link ButtonAddTab}) and when existing projects are re-opened at startup.
 */
public class ProjectTaskPanel extends JPanel {

  private static final Logger log = LogManager.getLogger(ProjectTaskPanel.class);

  private final JFrame frame;
  private final Project project;
  private final TaskRepository taskRepository;
  private final TaskTable table;

  public ProjectTaskPanel(JFrame frame, Project project, TaskRepository taskRepository) {
    super(new BorderLayout());
    this.frame = frame;
    this.project = project;
    this.taskRepository = taskRepository;

    this.table = new TaskTable(loadTasks());
    table.expandAll();

    add(buildToolbar(), BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  private JToolBar buildToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    JButton newTaskButton = new JButton("New task");
    JButton editTaskButton = new JButton("Edit task");
    JButton deleteTaskButton = new JButton("Delete task");
    JButton doneTaskButton = new JButton("Mark as done");
    editTaskButton.setEnabled(false);
    deleteTaskButton.setEnabled(false);
    doneTaskButton.setEnabled(false);

    toolbar.add(newTaskButton);
    toolbar.addSeparator();
    toolbar.add(editTaskButton);
    toolbar.addSeparator();
    toolbar.add(deleteTaskButton);
    toolbar.addSeparator();
    toolbar.add(doneTaskButton);

    // enable Edit/Delete/Done buttons when a task is selected
    table.getSelectionModel().addListSelectionListener(e -> {
      boolean isSelected = table.getSelectedRow() >= 0 && !table.isEmptyState();
      editTaskButton.setEnabled(isSelected);
      deleteTaskButton.setEnabled(isSelected);
      doneTaskButton.setEnabled(isSelected);
    });

    newTaskButton.addActionListener(e -> onNewTask());
    editTaskButton.addActionListener(e -> onEditTask());
    deleteTaskButton.addActionListener(e -> onDeleteTask());
    doneTaskButton.addActionListener(e -> onMarkTaskAsDone());

    return toolbar;
  }

  private void onNewTask() {
    TaskDialog dialog = new TaskDialog(frame, taskRepository, project.id(), null);
    if (!dialog.isConfirmed()) {
      return;
    }
    try {
      Task saved = taskRepository.save(dialog.getTask());
      table.addTask(saved);
    } catch (SQLException ex) {
      log.error("Failed to save task", ex);
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void onDeleteTask() {
    int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?",
        "Confirmation", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.NO_OPTION) {
      return;
    }
    Task selected = table.getSelectedTask();
    if (selected == null) {
      return;
    }
    try {
      taskRepository.delete(selected);
      table.removeTask(selected.id());
    } catch (SQLException ex) {
      log.error("Failed to delete task", ex);
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void onMarkTaskAsDone() {
    int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to mark this task as done?", "Confirmation", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.NO_OPTION) {
      return;
    }

    Task selected = table.getSelectedTask();
    Task done = new Task(selected.id(),
        selected.title(),
        selected.type(),
        selected.desc(),
        selected.priority(),
        TaskStatus.DONE,
        selected.dueDate(),
        selected.createdAt(),
        LocalDateTime.now(),
        selected.projectId(),
        selected.parentId());
    try {
      taskRepository.update(done);
      table.updateTask(done);
    } catch (SQLException ex) {
      log.error("Failed to mark task as done", ex);
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void onEditTask() {
    Task selected = table.getSelectedTask();
    if (selected == null) {
      return;
    }

    TaskDialog dialog = new TaskDialog(frame, taskRepository, selected);
    if (!dialog.isConfirmed()) {
      return;
    }
    try {
      Task updated = taskRepository.update(dialog.getTask());
      table.updateTask(updated);
    } catch (SQLException ex) {
      log.error("Failed to update task", ex);
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private List<Task> loadTasks() {
    try {
      return taskRepository.findAllByProjectId(project.id());
    } catch (SQLException ex) {
      log.error("Failed to load tasks for project {}", project.id(), ex);
      return List.of();
    }
  }

  public void refresh() {
    table.setTasks(loadTasks());
  }
}

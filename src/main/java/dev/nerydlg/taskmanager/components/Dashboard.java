package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Task;
import dev.nerydlg.taskmanager.entity.TaskStatus;
import dev.nerydlg.taskmanager.entity.TaskType;
import dev.nerydlg.taskmanager.repository.ProjectRepository;
import dev.nerydlg.taskmanager.repository.TaskRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    this.content = new JPanel(new GridBagLayout());
    setLayout(new BorderLayout());
    content.setAutoscrolls(true);
    try {
      load();
    } catch (SQLException ex) {
      log.error("Failed to load dashboard", ex);
    }
    add(content, BorderLayout.CENTER);
  }

  private static Color typeColor(TaskType type) {
    if (type == null) {
      return Color.GRAY;
    }
    return switch (type) {
      case TASK -> new Color(220, 220, 120);
      case FIX -> new Color(250, 100, 100);
      case FEATURE -> new Color(50, 100, 250);
      case INVESTIGATE -> new Color(60, 200, 50);
    };
  }

  public void refresh() throws SQLException {
    content.removeAll();
    load();
    content.revalidate();
    content.repaint();
  }

  private void load() throws SQLException {
    // Tasks per project
    Map<String, Integer> tasksPerProject = taskRepository.getNumOfTaskPerProjectByStatus(TaskStatus.NEW);
    List<Color> projColors = List.of(
        new Color(200, 240, 200),
        new Color(190, 220, 250),
        new Color(180, 220, 150),
        new Color(170, 210, 250),
        new Color(150, 220, 160),
        new Color(150, 190, 170)
    );
    CircleGraph pendingTasksGraph = new CircleGraph("Pending Tasks", 250, 250, tasksPerProject, projColors);

    // Task Done per project
    Map<String, Integer> doneTaskColors = taskRepository.getNumOfTaskPerProjectByStatus(TaskStatus.DONE);
    List<Color> colorsG = List.of(
        new Color(100, 140, 220),
        new Color(90, 160, 200),
        new Color(80, 180, 180),
        new Color(70, 190, 160),
        new Color(60, 210, 140),
        new Color(50, 220, 130)
    );
    CircleGraph doneTaskGraph = new CircleGraph("Tasks Done", 250, 250, doneTaskColors, colorsG);

    // Priority graph
    Map<String, Integer> TasksByPriority = taskRepository.CountTasksByPriority();
    List<Color> priorityColors = List.of(
        new Color(230, 110, 100),
        new Color(200, 120, 140),
        new Color(190, 190, 120),
        new Color(150, 200, 160),
        new Color(130, 220, 190),
        new Color(110, 250, 200)
    );
    CircleGraph priorityGraph = new CircleGraph("Tasks by priority", 250, 250, TasksByPriority, priorityColors);

    // Add List of Next Tasks
    JPanel nextTasksPanel = new JPanel(new BorderLayout());
    List<Task> tasks = taskRepository.findNextSuggestedTasks(5);
    nextTasksPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Next Suggested Task"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    JList<Task> taskList = getTaskJList(tasks);

    nextTasksPanel.add(taskList);

    // add to component
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(5, 5, 5, 5);

    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridx = 0;
    content.add(pendingTasksGraph, gbc);
    gbc.gridx = 1;
    content.add(doneTaskGraph, gbc);
    gbc.gridx = 2;
    content.add(priorityGraph, gbc);

    gbc.gridy = 1;
    gbc.gridx = 1;
    gbc.gridwidth = 1;
    content.add(nextTasksPanel, gbc);
  }

  private JList<Task> getTaskJList(List<Task> tasks) {
    JList<Task> taskList = new JList<>();
    taskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    taskList.setModel(new NextTaskListModel(tasks));
    taskList.setCellRenderer(new TaskListCellRenderer());
    taskList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
          return;
        }
        int index = taskList.locationToIndex(e.getPoint());
        if (index < 0 || !taskList.getCellBounds(index, index).contains(e.getPoint())) {
          return;
        }
        onEditNextTask(taskList.getModel().getElementAt(index));
      }
    });
    return taskList;
  }

  private void onEditNextTask(Task task) {
    JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
    TaskDialog dialog = new TaskDialog(owner, taskRepository, task);
    if (!dialog.isConfirmed()) {
      return;
    }
    try {
      taskRepository.update(dialog.getTask());
      refresh();
    } catch (SQLException ex) {
      log.error("Failed to update task", ex);
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * A small filled rectangle used as the per-type color chip.
   */
  private static final class ChipIcon implements Icon {
    private static final int WIDTH = 10;
    private static final int HEIGHT = 32;
    private final Color color;

    private ChipIcon(Color color) {
      this.color = color;
    }

    @Override
    public int getIconWidth() {
      return WIDTH;
    }

    @Override
    public int getIconHeight() {
      return HEIGHT;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color == null ? Color.GRAY : color);
      g2.fillRect(x, y, WIDTH, HEIGHT);
      g2.dispose();
    }
  }

  private static final class TaskListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof Task task) {
        JLabel label = (JLabel) this;
        label.setText(task.title());
        label.setIcon(new ChipIcon(typeColor(task.type())));
        label.setIconTextGap(5);
      }
      return this;
    }
  }
}

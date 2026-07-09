package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Task;
import dev.nerydlg.taskmanager.entity.TaskStatus;
import dev.nerydlg.taskmanager.entity.TaskType;
import dev.nerydlg.taskmanager.repository.TaskRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modal dialog for creating or editing a {@link Task}. Use {@link #isConfirmed()}
 * after {@code setVisible(true)} and then {@link #getTask()} to read the result.
 *
 * <pre>{@code
 * TaskDialog dialog = new TaskDialog(frame, taskRepository, projectId, parentId); // create
 * dialog.setVisible(true);
 * if (dialog.isConfirmed()) {
 *     Task task = dialog.getTask();
 * }
 * }</pre>
 */
public class TaskDialog extends JDialog {

  private static final Logger log = LogManager.getLogger(TaskDialog.class);
  private static final int PARENT_SEARCH_LIMIT = 8;

  private final JTextField titleField = new JTextField(18);
  private final JComboBox<TaskType> typeBox = new JComboBox<>(TaskType.values());
  private final JTextArea descArea = new JTextArea(4, 18);
  private final JSpinner prioritySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
  private final JComboBox<TaskStatus> statusBox = new JComboBox<>(TaskStatus.values());
  private final DatePicker dueDatePicker = new DatePicker();
  private final ParentTaskField parentField;

  private final TaskRepository taskRepository;
  private final Task original;
  private final Integer projectId;
  private boolean confirmed = false;

  /**
   * Create mode: a brand-new task for the given project (and optional parent).
   */
  public TaskDialog(JFrame owner, TaskRepository taskRepository, Integer projectId, Integer parentId) {
    this(owner, taskRepository, null, projectId, parentId);
  }

  /**
   * Edit mode: pre-fills the form from an existing task.
   */
  public TaskDialog(JFrame owner, TaskRepository taskRepository, Task task) {
    this(owner, taskRepository, task, task == null ? null : task.projectId(), task == null ? null : task.parentId());
  }

  private TaskDialog(JFrame owner, TaskRepository taskRepository, Task task, Integer projectId, Integer parentId) {
    super(owner, task == null ? "New Task" : "Edit Task", true);
    this.original = task;
    this.projectId = projectId;
    this.taskRepository = taskRepository;
    Integer excludeId = task == null ? null : task.id();
    this.parentField = new ParentTaskField(titleQuery ->
        taskRepository.searchTasksForParentPicker(titleQuery, projectId, excludeId, PARENT_SEARCH_LIMIT));

    getContentPane().add(buildForm(), BorderLayout.CENTER);
    getContentPane().add(buildButtons(), BorderLayout.SOUTH);

    if (task != null) {
      prefill(task);
    }
    if (parentId != null) {
      prefillParent(parentId);
    }

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(java.awt.event.ComponentEvent e) {
        titleField.requestFocusInWindow();
      }
    });

    pack();
    setLocationRelativeTo(owner);
    setVisible(true);
  }

  // --- result ----------------------------------------------------------

  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Builds a {@link Task} from the current field values.
   */
  public Task getTask() {
    LocalDate due = dueDatePicker.getDate();
    return new Task(
        original == null ? null : original.id(),
        titleField.getText().trim(),
        (TaskType) typeBox.getSelectedItem(),
        descArea.getText().trim(),
        (Integer) prioritySpinner.getValue(),
        (TaskStatus) statusBox.getSelectedItem(),
        due == null ? null : due.atStartOfDay(),
        original == null ? LocalDateTime.now() : original.createdAt(),
        LocalDateTime.now(),
        projectId,
        parentField.getSelectedTaskId());
  }

  // --- ui --------------------------------------------------------------

  private JPanel buildForm() {
    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    GridBagConstraints labels = new GridBagConstraints();
    labels.gridx = 0;
    labels.anchor = GridBagConstraints.NORTHWEST;
    labels.insets = new Insets(4, 4, 4, 8);

    GridBagConstraints fields = new GridBagConstraints();
    fields.gridx = 1;
    fields.fill = GridBagConstraints.HORIZONTAL;
    fields.weightx = 1.0;
    fields.insets = new Insets(4, 0, 4, 4);

    descArea.setLineWrap(true);
    descArea.setWrapStyleWord(true);

    addRow(form, labels, fields, 0, "Title:", titleField);
    addRow(form, labels, fields, 1, "Type:", typeBox);
    addRow(form, labels, fields, 2, "Description:", new JScrollPane(descArea));
    addRow(form, labels, fields, 3, "Priority:", prioritySpinner);
    addRow(form, labels, fields, 4, "Status:", statusBox);
    addRow(form, labels, fields, 5, "Due date:", dueDatePicker);
    addRow(form, labels, fields, 6, "Parent task:", parentField);

    return form;
  }

  private void addRow(JPanel form, GridBagConstraints labels, GridBagConstraints fields,
                      int row, String label, Component field) {
    labels.gridy = row;
    fields.gridy = row;
    form.add(new JLabel(label), labels);
    form.add(field, fields);
  }

  private JPanel buildButtons() {
    JButton ok = new JButton("OK");
    JButton cancel = new JButton("Cancel");

    ok.addActionListener(e -> onOk());
    cancel.addActionListener(e -> setVisible(false));

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(ok);
    buttons.add(cancel);

    getRootPane().setDefaultButton(ok);
    return buttons;
  }

  private void onOk() {
    if (titleField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Title is required.",
          "Validation", JOptionPane.WARNING_MESSAGE);
      titleField.requestFocusInWindow();
      return;
    }
    confirmed = true;
    setVisible(false);
  }

  private void prefill(Task task) {
    titleField.setText(task.title() == null ? "" : task.title());
    typeBox.setSelectedItem(task.type());
    descArea.setText(task.desc() == null ? "" : task.desc());
    prioritySpinner.setValue(task.priority() == null ? 0 : task.priority());
    statusBox.setSelectedItem(task.status());
    dueDatePicker.setDate(task.dueDate() == null ? null : task.dueDate().toLocalDate());
  }

  private void prefillParent(Integer parentId) {
    try {
      Task parent = taskRepository.findById(parentId);
      if (parent != null) {
        parentField.setSelectedTask(parent);
      }
    } catch (SQLException ex) {
      log.error("Failed to load parent task {}", parentId, ex);
    }
  }
}

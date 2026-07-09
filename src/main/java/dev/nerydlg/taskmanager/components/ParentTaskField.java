package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * A free-text field that looks up candidate tasks as the user types (debounced) and
 * lets them pick one from a dropdown, resolving the pick to a task id. Used by
 * {@link TaskDialog} to let the user search-and-link a parent task by title instead
 * of entering an id directly.
 *
 * <pre>{@code
 * ParentTaskField field = new ParentTaskField(titleQuery ->
 *     taskRepository.searchTasksForParentPicker(titleQuery, projectId, excludeId, 8));
 * Integer parentId = field.getSelectedTaskId(); // null until a suggestion is picked
 * }</pre>
 */
public class ParentTaskField extends JPanel {

  @FunctionalInterface
  public interface TaskSearch {
    List<Task> search(String titleQuery) throws SQLException;
  }

  private static final Logger log = LogManager.getLogger(ParentTaskField.class);
  private static final int DEBOUNCE_MS = 300;
  private static final int VISIBLE_ROWS = 6;

  private final JTextField field = new JTextField(18);
  private final JPopupMenu popup = new JPopupMenu();
  private final DefaultListModel<Task> resultsModel = new DefaultListModel<>();
  private final JList<Task> results = new JList<>(resultsModel);
  private final Timer debounce;
  private final TaskSearch search;

  private Integer selectedTaskId;
  private boolean suppressSearch;

  public ParentTaskField(TaskSearch search) {
    super(new BorderLayout());
    this.search = search;
    add(field, BorderLayout.CENTER);

    results.setVisibleRowCount(VISIBLE_ROWS);
    results.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                     boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Task task) {
          setText(task.title());
        }
        return this;
      }
    });
    popup.setBorder(BorderFactory.createLineBorder(field.getBackground().darker()));
    popup.add(new JScrollPane(results));
    popup.setFocusable(false);

    debounce = new Timer(DEBOUNCE_MS, e -> runSearch());
    debounce.setRepeats(false);

    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        onTyped();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        onTyped();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        onTyped();
      }
    });

    field.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (!popup.isVisible()) {
          return;
        }
        switch (e.getKeyCode()) {
          case KeyEvent.VK_DOWN -> moveSelection(1);
          case KeyEvent.VK_UP -> moveSelection(-1);
          case KeyEvent.VK_ENTER -> confirmHighlighted();
          case KeyEvent.VK_ESCAPE -> popup.setVisible(false);
          default -> {
          }
        }
      }
    });

    results.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int index = results.locationToIndex(e.getPoint());
        if (index >= 0) {
          select(resultsModel.get(index));
        }
      }
    });
  }

  // --- public API --------------------------------------------------------

  /**
   * The id of the task picked from the dropdown, or {@code null} if nothing (or no
   * longer valid selection, since editing the text after a pick clears it) is chosen.
   */
  public Integer getSelectedTaskId() {
    return selectedTaskId;
  }

  /**
   * Prefills the field with an already-known parent task, e.g. when editing a task
   * that already has a parent.
   */
  public void setSelectedTask(Task task) {
    suppressSearch = true;
    field.setText(task == null ? "" : task.title());
    suppressSearch = false;
    selectedTaskId = task == null ? null : task.id();
  }

  // --- search --------------------------------------------------------------

  private void onTyped() {
    if (suppressSearch) {
      return;
    }
    // Typing invalidates whatever was previously picked until a new suggestion is confirmed.
    selectedTaskId = null;
    debounce.restart();
  }

  private void runSearch() {
    String text = field.getText().trim();
    if (text.isEmpty()) {
      popup.setVisible(false);
      return;
    }
    try {
      List<Task> matches = search.search(text);
      if (matches.isEmpty()) {
        popup.setVisible(false);
        return;
      }
      resultsModel.clear();
      matches.forEach(resultsModel::addElement);
      results.setSelectedIndex(0);
      popup.pack();
      popup.show(this, 0, getHeight());
    } catch (SQLException ex) {
      log.error("Failed to search tasks for parent field", ex);
      popup.setVisible(false);
    }
  }

  private void moveSelection(int delta) {
    int size = resultsModel.size();
    if (size == 0) {
      return;
    }
    int next = Math.floorMod(results.getSelectedIndex() + delta, size);
    results.setSelectedIndex(next);
    results.ensureIndexIsVisible(next);
  }

  private void confirmHighlighted() {
    int index = results.getSelectedIndex();
    if (index >= 0) {
      select(resultsModel.get(index));
    }
  }

  private void select(Task task) {
    setSelectedTask(task);
    popup.setVisible(false);
    field.requestFocusInWindow();
  }
}

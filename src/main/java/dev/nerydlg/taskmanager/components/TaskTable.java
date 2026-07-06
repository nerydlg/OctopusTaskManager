package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Task;
import dev.nerydlg.taskmanager.entity.TaskStatus;
import dev.nerydlg.taskmanager.entity.TaskType;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An expandable, decorated, sortable task list backed by a {@link TaskTableModel}.
 *
 * <ul>
 *   <li>Rows nest to arbitrary depth via {@link Task#parentId()}; click the ▸/▾
 *       handle in the Title column to expand/collapse.</li>
 *   <li>Cells are decorated: a color chip per {@link TaskType}, a priority-tinted
 *       cell, a colored status badge, and strike-through for done/closed tasks.</li>
 *   <li>Clicking the Priority or Status header sorts by that column (toggling
 *       ascending/descending) while keeping the parent/child grouping intact.</li>
 * </ul>
 */
public class TaskTable extends JTable {

  private static final int INDENT_PER_LEVEL = 16;
  private static final int LEFT_PAD = 4;
  private static final int TOGGLE_WIDTH = 14;

  private final TaskTableModel model;

  public TaskTable() {
    this(List.of());
  }

  public TaskTable(List<Task> tasks) {
    this.model = new TaskTableModel(tasks);
    setModel(model);

    setRowHeight(24);
    setFillsViewportHeight(true);
    setShowVerticalLines(false);
    setAutoCreateRowSorter(false); // sorting is handled by the model
    getTableHeader().setReorderingAllowed(false);

    installRenderers();
    installExpandHandle();
    installHeaderSorting();
  }

  private static boolean isFinished(Task task) {
    return task != null && (task.status() == TaskStatus.DONE || task.status() == TaskStatus.CLOSE);
  }

  /**
   * Background tint by priority. Raw int convention: higher value = more urgent.
   * Tweak the thresholds/colors here if your scale differs.
   */
  private static Color priorityColor(Integer priority) {
    if (priority == null) {
      return null;
    }
    if (priority >= 3) return new Color(0xE8F5E9); // lowest  - green
    if (priority == 2) return new Color(0xFFF8E1); // low     - amber
    if (priority == 1) return new Color(0xFFE0B2); // medium  - orange
    return new Color(0xFFCDD2);                    // high    - red
  }

  private static Color statusColor(TaskStatus status) {
    if (status == null) {
      return Color.GRAY;
    }
    return switch (status) {
      case NEW -> new Color(0x1976D2);
      case IN_PROGRESS -> new Color(0xF9A825);
      case CLOSE -> new Color(0x757575);
      case DONE -> new Color(0x388E3C);
    };
  }

  private static Color typeColor(TaskType type) {
    if (type == null) {
      return Color.GRAY;
    }
    return switch (type) {
      case TASK -> new Color(0x607D8B);
      case FIX -> new Color(0xE53935);
      case FEATURE -> new Color(0x1E88E5);
      case INVESTIGATE -> new Color(0x8E24AA);
    };
  }

  /**
   * Replaces the displayed tasks (re-applying the active sort).
   */
  public void setTasks(List<Task> tasks) {
    model.setTasks(tasks);
  }

  // --- wiring ----------------------------------------------------------

  /**
   * Replaces the task with the same id in place.
   */
  public void updateTask(Task task) {
    model.updateTask(task);
  }

  /**
   * Removes the task (and its descendants) with the given id in place.
   */
  public void removeTask(Integer id) {
    model.removeTask(id);
  }

  public void expandAll() {
    model.expandAll();
  }

  public void collapseAll() {
    model.collapseAll();
  }

  // --- decoration helpers ---------------------------------------------

  private void installRenderers() {
    column(TaskTableModel.COL_TITLE).setCellRenderer(new TitleRenderer());
    column(TaskTableModel.COL_TYPE).setCellRenderer(new TypeRenderer());
    column(TaskTableModel.COL_DESC).setCellRenderer(new DescriptionRenderer());
    column(TaskTableModel.COL_PRIORITY).setCellRenderer(new PriorityRenderer());
    column(TaskTableModel.COL_STATUS).setCellRenderer(new StatusRenderer());

    column(TaskTableModel.COL_TITLE).setPreferredWidth(220);
    column(TaskTableModel.COL_TYPE).setPreferredWidth(110);
    column(TaskTableModel.COL_DESC).setPreferredWidth(280);
    column(TaskTableModel.COL_PRIORITY).setPreferredWidth(70);
    column(TaskTableModel.COL_STATUS).setPreferredWidth(120);

    TableCellRenderer base = getTableHeader().getDefaultRenderer();
    getTableHeader().setDefaultRenderer(new SortIndicatorHeaderRenderer(base));
  }

  private TableColumn column(int modelIndex) {
    return getColumnModel().getColumn(convertColumnIndexToView(modelIndex));
  }

  /**
   * Toggles expansion when the ▸/▾ handle in the Title column is clicked.
   */
  private void installExpandHandle() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (model.isEmpty()) {
          return;
        }
        int viewRow = rowAtPoint(e.getPoint());
        int viewCol = columnAtPoint(e.getPoint());
        if (viewRow < 0 || viewCol < 0) {
          return;
        }
        if (convertColumnIndexToModel(viewCol) != TaskTableModel.COL_TITLE) {
          return;
        }
        if (!model.hasChildrenAt(viewRow)) {
          return;
        }
        Rectangle cell = getCellRect(viewRow, viewCol, false);
        int handleStart = cell.x + LEFT_PAD + model.depthAt(viewRow) * INDENT_PER_LEVEL;
        int handleEnd = handleStart + TOGGLE_WIDTH;
        if (e.getX() >= handleStart && e.getX() <= handleEnd) {
          model.toggleExpanded(viewRow);
        }
      }
    });
  }

  /**
   * Click the Priority/Status header to sort, toggling asc/desc.
   */
  private void installHeaderSorting() {
    getTableHeader().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int viewCol = getTableHeader().columnAtPoint(e.getPoint());
        if (viewCol < 0) {
          return;
        }
        int modelCol = convertColumnIndexToModel(viewCol);
        if (modelCol != TaskTableModel.COL_PRIORITY && modelCol != TaskTableModel.COL_STATUS) {
          return;
        }
        boolean ascending = modelCol != model.getSortColumn() || !model.isAscending();
        model.sortBy(modelCol, ascending);
        getTableHeader().repaint();
      }
    });
  }

  private void applyStrikeThrough(JComponent component, Task task) {
    Font base = getFont();
    Map<TextAttribute, Object> attributes = new HashMap<>(base.getAttributes());
    attributes.put(TextAttribute.STRIKETHROUGH,
        isFinished(task) ? TextAttribute.STRIKETHROUGH_ON : false);
    component.setFont(base.deriveFont(attributes));
  }

  public boolean isEmptyState() {
    return model.isEmpty();
  }

  /**
   * Returns the currently selected task, or {@code null} if nothing is selected.
   */
  public Task getSelectedTask() {
    int row = getSelectedRow();
    if (row < 0 || model.isEmpty()) {
      return null;
    }
    return model.taskAt(row);
  }

  public void addTask(Task task) {
    model.addTask(task);
  }

  // --- renderers -------------------------------------------------------

  private enum Handle {NONE, COLLAPSED, EXPANDED}

  /**
   * Paints the ▸/▾ expand handle (or nothing, while reserving its width).
   */
  private static final class HandleIcon implements Icon {
    private final Handle handle;

    private HandleIcon(Handle handle) {
      this.handle = handle;
    }

    @Override
    public int getIconWidth() {
      return TOGGLE_WIDTH;
    }

    @Override
    public int getIconHeight() {
      return TOGGLE_WIDTH;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (handle == Handle.NONE) {
        return;
      }
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(c.getForeground());
      int s = 8;
      int ox = x + (getIconWidth() - s) / 2;
      int oy = y + (getIconHeight() - s) / 2;
      Polygon triangle = new Polygon();
      if (handle == Handle.COLLAPSED) { // pointing right
        triangle.addPoint(ox, oy);
        triangle.addPoint(ox + s, oy + s / 2);
        triangle.addPoint(ox, oy + s);
      } else { // pointing down
        triangle.addPoint(ox, oy);
        triangle.addPoint(ox + s, oy);
        triangle.addPoint(ox + s / 2, oy + s);
      }
      g2.fillPolygon(triangle);
      g2.dispose();
    }
  }

  /**
   * A small filled circle used as the per-type color chip.
   */
  private static final class ChipIcon implements Icon {
    private static final int SIZE = 10;
    private final Color color;

    private ChipIcon(Color color) {
      this.color = color;
    }

    @Override
    public int getIconWidth() {
      return SIZE;
    }

    @Override
    public int getIconHeight() {
      return SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color == null ? Color.GRAY : color);
      g2.fillOval(x, y, SIZE, SIZE);
      g2.dispose();
    }
  }

  private final class TitleRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (model.isEmpty()) {
        setIcon(null);
        setBorder(BorderFactory.createEmptyBorder(0, LEFT_PAD, 0, 0));
        setText("No tasks found yet");
        setFont(getFont().deriveFont(Font.ITALIC));
        if (!isSelected) {
          setForeground(Color.GRAY);
        }
        return this;
      }
      Task task = (Task) value;
      Handle handle = model.hasChildrenAt(row)
          ? (model.isExpandedAt(row) ? Handle.EXPANDED : Handle.COLLAPSED)
          : Handle.NONE;
      setIcon(new HandleIcon(handle));
      setIconTextGap(4);
      int indent = LEFT_PAD + model.depthAt(row) * INDENT_PER_LEVEL;
      setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
      setText(task == null ? "" : task.title());
      applyStrikeThrough(this, task);
      return this;
    }
  }

  private final class TypeRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (model.isEmpty()) {
        setIcon(null);
        setText("");
        return this;
      }
      Task task = (Task) value;
      TaskType type = task == null ? null : task.type();
      setIcon(new ChipIcon(typeColor(type)));
      setIconTextGap(6);
      setText(type == null ? "" : type.name());
      applyStrikeThrough(this, task);
      return this;
    }
  }

  private final class DescriptionRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (model.isEmpty()) {
        setText("");
        setToolTipText(null);
        return this;
      }
      Task task = (Task) value;
      String desc = task == null ? "" : task.desc();
      setText(desc);
      setToolTipText(desc == null || desc.isBlank() ? null : desc);
      applyStrikeThrough(this, task);
      return this;
    }
  }

  // --- programmatic icons ---------------------------------------------

  private final class PriorityRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      setHorizontalAlignment(CENTER);
      if (model.isEmpty()) {
        setText("");
        return this;
      }
      Task task = (Task) value;
      Integer priority = task == null ? null : task.priority();
      setText(priority == null ? "" : String.valueOf(priority));
      if (!isSelected) {
        Color tint = priorityColor(priority);
        setBackground(tint != null ? tint : table.getBackground());
      }
      applyStrikeThrough(this, task);
      return this;
    }
  }

  private final class StatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      setHorizontalAlignment(CENTER);
      if (model.isEmpty()) {
        setText("");
        return this;
      }
      Task task = (Task) value;
      TaskStatus status = task == null ? null : task.status();
      setText(status == null ? "" : status.name());
      setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
      if (!isSelected) {
        setBackground(statusColor(status));
        setForeground(Color.WHITE);
      }
      applyStrikeThrough(this, task);
      return this;
    }
  }

  /**
   * Wraps the default header renderer to append a ▲/▼ to the sorted column.
   */
  private final class SortIndicatorHeaderRenderer implements TableCellRenderer {
    private final TableCellRenderer delegate;

    private SortIndicatorHeaderRenderer(TableCellRenderer delegate) {
      this.delegate = delegate;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      Component component = delegate.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
      if (component instanceof JLabel label) {
        String text = value == null ? "" : value.toString();
        if (convertColumnIndexToModel(column) == model.getSortColumn()) {
          text += model.isAscending() ? " ▲" : " ▼";
        }
        label.setText(text);
      }
      return component;
    }
  }
}

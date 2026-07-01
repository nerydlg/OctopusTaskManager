package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Task;
import dev.nerydlg.taskmanager.entity.TaskStatus;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Hierarchical table model for {@link Task}s. A flat {@code List<Task>} is turned
 * into a tree using {@link Task#parentId()} (null = root, otherwise child of that
 * id). Only the currently expanded nodes are exposed as rows, so the table behaves
 * like an arbitrarily deep, expandable tree-table.
 *
 * <p>Sorting is applied at the model level (parents among themselves, and each
 * parent's children within its own group) so the hierarchy survives a sort, which
 * a plain {@code TableRowSorter} could not guarantee.
 */
public class TaskTableModel extends AbstractTableModel {

  public static final int COL_TITLE = 0;
  public static final int COL_TYPE = 1;
  public static final int COL_DESC = 2;
  public static final int COL_PRIORITY = 3;
  public static final int COL_STATUS = 4;

  private static final String[] COLUMNS = {"Title", "Type", "Description", "Priority", "Status"};
  private final List<Node> roots = new ArrayList<>();
  /**
   * Authoritative flat task list; all mutations go through here.
   */
  private final List<Task> source = new ArrayList<>();
  /**
   * Ids of expanded nodes, kept across rebuilds so refreshes don't collapse the tree.
   */
  private final Set<Integer> expandedIds = new HashSet<>();
  private List<Row> visibleRows = new ArrayList<>();
  private int sortColumn = -1;
  private boolean ascending = true;

  public TaskTableModel() {
    this(List.of());
  }
  public TaskTableModel(List<Task> tasks) {
    setTasks(tasks);
  }

  /**
   * Replaces all tasks, preserving the active sort and expansion state.
   */
  public void setTasks(List<Task> tasks) {
    source.clear();
    source.addAll(tasks);
    refresh();
  }

  /**
   * Adds a task and refreshes; sort and expansion state are kept.
   */
  public void addTask(Task task) {
    source.add(task);
    refresh();
  }

  /**
   * Replaces the task that shares the same id; no-op if none matches.
   */
  public void updateTask(Task task) {
    for (int i = 0; i < source.size(); i++) {
      if (Objects.equals(source.get(i).id(), task.id())) {
        source.set(i, task);
        break;
      }
    }
    refresh();
  }

  // --- mutation (call these instead of recreating the table) -----------

  /**
   * Removes the task with the given id together with all of its descendants.
   */
  public void removeTask(Integer id) {
    Set<Integer> toRemove = new HashSet<>();
    collectSubtree(id, toRemove);
    source.removeIf(t -> toRemove.contains(t.id()));
    expandedIds.removeAll(toRemove);
    refresh();
  }

  private void collectSubtree(Integer id, Set<Integer> out) {
    if (id == null || !out.add(id)) {
      return;
    }
    for (Task task : source) {
      if (id.equals(task.parentId())) {
        collectSubtree(task.id(), out);
      }
    }
  }

  /**
   * Rebuilds the tree from {@link #source}, keeping sort and expansion.
   */
  private void refresh() {
    buildTree(source);
    applySort();
    rebuildVisibleRows();
    fireTableDataChanged();
  }

  private void buildTree(List<Task> tasks) {
    roots.clear();
    Map<Integer, Node> byId = new LinkedHashMap<>();
    for (Task task : tasks) {
      if (task.id() != null) {
        byId.put(task.id(), new Node(task));
      }
    }
    for (Task task : tasks) {
      Node node = task.id() != null ? byId.get(task.id()) : new Node(task);
      Node parent = task.parentId() != null ? byId.get(task.parentId()) : null;
      if (parent != null) {
        parent.children.add(node);
      } else {
        // null parent, or a parentId we never saw (orphan) -> treat as root.
        roots.add(node);
      }
    }
  }

  public void toggleExpanded(int viewRow) {
    Node node = visibleRows.get(viewRow).node();
    Integer id = node.task.id();
    if (node.children.isEmpty() || id == null) {
      return;
    }
    if (!expandedIds.add(id)) {
      expandedIds.remove(id);
    }
    rebuildVisibleRows();
    fireTableDataChanged();
  }

  public void expandAll() {
    markExpandable(roots);
    rebuildVisibleRows();
    fireTableDataChanged();
  }

  // --- expansion -------------------------------------------------------

  public void collapseAll() {
    expandedIds.clear();
    rebuildVisibleRows();
    fireTableDataChanged();
  }

  private void markExpandable(List<Node> nodes) {
    for (Node node : nodes) {
      if (!node.children.isEmpty() && node.task.id() != null) {
        expandedIds.add(node.task.id());
      }
      markExpandable(node.children);
    }
  }

  private boolean isExpanded(Node node) {
    return node.task.id() != null && expandedIds.contains(node.task.id());
  }

  private void rebuildVisibleRows() {
    List<Row> rows = new ArrayList<>();
    appendRows(roots, 0, rows);
    visibleRows = rows;
  }

  private void appendRows(List<Node> nodes, int depth, List<Row> out) {
    for (Node node : nodes) {
      out.add(new Row(node, depth));
      if (isExpanded(node) && !node.children.isEmpty()) {
        appendRows(node.children, depth + 1, out);
      }
    }
  }

  /**
   * Sorts by the given column (only PRIORITY and STATUS are sortable).
   */
  public void sortBy(int column, boolean ascending) {
    if (column != COL_PRIORITY && column != COL_STATUS) {
      return;
    }
    this.sortColumn = column;
    this.ascending = ascending;
    applySort();
    rebuildVisibleRows();
    fireTableDataChanged();
  }

  private void applySort() {
    Comparator<Node> comparator = comparatorFor(sortColumn);
    if (comparator == null) {
      return;
    }
    sortRecursively(roots, ascending ? comparator : comparator.reversed());
  }

  // --- sorting ---------------------------------------------------------

  private void sortRecursively(List<Node> nodes, Comparator<Node> comparator) {
    nodes.sort(comparator);
    for (Node node : nodes) {
      sortRecursively(node.children, comparator);
    }
  }

  private Comparator<Node> comparatorFor(int column) {
    return switch (column) {
      case COL_PRIORITY -> Comparator.comparing(
          n -> n.task.priority(),
          Comparator.nullsLast(Comparator.naturalOrder()));
      case COL_STATUS -> Comparator.comparing(
          n -> n.task.status(),
          Comparator.nullsLast(Comparator.comparingInt(TaskStatus::getValue)));
      default -> null;
    };
  }

  public int getSortColumn() {
    return sortColumn;
  }

  public boolean isAscending() {
    return ascending;
  }

  public Task taskAt(int viewRow) {
    return visibleRows.get(viewRow).node().task;
  }

  public int depthAt(int viewRow) {
    return visibleRows.get(viewRow).depth();
  }

  // --- row accessors for the renderers / mouse handling ----------------

  public boolean hasChildrenAt(int viewRow) {
    return !visibleRows.get(viewRow).node().children.isEmpty();
  }

  public boolean isExpandedAt(int viewRow) {
    return isExpanded(visibleRows.get(viewRow).node());
  }

  /**
   * True when there are no tasks to show (the table renders a placeholder row).
   */
  public boolean isEmpty() {
    return visibleRows.isEmpty();
  }

  @Override
  public int getRowCount() {
    // Reserve a single row for the "no tasks" placeholder when empty.
    return visibleRows.isEmpty() ? 1 : visibleRows.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  // --- AbstractTableModel ---------------------------------------------

  @Override
  public String getColumnName(int column) {
    return COLUMNS[column];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    // Every column renderer receives the whole Task and pulls the field it needs.
    // When empty, the placeholder row has no backing task.
    return isEmpty() ? null : taskAt(rowIndex);
  }

  /**
   * A node in the task tree.
   */
  private static final class Node {
    final Task task;
    final List<Node> children = new ArrayList<>();

    Node(Task task) {
      this.task = task;
    }
  }

  /**
   * A node together with the depth at which it is currently shown.
   */
  private record Row(Node node, int depth) {
  }
}

package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Project;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;



public class ProjectManagementTable extends JTable {

  private static final int LEFT_PAD = 4;
  private final ProjectModel model;


  public ProjectManagementTable(List<Project> projects) {
    model = new ProjectModel(projects);
    setModel(model);

    setRowHeight(32);
    setFillsViewportHeight(true);
    setShowVerticalLines(true);
    setAutoCreateRowSorter(false);
    getTableHeader().setReorderingAllowed(false);

    installRenderers();
  }

  private TableColumn column(final int modelIndex) {
    return getColumnModel().getColumn(convertColumnIndexToView(modelIndex));
  }

  private void installRenderers() {
    column(ProjectModel.COL_PROJECT_NAME).setCellRenderer(new TitleRenderer());
    column(ProjectModel.COL_PROJECT_STATUS).setCellRenderer(new StatusRenderer());

    column(ProjectModel.COL_PROJECT_STATUS).setPreferredWidth(200);
    column(ProjectModel.COL_PROJECT_NAME).setPreferredWidth(100);

    TableCellRenderer base = getTableHeader().getDefaultRenderer();
  }

  private Color statusColor(Integer status) {
    return switch (status) {
      case 0 -> // OPEN
          new Color(0, 200, 0);
      case 1 -> // CLOSE
          new Color(200, 200, 200);
      case 2 -> // DONE
          new Color(100, 100, 250);
      case 3 -> // DELETED
          new Color(200, 10, 10);
      default -> throw new IllegalArgumentException("Invalid status value: " + status);
    };
  }

  public Project getSelectedProject() {
    int row = getSelectedRow();
    Project proj = null;
    if (row >= 0) {
      proj = model.getProjects().get(row);
    }
    return proj;
  }

  public void removeProject(Project project) {
    model.getProjects().remove(project);
    Project modifiedProject = new Project(project.id(), project.name(), 3, project.createdAt(), project.updatedAt());
    model.getProjects().add(modifiedProject);
    model.refresh();
  }

  public void addUpdatedProject(Project old, Project newProject) {
    model.getProjects().remove(old);
    model.getProjects().add(newProject);
    model.refresh();
  }

  private final class TitleRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      setIcon(null);
      setBorder(BorderFactory.createEmptyBorder(0, LEFT_PAD, 0, 0));
      if(model.isEmpty()) {
        setText("No Projects found");
        setFont(getFont().deriveFont(Font.ITALIC));
        if(!isSelected) {
          setForeground(Color.GRAY);
        }
        return this;
      }
      String projectName = (String) value;
      setText(projectName);
      return this;
    }
  }

  private final class StatusRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      setIcon(null);
      setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
      if(model.isEmpty()) {
        setText("");
        return this;
      }
      Integer status = (Integer) value;
      setText(getStatusText(status));
      if(!isSelected) {
        setBackground(statusColor(status));
        setForeground(Color.WHITE);
      }
      return this;
    }

    private String getStatusText(Integer status) {
      return switch (status) {
        case 0 -> "Open";
        case 1 -> "Closed";
        case 2 -> "Finished";
        case 3 -> "Deleted";
        default -> "Unknown";
      };
    }
  }

}

package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Project;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectModel extends AbstractTableModel {
  public static final int COL_PROJECT_NAME = 0;
  public static final int COL_PROJECT_STATUS = 1;

  private static final String[] COLUMN_NAMES = {"Project Name", "Status"};

  private final List<Project> projects;
  private final List<Project> visibleRows;

  ProjectModel(List<Project> projects) {
    this.projects = projects;
    this.visibleRows = new ArrayList<>();
  }

  public void SetProjects(List<Project> projects) {
    this.projects.clear();
    this.projects.addAll(projects);
  }

  public List<Project> getProjects() {
    return this.projects;
  }

  public List<Project> getVisibleRows() {
    return this.visibleRows;
  }

  public void updateProject(Project project) {
    for(int i = 0; i < projects.size(); i++) {
      if(Objects.equals(projects.get(i), project)) {
        projects.set(i, project);
        break;
      }
    }
    refresh();
  }

  public void refresh() {
    rebuildVisibleRows();
    fireTableDataChanged();
  }

  private void rebuildVisibleRows() {
    this.visibleRows.clear();
    this.visibleRows.addAll(this.projects);
  }

  @Override
  public int getRowCount() {
    return this.projects.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return COLUMN_NAMES[columnIndex];
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Project project = this.projects.get(rowIndex);
    if(columnIndex == COL_PROJECT_NAME) {
      return project.name();
    } else if(columnIndex == COL_PROJECT_STATUS) {
      return project.status();
    } else {
      return null;
    }
  }

  public boolean isEmpty() {
    return this.projects.isEmpty();
  }

}

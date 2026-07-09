package dev.nerydlg.taskmanager.components;

import dev.nerydlg.taskmanager.entity.Task;

import javax.swing.AbstractListModel;
import java.util.List;

public class NextTaskListModel extends AbstractListModel<Task> {

  private final List<Task> tasks;

  public NextTaskListModel(List<Task> tasks) {
    this.tasks = tasks;
  }

  @Override
  public int getSize() {
    return tasks.size();
  }

  @Override
  public Task getElementAt(int index) {
    return tasks.get(index);
  }
}

package dev.nerydlg.taskmanager.entity;

public enum TaskStatus {
  NEW(0),
  IN_PROGRESS(1),
  CLOSE(2),
  DONE(3);

  private final int value;

  private TaskStatus(int value) {
    this.value = value;
  }

  public static TaskStatus fromValue(int value) {
    for (TaskStatus taskStatus : values()) {
      if (taskStatus.getValue() == value) {
        return taskStatus;
      }
    }
    return null;
  }

  public int getValue() {
    return value;
  }
}

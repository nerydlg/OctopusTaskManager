package dev.nerydlg.taskmanager.entity;

public enum TaskType {
  TASK(0),
  FIX(1),
  FEATURE(2),
  INVESTIGATE(3);

  private final int value;

  private TaskType(int taskType) {
    this.value = taskType;
  }

  public static TaskType fromValue(int value) {
    for (TaskType taskType : values()) {
      if (taskType.getValue() == value) {
        return taskType;
      }
    }
    return null;
  }

  public int getValue() {
    return value;
  }
}

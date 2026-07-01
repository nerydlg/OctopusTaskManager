package dev.nerydlg.taskmanager.database;

public record Fields(String name, FieldType type) {
  public Fields(String name, FieldType type) {
    this.name = name;
    this.type = type;
  }
}

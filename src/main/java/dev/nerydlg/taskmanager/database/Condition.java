package dev.nerydlg.taskmanager.database;

public record Condition(Fields field, Operator operator, Integer numOfArguments) {
  public Condition(Fields field, Operator operator) {
    this(field, operator, 1);
  }
}

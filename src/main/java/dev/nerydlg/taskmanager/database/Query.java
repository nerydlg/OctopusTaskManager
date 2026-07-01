package dev.nerydlg.taskmanager.database;

import java.util.List;

public class Query {

  public final String query;
  public final List<Parameter> parameters;

  protected Query(String query, List<Parameter> parameters) {
    this.query = query;
    this.parameters = parameters;
  }

  public String getQuery() {
    return query;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }
}

package dev.nerydlg.taskmanager.database;

import dev.nerydlg.taskmanager.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryBuilder {

  public static final Logger log = LogManager.getLogger(QueryBuilder.class);
  private final StringBuilder query;
  private QueryType queryType;
  private List<String> fields;
  private List<Parameter> parameters;
  private String tableName;

  private QueryBuilder() {
    query = new StringBuilder();
  }

  public static QueryBuilder create() {
    return new QueryBuilder();
  }

  private String getFieldsAsString() {
    return String.join(", ", this.fields);
  }

  private String getParametersAsString() {
    return parameters.stream().map(_ -> "?").collect(Collectors.joining(", "));
  }

  public QueryBuilder select(String... fields) {
    queryType = QueryType.SELECT;
    query.append("SELECT ");
    this.fields = List.of(fields);
    query.append(getFieldsAsString()).append(" ");
    return this;
  }

  public QueryBuilder selectAll() {
    queryType = QueryType.SELECT_ALL;
    query.append("SELECT * ");
    parameters = new ArrayList<>();
    return this;
  }

  public QueryBuilder count() {
    queryType = QueryType.COUNT;
    query.append("SELECT COUNT(*) ");
    parameters = new ArrayList<>();
    return this;
  }

  public QueryBuilder insert() {
    queryType = QueryType.INSERT;
    query.append("INSERT INTO ");
    return this;
  }

  public QueryBuilder into(String... fields) {
    if (queryType != QueryType.INSERT) {
      throw new IllegalStateException("INTO can only be set for INSERT queries");
    }
    this.fields = List.of(fields);
    query.append("(").append(getFieldsAsString()).append(") ");
    return this;
  }

  public QueryBuilder values(List<Object> params, List<FieldType> fieldTypes) {
    if (QueryType.INSERT != queryType) {
      throw new IllegalStateException("VALUES can only be set for INSERT queries");
    }
    if (params.size() != fieldTypes.size()) {
      throw new IllegalArgumentException("Number of parameters does not match number of fields");
    }
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    for (int i = 0; i < params.size(); i++) {
      Fields field = new Fields(this.fields.get(i), fieldTypes.get(i));
      Parameter param = new Parameter(field, params.get(i));
      parameters.add(param);
    }
    this.query.append("VALUES (").append(getParametersAsString()).append(")");
    return this;
  }

  public QueryBuilder table(String tableName) {
    if (queryType != QueryType.INSERT && queryType != QueryType.UPDATE) {
      throw new IllegalStateException("Table name can only be set for INSERT queries");
    }
    this.tableName = tableName;
    query.append(tableName).append(" ");
    return this;
  }

  public QueryBuilder delete() {
    queryType = QueryType.DELETE;
    query.append("DELETE ");
    return this;
  }

  public QueryBuilder update() {
    queryType = QueryType.UPDATE;
    query.append("UPDATE ");
    return this;
  }

  public QueryBuilder fields(String... fields) {
    this.fields = List.of(fields);
    query.append(getFieldsAsString()).append(" ");
    return this;
  }

  public QueryBuilder from(String tableName) {
    this.tableName = tableName;
    query.append("FROM ").append(tableName).append(" ");
    return this;
  }

  public QueryBuilder where(String fieldName, Operator operator, FieldType fieldType, Object value) {
    if (queryType == QueryType.INSERT) {
      throw new IllegalStateException("WHERE can only be set for SELECT, UPDATE, DELETE queries");
    }

    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    Fields field = new Fields(fieldName, fieldType);
    Condition condition = new Condition(field, operator);
    Parameter param = new Parameter(field, value);
    parameters.add(param);
    query.append("WHERE ").append(conditionAsString(condition)).append(" ");
    return this;
  }

  private String conditionAsString(Condition condition) {
    StringBuilder conditionString = new StringBuilder();
    switch (condition.operator()) {
      case EQUALS -> conditionString.append(condition.field().name()).append(" = ").append("?");
      case NOT_EQUALS -> conditionString.append(condition.field().name()).append(" != ").append("?");
      case GREATER_THAN -> conditionString.append(condition.field().name()).append(" > ").append("?");
      case LESS_THAN -> conditionString.append(condition.field().name()).append(" < ").append("?");
      case GREATER_THAN_OR_EQUALS -> conditionString.append(condition.field().name()).append(" >= ").append("?");
      case LESS_THAN_OR_EQUALS -> conditionString.append(condition.field().name()).append(" <= ").append("?");
      case LIKE -> conditionString.append(condition.field().name()).append(" LIKE ").append("?");
      case NOT_LIKE -> conditionString.append(condition.field().name()).append(" NOT LIKE ").append("?");
      case IN ->
          conditionString.append(condition.field().name()).append(" IN ").append("(").append(getParametersCount(condition.numOfArguments())).append(")");
      default -> throw new IllegalArgumentException("Unsupported operator: " + condition.operator());
    }
    return conditionString.toString();
  }

  private String getParametersCount(Integer integer) {
    return Stream.generate(() -> "?").limit(integer).collect(Collectors.joining(","));
  }

  public QueryBuilder and(String fieldName, Operator operator, FieldType fieldType, Object value) {
    Fields field = new Fields(fieldName, fieldType);
    Condition condition = new Condition(field, operator);
    Parameter parameter = new Parameter(field, value);
    parameters.add(parameter);
    query.append("AND ").append(conditionAsString(condition)).append(" ");
    return this;
  }

  public QueryBuilder or(String fieldName, Operator operator, FieldType fieldType, Object value) {
    Fields field = new Fields(fieldName, fieldType);
    Condition condition = new Condition(field, operator);
    Parameter parameter = new Parameter(field, value);
    parameters.add(parameter);
    query.append("OR ").append(conditionAsString(condition)).append(" ");
    return this;
  }

  public QueryBuilder in(String fieldName, FieldType fieldType, Object... values) {
    if (QueryType.INSERT == queryType) {
      throw new IllegalStateException("IN can only be set for SELECT, DELETE, COUNT, or UPDATE queries");
    }
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    Fields field = new Fields(fieldName, fieldType);
    List<Object> valuesList = List.of(values);
    query.append(field)
        .append(" IN(")
        .append(valuesList.stream().map(_ -> "?").collect(Collectors.joining(", ")))
        .append(") ");
    List<Parameter> inParams = valuesList.stream()
        .map(value -> {
          Parameter param = new Parameter(field, value);
          parameters.add(param);
          return param;
        })
        .toList();
    parameters.addAll(inParams);
    return this;
  }

  public QueryBuilder set(String... fields) {
    if (queryType != QueryType.UPDATE) {
      throw new IllegalStateException("SET can only be set for UPDATE queries");
    }
    this.fields = List.of(fields);
    query.append("SET ")
        .append(this.fields.stream().map(field -> field + " = ?").collect(Collectors.joining(", ")))
        .append(" ");
    return this;
  }

  public QueryBuilder setValues(List<Object> values, List<FieldType> fieldTypes) {
    if (QueryType.UPDATE != queryType) {
      throw new IllegalStateException("IN can only be set for UPDATE queries");
    }
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    if (values.size() != fieldTypes.size()) {
      throw new IllegalArgumentException("Number of parameters does not match number of fields");
    }

    for (int i = 0; i < values.size(); i++) {
      Fields field = new Fields(this.fields.get(i), fieldTypes.get(i));
      parameters.add(new Parameter(field, values.get(i)));
    }

    return this;
  }

  public Query build() {
    if (tableName == null) {
      throw new IllegalArgumentException("Table name is required");
    }
    if (queryType == null) {
      throw new IllegalArgumentException("Query type is required");
    }
    if (fields == null && requiresFields()) {
      throw new IllegalArgumentException("Fields are required");
    }
    StringUtils.removeTrailingSpace(query);
    query.append(";");
    log.debug("Query: {}", query);
    return new Query(query.toString(), parameters);
  }

  private boolean requiresFields() {
    return queryType == QueryType.SELECT;
  }
}

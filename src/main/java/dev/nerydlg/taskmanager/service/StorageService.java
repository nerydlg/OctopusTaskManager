package dev.nerydlg.taskmanager.service;

import dev.nerydlg.taskmanager.database.Parameter;
import dev.nerydlg.taskmanager.database.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StorageService {

  private static final Logger log = LogManager.getLogger(StorageService.class);

  private static final String CONNECTION_PATH = "jdbc:sqlite:";
  public static StorageService instance;
  public Connection connection;

  private StorageService() {
  }

  public static StorageService getInstance() {
    if (instance == null) {
      instance = new StorageService();
    }
    return instance;
  }

  public void open(String storagePath) {
    try {
      if (connection != null) {
        connection.close();
      }
      log.debug("Opening database connection to {}", storagePath);
      StringBuilder path = new StringBuilder(CONNECTION_PATH);
      path.append(storagePath);
      connection = DriverManager.getConnection(path.toString());
      initDB();
      log.info("Database connection opened successfully");
    } catch (SQLException ex) {
      log.error("Failed to open database connection to {}", storagePath, ex);
    }
  }

  private void initDB() throws SQLException {
    int dbVersion = getDbVersion();
    List<String> schema = getSchema();
    for (int i = dbVersion; i < schema.size(); i++) {
      String createTableSql = schema.get(i);
      Statement stmt = connection.createStatement();
      stmt.execute(createTableSql);
      stmt.close();
    }
  }

  private int getDbVersion() throws SQLException {
    if (connection == null) {
      return -1;
    }
    Statement statement = executeStatement("PRAGMA schema_version;");
    int schemaVersion = statement.getResultSet().getInt("schema_version");
    statement.close();
    return schemaVersion;
  }

  public Statement executeStatement(String stmt) throws SQLException {
    if (connection == null) {
      throw new IllegalStateException("Database connection is not initialized");
    }
    Statement statement = null;
    statement = connection.createStatement();
    if (!statement.execute(stmt)) {
      statement.close();
      throw new SQLException("Failed to execute statement " + stmt);
    }
    return statement;
  }

  public ResultSet executeQuery(Query query) throws SQLException {
    if (connection == null) {
      throw new IllegalStateException("Database connection is not initialized");
    }
    PreparedStatement stmt = null;
    stmt = connection.prepareStatement(query.getQuery());
    for (int i = 0; i < query.getParameters().size(); i++) {
      Parameter param = query.getParameters().get(i);
      switch (param.field().type()) {
        case INTEGER -> stmt.setInt(i + 1, (Integer) param.value());
        case TEXT -> stmt.setString(i + 1, (String) param.value());
        case BLOB -> stmt.setBytes(i + 1, (byte[]) param.value());
        case DATE -> {
          LocalDateTime dateTime = (LocalDateTime) param.value();
          Timestamp timestamp = null;
          if (dateTime != null) {
            timestamp = Timestamp.valueOf(dateTime);
          }
          stmt.setTimestamp(i + 1, timestamp);
        }
        case BOOLEAN -> stmt.setBoolean(i, (Boolean) param.value());
        default -> throw new IllegalArgumentException("Unsupported parameter type: " + param.field().type());
      }
    }

    if (!stmt.execute() && query.getQuery().matches("(?i).*SELECT.*")) {
      stmt.close();
      throw new SQLException("Failed to execute query " + query.getQuery());
    }

    return stmt.getResultSet();
  }

  private List<String> getSchema() {
    List<String> schema = new ArrayList<>();
    schema.add("""
        CREATE TABLE IF NOT EXISTS tm_project(
        id INTEGER PRIMARY KEY,
        name TEXT,
        status INTEGER,
        created_date DATETIME,
        last_updated DATETIME
        );""");
    schema.add("""
        CREATE TABLE IF NOT EXISTS tm_task(
        id INTEGER PRIMARY KEY,
        title TEXT NOT NULL,
        type INTEGER,
        desc TEXT,
        priority INTEGER,
        status INTEGER,
        due_date DATETIME,
        created_date DATETIME,
        updated_date DATETIME,
        project_id INTEGER,
        parent_id REFERENCES tm_task(id) NULL,
        FOREIGN KEY(project_id) REFERENCES tm_project(id)
        );""");
    return schema;
  }

  public void close() {
    if (connection != null) {
      try {
        connection.close();
      } catch (Exception e) {
        log.error("Failed to close database connection", e);
      }
    }
  }
}

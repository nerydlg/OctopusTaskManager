package dev.nerydlg.taskmanager.repository;

import dev.nerydlg.taskmanager.database.FieldType;
import dev.nerydlg.taskmanager.database.Operator;
import dev.nerydlg.taskmanager.database.Query;
import dev.nerydlg.taskmanager.database.QueryBuilder;
import dev.nerydlg.taskmanager.entity.Task;
import dev.nerydlg.taskmanager.entity.TaskStatus;
import dev.nerydlg.taskmanager.entity.TaskType;
import dev.nerydlg.taskmanager.service.StorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static dev.nerydlg.taskmanager.database.FieldType.DATE;
import static dev.nerydlg.taskmanager.database.FieldType.INTEGER;
import static dev.nerydlg.taskmanager.database.FieldType.TEXT;

public class TaskRepository {

  private static final Logger log = LogManager.getLogger(TaskRepository.class);
  private static final String TABLE = "tm_task";

  private final StorageService storageService;

  public TaskRepository() {
    this.storageService = StorageService.getInstance();
  }

  public List<Task> findAllByProjectId(Integer projectId) throws SQLException {
    log.debug("Finding all tasks by project id {}", projectId);
    Query query = QueryBuilder.create()
        .selectAll()
        .from(TABLE)
        .where("project_id", Operator.EQUALS, INTEGER, projectId)
        .build();
    ResultSet rs = storageService.executeQuery(query);
    List<Task> tasks = new ArrayList<>();
    while (rs.next()) {
      tasks.add(createTaskFromResultSet(rs));
    }
    return tasks;
  }

  public List<Task> findAll() throws SQLException {
    log.debug("Finding all tasks");
    Query query = QueryBuilder.create()
        .selectAll()
        .from(TABLE)
        .build();
    ResultSet rs = storageService.executeQuery(query);
    List<Task> tasks = new ArrayList<>();
    while (rs.next()) {
      tasks.add(createTaskFromResultSet(rs));
    }
    return tasks;
  }

  public void delete(Task task) throws SQLException {
    log.debug("Deleting task {}", task);
    Query delete = QueryBuilder.create()
        .delete()
        .from(TABLE)
        .where("id", Operator.EQUALS, INTEGER, task.id())
        .build();
    storageService.executeQuery(delete);
  }

  public Task save(Task task) throws SQLException {
    log.debug("Saving task {}", task);
    List<Object> values = new ArrayList<>();
    values.add(task.title());
    values.add(task.type().getValue());
    values.add(task.desc());
    values.add(task.priority());
    values.add(task.status().getValue());
    values.add(task.dueDate());
    values.add(task.createdAt());
    values.add(task.updatedAt());
    values.add(task.projectId());
    values.add(task.parentId());
    List<FieldType> types = List.of(TEXT, INTEGER, TEXT, INTEGER, INTEGER, DATE, DATE, DATE, INTEGER, INTEGER);

    Query insert = QueryBuilder.create()
        .insert()
        .table(TABLE)
        .into("title", "type", "desc", "priority", "status", "due_date", "created_date", "updated_date", "project_id", "parent_id")
        .values(values, types)
        .build();
    storageService.executeQuery(insert);
    int id = storageService.lastInsertRowId();
    return new Task(id, task.title(), task.type(), task.desc(), task.priority(), task.status(),
        task.dueDate(), task.createdAt(), task.updatedAt(), task.projectId(), task.parentId());
  }

  public List<Task> findAllPendingTasksByProject(Integer project) throws SQLException {
    log.debug("Finding all pending tasks by project {}", project);
    Query query = QueryBuilder
        .create()
        .selectAll()
        .from(TABLE)
        .where("project_id", Operator.EQUALS, INTEGER, project)
        .and("status", Operator.LESS_THAN, INTEGER, TaskStatus.CLOSE)
        .build();
    ResultSet rs = storageService.executeQuery(query);
    List<Task> tasks = new ArrayList<>();
    while (rs.next()) {
      tasks.add(createTaskFromResultSet(rs));
    }
    return tasks;
  }

  private Task createTaskFromResultSet(ResultSet rs) throws SQLException {
    log.debug("Creating task from result set");
    Integer id = rs.getInt("id");
    String name = rs.getString("title");
    int type = rs.getInt("type");
    String description = rs.getString("desc");
    Integer priority = rs.getInt("priority");
    int status = rs.getInt("status");
    LocalDateTime dueDate = rs.getTimestamp("due_date").toLocalDateTime();
    LocalDateTime createdAt = rs.getTimestamp("created_date").toLocalDateTime();
    LocalDateTime updatedAt = rs.getTimestamp("updated_date").toLocalDateTime();
    Integer projId = rs.getInt("project_id");
    Integer parentId = rs.getInt("parent_id");
    return new Task(id,
        name,
        TaskType.fromValue(type),
        description,
        priority,
        TaskStatus.fromValue(status),
        dueDate,
        createdAt,
        updatedAt,
        projId,
        parentId);
  }

}

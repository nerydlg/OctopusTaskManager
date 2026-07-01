package dev.nerydlg.taskmanager.repository;

import dev.nerydlg.taskmanager.database.Operator;
import dev.nerydlg.taskmanager.database.Query;
import dev.nerydlg.taskmanager.database.QueryBuilder;
import dev.nerydlg.taskmanager.entity.Project;
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

public class ProjectRepository {

  private static final Logger log = LogManager.getLogger(ProjectRepository.class);
  private static final String TABLE = "tm_project";

  private final StorageService storageService;

  public ProjectRepository() {
    this.storageService = StorageService.getInstance();
  }

  public Project save(Project project) throws SQLException {
    log.debug("Saving project {}", project);
    Query insert = QueryBuilder.create()
        .insert()
        .table(TABLE)
        .into("name", "status", "created_date", "last_updated")
        .values(List.of(project.name(), project.status(), project.createdAt(), project.updatedAt())
            , List.of(TEXT, INTEGER, DATE, DATE))
        .build();
    storageService.executeQuery(insert);
    // Get the id of the inserted project
    return findWithFilters(project);
  }

  public List<Project> findAll() throws SQLException {
    log.debug("Finding all projects");
    List<Project> projects = new ArrayList<>();
    Query query = QueryBuilder.create()
        .selectAll()
        .from(TABLE)
        .build();
    ResultSet rs = storageService.executeQuery(query);
    while (rs.next()) {
      projects.add(createProjectFromResultSet(rs));
    }
    return projects;
  }

  public Project findWithFilters(Project project) throws SQLException {
    log.debug("Finding projects with filter");
    Query query = QueryWithFilters(project, false).build();
    ResultSet rs = storageService.executeQuery(query);
    if (rs.next()) {
      return createProjectFromResultSet(rs);
    }
    return null;
  }

  public Integer countProjects() throws SQLException {
    log.debug("Counting projects");
    Query query = QueryBuilder.create()
        .count()
        .from(TABLE)
        .build();
    ResultSet rs = storageService.executeQuery(query);
    rs.next();
    return rs.getInt(1);
  }

  public Integer countProjectsWithFilter(Project project) throws SQLException {
    log.debug("Count projects with filter");
    Query query = QueryWithFilters(project, true).build();
    ResultSet rs = storageService.executeQuery(query);
    return rs.getInt(1);
  }

  private QueryBuilder QueryWithFilters(Project project, boolean isCount) {
    log.debug("Building query with filters");
    QueryBuilder query = QueryBuilder.create();
    if (isCount) {
      query.count().from(TABLE);
    } else {
      query.selectAll().from(TABLE);
    }
    int conditionCount = 0;
    if (project.name() != null && !project.name().isEmpty()) {
      query.where("name", Operator.LIKE, TEXT, project.name());
      conditionCount++;
    }
    if (project.status() != null) {
      if (conditionCount == 0) {
        query.where("status", Operator.EQUALS, INTEGER, project.status());
        conditionCount++;
      } else {
        query.and("status", Operator.EQUALS, INTEGER, project.status());
      }
    }
    if (project.createdAt() != null) {
      if (conditionCount == 0) {
        query.where("created_date", Operator.EQUALS, DATE, project.createdAt());
        conditionCount++;
      } else {
        query.and("created_date", Operator.EQUALS, DATE, project.createdAt());
      }
    }
    return query;
  }

  private Project createProjectFromResultSet(ResultSet rs) throws SQLException {
    log.debug("reading ResultSet");
    Integer id = rs.getInt("id");
    String name = rs.getString("name");
    Integer status = rs.getInt("status");
    LocalDateTime createdAt = rs.getTimestamp("created_date").toLocalDateTime();
    LocalDateTime lastUpdated = rs.getTimestamp("last_updated").toLocalDateTime();
    return new Project(id, name, status, createdAt, lastUpdated);
  }

  public void update(Project updatedProject) throws SQLException {
    log.debug("Updating project {}", updatedProject);
    Query query = QueryBuilder.create()
        .update()
        .table(TABLE)
        .set("name", "status", "last_updated")
        .setValues(List.of(updatedProject.name(), updatedProject.status(), updatedProject.updatedAt())
            , List.of(TEXT, INTEGER, DATE))
        .where("id", Operator.EQUALS, INTEGER, updatedProject.id())
        .build();
    storageService.executeQuery(query);
  }
}

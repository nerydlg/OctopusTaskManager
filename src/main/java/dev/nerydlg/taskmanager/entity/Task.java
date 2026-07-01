package dev.nerydlg.taskmanager.entity;

import java.time.LocalDateTime;

/**
 * Mirrors the {@code tm_task} table. A {@code null} {@link #parentId()} marks a
 * root task; a non-null value links the task to its parent task's {@link #id()}.
 */
public record Task(Integer id,
                   String title,
                   TaskType type,
                   String desc,
                   Integer priority,
                   TaskStatus status,
                   LocalDateTime dueDate,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt,
                   Integer projectId,
                   Integer parentId) {

  /**
   * Convenience constructor for the fields the task list cares about.
   */
  public Task(Integer id, String title, TaskType type, String desc,
              Integer priority, TaskStatus status, Integer parentId) {
    this(id, title, type, desc, priority, status, null, null, null, null, parentId);
  }
}

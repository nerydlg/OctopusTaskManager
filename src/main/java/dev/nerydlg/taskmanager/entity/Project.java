package dev.nerydlg.taskmanager.entity;

import java.time.LocalDateTime;

public record Project(Integer id,
                      String name,
                      Integer status,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {

  public Project(String name) {
    this(0, name, 0, LocalDateTime.now(), LocalDateTime.now());
  }
}

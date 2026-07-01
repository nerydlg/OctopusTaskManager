package dev.nerydlg.taskmanager.configuration;

import dev.nerydlg.taskmanager.service.FileManagerService;

public class ServiceConfiguration {

  public FileManagerService createFileManagerService() {
    return new FileManagerService();
  }
}

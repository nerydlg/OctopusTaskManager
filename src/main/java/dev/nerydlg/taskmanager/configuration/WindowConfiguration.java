package dev.nerydlg.taskmanager.configuration;

import dev.nerydlg.taskmanager.repository.ProjectRepository;
import dev.nerydlg.taskmanager.repository.TaskRepository;
import dev.nerydlg.taskmanager.service.FileManagerService;
import dev.nerydlg.taskmanager.utils.WindowUtils;
import dev.nerydlg.taskmanager.window.TabManager;
import dev.nerydlg.taskmanager.window.TaskManagerWindow;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class WindowConfiguration {

  private final ServiceConfiguration serviceConfiguration;
  private final MenuConfiguration menuConfiguration;

  public WindowConfiguration(ServiceConfiguration serviceConfiguration,
                             MenuConfiguration menuConfiguration) {
    this.serviceConfiguration = serviceConfiguration;
    this.menuConfiguration = menuConfiguration;
  }

  public JFrame createFrame() {
    return new JFrame(WindowUtils.APP_NAME);
  }

  public JTabbedPane createTabbedPane() {
    return new JTabbedPane();
  }

  public TabManager createTabManager(JFrame frame) {
    return new TabManager(frame, createTabbedPane(), new ProjectRepository(), new TaskRepository());
  }

  public TaskManagerWindow createTaskManagerWindow() {
    JFrame frame = createFrame();
    FileManagerService fileManagerService = serviceConfiguration.createFileManagerService();
    TabManager tabManager = createTabManager(frame);
    return new TaskManagerWindow(frame
        , fileManagerService
        , menuConfiguration.createAppMenuBar(frame, fileManagerService, tabManager)
        , tabManager);
  }
}

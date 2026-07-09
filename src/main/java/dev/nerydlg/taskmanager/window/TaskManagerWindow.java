package dev.nerydlg.taskmanager.window;

import dev.nerydlg.taskmanager.service.FileManagerService;
import dev.nerydlg.taskmanager.service.StorageService;
import dev.nerydlg.taskmanager.window.menu.AppMenuBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.io.IOException;

import static dev.nerydlg.taskmanager.utils.ImageUtils.createImageIcon;

public class TaskManagerWindow implements Runnable {

  private static final Logger log = LogManager.getLogger(TaskManagerWindow.class);

  private final JFrame frame;
  private final FileManagerService fileManagerService;
  private final StorageService storageService;
  private final AppMenuBar menuBar;
  private final TabManager tabManager;
  private boolean isInitialized = false;

  public TaskManagerWindow(JFrame frame
      , FileManagerService fileManagerService
      , AppMenuBar menuBar
      , TabManager tabManager) {
    this.frame = frame;
    this.fileManagerService = fileManagerService;
    this.menuBar = menuBar;
    this.tabManager = tabManager;
    this.storageService = StorageService.getInstance();
  }

  private void initComponents() {
    log.debug("Initializing components");
    ImageIcon icon = createImageIcon("/ico/octopus_preview_64.png", 30, 30);
    frame.setIconImage(icon.getImage());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // configure the window
    menuBar.init();
    frame.setJMenuBar(menuBar.getMenuBar());
    frame.setSize(900, 700);
    frame.setLocationRelativeTo(null);
    tabManager.init();
    frame.add(tabManager.getTabPanel());
    isInitialized = true;
  }

  private void show() {
    if (isInitialized) {
      frame.pack(); // uncomment to make the window fit the content
      frame.setVisible(true);
    }
  }

  @Override
  public void run() {
    // initialize the components
    try {
      String latestStorage = fileManagerService.getLatestStorageUsed();
      storageService.open(latestStorage);
      initComponents();
      show();
    } catch (IOException e) {
      log.error("Failed While Opening db", e);
    }
    // show the window
  }

}
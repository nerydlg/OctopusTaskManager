package dev.nerydlg.taskmanager.configuration;

import dev.nerydlg.taskmanager.components.AlertDialog;
import dev.nerydlg.taskmanager.components.ConfirmationDialog;
import dev.nerydlg.taskmanager.service.FileManagerService;
import dev.nerydlg.taskmanager.service.StorageService;
import dev.nerydlg.taskmanager.window.TabManager;
import dev.nerydlg.taskmanager.window.menu.AppMenuBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static dev.nerydlg.taskmanager.utils.WindowUtils.setStorageTitle;

public class MenuConfiguration {

  private static final Logger log = LogManager.getLogger(MenuConfiguration.class);

  private static final String STORAGE_EXTENSION = ".db";
  private static final String NEW_STORAGE_NAME = "tasks" + STORAGE_EXTENSION;

  public JMenu createFileMenu(JFrame frame, FileManagerService fileManagerService,
                              TabManager tabManager) {
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    JMenuItem open = new JMenuItem("Open Tasks File");
    open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    open.addActionListener(e -> onOpenFile(frame, fileManagerService, tabManager));
    fileMenu.add(open);
    JMenuItem create = new JMenuItem("Create New Task File");
    create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
    create.addActionListener(e -> onCreateFile(frame, fileManagerService, tabManager));
    fileMenu.add(create);
    fileMenu.addSeparator();
    fileMenu.add(new JMenuItem("Manage Projects..."));
    fileMenu.addSeparator();
    JMenuItem exit = new JMenuItem("Exit");
    exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
    exit.addActionListener(e -> onExit(frame));

    fileMenu.add(exit);
    return fileMenu;
  }

  private void onOpenFile(JFrame frame, FileManagerService fileManagerService,
                          TabManager tabManager) {
    File selected = chooseTaskFile(frame, fileManagerService);
    if (selected == null) {
      return;
    }
    if (!selected.isFile()) {
      showAlert(frame, "Not a task file: " + selected.getName());
      return;
    }

    log.info("Opening task file {}", selected.getAbsolutePath());
    switchStorage(frame, fileManagerService, tabManager, selected);
  }

  private void onCreateFile(JFrame frame, FileManagerService fileManagerService,
                            TabManager tabManager) {
    File selected = chooseNewTaskFile(frame, fileManagerService);
    if (selected == null) {
      return;
    }
    // SQLite opens an existing file instead of replacing it, so an existing name would
    // silently reuse its tasks rather than start an empty one.
    if (selected.exists() && !replaceExistingFile(frame, fileManagerService, selected)) {
      return;
    }
    File parent = selected.getAbsoluteFile().getParentFile();
    if (parent != null && !parent.exists() && !parent.mkdirs()) {
      showAlert(frame, "Could not create folder " + parent.getName());
      return;
    }

    log.info("Creating task file {}", selected.getAbsolutePath());
    switchStorage(frame, fileManagerService, tabManager, selected);
  }

  private boolean replaceExistingFile(JFrame frame, FileManagerService fileManagerService,
                                      File selected) {
    ConfirmationDialog confirmationDialog =
        new ConfirmationDialog(frame, selected.getName() + " already exists, replace it?");
    confirmationDialog.setVisible(true);
    if (!confirmationDialog.isConfirmed()) {
      return false;
    }
    // The file may be the one currently open, so drop the connection before removing it.
    StorageService.getInstance().close();
    if (!selected.delete()) {
      log.error("Failed to delete task file {}", selected);
      showAlert(frame, "Could not replace " + selected.getName());
      reopenPreviousStorage(frame, fileManagerService);
      return false;
    }
    return true;
  }

  /**
   * Points the application at another task file: opens it, remembers it as the latest
   * one used and rebuilds the tabs. The previously opened file is restored when the
   * target turns out not to be a usable database.
   */
  private void switchStorage(JFrame frame, FileManagerService fileManagerService,
                             TabManager tabManager, File target) {
    StorageService storageService = StorageService.getInstance();
    String targetStorage = target.getAbsolutePath();

    storageService.close();
    if (!storageService.open(targetStorage)) {
      showAlert(frame, "Could not open " + target.getName());
      reopenPreviousStorage(frame, fileManagerService);
      return;
    }

    try {
      fileManagerService.writeToLatestFile(targetStorage);
    } catch (IOException ex) {
      // The file is open and usable, it just will not be the one restored on next start.
      log.error("Failed to remember task file {} as the latest one used", targetStorage, ex);
      showAlert(frame, "Could not remember " + target.getName() + " for the next start");
    }
    setStorageTitle(frame, targetStorage);
    tabManager.reload();
  }

  /**
   * Goes back to the file recorded as the latest one used. The record is only written
   * once a file has been opened, so it still points at the file used before the switch.
   */
  private void reopenPreviousStorage(JFrame frame, FileManagerService fileManagerService) {
    try {
      String previousStorage = fileManagerService.readFromLatestFile();
      StorageService.getInstance().open(previousStorage);
      setStorageTitle(frame, previousStorage);
    } catch (IOException ex) {
      log.error("Failed to reopen the previous task file", ex);
    }
  }

  private File chooseTaskFile(JFrame frame, FileManagerService fileManagerService) {
    JFileChooser chooser = createTaskFileChooser(fileManagerService);
    chooser.setDialogTitle("Open Task File");
    if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return chooser.getSelectedFile();
  }

  private File chooseNewTaskFile(JFrame frame, FileManagerService fileManagerService) {
    JFileChooser chooser = createTaskFileChooser(fileManagerService);
    chooser.setDialogTitle("Create New Task File");
    chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), NEW_STORAGE_NAME));
    if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return withStorageExtension(chooser.getSelectedFile());
  }

  private JFileChooser createTaskFileChooser(FileManagerService fileManagerService) {
    JFileChooser chooser =
        new JFileChooser(new File(fileManagerService.getDefaultStoragePath()).getParentFile());
    chooser.setFileFilter(new FileNameExtensionFilter("Task files (*.db)", "db"));
    return chooser;
  }

  private File withStorageExtension(File file) {
    if (file.getName().toLowerCase().endsWith(STORAGE_EXTENSION)) {
      return file;
    }
    File absolute = file.getAbsoluteFile();
    return new File(absolute.getParentFile(), absolute.getName() + STORAGE_EXTENSION);
  }

  private void showAlert(JFrame frame, String message) {
    new AlertDialog(frame, message).setVisible(true);
  }

  private void onExit(JFrame frame) {
    StorageService.getInstance().close();
    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
  }

  public List<JMenu> createMenuItems(JFrame frame, FileManagerService fileManagerService,
                                     TabManager tabManager) {
    return List.of(createFileMenu(frame, fileManagerService, tabManager));
  }

  public JMenuBar createJMenuBar() {
    return new JMenuBar();
  }

  public AppMenuBar createAppMenuBar(JFrame frame, FileManagerService fileManagerService,
                                     TabManager tabManager) {
    return new AppMenuBar(createJMenuBar(),
        createMenuItems(frame, fileManagerService, tabManager));
  }

}

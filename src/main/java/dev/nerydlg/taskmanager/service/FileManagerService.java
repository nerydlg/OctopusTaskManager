package dev.nerydlg.taskmanager.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManagerService {

  private static final Logger log = LogManager.getLogger(FileManagerService.class);

  private static final String APP_DATA_FOLDER = ".octopus-task-manager";
  private static final String LATEST_STORAGE_USED_FILE = "latest_storage_used.txt";
  private static final String DEFAULT_STORAGE_PATH = "tasks.db";
  private static final String USER_HOME = System.getProperty("user.home");

  public FileManagerService() {

  }

  public String getLatestStorageUsed() throws IOException {
    log.debug("Getting latest storage used");
    String latestStoragePath = getLatestStorageFilePath();
    File latestOpenedFile = new File(latestStoragePath);
    if (!latestOpenedFile.exists()) {
      log.debug("Latest storage file does not exist, using default storage path");
      writeToLatestFile(getDefaultStoragePath());
    }
    latestStoragePath = readFromLatestFile();
    return latestStoragePath;
  }

  private String getLatestStorageFilePath() {
    log.debug("Getting latest storage file path");
    return String.join(File.separator, USER_HOME, APP_DATA_FOLDER, LATEST_STORAGE_USED_FILE);
  }

  public String getDefaultStoragePath() {
    log.debug("Getting default storage path");
    return String.join(File.separator, USER_HOME, APP_DATA_FOLDER, DEFAULT_STORAGE_PATH);
  }

  public void writeToLatestFile(String storagePath) throws IOException {
    // write the path to the latest opened file
    log.debug("Writing to latest storage file");
    String latestStoragePath = getLatestStorageFilePath();
    createOrDeleteFile(latestStoragePath);
    BufferedWriter bw = new BufferedWriter(new FileWriter(latestStoragePath));
    bw.write(storagePath);
    bw.close();
  }

  private void createOrDeleteFile(String latestStoragePath) throws IOException, SecurityException {
    log.debug("Creating directory {}", latestStoragePath);
    File latestOpenedFile = new File(latestStoragePath);
    if (latestOpenedFile.exists()) {
      latestOpenedFile.delete();
    }
    latestOpenedFile.getParentFile().mkdirs();
    latestOpenedFile.createNewFile();
  }

  public String readFromLatestFile() throws IOException {
    // read the path to the latest opened file
    log.debug("Reading from latest storage file");
    BufferedReader br = new BufferedReader(new FileReader(getLatestStorageFilePath()));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    return sb.toString();
  }
}

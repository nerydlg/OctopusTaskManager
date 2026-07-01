package dev.nerydlg.taskmanager;

import com.formdev.flatlaf.FlatLightLaf;
import dev.nerydlg.taskmanager.configuration.MenuConfiguration;
import dev.nerydlg.taskmanager.configuration.ServiceConfiguration;
import dev.nerydlg.taskmanager.configuration.WindowConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SwingUtilities;

public class Application {
  private static final Logger log = LogManager.getLogger(Application.class);

  public static void main(String[] args) {
    // start java swing application
    log.info("Starting application");
    ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
    MenuConfiguration menuConfiguration = new MenuConfiguration();
    WindowConfiguration window = new WindowConfiguration(serviceConfiguration, menuConfiguration);
    FlatLightLaf.setup();
    SwingUtilities.invokeLater(window.createTaskManagerWindow());
  }
}
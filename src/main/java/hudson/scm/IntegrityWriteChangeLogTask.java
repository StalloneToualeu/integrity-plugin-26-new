/*******************************************************************************
 * Contributors: PTC 2016
 *******************************************************************************/
package hudson.scm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.remoting.RoleChecker;
import org.jenkinsci.remoting.RoleSensitive;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.security.Roles;

/**
 * Task to write the change log file on the agent/remote machine
 */
public class IntegrityWriteChangeLogTask implements FileCallable<Boolean>
{
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(IntegritySCM.class.getSimpleName());
  private final TaskListener listener;
  private final String changeLogContent;
  private final String changeLogFilePath;

  /**
   * Constructor for the write change log task
   * 
   * @param listener Task listener for logging
   * @param changeLogContent The change log content to write
   * @param changeLogFilePath The path to the change log file
   */
  public IntegrityWriteChangeLogTask(TaskListener listener, String changeLogContent, String changeLogFilePath)
  {
    this.listener = listener;
    this.changeLogContent = changeLogContent;
    this.changeLogFilePath = changeLogFilePath;
  }

  /**
   * Indicates that this task can run on slaves.
   * 
   * @param checker RoleChecker
   */
  public void checkRoles(RoleChecker checker) throws SecurityException
  {
    checker.check((RoleSensitive) this, Roles.SLAVE);
  }

  /**
   * Invoke the write change log task
   */
  @Override
  public Boolean invoke(File workspaceFile, VirtualChannel channel)
      throws IOException, InterruptedException
  {
    try
    {
      listener.getLogger().println("Writing build change log...");
      
      if (changeLogFilePath != null && changeLogFilePath.length() > 0)
      {
        File changeLogFile = new File(changeLogFilePath);
        
        try (PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(new FileOutputStream(changeLogFile), "UTF-8")))
        {
          writer.println(changeLogContent);
          listener.getLogger()
              .println("Change log successfully generated: " + changeLogFile.getAbsolutePath());
          LOGGER.log(Level.FINE, "Change log written to: " + changeLogFile.getAbsolutePath());
          return true;
        }
      }
      else
      {
        listener.getLogger().println("Change log file path is null or empty!");
        return false;
      }
    } catch (IOException ioex)
    {
      LOGGER.log(Level.SEVERE, "IOException while writing change log", ioex);
      listener.getLogger().println("Failed to write change log: " + ioex.getMessage());
      return false;
    } catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Exception while writing change log", ex);
      listener.getLogger().println("Exception while writing change log: " + ex.getMessage());
      return false;
    }
  }
}

package weka.experiment;

import java.io.File;
import java.io.PrintStream;
import javax.swing.DefaultListModel;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;






























public class RemoteExperimentSubTask
  implements Task, RevisionHandler
{
  private TaskStatusInfo m_result = new TaskStatusInfo();
  
  private Experiment m_experiment;
  
  public RemoteExperimentSubTask()
  {
    m_result.setStatusMessage("Not running.");
    m_result.setExecutionStatus(0);
  }
  



  public void setExperiment(Experiment task)
  {
    m_experiment = task;
  }
  



  public Experiment getExperiment()
  {
    return m_experiment;
  }
  



  public void execute()
  {
    m_result = new TaskStatusInfo();
    m_result.setStatusMessage("Running...");
    String goodResult = "(sub)experiment completed successfully";
    String subTaskType;
    String subTaskType; if (m_experiment.getRunLower() != m_experiment.getRunUpper()) {
      subTaskType = "(dataset " + ((File)m_experiment.getDatasets().elementAt(0)).getName();
    }
    else {
      subTaskType = "(exp run # " + m_experiment.getRunLower();
    }
    try
    {
      System.err.println("Initializing " + subTaskType + ")...");
      m_experiment.initialize();
      System.err.println("Iterating " + subTaskType + ")...");
      
      while (m_experiment.hasMoreIterations()) {
        m_experiment.nextIteration();
      }
      System.err.println("Postprocessing " + subTaskType + ")...");
      m_experiment.postProcess();
    } catch (Exception ex) {
      ex.printStackTrace();
      String badResult = "(sub)experiment " + subTaskType + ") failed : " + ex.toString();
      
      m_result.setExecutionStatus(2);
      

      m_result.setStatusMessage(badResult);
      m_result.setTaskResult("Failed");
      
      return;
    }
    

    m_result.setExecutionStatus(3);
    m_result.setStatusMessage(goodResult + " " + subTaskType + ").");
    m_result.setTaskResult("No errors");
  }
  
  public TaskStatusInfo getTaskStatus()
  {
    return m_result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
}

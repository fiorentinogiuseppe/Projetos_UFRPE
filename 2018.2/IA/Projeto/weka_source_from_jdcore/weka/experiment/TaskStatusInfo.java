package weka.experiment;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;









































public class TaskStatusInfo
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -6129343303703560015L;
  public static final int TO_BE_RUN = 0;
  public static final int PROCESSING = 1;
  public static final int FAILED = 2;
  public static final int FINISHED = 3;
  private int m_ExecutionStatus = 0;
  



  private String m_StatusMessage = "New Task";
  



  private Object m_TaskResult = null;
  

  public TaskStatusInfo() {}
  

  public void setExecutionStatus(int newStatus)
  {
    m_ExecutionStatus = newStatus;
  }
  



  public int getExecutionStatus()
  {
    return m_ExecutionStatus;
  }
  




  public void setStatusMessage(String newMessage)
  {
    m_StatusMessage = newMessage;
  }
  




  public String getStatusMessage()
  {
    return m_StatusMessage;
  }
  





  public void setTaskResult(Object taskResult)
  {
    m_TaskResult = taskResult;
  }
  






  public Object getTaskResult()
  {
    return m_TaskResult;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

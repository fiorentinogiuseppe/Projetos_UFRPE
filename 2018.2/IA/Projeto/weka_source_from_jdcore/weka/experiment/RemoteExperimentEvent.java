package weka.experiment;

import java.io.Serializable;
















































public class RemoteExperimentEvent
  implements Serializable
{
  private static final long serialVersionUID = 7000867987391866451L;
  public boolean m_statusMessage;
  public boolean m_logMessage;
  public String m_messageString;
  public boolean m_experimentFinished;
  
  public RemoteExperimentEvent(boolean status, boolean log, boolean finished, String message)
  {
    m_statusMessage = status;
    m_logMessage = log;
    m_experimentFinished = finished;
    m_messageString = message;
  }
}

package weka.gui.beans;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;
import weka.gui.Logger;




























public class TrainingSetMaker
  extends AbstractTrainingSetProducer
  implements DataSourceListener, TestSetListener, EventConstraints, Serializable
{
  private static final long serialVersionUID = -6152577265471535786L;
  protected boolean m_receivedStopNotification = false;
  
  public TrainingSetMaker() {
    m_visual.loadIcons("weka/gui/beans/icons/TrainingSetMaker.gif", "weka/gui/beans/icons/TrainingSetMaker_animated.gif");
    
    m_visual.setText("TrainingSetMaker");
  }
  





  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  





  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("TrainingSetMaker_GlobalInfo_Text");
  }
  





  public void acceptDataSet(DataSetEvent e)
  {
    m_receivedStopNotification = false;
    TrainingSetEvent tse = new TrainingSetEvent(this, e.getDataSet());
    m_setNumber = 1;
    m_maxSetNumber = 1;
    notifyTrainingSetProduced(tse);
  }
  
  public void acceptTestSet(TestSetEvent e)
  {
    m_receivedStopNotification = false;
    TrainingSetEvent tse = new TrainingSetEvent(this, e.getTestSet());
    m_setNumber = 1;
    m_maxSetNumber = 1;
    notifyTrainingSetProduced(tse);
  }
  


  protected void notifyTrainingSetProduced(TrainingSetEvent tse)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_listeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        if (m_receivedStopNotification) {
          if (m_logger != null) {
            Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("TrainingSetMaker_NotifyTrainingSetProduced_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("TrainingSetMaker_NotifyTrainingSetProduced_LogMessage_Text_Second"));
            








            Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("TrainingSetMaker_NotifyTrainingSetProduced_LogMessage_Text_Third"));
          }
          




          m_receivedStopNotification = false;
          break;
        }
        Messages.getInstance();System.err.println(Messages.getString("TrainingSetMaker_NotifyTrainingSetProduced_Error_Text"));
        
        ((TrainingSetListener)l.elementAt(i)).acceptTrainingSet(tse);
      }
    }
  }
  



  public void stop()
  {
    m_receivedStopNotification = true;
    

    if ((m_listenee instanceof BeanCommon))
    {
      ((BeanCommon)m_listenee).stop();
    }
  }
  






  public boolean isBusy()
  {
    return false;
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (m_listenee == null) {
      return false;
    }
    
    if (((m_listenee instanceof EventConstraints)) && 
      (!((EventConstraints)m_listenee).eventGeneratable("dataSet"))) {
      return false;
    }
    
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}

package weka.gui.beans;

import java.io.Serializable;
import java.util.Vector;
import weka.gui.Logger;





























public class TestSetMaker
  extends AbstractTestSetProducer
  implements DataSourceListener, TrainingSetListener, EventConstraints, Serializable
{
  private static final long serialVersionUID = -8473882857628061841L;
  protected boolean m_receivedStopNotification = false;
  
  public TestSetMaker() {
    m_visual.loadIcons("weka/gui/beans/icons/TestSetMaker.gif", "weka/gui/beans/icons/TestSetMaker_animated.gif");
    
    m_visual.setText("TestSetMaker");
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
    Messages.getInstance();return Messages.getString("TestSetMaker_GlobalInfo_Text");
  }
  





  public void acceptDataSet(DataSetEvent e)
  {
    m_receivedStopNotification = false;
    TestSetEvent tse = new TestSetEvent(this, e.getDataSet());
    m_setNumber = 1;
    m_maxSetNumber = 1;
    notifyTestSetProduced(tse);
  }
  
  public void acceptTrainingSet(TrainingSetEvent e)
  {
    m_receivedStopNotification = false;
    TestSetEvent tse = new TestSetEvent(this, e.getTrainingSet());
    m_setNumber = 1;
    m_maxSetNumber = 1;
    notifyTestSetProduced(tse);
  }
  


  protected void notifyTestSetProduced(TestSetEvent tse)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_listeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        if (m_receivedStopNotification) {
          if (m_logger != null) {
            Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("TestSetMaker_NotifyTestSetProduced_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("TestSetMaker_NotifyTestSetProduced_LogMessage_Text_Second"));
            






            Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("TestSetMaker_NotifyTestSetProduced_LogMessage_Text_Third"));
          }
          




          m_receivedStopNotification = false;
          break;
        }
        ((TestSetListener)l.elementAt(i)).acceptTestSet(tse);
      }
    }
  }
  

  public void stop()
  {
    m_receivedStopNotification = true;
    

    if ((m_listenee instanceof BeanCommon)) {
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

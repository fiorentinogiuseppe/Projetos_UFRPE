package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JPanel;
import weka.gui.Logger;







































public abstract class AbstractTrainingSetProducer
  extends JPanel
  implements TrainingSetProducer, Visible, BeanCommon, Serializable
{
  private static final long serialVersionUID = -7842746199524591125L;
  protected Vector m_listeners = new Vector();
  
  protected BeanVisual m_visual = new BeanVisual("AbstractTraingSetProducer", "weka/gui/beans/icons/DefaultTrainTest.gif", "weka/gui/beans/icons/DefaultTrainTest_animated.gif");
  







  protected Object m_listenee = null;
  
  protected transient Logger m_logger = null;
  


  public AbstractTrainingSetProducer()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  




  public synchronized void addTrainingSetListener(TrainingSetListener tsl)
  {
    m_listeners.addElement(tsl);
  }
  




  public synchronized void removeTrainingSetListener(TrainingSetListener tsl)
  {
    m_listeners.removeElement(tsl);
  }
  




  public void setVisual(BeanVisual newVisual)
  {
    m_visual = newVisual;
  }
  




  public BeanVisual getVisual()
  {
    return m_visual;
  }
  


  public void useDefaultVisual()
  {
    m_visual.loadIcons("weka/gui/beans/icons/DefaultTrainTest.gif", "weka/gui/beans/icons/DefaultTrainTest_animated.gif");
  }
  








  public boolean connectionAllowed(String eventName)
  {
    return m_listenee == null;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  








  public synchronized void connectionNotification(String eventName, Object source)
  {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }
  








  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    if (m_listenee == source) {
      m_listenee = null;
    }
  }
  




  public void setLog(Logger logger)
  {
    m_logger = logger;
  }
  
  public abstract void stop();
}

package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JPanel;
import weka.gui.Logger;





































public abstract class AbstractTrainAndTestSetProducer
  extends JPanel
  implements Visible, TrainingSetProducer, TestSetProducer, BeanCommon, Serializable, DataSourceListener
{
  private static final long serialVersionUID = -1809339823613492037L;
  protected Vector m_trainingListeners = new Vector();
  



  protected Vector m_testListeners = new Vector();
  
  protected BeanVisual m_visual = new BeanVisual("AbstractTrainingSetProducer", "weka/gui/beans/icons/DefaultTrainTest.gif", "weka/gui/beans/icons/DefaultTrainTest_animated.gif");
  






  protected Object m_listenee = null;
  
  protected transient Logger m_logger = null;
  


  public AbstractTrainAndTestSetProducer()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  





  public abstract void acceptDataSet(DataSetEvent paramDataSetEvent);
  




  public synchronized void addTrainingSetListener(TrainingSetListener tsl)
  {
    m_trainingListeners.addElement(tsl);
  }
  




  public synchronized void removeTrainingSetListener(TrainingSetListener tsl)
  {
    m_trainingListeners.removeElement(tsl);
  }
  




  public synchronized void addTestSetListener(TestSetListener tsl)
  {
    m_testListeners.addElement(tsl);
  }
  




  public synchronized void removeTestSetListener(TestSetListener tsl)
  {
    m_testListeners.removeElement(tsl);
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

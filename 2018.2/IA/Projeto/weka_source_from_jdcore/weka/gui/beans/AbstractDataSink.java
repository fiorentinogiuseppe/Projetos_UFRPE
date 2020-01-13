package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.Serializable;
import javax.swing.JPanel;
import weka.gui.Logger;









































public abstract class AbstractDataSink
  extends JPanel
  implements DataSink, BeanCommon, Visible, DataSourceListener, TrainingSetListener, TestSetListener, InstanceListener, ThresholdDataListener, Serializable
{
  private static final long serialVersionUID = 3956528599473814287L;
  protected BeanVisual m_visual = new BeanVisual("AbstractDataSink", "weka/gui/beans/icons/DefaultDataSink.gif", "weka/gui/beans/icons/DefaultDataSink_animated.gif");
  









  protected Object m_listenee = null;
  
  protected transient Logger m_logger = null;
  
  public AbstractDataSink() {
    useDefaultVisual();
    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  





  public abstract void acceptTrainingSet(TrainingSetEvent paramTrainingSetEvent);
  





  public abstract void acceptTestSet(TestSetEvent paramTestSetEvent);
  





  public abstract void acceptDataSet(DataSetEvent paramDataSetEvent);
  





  public abstract void acceptDataSet(ThresholdDataEvent paramThresholdDataEvent);
  





  public abstract void acceptInstance(InstanceEvent paramInstanceEvent);
  




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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultDataSink.gif", "weka/gui/beans/icons/DefaultDataSink_animated.gif");
  }
  








  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  







  public boolean connectionAllowed(String eventName)
  {
    return m_listenee == null;
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

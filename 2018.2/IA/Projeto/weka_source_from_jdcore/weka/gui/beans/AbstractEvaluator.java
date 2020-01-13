package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.Serializable;
import javax.swing.JPanel;
import weka.gui.Logger;









































public abstract class AbstractEvaluator
  extends JPanel
  implements Visible, BeanCommon, Serializable
{
  private static final long serialVersionUID = 3983303541814121632L;
  protected BeanVisual m_visual = new BeanVisual("AbstractEvaluator", "weka/gui/beans/icons/DefaultEvaluator.gif", "weka/gui/beans/icons/DefaultEvaluator_animated.gif");
  



  protected Object m_listenee = null;
  
  protected transient Logger m_logger = null;
  


  public AbstractEvaluator()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultEvaluator.gif", "weka/gui/beans/icons/DefaultEvaluator_animated.gif");
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

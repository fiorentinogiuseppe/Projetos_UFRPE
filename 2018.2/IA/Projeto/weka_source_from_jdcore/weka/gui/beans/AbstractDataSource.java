package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JPanel;











































public abstract class AbstractDataSource
  extends JPanel
  implements DataSource, Visible, Serializable, BeanContextChild
{
  private static final long serialVersionUID = -4127257701890044793L;
  protected boolean m_design;
  protected transient BeanContext m_beanContext = null;
  



  protected BeanContextChildSupport m_bcSupport = new BeanContextChildSupport(this);
  




  protected BeanVisual m_visual = new BeanVisual("AbstractDataSource", "weka/gui/beans/icons/DefaultDataSource.gif", "weka/gui/beans/icons/DefaultDataSource_animated.gif");
  




  protected Vector m_listeners;
  





  public AbstractDataSource()
  {
    useDefaultVisual();
    setLayout(new BorderLayout());
    add(m_visual, "Center");
    m_listeners = new Vector();
  }
  




  public synchronized void addDataSourceListener(DataSourceListener dsl)
  {
    m_listeners.addElement(dsl);
  }
  




  public synchronized void removeDataSourceListener(DataSourceListener dsl)
  {
    m_listeners.remove(dsl);
  }
  




  public synchronized void addInstanceListener(InstanceListener dsl)
  {
    m_listeners.addElement(dsl);
  }
  




  public synchronized void removeInstanceListener(InstanceListener dsl)
  {
    m_listeners.remove(dsl);
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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultDataSource.gif", "weka/gui/beans/icons/DefaultDataSource_animated.gif");
  }
  





  public void setBeanContext(BeanContext bc)
  {
    m_beanContext = bc;
    m_design = m_beanContext.isDesignTime();
  }
  




  public BeanContext getBeanContext()
  {
    return m_beanContext;
  }
  






  public void addPropertyChangeListener(String name, PropertyChangeListener pcl)
  {
    m_bcSupport.addPropertyChangeListener(name, pcl);
  }
  






  public void removePropertyChangeListener(String name, PropertyChangeListener pcl)
  {
    m_bcSupport.removePropertyChangeListener(name, pcl);
  }
  






  public void addVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.addVetoableChangeListener(name, vcl);
  }
  






  public void removeVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.removeVetoableChangeListener(name, vcl);
  }
}

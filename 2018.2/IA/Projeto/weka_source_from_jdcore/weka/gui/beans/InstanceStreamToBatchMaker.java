package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.util.ArrayList;
import javax.swing.JPanel;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.Logger;




































public class InstanceStreamToBatchMaker
  extends JPanel
  implements BeanCommon, Visible, InstanceListener, EventConstraints, DataSource
{
  private static final long serialVersionUID = -7037141087208627799L;
  protected BeanVisual m_visual = new BeanVisual("InstanceStreamToBatchMaker", "weka/gui/beans/icons/InstanceStreamToBatchMaker.gif", "weka/gui/beans/icons/InstanceStreamToBatchMaker_animated.gif");
  



  private transient Logger m_log;
  



  private Object m_listenee;
  



  private ArrayList<DataSourceListener> m_dataListeners = new ArrayList();
  

  private ArrayList<Instance> m_batch;
  

  private Instances m_structure;
  

  public InstanceStreamToBatchMaker()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  




  public void acceptInstance(InstanceEvent e)
  {
    if (e.getStatus() == 0) {
      m_batch = new ArrayList();
      m_structure = e.getStructure();
      

      if (m_log != null) {
        Messages.getInstance();m_log.logMessage(Messages.getString("InstanceStreamToBatchMaker_AcceptInstance_LogMessage_Text_First"));
      }
      DataSetEvent dse = new DataSetEvent(this, m_structure);
      notifyDataListeners(dse);
    } else if (e.getStatus() == 1) {
      m_batch.add(e.getInstance());

    }
    else
    {
      m_batch.add(e.getInstance());
      

      Instances dataSet = new Instances(m_structure, m_batch.size());
      for (Instance i : m_batch) {
        dataSet.add(i);
      }
      dataSet.compactify();
      

      m_batch = null;
      
      if (m_log != null) {
        Messages.getInstance();m_log.logMessage(Messages.getString("InstanceStreamToBatchMaker_AcceptInstance_LogMessage_Text_Second"));
      }
      

      DataSetEvent dse = new DataSetEvent(this, dataSet);
      notifyDataListeners(dse);
    }
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  






  public boolean connectionAllowed(String eventName)
  {
    if ((m_listenee != null) || (!eventName.equals("instance"))) {
      return false;
    }
    return true;
  }
  







  public void connectionNotification(String eventName, Object source)
  {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }
  







  public void disconnectionNotification(String eventName, Object source)
  {
    m_listenee = null;
  }
  






  public boolean eventGeneratable(String eventName)
  {
    if (!eventName.equals("dataSet")) {
      return false;
    }
    
    if (m_listenee == null) {
      return false;
    }
    
    if (((m_listenee instanceof EventConstraints)) && 
      (!((EventConstraints)m_listenee).eventGeneratable("instance"))) {
      return false;
    }
    

    return true;
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public void setLog(Logger logger)
  {
    m_log = logger;
  }
  





  public boolean isBusy()
  {
    return false;
  }
  




  public void stop() {}
  



  public BeanVisual getVisual()
  {
    return m_visual;
  }
  




  public void setVisual(BeanVisual newVisual)
  {
    m_visual = newVisual;
  }
  


  public void useDefaultVisual()
  {
    m_visual.loadIcons("weka/gui/beans/icons/InstanceStreamToBatchMaker.gif", "weka/gui/beans/icons/InstanceStreamToBatchMaker_animated.gif");
  }
  



  protected void notifyDataListeners(DataSetEvent tse)
  {
    ArrayList<DataSourceListener> l;
    

    synchronized (this) {
      l = (ArrayList)m_dataListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((DataSourceListener)l.get(i)).acceptDataSet(tse);
      }
    }
  }
  
  public synchronized void addDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.add(tsl);
    
    if (m_structure != null) {
      DataSetEvent e = new DataSetEvent(this, m_structure);
      tsl.acceptDataSet(e);
    }
  }
  
  public synchronized void removeDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.remove(tsl);
  }
  
  public synchronized void addInstanceListener(InstanceListener il) {}
  
  public synchronized void removeInstanceListener(InstanceListener il) {}
}

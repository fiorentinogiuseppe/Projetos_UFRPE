package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JPanel;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.AllFilter;
import weka.filters.StreamableFilter;
import weka.filters.SupervisedFilter;
import weka.gui.Logger;





































public class Filter
  extends JPanel
  implements BeanCommon, Visible, WekaWrapper, Serializable, UserRequestAcceptor, TrainingSetListener, TestSetListener, TrainingSetProducer, TestSetProducer, DataSource, DataSourceListener, InstanceListener, EventConstraints
{
  private static final long serialVersionUID = 8249759470189439321L;
  protected BeanVisual m_visual = new BeanVisual("Filter", "weka/gui/beans/icons/DefaultFilter.gif", "weka/gui/beans/icons/DefaultFilter_animated.gif");
  



  private static int IDLE = 0;
  private static int FILTERING_TRAINING = 1;
  private static int FILTERING_TEST = 2;
  private int m_state = IDLE;
  
  protected transient Thread m_filterThread = null;
  


  private transient Instances m_trainingSet;
  

  private transient Instances m_testingSet;
  

  protected String m_globalInfo;
  

  private Hashtable m_listenees = new Hashtable();
  



  private Vector m_trainingListeners = new Vector();
  



  private Vector m_testListeners = new Vector();
  



  private Vector m_instanceListeners = new Vector();
  



  private Vector m_dataListeners = new Vector();
  



  private weka.filters.Filter m_Filter = new AllFilter();
  



  private InstanceEvent m_ie = new InstanceEvent(this);
  



  private transient Logger m_log = null;
  



  private transient int m_instanceCount;
  




  public String globalInfo()
  {
    return m_globalInfo;
  }
  
  public Filter() {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
    setFilter(m_Filter);
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public void setFilter(weka.filters.Filter c)
  {
    boolean loadImages = true;
    if (c.getClass().getName().compareTo(m_Filter.getClass().getName()) == 0)
    {
      loadImages = false;
    }
    m_Filter = c;
    String filterName = c.getClass().toString();
    filterName = filterName.substring(filterName.indexOf('.') + 1, filterName.length());
    

    if (loadImages) {
      if ((m_Filter instanceof Visible)) {
        m_visual = ((Visible)m_Filter).getVisual();
      }
      else if (!m_visual.loadIcons("weka/gui/beans/icons/" + filterName + ".gif", "weka/gui/beans/icons/" + filterName + "_animated.gif"))
      {
        useDefaultVisual();
      }
    }
    
    m_visual.setText(filterName.substring(filterName.lastIndexOf('.') + 1, filterName.length()));
    

    if (((m_Filter instanceof LogWriter)) && (m_log != null)) {
      ((LogWriter)m_Filter).setLog(m_log);
    }
    
    if ((!(m_Filter instanceof StreamableFilter)) && (m_listenees.containsKey("instance")))
    {
      if (m_log != null) {
        m_log.logMessage("[Filter] " + statusMessagePrefix() + " WARNING : " + m_Filter.getClass().getName() + " is not an incremental filter");
        


        m_log.statusMessage(statusMessagePrefix() + "WARNING: Not an incremental filter.");
      }
    }
    


    m_globalInfo = KnowledgeFlowApp.getGlobalInfo(m_Filter);
  }
  
  public weka.filters.Filter getFilter() {
    return m_Filter;
  }
  






  public void setWrappedAlgorithm(Object algorithm)
  {
    if (!(algorithm instanceof weka.filters.Filter)) {
      Messages.getInstance();throw new IllegalArgumentException(algorithm.getClass() + Messages.getString("Filter_SetWrappedAlgorithm_IllegalArgumentException_Text"));
    }
    setFilter((weka.filters.Filter)algorithm);
  }
  




  public Object getWrappedAlgorithm()
  {
    return getFilter();
  }
  




  public void acceptTrainingSet(TrainingSetEvent e)
  {
    processTrainingOrDataSourceEvents(e);
  }
  
  private boolean m_structurePassedOn = false;
  




  public void acceptInstance(InstanceEvent e)
  {
    if (m_filterThread != null) {
      Messages.getInstance();Messages.getInstance();String messg = Messages.getString("Filter_AcceptInstance_Mess_Text_First") + statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_Mess_Text_Second");
      
      if (m_log != null) {
        m_log.logMessage(messg);
        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_First"));
      }
      else {
        System.err.println(messg);
      }
      return;
    }
    if (!(m_Filter instanceof StreamableFilter)) {
      stop();
      if (m_log != null) {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Filter_AcceptInstance_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_LogMessage_Text_Second") + m_Filter.getClass().getName() + Messages.getString("Filter_AcceptInstance_LogMessage_Text_Third"));
        

        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Second"));
      }
      
      return;
    }
    if (e.getStatus() == 0) {
      try {
        m_instanceCount = 0;
        

        Instances dataset = e.getStructure();
        if ((m_Filter instanceof SupervisedFilter))
        {
          if (dataset.classIndex() < 0) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
          }
        }
        
        m_Filter.setInputFormat(dataset);
        


        m_structurePassedOn = false;
        try {
          if (m_Filter.isOutputFormatDefined())
          {

            m_ie.setStructure(m_Filter.getOutputFormat());
            notifyInstanceListeners(m_ie);
            m_structurePassedOn = true;
          }
        } catch (Exception ex) {
          stop();
          if (m_log != null) {
            Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Filter_AcceptInstance_LogMessage_Text_Fourth") + statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_LogMessage_Text_Fifth") + ex.getMessage());
            

            Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Third"));
          }
          else {
            Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("Filter_AcceptInstance_Error_Text_First") + statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_Error_Text_Second"));
          }
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
      return;
    }
    
    if (e.getStatus() == 2)
    {
      try {
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Fourth"));
        }
        
        if (m_Filter.input(e.getInstance())) {
          Instance filteredInstance = m_Filter.output();
          if (filteredInstance != null) {
            if (!m_structurePassedOn)
            {
              m_ie.setStructure(new Instances(filteredInstance.dataset(), 0));
              notifyInstanceListeners(m_ie);
              m_structurePassedOn = true;
            }
            
            m_ie.setInstance(filteredInstance);
            



            if ((m_Filter.batchFinished()) && (m_Filter.numPendingOutput() > 0)) {
              m_ie.setStatus(1);
            } else {
              m_ie.setStatus(e.getStatus());
            }
            notifyInstanceListeners(m_ie);
          }
        }
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Fourth_Alpha"));
        }
      } catch (Exception ex) {
        stop();
        if (m_log != null) {
          Messages.getInstance();m_log.logMessage(Messages.getString("Filter_AcceptInstance_LogMessage_Text_Sixth") + statusMessagePrefix() + ex.getMessage());
          
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Fifth"));
        }
        
        ex.printStackTrace();
      }
      
      try
      {
        if ((m_Filter.batchFinished()) && (m_Filter.numPendingOutput() > 0)) {
          if (m_log != null) {
            Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Sixth"));
          }
          
          Instance filteredInstance = m_Filter.output();
          if (filteredInstance != null) {
            if (!m_structurePassedOn)
            {
              m_ie.setStructure(new Instances(filteredInstance.dataset(), 0));
              notifyInstanceListeners(m_ie);
              m_structurePassedOn = true;
            }
            
            m_ie.setInstance(filteredInstance);
            

            m_ie.setStatus(1);
            notifyInstanceListeners(m_ie);
          }
          while (m_Filter.numPendingOutput() > 0) {
            filteredInstance = m_Filter.output();
            m_ie.setInstance(filteredInstance);
            
            if (m_Filter.numPendingOutput() == 0) {
              m_ie.setStatus(2);
            } else {
              m_ie.setStatus(1);
            }
            notifyInstanceListeners(m_ie);
          }
          if (m_log != null) {
            Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Seventh"));
          }
        }
      } catch (Exception ex) {
        stop();
        if (m_log != null) {
          Messages.getInstance();m_log.logMessage(Messages.getString("Filter_AcceptInstance_LogMessage_Text_Seventh") + statusMessagePrefix() + ex.toString());
          

          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Eighth"));
        }
        
        ex.printStackTrace();
      }
    }
    else {
      try {
        if (!m_Filter.input(e.getInstance()))
        {






          return;
        }
        

        Instance filteredInstance = m_Filter.output();
        if (filteredInstance == null) {
          return;
        }
        m_instanceCount += 1;
        
        if (!m_structurePassedOn)
        {
          m_ie.setStructure(new Instances(filteredInstance.dataset(), 0));
          notifyInstanceListeners(m_ie);
          m_structurePassedOn = true;
        }
        
        m_ie.setInstance(filteredInstance);
        m_ie.setStatus(e.getStatus());
        
        if ((m_log != null) && (m_instanceCount % 10000 == 0)) {
          Messages.getInstance();Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Nineth") + m_instanceCount + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Tenth"));
        }
        
        notifyInstanceListeners(m_ie);
      } catch (Exception ex) {
        stop();
        if (m_log != null) {
          Messages.getInstance();m_log.logMessage(Messages.getString("Filter_AcceptInstance_LogMessage_Text_Eighth") + statusMessagePrefix() + ex.toString());
          
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Filter_AcceptInstance_StatusMessage_Text_Eleventh"));
        }
        
        ex.printStackTrace();
      }
    }
  }
  
  private void processTrainingOrDataSourceEvents(final EventObject e) {
    boolean structureOnly = false;
    if ((e instanceof DataSetEvent)) {
      structureOnly = ((DataSetEvent)e).isStructureOnly();
      if (structureOnly) {
        notifyDataOrTrainingListeners(e);
      }
    }
    if ((e instanceof TrainingSetEvent)) {
      structureOnly = ((TrainingSetEvent)e).isStructureOnly();
      if (structureOnly) {
        notifyDataOrTrainingListeners(e);
      }
    }
    if ((structureOnly) && (!(m_Filter instanceof StreamableFilter))) {
      return;
    }
    
    if (m_filterThread == null) {
      try {
        if (m_state == IDLE) {
          synchronized (this) {
            m_state = FILTERING_TRAINING;
          }
          m_trainingSet = ((e instanceof TrainingSetEvent) ? ((TrainingSetEvent)e).getTrainingSet() : ((DataSetEvent)e).getDataSet());
          



          m_filterThread = new Thread() {
            public void run() {
              try {
                if (m_trainingSet != null) {
                  m_visual.setAnimated();
                  
                  if (m_log != null) {
                    Messages.getInstance();Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_StatusMessage_Text_First") + m_trainingSet.relationName() + Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_StatusMessage_Text_Second"));
                  }
                  

                  m_Filter.setInputFormat(m_trainingSet);
                  Instances filteredData = weka.filters.Filter.useFilter(m_trainingSet, m_Filter);
                  

                  m_visual.setStatic();
                  EventObject ne;
                  if ((e instanceof TrainingSetEvent)) {
                    EventObject ne = new TrainingSetEvent(Filter.this, filteredData);
                    
                    m_setNumber = em_setNumber;
                    
                    m_maxSetNumber = em_maxSetNumber;
                  }
                  else {
                    ne = new DataSetEvent(Filter.this, filteredData);
                  }
                  

                  Filter.this.notifyDataOrTrainingListeners(ne);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
                if (m_log != null) {
                  Messages.getInstance();m_log.logMessage(Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_LogMessage_Text_First") + Filter.this.statusMessagePrefix() + ex.getMessage());
                  
                  Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_StatusMessage_Text_Third"));
                }
                

                stop();
              }
              finally {
                m_visual.setStatic();
                m_state = Filter.IDLE;
                if (isInterrupted()) {
                  m_trainingSet = null;
                  if (m_log != null) {
                    Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_LogMessage_Text_Second") + Filter.this.statusMessagePrefix() + Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_LogMessage_Text_Third"));
                    
                    Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_StatusMessage_Text_Fifth"));
                  }
                  
                }
                else if (m_log != null) {
                  Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_ProcessTrainingOrDataSourceEvents_StatusMessage_Text_Sixth"));
                }
                
                Filter.this.block(false);
                m_filterThread = null;
              }
            }
          };
          m_filterThread.setPriority(1);
          m_filterThread.start();
          block(true);
          m_filterThread = null;
          m_state = IDLE;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  




  public void acceptTestSet(final TestSetEvent e)
  {
    if (e.isStructureOnly())
      notifyTestListeners(e);
    if ((m_trainingSet != null) && (m_trainingSet.equalHeaders(e.getTestSet())) && (m_filterThread == null))
    {
      try
      {
        if (m_state == IDLE) {
          m_state = FILTERING_TEST;
        }
        m_testingSet = e.getTestSet();
        
        m_filterThread = new Thread() {
          public void run() {
            try {
              if (m_testingSet != null) {
                m_visual.setAnimated();
                
                if (m_log != null) {
                  Messages.getInstance();Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_AcceptTestSet_StatusMessage_Text_First") + m_testingSet.relationName() + Messages.getString("Filter_AcceptTestSet_StatusMessage_Text_Second"));
                }
                

                Instances filteredTest = weka.filters.Filter.useFilter(m_testingSet, m_Filter);
                

                m_visual.setStatic();
                TestSetEvent ne = new TestSetEvent(Filter.this, filteredTest);
                

                m_setNumber = em_setNumber;
                m_maxSetNumber = em_maxSetNumber;
                Filter.this.notifyTestListeners(ne);
              }
            } catch (Exception ex) {
              ex.printStackTrace();
              if (m_log != null) {
                Messages.getInstance();m_log.logMessage(Messages.getString("Filter_AcceptTestSet_LogMessage_Text_First") + Filter.this.statusMessagePrefix() + ex.getMessage());
                
                Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_AcceptTestSet_StatusMessage_Text_Third"));
              }
              
              stop();
            }
            finally {
              m_visual.setStatic();
              m_state = Filter.IDLE;
              if (isInterrupted()) {
                m_trainingSet = null;
                if (m_log != null) {
                  Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Filter_AcceptTestSet_LogMessage_Text_Second") + Filter.this.statusMessagePrefix() + Messages.getString("Filter_AcceptTestSet_LogMessage_Text_Third"));
                  
                  Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_AcceptTestSet_StatusMessage_Text_Fourth"));
                }
                

              }
              else if (m_log != null) {
                Messages.getInstance();m_log.statusMessage(Filter.this.statusMessagePrefix() + Messages.getString("Filter_AcceptTestSet_StatusMessage_Text_Fifth"));
              }
              
              Filter.this.block(false);
              m_filterThread = null;
            }
          }
        };
        m_filterThread.setPriority(1);
        m_filterThread.start();
        block(true);
        m_filterThread = null;
        m_state = IDLE;
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  




  public void acceptDataSet(DataSetEvent e)
  {
    processTrainingOrDataSourceEvents(e);
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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultFilter.gif", "weka/gui/beans/icons/DefaultFilter_animated.gif");
  }
  





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
  




  public synchronized void addDataSourceListener(DataSourceListener dsl)
  {
    m_dataListeners.addElement(dsl);
  }
  




  public synchronized void removeDataSourceListener(DataSourceListener dsl)
  {
    m_dataListeners.remove(dsl);
  }
  




  public synchronized void addInstanceListener(InstanceListener tsl)
  {
    m_instanceListeners.addElement(tsl);
  }
  




  public synchronized void removeInstanceListener(InstanceListener tsl)
  {
    m_instanceListeners.removeElement(tsl);
  }
  
  private void notifyDataOrTrainingListeners(EventObject ce) {
    Vector l;
    synchronized (this) {
      l = (ce instanceof TrainingSetEvent) ? (Vector)m_trainingListeners.clone() : (Vector)m_dataListeners.clone();
    }
    

    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        if ((ce instanceof TrainingSetEvent)) {
          ((TrainingSetListener)l.elementAt(i)).acceptTrainingSet((TrainingSetEvent)ce);
        }
        else {
          ((DataSourceListener)l.elementAt(i)).acceptDataSet((DataSetEvent)ce);
        }
      }
    }
  }
  
  private void notifyTestListeners(TestSetEvent ce) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_testListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((TestSetListener)l.elementAt(i)).acceptTestSet(ce);
      }
    }
  }
  
  protected void notifyInstanceListeners(InstanceEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_instanceListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++)
      {

        ((InstanceListener)l.elementAt(i)).acceptInstance(tse);
      }
    }
  }
  








  public boolean connectionAllowed(String eventName)
  {
    if (m_listenees.containsKey(eventName)) {
      return false;
    }
    










    if ((m_listenees.containsKey("dataSet")) && ((eventName.compareTo("trainingSet") == 0) || (eventName.compareTo("testSet") == 0) || (eventName.compareTo("instance") == 0)))
    {


      return false;
    }
    
    if (((m_listenees.containsKey("trainingSet")) || (m_listenees.containsKey("testSet"))) && ((eventName.compareTo("dataSet") == 0) || (eventName.compareTo("instance") == 0)))
    {


      return false;
    }
    
    if ((m_listenees.containsKey("instance")) && ((eventName.compareTo("trainingSet") == 0) || (eventName.compareTo("testSet") == 0) || (eventName.compareTo("dataSet") == 0)))
    {


      return false;
    }
    


    if ((eventName.compareTo("instance") == 0) && (!(m_Filter instanceof StreamableFilter)))
    {
      return false;
    }
    return true;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  








  public synchronized void connectionNotification(String eventName, Object source)
  {
    if (connectionAllowed(eventName)) {
      m_listenees.put(eventName, source);
      if ((m_Filter instanceof ConnectionNotificationConsumer)) {
        ((ConnectionNotificationConsumer)m_Filter).connectionNotification(eventName, source);
      }
    }
  }
  









  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    if ((m_Filter instanceof ConnectionNotificationConsumer)) {
      ((ConnectionNotificationConsumer)m_Filter).disconnectionNotification(eventName, source);
    }
    
    m_listenees.remove(eventName);
  }
  







  private synchronized void block(boolean tf)
  {
    if (tf) {
      try
      {
        if ((m_filterThread.isAlive()) && (m_state != IDLE)) {
          wait();
        }
      }
      catch (InterruptedException ex) {}
    } else {
      notifyAll();
    }
  }
  



  public void stop()
  {
    Enumeration en = m_listenees.keys();
    while (en.hasMoreElements()) {
      Object tempO = m_listenees.get(en.nextElement());
      if ((tempO instanceof BeanCommon)) {
        ((BeanCommon)tempO).stop();
      }
    }
    

    if (m_filterThread != null) {
      m_filterThread.interrupt();
      m_filterThread.stop();
      m_filterThread = null;
      m_visual.setStatic();
    }
  }
  





  public boolean isBusy()
  {
    return m_filterThread != null;
  }
  




  public void setLog(Logger logger)
  {
    m_log = logger;
    
    if ((m_Filter != null) && ((m_Filter instanceof LogWriter))) {
      ((LogWriter)m_Filter).setLog(m_log);
    }
  }
  




  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_filterThread != null) {
      newVector.addElement("Stop");
    }
    return newVector.elements();
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Stop") == 0) {
      stop();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("Filter_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  











  public boolean eventGeneratable(String eventName)
  {
    if (!m_listenees.containsKey(eventName)) {
      return false;
    }
    Object source = m_listenees.get(eventName);
    if (((source instanceof EventConstraints)) && 
      (!((EventConstraints)source).eventGeneratable(eventName))) {
      return false;
    }
    
    if ((eventName.compareTo("instance") == 0) && 
      (!(m_Filter instanceof StreamableFilter))) {
      return false;
    }
    
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|" + (((m_Filter instanceof OptionHandler)) && (Utils.joinOptions(((OptionHandler)m_Filter).getOptions()).length() > 0) ? Utils.joinOptions(((OptionHandler)m_Filter).getOptions()) + "|" : "");
  }
}

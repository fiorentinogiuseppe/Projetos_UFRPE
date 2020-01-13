package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.EventSetDescriptor;
import java.beans.beancontext.BeanContext;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ArffLoader;
import weka.core.converters.BatchConverter;
import weka.core.converters.DatabaseLoader;
import weka.core.converters.FileSourcedConverter;
import weka.core.converters.IncrementalConverter;
import weka.gui.Logger;
















































public class Loader
  extends AbstractDataSource
  implements Startable, WekaWrapper, EventConstraints, BeanCommon, EnvironmentHandler, StructureProducer
{
  private static final long serialVersionUID = 1993738191961163027L;
  private transient Instances m_dataSet;
  private transient Instances m_dataFormat;
  protected String m_globalInfo;
  private LoadThread m_ioThread;
  private static int IDLE = 0;
  private static int BATCH_LOADING = 1;
  private static int INCREMENTAL_LOADING = 2;
  private int m_state = IDLE;
  



  private weka.core.converters.Loader m_Loader = new ArffLoader();
  
  private InstanceEvent m_ie = new InstanceEvent(this);
  



  private int m_instanceEventTargets = 0;
  private int m_dataSetEventTargets = 0;
  

  private boolean m_dbSet = false;
  



  protected transient Logger m_log;
  



  protected transient Environment m_env;
  



  protected boolean m_stopped = false;
  
  private class LoadThread extends Thread {
    private DataSource m_DP;
    
    public LoadThread(DataSource dp) {
      m_DP = dp;
    }
    
    public void run() {
      try {
        m_visual.setAnimated();
        

        boolean instanceGeneration = true;
        






        if (m_dataSetEventTargets > 0) {
          instanceGeneration = false;
          m_state = Loader.BATCH_LOADING;
        }
        

        if (((m_Loader instanceof EnvironmentHandler)) && (m_env != null)) {
          ((EnvironmentHandler)m_Loader).setEnvironment(m_env);
        }
        
        String msg = Loader.this.statusMessagePrefix();
        if ((m_Loader instanceof FileSourcedConverter)) {
          Messages.getInstance();msg = msg + Messages.getString("Loader_LoadThread_Msg_Text_First") + ((FileSourcedConverter)m_Loader).retrieveFile().getName();
        } else {
          Messages.getInstance();msg = msg + Messages.getString("Loader_LoadThread_Msg_Text_Second");
        }
        if (m_log != null) {
          m_log.statusMessage(msg);
        }
        
        if (instanceGeneration) {
          m_state = Loader.INCREMENTAL_LOADING;
          
          Instance nextInstance = null;
          
          Instances structure = null;
          try {
            m_Loader.reset();
            m_Loader.setRetrieval(2);
            
            structure = m_Loader.getStructure();
            notifyStructureAvailable(structure);
          } catch (IOException e) {
            if (m_log != null) {
              Messages.getInstance();m_log.statusMessage(Loader.this.statusMessagePrefix() + Messages.getString("Loader_LoadThread_StatusMessage_Text_First"));
              
              Messages.getInstance();m_log.logMessage(Messages.getString("Loader_LoadThread_StatusMessage_Text_Second") + Loader.this.statusMessagePrefix() + " " + e.getMessage());
            }
            
            e.printStackTrace();
          }
          try {
            nextInstance = m_Loader.getNextInstance(structure);
          } catch (IOException e) {
            if (m_log != null) {
              Messages.getInstance();m_log.statusMessage(Loader.this.statusMessagePrefix() + Messages.getString("Loader_LoadThread_StatusMessage_Text_Third"));
              
              Messages.getInstance();m_log.logMessage(Messages.getString("Loader_LoadThread_StatusMessage_Text_Fourth") + Loader.this.statusMessagePrefix() + " " + e.getMessage());
            }
            
            e.printStackTrace();
          }
          int z = 0;
          for (; (nextInstance != null) && 
                (!m_stopped); 
              

























              m_log.statusMessage(Loader.this.statusMessagePrefix() + Messages.getString("Loader_LoadThread_StatusMessage_Text_Fifth") + z + Messages.getString("Loader_LoadThread_StatusMessage_Text_Sixth")))
          {
            label530:
            nextInstance.setDataset(structure);
            








            m_ie.setStatus(1);
            
            m_ie.setInstance(nextInstance);
            

            nextInstance = m_Loader.getNextInstance(structure);
            if (nextInstance == null) {
              m_ie.setStatus(2);
            }
            notifyInstanceLoaded(m_ie);
            z++;
            if ((z % 10000 != 0) || 
            
              (m_log == null)) break label530;
            Messages.getInstance();Messages.getInstance();
          }
          


          m_visual.setStatic();
        }
        else {
          m_Loader.reset();
          m_Loader.setRetrieval(1);
          m_dataSet = m_Loader.getDataSet();
          m_visual.setStatic();
          if (m_log != null) {
            Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Loader_LoadThread_LogMessage_Text_First") + Loader.this.statusMessagePrefix() + Messages.getString("Loader_LoadThread_LogMessage_Text_Second") + m_dataSet.relationName());
          }
          

          notifyDataSetLoaded(new DataSetEvent(m_DP, m_dataSet));
        }
      } catch (Exception ex) {
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(Loader.this.statusMessagePrefix() + Messages.getString("Loader_LoadThread_StatusMessage_Text_Seventh"));
          
          Messages.getInstance();m_log.logMessage(Messages.getString("Loader_LoadThread_StatusMessage_Text_Eighth") + Loader.this.statusMessagePrefix() + " " + ex.getMessage());
        }
        
        ex.printStackTrace();
      } finally {
        if ((Thread.currentThread().isInterrupted()) && 
          (m_log != null)) {
          Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Loader_LoadThread_LogMessage_Text_Third") + Loader.this.statusMessagePrefix() + Messages.getString("Loader_LoadThread_LogMessage_Text_Fourth"));
        }
        

        m_ioThread = null;
        

        m_visual.setStatic();
        m_state = Loader.IDLE;
        m_stopped = false;
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(Loader.this.statusMessagePrefix() + Messages.getString("Loader_LoadThread_StatusMessage_Text_Nineth"));
        }
        Loader.this.block(false);
      }
    }
  }
  




  public String globalInfo()
  {
    return m_globalInfo;
  }
  
  public Loader()
  {
    setLoader(m_Loader);
    appearanceFinal();
  }
  
  public void setDB(boolean flag)
  {
    m_dbSet = flag;
  }
  
  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    Messages.getInstance();JButton goButton = new JButton(Messages.getString("Loader_AppearanceFinal_GoButton_JButton_Text"));
    add(goButton, "Center");
    goButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startLoading();
      }
    });
  }
  
  protected void appearanceDesign() {
    removeAll();
    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  




  public void setBeanContext(BeanContext bc)
  {
    super.setBeanContext(bc);
    if (m_design) {
      appearanceDesign();
    } else {
      appearanceFinal();
    }
  }
  




  public void setLoader(weka.core.converters.Loader loader)
  {
    boolean loadImages = true;
    if (loader.getClass().getName().compareTo(m_Loader.getClass().getName()) == 0)
    {
      loadImages = false;
    }
    m_Loader = loader;
    String loaderName = loader.getClass().toString();
    loaderName = loaderName.substring(loaderName.lastIndexOf('.') + 1, loaderName.length());
    

    if (loadImages) {
      if ((m_Loader instanceof Visible)) {
        m_visual = ((Visible)m_Loader).getVisual();

      }
      else if (!m_visual.loadIcons("weka/gui/beans/icons/" + loaderName + ".gif", "weka/gui/beans/icons/" + loaderName + "_animated.gif"))
      {
        useDefaultVisual();
      }
    }
    
    m_visual.setText(loaderName);
    

    m_globalInfo = KnowledgeFlowApp.getGlobalInfo(m_Loader);
  }
  
  protected void newFileSelected() throws Exception {
    if (!(m_Loader instanceof DatabaseLoader)) {
      newStructure();
    }
  }
  
  protected void newStructure() throws Exception {
    if (((m_Loader instanceof EnvironmentHandler)) && (m_env != null)) {
      try {
        ((EnvironmentHandler)m_Loader).setEnvironment(m_env);
      } catch (Exception ex) {}
    }
    m_dataFormat = m_Loader.getStructure();
  }
  















  public Instances getStructure(String eventName)
  {
    if ((!eventName.equals("dataSet")) && (!eventName.equals("instance"))) {
      return null;
    }
    if ((m_dataSetEventTargets > 0) && (!eventName.equals("dataSet"))) {
      return null;
    }
    if ((m_dataSetEventTargets == 0) && (!eventName.equals("instance"))) {
      return null;
    }
    try
    {
      newStructure();
    }
    catch (Exception ex)
    {
      System.err.println("[KnowledgeFlow/Loader] Warning: " + ex.getMessage());
      m_dataFormat = null;
    }
    return m_dataFormat;
  }
  




  public weka.core.converters.Loader getLoader()
  {
    return m_Loader;
  }
  







  public void setWrappedAlgorithm(Object algorithm)
  {
    if (!(algorithm instanceof weka.core.converters.Loader)) {
      Messages.getInstance();throw new IllegalArgumentException(algorithm.getClass() + Messages.getString("Loader_SetWrappedAlgorithm_IllegalArgumentException_Text"));
    }
    setLoader((weka.core.converters.Loader)algorithm);
  }
  




  public Object getWrappedAlgorithm()
  {
    return getLoader();
  }
  





  protected void notifyStructureAvailable(Instances structure)
  {
    if ((m_dataSetEventTargets > 0) && (structure != null)) {
      DataSetEvent dse = new DataSetEvent(this, structure);
      notifyDataSetLoaded(dse);
    } else if ((m_instanceEventTargets > 0) && (structure != null)) {
      m_ie.setStructure(structure);
      notifyInstanceLoaded(m_ie);
    }
  }
  


  protected void notifyDataSetLoaded(DataSetEvent e)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_listeners.clone();
    }
    
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((DataSourceListener)l.elementAt(i)).acceptDataSet(e);
      }
      m_dataSet = null;
    }
  }
  


  protected void notifyInstanceLoaded(InstanceEvent e)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_listeners.clone();
    }
    
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((InstanceListener)l.elementAt(i)).acceptInstance(e);
      }
      m_dataSet = null;
    }
  }
  



  public void startLoading()
  {
    if (m_ioThread == null)
    {
      m_state = BATCH_LOADING;
      m_ioThread = new LoadThread(this);
      m_ioThread.setPriority(1);
      m_ioThread.start();
    } else {
      m_ioThread = null;
      m_state = IDLE;
    }
  }
  















































  public void start()
    throws Exception
  {
    startLoading();
    block(true);
  }
  








  public String getStartMessage()
  {
    boolean ok = true;
    Messages.getInstance();String entry = Messages.getString("Loader_GetStartMessage_Entry_Text");
    if (m_ioThread == null) {
      if ((m_Loader instanceof FileSourcedConverter)) {
        String temp = ((FileSourcedConverter)m_Loader).retrieveFile().getPath();
        Environment env = m_env == null ? Environment.getSystemWide() : m_env;
        try {
          temp = env.substitute(temp);
        } catch (Exception ex) {}
        File tempF = new File(temp);
        if (!tempF.isFile()) {
          ok = false;
        }
      }
      if (!ok) {
        entry = "$" + entry;
      }
    }
    
    return entry;
  }
  







  private synchronized void block(boolean tf)
  {
    if (tf) {
      try
      {
        if ((m_ioThread.isAlive()) && (m_state != IDLE)) {
          wait();
        }
      }
      catch (InterruptedException ex) {}
    } else {
      notifyAll();
    }
  }
  





  public boolean eventGeneratable(String eventName)
  {
    if (eventName.compareTo("instance") == 0) {
      if (!(m_Loader instanceof IncrementalConverter)) {
        return false;
      }
      if (m_dataSetEventTargets > 0) {
        return false;
      }
    }
    





    if (eventName.compareTo("dataSet") == 0) {
      if (!(m_Loader instanceof BatchConverter)) {
        return false;
      }
      if (m_instanceEventTargets > 0) {
        return false;
      }
    }
    




    return true;
  }
  




  public synchronized void addDataSourceListener(DataSourceListener dsl)
  {
    super.addDataSourceListener(dsl);
    m_dataSetEventTargets += 1;
    try
    {
      if ((((m_Loader instanceof DatabaseLoader)) && (m_dbSet) && (m_dataFormat == null)) || ((!(m_Loader instanceof DatabaseLoader)) && (m_dataFormat == null)))
      {
        m_dataFormat = m_Loader.getStructure();
        m_dbSet = false;
      }
    }
    catch (Exception ex) {}
    notifyStructureAvailable(m_dataFormat);
  }
  




  public synchronized void removeDataSourceListener(DataSourceListener dsl)
  {
    super.removeDataSourceListener(dsl);
    m_dataSetEventTargets -= 1;
  }
  




  public synchronized void addInstanceListener(InstanceListener dsl)
  {
    super.addInstanceListener(dsl);
    m_instanceEventTargets += 1;
    try {
      if ((((m_Loader instanceof DatabaseLoader)) && (m_dbSet) && (m_dataFormat == null)) || ((!(m_Loader instanceof DatabaseLoader)) && (m_dataFormat == null)))
      {
        m_dataFormat = m_Loader.getStructure();
        m_dbSet = false;
      }
    }
    catch (Exception ex) {}
    
    notifyStructureAvailable(m_dataFormat);
  }
  




  public synchronized void removeInstanceListener(InstanceListener dsl)
  {
    super.removeInstanceListener(dsl);
    m_instanceEventTargets -= 1;
  }
  
  public static void main(String[] args) {
    try {
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      
      Loader tv = new Loader();
      
      jf.getContentPane().add(tv, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.setSize(800, 600);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private Object readResolve() throws ObjectStreamException
  {
    if (m_Loader != null) {
      try {
        m_Loader.reset();
      }
      catch (Exception ex) {}
    }
    return this;
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  



  public void setLog(Logger logger)
  {
    m_log = logger;
  }
  





  public void setEnvironment(Environment env)
  {
    m_env = env;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return false;
  }
  






  public boolean connectionAllowed(String eventName)
  {
    return false;
  }
  








  public void connectionNotification(String eventName, Object source) {}
  








  public void disconnectionNotification(String eventName, Object source) {}
  








  public void stop()
  {
    m_stopped = true;
  }
  





  public boolean isBusy()
  {
    return m_ioThread != null;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|" + ((m_Loader instanceof OptionHandler) ? Utils.joinOptions(((OptionHandler)m_Loader).getOptions()) + "|" : "");
  }
  




  private void readObject(ObjectInputStream aStream)
    throws IOException, ClassNotFoundException
  {
    aStream.defaultReadObject();
    

    m_env = Environment.getSystemWide();
  }
}

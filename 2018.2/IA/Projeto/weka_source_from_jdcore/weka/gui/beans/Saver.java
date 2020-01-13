package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import javax.swing.JFrame;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.converters.DatabaseConverter;
import weka.core.converters.DatabaseSaver;
import weka.gui.Logger;
import weka.gui.visualize.PlotData2D;











































public class Saver
  extends AbstractDataSink
  implements WekaWrapper, EnvironmentHandler
{
  private static final long serialVersionUID = 5371716690308950755L;
  private Instances m_dataSet;
  private Instances m_structure;
  protected String m_globalInfo;
  private transient SaveBatchThread m_ioThread;
  private weka.core.converters.Saver m_Saver = new ArffSaver();
  private weka.core.converters.Saver m_SaverTemplate = m_Saver;
  





  private String m_fileName;
  





  private boolean m_isDBSaver;
  




  private boolean m_relationNameForFilename = true;
  

  private int m_count;
  

  protected transient Environment m_env;
  


  private weka.core.converters.Saver makeCopy()
    throws Exception
  {
    return (weka.core.converters.Saver)new SerializedObject(m_SaverTemplate).getObject();
  }
  
  private class SaveBatchThread extends Thread
  {
    private final DataSink m_DS;
    
    public SaveBatchThread(DataSink ds) {
      m_DS = ds;
    }
    
    public void run()
    {
      try {
        m_visual.setAnimated();
        
        m_Saver.setInstances(m_dataSet);
        if (m_logger != null) {
          Messages.getInstance();Messages.getInstance();m_logger.statusMessage(Saver.this.statusMessagePrefix() + Messages.getString("Saver_SaveBatchThread_Run_StatusMessage_Text_First") + m_dataSet.relationName() + Messages.getString("Saver_SaveBatchThread_Run_StatusMessage_Text_Second"));
        }
        




        m_Saver.writeBatch();
        if (m_logger != null) {
          Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_SaveBatchThread_Run_LogMessage_Text_First") + Saver.this.statusMessagePrefix() + Messages.getString("Saver_SaveBatchThread_Run_LogMessage_Text_Second"));
        }
        

      }
      catch (Exception ex)
      {

        if (m_logger != null) {
          Messages.getInstance();m_logger.statusMessage(Saver.this.statusMessagePrefix() + Messages.getString("Saver_SaveBatchThread_Run_StatusMessage_Text_Third"));
          

          Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_SaveBatchThread_Run_LogMessage_Text_Third") + Saver.this.statusMessagePrefix() + Messages.getString("Saver_SaveBatchThread_Run_LogMessage_Text_Fourth") + ex.getMessage());
        }
        




        ex.printStackTrace();
      } finally {
        if ((Thread.currentThread().isInterrupted()) && 
          (m_logger != null)) {
          Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_SaveBatchThread_Run_LogMessage_Text_Fifth") + Saver.this.statusMessagePrefix() + Messages.getString("Saver_SaveBatchThread_Run_LogMessage_Text_Sixth"));
        }
        




        if (m_logger != null) {
          Messages.getInstance();m_logger.statusMessage(Saver.this.statusMessagePrefix() + Messages.getString("Saver_SaveBatchThread_Run_StatusMessage_Text_Fourth"));
        }
        

        Saver.this.block(false);
        m_visual.setStatic();
        m_ioThread = null;
      }
    }
  }
  






  private synchronized void block(boolean tf)
  {
    if (tf) {
      try {
        if (m_ioThread.isAlive()) {
          wait();
        }
      }
      catch (InterruptedException ex) {}
    } else {
      notifyAll();
    }
  }
  





  public boolean isBusy()
  {
    return m_ioThread != null;
  }
  




  public String globalInfo()
  {
    return m_globalInfo;
  }
  

  public Saver()
  {
    setSaverTemplate(m_Saver);
    m_fileName = "";
    m_dataSet = null;
    m_count = 0;
  }
  





  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public void setEnvironment(Environment env)
  {
    m_env = env;
  }
  



  private void passEnvOnToSaver()
  {
    if (((m_SaverTemplate instanceof EnvironmentHandler)) && (m_env != null)) {
      ((EnvironmentHandler)m_Saver).setEnvironment(m_env);
    }
  }
  




  public void setSaverTemplate(weka.core.converters.Saver saver)
  {
    boolean loadImages = true;
    if (saver.getClass().getName().compareTo(m_SaverTemplate.getClass().getName()) == 0)
    {
      loadImages = false;
    }
    m_SaverTemplate = saver;
    String saverName = saver.getClass().toString();
    saverName = saverName.substring(saverName.lastIndexOf('.') + 1, saverName.length());
    
    if (loadImages)
    {
      if (!m_visual.loadIcons("weka/gui/beans/icons/" + saverName + ".gif", "weka/gui/beans/icons/" + saverName + "_animated.gif"))
      {
        useDefaultVisual();
      }
    }
    m_visual.setText(saverName);
    

    m_globalInfo = KnowledgeFlowApp.getGlobalInfo(m_SaverTemplate);
    if ((m_SaverTemplate instanceof DatabaseConverter)) {
      m_isDBSaver = true;
    } else {
      m_isDBSaver = false;
    }
  }
  






  protected String sanitizeFilename(String filename)
  {
    filename = filename.replaceAll("\\\\", "_").replaceAll(":", "_").replaceAll("/", "_");
    
    filename = Utils.removeSubstring(filename, "weka.filters.supervised.instance.");
    
    filename = Utils.removeSubstring(filename, "weka.filters.supervised.attribute.");
    
    filename = Utils.removeSubstring(filename, "weka.filters.unsupervised.instance.");
    
    filename = Utils.removeSubstring(filename, "weka.filters.unsupervised.attribute.");
    
    filename = Utils.removeSubstring(filename, "weka.clusterers.");
    filename = Utils.removeSubstring(filename, "weka.associations.");
    filename = Utils.removeSubstring(filename, "weka.attributeSelection.");
    filename = Utils.removeSubstring(filename, "weka.estimators.");
    filename = Utils.removeSubstring(filename, "weka.datagenerators.");
    
    if ((!m_isDBSaver) && (!m_relationNameForFilename)) {
      filename = "";
      try {
        if (m_Saver.filePrefix().equals("")) {
          m_Saver.setFilePrefix("no-name");
        }
      } catch (Exception ex) {
        System.err.println(ex);
      }
    }
    
    return filename;
  }
  






  public synchronized void acceptDataSet(DataSetEvent e)
  {
    try
    {
      m_Saver = makeCopy();
    } catch (Exception ex) {
      if (m_logger != null) {
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("Saver_AcceptDataSet_StatusMessage_Text_First"));
        

        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_AcceptDataSet_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Saver_AcceptDataSet_LogMessage_Text_Second") + ex.getMessage());
      }
    }
    





    passEnvOnToSaver();
    m_fileName = sanitizeFilename(e.getDataSet().relationName());
    m_dataSet = e.getDataSet();
    if ((e.isStructureOnly()) && (m_isDBSaver) && (((DatabaseSaver)m_SaverTemplate).getRelationForTableName()))
    {
      ((DatabaseSaver)m_Saver).setTableName(m_fileName);
    }
    if (!e.isStructureOnly()) {
      if (!m_isDBSaver) {
        try {
          m_Saver.setDirAndPrefix(m_fileName, "");
        } catch (Exception ex) {
          System.out.println(ex);
        }
      }
      saveBatch();
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("Saver_AcceptDataSet_Text_First") + m_fileName + Messages.getString("Saver_AcceptDataSet_Text_Second"));
    }
  }
  










  public synchronized void acceptDataSet(ThresholdDataEvent e)
  {
    try
    {
      m_Saver = makeCopy();
    } catch (Exception ex) {
      if (m_logger != null) {
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("Saver_AcceptDataSet_StatusMessage_Text_Second"));
        

        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_AcceptDataSet_LogMessage_Text_Third") + statusMessagePrefix() + Messages.getString("Saver_AcceptDataSet_LogMessage_Text_Fourth") + ex.getMessage());
      }
    }
    






    passEnvOnToSaver();
    m_fileName = sanitizeFilename(e.getDataSet().getPlotInstances().relationName());
    
    m_dataSet = e.getDataSet().getPlotInstances();
    
    if ((m_isDBSaver) && (((DatabaseSaver)m_SaverTemplate).getRelationForTableName()))
    {
      ((DatabaseSaver)m_Saver).setTableName(m_fileName);
      ((DatabaseSaver)m_Saver).setRelationForTableName(false);
    }
    
    if (!m_isDBSaver) {
      try {
        m_Saver.setDirAndPrefix(m_fileName, "");
      } catch (Exception ex) {
        System.out.println(ex);
      }
    }
    saveBatch();
    Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("Saver_AcceptDataSet_Text_Third") + m_fileName + Messages.getString("Saver_AcceptDataSet_Text_Fourth"));
  }
  









  public synchronized void acceptTestSet(TestSetEvent e)
  {
    try
    {
      m_Saver = makeCopy();
    } catch (Exception ex) {
      if (m_logger != null) {
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("Saver_AcceptTestSet_StatusMessage_Text_First"));
        

        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_AcceptTestSet_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Saver_AcceptTestSet_LogMessage_Text_Second") + ex.getMessage());
      }
    }
    






    passEnvOnToSaver();
    m_fileName = sanitizeFilename(e.getTestSet().relationName());
    m_dataSet = e.getTestSet();
    if ((e.isStructureOnly()) && (m_isDBSaver) && (((DatabaseSaver)m_SaverTemplate).getRelationForTableName()))
    {
      ((DatabaseSaver)m_Saver).setTableName(m_fileName);
    }
    if (!e.isStructureOnly()) {
      if (!m_isDBSaver) {
        try {
          m_Saver.setDirAndPrefix(m_fileName, "_test_" + e.getSetNumber() + "_of_" + e.getMaxSetNumber());
        }
        catch (Exception ex) {
          System.out.println(ex);
        }
      } else {
        ((DatabaseSaver)m_Saver).setRelationForTableName(false);
        String setName = ((DatabaseSaver)m_Saver).getTableName();
        setName = setName.replaceFirst("_[tT][eE][sS][tT]_[0-9]+_[oO][fF]_[0-9]+", "");
        
        ((DatabaseSaver)m_Saver).setTableName(setName + "_test_" + e.getSetNumber() + "_of_" + e.getMaxSetNumber());
      }
      
      saveBatch();
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("Saver_AcceptTestSet_Text_First") + e.getSetNumber() + Messages.getString("Saver_AcceptTestSet_Text_Second") + e.getMaxSetNumber() + Messages.getString("Saver_AcceptTestSet_Text_Third") + m_fileName + Messages.getString("Saver_AcceptTestSet_Text_Fourth"));
    }
  }
  

















  public synchronized void acceptTrainingSet(TrainingSetEvent e)
  {
    try
    {
      m_Saver = makeCopy();
    } catch (Exception ex) {
      if (m_logger != null) {
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("Saver_AcceptTrainingSet_StatusMessage_Text_First"));
        

        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_AcceptTrainingSet_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Saver_AcceptTrainingSet_LogMessage_Text_Second") + ex.getMessage());
      }
    }
    





    passEnvOnToSaver();
    m_fileName = sanitizeFilename(e.getTrainingSet().relationName());
    m_dataSet = e.getTrainingSet();
    if ((e.isStructureOnly()) && (m_isDBSaver) && (((DatabaseSaver)m_SaverTemplate).getRelationForTableName()))
    {
      ((DatabaseSaver)m_Saver).setTableName(m_fileName);
    }
    if (!e.isStructureOnly()) {
      if (!m_isDBSaver) {
        try {
          m_Saver.setDirAndPrefix(m_fileName, "_training_" + e.getSetNumber() + "_of_" + e.getMaxSetNumber());
        }
        catch (Exception ex) {
          System.out.println(ex);
        }
      } else {
        ((DatabaseSaver)m_Saver).setRelationForTableName(false);
        String setName = ((DatabaseSaver)m_Saver).getTableName();
        setName = setName.replaceFirst("_[tT][rR][aA][iI][nN][iI][nN][gG]_[0-9]+_[oO][fF]_[0-9]+", "");
        
        ((DatabaseSaver)m_Saver).setTableName(setName + "_training_" + e.getSetNumber() + "_of_" + e.getMaxSetNumber());
      }
      
      saveBatch();
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("Saver_AcceptTrainingSet_Text_First") + e.getSetNumber() + Messages.getString("Saver_AcceptTrainingSet_Text_Second") + e.getMaxSetNumber() + Messages.getString("Saver_AcceptTrainingSet_Text_Third") + m_fileName + Messages.getString("Saver_AcceptTrainingSet_Text_Fourth"));
    }
  }
  











  public synchronized void saveBatch()
  {
    m_Saver.setRetrieval(1);
    




    m_ioThread = new SaveBatchThread(this);
    m_ioThread.setPriority(1);
    m_ioThread.start();
    block(true);
  }
  








  public synchronized void acceptInstance(InstanceEvent e)
  {
    if (e.getStatus() == 0)
    {
      try {
        m_Saver = makeCopy();
      } catch (Exception ex) {
        if (m_logger != null) {
          Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("Saver_AcceptInstance_StatusMessage_Text_First"));
          

          Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("Saver_AcceptInstance_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Saver_AcceptInstance_LogMessage_Text_Second") + ex.getMessage());
        }
      }
      




      m_Saver.setRetrieval(2);
      m_structure = e.getStructure();
      m_fileName = sanitizeFilename(m_structure.relationName());
      m_Saver.setInstances(m_structure);
      if ((m_isDBSaver) && 
        (((DatabaseSaver)m_SaverTemplate).getRelationForTableName())) {
        ((DatabaseSaver)m_Saver).setTableName(m_fileName);
        ((DatabaseSaver)m_Saver).setRelationForTableName(false);
      }
    }
    if (e.getStatus() == 1) {
      m_visual.setAnimated();
      if (m_count == 0) {
        passEnvOnToSaver();
        if (!m_isDBSaver) {
          try {
            m_Saver.setDirAndPrefix(m_fileName, "");
          } catch (Exception ex) {
            System.out.println(ex);
            m_visual.setStatic();
          }
        }
        m_count += 1;
      }
      


      try
      {
        m_Saver.writeIncremental(e.getInstance());
      } catch (Exception ex) {
        m_visual.setStatic();
        Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("Saver_AcceptInstance_Error_Text_First") + e.getInstance() + Messages.getString("Saver_AcceptInstance_Error_Text_Second"));
        



        ex.printStackTrace();
      }
    }
    if (e.getStatus() == 2) {
      try {
        if (m_count == 0) {
          passEnvOnToSaver();
          if (!m_isDBSaver) {
            try {
              m_Saver.setDirAndPrefix(m_fileName, "");
            } catch (Exception ex) {
              System.out.println(ex);
              m_visual.setStatic();
            }
          }
          m_count += 1;
        }
        m_Saver.writeIncremental(e.getInstance());
        if (e.getInstance() != null) {
          m_Saver.writeIncremental(null);
        }
        
        m_visual.setStatic();
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("Saver_AcceptInstance_Text_First") + m_fileName + Messages.getString("Saver_AcceptInstance_Text_Second"));
        







        m_count = 0;
      } catch (Exception ex) {
        m_visual.setStatic();
        Messages.getInstance();System.err.println(Messages.getString("Saver_AcceptInstance_Text_Third"));
        
        ex.printStackTrace();
      }
    }
  }
  




  public weka.core.converters.Saver getSaverTemplate()
  {
    return m_SaverTemplate;
  }
  




  public void setWrappedAlgorithm(Object algorithm)
  {
    if (!(algorithm instanceof weka.core.converters.Saver)) {
      Messages.getInstance();throw new IllegalArgumentException(algorithm.getClass() + Messages.getString("Saver_SetWrappedAlgorithm_IllegalArgumentException_Text"));
    }
    

    setSaverTemplate((weka.core.converters.Saver)algorithm);
  }
  




  public Object getWrappedAlgorithm()
  {
    return getSaverTemplate();
  }
  





  public void setRelationNameForFilename(boolean r)
  {
    m_relationNameForFilename = r;
  }
  




  public boolean getRelationNameForFilename()
  {
    return m_relationNameForFilename;
  }
  


  public void stop()
  {
    if ((m_listenee instanceof BeanCommon)) {
      ((BeanCommon)m_listenee).stop();
    }
    

    if (m_ioThread != null) {
      m_ioThread.interrupt();
      m_ioThread.stop();
      m_ioThread = null;
      m_visual.setStatic();
    }
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|" + ((m_Saver instanceof OptionHandler) ? Utils.joinOptions(((OptionHandler)m_Saver).getOptions()) + "|" : "");
  }
  






  private void readObject(ObjectInputStream aStream)
    throws IOException, ClassNotFoundException
  {
    aStream.defaultReadObject();
    

    m_env = Environment.getSystemWide();
  }
  



  public static void main(String[] args)
  {
    try
    {
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      
      Saver tv = new Saver();
      
      jf.getContentPane().add(tv, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
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
}

package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.xml.KOML;
import weka.core.xml.XStream;
import weka.experiment.Task;
import weka.experiment.TaskStatusInfo;
import weka.gui.ExtensionFileFilter;
import weka.gui.Logger;





































public class Classifier
  extends JPanel
  implements BeanCommon, Visible, WekaWrapper, EventConstraints, Serializable, UserRequestAcceptor, TrainingSetListener, TestSetListener, InstanceListener
{
  private static final long serialVersionUID = 659603893917736008L;
  protected BeanVisual m_visual = new BeanVisual("Classifier", "weka/gui/beans/icons/DefaultClassifier.gif", "weka/gui/beans/icons/DefaultClassifier_animated.gif");
  


  private static int IDLE = 0;
  private static int BUILDING_MODEL = 1;
  private int m_state = IDLE;
  




  protected String m_globalInfo;
  




  private final Hashtable m_listenees = new Hashtable();
  



  private final Vector m_batchClassifierListeners = new Vector();
  



  private final Vector m_incrementalClassifierListeners = new Vector();
  



  private final Vector m_graphListeners = new Vector();
  



  private final Vector m_textListeners = new Vector();
  


  private Instances m_trainingSet;
  


  private weka.classifiers.Classifier m_Classifier = new ZeroR();
  
  private weka.classifiers.Classifier m_ClassifierTemplate = m_Classifier;
  
  private final IncrementalClassifierEvent m_ie = new IncrementalClassifierEvent(this);
  

  public static final String FILE_EXTENSION = "model";
  

  private transient JFileChooser m_fileChooser = null;
  




  protected FileFilter m_binaryFilter;
  




  protected FileFilter m_KOMLFilter;
  




  protected FileFilter m_XStreamFilter;
  




  private boolean m_updateIncrementalClassifier;
  




  private transient Logger m_log;
  




  private InstanceEvent m_incrementalEvent;
  



  protected int m_executionSlots;
  



  protected transient ThreadPoolExecutor m_executorPool;
  



  protected transient BatchClassifierEvent[][] m_outputQueues;
  



  protected transient boolean[][] m_completedSets;
  



  protected transient Date m_currentBatchIdentifier;
  



  protected transient boolean m_batchStarted;
  



  protected String m_oldText;
  



  protected boolean m_block;
  




  public String globalInfo()
  {
    return m_globalInfo;
  }
  
  public Classifier()
  {
    Messages.getInstance();Messages.getInstance();m_binaryFilter = new ExtensionFileFilter(".model", Messages.getString("Classifier_BinaryFilter_ExtensionFileFilter_Text_First") + "model" + Messages.getString("Classifier_BinaryFilter_ExtensionFileFilter_Text_Second"));
    





    Messages.getInstance();Messages.getInstance();m_KOMLFilter = new ExtensionFileFilter(".komlmodel", Messages.getString("Classifier_KOMLFilter_ExtensionFileFilter_Text_First") + ".koml" + "model" + Messages.getString("Classifier_KOMLFilter_ExtensionFileFilter_Text_Second"));
    






    Messages.getInstance();Messages.getInstance();m_XStreamFilter = new ExtensionFileFilter(".xstreammodel", Messages.getString("Classifier_XStreamFilter_ExtensionFileFilter_Text_First") + ".xstream" + "model" + Messages.getString("Classifier_XStreamFilter_ExtensionFileFilter_Text_Second"));
    












    m_updateIncrementalClassifier = true;
    
    m_log = null;
    








    m_executionSlots = 2;
    





















    m_batchStarted = false;
    



    m_oldText = "";
    



    m_block = false;
    













    setLayout(new BorderLayout());
    add(m_visual, "Center");
    setClassifierTemplate(m_ClassifierTemplate);
  }
  


  private void startExecutorPool()
  {
    if (m_executorPool != null) {
      m_executorPool.shutdownNow();
    }
    
    m_executorPool = new ThreadPoolExecutor(m_executionSlots, m_executionSlots, 120L, TimeUnit.SECONDS, new LinkedBlockingQueue());
  }
  







  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  





  public String getCustomName()
  {
    return m_visual.getText();
  }
  
  protected void setupFileChooser() {
    if (m_fileChooser == null) {
      m_fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    }
    

    m_fileChooser.addChoosableFileFilter(m_binaryFilter);
    if (KOML.isPresent()) {
      m_fileChooser.addChoosableFileFilter(m_KOMLFilter);
    }
    if (XStream.isPresent()) {
      m_fileChooser.addChoosableFileFilter(m_XStreamFilter);
    }
    m_fileChooser.setFileFilter(m_binaryFilter);
  }
  




  public int getExecutionSlots()
  {
    return m_executionSlots;
  }
  




  public void setExecutionSlots(int slots)
  {
    m_executionSlots = slots;
  }
  




  public void setClassifierTemplate(weka.classifiers.Classifier c)
  {
    boolean loadImages = true;
    if (c.getClass().getName().compareTo(m_ClassifierTemplate.getClass().getName()) == 0)
    {
      loadImages = false;
    }
    else
    {
      m_trainingSet = null;
    }
    m_ClassifierTemplate = c;
    String classifierName = c.getClass().toString();
    classifierName = classifierName.substring(classifierName.lastIndexOf('.') + 1, classifierName.length());
    

    if (loadImages) {
      if (!m_visual.loadIcons("weka/gui/beans/icons/" + classifierName + ".gif", "weka/gui/beans/icons/" + classifierName + "_animated.gif"))
      {
        useDefaultVisual();
      }
      m_visual.setText(classifierName);
    }
    
    if ((!(m_ClassifierTemplate instanceof UpdateableClassifier)) && (m_listenees.containsKey("instance")))
    {
      if (m_log != null) {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_SetClassifierTemplate_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Classifier_SetClassifierTemplate_LogMessage_Text_Second") + getCustomName() + Messages.getString("Classifier_SetClassifierTemplate_LogMessage_Text_Third"));
      }
    }
    







    m_globalInfo = KnowledgeFlowApp.getGlobalInfo(m_ClassifierTemplate);
  }
  




  public weka.classifiers.Classifier getClassifierTemplate()
  {
    return m_ClassifierTemplate;
  }
  

  private void setTrainedClassifier(weka.classifiers.Classifier tc)
    throws Exception
  {
    weka.classifiers.Classifier newTemplate = null;
    
    String[] options = tc.getOptions();
    newTemplate = weka.classifiers.Classifier.forName(tc.getClass().getName(), options);
    

    if (!newTemplate.getClass().equals(m_ClassifierTemplate.getClass())) {
      throw new Exception("Classifier model " + tc.getClass().getName() + " is not the same type " + "of classifier as this one (" + m_ClassifierTemplate.getClass().getName() + ")");
    }
    

    setClassifierTemplate(newTemplate);
    
    m_Classifier = tc;
  }
  





  public boolean hasIncomingStreamInstances()
  {
    if (m_listenees.size() == 0) {
      return false;
    }
    if (m_listenees.containsKey("instance")) {
      return true;
    }
    return false;
  }
  





  public boolean hasIncomingBatchInstances()
  {
    if (m_listenees.size() == 0) {
      return false;
    }
    if ((m_listenees.containsKey("trainingSet")) || (m_listenees.containsKey("testSet")))
    {
      return true;
    }
    return false;
  }
  




  public weka.classifiers.Classifier getClassifier()
  {
    return m_Classifier;
  }
  







  public void setWrappedAlgorithm(Object algorithm)
  {
    if (!(algorithm instanceof weka.classifiers.Classifier)) {
      Messages.getInstance();throw new IllegalArgumentException(algorithm.getClass() + Messages.getString("Classifier_SetWrappedAlgorithm_IllegalArgumentException_Text_First"));
    }
    

    setClassifierTemplate((weka.classifiers.Classifier)algorithm);
  }
  





  public Object getWrappedAlgorithm()
  {
    return getClassifierTemplate();
  }
  





  public boolean getUpdateIncrementalClassifier()
  {
    return m_updateIncrementalClassifier;
  }
  





  public void setUpdateIncrementalClassifier(boolean update)
  {
    m_updateIncrementalClassifier = update;
  }
  





  public void acceptInstance(InstanceEvent e)
  {
    m_incrementalEvent = e;
    handleIncrementalEvent();
  }
  


  private void handleIncrementalEvent()
  {
    if ((m_executorPool != null) && ((m_executorPool.getQueue().size() > 0) || (m_executorPool.getActiveCount() > 0)))
    {


      Messages.getInstance();Messages.getInstance();String messg = Messages.getString("Classifier_HandleIncrementalEvent_Messg_Text_First") + statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_Messg_Text_Second");
      




      if (m_log != null) {
        m_log.logMessage(messg);
        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_StatusMessage_Text_First"));
      }
      else
      {
        System.err.println(messg);
      }
      return;
    }
    
    if (m_incrementalEvent.getStatus() == 0)
    {
      if (m_log != null) {
        m_log.statusMessage(statusMessagePrefix() + "remove");
      }
      

      Instances dataset = m_incrementalEvent.getStructure();
      
      if (dataset.classIndex() < 0) {
        stop();
        Messages.getInstance();String errorMessage = statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_ErrorMessage_Text_First");
        


        if (m_log != null) {
          m_log.statusMessage(errorMessage);
          Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_HandleIncrementalEvent_LogMessage_Text_First") + getCustomName() + Messages.getString("Classifier_HandleIncrementalEvent_LogMessage_Text_Second") + errorMessage);


        }
        else
        {

          Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("Classifier_HandleIncrementalEvent_Error_Text_First") + getCustomName() + Messages.getString("Classifier_HandleIncrementalEvent_Error_Text_Second") + errorMessage);
        }
        




        return;
      }
      




      try
      {
        if ((m_trainingSet == null) || (!dataset.equalHeaders(m_trainingSet))) {
          if (!(m_ClassifierTemplate instanceof UpdateableClassifier)) {
            stop();
            if (m_log != null) {
              Messages.getInstance();Messages.getInstance();String msg = statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_Msg_Text_Second");
              





              Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_HandleIncrementalEvent_LogMessage_Text_Third") + msg);
              

              m_log.statusMessage(msg);
            }
            return;
          }
          if ((m_trainingSet != null) && (!dataset.equalHeaders(m_trainingSet))) {
            if (m_log != null) {
              Messages.getInstance();String msg = statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_Msg_Text_Third");
              


              Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_HandleIncrementalEvent_LogMessage_Text_Fourth") + msg);
              

              m_log.statusMessage(msg);
            }
            m_trainingSet = null;
          }
          if (m_trainingSet == null)
          {
            m_trainingSet = new Instances(dataset, 0);
            m_Classifier = weka.classifiers.Classifier.makeCopy(m_ClassifierTemplate);
            
            m_Classifier.buildClassifier(m_trainingSet);
          }
        }
      } catch (Exception ex) {
        stop();
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_StatusMessage_Text_Second"));
          

          Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_HandleIncrementalEvent_LogMessage_Text_Fifth") + statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_LogMessage_Text_Sixth") + ex.getMessage());
        }
        




        ex.printStackTrace();
        return;
      }
      
      System.err.println("NOTIFYING NEW BATCH");
      m_ie.setStructure(dataset);
      m_ie.setClassifier(m_Classifier);
      
      notifyIncrementalClassifierListeners(m_ie);
      return;
    }
    if (m_trainingSet == null)
    {



      return;
    }
    

    try
    {
      if (m_incrementalEvent.getInstance().dataset().classIndex() < 0)
      {
        m_incrementalEvent.getInstance().dataset().setClassIndex(m_incrementalEvent.getInstance().dataset().numAttributes() - 1);
      }
      




      int status = 1;
      



      if (m_incrementalEvent.getStatus() == 2) {
        status = 2;
      }
      
      m_ie.setStatus(status);
      m_ie.setClassifier(m_Classifier);
      m_ie.setCurrentInstance(m_incrementalEvent.getInstance());
      
      notifyIncrementalClassifierListeners(m_ie);
      



      if (((m_ClassifierTemplate instanceof UpdateableClassifier)) && (m_updateIncrementalClassifier == true) && (!m_incrementalEvent.getInstance().isMissing(m_incrementalEvent.getInstance().dataset().classIndex())))
      {


        ((UpdateableClassifier)m_Classifier).updateClassifier(m_incrementalEvent.getInstance());
      }
      
      if ((m_incrementalEvent.getStatus() == 2) && 
        (m_textListeners.size() > 0)) {
        String modelString = m_Classifier.toString();
        String titleString = m_Classifier.getClass().getName();
        
        titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
        

        Messages.getInstance();Messages.getInstance();modelString = Messages.getString("Classifier_HandleIncrementalEvent_ModelString_Text_First") + titleString + "\n" + Messages.getString("Classifier_HandleIncrementalEvent_ModelString_Text_Second") + m_trainingSet.relationName() + "\n\n" + modelString;
        






        Messages.getInstance();titleString = Messages.getString("Classifier_HandleIncrementalEvent_TitleString_Text_First") + titleString;
        


        TextEvent nt = new TextEvent(this, modelString, titleString);
        notifyTextListeners(nt);
      }
    }
    catch (Exception ex) {
      stop();
      if (m_log != null) {
        Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_HandleIncrementalEvent_LogMessage_Text_Seventh") + statusMessagePrefix() + ex.getMessage());
        

        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_HandleIncrementalEvent_StatusMessage_Text_Third"));
        

        ex.printStackTrace();
      } else {
        ex.printStackTrace();
      }
    }
  }
  
  protected class TrainingTask implements Runnable, Task {
    private final int m_runNum;
    private final int m_maxRunNum;
    private final int m_setNum;
    private final int m_maxSetNum;
    private Instances m_train = null;
    private final TaskStatusInfo m_taskInfo = new TaskStatusInfo();
    
    public TrainingTask(int runNum, int maxRunNum, int setNum, int maxSetNum, Instances train)
    {
      m_runNum = runNum;
      m_maxRunNum = maxRunNum;
      m_setNum = setNum;
      m_maxSetNum = maxSetNum;
      m_train = train;
      m_taskInfo.setExecutionStatus(0);
    }
    
    public void run()
    {
      execute();
    }
    
    public void execute()
    {
      try {
        if (m_train != null) {
          if (m_train.classIndex() < 0)
          {
            stop();
            Messages.getInstance();String errorMessage = Classifier.this.statusMessagePrefix() + Messages.getString("Classifier_TrainingTask_Execute_ErrorMessage_Text_First");
            


            if (m_log != null) {
              m_log.statusMessage(errorMessage);
              Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_TrainingTask_Execute_LogMessage_Text_First") + errorMessage);
            }
            else
            {
              Messages.getInstance();System.err.println(Messages.getString("Classifier_TrainingTask_Execute_Error_Text_First") + errorMessage);
            }
            

            return;
          }
          






          if ((m_runNum == 1) && (m_setNum == 1))
          {

            m_state = Classifier.BUILDING_MODEL;
            

            m_taskInfo.setExecutionStatus(1);
          }
          


          Messages.getInstance();Messages.getInstance();String msg = Classifier.this.statusMessagePrefix() + Messages.getString("Classifier_TrainingTask_Execute_Msg_Text_First") + m_runNum + Messages.getString("Classifier_TrainingTask_Execute_Msg_Text_Second") + m_setNum;
          





          if (m_log != null) {
            m_log.statusMessage(msg);
          } else {
            System.err.println(msg);
          }
          


          weka.classifiers.Classifier classifierCopy = weka.classifiers.Classifier.makeCopy(m_ClassifierTemplate);
          


          classifierCopy.buildClassifier(m_train);
          if ((m_runNum == m_maxRunNum) && (m_setNum == m_maxSetNum))
          {

            m_Classifier = classifierCopy;
            m_trainingSet = new Instances(m_train, 0);
          }
          



          BatchClassifierEvent ce = new BatchClassifierEvent(Classifier.this, classifierCopy, new DataSetEvent(this, m_train), null, m_setNum, m_maxSetNum);
          




          ce.setGroupIdentifier(m_currentBatchIdentifier.getTime());
          Classifier.this.notifyBatchClassifierListeners(ce);
          

          Classifier.this.classifierTrainingComplete(ce);
          

          if (((classifierCopy instanceof Drawable)) && (m_graphListeners.size() > 0))
          {
            String grphString = ((Drawable)classifierCopy).graph();
            int grphType = ((Drawable)classifierCopy).graphType();
            String grphTitle = classifierCopy.getClass().getName();
            grphTitle = grphTitle.substring(grphTitle.lastIndexOf('.') + 1, grphTitle.length());
            

            Messages.getInstance();grphTitle = Messages.getString("Classifier_TrainingTask_Execute_GrphTitle_Text_First") + m_setNum + " (" + m_train.relationName() + ") " + grphTitle;
            



            GraphEvent ge = new GraphEvent(Classifier.this, grphString, grphTitle, grphType);
            
            Classifier.this.notifyGraphListeners(ge);
          }
          
          if (m_textListeners.size() > 0) {
            String modelString = classifierCopy.toString();
            String titleString = classifierCopy.getClass().getName();
            
            titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
            

            Messages.getInstance();Messages.getInstance();Messages.getInstance();modelString = Messages.getString("Classifier_TrainingTask_Execute_ModelString_Text_First") + titleString + "\n" + Messages.getString("Classifier_TrainingTask_Execute_ModelString_Text_Second") + m_train.relationName() + (m_maxSetNum > 1 ? Messages.getString("Classifier_TrainingTask_Execute_ModelString_Text_Third") + m_setNum : "") + "\n\n" + modelString;
            









            Messages.getInstance();titleString = Messages.getString("Classifier_TrainingTask_Execute_TitleString_Text_First") + titleString;
            



            TextEvent nt = new TextEvent(Classifier.this, modelString, titleString + (m_maxSetNum > 1 ? " (fold " + m_setNum + ")" : ""));
            

            Classifier.this.notifyTextListeners(nt);
          }
        }
      }
      catch (Exception ex) {
        stop();
        ex.printStackTrace();
        if (m_log != null) {
          Messages.getInstance();String titleString = Messages.getString("Classifier_TrainingTask_Execute_TitleString_Text_Second") + Classifier.this.statusMessagePrefix();
          



          Messages.getInstance();Messages.getInstance();Messages.getInstance();titleString = titleString + Messages.getString("Classifier_TrainingTask_Execute_TitleString_Text_Third") + m_runNum + Messages.getString("Classifier_TrainingTask_Execute_TitleString_Text_Fourth") + m_setNum + Messages.getString("Classifier_TrainingTask_Execute_TitleString_Text_Fifth");
          







          Messages.getInstance();m_log.logMessage(titleString + Messages.getString("Classifier_TrainingTask_Execute_LogMessage_Text_Fourth") + ex.getMessage());
          


          Messages.getInstance();m_log.statusMessage(Classifier.this.statusMessagePrefix() + Messages.getString("Classifier_TrainingTask_Execute_StatusMessage_Text_First"));
          

          ex.printStackTrace();
        }
        m_taskInfo.setExecutionStatus(2);
      } finally {
        m_visual.setStatic();
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(Classifier.this.statusMessagePrefix() + Messages.getString("Classifier_TrainingTask_Execute_StatusMessage_Text_Second"));
        }
        

        m_state = Classifier.IDLE;
        
        if (Thread.currentThread().isInterrupted())
        {
          m_trainingSet = null;
          if (m_log != null) {
            Messages.getInstance();String titleString = Messages.getString("Classifier_TrainingTask_Execute_TitleString_Text_Sixth") + Classifier.this.statusMessagePrefix();
            



            Messages.getInstance();Messages.getInstance();Messages.getInstance();m_log.logMessage(titleString + Messages.getString("Classifier_TrainingTask_Execute_LogMessage_Text_Fifth") + m_runNum + Messages.getString("Classifier_TrainingTask_Execute_LogMessage_Text_Sixth") + m_setNum + Messages.getString("Classifier_TrainingTask_Execute_LogMessage_Text_Seventh"));
            







            Messages.getInstance();m_log.statusMessage(Classifier.this.statusMessagePrefix() + Messages.getString("Classifier_TrainingTask_Execute_LogMessage_Text_Seventh"));
          }
        }
      }
    }
    






























    public TaskStatusInfo getTaskStatus()
    {
      return null;
    }
  }
  






  public void acceptTrainingSet(TrainingSetEvent e)
  {
    if (e.isStructureOnly())
    {



      BatchClassifierEvent ce = new BatchClassifierEvent(this, m_Classifier, new DataSetEvent(this, e.getTrainingSet()), new DataSetEvent(this, e.getTrainingSet()), e.getSetNumber(), e.getMaxSetNumber());
      



      notifyBatchClassifierListeners(ce);
      return;
    }
    
    if (m_block)
    {
      if (m_log != null) {
        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_AcceptTrainingSet_StatusMessage_Text_First"));
        

        Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_AcceptTrainingSet_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTrainingSet_LogMessage_Text_Second"));
      }
      



      return;
    }
    

    if ((e.getRunNumber() == 1) && (e.getSetNumber() == 1))
    {

      m_trainingSet = new Instances(e.getTrainingSet(), 0);
      m_state = BUILDING_MODEL;
      
      Messages.getInstance();Messages.getInstance();Messages.getInstance();String msg = Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_First") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Second") + getExecutionSlots() + Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Third");
      







      if (m_log != null) {
        m_log.logMessage(msg);
      } else {
        System.err.println(msg);
      }
      




      startExecutorPool();
      


      Messages.getInstance();Messages.getInstance();msg = Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Fourth") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Fifth");
      




      if (m_log != null) {
        m_log.logMessage(msg);
      } else {
        System.err.println(msg);
      }
      
      if (!m_batchStarted) {
        m_outputQueues = new BatchClassifierEvent[e.getMaxRunNumber()][e.getMaxSetNumber()];
        
        m_completedSets = new boolean[e.getMaxRunNumber()][e.getMaxSetNumber()];
        m_currentBatchIdentifier = new Date();
        m_batchStarted = true;
      }
    }
    

    TrainingTask newTask = new TrainingTask(e.getRunNumber(), e.getMaxRunNumber(), e.getSetNumber(), e.getMaxSetNumber(), e.getTrainingSet());
    

    Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();String msg = Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Sixth") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Seventh") + e.getRunNumber() + Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Eighth") + e.getSetNumber() + Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Nineth");
    










    if (m_log != null) {
      m_log.logMessage(msg);
    } else {
      System.err.println(msg);
    }
    




    m_executorPool.execute(newTask);
  }
  






  public synchronized void acceptTestSet(TestSetEvent e)
  {
    if (m_block)
    {
      if (m_log != null) {
        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_AcceptTrainingSet_StatusMessage_Text_Second"));
        

        Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_AcceptTrainingSet_Msg_Text_Nineth") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTrainingSet_StatusMessage_Text_Second"));
      }
      



      return;
    }
    
    Instances testSet = e.getTestSet();
    if ((testSet != null) && 
      (testSet.classIndex() < 0))
    {

      stop();
      Messages.getInstance();String errorMessage = statusMessagePrefix() + Messages.getString("Classifier_AcceptTestSet_ErrorMessage_Text_First");
      


      if (m_log != null) {
        m_log.statusMessage(errorMessage);
        Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_AcceptTestSet_LogMessage_Text_First") + errorMessage);
      }
      else
      {
        Messages.getInstance();System.err.println(Messages.getString("Classifier_AcceptTestSet_Error_Text_First") + errorMessage);
      }
      

      return;
    }
    





    if ((m_Classifier != null) && (m_state == IDLE) && (!m_listenees.containsKey("trainingSet")))
    {


      if ((e.getTestSet() != null) && (e.isStructureOnly())) {
        return;
      }
      


      if (m_trainingSet == null) {
        stop();
        Messages.getInstance();String errorMessage = statusMessagePrefix() + Messages.getString("Classifier_AcceptTestSet_ErrorMessage_Text_First_Alpha");
        


        if (m_log != null) {
          m_log.statusMessage(errorMessage);
          Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_AcceptTestSet_LogMessage_Text_Second") + errorMessage);
        }
        else
        {
          Messages.getInstance();System.err.println(Messages.getString("Classifier_AcceptTestSet_Error_Text_Second") + errorMessage);
        }
        

        return;
      }
      
      testSet = e.getTestSet();
      if ((e.getRunNumber() == 1) && (e.getSetNumber() == 1)) {
        m_currentBatchIdentifier = new Date();
      }
      
      if (testSet != null)
      {




        if (m_trainingSet.equalHeaders(testSet)) {
          BatchClassifierEvent ce = new BatchClassifierEvent(this, m_Classifier, new DataSetEvent(this, m_trainingSet), new DataSetEvent(this, e.getTestSet()), e.getRunNumber(), e.getMaxRunNumber(), e.getSetNumber(), e.getMaxSetNumber());
          



          ce.setGroupIdentifier(m_currentBatchIdentifier.getTime());
          
          if ((m_log != null) && (!e.isStructureOnly())) {
            Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_AcceptTestSet_StatusMessage_Text_First"));
          }
          

          m_batchStarted = false;
          notifyBatchClassifierListeners(ce);
        }
        
      }
      
    }
    else
    {
      if ((e.getRunNumber() == 1) && (e.getSetNumber() == 1) && 
        (!m_batchStarted)) {
        m_outputQueues = new BatchClassifierEvent[e.getMaxRunNumber()][e.getMaxSetNumber()];
        
        m_completedSets = new boolean[e.getMaxRunNumber()][e.getMaxSetNumber()];
        
        m_currentBatchIdentifier = new Date();
        m_batchStarted = true;
      }
      

      if (m_outputQueues[(e.getRunNumber() - 1)][(e.getSetNumber() - 1)] == null)
      {

        m_outputQueues[(e.getRunNumber() - 1)][(e.getSetNumber() - 1)] = new BatchClassifierEvent(this, null, null, new DataSetEvent(this, e.getTestSet()), e.getRunNumber(), e.getMaxRunNumber(), e.getSetNumber(), e.getMaxSetNumber());
        



        if ((e.getRunNumber() == e.getMaxRunNumber()) && (e.getSetNumber() == e.getMaxSetNumber()))
        {






          if (e.getMaxSetNumber() != 1) {
            m_block = true;
          }
        }
      }
      else {
        m_outputQueues[(e.getRunNumber() - 1)][(e.getSetNumber() - 1)].setTestSet(new DataSetEvent(this, e.getTestSet()));
        
        checkCompletedRun(e.getRunNumber(), e.getMaxRunNumber(), e.getMaxSetNumber());
      }
    }
  }
  

  private synchronized void classifierTrainingComplete(BatchClassifierEvent ce)
  {
    if (m_listenees.containsKey("testSet")) {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();String msg = Messages.getString("Classifier_AcceptTestSet_Msg_Text_First") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTestSet_Msg_Text_Second") + ce.getRunNumber() + Messages.getString("Classifier_AcceptTestSet_Msg_Text_Third") + ce.getSetNumber();
      







      if (m_log != null) {
        m_log.logMessage(msg);
      } else {
        System.err.println(msg);
      }
      
      if (m_outputQueues[(ce.getRunNumber() - 1)][(ce.getSetNumber() - 1)] == null)
      {
        m_outputQueues[(ce.getRunNumber() - 1)][(ce.getSetNumber() - 1)] = ce;
      }
      else {
        m_outputQueues[(ce.getRunNumber() - 1)][(ce.getSetNumber() - 1)].setClassifier(ce.getClassifier());
        
        m_outputQueues[(ce.getRunNumber() - 1)][(ce.getSetNumber() - 1)].setTrainSet(ce.getTrainSet());
      }
      

      checkCompletedRun(ce.getRunNumber(), ce.getMaxRunNumber(), ce.getMaxSetNumber());
    }
  }
  



  private synchronized void checkCompletedRun(int runNum, int maxRunNum, int maxSets)
  {
    for (int i = 0; i < maxSets; i++) {
      if ((m_outputQueues[(runNum - 1)][i] != null) && 
        (m_outputQueues[(runNum - 1)][i].getClassifier() != null) && (m_outputQueues[(runNum - 1)][i].getTestSet() != null))
      {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();String msg = Messages.getString("Classifier_AcceptTestSet_Msg_Text_Fourth") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTestSet_Msg_Text_Fifth") + runNum + "/" + (i + 1) + Messages.getString("Classifier_AcceptTestSet_Msg_Text_Sixth");
        









        if (m_log != null) {
          m_log.logMessage(msg);
        } else {
          System.err.println(msg);
        }
        

        m_outputQueues[(runNum - 1)][i].setGroupIdentifier(m_currentBatchIdentifier.getTime());
        
        notifyBatchClassifierListeners(m_outputQueues[(runNum - 1)][i]);
        
        m_outputQueues[(runNum - 1)][i] = null;
        
        m_completedSets[(runNum - 1)][i] = 1;
      }
    }
    


    boolean done = true;
    for (int i = 0; i < maxRunNum; i++) {
      for (int j = 0; j < maxSets; j++) {
        if (m_completedSets[i][j] == 0) {
          done = false;
          break;
        }
      }
      if (!done) {
        break;
      }
    }
    
    if (done) {
      Messages.getInstance();Messages.getInstance();String msg = Messages.getString("Classifier_AcceptTestSet_Msg_Text_Seventh") + statusMessagePrefix() + Messages.getString("Classifier_AcceptTestSet_Msg_Text_Eighth");
      





      if (m_log != null) {
        m_log.logMessage(msg);
      } else {
        System.err.println(msg);
      }
      

      if (m_log != null) {
        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_AcceptTestSet_StatusMessage_Text_Second"));
      }
      



      m_batchStarted = false;
      block(false);
      m_block = false;
      m_state = IDLE;
    }
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
    String name = m_ClassifierTemplate.getClass().toString();
    String packageName = name.substring(0, name.lastIndexOf('.'));
    packageName = packageName.substring(packageName.lastIndexOf('.') + 1, packageName.length());
    

    if (!m_visual.loadIcons("weka/gui/beans/icons/Default_" + packageName + "Classifier.gif", "weka/gui/beans/icons/Default_" + packageName + "Classifier_animated.gif"))
    {

      m_visual.loadIcons("weka/gui/beans/icons/DefaultClassifier.gif", "weka/gui/beans/icons/DefaultClassifier_animated.gif");
    }
  }
  





  public synchronized void addBatchClassifierListener(BatchClassifierListener cl)
  {
    m_batchClassifierListeners.addElement(cl);
  }
  





  public synchronized void removeBatchClassifierListener(BatchClassifierListener cl)
  {
    m_batchClassifierListeners.remove(cl);
  }
  






  private void notifyBatchClassifierListeners(BatchClassifierEvent ce)
  {
    if (Thread.currentThread().isInterrupted()) {
      return;
    }
    
    Vector l;
    synchronized (this) {
      l = (Vector)m_batchClassifierListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((BatchClassifierListener)l.elementAt(i)).acceptClassifier(ce);
      }
    }
  }
  




  public synchronized void addGraphListener(GraphListener cl)
  {
    m_graphListeners.addElement(cl);
  }
  




  public synchronized void removeGraphListener(GraphListener cl)
  {
    m_graphListeners.remove(cl);
  }
  


  private void notifyGraphListeners(GraphEvent ge)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_graphListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((GraphListener)l.elementAt(i)).acceptGraph(ge);
      }
    }
  }
  




  public synchronized void addTextListener(TextListener cl)
  {
    m_textListeners.addElement(cl);
  }
  




  public synchronized void removeTextListener(TextListener cl)
  {
    m_textListeners.remove(cl);
  }
  


  private void notifyTextListeners(TextEvent ge)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_textListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((TextListener)l.elementAt(i)).acceptText(ge);
      }
    }
  }
  





  public synchronized void addIncrementalClassifierListener(IncrementalClassifierListener cl)
  {
    m_incrementalClassifierListeners.add(cl);
  }
  





  public synchronized void removeIncrementalClassifierListener(IncrementalClassifierListener cl)
  {
    m_incrementalClassifierListeners.remove(cl);
  }
  








  private void notifyIncrementalClassifierListeners(IncrementalClassifierEvent ce)
  {
    if (Thread.currentThread().isInterrupted()) {
      return;
    }
    
    Vector l;
    synchronized (this) {
      l = (Vector)m_incrementalClassifierListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((IncrementalClassifierListener)l.elementAt(i)).acceptClassifier(ce);
      }
    }
  }
  











  public boolean connectionAllowed(String eventName)
  {
    if (m_listenees.containsKey(eventName)) {
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
    if ((eventName.compareTo("instance") == 0) && 
      (!(m_ClassifierTemplate instanceof UpdateableClassifier)) && 
      (m_log != null)) {
      Messages.getInstance();Messages.getInstance();String msg = statusMessagePrefix() + Messages.getString("Classifier_ConnectionNotification_Msg_Text_First") + m_ClassifierTemplate.getClass().getName() + Messages.getString("Classifier_ConnectionNotification_Msg_Text_Second");
      





      Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_ConnectionNotification_LogMessage_Text_First") + msg);
      

      m_log.statusMessage(msg);
    }
    


    if (connectionAllowed(eventName)) {
      m_listenees.put(eventName, source);
    }
  }
  













  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    m_listenees.remove(eventName);
    if (eventName.compareTo("instance") == 0) {
      stop();
    }
  }
  






  private synchronized void block(boolean tf)
  {
    if (tf) {
      try
      {
        if (m_state != IDLE) {
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
    

    if (m_executorPool != null) {
      m_executorPool.shutdownNow();
      m_executorPool.purge();
      m_executorPool = null;
    }
    m_block = false;
    m_batchStarted = false;
    m_visual.setStatic();
    if (m_oldText.length() > 0) {}
  }
  






  public void loadModel()
  {
    try
    {
      if (m_fileChooser == null)
      {
        setupFileChooser();
      }
      int returnVal = m_fileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        File loadFrom = m_fileChooser.getSelectedFile();
        

        if (m_fileChooser.getFileFilter() == m_binaryFilter) {
          if (!loadFrom.getName().toLowerCase().endsWith(".model")) {
            loadFrom = new File(loadFrom.getParent(), loadFrom.getName() + "." + "model");
          }
          
        }
        else if (m_fileChooser.getFileFilter() == m_KOMLFilter) {
          if (!loadFrom.getName().toLowerCase().endsWith(".komlmodel"))
          {
            loadFrom = new File(loadFrom.getParent(), loadFrom.getName() + ".koml" + "model");
          }
          
        }
        else if ((m_fileChooser.getFileFilter() == m_XStreamFilter) && 
          (!loadFrom.getName().toLowerCase().endsWith(".xstreammodel")))
        {
          loadFrom = new File(loadFrom.getParent(), loadFrom.getName() + ".xstream" + "model");
        }
        



        weka.classifiers.Classifier temp = null;
        Instances tempHeader = null;
        
        if ((KOML.isPresent()) && (loadFrom.getAbsolutePath().toLowerCase().endsWith(".komlmodel")))
        {

          Vector v = (Vector)KOML.read(loadFrom.getAbsolutePath());
          temp = (weka.classifiers.Classifier)v.elementAt(0);
          if (v.size() == 2)
          {
            tempHeader = (Instances)v.elementAt(1);
          }
        } else if ((XStream.isPresent()) && (loadFrom.getAbsolutePath().toLowerCase().endsWith(".xstreammodel")))
        {

          Vector v = (Vector)XStream.read(loadFrom.getAbsolutePath());
          temp = (weka.classifiers.Classifier)v.elementAt(0);
          if (v.size() == 2)
          {
            tempHeader = (Instances)v.elementAt(1);
          }
        }
        else {
          ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(loadFrom)));
          


          temp = (weka.classifiers.Classifier)is.readObject();
          try
          {
            tempHeader = (Instances)is.readObject();
          }
          catch (Exception ex) {}
          

          is.close();
        }
        

        setTrainedClassifier(temp);
        
        m_trainingSet = tempHeader;
        
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_ConnectionNotification_StatusMessage_Text_First"));
          

          Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_ConnectionNotification_LogMessage_Text_Second") + statusMessagePrefix() + Messages.getString("Classifier_ConnectionNotification_LogMessage_Text_Third") + m_Classifier.getClass().toString());
        }
        
      }
      

    }
    catch (Exception ex)
    {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("Classifier_ConnectionNotification_JOptionPane_ShowMessageDialog_Text_First"), Messages.getString("Classifier_ConnectionNotification_JOptionPane_ShowMessageDialog_Text_Second"), 0);
      










      if (m_log != null) {
        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_ConnectionNotification_StatusMessage_Text_Second"));
        

        Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_ConnectionNotification_LogMessage_Text_Fourth") + statusMessagePrefix() + Messages.getString("Classifier_ConnectionNotification_LogMessage_Text_Fifth") + ex.getMessage());
      }
    }
  }
  



  public void saveModel()
  {
    try
    {
      if (m_fileChooser == null)
      {
        setupFileChooser();
      }
      int returnVal = m_fileChooser.showSaveDialog(this);
      if (returnVal == 0) {
        File saveTo = m_fileChooser.getSelectedFile();
        String fn = saveTo.getAbsolutePath();
        if (m_fileChooser.getFileFilter() == m_binaryFilter) {
          if (!fn.toLowerCase().endsWith(".model")) {
            fn = fn + ".model";
          }
        } else if (m_fileChooser.getFileFilter() == m_KOMLFilter) {
          if (!fn.toLowerCase().endsWith(".komlmodel")) {
            fn = fn + ".komlmodel";
          }
        } else if ((m_fileChooser.getFileFilter() == m_XStreamFilter) && 
          (!fn.toLowerCase().endsWith(".xstreammodel")))
        {
          fn = fn + ".xstreammodel";
        }
        
        saveTo = new File(fn);
        


        if ((KOML.isPresent()) && (saveTo.getAbsolutePath().toLowerCase().endsWith(".komlmodel")))
        {

          SerializedModelSaver.saveKOML(saveTo, m_Classifier, m_trainingSet != null ? new Instances(m_trainingSet, 0) : null);





        }
        else if ((XStream.isPresent()) && (saveTo.getAbsolutePath().toLowerCase().endsWith(".xstreammodel")))
        {


          SerializedModelSaver.saveXStream(saveTo, m_Classifier, m_trainingSet != null ? new Instances(m_trainingSet, 0) : null);


        }
        else
        {


          ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(saveTo)));
          

          os.writeObject(m_Classifier);
          if (m_trainingSet != null) {
            Instances header = new Instances(m_trainingSet, 0);
            os.writeObject(header);
          }
          os.close();
        }
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_SaveModel_StatusMessage_Text_First"));
          

          Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_SaveModel_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("Classifier_SaveModel_LogMessage_Text_Second") + getCustomName());
        }
        
      }
      
    }
    catch (Exception ex)
    {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("Classifier_SaveModel_JOptionPane_ShowMessageDialog_Text_First"), Messages.getString("Classifier_SaveModel_JOptionPane_ShowMessageDialog_Text_Second"), 0);
      





      if (m_log != null) {
        Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Classifier_SaveModel_StatusMessage_Text_Second"));
        

        Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Classifier_SaveModel_LogMessage_Text_Third") + statusMessagePrefix() + Messages.getString("Classifier_SaveModel_LogMessage_Text_Fourth") + getCustomName() + ex.getMessage());
      }
    }
  }
  











  public void setLog(Logger logger)
  {
    m_log = logger;
  }
  





  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if ((m_executorPool != null) && ((m_executorPool.getQueue().size() > 0) || (m_executorPool.getActiveCount() > 0)))
    {

      newVector.addElement("Stop");
    }
    
    if (((m_executorPool == null) || ((m_executorPool.getQueue().size() == 0) && (m_executorPool.getActiveCount() == 0))) && (m_Classifier != null))
    {
      newVector.addElement("Save model");
    }
    
    if ((m_executorPool == null) || ((m_executorPool.getQueue().size() == 0) && (m_executorPool.getActiveCount() == 0)))
    {

      newVector.addElement("Load model");
    }
    return newVector.elements();
  }
  






  public void performRequest(String request)
  {
    if (request.compareTo("Stop") == 0) {
      stop();
    } else if (request.compareTo("Save model") == 0) {
      saveModel();
    } else if (request.compareTo("Load model") == 0) {
      loadModel();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("Classifier_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  








  public boolean eventGeneratable(EventSetDescriptor esd)
  {
    String eventName = esd.getName();
    return eventGeneratable(eventName);
  }
  




  private boolean generatableEvent(String eventName)
  {
    if ((eventName.compareTo("graph") == 0) || (eventName.compareTo("text") == 0) || (eventName.compareTo("batchClassifier") == 0) || (eventName.compareTo("incrementalClassifier") == 0))
    {

      return true;
    }
    return false;
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (!generatableEvent(eventName)) {
      return false;
    }
    if (eventName.compareTo("graph") == 0)
    {
      if (!(m_ClassifierTemplate instanceof Drawable)) {
        return false;
      }
      

      if (!m_listenees.containsKey("trainingSet")) {
        return false;
      }
      

      Object source = m_listenees.get("trainingSet");
      if (((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("trainingSet"))) {
        return false;
      }
    }
    

    if (eventName.compareTo("batchClassifier") == 0)
    {




      if ((!m_listenees.containsKey("testSet")) && (!m_listenees.containsKey("trainingSet")))
      {
        return false;
      }
      Object source = m_listenees.get("testSet");
      if (((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("testSet"))) {
        return false;
      }
    }
    







    if (eventName.compareTo("text") == 0) {
      if ((!m_listenees.containsKey("trainingSet")) && (!m_listenees.containsKey("instance")))
      {
        return false;
      }
      Object source = m_listenees.get("trainingSet");
      if ((source != null) && ((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("trainingSet"))) {
        return false;
      }
      
      source = m_listenees.get("instance");
      if ((source != null) && ((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("instance"))) {
        return false;
      }
    }
    

    if (eventName.compareTo("incrementalClassifier") == 0)
    {



      if (!m_listenees.containsKey("instance")) {
        return false;
      }
      Object source = m_listenees.get("instance");
      if (((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("instance"))) {
        return false;
      }
    }
    
    return true;
  }
  






  public boolean isBusy()
  {
    if ((m_executorPool == null) || ((m_executorPool.getQueue().size() == 0) && (m_executorPool.getActiveCount() == 0) && (m_state == IDLE)))
    {

      return false;
    }
    



    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|" + (((m_Classifier instanceof OptionHandler)) && (Utils.joinOptions(m_ClassifierTemplate.getOptions()).length() > 0) ? Utils.joinOptions(m_ClassifierTemplate.getOptions()) + "|" : "");
  }
}

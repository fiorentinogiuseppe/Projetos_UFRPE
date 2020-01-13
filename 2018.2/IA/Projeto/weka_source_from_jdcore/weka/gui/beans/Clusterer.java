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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import weka.clusterers.EM;
import weka.core.Drawable;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.ExtensionFileFilter;
import weka.gui.Logger;





































public class Clusterer
  extends JPanel
  implements BeanCommon, Visible, WekaWrapper, EventConstraints, UserRequestAcceptor, TrainingSetListener, TestSetListener
{
  private static final long serialVersionUID = 7729795159836843810L;
  protected BeanVisual m_visual = new BeanVisual("Clusterer", "weka/gui/beans/icons/EM.gif", "weka/gui/beans/icons/EM_animated.gif");
  



  private static int IDLE = 0;
  private static int BUILDING_MODEL = 1;
  private static int CLUSTERING = 2;
  
  private int m_state = IDLE;
  
  private Thread m_buildThread = null;
  



  protected String m_globalInfo;
  



  private Hashtable m_listenees = new Hashtable();
  



  private Vector m_batchClustererListeners = new Vector();
  




  private Vector m_graphListeners = new Vector();
  



  private Vector m_textListeners = new Vector();
  

  private Instances m_trainingSet;
  
  private transient Instances m_testingSet;
  
  private weka.clusterers.Clusterer m_Clusterer = new EM();
  

  private transient Logger m_log = null;
  
  private Double m_dummy = new Double(0.0D);
  
  private transient JFileChooser m_fileChooser = null;
  




  public String globalInfo()
  {
    return m_globalInfo;
  }
  


  public Clusterer()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
    setClusterer(m_Clusterer);
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public void setClusterer(weka.clusterers.Clusterer c)
  {
    boolean loadImages = true;
    if (c.getClass().getName().compareTo(m_Clusterer.getClass().getName()) == 0)
    {
      loadImages = false;
    }
    else
    {
      m_trainingSet = null;
    }
    m_Clusterer = c;
    String clustererName = c.getClass().toString();
    clustererName = clustererName.substring(clustererName.lastIndexOf('.') + 1, clustererName.length());
    

    if ((loadImages) && 
      (!m_visual.loadIcons("weka/gui/beans/icons/" + clustererName + ".gif", "weka/gui/beans/icons/" + clustererName + "_animated.gif")))
    {
      useDefaultVisual();
    }
    
    m_visual.setText(clustererName);
    

    m_globalInfo = KnowledgeFlowApp.getGlobalInfo(m_Clusterer);
  }
  






  public boolean hasIncomingBatchInstances()
  {
    if (m_listenees.size() == 0) {
      return false;
    }
    if ((m_listenees.containsKey("trainingSet")) || (m_listenees.containsKey("testSet")) || (m_listenees.containsKey("dataSet")))
    {

      return true;
    }
    return false;
  }
  




  public weka.clusterers.Clusterer getClusterer()
  {
    return m_Clusterer;
  }
  







  public void setWrappedAlgorithm(Object algorithm)
  {
    if (!(algorithm instanceof weka.clusterers.Clusterer)) {
      Messages.getInstance();throw new IllegalArgumentException(algorithm.getClass() + Messages.getString("Clusterer_SetWrappedAlgorithm_IllegalArgumentException_Text"));
    }
    setClusterer((weka.clusterers.Clusterer)algorithm);
  }
  




  public Object getWrappedAlgorithm()
  {
    return getClusterer();
  }
  





  public void acceptTrainingSet(final TrainingSetEvent e)
  {
    if (e.isStructureOnly())
    {


      BatchClustererEvent ce = new BatchClustererEvent(this, m_Clusterer, new DataSetEvent(this, e.getTrainingSet()), e.getSetNumber(), e.getMaxSetNumber(), 1);
      



      notifyBatchClustererListeners(ce);
      return;
    }
    if (m_buildThread == null) {
      try {
        if (m_state == IDLE) {
          synchronized (this) {
            m_state = BUILDING_MODEL;
          }
          m_trainingSet = e.getTrainingSet();
          
          m_buildThread = new Thread() {
            public void run() {
              try {
                if (m_trainingSet != null) {
                  m_visual.setAnimated();
                  
                  if (m_log != null) {
                    Messages.getInstance();m_log.statusMessage(Clusterer.this.statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_LogMessage_Text"));
                  }
                  
                  Clusterer.this.buildClusterer();
                  if (m_batchClustererListeners.size() > 0) {
                    BatchClustererEvent ce = new BatchClustererEvent(this, m_Clusterer, new DataSetEvent(this, e.getTrainingSet()), e.getSetNumber(), e.getMaxSetNumber(), 1);
                    


                    Clusterer.this.notifyBatchClustererListeners(ce);
                  }
                  if (((m_Clusterer instanceof Drawable)) && (m_graphListeners.size() > 0))
                  {
                    String grphString = ((Drawable)m_Clusterer).graph();
                    
                    int grphType = ((Drawable)m_Clusterer).graphType();
                    String grphTitle = m_Clusterer.getClass().getName();
                    grphTitle = grphTitle.substring(grphTitle.lastIndexOf('.') + 1, grphTitle.length());
                    

                    Messages.getInstance();grphTitle = Messages.getString("Clusterer_AcceptTrainingSet_GrphTitle_Text_First") + e.getSetNumber() + " (" + e.getTrainingSet().relationName() + ") " + grphTitle;
                    


                    GraphEvent ge = new GraphEvent(Clusterer.this, grphString, grphTitle, grphType);
                    


                    Clusterer.this.notifyGraphListeners(ge);
                  }
                  
                  if (m_textListeners.size() > 0) {
                    String modelString = m_Clusterer.toString();
                    String titleString = m_Clusterer.getClass().getName();
                    
                    titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
                    

                    Messages.getInstance();Messages.getInstance();Messages.getInstance();modelString = Messages.getString("Clusterer_AcceptTrainingSet_ModelString_Text_First") + titleString + Messages.getString("Clusterer_AcceptTrainingSet_ModelString_Text_Second") + m_trainingSet.relationName() + (e.getMaxSetNumber() > 1 ? Messages.getString("Clusterer_AcceptTrainingSet_ModelString_Text_Third") + e.getSetNumber() : "") + "\n\n" + modelString;
                    





                    Messages.getInstance();titleString = Messages.getString("Clusterer_AcceptTrainingSet_TitleString_Text") + titleString;
                    
                    TextEvent nt = new TextEvent(Clusterer.this, modelString, titleString);
                    

                    Clusterer.this.notifyTextListeners(nt);
                  }
                }
              } catch (Exception ex) {
                stop();
                if (m_log != null) {
                  Messages.getInstance();m_log.statusMessage(Clusterer.this.statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_StatusMessage_Text_First"));
                  
                  Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Clusterer_AcceptTrainingSet_LogMessage_Text_First") + Clusterer.this.statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_LogMessage_Text_First_Alpha") + ex.getMessage());
                }
                
                ex.printStackTrace();
              }
              finally {
                m_visual.setStatic();
                m_state = Clusterer.IDLE;
                if (isInterrupted())
                {
                  m_trainingSet = null;
                  if (m_log != null) {
                    Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Clusterer_AcceptTrainingSet_LogMessage_Text_First") + Clusterer.this.statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_LogMessage_Text_Second"));
                    
                    Messages.getInstance();m_log.statusMessage(Clusterer.this.statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_StatusMessage_Text_Second"));
                  }
                }
                else
                {
                  m_trainingSet = new Instances(m_trainingSet, 0);
                  if (m_log != null) {
                    Messages.getInstance();m_log.statusMessage(Clusterer.this.statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_StatusMessage_Text_Third"));
                  }
                }
                Clusterer.this.block(false);
              }
            }
          };
          m_buildThread.setPriority(1);
          m_buildThread.start();
          

          block(true);
          
          m_buildThread = null;
          m_state = IDLE;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  





  public void acceptTestSet(TestSetEvent e)
  {
    if (m_trainingSet != null) {
      try {
        if (m_state == IDLE) {
          synchronized (this) {
            m_state = CLUSTERING;
          }
          m_testingSet = e.getTestSet();
          if (m_trainingSet.equalHeaders(m_testingSet)) {
            BatchClustererEvent ce = new BatchClustererEvent(this, m_Clusterer, new DataSetEvent(this, e.getTestSet()), e.getSetNumber(), e.getMaxSetNumber(), 0);
            



            notifyBatchClustererListeners(ce);
          }
          
          m_state = IDLE;
        }
      } catch (Exception ex) {
        stop();
        if (m_log != null) {
          Messages.getInstance();m_log.statusMessage(statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_StatusMessage_Text_Fourth"));
          
          Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Clusterer_AcceptTrainingSet_LogMessage_Text_Third") + statusMessagePrefix() + Messages.getString("Clusterer_AcceptTrainingSet_LogMessage_Text_Fourth") + ex.getMessage());
        }
        
        ex.printStackTrace();
      }
    }
  }
  

  private void buildClusterer()
    throws Exception
  {
    if (m_trainingSet.classIndex() < 0) {
      m_Clusterer.buildClusterer(m_trainingSet);
    } else {
      Remove removeClass = new Remove();
      removeClass.setAttributeIndices("" + (m_trainingSet.classIndex() + 1));
      removeClass.setInvertSelection(false);
      removeClass.setInputFormat(m_trainingSet);
      Instances clusterTrain = Filter.useFilter(m_trainingSet, removeClass);
      m_Clusterer.buildClusterer(clusterTrain);
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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultClusterer.gif", "weka/gui/beans/icons/DefaultClusterer_animated.gif");
  }
  






  public synchronized void addBatchClustererListener(BatchClustererListener cl)
  {
    m_batchClustererListeners.addElement(cl);
  }
  





  public synchronized void removeBatchClustererListener(BatchClustererListener cl)
  {
    m_batchClustererListeners.remove(cl);
  }
  


  private void notifyBatchClustererListeners(BatchClustererEvent ce)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_batchClustererListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((BatchClustererListener)l.elementAt(i)).acceptClusterer(ce);
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
    if (connectionAllowed(eventName)) {
      m_listenees.put(eventName, source);
    }
  }
  











  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    m_listenees.remove(eventName);
  }
  







  private synchronized void block(boolean tf)
  {
    if (tf) {
      try
      {
        if ((m_buildThread.isAlive()) && (m_state != IDLE)) {
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
    return m_buildThread != null;
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
    

    if (m_buildThread != null) {
      m_buildThread.interrupt();
      m_buildThread.stop();
      m_buildThread = null;
      m_visual.setStatic();
    }
  }
  




  public void setLog(Logger logger)
  {
    m_log = logger;
  }
  
  public void saveModel() {
    try {
      if (m_fileChooser == null)
      {
        m_fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        
        ExtensionFileFilter ef = new ExtensionFileFilter("model", "Serialized weka clusterer");
        m_fileChooser.setFileFilter(ef);
      }
      int returnVal = m_fileChooser.showSaveDialog(this);
      if (returnVal == 0) {
        File saveTo = m_fileChooser.getSelectedFile();
        String fn = saveTo.getAbsolutePath();
        if (!fn.endsWith(".model")) {
          fn = fn + ".model";
          saveTo = new File(fn);
        }
        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(saveTo)));
        

        os.writeObject(m_Clusterer);
        if (m_trainingSet != null) {
          Instances header = new Instances(m_trainingSet, 0);
          os.writeObject(header);
        }
        os.close();
        if (m_log != null) {
          Messages.getInstance();m_log.logMessage(Messages.getString("Clusterer_SaveModel_LogMessage_Text_Fourth") + getCustomName());
        }
      }
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("Clusterer_SaveModel_JOptionPane_ShowMessageDialog_Text_First"), Messages.getString("Clusterer_SaveModel_JOptionPane_ShowMessageDialog_Text_Second"), 0);
      


      if (m_log != null) {
        Messages.getInstance();m_log.logMessage(Messages.getString("Clusterer_SaveModel_LogMessage_Text_Fifth") + getCustomName() + ex.getMessage());
      }
    }
  }
  
  public void loadModel()
  {
    try {
      if (m_fileChooser == null)
      {
        m_fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        
        ExtensionFileFilter ef = new ExtensionFileFilter("model", "Serialized weka clusterer");
        m_fileChooser.setFileFilter(ef);
      }
      int returnVal = m_fileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        File loadFrom = m_fileChooser.getSelectedFile();
        ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(loadFrom)));
        


        weka.clusterers.Clusterer temp = (weka.clusterers.Clusterer)is.readObject();
        

        setClusterer(temp);
        
        try
        {
          m_trainingSet = ((Instances)is.readObject());
        }
        catch (Exception ex) {}
        
        is.close();
        if (m_log != null) {
          Messages.getInstance();m_log.logMessage(Messages.getString("Clusterer_LoadModel_LogMessage_Text_First") + m_Clusterer.getClass().toString());
        }
      }
    }
    catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("Clusterer_LoadModel_JOptionPane_ShowMessageDialog_Text_First"), Messages.getString("Clusterer_LoadModel_JOptionPane_ShowMessageDialog_Text_Second"), 0);
      


      if (m_log != null) {
        Messages.getInstance();m_log.logMessage(Messages.getString("Clusterer_LoadModel_LogMessage_Text_Second") + ex.getMessage());
      }
    }
  }
  





  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_buildThread != null) {
      newVector.addElement("Stop");
    }
    
    if ((m_buildThread == null) && (m_Clusterer != null))
    {
      newVector.addElement("Save model");
    }
    
    if (m_buildThread == null) {
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
      throw new IllegalArgumentException(request + " not supported (Clusterer)");
    }
  }
  






  public boolean eventGeneratable(EventSetDescriptor esd)
  {
    String eventName = esd.getName();
    return eventGeneratable(eventName);
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (eventName.compareTo("graph") == 0)
    {
      if (!(m_Clusterer instanceof Drawable)) {
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
    

    if (eventName.compareTo("batchClusterer") == 0) {
      if (!m_listenees.containsKey("trainingSet")) {
        return false;
      }
      
      Object source = m_listenees.get("trainingSet");
      if ((source != null) && ((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("trainingSet"))) {
        return false;
      }
    }
    

    if (eventName.compareTo("text") == 0) {
      if (!m_listenees.containsKey("trainingSet")) {
        return false;
      }
      Object source = m_listenees.get("trainingSet");
      if ((source != null) && ((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("trainingSet"))) {
        return false;
      }
    }
    

    if (eventName.compareTo("batchClassifier") == 0)
      return false;
    if (eventName.compareTo("incrementalClassifier") == 0) {
      return false;
    }
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|" + (((m_Clusterer instanceof OptionHandler)) && (Utils.joinOptions(((OptionHandler)m_Clusterer).getOptions()).length() > 0) ? Utils.joinOptions(((OptionHandler)m_Clusterer).getOptions()) + "|" : "");
  }
}

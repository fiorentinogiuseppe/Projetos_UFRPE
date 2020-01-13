package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JPanel;
import weka.associations.Apriori;
import weka.core.Drawable;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.Logger;













































public class Associator
  extends JPanel
  implements BeanCommon, Visible, WekaWrapper, EventConstraints, Serializable, UserRequestAcceptor, DataSourceListener, TrainingSetListener
{
  private static final long serialVersionUID = -7843500322130210057L;
  protected BeanVisual m_visual = new BeanVisual("Associator", "weka/gui/beans/icons/DefaultAssociator.gif", "weka/gui/beans/icons/DefaultAssociator_animated.gif");
  



  private static int IDLE = 0;
  private static int BUILDING_MODEL = 1;
  
  private int m_state = IDLE;
  
  private Thread m_buildThread = null;
  



  protected String m_globalInfo;
  



  private Hashtable m_listenees = new Hashtable();
  



  private Vector m_textListeners = new Vector();
  



  private Vector m_graphListeners = new Vector();
  
  private weka.associations.Associator m_Associator = new Apriori();
  
  private transient Logger m_log = null;
  




  public String globalInfo()
  {
    return m_globalInfo;
  }
  


  public Associator()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
    setAssociator(m_Associator);
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public void setAssociator(weka.associations.Associator c)
  {
    boolean loadImages = true;
    if (c.getClass().getName().compareTo(m_Associator.getClass().getName()) == 0)
    {
      loadImages = false;
    }
    m_Associator = c;
    String associatorName = c.getClass().toString();
    associatorName = associatorName.substring(associatorName.lastIndexOf('.') + 1, associatorName.length());
    

    if ((loadImages) && 
      (!m_visual.loadIcons("weka/gui/beans/icons/" + associatorName + ".gif", "weka/gui/beans/icons/" + associatorName + "_animated.gif")))
    {
      useDefaultVisual();
    }
    
    m_visual.setText(associatorName);
    

    m_globalInfo = KnowledgeFlowApp.getGlobalInfo(m_Associator);
  }
  




  public weka.associations.Associator getAssociator()
  {
    return m_Associator;
  }
  






  public void setWrappedAlgorithm(Object algorithm)
  {
    if (!(algorithm instanceof weka.associations.Associator)) {
      Messages.getInstance();throw new IllegalArgumentException(algorithm.getClass() + Messages.getString("Associator_SetWrappedAlgorithm_IllegalArgumentException_Text"));
    }
    
    setAssociator((weka.associations.Associator)algorithm);
  }
  




  public Object getWrappedAlgorithm()
  {
    return getAssociator();
  }
  





  public void acceptTrainingSet(TrainingSetEvent e)
  {
    Instances trainingSet = e.getTrainingSet();
    DataSetEvent dse = new DataSetEvent(this, trainingSet);
    acceptDataSet(dse);
  }
  
  public void acceptDataSet(final DataSetEvent e) {
    if (e.isStructureOnly())
    {
      return;
    }
    

    if (m_buildThread == null) {
      try {
        if (m_state == IDLE) {
          synchronized (this) {
            m_state = BUILDING_MODEL;
          }
          final Instances trainingData = e.getDataSet();
          
          m_buildThread = new Thread() {
            public void run() {
              try {
                if (trainingData != null) {
                  m_visual.setAnimated();
                  
                  if (m_log != null) {
                    Messages.getInstance();m_log.statusMessage(Associator.this.statusMessagePrefix() + Messages.getString("Associator_AcceptDataSet_StatusMessage_Text_First"));
                  }
                  
                  Associator.this.buildAssociations(trainingData);
                  
                  if (m_textListeners.size() > 0) {
                    String modelString = m_Associator.toString();
                    String titleString = m_Associator.getClass().getName();
                    
                    titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
                    

                    Messages.getInstance();Messages.getInstance();modelString = Messages.getString("Associator_AcceptDataSet_ModelString_Text_First") + Messages.getString("Associator_AcceptDataSet_ModelString_Text_Second") + trainingData.relationName() + "\n\n" + modelString;
                    


                    Messages.getInstance();titleString = Messages.getString("Associator_AcceptDataSet_TitleString_Text_First") + titleString;
                    
                    TextEvent nt = new TextEvent(Associator.this, modelString, titleString);
                    

                    Associator.this.notifyTextListeners(nt);
                  }
                  
                  if (((m_Associator instanceof Drawable)) && (m_graphListeners.size() > 0))
                  {
                    String grphString = ((Drawable)m_Associator).graph();
                    
                    int grphType = ((Drawable)m_Associator).graphType();
                    String grphTitle = m_Associator.getClass().getName();
                    grphTitle = grphTitle.substring(grphTitle.lastIndexOf('.') + 1, grphTitle.length());
                    

                    grphTitle = " (" + e.getDataSet().relationName() + ") " + grphTitle;
                    


                    GraphEvent ge = new GraphEvent(Associator.this, grphString, grphTitle, grphType);
                    


                    Associator.this.notifyGraphListeners(ge);
                  }
                }
              } catch (Exception ex) {
                stop();
                if (m_log != null) {
                  Messages.getInstance();m_log.statusMessage(Associator.this.statusMessagePrefix() + Messages.getString("Associator_AcceptDataSet_StatusMessage_Text_Second"));
                  
                  Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Associator_AcceptDataSet_LogMessage_Text_First") + Associator.this.statusMessagePrefix() + Messages.getString("Associator_AcceptDataSet_LogMessage_Text_Second") + ex.getMessage());
                }
                
                ex.printStackTrace();
              }
              finally {
                m_visual.setStatic();
                m_state = Associator.IDLE;
                if (isInterrupted()) {
                  if (m_log != null) {
                    String titleString = m_Associator.getClass().getName();
                    titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
                    

                    Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("Associator_AcceptDataSet_LogMessage_Text_Third") + Associator.this.statusMessagePrefix() + Messages.getString("Associator_AcceptDataSet_LogMessage_Text_Fourth"));
                    
                    Messages.getInstance();m_log.statusMessage(Associator.this.statusMessagePrefix() + Messages.getString("Associator_AcceptDataSet_StatusMessage_Text_Third"));
                  }
                }
                else if (m_log != null) {
                  Messages.getInstance();m_log.statusMessage(Associator.this.statusMessagePrefix() + Messages.getString("Associator_AcceptDataSet_StatusMessage_Text_Fourth"));
                }
                
                Associator.this.block(false);
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
  
  private void buildAssociations(Instances data)
    throws Exception
  {
    m_Associator.buildAssociations(data);
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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultAssociator.gif", "weka/gui/beans/icons/DefaultAssociator_animated.gif");
  }
  





  public synchronized void addTextListener(TextListener cl)
  {
    m_textListeners.addElement(cl);
  }
  




  public synchronized void removeTextListener(TextListener cl)
  {
    m_textListeners.remove(cl);
  }
  




  public synchronized void addGraphListener(GraphListener cl)
  {
    m_graphListeners.addElement(cl);
  }
  




  public synchronized void removeGraphListener(GraphListener cl)
  {
    m_graphListeners.remove(cl);
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
  




  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_buildThread != null) {
      newVector.addElement("Stop");
    }
    return newVector.elements();
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Stop") == 0) {
      stop();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("Associator_PerformRequest_IllegalArgumentException_Text_First"));
    }
  }
  







  public boolean eventGeneratable(EventSetDescriptor esd)
  {
    String eventName = esd.getName();
    return eventGeneratable(eventName);
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if ((eventName.compareTo("text") == 0) || (eventName.compareTo("graph") == 0))
    {
      if ((!m_listenees.containsKey("dataSet")) && (!m_listenees.containsKey("trainingSet")))
      {
        return false;
      }
      Object source = m_listenees.get("trainingSet");
      if ((source != null) && ((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("trainingSet"))) {
        return false;
      }
      
      source = m_listenees.get("dataSet");
      if ((source != null) && ((source instanceof EventConstraints)) && 
        (!((EventConstraints)source).eventGeneratable("dataSet"))) {
        return false;
      }
      

      if ((eventName.compareTo("graph") == 0) && (!(m_Associator instanceof Drawable)))
      {
        return false;
      }
    }
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|" + (((m_Associator instanceof OptionHandler)) && (Utils.joinOptions(((OptionHandler)m_Associator).getOptions()).length() > 0) ? Utils.joinOptions(((OptionHandler)m_Associator).getOptions()) + "|" : "");
  }
}

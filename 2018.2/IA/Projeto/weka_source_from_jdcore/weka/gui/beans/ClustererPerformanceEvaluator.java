package weka.gui.beans;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instances;
import weka.gui.Logger;






































public class ClustererPerformanceEvaluator
  extends AbstractEvaluator
  implements BatchClustererListener, Serializable, UserRequestAcceptor, EventConstraints
{
  private static final long serialVersionUID = 8041163601333978584L;
  private transient ClusterEvaluation m_eval;
  private transient Clusterer m_clusterer;
  private transient Thread m_evaluateThread = null;
  
  private Vector m_textListeners = new Vector();
  
  public ClustererPerformanceEvaluator() {
    m_visual.loadIcons("weka/gui/beans/icons/ClustererPerformanceEvaluator.gif", "weka/gui/beans/icons/ClustererPerformanceEvaluator_animated.gif");
    


    m_visual.setText("ClustererPerformanceEvaluator");
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("ClustererPerformanceEvaluator_GlobalInfo_Text");
  }
  





  public void acceptClusterer(final BatchClustererEvent ce)
  {
    if (ce.getTestSet().isStructureOnly()) {
      return;
    }
    try {
      if (m_evaluateThread == null) {
        m_evaluateThread = new Thread() {
          public void run() {
            boolean numericClass = false;
            try
            {
              if (ce.getSetNumber() == 1)
              {
                m_eval = new ClusterEvaluation();
                m_clusterer = ce.getClusterer();
                m_eval.setClusterer(m_clusterer);
              }
              
              if (ce.getSetNumber() <= ce.getMaxSetNumber())
              {
                if (m_logger != null) {
                  Messages.getInstance();Messages.getInstance();m_logger.statusMessage(ClustererPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_StatusMessage_Text_First") + ce.getSetNumber() + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_StatusMessage_Text_Second"));
                }
                

                m_visual.setAnimated();
                if ((ce.getTestSet().getDataSet().classIndex() != -1) && (ce.getTestSet().getDataSet().classAttribute().isNumeric())) {
                  numericClass = true;
                  ce.getTestSet().getDataSet().setClassIndex(-1);
                }
                m_eval.evaluateClusterer(ce.getTestSet().getDataSet());
              }
              
              if (ce.getSetNumber() == ce.getMaxSetNumber()) {
                String textTitle = m_clusterer.getClass().getName();
                textTitle = textTitle.substring(textTitle.lastIndexOf('.') + 1, textTitle.length());
                
                String test;
                String test;
                if (ce.getTestOrTrain() == 0) {
                  Messages.getInstance();test = Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_Test_Text_First");
                } else {
                  Messages.getInstance();test = Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_Test_Text_Second"); }
                Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();String resultT = Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_ResultT_Text_First") + test + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_ResultT_Text_Second") + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_ResultT_Text_Third") + textTitle + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_ResultT_Text_Fourth") + ce.getTestSet().getDataSet().relationName() + "\n\n" + m_eval.clusterResultsToString();
                



                if (numericClass) {
                  Messages.getInstance();resultT = resultT + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_ResultT_Text_Fifth"); }
                TextEvent te = new TextEvent(ClustererPerformanceEvaluator.this, resultT, textTitle);
                


                ClustererPerformanceEvaluator.this.notifyTextListeners(te);
                if (m_logger != null) {
                  Messages.getInstance();m_logger.statusMessage(ClustererPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_StatusMessage_Text_Third"));
                }
              }
            }
            catch (Exception ex) {
              stop();
              if (m_logger != null) {
                Messages.getInstance();m_logger.statusMessage(ClustererPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_StatusMessage_Text_Fourth"));
                
                Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_LogMessage_Text_First") + ClustererPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_LogMessage_Text_Second") + ex.getMessage());
              }
              

              ex.printStackTrace();
            }
            finally {
              m_visual.setStatic();
              m_evaluateThread = null;
              if ((isInterrupted()) && 
                (m_logger != null)) {
                Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_LogMessage_Text_Third") + getCustomName() + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_LogMessage_Text_Fourth"));
                
                Messages.getInstance();m_logger.statusMessage(ClustererPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClustererPerformanceEvaluator_AcceptClusterer_LogMessage_Text_Fifth"));
              }
              

              ClustererPerformanceEvaluator.this.block(false);
            }
          }
        };
        m_evaluateThread.setPriority(1);
        m_evaluateThread.start();
        


        block(true);
        
        m_evaluateThread = null;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  





  public boolean isBusy()
  {
    return m_evaluateThread != null;
  }
  



  public void stop()
  {
    if ((m_listenee instanceof BeanCommon))
    {
      ((BeanCommon)m_listenee).stop();
    }
    

    if (m_evaluateThread != null) {
      m_evaluateThread.interrupt();
      m_evaluateThread.stop();
      m_evaluateThread = null;
      m_visual.setStatic();
    }
  }
  






  private synchronized void block(boolean tf)
  {
    if (tf) {
      try
      {
        if ((m_evaluateThread != null) && (m_evaluateThread.isAlive())) {
          wait();
        }
      }
      catch (InterruptedException ex) {}
    } else {
      notifyAll();
    }
  }
  




  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_evaluateThread != null) {
      newVector.addElement("Stop");
    }
    return newVector.elements();
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Stop") == 0) {
      stop();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("ClustererPerformanceEvaluator_PerformRequest_IllegalArgumentException_Text"));
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
  


  private void notifyTextListeners(TextEvent te)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_textListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++)
      {

        ((TextListener)l.elementAt(i)).acceptText(te);
      }
    }
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (m_listenee == null) {
      return false;
    }
    
    if (((m_listenee instanceof EventConstraints)) && 
      (!((EventConstraints)m_listenee).eventGeneratable("batchClusterer")))
    {
      return false;
    }
    
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}

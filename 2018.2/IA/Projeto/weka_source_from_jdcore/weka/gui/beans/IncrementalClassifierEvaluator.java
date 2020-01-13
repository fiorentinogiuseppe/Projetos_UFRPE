package weka.gui.beans;

import java.io.PrintStream;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.Logger;

































public class IncrementalClassifierEvaluator
  extends AbstractEvaluator
  implements IncrementalClassifierListener, EventConstraints
{
  private static final long serialVersionUID = -3105419818939541291L;
  private transient Evaluation m_eval;
  private transient Classifier m_classifier;
  private Vector m_listeners = new Vector();
  private Vector m_textListeners = new Vector();
  
  private Vector m_dataLegend = new Vector();
  
  private ChartEvent m_ce = new ChartEvent(this);
  private double[] m_dataPoint = new double[1];
  private boolean m_reset = false;
  
  private double m_min = Double.MAX_VALUE;
  private double m_max = Double.MIN_VALUE;
  

  private int m_statusFrequency = 100;
  private int m_instanceCount = 0;
  

  private boolean m_outputInfoRetrievalStats = false;
  
  public IncrementalClassifierEvaluator() {
    m_visual.loadIcons("weka/gui/beans/icons/IncrementalClassifierEvaluator.gif", "weka/gui/beans/icons/IncrementalClassifierEvaluator_animated.gif");
    


    m_visual.setText("IncrementalClassifierEvaluator");
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
    Messages.getInstance();return Messages.getString("IncrementalClassifierEvaluator_GlobalInfo_Text");
  }
  




  public void acceptClassifier(IncrementalClassifierEvent ce)
  {
    try
    {
      if (ce.getStatus() == 0)
      {
        m_eval = new Evaluation(ce.getStructure());
        m_eval.useNoPriors();
        
        m_dataLegend = new Vector();
        m_reset = true;
        m_dataPoint = new double[0];
        Instances inst = ce.getStructure();
        Messages.getInstance();System.err.println(Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_Error_Text"));
        m_instanceCount = 0;
        if (m_logger != null) {
          Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_StatusMessage_Text_First"));
          
          Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_LogMessage_Text_Second"));




        }
        





      }
      else
      {




        if ((m_instanceCount > 0) && (m_instanceCount % m_statusFrequency == 0) && 
          (m_logger != null)) {
          Messages.getInstance();Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_StatusMessage_Text_Second") + m_instanceCount + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_StatusMessage_Text_Third"));
        }
        

        m_instanceCount += 1;
        Instance inst = ce.getCurrentInstance();
        
        double[] dist = ce.getClassifier().distributionForInstance(inst);
        double pred = 0.0D;
        if (!inst.isMissing(inst.classIndex())) {
          if (m_outputInfoRetrievalStats)
          {
            m_eval.evaluateModelOnceAndRecordPrediction(dist, inst);
          } else {
            m_eval.evaluateModelOnce(dist, inst);
          }
        } else {
          pred = ce.getClassifier().classifyInstance(inst);
        }
        if (inst.classIndex() >= 0)
        {
          if (inst.attribute(inst.classIndex()).isNominal()) {
            if (!inst.isMissing(inst.classIndex())) {
              if (m_dataPoint.length < 2) {
                m_dataPoint = new double[2];
                m_dataLegend.addElement("Accuracy");
                m_dataLegend.addElement("RMSE (prob)");
              }
              
              m_dataPoint[1] = m_eval.rootMeanSquaredError();






            }
            else if (m_dataPoint.length < 1) {
              m_dataPoint = new double[1];
              m_dataLegend.addElement("Confidence");
            }
            
            double primaryMeasure = 0.0D;
            if (!inst.isMissing(inst.classIndex())) {
              primaryMeasure = 1.0D - m_eval.errorRate();

            }
            else
            {

              primaryMeasure = dist[weka.core.Utils.maxIndex(dist)];
            }
            
            m_dataPoint[0] = primaryMeasure;
            



            m_ce.setLegendText(m_dataLegend);
            m_ce.setMin(0.0D);m_ce.setMax(1.0D);
            m_ce.setDataPoint(m_dataPoint);
            m_ce.setReset(m_reset);
            m_reset = false;
          }
          else {
            if (m_dataPoint.length < 1) {
              m_dataPoint = new double[1];
              if (inst.isMissing(inst.classIndex())) {
                m_dataLegend.addElement("Prediction");
              } else {
                m_dataLegend.addElement("RMSE");
              }
            }
            if (!inst.isMissing(inst.classIndex())) { double update;
              double update;
              if (!inst.isMissing(inst.classIndex())) {
                update = m_eval.rootMeanSquaredError();
              } else {
                update = pred;
              }
              m_dataPoint[0] = update;
              if (update > m_max) {
                m_max = update;
              }
              if (update < m_min) {
                m_min = update;
              }
            }
            
            m_ce.setLegendText(m_dataLegend);
            m_ce.setMin(inst.isMissing(inst.classIndex()) ? m_min : 0.0D);
            

            m_ce.setMax(m_max);
            m_ce.setDataPoint(m_dataPoint);
            m_ce.setReset(m_reset);
            m_reset = false;
          }
          notifyChartListeners(m_ce);
          
          if (ce.getStatus() == 2) {
            if (m_logger != null) {
              Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_LogMessage_Text_Third") + statusMessagePrefix() + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_LogMessage_Text_Fourth"));
              
              Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_StatusMessage_Text_Fourth"));
            }
            if (m_textListeners.size() > 0) {
              String textTitle = ce.getClassifier().getClass().getName();
              textTitle = textTitle.substring(textTitle.lastIndexOf('.') + 1, textTitle.length());
              

              Messages.getInstance();Messages.getInstance();String results = Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_Result_Text_First") + textTitle + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_Result_Text_Second") + inst.dataset().relationName() + "\n\n" + m_eval.toSummaryString();
              

              if ((inst.classIndex() >= 0) && (inst.classAttribute().isNominal()) && (m_outputInfoRetrievalStats))
              {

                results = results + "\n" + m_eval.toClassDetailsString();
              }
              
              if ((inst.classIndex() >= 0) && (inst.classAttribute().isNominal()))
              {
                results = results + "\n" + m_eval.toMatrixString();
              }
              Messages.getInstance();textTitle = Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_TextTitle_Text") + textTitle;
              TextEvent te = new TextEvent(this, results, textTitle);
              


              notifyTextListeners(te);
            }
          }
        }
      }
    } catch (Exception ex) {
      if (m_logger != null) {
        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_LogMessage_Text_Fifth") + statusMessagePrefix() + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_LogMessage_Text_Sixth") + ex.getMessage());
        

        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("IncrementalClassifierEvaluator_AcceptClassifier_StatusMessage_Text_Fifth"));
      }
      
      ex.printStackTrace();
      stop();
    }
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (m_listenee == null) {
      return false;
    }
    
    if (((m_listenee instanceof EventConstraints)) && 
      (!((EventConstraints)m_listenee).eventGeneratable("incrementalClassifier")))
    {
      return false;
    }
    
    return true;
  }
  



  public void stop()
  {
    if ((m_listenee instanceof BeanCommon))
    {
      ((BeanCommon)m_listenee).stop();
    }
  }
  





  public boolean isBusy()
  {
    return false;
  }
  
  private void notifyChartListeners(ChartEvent ce) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_listeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((ChartListener)l.elementAt(i)).acceptDataPoint(ce);
      }
    }
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
  




  public void setStatusFrequency(int s)
  {
    m_statusFrequency = s;
  }
  





  public int getStatusFrequency()
  {
    return m_statusFrequency;
  }
  




  public String statusFrequencyTipText()
  {
    Messages.getInstance();return Messages.getString("IncrementalClassifierEvaluator_StatusFrequencyTipText_Text");
  }
  





  public void setOutputPerClassInfoRetrievalStats(boolean i)
  {
    m_outputInfoRetrievalStats = i;
  }
  




  public boolean getOutputPerClassInfoRetrievalStats()
  {
    return m_outputInfoRetrievalStats;
  }
  




  public String outputPerClassInfoRetrievalStatsTipText()
  {
    Messages.getInstance();return Messages.getString("IncrementalClassifierEvaluator_OutputPerClassInfoRetrievalStatsTipText_Text");
  }
  




  public synchronized void addChartListener(ChartListener cl)
  {
    m_listeners.addElement(cl);
  }
  




  public synchronized void removeChartListener(ChartListener cl)
  {
    m_listeners.remove(cl);
  }
  




  public synchronized void addTextListener(TextListener cl)
  {
    m_textListeners.addElement(cl);
  }
  




  public synchronized void removeTextListener(TextListener cl)
  {
    m_textListeners.remove(cl);
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}

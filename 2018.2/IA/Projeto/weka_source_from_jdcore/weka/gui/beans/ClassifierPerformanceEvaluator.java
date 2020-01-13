package weka.gui.beans;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.Logger;
import weka.gui.explorer.ClassifierPanel;
import weka.gui.visualize.PlotData2D;


































public class ClassifierPerformanceEvaluator
  extends AbstractEvaluator
  implements BatchClassifierListener, Serializable, UserRequestAcceptor, EventConstraints
{
  private static final long serialVersionUID = -3511801418192148690L;
  private transient Evaluation m_eval;
  private transient Thread m_evaluateThread = null;
  
  private transient long m_currentBatchIdentifier;
  
  private transient int m_setsComplete;
  private Vector m_textListeners = new Vector();
  private Vector m_thresholdListeners = new Vector();
  private Vector m_visualizableErrorListeners = new Vector();
  
  public ClassifierPerformanceEvaluator() {
    m_visual.loadIcons("weka/gui/beans/icons/ClassifierPerformanceEvaluator.gif", "weka/gui/beans/icons/ClassifierPerformanceEvaluator_animated.gif");
    


    m_visual.setText("ClassifierPerformanceEvaluator");
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
    Messages.getInstance();return Messages.getString("ClassifierPerformanceEvaluator_GlobalInfo_Text");
  }
  

  private boolean m_rocListenersConnected = false;
  
  private transient Instances m_predInstances = null;
  
  private transient FastVector m_plotShape = null;
  private transient FastVector m_plotSize = null;
  




  public void acceptClassifier(final BatchClassifierEvent ce)
  {
    if ((ce.getTestSet() == null) || (ce.getTestSet().isStructureOnly())) {
      return;
    }
    try {
      if (m_evaluateThread == null) {
        m_evaluateThread = new Thread() {
          public void run() {
            boolean errorOccurred = false;
            
            Classifier classifier = ce.getClassifier();
            try
            {
              if (ce.getGroupIdentifier() != m_currentBatchIdentifier) {
                if ((ce.getTrainSet().getDataSet() == null) || (ce.getTrainSet().getDataSet().numInstances() == 0))
                {


                  m_eval = new Evaluation(ce.getTestSet().getDataSet());
                  m_eval.useNoPriors();
                } else {
                  m_eval = new Evaluation(ce.getTrainSet().getDataSet());
                }
                

                if (m_visualizableErrorListeners.size() > 0) {
                  m_predInstances = ClassifierPanel.setUpVisualizableInstances(new Instances(ce.getTestSet().getDataSet()));
                  

                  m_plotShape = new FastVector();
                  m_plotSize = new FastVector();
                }
                
                m_currentBatchIdentifier = ce.getGroupIdentifier();
                m_setsComplete = 0;
              }
              
              if (m_setsComplete < ce.getMaxSetNumber()) {
                if ((ce.getTrainSet().getDataSet() != null) && (ce.getTrainSet().getDataSet().numInstances() > 0))
                {

                  m_eval.setPriors(ce.getTrainSet().getDataSet());
                }
                

                if (m_logger != null) {
                  Messages.getInstance();Messages.getInstance();m_logger.statusMessage(ClassifierPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_Visual_SetText_Text_First") + ce.getSetNumber() + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_Visual_SetText_Text_Second"));
                }
                

                m_visual.setAnimated();
                


                for (int i = 0; i < ce.getTestSet().getDataSet().numInstances(); i++) {
                  Instance temp = ce.getTestSet().getDataSet().instance(i);
                  ClassifierPanel.processClassifierPrediction(temp, ce.getClassifier(), m_eval, m_predInstances, m_plotShape, m_plotSize);
                }
                



                ClassifierPerformanceEvaluator.access$608(ClassifierPerformanceEvaluator.this);
              }
              
              if (ce.getSetNumber() == ce.getMaxSetNumber())
              {


                String textTitle = classifier.getClass().getName();
                String textOptions = "";
                if ((classifier instanceof OptionHandler)) {
                  textOptions = Utils.joinOptions(classifier.getOptions());
                }
                
                textTitle = textTitle.substring(textTitle.lastIndexOf('.') + 1, textTitle.length());
                

                Messages.getInstance();Messages.getInstance();Messages.getInstance();String resultT = Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_ResultT_Text_First") + textTitle + "\n" + (textOptions.length() > 0 ? Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_ResultT_Text_Second") + textOptions + "\n" : "") + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_ResultT_Text_Third") + ce.getTestSet().getDataSet().relationName() + "\n\n" + m_eval.toSummaryString();
                



                if (ce.getTestSet().getDataSet().classAttribute().isNominal())
                {
                  resultT = resultT + "\n" + m_eval.toClassDetailsString() + "\n" + m_eval.toMatrixString();
                }
                

                TextEvent te = new TextEvent(ClassifierPerformanceEvaluator.this, resultT, textTitle);
                


                ClassifierPerformanceEvaluator.this.notifyTextListeners(te);
                

                if (m_visualizableErrorListeners.size() > 0) {
                  PlotData2D errorD = new PlotData2D(m_predInstances);
                  errorD.setShapeSize(m_plotSize);
                  errorD.setShapeType(m_plotShape);
                  errorD.setPlotName(textTitle + " " + textOptions + " (" + ce.getTestSet().getDataSet().relationName() + ")");
                  

                  errorD.addInstanceNumberAttribute();
                  VisualizableErrorEvent vel = new VisualizableErrorEvent(ClassifierPerformanceEvaluator.this, errorD);
                  

                  ClassifierPerformanceEvaluator.this.notifyVisualizableErrorListeners(vel);
                }
                

                if ((ce.getTestSet().getDataSet().classAttribute().isNominal()) && (m_thresholdListeners.size() > 0))
                {
                  ThresholdCurve tc = new ThresholdCurve();
                  Instances result = tc.getCurve(m_eval.predictions(), 0);
                  result.setRelationName(ce.getTestSet().getDataSet().relationName());
                  
                  PlotData2D pd = new PlotData2D(result);
                  Messages.getInstance();String htmlTitle = Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_HtmlTitle_Text_First") + textTitle;
                  
                  String newOptions = "";
                  if ((classifier instanceof OptionHandler)) {
                    String[] options = classifier.getOptions();
                    
                    if (options.length > 0) {
                      for (int ii = 0; ii < options.length; ii++) {
                        if (options[ii].length() != 0)
                        {

                          if ((options[ii].charAt(0) == '-') && ((options[ii].charAt(1) < '0') || (options[ii].charAt(1) > '9')))
                          {

                            newOptions = newOptions + "<br>";
                          }
                          newOptions = newOptions + options[ii];
                        }
                      }
                    }
                  }
                  Messages.getInstance();Messages.getInstance();htmlTitle = htmlTitle + " " + newOptions + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_HtmlTitle_Text_Second") + ce.getTestSet().getDataSet().classAttribute().value(0) + ")" + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_HtmlTitle_Text_Third");
                  



                  Messages.getInstance();Messages.getInstance();pd.setPlotName(textTitle + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_HtmlTitle_Text_Fourth") + ce.getTestSet().getDataSet().classAttribute().value(0) + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_HtmlTitle_Text_Fifth"));
                  

                  pd.setPlotNameHTML(htmlTitle);
                  boolean[] connectPoints = new boolean[result.numInstances()];
                  
                  for (int jj = 1; jj < connectPoints.length; jj++) {
                    connectPoints[jj] = true;
                  }
                  pd.setConnectPoints(connectPoints);
                  ThresholdDataEvent rde = new ThresholdDataEvent(ClassifierPerformanceEvaluator.this, pd, ce.getTestSet().getDataSet().classAttribute());
                  

                  ClassifierPerformanceEvaluator.this.notifyThresholdListeners(rde);
                }
                



                if (m_logger != null) {
                  Messages.getInstance();m_logger.statusMessage(ClassifierPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_StatusMessage_Text_Third"));
                }
                

                m_predInstances = null;
                m_plotShape = null;
                m_plotSize = null;
              }
            } catch (Exception ex) {
              errorOccurred = true;
              stop();
              if (m_logger != null) {
                Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_LogMessage_Text_First") + ClassifierPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_LogMessage_Text_Second") + ex.getMessage());
              }
              


              ex.printStackTrace();
            }
            finally {
              m_visual.setStatic();
              m_evaluateThread = null;
              
              if (m_logger != null) {
                if (errorOccurred) {
                  Messages.getInstance();m_logger.statusMessage(ClassifierPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_StatusMessage_Text_Fourth"));
                }
                else if (isInterrupted()) {
                  Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_LogMessage_Text_Third") + getCustomName() + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_LogMessage_Text_Fourth"));
                  Messages.getInstance();m_logger.statusMessage(ClassifierPerformanceEvaluator.this.statusMessagePrefix() + Messages.getString("ClassifierPerformanceEvaluator_AcceptClassifier_StatusMessage_Text_Fifth"));
                }
              }
              
              ClassifierPerformanceEvaluator.this.block(false);
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
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("ClassifierPerformanceEvaluator_PerformRequest_IllegalArgumentException_Text"));
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
  




  public synchronized void addThresholdDataListener(ThresholdDataListener cl)
  {
    m_thresholdListeners.addElement(cl);
  }
  




  public synchronized void removeThresholdDataListener(ThresholdDataListener cl)
  {
    m_thresholdListeners.remove(cl);
  }
  




  public synchronized void addVisualizableErrorListener(VisualizableErrorListener vel)
  {
    m_visualizableErrorListeners.add(vel);
  }
  




  public synchronized void removeVisualizableErrorListener(VisualizableErrorListener vel)
  {
    m_visualizableErrorListeners.remove(vel);
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
  


  private void notifyThresholdListeners(ThresholdDataEvent re)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_thresholdListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++)
      {

        ((ThresholdDataListener)l.elementAt(i)).acceptDataSet(re);
      }
    }
  }
  


  private void notifyVisualizableErrorListeners(VisualizableErrorEvent re)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_visualizableErrorListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++)
      {

        ((VisualizableErrorListener)l.elementAt(i)).acceptDataSet(re);
      }
    }
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (m_listenee == null) {
      return false;
    }
    
    if (((m_listenee instanceof EventConstraints)) && 
      (!((EventConstraints)m_listenee).eventGeneratable("batchClassifier")))
    {
      return false;
    }
    
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}

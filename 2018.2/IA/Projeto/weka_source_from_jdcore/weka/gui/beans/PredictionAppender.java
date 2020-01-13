package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JPanel;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.clusterers.DensityBasedClusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.gui.Logger;

































public class PredictionAppender
  extends JPanel
  implements DataSource, TrainingSetProducer, TestSetProducer, Visible, BeanCommon, EventConstraints, BatchClassifierListener, IncrementalClassifierListener, BatchClustererListener, Serializable
{
  private static final long serialVersionUID = -2987740065058976673L;
  protected Vector m_dataSourceListeners = new Vector();
  



  protected Vector m_instanceListeners = new Vector();
  



  protected Vector m_trainingSetListeners = new Vector();
  



  protected Vector m_testSetListeners = new Vector();
  



  protected Object m_listenee = null;
  


  protected Instances m_format;
  

  protected BeanVisual m_visual = new BeanVisual("PredictionAppender", "weka/gui/beans/icons/PredictionAppender.gif", "weka/gui/beans/icons/PredictionAppender_animated.gif");
  


  protected boolean m_appendProbabilities;
  


  protected transient Logger m_logger;
  


  protected InstanceEvent m_instanceEvent;
  



  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("PredictionAppender_GlobalInfo_Text");
  }
  


  public PredictionAppender()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public String appendPredictedProbabilitiesTipText()
  {
    Messages.getInstance();return Messages.getString("PredictionAppender_AppendPredictedProbabilitiesTipText_Text");
  }
  





  public boolean getAppendPredictedProbabilities()
  {
    return m_appendProbabilities;
  }
  





  public void setAppendPredictedProbabilities(boolean ap)
  {
    m_appendProbabilities = ap;
  }
  





  public void addTrainingSetListener(TrainingSetListener tsl)
  {
    m_trainingSetListeners.addElement(tsl);
    
    if (m_format != null) {
      TrainingSetEvent e = new TrainingSetEvent(this, m_format);
      tsl.acceptTrainingSet(e);
    }
  }
  




  public void removeTrainingSetListener(TrainingSetListener tsl)
  {
    m_trainingSetListeners.removeElement(tsl);
  }
  




  public void addTestSetListener(TestSetListener tsl)
  {
    m_testSetListeners.addElement(tsl);
    
    if (m_format != null) {
      TestSetEvent e = new TestSetEvent(this, m_format);
      tsl.acceptTestSet(e);
    }
  }
  




  public void removeTestSetListener(TestSetListener tsl)
  {
    m_testSetListeners.removeElement(tsl);
  }
  




  public synchronized void addDataSourceListener(DataSourceListener dsl)
  {
    m_dataSourceListeners.addElement(dsl);
    
    if (m_format != null) {
      DataSetEvent e = new DataSetEvent(this, m_format);
      dsl.acceptDataSet(e);
    }
  }
  




  public synchronized void removeDataSourceListener(DataSourceListener dsl)
  {
    m_dataSourceListeners.remove(dsl);
  }
  




  public synchronized void addInstanceListener(InstanceListener dsl)
  {
    m_instanceListeners.addElement(dsl);
    
    if (m_format != null) {
      InstanceEvent e = new InstanceEvent(this, m_format);
      dsl.acceptInstance(e);
    }
  }
  




  public synchronized void removeInstanceListener(InstanceListener dsl)
  {
    m_instanceListeners.remove(dsl);
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
    m_visual.loadIcons("weka/gui/beans/icons/PredictionAppender.gif", "weka/gui/beans/icons/PredictionAppender_animated.gif");
  }
  








  public void acceptClassifier(IncrementalClassifierEvent e)
  {
    Classifier classifier = e.getClassifier();
    Instance currentI = e.getCurrentInstance();
    int status = e.getStatus();
    int oldNumAtts = 0;
    if (status == 0) {
      oldNumAtts = e.getStructure().numAttributes();
    } else {
      oldNumAtts = currentI.dataset().numAttributes();
    }
    if (status == 0) {
      m_instanceEvent = new InstanceEvent(this, null, 0);
      
      Instances oldStructure = new Instances(e.getStructure(), 0);
      

      String relationNameModifier = "_with predictions";
      
      if ((!m_appendProbabilities) || (oldStructure.classAttribute().isNumeric())) {
        try
        {
          m_format = makeDataSetClass(oldStructure, classifier, relationNameModifier);
        }
        catch (Exception ex) {
          ex.printStackTrace();
          return;
        }
      } else if (m_appendProbabilities) {
        try {
          m_format = makeDataSetProbabilities(oldStructure, classifier, relationNameModifier);
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          return;
        }
      }
      
      m_instanceEvent.setStructure(m_format);
      notifyInstanceAvailable(m_instanceEvent);
      return;
    }
    
    double[] instanceVals = new double[m_format.numAttributes()];
    
    try
    {
      for (int i = 0; i < oldNumAtts; i++) {
        instanceVals[i] = currentI.value(i);
      }
      if ((!m_appendProbabilities) || (currentI.dataset().classAttribute().isNumeric()))
      {
        double predClass = classifier.classifyInstance(currentI);
        
        instanceVals[(instanceVals.length - 1)] = predClass;
      } else if (m_appendProbabilities) {
        double[] preds = classifier.distributionForInstance(currentI);
        for (int i = oldNumAtts; i < instanceVals.length; i++) {
          instanceVals[i] = preds[(i - oldNumAtts)];
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    } finally {
      Instance newInst = new Instance(currentI.weight(), instanceVals);
      newInst.setDataset(m_format);
      m_instanceEvent.setInstance(newInst);
      m_instanceEvent.setStatus(status);
      
      notifyInstanceAvailable(m_instanceEvent);
    }
    
    if (status == 2)
    {

      m_instanceEvent = null;
    }
  }
  




  public void acceptClassifier(BatchClassifierEvent e)
  {
    if ((m_dataSourceListeners.size() > 0) || (m_trainingSetListeners.size() > 0) || (m_testSetListeners.size() > 0))
    {


      if (e.getTestSet() == null)
      {
        return;
      }
      
      Instances testSet = e.getTestSet().getDataSet();
      Instances trainSet = e.getTrainSet().getDataSet();
      int setNum = e.getSetNumber();
      int maxNum = e.getMaxSetNumber();
      
      Classifier classifier = e.getClassifier();
      String relationNameModifier = "_set_" + e.getSetNumber() + "_of_" + e.getMaxSetNumber();
      
      if ((!m_appendProbabilities) || (testSet.classAttribute().isNumeric())) {
        try {
          Instances newTestSetInstances = makeDataSetClass(testSet, classifier, relationNameModifier);
          
          Instances newTrainingSetInstances = makeDataSetClass(trainSet, classifier, relationNameModifier);
          

          if (m_trainingSetListeners.size() > 0) {
            TrainingSetEvent tse = new TrainingSetEvent(this, new Instances(newTrainingSetInstances, 0));
            
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTrainingSetAvailable(tse);
            
            for (int i = 0; i < trainSet.numInstances(); i++) {
              double predClass = classifier.classifyInstance(trainSet.instance(i));
              
              newTrainingSetInstances.instance(i).setValue(newTrainingSetInstances.numAttributes() - 1, predClass);
            }
            
            tse = new TrainingSetEvent(this, newTrainingSetInstances);
            
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTrainingSetAvailable(tse);
          }
          
          if (m_testSetListeners.size() > 0) {
            TestSetEvent tse = new TestSetEvent(this, new Instances(newTestSetInstances, 0));
            
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTestSetAvailable(tse);
          }
          if (m_dataSourceListeners.size() > 0) {
            notifyDataSetAvailable(new DataSetEvent(this, new Instances(newTestSetInstances, 0)));
          }
          if (e.getTestSet().isStructureOnly()) {
            m_format = newTestSetInstances;
          }
          if ((m_dataSourceListeners.size() > 0) || (m_testSetListeners.size() > 0))
          {
            for (int i = 0; i < testSet.numInstances(); i++) {
              double predClass = classifier.classifyInstance(testSet.instance(i));
              
              newTestSetInstances.instance(i).setValue(newTestSetInstances.numAttributes() - 1, predClass);
            }
          }
          

          if (m_testSetListeners.size() > 0) {
            TestSetEvent tse = new TestSetEvent(this, newTestSetInstances);
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTestSetAvailable(tse);
          }
          if (m_dataSourceListeners.size() > 0) {
            notifyDataSetAvailable(new DataSetEvent(this, newTestSetInstances));
          }
          return;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if (m_appendProbabilities) {
        try {
          Instances newTestSetInstances = makeDataSetProbabilities(testSet, classifier, relationNameModifier);
          

          Instances newTrainingSetInstances = makeDataSetProbabilities(trainSet, classifier, relationNameModifier);
          

          if (m_trainingSetListeners.size() > 0) {
            TrainingSetEvent tse = new TrainingSetEvent(this, new Instances(newTrainingSetInstances, 0));
            
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTrainingSetAvailable(tse);
            
            for (int i = 0; i < trainSet.numInstances(); i++) {
              double[] preds = classifier.distributionForInstance(trainSet.instance(i));
              
              for (int j = 0; j < trainSet.classAttribute().numValues(); j++) {
                newTrainingSetInstances.instance(i).setValue(trainSet.numAttributes() + j, preds[j]);
              }
            }
            
            tse = new TrainingSetEvent(this, newTrainingSetInstances);
            
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTrainingSetAvailable(tse);
          }
          if (m_testSetListeners.size() > 0) {
            TestSetEvent tse = new TestSetEvent(this, new Instances(newTestSetInstances, 0));
            
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTestSetAvailable(tse);
          }
          if (m_dataSourceListeners.size() > 0) {
            notifyDataSetAvailable(new DataSetEvent(this, new Instances(newTestSetInstances, 0)));
          }
          if (e.getTestSet().isStructureOnly()) {
            m_format = newTestSetInstances;
          }
          if ((m_dataSourceListeners.size() > 0) || (m_testSetListeners.size() > 0))
          {
            for (int i = 0; i < testSet.numInstances(); i++) {
              double[] preds = classifier.distributionForInstance(testSet.instance(i));
              
              for (int j = 0; j < testSet.classAttribute().numValues(); j++) {
                newTestSetInstances.instance(i).setValue(testSet.numAttributes() + j, preds[j]);
              }
            }
          }
          


          if (m_testSetListeners.size() > 0) {
            TestSetEvent tse = new TestSetEvent(this, newTestSetInstances);
            m_setNumber = setNum;
            m_maxSetNumber = maxNum;
            notifyTestSetAvailable(tse);
          }
          if (m_dataSourceListeners.size() > 0) {
            notifyDataSetAvailable(new DataSetEvent(this, newTestSetInstances));
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }
  





  public void acceptClusterer(BatchClustererEvent e)
  {
    if ((m_dataSourceListeners.size() > 0) || (m_trainingSetListeners.size() > 0) || (m_testSetListeners.size() > 0))
    {


      if (e.getTestSet().isStructureOnly()) {
        return;
      }
      Instances testSet = e.getTestSet().getDataSet();
      
      Clusterer clusterer = e.getClusterer();
      String test;
      String test; if (e.getTestOrTrain() == 0) {
        test = "test";
      } else {
        test = "training";
      }
      String relationNameModifier = "_" + test + "_" + e.getSetNumber() + "_of_" + e.getMaxSetNumber();
      
      if ((!m_appendProbabilities) || (!(clusterer instanceof DensityBasedClusterer))) {
        if ((m_appendProbabilities) && (!(clusterer instanceof DensityBasedClusterer))) {
          Messages.getInstance();System.err.println(Messages.getString("PredictionAppender_AcceptClusterer_Error_Text_First"));
          if (m_logger != null) {
            Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("PredictionAppender_AcceptClusterer_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("PredictionAppender_AcceptClusterer_LogMessage_Text_Second"));
            
            Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("PredictionAppender_AcceptClusterer_StatusMessage_Text_First"));
          }
        }
        try
        {
          Instances newInstances = makeClusterDataSetClass(testSet, clusterer, relationNameModifier);
          


          if (m_dataSourceListeners.size() > 0) {
            notifyDataSetAvailable(new DataSetEvent(this, new Instances(newInstances, 0)));
          }
          
          if ((m_trainingSetListeners.size() > 0) && (e.getTestOrTrain() > 0)) {
            TrainingSetEvent tse = new TrainingSetEvent(this, new Instances(newInstances, 0));
            
            m_setNumber = e.getSetNumber();
            m_maxSetNumber = e.getMaxSetNumber();
            notifyTrainingSetAvailable(tse);
          }
          
          if ((m_testSetListeners.size() > 0) && (e.getTestOrTrain() == 0)) {
            TestSetEvent tse = new TestSetEvent(this, new Instances(newInstances, 0));
            
            m_setNumber = e.getSetNumber();
            m_maxSetNumber = e.getMaxSetNumber();
            notifyTestSetAvailable(tse);
          }
          

          for (int i = 0; i < testSet.numInstances(); i++) {
            double predCluster = clusterer.clusterInstance(testSet.instance(i));
            
            newInstances.instance(i).setValue(newInstances.numAttributes() - 1, predCluster);
          }
          

          if (m_dataSourceListeners.size() > 0) {
            notifyDataSetAvailable(new DataSetEvent(this, newInstances));
          }
          if ((m_trainingSetListeners.size() > 0) && (e.getTestOrTrain() > 0)) {
            TrainingSetEvent tse = new TrainingSetEvent(this, newInstances);
            
            m_setNumber = e.getSetNumber();
            m_maxSetNumber = e.getMaxSetNumber();
            notifyTrainingSetAvailable(tse);
          }
          if ((m_testSetListeners.size() > 0) && (e.getTestOrTrain() == 0)) {
            TestSetEvent tse = new TestSetEvent(this, newInstances);
            
            m_setNumber = e.getSetNumber();
            m_maxSetNumber = e.getMaxSetNumber();
            notifyTestSetAvailable(tse);
          }
          
          return;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      else {
        try {
          Instances newInstances = makeClusterDataSetProbabilities(testSet, clusterer, relationNameModifier);
          

          notifyDataSetAvailable(new DataSetEvent(this, new Instances(newInstances, 0)));
          

          for (int i = 0; i < testSet.numInstances(); i++) {
            double[] probs = clusterer.distributionForInstance(testSet.instance(i));
            
            for (int j = 0; j < clusterer.numberOfClusters(); j++) {
              newInstances.instance(i).setValue(testSet.numAttributes() + j, probs[j]);
            }
          }
          

          notifyDataSetAvailable(new DataSetEvent(this, newInstances));
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }
  


  private Instances makeDataSetProbabilities(Instances format, Classifier classifier, String relationNameModifier)
    throws Exception
  {
    String classifierName = classifier.getClass().getName();
    classifierName = classifierName.substring(classifierName.lastIndexOf('.') + 1, classifierName.length());
    
    int numOrigAtts = format.numAttributes();
    Instances newInstances = new Instances(format);
    for (int i = 0; i < format.classAttribute().numValues(); i++) {
      Add addF = new Add();
      
      addF.setAttributeIndex("last");
      addF.setAttributeName(classifierName + "_prob_" + format.classAttribute().value(i));
      addF.setInputFormat(newInstances);
      newInstances = Filter.useFilter(newInstances, addF);
    }
    newInstances.setRelationName(format.relationName() + relationNameModifier);
    return newInstances;
  }
  


  private Instances makeDataSetClass(Instances format, Classifier classifier, String relationNameModifier)
    throws Exception
  {
    Add addF = new Add();
    
    addF.setAttributeIndex("last");
    String classifierName = classifier.getClass().getName();
    classifierName = classifierName.substring(classifierName.lastIndexOf('.') + 1, classifierName.length());
    
    addF.setAttributeName("class_predicted_by: " + classifierName);
    if (format.classAttribute().isNominal()) {
      String classLabels = "";
      Enumeration enu = format.classAttribute().enumerateValues();
      classLabels = classLabels + (String)enu.nextElement();
      while (enu.hasMoreElements()) {
        classLabels = classLabels + "," + (String)enu.nextElement();
      }
      addF.setNominalLabels(classLabels);
    }
    addF.setInputFormat(format);
    

    Instances newInstances = Filter.useFilter(format, addF);
    
    newInstances.setRelationName(format.relationName() + relationNameModifier);
    return newInstances;
  }
  


  private Instances makeClusterDataSetProbabilities(Instances format, Clusterer clusterer, String relationNameModifier)
    throws Exception
  {
    int numOrigAtts = format.numAttributes();
    Instances newInstances = new Instances(format);
    for (int i = 0; i < clusterer.numberOfClusters(); i++) {
      Add addF = new Add();
      
      addF.setAttributeIndex("last");
      addF.setAttributeName("prob_cluster" + i);
      addF.setInputFormat(newInstances);
      newInstances = Filter.useFilter(newInstances, addF);
    }
    newInstances.setRelationName(format.relationName() + relationNameModifier);
    return newInstances;
  }
  


  private Instances makeClusterDataSetClass(Instances format, Clusterer clusterer, String relationNameModifier)
    throws Exception
  {
    Add addF = new Add();
    
    addF.setAttributeIndex("last");
    String clustererName = clusterer.getClass().getName();
    clustererName = clustererName.substring(clustererName.lastIndexOf('.') + 1, clustererName.length());
    
    addF.setAttributeName("assigned_cluster: " + clustererName);
    
    String clusterLabels = "0";
    




    for (int i = 1; i <= clusterer.numberOfClusters() - 1; i++)
      clusterLabels = clusterLabels + "," + i;
    addF.setNominalLabels(clusterLabels);
    
    addF.setInputFormat(format);
    

    Instances newInstances = Filter.useFilter(format, addF);
    
    newInstances.setRelationName(format.relationName() + relationNameModifier);
    return newInstances;
  }
  


  protected void notifyInstanceAvailable(InstanceEvent e)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_instanceListeners.clone();
    }
    
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((InstanceListener)l.elementAt(i)).acceptInstance(e);
      }
    }
  }
  


  protected void notifyDataSetAvailable(DataSetEvent e)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_dataSourceListeners.clone();
    }
    
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((DataSourceListener)l.elementAt(i)).acceptDataSet(e);
      }
    }
  }
  


  protected void notifyTestSetAvailable(TestSetEvent e)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_testSetListeners.clone();
    }
    
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((TestSetListener)l.elementAt(i)).acceptTestSet(e);
      }
    }
  }
  


  protected void notifyTrainingSetAvailable(TrainingSetEvent e)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_trainingSetListeners.clone();
    }
    
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((TrainingSetListener)l.elementAt(i)).acceptTrainingSet(e);
      }
    }
  }
  




  public void setLog(Logger logger)
  {
    m_logger = logger;
  }
  
  public void stop()
  {
    if ((m_listenee instanceof BeanCommon)) {
      ((BeanCommon)m_listenee).stop();
    }
  }
  





  public boolean isBusy()
  {
    return false;
  }
  







  public boolean connectionAllowed(String eventName)
  {
    return m_listenee == null;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  








  public synchronized void connectionNotification(String eventName, Object source)
  {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }
  








  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    if (m_listenee == source) {
      m_listenee = null;
      m_format = null;
    }
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (m_listenee == null) {
      return false;
    }
    
    if ((m_listenee instanceof EventConstraints)) {
      if ((eventName.equals("instance")) && 
        (!((EventConstraints)m_listenee).eventGeneratable("incrementalClassifier")))
      {
        return false;
      }
      
      if ((eventName.equals("dataSet")) || (eventName.equals("trainingSet")) || (eventName.equals("testSet")))
      {

        if (((EventConstraints)m_listenee).eventGeneratable("batchClassifier"))
        {
          return true;
        }
        if (((EventConstraints)m_listenee).eventGeneratable("batchClusterer")) {
          return true;
        }
        return false;
      }
    }
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}

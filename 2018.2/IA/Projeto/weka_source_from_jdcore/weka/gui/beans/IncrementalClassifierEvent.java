package weka.gui.beans;

import java.util.EventObject;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;











































public class IncrementalClassifierEvent
  extends EventObject
{
  private static final long serialVersionUID = 28979464317643232L;
  public static final int NEW_BATCH = 0;
  public static final int WITHIN_BATCH = 1;
  public static final int BATCH_FINISHED = 2;
  private Instances m_structure;
  private int m_status;
  protected Classifier m_classifier;
  protected Instance m_currentInstance;
  
  public IncrementalClassifierEvent(Object source, Classifier scheme, Instance currentI, int status)
  {
    super(source);
    
    m_classifier = scheme;
    m_currentInstance = currentI;
    m_status = status;
  }
  








  public IncrementalClassifierEvent(Object source, Classifier scheme, Instances structure)
  {
    super(source);
    m_structure = structure;
    m_status = 0;
    m_classifier = scheme;
  }
  
  public IncrementalClassifierEvent(Object source) {
    super(source);
  }
  




  public Classifier getClassifier()
  {
    return m_classifier;
  }
  
  public void setClassifier(Classifier c) {
    m_classifier = c;
  }
  




  public Instance getCurrentInstance()
  {
    return m_currentInstance;
  }
  




  public void setCurrentInstance(Instance i)
  {
    m_currentInstance = i;
  }
  




  public int getStatus()
  {
    return m_status;
  }
  




  public void setStatus(int s)
  {
    m_status = s;
  }
  




  public void setStructure(Instances structure)
  {
    m_structure = structure;
    m_currentInstance = null;
    m_status = 0;
  }
  





  public Instances getStructure()
  {
    return m_structure;
  }
}

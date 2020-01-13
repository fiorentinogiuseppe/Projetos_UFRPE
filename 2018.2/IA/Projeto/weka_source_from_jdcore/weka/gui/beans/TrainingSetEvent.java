package weka.gui.beans;

import java.util.EventObject;
import weka.core.Instances;







































public class TrainingSetEvent
  extends EventObject
{
  private static final long serialVersionUID = 5872343811810872662L;
  protected Instances m_trainingSet;
  private boolean m_structureOnly;
  protected int m_runNumber = 1;
  




  protected int m_maxRunNumber = 1;
  




  protected int m_setNumber;
  



  protected int m_maxSetNumber;
  




  public TrainingSetEvent(Object source, Instances trainSet)
  {
    super(source);
    m_trainingSet = trainSet;
    if ((m_trainingSet != null) && (m_trainingSet.numInstances() == 0)) {
      m_structureOnly = true;
    }
  }
  







  public TrainingSetEvent(Object source, Instances trainSet, int setNum, int maxSetNum)
  {
    this(source, trainSet);
    m_setNumber = setNum;
    m_maxSetNumber = maxSetNum;
  }
  










  public TrainingSetEvent(Object source, Instances trainSet, int runNum, int maxRunNum, int setNum, int maxSetNum)
  {
    this(source, trainSet, setNum, maxSetNum);
  }
  







  public Instances getTrainingSet()
  {
    return m_trainingSet;
  }
  




  public int getRunNumber()
  {
    return m_runNumber;
  }
  




  public int getMaxRunNumber()
  {
    return m_maxRunNumber;
  }
  




  public int getSetNumber()
  {
    return m_setNumber;
  }
  




  public int getMaxSetNumber()
  {
    return m_maxSetNumber;
  }
  






  public boolean isStructureOnly()
  {
    return m_structureOnly;
  }
}

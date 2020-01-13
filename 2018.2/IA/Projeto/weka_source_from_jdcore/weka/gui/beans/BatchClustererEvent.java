package weka.gui.beans;

import java.util.EventObject;
import weka.clusterers.Clusterer;

























































public class BatchClustererEvent
  extends EventObject
{
  private static final long serialVersionUID = 7268777944939129714L;
  protected Clusterer m_clusterer;
  protected DataSetEvent m_testSet;
  protected int m_setNumber;
  protected int m_testOrTrain;
  protected int m_maxSetNumber;
  public static int TEST = 0;
  public static int TRAINING = 1;
  









  public BatchClustererEvent(Object source, Clusterer scheme, DataSetEvent tstI, int setNum, int maxSetNum, int testOrTrain)
  {
    super(source);
    
    m_clusterer = scheme;
    m_testSet = tstI;
    m_setNumber = setNum;
    m_maxSetNumber = maxSetNum;
    if (testOrTrain == 0) {
      m_testOrTrain = TEST;
    } else {
      m_testOrTrain = TRAINING;
    }
  }
  




  public Clusterer getClusterer()
  {
    return m_clusterer;
  }
  




  public DataSetEvent getTestSet()
  {
    return m_testSet;
  }
  





  public int getSetNumber()
  {
    return m_setNumber;
  }
  





  public int getMaxSetNumber()
  {
    return m_maxSetNumber;
  }
  




  public int getTestOrTrain()
  {
    return m_testOrTrain;
  }
}

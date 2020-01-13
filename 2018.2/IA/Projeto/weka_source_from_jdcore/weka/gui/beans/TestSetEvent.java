package weka.gui.beans;

import java.util.EventObject;
import weka.core.Instances;







































public class TestSetEvent
  extends EventObject
{
  private static final long serialVersionUID = 8780718708498854231L;
  protected Instances m_testSet;
  private boolean m_structureOnly;
  protected int m_runNumber = 1;
  




  protected int m_maxRunNumber = 1;
  




  protected int m_setNumber;
  



  protected int m_maxSetNumber;
  




  public TestSetEvent(Object source, Instances testSet)
  {
    super(source);
    m_testSet = testSet;
    if ((m_testSet != null) && (m_testSet.numInstances() == 0)) {
      m_structureOnly = true;
    }
  }
  








  public TestSetEvent(Object source, Instances testSet, int setNum, int maxSetNum)
  {
    this(source, testSet);
    m_setNumber = setNum;
    m_maxSetNumber = maxSetNum;
  }
  










  public TestSetEvent(Object source, Instances testSet, int runNum, int maxRunNum, int setNum, int maxSetNum)
  {
    this(source, testSet, setNum, maxSetNum);
  }
  







  public Instances getTestSet()
  {
    return m_testSet;
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

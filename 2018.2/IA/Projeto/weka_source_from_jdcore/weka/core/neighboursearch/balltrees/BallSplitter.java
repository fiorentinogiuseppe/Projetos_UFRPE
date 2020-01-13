package weka.core.neighboursearch.balltrees;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

















































public abstract class BallSplitter
  implements Serializable, OptionHandler, RevisionHandler
{
  protected Instances m_Instances;
  protected EuclideanDistance m_DistanceFunction;
  protected int[] m_Instlist;
  
  public BallSplitter() {}
  
  public BallSplitter(int[] instList, Instances insts, EuclideanDistance e)
  {
    m_Instlist = instList;
    m_Instances = insts;
    m_DistanceFunction = e;
  }
  






  protected void correctlyInitialized()
    throws Exception
  {
    if (m_Instances == null)
      throw new Exception("No instances supplied.");
    if (m_Instlist == null)
      throw new Exception("No instance list supplied.");
    if (m_DistanceFunction == null)
      throw new Exception("No Euclidean distance function supplied.");
    if (m_Instances.numInstances() != m_Instlist.length) {
      throw new Exception("The supplied instance list doesn't seem to match the supplied instances");
    }
  }
  




  public Enumeration listOptions()
  {
    return new Vector().elements();
  }
  





  public void setOptions(String[] options)
    throws Exception
  {}
  




  public String[] getOptions()
  {
    return new String[0];
  }
  







  public abstract void splitNode(BallNode paramBallNode, int paramInt)
    throws Exception;
  






  public void setInstances(Instances inst)
  {
    m_Instances = inst;
  }
  







  public void setInstanceList(int[] instList)
  {
    m_Instlist = instList;
  }
  




  public void setEuclideanDistanceFunction(EuclideanDistance func)
  {
    m_DistanceFunction = func;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

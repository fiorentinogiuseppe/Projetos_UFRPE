package weka.core.neighboursearch.kdtrees;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




























































public abstract class KDTreeNodeSplitter
  implements Serializable, OptionHandler, RevisionHandler
{
  protected Instances m_Instances;
  protected EuclideanDistance m_EuclideanDistance;
  protected int[] m_InstList;
  protected boolean m_NormalizeNodeWidth;
  public static final int MIN = 0;
  public static final int MAX = 1;
  public static final int WIDTH = 2;
  
  public KDTreeNodeSplitter() {}
  
  public KDTreeNodeSplitter(int[] instList, Instances insts, EuclideanDistance e)
  {
    m_InstList = instList;
    m_Instances = insts;
    m_EuclideanDistance = e;
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
  






  protected void correctlyInitialized()
    throws Exception
  {
    if (m_Instances == null)
      throw new Exception("No instances supplied.");
    if (m_InstList == null)
      throw new Exception("No instance list supplied.");
    if (m_EuclideanDistance == null)
      throw new Exception("No Euclidean distance function supplied.");
    if (m_Instances.numInstances() != m_InstList.length) {
      throw new Exception("The supplied instance list doesn't seem to match the supplied instances");
    }
  }
  










  public abstract void splitNode(KDTreeNode paramKDTreeNode, int paramInt, double[][] paramArrayOfDouble1, double[][] paramArrayOfDouble2)
    throws Exception;
  










  public void setInstances(Instances inst)
  {
    m_Instances = inst;
  }
  







  public void setInstanceList(int[] instList)
  {
    m_InstList = instList;
  }
  




  public void setEuclideanDistanceFunction(EuclideanDistance func)
  {
    m_EuclideanDistance = func;
  }
  













  public void setNodeWidthNormalization(boolean normalize)
  {
    m_NormalizeNodeWidth = normalize;
  }
  












  protected int widestDim(double[][] nodeRanges, double[][] universe)
  {
    int classIdx = m_Instances.classIndex();
    double widest = 0.0D;
    int w = -1;
    if (m_NormalizeNodeWidth) {
      for (int i = 0; i < nodeRanges.length; i++) {
        double newWidest = nodeRanges[i][2] / universe[i][2];
        if ((newWidest > widest) && 
          (i != classIdx))
        {
          widest = newWidest;
          w = i;
        }
      }
    } else {
      for (int i = 0; i < nodeRanges.length; i++) {
        if ((nodeRanges[i][2] > widest) && 
          (i != classIdx))
        {
          widest = nodeRanges[i][2];
          w = i;
        }
      }
    }
    return w;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

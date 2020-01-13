package weka.core.neighboursearch.kdtrees;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;













































public class KDTreeNode
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -3660396067582792648L;
  public int m_NodeNumber;
  public KDTreeNode m_Left = null;
  

  public KDTreeNode m_Right = null;
  



  public double m_SplitValue;
  



  public int m_SplitDim;
  



  public double[][] m_NodeRanges;
  



  public double[][] m_NodesRectBounds;
  



  public int m_Start = 0;
  





  public int m_End = 0;
  







  public KDTreeNode() {}
  






  public KDTreeNode(int nodeNum, int startidx, int endidx, double[][] nodeRanges)
  {
    m_NodeNumber = nodeNum;
    m_Start = startidx;m_End = endidx;
    m_NodeRanges = nodeRanges;
  }
  













  public KDTreeNode(int nodeNum, int startidx, int endidx, double[][] nodeRanges, double[][] rectBounds)
  {
    m_NodeNumber = nodeNum;
    m_Start = startidx;m_End = endidx;
    m_NodeRanges = nodeRanges;
    m_NodesRectBounds = rectBounds;
  }
  




  public int getSplitDim()
  {
    return m_SplitDim;
  }
  




  public double getSplitValue()
  {
    return m_SplitValue;
  }
  




  public boolean isALeaf()
  {
    return m_Left == null;
  }
  






  public int numInstances()
  {
    return m_End - m_Start + 1;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

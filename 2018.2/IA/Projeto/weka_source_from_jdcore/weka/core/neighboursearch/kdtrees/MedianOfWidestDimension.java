package weka.core.neighboursearch.kdtrees;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
























































public class MedianOfWidestDimension
  extends KDTreeNodeSplitter
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 1383443320160540663L;
  
  public MedianOfWidestDimension() {}
  
  public String globalInfo()
  {
    return "The class that splits a KDTree node based on the median value of a dimension in which the node's points have the widest spread.\n\nFor more information see also:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Jerome H. Friedman and Jon Luis Bentley and Raphael Ari Finkel");
    result.setValue(TechnicalInformation.Field.YEAR, "1977");
    result.setValue(TechnicalInformation.Field.TITLE, "An Algorithm for Finding Best Matches in Logarithmic Expected Time");
    result.setValue(TechnicalInformation.Field.JOURNAL, "ACM Transactions on Mathematics Software");
    result.setValue(TechnicalInformation.Field.PAGES, "209-226");
    result.setValue(TechnicalInformation.Field.MONTH, "September");
    result.setValue(TechnicalInformation.Field.VOLUME, "3");
    result.setValue(TechnicalInformation.Field.NUMBER, "3");
    
    return result;
  }
  

















  public void splitNode(KDTreeNode node, int numNodesCreated, double[][] nodeRanges, double[][] universe)
    throws Exception
  {
    correctlyInitialized();
    
    int splitDim = widestDim(nodeRanges, universe);
    



    int medianIdxIdx = m_Start + (m_End - m_Start) / 2;
    

    int medianIdx = select(splitDim, m_InstList, m_Start, m_End, (m_End - m_Start) / 2 + 1);
    

    m_SplitDim = splitDim;
    m_SplitValue = m_Instances.instance(m_InstList[medianIdx]).value(splitDim);
    
    m_Left = new KDTreeNode(numNodesCreated + 1, m_Start, medianIdxIdx, m_EuclideanDistance.initializeRanges(m_InstList, m_Start, medianIdxIdx));
    
    m_Right = new KDTreeNode(numNodesCreated + 2, medianIdxIdx + 1, m_End, m_EuclideanDistance.initializeRanges(m_InstList, medianIdxIdx + 1, m_End));
  }
  















  protected int partition(int attIdx, int[] index, int l, int r)
  {
    double pivot = m_Instances.instance(index[((l + r) / 2)]).value(attIdx);
    

    while (l < r) {
      while ((m_Instances.instance(index[l]).value(attIdx) < pivot) && (l < r)) {
        l++;
      }
      while ((m_Instances.instance(index[r]).value(attIdx) > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        int help = index[l];
        index[l] = index[r];
        index[r] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (m_Instances.instance(index[r]).value(attIdx) > pivot)) {
      r--;
    }
    
    return r;
  }
  















  public int select(int attIdx, int[] indices, int left, int right, int k)
  {
    if (left == right) {
      return left;
    }
    int middle = partition(attIdx, indices, left, right);
    if (middle - left + 1 >= k) {
      return select(attIdx, indices, left, middle, k);
    }
    return select(attIdx, indices, middle + 1, right, k - (middle - left + 1));
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

package weka.core.neighboursearch.kdtrees;

import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;























































public class MidPointOfWidestDimension
  extends KDTreeNodeSplitter
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -7617277960046591906L;
  
  public MidPointOfWidestDimension() {}
  
  public String globalInfo()
  {
    return "The class that splits a KDTree node based on the midpoint value of a dimension in which the node's points have the widest spread.\n\nFor more information see also:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.TECHREPORT);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Andrew Moore");
    result.setValue(TechnicalInformation.Field.YEAR, "1991");
    result.setValue(TechnicalInformation.Field.TITLE, "A tutorial on kd-trees");
    result.setValue(TechnicalInformation.Field.HOWPUBLISHED, "Extract from PhD Thesis");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "University of Cambridge Computer Laboratory Technical Report No. 209");
    result.setValue(TechnicalInformation.Field.HTTP, "http://www.autonlab.org/autonweb/14665.html");
    
    return result;
  }
  
















  public void splitNode(KDTreeNode node, int numNodesCreated, double[][] nodeRanges, double[][] universe)
    throws Exception
  {
    correctlyInitialized();
    
    int splitDim = widestDim(nodeRanges, universe);
    
    double splitVal = m_EuclideanDistance.getMiddle(nodeRanges[splitDim]);
    
    int rightStart = rearrangePoints(m_InstList, m_Start, m_End, splitDim, splitVal);
    

    if ((rightStart == m_Start) || (rightStart > m_End)) {
      if (rightStart == m_Start) {
        throw new Exception("Left child is empty in node " + m_NodeNumber + ". Not possible with " + "MidPointofWidestDim splitting method. Please " + "check code.");
      }
      



      throw new Exception("Right child is empty in node " + m_NodeNumber + ". Not possible with " + "MidPointofWidestDim splitting method. Please " + "check code.");
    }
    



    m_SplitDim = splitDim;
    m_SplitValue = splitVal;
    m_Left = new KDTreeNode(numNodesCreated + 1, m_Start, rightStart - 1, m_EuclideanDistance.initializeRanges(m_InstList, m_Start, rightStart - 1));
    

    m_Right = new KDTreeNode(numNodesCreated + 2, rightStart, m_End, m_EuclideanDistance.initializeRanges(m_InstList, rightStart, m_End));
  }
  

















  protected int rearrangePoints(int[] indices, int startidx, int endidx, int splitDim, double splitVal)
  {
    int left = startidx - 1;
    for (int i = startidx; i <= endidx; i++) {
      if (m_EuclideanDistance.valueIsSmallerEqual(m_Instances.instance(indices[i]), splitDim, splitVal))
      {
        left++;
        int tmp = indices[left];
        indices[left] = indices[i];
        indices[i] = tmp;
      }
    }
    return left + 1;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

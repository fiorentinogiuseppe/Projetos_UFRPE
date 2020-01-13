package weka.core.neighboursearch.kdtrees;

import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;























































public class SlidingMidPointOfWidestSide
  extends KDTreeNodeSplitter
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 852857628205680562L;
  protected static double ERR = 0.001D;
  


  public SlidingMidPointOfWidestSide() {}
  

  public String globalInfo()
  {
    return "The class that splits a node into two based on the midpoint value of the dimension in which the node's rectangle is widest. If after splitting one side is empty then it is slided towards the non-empty side until there is at least one point on the empty side.\n\nFor more information see also:\n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MANUAL);
    result.setValue(TechnicalInformation.Field.AUTHOR, "David M. Mount");
    result.setValue(TechnicalInformation.Field.YEAR, "2006");
    result.setValue(TechnicalInformation.Field.TITLE, "ANN Programming Manual");
    result.setValue(TechnicalInformation.Field.ORGANIZATION, "Department of Computer Science, University of Maryland");
    result.setValue(TechnicalInformation.Field.ADDRESS, "College Park, MD, USA");
    
    result.setValue(TechnicalInformation.Field.HTTP, "Available from http://www.cs.umd.edu/~mount/ANN/");
    

    return result;
  }
  


















  public void splitNode(KDTreeNode node, int numNodesCreated, double[][] nodeRanges, double[][] universe)
    throws Exception
  {
    correctlyInitialized();
    
    if (m_NodesRectBounds == null) {
      m_NodesRectBounds = new double[2][m_NodeRanges.length];
      for (int i = 0; i < m_NodeRanges.length; i++) {
        m_NodesRectBounds[0][i] = m_NodeRanges[i][0];
        m_NodesRectBounds[1][i] = m_NodeRanges[i][1];
      }
    }
    

    double maxRectWidth = Double.NEGATIVE_INFINITY;double maxPtWidth = Double.NEGATIVE_INFINITY;
    int splitDim = -1;int classIdx = m_Instances.classIndex();
    
    for (int i = 0; i < m_NodesRectBounds[0].length; i++) {
      if (i != classIdx)
      {
        double tempval = m_NodesRectBounds[1][i] - m_NodesRectBounds[0][i];
        if (m_NormalizeNodeWidth) {
          tempval /= universe[i][2];
        }
        if ((tempval > maxRectWidth) && (m_NodeRanges[i][2] > 0.0D))
          maxRectWidth = tempval;
      }
    }
    for (int i = 0; i < m_NodesRectBounds[0].length; i++) {
      if (i != classIdx)
      {
        double tempval = m_NodesRectBounds[1][i] - m_NodesRectBounds[0][i];
        if (m_NormalizeNodeWidth) {
          tempval /= universe[i][2];
        }
        if ((tempval >= maxRectWidth * (1.0D - ERR)) && (m_NodeRanges[i][2] > 0.0D))
        {
          if (m_NodeRanges[i][2] > maxPtWidth) {
            maxPtWidth = m_NodeRanges[i][2];
            if (m_NormalizeNodeWidth)
              maxPtWidth /= universe[i][2];
            splitDim = i;
          }
        }
      }
    }
    double splitVal = m_NodesRectBounds[0][splitDim] + (m_NodesRectBounds[1][splitDim] - m_NodesRectBounds[0][splitDim]) * 0.5D;
    




    if (splitVal < m_NodeRanges[splitDim][0]) {
      splitVal = m_NodeRanges[splitDim][0];
    } else if (splitVal >= m_NodeRanges[splitDim][1]) {
      splitVal = m_NodeRanges[splitDim][1] - m_NodeRanges[splitDim][2] * 0.001D;
    }
    
    int rightStart = rearrangePoints(m_InstList, m_Start, m_End, splitDim, splitVal);
    

    if ((rightStart == m_Start) || (rightStart > m_End)) {
      if (rightStart == m_Start) {
        throw new Exception("Left child is empty in node " + m_NodeNumber + ". Not possible with " + "SlidingMidPointofWidestSide splitting method. Please " + "check code.");
      }
      


      throw new Exception("Right child is empty in node " + m_NodeNumber + ". Not possible with " + "SlidingMidPointofWidestSide splitting method. Please " + "check code.");
    }
    



    m_SplitDim = splitDim;
    m_SplitValue = splitVal;
    
    double[][] widths = new double[2][m_NodesRectBounds[0].length];
    
    System.arraycopy(m_NodesRectBounds[0], 0, widths[0], 0, m_NodesRectBounds[0].length);
    
    System.arraycopy(m_NodesRectBounds[1], 0, widths[1], 0, m_NodesRectBounds[1].length);
    
    widths[1][splitDim] = splitVal;
    
    m_Left = new KDTreeNode(numNodesCreated + 1, m_Start, rightStart - 1, m_EuclideanDistance.initializeRanges(m_InstList, m_Start, rightStart - 1), widths);
    


    widths = new double[2][m_NodesRectBounds[0].length];
    System.arraycopy(m_NodesRectBounds[0], 0, widths[0], 0, m_NodesRectBounds[0].length);
    
    System.arraycopy(m_NodesRectBounds[1], 0, widths[1], 0, m_NodesRectBounds[1].length);
    
    widths[0][splitDim] = splitVal;
    
    m_Right = new KDTreeNode(numNodesCreated + 2, rightStart, m_End, m_EuclideanDistance.initializeRanges(m_InstList, rightStart, m_End), widths);
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
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

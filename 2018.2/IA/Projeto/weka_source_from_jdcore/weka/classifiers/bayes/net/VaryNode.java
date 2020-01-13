package weka.classifiers.bayes.net;

import java.io.PrintStream;
import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




































public class VaryNode
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -6196294370675872424L;
  public int m_iNode;
  public int m_nMCV;
  public ADNode[] m_ADNodes;
  
  public VaryNode(int iNode)
  {
    m_iNode = iNode;
  }
  

















  public void getCounts(int[] nCounts, int[] nNodes, int[] nOffsets, int iNode, int iOffset, ADNode parent, boolean bSubstract)
  {
    int nCurrentNode = nNodes[iNode];
    for (int iValue = 0; iValue < m_ADNodes.length; iValue++) {
      if (iValue != m_nMCV) {
        if (m_ADNodes[iValue] != null) {
          m_ADNodes[iValue].getCounts(nCounts, nNodes, nOffsets, iNode + 1, iOffset + nOffsets[iNode] * iValue, bSubstract);
        }
        

      }
      else
      {

        parent.getCounts(nCounts, nNodes, nOffsets, iNode + 1, iOffset + nOffsets[iNode] * iValue, bSubstract);
        




        for (int iValue2 = 0; iValue2 < m_ADNodes.length; iValue2++) {
          if ((iValue2 != m_nMCV) && (m_ADNodes[iValue2] != null)) {
            m_ADNodes[iValue2].getCounts(nCounts, nNodes, nOffsets, iNode + 1, iOffset + nOffsets[iNode] * iValue, !bSubstract);
          }
        }
      }
    }
  }
  









  public void print(String sTab)
  {
    for (int iValue = 0; iValue < m_ADNodes.length; iValue++) {
      System.out.print(sTab + iValue + ": ");
      if (m_ADNodes[iValue] == null) {
        if (iValue == m_nMCV) {
          System.out.println("MCV");
        } else {
          System.out.println("null");
        }
      } else {
        System.out.println();
        m_ADNodes[iValue].print();
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

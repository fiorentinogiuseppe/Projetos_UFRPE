package weka.classifiers.trees.lmt;

import java.util.Comparator;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;











































class CompareNode
  implements Comparator, RevisionHandler
{
  CompareNode() {}
  
  public int compare(Object o1, Object o2)
  {
    if (m_alpha < m_alpha) return -1;
    if (m_alpha > m_alpha) return 1;
    return 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}

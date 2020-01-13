package weka.classifiers.bayes.net.search.ci;

import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.search.local.LocalScoreSearchAlgorithm;
import weka.core.Instances;
import weka.core.RevisionUtils;


















































public class CISearchAlgorithm
  extends LocalScoreSearchAlgorithm
{
  static final long serialVersionUID = 3165802334119704560L;
  BayesNet m_BayesNet;
  Instances m_instances;
  
  public CISearchAlgorithm() {}
  
  public String globalInfo()
  {
    return "The CISearchAlgorithm class supports Bayes net structure search algorithms that are based on conditional independence test (as opposed to for example score based of cross validation based search algorithms).";
  }
  
















  protected boolean isConditionalIndependent(int iAttributeX, int iAttributeY, int[] iAttributesZ, int nAttributesZ)
  {
    ParentSet oParentSetX = m_BayesNet.getParentSet(iAttributeX);
    
    while (oParentSetX.getNrOfParents() > 0) {
      oParentSetX.deleteLastParent(m_instances);
    }
    

    for (int iAttributeZ = 0; iAttributeZ < nAttributesZ; iAttributeZ++) {
      oParentSetX.addParent(iAttributesZ[iAttributeZ], m_instances);
    }
    
    double fScoreZ = calcNodeScore(iAttributeX);
    double fScoreZY = calcScoreWithExtraParent(iAttributeX, iAttributeY);
    if (fScoreZY <= fScoreZ)
    {


      return true;
    }
    return false;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

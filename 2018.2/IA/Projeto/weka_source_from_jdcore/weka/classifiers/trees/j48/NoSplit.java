package weka.classifiers.trees.j48;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;

































public final class NoSplit
  extends ClassifierSplitModel
{
  private static final long serialVersionUID = -1292620749331337546L;
  
  public NoSplit(Distribution distribution)
  {
    m_distribution = new Distribution(distribution);
    m_numSubsets = 1;
  }
  





  public final void buildClassifier(Instances instances)
    throws Exception
  {
    m_distribution = new Distribution(instances);
    m_numSubsets = 1;
  }
  



  public final int whichSubset(Instance instance)
  {
    return 0;
  }
  



  public final double[] weights(Instance instance)
  {
    return null;
  }
  



  public final String leftSide(Instances instances)
  {
    return "";
  }
  



  public final String rightSide(int index, Instances instances)
  {
    return "";
  }
  








  public final String sourceExpression(int index, Instances data)
  {
    return "true";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}

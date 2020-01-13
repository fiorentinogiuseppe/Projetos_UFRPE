package weka.classifiers.bayes.net.search.fixed;

import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.core.Instances;
import weka.core.RevisionUtils;




































public class NaiveBayes
  extends SearchAlgorithm
{
  static final long serialVersionUID = -4808572519709755811L;
  
  public NaiveBayes() {}
  
  public String globalInfo()
  {
    return "The NaiveBayes class generates a fixed Bayes network structure with arrows from the class variable to each of the attribute variables.";
  }
  







  public void buildStructure(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      if (iAttribute != instances.classIndex()) {
        bayesNet.getParentSet(iAttribute).addParent(instances.classIndex(), instances);
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

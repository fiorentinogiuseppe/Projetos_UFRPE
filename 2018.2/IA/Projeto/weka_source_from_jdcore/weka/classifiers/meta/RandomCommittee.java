package weka.classifiers.meta;

import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
















































































public class RandomCommittee
  extends RandomizableIteratedSingleClassifierEnhancer
  implements WeightedInstancesHandler
{
  static final long serialVersionUID = -9204394360557300092L;
  
  public RandomCommittee()
  {
    m_Classifier = new RandomTree();
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.RandomTree";
  }
  





  public String globalInfo()
  {
    return "Class for building an ensemble of randomizable base classifiers. Each base classifiers is built using a different random number seed (but based one the same data). The final prediction is a straight average of the predictions generated by the individual base classifiers.";
  }
  










  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    if (!(m_Classifier instanceof Randomizable)) {
      throw new IllegalArgumentException("Base learner must implement Randomizable!");
    }
    
    m_Classifiers = Classifier.makeCopies(m_Classifier, m_NumIterations);
    
    Random random = data.getRandomNumberGenerator(m_Seed);
    for (int j = 0; j < m_Classifiers.length; j++)
    {

      ((Randomizable)m_Classifiers[j]).setSeed(random.nextInt());
      

      m_Classifiers[j].buildClassifier(data);
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] sums = new double[instance.numClasses()];
    
    for (int i = 0; i < m_NumIterations; i++) {
      if (instance.classAttribute().isNumeric() == true) {
        sums[0] += m_Classifiers[i].classifyInstance(instance);
      } else {
        double[] newProbs = m_Classifiers[i].distributionForInstance(instance);
        for (int j = 0; j < newProbs.length; j++)
          sums[j] += newProbs[j];
      }
    }
    if (instance.classAttribute().isNumeric() == true) {
      sums[0] /= m_NumIterations;
      return sums; }
    if (Utils.eq(Utils.sum(sums), 0.0D)) {
      return sums;
    }
    Utils.normalize(sums);
    return sums;
  }
  






  public String toString()
  {
    if (m_Classifiers == null) {
      return "RandomCommittee: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("All the base classifiers: \n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append(m_Classifiers[i].toString() + "\n\n");
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new RandomCommittee(), argv);
  }
}

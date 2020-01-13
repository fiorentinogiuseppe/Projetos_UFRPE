package weka.classifiers.trees.j48;

import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;





































public final class NBTreeNoSplit
  extends ClassifierSplitModel
{
  private static final long serialVersionUID = 7824804381545259618L;
  private NaiveBayesUpdateable m_nb;
  private Discretize m_disc;
  private double m_errors;
  
  public NBTreeNoSplit()
  {
    m_numSubsets = 1;
  }
  




  public final void buildClassifier(Instances instances)
    throws Exception
  {
    m_nb = new NaiveBayesUpdateable();
    m_disc = new Discretize();
    m_disc.setInputFormat(instances);
    Instances temp = Filter.useFilter(instances, m_disc);
    m_nb.buildClassifier(temp);
    if (temp.numInstances() >= 5) {
      m_errors = crossValidate(m_nb, temp, new Random(1L));
    }
    m_numSubsets = 1;
  }
  




  public double getErrors()
  {
    return m_errors;
  }
  




  public Discretize getDiscretizer()
  {
    return m_disc;
  }
  




  public NaiveBayesUpdateable getNaiveBayesModel()
  {
    return m_nb;
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
  








  public double classProb(int classIndex, Instance instance, int theSubset)
    throws Exception
  {
    m_disc.input(instance);
    Instance temp = m_disc.output();
    return m_nb.distributionForInstance(temp)[classIndex];
  }
  




  public String toString()
  {
    return m_nb.toString();
  }
  











  public static double crossValidate(NaiveBayesUpdateable fullModel, Instances trainingSet, Random r)
    throws Exception
  {
    Classifier[] copies = Classifier.makeCopies(fullModel, 5);
    Evaluation eval = new Evaluation(trainingSet);
    
    for (int j = 0; j < 5; j++) {
      Instances test = trainingSet.testCV(5, j);
      
      for (int k = 0; k < test.numInstances(); k++) {
        test.instance(k).setWeight(-test.instance(k).weight());
        ((NaiveBayesUpdateable)copies[j]).updateClassifier(test.instance(k));
        
        test.instance(k).setWeight(-test.instance(k).weight());
      }
      eval.evaluateModel(copies[j], test, new Object[0]);
    }
    return eval.incorrect();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

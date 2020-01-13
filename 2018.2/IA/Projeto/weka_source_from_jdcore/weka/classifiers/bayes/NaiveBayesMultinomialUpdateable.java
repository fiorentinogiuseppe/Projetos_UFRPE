package weka.classifiers.bayes;

import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;






































































public class NaiveBayesMultinomialUpdateable
  extends NaiveBayesMultinomial
  implements UpdateableClassifier
{
  private static final long serialVersionUID = -7204398796974263186L;
  protected double[] m_wordsPerClass;
  
  public NaiveBayesMultinomialUpdateable() {}
  
  public String globalInfo()
  {
    return super.globalInfo() + "\n\n" + "Incremental version of the algorithm.";
  }
  







  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    m_headerInfo = new Instances(instances, 0);
    m_numClasses = instances.numClasses();
    m_numAttributes = instances.numAttributes();
    m_probOfWordGivenClass = new double[m_numClasses][];
    m_wordsPerClass = new double[m_numClasses];
    m_probOfClass = new double[m_numClasses];
    



    double laplace = 1.0D;
    for (int c = 0; c < m_numClasses; c++) {
      m_probOfWordGivenClass[c] = new double[m_numAttributes];
      m_probOfClass[c] = laplace;
      m_wordsPerClass[c] = (laplace * m_numAttributes);
      for (int att = 0; att < m_numAttributes; att++) {
        m_probOfWordGivenClass[c][att] = laplace;
      }
    }
    
    for (int i = 0; i < instances.numInstances(); i++) {
      updateClassifier(instances.instance(i));
    }
  }
  




  public void updateClassifier(Instance instance)
    throws Exception
  {
    int classIndex = (int)instance.value(instance.classIndex());
    m_probOfClass[classIndex] += instance.weight();
    
    for (int a = 0; a < instance.numValues(); a++) {
      if ((instance.index(a) != instance.classIndex()) && (!instance.isMissingSparse(a)))
      {


        double numOccurences = instance.valueSparse(a) * instance.weight();
        


        m_wordsPerClass[classIndex] += numOccurences;
        if (m_wordsPerClass[classIndex] < 0.0D) {
          throw new Exception("Can't have a negative number of words for class " + (classIndex + 1));
        }
        
        m_probOfWordGivenClass[classIndex][instance.index(a)] += numOccurences;
        if (m_probOfWordGivenClass[classIndex][instance.index(a)] < 0.0D) {
          throw new Exception("Can't have a negative conditional sum for attribute " + instance.index(a));
        }
      }
    }
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probOfClassGivenDoc = new double[m_numClasses];
    

    double[] logDocGivenClass = new double[m_numClasses];
    for (int c = 0; c < m_numClasses; c++) {
      logDocGivenClass[c] += Math.log(m_probOfClass[c]);
      int allWords = 0;
      for (int i = 0; i < instance.numValues(); i++) {
        if (instance.index(i) != instance.classIndex())
        {
          double frequencies = instance.valueSparse(i);
          allWords = (int)(allWords + frequencies);
          logDocGivenClass[c] += frequencies * Math.log(m_probOfWordGivenClass[c][instance.index(i)]);
        }
      }
      logDocGivenClass[c] -= allWords * Math.log(m_wordsPerClass[c]);
    }
    
    double max = logDocGivenClass[Utils.maxIndex(logDocGivenClass)];
    for (int i = 0; i < m_numClasses; i++) {
      probOfClassGivenDoc[i] = Math.exp(logDocGivenClass[i] - max);
    }
    Utils.normalize(probOfClassGivenDoc);
    
    return probOfClassGivenDoc;
  }
  




  public String toString()
  {
    StringBuffer result = new StringBuffer();
    
    result.append("The independent probability of a class\n");
    result.append("--------------------------------------\n");
    
    for (int c = 0; c < m_numClasses; c++) {
      result.append(m_headerInfo.classAttribute().value(c)).append("\t").append(Double.toString(m_probOfClass[c])).append("\n");
    }
    
    result.append("\nThe probability of a word given the class\n");
    result.append("-----------------------------------------\n\t");
    
    for (int c = 0; c < m_numClasses; c++) {
      result.append(m_headerInfo.classAttribute().value(c)).append("\t");
    }
    result.append("\n");
    
    for (int w = 0; w < m_numAttributes; w++) {
      result.append(m_headerInfo.attribute(w).name()).append("\t");
      for (int c = 0; c < m_numClasses; c++) {
        result.append(Double.toString(Math.exp(m_probOfWordGivenClass[c][w]))).append("\t");
      }
      result.append("\n");
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new NaiveBayesMultinomialUpdateable(), args);
  }
}

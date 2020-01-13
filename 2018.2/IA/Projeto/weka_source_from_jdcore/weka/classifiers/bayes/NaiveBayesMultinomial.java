package weka.classifiers.bayes;

import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SpecialFunctions;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;










































































public class NaiveBayesMultinomial
  extends Classifier
  implements WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 5932177440181257085L;
  protected double[][] m_probOfWordGivenClass;
  protected double[] m_probOfClass;
  protected int m_numAttributes;
  protected int m_numClasses;
  protected double[] m_lnFactorialCache = { 0.0D, 0.0D };
  

  protected Instances m_headerInfo;
  

  public NaiveBayesMultinomial() {}
  

  public String globalInfo()
  {
    return "Class for building and using a multinomial Naive Bayes classifier. For more information see,\n\n" + getTechnicalInformation().toString() + "\n\n" + "The core equation for this classifier:\n\n" + "P[Ci|D] = (P[D|Ci] x P[Ci]) / P[D] (Bayes rule)\n\n" + "where Ci is class i and D is a document.";
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Andrew Mccallum and Kamal Nigam");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "A Comparison of Event Models for Naive Bayes Text Classification");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "AAAI-98 Workshop on 'Learning for Text Categorization'");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
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
    





    for (int c = 0; c < m_numClasses; c++)
    {
      m_probOfWordGivenClass[c] = new double[m_numAttributes];
      for (int att = 0; att < m_numAttributes; att++)
      {
        m_probOfWordGivenClass[c][att] = 1.0D;
      }
    }
    




    double[] docsPerClass = new double[m_numClasses];
    double[] wordsPerClass = new double[m_numClasses];
    
    Enumeration enumInsts = instances.enumerateInstances();
    while (enumInsts.hasMoreElements())
    {
      Instance instance = (Instance)enumInsts.nextElement();
      int classIndex = (int)instance.value(instance.classIndex());
      docsPerClass[classIndex] += instance.weight();
      
      for (int a = 0; a < instance.numValues(); a++) {
        if (instance.index(a) != instance.classIndex())
        {
          if (!instance.isMissingSparse(a))
          {
            double numOccurences = instance.valueSparse(a) * instance.weight();
            if (numOccurences < 0.0D)
              throw new Exception("Numeric attribute values must all be greater or equal to zero.");
            wordsPerClass[classIndex] += numOccurences;
            m_probOfWordGivenClass[classIndex][instance.index(a)] += numOccurences;
          }
        }
      }
    }
    



    for (int c = 0; c < m_numClasses; c++) {
      for (int v = 0; v < m_numAttributes; v++) {
        m_probOfWordGivenClass[c][v] = Math.log(m_probOfWordGivenClass[c][v] / (wordsPerClass[c] + m_numAttributes - 1.0D));
      }
    }
    



    double numDocs = instances.sumOfWeights() + m_numClasses;
    m_probOfClass = new double[m_numClasses];
    for (int h = 0; h < m_numClasses; h++) {
      m_probOfClass[h] = ((docsPerClass[h] + 1.0D) / numDocs);
    }
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probOfClassGivenDoc = new double[m_numClasses];
    

    double[] logDocGivenClass = new double[m_numClasses];
    for (int h = 0; h < m_numClasses; h++) {
      logDocGivenClass[h] = probOfDocGivenClass(instance, h);
    }
    double max = logDocGivenClass[Utils.maxIndex(logDocGivenClass)];
    double probOfDoc = 0.0D;
    
    for (int i = 0; i < m_numClasses; i++)
    {
      probOfClassGivenDoc[i] = (Math.exp(logDocGivenClass[i] - max) * m_probOfClass[i]);
      probOfDoc += probOfClassGivenDoc[i];
    }
    
    Utils.normalize(probOfClassGivenDoc, probOfDoc);
    
    return probOfClassGivenDoc;
  }
  














  private double probOfDocGivenClass(Instance inst, int classIndex)
  {
    double answer = 0.0D;
    


    for (int i = 0; i < inst.numValues(); i++) {
      if (inst.index(i) != inst.classIndex())
      {
        double freqOfWordInDoc = inst.valueSparse(i);
        
        answer += freqOfWordInDoc * m_probOfWordGivenClass[classIndex][inst.index(i)];
      }
    }
    




    return answer;
  }
  















  public double lnFactorial(int n)
  {
    if (n < 0) { return SpecialFunctions.lnFactorial(n);
    }
    if (m_lnFactorialCache.length <= n) {
      double[] tmp = new double[n + 1];
      System.arraycopy(m_lnFactorialCache, 0, tmp, 0, m_lnFactorialCache.length);
      for (int i = m_lnFactorialCache.length; i < tmp.length; i++)
        tmp[i] = (tmp[(i - 1)] + Math.log(i));
      m_lnFactorialCache = tmp;
    }
    
    return m_lnFactorialCache[n];
  }
  





  public String toString()
  {
    StringBuffer result = new StringBuffer("The independent probability of a class\n--------------------------------------\n");
    
    for (int c = 0; c < m_numClasses; c++) {
      result.append(m_headerInfo.classAttribute().value(c)).append("\t").append(Double.toString(m_probOfClass[c])).append("\n");
    }
    result.append("\nThe probability of a word given the class\n-----------------------------------------\n\t");
    
    for (int c = 0; c < m_numClasses; c++) {
      result.append(m_headerInfo.classAttribute().value(c)).append("\t");
    }
    result.append("\n");
    
    for (int w = 0; w < m_numAttributes; w++)
    {
      if (w != m_headerInfo.classIndex()) {
        result.append(m_headerInfo.attribute(w).name()).append("\t");
        for (int c = 0; c < m_numClasses; c++)
          result.append(Double.toString(Math.exp(m_probOfWordGivenClass[c][w]))).append("\t");
        result.append("\n");
      }
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11303 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new NaiveBayesMultinomial(), argv);
  }
}

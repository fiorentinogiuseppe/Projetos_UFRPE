package weka.classifiers.bayes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;




















































public class DMNBtext
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler, UpdateableClassifier
{
  static final long serialVersionUID = 5932177450183457085L;
  protected int m_NumIterations;
  protected boolean m_MultinomialWord;
  int m_numClasses;
  protected Instances m_headerInfo;
  DNBBinary[] m_binaryClassifiers;
  
  public DMNBtext()
  {
    m_NumIterations = 1;
    m_MultinomialWord = false;
    m_numClasses = -1;
    
    m_binaryClassifiers = null;
  }
  



  public String globalInfo()
  {
    return "Class for building and using a Discriminative Multinomial Naive Bayes classifier. For more information see,\n\n" + getTechnicalInformation().toString() + "\n\n" + "The core equation for this classifier:\n\n" + "P[Ci|D] = (P[D|Ci] x P[Ci]) / P[D] (Bayes rule)\n\n" + "where Ci is class i and D is a document.";
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Jiang Su,Harry Zhang,Charles X. Ling,Stan Matwin");
    result.setValue(TechnicalInformation.Field.YEAR, "2008");
    result.setValue(TechnicalInformation.Field.TITLE, "Discriminative Parameter Learning for Bayesian Networks");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "ICML 2008'");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    Instances instances = new Instances(data);
    instances.deleteWithMissingClass();
    
    m_binaryClassifiers = new DNBBinary[instances.numClasses()];
    m_numClasses = instances.numClasses();
    m_headerInfo = new Instances(instances, 0);
    for (int i = 0; i < instances.numClasses(); i++) {
      m_binaryClassifiers[i] = new DNBBinary();
      m_binaryClassifiers[i].setTargetClass(i);
      m_binaryClassifiers[i].initClassifier(instances);
    }
    
    if (instances.numInstances() == 0) {
      return;
    }
    Random random = new Random();
    for (int it = 0; it < m_NumIterations; it++) {
      for (int i = 0; i < instances.numInstances(); i++) {
        updateClassifier(instances.instance(i));
      }
    }
  }
  













  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (m_numClasses == 2) {
      m_binaryClassifiers[0].updateClassifier(instance);
    } else {
      for (int i = 0; i < instance.numClasses(); i++) {
        m_binaryClassifiers[i].updateClassifier(instance);
      }
    }
  }
  





  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_numClasses == 2)
    {
      return m_binaryClassifiers[0].distributionForInstance(instance);
    }
    double[] logDocGivenClass = new double[instance.numClasses()];
    for (int i = 0; i < m_numClasses; i++) {
      logDocGivenClass[i] = m_binaryClassifiers[i].getLogProbForTargetClass(instance);
    }
    
    double max = logDocGivenClass[Utils.maxIndex(logDocGivenClass)];
    for (int i = 0; i < m_numClasses; i++) {
      logDocGivenClass[i] = Math.exp(logDocGivenClass[i] - max);
    }
    try
    {
      Utils.normalize(logDocGivenClass);
    } catch (Exception e) {
      e.printStackTrace();
    }
    


    return logDocGivenClass;
  }
  



  public String toString()
  {
    StringBuffer result = new StringBuffer("");
    result.append("The log ratio of two conditional probabilities of a word w_i: log(p(w_i)|+)/p(w_i)|-)) in decent order based on their absolute values\n");
    result.append("Can be used to measure the discriminative power of each word.\n");
    if (m_numClasses == 2)
    {
      return m_binaryClassifiers[0].toString();
    }
    for (int i = 0; i < m_numClasses; i++) {
      result.append(i + " against the rest classes\n");
      result.append(m_binaryClassifiers[i].toString() + "\n");
    }
    return result.toString();
  }
  




  public Enumeration<Option> listOptions()
  {
    Vector<Option> newVector = new Vector();
    
    newVector.add(new Option("\tThe number of iterations that the classifier \n\twill scan the training data (default = 1)", "I", 1, "-I <iterations>"));
    


    newVector.add(new Option("\tUse the frequency information in data", "M", 0, "-M"));
    

    return newVector.elements();
  }
  





  public void setOptions(String[] options)
    throws Exception
  {
    String iterations = Utils.getOption('I', options);
    if (iterations.length() != 0) {
      setNumIterations(Integer.parseInt(iterations));
    }
    
    setMultinomialWord(Utils.getFlag('M', options));
  }
  





  public String[] getOptions()
  {
    ArrayList<String> options = new ArrayList();
    
    options.add("-I");
    options.add("" + getNumIterations());
    
    if (getMultinomialWord()) {
      options.add("-M");
    }
    
    return (String[])options.toArray(new String[1]);
  }
  




  public String numIterationsTipText()
  {
    return "The number of iterations that the classifier will scan the training data";
  }
  



  public void setNumIterations(int numIterations)
  {
    m_NumIterations = numIterations;
  }
  





  public int getNumIterations()
  {
    return m_NumIterations;
  }
  




  public String multinomialWordTipText()
  {
    return "Make use of frequency information in data";
  }
  



  public void setMultinomialWord(boolean val)
  {
    m_MultinomialWord = val;
  }
  





  public boolean getMultinomialWord()
  {
    return m_MultinomialWord;
  }
  




  public String getRevision()
  {
    return "$Revision: 1.0";
  }
  
  public class DNBBinary
    implements Serializable
  {
    private double[][] m_perWordPerClass;
    private double[] m_wordsPerClass;
    int m_classIndex = -1;
    
    private double[] m_classDistribution;
    
    private int m_numAttributes;
    private int m_targetClass = -1;
    
    private double m_WordLaplace = 1.0D;
    private double[] m_coefficient;
    private double m_classRatio;
    private double m_wordRatio;
    
    public DNBBinary() {}
    
    public void initClassifier(Instances instances) throws Exception { m_numAttributes = instances.numAttributes();
      m_perWordPerClass = new double[2][m_numAttributes];
      m_coefficient = new double[m_numAttributes];
      m_wordsPerClass = new double[2];
      m_classDistribution = new double[2];
      m_WordLaplace = Math.log(m_numAttributes);
      m_classIndex = instances.classIndex();
      

      for (int c = 0; c < 2; c++) {
        m_classDistribution[c] = 1.0D;
        m_wordsPerClass[c] = (m_WordLaplace * m_numAttributes);
        Arrays.fill(m_perWordPerClass[c], m_WordLaplace);
      }
    }
    

    public void updateClassifier(Instance ins)
      throws Exception
    {
      int classIndex = 0;
      if (ins.value(ins.classIndex()) != m_targetClass)
        classIndex = 1;
      double prob = 1.0D - distributionForInstance(ins)[classIndex];
      


      double weight = prob * ins.weight();
      
      for (int a = 0; a < ins.numValues(); a++) {
        if (ins.index(a) != m_classIndex)
        {

          if (!m_MultinomialWord) {
            if (ins.valueSparse(a) > 0.0D) {
              m_wordsPerClass[classIndex] += weight;
              
              m_perWordPerClass[classIndex][ins.index(a)] += weight;
            }
          }
          else
          {
            double t = ins.valueSparse(a) * weight;
            m_wordsPerClass[classIndex] += t;
            m_perWordPerClass[classIndex][ins.index(a)] += t;
          }
          
          m_coefficient[ins.index(a)] = Math.log(m_perWordPerClass[0][ins.index(a)] / m_perWordPerClass[1][ins.index(a)]);
        }
      }
      

      m_wordRatio = Math.log(m_wordsPerClass[0] / m_wordsPerClass[1]);
      m_classDistribution[classIndex] += weight;
      m_classRatio = Math.log(m_classDistribution[0] / m_classDistribution[1]);
    }
    









    public double getLogProbForTargetClass(Instance ins)
      throws Exception
    {
      double probLog = m_classRatio;
      for (int a = 0; a < ins.numValues(); a++) {
        if (ins.index(a) != m_classIndex)
        {

          if (!m_MultinomialWord) {
            if (ins.valueSparse(a) > 0.0D) {
              probLog += m_coefficient[ins.index(a)] - m_wordRatio;
            }
          }
          else {
            probLog += ins.valueSparse(a) * (m_coefficient[ins.index(a)] - m_wordRatio);
          }
        }
      }
      
      return probLog;
    }
    







    public double[] distributionForInstance(Instance instance)
      throws Exception
    {
      double[] probOfClassGivenDoc = new double[2];
      double ratio = getLogProbForTargetClass(instance);
      if (ratio > 709.0D) {
        probOfClassGivenDoc[0] = 1.0D;
      }
      else {
        ratio = Math.exp(ratio);
        probOfClassGivenDoc[0] = (ratio / (1.0D + ratio));
      }
      
      probOfClassGivenDoc[1] = (1.0D - probOfClassGivenDoc[0]);
      return probOfClassGivenDoc;
    }
    





    public String toString()
    {
      StringBuffer result = new StringBuffer();
      
      result.append("\n");
      TreeMap sort = new TreeMap();
      double[] absCoeff = new double[m_numAttributes];
      for (int w = 0; w < m_numAttributes; w++)
      {
        if (w != m_headerInfo.classIndex()) {
          String val = m_headerInfo.attribute(w).name() + ": " + m_coefficient[w];
          sort.put(Double.valueOf(-1.0D * Math.abs(m_coefficient[w])), val);
        } }
      Iterator it = sort.values().iterator();
      while (it.hasNext())
      {
        result.append((String)it.next());
        result.append("\n");
      }
      
      return result.toString();
    }
    



    public void setTargetClass(int targetClass)
    {
      m_targetClass = targetClass;
    }
    





    public int getTargetClass()
    {
      return m_targetClass;
    }
  }
  







  public static void main(String[] argv)
  {
    DMNBtext c = new DMNBtext();
    
    runClassifier(c, argv);
  }
}

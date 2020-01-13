package weka.attributeSelection;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;













































































































public class WrapperSubsetEval
  extends ASEvaluation
  implements SubsetEvaluator, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -4573057658746728675L;
  private Instances m_trainInstances;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_numInstances;
  private Evaluation m_Evaluation;
  private Classifier m_BaseClassifier;
  private int m_folds;
  private int m_seed;
  private double m_threshold;
  
  public String globalInfo()
  {
    return "WrapperSubsetEval:\n\nEvaluates attribute sets by using a learning scheme. Cross validation is used to estimate the accuracy of the learning scheme for a set of attributes.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ron Kohavi and George H. John");
    result.setValue(TechnicalInformation.Field.YEAR, "1997");
    result.setValue(TechnicalInformation.Field.TITLE, "Wrappers for feature subset selection");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.VOLUME, "97");
    result.setValue(TechnicalInformation.Field.NUMBER, "1-2");
    result.setValue(TechnicalInformation.Field.PAGES, "273-324");
    result.setValue(TechnicalInformation.Field.NOTE, "Special issue on relevance");
    result.setValue(TechnicalInformation.Field.ISSN, "0004-3702");
    
    return result;
  }
  


  public WrapperSubsetEval()
  {
    resetOptions();
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    newVector.addElement(new Option("\tclass name of base learner to use for \taccuracy estimation.\n\tPlace any classifier options LAST on the command line\n\tfollowing a \"--\". eg.:\n\t\t-B weka.classifiers.bayes.NaiveBayes ... -- -K\n\t(default: weka.classifiers.rules.ZeroR)", "B", 1, "-B <base learner>"));
    






    newVector.addElement(new Option("\tnumber of cross validation folds to use for estimating accuracy.\n\t(default=5)", "F", 1, "-F <num>"));
    



    newVector.addElement(new Option("\tSeed for cross validation accuracy testimation.\n\t(default = 1)", "R", 1, "-R <seed>"));
    



    newVector.addElement(new Option("\tthreshold by which to execute another cross validation\n\t(standard deviation---expressed as a percentage of the mean).\n\t(default: 0.01 (1%))", "T", 1, "-T <num>"));
    




    if ((m_BaseClassifier != null) && ((m_BaseClassifier instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to scheme " + m_BaseClassifier.getClass().getName() + ":"));
      

      Enumeration enu = m_BaseClassifier.listOptions();
      
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    
    return newVector.elements();
  }
  








































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    String optionString = Utils.getOption('B', options);
    
    if (optionString.length() == 0)
      optionString = ZeroR.class.getName();
    setClassifier(Classifier.forName(optionString, Utils.partitionOptions(options)));
    
    optionString = Utils.getOption('F', options);
    
    if (optionString.length() != 0) {
      setFolds(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('R', options);
    if (optionString.length() != 0) {
      setSeed(Integer.parseInt(optionString));
    }
    





    optionString = Utils.getOption('T', options);
    
    if (optionString.length() != 0)
    {
      Double temp = Double.valueOf(optionString);
      setThreshold(temp.doubleValue());
    }
  }
  




  public String thresholdTipText()
  {
    return "Repeat xval if stdev of mean exceeds this value.";
  }
  




  public void setThreshold(double t)
  {
    m_threshold = t;
  }
  





  public double getThreshold()
  {
    return m_threshold;
  }
  




  public String foldsTipText()
  {
    return "Number of xval folds to use when estimating subset accuracy.";
  }
  




  public void setFolds(int f)
  {
    m_folds = f;
  }
  





  public int getFolds()
  {
    return m_folds;
  }
  




  public String seedTipText()
  {
    return "Seed to use for randomly generating xval splits.";
  }
  




  public void setSeed(int s)
  {
    m_seed = s;
  }
  





  public int getSeed()
  {
    return m_seed;
  }
  




  public String classifierTipText()
  {
    return "Classifier to use for estimating the accuracy of subsets";
  }
  




  public void setClassifier(Classifier newClassifier)
  {
    m_BaseClassifier = newClassifier;
  }
  





  public Classifier getClassifier()
  {
    return m_BaseClassifier;
  }
  





  public String[] getOptions()
  {
    String[] classifierOptions = new String[0];
    
    if ((m_BaseClassifier != null) && ((m_BaseClassifier instanceof OptionHandler)))
    {
      classifierOptions = m_BaseClassifier.getOptions();
    }
    
    String[] options = new String[9 + classifierOptions.length];
    int current = 0;
    
    if (getClassifier() != null) {
      options[(current++)] = "-B";
      options[(current++)] = getClassifier().getClass().getName();
    }
    
    options[(current++)] = "-F";
    options[(current++)] = ("" + getFolds());
    options[(current++)] = "-T";
    options[(current++)] = ("" + getThreshold());
    options[(current++)] = "-R";
    options[(current++)] = ("" + getSeed());
    options[(current++)] = "--";
    System.arraycopy(classifierOptions, 0, options, current, classifierOptions.length);
    
    current += classifierOptions.length;
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  
  protected void resetOptions()
  {
    m_trainInstances = null;
    m_Evaluation = null;
    m_BaseClassifier = new ZeroR();
    m_folds = 5;
    m_seed = 1;
    m_threshold = 0.01D;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result;
    


    if (getClassifier() == null) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
    } else {
      result = getClassifier().getCapabilities();
    }
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    result.setMinimumNumberInstances(getFolds());
    
    return result;
  }
  









  public void buildEvaluator(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_trainInstances = data;
    m_classIndex = m_trainInstances.classIndex();
    m_numAttribs = m_trainInstances.numAttributes();
    m_numInstances = m_trainInstances.numInstances();
  }
  








  public double evaluateSubset(BitSet subset)
    throws Exception
  {
    double errorRate = 0.0D;
    double[] repError = new double[5];
    int numAttributes = 0;
    
    Random Rnd = new Random(m_seed);
    Remove delTransform = new Remove();
    delTransform.setInvertSelection(true);
    
    Instances trainCopy = new Instances(m_trainInstances);
    

    for (int i = 0; i < m_numAttribs; i++) {
      if (subset.get(i)) {
        numAttributes++;
      }
    }
    

    int[] featArray = new int[numAttributes + 1];
    
    i = 0; for (int j = 0; i < m_numAttribs; i++) {
      if (subset.get(i)) {
        featArray[(j++)] = i;
      }
    }
    
    featArray[j] = m_classIndex;
    delTransform.setAttributeIndicesArray(featArray);
    delTransform.setInputFormat(trainCopy);
    trainCopy = Filter.useFilter(trainCopy, delTransform);
    

    for (i = 0; i < 5; i++) {
      m_Evaluation = new Evaluation(trainCopy);
      m_Evaluation.crossValidateModel(m_BaseClassifier, trainCopy, m_folds, Rnd, new Object[0]);
      repError[i] = m_Evaluation.errorRate();
      

      if (!repeat(repError, i + 1)) {
        i++;
        break;
      }
    }
    
    for (j = 0; j < i; j++) {
      errorRate += repError[j];
    }
    
    errorRate /= i;
    m_Evaluation = null;
    return m_trainInstances.classAttribute().isNumeric() ? -errorRate : 1.0D - errorRate;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_trainInstances == null) {
      text.append("\tWrapper subset evaluator has not been built yet\n");
    }
    else {
      text.append("\tWrapper Subset Evaluator\n");
      text.append("\tLearning scheme: " + getClassifier().getClass().getName() + "\n");
      
      text.append("\tScheme options: ");
      String[] classifierOptions = new String[0];
      
      if ((m_BaseClassifier instanceof OptionHandler)) {
        classifierOptions = m_BaseClassifier.getOptions();
        
        for (int i = 0; i < classifierOptions.length; i++) {
          text.append(classifierOptions[i] + " ");
        }
      }
      
      text.append("\n");
      if (m_trainInstances.attribute(m_classIndex).isNumeric()) {
        text.append("\tSubset evaluation: RMSE\n");
      } else {
        text.append("\tSubset evaluation: classification accuracy\n");
      }
      
      text.append("\tNumber of folds for accuracy estimation: " + m_folds + "\n");
    }
    


    return text.toString();
  }
  











  private boolean repeat(double[] repError, int entries)
  {
    double mean = 0.0D;
    double variance = 0.0D;
    


    if (m_threshold < 0.0D) {
      return false;
    }
    
    if (entries == 1) {
      return true;
    }
    
    for (int i = 0; i < entries; i++) {
      mean += repError[i];
    }
    
    mean /= entries;
    
    for (i = 0; i < entries; i++) {
      variance += (repError[i] - mean) * (repError[i] - mean);
    }
    
    variance /= entries;
    
    if (variance > 0.0D) {
      variance = Math.sqrt(variance);
    }
    
    if (variance / mean > m_threshold) {
      return true;
    }
    
    return false;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11851 $");
  }
  


  public void clean()
  {
    m_trainInstances = new Instances(m_trainInstances, 0);
  }
  





  public static void main(String[] args)
  {
    runEvaluator(new WrapperSubsetEval(), args);
  }
}

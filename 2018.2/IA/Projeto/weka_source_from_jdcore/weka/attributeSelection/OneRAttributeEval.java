package weka.attributeSelection;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;













































































public class OneRAttributeEval
  extends ASEvaluation
  implements AttributeEvaluator, OptionHandler
{
  static final long serialVersionUID = 4386514823886856980L;
  private Instances m_trainInstances;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_numInstances;
  private int m_randomSeed;
  private int m_folds;
  private boolean m_evalUsingTrainingData;
  private int m_minBucketSize;
  
  public String globalInfo()
  {
    return "OneRAttributeEval :\n\nEvaluates the worth of an attribute by using the OneR classifier.\n";
  }
  






  public String seedTipText()
  {
    return "Set the seed for use in cross validation.";
  }
  




  public void setSeed(int seed)
  {
    m_randomSeed = seed;
  }
  




  public int getSeed()
  {
    return m_randomSeed;
  }
  





  public String foldsTipText()
  {
    return "Set the number of folds for cross validation.";
  }
  




  public void setFolds(int folds)
  {
    m_folds = folds;
    if (m_folds < 2) {
      m_folds = 2;
    }
  }
  




  public int getFolds()
  {
    return m_folds;
  }
  





  public String evalUsingTrainingDataTipText()
  {
    return "Use the training data to evaluate attributes rather than cross validation.";
  }
  





  public void setEvalUsingTrainingData(boolean e)
  {
    m_evalUsingTrainingData = e;
  }
  





  public String minimumBucketSizeTipText()
  {
    return "The minimum number of objects in a bucket (passed to OneR).";
  }
  





  public void setMinimumBucketSize(int minB)
  {
    m_minBucketSize = minB;
  }
  




  public int getMinimumBucketSize()
  {
    return m_minBucketSize;
  }
  




  public boolean getEvalUsingTrainingData()
  {
    return m_evalUsingTrainingData;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tRandom number seed for cross validation\n\t(default = 1)", "S", 1, "-S <seed>"));
    



    newVector.addElement(new Option("\tNumber of folds for cross validation\n\t(default = 10)", "F", 1, "-F <folds>"));
    



    newVector.addElement(new Option("\tUse training data for evaluation rather than cross validaton", "D", 0, "-D"));
    


    newVector.addElement(new Option("\tMinimum number of objects in a bucket\n\t(passed on to OneR, default = 6)", "B", 1, "-B <minimum bucket size>"));
    




    return newVector.elements();
  }
  
























  public void setOptions(String[] options)
    throws Exception
  {
    String temp = Utils.getOption('S', options);
    
    if (temp.length() != 0) {
      setSeed(Integer.parseInt(temp));
    }
    
    temp = Utils.getOption('F', options);
    if (temp.length() != 0) {
      setFolds(Integer.parseInt(temp));
    }
    
    temp = Utils.getOption('B', options);
    if (temp.length() != 0) {
      setMinimumBucketSize(Integer.parseInt(temp));
    }
    
    setEvalUsingTrainingData(Utils.getFlag('D', options));
    Utils.checkForRemainingOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] options = new String[7];
    int current = 0;
    
    if (getEvalUsingTrainingData()) {
      options[(current++)] = "-D";
    }
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSeed());
    options[(current++)] = "-F";
    options[(current++)] = ("" + getFolds());
    options[(current++)] = "-B";
    options[(current++)] = ("" + getMinimumBucketSize());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  


  public OneRAttributeEval()
  {
    resetOptions();
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
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
  



  protected void resetOptions()
  {
    m_trainInstances = null;
    m_randomSeed = 1;
    m_folds = 10;
    m_evalUsingTrainingData = false;
    m_minBucketSize = 6;
  }
  







  public double evaluateAttribute(int attribute)
    throws Exception
  {
    int[] featArray = new int[2];
    

    Remove delTransform = new Remove();
    delTransform.setInvertSelection(true);
    
    Instances trainCopy = new Instances(m_trainInstances);
    featArray[0] = attribute;
    featArray[1] = trainCopy.classIndex();
    delTransform.setAttributeIndicesArray(featArray);
    delTransform.setInputFormat(trainCopy);
    trainCopy = Filter.useFilter(trainCopy, delTransform);
    Evaluation o_Evaluation = new Evaluation(trainCopy);
    String[] oneROpts = { "-B", "" + getMinimumBucketSize() };
    Classifier oneR = Classifier.forName("weka.classifiers.rules.OneR", oneROpts);
    if (m_evalUsingTrainingData) {
      oneR.buildClassifier(trainCopy);
      o_Evaluation.evaluateModel(oneR, trainCopy, new Object[0]);

    }
    else
    {
      o_Evaluation.crossValidateModel(oneR, trainCopy, m_folds, new Random(m_randomSeed), new Object[0]);
    }
    double errorRate = o_Evaluation.errorRate();
    return (1.0D - errorRate) * 100.0D;
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_trainInstances == null) {
      text.append("\tOneR feature evaluator has not been built yet");
    }
    else {
      text.append("\tOneR feature evaluator.\n\n");
      text.append("\tUsing ");
      if (m_evalUsingTrainingData) {
        text.append("training data for evaluation of attributes.");
      } else {
        text.append("" + getFolds() + " fold cross validation for evaluating " + "attributes.");
      }
      
      text.append("\n\tMinimum bucket size for OneR: " + getMinimumBucketSize());
    }
    

    text.append("\n");
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11219 $");
  }
  


  public int[] postProcess(int[] attributeSet)
  {
    m_trainInstances = new Instances(m_trainInstances, 0);
    
    return attributeSet;
  }
  








  public static void main(String[] args)
  {
    runEvaluator(new OneRAttributeEval(), args);
  }
}

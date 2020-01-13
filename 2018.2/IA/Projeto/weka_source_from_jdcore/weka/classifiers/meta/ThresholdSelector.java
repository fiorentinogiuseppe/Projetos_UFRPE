package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;







































































































public class ThresholdSelector
  extends RandomizableSingleClassifierEnhancer
  implements OptionHandler, Drawable
{
  static final long serialVersionUID = -1795038053239867444L;
  public static final int RANGE_NONE = 0;
  public static final int RANGE_BOUNDS = 1;
  public static final Tag[] TAGS_RANGE = { new Tag(0, "No range correction"), new Tag(1, "Correct based on min/max observed") };
  

  public static final int EVAL_TRAINING_SET = 2;
  

  public static final int EVAL_TUNED_SPLIT = 1;
  

  public static final int EVAL_CROSS_VALIDATION = 0;
  

  public static final Tag[] TAGS_EVAL = { new Tag(2, "Entire training set"), new Tag(1, "Single tuned fold"), new Tag(0, "N-Fold cross validation") };
  

  public static final int OPTIMIZE_0 = 0;
  

  public static final int OPTIMIZE_1 = 1;
  

  public static final int OPTIMIZE_LFREQ = 2;
  

  public static final int OPTIMIZE_MFREQ = 3;
  

  public static final int OPTIMIZE_POS_NAME = 4;
  
  public static final Tag[] TAGS_OPTIMIZE = { new Tag(0, "First class value"), new Tag(1, "Second class value"), new Tag(2, "Least frequent class value"), new Tag(3, "Most frequent class value"), new Tag(4, "Class value named: \"yes\", \"pos(itive)\",\"1\"") };
  

  public static final int FMEASURE = 1;
  

  public static final int ACCURACY = 2;
  

  public static final int TRUE_POS = 3;
  

  public static final int TRUE_NEG = 4;
  

  public static final int TP_RATE = 5;
  

  public static final int PRECISION = 6;
  

  public static final int RECALL = 7;
  
  public static final Tag[] TAGS_MEASURE = { new Tag(1, "FMEASURE"), new Tag(2, "ACCURACY"), new Tag(3, "TRUE_POS"), new Tag(4, "TRUE_NEG"), new Tag(5, "TP_RATE"), new Tag(6, "PRECISION"), new Tag(7, "RECALL") };
  









  protected double m_HighThreshold = 1.0D;
  

  protected double m_LowThreshold = 0.0D;
  

  protected double m_BestThreshold = -1.7976931348623157E308D;
  

  protected double m_BestValue = -1.7976931348623157E308D;
  

  protected int m_NumXValFolds = 3;
  

  protected int m_DesignatedClass = 0;
  

  protected int m_ClassMode = 4;
  

  protected int m_EvalMode = 1;
  

  protected int m_RangeMode = 0;
  

  int m_nMeasure = 1;
  

  protected boolean m_manualThreshold = false;
  
  protected double m_manualThresholdValue = -1.0D;
  


  protected static final double MIN_VALUE = 0.05D;
  



  public ThresholdSelector()
  {
    m_Classifier = new Logistic();
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.functions.Logistic";
  }
  











  protected FastVector getPredictions(Instances instances, int mode, int numFolds)
    throws Exception
  {
    EvaluationUtils eu = new EvaluationUtils();
    eu.setSeed(m_Seed);
    
    switch (mode) {
    case 1: 
      Instances trainData = null;Instances evalData = null;
      Instances data = new Instances(instances);
      Random random = new Random(m_Seed);
      data.randomize(random);
      data.stratify(numFolds);
      

      for (int subsetIndex = 0; subsetIndex < numFolds; subsetIndex++) {
        trainData = data.trainCV(numFolds, subsetIndex, random);
        evalData = data.testCV(numFolds, subsetIndex);
        if ((checkForInstance(trainData)) && (checkForInstance(evalData))) {
          break;
        }
      }
      return eu.getTrainTestPredictions(m_Classifier, trainData, evalData);
    case 2: 
      return eu.getTrainTestPredictions(m_Classifier, instances, instances);
    case 0: 
      return eu.getCVPredictions(m_Classifier, instances, numFolds);
    }
    throw new RuntimeException("Unrecognized evaluation mode");
  }
  






  public String measureTipText()
  {
    return "Sets the measure for determining the threshold.";
  }
  




  public void setMeasure(SelectedTag newMeasure)
  {
    if (newMeasure.getTags() == TAGS_MEASURE) {
      m_nMeasure = newMeasure.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getMeasure()
  {
    return new SelectedTag(m_nMeasure, TAGS_MEASURE);
  }
  








  protected void findThreshold(FastVector predictions)
  {
    Instances curve = new ThresholdCurve().getCurve(predictions, m_DesignatedClass);
    
    double low = 1.0D;
    double high = 0.0D;
    

    if (curve.numInstances() > 0) {
      Instance maxInst = curve.instance(0);
      double maxValue = 0.0D;
      int index1 = 0;
      int index2 = 0;
      switch (m_nMeasure) {
      case 1: 
        index1 = curve.attribute("FMeasure").index();
        maxValue = maxInst.value(index1);
        break;
      case 3: 
        index1 = curve.attribute("True Positives").index();
        maxValue = maxInst.value(index1);
        break;
      case 4: 
        index1 = curve.attribute("True Negatives").index();
        maxValue = maxInst.value(index1);
        break;
      case 5: 
        index1 = curve.attribute("True Positive Rate").index();
        maxValue = maxInst.value(index1);
        break;
      case 6: 
        index1 = curve.attribute("Precision").index();
        maxValue = maxInst.value(index1);
        break;
      case 7: 
        index1 = curve.attribute("Recall").index();
        maxValue = maxInst.value(index1);
        break;
      case 2: 
        index1 = curve.attribute("True Positives").index();
        index2 = curve.attribute("True Negatives").index();
        maxValue = maxInst.value(index1) + maxInst.value(index2);
      }
      
      int indexThreshold = curve.attribute("Threshold").index();
      for (int i = 1; i < curve.numInstances(); i++) {
        Instance current = curve.instance(i);
        double currentValue = 0.0D;
        if (m_nMeasure == 2) {
          currentValue = current.value(index1) + current.value(index2);
        } else {
          currentValue = current.value(index1);
        }
        
        if (currentValue > maxValue) {
          maxInst = current;
          maxValue = currentValue;
        }
        if (m_RangeMode == 1) {
          double thresh = current.value(indexThreshold);
          if (thresh < low) {
            low = thresh;
          }
          if (thresh > high) {
            high = thresh;
          }
        }
      }
      if (maxValue > 0.05D) {
        m_BestThreshold = maxInst.value(indexThreshold);
        m_BestValue = maxValue;
      }
      
      if (m_RangeMode == 1) {
        m_LowThreshold = low;
        m_HighThreshold = high;
      }
    }
  }
  







  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tThe class for which threshold is determined. Valid values are:\n\t1, 2 (for first and second classes, respectively), 3 (for whichever\n\tclass is least frequent), and 4 (for whichever class value is most\n\tfrequent), and 5 (for the first class named any of \"yes\",\"pos(itive)\"\n\t\"1\", or method 3 if no matches). (default 5).", "C", 1, "-C <integer>"));
    






    newVector.addElement(new Option("\tNumber of folds used for cross validation. If just a\n\thold-out set is used, this determines the size of the hold-out set\n\t(default 3).", "X", 1, "-X <number of folds>"));
    




    newVector.addElement(new Option("\tSets whether confidence range correction is applied. This\n\tcan be used to ensure the confidences range from 0 to 1.\n\tUse 0 for no range correction, 1 for correction based on\n\tthe min/max values seen during threshold selection\n\t(default 0).", "R", 1, "-R <integer>"));
    






    newVector.addElement(new Option("\tSets the evaluation mode. Use 0 for\n\tevaluation using cross-validation,\n\t1 for evaluation using hold-out set,\n\tand 2 for evaluation on the\n\ttraining data (default 1).", "E", 1, "-E <integer>"));
    






    newVector.addElement(new Option("\tMeasure used for evaluation (default is FMEASURE).\n", "M", 1, "-M [FMEASURE|ACCURACY|TRUE_POS|TRUE_NEG|TP_RATE|PRECISION|RECALL]"));
    


    newVector.addElement(new Option("\tSet a manual threshold to use. This option overrides\n\tautomatic selection and options pertaining to\n\tautomatic selection will be ignored.\n\t(default -1, i.e. do not use a manual threshold).", "manual", 1, "-manual <real>"));
    





    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  









































































  public void setOptions(String[] options)
    throws Exception
  {
    String manualS = Utils.getOption("manual", options);
    if (manualS.length() > 0) {
      double val = Double.parseDouble(manualS);
      if (val >= 0.0D) {
        setManualThresholdValue(val);
      }
    }
    
    String classString = Utils.getOption('C', options);
    if (classString.length() != 0) {
      setDesignatedClass(new SelectedTag(Integer.parseInt(classString) - 1, TAGS_OPTIMIZE));
    }
    else {
      setDesignatedClass(new SelectedTag(4, TAGS_OPTIMIZE));
    }
    
    String modeString = Utils.getOption('E', options);
    if (modeString.length() != 0) {
      setEvaluationMode(new SelectedTag(Integer.parseInt(modeString), TAGS_EVAL));
    }
    else {
      setEvaluationMode(new SelectedTag(1, TAGS_EVAL));
    }
    
    String rangeString = Utils.getOption('R', options);
    if (rangeString.length() != 0) {
      setRangeCorrection(new SelectedTag(Integer.parseInt(rangeString), TAGS_RANGE));
    }
    else {
      setRangeCorrection(new SelectedTag(0, TAGS_RANGE));
    }
    
    String measureString = Utils.getOption('M', options);
    if (measureString.length() != 0) {
      setMeasure(new SelectedTag(measureString, TAGS_MEASURE));
    } else {
      setMeasure(new SelectedTag(1, TAGS_MEASURE));
    }
    
    String foldsString = Utils.getOption('X', options);
    if (foldsString.length() != 0) {
      setNumXValFolds(Integer.parseInt(foldsString));
    } else {
      setNumXValFolds(3);
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 12];
    
    int current = 0;
    
    if (m_manualThreshold) {
      options[(current++)] = "-manual";options[(current++)] = ("" + getManualThresholdValue());
    }
    options[(current++)] = "-C";options[(current++)] = ("" + (m_ClassMode + 1));
    options[(current++)] = "-X";options[(current++)] = ("" + getNumXValFolds());
    options[(current++)] = "-E";options[(current++)] = ("" + m_EvalMode);
    options[(current++)] = "-R";options[(current++)] = ("" + m_RangeMode);
    options[(current++)] = "-M";options[(current++)] = ("" + getMeasure().getSelectedTag().getReadable());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.BINARY_CLASS);
    
    return result;
  }
  







  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    AttributeStats stats = instances.attributeStats(instances.classIndex());
    if (m_manualThreshold) {
      m_BestThreshold = m_manualThresholdValue;
    } else {
      m_BestThreshold = 0.5D;
    }
    m_BestValue = 0.05D;
    m_HighThreshold = 1.0D;
    m_LowThreshold = 0.0D;
    


    if (distinctCount != 2) {
      System.err.println("Couldn't find examples of both classes. No adjustment.");
      m_Classifier.buildClassifier(instances);
    }
    else
    {
      switch (m_ClassMode) {
      case 0: 
        m_DesignatedClass = 0;
        break;
      case 1: 
        m_DesignatedClass = 1;
        break;
      case 4: 
        Attribute cAtt = instances.classAttribute();
        boolean found = false;
        for (int i = 0; (i < cAtt.numValues()) && (!found); i++) {
          String name = cAtt.value(i).toLowerCase();
          if ((name.startsWith("yes")) || (name.equals("1")) || (name.startsWith("pos")))
          {
            found = true;
            m_DesignatedClass = i;
          }
        }
        if (found) {
          break;
        }
      
      case 2: 
        m_DesignatedClass = (nominalCounts[0] > nominalCounts[1] ? 1 : 0);
        break;
      case 3: 
        m_DesignatedClass = (nominalCounts[0] > nominalCounts[1] ? 0 : 1);
        break;
      default: 
        throw new Exception("Unrecognized class value selection mode");
      }
      
      







      if (m_manualThreshold) {
        m_Classifier.buildClassifier(instances);
        return;
      }
      
      if (nominalCounts[m_DesignatedClass] == 1) {
        System.err.println("Only 1 positive found: optimizing on training data");
        findThreshold(getPredictions(instances, 2, 0));
      } else {
        int numFolds = Math.min(m_NumXValFolds, nominalCounts[m_DesignatedClass]);
        
        findThreshold(getPredictions(instances, m_EvalMode, numFolds));
        if (m_EvalMode != 2) {
          m_Classifier.buildClassifier(instances);
        }
      }
    }
  }
  






  private boolean checkForInstance(Instances data)
    throws Exception
  {
    for (int i = 0; i < data.numInstances(); i++) {
      if ((int)data.instance(i).classValue() == m_DesignatedClass) {
        return true;
      }
    }
    return false;
  }
  









  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] pred = m_Classifier.distributionForInstance(instance);
    double prob = pred[m_DesignatedClass];
    

    if (prob > m_BestThreshold) {
      prob = 0.5D + (prob - m_BestThreshold) / ((m_HighThreshold - m_BestThreshold) * 2.0D);
    }
    else {
      prob = (prob - m_LowThreshold) / ((m_BestThreshold - m_LowThreshold) * 2.0D);
    }
    
    if (prob < 0.0D) {
      prob = 0.0D;
    } else if (prob > 1.0D) {
      prob = 1.0D;
    }
    

    pred[m_DesignatedClass] = prob;
    if (pred.length == 2) {
      pred[((m_DesignatedClass + 1) % 2)] = (1.0D - prob);
    }
    return pred;
  }
  




  public String globalInfo()
  {
    return "A metaclassifier that selecting a mid-point threshold on the probability output by a Classifier. The midpoint threshold is set so that a given performance measure is optimized. Currently this is the F-measure. Performance is measured either on the training data, a hold-out set or using cross-validation. In addition, the probabilities returned by the base learner can have their range expanded so that the output probabilities will reside between 0 and 1 (this is useful if the scheme normally produces probabilities in a very narrow range).";
  }
  












  public String designatedClassTipText()
  {
    return "Sets the class value for which the optimization is performed. The options are: pick the first class value; pick the second class value; pick whichever class is least frequent; pick whichever class value is most frequent; pick the first class named any of \"yes\",\"pos(itive)\", \"1\", or the least frequent if no matches).";
  }
  











  public SelectedTag getDesignatedClass()
  {
    return new SelectedTag(m_ClassMode, TAGS_OPTIMIZE);
  }
  







  public void setDesignatedClass(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_OPTIMIZE) {
      m_ClassMode = newMethod.getSelectedTag().getID();
    }
  }
  




  public String evaluationModeTipText()
  {
    return "Sets the method used to determine the threshold/performance curve. The options are: perform optimization based on the entire training set (may result in overfitting); perform an n-fold cross-validation (may be time consuming); perform one fold of an n-fold cross-validation (faster but likely less accurate).";
  }
  










  public void setEvaluationMode(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_EVAL) {
      m_EvalMode = newMethod.getSelectedTag().getID();
    }
  }
  






  public SelectedTag getEvaluationMode()
  {
    return new SelectedTag(m_EvalMode, TAGS_EVAL);
  }
  




  public String rangeCorrectionTipText()
  {
    return "Sets the type of prediction range correction performed. The options are: do not do any range correction; expand predicted probabilities so that the minimum probability observed during the optimization maps to 0, and the maximum maps to 1 (values outside this range are clipped to 0 and 1).";
  }
  










  public void setRangeCorrection(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_RANGE) {
      m_RangeMode = newMethod.getSelectedTag().getID();
    }
  }
  






  public SelectedTag getRangeCorrection()
  {
    return new SelectedTag(m_RangeMode, TAGS_RANGE);
  }
  




  public String numXValFoldsTipText()
  {
    return "Sets the number of folds used during full cross-validation and tuned fold evaluation. This number will be automatically reduced if there are insufficient positive examples.";
  }
  







  public int getNumXValFolds()
  {
    return m_NumXValFolds;
  }
  





  public void setNumXValFolds(int newNumFolds)
  {
    if (newNumFolds < 2) {
      throw new IllegalArgumentException("Number of folds must be greater than 1");
    }
    m_NumXValFolds = newNumFolds;
  }
  






  public int graphType()
  {
    if ((m_Classifier instanceof Drawable)) {
      return ((Drawable)m_Classifier).graphType();
    }
    return 0;
  }
  





  public String graph()
    throws Exception
  {
    if ((m_Classifier instanceof Drawable))
      return ((Drawable)m_Classifier).graph();
    throw new Exception("Classifier: " + getClassifierSpec() + " cannot be graphed");
  }
  





  public String manualThresholdValueTipText()
  {
    return "Sets a manual threshold value to use. If this is set (non-negative value between 0 and 1), then all options pertaining to automatic threshold selection are ignored. ";
  }
  








  public void setManualThresholdValue(double threshold)
    throws Exception
  {
    m_manualThresholdValue = threshold;
    if ((threshold >= 0.0D) && (threshold <= 1.0D)) {
      m_manualThreshold = true;
    } else {
      m_manualThreshold = false;
      if (threshold >= 0.0D) {
        throw new IllegalArgumentException("Threshold must be in the range 0..1.");
      }
    }
  }
  






  public double getManualThresholdValue()
  {
    return m_manualThresholdValue;
  }
  





  public String toString()
  {
    if (m_BestValue == -1.7976931348623157E308D) {
      return "ThresholdSelector: No model built yet.";
    }
    String result = "Threshold Selector.\nClassifier: " + m_Classifier.getClass().getName() + "\n";
    

    result = result + "Index of designated class: " + m_DesignatedClass + "\n";
    
    if (m_manualThreshold) {
      result = result + "User supplied threshold: " + m_BestThreshold + "\n";
    } else {
      result = result + "Evaluation mode: ";
      switch (m_EvalMode) {
      case 0: 
        result = result + m_NumXValFolds + "-fold cross-validation";
        break;
      case 1: 
        result = result + "tuning on 1/" + m_NumXValFolds + " of the data";
        break;
      case 2: 
      default: 
        result = result + "tuning on the training data";
      }
      result = result + "\n";
      
      result = result + "Threshold: " + m_BestThreshold + "\n";
      result = result + "Best value: " + m_BestValue + "\n";
      if (m_RangeMode == 1) {
        result = result + "Expanding range [" + m_LowThreshold + "," + m_HighThreshold + "] to [0, 1]\n";
      }
      
      result = result + "Measure: " + getMeasure().getSelectedTag().getReadable() + "\n";
    }
    result = result + m_Classifier.toString();
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.43 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new ThresholdSelector(), argv);
  }
}

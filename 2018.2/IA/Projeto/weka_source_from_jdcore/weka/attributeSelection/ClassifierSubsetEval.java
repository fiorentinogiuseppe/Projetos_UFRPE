package weka.attributeSelection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;






































































public class ClassifierSubsetEval
  extends HoldOutSubsetEvaluator
  implements OptionHandler, ErrorBasedMeritEvaluator
{
  static final long serialVersionUID = 7532217899385278710L;
  private Instances m_trainingInstances;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_numInstances;
  private Classifier m_Classifier = new ZeroR();
  

  private Evaluation m_Evaluation;
  

  private File m_holdOutFile = new File("Click to set hold out or test instances");
  


  private Instances m_holdOutInstances = null;
  

  private boolean m_useTraining = true;
  

  public ClassifierSubsetEval() {}
  

  public String globalInfo()
  {
    return "Classifier subset evaluator:\n\nEvaluates attribute subsets on training data or a seperate hold out testing set. Uses a classifier to estimate the 'merit' of a set of attributes.";
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tclass name of the classifier to use for accuracy estimation.\n\tPlace any classifier options LAST on the command line\n\tfollowing a \"--\". eg.:\n\t\t-B weka.classifiers.bayes.NaiveBayes ... -- -K\n\t(default: weka.classifiers.rules.ZeroR)", "B", 1, "-B <classifier>"));
    






    newVector.addElement(new Option("\tUse the training data to estimate accuracy.", "T", 0, "-T"));
    



    newVector.addElement(new Option("\tName of the hold out/test set to \n\testimate accuracy on.", "H", 1, "-H <filename>"));
    



    if ((m_Classifier != null) && ((m_Classifier instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to scheme " + m_Classifier.getClass().getName() + ":"));
      


      Enumeration enu = m_Classifier.listOptions();
      
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
    

    optionString = Utils.getOption('H', options);
    if (optionString.length() != 0) {
      setHoldOutFile(new File(optionString));
    }
    
    setUseTraining(Utils.getFlag('T', options));
  }
  




  public String classifierTipText()
  {
    return "Classifier to use for estimating the accuracy of subsets";
  }
  




  public void setClassifier(Classifier newClassifier)
  {
    m_Classifier = newClassifier;
  }
  





  public Classifier getClassifier()
  {
    return m_Classifier;
  }
  




  public String holdOutFileTipText()
  {
    return "File containing hold out/test instances.";
  }
  



  public File getHoldOutFile()
  {
    return m_holdOutFile;
  }
  




  public void setHoldOutFile(File h)
  {
    m_holdOutFile = h;
  }
  




  public String useTrainingTipText()
  {
    return "Use training data instead of hold out/test instances.";
  }
  



  public boolean getUseTraining()
  {
    return m_useTraining;
  }
  



  public void setUseTraining(boolean t)
  {
    m_useTraining = t;
  }
  




  public String[] getOptions()
  {
    String[] classifierOptions = new String[0];
    
    if ((m_Classifier != null) && ((m_Classifier instanceof OptionHandler)))
    {
      classifierOptions = m_Classifier.getOptions();
    }
    
    String[] options = new String[6 + classifierOptions.length];
    int current = 0;
    
    if (getClassifier() != null) {
      options[(current++)] = "-B";
      options[(current++)] = getClassifier().getClass().getName();
    }
    
    if (getUseTraining()) {
      options[(current++)] = "-T";
    }
    options[(current++)] = "-H";options[(current++)] = getHoldOutFile().getPath();
    
    if (classifierOptions.length > 0) {
      options[(current++)] = "--";
      System.arraycopy(classifierOptions, 0, options, current, classifierOptions.length);
      
      current += classifierOptions.length;
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
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
    return result;
  }
  









  public void buildEvaluator(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_trainingInstances = data;
    m_classIndex = m_trainingInstances.classIndex();
    m_numAttribs = m_trainingInstances.numAttributes();
    m_numInstances = m_trainingInstances.numInstances();
    

    if ((!m_useTraining) && (!getHoldOutFile().getPath().startsWith("Click to set")))
    {
      Reader r = new BufferedReader(new FileReader(getHoldOutFile().getPath()));
      
      m_holdOutInstances = new Instances(r);
      m_holdOutInstances.setClassIndex(m_trainingInstances.classIndex());
      if (!m_trainingInstances.equalHeaders(m_holdOutInstances)) {
        throw new Exception("Hold out/test set is not compatable with training data.");
      }
    }
  }
  









  public double evaluateSubset(BitSet subset)
    throws Exception
  {
    double errorRate = 0.0D;
    int numAttributes = 0;
    Instances trainCopy = null;
    Instances testCopy = null;
    
    Remove delTransform = new Remove();
    delTransform.setInvertSelection(true);
    
    trainCopy = new Instances(m_trainingInstances);
    
    if (!m_useTraining) {
      if (m_holdOutInstances == null) {
        throw new Exception("Must specify a set of hold out/test instances with -H");
      }
      

      testCopy = new Instances(m_holdOutInstances);
    }
    

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
    if (!m_useTraining) {
      testCopy = Filter.useFilter(testCopy, delTransform);
    }
    

    m_Classifier.buildClassifier(trainCopy);
    
    m_Evaluation = new Evaluation(trainCopy);
    if (!m_useTraining) {
      m_Evaluation.evaluateModel(m_Classifier, testCopy, new Object[0]);
    } else {
      m_Evaluation.evaluateModel(m_Classifier, trainCopy, new Object[0]);
    }
    
    if (m_trainingInstances.classAttribute().isNominal()) {
      errorRate = m_Evaluation.errorRate();
    } else {
      errorRate = m_Evaluation.meanAbsoluteError();
    }
    
    m_Evaluation = null;
    

    return -errorRate;
  }
  













  public double evaluateSubset(BitSet subset, Instances holdOut)
    throws Exception
  {
    int numAttributes = 0;
    Instances trainCopy = null;
    Instances testCopy = null;
    
    if (!m_trainingInstances.equalHeaders(holdOut)) {
      throw new Exception("evaluateSubset : Incompatable instance types.");
    }
    
    Remove delTransform = new Remove();
    delTransform.setInvertSelection(true);
    
    trainCopy = new Instances(m_trainingInstances);
    
    testCopy = new Instances(holdOut);
    

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
    testCopy = Filter.useFilter(testCopy, delTransform);
    

    m_Classifier.buildClassifier(trainCopy);
    
    m_Evaluation = new Evaluation(trainCopy);
    m_Evaluation.evaluateModel(m_Classifier, testCopy, new Object[0]);
    double errorRate;
    double errorRate; if (m_trainingInstances.classAttribute().isNominal()) {
      errorRate = m_Evaluation.errorRate();
    } else {
      errorRate = m_Evaluation.meanAbsoluteError();
    }
    
    m_Evaluation = null;
    

    return -errorRate;
  }
  















  public double evaluateSubset(BitSet subset, Instance holdOut, boolean retrain)
    throws Exception
  {
    int numAttributes = 0;
    Instances trainCopy = null;
    Instance testCopy = null;
    
    if (!m_trainingInstances.equalHeaders(holdOut.dataset())) {
      throw new Exception("evaluateSubset : Incompatable instance types.");
    }
    
    Remove delTransform = new Remove();
    delTransform.setInvertSelection(true);
    
    trainCopy = new Instances(m_trainingInstances);
    
    testCopy = (Instance)holdOut.copy();
    

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
    
    if (retrain) {
      trainCopy = Filter.useFilter(trainCopy, delTransform);
      
      m_Classifier.buildClassifier(trainCopy);
    }
    
    delTransform.input(testCopy);
    testCopy = delTransform.output();
    


    double[] distrib = m_Classifier.distributionForInstance(testCopy);
    double pred; double pred; if (m_trainingInstances.classAttribute().isNominal()) {
      pred = distrib[((int)testCopy.classValue())];
    } else
      pred = distrib[0];
    double error;
    double error;
    if (m_trainingInstances.classAttribute().isNominal()) {
      error = 1.0D - pred;
    } else {
      error = testCopy.classValue() - pred;
    }
    


    return -error;
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_trainingInstances == null) {
      text.append("\tClassifier subset evaluator has not been built yet\n");
    }
    else {
      text.append("\tClassifier Subset Evaluator\n");
      text.append("\tLearning scheme: " + getClassifier().getClass().getName() + "\n");
      
      text.append("\tScheme options: ");
      String[] classifierOptions = new String[0];
      
      if ((m_Classifier instanceof OptionHandler)) {
        classifierOptions = m_Classifier.getOptions();
        
        for (int i = 0; i < classifierOptions.length; i++) {
          text.append(classifierOptions[i] + " ");
        }
      }
      
      text.append("\n");
      text.append("\tHold out/test set: ");
      if (!m_useTraining) {
        if (getHoldOutFile().getPath().startsWith("Click to set")) {
          text.append("none\n");
        } else {
          text.append(getHoldOutFile().getPath() + '\n');
        }
      } else {
        text.append("Training data\n");
      }
      if (m_trainingInstances.attribute(m_classIndex).isNumeric()) {
        text.append("\tAccuracy estimation: MAE\n");
      } else {
        text.append("\tAccuracy estimation: classification error\n");
      }
    }
    return text.toString();
  }
  


  protected void resetOptions()
  {
    m_trainingInstances = null;
    m_Evaluation = null;
    m_Classifier = new ZeroR();
    m_holdOutFile = new File("Click to set hold out or test instances");
    m_holdOutInstances = null;
    m_useTraining = false;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5511 $");
  }
  




  public static void main(String[] args)
  {
    runEvaluator(new ClassifierSubsetEval(), args);
  }
}

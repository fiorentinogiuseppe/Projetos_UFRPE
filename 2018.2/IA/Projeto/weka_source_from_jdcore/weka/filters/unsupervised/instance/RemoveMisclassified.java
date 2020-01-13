package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;


































































public class RemoveMisclassified
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 5469157004717663171L;
  protected Classifier m_cleansingClassifier = new ZeroR();
  

  protected int m_classIndex = -1;
  

  protected int m_numOfCrossValidationFolds = 0;
  

  protected int m_numOfCleansingIterations = 0;
  

  protected double m_numericClassifyThreshold = 0.1D;
  

  protected boolean m_invertMatching = false;
  

  protected boolean m_firstBatchFinished = false;
  


  public RemoveMisclassified() {}
  

  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    if (getClassifier() == null) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
    } else {
      result = getClassifier().getCapabilities();
    }
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    m_firstBatchFinished = false;
    return true;
  }
  







  private Instances cleanseTrain(Instances data)
    throws Exception
  {
    Instances buildSet = new Instances(data);
    Instances temp = new Instances(data, data.numInstances());
    Instances inverseSet = new Instances(data, data.numInstances());
    int count = 0;
    
    int iterations = 0;
    int classIndex = m_classIndex;
    if (classIndex < 0) classIndex = data.classIndex();
    if (classIndex < 0) { classIndex = data.numAttributes() - 1;
    }
    
    while (count != buildSet.numInstances())
    {

      iterations++;
      if ((m_numOfCleansingIterations > 0) && (iterations > m_numOfCleansingIterations)) {
        break;
      }
      count = buildSet.numInstances();
      buildSet.setClassIndex(classIndex);
      m_cleansingClassifier.buildClassifier(buildSet);
      
      temp = new Instances(buildSet, buildSet.numInstances());
      

      for (int i = 0; i < buildSet.numInstances(); i++) {
        Instance inst = buildSet.instance(i);
        double ans = m_cleansingClassifier.classifyInstance(inst);
        if (buildSet.classAttribute().isNumeric()) {
          if ((ans >= inst.classValue() - m_numericClassifyThreshold) && (ans <= inst.classValue() + m_numericClassifyThreshold))
          {
            temp.add(inst);
          } else if (m_invertMatching) {
            inverseSet.add(inst);
          }
          
        }
        else if (ans == inst.classValue()) {
          temp.add(inst);
        } else if (m_invertMatching) {
          inverseSet.add(inst);
        }
      }
      
      buildSet = temp;
    }
    
    if (m_invertMatching) {
      inverseSet.setClassIndex(data.classIndex());
      return inverseSet;
    }
    
    buildSet.setClassIndex(data.classIndex());
    return buildSet;
  }
  








  private Instances cleanseCross(Instances data)
    throws Exception
  {
    Instances crossSet = new Instances(data);
    Instances temp = new Instances(data, data.numInstances());
    Instances inverseSet = new Instances(data, data.numInstances());
    int count = 0;
    
    int iterations = 0;
    int classIndex = m_classIndex;
    if (classIndex < 0) classIndex = data.classIndex();
    if (classIndex < 0) { classIndex = data.numAttributes() - 1;
    }
    
    while ((count != crossSet.numInstances()) && (crossSet.numInstances() >= m_numOfCrossValidationFolds))
    {

      count = crossSet.numInstances();
      

      iterations++;
      if ((m_numOfCleansingIterations > 0) && (iterations > m_numOfCleansingIterations))
        break;
      crossSet.setClassIndex(classIndex);
      
      if (crossSet.classAttribute().isNominal()) {
        crossSet.stratify(m_numOfCrossValidationFolds);
      }
      
      temp = new Instances(crossSet, crossSet.numInstances());
      
      for (int fold = 0; fold < m_numOfCrossValidationFolds; fold++) {
        Instances train = crossSet.trainCV(m_numOfCrossValidationFolds, fold);
        m_cleansingClassifier.buildClassifier(train);
        Instances test = crossSet.testCV(m_numOfCrossValidationFolds, fold);
        
        for (int i = 0; i < test.numInstances(); i++) {
          Instance inst = test.instance(i);
          double ans = m_cleansingClassifier.classifyInstance(inst);
          if (crossSet.classAttribute().isNumeric()) {
            if ((ans >= inst.classValue() - m_numericClassifyThreshold) && (ans <= inst.classValue() + m_numericClassifyThreshold))
            {
              temp.add(inst);
            } else if (m_invertMatching) {
              inverseSet.add(inst);
            }
            
          }
          else if (ans == inst.classValue()) {
            temp.add(inst);
          } else if (m_invertMatching) {
            inverseSet.add(inst);
          }
        }
      }
      
      crossSet = temp;
    }
    
    if (m_invertMatching) {
      inverseSet.setClassIndex(data.classIndex());
      return inverseSet;
    }
    
    crossSet.setClassIndex(data.classIndex());
    return crossSet;
  }
  












  public boolean input(Instance instance)
    throws Exception
  {
    if (inputFormatPeek() == null) {
      throw new NullPointerException("No input instance format defined");
    }
    
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (m_firstBatchFinished) {
      push(instance);
      return true;
    }
    bufferInput(instance);
    return false;
  }
  






  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    if (!m_firstBatchFinished) {
      Instances filtered;
      Instances filtered;
      if (m_numOfCrossValidationFolds < 2) {
        filtered = cleanseTrain(getInputFormat());
      } else {
        filtered = cleanseCross(getInputFormat());
      }
      
      for (int i = 0; i < filtered.numInstances(); i++) {
        push(filtered.instance(i));
      }
      
      m_firstBatchFinished = true;
      flushInput();
    }
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tFull class name of classifier to use, followed\n\tby scheme options. eg:\n\t\t\"weka.classifiers.bayes.NaiveBayes -D\"\n\t(default: weka.classifiers.rules.ZeroR)", "W", 1, "-W <classifier specification>"));
    




    newVector.addElement(new Option("\tAttribute on which misclassifications are based.\n\tIf < 0 will use any current set class or default to the last attribute.", "C", 1, "-C <class index>"));
    


    newVector.addElement(new Option("\tThe number of folds to use for cross-validation cleansing.\n\t(<2 = no cross-validation - default).", "F", 1, "-F <number of folds>"));
    


    newVector.addElement(new Option("\tThreshold for the max error when predicting numeric class.\n\t(Value should be >= 0, default = 0.1).", "T", 1, "-T <threshold>"));
    


    newVector.addElement(new Option("\tThe maximum number of cleansing iterations to perform.\n\t(<1 = until fully cleansed - default)", "I", 1, "-I"));
    


    newVector.addElement(new Option("\tInvert the match so that correctly classified instances are discarded.\n", "V", 0, "-V"));
    


    return newVector.elements();
  }
  





































  public void setOptions(String[] options)
    throws Exception
  {
    String classifierString = Utils.getOption('W', options);
    if (classifierString.length() == 0)
      classifierString = ZeroR.class.getName();
    String[] classifierSpec = Utils.splitOptions(classifierString);
    if (classifierSpec.length == 0) {
      throw new Exception("Invalid classifier specification string");
    }
    String classifierName = classifierSpec[0];
    classifierSpec[0] = "";
    setClassifier(Classifier.forName(classifierName, classifierSpec));
    
    String cString = Utils.getOption('C', options);
    if (cString.length() != 0) {
      setClassIndex(new Double(cString).intValue());
    } else {
      setClassIndex(-1);
    }
    
    String fString = Utils.getOption('F', options);
    if (fString.length() != 0) {
      setNumFolds(new Double(fString).intValue());
    } else {
      setNumFolds(0);
    }
    
    String tString = Utils.getOption('T', options);
    if (tString.length() != 0) {
      setThreshold(new Double(tString).doubleValue());
    } else {
      setThreshold(0.1D);
    }
    
    String iString = Utils.getOption('I', options);
    if (iString.length() != 0) {
      setMaxIterations(new Double(iString).intValue());
    } else {
      setMaxIterations(0);
    }
    
    if (Utils.getFlag('V', options)) {
      setInvert(true);
    } else {
      setInvert(false);
    }
    
    Utils.checkForRemainingOptions(options);
  }
  






  public String[] getOptions()
  {
    String[] options = new String[15];
    int current = 0;
    
    options[(current++)] = "-W";options[(current++)] = ("" + getClassifierSpec());
    options[(current++)] = "-C";options[(current++)] = ("" + getClassIndex());
    options[(current++)] = "-F";options[(current++)] = ("" + getNumFolds());
    options[(current++)] = "-T";options[(current++)] = ("" + getThreshold());
    options[(current++)] = "-I";options[(current++)] = ("" + getMaxIterations());
    if (getInvert()) {
      options[(current++)] = "-V";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String globalInfo()
  {
    return "A filter that removes instances which are incorrectly classified. Useful for removing outliers.";
  }
  








  public String classifierTipText()
  {
    return "The classifier upon which to base the misclassifications.";
  }
  





  public void setClassifier(Classifier classifier)
  {
    m_cleansingClassifier = classifier;
  }
  





  public Classifier getClassifier()
  {
    return m_cleansingClassifier;
  }
  






  protected String getClassifierSpec()
  {
    Classifier c = getClassifier();
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(c.getOptions());
    }
    
    return c.getClass().getName();
  }
  






  public String classIndexTipText()
  {
    return "Index of the class upon which to base the misclassifications. If < 0 will use any current set class or default to the last attribute.";
  }
  







  public void setClassIndex(int classIndex)
  {
    m_classIndex = classIndex;
  }
  





  public int getClassIndex()
  {
    return m_classIndex;
  }
  






  public String numFoldsTipText()
  {
    return "The number of cross-validation folds to use. If < 2 then no cross-validation will be performed.";
  }
  






  public void setNumFolds(int numOfFolds)
  {
    m_numOfCrossValidationFolds = numOfFolds;
  }
  





  public int getNumFolds()
  {
    return m_numOfCrossValidationFolds;
  }
  






  public String thresholdTipText()
  {
    return "Threshold for the max allowable error when predicting a numeric class. Should be >= 0.";
  }
  






  public void setThreshold(double threshold)
  {
    m_numericClassifyThreshold = threshold;
  }
  





  public double getThreshold()
  {
    return m_numericClassifyThreshold;
  }
  






  public String maxIterationsTipText()
  {
    return "The maximum number of iterations to perform. < 1 means filter will go until fully cleansed.";
  }
  






  public void setMaxIterations(int iterations)
  {
    m_numOfCleansingIterations = iterations;
  }
  





  public int getMaxIterations()
  {
    return m_numOfCleansingIterations;
  }
  






  public String invertTipText()
  {
    return "Whether or not to invert the selection. If true, correctly classified instances will be discarded.";
  }
  





  public void setInvert(boolean invert)
  {
    m_invertMatching = invert;
  }
  





  public boolean getInvert()
  {
    return m_invertMatching;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5548 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new RemoveMisclassified(), argv);
  }
}

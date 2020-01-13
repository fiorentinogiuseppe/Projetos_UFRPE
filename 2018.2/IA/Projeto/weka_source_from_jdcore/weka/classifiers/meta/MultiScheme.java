package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;


































































public class MultiScheme
  extends RandomizableMultipleClassifiersCombiner
{
  static final long serialVersionUID = 5710744346128957520L;
  protected Classifier m_Classifier;
  protected int m_ClassifierIndex;
  protected int m_NumXValFolds;
  
  public MultiScheme() {}
  
  public String globalInfo()
  {
    return "Class for selecting a classifier from among several using cross validation on the training data or the performance on the training data. Performance is measured based on percent correct (classification) or mean-squared error (regression).";
  }
  








  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    newVector.addElement(new Option("\tUse cross validation for model selection using the\n\tgiven number of folds. (default 0, is to\n\tuse training error)", "X", 1, "-X <number of folds>"));
    




    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    String numFoldsString = Utils.getOption('X', options);
    if (numFoldsString.length() != 0) {
      setNumFolds(Integer.parseInt(numFoldsString));
    } else {
      setNumFolds(0);
    }
    super.setOptions(options);
  }
  






  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 2];
    
    int current = 0;
    options[(current++)] = "-X";options[(current++)] = ("" + getNumFolds());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    return options;
  }
  




  public String classifiersTipText()
  {
    return "The classifiers to be chosen from.";
  }
  





  public void setClassifiers(Classifier[] classifiers)
  {
    m_Classifiers = classifiers;
  }
  





  public Classifier[] getClassifiers()
  {
    return m_Classifiers;
  }
  






  public Classifier getClassifier(int index)
  {
    return m_Classifiers[index];
  }
  









  protected String getClassifierSpec(int index)
  {
    if (m_Classifiers.length < index) {
      return "";
    }
    Classifier c = getClassifier(index);
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(c.getOptions());
    }
    
    return c.getClass().getName();
  }
  




  public String seedTipText()
  {
    return "The seed used for randomizing the data for cross-validation.";
  }
  






  public void setSeed(int seed)
  {
    m_Seed = seed;
  }
  





  public int getSeed()
  {
    return m_Seed;
  }
  




  public String numFoldsTipText()
  {
    return "The number of folds used for cross-validation (if 0, performance on training data will be used).";
  }
  







  public int getNumFolds()
  {
    return m_NumXValFolds;
  }
  






  public void setNumFolds(int numFolds)
  {
    m_NumXValFolds = numFolds;
  }
  




  public String debugTipText()
  {
    return "Whether debug information is output to console.";
  }
  





  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  





  public boolean getDebug()
  {
    return m_Debug;
  }
  





  public int getBestClassifierIndex()
  {
    return m_ClassifierIndex;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    if (m_Classifiers.length == 0) {
      throw new Exception("No base classifiers have been set!");
    }
    

    getCapabilities().testWithFail(data);
    

    Instances newData = new Instances(data);
    newData.deleteWithMissingClass();
    
    Random random = new Random(m_Seed);
    newData.randomize(random);
    if ((newData.classAttribute().isNominal()) && (m_NumXValFolds > 1)) {
      newData.stratify(m_NumXValFolds);
    }
    Instances train = newData;
    Instances test = newData;
    Classifier bestClassifier = null;
    int bestIndex = -1;
    double bestPerformance = NaN.0D;
    int numClassifiers = m_Classifiers.length;
    for (int i = 0; i < numClassifiers; i++) {
      Classifier currentClassifier = getClassifier(i);
      Evaluation evaluation;
      if (m_NumXValFolds > 1) {
        Evaluation evaluation = new Evaluation(newData);
        for (int j = 0; j < m_NumXValFolds; j++)
        {


          train = newData.trainCV(m_NumXValFolds, j, new Random(1L));
          test = newData.testCV(m_NumXValFolds, j);
          currentClassifier.buildClassifier(train);
          evaluation.setPriors(train);
          evaluation.evaluateModel(currentClassifier, test, new Object[0]);
        }
      } else {
        currentClassifier.buildClassifier(train);
        evaluation = new Evaluation(train);
        evaluation.evaluateModel(currentClassifier, test, new Object[0]);
      }
      
      double error = evaluation.errorRate();
      if (m_Debug) {
        System.err.println("Error rate: " + Utils.doubleToString(error, 6, 4) + " for classifier " + currentClassifier.getClass().getName());
      }
      


      if ((i == 0) || (error < bestPerformance)) {
        bestClassifier = currentClassifier;
        bestPerformance = error;
        bestIndex = i;
      }
    }
    m_ClassifierIndex = bestIndex;
    if (m_NumXValFolds > 1) {
      bestClassifier.buildClassifier(newData);
    }
    m_Classifier = bestClassifier;
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_Classifier.distributionForInstance(instance);
  }
  




  public String toString()
  {
    if (m_Classifier == null) {
      return "MultiScheme: No model built yet.";
    }
    
    String result = "MultiScheme selection using";
    if (m_NumXValFolds > 1) {
      result = result + " cross validation error";
    } else {
      result = result + " error on training data";
    }
    result = result + " from the following:\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result = result + '\t' + getClassifierSpec(i) + '\n';
    }
    
    result = result + "Selected scheme: " + getClassifierSpec(m_ClassifierIndex) + "\n\n" + m_Classifier.toString();
    


    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.25 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MultiScheme(), argv);
  }
}

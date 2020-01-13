package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;


































































































public class RegressionByDiscretization
  extends SingleClassifierEnhancer
{
  static final long serialVersionUID = 5066426153134050378L;
  protected Discretize m_Discretizer = new Discretize();
  

  protected int m_NumBins = 10;
  

  protected double[] m_ClassMeans;
  

  protected boolean m_DeleteEmptyBins;
  

  protected Instances m_DiscretizedHeader = null;
  

  protected boolean m_UseEqualFrequency = false;
  





  public String globalInfo()
  {
    return "A regression scheme that employs any classifier on a copy of the data that has the class attribute (equal-width) discretized. The predicted value is the expected value of the mean class value for each discretized interval (based on the predicted probabilities for each interval).";
  }
  









  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.J48";
  }
  



  public RegressionByDiscretization()
  {
    m_Classifier = new J48();
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    
    result.setMinimumNumberInstances(2);
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    

    m_Discretizer.setIgnoreClass(true);
    m_Discretizer.setAttributeIndices("" + (instances.classIndex() + 1));
    m_Discretizer.setBins(getNumBins());
    m_Discretizer.setUseEqualFrequency(getUseEqualFrequency());
    m_Discretizer.setInputFormat(instances);
    Instances newTrain = Filter.useFilter(instances, m_Discretizer);
    

    if (m_DeleteEmptyBins)
    {

      int numNonEmptyClasses = 0;
      boolean[] notEmptyClass = new boolean[newTrain.numClasses()];
      for (int i = 0; i < newTrain.numInstances(); i++) {
        if (notEmptyClass[((int)newTrain.instance(i).classValue())] == 0) {
          numNonEmptyClasses++;
          notEmptyClass[((int)newTrain.instance(i).classValue())] = true;
        }
      }
      

      FastVector newClassVals = new FastVector(numNonEmptyClasses);
      int[] oldIndexToNewIndex = new int[newTrain.numClasses()];
      for (int i = 0; i < newTrain.numClasses(); i++) {
        if (notEmptyClass[i] != 0) {
          oldIndexToNewIndex[i] = newClassVals.size();
          newClassVals.addElement(newTrain.classAttribute().value(i));
        }
      }
      

      Attribute newClass = new Attribute(newTrain.classAttribute().name(), newClassVals);
      
      FastVector newAttributes = new FastVector(newTrain.numAttributes());
      for (int i = 0; i < newTrain.numAttributes(); i++) {
        if (i != newTrain.classIndex()) {
          newAttributes.addElement(newTrain.attribute(i).copy());
        } else {
          newAttributes.addElement(newClass);
        }
      }
      

      Instances newTrainTransformed = new Instances(newTrain.relationName(), newAttributes, newTrain.numInstances());
      

      newTrainTransformed.setClassIndex(newTrain.classIndex());
      for (int i = 0; i < newTrain.numInstances(); i++) {
        Instance inst = newTrain.instance(i);
        newTrainTransformed.add(inst);
        newTrainTransformed.lastInstance().setClassValue(oldIndexToNewIndex[((int)inst.classValue())]);
      }
      
      newTrain = newTrainTransformed;
    }
    m_DiscretizedHeader = new Instances(newTrain, 0);
    
    int numClasses = newTrain.numClasses();
    

    m_ClassMeans = new double[numClasses];
    int[] classCounts = new int[numClasses];
    for (int i = 0; i < instances.numInstances(); i++) {
      Instance inst = newTrain.instance(i);
      if (!inst.classIsMissing()) {
        int classVal = (int)inst.classValue();
        classCounts[classVal] += 1;
        m_ClassMeans[classVal] += instances.instance(i).classValue();
      }
    }
    
    for (int i = 0; i < numClasses; i++) {
      if (classCounts[i] > 0) {
        m_ClassMeans[i] /= classCounts[i];
      }
    }
    
    if (m_Debug) {
      System.out.println("Bin Means");
      System.out.println("==========");
      for (int i = 0; i < m_ClassMeans.length; i++) {
        System.out.println(m_ClassMeans[i]);
      }
      System.out.println();
    }
    

    m_Classifier.buildClassifier(newTrain);
  }
  







  public double classifyInstance(Instance instance)
    throws Exception
  {
    Instance newInstance = (Instance)instance.copy();
    newInstance.setDataset(m_DiscretizedHeader);
    double[] probs = m_Classifier.distributionForInstance(newInstance);
    

    double prediction = 0.0D;double probSum = 0.0D;
    for (int j = 0; j < probs.length; j++) {
      prediction += probs[j] * m_ClassMeans[j];
      probSum += probs[j];
    }
    
    return prediction / probSum;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tNumber of bins for equal-width discretization\n\t(default 10).\n", "B", 1, "-B <int>"));
    



    newVector.addElement(new Option("\tWhether to delete empty bins after discretization\n\t(default false).\n", "E", 0, "-E"));
    



    newVector.addElement(new Option("\tUse equal-frequency instead of equal-width discretization.", "F", 0, "-F"));
    


    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  





































































  public void setOptions(String[] options)
    throws Exception
  {
    String binsString = Utils.getOption('B', options);
    if (binsString.length() != 0) {
      setNumBins(Integer.parseInt(binsString));
    } else {
      setNumBins(10);
    }
    
    setDeleteEmptyBins(Utils.getFlag('E', options));
    setUseEqualFrequency(Utils.getFlag('F', options));
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 4];
    int current = 0;
    
    options[(current++)] = "-B";
    options[(current++)] = ("" + getNumBins());
    
    if (getDeleteEmptyBins()) {
      options[(current++)] = "-E";
    }
    
    if (getUseEqualFrequency()) {
      options[(current++)] = "-F";
    }
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  






  public String numBinsTipText()
  {
    return "Number of bins for discretization.";
  }
  





  public int getNumBins()
  {
    return m_NumBins;
  }
  





  public void setNumBins(int numBins)
  {
    m_NumBins = numBins;
  }
  







  public String deleteEmptyBinsTipText()
  {
    return "Whether to delete empty bins after discretization.";
  }
  






  public boolean getDeleteEmptyBins()
  {
    return m_DeleteEmptyBins;
  }
  





  public void setDeleteEmptyBins(boolean b)
  {
    m_DeleteEmptyBins = b;
  }
  






  public String useEqualFrequencyTipText()
  {
    return "If set to true, equal-frequency binning will be used instead of equal-width binning.";
  }
  






  public boolean getUseEqualFrequency()
  {
    return m_UseEqualFrequency;
  }
  





  public void setUseEqualFrequency(boolean newUseEqualFrequency)
  {
    m_UseEqualFrequency = newUseEqualFrequency;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("Regression by discretization");
    if (m_ClassMeans == null) {
      text.append(": No model built yet.");
    } else {
      text.append("\n\nClass attribute discretized into " + m_ClassMeans.length + " values\n");
      

      text.append("\nClassifier spec: " + getClassifierSpec() + "\n");
      
      text.append(m_Classifier.toString());
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4746 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new RegressionByDiscretization(), argv);
  }
}

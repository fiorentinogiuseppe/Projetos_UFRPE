package weka.classifiers.bayes;

import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;











































































public class ComplementNaiveBayes
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 7246302925903086397L;
  private double[][] wordWeights;
  private double smoothingParameter = 1.0D;
  

  private boolean m_normalizeWordWeights = false;
  

  private int numClasses;
  

  private Instances header;
  


  public ComplementNaiveBayes() {}
  


  public Enumeration listOptions()
  {
    FastVector newVector = new FastVector(2);
    newVector.addElement(new Option("\tNormalize the word weights for each class\n", "N", 0, "-N"));
    

    newVector.addElement(new Option("\tSmoothing value to avoid zero WordGivenClass probabilities (default=1.0).\n", "S", 1, "-S"));
    



    return newVector.elements();
  }
  




  public String[] getOptions()
  {
    String[] options = new String[4];
    int current = 0;
    
    if (getNormalizeWordWeights()) {
      options[(current++)] = "-N";
    }
    options[(current++)] = "-S";
    options[(current++)] = Double.toString(smoothingParameter);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    setNormalizeWordWeights(Utils.getFlag('N', options));
    
    String val = Utils.getOption('S', options);
    if (val.length() != 0) {
      setSmoothingParameter(Double.parseDouble(val));
    } else {
      setSmoothingParameter(1.0D);
    }
  }
  



  public boolean getNormalizeWordWeights()
  {
    return m_normalizeWordWeights;
  }
  




  public void setNormalizeWordWeights(boolean doNormalize)
  {
    m_normalizeWordWeights = doNormalize;
  }
  




  public String normalizeWordWeightsTipText()
  {
    return "Normalizes the word weights for each class.";
  }
  





  public double getSmoothingParameter()
  {
    return smoothingParameter;
  }
  




  public void setSmoothingParameter(double val)
  {
    smoothingParameter = val;
  }
  




  public String smoothingParameterTipText()
  {
    return "Sets the smoothing parameter to avoid zero WordGivenClass probabilities (default=1.0).";
  }
  






  public String globalInfo()
  {
    return "Class for building and using a Complement class Naive Bayes classifier.\n\nFor more information see, \n\n" + getTechnicalInformation().toString() + "\n\n" + "P.S.: TF, IDF and length normalization transforms, as " + "described in the paper, can be performed through " + "weka.filters.unsupervised.StringToWordVector.";
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Jason D. Rennie and Lawrence Shih and Jaime Teevan and David R. Karger");
    result.setValue(TechnicalInformation.Field.TITLE, "Tackling the Poor Assumptions of Naive Bayes Text Classifiers");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "ICML");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.PAGES, "616-623");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "AAAI Press");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

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
    
    numClasses = instances.numClasses();
    int numAttributes = instances.numAttributes();
    
    header = new Instances(instances, 0);
    double[][] ocrnceOfWordInClass = new double[numClasses][numAttributes];
    wordWeights = new double[numClasses][numAttributes];
    
    double[] wordsPerClass = new double[numClasses];
    double totalWordOccurrences = 0.0D;
    double sumOfSmoothingParams = (numAttributes - 1) * smoothingParameter;
    int classIndex = instances.instance(0).classIndex();
    



    Enumeration enumInsts = instances.enumerateInstances();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance)enumInsts.nextElement();
      int docClass = (int)instance.value(classIndex);
      

      for (int a = 0; a < instance.numValues(); a++) {
        if ((instance.index(a) != instance.classIndex()) && 
          (!instance.isMissing(a))) {
          double numOccurrences = instance.valueSparse(a) * instance.weight();
          if (numOccurrences < 0.0D) {
            throw new Exception("Numeric attribute values must all be greater or equal to zero.");
          }
          
          totalWordOccurrences += numOccurrences;
          wordsPerClass[docClass] += numOccurrences;
          ocrnceOfWordInClass[docClass][instance.index(a)] += numOccurrences;
          



          wordWeights[0][instance.index(a)] += numOccurrences;
        }
      }
    }
    


    for (int c = 1; c < numClasses; c++)
    {
      double totalWordOcrnces = totalWordOccurrences - wordsPerClass[c];
      
      for (int w = 0; w < numAttributes; w++) {
        if (w != classIndex)
        {
          double ocrncesOfWord = wordWeights[0][w] - ocrnceOfWordInClass[c][w];
          

          wordWeights[c][w] = Math.log((ocrncesOfWord + smoothingParameter) / (totalWordOcrnces + sumOfSmoothingParams));
        }
      }
    }
    



    for (int w = 0; w < numAttributes; w++) {
      if (w != classIndex)
      {
        double ocrncesOfWord = wordWeights[0][w] - ocrnceOfWordInClass[0][w];
        
        double totalWordOcrnces = totalWordOccurrences - wordsPerClass[0];
        
        wordWeights[0][w] = Math.log((ocrncesOfWord + smoothingParameter) / (totalWordOcrnces + sumOfSmoothingParams));
      }
    }
    



    if (m_normalizeWordWeights == true) {
      for (int c = 0; c < numClasses; c++) {
        double sum = 0.0D;
        for (int w = 0; w < numAttributes; w++) {
          if (w != classIndex)
            sum += Math.abs(wordWeights[c][w]);
        }
        for (int w = 0; w < numAttributes; w++) {
          if (w != classIndex) {
            wordWeights[c][w] /= sum;
          }
        }
      }
    }
  }
  
















  public double classifyInstance(Instance instance)
    throws Exception
  {
    if (wordWeights == null) {
      throw new Exception("Error. The classifier has not been built properly.");
    }
    
    double[] valueForClass = new double[numClasses];
    double sumOfClassValues = 0.0D;
    
    for (int c = 0; c < numClasses; c++) {
      double sumOfWordValues = 0.0D;
      for (int w = 0; w < instance.numValues(); w++) {
        if (instance.index(w) != instance.classIndex()) {
          double freqOfWordInDoc = instance.valueSparse(w);
          sumOfWordValues += freqOfWordInDoc * wordWeights[c][instance.index(w)];
        }
      }
      

      valueForClass[c] = sumOfWordValues;
      sumOfClassValues += valueForClass[c];
    }
    
    int minidx = 0;
    for (int i = 0; i < numClasses; i++) {
      if (valueForClass[i] < valueForClass[minidx])
        minidx = i;
    }
    return minidx;
  }
  




  public String toString()
  {
    if (wordWeights == null) {
      return "The classifier hasn't been built yet.";
    }
    
    int numAttributes = header.numAttributes();
    StringBuffer result = new StringBuffer("The word weights for each class are: \n------------------------------------\n\t");
    

    for (int c = 0; c < numClasses; c++) {
      result.append(header.classAttribute().value(c)).append("\t");
    }
    result.append("\n");
    
    for (int w = 0; w < numAttributes; w++) {
      result.append(header.attribute(w).name()).append("\t");
      for (int c = 0; c < numClasses; c++)
        result.append(Double.toString(wordWeights[c][w])).append("\t");
      result.append("\n");
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5516 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new ComplementNaiveBayes(), argv);
  }
}

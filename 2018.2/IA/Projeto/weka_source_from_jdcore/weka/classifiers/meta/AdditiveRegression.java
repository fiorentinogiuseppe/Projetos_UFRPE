package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.IteratedSingleClassifierEnhancer;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
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






















































































public class AdditiveRegression
  extends IteratedSingleClassifierEnhancer
  implements OptionHandler, AdditionalMeasureProducer, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -2368937577670527151L;
  protected double m_shrinkage = 1.0D;
  

  protected int m_NumIterationsPerformed;
  

  protected ZeroR m_zeroR;
  

  protected boolean m_SuitableData = true;
  




  public String globalInfo()
  {
    return " Meta classifier that enhances the performance of a regression base classifier. Each iteration fits a model to the residuals left by the classifier on the previous iteration. Prediction is accomplished by adding the predictions of each classifier. Reducing the shrinkage (learning rate) parameter helps prevent overfitting and has a smoothing effect but increases the learning time.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.TECHREPORT);
    result.setValue(TechnicalInformation.Field.AUTHOR, "J.H. Friedman");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.TITLE, "Stochastic Gradient Boosting");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "Stanford University");
    result.setValue(TechnicalInformation.Field.PS, "http://www-stat.stanford.edu/~jhf/ftp/stobst.ps");
    
    return result;
  }
  



  public AdditiveRegression()
  {
    this(new DecisionStump());
  }
  





  public AdditiveRegression(Classifier classifier)
  {
    m_Classifier = classifier;
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.DecisionStump";
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tSpecify shrinkage rate. (default = 1.0, ie. no shrinkage)\n", "S", 1, "-S"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  


































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      Double temp = Double.valueOf(optionString);
      setShrinkage(temp.doubleValue());
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 2];
    int current = 0;
    
    options[(current++)] = "-S";options[(current++)] = ("" + getShrinkage());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String shrinkageTipText()
  {
    return "Shrinkage rate. Smaller values help prevent overfitting and have a smoothing effect (but increase learning time). Default = 1.0, ie. no shrinkage.";
  }
  






  public void setShrinkage(double l)
  {
    m_shrinkage = l;
  }
  




  public double getShrinkage()
  {
    return m_shrinkage;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    super.buildClassifier(data);
    

    getCapabilities().testWithFail(data);
    

    Instances newData = new Instances(data);
    newData.deleteWithMissingClass();
    
    double sum = 0.0D;
    double temp_sum = 0.0D;
    
    m_zeroR = new ZeroR();
    m_zeroR.buildClassifier(newData);
    

    if (newData.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_SuitableData = false;
      return;
    }
    
    m_SuitableData = true;
    

    newData = residualReplace(newData, m_zeroR, false);
    for (int i = 0; i < newData.numInstances(); i++) {
      sum += newData.instance(i).weight() * newData.instance(i).classValue() * newData.instance(i).classValue();
    }
    
    if (m_Debug) {
      System.err.println("Sum of squared residuals (predicting the mean) : " + sum);
    }
    

    m_NumIterationsPerformed = 0;
    do {
      temp_sum = sum;
      

      m_Classifiers[m_NumIterationsPerformed].buildClassifier(newData);
      
      newData = residualReplace(newData, m_Classifiers[m_NumIterationsPerformed], true);
      sum = 0.0D;
      for (int i = 0; i < newData.numInstances(); i++) {
        sum += newData.instance(i).weight() * newData.instance(i).classValue() * newData.instance(i).classValue();
      }
      
      if (m_Debug) {
        System.err.println("Sum of squared residuals : " + sum);
      }
      m_NumIterationsPerformed += 1;
    } while ((temp_sum - sum > Utils.SMALL) && (m_NumIterationsPerformed < m_Classifiers.length));
  }
  







  public double classifyInstance(Instance inst)
    throws Exception
  {
    double prediction = m_zeroR.classifyInstance(inst);
    

    if (!m_SuitableData) {
      return prediction;
    }
    
    for (int i = 0; i < m_NumIterationsPerformed; i++) {
      double toAdd = m_Classifiers[i].classifyInstance(inst);
      toAdd *= getShrinkage();
      prediction += toAdd;
    }
    
    return prediction;
  }
  










  private Instances residualReplace(Instances data, Classifier c, boolean useShrinkage)
    throws Exception
  {
    Instances newInst = new Instances(data);
    
    for (int i = 0; i < newInst.numInstances(); i++) {
      double pred = c.classifyInstance(newInst.instance(i));
      if (useShrinkage) {
        pred *= getShrinkage();
      }
      double residual = newInst.instance(i).classValue() - pred;
      newInst.instance(i).setClassValue(residual);
    }
    
    return newInst;
  }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(1);
    newVector.addElement("measureNumIterations");
    return newVector.elements();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureNumIterations") == 0) {
      return measureNumIterations();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (AdditiveRegression)");
  }
  






  public double measureNumIterations()
  {
    return m_NumIterationsPerformed;
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    

    if (!m_SuitableData) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_zeroR.toString());
      return buf.toString();
    }
    
    if (m_NumIterations == 0) {
      return "Classifier hasn't been built yet!";
    }
    
    text.append("Additive Regression\n\n");
    
    text.append("ZeroR model\n\n" + m_zeroR + "\n\n");
    
    text.append("Base classifier " + getClassifier().getClass().getName() + "\n\n");
    

    text.append("" + m_NumIterationsPerformed + " models generated.\n");
    
    for (int i = 0; i < m_NumIterationsPerformed; i++) {
      text.append("\nModel number " + i + "\n\n" + m_Classifiers[i] + "\n");
    }
    

    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.25 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new AdditiveRegression(), argv);
  }
}

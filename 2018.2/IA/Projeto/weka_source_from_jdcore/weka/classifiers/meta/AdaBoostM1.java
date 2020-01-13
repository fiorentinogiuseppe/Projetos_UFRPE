package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.Sourcable;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;




























































































public class AdaBoostM1
  extends RandomizableIteratedSingleClassifierEnhancer
  implements WeightedInstancesHandler, Sourcable, TechnicalInformationHandler
{
  static final long serialVersionUID = -7378107808933117974L;
  private static int MAX_NUM_RESAMPLING_ITERATIONS = 10;
  

  protected double[] m_Betas;
  

  protected int m_NumIterationsPerformed;
  

  protected int m_WeightThreshold = 100;
  


  protected boolean m_UseResampling;
  

  protected int m_NumClasses;
  

  protected Classifier m_ZeroR;
  


  public AdaBoostM1()
  {
    m_Classifier = new DecisionStump();
  }
  





  public String globalInfo()
  {
    return "Class for boosting a nominal class classifier using the Adaboost M1 method. Only nominal class problems can be tackled. Often dramatically improves performance, but sometimes overfits.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Yoav Freund and Robert E. Schapire");
    result.setValue(TechnicalInformation.Field.TITLE, "Experiments with a new boosting algorithm");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Thirteenth International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.PAGES, "148-156");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    result.setValue(TechnicalInformation.Field.ADDRESS, "San Francisco");
    
    return result;
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.DecisionStump";
  }
  









  protected Instances selectWeightQuantile(Instances data, double quantile)
  {
    int numInstances = data.numInstances();
    Instances trainData = new Instances(data, numInstances);
    double[] weights = new double[numInstances];
    
    double sumOfWeights = 0.0D;
    for (int i = 0; i < numInstances; i++) {
      weights[i] = data.instance(i).weight();
      sumOfWeights += weights[i];
    }
    double weightMassToSelect = sumOfWeights * quantile;
    int[] sortedIndices = Utils.sort(weights);
    

    sumOfWeights = 0.0D;
    for (int i = numInstances - 1; i >= 0; i--) {
      Instance instance = (Instance)data.instance(sortedIndices[i]).copy();
      trainData.add(instance);
      sumOfWeights += weights[sortedIndices[i]];
      if ((sumOfWeights > weightMassToSelect) && (i > 0) && (weights[sortedIndices[i]] != weights[sortedIndices[(i - 1)]])) {
        break;
      }
    }
    

    if (m_Debug) {
      System.err.println("Selected " + trainData.numInstances() + " out of " + numInstances);
    }
    
    return trainData;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tPercentage of weight mass to base training on.\n\t(default 100, reduce to around 90 speed up)", "P", 1, "-P <num>"));
    



    newVector.addElement(new Option("\tUse resampling for boosting.", "Q", 0, "-Q"));
    


    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  












































  public void setOptions(String[] options)
    throws Exception
  {
    String thresholdString = Utils.getOption('P', options);
    if (thresholdString.length() != 0) {
      setWeightThreshold(Integer.parseInt(thresholdString));
    } else {
      setWeightThreshold(100);
    }
    
    setUseResampling(Utils.getFlag('Q', options));
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getUseResampling()) {
      result.add("-Q");
    }
    result.add("-P");
    result.add("" + getWeightThreshold());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String weightThresholdTipText()
  {
    return "Weight threshold for weight pruning.";
  }
  





  public void setWeightThreshold(int threshold)
  {
    m_WeightThreshold = threshold;
  }
  





  public int getWeightThreshold()
  {
    return m_WeightThreshold;
  }
  




  public String useResamplingTipText()
  {
    return "Whether resampling is used instead of reweighting.";
  }
  





  public void setUseResampling(boolean r)
  {
    m_UseResampling = r;
  }
  





  public boolean getUseResampling()
  {
    return m_UseResampling;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    if (super.getCapabilities().handles(Capabilities.Capability.NOMINAL_CLASS))
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
    if (super.getCapabilities().handles(Capabilities.Capability.BINARY_CLASS)) {
      result.enable(Capabilities.Capability.BINARY_CLASS);
    }
    return result;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    super.buildClassifier(data);
    

    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    

    if (data.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(data);
      return;
    }
    
    m_ZeroR = null;
    

    m_NumClasses = data.numClasses();
    if ((!m_UseResampling) && ((m_Classifier instanceof WeightedInstancesHandler)))
    {
      buildClassifierWithWeights(data);
    } else {
      buildClassifierUsingResampling(data);
    }
  }
  










  protected void buildClassifierUsingResampling(Instances data)
    throws Exception
  {
    int numInstances = data.numInstances();
    Random randomInstance = new Random(m_Seed);
    int resamplingIterations = 0;
    

    m_Betas = new double[m_Classifiers.length];
    m_NumIterationsPerformed = 0;
    

    Instances training = new Instances(data, 0, numInstances);
    double sumProbs = training.sumOfWeights();
    for (int i = 0; i < training.numInstances(); i++) {
      training.instance(i).setWeight(training.instance(i).weight() / sumProbs);
    }
    


    for (m_NumIterationsPerformed = 0; m_NumIterationsPerformed < m_Classifiers.length; 
        m_NumIterationsPerformed += 1) {
      if (m_Debug) {
        System.err.println("Training classifier " + (m_NumIterationsPerformed + 1));
      }
      Instances trainData;
      Instances trainData;
      if (m_WeightThreshold < 100) {
        trainData = selectWeightQuantile(training, m_WeightThreshold / 100.0D);
      }
      else {
        trainData = new Instances(training);
      }
      

      resamplingIterations = 0;
      double[] weights = new double[trainData.numInstances()];
      for (int i = 0; i < weights.length; i++)
        weights[i] = trainData.instance(i).weight();
      double epsilon;
      do {
        Instances sample = trainData.resampleWithWeights(randomInstance, weights);
        

        m_Classifiers[m_NumIterationsPerformed].buildClassifier(sample);
        Evaluation evaluation = new Evaluation(data);
        evaluation.evaluateModel(m_Classifiers[m_NumIterationsPerformed], training, new Object[0]);
        
        epsilon = evaluation.errorRate();
        resamplingIterations++;
      } while ((Utils.eq(epsilon, 0.0D)) && (resamplingIterations < MAX_NUM_RESAMPLING_ITERATIONS));
      


      if ((Utils.grOrEq(epsilon, 0.5D)) || (Utils.eq(epsilon, 0.0D))) {
        if (m_NumIterationsPerformed != 0) break;
        m_NumIterationsPerformed = 1; break;
      }
      



      m_Betas[m_NumIterationsPerformed] = Math.log((1.0D - epsilon) / epsilon);
      double reweight = (1.0D - epsilon) / epsilon;
      if (m_Debug) {
        System.err.println("\terror rate = " + epsilon + "  beta = " + m_Betas[m_NumIterationsPerformed]);
      }
      


      setWeights(training, reweight);
    }
  }
  









  protected void setWeights(Instances training, double reweight)
    throws Exception
  {
    double oldSumOfWeights = training.sumOfWeights();
    Enumeration enu = training.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = (Instance)enu.nextElement();
      if (!Utils.eq(m_Classifiers[m_NumIterationsPerformed].classifyInstance(instance), instance.classValue()))
      {
        instance.setWeight(instance.weight() * reweight);
      }
    }
    
    double newSumOfWeights = training.sumOfWeights();
    enu = training.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = (Instance)enu.nextElement();
      instance.setWeight(instance.weight() * oldSumOfWeights / newSumOfWeights);
    }
  }
  












  protected void buildClassifierWithWeights(Instances data)
    throws Exception
  {
    int numInstances = data.numInstances();
    Random randomInstance = new Random(m_Seed);
    

    m_Betas = new double[m_Classifiers.length];
    m_NumIterationsPerformed = 0;
    


    Instances training = new Instances(data, 0, numInstances);
    

    for (m_NumIterationsPerformed = 0; m_NumIterationsPerformed < m_Classifiers.length; 
        m_NumIterationsPerformed += 1) {
      if (m_Debug)
        System.err.println("Training classifier " + (m_NumIterationsPerformed + 1));
      Instances trainData;
      Instances trainData;
      if (m_WeightThreshold < 100) {
        trainData = selectWeightQuantile(training, m_WeightThreshold / 100.0D);
      }
      else {
        trainData = new Instances(training, 0, numInstances);
      }
      

      if ((m_Classifiers[m_NumIterationsPerformed] instanceof Randomizable))
        ((Randomizable)m_Classifiers[m_NumIterationsPerformed]).setSeed(randomInstance.nextInt());
      m_Classifiers[m_NumIterationsPerformed].buildClassifier(trainData);
      

      Evaluation evaluation = new Evaluation(data);
      evaluation.evaluateModel(m_Classifiers[m_NumIterationsPerformed], training, new Object[0]);
      double epsilon = evaluation.errorRate();
      

      if ((Utils.grOrEq(epsilon, 0.5D)) || (Utils.eq(epsilon, 0.0D))) {
        if (m_NumIterationsPerformed != 0) break;
        m_NumIterationsPerformed = 1; break;
      }
      


      m_Betas[m_NumIterationsPerformed] = Math.log((1.0D - epsilon) / epsilon);
      double reweight = (1.0D - epsilon) / epsilon;
      if (m_Debug) {
        System.err.println("\terror rate = " + epsilon + "  beta = " + m_Betas[m_NumIterationsPerformed]);
      }
      


      setWeights(training, reweight);
    }
  }
  









  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    
    if (m_NumIterationsPerformed == 0) {
      throw new Exception("No model built");
    }
    double[] sums = new double[instance.numClasses()];
    
    if (m_NumIterationsPerformed == 1) {
      return m_Classifiers[0].distributionForInstance(instance);
    }
    for (int i = 0; i < m_NumIterationsPerformed; i++) {
      sums[((int)m_Classifiers[i].classifyInstance(instance))] += m_Betas[i];
    }
    return Utils.logs2probs(sums);
  }
  







  public String toSource(String className)
    throws Exception
  {
    if (m_NumIterationsPerformed == 0) {
      throw new Exception("No model built yet");
    }
    if (!(m_Classifiers[0] instanceof Sourcable)) {
      throw new Exception("Base learner " + m_Classifier.getClass().getName() + " is not Sourcable");
    }
    

    StringBuffer text = new StringBuffer("class ");
    text.append(className).append(" {\n\n");
    
    text.append("  public static double classify(Object[] i) {\n");
    
    if (m_NumIterationsPerformed == 1) {
      text.append("    return " + className + "_0.classify(i);\n");
    } else {
      text.append("    double [] sums = new double [" + m_NumClasses + "];\n");
      for (int i = 0; i < m_NumIterationsPerformed; i++) {
        text.append("    sums[(int) " + className + '_' + i + ".classify(i)] += " + m_Betas[i] + ";\n");
      }
      
      text.append("    double maxV = sums[0];\n    int maxI = 0;\n    for (int j = 1; j < " + m_NumClasses + "; j++) {\n" + "      if (sums[j] > maxV) { maxV = sums[j]; maxI = j; }\n" + "    }\n    return (double) maxI;\n");
    }
    



    text.append("  }\n}\n");
    
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append(((Sourcable)m_Classifiers[i]).toSource(className + '_' + i));
    }
    
    return text.toString();
  }
  






  public String toString()
  {
    if (m_ZeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    StringBuffer text = new StringBuffer();
    
    if (m_NumIterationsPerformed == 0) {
      text.append("AdaBoostM1: No model built yet.\n");
    } else if (m_NumIterationsPerformed == 1) {
      text.append("AdaBoostM1: No boosting possible, one classifier used!\n");
      text.append(m_Classifiers[0].toString() + "\n");
    } else {
      text.append("AdaBoostM1: Base classifiers and their weights: \n\n");
      for (int i = 0; i < m_NumIterationsPerformed; i++) {
        text.append(m_Classifiers[i].toString() + "\n\n");
        text.append("Weight: " + Utils.roundDouble(m_Betas[i], 2) + "\n\n");
      }
      text.append("Number of performed Iterations: " + m_NumIterationsPerformed + "\n");
    }
    

    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.40 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new AdaBoostM1(), argv);
  }
}

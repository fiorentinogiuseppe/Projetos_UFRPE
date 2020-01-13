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
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;


















































































































public class LogitBoost
  extends RandomizableIteratedSingleClassifierEnhancer
  implements Sourcable, WeightedInstancesHandler, TechnicalInformationHandler
{
  private static final long serialVersionUID = 8627452775249625582L;
  protected Classifier[][] m_Classifiers;
  protected int m_NumClasses;
  protected int m_NumGenerated;
  protected int m_NumFolds = 0;
  

  protected int m_NumRuns = 1;
  

  protected int m_WeightThreshold = 100;
  

  protected static final double Z_MAX = 3.0D;
  

  protected Instances m_NumericClassData;
  

  protected Attribute m_ClassAttribute;
  

  protected boolean m_UseResampling;
  

  protected double m_Precision = -1.7976931348623157E308D;
  

  protected double m_Shrinkage = 1.0D;
  

  protected Random m_RandomInstance = null;
  


  protected double m_Offset = 0.0D;
  



  protected Classifier m_ZeroR;
  



  public String globalInfo()
  {
    return "Class for performing additive logistic regression. \nThis class performs classification using a regression scheme as the base learner, and can handle multi-class problems.  For more information, see\n\n" + getTechnicalInformation().toString() + "\n\n" + "Can do efficient internal cross-validation to determine " + "appropriate number of iterations.";
  }
  









  public LogitBoost()
  {
    m_Classifier = new DecisionStump();
  }
  








  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.TECHREPORT);
    result.setValue(TechnicalInformation.Field.AUTHOR, "J. Friedman and T. Hastie and R. Tibshirani");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "Additive Logistic Regression: a Statistical View of Boosting");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Stanford University");
    result.setValue(TechnicalInformation.Field.PS, "http://www-stat.stanford.edu/~jhf/ftp/boost.ps");
    
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
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tUse resampling instead of reweighting for boosting.", "Q", 0, "-Q"));
    

    newVector.addElement(new Option("\tPercentage of weight mass to base training on.\n\t(default 100, reduce to around 90 speed up)", "P", 1, "-P <percent>"));
    


    newVector.addElement(new Option("\tNumber of folds for internal cross-validation.\n\t(default 0 -- no cross-validation)", "F", 1, "-F <num>"));
    


    newVector.addElement(new Option("\tNumber of runs for internal cross-validation.\n\t(default 1)", "R", 1, "-R <num>"));
    


    newVector.addElement(new Option("\tThreshold on the improvement of the likelihood.\n\t(default -Double.MAX_VALUE)", "L", 1, "-L <num>"));
    


    newVector.addElement(new Option("\tShrinkage parameter.\n\t(default 1)", "H", 1, "-H <num>"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  




























































  public void setOptions(String[] options)
    throws Exception
  {
    String numFolds = Utils.getOption('F', options);
    if (numFolds.length() != 0) {
      setNumFolds(Integer.parseInt(numFolds));
    } else {
      setNumFolds(0);
    }
    
    String numRuns = Utils.getOption('R', options);
    if (numRuns.length() != 0) {
      setNumRuns(Integer.parseInt(numRuns));
    } else {
      setNumRuns(1);
    }
    
    String thresholdString = Utils.getOption('P', options);
    if (thresholdString.length() != 0) {
      setWeightThreshold(Integer.parseInt(thresholdString));
    } else {
      setWeightThreshold(100);
    }
    
    String precisionString = Utils.getOption('L', options);
    if (precisionString.length() != 0) {
      setLikelihoodThreshold(new Double(precisionString).doubleValue());
    }
    else {
      setLikelihoodThreshold(-1.7976931348623157E308D);
    }
    
    String shrinkageString = Utils.getOption('H', options);
    if (shrinkageString.length() != 0) {
      setShrinkage(new Double(shrinkageString).doubleValue());
    }
    else {
      setShrinkage(1.0D);
    }
    
    setUseResampling(Utils.getFlag('Q', options));
    if ((m_UseResampling) && (thresholdString.length() != 0)) {
      throw new Exception("Weight pruning with resamplingnot allowed.");
    }
    

    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 10];
    
    int current = 0;
    if (getUseResampling()) {
      options[(current++)] = "-Q";
    } else {
      options[(current++)] = "-P";
      options[(current++)] = ("" + getWeightThreshold());
    }
    options[(current++)] = "-F";options[(current++)] = ("" + getNumFolds());
    options[(current++)] = "-R";options[(current++)] = ("" + getNumRuns());
    options[(current++)] = "-L";options[(current++)] = ("" + getLikelihoodThreshold());
    options[(current++)] = "-H";options[(current++)] = ("" + getShrinkage());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String shrinkageTipText()
  {
    return "Shrinkage parameter (use small value like 0.1 to reduce overfitting).";
  }
  






  public double getShrinkage()
  {
    return m_Shrinkage;
  }
  





  public void setShrinkage(double newShrinkage)
  {
    m_Shrinkage = newShrinkage;
  }
  




  public String likelihoodThresholdTipText()
  {
    return "Threshold on improvement in likelihood.";
  }
  





  public double getLikelihoodThreshold()
  {
    return m_Precision;
  }
  





  public void setLikelihoodThreshold(double newPrecision)
  {
    m_Precision = newPrecision;
  }
  




  public String numRunsTipText()
  {
    return "Number of runs for internal cross-validation.";
  }
  





  public int getNumRuns()
  {
    return m_NumRuns;
  }
  





  public void setNumRuns(int newNumRuns)
  {
    m_NumRuns = newNumRuns;
  }
  




  public String numFoldsTipText()
  {
    return "Number of folds for internal cross-validation (default 0 means no cross-validation is performed).";
  }
  






  public int getNumFolds()
  {
    return m_NumFolds;
  }
  





  public void setNumFolds(int newNumFolds)
  {
    m_NumFolds = newNumFolds;
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
  




  public String weightThresholdTipText()
  {
    return "Weight threshold for weight pruning (reduce to 90 for speeding up learning process).";
  }
  






  public void setWeightThreshold(int threshold)
  {
    m_WeightThreshold = threshold;
  }
  





  public int getWeightThreshold()
  {
    return m_WeightThreshold;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    m_RandomInstance = new Random(m_Seed);
    int classIndex = data.classIndex();
    
    if (m_Classifier == null) {
      throw new Exception("A base classifier has not been specified!");
    }
    
    if ((!(m_Classifier instanceof WeightedInstancesHandler)) && (!m_UseResampling))
    {
      m_UseResampling = true;
    }
    

    getCapabilities().testWithFail(data);
    
    if (m_Debug) {
      System.err.println("Creating copy of the training data");
    }
    

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
    m_ClassAttribute = data.classAttribute();
    

    if (m_Debug) {
      System.err.println("Creating base classifiers");
    }
    m_Classifiers = new Classifier[m_NumClasses][];
    for (int j = 0; j < m_NumClasses; j++) {
      m_Classifiers[j] = Classifier.makeCopies(m_Classifier, getNumIterations());
    }
    



    int bestNumIterations = getNumIterations();
    if (m_NumFolds > 1) {
      if (m_Debug) {
        System.err.println("Processing first fold.");
      }
      

      double[] results = new double[getNumIterations()];
      

      for (int r = 0; r < m_NumRuns; r++)
      {

        data.randomize(m_RandomInstance);
        data.stratify(m_NumFolds);
        

        for (int i = 0; i < m_NumFolds; i++)
        {

          Instances train = data.trainCV(m_NumFolds, i, m_RandomInstance);
          Instances test = data.testCV(m_NumFolds, i);
          

          Instances trainN = new Instances(train);
          trainN.setClassIndex(-1);
          trainN.deleteAttributeAt(classIndex);
          trainN.insertAttributeAt(new Attribute("'pseudo class'"), classIndex);
          trainN.setClassIndex(classIndex);
          m_NumericClassData = new Instances(trainN, 0);
          

          int numInstances = train.numInstances();
          double[][] trainFs = new double[numInstances][m_NumClasses];
          double[][] trainYs = new double[numInstances][m_NumClasses];
          for (int j = 0; j < m_NumClasses; j++) {
            for (int k = 0; k < numInstances; k++) {
              trainYs[k][j] = (train.instance(k).classValue() == j ? 1.0D - m_Offset : 0.0D + m_Offset / m_NumClasses);
            }
          }
          


          double[][] probs = initialProbs(numInstances);
          m_NumGenerated = 0;
          double sumOfWeights = train.sumOfWeights();
          for (int j = 0; j < getNumIterations(); j++) {
            performIteration(trainYs, trainFs, probs, trainN, sumOfWeights);
            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(this, test, new Object[0]);
            results[j] += eval.correct();
          }
        }
      }
      

      double bestResult = -1.7976931348623157E308D;
      for (int j = 0; j < getNumIterations(); j++) {
        if (results[j] > bestResult) {
          bestResult = results[j];
          bestNumIterations = j;
        }
      }
      if (m_Debug) {
        System.err.println("Best result for " + bestNumIterations + " iterations: " + bestResult);
      }
    }
    



    int numInstances = data.numInstances();
    double[][] trainFs = new double[numInstances][m_NumClasses];
    double[][] trainYs = new double[numInstances][m_NumClasses];
    for (int j = 0; j < m_NumClasses; j++) {
      int i = 0; for (int k = 0; i < numInstances; k++) {
        trainYs[i][j] = (data.instance(k).classValue() == j ? 1.0D - m_Offset : 0.0D + m_Offset / m_NumClasses);i++;
      }
    }
    


    data.setClassIndex(-1);
    data.deleteAttributeAt(classIndex);
    data.insertAttributeAt(new Attribute("'pseudo class'"), classIndex);
    data.setClassIndex(classIndex);
    m_NumericClassData = new Instances(data, 0);
    

    double[][] probs = initialProbs(numInstances);
    double logLikelihood = logLikelihood(trainYs, probs);
    m_NumGenerated = 0;
    if (m_Debug) {
      System.err.println("Avg. log-likelihood: " + logLikelihood);
    }
    double sumOfWeights = data.sumOfWeights();
    for (int j = 0; j < bestNumIterations; j++) {
      double previousLoglikelihood = logLikelihood;
      performIteration(trainYs, trainFs, probs, data, sumOfWeights);
      logLikelihood = logLikelihood(trainYs, probs);
      if (m_Debug) {
        System.err.println("Avg. log-likelihood: " + logLikelihood);
      }
      if (Math.abs(previousLoglikelihood - logLikelihood) < m_Precision) {
        return;
      }
    }
  }
  






  private double[][] initialProbs(int numInstances)
  {
    double[][] probs = new double[numInstances][m_NumClasses];
    for (int i = 0; i < numInstances; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        probs[i][j] = (1.0D / m_NumClasses);
      }
    }
    return probs;
  }
  








  private double logLikelihood(double[][] trainYs, double[][] probs)
  {
    double logLikelihood = 0.0D;
    for (int i = 0; i < trainYs.length; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        if (trainYs[i][j] == 1.0D - m_Offset) {
          logLikelihood -= Math.log(probs[i][j]);
        }
      }
    }
    return logLikelihood / trainYs.length;
  }
  













  private void performIteration(double[][] trainYs, double[][] trainFs, double[][] probs, Instances data, double origSumOfWeights)
    throws Exception
  {
    if (m_Debug) {
      System.err.println("Training classifier " + (m_NumGenerated + 1));
    }
    

    for (int j = 0; j < m_NumClasses; j++) {
      if (m_Debug) {
        System.err.println("\t...for class " + (j + 1) + " (" + m_ClassAttribute.name() + "=" + m_ClassAttribute.value(j) + ")");
      }
      



      Instances boostData = new Instances(data);
      

      for (int i = 0; i < probs.length; i++)
      {

        double p = probs[i][j];
        double actual = trainYs[i][j];
        double z; if (actual == 1.0D - m_Offset) {
          double z = 1.0D / p;
          if (z > 3.0D) {
            z = 3.0D;
          }
        } else {
          z = -1.0D / (1.0D - p);
          if (z < -3.0D) {
            z = -3.0D;
          }
        }
        double w = (actual - p) / z;
        

        Instance current = boostData.instance(i);
        current.setValue(boostData.classIndex(), z);
        current.setWeight(current.weight() * w);
      }
      

      double sumOfWeights = boostData.sumOfWeights();
      double scalingFactor = origSumOfWeights / sumOfWeights;
      for (int i = 0; i < probs.length; i++) {
        Instance current = boostData.instance(i);
        current.setWeight(current.weight() * scalingFactor);
      }
      

      Instances trainData = boostData;
      if (m_WeightThreshold < 100) {
        trainData = selectWeightQuantile(boostData, m_WeightThreshold / 100.0D);

      }
      else if (m_UseResampling) {
        double[] weights = new double[boostData.numInstances()];
        for (int kk = 0; kk < weights.length; kk++) {
          weights[kk] = boostData.instance(kk).weight();
        }
        trainData = boostData.resampleWithWeights(m_RandomInstance, weights);
      }
      



      m_Classifiers[j][m_NumGenerated].buildClassifier(trainData);
    }
    

    for (int i = 0; i < trainFs.length; i++) {
      double[] pred = new double[m_NumClasses];
      double predSum = 0.0D;
      for (int j = 0; j < m_NumClasses; j++) {
        pred[j] = (m_Shrinkage * m_Classifiers[j][m_NumGenerated].classifyInstance(data.instance(i)));
        
        predSum += pred[j];
      }
      predSum /= m_NumClasses;
      for (int j = 0; j < m_NumClasses; j++) {
        trainFs[i][j] += (pred[j] - predSum) * (m_NumClasses - 1) / m_NumClasses;
      }
    }
    
    m_NumGenerated += 1;
    

    for (int i = 0; i < trainYs.length; i++) {
      probs[i] = probs(trainFs[i]);
    }
  }
  





  public Classifier[][] classifiers()
  {
    Classifier[][] classifiers = new Classifier[m_NumClasses][m_NumGenerated];
    
    for (int j = 0; j < m_NumClasses; j++) {
      for (int i = 0; i < m_NumGenerated; i++) {
        classifiers[j][i] = m_Classifiers[j][i];
      }
    }
    return classifiers;
  }
  






  private double[] probs(double[] Fs)
  {
    double maxF = -1.7976931348623157E308D;
    for (int i = 0; i < Fs.length; i++) {
      if (Fs[i] > maxF) {
        maxF = Fs[i];
      }
    }
    double sum = 0.0D;
    double[] probs = new double[Fs.length];
    for (int i = 0; i < Fs.length; i++) {
      probs[i] = Math.exp(Fs[i] - maxF);
      sum += probs[i];
    }
    Utils.normalize(probs, sum);
    return probs;
  }
  









  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    
    instance = (Instance)instance.copy();
    instance.setDataset(m_NumericClassData);
    double[] pred = new double[m_NumClasses];
    double[] Fs = new double[m_NumClasses];
    for (int i = 0; i < m_NumGenerated; i++) {
      double predSum = 0.0D;
      for (int j = 0; j < m_NumClasses; j++) {
        pred[j] = (m_Shrinkage * m_Classifiers[j][i].classifyInstance(instance));
        predSum += pred[j];
      }
      predSum /= m_NumClasses;
      for (int j = 0; j < m_NumClasses; j++) {
        Fs[j] += (pred[j] - predSum) * (m_NumClasses - 1) / m_NumClasses;
      }
    }
    

    return probs(Fs);
  }
  






  public String toSource(String className)
    throws Exception
  {
    if (m_NumGenerated == 0) {
      throw new Exception("No model built yet");
    }
    if (!(m_Classifiers[0][0] instanceof Sourcable)) {
      throw new Exception("Base learner " + m_Classifier.getClass().getName() + " is not Sourcable");
    }
    

    StringBuffer text = new StringBuffer("class ");
    text.append(className).append(" {\n\n");
    text.append("  private static double RtoP(double []R, int j) {\n    double Rcenter = 0;\n    for (int i = 0; i < R.length; i++) {\n      Rcenter += R[i];\n    }\n    Rcenter /= R.length;\n    double Rsum = 0;\n    for (int i = 0; i < R.length; i++) {\n      Rsum += Math.exp(R[i] - Rcenter);\n    }\n    return Math.exp(R[j]) / Rsum;\n  }\n\n");
    











    text.append("  public static double classify(Object[] i) {\n    double [] d = distribution(i);\n    double maxV = d[0];\n    int maxI = 0;\n    for (int j = 1; j < " + m_NumClasses + "; j++) {\n" + "      if (d[j] > maxV) { maxV = d[j]; maxI = j; }\n" + "    }\n    return (double) maxI;\n  }\n\n");
    






    text.append("  public static double [] distribution(Object [] i) {\n");
    text.append("    double [] Fs = new double [" + m_NumClasses + "];\n");
    text.append("    double [] Fi = new double [" + m_NumClasses + "];\n");
    text.append("    double Fsum;\n");
    for (int i = 0; i < m_NumGenerated; i++) {
      text.append("    Fsum = 0;\n");
      for (int j = 0; j < m_NumClasses; j++) {
        text.append("    Fi[" + j + "] = " + className + '_' + j + '_' + i + ".classify(i); Fsum += Fi[" + j + "];\n");
      }
      
      text.append("    Fsum /= " + m_NumClasses + ";\n");
      text.append("    for (int j = 0; j < " + m_NumClasses + "; j++) {");
      text.append(" Fs[j] += (Fi[j] - Fsum) * " + (m_NumClasses - 1) + " / " + m_NumClasses + "; }\n");
    }
    

    text.append("    double [] dist = new double [" + m_NumClasses + "];\n" + "    for (int j = 0; j < " + m_NumClasses + "; j++) {\n" + "      dist[j] = RtoP(Fs, j);\n" + "    }\n    return dist;\n");
    


    text.append("  }\n}\n");
    
    for (int i = 0; i < m_Classifiers.length; i++) {
      for (int j = 0; j < m_Classifiers[i].length; j++) {
        text.append(((Sourcable)m_Classifiers[i][j]).toSource(className + '_' + i + '_' + j));
      }
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
    
    if (m_NumGenerated == 0) {
      text.append("LogitBoost: No model built yet.");
    }
    else {
      text.append("LogitBoost: Base classifiers and their weights: \n");
      for (int i = 0; i < m_NumGenerated; i++) {
        text.append("\nIteration " + (i + 1));
        for (int j = 0; j < m_NumClasses; j++) {
          text.append("\n\tClass " + (j + 1) + " (" + m_ClassAttribute.name() + "=" + m_ClassAttribute.value(j) + ")\n\n" + m_Classifiers[j][i].toString() + "\n");
        }
      }
      


      text.append("Number of performed iterations: " + m_NumGenerated + "\n");
    }
    

    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9371 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new LogitBoost(), argv);
  }
}

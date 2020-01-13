package weka.classifiers.bayes;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.blr.GaussianPriorImpl;
import weka.classifiers.bayes.blr.LaplacePriorImpl;
import weka.classifiers.bayes.blr.Prior;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.SerializedObject;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
























































public class BayesianLogisticRegression
  extends Classifier
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -8013478897911757631L;
  public static double[] LogLikelihood;
  public static double[] InputHyperparameterValues;
  boolean debug = false;
  

  public boolean NormalizeData = false;
  

  public double Tolerance = 5.0E-4D;
  

  public double Threshold = 0.5D;
  
  public static final int GAUSSIAN = 1;
  
  public static final int LAPLACIAN = 2;
  
  public static final Tag[] TAGS_PRIOR = { new Tag(1, "Gaussian"), new Tag(2, "Laplacian") };
  




  public int PriorClass = 1;
  

  public int NumFolds = 2;
  

  public int m_seed = 1;
  
  public static final int NORM_BASED = 1;
  
  public static final int CV_BASED = 2;
  
  public static final int SPECIFIC_VALUE = 3;
  public static final Tag[] TAGS_HYPER_METHOD = { new Tag(1, "Norm-based"), new Tag(2, "CV-based"), new Tag(3, "Specific value") };
  





  public int HyperparameterSelection = 1;
  

  public int ClassIndex = -1;
  

  public double HyperparameterValue = 0.27D;
  

  public String HyperparameterRange = "R:0.01-316,3.16";
  

  public int maxIterations = 100;
  

  public int iterationCounter = 0;
  


  public double[] BetaVector;
  


  public double[] DeltaBeta;
  


  public double[] DeltaUpdate;
  


  public double[] Delta;
  


  public double[] Hyperparameters;
  


  public double[] R;
  


  public double[] DeltaR;
  

  public double Change;
  

  public Filter m_Filter;
  

  protected Instances m_Instances;
  

  protected Prior m_PriorUpdate;
  


  public BayesianLogisticRegression() {}
  


  public String globalInfo()
  {
    return "Implements Bayesian Logistic Regression for both Gaussian and Laplace Priors.\n\nFor more information, see\n\n" + getTechnicalInformation();
  }
  













  public void initialize()
    throws Exception
  {
    Change = 0.0D;
    

    if (NormalizeData) {
      m_Filter = new Normalize();
      m_Filter.setInputFormat(m_Instances);
      m_Instances = Filter.useFilter(m_Instances, m_Filter);
    }
    

    String attName = "(intercept)";
    String attAtZero = m_Instances.attribute(0).name();
    int attNameIncr = 0;
    if (attAtZero.startsWith(attName)) {
      if (attAtZero.indexOf(')') < attAtZero.length() - 1)
      {
        String tempNum = attAtZero.substring(attAtZero.indexOf(')') + 1, attAtZero.length());
        
        attNameIncr = Integer.parseInt(tempNum);
        attNameIncr++;
      }
      attName = attName + "" + attNameIncr;
    }
    
    Attribute att = new Attribute(attName);
    

    m_Instances.insertAttributeAt(att, 0);
    
    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance instance = m_Instances.instance(i);
      instance.setValue(0, 1.0D);
    }
    

    int numOfAttributes = m_Instances.numAttributes();
    int numOfInstances = m_Instances.numInstances();
    ClassIndex = m_Instances.classIndex();
    iterationCounter = 0;
    

    switch (HyperparameterSelection) {
    case 1: 
      HyperparameterValue = normBasedHyperParameter();
      
      if (debug) {
        System.out.println("Norm-based Hyperparameter: " + HyperparameterValue);
      }
      

      break;
    case 2: 
      HyperparameterValue = CVBasedHyperparameter();
      
      if (debug) {
        System.out.println("CV-based Hyperparameter: " + HyperparameterValue);
      }
      
      break;
    }
    
    BetaVector = new double[numOfAttributes];
    Delta = new double[numOfAttributes];
    DeltaBeta = new double[numOfAttributes];
    Hyperparameters = new double[numOfAttributes];
    DeltaUpdate = new double[numOfAttributes];
    
    for (int j = 0; j < numOfAttributes; j++) {
      BetaVector[j] = 0.0D;
      Delta[j] = 1.0D;
      DeltaBeta[j] = 0.0D;
      DeltaUpdate[j] = 0.0D;
      

      Hyperparameters[j] = HyperparameterValue;
    }
    
    DeltaR = new double[numOfInstances];
    R = new double[numOfInstances];
    
    for (i = 0; i < numOfInstances; i++) {
      DeltaR[i] = 0.0D;
      R[i] = 0.0D;
    }
    

    if (PriorClass == 1) {
      m_PriorUpdate = new GaussianPriorImpl();
    } else {
      m_PriorUpdate = new LaplacePriorImpl();
    }
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    
    result.enable(Capabilities.Capability.BINARY_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.BINARY_CLASS);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  











  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    m_Instances = new Instances(data);
    

    initialize();
    
    do
    {
      for (int j = 0; j < m_Instances.numAttributes(); j++) {
        if (j != ClassIndex) {
          DeltaUpdate[j] = m_PriorUpdate.update(j, m_Instances, BetaVector[j], Hyperparameters[j], R, Delta[j]);
          

          DeltaBeta[j] = Math.min(Math.max(DeltaUpdate[j], 0.0D - Delta[j]), Delta[j]);
          


          for (int i = 0; i < m_Instances.numInstances(); i++) {
            Instance instance = m_Instances.instance(i);
            
            if (instance.value(j) != 0.0D) {
              DeltaR[i] = (DeltaBeta[j] * instance.value(j) * classSgn(instance.classValue()));
              R[i] += DeltaR[i];
            }
          }
          

          BetaVector[j] += DeltaBeta[j];
          

          Delta[j] = Math.max(2.0D * Math.abs(DeltaBeta[j]), Delta[j] / 2.0D);
        }
      }
    } while (!stoppingCriterion());
    
    m_PriorUpdate.computelogLikelihood(BetaVector, m_Instances);
    m_PriorUpdate.computePenalty(BetaVector, Hyperparameters);
  }
  














  public static double classSgn(double value)
  {
    if (value == 0.0D) {
      return -1.0D;
    }
    return 1.0D;
  }
  







  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = null;
    
    result = new TechnicalInformation(TechnicalInformation.Type.TECHREPORT);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Alexander Genkin and David D. Lewis and David Madigan");
    result.setValue(TechnicalInformation.Field.YEAR, "2004");
    result.setValue(TechnicalInformation.Field.TITLE, "Large-scale bayesian logistic regression for text categorization");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "DIMACS");
    result.setValue(TechnicalInformation.Field.URL, "http://www.stat.rutgers.edu/~madigan/PAPERS/shortFat-v3a.pdf");
    return result;
  }
  








  public static double bigF(double r, double sigma)
  {
    double funcValue = 0.25D;
    double absR = Math.abs(r);
    
    if (absR > sigma) {
      funcValue = 1.0D / (2.0D + Math.exp(absR - sigma) + Math.exp(sigma - absR));
    }
    
    return funcValue;
  }
  






  public boolean stoppingCriterion()
  {
    double sum_deltaR = 0.0D;
    double sum_R = 1.0D;
    
    double value = 0.0D;
    


    for (int i = 0; i < m_Instances.numInstances(); i++) {
      sum_deltaR += Math.abs(DeltaR[i]);
      sum_R += Math.abs(R[i]);
    }
    
    double delta = Math.abs(sum_deltaR - Change);
    Change = (delta / sum_R);
    
    if (debug) {
      System.out.println(Change + " <= " + Tolerance);
    }
    
    boolean shouldStop = (Change <= Tolerance) || (iterationCounter >= maxIterations);
    
    iterationCounter += 1;
    Change = sum_deltaR;
    
    return shouldStop;
  }
  





  public static double logisticLinkFunction(double r)
  {
    return Math.exp(r) / (1.0D + Math.exp(r));
  }
  




  public static double sgn(double r)
  {
    double sgn = 0.0D;
    
    if (r > 0.0D) {
      sgn = 1.0D;
    } else if (r < 0.0D) {
      sgn = -1.0D;
    }
    
    return sgn;
  }
  






  public double normBasedHyperParameter()
  {
    double mean = 0.0D;
    
    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance instance = m_Instances.instance(i);
      
      double sqr_sum = 0.0D;
      
      for (int j = 0; j < m_Instances.numAttributes(); j++) {
        if (j != ClassIndex) {
          sqr_sum += instance.value(j) * instance.value(j);
        }
      }
      

      mean += sqr_sum;
    }
    
    mean /= m_Instances.numInstances();
    
    return m_Instances.numAttributes() / mean;
  }
  






  public double classifyInstance(Instance instance)
    throws Exception
  {
    double sum_R = 0.0D;
    double classification = 0.0D;
    
    sum_R = BetaVector[0];
    
    for (int j = 0; j < instance.numAttributes(); j++) {
      if (j != ClassIndex - 1) {
        sum_R += BetaVector[(j + 1)] * instance.value(j);
      }
    }
    
    sum_R = logisticLinkFunction(sum_R);
    
    if (sum_R > Threshold) {
      classification = 1.0D;
    } else {
      classification = 0.0D;
    }
    
    return classification;
  }
  





  public String toString()
  {
    if (m_Instances == null) {
      return "Bayesian logistic regression: No model built yet.";
    }
    
    StringBuffer buf = new StringBuffer();
    String text = "";
    
    switch (HyperparameterSelection) {
    case 1: 
      text = "Norm-Based Hyperparameter Selection: ";
      
      break;
    
    case 2: 
      text = "Cross-Validation Based Hyperparameter Selection: ";
      
      break;
    
    case 3: 
      text = "Specified Hyperparameter: ";
    }
    
    

    buf.append(text).append(HyperparameterValue).append("\n\n");
    
    buf.append("Regression Coefficients\n");
    buf.append("=========================\n\n");
    
    for (int j = 0; j < m_Instances.numAttributes(); j++) {
      if ((j != ClassIndex) && 
        (BetaVector[j] != 0.0D)) {
        buf.append(m_Instances.attribute(j).name()).append(" : ").append(BetaVector[j]).append("\n");
      }
    }
    


    buf.append("===========================\n\n");
    buf.append("Likelihood: " + m_PriorUpdate.getLoglikelihood() + "\n\n");
    buf.append("Penalty: " + m_PriorUpdate.getPenalty() + "\n\n");
    buf.append("Regularized Log Posterior: " + m_PriorUpdate.getLogPosterior() + "\n");
    
    buf.append("===========================\n\n");
    
    return buf.toString();
  }
  













  public double CVBasedHyperparameter()
    throws Exception
  {
    int size = 0;
    double[] list = null;
    double MaxHypeValue = 0.0D;
    double MaxLikelihood = 0.0D;
    StringTokenizer tokenizer = new StringTokenizer(HyperparameterRange);
    String rangeType = tokenizer.nextToken(":");
    
    if (rangeType.equals("R")) {
      String temp = tokenizer.nextToken();
      tokenizer = new StringTokenizer(temp);
      double start = Double.parseDouble(tokenizer.nextToken("-"));
      tokenizer = new StringTokenizer(tokenizer.nextToken());
      double end = Double.parseDouble(tokenizer.nextToken(","));
      double multiplier = Double.parseDouble(tokenizer.nextToken());
      
      int steps = (int)((Math.log10(end) - Math.log10(start)) / Math.log10(multiplier) + 1.0D);
      
      list = new double[steps];
      
      int count = 0;
      
      for (double i = start; i <= end; i *= multiplier) {
        list[(count++)] = i;
      }
    } else if (rangeType.equals("L")) {
      Vector vec = new Vector();
      
      while (tokenizer.hasMoreTokens()) {
        vec.add(tokenizer.nextToken(","));
      }
      
      list = new double[vec.size()];
      
      for (int i = 0; i < vec.size(); i++) {
        list[i] = Double.parseDouble((String)vec.get(i));
      }
    }
    




    if (list != null) {
      int numFolds = NumFolds;
      Random random = new Random(m_seed);
      m_Instances.randomize(random);
      m_Instances.stratify(numFolds);
      
      for (int k = 0; k < list.length; k++) {
        for (int i = 0; i < numFolds; i++) {
          Instances train = m_Instances.trainCV(numFolds, i, random);
          SerializedObject so = new SerializedObject(this);
          BayesianLogisticRegression blr = (BayesianLogisticRegression)so.getObject();
          
          blr.setHyperparameterSelection(new SelectedTag(3, TAGS_HYPER_METHOD));
          
          blr.setHyperparameterValue(list[k]);
          
          blr.setPriorClass(new SelectedTag(PriorClass, TAGS_PRIOR));
          
          blr.setThreshold(Threshold);
          blr.setTolerance(Tolerance);
          blr.buildClassifier(train);
          
          Instances test = m_Instances.testCV(numFolds, i);
          double val = blr.getLoglikeliHood(BetaVector, test);
          
          if (debug) {
            System.out.println("Fold " + i + "Hyperparameter: " + list[k]);
            System.out.println("===================================");
            System.out.println(" Likelihood: " + val);
          }
          
          if (((k == 0 ? 1 : 0) | (val > MaxLikelihood ? 1 : 0)) != 0) {
            MaxLikelihood = val;
            MaxHypeValue = list[k];
          }
        }
      }
    } else {
      return HyperparameterValue;
    }
    
    return MaxHypeValue;
  }
  



  public double getLoglikeliHood(double[] betas, Instances instances)
  {
    m_PriorUpdate.computelogLikelihood(betas, instances);
    
    return m_PriorUpdate.getLoglikelihood();
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tShow Debugging Output\n", "D", 0, "-D"));
    newVector.addElement(new Option("\tDistribution of the Prior (1=Gaussian, 2=Laplacian)\n\t(default: 1=Gaussian)", "P", 1, "-P <integer>"));
    



    newVector.addElement(new Option("\tHyperparameter Selection Method (1=Norm-based, 2=CV-based, 3=specific value)\n\t(default: 1=Norm-based)", "H", 1, "-H <integer>"));
    




    newVector.addElement(new Option("\tSpecified Hyperparameter Value (use in conjunction with -H 3)\n\t(default: 0.27)", "V", 1, "-V <double>"));
    



    newVector.addElement(new Option("\tHyperparameter Range (use in conjunction with -H 2)\n\t(format: R:start-end,multiplier OR L:val(1), val(2), ..., val(n))\n\t(default: R:0.01-316,3.16)", "R", 1, "-R <string>"));
    





    newVector.addElement(new Option("\tTolerance Value\n\t(default: 0.0005)", "Tl", 1, "-Tl <double>"));
    


    newVector.addElement(new Option("\tThreshold Value\n\t(default: 0.5)", "S", 1, "-S <double>"));
    


    newVector.addElement(new Option("\tNumber Of Folds (use in conjuction with -H 2)\n\t(default: 2)", "F", 1, "-F <integer>"));
    



    newVector.addElement(new Option("\tMax Number of Iterations\n\t(default: 100)", "I", 1, "-I <integer>"));
    


    newVector.addElement(new Option("\tNormalize the data", "N", 0, "-N"));
    

    newVector.addElement(new Option("\tSeed for randomizing instances order\n\tin CV-based hyperparameter selection\n\t(default: 1)", "seed", 1, "-seed <number>"));
    



    return newVector.elements();
  }
  























































  public void setOptions(String[] options)
    throws Exception
  {
    debug = Utils.getFlag('D', options);
    

    String Tol = Utils.getOption("Tl", options);
    
    if (Tol.length() != 0) {
      Tolerance = Double.parseDouble(Tol);
    }
    

    String Thres = Utils.getOption('S', options);
    
    if (Thres.length() != 0) {
      Threshold = Double.parseDouble(Thres);
    }
    

    String Hype = Utils.getOption('H', options);
    
    if (Hype.length() != 0) {
      HyperparameterSelection = Integer.parseInt(Hype);
    }
    

    String HyperValue = Utils.getOption('V', options);
    
    if (HyperValue.length() != 0) {
      HyperparameterValue = Double.parseDouble(HyperValue);
    }
    

    String HyperparameterRange = Utils.getOption("R", options);
    

    String strPrior = Utils.getOption('P', options);
    
    if (strPrior.length() != 0) {
      PriorClass = Integer.parseInt(strPrior);
    }
    
    String folds = Utils.getOption('F', options);
    
    if (folds.length() != 0) {
      NumFolds = Integer.parseInt(folds);
    }
    
    String seed = Utils.getOption("seed", options);
    if (seed.length() > 0) {
      setSeed(Integer.parseInt(seed));
    }
    
    String iterations = Utils.getOption('I', options);
    
    if (iterations.length() != 0) {
      maxIterations = Integer.parseInt(iterations);
    }
    
    NormalizeData = Utils.getFlag('N', options);
    

    Utils.checkForRemainingOptions(options);
  }
  


  public String[] getOptions()
  {
    Vector result = new Vector();
    

    result.add("-D");
    

    result.add("-Tl");
    result.add("" + Tolerance);
    

    result.add("-S");
    result.add("" + Threshold);
    

    result.add("-H");
    result.add("" + HyperparameterSelection);
    
    result.add("-V");
    result.add("" + HyperparameterValue);
    
    result.add("-R");
    result.add("" + HyperparameterRange);
    

    result.add("-P");
    result.add("" + PriorClass);
    
    result.add("-F");
    result.add("" + NumFolds);
    
    result.add("-seed");
    result.add("" + getSeed());
    
    result.add("-I");
    result.add("" + maxIterations);
    
    result.add("-N");
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new BayesianLogisticRegression(), argv);
  }
  





  public String debugTipText()
  {
    return "Turns on debugging mode.";
  }
  


  public void setDebug(boolean debugMode)
  {
    debug = debugMode;
  }
  





  public String hyperparameterSelectionTipText()
  {
    return "Select the type of Hyperparameter to be used.";
  }
  




  public SelectedTag getHyperparameterSelection()
  {
    return new SelectedTag(HyperparameterSelection, TAGS_HYPER_METHOD);
  }
  





  public void setHyperparameterSelection(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_HYPER_METHOD) {
      int c = newMethod.getSelectedTag().getID();
      if ((c >= 1) && (c <= 3)) {
        HyperparameterSelection = c;
      } else {
        throw new IllegalArgumentException("Wrong selection type, -H value should be: 1 for norm-based, 2 for CV-based and 3 for specific value");
      }
    }
  }
  







  public String priorClassTipText()
  {
    return "The type of prior to be used.";
  }
  




  public void setPriorClass(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_PRIOR) {
      int c = newMethod.getSelectedTag().getID();
      if ((c == 1) || (c == 2)) {
        PriorClass = c;
      } else {
        throw new IllegalArgumentException("Wrong selection type, -P value should be: 1 for Gaussian or 2 for Laplacian");
      }
    }
  }
  





  public SelectedTag getPriorClass()
  {
    return new SelectedTag(PriorClass, TAGS_PRIOR);
  }
  






  public String thresholdTipText()
  {
    return "Set the threshold for classifiction. The logistic function doesn't return a class label but an estimate of p(y=+1|B,x(i)). These estimates need to be converted to binary class label predictions. values above the threshold are assigned class +1.";
  }
  







  public double getThreshold()
  {
    return Threshold;
  }
  




  public void setThreshold(double threshold)
  {
    Threshold = threshold;
  }
  





  public String toleranceTipText()
  {
    return "This value decides the stopping criterion.";
  }
  




  public double getTolerance()
  {
    return Tolerance;
  }
  




  public void setTolerance(double tolerance)
  {
    Tolerance = tolerance;
  }
  





  public String hyperparameterValueTipText()
  {
    return "Specific hyperparameter value. Used when the hyperparameter selection method is set to specific value";
  }
  






  public double getHyperparameterValue()
  {
    return HyperparameterValue;
  }
  





  public void setHyperparameterValue(double hyperparameterValue)
  {
    HyperparameterValue = hyperparameterValue;
  }
  





  public String numFoldsTipText()
  {
    return "The number of folds to use for CV-based hyperparameter selection.";
  }
  




  public int getNumFolds()
  {
    return NumFolds;
  }
  





  public void setNumFolds(int numFolds)
  {
    NumFolds = numFolds;
  }
  





  public String seedTipText()
  {
    return "Seed for randomizing instances order prior to CV-based hyperparameter selection";
  }
  






  public void setSeed(int seed)
  {
    m_seed = seed;
  }
  





  public int getSeed()
  {
    return m_seed;
  }
  





  public String maxIterationsTipText()
  {
    return "The maximum number of iterations to perform.";
  }
  




  public int getMaxIterations()
  {
    return maxIterations;
  }
  




  public void setMaxIterations(int maxIterations)
  {
    this.maxIterations = maxIterations;
  }
  





  public String normalizeDataTipText()
  {
    return "Normalize the data.";
  }
  




  public boolean isNormalizeData()
  {
    return NormalizeData;
  }
  




  public void setNormalizeData(boolean normalizeData)
  {
    NormalizeData = normalizeData;
  }
  





  public String hyperparameterRangeTipText()
  {
    return "Hyperparameter value range. In case of CV-based Hyperparameters, you can specify the range in two ways: \nComma-Separated: L: 3,5,6 (This will be a list of possible values.)\nRange: R:0.01-316,3.16 (This will take values from 0.01-316 (inclusive) in multiplications of 3.16";
  }
  









  public String getHyperparameterRange()
  {
    return HyperparameterRange;
  }
  





  public void setHyperparameterRange(String hyperparameterRange)
  {
    HyperparameterRange = hyperparameterRange;
  }
  




  public boolean isDebug()
  {
    return debug;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7984 $");
  }
}

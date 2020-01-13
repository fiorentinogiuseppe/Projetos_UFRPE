package weka.classifiers.functions;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.classifiers.RandomizableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;








































































































































































































public class LibSVM
  extends RandomizableClassifier
  implements TechnicalInformationHandler
{
  protected static final String CLASS_SVM = "libsvm.svm";
  protected static final String CLASS_SVMMODEL = "libsvm.svm_model";
  protected static final String CLASS_SVMPROBLEM = "libsvm.svm_problem";
  protected static final String CLASS_SVMPARAMETER = "libsvm.svm_parameter";
  protected static final String CLASS_SVMNODE = "libsvm.svm_node";
  protected static final long serialVersionUID = 14172L;
  protected Object m_Model;
  protected Filter m_Filter = null;
  

  protected Filter m_NominalToBinary;
  

  protected ReplaceMissingValues m_ReplaceMissingValues;
  

  protected boolean m_Normalize = false;
  

  private boolean m_noReplaceMissingValues;
  

  public static final int SVMTYPE_C_SVC = 0;
  
  public static final int SVMTYPE_NU_SVC = 1;
  
  public static final int SVMTYPE_ONE_CLASS_SVM = 2;
  
  public static final int SVMTYPE_EPSILON_SVR = 3;
  
  public static final int SVMTYPE_NU_SVR = 4;
  
  public static final Tag[] TAGS_SVMTYPE = { new Tag(0, "C-SVC (classification)"), new Tag(1, "nu-SVC (classification)"), new Tag(2, "one-class SVM (classification)"), new Tag(3, "epsilon-SVR (regression)"), new Tag(4, "nu-SVR (regression)") };
  






  protected int m_SVMType = 0;
  

  public static final int KERNELTYPE_LINEAR = 0;
  
  public static final int KERNELTYPE_POLYNOMIAL = 1;
  
  public static final int KERNELTYPE_RBF = 2;
  
  public static final int KERNELTYPE_SIGMOID = 3;
  
  public static final Tag[] TAGS_KERNELTYPE = { new Tag(0, "linear: u'*v"), new Tag(1, "polynomial: (gamma*u'*v + coef0)^degree"), new Tag(2, "radial basis function: exp(-gamma*|u-v|^2)"), new Tag(3, "sigmoid: tanh(gamma*u'*v + coef0)") };
  





  protected int m_KernelType = 2;
  




  protected int m_Degree = 3;
  

  protected double m_Gamma = 0.0D;
  

  protected double m_GammaActual = 0.0D;
  

  protected double m_Coef0 = 0.0D;
  

  protected double m_CacheSize = 40.0D;
  

  protected double m_eps = 0.001D;
  

  protected double m_Cost = 1.0D;
  

  protected int[] m_WeightLabel = new int[0];
  

  protected double[] m_Weight = new double[0];
  

  protected double m_nu = 0.5D;
  

  protected double m_Loss = 0.1D;
  

  protected boolean m_Shrinking = true;
  




  protected boolean m_ProbabilityEstimates = false;
  

  protected static boolean m_Present = false;
  
  static {
    try { Class.forName("libsvm.svm");
      m_Present = true;
    } catch (Exception e) {
      m_Present = false;
    }
  }
  





  public String globalInfo()
  {
    return "A wrapper class for the libsvm tools (the libsvm classes, typically the jar file, need to be in the classpath to use this classifier).\nLibSVM runs faster than SMO since it uses LibSVM to build the SVM classifier.\nLibSVM allows users to experiment with One-class SVM, Regressing SVM, and nu-SVM supported by LibSVM tool. LibSVM reports many useful statistics about LibSVM classifier (e.g., confusion matrix,precision, recall, ROC score, etc.).\n\n" + getTechnicalInformation().toString();
  }
  



















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Yasser EL-Manzalawy");
    result.setValue(TechnicalInformation.Field.YEAR, "2005");
    result.setValue(TechnicalInformation.Field.TITLE, "WLSVM");
    result.setValue(TechnicalInformation.Field.NOTE, "LibSVM was originally developed as 'WLSVM'");
    
    result.setValue(TechnicalInformation.Field.URL, "http://www.cs.iastate.edu/~yasser/wlsvm/");
    
    result.setValue(TechnicalInformation.Field.NOTE, "You don't need to include the WLSVM package in the CLASSPATH");
    

    TechnicalInformation additional = result.add(TechnicalInformation.Type.MISC);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Chih-Chung Chang and Chih-Jen Lin");
    
    additional.setValue(TechnicalInformation.Field.TITLE, "LIBSVM - A Library for Support Vector Machines");
    
    additional.setValue(TechnicalInformation.Field.YEAR, "2001");
    additional.setValue(TechnicalInformation.Field.URL, "http://www.csie.ntu.edu.tw/~cjlin/libsvm/");
    
    additional.setValue(TechnicalInformation.Field.NOTE, "The Weka classifier works with version 2.82 of LIBSVM");
    

    return result;
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSet type of SVM (default: 0)\n\t\t 0 = C-SVC\n\t\t 1 = nu-SVC\n\t\t 2 = one-class SVM\n\t\t 3 = epsilon-SVR\n\t\t 4 = nu-SVR", "S", 1, "-S <int>"));
    


    result.addElement(new Option("\tSet type of kernel function (default: 2)\n\t\t 0 = linear: u'*v\n\t\t 1 = polynomial: (gamma*u'*v + coef0)^degree\n\t\t 2 = radial basis function: exp(-gamma*|u-v|^2)\n\t\t 3 = sigmoid: tanh(gamma*u'*v + coef0)", "K", 1, "-K <int>"));
    




    result.addElement(new Option("\tSet degree in kernel function (default: 3)", "D", 1, "-D <int>"));
    

    result.addElement(new Option("\tSet gamma in kernel function (default: 1/k)", "G", 1, "-G <double>"));
    

    result.addElement(new Option("\tSet coef0 in kernel function (default: 0)", "R", 1, "-R <double>"));
    

    result.addElement(new Option("\tSet the parameter C of C-SVC, epsilon-SVR, and nu-SVR\n\t (default: 1)", "C", 1, "-C <double>"));
    


    result.addElement(new Option("\tSet the parameter nu of nu-SVC, one-class SVM, and nu-SVR\n\t (default: 0.5)", "N", 1, "-N <double>"));
    


    result.addElement(new Option("\tTurns on normalization of input data (default: off)", "Z", 0, "-Z"));
    

    result.addElement(new Option("\tTurn off nominal to binary conversion.\n\tWARNING: use only if your data is all numeric!", "J", 0, "-J"));
    

    result.addElement(new Option("\tTurn off missing value replacement.\n\tWARNING: use only if your data has no missing values.", "V", 0, "-V"));
    


    result.addElement(new Option("\tSet the epsilon in loss function of epsilon-SVR (default: 0.1)", "P", 1, "-P <double>"));
    


    result.addElement(new Option("\tSet cache memory size in MB (default: 40)", "M", 1, "-M <double>"));
    

    result.addElement(new Option("\tSet tolerance of termination criterion (default: 0.001)", "E", 1, "-E <double>"));
    


    result.addElement(new Option("\tTurns the shrinking heuristics off (default: on)", "H", 0, "-H"));
    

    result.addElement(new Option("\tSet the parameters C of class i to weight[i]*C, for C-SVC\n\tE.g., for a 3-class problem, you could use \"1 1 1\" for equally\n\tweighted classes.\n\t(default: 1 for all classes)", "W", 1, "-W <double>"));
    





    result.addElement(new Option("\tGenerate probability estimates for classification", "B", 0, "-B"));
    

    result.addElement(new Option("\tRandom seed\n\t(default = 1)", "seed", 1, "-seed <num>"));
    

    return result.elements();
  }
  




















































































































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSVMType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_SVMTYPE));
    } else {
      setSVMType(new SelectedTag(0, TAGS_SVMTYPE));
    }
    
    tmpStr = Utils.getOption('K', options);
    if (tmpStr.length() != 0) {
      setKernelType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_KERNELTYPE));
    } else {
      setKernelType(new SelectedTag(2, TAGS_KERNELTYPE));
    }
    
    tmpStr = Utils.getOption('D', options);
    if (tmpStr.length() != 0) {
      setDegree(Integer.parseInt(tmpStr));
    } else {
      setDegree(3);
    }
    
    tmpStr = Utils.getOption('G', options);
    if (tmpStr.length() != 0) {
      setGamma(Double.parseDouble(tmpStr));
    } else {
      setGamma(0.0D);
    }
    
    tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setCoef0(Double.parseDouble(tmpStr));
    } else {
      setCoef0(0.0D);
    }
    
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNu(Double.parseDouble(tmpStr));
    } else {
      setNu(0.5D);
    }
    
    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      setCacheSize(Double.parseDouble(tmpStr));
    } else {
      setCacheSize(40.0D);
    }
    
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setCost(Double.parseDouble(tmpStr));
    } else {
      setCost(1.0D);
    }
    
    tmpStr = Utils.getOption('E', options);
    if (tmpStr.length() != 0) {
      setEps(Double.parseDouble(tmpStr));
    } else {
      setEps(0.001D);
    }
    
    setNormalize(Utils.getFlag('Z', options));
    
    setDoNotReplaceMissingValues(Utils.getFlag("V", options));
    
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setLoss(Double.parseDouble(tmpStr));
    } else {
      setLoss(0.1D);
    }
    
    setShrinking(!Utils.getFlag('H', options));
    
    setWeights(Utils.getOption('W', options));
    
    setProbabilityEstimates(Utils.getFlag('B', options));
    
    String seedString = Utils.getOption("seed", options);
    if (seedString.length() > 0) {
      setSeed(Integer.parseInt(seedString.trim()));
    }
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-S");
    result.add("" + m_SVMType);
    
    result.add("-K");
    result.add("" + m_KernelType);
    
    result.add("-D");
    result.add("" + getDegree());
    
    result.add("-G");
    result.add("" + getGamma());
    
    result.add("-R");
    result.add("" + getCoef0());
    
    result.add("-N");
    result.add("" + getNu());
    
    result.add("-M");
    result.add("" + getCacheSize());
    
    result.add("-C");
    result.add("" + getCost());
    
    result.add("-E");
    result.add("" + getEps());
    
    result.add("-P");
    result.add("" + getLoss());
    
    if (!getShrinking()) {
      result.add("-H");
    }
    
    if (getNormalize()) {
      result.add("-Z");
    }
    
    if (getDoNotReplaceMissingValues()) {
      result.add("-V");
    }
    
    if (getWeights().length() != 0) {
      result.add("-W");
      result.add("" + getWeights());
    }
    
    if (getProbabilityEstimates()) {
      result.add("-B");
    }
    
    result.add("-seed");
    result.add("" + getSeed());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public static boolean isPresent()
  {
    return m_Present;
  }
  




  public void setSVMType(SelectedTag value)
  {
    if (value.getTags() == TAGS_SVMTYPE) {
      m_SVMType = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getSVMType()
  {
    return new SelectedTag(m_SVMType, TAGS_SVMTYPE);
  }
  





  public String SVMTypeTipText()
  {
    return "The type of SVM to use.";
  }
  




  public void setKernelType(SelectedTag value)
  {
    if (value.getTags() == TAGS_KERNELTYPE) {
      m_KernelType = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getKernelType()
  {
    return new SelectedTag(m_KernelType, TAGS_KERNELTYPE);
  }
  





  public String kernelTypeTipText()
  {
    return "The type of kernel to use";
  }
  




  public void setDegree(int value)
  {
    m_Degree = value;
  }
  




  public int getDegree()
  {
    return m_Degree;
  }
  





  public String degreeTipText()
  {
    return "The degree of the kernel.";
  }
  




  public void setGamma(double value)
  {
    m_Gamma = value;
  }
  




  public double getGamma()
  {
    return m_Gamma;
  }
  





  public String gammaTipText()
  {
    return "The gamma to use, if 0 then 1/max_index is used.";
  }
  




  public void setCoef0(double value)
  {
    m_Coef0 = value;
  }
  




  public double getCoef0()
  {
    return m_Coef0;
  }
  





  public String coef0TipText()
  {
    return "The coefficient to use.";
  }
  




  public void setNu(double value)
  {
    m_nu = value;
  }
  




  public double getNu()
  {
    return m_nu;
  }
  





  public String nuTipText()
  {
    return "The value of nu for nu-SVC, one-class SVM and nu-SVR.";
  }
  




  public void setCacheSize(double value)
  {
    m_CacheSize = value;
  }
  




  public double getCacheSize()
  {
    return m_CacheSize;
  }
  





  public String cacheSizeTipText()
  {
    return "The cache size in MB.";
  }
  




  public void setCost(double value)
  {
    m_Cost = value;
  }
  




  public double getCost()
  {
    return m_Cost;
  }
  





  public String costTipText()
  {
    return "The cost parameter C for C-SVC, epsilon-SVR and nu-SVR.";
  }
  




  public void setEps(double value)
  {
    m_eps = value;
  }
  




  public double getEps()
  {
    return m_eps;
  }
  





  public String epsTipText()
  {
    return "The tolerance of the termination criterion.";
  }
  




  public void setLoss(double value)
  {
    m_Loss = value;
  }
  




  public double getLoss()
  {
    return m_Loss;
  }
  





  public String lossTipText()
  {
    return "The epsilon for the loss function in epsilon-SVR.";
  }
  




  public void setShrinking(boolean value)
  {
    m_Shrinking = value;
  }
  




  public boolean getShrinking()
  {
    return m_Shrinking;
  }
  





  public String shrinkingTipText()
  {
    return "Whether to use the shrinking heuristic.";
  }
  




  public void setNormalize(boolean value)
  {
    m_Normalize = value;
  }
  




  public boolean getNormalize()
  {
    return m_Normalize;
  }
  





  public String normalizeTipText()
  {
    return "Whether to normalize the data.";
  }
  





  public String doNotReplaceMissingValuesTipText()
  {
    return "Whether to turn off automatic replacement of missing values. WARNING: set to true only if the data does not contain missing values.";
  }
  







  public void setDoNotReplaceMissingValues(boolean b)
  {
    m_noReplaceMissingValues = b;
  }
  




  public boolean getDoNotReplaceMissingValues()
  {
    return m_noReplaceMissingValues;
  }
  








  public void setWeights(String weightsStr)
  {
    StringTokenizer tok = new StringTokenizer(weightsStr, " ");
    m_Weight = new double[tok.countTokens()];
    m_WeightLabel = new int[tok.countTokens()];
    
    if (m_Weight.length == 0) {
      System.out.println("Zero Weights processed. Default weights will be used");
    }
    

    for (int i = 0; i < m_Weight.length; i++) {
      m_Weight[i] = Double.parseDouble(tok.nextToken());
      m_WeightLabel[i] = i;
    }
  }
  








  public String getWeights()
  {
    String result = "";
    for (int i = 0; i < m_Weight.length; i++) {
      if (i > 0) {
        result = result + " ";
      }
      result = result + Double.toString(m_Weight[i]);
    }
    
    return result;
  }
  





  public String weightsTipText()
  {
    return "The weights to use for the classes (blank-separated list, eg, \"1 1 1\" for a 3-class problem), if empty 1 is used by default.";
  }
  





  public void setProbabilityEstimates(boolean value)
  {
    m_ProbabilityEstimates = value;
  }
  





  public boolean getProbabilityEstimates()
  {
    return m_ProbabilityEstimates;
  }
  





  public String probabilityEstimatesTipText()
  {
    return "Whether to generate probability estimates instead of -1/+1 for classification problems.";
  }
  







  protected void setField(Object o, String name, Object value)
  {
    try
    {
      Field f = o.getClass().getField(name);
      f.set(o, value);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  








  protected void setField(Object o, String name, int index, Object value)
  {
    try
    {
      Field f = o.getClass().getField(name);
      Array.set(f.get(o), index, value);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  protected Object getField(Object o, String name)
  {
    Object result;
    


    try
    {
      Field f = o.getClass().getField(name);
      result = f.get(o);
    } catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  







  protected void newArray(Object o, String name, Class type, int length)
  {
    newArray(o, name, type, new int[] { length });
  }
  








  protected void newArray(Object o, String name, Class type, int[] dimensions)
  {
    try
    {
      Field f = o.getClass().getField(name);
      f.set(o, Array.newInstance(type, dimensions));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  












  protected Object invokeMethod(Object o, String name, Class[] paramClasses, Object[] paramValues)
  {
    Object result = null;
    try
    {
      Method m = o.getClass().getMethod(name, paramClasses);
      result = m.invoke(o, paramValues);
    } catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  



  protected Object getParameters()
  {
    Object result;
    

    try
    {
      result = Class.forName("libsvm.svm_parameter").newInstance();
      
      setField(result, "svm_type", new Integer(m_SVMType));
      setField(result, "kernel_type", new Integer(m_KernelType));
      setField(result, "degree", new Integer(m_Degree));
      setField(result, "gamma", new Double(m_GammaActual));
      setField(result, "coef0", new Double(m_Coef0));
      setField(result, "nu", new Double(m_nu));
      setField(result, "cache_size", new Double(m_CacheSize));
      setField(result, "C", new Double(m_Cost));
      setField(result, "eps", new Double(m_eps));
      setField(result, "p", new Double(m_Loss));
      setField(result, "shrinking", new Integer(m_Shrinking ? 1 : 0));
      setField(result, "nr_weight", new Integer(m_Weight.length));
      setField(result, "probability", new Integer(m_ProbabilityEstimates ? 1 : 0));
      

      newArray(result, "weight", Double.TYPE, m_Weight.length);
      newArray(result, "weight_label", Integer.TYPE, m_Weight.length);
      for (int i = 0; i < m_Weight.length; i++) {
        setField(result, "weight", i, new Double(m_Weight[i]));
        setField(result, "weight_label", i, new Integer(m_WeightLabel[i]));
      }
    } catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  



  protected Object getProblem(Vector vx, Vector vy)
  {
    Object result;
    


    try
    {
      result = Class.forName("libsvm.svm_problem").newInstance();
      
      setField(result, "l", new Integer(vy.size()));
      
      newArray(result, "x", Class.forName("libsvm.svm_node"), new int[] { vy.size(), 0 });
      
      for (int i = 0; i < vy.size(); i++) {
        setField(result, "x", i, vx.elementAt(i));
      }
      
      newArray(result, "y", Double.TYPE, vy.size());
      for (int i = 0; i < vy.size(); i++) {
        setField(result, "y", i, vy.elementAt(i));
      }
    } catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  















  protected Object instanceToArray(Instance instance)
    throws Exception
  {
    int count = 0;
    for (int i = 0; i < instance.numValues(); i++) {
      if (instance.index(i) != instance.classIndex())
      {

        if (instance.valueSparse(i) != 0.0D) {
          count++;
        }
      }
    }
    











    Object result = Array.newInstance(Class.forName("libsvm.svm_node"), count);
    int index = 0;
    for (i = 0; i < instance.numValues(); i++)
    {
      int idx = instance.index(i);
      if (idx != instance.classIndex())
      {

        if (instance.valueSparse(i) != 0.0D)
        {


          Array.set(result, index, Class.forName("libsvm.svm_node").newInstance());
          setField(Array.get(result, index), "index", new Integer(idx + 1));
          setField(Array.get(result, index), "value", new Double(instance.valueSparse(i)));
          
          index++;
        } }
    }
    return result;
  }
  








  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    int[] labels = new int[instance.numClasses()];
    double[] prob_estimates = null;
    
    if (m_ProbabilityEstimates) {
      invokeMethod(Class.forName("libsvm.svm").newInstance(), "svm_get_labels", new Class[] { Class.forName("libsvm.svm_model"), Array.newInstance(Integer.TYPE, instance.numClasses()).getClass() }, new Object[] { m_Model, labels });
      





      prob_estimates = new double[instance.numClasses()];
    }
    
    if (!getDoNotReplaceMissingValues()) {
      m_ReplaceMissingValues.input(instance);
      m_ReplaceMissingValues.batchFinished();
      instance = m_ReplaceMissingValues.output();
    }
    
    if (m_Filter != null) {
      m_Filter.input(instance);
      m_Filter.batchFinished();
      instance = m_Filter.output();
    }
    
    m_NominalToBinary.input(instance);
    m_NominalToBinary.batchFinished();
    instance = m_NominalToBinary.output();
    
    Object x = instanceToArray(instance);
    
    double[] result = new double[instance.numClasses()];
    if ((m_ProbabilityEstimates) && ((m_SVMType == 0) || (m_SVMType == 1)))
    {
      double v = ((Double)invokeMethod(Class.forName("libsvm.svm").newInstance(), "svm_predict_probability", new Class[] { Class.forName("libsvm.svm_model"), Array.newInstance(Class.forName("libsvm.svm_node"), Array.getLength(x)).getClass(), Array.newInstance(Double.TYPE, prob_estimates.length).getClass() }, new Object[] { m_Model, x, prob_estimates })).doubleValue();
      










      for (int k = 0; k < prob_estimates.length; k++) {
        result[labels[k]] = prob_estimates[k];
      }
    } else {
      double v = ((Double)invokeMethod(Class.forName("libsvm.svm").newInstance(), "svm_predict", new Class[] { Class.forName("libsvm.svm_model"), Array.newInstance(Class.forName("libsvm.svm_node"), Array.getLength(x)).getClass() }, new Object[] { m_Model, x })).doubleValue();
      





      if (instance.classAttribute().isNominal()) {
        if (m_SVMType == 2) {
          if (v > 0.0D) {
            result[0] = 1.0D;

          }
          else
          {
            result[0] = 0.0D;
          }
        } else {
          result[((int)v)] = 1.0D;
        }
      } else {
        result[0] = v;
      }
    }
    
    return result;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableDependency(Capabilities.Capability.UNARY_CLASS);
    result.enableDependency(Capabilities.Capability.NOMINAL_CLASS);
    result.enableDependency(Capabilities.Capability.NUMERIC_CLASS);
    result.enableDependency(Capabilities.Capability.DATE_CLASS);
    
    switch (m_SVMType) {
    case 0: 
    case 1: 
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
      break;
    
    case 2: 
      result.enable(Capabilities.Capability.UNARY_CLASS);
      break;
    
    case 3: 
    case 4: 
      result.enable(Capabilities.Capability.NUMERIC_CLASS);
      result.enable(Capabilities.Capability.DATE_CLASS);
      break;
    
    default: 
      throw new IllegalArgumentException("SVMType " + m_SVMType + " is not supported!");
    }
    
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances insts)
    throws Exception
  {
    m_Filter = null;
    
    if (!isPresent()) {
      throw new Exception("libsvm classes not in CLASSPATH!");
    }
    

    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    
    if (!getDoNotReplaceMissingValues()) {
      m_ReplaceMissingValues = new ReplaceMissingValues();
      m_ReplaceMissingValues.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_ReplaceMissingValues);
    }
    




    getCapabilities().testWithFail(insts);
    
    if (getNormalize()) {
      m_Filter = new Normalize();
      m_Filter.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_Filter);
    }
    

    m_NominalToBinary = new NominalToBinary();
    m_NominalToBinary.setInputFormat(insts);
    insts = Filter.useFilter(insts, m_NominalToBinary);
    
    Vector vy = new Vector();
    Vector vx = new Vector();
    int max_index = 0;
    
    for (int d = 0; d < insts.numInstances(); d++) {
      Instance inst = insts.instance(d);
      Object x = instanceToArray(inst);
      int m = Array.getLength(x);
      
      if (m > 0) {
        max_index = Math.max(max_index, ((Integer)getField(Array.get(x, m - 1), "index")).intValue());
      }
      

      vx.addElement(x);
      vy.addElement(new Double(inst.classValue()));
    }
    

    if (getGamma() == 0.0D) {
      m_GammaActual = (1.0D / max_index);
    } else {
      m_GammaActual = m_Gamma;
    }
    

    String error_msg = (String)invokeMethod(Class.forName("libsvm.svm").newInstance(), "svm_check_parameter", new Class[] { Class.forName("libsvm.svm_problem"), Class.forName("libsvm.svm_parameter") }, new Object[] { getProblem(vx, vy), getParameters() });
    




    if (error_msg != null) {
      throw new Exception("Error: " + error_msg);
    }
    

    Class svmClass = Class.forName("libsvm.svm");
    Field randF = svmClass.getField("rand");
    Random rand = (Random)randF.get(null);
    rand.setSeed(m_Seed);
    

    m_Model = invokeMethod(Class.forName("libsvm.svm").newInstance(), "svm_train", new Class[] { Class.forName("libsvm.svm_problem"), Class.forName("libsvm.svm_parameter") }, new Object[] { getProblem(vx, vy), getParameters() });
  }
  











  public String toString()
  {
    return "LibSVM wrapper, original code by Yasser EL-Manzalawy (= WLSVM)";
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10660 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new LibSVM(), args);
  }
  
  public LibSVM() {}
}

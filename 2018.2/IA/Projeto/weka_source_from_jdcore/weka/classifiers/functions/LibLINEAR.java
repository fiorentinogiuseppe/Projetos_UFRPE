package weka.classifiers.functions;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.classifiers.Classifier;
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
import weka.core.WekaException;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;







































































































public class LibLINEAR
  extends Classifier
  implements TechnicalInformationHandler
{
  protected static final String CLASS_LINEAR = "liblinear.Linear";
  protected static final String CLASS_MODEL = "liblinear.Model";
  protected static final String CLASS_PROBLEM = "liblinear.Problem";
  protected static final String CLASS_PARAMETER = "liblinear.Parameter";
  protected static final String CLASS_SOLVERTYPE = "liblinear.SolverType";
  protected static final String CLASS_FEATURENODE = "liblinear.FeatureNode";
  protected static final long serialVersionUID = 230504711L;
  protected Object m_Model;
  
  public Object getModel()
  {
    return m_Model;
  }
  

  protected Filter m_Filter = null;
  

  protected boolean m_Normalize = false;
  


  public static final int SVMTYPE_L2_LR = 0;
  

  public static final int SVMTYPE_L2LOSS_SVM_DUAL = 1;
  

  public static final int SVMTYPE_L2LOSS_SVM = 2;
  

  public static final int SVMTYPE_L1LOSS_SVM_DUAL = 3;
  

  public static final int SVMTYPE_MCSVM_CS = 4;
  

  public static final Tag[] TAGS_SVMTYPE;
  

  protected int m_SVMType = 1;
  

  protected double m_eps = 0.01D;
  

  protected double m_Cost = 1.0D;
  

  protected double m_Bias = 1.0D;
  
  protected int[] m_WeightLabel = new int[0];
  
  protected double[] m_Weight = new double[0];
  


  protected boolean m_ProbabilityEstimates = false;
  

  protected ReplaceMissingValues m_ReplaceMissingValues;
  

  protected NominalToBinary m_NominalToBinary;
  

  private boolean m_nominalToBinary = false;
  private boolean m_noReplaceMissingValues;
  protected static boolean m_Present;
  
  static
  {
    TAGS_SVMTYPE = new Tag[] { new Tag(0, "L2-regularized logistic regression"), new Tag(1, "L2-loss support vector machines (dual)"), new Tag(2, "L2-loss support vector machines (primal)"), new Tag(3, "L1-loss support vector machines (dual)"), new Tag(4, "multi-class support vector machines by Crammer and Singer") };
    







































    m_Present = false;
    try
    {
      Class.forName("liblinear.Linear");
      m_Present = true;
    }
    catch (Exception e) {
      m_Present = false;
    }
  }
  





  public String globalInfo()
  {
    return "A wrapper class for the liblinear tools (the liblinear classes, typically the jar file, need to be in the classpath to use this classifier).\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Rong-En Fan and Kai-Wei Chang and Cho-Jui Hsieh and Xiang-Rui Wang and Chih-Jen Lin");
    result.setValue(TechnicalInformation.Field.TITLE, "LIBLINEAR - A Library for Large Linear Classification");
    result.setValue(TechnicalInformation.Field.YEAR, "2008");
    result.setValue(TechnicalInformation.Field.URL, "http://www.csie.ntu.edu.tw/~cjlin/liblinear/");
    result.setValue(TechnicalInformation.Field.NOTE, "The Weka classifier works with version 1.33 of LIBLINEAR");
    
    return result;
  }
  






  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSet type of solver (default: 1)\n\t\t 0 = L2-regularized logistic regression\n\t\t 1 = L2-loss support vector machines (dual)\n\t\t 2 = L2-loss support vector machines (primal)\n\t\t 3 = L1-loss support vector machines (dual)\n\t\t 4 = multi-class support vector machines by Crammer and Singer", "S", 1, "-S <int>"));
    








    result.addElement(new Option("\tSet the cost parameter C\n\t (default: 1)", "C", 1, "-C <double>"));
    




    result.addElement(new Option("\tTurn on normalization of input data (default: off)", "Z", 0, "-Z"));
    



    result.addElement(new Option("\tTurn on nominal to binary conversion.", "N", 0, "-N"));
    


    result.addElement(new Option("\tTurn off missing value replacement.\n\tWARNING: use only if your data has no missing values.", "M", 0, "-M"));
    



    result.addElement(new Option("\tUse probability estimation (default: off)\ncurrently for L2-regularized logistic regression only! ", "P", 0, "-P"));
    




    result.addElement(new Option("\tSet tolerance of termination criterion (default: 0.01)", "E", 1, "-E <double>"));
    



    result.addElement(new Option("\tSet the parameters C of class i to weight[i]*C\n\t (default: 1)", "W", 1, "-W <double>"));
    




    result.addElement(new Option("\tAdd Bias term with the given value if >= 0; if < 0, no bias term added (default: 1)", "B", 1, "-B <double>"));
    



    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    return result.elements();
  }
  



















































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSVMType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_SVMTYPE));
    }
    else {
      setSVMType(new SelectedTag(1, TAGS_SVMTYPE));
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
    
    setConvertNominalToBinary(Utils.getFlag('N', options));
    setDoNotReplaceMissingValues(Utils.getFlag('M', options));
    
    tmpStr = Utils.getOption('B', options);
    if (tmpStr.length() != 0) {
      setBias(Double.parseDouble(tmpStr));
    } else {
      setBias(1.0D);
    }
    setWeights(Utils.getOption('W', options));
    
    setProbabilityEstimates(Utils.getFlag('P', options));
    
    super.setOptions(options);
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-S");
    result.add("" + m_SVMType);
    
    result.add("-C");
    result.add("" + getCost());
    
    result.add("-E");
    result.add("" + getEps());
    
    result.add("-B");
    result.add("" + getBias());
    
    if (getNormalize()) {
      result.add("-Z");
    }
    if (getConvertNominalToBinary()) {
      result.add("-N");
    }
    if (getDoNotReplaceMissingValues()) {
      result.add("-M");
    }
    if (getWeights().length() != 0) {
      result.add("-W");
      result.add("" + getWeights());
    }
    
    if (getProbabilityEstimates()) {
      result.add("-P");
    }
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
    return "The cost parameter C.";
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
  





  public void setBias(double value)
  {
    m_Bias = value;
  }
  





  public double getBias()
  {
    return m_Bias;
  }
  





  public String biasTipText()
  {
    return "If >= 0, a bias term with that value is added; otherwise (<0) no bias term is added (default: 1).";
  }
  






  public String normalizeTipText()
  {
    return "Whether to normalize the data.";
  }
  




  public void setNormalize(boolean value)
  {
    m_Normalize = value;
  }
  




  public boolean getNormalize()
  {
    return m_Normalize;
  }
  





  public String convertNominalToBinaryTipText()
  {
    return "Whether to turn on conversion of nominal attributes to binary.";
  }
  







  public void setConvertNominalToBinary(boolean b)
  {
    m_nominalToBinary = b;
  }
  






  public boolean getConvertNominalToBinary()
  {
    return m_nominalToBinary;
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
      if (i > 0)
        result = result + " ";
      result = result + Double.toString(m_Weight[i]);
    }
    
    return result;
  }
  





  public String weightsTipText()
  {
    return "The weights to use for the classes, if empty 1 is used by default.";
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
    return "Whether to generate probability estimates instead of -1/+1 for classification problems (currently for L2-regularized logistic regression only!)";
  }
  








  protected void setField(Object o, String name, Object value)
  {
    try
    {
      Field f = o.getClass().getField(name);
      f.set(o, value);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  








  protected void setField(Object o, String name, int index, Object value)
  {
    try
    {
      Field f = o.getClass().getField(name);
      Array.set(f.get(o), index, value);
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
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
      Class solverTypeEnumClass = Class.forName("liblinear.SolverType");
      Object[] enumValues = solverTypeEnumClass.getEnumConstants();
      Object solverType = enumValues[m_SVMType];
      
      Class[] constructorClasses = { solverTypeEnumClass, Double.TYPE, Double.TYPE };
      Constructor parameterConstructor = Class.forName("liblinear.Parameter").getConstructor(constructorClasses);
      
      result = parameterConstructor.newInstance(new Object[] { solverType, Double.valueOf(m_Cost), Double.valueOf(m_eps) });
      

      if (m_Weight.length > 0) {
        invokeMethod(result, "setWeights", new Class[] { [D.class, [I.class }, new Object[] { m_Weight, m_WeightLabel });
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  




  protected Object getProblem(List<Object> vx, List<Integer> vy, int max_index)
  {
    Object result;
    


    try
    {
      result = Class.forName("liblinear.Problem").newInstance();
      
      setField(result, "l", Integer.valueOf(vy.size()));
      setField(result, "n", Integer.valueOf(max_index));
      setField(result, "bias", Double.valueOf(getBias()));
      
      newArray(result, "x", Class.forName("liblinear.FeatureNode"), new int[] { vy.size(), 0 });
      for (int i = 0; i < vy.size(); i++) {
        setField(result, "x", i, vx.get(i));
      }
      newArray(result, "y", Integer.TYPE, vy.size());
      for (int i = 0; i < vy.size(); i++) {
        setField(result, "y", i, vy.get(i));
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
        if (instance.valueSparse(i) != 0.0D)
          count++;
      }
    }
    if (m_Bias >= 0.0D) {
      count++;
    }
    
    Class[] intDouble = { Integer.TYPE, Double.TYPE };
    Constructor nodeConstructor = Class.forName("liblinear.FeatureNode").getConstructor(intDouble);
    

    Object result = Array.newInstance(Class.forName("liblinear.FeatureNode"), count);
    int index = 0;
    for (i = 0; i < instance.numValues(); i++)
    {
      int idx = instance.index(i);
      double val = instance.valueSparse(i);
      
      if (idx != instance.classIndex())
      {
        if (val != 0.0D)
        {

          Object node = nodeConstructor.newInstance(new Object[] { Integer.valueOf(idx + 1), Double.valueOf(val) });
          Array.set(result, index, node);
          index++;
        }
      }
    }
    if (m_Bias >= 0.0D) {
      Integer idx = Integer.valueOf(instance.numAttributes() + 1);
      Double value = Double.valueOf(m_Bias);
      Object node = nodeConstructor.newInstance(new Object[] { idx, value });
      Array.set(result, index, node);
    }
    
    return result;
  }
  





  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (!getDoNotReplaceMissingValues()) {
      m_ReplaceMissingValues.input(instance);
      m_ReplaceMissingValues.batchFinished();
      instance = m_ReplaceMissingValues.output();
    }
    
    if ((getConvertNominalToBinary()) && (m_NominalToBinary != null))
    {
      m_NominalToBinary.input(instance);
      m_NominalToBinary.batchFinished();
      instance = m_NominalToBinary.output();
    }
    
    if (m_Filter != null) {
      m_Filter.input(instance);
      m_Filter.batchFinished();
      instance = m_Filter.output();
    }
    
    Object x = instanceToArray(instance);
    
    double[] result = new double[instance.numClasses()];
    if (m_ProbabilityEstimates) {
      if (m_SVMType != 0) {
        throw new WekaException("probability estimation is currently only supported for L2-regularized logistic regression");
      }
      

      int[] labels = (int[])invokeMethod(m_Model, "getLabels", null, null);
      double[] prob_estimates = new double[instance.numClasses()];
      
      double v = ((Integer)invokeMethod(Class.forName("liblinear.Linear").newInstance(), "predictProbability", new Class[] { Class.forName("liblinear.Model"), Array.newInstance(Class.forName("liblinear.FeatureNode"), Array.getLength(x)).getClass(), Array.newInstance(Double.TYPE, prob_estimates.length).getClass() }, new Object[] { m_Model, x, prob_estimates })).doubleValue();
      








      for (int k = 0; k < prob_estimates.length; k++) {
        result[labels[k]] = prob_estimates[k];
      }
    }
    else {
      double v = ((Integer)invokeMethod(Class.forName("liblinear.Linear").newInstance(), "predict", new Class[] { Class.forName("liblinear.Model"), Array.newInstance(Class.forName("liblinear.FeatureNode"), Array.getLength(x)).getClass() }, new Object[] { m_Model, x })).doubleValue();
      








      assert (instance.classAttribute().isNominal());
      result[((int)v)] = 1.0D;
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
    


    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    return result;
  }
  





  public void buildClassifier(Instances insts)
    throws Exception
  {
    m_NominalToBinary = null;
    m_Filter = null;
    
    if (!isPresent()) {
      throw new Exception("liblinear classes not in CLASSPATH!");
    }
    
    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    
    if (!getDoNotReplaceMissingValues()) {
      m_ReplaceMissingValues = new ReplaceMissingValues();
      m_ReplaceMissingValues.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_ReplaceMissingValues);
    }
    




    getCapabilities().testWithFail(insts);
    
    if (getConvertNominalToBinary()) {
      insts = nominalToBinary(insts);
    }
    
    if (getNormalize()) {
      m_Filter = new Normalize();
      m_Filter.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_Filter);
    }
    
    List<Integer> vy = new ArrayList(insts.numInstances());
    List<Object> vx = new ArrayList(insts.numInstances());
    int max_index = 0;
    
    for (int d = 0; d < insts.numInstances(); d++) {
      Instance inst = insts.instance(d);
      Object x = instanceToArray(inst);
      int m = Array.getLength(x);
      if (m > 0)
        max_index = Math.max(max_index, ((Integer)getField(Array.get(x, m - 1), "index")).intValue());
      vx.add(x);
      double classValue = inst.classValue();
      int classValueInt = (int)classValue;
      if (classValueInt != classValue) throw new RuntimeException("unsupported class value: " + classValue);
      vy.add(Integer.valueOf(classValueInt));
    }
    
    if (!m_Debug) {
      invokeMethod(Class.forName("liblinear.Linear").newInstance(), "disableDebugOutput", null, null);
    }
    else
    {
      invokeMethod(Class.forName("liblinear.Linear").newInstance(), "enableDebugOutput", null, null);
    }
    



    invokeMethod(Class.forName("liblinear.Linear").newInstance(), "resetRandom", null, null);
    



    m_Model = invokeMethod(Class.forName("liblinear.Linear").newInstance(), "train", new Class[] { Class.forName("liblinear.Problem"), Class.forName("liblinear.Parameter") }, new Object[] { getProblem(vx, vy, max_index), getParameters() });
  }
  










  private Instances nominalToBinary(Instances insts)
    throws Exception
  {
    boolean onlyNumeric = true;
    for (int i = 0; i < insts.numAttributes(); i++) {
      if ((i != insts.classIndex()) && 
        (!insts.attribute(i).isNumeric())) {
        onlyNumeric = false;
        break;
      }
    }
    

    if (!onlyNumeric) {
      m_NominalToBinary = new NominalToBinary();
      m_NominalToBinary.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_NominalToBinary);
    }
    return insts;
  }
  




  public String toString()
  {
    return "LibLINEAR wrapper";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5917 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new LibLINEAR(), args);
  }
  
  public LibLINEAR() {}
}

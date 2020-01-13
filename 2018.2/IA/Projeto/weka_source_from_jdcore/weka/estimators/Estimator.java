package weka.estimators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;









































































public abstract class Estimator
  implements Cloneable, Serializable, OptionHandler, CapabilitiesHandler, RevisionHandler
{
  static final long serialVersionUID = -5902411487362274342L;
  private boolean m_Debug;
  protected double m_classValueIndex;
  protected boolean m_noClass;
  
  public Estimator()
  {
    m_Debug = false;
    

    m_classValueIndex = -1.0D;
    

    m_noClass = true;
  }
  



  private static class Builder
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -5810927990193597303L;
    

    Instances m_instances = null;
    

    int m_attrIndex = -1;
    

    int m_classIndex = -1;
    

    int m_classValueIndex = -1;
    

    private Builder() {}
    

    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5539 $");
    }
  }
  




  public void addValue(double data, double weight)
  {
    try
    {
      throw new Exception("Method to add single value is not implemented!\nEstimator should implement IncrementalEstimator.");
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
  }
  







  public void addValues(Instances data, int attrIndex)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    double[] minMax = new double[2];
    try
    {
      EstimatorUtils.getMinMax(data, attrIndex, minMax);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
    
    double min = minMax[0];
    double max = minMax[1];
    

    addValues(data, attrIndex, min, max, 1.0D);
  }
  














  public void addValues(Instances data, int attrIndex, double min, double max, double factor)
    throws Exception
  {
    int numInst = data.numInstances();
    for (int i = 1; i < numInst; i++) {
      addValue(data.instance(i).value(attrIndex), 1.0D);
    }
  }
  










  public void addValues(Instances data, int attrIndex, int classIndex, int classValue)
    throws Exception
  {
    m_noClass = false;
    getCapabilities().testWithFail(data);
    

    double[] minMax = new double[2];
    try
    {
      EstimatorUtils.getMinMax(data, attrIndex, minMax);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
    
    double min = minMax[0];
    double max = minMax[1];
    

    Instances workData = new Instances(data, 0);
    double factor = getInstancesFromClass(data, attrIndex, classIndex, classValue, workData);
    



    if (workData.numInstances() == 0) { return;
    }
    addValues(data, attrIndex, min, max, factor);
  }
  














  public void addValues(Instances data, int attrIndex, int classIndex, int classValue, double min, double max)
    throws Exception
  {
    Instances workData = new Instances(data, 0);
    double factor = getInstancesFromClass(data, attrIndex, classIndex, classValue, workData);
    



    if (workData.numInstances() == 0) { return;
    }
    addValues(data, attrIndex, min, max, factor);
  }
  













  private double getInstancesFromClass(Instances data, int attrIndex, int classIndex, double classValue, Instances workData)
  {
    int num = 0;
    int numClassValue = 0;
    for (int i = 0; i < data.numInstances(); i++) {
      if (!data.instance(i).isMissing(attrIndex)) {
        num++;
        if (data.instance(i).value(classIndex) == classValue) {
          workData.add(data.instance(i));
          numClassValue++;
        }
      }
    }
    
    Double alphaFactor = new Double(numClassValue / num);
    return alphaFactor.doubleValue();
  }
  









  public abstract double getProbability(double paramDouble);
  








  public static void buildEstimator(Estimator est, String[] options, boolean isIncremental)
    throws Exception
  {
    boolean debug = false;
    


    Builder build = new Builder(null);
    try {
      setGeneralOptions(build, est, options);
      
      if ((est instanceof OptionHandler)) {
        est.setOptions(options);
      }
      
      Utils.checkForRemainingOptions(options);
      

      buildEstimator(est, m_instances, m_attrIndex, m_classIndex, m_classValueIndex, isIncremental);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
      String specificOptions = "";
      
      if ((est instanceof OptionHandler)) {
        specificOptions = specificOptions + "\nEstimator options:\n\n";
        Enumeration enumOptions = est.listOptions();
        while (enumOptions.hasMoreElements()) {
          Option option = (Option)enumOptions.nextElement();
          specificOptions = specificOptions + option.synopsis() + '\n' + option.description() + "\n";
        }
      }
      

      String genericOptions = "\nGeneral options:\n\n-h\n\tGet help on available options.\n-i <file>\n\tThe name of the file containing input instances.\n\tIf not supplied then instances will be read from stdin.\n-a <attribute index>\n\tThe number of the attribute the probability distribution\n\testimation is done for.\n\t\"first\" and \"last\" are also valid entries.\n\tIf not supplied then no class is assigned.\n-c <class index>\n\tIf class value index is set, this attribute is taken as class.\n\t\"first\" and \"last\" are also valid entries.\n\tIf not supplied then last is default.\n-v <class value index>\n\tIf value is different to -1, select instances of this class value.\n\t\"first\" and \"last\" are also valid entries.\n\tIf not supplied then all instances are taken.\n";
      


















      throw new Exception('\n' + ex.getMessage() + specificOptions + genericOptions);
    }
  }
  






  public static void buildEstimator(Estimator est, Instances instances, int attrIndex, int classIndex, int classValueIndex, boolean isIncremental)
    throws Exception
  {
    if (!isIncremental)
    {
      if (classValueIndex == -1)
      {
        est.addValues(instances, attrIndex);
      }
      else {
        est.addValues(instances, attrIndex, classIndex, classValueIndex);
      }
    }
    else
    {
      Enumeration enumInsts = instances.enumerateInstances();
      while (enumInsts.hasMoreElements()) {
        Instance instance = (Instance)enumInsts.nextElement();
        
        ((IncrementalEstimator)est).addValue(instance.value(attrIndex), instance.weight());
      }
    }
  }
  







  private static void setGeneralOptions(Builder build, Estimator est, String[] options)
    throws Exception
  {
    Reader input = null;
    

    boolean helpRequest = Utils.getFlag('h', options);
    if (helpRequest) {
      throw new Exception("Help requested.\n");
    }
    

    String infileName = Utils.getOption('i', options);
    if (infileName.length() != 0) {
      input = new BufferedReader(new FileReader(infileName));
    } else {
      input = new BufferedReader(new InputStreamReader(System.in));
    }
    
    m_instances = new Instances(input);
    

    String attrIndex = Utils.getOption('a', options);
    
    if (attrIndex.length() != 0) {
      if (attrIndex.equals("first")) {
        m_attrIndex = 0;
      } else if (attrIndex.equals("last")) {
        m_attrIndex = (m_instances.numAttributes() - 1);
      } else {
        int index = Integer.parseInt(attrIndex) - 1;
        if ((index < 0) || (index >= m_instances.numAttributes())) {
          throw new IllegalArgumentException("Option a: attribute index out of range.");
        }
        m_attrIndex = index;
      }
      
    }
    else {
      m_attrIndex = 0;
    }
    

    String classIndex = Utils.getOption('c', options);
    if (classIndex.length() == 0) { classIndex = "last";
    }
    if (classIndex.length() != 0) {
      if (classIndex.equals("first")) {
        m_classIndex = 0;
      } else if (classIndex.equals("last")) {
        m_classIndex = (m_instances.numAttributes() - 1);
      } else {
        int cl = Integer.parseInt(classIndex);
        if (cl == -1) {
          m_classIndex = (m_instances.numAttributes() - 1);
        } else {
          m_classIndex = (cl - 1);
        }
      }
    }
    

    String classValueIndex = Utils.getOption('v', options);
    if (classValueIndex.length() != 0) {
      if (classValueIndex.equals("first")) {
        m_classValueIndex = 0;
      } else if (classValueIndex.equals("last")) {
        m_classValueIndex = (m_instances.numAttributes() - 1);
      } else {
        int cl = Integer.parseInt(classValueIndex);
        if (cl == -1) {
          m_classValueIndex = -1;
        } else {
          m_classValueIndex = (cl - 1);
        }
      }
    }
    
    m_instances.setClassIndex(m_classIndex);
  }
  






  public static Estimator clone(Estimator model)
    throws Exception
  {
    return makeCopy(model);
  }
  






  public static Estimator makeCopy(Estimator model)
    throws Exception
  {
    return (Estimator)new SerializedObject(model).getObject();
  }
  








  public static Estimator[] makeCopies(Estimator model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model estimator set");
    }
    Estimator[] estimators = new Estimator[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < estimators.length; i++) {
      estimators[i] = ((Estimator)so.getObject());
    }
    return estimators;
  }
  







  public boolean equals(Object obj)
  {
    if ((obj == null) || (!obj.getClass().equals(getClass()))) {
      return false;
    }
    Estimator cmp = (Estimator)obj;
    if (m_Debug != m_Debug) return false;
    if (m_classValueIndex != m_classValueIndex) return false;
    if (m_noClass != m_noClass) { return false;
    }
    return true;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tIf set, estimator is run in debug mode and\n\tmay output additional info to the console", "D", 0, "-D"));
    


    return newVector.elements();
  }
  









  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
  }
  



  public String[] getOptions()
  {
    String[] options;
    

    if (getDebug()) {
      String[] options = new String[1];
      options[0] = "-D";
    } else {
      options = new String[0];
    }
    return options;
  }
  













  public static Estimator forName(String name, String[] options)
    throws Exception
  {
    return (Estimator)Utils.forName(Estimator.class, name, options);
  }
  







  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  





  public boolean getDebug()
  {
    return m_Debug;
  }
  




  public String debugTipText()
  {
    return "If set to true, estimator may output additional info to the console.";
  }
  







  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    








    return result;
  }
  




  public void testCapabilities(Instances data, int attrIndex)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    getCapabilities().testWithFail(data.attribute(attrIndex));
  }
}

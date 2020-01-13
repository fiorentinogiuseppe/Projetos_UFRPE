package weka.classifiers.functions;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.IntervalEstimator;
import weka.classifiers.functions.supportVector.CachedKernel;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Statistics;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.matrix.LUDecomposition;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;































































































public class GaussianProcesses
  extends Classifier
  implements OptionHandler, IntervalEstimator, TechnicalInformationHandler
{
  static final long serialVersionUID = -8620066949967678545L;
  protected NominalToBinary m_NominalToBinary;
  public static final int FILTER_NORMALIZE = 0;
  public static final int FILTER_STANDARDIZE = 1;
  public static final int FILTER_NONE = 2;
  public static final Tag[] TAGS_FILTER = { new Tag(0, "Normalize training data"), new Tag(1, "Standardize training data"), new Tag(2, "No normalization/standardization") };
  





  protected Filter m_Filter = null;
  

  protected int m_filterType = 0;
  


  protected ReplaceMissingValues m_Missing;
  


  protected boolean m_checksTurnedOff = false;
  

  protected double m_delta = 1.0D;
  

  protected int m_classIndex = -1;
  

  protected double m_Alin;
  

  protected double m_Blin;
  
  protected Kernel m_kernel = null;
  

  protected int m_NumTrain = 0;
  

  protected double m_avg_target;
  

  protected Matrix m_C;
  

  protected Matrix m_t;
  

  protected boolean m_KernelIsLinear = false;
  




  public GaussianProcesses()
  {
    m_kernel = new RBFKernel();
    ((RBFKernel)m_kernel).setGamma(1.0D);
  }
  





  public String globalInfo()
  {
    return "Implements Gaussian Processes for regression without hyperparameter-tuning. For more information see\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "David J.C. Mackay");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "Introduction to Gaussian Processes");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Dept. of Physics, Cambridge University, UK");
    result.setValue(TechnicalInformation.Field.PS, "http://wol.ra.phy.cam.ac.uk/mackay/gpB.ps.gz");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = getKernel().getCapabilities();
    result.setOwner(this);
    

    result.enableAllAttributeDependencies();
    

    if (result.handles(Capabilities.Capability.NUMERIC_ATTRIBUTES))
      result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances insts)
    throws Exception
  {
    if (!m_checksTurnedOff)
    {
      getCapabilities().testWithFail(insts);
      

      insts = new Instances(insts);
      insts.deleteWithMissingClass();
    }
    
    if (!m_checksTurnedOff) {
      m_Missing = new ReplaceMissingValues();
      m_Missing.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_Missing);
    } else {
      m_Missing = null;
    }
    
    if (getCapabilities().handles(Capabilities.Capability.NUMERIC_ATTRIBUTES)) {
      boolean onlyNumeric = true;
      if (!m_checksTurnedOff) {
        for (int i = 0; i < insts.numAttributes(); i++) {
          if ((i != insts.classIndex()) && 
            (!insts.attribute(i).isNumeric())) {
            onlyNumeric = false;
            break;
          }
        }
      }
      

      if (!onlyNumeric) {
        m_NominalToBinary = new NominalToBinary();
        m_NominalToBinary.setInputFormat(insts);
        insts = Filter.useFilter(insts, m_NominalToBinary);
      } else {
        m_NominalToBinary = null;
      }
    }
    else {
      m_NominalToBinary = null;
    }
    
    m_classIndex = insts.classIndex();
    if (m_filterType == 1) {
      m_Filter = new Standardize();
      
      m_Filter.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_Filter);
    } else if (m_filterType == 0) {
      m_Filter = new Normalize();
      
      m_Filter.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_Filter);
    } else {
      m_Filter = null;
    }
    
    m_NumTrain = insts.numInstances();
    


    if (m_Filter != null) {
      Instance witness = (Instance)insts.instance(0).copy();
      witness.setValue(m_classIndex, 0.0D);
      m_Filter.input(witness);
      m_Filter.batchFinished();
      Instance res = m_Filter.output();
      m_Blin = res.value(m_classIndex);
      witness.setValue(m_classIndex, 1.0D);
      m_Filter.input(witness);
      m_Filter.batchFinished();
      res = m_Filter.output();
      m_Alin = (res.value(m_classIndex) - m_Blin);
    } else {
      m_Alin = 1.0D;
      m_Blin = 0.0D;
    }
    

    m_kernel.buildKernel(insts);
    m_KernelIsLinear = (((m_kernel instanceof PolyKernel)) && (((PolyKernel)m_kernel).getExponent() == 1.0D));
    

    if ((m_kernel instanceof CachedKernel)) {
      m_kernel = Kernel.makeCopy(m_kernel);
      ((CachedKernel)m_kernel).setCacheSize(-1);
      m_kernel.buildKernel(insts);
    }
    


    m_C = new Matrix(insts.numInstances(), insts.numInstances());
    
    double sum = 0.0D;
    
    for (int i = 0; i < insts.numInstances(); i++) {
      sum += insts.instance(i).classValue();
      for (int j = 0; j < i; j++) {
        double kv = m_kernel.eval(i, j, insts.instance(i));
        m_C.set(i, j, kv);
        m_C.set(j, i, kv);
      }
      double kv = m_kernel.eval(i, i, insts.instance(i));
      m_C.set(i, i, kv + m_delta * m_delta);
    }
    
    m_avg_target = (sum / insts.numInstances());
    





    LUDecomposition lu = new LUDecomposition(m_C);
    if (!lu.isNonsingular()) {
      throw new Exception("Singular Matrix?!?");
    }
    Matrix iMat = Matrix.identity(insts.numInstances(), insts.numInstances());
    
    m_C = lu.solve(iMat);
    
    m_t = new Matrix(insts.numInstances(), 1);
    
    for (int i = 0; i < insts.numInstances(); i++) {
      m_t.set(i, 0, insts.instance(i).classValue() - m_avg_target);
    }
    m_t = m_C.times(m_t);
  }
  









  public double classifyInstance(Instance inst)
    throws Exception
  {
    if (!m_checksTurnedOff) {
      m_Missing.input(inst);
      m_Missing.batchFinished();
      inst = m_Missing.output();
    }
    
    if (m_NominalToBinary != null) {
      m_NominalToBinary.input(inst);
      m_NominalToBinary.batchFinished();
      inst = m_NominalToBinary.output();
    }
    
    if (m_Filter != null) {
      m_Filter.input(inst);
      m_Filter.batchFinished();
      inst = m_Filter.output();
    }
    


    Matrix k = new Matrix(m_NumTrain, 1);
    for (int i = 0; i < m_NumTrain; i++) {
      k.set(i, 0, m_kernel.eval(-1, i, inst));
    }
    double result = k.transpose().times(m_t).get(0, 0) + m_avg_target;
    
    return result;
  }
  










  public double[][] predictInterval(Instance inst, double confidenceLevel)
    throws Exception
  {
    if (!m_checksTurnedOff) {
      m_Missing.input(inst);
      m_Missing.batchFinished();
      inst = m_Missing.output();
    }
    
    if (m_NominalToBinary != null) {
      m_NominalToBinary.input(inst);
      m_NominalToBinary.batchFinished();
      inst = m_NominalToBinary.output();
    }
    
    if (m_Filter != null) {
      m_Filter.input(inst);
      m_Filter.batchFinished();
      inst = m_Filter.output();
    }
    


    Matrix k = new Matrix(m_NumTrain, 1);
    for (int i = 0; i < m_NumTrain; i++) {
      k.set(i, 0, m_kernel.eval(-1, i, inst));
    }
    double kappa = m_kernel.eval(-1, -1, inst) + m_delta * m_delta;
    
    double estimate = k.transpose().times(m_t).get(0, 0) + m_avg_target;
    
    double sigma = Math.sqrt(kappa - k.transpose().times(m_C).times(k).get(0, 0));
    
    confidenceLevel = 1.0D - (1.0D - confidenceLevel) / 2.0D;
    
    double z = Statistics.normalInverse(confidenceLevel);
    
    double[][] interval = new double[1][2];
    
    interval[0][0] = (estimate - z * sigma);
    interval[0][1] = (estimate + z * sigma);
    
    return interval;
  }
  








  public double getStandardDeviation(Instance inst)
    throws Exception
  {
    if (!m_checksTurnedOff) {
      m_Missing.input(inst);
      m_Missing.batchFinished();
      inst = m_Missing.output();
    }
    
    if (m_NominalToBinary != null) {
      m_NominalToBinary.input(inst);m_Alin = 1.0D;
      m_Blin = 0.0D;
      
      m_NominalToBinary.batchFinished();
      inst = m_NominalToBinary.output();
    }
    
    if (m_Filter != null) {
      m_Filter.input(inst);
      m_Filter.batchFinished();
      inst = m_Filter.output();
    }
    
    Matrix k = new Matrix(m_NumTrain, 1);
    for (int i = 0; i < m_NumTrain; i++) {
      k.set(i, 0, m_kernel.eval(-1, i, inst));
    }
    double kappa = m_kernel.eval(-1, -1, inst) + m_delta * m_delta;
    
    double var = kappa - k.transpose().times(m_C).times(k).get(0, 0);
    
    if (var < 0.0D) { System.out.println("Aiaiai: variance is negative (" + var + ")!!!");
    }
    double sigma = Math.sqrt(var);
    
    return sigma;
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tLevel of Gaussian Noise.\n\t(default: 1.0)", "L", 1, "-L <double>"));
    



    result.addElement(new Option("\tWhether to 0=normalize/1=standardize/2=neither.\n\t(default: 0=normalize)", "N", 1, "-N"));
    



    result.addElement(new Option("\tThe Kernel to use.\n\t(default: weka.classifiers.functions.supportVector.PolyKernel)", "K", 1, "-K <classname and parameters>"));
    



    result.addElement(new Option("", "", 0, "\nOptions specific to kernel " + getKernel().getClass().getName() + ":"));
    



    enm = getKernel().listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    return result.elements();
  }
  


















































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('L', options);
    if (tmpStr.length() != 0) {
      setNoise(Double.parseDouble(tmpStr));
    } else {
      setNoise(1.0D);
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(0, TAGS_FILTER));
    }
    tmpStr = Utils.getOption('K', options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setKernel(Kernel.forName(tmpStr, tmpOptions));
    }
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-L");
    result.add("" + getNoise());
    
    result.add("-N");
    result.add("" + m_filterType);
    
    result.add("-K");
    result.add("" + m_kernel.getClass().getName() + " " + Utils.joinOptions(m_kernel.getOptions()));
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String kernelTipText()
  {
    return "The kernel to use.";
  }
  




  public Kernel getKernel()
  {
    return m_kernel;
  }
  




  public void setKernel(Kernel value)
  {
    m_kernel = value;
  }
  




  public String filterTypeTipText()
  {
    return "Determines how/if the data will be transformed.";
  }
  






  public SelectedTag getFilterType()
  {
    return new SelectedTag(m_filterType, TAGS_FILTER);
  }
  






  public void setFilterType(SelectedTag newType)
  {
    if (newType.getTags() == TAGS_FILTER) {
      m_filterType = newType.getSelectedTag().getID();
    }
  }
  




  public String noiseTipText()
  {
    return "The level of Gaussian Noise (added to the diagonal of the Covariance Matrix).";
  }
  




  public double getNoise()
  {
    return m_delta;
  }
  




  public void setNoise(double v)
  {
    m_delta = v;
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_t == null) {
      return "Gaussian Processes: No model built yet.";
    }
    try
    {
      text.append("Gaussian Processes\n\n");
      text.append("Kernel used:\n  " + m_kernel.toString() + "\n\n");
      
      text.append("Average Target Value : " + m_avg_target + "\n");
      
      text.append("Inverted Covariance Matrix:\n");
      double min = m_C.get(0, 0);
      double max = m_C.get(0, 0);
      for (int i = 0; i < m_NumTrain; i++) {
        for (int j = 0; j < m_NumTrain; j++)
          if (m_C.get(i, j) < min) { min = m_C.get(i, j);
          } else if (m_C.get(i, j) > max) max = m_C.get(i, j);
      }
      text.append("    Lowest Value = " + min + "\n");
      text.append("    Highest Value = " + max + "\n");
      text.append("Inverted Covariance Matrix * Target-value Vector:\n");
      min = m_t.get(0, 0);
      max = m_t.get(0, 0);
      for (int i = 0; i < m_NumTrain; i++) {
        if (m_t.get(i, 0) < min) { min = m_t.get(i, 0);
        } else if (m_t.get(i, 0) > max) max = m_t.get(i, 0);
      }
      text.append("    Lowest Value = " + min + "\n");
      text.append("    Highest Value = " + max + "\n \n");
    }
    catch (Exception e) {
      return "Can't print the classifier.";
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new GaussianProcesses(), argv);
  }
}

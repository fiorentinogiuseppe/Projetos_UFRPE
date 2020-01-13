package weka.filters.unsupervised.attribute;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MathematicalExpression;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SingleIndex;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;
import weka.filters.UnsupervisedFilter;










































































































































public class KernelFilter
  extends SimpleBatchFilter
  implements UnsupervisedFilter, TechnicalInformationHandler
{
  static final long serialVersionUID = 213800899640387499L;
  protected int m_NumTrainInstances;
  protected Kernel m_Kernel = new PolyKernel();
  

  protected Kernel m_ActualKernel = null;
  


  protected boolean m_checksTurnedOff;
  


  protected NominalToBinary m_NominalToBinary;
  


  protected ReplaceMissingValues m_Missing;
  


  protected File m_InitFile = new File(System.getProperty("user.dir"));
  


  protected SingleIndex m_InitFileClassIndex = new SingleIndex("last");
  

  protected boolean m_Initialized = false;
  


  protected String m_KernelFactorExpression = "1";
  


  protected double m_KernelFactor = 1.0D;
  

  protected Filter m_Filter = new Center();
  

  protected Filter m_ActualFilter = null;
  


  public KernelFilter() {}
  

  public String globalInfo()
  {
    return "Converts the given set of predictor variables into a kernel matrix. The class value remains unchangedm, as long as the preprocessing filter doesn't change it.\nBy default, the data is preprocessed with the Center filter, but the user can choose any filter (NB: one must be careful that the filter does not alter the class attribute unintentionally). With weka.filters.AllFilter the preprocessing gets disabled.\n\nFor more information regarding preprocessing the data, see:\n\n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "K.P. Bennett and M.J. Embrechts");
    result.setValue(TechnicalInformation.Field.TITLE, "An Optimization Perspective on Kernel Partial Least Squares Regression");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.EDITOR, "J. Suykens et al.");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Advances in Learning Theory: Methods, Models and Applications");
    result.setValue(TechnicalInformation.Field.PAGES, "227-249");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "IOS Press, Amsterdam, The Netherlands");
    result.setValue(TechnicalInformation.Field.SERIES, "NATO Science Series, Series III: Computer and System Sciences");
    result.setValue(TechnicalInformation.Field.VOLUME, "190");
    
    return result;
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tTurns off all checks - use with caution!\n\tTurning them off assumes that data is purely numeric, doesn't\n\tcontain any missing values, and has a nominal class. Turning them\n\toff also means that no header information will be stored if the\n\tmachine is linear. Finally, it also assumes that no instance has\n\ta weight equal to 0.\n\t(default: checks on)", "no-checks", 0, "-no-checks"));
    








    result.addElement(new Option("\tThe file to initialize the filter with (optional).", "F", 1, "-F <filename>"));
    


    result.addElement(new Option("\tThe class index for the file to initialize with,\n\tFirst and last are valid (optional, default: last).", "C", 1, "-C <num>"));
    



    result.addElement(new Option("\tThe Kernel to use.\n\t(default: weka.classifiers.functions.supportVector.PolyKernel)", "K", 1, "-K <classname and parameters>"));
    



    result.addElement(new Option("\tDefines a factor for the kernel.\n\t\t- RBFKernel: a factor for gamma\n\t\t\tStandardize: 1/(2*N)\n\t\t\tNormalize..: 6/N\n\tAvailable parameters are:\n\t\tN for # of instances, A for # of attributes\n\t(default: 1)", "kernel-factor", 0, "-kernel-factor"));
    








    result.addElement(new Option("\tThe Filter used for preprocessing (use weka.filters.AllFilter\n\tto disable preprocessing).\n\t(default: " + Center.class.getName() + ")", "P", 1, "-P <classname and parameters>"));
    





    result.addElement(new Option("", "", 0, "\nOptions specific to kernel " + getKernel().getClass().getName() + ":"));
    



    enm = getKernel().listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    
    if ((getPreprocessing() instanceof OptionHandler)) {
      result.addElement(new Option("", "", 0, "\nOptions specific to preprocessing filter " + getPreprocessing().getClass().getName() + ":"));
      



      enm = ((OptionHandler)getPreprocessing()).listOptions();
      while (enm.hasMoreElements()) {
        result.addElement(enm.nextElement());
      }
    }
    return result.elements();
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getChecksTurnedOff()) {
      result.add("-no-checks");
    }
    if ((getInitFile() != null) && (getInitFile().isFile())) {
      result.add("-F");
      result.add("" + getInitFile().getAbsolutePath());
      
      result.add("-C");
      result.add("" + getInitFileClassIndex());
    }
    
    result.add("-K");
    result.add("" + getKernel().getClass().getName() + " " + Utils.joinOptions(getKernel().getOptions()));
    
    result.add("-kernel-factor");
    result.add("" + getKernelFactorExpression());
    
    result.add("-P");
    String tmpStr = getPreprocessing().getClass().getName();
    if ((getPreprocessing() instanceof OptionHandler))
      tmpStr = tmpStr + " " + Utils.joinOptions(((OptionHandler)getPreprocessing()).getOptions());
    result.add("" + tmpStr);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  



















































































  public void setOptions(String[] options)
    throws Exception
  {
    setChecksTurnedOff(Utils.getFlag("no-checks", options));
    
    String tmpStr = Utils.getOption('F', options);
    if (tmpStr.length() != 0) {
      setInitFile(new File(tmpStr));
    } else {
      setInitFile(null);
    }
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setInitFileClassIndex(tmpStr);
    } else {
      setInitFileClassIndex("last");
    }
    tmpStr = Utils.getOption('K', options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setKernel(Kernel.forName(tmpStr, tmpOptions));
    }
    
    tmpStr = Utils.getOption("kernel-factor", options);
    if (tmpStr.length() != 0) {
      setKernelFactorExpression(tmpStr);
    } else {
      setKernelFactorExpression("1");
    }
    tmpStr = Utils.getOption("P", options);
    tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setPreprocessing((Filter)Utils.forName(Filter.class, tmpStr, tmpOptions));
    }
    else {
      setPreprocessing(new Center());
    }
    
    super.setOptions(options);
  }
  





  public String initFileTipText()
  {
    return "The dataset to initialize the filter with.";
  }
  




  public File getInitFile()
  {
    return m_InitFile;
  }
  




  public void setInitFile(File value)
  {
    m_InitFile = value;
  }
  





  public String initFileClassIndexTipText()
  {
    return "The class index of the dataset to initialize the filter with (first and last are valid).";
  }
  




  public String getInitFileClassIndex()
  {
    return m_InitFileClassIndex.getSingleIndex();
  }
  




  public void setInitFileClassIndex(String value)
  {
    m_InitFileClassIndex.setSingleIndex(value);
  }
  





  public String kernelTipText()
  {
    return "The kernel to use.";
  }
  




  public Kernel getKernel()
  {
    return m_Kernel;
  }
  




  public void setKernel(Kernel value)
  {
    m_Kernel = value;
  }
  





  public void setChecksTurnedOff(boolean value)
  {
    m_checksTurnedOff = value;
  }
  




  public boolean getChecksTurnedOff()
  {
    return m_checksTurnedOff;
  }
  





  public String checksTurnedOffTipText()
  {
    return "Turns time-consuming checks off - use with caution.";
  }
  





  public String kernelFactorExpressionTipText()
  {
    return "The factor for the kernel, with A = # of attributes and N = # of instances.";
  }
  




  public String getKernelFactorExpression()
  {
    return m_KernelFactorExpression;
  }
  




  public void setKernelFactorExpression(String value)
  {
    m_KernelFactorExpression = value;
  }
  





  public String preprocessingTipText()
  {
    return "Sets the filter to use for preprocessing (use the AllFilter for no preprocessing).";
  }
  





  public void setPreprocessing(Filter value)
  {
    m_Filter = value;
    m_ActualFilter = null;
  }
  




  public Filter getPreprocessing()
  {
    return m_Filter;
  }
  



  protected void reset()
  {
    super.reset();
    
    m_Initialized = false;
  }
  










  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    return new Instances(inputFormat);
  }
  









  public void initFilter(Instances instances)
    throws Exception
  {
    HashMap symbols = new HashMap();
    symbols.put("A", new Double(instances.numAttributes()));
    symbols.put("N", new Double(instances.numInstances()));
    m_KernelFactor = MathematicalExpression.evaluate(getKernelFactorExpression(), symbols);
    

    if (!m_checksTurnedOff) {
      m_Missing = new ReplaceMissingValues();
      m_Missing.setInputFormat(instances);
      instances = Filter.useFilter(instances, m_Missing);
    }
    else {
      m_Missing = null;
    }
    
    if (getKernel().getCapabilities().handles(Capabilities.Capability.NUMERIC_ATTRIBUTES)) {
      boolean onlyNumeric = true;
      if (!m_checksTurnedOff) {
        for (int i = 0; i < instances.numAttributes(); i++) {
          if ((i != instances.classIndex()) && 
            (!instances.attribute(i).isNumeric())) {
            onlyNumeric = false;
            break;
          }
        }
      }
      

      if (!onlyNumeric) {
        m_NominalToBinary = new NominalToBinary();
        m_NominalToBinary.setInputFormat(instances);
        instances = Filter.useFilter(instances, m_NominalToBinary);
      }
      else {
        m_NominalToBinary = null;
      }
    }
    else {
      m_NominalToBinary = null;
    }
    
    if ((m_Filter != null) && (m_Filter.getClass() != AllFilter.class)) {
      m_ActualFilter = Filter.makeCopy(m_Filter);
      m_ActualFilter.setInputFormat(instances);
      instances = Filter.useFilter(instances, m_ActualFilter);
    }
    else {
      m_ActualFilter = null;
    }
    
    m_NumTrainInstances = instances.numInstances();
    

    m_ActualKernel = Kernel.makeCopy(m_Kernel);
    if ((m_ActualKernel instanceof RBFKernel)) {
      ((RBFKernel)m_ActualKernel).setGamma(m_KernelFactor * ((RBFKernel)m_ActualKernel).getGamma());
    }
    
    m_ActualKernel.buildKernel(instances);
    
    m_Initialized = true;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result;
    


    if (getKernel() == null) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
    } else {
      result = getKernel().getCapabilities();
    }
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  








  protected Instances process(Instances instances)
    throws Exception
  {
    if (!m_Initialized)
    {
      if ((getInitFile() != null) && (getInitFile().isFile())) {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(getInitFile().getAbsolutePath());
        Instances data = source.getDataSet();
        m_InitFileClassIndex.setUpper(data.numAttributes() - 1);
        data.setClassIndex(m_InitFileClassIndex.getIndex());
        initFilter(data);
      }
      else {
        initFilter(instances);
      }
    }
    

    if (m_Missing != null)
      instances = Filter.useFilter(instances, m_Missing);
    if (m_NominalToBinary != null)
      instances = Filter.useFilter(instances, m_NominalToBinary);
    if (m_ActualFilter != null) {
      instances = Filter.useFilter(instances, m_ActualFilter);
    }
    
    double[] classes = instances.attributeToDoubleArray(instances.classIndex());
    int classIndex = instances.classIndex();
    Attribute classAttribute = (Attribute)instances.classAttribute().copy();
    instances.setClassIndex(-1);
    instances.deleteAttributeAt(classIndex);
    

    FastVector atts = new FastVector();
    for (int j = 0; j < m_NumTrainInstances; j++)
      atts.addElement(new Attribute("Kernel " + j));
    atts.addElement(classAttribute);
    Instances result = new Instances("Kernel", atts, 0);
    result.setClassIndex(result.numAttributes() - 1);
    

    for (int i = 0; i < instances.numInstances(); i++) {
      double[] k = new double[m_NumTrainInstances + 1];
      
      for (int j = 0; j < m_NumTrainInstances; j++) {
        double v = m_ActualKernel.eval(-1, j, instances.instance(i));
        k[j] = v;
      }
      k[(k.length - 1)] = classes[i];
      

      Instance in = new Instance(1.0D, k);
      result.add(in);
    }
    
    if (!isFirstBatchDone()) {
      setOutputFormat(result);
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9570 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new KernelFilter(), args);
  }
}

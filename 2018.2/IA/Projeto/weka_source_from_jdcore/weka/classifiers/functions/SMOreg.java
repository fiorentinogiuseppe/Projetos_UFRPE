package weka.classifiers.functions;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RegOptimizer;
import weka.classifiers.functions.supportVector.RegSMOImproved;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;








































































































































public class SMOreg
  extends Classifier
  implements WeightedInstancesHandler, AdditionalMeasureProducer, TechnicalInformationHandler
{
  private static final long serialVersionUID = -7149606251113102827L;
  public static final int FILTER_NORMALIZE = 0;
  public static final int FILTER_STANDARDIZE = 1;
  public static final int FILTER_NONE = 2;
  public static final Tag[] TAGS_FILTER = { new Tag(0, "Normalize training data"), new Tag(1, "Standardize training data"), new Tag(2, "No normalization/standardization") };
  






  protected int m_filterType = 0;
  

  protected NominalToBinary m_NominalToBinary;
  

  protected Filter m_Filter = null;
  

  protected ReplaceMissingValues m_Missing;
  

  protected boolean m_onlyNumeric;
  

  protected double m_C = 1.0D;
  


  protected double m_x1 = 1.0D;
  protected double m_x0 = 0.0D;
  

  protected RegOptimizer m_optimizer = new RegSMOImproved();
  

  protected Kernel m_kernel = new PolyKernel();
  


  public SMOreg() {}
  

  public String globalInfo()
  {
    return "SMOreg implements the support vector machine for regression. The parameters can be learned using various algorithms. The algorithm is selected by setting the RegOptimizer. The most popular algorithm (" + RegSMOImproved.class.getName().replaceAll(".*\\.", "") + ") is due to Shevade, Keerthi " + "et al and this is the default RegOptimizer.\n\n" + "For more information see:\n\n" + getTechnicalInformation().toString();
  }
  


















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "S.K. Shevade and S.S. Keerthi and C. Bhattacharyya and K.R.K. Murthy");
    result.setValue(TechnicalInformation.Field.TITLE, "Improvements to the SMO Algorithm for SVM Regression");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "IEEE Transactions on Neural Networks");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.PS, "http://guppy.mpe.nus.edu.sg/~mpessk/svm/ieee_smo_reg.ps.gz");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.TECHREPORT);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "A.J. Smola and B. Schoelkopf");
    additional.setValue(TechnicalInformation.Field.TITLE, "A tutorial on support vector regression");
    additional.setValue(TechnicalInformation.Field.NOTE, "NeuroCOLT2 Technical Report NC2-TR-1998-030");
    additional.setValue(TechnicalInformation.Field.YEAR, "1998");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe complexity constant C.\n\t(default 1)", "C", 1, "-C <double>"));
    



    result.addElement(new Option("\tWhether to 0=normalize/1=standardize/2=neither.\n\t(default 0=normalize)", "N", 1, "-N"));
    



    result.addElement(new Option("\tOptimizer class used for solving quadratic optimization problem\n\t(default " + RegSMOImproved.class.getName() + ")", "I", 1, "-I <classname and parameters>"));
    



    result.addElement(new Option("\tThe Kernel to use.\n\t(default: weka.classifiers.functions.supportVector.PolyKernel)", "K", 1, "-K <classname and parameters>"));
    



    result.addElement(new Option("", "", 0, "\nOptions specific to optimizer ('-I') " + getRegOptimizer().getClass().getName() + ":"));
    



    Enumeration enm = getRegOptimizer().listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("", "", 0, "\nOptions specific to kernel ('-K') " + getKernel().getClass().getName() + ":"));
    



    enm = getKernel().listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    return result.elements();
  }
  













































































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setC(Double.parseDouble(tmpStr));
    } else {
      setC(1.0D);
    }
    
    String nString = Utils.getOption('N', options);
    if (nString.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(nString), TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(0, TAGS_FILTER));
    }
    
    tmpStr = Utils.getOption('I', options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setRegOptimizer((RegOptimizer)Utils.forName(RegOptimizer.class, tmpStr, tmpOptions));
    }
    else
    {
      setRegOptimizer(new RegSMOImproved());
    }
    
    tmpStr = Utils.getOption('K', options);
    tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setKernel(Kernel.forName(tmpStr, tmpOptions));
    }
    else {
      setKernel(new PolyKernel());
    }
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-C");
    result.add("" + getC());
    
    result.add("-N");
    result.add("" + m_filterType);
    
    result.add("-I");
    result.add("" + getRegOptimizer().getClass().getName() + " " + Utils.joinOptions(getRegOptimizer().getOptions()));
    
    result.add("-K");
    result.add("" + getKernel().getClass().getName() + " " + Utils.joinOptions(getKernel().getOptions()));
    
    return (String[])result.toArray(new String[result.size()]);
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
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    



    Instances data = new Instances(instances, 0);
    for (int i = 0; i < instances.numInstances(); i++) {
      if (instances.instance(i).weight() > 0.0D) {
        data.add(instances.instance(i));
      }
    }
    
    if (data.numInstances() == 0) {
      throw new Exception("No training instances left after removing instance with either a weight null or a missing class!");
    }
    instances = data;
    
    m_onlyNumeric = true;
    for (int i = 0; i < instances.numAttributes(); i++) {
      if ((i != instances.classIndex()) && 
        (!instances.attribute(i).isNumeric())) {
        m_onlyNumeric = false;
        break;
      }
    }
    
    m_Missing = new ReplaceMissingValues();
    m_Missing.setInputFormat(instances);
    instances = Filter.useFilter(instances, m_Missing);
    
    if (getCapabilities().handles(Capabilities.Capability.NUMERIC_ATTRIBUTES)) {
      if (!m_onlyNumeric) {
        m_NominalToBinary = new NominalToBinary();
        m_NominalToBinary.setInputFormat(instances);
        instances = Filter.useFilter(instances, m_NominalToBinary);
      } else {
        m_NominalToBinary = null;
      }
    } else {
      m_NominalToBinary = null;
    }
    

    double y0 = instances.instance(0).classValue();
    int index = 1;
    while ((index < instances.numInstances()) && (instances.instance(index).classValue() == y0)) {
      index++;
    }
    if (index == instances.numInstances())
    {

      throw new Exception("All class values are the same. At least two class values should be different");
    }
    double y1 = instances.instance(index).classValue();
    

    if (m_filterType == 1) {
      m_Filter = new Standardize();
      ((Standardize)m_Filter).setIgnoreClass(true);
      m_Filter.setInputFormat(instances);
      instances = Filter.useFilter(instances, m_Filter);
    } else if (m_filterType == 0) {
      m_Filter = new Normalize();
      ((Normalize)m_Filter).setIgnoreClass(true);
      m_Filter.setInputFormat(instances);
      instances = Filter.useFilter(instances, m_Filter);
    } else {
      m_Filter = null;
    }
    if (m_Filter != null) {
      double z0 = instances.instance(0).classValue();
      double z1 = instances.instance(index).classValue();
      m_x1 = ((y0 - y1) / (z0 - z1));
      m_x0 = (y0 - m_x1 * z0);
    } else {
      m_x1 = 1.0D;
      m_x0 = 0.0D;
    }
    
    m_optimizer.setSMOReg(this);
    m_optimizer.buildClassifier(instances);
  }
  






  public double classifyInstance(Instance instance)
    throws Exception
  {
    m_Missing.input(instance);
    m_Missing.batchFinished();
    instance = m_Missing.output();
    
    if ((!m_onlyNumeric) && (m_NominalToBinary != null)) {
      m_NominalToBinary.input(instance);
      m_NominalToBinary.batchFinished();
      instance = m_NominalToBinary.output();
    }
    
    if (m_Filter != null) {
      m_Filter.input(instance);
      m_Filter.batchFinished();
      instance = m_Filter.output();
    }
    
    double result = m_optimizer.SVMOutput(instance);
    return result * m_x1 + m_x0;
  }
  





  public String regOptimizerTipText()
  {
    return "The learning algorithm.";
  }
  




  public void setRegOptimizer(RegOptimizer regOptimizer)
  {
    m_optimizer = regOptimizer;
  }
  




  public RegOptimizer getRegOptimizer()
  {
    return m_optimizer;
  }
  





  public String kernelTipText()
  {
    return "The kernel to use.";
  }
  




  public void setKernel(Kernel value)
  {
    m_kernel = value;
  }
  




  public Kernel getKernel()
  {
    return m_kernel;
  }
  





  public String cTipText()
  {
    return "The complexity parameter C.";
  }
  




  public double getC()
  {
    return m_C;
  }
  




  public void setC(double v)
  {
    m_C = v;
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
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if ((m_optimizer == null) || (!m_optimizer.modelBuilt())) {
      return "SMOreg: No model built yet.";
    }
    try
    {
      text.append(m_optimizer.toString());
    }
    catch (Exception e) {
      return "Can't print SMVreg classifier.";
    }
    
    return text.toString();
  }
  






  public Enumeration enumerateMeasures()
  {
    Vector result = new Vector();
    
    result.addElement("measureKernelEvaluations");
    result.addElement("measureCacheHits");
    
    return result.elements();
  }
  





  public double getMeasure(String measureName)
  {
    if (measureName.equalsIgnoreCase("measureKernelEvaluations"))
      return measureKernelEvaluations();
    if (measureName.equalsIgnoreCase("measureCacheHits")) {
      return measureCacheHits();
    }
    throw new IllegalArgumentException("Measure '" + measureName + "' is not supported!");
  }
  




  protected double measureKernelEvaluations()
  {
    if (m_optimizer != null) {
      return m_optimizer.getKernelEvaluations();
    }
    return 0.0D;
  }
  





  protected double measureCacheHits()
  {
    if (m_optimizer != null) {
      return m_optimizer.getCacheHits();
    }
    return 0.0D;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8126 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new SMOreg(), args);
  }
}

package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
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
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.SimpleBatchFilter;































































































public class Wavelet
  extends SimpleBatchFilter
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -3335106965521265631L;
  public static final int ALGORITHM_HAAR = 0;
  public static final Tag[] TAGS_ALGORITHM = { new Tag(0, "Haar") };
  


  public static final int PADDING_ZERO = 0;
  

  public static final Tag[] TAGS_PADDING = { new Tag(0, "Zero") };
  



  protected Filter m_Filter = null;
  

  protected int m_Algorithm = 0;
  

  protected int m_Padding = 0;
  




  public Wavelet()
  {
    m_Filter = new MultiFilter();
    ((MultiFilter)m_Filter).setFilters(new Filter[] { new ReplaceMissingValues(), new Normalize() });
  }
  









  public String globalInfo()
  {
    return "A filter for wavelet transformation.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Wikipedia");
    result.setValue(TechnicalInformation.Field.YEAR, "2004");
    result.setValue(TechnicalInformation.Field.TITLE, "Discrete wavelet transform");
    result.setValue(TechnicalInformation.Field.HTTP, "http://en.wikipedia.org/wiki/Discrete_wavelet_transform");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.MISC);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Kristian Sandberg");
    additional.setValue(TechnicalInformation.Field.YEAR, "2000");
    additional.setValue(TechnicalInformation.Field.TITLE, "The Haar wavelet transform");
    additional.setValue(TechnicalInformation.Field.INSTITUTION, "Dept. of Applied Mathematics");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "University of Colorado at Boulder, USA");
    additional.setValue(TechnicalInformation.Field.HTTP, "http://amath.colorado.edu/courses/5720/2000Spr/Labs/Haar/haar.html");
    
    return result;
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    String param = "";
    for (int i = 0; i < TAGS_ALGORITHM.length; i++) {
      if (i > 0)
        param = param + "|";
      SelectedTag tag = new SelectedTag(TAGS_ALGORITHM[i].getID(), TAGS_ALGORITHM);
      param = param + tag.getSelectedTag().getReadable();
    }
    result.addElement(new Option("\tThe algorithm to use.\n\t(default: HAAR)", "A", 1, "-A <" + param + ">"));
    



    param = "";
    for (i = 0; i < TAGS_PADDING.length; i++) {
      if (i > 0)
        param = param + "|";
      SelectedTag tag = new SelectedTag(TAGS_PADDING[i].getID(), TAGS_PADDING);
      param = param + tag.getSelectedTag().getReadable();
    }
    result.addElement(new Option("\tThe padding to use.\n\t(default: ZERO)", "P", 1, "-P <" + param + ">"));
    



    result.addElement(new Option("\tThe filter to use as preprocessing step (classname and options).\n\t(default: MultiFilter with ReplaceMissingValues and Normalize)", "F", 1, "-F <filter specification>"));
    



    if ((getFilter() instanceof OptionHandler)) {
      result.addElement(new Option("", "", 0, "\nOptions specific to filter " + getFilter().getClass().getName() + " ('-F'):"));
      



      enm = ((OptionHandler)getFilter()).listOptions();
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
    result.add("-A");
    result.add("" + getAlgorithm().getSelectedTag().getReadable());
    
    result.add("-P");
    result.add("" + getPadding().getSelectedTag().getReadable());
    
    result.add("-F");
    if ((getFilter() instanceof OptionHandler)) {
      result.add(getFilter().getClass().getName() + " " + Utils.joinOptions(((OptionHandler)getFilter()).getOptions()));

    }
    else
    {
      result.add(getFilter().getClass().getName());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  






































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption("A", options);
    if (tmpStr.length() != 0) {
      setAlgorithm(new SelectedTag(tmpStr, TAGS_ALGORITHM));
    } else {
      setAlgorithm(new SelectedTag(0, TAGS_ALGORITHM));
    }
    tmpStr = Utils.getOption("P", options);
    if (tmpStr.length() != 0) {
      setPadding(new SelectedTag(tmpStr, TAGS_PADDING));
    } else {
      setPadding(new SelectedTag(0, TAGS_PADDING));
    }
    tmpStr = Utils.getOption("F", options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setFilter((Filter)Utils.forName(Filter.class, tmpStr, tmpOptions));
    }
    else {
      Filter filter = new MultiFilter();
      ((MultiFilter)filter).setFilters(new Filter[] { new ReplaceMissingValues(), new Normalize() });
      



      setFilter(filter);
    }
  }
  





  public String filterTipText()
  {
    return "The preprocessing filter to use.";
  }
  




  public void setFilter(Filter value)
  {
    m_Filter = value;
  }
  




  public Filter getFilter()
  {
    return m_Filter;
  }
  





  public String algorithmTipText()
  {
    return "Sets the type of algorithm to use.";
  }
  




  public void setAlgorithm(SelectedTag value)
  {
    if (value.getTags() == TAGS_ALGORITHM) {
      m_Algorithm = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getAlgorithm()
  {
    return new SelectedTag(m_Algorithm, TAGS_ALGORITHM);
  }
  





  public String paddingTipText()
  {
    return "Sets the type of padding to use.";
  }
  




  public void setPadding(SelectedTag value)
  {
    if (value.getTags() == TAGS_PADDING) {
      m_Padding = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getPadding()
  {
    return new SelectedTag(m_Padding, TAGS_PADDING);
  }
  









  protected static int nextPowerOf2(int n)
  {
    int exp = (int)StrictMath.ceil(StrictMath.log(n) / StrictMath.log(2.0D));
    exp = StrictMath.max(2, exp);
    
    return (int)StrictMath.pow(2.0D, exp);
  }
  








  protected Instances pad(Instances data)
  {
    int numAtts;
    







    switch (m_Padding) {
    case 0:  int numAtts;
      if (data.classIndex() > -1) {
        numAtts = nextPowerOf2(data.numAttributes() - 1) + 1 - data.numAttributes();
      } else
        numAtts = nextPowerOf2(data.numAttributes()) - data.numAttributes();
      break;
    
    default: 
      throw new IllegalStateException("Padding " + new SelectedTag(m_Algorithm, TAGS_PADDING) + " not implemented!");
    }
    
    

    Instances result = new Instances(data);
    String prefix = getAlgorithm().getSelectedTag().getReadable();
    

    if (numAtts > 0)
    {
      boolean isLast = data.classIndex() == data.numAttributes() - 1;
      Vector<Integer> padded = new Vector();
      for (int i = 0; i < numAtts; i++) { int index;
        int index; if (isLast) {
          index = result.numAttributes() - 1;
        } else {
          index = result.numAttributes();
        }
        result.insertAttributeAt(new Attribute(prefix + "_padding_" + (i + 1)), index);
        



        padded.add(new Integer(index));
      }
      

      int[] indices = new int[padded.size()];
      for (i = 0; i < padded.size(); i++) {
        indices[i] = ((Integer)padded.get(i)).intValue();
      }
      
      switch (m_Padding) {
      case 0: 
        for (i = 0; i < result.numInstances(); i++) {
          for (int n = 0; n < indices.length; n++) {
            result.instance(i).setValue(indices[n], 0.0D);
          }
        }
      }
      
    }
    
    data = result;
    FastVector atts = new FastVector();
    int n = 0;
    for (int i = 0; i < data.numAttributes(); i++) {
      n++;
      if (i == data.classIndex()) {
        atts.addElement((Attribute)data.attribute(i).copy());
      } else {
        atts.addElement(new Attribute(prefix + "_" + n));
      }
    }
    
    result = new Instances(data.relationName(), atts, data.numInstances());
    result.setClassIndex(data.classIndex());
    for (i = 0; i < data.numInstances(); i++) {
      result.add(new Instance(1.0D, data.instance(i).toDoubleArray()));
    }
    return result;
  }
  












  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    return pad(new Instances(inputFormat, 0));
  }
  

















  protected Instances processHAAR(Instances instances)
    throws Exception
  {
    int clsIdx = instances.classIndex();
    double[] clsVal = null;
    Attribute clsAtt = null;
    if (clsIdx > -1) {
      clsVal = instances.attributeToDoubleArray(clsIdx);
      clsAtt = (Attribute)instances.classAttribute().copy();
      instances.setClassIndex(-1);
      instances.deleteAttributeAt(clsIdx);
    }
    Instances result = new Instances(instances, 0);
    int level = (int)StrictMath.ceil(StrictMath.log(instances.numAttributes()) / StrictMath.log(2.0D));
    


    for (int i = 0; i < instances.numInstances(); i++) {
      double[] oldVal = instances.instance(i).toDoubleArray();
      double[] newVal = new double[oldVal.length];
      
      for (int n = level; n > 0; n--) {
        int length = (int)StrictMath.pow(2.0D, n - 1);
        
        for (int j = 0; j < length; j++) {
          newVal[j] = ((oldVal[(j * 2)] + oldVal[(j * 2 + 1)]) / StrictMath.sqrt(2.0D));
          newVal[(j + length)] = ((oldVal[(j * 2)] - oldVal[(j * 2 + 1)]) / StrictMath.sqrt(2.0D));
        }
        
        System.arraycopy(newVal, 0, oldVal, 0, newVal.length);
      }
      

      result.add(new Instance(1.0D, newVal));
    }
    

    if (clsIdx > -1) {
      result.insertAttributeAt(clsAtt, clsIdx);
      result.setClassIndex(clsIdx);
      for (i = 0; i < clsVal.length; i++) {
        result.instance(i).setClassValue(clsVal[i]);
      }
    }
    return result;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  







  protected Instances process(Instances instances)
    throws Exception
  {
    if (!isFirstBatchDone())
      m_Filter.setInputFormat(instances);
    instances = Filter.useFilter(instances, m_Filter);
    
    switch (m_Algorithm) {
    case 0: 
      return processHAAR(pad(instances));
    }
    throw new IllegalStateException("Algorithm type '" + m_Algorithm + "' is not recognized!");
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5547 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new Wavelet(), args);
  }
}

package weka.filters.unsupervised.attribute;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.SimpleBatchFilter;






























































































public class InterquartileRange
  extends SimpleBatchFilter
{
  private static final long serialVersionUID = -227879653639723030L;
  public static final int NON_NUMERIC = -1;
  protected Range m_Attributes = new Range("first-last");
  

  protected int[] m_AttributeIndices = null;
  

  protected double m_OutlierFactor = 3.0D;
  

  protected double m_ExtremeValuesFactor = 2.0D * m_OutlierFactor;
  

  protected boolean m_ExtremeValuesAsOutliers = false;
  

  protected double[] m_UpperExtremeValue = null;
  

  protected double[] m_UpperOutlier = null;
  

  protected double[] m_LowerOutlier = null;
  

  protected double[] m_IQR = null;
  

  protected double[] m_Median = null;
  

  protected double[] m_LowerExtremeValue = null;
  


  protected boolean m_DetectionPerAttribute = false;
  

  protected int[] m_OutlierAttributePosition = null;
  





  protected boolean m_OutputOffsetMultiplier = false;
  


  public InterquartileRange() {}
  

  public String globalInfo()
  {
    return "A filter for detecting outliers and extreme values based on interquartile ranges. The filter skips the class attribute.\n\nOutliers:\n  Q3 + OF*IQR < x <= Q3 + EVF*IQR\n  or\n  Q1 - EVF*IQR <= x < Q1 - OF*IQR\n\nExtreme values:\n  x > Q3 + EVF*IQR\n  or\n  x < Q1 - EVF*IQR\n\nKey:\n  Q1  = 25% quartile\n  Q3  = 75% quartile\n  IQR = Interquartile Range, difference between Q1 and Q3\n  OF  = Outlier Factor\n  EVF = Extreme Value Factor";
  }
  






















  public Enumeration listOptions()
  {
    Vector result = new Vector();
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    result.addElement(new Option("\tSpecifies list of columns to base outlier/extreme value detection\n\ton. If an instance is considered in at least one of those\n\tattributes an outlier/extreme value, it is tagged accordingly.\n 'first' and 'last' are valid indexes.\n\t(default none)", "R", 1, "-R <col1,col2-col4,...>"));
    






    result.addElement(new Option("\tThe factor for outlier detection.\n\t(default: 3)", "O", 1, "-O <num>"));
    



    result.addElement(new Option("\tThe factor for extreme values detection.\n\t(default: 2*Outlier Factor)", "E", 1, "-E <num>"));
    



    result.addElement(new Option("\tTags extreme values also as outliers.\n\t(default: off)", "E-as-O", 0, "-E-as-O"));
    



    result.addElement(new Option("\tGenerates Outlier/ExtremeValue pair for each numeric attribute in\n\tthe range, not just a single indicator pair for all the attributes.\n\t(default: off)", "P", 0, "-P"));
    




    result.addElement(new Option("\tGenerates an additional attribute 'Offset' per Outlier/ExtremeValue\n\tpair that contains the multiplier that the value is off the median.\n\t   value = median + 'multiplier' * IQR\nNote: implicitely sets '-P'.\t(default: off)", "M", 0, "-M"));
    






    return result.elements();
  }
  












































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption("R", options);
    if (tmpStr.length() != 0) {
      setAttributeIndices(tmpStr);
    } else {
      setAttributeIndices("first-last");
    }
    tmpStr = Utils.getOption("O", options);
    if (tmpStr.length() != 0) {
      setOutlierFactor(Double.parseDouble(tmpStr));
    } else {
      setOutlierFactor(3.0D);
    }
    tmpStr = Utils.getOption("E", options);
    if (tmpStr.length() != 0) {
      setExtremeValuesFactor(Double.parseDouble(tmpStr));
    } else {
      setExtremeValuesFactor(2.0D * getOutlierFactor());
    }
    setExtremeValuesAsOutliers(Utils.getFlag("E-as-O", options));
    
    setDetectionPerAttribute(Utils.getFlag("P", options));
    
    setOutputOffsetMultiplier(Utils.getFlag("M", options));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-R");
    if (!getAttributeIndices().equals("")) {
      result.add(getAttributeIndices());
    } else {
      result.add("first-last");
    }
    result.add("-O");
    result.add("" + getOutlierFactor());
    
    result.add("-E");
    result.add("" + getExtremeValuesFactor());
    
    if (getExtremeValuesAsOutliers()) {
      result.add("-E-as-O");
    }
    if (getDetectionPerAttribute()) {
      result.add("-P");
    }
    if (getOutputOffsetMultiplier()) {
      result.add("-M");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on;  this is a comma separated list of attribute indices, with \"first\" and \"last\" valid values; specify an inclusive range with \"-\", eg: \"first-3,5,6-10,last\".";
  }
  








  public String getAttributeIndices()
  {
    return m_Attributes.getRanges();
  }
  









  public void setAttributeIndices(String value)
  {
    m_Attributes.setRanges(value);
  }
  









  public void setAttributeIndicesArray(int[] value)
  {
    setAttributeIndices(Range.indicesToRangeList(value));
  }
  





  public String outlierFactorTipText()
  {
    return "The factor for determining the thresholds for outliers.";
  }
  




  public void setOutlierFactor(double value)
  {
    if (value >= getExtremeValuesFactor()) {
      System.err.println("OutlierFactor must be smaller than ExtremeValueFactor");
    } else {
      m_OutlierFactor = value;
    }
  }
  



  public double getOutlierFactor()
  {
    return m_OutlierFactor;
  }
  





  public String extremeValuesFactorTipText()
  {
    return "The factor for determining the thresholds for extreme values.";
  }
  




  public void setExtremeValuesFactor(double value)
  {
    if (value <= getOutlierFactor()) {
      System.err.println("ExtremeValuesFactor must be greater than OutlierFactor!");
    } else {
      m_ExtremeValuesFactor = value;
    }
  }
  



  public double getExtremeValuesFactor()
  {
    return m_ExtremeValuesFactor;
  }
  





  public String extremeValuesAsOutliersTipText()
  {
    return "Whether to tag extreme values also as outliers.";
  }
  




  public void setExtremeValuesAsOutliers(boolean value)
  {
    m_ExtremeValuesAsOutliers = value;
  }
  




  public boolean getExtremeValuesAsOutliers()
  {
    return m_ExtremeValuesAsOutliers;
  }
  





  public String detectionPerAttributeTipText()
  {
    return "Generates Outlier/ExtremeValue attribute pair for each numeric attribute, not just a single pair for all numeric attributes together.";
  }
  









  public void setDetectionPerAttribute(boolean value)
  {
    m_DetectionPerAttribute = value;
    if (!m_DetectionPerAttribute) {
      m_OutputOffsetMultiplier = false;
    }
  }
  






  public boolean getDetectionPerAttribute()
  {
    return m_DetectionPerAttribute;
  }
  





  public String outputOffsetMultiplierTipText()
  {
    return "Generates an additional attribute 'Offset' that contains the multiplier the value is off the median: value = median + 'multiplier' * IQR";
  }
  









  public void setOutputOffsetMultiplier(boolean value)
  {
    m_OutputOffsetMultiplier = value;
    if (m_OutputOffsetMultiplier) {
      m_DetectionPerAttribute = true;
    }
  }
  





  public boolean getOutputOffsetMultiplier()
  {
    return m_OutputOffsetMultiplier;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  



















  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    m_Attributes.setUpper(inputFormat.numAttributes() - 1);
    m_AttributeIndices = m_Attributes.getSelection();
    for (int i = 0; i < m_AttributeIndices.length; i++)
    {
      if (m_AttributeIndices[i] == inputFormat.classIndex()) {
        m_AttributeIndices[i] = -1;


      }
      else if (!inputFormat.attribute(m_AttributeIndices[i]).isNumeric()) {
        m_AttributeIndices[i] = -1;
      }
    }
    
    FastVector atts = new FastVector();
    for (i = 0; i < inputFormat.numAttributes(); i++) {
      atts.addElement(inputFormat.attribute(i));
    }
    if (!getDetectionPerAttribute()) {
      m_OutlierAttributePosition = new int[1];
      m_OutlierAttributePosition[0] = atts.size();
      

      FastVector values = new FastVector();
      values.addElement("no");
      values.addElement("yes");
      atts.addElement(new Attribute("Outlier", values));
      
      values = new FastVector();
      values.addElement("no");
      values.addElement("yes");
      atts.addElement(new Attribute("ExtremeValue", values));
    }
    else {
      m_OutlierAttributePosition = new int[m_AttributeIndices.length];
      
      for (i = 0; i < m_AttributeIndices.length; i++) {
        if (m_AttributeIndices[i] != -1)
        {

          m_OutlierAttributePosition[i] = atts.size();
          

          FastVector values = new FastVector();
          values.addElement("no");
          values.addElement("yes");
          atts.addElement(new Attribute(inputFormat.attribute(m_AttributeIndices[i]).name() + "_Outlier", values));
          



          values = new FastVector();
          values.addElement("no");
          values.addElement("yes");
          atts.addElement(new Attribute(inputFormat.attribute(m_AttributeIndices[i]).name() + "_ExtremeValue", values));
          



          if (getOutputOffsetMultiplier()) {
            atts.addElement(new Attribute(inputFormat.attribute(m_AttributeIndices[i]).name() + "_Offset"));
          }
        }
      }
    }
    


    Instances result = new Instances(inputFormat.relationName(), atts, 0);
    result.setClassIndex(inputFormat.classIndex());
    
    return result;
  }
  













  protected void computeThresholds(Instances instances)
  {
    m_UpperExtremeValue = new double[m_AttributeIndices.length];
    m_UpperOutlier = new double[m_AttributeIndices.length];
    m_LowerOutlier = new double[m_AttributeIndices.length];
    m_LowerExtremeValue = new double[m_AttributeIndices.length];
    m_Median = new double[m_AttributeIndices.length];
    m_IQR = new double[m_AttributeIndices.length];
    
    for (int i = 0; i < m_AttributeIndices.length; i++)
    {
      if (m_AttributeIndices[i] != -1)
      {


        double[] values = instances.attributeToDoubleArray(m_AttributeIndices[i]);
        int[] sortedIndices = Utils.sort(values);
        

        int half = sortedIndices.length / 2;
        int quarter = half / 2;
        double q2;
        double q2; if (sortedIndices.length % 2 == 1) {
          q2 = values[sortedIndices[half]];
        }
        else
          q2 = (values[sortedIndices[half]] + values[sortedIndices[(half + 1)]]) / 2.0D;
        double q3;
        double q1;
        double q3; if (half % 2 == 1) {
          double q1 = values[sortedIndices[quarter]];
          q3 = values[sortedIndices[(sortedIndices.length - quarter - 1)]];
        }
        else {
          q1 = (values[sortedIndices[quarter]] + values[sortedIndices[(quarter + 1)]]) / 2.0D;
          q3 = (values[sortedIndices[(sortedIndices.length - quarter - 1)]] + values[sortedIndices[(sortedIndices.length - quarter)]]) / 2.0D;
        }
        

        m_Median[i] = q2;
        m_IQR[i] = (q3 - q1);
        m_UpperExtremeValue[i] = (q3 + getExtremeValuesFactor() * m_IQR[i]);
        m_UpperOutlier[i] = (q3 + getOutlierFactor() * m_IQR[i]);
        m_LowerOutlier[i] = (q1 - getOutlierFactor() * m_IQR[i]);
        m_LowerExtremeValue[i] = (q1 - getExtremeValuesFactor() * m_IQR[i]);
      }
    }
  }
  









  protected boolean isOutlier(Instance inst, int index)
  {
    double value = inst.value(m_AttributeIndices[index]);
    boolean result = ((m_UpperOutlier[index] < value) && (value <= m_UpperExtremeValue[index])) || ((m_LowerExtremeValue[index] <= value) && (value < m_LowerOutlier[index]));
    

    return result;
  }
  








  protected boolean isOutlier(Instance inst)
  {
    boolean result = false;
    
    for (int i = 0; i < m_AttributeIndices.length; i++)
    {
      if (m_AttributeIndices[i] != -1)
      {

        result = isOutlier(inst, i);
        
        if (result)
          break;
      }
    }
    return result;
  }
  










  protected boolean isExtremeValue(Instance inst, int index)
  {
    double value = inst.value(m_AttributeIndices[index]);
    boolean result = (value > m_UpperExtremeValue[index]) || (value < m_LowerExtremeValue[index]);
    

    return result;
  }
  








  protected boolean isExtremeValue(Instance inst)
  {
    boolean result = false;
    
    for (int i = 0; i < m_AttributeIndices.length; i++)
    {
      if (m_AttributeIndices[i] != -1)
      {

        result = isExtremeValue(inst, i);
        
        if (result)
          break;
      }
    }
    return result;
  }
  










  protected double calculateMultiplier(Instance inst, int index)
  {
    double value = inst.value(m_AttributeIndices[index]);
    double result = (value - m_Median[index]) / m_IQR[index];
    
    return result;
  }
  


















  protected Instances process(Instances instances)
    throws Exception
  {
    if (!isFirstBatchDone()) {
      computeThresholds(instances);
    }
    Instances result = getOutputFormat();
    int numAttOld = instances.numAttributes();
    int numAttNew = result.numAttributes();
    
    for (int n = 0; n < instances.numInstances(); n++) {
      Instance instOld = instances.instance(n);
      double[] values = new double[numAttNew];
      System.arraycopy(instOld.toDoubleArray(), 0, values, 0, numAttOld);
      

      Instance instNew = new Instance(1.0D, values);
      instNew.setDataset(result);
      

      if (!getDetectionPerAttribute())
      {
        if (isOutlier(instOld)) {
          instNew.setValue(m_OutlierAttributePosition[0], 1.0D);
        }
        if (isExtremeValue(instOld)) {
          instNew.setValue(m_OutlierAttributePosition[0] + 1, 1.0D);
          
          if (getExtremeValuesAsOutliers()) {
            instNew.setValue(m_OutlierAttributePosition[0], 1.0D);
          }
        }
      } else {
        for (int i = 0; i < m_AttributeIndices.length; i++)
        {
          if (m_AttributeIndices[i] != -1)
          {


            if (isOutlier(instOld, m_AttributeIndices[i])) {
              instNew.setValue(m_OutlierAttributePosition[i], 1.0D);
            }
            if (isExtremeValue(instOld, m_AttributeIndices[i])) {
              instNew.setValue(m_OutlierAttributePosition[i] + 1, 1.0D);
              
              if (getExtremeValuesAsOutliers()) {
                instNew.setValue(m_OutlierAttributePosition[i], 1.0D);
              }
            }
            if (getOutputOffsetMultiplier()) {
              instNew.setValue(m_OutlierAttributePosition[i] + 2, calculateMultiplier(instOld, m_AttributeIndices[i]));
            }
          }
        }
      }
      

      copyValues(instNew, false, instOld.dataset(), getOutputFormat());
      

      result.add(instNew);
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9529 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new InterquartileRange(), args);
  }
}

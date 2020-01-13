package weka.filters.unsupervised.attribute;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.SimpleStreamFilter;



















































































public class NumericCleaner
  extends SimpleStreamFilter
{
  private static final long serialVersionUID = -352890679895066592L;
  protected double m_MinThreshold = -1.7976931348623157E308D;
  

  protected double m_MinDefault = -1.7976931348623157E308D;
  

  protected double m_MaxThreshold = Double.MAX_VALUE;
  

  protected double m_MaxDefault = Double.MAX_VALUE;
  

  protected double m_CloseTo = 0.0D;
  

  protected double m_CloseToDefault = 0.0D;
  

  protected double m_CloseToTolerance = 1.0E-6D;
  

  protected Range m_Cols = new Range("first-last");
  

  protected boolean m_IncludeClass = false;
  

  protected int m_Decimals = -1;
  


  public NumericCleaner() {}
  

  public String globalInfo()
  {
    return "A filter that 'cleanses' the numeric data from values that are too small, too big or very close to a certain value (e.g., 0) and sets these values to a pre-defined default.";
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tThe minimum threshold. (default -Double.MAX_VALUE)", "min", 1, "-min <double>"));
    


    result.addElement(new Option("\tThe replacement for values smaller than the minimum threshold.\n\t(default -Double.MAX_VALUE)", "min-default", 1, "-min-default <double>"));
    



    result.addElement(new Option("\tThe maximum threshold. (default Double.MAX_VALUE)", "max", 1, "-max <double>"));
    


    result.addElement(new Option("\tThe replacement for values larger than the maximum threshold.\n\t(default Double.MAX_VALUE)", "max-default", 1, "-max-default <double>"));
    



    result.addElement(new Option("\tThe number values are checked for closeness. (default 0)", "closeto", 1, "-closeto <double>"));
    


    result.addElement(new Option("\tThe replacement for values that are close to '-closeto'.\n\t(default 0)", "closeto-default", 1, "-closeto-default <double>"));
    



    result.addElement(new Option("\tThe tolerance below which numbers are considered being close to \n\tto each other. (default 1E-6)", "closeto-tolerance", 1, "-closeto-tolerance <double>"));
    



    result.addElement(new Option("\tThe number of decimals to round to, -1 means no rounding at all.\n\t(default -1)", "decimals", 1, "-decimals <int>"));
    



    result.addElement(new Option("\tThe list of columns to cleanse, e.g., first-last or first-3,5-last.\n\t(default first-last)", "R", 1, "-R <col1,col2,...>"));
    



    result.addElement(new Option("\tInverts the matching sense.", "V", 0, "-V"));
    


    result.addElement(new Option("\tWhether to include the class in the cleansing.\n\tThe class column will always be skipped, if this flag is not\n\tpresent. (default no)", "include-class", 0, "-include-class"));
    




    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-min");
    result.add("" + m_MinThreshold);
    
    result.add("-min-default");
    result.add("" + m_MinDefault);
    
    result.add("-max");
    result.add("" + m_MaxThreshold);
    
    result.add("-max-default");
    result.add("" + m_MaxDefault);
    
    result.add("-closeto");
    result.add("" + m_CloseTo);
    
    result.add("-closeto-default");
    result.add("" + m_CloseToDefault);
    
    result.add("-closeto-tolerance");
    result.add("" + m_CloseToTolerance);
    
    result.add("-R");
    result.add("" + m_Cols.getRanges());
    
    if (m_Cols.getInvert()) {
      result.add("-V");
    }
    if (m_IncludeClass) {
      result.add("-include-class");
    }
    result.add("-decimals");
    result.add("" + getDecimals());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  























































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption("min", options);
    if (tmpStr.length() != 0) {
      setMinThreshold(Double.parseDouble(tmpStr));
    } else {
      setMinThreshold(-1.7976931348623157E308D);
    }
    tmpStr = Utils.getOption("min-default", options);
    if (tmpStr.length() != 0) {
      setMinDefault(Double.parseDouble(tmpStr));
    } else {
      setMinDefault(-1.7976931348623157E308D);
    }
    tmpStr = Utils.getOption("max", options);
    if (tmpStr.length() != 0) {
      setMaxThreshold(Double.parseDouble(tmpStr));
    } else {
      setMaxThreshold(Double.MAX_VALUE);
    }
    tmpStr = Utils.getOption("max-default", options);
    if (tmpStr.length() != 0) {
      setMaxDefault(Double.parseDouble(tmpStr));
    } else {
      setMaxDefault(Double.MAX_VALUE);
    }
    tmpStr = Utils.getOption("closeto", options);
    if (tmpStr.length() != 0) {
      setCloseTo(Double.parseDouble(tmpStr));
    } else {
      setCloseTo(0.0D);
    }
    tmpStr = Utils.getOption("closeto-default", options);
    if (tmpStr.length() != 0) {
      setCloseToDefault(Double.parseDouble(tmpStr));
    } else {
      setCloseToDefault(0.0D);
    }
    tmpStr = Utils.getOption("closeto-tolerance", options);
    if (tmpStr.length() != 0) {
      setCloseToTolerance(Double.parseDouble(tmpStr));
    } else {
      setCloseToTolerance(1.0E-6D);
    }
    tmpStr = Utils.getOption("R", options);
    if (tmpStr.length() != 0) {
      setAttributeIndices(tmpStr);
    } else {
      setAttributeIndices("first-last");
    }
    setInvertSelection(Utils.getFlag("V", options));
    
    setIncludeClass(Utils.getFlag("include-class", options));
    
    tmpStr = Utils.getOption("decimals", options);
    if (tmpStr.length() != 0) {
      setDecimals(Integer.parseInt(tmpStr));
    } else {
      setDecimals(-1);
    }
    super.setOptions(options);
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  












  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    m_Cols.setUpper(inputFormat.numAttributes() - 1);
    
    return new Instances(inputFormat);
  }
  











  protected Instance process(Instance instance)
    throws Exception
  {
    Instance result = (Instance)instance.copy();
    double factor;
    double factor; if (m_Decimals > -1) {
      factor = StrictMath.pow(10.0D, m_Decimals);
    } else {
      factor = 1.0D;
    }
    for (int i = 0; i < result.numAttributes(); i++)
    {
      if (result.attribute(i).isNumeric())
      {


        if (m_Cols.isInRange(i))
        {


          if ((result.classIndex() != i) || (m_IncludeClass))
          {


            if (result.value(i) < m_MinThreshold) {
              if (getDebug())
                System.out.println("Too small: " + result.value(i) + " -> " + m_MinDefault);
              result.setValue(i, m_MinDefault);

            }
            else if (result.value(i) > m_MaxThreshold) {
              if (getDebug())
                System.out.println("Too big: " + result.value(i) + " -> " + m_MaxDefault);
              result.setValue(i, m_MaxDefault);

            }
            else if ((result.value(i) - m_CloseTo < m_CloseToTolerance) && (m_CloseTo - result.value(i) < m_CloseToTolerance) && (result.value(i) != m_CloseTo))
            {

              if (getDebug())
                System.out.println("Too close: " + result.value(i) + " -> " + m_CloseToDefault);
              result.setValue(i, m_CloseToDefault);
            }
            

            if ((m_Decimals > -1) && (!result.isMissing(i))) {
              double val = result.value(i);
              val = StrictMath.round(val * factor) / factor;
              result.setValue(i, val);
            }
          } } }
    }
    return result;
  }
  





  public String minThresholdTipText()
  {
    return "The minimum threshold below values are replaced by a default.";
  }
  




  public double getMinThreshold()
  {
    return m_MinThreshold;
  }
  




  public void setMinThreshold(double value)
  {
    m_MinThreshold = value;
  }
  





  public String minDefaultTipText()
  {
    return "The default value to replace values that are below the minimum threshold.";
  }
  




  public double getMinDefault()
  {
    return m_MinDefault;
  }
  




  public void setMinDefault(double value)
  {
    m_MinDefault = value;
  }
  





  public String maxThresholdTipText()
  {
    return "The maximum threshold above values are replaced by a default.";
  }
  




  public double getMaxThreshold()
  {
    return m_MaxThreshold;
  }
  




  public void setMaxThreshold(double value)
  {
    m_MaxThreshold = value;
  }
  





  public String maxDefaultTipText()
  {
    return "The default value to replace values that are above the maximum threshold.";
  }
  




  public double getMaxDefault()
  {
    return m_MaxDefault;
  }
  




  public void setMaxDefault(double value)
  {
    m_MaxDefault = value;
  }
  





  public String closeToTipText()
  {
    return "The number values are checked for whether they are too close to and get replaced by a default.";
  }
  






  public double getCloseTo()
  {
    return m_CloseTo;
  }
  




  public void setCloseTo(double value)
  {
    m_CloseTo = value;
  }
  





  public String closeToDefaultTipText()
  {
    return "The default value to replace values with that are too close.";
  }
  




  public double getCloseToDefault()
  {
    return m_CloseToDefault;
  }
  




  public void setCloseToDefault(double value)
  {
    m_CloseToDefault = value;
  }
  





  public String closeToToleranceTipText()
  {
    return "The value below which values are considered close to.";
  }
  




  public double getCloseToTolerance()
  {
    return m_CloseToTolerance;
  }
  




  public void setCloseToTolerance(double value)
  {
    m_CloseToTolerance = value;
  }
  





  public String attributeIndicesTipText()
  {
    return "The selection of columns to use in the cleansing processs, first and last are valid indices.";
  }
  




  public String getAttributeIndices()
  {
    return m_Cols.getRanges();
  }
  




  public void setAttributeIndices(String value)
  {
    m_Cols.setRanges(value);
  }
  





  public String invertSelectionTipText()
  {
    return "If enabled the selection of the columns is inverted.";
  }
  




  public boolean getInvertSelection()
  {
    return m_Cols.getInvert();
  }
  




  public void setInvertSelection(boolean value)
  {
    m_Cols.setInvert(value);
  }
  





  public String includeClassTipText()
  {
    return "If disabled, the class attribute will be always left out of the cleaning process.";
  }
  





  public boolean getIncludeClass()
  {
    return m_IncludeClass;
  }
  




  public void setIncludeClass(boolean value)
  {
    m_IncludeClass = value;
  }
  





  public String decimalsTipText()
  {
    return "The number of decimals to round to, -1 means no rounding at all.";
  }
  




  public int getDecimals()
  {
    return m_Decimals;
  }
  




  public void setDecimals(int value)
  {
    m_Decimals = value;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8281 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new NumericCleaner(), args);
  }
}

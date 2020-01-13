package weka.filters.unsupervised.attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import weka.core.SingleIndex;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;
















































public class ChangeDateFormat
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = -1609344074013448737L;
  private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  

  private SingleIndex m_AttIndex = new SingleIndex("last");
  

  private SimpleDateFormat m_DateFormat = DEFAULT_FORMAT;
  

  private Attribute m_OutputAttribute;
  


  public ChangeDateFormat() {}
  

  public String globalInfo()
  {
    return "Changes the date format used by a date attribute. This is most useful for converting to a format with less precision, for example, from an absolute date to day of year, etc. This changes the format string, and changes the date values to those that would be parsed by the new format.";
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
  










  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    m_AttIndex.setUpper(instanceInfo.numAttributes() - 1);
    if (!instanceInfo.attribute(m_AttIndex.getIndex()).isDate()) {
      throw new UnsupportedAttributeTypeException("Chosen attribute not date.");
    }
    
    setOutputFormat();
    return true;
  }
  








  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    Instance newInstance = (Instance)instance.copy();
    int index = m_AttIndex.getIndex();
    if (!newInstance.isMissing(index)) {
      double value = instance.value(index);
      
      try
      {
        value = m_OutputAttribute.parseDate(m_OutputAttribute.formatDate(value));
      } catch (ParseException pe) {
        throw new RuntimeException("Output date format couldn't parse its own output!!");
      }
      newInstance.setValue(index, value);
    }
    push(newInstance);
    return true;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tSets the attribute index (default last).", "C", 1, "-C <col>"));
    


    newVector.addElement(new Option("\tSets the output date format string (default corresponds to ISO-8601).", "F", 1, "-F <value index>"));
    


    return newVector.elements();
  }
  
















  public void setOptions(String[] options)
    throws Exception
  {
    String attIndex = Utils.getOption('C', options);
    if (attIndex.length() != 0) {
      setAttributeIndex(attIndex);
    } else {
      setAttributeIndex("last");
    }
    
    String formatString = Utils.getOption('F', options);
    if (formatString.length() != 0) {
      setDateFormat(formatString);
    } else {
      setDateFormat(DEFAULT_FORMAT);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[4];
    int current = 0;
    
    options[(current++)] = "-C";
    options[(current++)] = ("" + getAttributeIndex());
    options[(current++)] = "-F";
    options[(current++)] = ("" + getDateFormat().toPattern());
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String attributeIndexTipText()
  {
    return "Sets which attribute to process. This attribute must be of type date (\"first\" and \"last\" are valid values)";
  }
  






  public String getAttributeIndex()
  {
    return m_AttIndex.getSingleIndex();
  }
  





  public void setAttributeIndex(String attIndex)
  {
    m_AttIndex.setSingleIndex(attIndex);
  }
  




  public String dateFormatTipText()
  {
    return "The date format to change to. This should be a format understood by Java's SimpleDateFormat class.";
  }
  






  public SimpleDateFormat getDateFormat()
  {
    return m_DateFormat;
  }
  





  public void setDateFormat(String dateFormat)
  {
    setDateFormat(new SimpleDateFormat(dateFormat));
  }
  




  public void setDateFormat(SimpleDateFormat dateFormat)
  {
    if (dateFormat == null) {
      throw new NullPointerException();
    }
    m_DateFormat = dateFormat;
  }
  





  private void setOutputFormat()
  {
    FastVector newAtts = new FastVector(getInputFormat().numAttributes());
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if (j == m_AttIndex.getIndex()) {
        newAtts.addElement(new Attribute(att.name(), getDateFormat().toPattern()));
      } else {
        newAtts.addElement(att.copy());
      }
    }
    

    Instances newData = new Instances(getInputFormat().relationName(), newAtts, 0);
    newData.setClassIndex(getInputFormat().classIndex());
    m_OutputAttribute = newData.attribute(m_AttIndex.getIndex());
    setOutputFormat(newData);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new ChangeDateFormat(), argv);
  }
}

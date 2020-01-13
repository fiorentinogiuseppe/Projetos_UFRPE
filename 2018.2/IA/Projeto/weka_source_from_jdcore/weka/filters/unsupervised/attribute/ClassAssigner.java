package weka.filters.unsupervised.attribute;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.SimpleStreamFilter;





















































public class ClassAssigner
  extends SimpleStreamFilter
{
  private static final long serialVersionUID = 1775780193887394115L;
  public static final int FIRST = 0;
  public static final int LAST = -2;
  public static final int UNSET = -1;
  protected int m_ClassIndex = -2;
  


  public ClassAssigner() {}
  

  public String globalInfo()
  {
    return "Filter that can set and unset the class index.";
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    result.addElement(new Option("\tThe index of the class attribute. Index starts with 1, 'first'\n\tand 'last' are accepted, '0' unsets the class index.\n\t(default: last)", "C", 1, "-C <num|first|last|0>"));
    




    return result.elements();
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption("C", options);
    if (tmpStr.length() != 0) {
      setClassIndex(tmpStr);
    } else {
      setClassIndex("last");
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
    result.add(getClassIndex());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String classIndexTipText()
  {
    return "The index of the class attribute, starts with 1, 'first' and 'last' are accepted as well, '0' unsets the class index.";
  }
  






  public void setClassIndex(String value)
  {
    if (value.equalsIgnoreCase("first")) {
      m_ClassIndex = 0;
    }
    else if (value.equalsIgnoreCase("last")) {
      m_ClassIndex = -2;
    }
    else if (value.equalsIgnoreCase("0")) {
      m_ClassIndex = -1;
    } else {
      try
      {
        m_ClassIndex = (Integer.parseInt(value) - 1);
      }
      catch (Exception e) {
        System.err.println("Error parsing '" + value + "'!");
      }
    }
  }
  




  public String getClassIndex()
  {
    if (m_ClassIndex == 0)
      return "first";
    if (m_ClassIndex == -2)
      return "last";
    if (m_ClassIndex == -1) {
      return "0";
    }
    return "" + (m_ClassIndex + 1);
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  








  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    Instances result = new Instances(inputFormat, 0);
    
    if (m_ClassIndex == 0) {
      result.setClassIndex(0);
    } else if (m_ClassIndex == -2) {
      result.setClassIndex(result.numAttributes() - 1);
    } else if (m_ClassIndex == -1) {
      result.setClassIndex(-1);
    } else {
      result.setClassIndex(m_ClassIndex);
    }
    return result;
  }
  






  protected Instance process(Instance instance)
    throws Exception
  {
    return instance;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new ClassAssigner(), args);
  }
}

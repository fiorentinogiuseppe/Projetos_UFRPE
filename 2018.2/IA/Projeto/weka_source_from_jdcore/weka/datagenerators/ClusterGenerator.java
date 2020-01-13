package weka.datagenerators;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.Range;
import weka.core.Utils;



















































public abstract class ClusterGenerator
  extends DataGenerator
{
  private static final long serialVersionUID = 6131722618472046365L;
  protected int m_NumAttributes;
  protected boolean m_ClassFlag = false;
  


  protected Range m_booleanCols;
  


  protected Range m_nominalCols;
  


  public ClusterGenerator()
  {
    setNumAttributes(defaultNumAttributes());
  }
  




  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe number of attributes (default " + defaultNumAttributes() + ").", "a", 1, "-a <num>"));
    



    result.addElement(new Option("\tClass Flag, if set, the cluster is listed in extra attribute.", "c", 0, "-c"));
    


    result.addElement(new Option("\tThe indices for boolean attributes.", "b", 1, "-b <range>"));
    


    result.addElement(new Option("\tThe indices for nominal attributes.", "m", 1, "-m <range>"));
    


    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('a', options);
    if (tmpStr.length() != 0) {
      setNumAttributes(Integer.parseInt(tmpStr));
    } else {
      setNumAttributes(defaultNumAttributes());
    }
    setClassFlag(Utils.getFlag('c', options));
    
    tmpStr = Utils.getOption('b', options);
    setBooleanIndices(tmpStr);
    m_booleanCols.setUpper(getNumAttributes());
    
    tmpStr = Utils.getOption('m', options);
    setNominalIndices(tmpStr);
    m_nominalCols.setUpper(getNumAttributes());
    

    tmpStr = checkIndices();
    if (tmpStr.length() > 0) {
      throw new IllegalArgumentException(tmpStr);
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-a");
    result.add("" + getNumAttributes());
    
    if (getClassFlag()) {
      result.add("-c");
    }
    if (!getBooleanCols().toString().equalsIgnoreCase("empty")) {
      result.add("-b");
      result.add("" + getBooleanCols());
    }
    
    if (!getNominalCols().toString().equalsIgnoreCase("empty")) {
      result.add("-m");
      result.add("" + getNominalCols());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected int defaultNumAttributes()
  {
    return 10;
  }
  



  public void setNumAttributes(int numAttributes)
  {
    m_NumAttributes = numAttributes;
    getBooleanCols().setUpper(getNumAttributes());
    getNominalCols().setUpper(getNumAttributes());
  }
  



  public int getNumAttributes()
  {
    return m_NumAttributes;
  }
  





  public String numAttributesTipText()
  {
    return "The number of attributes the generated data will contain.";
  }
  




  public void setClassFlag(boolean classFlag)
  {
    m_ClassFlag = classFlag;
  }
  



  public boolean getClassFlag()
  {
    return m_ClassFlag;
  }
  





  public String classFlagTipText()
  {
    return "If set to TRUE, lists the cluster as an extra attribute.";
  }
  







  public void setBooleanIndices(String rangeList)
  {
    m_booleanCols.setRanges(rangeList);
  }
  



  public void setBooleanCols(Range value)
  {
    m_booleanCols.setRanges(value.getRanges());
  }
  




  public Range getBooleanCols()
  {
    if (m_booleanCols == null) {
      m_booleanCols = new Range();
    }
    return m_booleanCols;
  }
  





  public String booleanColsTipText()
  {
    return "The range of attributes that are generated as boolean ones.";
  }
  







  public void setNominalIndices(String rangeList)
  {
    m_nominalCols.setRanges(rangeList);
  }
  



  public void setNominalCols(Range value)
  {
    m_nominalCols.setRanges(value.getRanges());
  }
  




  public Range getNominalCols()
  {
    if (m_nominalCols == null) {
      m_nominalCols = new Range();
    }
    return m_nominalCols;
  }
  





  public String nominalColsTipText()
  {
    return "The range of attributes to generate as nominal ones.";
  }
  




  protected String checkIndices()
  {
    for (int i = 1; i < getNumAttributes() + 1; i++) {
      m_booleanCols.isInRange(i);
      if ((m_booleanCols.isInRange(i)) && (m_nominalCols.isInRange(i))) {
        return "Error in attribute type: Attribute " + i + " is set boolean and nominal.";
      }
    }
    
    return "";
  }
}

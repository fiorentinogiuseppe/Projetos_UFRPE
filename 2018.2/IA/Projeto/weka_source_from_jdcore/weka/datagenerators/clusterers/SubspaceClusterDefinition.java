package weka.datagenerators.clusterers;

import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.datagenerators.ClusterDefinition;
import weka.datagenerators.ClusterGenerator;










































































































public class SubspaceClusterDefinition
  extends ClusterDefinition
{
  static final long serialVersionUID = 3135678125044007231L;
  protected int m_clustertype;
  protected int m_clustersubtype;
  protected int m_numClusterAttributes;
  protected int m_numInstances;
  protected int m_MinInstNum;
  protected int m_MaxInstNum;
  protected Range m_AttrIndexRange;
  protected boolean[] m_attributes;
  protected int[] m_attrIndices;
  protected double[] m_minValue;
  protected double[] m_maxValue;
  protected double[] m_meanValue;
  protected double[] m_stddevValue;
  
  public SubspaceClusterDefinition() {}
  
  public SubspaceClusterDefinition(ClusterGenerator parent)
  {
    super(parent);
  }
  




  protected void setDefaults()
    throws Exception
  {
    setClusterType(defaultClusterType());
    setClusterSubType(defaultClusterSubType());
    setMinInstNum(defaultMinInstNum());
    setMaxInstNum(defaultMaxInstNum());
    setAttrIndexRange(defaultAttrIndexRange());
    m_numClusterAttributes = 1;
    setValuesList(defaultValuesList());
  }
  






  public String globalInfo()
  {
    return "A single cluster for the SubspaceCluster datagenerator";
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tGenerates randomly distributed instances in the cluster.", "A", 1, "-A <range>"));
    


    result.addElement(new Option("\tGenerates uniformly distributed instances in the cluster.", "U", 1, "-U <range>"));
    


    result.addElement(new Option("\tGenerates gaussian distributed instances in the cluster.", "G", 1, "-G <range>"));
    


    result.addElement(new Option("\tThe attribute min/max (-A and -U) or mean/stddev (-G) for\n\tthe cluster.", "D", 1, "-D <num>,<num>"));
    



    result.addElement(new Option("\tThe range of number of instances per cluster (default " + defaultMinInstNum() + ".." + defaultMaxInstNum() + ").", "N", 1, "-N <num>..<num>"));
    



    result.addElement(new Option("\tUses integer instead of continuous values (default continuous).", "I", 0, "-I"));
    


    return result.elements();
  }
  














































  public void setOptions(String[] options)
    throws Exception
  {
    int typeCount = 0;
    String fromToStr = "";
    
    String tmpStr = Utils.getOption('A', options);
    if (tmpStr.length() != 0) {
      fromToStr = tmpStr;
      setClusterType(new SelectedTag(0, SubspaceCluster.TAGS_CLUSTERTYPE));
      
      typeCount++;
    }
    
    tmpStr = Utils.getOption('U', options);
    if (tmpStr.length() != 0) {
      fromToStr = tmpStr;
      setClusterType(new SelectedTag(1, SubspaceCluster.TAGS_CLUSTERTYPE));
      
      typeCount++;
    }
    
    tmpStr = Utils.getOption('G', options);
    if (tmpStr.length() != 0) {
      fromToStr = tmpStr;
      setClusterType(new SelectedTag(2, SubspaceCluster.TAGS_CLUSTERTYPE));
      
      typeCount++;
    }
    

    if (typeCount == 0) {
      setClusterType(new SelectedTag(0, SubspaceCluster.TAGS_CLUSTERTYPE));
    }
    else if (typeCount > 1) {
      throw new Exception("Only one cluster type can be specified!");
    }
    
    if (getParent() != null) {
      setAttrIndexRange(fromToStr);
    }
    
    tmpStr = Utils.getOption('D', options);
    if (isGaussian()) {
      if (tmpStr.length() != 0) {
        setMeanStddev(tmpStr);
      } else {
        setMeanStddev(defaultMeanStddev());
      }
      
    }
    else if (tmpStr.length() != 0) {
      setValuesList(tmpStr);
    } else {
      m_numClusterAttributes = 1;
      setValuesList(defaultValuesList());
    }
    

    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setInstNums(tmpStr);
    } else {
      setInstNums(defaultMinInstNum() + ".." + defaultMaxInstNum());
    }
    
    if (Utils.getFlag('I', options)) {
      setClusterSubType(new SelectedTag(1, SubspaceCluster.TAGS_CLUSTERSUBTYPE));
    }
    else {
      setClusterSubType(new SelectedTag(0, SubspaceCluster.TAGS_CLUSTERSUBTYPE));
    }
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (isRandom()) {
      if (getAttrIndexRange().length() > 0) {
        result.add("-A");
        result.add("" + getAttrIndexRange());
      }
      if (getValuesList().length() > 0) {
        result.add("-D");
        result.add("" + getValuesList());
      }
    }
    else if (isUniform()) {
      if (getAttrIndexRange().length() > 0) {
        result.add("-U");
        result.add("" + getAttrIndexRange());
      }
      if (getValuesList().length() > 0) {
        result.add("-D");
        result.add("" + getValuesList());
      }
    }
    else if (isGaussian()) {
      if (getAttrIndexRange().length() > 0) {
        result.add("-G");
        result.add("" + getAttrIndexRange());
      }
      if (getValuesList().length() > 0) {
        result.add("-D");
        result.add("" + getMeanStddev());
      }
    }
    
    result.add("-N");
    result.add("" + getInstNums());
    
    if (m_clustersubtype == 1) {
      result.add("-I");
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String attributesToString()
  {
    StringBuffer text = new StringBuffer();
    int j = 0;
    for (int i = 0; i < m_attributes.length; i++) {
      if (m_attributes[i] != 0) {
        if (isGaussian()) {
          text.append(" Attribute: " + i);
          text.append(" Mean: " + m_meanValue[j]);
          text.append(" StdDev: " + m_stddevValue[j] + "\n%");
        }
        else {
          text.append(" Attribute: " + i);
          text.append(" Range: " + m_minValue[j]);
          text.append(" - " + m_maxValue[j] + "\n%");
        }
        j++;
      }
    }
    return text.toString();
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    text.append("attributes " + attributesToString() + "\n");
    text.append("number of instances " + getInstNums());
    return text.toString();
  }
  




  public void setParent(SubspaceCluster parent)
  {
    super.setParent(parent);
    m_AttrIndexRange.setUpper(getParent().getNumAttributes());
  }
  




  protected String defaultAttrIndexRange()
  {
    return "1";
  }
  








  public void setAttrIndexRange(String rangeList)
  {
    m_numClusterAttributes = 0;
    if (m_AttrIndexRange == null) {
      m_AttrIndexRange = new Range();
    }
    m_AttrIndexRange.setRanges(rangeList);
    
    if (getParent() != null) {
      m_AttrIndexRange.setUpper(getParent().getNumAttributes());
      m_attributes = new boolean[getParent().getNumAttributes()];
      for (int i = 0; i < m_attributes.length; i++) {
        if (m_AttrIndexRange.isInRange(i)) {
          m_numClusterAttributes += 1;
          m_attributes[i] = true;
        }
        else {
          m_attributes[i] = false;
        }
      }
      

      m_attrIndices = new int[m_numClusterAttributes];
      int clusterI = -1;
      for (int i = 0; i < m_attributes.length; i++) {
        if (m_AttrIndexRange.isInRange(i)) {
          clusterI++;
          m_attrIndices[clusterI] = i;
        }
      }
    }
  }
  




  public String getAttrIndexRange()
  {
    return m_AttrIndexRange.getRanges();
  }
  





  public String attrIndexRangeTipText()
  {
    return "The attribute range(s).";
  }
  
  public boolean[] getAttributes() {
    return m_attributes;
  }
  
  public double[] getMinValue() {
    return m_minValue;
  }
  
  public double[] getMaxValue() {
    return m_maxValue;
  }
  
  public double[] getMeanValue() {
    return m_meanValue;
  }
  
  public double[] getStddevValue() {
    return m_stddevValue;
  }
  
  public int getNumInstances() {
    return m_numInstances;
  }
  




  protected SelectedTag defaultClusterType()
  {
    return new SelectedTag(0, SubspaceCluster.TAGS_CLUSTERTYPE);
  }
  






  public SelectedTag getClusterType()
  {
    return new SelectedTag(m_clustertype, SubspaceCluster.TAGS_CLUSTERTYPE);
  }
  





  public void setClusterType(SelectedTag value)
  {
    if (value.getTags() == SubspaceCluster.TAGS_CLUSTERTYPE) {
      m_clustertype = value.getSelectedTag().getID();
    }
  }
  





  public String clusterTypeTipText()
  {
    return "The type of cluster to use.";
  }
  




  protected SelectedTag defaultClusterSubType()
  {
    return new SelectedTag(0, SubspaceCluster.TAGS_CLUSTERSUBTYPE);
  }
  






  public SelectedTag getClusterSubType()
  {
    return new SelectedTag(m_clustersubtype, SubspaceCluster.TAGS_CLUSTERSUBTYPE);
  }
  






  public void setClusterSubType(SelectedTag value)
  {
    if (value.getTags() == SubspaceCluster.TAGS_CLUSTERSUBTYPE) {
      m_clustersubtype = value.getSelectedTag().getID();
    }
  }
  





  public String clusterSubTypeTipText()
  {
    return "The sub-type of cluster to use.";
  }
  




  public boolean isRandom()
  {
    return m_clustertype == 0;
  }
  




  public boolean isUniform()
  {
    return m_clustertype == 1;
  }
  




  public boolean isGaussian()
  {
    return m_clustertype == 2;
  }
  




  public boolean isContinuous()
  {
    return m_clustertype == 0;
  }
  




  public boolean isInteger()
  {
    return m_clustertype == 1;
  }
  





  protected void setInstNums(String fromTo)
  {
    int i = fromTo.indexOf("..");
    if (i == -1) {
      i = fromTo.length();
    }
    String from = fromTo.substring(0, i);
    m_MinInstNum = Integer.parseInt(from);
    if (i < fromTo.length()) {
      String to = fromTo.substring(i + 2, fromTo.length());
      m_MaxInstNum = Integer.parseInt(to);
    }
    else {
      m_MaxInstNum = m_MinInstNum;
    }
  }
  






  protected String getInstNums()
  {
    String text = new String("" + m_MinInstNum + ".." + m_MaxInstNum);
    return text;
  }
  





  protected String instNumsTipText()
  {
    return "The lower and upper boundary for the number of instances in this cluster.";
  }
  




  protected int defaultMinInstNum()
  {
    return 1;
  }
  




  public int getMinInstNum()
  {
    return m_MinInstNum;
  }
  




  public void setMinInstNum(int newMinInstNum)
  {
    m_MinInstNum = newMinInstNum;
  }
  





  public String minInstNumTipText()
  {
    return "The lower boundary for instances per cluster.";
  }
  




  protected int defaultMaxInstNum()
  {
    return 50;
  }
  




  public int getMaxInstNum()
  {
    return m_MaxInstNum;
  }
  




  public void setMaxInstNum(int newMaxInstNum)
  {
    m_MaxInstNum = newMaxInstNum;
  }
  





  public String maxInstNumTipText()
  {
    return "The upper boundary for instances per cluster.";
  }
  




  public void setNumInstances(Random r)
  {
    if (m_MaxInstNum > m_MinInstNum) {
      m_numInstances = ((int)(r.nextDouble() * (m_MaxInstNum - m_MinInstNum) + m_MinInstNum));
    }
    else {
      m_numInstances = m_MinInstNum;
    }
  }
  




  protected String defaultValuesList()
  {
    return "1,10";
  }
  





  public void setValuesList(String fromToList)
    throws Exception
  {
    m_minValue = new double[m_numClusterAttributes];
    m_maxValue = new double[m_numClusterAttributes];
    setValuesList(fromToList, m_minValue, m_maxValue, "D");
    SubspaceCluster parent = (SubspaceCluster)getParent();
    
    for (int i = 0; i < m_numClusterAttributes; i++) {
      if (m_minValue[i] > m_maxValue[i]) {
        throw new Exception("Min must be smaller than max.");
      }
      
      if (getParent() != null)
      {
        if (parent.isBoolean(m_attrIndices[i])) {
          parent.getNumValues()[m_attrIndices[i]] = 2;
          if (((m_minValue[i] != 0.0D) && (m_minValue[i] != 1.0D)) || ((m_maxValue[i] != 0.0D) && (m_maxValue[i] != 1.0D)))
          {
            throw new Exception("Ranges for boolean must be 0 or 1 only.");
          }
        }
        
        if (parent.isNominal(m_attrIndices[i]))
        {
          double rest = m_minValue[i] - Math.rint(m_minValue[i]);
          if (rest != 0.0D) {
            throw new Exception(" Ranges for nominal must be integer");
          }
          rest = m_maxValue[i] - Math.rint(m_maxValue[i]);
          if (rest != 0.0D) {
            throw new Exception("Ranges for nominal must be integer");
          }
          if (m_minValue[i] < 0.0D) {
            throw new Exception("Range for nominal must start with number 0.0 or higher");
          }
          
          if (m_maxValue[i] + 1.0D > parent.getNumValues()[m_attrIndices[i]])
          {

            parent.getNumValues()[m_attrIndices[i]] = ((int)m_maxValue[i] + 1);
          }
        }
      }
    }
  }
  





  public String getValuesList()
  {
    String result = "";
    
    if (m_minValue != null) {
      for (int i = 0; i < m_minValue.length; i++) {
        if (i > 0) {
          result = result + ",";
        }
        result = result + "" + m_minValue[i] + "," + m_maxValue[i];
      }
    }
    
    return result;
  }
  





  public String valuesListTipText()
  {
    return "The range for each each attribute as string.";
  }
  


  protected String defaultMeanStddev()
  {
    return "0,1.0";
  }
  





  public void setMeanStddev(String meanstddev)
    throws Exception
  {
    m_meanValue = new double[m_numClusterAttributes];
    m_stddevValue = new double[m_numClusterAttributes];
    setValuesList(meanstddev, m_meanValue, m_stddevValue, "D");
  }
  





  public String getMeanStddev()
  {
    String result = "";
    
    if (m_meanValue != null) {
      for (int i = 0; i < m_meanValue.length; i++) {
        if (i > 0) {
          result = result + ",";
        }
        result = result + "" + m_meanValue[i] + "," + m_stddevValue[i];
      }
    }
    
    return result;
  }
  





  public String meanStddevTipText()
  {
    return "The mean and stddev, in case of gaussian.";
  }
  













  public void setValuesList(String fromToList, double[] first, double[] second, String optionLetter)
    throws Exception
  {
    StringTokenizer tok = new StringTokenizer(fromToList, ",");
    if (tok.countTokens() != first.length + second.length) {
      throw new Exception("Wrong number of values for option '-" + optionLetter + "'.");
    }
    

    int index = 0;
    while (tok.hasMoreTokens()) {
      first[index] = Double.parseDouble(tok.nextToken());
      second[index] = Double.parseDouble(tok.nextToken());
      index++;
    }
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

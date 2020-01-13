package weka.core;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.neighboursearch.PerformanceStats;











































public abstract class NormalizableDistance
  implements DistanceFunction, OptionHandler, Serializable, RevisionHandler
{
  public static final int R_MIN = 0;
  public static final int R_MAX = 1;
  public static final int R_WIDTH = 2;
  protected Instances m_Data = null;
  

  protected boolean m_DontNormalize = false;
  

  protected double[][] m_Ranges;
  

  protected Range m_AttributeIndices = new Range("first-last");
  

  protected boolean[] m_ActiveIndices;
  

  protected boolean m_Validated;
  


  public NormalizableDistance()
  {
    invalidate();
  }
  




  public NormalizableDistance(Instances data)
  {
    setInstances(data);
  }
  






  public abstract String globalInfo();
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.add(new Option("\tTurns off the normalization of attribute \n\tvalues in distance calculation.", "D", 0, "-D"));
    

    result.addElement(new Option("\tSpecifies list of columns to used in the calculation of the \n\tdistance. 'first' and 'last' are valid indices.\n\t(default: first-last)", "R", 1, "-R <col1,col2-col4,...>"));
    



    result.addElement(new Option("\tInvert matching sense of column indices.", "V", 0, "-V"));
    

    return result.elements();
  }
  







  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (getDontNormalize()) {
      result.add("-D");
    }
    
    result.add("-R");
    result.add(getAttributeIndices());
    
    if (getInvertSelection()) {
      result.add("-V");
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  







  public void setOptions(String[] options)
    throws Exception
  {
    setDontNormalize(Utils.getFlag('D', options));
    
    String tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setAttributeIndices(tmpStr);
    } else {
      setAttributeIndices("first-last");
    }
    
    setInvertSelection(Utils.getFlag('V', options));
  }
  





  public String dontNormalizeTipText()
  {
    return "Whether if the normalization of attributes should be turned off for distance calculation (Default: false i.e. attribute values are normalized). ";
  }
  







  public void setDontNormalize(boolean dontNormalize)
  {
    m_DontNormalize = dontNormalize;
    invalidate();
  }
  





  public boolean getDontNormalize()
  {
    return m_DontNormalize;
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  










  public void setAttributeIndices(String value)
  {
    m_AttributeIndices.setRanges(value);
    invalidate();
  }
  





  public String getAttributeIndices()
  {
    return m_AttributeIndices.getRanges();
  }
  





  public String invertSelectionTipText()
  {
    return "Set attribute selection mode. If false, only selected attributes in the range will be used in the distance calculation; if true, only non-selected attributes will be used for the calculation.";
  }
  







  public void setInvertSelection(boolean value)
  {
    m_AttributeIndices.setInvert(value);
    invalidate();
  }
  





  public boolean getInvertSelection()
  {
    return m_AttributeIndices.getInvert();
  }
  


  protected void invalidate()
  {
    m_Validated = false;
  }
  


  protected void validate()
  {
    if (!m_Validated) {
      initialize();
      m_Validated = true;
    }
  }
  


  protected void initialize()
  {
    initializeAttributeIndices();
    initializeRanges();
  }
  


  protected void initializeAttributeIndices()
  {
    m_AttributeIndices.setUpper(m_Data.numAttributes() - 1);
    m_ActiveIndices = new boolean[m_Data.numAttributes()];
    for (int i = 0; i < m_ActiveIndices.length; i++) {
      m_ActiveIndices[i] = m_AttributeIndices.isInRange(i);
    }
  }
  





  public void setInstances(Instances insts)
  {
    m_Data = insts;
    invalidate();
  }
  





  public Instances getInstances()
  {
    return m_Data;
  }
  






  public void postProcessDistances(double[] distances) {}
  






  public void update(Instance ins)
  {
    validate();
    
    m_Ranges = updateRanges(ins, m_Ranges);
  }
  







  public double distance(Instance first, Instance second)
  {
    return distance(first, second, null);
  }
  








  public double distance(Instance first, Instance second, PerformanceStats stats)
  {
    return distance(first, second, Double.POSITIVE_INFINITY, stats);
  }
  















  public double distance(Instance first, Instance second, double cutOffValue)
  {
    return distance(first, second, cutOffValue, null);
  }
  

















  public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats)
  {
    double distance = 0.0D;
    
    int firstNumValues = first.numValues();
    int secondNumValues = second.numValues();
    int numAttributes = m_Data.numAttributes();
    int classIndex = m_Data.classIndex();
    
    validate();
    
    int p1 = 0; for (int p2 = 0; (p1 < firstNumValues) || (p2 < secondNumValues);) { int firstI;
      int firstI; if (p1 >= firstNumValues) {
        firstI = numAttributes;
      } else
        firstI = first.index(p1);
      int secondI;
      int secondI;
      if (p2 >= secondNumValues) {
        secondI = numAttributes;
      } else {
        secondI = second.index(p2);
      }
      
      if (firstI == classIndex) {
        p1++;

      }
      else if ((firstI < numAttributes) && (m_ActiveIndices[firstI] == 0)) {
        p1++;


      }
      else if (secondI == classIndex) {
        p2++;

      }
      else if ((secondI < numAttributes) && (m_ActiveIndices[secondI] == 0)) {
        p2++;
      }
      else
      {
        double diff;
        
        if (firstI == secondI) {
          double diff = difference(firstI, first.valueSparse(p1), second.valueSparse(p2));
          p1++;
          p2++;
        } else if (firstI > secondI) {
          double diff = difference(secondI, 0.0D, second.valueSparse(p2));
          p2++;
        } else {
          diff = difference(firstI, first.valueSparse(p1), 0.0D);
          p1++;
        }
        if (stats != null) {
          stats.incrCoordCount();
        }
        
        distance = updateDistance(distance, diff);
        if (distance > cutOffValue) {
          return Double.POSITIVE_INFINITY;
        }
      }
    }
    return distance;
  }
  








  protected abstract double updateDistance(double paramDouble1, double paramDouble2);
  








  protected double norm(double x, int i)
  {
    if ((Double.isNaN(m_Ranges[i][0])) || (m_Ranges[i][1] == m_Ranges[i][0]))
    {
      return 0.0D;
    }
    return (x - m_Ranges[i][0]) / m_Ranges[i][2];
  }
  








  protected double difference(int index, double val1, double val2)
  {
    switch (m_Data.attribute(index).type()) {
    case 1: 
      if ((Instance.isMissingValue(val1)) || (Instance.isMissingValue(val2)) || ((int)val1 != (int)val2))
      {
        return 1.0D;
      }
      return 0.0D;
    

    case 0: 
      if ((Instance.isMissingValue(val1)) || (Instance.isMissingValue(val2))) {
        if ((Instance.isMissingValue(val1)) && (Instance.isMissingValue(val2))) {
          if (!m_DontNormalize) {
            return 1.0D;
          }
          return m_Ranges[index][1] - m_Ranges[index][0];
        }
        double diff;
        double diff;
        if (Instance.isMissingValue(val2)) {
          diff = !m_DontNormalize ? norm(val1, index) : val1;
        } else {
          diff = !m_DontNormalize ? norm(val2, index) : val2;
        }
        if ((!m_DontNormalize) && (diff < 0.5D)) {
          diff = 1.0D - diff;
        } else if (m_DontNormalize) {
          if (m_Ranges[index][1] - diff > diff - m_Ranges[index][0]) {
            return m_Ranges[index][1] - diff;
          }
          return diff - m_Ranges[index][0];
        }
        
        return diff;
      }
      
      return !m_DontNormalize ? norm(val1, index) - norm(val2, index) : val1 - val2;
    }
    
    

    return 0.0D;
  }
  





  public double[][] initializeRanges()
  {
    if (m_Data == null) {
      m_Ranges = ((double[][])null);
      return m_Ranges;
    }
    
    int numAtt = m_Data.numAttributes();
    double[][] ranges = new double[numAtt][3];
    
    if (m_Data.numInstances() <= 0) {
      initializeRangesEmpty(numAtt, ranges);
      m_Ranges = ranges;
      return m_Ranges;
    }
    
    updateRangesFirst(m_Data.instance(0), numAtt, ranges);
    


    for (int i = 1; i < m_Data.numInstances(); i++) {
      updateRanges(m_Data.instance(i), numAtt, ranges);
    }
    
    m_Ranges = ranges;
    
    return m_Ranges;
  }
  








  public void updateRangesFirst(Instance instance, int numAtt, double[][] ranges)
  {
    for (int j = 0; j < numAtt; j++) {
      if (!instance.isMissing(j)) {
        ranges[j][0] = instance.value(j);
        ranges[j][1] = instance.value(j);
        ranges[j][2] = 0.0D;
      } else {
        ranges[j][0] = Double.POSITIVE_INFINITY;
        ranges[j][1] = Double.NEGATIVE_INFINITY;
        ranges[j][2] = Double.POSITIVE_INFINITY;
      }
    }
  }
  








  public void updateRanges(Instance instance, int numAtt, double[][] ranges)
  {
    for (int j = 0; j < numAtt; j++) {
      double value = instance.value(j);
      if (!instance.isMissing(j)) {
        if (value < ranges[j][0]) {
          ranges[j][0] = value;
          ranges[j][2] = (ranges[j][1] - ranges[j][0]);
          if (value > ranges[j][1]) {
            ranges[j][1] = value;
            ranges[j][2] = (ranges[j][1] - ranges[j][0]);
          }
        }
        else if (value > ranges[j][1]) {
          ranges[j][1] = value;
          ranges[j][2] = (ranges[j][1] - ranges[j][0]);
        }
      }
    }
  }
  






  public void initializeRangesEmpty(int numAtt, double[][] ranges)
  {
    for (int j = 0; j < numAtt; j++) {
      ranges[j][0] = Double.POSITIVE_INFINITY;
      ranges[j][1] = Double.NEGATIVE_INFINITY;
      ranges[j][2] = Double.POSITIVE_INFINITY;
    }
  }
  







  public double[][] updateRanges(Instance instance, double[][] ranges)
  {
    for (int j = 0; j < ranges.length; j++) {
      double value = instance.value(j);
      if (!instance.isMissing(j)) {
        if (value < ranges[j][0]) {
          ranges[j][0] = value;
          ranges[j][2] = (ranges[j][1] - ranges[j][0]);
        }
        else if (instance.value(j) > ranges[j][1]) {
          ranges[j][1] = value;
          ranges[j][2] = (ranges[j][1] - ranges[j][0]);
        }
      }
    }
    

    return ranges;
  }
  






  public double[][] initializeRanges(int[] instList)
    throws Exception
  {
    if (m_Data == null) {
      throw new Exception("No instances supplied.");
    }
    
    int numAtt = m_Data.numAttributes();
    double[][] ranges = new double[numAtt][3];
    
    if (m_Data.numInstances() <= 0) {
      initializeRangesEmpty(numAtt, ranges);
      return ranges;
    }
    
    updateRangesFirst(m_Data.instance(instList[0]), numAtt, ranges);
    
    for (int i = 1; i < instList.length; i++) {
      updateRanges(m_Data.instance(instList[i]), numAtt, ranges);
    }
    
    return ranges;
  }
  











  public double[][] initializeRanges(int[] instList, int startIdx, int endIdx)
    throws Exception
  {
    if (m_Data == null) {
      throw new Exception("No instances supplied.");
    }
    
    int numAtt = m_Data.numAttributes();
    double[][] ranges = new double[numAtt][3];
    
    if (m_Data.numInstances() <= 0) {
      initializeRangesEmpty(numAtt, ranges);
      return ranges;
    }
    
    updateRangesFirst(m_Data.instance(instList[startIdx]), numAtt, ranges);
    
    for (int i = startIdx + 1; i <= endIdx; i++) {
      updateRanges(m_Data.instance(instList[i]), numAtt, ranges);
    }
    

    return ranges;
  }
  




  public void updateRanges(Instance instance)
  {
    validate();
    
    m_Ranges = updateRanges(instance, m_Ranges);
  }
  






  public boolean inRanges(Instance instance, double[][] ranges)
  {
    boolean isIn = true;
    

    for (int j = 0; (isIn) && (j < ranges.length); j++) {
      if (!instance.isMissing(j)) {
        double value = instance.value(j);
        isIn = value <= ranges[j][1];
        if (isIn) {
          isIn = value >= ranges[j][0];
        }
      }
    }
    
    return isIn;
  }
  



  public void clean()
  {
    m_Data = new Instances(m_Data, 0);
  }
  




  public boolean rangesSet()
  {
    return m_Ranges != null;
  }
  




  public double[][] getRanges()
    throws Exception
  {
    validate();
    
    if (m_Ranges == null) {
      throw new Exception("Ranges not yet set.");
    }
    
    return m_Ranges;
  }
  





  public String toString()
  {
    return "";
  }
}

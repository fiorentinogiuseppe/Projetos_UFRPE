package weka.filters.unsupervised.instance;

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
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SingleIndex;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;









































































public class RemoveWithValues
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = 4752870193679263361L;
  private SingleIndex m_AttIndex = new SingleIndex("last");
  

  protected Range m_Values;
  

  protected double m_Value = 0.0D;
  

  protected boolean m_MatchMissingValues = false;
  

  protected boolean m_ModifyHeader = false;
  

  protected int[] m_NominalMapping;
  

  protected boolean m_dontFilterAfterFirstBatch = false;
  




  public String globalInfo()
  {
    return "Filters instances according to the value of an attribute.";
  }
  

  public RemoveWithValues()
  {
    m_Values = new Range("first-last");
    m_Values.setInvert(true);
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tChoose attribute to be used for selection.", "C", 1, "-C <num>"));
    

    newVector.addElement(new Option("\tNumeric value to be used for selection on numeric\n\tattribute.\n\tInstances with values smaller than given value will\n\tbe selected. (default 0)", "S", 1, "-S <num>"));
    




    newVector.addElement(new Option("\tRange of label indices to be used for selection on\n\tnominal attribute.\n\tFirst and last are valid indexes. (default all values)", "L", 1, "-L <index1,index2-index4,...>"));
    



    newVector.addElement(new Option("\tMissing values count as a match. This setting is\n\tindependent of the -V option.\n\t(default missing values don't match)", "M", 0, "-M"));
    



    newVector.addElement(new Option("\tInvert matching sense.", "V", 0, "-V"));
    

    newVector.addElement(new Option("\tWhen selecting on nominal attributes, removes header\n\treferences to excluded values.", "H", 0, "-H"));
    


    newVector.addElement(new Option("\tDo not apply the filter to instances that arrive after the first\n\t(training) batch. The default is to apply the filter (i.e.\n\tthe filter may not return an instance if it matches the remove criteria)", "F", 0, "-F"));
    




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
    
    String splitPoint = Utils.getOption('S', options);
    if (splitPoint.length() != 0) {
      setSplitPoint(new Double(splitPoint).doubleValue());
    } else {
      setSplitPoint(0.0D);
    }
    
    String convertList = Utils.getOption('L', options);
    if (convertList.length() != 0) {
      setNominalIndices(convertList);
    } else {
      setNominalIndices("first-last");
    }
    setInvertSelection(Utils.getFlag('V', options));
    setMatchMissingValues(Utils.getFlag('M', options));
    setModifyHeader(Utils.getFlag('H', options));
    setDontFilterAfterFirstBatch(Utils.getFlag('F', options));
    

    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[10];
    int current = 0;
    
    options[(current++)] = "-S";options[(current++)] = ("" + getSplitPoint());
    options[(current++)] = "-C";
    options[(current++)] = ("" + getAttributeIndex());
    if (!getNominalIndices().equals("")) {
      options[(current++)] = "-L";options[(current++)] = getNominalIndices();
    }
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    if (getMatchMissingValues()) {
      options[(current++)] = "-M";
    }
    if (getModifyHeader()) {
      options[(current++)] = "-H";
    }
    if (getDontFilterAfterFirstBatch()) {
      options[(current++)] = "-F";
    }
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
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
    if ((!isNumeric()) && (!isNominal())) {
      throw new UnsupportedAttributeTypeException("Can only handle numeric or nominal attributes.");
    }
    
    m_Values.setUpper(instanceInfo.attribute(m_AttIndex.getIndex()).numValues() - 1);
    if ((isNominal()) && (m_ModifyHeader)) {
      instanceInfo = new Instances(instanceInfo, 0);
      Attribute oldAtt = instanceInfo.attribute(m_AttIndex.getIndex());
      int[] selection = m_Values.getSelection();
      FastVector newVals = new FastVector();
      for (int i = 0; i < selection.length; i++) {
        newVals.addElement(oldAtt.value(selection[i]));
      }
      instanceInfo.deleteAttributeAt(m_AttIndex.getIndex());
      Attribute newAtt = new Attribute(oldAtt.name(), newVals);
      newAtt.setWeight(oldAtt.weight());
      instanceInfo.insertAttributeAt(newAtt, m_AttIndex.getIndex());
      
      m_NominalMapping = new int[oldAtt.numValues()];
      for (int i = 0; i < m_NominalMapping.length; i++) {
        boolean found = false;
        for (int j = 0; j < selection.length; j++) {
          if (selection[j] == i) {
            m_NominalMapping[i] = j;
            found = true;
            break;
          }
        }
        if (!found) {
          m_NominalMapping[i] = -1;
        }
      }
    }
    setOutputFormat(instanceInfo);
    return true;
  }
  










  public boolean input(Instance instance)
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    
    if ((isFirstBatchDone()) && (m_dontFilterAfterFirstBatch)) {
      push((Instance)instance.copy());
      return true;
    }
    
    if (instance.isMissing(m_AttIndex.getIndex())) {
      if (!getMatchMissingValues()) {
        push((Instance)instance.copy());
        return true;
      }
      return false;
    }
    
    if (isNumeric()) {
      if (!m_Values.getInvert()) {
        if (instance.value(m_AttIndex.getIndex()) < m_Value) {
          push((Instance)instance.copy());
          return true;
        }
      }
      else if (instance.value(m_AttIndex.getIndex()) >= m_Value) {
        push((Instance)instance.copy());
        return true;
      }
    }
    
    if ((isNominal()) && 
      (m_Values.isInRange((int)instance.value(m_AttIndex.getIndex())))) {
      Instance temp = (Instance)instance.copy();
      if (getModifyHeader()) {
        temp.setValue(m_AttIndex.getIndex(), m_NominalMapping[((int)instance.value(m_AttIndex.getIndex()))]);
      }
      
      push(temp);
      return true;
    }
    
    return false;
  }
  





  public boolean isNominal()
  {
    if (getInputFormat() == null) {
      return false;
    }
    return getInputFormat().attribute(m_AttIndex.getIndex()).isNominal();
  }
  






  public boolean isNumeric()
  {
    if (getInputFormat() == null) {
      return false;
    }
    return getInputFormat().attribute(m_AttIndex.getIndex()).isNumeric();
  }
  





  public String modifyHeaderTipText()
  {
    return "When selecting on nominal attributes, removes header references to excluded values.";
  }
  







  public boolean getModifyHeader()
  {
    return m_ModifyHeader;
  }
  






  public void setModifyHeader(boolean newModifyHeader)
  {
    m_ModifyHeader = newModifyHeader;
  }
  




  public String attributeIndexTipText()
  {
    return "Choose attribute to be used for selection (default last).";
  }
  





  public String getAttributeIndex()
  {
    return m_AttIndex.getSingleIndex();
  }
  





  public void setAttributeIndex(String attIndex)
  {
    m_AttIndex.setSingleIndex(attIndex);
  }
  




  public String splitPointTipText()
  {
    return "Numeric value to be used for selection on numeric attribute. Instances with values smaller than given value will be selected.";
  }
  






  public double getSplitPoint()
  {
    return m_Value;
  }
  





  public void setSplitPoint(double value)
  {
    m_Value = value;
  }
  




  public String matchMissingValuesTipText()
  {
    return "Missing values count as a match. This setting is independent of the invertSelection option.";
  }
  






  public boolean getMatchMissingValues()
  {
    return m_MatchMissingValues;
  }
  





  public void setMatchMissingValues(boolean newMatchMissingValues)
  {
    m_MatchMissingValues = newMatchMissingValues;
  }
  




  public String invertSelectionTipText()
  {
    return "Invert matching sense.";
  }
  





  public boolean getInvertSelection()
  {
    return !m_Values.getInvert();
  }
  






  public void setInvertSelection(boolean invert)
  {
    m_Values.setInvert(!invert);
  }
  




  public String nominalIndicesTipText()
  {
    return "Range of label indices to be used for selection on nominal attribute. First and last are valid indexes.";
  }
  






  public String getNominalIndices()
  {
    return m_Values.getRanges();
  }
  







  public void setNominalIndices(String rangeList)
  {
    m_Values.setRanges(rangeList);
  }
  










  public void setDontFilterAfterFirstBatch(boolean b)
  {
    m_dontFilterAfterFirstBatch = b;
  }
  










  public boolean getDontFilterAfterFirstBatch()
  {
    return m_dontFilterAfterFirstBatch;
  }
  





  public String dontFilterAfterFirstBatchTipText()
  {
    return "Whether to apply the filtering process to instances that are input after the first (training) batch. The default is false so instances in subsequent batches can potentially get 'consumed' by the filter.";
  }
  











  public void setNominalIndicesArr(int[] values)
  {
    String rangeList = "";
    for (int i = 0; i < values.length; i++) {
      if (i == 0) {
        rangeList = "" + (values[i] + 1);
      } else {
        rangeList = rangeList + "," + (values[i] + 1);
      }
    }
    setNominalIndices(rangeList);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8593 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new RemoveWithValues(), argv);
  }
}

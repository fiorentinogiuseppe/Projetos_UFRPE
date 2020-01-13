package weka.filters.unsupervised.instance;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
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
import weka.filters.UnsupervisedFilter;




























































public class RemoveFrequentValues
  extends Filter
  implements OptionHandler, UnsupervisedFilter
{
  static final long serialVersionUID = -2447432930070059511L;
  private SingleIndex m_AttIndex = new SingleIndex("last");
  

  protected int m_NumValues = 2;
  

  protected boolean m_LeastValues = false;
  

  protected boolean m_Invert = false;
  

  protected boolean m_ModifyHeader = false;
  

  protected int[] m_NominalMapping;
  

  protected HashSet m_Values = null;
  

  public RemoveFrequentValues() {}
  

  public String globalInfo()
  {
    return "Determines which values (frequent or infrequent ones) of an (nominal) attribute are retained and filters the instances accordingly. In case of values with the same frequency, they are kept in the way they appear in the original instances object. E.g. if you have the values \"1,2,3,4\" with the frequencies \"10,5,5,3\" and you chose to keep the 2 most common values, the values \"1,2\" would be returned, since the value \"2\" comes before \"3\", even though they have the same frequency.";
  }
  












  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tChoose attribute to be used for selection.", "C", 1, "-C <num>"));
    

    newVector.addElement(new Option("\tNumber of values to retain for the sepcified attribute, \n\ti.e. the ones with the most instances (default 2).", "N", 1, "-N <num>"));
    


    newVector.addElement(new Option("\tInstead of values with the most instances the ones with the \n\tleast are retained.\n", "L", 0, "-L"));
    


    newVector.addElement(new Option("\tWhen selecting on nominal attributes, removes header\n\treferences to excluded values.", "H", 0, "-H"));
    


    newVector.addElement(new Option("\tInvert matching sense.", "V", 0, "-V"));
    


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
    
    String numValues = Utils.getOption('N', options);
    if (numValues.length() != 0) {
      setNumValues(Integer.parseInt(numValues));
    } else {
      setNumValues(2);
    }
    
    setUseLeastValues(Utils.getFlag('L', options));
    
    setModifyHeader(Utils.getFlag('H', options));
    
    setInvertSelection(Utils.getFlag('V', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[7];
    int current = 0;
    
    options[(current++)] = "-C";
    options[(current++)] = ("" + getAttributeIndex());
    options[(current++)] = "-N";
    options[(current++)] = ("" + getNumValues());
    if (getUseLeastValues()) {
      options[(current++)] = "-H";
    }
    if (getModifyHeader()) {
      options[(current++)] = "-H";
    }
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
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
  





  public String numValuesTipText()
  {
    return "The number of values to retain.";
  }
  




  public int getNumValues()
  {
    return m_NumValues;
  }
  




  public void setNumValues(int numValues)
  {
    m_NumValues = numValues;
  }
  





  public String useLeastValuesTipText()
  {
    return "Retains values with least instance instead of most.";
  }
  




  public boolean getUseLeastValues()
  {
    return m_LeastValues;
  }
  




  public void setUseLeastValues(boolean leastValues)
  {
    m_LeastValues = leastValues;
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
  





  public String invertSelectionTipText()
  {
    return "Invert matching sense.";
  }
  




  public boolean getInvertSelection()
  {
    return m_Invert;
  }
  





  public void setInvertSelection(boolean invert)
  {
    m_Invert = invert;
  }
  




  public boolean isNominal()
  {
    if (getInputFormat() == null) {
      return false;
    }
    return getInputFormat().attribute(m_AttIndex.getIndex()).isNominal();
  }
  













  public void determineValues(Instances inst)
  {
    m_AttIndex.setUpper(inst.numAttributes() - 1);
    int attIdx = m_AttIndex.getIndex();
    

    m_Values = new HashSet();
    
    if (inst == null) {
      return;
    }
    
    AttributeStats stats = inst.attributeStats(attIdx);
    int count; int count; if (m_Invert) {
      count = nominalCounts.length - m_NumValues;
    } else {
      count = m_NumValues;
    }
    if (count < 1)
      count = 1;
    if (count > nominalCounts.length) {
      count = nominalCounts.length;
    }
    
    Arrays.sort(nominalCounts);
    int max; int min; int max; if (m_LeastValues) {
      int min = nominalCounts[0];
      max = nominalCounts[(count - 1)];
    }
    else {
      min = nominalCounts[(nominalCounts.length - 1 - count + 1)];
      max = nominalCounts[(nominalCounts.length - 1)];
    }
    

    stats = inst.attributeStats(attIdx);
    for (int i = 0; i < nominalCounts.length; i++) {
      if ((nominalCounts[i] >= min) && (nominalCounts[i] <= max) && (m_Values.size() < count)) {
        m_Values.add(inst.attribute(attIdx).value(i));
      }
    }
  }
  





  protected Instances modifyHeader(Instances instanceInfo)
  {
    instanceInfo = new Instances(getInputFormat(), 0);
    Attribute oldAtt = instanceInfo.attribute(m_AttIndex.getIndex());
    int[] selection = new int[m_Values.size()];
    Iterator iter = m_Values.iterator();
    int i = 0;
    while (iter.hasNext()) {
      selection[i] = oldAtt.indexOfValue(iter.next().toString());
      i++;
    }
    FastVector newVals = new FastVector();
    for (i = 0; i < selection.length; i++) {
      newVals.addElement(oldAtt.value(selection[i]));
    }
    instanceInfo.deleteAttributeAt(m_AttIndex.getIndex());
    Attribute newAtt = new Attribute(oldAtt.name(), newVals);
    newAtt.setWeight(oldAtt.weight());
    instanceInfo.insertAttributeAt(newAtt, m_AttIndex.getIndex());
    
    m_NominalMapping = new int[oldAtt.numValues()];
    for (i = 0; i < m_NominalMapping.length; i++) {
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
    
    return instanceInfo;
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
    
    if (!isNominal()) {
      throw new UnsupportedAttributeTypeException("Can only handle nominal attributes.");
    }
    m_Values = null;
    
    return false;
  }
  








  protected void setOutputFormat()
  {
    if (m_Values == null) {
      setOutputFormat(null); return;
    }
    
    Instances instances;
    Instances instances;
    if (getModifyHeader()) {
      instances = modifyHeader(getInputFormat());
    } else
      instances = new Instances(getInputFormat(), 0);
    setOutputFormat(instances);
    


    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      Instance instance = getInputFormat().instance(i);
      if (instance.isMissing(m_AttIndex.getIndex())) {
        push(instance);

      }
      else if (m_Values.contains(instance.stringValue(m_AttIndex.getIndex()))) {
        if (getModifyHeader()) {
          instance.setValue(m_AttIndex.getIndex(), m_NominalMapping[((int)instance.value(m_AttIndex.getIndex()))]);
        }
        
        push(instance);
      }
    }
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
    
    if (isFirstBatchDone()) {
      push(instance);
      return true;
    }
    
    bufferInput(instance);
    return false;
  }
  








  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    

    if (m_Values == null) {
      determineValues(getInputFormat());
      setOutputFormat();
    }
    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    return numPendingOutput() != 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8972 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new RemoveFrequentValues(), argv);
  }
}

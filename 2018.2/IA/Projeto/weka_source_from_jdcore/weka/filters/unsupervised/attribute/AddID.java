package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SingleIndex;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;

















































public class AddID
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 4734383199819293390L;
  protected SingleIndex m_Index = new SingleIndex("first");
  

  protected String m_Name = "ID";
  

  protected int m_Counter = -1;
  


  public AddID() {}
  

  public String globalInfo()
  {
    return "An instance filter that adds an ID attribute to the dataset. The new attribute contains a unique ID for each instance.\nNote: The ID is not reset for the second batch of files (using -b and -r and -s).";
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSpecify where to insert the ID. First and last\n\tare valid indexes.(default first)", "C", 1, "-C <index>"));
    



    result.addElement(new Option("\tName of the new attribute.\n\t(default = 'ID')", "N", 1, "-N <name>"));
    



    return result.elements();
  }
  




















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      m_Index.setSingleIndex(tmpStr);
    } else {
      m_Index.setSingleIndex("first");
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      m_Name = tmpStr;
    } else {
      m_Name = "ID";
    }
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-C");
    result.add(getIDIndex());
    
    result.add("-N");
    result.add(getAttributeName());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String attributeNameTipText()
  {
    return "Set the new attribute's name.";
  }
  




  public String getAttributeName()
  {
    return m_Name;
  }
  




  public void setAttributeName(String value)
  {
    m_Name = value;
  }
  





  public String IDIndexTipText()
  {
    return "The position (starting from 1) where the attribute will be inserted (first and last are valid indices).";
  }
  






  public String getIDIndex()
  {
    return m_Index.getSingleIndex();
  }
  




  public void setIDIndex(String value)
  {
    m_Index.setSingleIndex(value);
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
    
    m_Counter = -1;
    m_Index.setUpper(instanceInfo.numAttributes());
    Instances outputFormat = new Instances(instanceInfo, 0);
    Attribute newAttribute = new Attribute(m_Name);
    
    if ((m_Index.getIndex() < 0) || (m_Index.getIndex() > getInputFormat().numAttributes()))
    {
      throw new IllegalArgumentException("Index out of range");
    }
    outputFormat.insertAttributeAt(newAttribute, m_Index.getIndex());
    setOutputFormat(outputFormat);
    
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
    
    if (!isFirstBatchDone()) {
      bufferInput(instance);
      return false;
    }
    
    convertInstance(instance);
    return true;
  }
  








  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (!isFirstBatchDone()) {
      m_Counter = 0;
      

      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        convertInstance(getInputFormat().instance(i));
      }
    }
    
    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    return numPendingOutput() != 0;
  }
  







  protected void convertInstance(Instance instance)
  {
    m_Counter += 1;
    
    try
    {
      Instance inst = (Instance)instance.copy();
      

      copyValues(inst, true, inst.dataset(), getOutputFormat());
      

      inst.setDataset(null);
      inst.insertAttributeAt(m_Index.getIndex());
      inst.setValue(m_Index.getIndex(), m_Counter);
      inst.setDataset(getOutputFormat());
      
      push(inst);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new AddID(), args);
  }
}

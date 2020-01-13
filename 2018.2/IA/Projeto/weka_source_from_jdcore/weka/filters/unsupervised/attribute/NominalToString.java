package weka.filters.unsupervised.attribute;

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
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;














































public class NominalToString
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 8655492378380068939L;
  private Range m_AttIndex = new Range("last");
  


  public NominalToString() {}
  

  public String globalInfo()
  {
    return "Converts a nominal attribute (that is, one with a set number of values) to string (i.e. unspecified number of values).";
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
    




    return false;
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
    
    if (isOutputFormatDefined()) {
      Instance newInstance = (Instance)instance.copy();
      push(newInstance);
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
    if (!isOutputFormatDefined()) {
      setOutputFormat();
      

      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        push((Instance)getInputFormat().instance(i).copy());
      }
    }
    flushInput();
    m_NewBatch = true;
    
    return numPendingOutput() != 0;
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSets the range of attributes to convert (default last).", "C", 1, "-C <col>"));
    


    return result.elements();
  }
  















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setAttributeIndexes(tmpStr);
    } else {
      setAttributeIndexes("last");
    }
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-C");
    result.add("" + getAttributeIndexes());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String attributeIndexesTipText()
  {
    return "Sets a range attributes to process. Any non-nominal attributes in the range are left untouched (\"first\" and \"last\" are valid values)";
  }
  






  public String getAttributeIndexes()
  {
    return m_AttIndex.getRanges();
  }
  





  public void setAttributeIndexes(String attIndex)
  {
    m_AttIndex.setRanges(attIndex);
  }
  








  private void setOutputFormat()
  {
    FastVector newAtts = new FastVector(getInputFormat().numAttributes());
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      
      if ((!att.isNominal()) || (!m_AttIndex.isInRange(j))) {
        newAtts.addElement(att);
      } else {
        Attribute newAtt = new Attribute(att.name(), (FastVector)null);
        newAtt.setWeight(getInputFormat().attribute(j).weight());
        newAtts.addElement(newAtt);
      }
    }
    

    Instances newData = new Instances(getInputFormat().relationName(), newAtts, 0);
    newData.setClassIndex(getInputFormat().classIndex());
    
    setOutputFormat(newData);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8573 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new NominalToString(), args);
  }
}

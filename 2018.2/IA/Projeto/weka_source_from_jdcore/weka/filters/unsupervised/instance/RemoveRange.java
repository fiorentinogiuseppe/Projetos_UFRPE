package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;



















































public class RemoveRange
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = -3064641215340828695L;
  private Range m_Range = new Range("first-last");
  


  public RemoveRange() {}
  

  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tSpecifies list of instances to select. First and last\n\tare valid indexes. (required)\n", "R", 1, "-R <inst1,inst2-inst4,...>"));
    



    newVector.addElement(new Option("\tSpecifies if inverse of selection is to be output.\n", "V", 0, "-V"));
    


    return newVector.elements();
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    String str = Utils.getOption('R', options);
    if (str.length() != 0) {
      setInstancesIndices(str);
    } else {
      setInstancesIndices("first-last");
    }
    setInvertSelection(Utils.getFlag('V', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[8];
    int current = 0;
    
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    options[(current++)] = "-R";options[(current++)] = getInstancesIndices();
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String globalInfo()
  {
    return "A filter that removes a given range of instances of a dataset.";
  }
  






  public String instancesIndicesTipText()
  {
    return "The range of instances to select. First and last are valid indexes.";
  }
  





  public String getInstancesIndices()
  {
    return m_Range.getRanges();
  }
  








  public void setInstancesIndices(String rangeList)
  {
    m_Range.setRanges(rangeList);
  }
  






  public String invertSelectionTipText()
  {
    return "Whether to invert the selection.";
  }
  





  public boolean getInvertSelection()
  {
    return m_Range.getInvert();
  }
  





  public void setInvertSelection(boolean inverse)
  {
    m_Range.setInvert(inverse);
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
    
    if (!isFirstBatchDone())
    {
      m_Range.setUpper(getInputFormat().numInstances() - 1);
      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        if (!m_Range.isInRange(i)) {
          push(getInputFormat().instance(i));
        }
      }
    }
    else {
      for (int i = 0; i < getInputFormat().numInstances(); i++) {
        push(getInputFormat().instance(i));
      }
    }
    
    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    return numPendingOutput() != 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5548 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new RemoveRange(), argv);
  }
}

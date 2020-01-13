package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Queue;
import weka.core.Range;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;
























































public abstract class AbstractTimeSeries
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  private static final long serialVersionUID = -3795656792078022357L;
  protected Range m_SelectedCols = new Range();
  




  protected boolean m_FillWithMissing = true;
  




  protected int m_InstanceRange = -1;
  

  protected Queue m_History;
  


  public AbstractTimeSeries() {}
  

  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tSpecify list of columns to translate in time. First and\n\tlast are valid indexes. (default none)", "R", 1, "-R <index1,index2-index4,...>"));
    


    newVector.addElement(new Option("\tInvert matching sense (i.e. calculate for all non-specified columns)", "V", 0, "-V"));
    

    newVector.addElement(new Option("\tThe number of instances forward to translate values\n\tbetween. A negative number indicates taking values from\n\ta past instance. (default -1)", "I", 1, "-I <num>"));
    



    newVector.addElement(new Option("\tFor instances at the beginning or end of the dataset where\n\tthe translated values are not known, remove those instances\n\t(default is to use missing values).", "M", 0, "-M"));
    




    return newVector.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String copyList = Utils.getOption('R', options);
    if (copyList.length() != 0) {
      setAttributeIndices(copyList);
    } else {
      setAttributeIndices("");
    }
    
    setInvertSelection(Utils.getFlag('V', options));
    
    setFillWithMissing(!Utils.getFlag('M', options));
    
    String instanceRange = Utils.getOption('I', options);
    if (instanceRange.length() != 0) {
      setInstanceRange(Integer.parseInt(instanceRange));
    } else {
      setInstanceRange(-1);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[6];
    int current = 0;
    
    if (!getAttributeIndices().equals("")) {
      options[(current++)] = "-R";options[(current++)] = getAttributeIndices();
    }
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    options[(current++)] = "-I";options[(current++)] = ("" + getInstanceRange());
    if (!getFillWithMissing()) {
      options[(current++)] = "-M";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    resetHistory();
    m_SelectedCols.setUpper(instanceInfo.numAttributes() - 1);
    return false;
  }
  











  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new NullPointerException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
      resetHistory();
    }
    
    Instance newInstance = historyInput(instance);
    if (newInstance != null) {
      push(newInstance);
      return true;
    }
    return false;
  }
  









  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if ((getFillWithMissing()) && (m_InstanceRange > 0)) {
      while (!m_History.empty()) {
        push(mergeInstances(null, (Instance)m_History.pop()));
      }
    }
    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return numPendingOutput() != 0;
  }
  




  public String fillWithMissingTipText()
  {
    return "For instances at the beginning or end of the dataset where the translated values are not known, use missing values (default is to remove those instances)";
  }
  








  public boolean getFillWithMissing()
  {
    return m_FillWithMissing;
  }
  






  public void setFillWithMissing(boolean newFillWithMissing)
  {
    m_FillWithMissing = newFillWithMissing;
  }
  




  public String instanceRangeTipText()
  {
    return "The number of instances forward/backward to merge values between. A negative number indicates taking values from a past instance.";
  }
  







  public int getInstanceRange()
  {
    return m_InstanceRange;
  }
  






  public void setInstanceRange(int newInstanceRange)
  {
    m_InstanceRange = newInstanceRange;
  }
  




  public String invertSelectionTipText()
  {
    return "Invert matching sense. ie calculate for all non-specified columns.";
  }
  





  public boolean getInvertSelection()
  {
    return m_SelectedCols.getInvert();
  }
  







  public void setInvertSelection(boolean invert)
  {
    m_SelectedCols.setInvert(invert);
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getAttributeIndices()
  {
    return m_SelectedCols.getRanges();
  }
  








  public void setAttributeIndices(String rangeList)
  {
    m_SelectedCols.setRanges(rangeList);
  }
  







  public void setAttributeIndicesArray(int[] attributes)
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  

  protected void resetHistory()
  {
    if (m_History == null) {
      m_History = new Queue();
    } else {
      m_History.removeAllElements();
    }
  }
  









  protected Instance historyInput(Instance instance)
  {
    m_History.push(instance);
    if (m_History.size() <= Math.abs(m_InstanceRange)) {
      if ((getFillWithMissing()) && (m_InstanceRange < 0)) {
        return mergeInstances(null, instance);
      }
      return null;
    }
    
    if (m_InstanceRange < 0) {
      return mergeInstances((Instance)m_History.pop(), instance);
    }
    return mergeInstances(instance, (Instance)m_History.pop());
  }
  
  protected abstract Instance mergeInstances(Instance paramInstance1, Instance paramInstance2);
}

package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;



















































public class RemovePercentage
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 2150341191158533133L;
  private double m_Percentage = 50.0D;
  

  private boolean m_Inverse = false;
  


  public RemovePercentage() {}
  

  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tSpecifies percentage of instances to select. (default 50)\n", "P", 1, "-P <percentage>"));
    


    newVector.addElement(new Option("\tSpecifies if inverse of selection is to be output.\n", "V", 0, "-V"));
    


    return newVector.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    String percent = Utils.getOption('P', options);
    if (percent.length() != 0) {
      setPercentage(Double.parseDouble(percent));
    } else {
      setPercentage(50.0D);
    }
    setInvertSelection(Utils.getFlag('V', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    options[(current++)] = "-P";options[(current++)] = ("" + getPercentage());
    if (getInvertSelection()) {
      options[(current++)] = "-V";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String globalInfo()
  {
    return "A filter that removes a given percentage of a dataset.";
  }
  






  public String percentageTipText()
  {
    return "The percentage of the data to select.";
  }
  





  public double getPercentage()
  {
    return m_Percentage;
  }
  






  public void setPercentage(double percent)
  {
    if ((percent < 0.0D) || (percent > 100.0D)) {
      throw new IllegalArgumentException("Percentage must be between 0 and 100.");
    }
    m_Percentage = percent;
  }
  






  public String invertSelectionTipText()
  {
    return "Whether to invert the selection.";
  }
  





  public boolean getInvertSelection()
  {
    return m_Inverse;
  }
  





  public void setInvertSelection(boolean inverse)
  {
    m_Inverse = inverse;
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
    

    Instances toFilter = getInputFormat();
    int cutOff = (int)Math.round(toFilter.numInstances() * m_Percentage / 100.0D);
    
    if (m_Inverse) {
      for (int i = 0; i < cutOff; i++) {
        push(toFilter.instance(i));
      }
    } else {
      for (int i = cutOff; i < toFilter.numInstances(); i++) {
        push(toFilter.instance(i));
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
    runFilter(new RemovePercentage(), argv);
  }
}

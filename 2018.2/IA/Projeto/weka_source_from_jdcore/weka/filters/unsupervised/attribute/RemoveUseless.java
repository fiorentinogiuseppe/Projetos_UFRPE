package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
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











































public class RemoveUseless
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = -8659417851407640038L;
  protected Remove m_removeFilter = null;
  

  protected double m_maxVariancePercentage = 99.0D;
  


  public RemoveUseless() {}
  

  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
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
    m_removeFilter = null;
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
    if (m_removeFilter != null) {
      m_removeFilter.input(instance);
      Instance processed = m_removeFilter.output();
      processed.setDataset(getOutputFormat());
      copyValues(processed, false, instance.dataset(), getOutputFormat());
      push(processed);
      return true;
    }
    bufferInput(instance);
    return false;
  }
  





  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_removeFilter == null)
    {


      Instances toFilter = getInputFormat();
      int[] attsToDelete = new int[toFilter.numAttributes()];
      int numToDelete = 0;
      for (int i = 0; i < toFilter.numAttributes(); i++) {
        if (i != toFilter.classIndex()) {
          AttributeStats stats = toFilter.attributeStats(i);
          if (missingCount == toFilter.numInstances()) {
            attsToDelete[(numToDelete++)] = i;
          } else if (distinctCount < 2)
          {
            attsToDelete[(numToDelete++)] = i;
          } else if (toFilter.attribute(i).isNominal())
          {
            double variancePercent = distinctCount / (totalCount - missingCount) * 100.0D;
            
            if (variancePercent > m_maxVariancePercentage) {
              attsToDelete[(numToDelete++)] = i;
            }
          }
        }
      }
      int[] finalAttsToDelete = new int[numToDelete];
      System.arraycopy(attsToDelete, 0, finalAttsToDelete, 0, numToDelete);
      
      m_removeFilter = new Remove();
      m_removeFilter.setAttributeIndicesArray(finalAttsToDelete);
      m_removeFilter.setInvertSelection(false);
      m_removeFilter.setInputFormat(toFilter);
      
      for (int i = 0; i < toFilter.numInstances(); i++) {
        m_removeFilter.input(toFilter.instance(i));
      }
      m_removeFilter.batchFinished();
      

      Instances outputDataset = m_removeFilter.getOutputFormat();
      

      outputDataset.setRelationName(toFilter.relationName());
      
      setOutputFormat(outputDataset);
      Instance processed; while ((processed = m_removeFilter.output()) != null) {
        processed.setDataset(outputDataset);
        push(processed);
      }
    }
    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tMaximum variance percentage allowed (default 99)", "M", 1, "-M <max variance %>"));
    



    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    String mString = Utils.getOption('M', options);
    if (mString.length() != 0) {
      setMaximumVariancePercentageAllowed((int)Double.valueOf(mString).doubleValue());
    } else {
      setMaximumVariancePercentageAllowed(99.0D);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + getMaximumVariancePercentageAllowed());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String globalInfo()
  {
    return "This filter removes attributes that do not vary at all or that vary too much. All constant attributes are deleted automatically, along with any that exceed the maximum percentage of variance parameter. The maximum variance test is only applied to nominal attributes.";
  }
  










  public String maximumVariancePercentageAllowedTipText()
  {
    return "Set the threshold for the highest variance allowed before a nominal attribute will be deleted.Specifically, if (number_of_distinct_values / total_number_of_values * 100) is greater than this value then the attribute will be removed.";
  }
  








  public void setMaximumVariancePercentageAllowed(double maxVariance)
  {
    m_maxVariancePercentage = maxVariance;
  }
  






  public double getMaximumVariancePercentageAllowed()
  {
    return m_maxVariancePercentage;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7470 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new RemoveUseless(), argv);
  }
}

package weka.filters.unsupervised.attribute;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.UnsupportedAttributeTypeException;
























































public class TimeSeriesDelta
  extends TimeSeriesTranslate
{
  static final long serialVersionUID = 3101490081896634942L;
  
  public TimeSeriesDelta() {}
  
  public String globalInfo()
  {
    return "An instance filter that assumes instances form time-series data and replaces attribute values in the current instance with the difference between the current value and the equivalent attribute attribute value of some previous (or future) instance. For instances where the time-shifted value is unknown either the instance may be dropped, or missing values used. Skips the class attribute if it is set.";
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
    if ((instanceInfo.classIndex() > 0) && (!getFillWithMissing())) {
      throw new IllegalArgumentException("TimeSeriesDelta: Need to fill in missing values using appropriate option when class index is set.");
    }
    
    super.setInputFormat(instanceInfo);
    
    Instances outputFormat = new Instances(instanceInfo, 0);
    for (int i = 0; i < instanceInfo.numAttributes(); i++) {
      if ((i != instanceInfo.classIndex()) && 
        (m_SelectedCols.isInRange(i))) {
        if (outputFormat.attribute(i).isNumeric()) {
          outputFormat.renameAttribute(i, outputFormat.attribute(i).name() + " d" + (m_InstanceRange < 0 ? '-' : '+') + Math.abs(m_InstanceRange));

        }
        else
        {
          throw new UnsupportedAttributeTypeException("Time delta attributes must be numeric!");
        }
      }
    }
    
    outputFormat.setClassIndex(instanceInfo.classIndex());
    setOutputFormat(outputFormat);
    return true;
  }
  










  protected Instance mergeInstances(Instance source, Instance dest)
  {
    Instances outputFormat = outputFormatPeek();
    double[] vals = new double[outputFormat.numAttributes()];
    for (int i = 0; i < vals.length; i++) {
      if ((i != outputFormat.classIndex()) && (m_SelectedCols.isInRange(i))) {
        if ((source != null) && (!source.isMissing(i)) && (!dest.isMissing(i))) {
          vals[i] = (dest.value(i) - source.value(i));
        } else {
          vals[i] = Instance.missingValue();
        }
      } else {
        vals[i] = dest.value(i);
      }
    }
    Instance inst = null;
    if ((dest instanceof SparseInstance)) {
      inst = new SparseInstance(dest.weight(), vals);
    } else {
      inst = new Instance(dest.weight(), vals);
    }
    inst.setDataset(dest.dataset());
    return inst;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5547 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new TimeSeriesDelta(), argv);
  }
}

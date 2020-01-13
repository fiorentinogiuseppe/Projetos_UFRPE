package weka.filters.unsupervised.attribute;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;















































public class NumericToBinary
  extends PotentialClassIgnorer
  implements UnsupervisedFilter, StreamableFilter
{
  static final long serialVersionUID = 2616879323359470802L;
  
  public NumericToBinary() {}
  
  public String globalInfo()
  {
    return "Converts all numeric attributes into binary attributes (apart from the class attribute, if set): if the value of the numeric attribute is exactly zero, the value of the new attribute will be zero. If the value of the numeric attribute is missing, the value of the new attribute will be missing. Otherwise, the value of the new attribute will be one. The new attributes will be nominal.";
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
    setOutputFormat();
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
    convertInstance(instance);
    return true;
  }
  










  private void setOutputFormat()
  {
    int newClassIndex = getInputFormat().classIndex();
    FastVector newAtts = new FastVector();
    for (int j = 0; j < getInputFormat().numAttributes(); j++) {
      Attribute att = getInputFormat().attribute(j);
      if ((j == newClassIndex) || (!att.isNumeric())) {
        newAtts.addElement(att.copy());
      } else {
        StringBuffer attributeName = new StringBuffer(att.name() + "_binarized");
        FastVector vals = new FastVector(2);
        vals.addElement("0");vals.addElement("1");
        newAtts.addElement(new Attribute(attributeName.toString(), vals));
      }
    }
    Instances outputFormat = new Instances(getInputFormat().relationName(), newAtts, 0);
    outputFormat.setClassIndex(newClassIndex);
    setOutputFormat(outputFormat);
  }
  






  private void convertInstance(Instance instance)
  {
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      double[] vals = new double[instance.numValues()];
      int[] newIndices = new int[instance.numValues()];
      for (int j = 0; j < instance.numValues(); j++) {
        Attribute att = getInputFormat().attribute(instance.index(j));
        if ((!att.isNumeric()) || (instance.index(j) == getInputFormat().classIndex())) {
          vals[j] = instance.valueSparse(j);
        }
        else if (instance.isMissingSparse(j)) {
          vals[j] = instance.valueSparse(j);
        } else {
          vals[j] = 1.0D;
        }
        
        newIndices[j] = instance.index(j);
      }
      inst = new SparseInstance(instance.weight(), vals, newIndices, outputFormatPeek().numAttributes());
    }
    else {
      double[] vals = new double[outputFormatPeek().numAttributes()];
      for (int j = 0; j < getInputFormat().numAttributes(); j++) {
        Attribute att = getInputFormat().attribute(j);
        if ((!att.isNumeric()) || (j == getInputFormat().classIndex())) {
          vals[j] = instance.value(j);
        }
        else if ((instance.isMissing(j)) || (instance.value(j) == 0.0D)) {
          vals[j] = instance.value(j);
        } else {
          vals[j] = 1.0D;
        }
      }
      
      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(instance.dataset());
    push(inst);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new NumericToBinary(), argv);
  }
}

package weka.filters.unsupervised.instance;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;





































public class SparseToNonSparse
  extends Filter
  implements UnsupervisedFilter, StreamableFilter
{
  static final long serialVersionUID = 2481634184210236074L;
  
  public SparseToNonSparse() {}
  
  public String globalInfo()
  {
    return "An instance filter that converts all incoming sparse instances into non-sparse format.";
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
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      inst = new Instance(instance.weight(), instance.toDoubleArray());
      inst.setDataset(instance.dataset());
    } else {
      inst = instance;
    }
    push(inst);
    return true;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5548 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new SparseToNonSparse(), argv);
  }
}

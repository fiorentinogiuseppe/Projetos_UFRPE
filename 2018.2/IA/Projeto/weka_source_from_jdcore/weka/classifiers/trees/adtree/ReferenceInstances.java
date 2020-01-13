package weka.classifiers.trees.adtree;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;





































public class ReferenceInstances
  extends Instances
{
  private static final long serialVersionUID = -8022666381920252997L;
  
  public ReferenceInstances(Instances dataset, int capacity)
  {
    super(dataset, capacity);
  }
  








  public final void addReference(Instance instance)
  {
    m_Instances.addElement(instance);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

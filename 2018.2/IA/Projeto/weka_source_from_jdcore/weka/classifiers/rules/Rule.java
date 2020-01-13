package weka.classifiers.rules;

import java.io.Serializable;
import weka.core.Copyable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.WeightedInstancesHandler;































public abstract class Rule
  implements WeightedInstancesHandler, Copyable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = 8815687740470471229L;
  
  public Rule() {}
  
  public Object copy()
  {
    return this;
  }
  
  public abstract boolean covers(Instance paramInstance);
  
  public abstract void grow(Instances paramInstances)
    throws Exception;
  
  public abstract boolean hasAntds();
  
  public abstract double getConsequent();
  
  public abstract double size();
}

package weka.classifiers.trees.j48;

import java.io.Serializable;
import weka.core.RevisionHandler;



































public abstract class SplitCriterion
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 5490996638027101259L;
  
  public SplitCriterion() {}
  
  public double splitCritValue(Distribution bags)
  {
    return 0.0D;
  }
  






  public double splitCritValue(Distribution train, Distribution test)
  {
    return 0.0D;
  }
  







  public double splitCritValue(Distribution train, Distribution test, int noClassesDefault)
  {
    return 0.0D;
  }
  







  public double splitCritValue(Distribution train, Distribution test, Distribution defC)
  {
    return 0.0D;
  }
}

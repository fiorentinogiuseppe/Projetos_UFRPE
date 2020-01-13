package weka.classifiers.trees.j48;

import java.io.Serializable;
import weka.core.Instances;
import weka.core.RevisionHandler;






































public abstract class ModelSelection
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -4850147125096133642L;
  
  public ModelSelection() {}
  
  public abstract ClassifierSplitModel selectModel(Instances paramInstances)
    throws Exception;
  
  public ClassifierSplitModel selectModel(Instances train, Instances test)
    throws Exception
  {
    throw new Exception("Model selection method not implemented");
  }
}

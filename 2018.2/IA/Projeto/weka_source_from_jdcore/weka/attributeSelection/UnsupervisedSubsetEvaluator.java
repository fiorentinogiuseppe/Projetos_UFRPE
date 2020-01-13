package weka.attributeSelection;

import weka.clusterers.Clusterer;

public abstract class UnsupervisedSubsetEvaluator
  extends ASEvaluation
  implements SubsetEvaluator
{
  static final long serialVersionUID = 627934376267488763L;
  
  public UnsupervisedSubsetEvaluator() {}
  
  public abstract int getNumClusters()
    throws Exception;
  
  public abstract Clusterer getClusterer();
  
  public abstract void setClusterer(Clusterer paramClusterer);
}

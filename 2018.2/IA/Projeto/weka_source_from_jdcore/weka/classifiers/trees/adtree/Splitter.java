package weka.classifiers.trees.adtree;

import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;

public abstract class Splitter
  implements Serializable, Cloneable, RevisionHandler
{
  private static final long serialVersionUID = 8190449848490055L;
  public int orderAdded;
  
  public Splitter() {}
  
  public abstract int getNumOfBranches();
  
  public abstract int branchInstanceGoesDown(Instance paramInstance);
  
  public abstract ReferenceInstances instancesDownBranch(int paramInt, Instances paramInstances);
  
  public abstract String attributeString(Instances paramInstances);
  
  public abstract String comparisonString(int paramInt, Instances paramInstances);
  
  public abstract boolean equalTo(Splitter paramSplitter);
  
  public abstract void setChildForBranch(int paramInt, PredictionNode paramPredictionNode);
  
  public abstract PredictionNode getChildForBranch(int paramInt);
  
  public abstract Object clone();
}

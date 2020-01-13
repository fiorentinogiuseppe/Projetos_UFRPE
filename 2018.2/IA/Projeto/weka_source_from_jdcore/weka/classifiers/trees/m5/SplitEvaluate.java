package weka.classifiers.trees.m5;

import weka.core.Instances;

public abstract interface SplitEvaluate
{
  public abstract SplitEvaluate copy()
    throws Exception;
  
  public abstract void attrSplit(int paramInt, Instances paramInstances)
    throws Exception;
  
  public abstract double maxImpurity();
  
  public abstract int position();
  
  public abstract int splitAttr();
  
  public abstract double splitValue();
}

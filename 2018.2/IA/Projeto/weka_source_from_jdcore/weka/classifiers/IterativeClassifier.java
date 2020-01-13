package weka.classifiers;

import weka.core.Instances;

public abstract interface IterativeClassifier
{
  public abstract void initClassifier(Instances paramInstances)
    throws Exception;
  
  public abstract void next(int paramInt)
    throws Exception;
  
  public abstract void done()
    throws Exception;
  
  public abstract Object clone()
    throws CloneNotSupportedException;
}

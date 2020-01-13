package weka.clusterers;

import weka.core.Instance;

public abstract interface UpdateableClusterer
{
  public abstract void updateClusterer(Instance paramInstance)
    throws Exception;
  
  public abstract void updateFinished();
}

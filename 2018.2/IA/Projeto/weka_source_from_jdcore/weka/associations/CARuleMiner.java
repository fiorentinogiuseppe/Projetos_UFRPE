package weka.associations;

import weka.core.FastVector;
import weka.core.Instances;
import weka.core.OptionHandler;

public abstract interface CARuleMiner
  extends OptionHandler
{
  public abstract FastVector[] mineCARs(Instances paramInstances)
    throws Exception;
  
  public abstract Instances getInstancesNoClass();
  
  public abstract Instances getInstancesOnlyClass();
  
  public abstract String metricString();
  
  public abstract void setClassIndex(int paramInt);
}

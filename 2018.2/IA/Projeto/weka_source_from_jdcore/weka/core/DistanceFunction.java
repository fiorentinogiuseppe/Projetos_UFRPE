package weka.core;

import weka.core.neighboursearch.PerformanceStats;

public abstract interface DistanceFunction
  extends OptionHandler
{
  public abstract void setInstances(Instances paramInstances);
  
  public abstract Instances getInstances();
  
  public abstract void setAttributeIndices(String paramString);
  
  public abstract String getAttributeIndices();
  
  public abstract void setInvertSelection(boolean paramBoolean);
  
  public abstract boolean getInvertSelection();
  
  public abstract double distance(Instance paramInstance1, Instance paramInstance2);
  
  public abstract double distance(Instance paramInstance1, Instance paramInstance2, PerformanceStats paramPerformanceStats)
    throws Exception;
  
  public abstract double distance(Instance paramInstance1, Instance paramInstance2, double paramDouble);
  
  public abstract double distance(Instance paramInstance1, Instance paramInstance2, double paramDouble, PerformanceStats paramPerformanceStats);
  
  public abstract void postProcessDistances(double[] paramArrayOfDouble);
  
  public abstract void update(Instance paramInstance);
  
  public abstract void clean();
}

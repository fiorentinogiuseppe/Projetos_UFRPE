package weka.clusterers;

import weka.core.Instance;

public abstract interface DensityBasedClusterer
  extends Clusterer
{
  public abstract double[] clusterPriors()
    throws Exception;
  
  public abstract double[] logDensityPerClusterForInstance(Instance paramInstance)
    throws Exception;
  
  public abstract double logDensityForInstance(Instance paramInstance)
    throws Exception;
  
  public abstract double[] logJointDensitiesForInstance(Instance paramInstance)
    throws Exception;
  
  public abstract double[] distributionForInstance(Instance paramInstance)
    throws Exception;
}

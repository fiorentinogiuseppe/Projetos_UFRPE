package weka.clusterers;

import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public abstract interface Clusterer
{
  public abstract void buildClusterer(Instances paramInstances)
    throws Exception;
  
  public abstract int clusterInstance(Instance paramInstance)
    throws Exception;
  
  public abstract double[] distributionForInstance(Instance paramInstance)
    throws Exception;
  
  public abstract int numberOfClusters()
    throws Exception;
  
  public abstract Capabilities getCapabilities();
}

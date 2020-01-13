package weka.associations;

import weka.core.Capabilities;
import weka.core.Instances;

public abstract interface Associator
{
  public abstract void buildAssociations(Instances paramInstances)
    throws Exception;
  
  public abstract Capabilities getCapabilities();
}

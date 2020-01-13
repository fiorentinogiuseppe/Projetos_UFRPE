package weka.attributeSelection;

import weka.core.Instance;
import weka.core.Instances;

public abstract interface AttributeTransformer
{
  public abstract Instances transformedHeader()
    throws Exception;
  
  public abstract Instances transformedData(Instances paramInstances)
    throws Exception;
  
  public abstract Instance convertInstance(Instance paramInstance)
    throws Exception;
}

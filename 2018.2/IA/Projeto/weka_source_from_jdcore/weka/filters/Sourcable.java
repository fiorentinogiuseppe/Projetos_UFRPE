package weka.filters;

import weka.core.Instances;

public abstract interface Sourcable
{
  public abstract String toSource(String paramString, Instances paramInstances)
    throws Exception;
}

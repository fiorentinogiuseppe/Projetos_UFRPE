package weka.classifiers;

import weka.core.Instance;

public abstract interface IntervalEstimator
{
  public abstract double[][] predictInterval(Instance paramInstance, double paramDouble)
    throws Exception;
}

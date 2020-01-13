package weka.classifiers;

import weka.core.Instance;

public abstract interface UpdateableClassifier
{
  public abstract void updateClassifier(Instance paramInstance)
    throws Exception;
}

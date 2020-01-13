package weka.classifiers.evaluation;

import weka.core.Instance;
































public abstract interface Prediction
{
  public static final double MISSING_VALUE = ;
  
  public abstract double weight();
  
  public abstract double actual();
  
  public abstract double predicted();
}

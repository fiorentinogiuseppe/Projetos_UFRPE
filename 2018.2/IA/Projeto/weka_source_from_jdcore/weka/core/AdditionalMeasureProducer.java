package weka.core;

import java.util.Enumeration;

public abstract interface AdditionalMeasureProducer
{
  public abstract Enumeration enumerateMeasures();
  
  public abstract double getMeasure(String paramString);
}

package weka.experiment;

import java.io.Serializable;
import weka.core.Instances;

public abstract interface SplitEvaluator
  extends Serializable
{
  public abstract void setAdditionalMeasures(String[] paramArrayOfString);
  
  public abstract String[] getKeyNames();
  
  public abstract Object[] getKeyTypes();
  
  public abstract String[] getResultNames();
  
  public abstract Object[] getResultTypes();
  
  public abstract Object[] getKey();
  
  public abstract Object[] getResult(Instances paramInstances1, Instances paramInstances2)
    throws Exception;
  
  public abstract String getRawResultOutput();
}

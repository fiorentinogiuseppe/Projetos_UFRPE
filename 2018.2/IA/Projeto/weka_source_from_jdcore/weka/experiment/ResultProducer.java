package weka.experiment;

import java.io.Serializable;
import weka.core.Instances;

public abstract interface ResultProducer
  extends Serializable
{
  public abstract void setInstances(Instances paramInstances);
  
  public abstract void setResultListener(ResultListener paramResultListener);
  
  public abstract void setAdditionalMeasures(String[] paramArrayOfString);
  
  public abstract void preProcess()
    throws Exception;
  
  public abstract void postProcess()
    throws Exception;
  
  public abstract void doRun(int paramInt)
    throws Exception;
  
  public abstract void doRunKeys(int paramInt)
    throws Exception;
  
  public abstract String[] getKeyNames()
    throws Exception;
  
  public abstract Object[] getKeyTypes()
    throws Exception;
  
  public abstract String[] getResultNames()
    throws Exception;
  
  public abstract Object[] getResultTypes()
    throws Exception;
  
  public abstract String getCompatibilityState();
}

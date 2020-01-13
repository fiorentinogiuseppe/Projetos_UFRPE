package weka.experiment;

import java.io.Serializable;

public abstract interface ResultListener
  extends Serializable
{
  public abstract String[] determineColumnConstraints(ResultProducer paramResultProducer)
    throws Exception;
  
  public abstract void preProcess(ResultProducer paramResultProducer)
    throws Exception;
  
  public abstract void postProcess(ResultProducer paramResultProducer)
    throws Exception;
  
  public abstract void acceptResult(ResultProducer paramResultProducer, Object[] paramArrayOfObject1, Object[] paramArrayOfObject2)
    throws Exception;
  
  public abstract boolean isResultRequired(ResultProducer paramResultProducer, Object[] paramArrayOfObject)
    throws Exception;
}

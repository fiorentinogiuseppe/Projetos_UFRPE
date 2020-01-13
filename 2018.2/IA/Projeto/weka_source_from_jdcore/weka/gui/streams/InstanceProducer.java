package weka.gui.streams;

import weka.core.Instance;
import weka.core.Instances;

public abstract interface InstanceProducer
{
  public abstract void addInstanceListener(InstanceListener paramInstanceListener);
  
  public abstract void removeInstanceListener(InstanceListener paramInstanceListener);
  
  public abstract Instances outputFormat()
    throws Exception;
  
  public abstract Instance outputPeek()
    throws Exception;
}

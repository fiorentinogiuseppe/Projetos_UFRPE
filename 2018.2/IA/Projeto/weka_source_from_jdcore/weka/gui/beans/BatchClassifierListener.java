package weka.gui.beans;

import java.util.EventListener;

public abstract interface BatchClassifierListener
  extends EventListener
{
  public abstract void acceptClassifier(BatchClassifierEvent paramBatchClassifierEvent);
}

package weka.gui.beans;

import java.util.EventListener;

public abstract interface IncrementalClassifierListener
  extends EventListener
{
  public abstract void acceptClassifier(IncrementalClassifierEvent paramIncrementalClassifierEvent);
}

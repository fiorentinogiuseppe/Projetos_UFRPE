package weka.gui.beans;

import java.util.EventListener;

public abstract interface BatchClustererListener
  extends EventListener
{
  public abstract void acceptClusterer(BatchClustererEvent paramBatchClustererEvent);
}

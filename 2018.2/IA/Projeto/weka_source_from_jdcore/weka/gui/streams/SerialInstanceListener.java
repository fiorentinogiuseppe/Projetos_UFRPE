package weka.gui.streams;

import java.util.EventListener;

public abstract interface SerialInstanceListener
  extends EventListener
{
  public abstract void secondInstanceProduced(InstanceEvent paramInstanceEvent);
}

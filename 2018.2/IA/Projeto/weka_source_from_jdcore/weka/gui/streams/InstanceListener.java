package weka.gui.streams;

import java.util.EventListener;

public abstract interface InstanceListener
  extends EventListener
{
  public abstract void instanceProduced(InstanceEvent paramInstanceEvent);
}

package weka.gui.beans;

import java.util.EventListener;

public abstract interface InstanceListener
  extends EventListener
{
  public abstract void acceptInstance(InstanceEvent paramInstanceEvent);
}

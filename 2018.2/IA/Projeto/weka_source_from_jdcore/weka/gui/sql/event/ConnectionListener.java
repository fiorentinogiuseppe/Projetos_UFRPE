package weka.gui.sql.event;

import java.util.EventListener;

public abstract interface ConnectionListener
  extends EventListener
{
  public abstract void connectionChange(ConnectionEvent paramConnectionEvent);
}

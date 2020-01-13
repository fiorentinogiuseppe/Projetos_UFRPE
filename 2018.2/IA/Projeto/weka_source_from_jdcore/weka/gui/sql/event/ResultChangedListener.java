package weka.gui.sql.event;

import java.util.EventListener;

public abstract interface ResultChangedListener
  extends EventListener
{
  public abstract void resultChanged(ResultChangedEvent paramResultChangedEvent);
}

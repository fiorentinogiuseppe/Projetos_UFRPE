package weka.gui.sql.event;

import java.util.EventListener;

public abstract interface HistoryChangedListener
  extends EventListener
{
  public abstract void historyChanged(HistoryChangedEvent paramHistoryChangedEvent);
}

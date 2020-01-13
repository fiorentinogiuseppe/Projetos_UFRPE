package weka.gui.sql.event;

import java.util.EventListener;

public abstract interface QueryExecuteListener
  extends EventListener
{
  public abstract void queryExecuted(QueryExecuteEvent paramQueryExecuteEvent);
}

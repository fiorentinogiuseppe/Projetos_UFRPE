package weka.gui.beans;

import java.util.EventListener;

public abstract interface TestSetListener
  extends EventListener
{
  public abstract void acceptTestSet(TestSetEvent paramTestSetEvent);
}

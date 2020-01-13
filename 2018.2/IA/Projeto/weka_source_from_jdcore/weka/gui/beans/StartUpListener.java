package weka.gui.beans;

import java.util.EventListener;

public abstract interface StartUpListener
  extends EventListener
{
  public abstract void startUpComplete();
}

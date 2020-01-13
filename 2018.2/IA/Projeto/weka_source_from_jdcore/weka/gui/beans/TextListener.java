package weka.gui.beans;

import java.util.EventListener;

public abstract interface TextListener
  extends EventListener
{
  public abstract void acceptText(TextEvent paramTextEvent);
}

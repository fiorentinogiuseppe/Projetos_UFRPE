package weka.gui.beans;

import java.util.EventListener;

public abstract interface TrainingSetListener
  extends EventListener
{
  public abstract void acceptTrainingSet(TrainingSetEvent paramTrainingSetEvent);
}

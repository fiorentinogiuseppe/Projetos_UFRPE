package weka.gui.beans;

public abstract interface TrainingSetProducer
{
  public abstract void addTrainingSetListener(TrainingSetListener paramTrainingSetListener);
  
  public abstract void removeTrainingSetListener(TrainingSetListener paramTrainingSetListener);
}

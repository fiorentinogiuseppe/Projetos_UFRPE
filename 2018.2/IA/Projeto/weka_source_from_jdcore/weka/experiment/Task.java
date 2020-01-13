package weka.experiment;

import java.io.Serializable;

public abstract interface Task
  extends Serializable
{
  public abstract void execute();
  
  public abstract TaskStatusInfo getTaskStatus();
}

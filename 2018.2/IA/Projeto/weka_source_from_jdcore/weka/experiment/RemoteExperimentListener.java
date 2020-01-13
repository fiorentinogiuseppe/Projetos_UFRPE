package weka.experiment;

public abstract interface RemoteExperimentListener
{
  public abstract void remoteExperimentStatus(RemoteExperimentEvent paramRemoteExperimentEvent);
}

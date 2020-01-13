package weka.experiment;

import java.rmi.Remote;
import java.rmi.RemoteException;

public abstract interface Compute
  extends Remote
{
  public abstract Object executeTask(Task paramTask)
    throws RemoteException;
  
  public abstract Object checkStatus(Object paramObject)
    throws Exception;
}

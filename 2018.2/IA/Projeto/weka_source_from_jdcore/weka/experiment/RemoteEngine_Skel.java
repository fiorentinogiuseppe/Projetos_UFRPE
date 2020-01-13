package weka.experiment;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.UnmarshalException;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.Skeleton;
import java.rmi.server.SkeletonMismatchException;

public final class RemoteEngine_Skel
  implements Skeleton
{
  private static final Operation[] operations = { new Operation("java.lang.Object checkStatus(java.lang.Object)"), new Operation("java.lang.Object executeTask(weka.experiment.Task)") };
  private static final long interfaceHash = -8065814382466137525L;
  
  public RemoteEngine_Skel() {}
  
  public void dispatch(Remote paramRemote, RemoteCall paramRemoteCall, int paramInt, long paramLong)
    throws Exception
  {
    if (paramInt < 0)
    {
      if (paramLong == 8527603492100504454L) {
        paramInt = 0;
      } else if (paramLong == 3272821424511182813L) {
        paramInt = 1;
      } else {
        throw new UnmarshalException("invalid method hash");
      }
    }
    else if (paramLong != -8065814382466137525L) {
      throw new SkeletonMismatchException("interface hash mismatch");
    }
    RemoteEngine localRemoteEngine = (RemoteEngine)paramRemote;
    Object localObject1;
    switch (paramInt)
    {
    case 0: 
      try
      {
        ObjectInput localObjectInput1 = paramRemoteCall.getInputStream();
        localObject1 = localObjectInput1.readObject();
      }
      catch (IOException localIOException3)
      {
        throw new UnmarshalException("error unmarshalling arguments", localIOException3);
      }
      catch (ClassNotFoundException localClassNotFoundException1)
      {
        throw new UnmarshalException("error unmarshalling arguments", localClassNotFoundException1);
      }
      finally
      {
        paramRemoteCall.releaseInputStream();
      }
      Object localObject3 = localRemoteEngine.checkStatus(localObject1);
      try
      {
        ObjectOutput localObjectOutput1 = paramRemoteCall.getResultStream(true);
        localObjectOutput1.writeObject(localObject3);
      }
      catch (IOException localIOException1)
      {
        throw new MarshalException("error marshalling return", localIOException1);
      }
    case 1: 
      try
      {
        ObjectInput localObjectInput2 = paramRemoteCall.getInputStream();
        localObject1 = (Task)localObjectInput2.readObject();
      }
      catch (IOException localIOException4)
      {
        throw new UnmarshalException("error unmarshalling arguments", localIOException4);
      }
      catch (ClassNotFoundException localClassNotFoundException2)
      {
        throw new UnmarshalException("error unmarshalling arguments", localClassNotFoundException2);
      }
      finally
      {
        paramRemoteCall.releaseInputStream();
      }
      Object localObject5 = localRemoteEngine.executeTask((Task)localObject1);
      try
      {
        ObjectOutput localObjectOutput2 = paramRemoteCall.getResultStream(true);
        localObjectOutput2.writeObject(localObject5);
      }
      catch (IOException localIOException2)
      {
        throw new MarshalException("error marshalling return", localIOException2);
      }
    default: 
      throw new UnmarshalException("invalid method number");
    }
  }
  
  public Operation[] getOperations()
  {
    return (Operation[])operations.clone();
  }
}

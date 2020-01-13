package weka.experiment;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import java.rmi.UnmarshalException;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

public final class RemoteEngine_Stub
  extends RemoteStub
  implements Compute, Remote
{
  private static final Operation[] operations = { new Operation("java.lang.Object checkStatus(java.lang.Object)"), new Operation("java.lang.Object executeTask(weka.experiment.Task)") };
  private static final long interfaceHash = -8065814382466137525L;
  private static final long serialVersionUID = 2L;
  private static boolean useNewInvoke;
  private static Method $method_checkStatus_0;
  private static Method $method_executeTask_1;
  
  static
  {
    try
    {
      RemoteRef.class.getMethod("invoke", new Class[] { Remote.class, Method.class, new Objec[0].getClass(), Long.TYPE });
      useNewInvoke = true;
      $method_checkStatus_0 = Compute.class.getMethod("checkStatus", new Class[] { Object.class });
      $method_executeTask_1 = Compute.class.getMethod("executeTask", new Class[] { Task.class });
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      useNewInvoke = false;
    }
  }
  
  public RemoteEngine_Stub() {}
  
  public RemoteEngine_Stub(RemoteRef paramRemoteRef)
  {
    super(paramRemoteRef);
  }
  
  public Object checkStatus(Object paramObject)
    throws Exception
  {
    if (useNewInvoke)
    {
      localObject1 = ref.invoke(this, $method_checkStatus_0, new Object[] { paramObject }, 8527603492100504454L);
      return localObject1;
    }
    Object localObject1 = ref.newCall(this, operations, 0, -8065814382466137525L);
    try
    {
      ObjectOutput localObjectOutput = ((RemoteCall)localObject1).getOutputStream();
      localObjectOutput.writeObject(paramObject);
    }
    catch (IOException localIOException1)
    {
      throw new MarshalException("error marshalling arguments", localIOException1);
    }
    ref.invoke((RemoteCall)localObject1);
    Object localObject2;
    try
    {
      ObjectInput localObjectInput = ((RemoteCall)localObject1).getInputStream();
      localObject2 = localObjectInput.readObject();
    }
    catch (IOException localIOException2)
    {
      throw new UnmarshalException("error unmarshalling return", localIOException2);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new UnmarshalException("error unmarshalling return", localClassNotFoundException);
    }
    finally
    {
      ref.done((RemoteCall)localObject1);
    }
    return localObject2;
  }
  
  public Object executeTask(Task paramTask)
    throws RemoteException
  {
    try
    {
      if (useNewInvoke)
      {
        localObject1 = ref.invoke(this, $method_executeTask_1, new Object[] { paramTask }, 3272821424511182813L);
        return localObject1;
      }
      Object localObject1 = ref.newCall(this, operations, 1, -8065814382466137525L);
      try
      {
        ObjectOutput localObjectOutput = ((RemoteCall)localObject1).getOutputStream();
        localObjectOutput.writeObject(paramTask);
      }
      catch (IOException localIOException1)
      {
        throw new MarshalException("error marshalling arguments", localIOException1);
      }
      ref.invoke((RemoteCall)localObject1);
      Object localObject2;
      try
      {
        ObjectInput localObjectInput = ((RemoteCall)localObject1).getInputStream();
        localObject2 = localObjectInput.readObject();
      }
      catch (IOException localIOException2)
      {
        throw new UnmarshalException("error unmarshalling return", localIOException2);
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        throw new UnmarshalException("error unmarshalling return", localClassNotFoundException);
      }
      finally
      {
        ref.done((RemoteCall)localObject1);
      }
      return localObject2;
    }
    catch (RuntimeException localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (RemoteException localRemoteException)
    {
      throw localRemoteException;
    }
    catch (Exception localException)
    {
      throw new UnexpectedException("undeclared checked exception", localException);
    }
  }
}

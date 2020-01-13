package weka.experiment;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;
import java.util.Enumeration;
import java.util.Hashtable;
import weka.core.Queue;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;






























public class RemoteEngine
  extends UnicastRemoteObject
  implements Compute, RevisionHandler
{
  private static final long serialVersionUID = -1021538162895448259L;
  private String m_HostName = "local";
  

  private Queue m_TaskQueue = new Queue();
  

  private Queue m_TaskIdQueue = new Queue();
  

  private Hashtable m_TaskStatus = new Hashtable();
  

  private boolean m_TaskRunning = false;
  

  protected static long CLEANUPTIMEOUT = 3600000L;
  




  public RemoteEngine(String hostName)
    throws RemoteException
  {
    m_HostName = hostName;
    




    Thread cleanUpThread = new Thread()
    {
      public void run() {
        for (;;) {
          try {
            Thread.sleep(RemoteEngine.CLEANUPTIMEOUT);
          }
          catch (InterruptedException ie) {}
          if (m_TaskStatus.size() > 0) {
            RemoteEngine.this.purge();
          } else {
            System.err.println("RemoteEngine : purge - no tasks to check.");
          }
        }
      }
    };
    cleanUpThread.setPriority(1);
    cleanUpThread.setDaemon(true);
    cleanUpThread.start();
  }
  




  public synchronized Object executeTask(Task t)
    throws RemoteException
  {
    String taskId = "" + System.currentTimeMillis() + ":";
    taskId = taskId + t.hashCode();
    addTaskToQueue(t, taskId);
    
    return taskId;
  }
  







  public Object checkStatus(Object taskId)
    throws Exception
  {
    TaskStatusInfo inf = (TaskStatusInfo)m_TaskStatus.get(taskId);
    
    if (inf == null) {
      throw new Exception("RemoteEngine (" + m_HostName + ") : Task not found.");
    }
    
    TaskStatusInfo result = new TaskStatusInfo();
    result.setExecutionStatus(inf.getExecutionStatus());
    result.setStatusMessage(inf.getStatusMessage());
    result.setTaskResult(inf.getTaskResult());
    
    if ((inf.getExecutionStatus() == 3) || (inf.getExecutionStatus() == 2))
    {
      System.err.println("Finished/failed Task id : " + taskId + " checked by client. Removing.");
      
      inf.setTaskResult(null);
      inf = null;
      m_TaskStatus.remove(taskId);
    }
    inf = null;
    return result;
  }
  





  private synchronized void addTaskToQueue(Task t, String taskId)
  {
    TaskStatusInfo newTask = t.getTaskStatus();
    if (newTask == null) {
      newTask = new TaskStatusInfo();
    }
    m_TaskQueue.push(t);
    m_TaskIdQueue.push(taskId);
    newTask.setStatusMessage("RemoteEngine (" + m_HostName + ") : task " + taskId + " queued at postion: " + m_TaskQueue.size());
    



    m_TaskStatus.put(taskId, newTask);
    System.err.println("Task id : " + taskId + " Queued.");
    if (!m_TaskRunning) {
      startTask();
    }
  }
  




  private void startTask()
  {
    if ((!m_TaskRunning) && (m_TaskQueue.size() > 0))
    {
      Thread activeTaskThread = new Thread() {
        public void run() {
          m_TaskRunning = true;
          Task currentTask = (Task)m_TaskQueue.pop();
          String taskId = (String)m_TaskIdQueue.pop();
          TaskStatusInfo tsi = (TaskStatusInfo)m_TaskStatus.get(taskId);
          tsi.setExecutionStatus(1);
          tsi.setStatusMessage("RemoteEngine (" + m_HostName + ") : task " + taskId + " running...");
          
          try
          {
            System.err.println("Launching task id : " + taskId + "...");
            
            currentTask.execute();
            TaskStatusInfo runStatus = currentTask.getTaskStatus();
            tsi.setExecutionStatus(runStatus.getExecutionStatus());
            tsi.setStatusMessage("RemoteExperiment (" + m_HostName + ") " + runStatus.getStatusMessage());
            

            tsi.setTaskResult(runStatus.getTaskResult());
          }
          catch (Error er) {
            tsi.setExecutionStatus(2);
            if ((er.getCause() instanceof AccessControlException)) {
              tsi.setStatusMessage("RemoteEngine (" + m_HostName + ") : security error, check remote policy file.");
              

              System.err.println("Task id " + taskId + " Failed! Check remote policy file");
            }
            else {
              tsi.setStatusMessage("RemoteEngine (" + m_HostName + ") : unknown initialization error.");
              

              System.err.println("Task id " + taskId + " Unknown initialization error");
            }
          } catch (Exception ex) {
            tsi.setExecutionStatus(2);
            if ((ex instanceof FileNotFoundException)) {
              tsi.setStatusMessage("RemoteEngine (" + m_HostName + ") : " + ex.getMessage());
              

              System.err.println("Task id " + taskId + " Failed, " + ex.getMessage());
            }
            else {
              tsi.setStatusMessage("RemoteEngine (" + m_HostName + ") : task " + taskId + " failed.");
              

              System.err.println("Task id " + taskId + " Failed!");
            }
          } finally {
            if (m_TaskStatus.size() == 0) {
              RemoteEngine.this.purgeClasses();
            }
            m_TaskRunning = false;
            
            RemoteEngine.this.startTask();
          }
        }
      };
      activeTaskThread.setPriority(1);
      activeTaskThread.start();
    }
  }
  



  private void purgeClasses()
  {
    try
    {
      ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
      
      ClassLoader urlCl = URLClassLoader.newInstance(new URL[] { new URL("file:.") }, prevCl);
      
      Thread.currentThread().setContextClassLoader(urlCl);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  








  private void purge()
  {
    Enumeration keys = m_TaskStatus.keys();
    long currentTime = System.currentTimeMillis();
    System.err.println("RemoteEngine purge. Current time : " + currentTime);
    while (keys.hasMoreElements()) {
      String taskId = (String)keys.nextElement();
      System.err.print("Examining task id : " + taskId + "... ");
      String timeString = taskId.substring(0, taskId.indexOf(':'));
      long ts = Long.valueOf(timeString).longValue();
      if (currentTime - ts > CLEANUPTIMEOUT) {
        TaskStatusInfo tsi = (TaskStatusInfo)m_TaskStatus.get(taskId);
        if ((tsi != null) && ((tsi.getExecutionStatus() == 3) || (tsi.getExecutionStatus() == 2)))
        {

          System.err.println("\nTask id : " + taskId + " has gone stale. Removing.");
          
          m_TaskStatus.remove(taskId);
          tsi.setTaskResult(null);
          tsi = null;
        }
      } else {
        System.err.println("ok.");
      }
    }
    if (m_TaskStatus.size() == 0) {
      purgeClasses();
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.12 $");
  }
  






  public static void main(String[] args)
  {
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new RMISecurityManager());
    }
    
    int port = 1099;
    InetAddress localhost = null;
    try {
      localhost = InetAddress.getLocalHost();
      System.err.println("Host name : " + localhost.getHostName());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    String name;
    if (localhost != null) {
      name = localhost.getHostName();
    } else {
      name = "localhost";
    }
    
    try
    {
      String portOption = Utils.getOption("p", args);
      if (!portOption.equals(""))
        port = Integer.parseInt(portOption);
    } catch (Exception ex) {
      System.err.println("Usage : -p <port>");
    }
    
    if (port != 1099) {
      name = name + ":" + port;
    }
    String name = "//" + name + "/RemoteEngine";
    try
    {
      Compute engine = new RemoteEngine(name);
      try
      {
        Naming.rebind(name, engine);
        System.out.println("RemoteEngine bound in RMI registry");
      }
      catch (RemoteException ex) {
        System.err.println("Attempting to start RMI registry on port " + port + "...");
        LocateRegistry.createRegistry(port);
        Naming.bind(name, engine);
        System.out.println("RemoteEngine bound in RMI registry");
      }
    }
    catch (Exception e) {
      System.err.println("RemoteEngine exception: " + e.getMessage());
      
      e.printStackTrace();
    }
  }
}

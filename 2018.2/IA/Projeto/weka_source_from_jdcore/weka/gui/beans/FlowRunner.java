package weka.gui.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.RevisionHandler;
import weka.core.logging.Logger.Level;
import weka.gui.beans.xml.XMLBeans;






































public class FlowRunner
  implements RevisionHandler
{
  protected Vector m_beans;
  protected int m_runningCount = 0;
  
  protected transient weka.gui.Logger m_log = null;
  

  protected transient Environment m_env;
  
  protected boolean m_startSequentially = false;
  
  public static class SimpleLogger implements weka.gui.Logger {
    SimpleDateFormat m_DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public SimpleLogger() {}
    
    public void logMessage(String lm) { System.out.println(m_DateFormat.format(new Date()) + ": " + lm); }
    
    public void statusMessage(String lm)
    {
      System.out.println(m_DateFormat.format(new Date()) + ": " + lm);
    }
  }
  



  public FlowRunner()
  {
    KnowledgeFlowApp.loadProperties();
  }
  
  public void setLog(weka.gui.Logger log) {
    m_log = log;
  }
  
  protected void runSequentially(TreeMap<Integer, Startable> startables) {
    Set<Integer> s = startables.keySet();
    for (Integer i : s) {
      try {
        Startable startPoint = (Startable)startables.get(i);
        startPoint.start();
        Thread.sleep(200L);
        waitUntilFinished();
      } catch (Exception ex) {
        ex.printStackTrace();
        if (m_log != null) {
          m_log.logMessage(ex.getMessage());
          Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_RunSequentially_LogMessage_Text_First"));
        } else {
          System.err.println(ex.getMessage());
          Messages.getInstance();System.err.println(Messages.getString("FlowRunner_RunSequentially_LogMessage_Text_Second"));
        }
        break;
      }
    }
  }
  
  protected synchronized void launchThread(final Startable s, final int flowNum) {
    Thread t = new Thread() {
      private int m_num = flowNum;
      
      public void run() {
        try { s.start();
        } catch (Exception ex) {
          ex.printStackTrace();
          if (m_log != null) {
            m_log.logMessage(ex.getMessage());
          } else {
            System.err.println(ex.getMessage());

          }
          


        }
        finally
        {

          decreaseCount();
        }
      }
    };
    m_runningCount += 1;
    t.setPriority(1);
    t.start();
  }
  
  protected synchronized void decreaseCount() {
    m_runningCount -= 1;
  }
  
  public synchronized void stopAllFlows() {
    for (int i = 0; i < m_beans.size(); i++) {
      BeanInstance temp = (BeanInstance)m_beans.elementAt(i);
      if ((temp.getBean() instanceof BeanCommon))
      {
        ((BeanCommon)temp.getBean()).stop();
      }
    }
  }
  


  public void waitUntilFinished()
  {
    try
    {
      while (m_runningCount > 0) {
        Thread.sleep(200L);
      }
      

      for (;;)
      {
        boolean busy = false;
        for (int i = 0; i < m_beans.size(); i++) {
          BeanInstance temp = (BeanInstance)m_beans.elementAt(i);
          if (((temp.getBean() instanceof BeanCommon)) && 
            (((BeanCommon)temp.getBean()).isBusy())) {
            busy = true;
            break;
          }
        }
        
        if (!busy) break;
        Thread.sleep(3000L);
      }
      
    }
    catch (Exception ex)
    {
      if (m_log != null) {
        Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_WaitUntilFinished_LogMessage_Text"));
      } else {
        Messages.getInstance();System.err.println(Messages.getString("FlowRunner_WaitUntilFinished_Error_Text"));
      }
      stopAllFlows();
    }
  }
  





  public void load(String fileName)
    throws Exception
  {
    if ((!fileName.endsWith(".kf")) && (!fileName.endsWith(".kfml"))) {
      Messages.getInstance();throw new Exception(Messages.getString("FlowRunner_Load_Exception_Text"));
    }
    
    if (fileName.endsWith(".kf")) {
      loadBinary(fileName);
    } else if (fileName.endsWith(".kfml")) {
      loadXML(fileName);
    }
  }
  




  public void loadBinary(String fileName)
    throws Exception
  {
    if (!fileName.endsWith(".kf")) {
      Messages.getInstance();throw new Exception(Messages.getString("FlowRunner_LoadBinary_Exception_Text_First"));
    }
    
    InputStream is = new FileInputStream(fileName);
    ObjectInputStream ois = new ObjectInputStream(is);
    m_beans = ((Vector)ois.readObject());
    

    ois.close();
    
    if (m_env != null) {
      String parentDir = new File(fileName).getParent();
      if (parentDir == null) {
        parentDir = "./";
      }
      m_env.addVariable("Internal.knowledgeflow.directory", parentDir);
    }
  }
  





  public void loadXML(String fileName)
    throws Exception
  {
    if (!fileName.endsWith(".kfml")) {
      Messages.getInstance();throw new Exception(Messages.getString("FlowRunner_LoadXML_Exception_Text"));
    }
    
    XMLBeans xml = new XMLBeans(null, null);
    Vector v = (Vector)xml.read(new File(fileName));
    m_beans = ((Vector)v.get(0));
    
    if (m_env != null) {
      String parentDir = new File(fileName).getParent();
      if (parentDir == null) {
        parentDir = "./";
      }
      m_env.addVariable("Internal.knowledgeflow.directory", parentDir);
    }
    else {
      Messages.getInstance();System.err.println(Messages.getString("FlowRunner_LoadXML_Error_Text"));
    }
  }
  




  public Vector getFlows()
  {
    return m_beans;
  }
  




  public void setFlows(Vector beans)
  {
    m_beans = beans;
  }
  







  public void setEnvironment(Environment env)
  {
    m_env = env;
  }
  




  public Environment getEnvironment()
  {
    return m_env;
  }
  





  public void setStartSequentially(boolean s)
  {
    m_startSequentially = s;
  }
  





  public boolean getStartSequentially()
  {
    return m_startSequentially;
  }
  



  public void run()
    throws Exception
  {
    if (m_beans == null) {
      Messages.getInstance();throw new Exception(Messages.getString("FlowRunner_Run_Exception_Text"));
    }
    

    for (int i = 0; i < m_beans.size(); i++) {
      BeanInstance tempB = (BeanInstance)m_beans.elementAt(i);
      if ((m_log != null) && 
        ((tempB.getBean() instanceof BeanCommon))) {
        ((BeanCommon)tempB.getBean()).setLog(m_log);
      }
      

      if ((tempB.getBean() instanceof EnvironmentHandler)) {
        ((EnvironmentHandler)tempB.getBean()).setEnvironment(m_env);
      }
    }
    
    int numFlows = 1;
    
    if (m_log != null) {
      if (m_startSequentially) {
        Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_Run_LogMessage_Text_First"));
      } else {
        Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_Run_LogMessage_Text_Second"));
      }
    }
    TreeMap<Integer, Startable> startables = new TreeMap();
    
    for (int i = 0; i < m_beans.size(); i++) {
      BeanInstance tempB = (BeanInstance)m_beans.elementAt(i);
      if ((tempB.getBean() instanceof Startable)) {
        Startable s = (Startable)tempB.getBean();
        
        if (!m_startSequentially) {
          if (s.getStartMessage().charAt(0) != '$') {
            if (m_log != null) {
              Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_Run_LogMessage_Text_Third") + numFlows + Messages.getString("FlowRunner_Run_LogMessage_Text_Fourth"));
            } else {
              Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("FlowRunner_Run_Text_First") + numFlows + Messages.getString("FlowRunner_Run_Text_Second"));
            }
            launchThread(s, numFlows);
            numFlows++;
          } else {
            String beanName = s.getClass().getName();
            if ((s instanceof BeanCommon)) {
              String customName = ((BeanCommon)s).getCustomName();
              beanName = customName;
            }
            if (m_log != null) {
              Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_Run_LogMessage_Text_Fifth") + beanName + Messages.getString("FlowRunner_Run_LogMessage_Text_Sixth"));
            } else {
              Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("FlowRunner_Run_Text_Third") + beanName + Messages.getString("FlowRunner_Run_Text_Fourth"));
            }
          }
        } else {
          boolean ok = false;
          Integer position = null;
          String beanName = s.getClass().getName();
          if ((s instanceof BeanCommon)) {
            String customName = ((BeanCommon)s).getCustomName();
            beanName = customName;
            
            if (customName.indexOf(':') > 0) {
              String startPos = customName.substring(0, customName.indexOf(':'));
              try {
                position = new Integer(startPos);
                ok = true;
              }
              catch (NumberFormatException n) {}
            }
          }
          
          if (!ok) {
            if (startables.size() == 0) {
              position = new Integer(0);
            } else {
              int newPos = ((Integer)startables.lastKey()).intValue();
              newPos++;
              position = new Integer(newPos);
            }
          }
          
          if (s.getStartMessage().charAt(0) != '$') {
            if (m_log != null) {
              Messages.getInstance();Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_Run_LogMessage_Text_Seventh") + beanName + Messages.getString("FlowRunner_Run_LogMessage_Text_Eighth") + position + Messages.getString("FlowRunner_Run_LogMessage_Text_Nineth"));
            }
            else
            {
              Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("FlowRunner_Run_Text_Fifth") + beanName + Messages.getString("FlowRunner_Run_Text_Sixth") + position + Messages.getString("FlowRunner_Run_Text_Seventh"));
            }
            
            startables.put(position, s);
          }
          else if (m_log != null) {
            Messages.getInstance();Messages.getInstance();m_log.logMessage(Messages.getString("FlowRunner_Run_LogMessage_Text_Tenth") + beanName + Messages.getString("FlowRunner_Run_LogMessage_Text_Eleventh"));
          } else {
            Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("FlowRunner_Run_Text_Eighth") + beanName + Messages.getString("FlowRunner_Run_Text_Nineth"));
          }
        }
      }
    }
    

    if (m_startSequentially) {
      runSequentially(startables);
    }
  }
  






  public static void main(String[] args)
  {
    Messages.getInstance();weka.core.logging.Logger.log(Logger.Level.INFO, Messages.getString("FlowRunner_Main_Logger_Text"));
    if (args.length < 1) {
      Messages.getInstance();System.err.println(Messages.getString("FlowRunner_Main_Error_Text"));
    } else {
      try {
        FlowRunner fr = new FlowRunner();
        SimpleLogger sl = new SimpleLogger();
        String fileName = args[0];
        
        if ((args.length == 2) && (args[1].equals("-s"))) {
          fr.setStartSequentially(true);
        }
        

        Environment env = Environment.getSystemWide();
        
        fr.setLog(sl);
        fr.setEnvironment(env);
        
        fr.load(fileName);
        fr.run();
        fr.waitUntilFinished();
        Messages.getInstance();System.out.println(Messages.getString("FlowRunner_Main_Text"));
        System.exit(1);
      } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println(ex.getMessage());
      }
    }
  }
  
  public String getRevision() {
    return "$Revision: 7059 $";
  }
}

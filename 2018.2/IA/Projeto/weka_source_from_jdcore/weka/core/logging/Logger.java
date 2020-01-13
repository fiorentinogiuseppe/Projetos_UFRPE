package weka.core.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Properties;
import weka.core.RevisionHandler;
import weka.core.Utils;
import weka.gui.LogPanel;






























public abstract class Logger
  implements RevisionHandler
{
  public static final String PROPERTIES_FILE = "weka/core/logging/Logging.props";
  protected Level m_MinLevel;
  protected static Logger m_Singleton;
  protected static Properties m_Properties;
  protected static SimpleDateFormat m_DateFormat;
  
  public static enum Level
  {
    ALL(0), 
    
    FINEST(1), 
    
    FINER(2), 
    
    FINE(3), 
    
    INFO(4), 
    
    WARNING(5), 
    
    SEVERE(6), 
    
    OFF(10);
    



    private int m_Order;
    


    private Level(int order)
    {
      m_Order = order;
    }
    




    public int getOrder()
    {
      return m_Order;
    }
  }
  










  static
  {
    try
    {
      m_Properties = Utils.readProperties("weka/core/logging/Logging.props");
    }
    catch (Exception e) {
      System.err.println("Error reading the logging properties 'weka/core/logging/Logging.props': " + e);
      
      m_Properties = new Properties();
    }
  }
  




  public Logger()
  {
    initialize();
  }
  


  protected void initialize()
  {
    m_MinLevel = Level.valueOf(m_Properties.getProperty("MinLevel", "INFO"));
  }
  





  public Level getMinLevel()
  {
    return m_MinLevel;
  }
  










  protected static String[] getLocation()
  {
    String[] result = new String[3];
    
    Throwable t = new Throwable();
    t.fillInStackTrace();
    StackTraceElement[] trace = t.getStackTrace();
    
    for (int i = 0; i < trace.length; i++)
    {
      if (!trace[i].getClassName().equals(Logger.class.getName()))
      {

        if (!trace[i].getClassName().equals(LogPanel.class.getName()))
        {


          result[0] = trace[i].getClassName();
          result[1] = trace[i].getMethodName();
          result[2] = ("" + trace[i].getLineNumber());
          break;
        } }
    }
    return result;
  }
  


















  public static Logger getSingleton()
  {
    if (m_Singleton == null)
    {
      String classname = m_Properties.getProperty("Logger", ConsoleLogger.class.getName());
      try {
        m_Singleton = (Logger)Class.forName(classname).newInstance();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      

      m_DateFormat = new SimpleDateFormat(m_Properties.getProperty("DateFormat", "yyyy-MM-dd HH:mm:ss"));
    }
    
    return m_Singleton;
  }
  









  public static void log(Level level, String msg)
  {
    Logger logger = getSingleton();
    if (logger == null) {
      return;
    }
    synchronized (logger) {
      boolean log = false;
      if (logger.getMinLevel() == Level.ALL) {
        log = true;
      } else if (level.getOrder() >= logger.getMinLevel().getOrder())
        log = true;
      if (!log)
        return;
      String[] location = getLocation();
      logger.doLog(level, msg, location[0], location[1], Integer.parseInt(location[2]));
    }
  }
  








  public static void log(Level level, Throwable t)
  {
    StringWriter swriter = new StringWriter();
    PrintWriter pwriter = new PrintWriter(swriter);
    t.printStackTrace(pwriter);
    pwriter.close();
    
    log(level, swriter.toString());
  }
  
  protected abstract void doLog(Level paramLevel, String paramString1, String paramString2, String paramString3, int paramInt);
}

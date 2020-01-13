package weka.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;





























public class Debug
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 66171861743328020L;
  public static final Level ALL = Level.ALL;
  
  public static final Level CONFIG = Level.CONFIG;
  
  public static final Level FINE = Level.FINE;
  
  public static final Level FINER = Level.FINER;
  
  public static final Level FINEST = Level.FINEST;
  
  public static final Level INFO = Level.INFO;
  
  public static final Level OFF = Level.OFF;
  
  public static final Level SEVERE = Level.SEVERE;
  
  public static final Level WARNING = Level.WARNING;
  

  protected boolean m_Enabled = true;
  

  protected Log m_Log;
  

  protected Clock m_Clock = new Clock();
  




  public static class Clock
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 4622161807307942201L;
    



    public static final int FORMAT_MILLISECONDS = 0;
    



    public static final int FORMAT_SECONDS = 1;
    



    public static final int FORMAT_HHMMSS = 2;
    



    public static final Tag[] TAGS_FORMAT = { new Tag(0, "milli-seconds"), new Tag(1, "seconds"), new Tag(2, "hh:mm:ss") };
    





    public int m_OutputFormat = 1;
    


    protected long m_Start;
    


    protected long m_Stop;
    


    protected boolean m_Running;
    

    protected long m_ThreadID;
    

    protected boolean m_CanMeasureCpuTime;
    

    protected boolean m_UseCpuTime;
    

    protected transient ThreadMXBean m_ThreadMonitor;
    


    public Clock()
    {
      this(true);
    }
    






    public Clock(int format)
    {
      this(true, format);
    }
    






    public Clock(boolean start)
    {
      this(start, 1);
    }
    







    public Clock(boolean start, int format)
    {
      m_Running = false;
      m_Start = 0L;
      m_Stop = 0L;
      m_UseCpuTime = true;
      setOutputFormat(format);
      
      if (start) {
        start();
      }
    }
    

    protected void init()
    {
      m_ThreadMonitor = null;
      m_ThreadMonitor = getThreadMonitor();
      

      m_CanMeasureCpuTime = m_ThreadMonitor.isThreadCpuTimeSupported();
    }
    










    public boolean isCpuTime()
    {
      return (m_UseCpuTime) && (m_CanMeasureCpuTime);
    }
    






    public void setUseCpuTime(boolean value)
    {
      m_UseCpuTime = value;
      


      if (m_Running) {
        stop();
        start();
      }
    }
    





    public boolean getUseCpuTime()
    {
      return m_UseCpuTime;
    }
    






    protected ThreadMXBean getThreadMonitor()
    {
      if (m_ThreadMonitor == null) {
        m_ThreadMonitor = ManagementFactory.getThreadMXBean();
        if ((m_CanMeasureCpuTime) && (!m_ThreadMonitor.isThreadCpuTimeEnabled()))
          m_ThreadMonitor.setThreadCpuTimeEnabled(true);
        m_ThreadID = Thread.currentThread().getId();
      }
      
      return m_ThreadMonitor;
    }
    


    protected long getCurrentTime()
    {
      long result;
      
      long result;
      
      if (isCpuTime()) {
        result = getThreadMonitor().getThreadUserTime(m_ThreadID) / 1000000L;
      } else {
        result = System.currentTimeMillis();
      }
      return result;
    }
    





    public void start()
    {
      init();
      
      m_Start = getCurrentTime();
      m_Stop = m_Start;
      m_Running = true;
    }
    




    public void stop()
    {
      m_Stop = getCurrentTime();
      m_Running = false;
    }
    




    public long getStart()
    {
      return m_Start;
    }
    


    public long getStop()
    {
      long result;
      
      long result;
      
      if (isRunning()) {
        result = getCurrentTime();
      } else {
        result = m_Stop;
      }
      return result;
    }
    




    public boolean isRunning()
    {
      return m_Running;
    }
    





    public void setOutputFormat(int value)
    {
      if (value == 0) {
        m_OutputFormat = value;
      } else if (value == 1) {
        m_OutputFormat = value;
      } else if (value == 2) {
        m_OutputFormat = value;
      } else {
        System.out.println("Format '" + value + "' is not recognized!");
      }
    }
    




    public int getOutputFormat()
    {
      return m_OutputFormat;
    }
    













    public String toString()
    {
      String result = "";
      long elapsed = getStop() - getStart();
      
      switch (getOutputFormat()) {
      case 2: 
        long hours = elapsed / 3600000L;
        elapsed %= 3600000L;
        long mins = elapsed / 60000L;
        elapsed %= 60000L;
        long secs = elapsed / 1000L;
        long msecs = elapsed % 1000L;
        
        if (hours > 0L) {
          result = result + "" + hours + ":";
        }
        if (mins < 10L) {
          result = result + "0" + mins + ":";
        } else {
          result = result + "" + mins + ":";
        }
        if (secs < 10L) {
          result = result + "0" + secs + ".";
        } else {
          result = result + "" + secs + ".";
        }
        result = result + Utils.doubleToString(msecs / 1000.0D, 3).replaceAll(".*\\.", "");
        
        break;
      
      case 1: 
        result = Utils.doubleToString(elapsed / 1000.0D, 3) + "s";
        break;
      
      case 0: 
        result = "" + elapsed + "ms";
        break;
      
      default: 
        result = "<unknown time format>";
      }
      
      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 7519 $");
    }
  }
  




  public static class Timestamp
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -6099868388466922753L;
    



    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    



    protected Date m_Stamp;
    


    protected String m_Format;
    


    protected SimpleDateFormat m_Formatter;
    



    public Timestamp()
    {
      this("yyyy-MM-dd HH:mm:ss");
    }
    






    public Timestamp(String format)
    {
      this(new Date(), format);
    }
    




    public Timestamp(Date stamp)
    {
      this(stamp, "yyyy-MM-dd HH:mm:ss");
    }
    








    public Timestamp(Date stamp, String format)
    {
      m_Stamp = stamp;
      setFormat(format);
    }
    




    public void setFormat(String value)
    {
      try
      {
        m_Formatter = new SimpleDateFormat(value);
        m_Format = value;
      }
      catch (Exception e) {
        m_Formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        m_Format = "yyyy-MM-dd HH:mm:ss";
      }
    }
    




    public String getFormat()
    {
      return m_Format;
    }
    




    public Date getStamp()
    {
      return m_Stamp;
    }
    




    public String toString()
    {
      return m_Formatter.format(getStamp());
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 7519 $");
    }
  }
  






  public static class SimpleLog
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -2671928223819510830L;
    





    protected String m_Filename = null;
    


    public SimpleLog()
    {
      this(null);
    }
    





    public SimpleLog(String filename)
    {
      this(filename, true);
    }
    








    public SimpleLog(String filename, boolean append)
    {
      m_Filename = filename;
      
      Debug.writeToFile(m_Filename, "--> Log started", append);
    }
    




    public String getFilename()
    {
      return m_Filename;
    }
    






    public void log(String message)
    {
      String log = new Debug.Timestamp() + " " + message;
      
      if (getFilename() != null) {
        Debug.writeToFile(getFilename(), log);
      }
      System.out.println(log);
    }
    





    public void logSystemInfo()
    {
      log("SystemInfo:\n" + new SystemInfo().toString());
    }
    






    public String toString()
    {
      String result = "Filename: " + getFilename();
      
      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 7519 $");
    }
  }
  







  public static class Log
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 1458435732111675823L;
    





    protected transient Logger m_Logger = null;
    

    protected String m_Filename = null;
    

    protected int m_Size;
    

    protected int m_NumFiles;
    

    protected boolean m_LoggerInitFailed = false;
    


    public Log()
    {
      this(null);
    }
    





    public Log(String filename)
    {
      this(filename, 1000000, 1);
    }
    







    public Log(String filename, int size, int numFiles)
    {
      m_Filename = filename;
      m_Size = size;
      m_NumFiles = numFiles;
    }
    





    protected Logger getLogger()
    {
      if ((m_Logger == null) && (!m_LoggerInitFailed) && 
        (m_Filename != null)) {
        m_Logger = Logger.getLogger(m_Filename);
        Handler fh = null;
        try {
          fh = new FileHandler(m_Filename, m_Size, m_NumFiles);
          fh.setFormatter(new SimpleFormatter());
          m_Logger.addHandler(fh);
          m_LoggerInitFailed = false;
        }
        catch (Exception e) {
          System.out.println("Cannot init fileHandler for logger:" + e.toString());
          m_Logger = null;
          m_LoggerInitFailed = true;
        }
      }
      

      return m_Logger;
    }
    


    public static Level stringToLevel(String level)
    {
      Level result;
      

      Level result;
      

      if (level.equalsIgnoreCase("ALL")) {
        result = Debug.ALL; } else { Level result;
        if (level.equalsIgnoreCase("CONFIG")) {
          result = Debug.CONFIG; } else { Level result;
          if (level.equalsIgnoreCase("FINE")) {
            result = Debug.FINE; } else { Level result;
            if (level.equalsIgnoreCase("FINER")) {
              result = Debug.FINER; } else { Level result;
              if (level.equalsIgnoreCase("FINEST")) {
                result = Debug.FINEST; } else { Level result;
                if (level.equalsIgnoreCase("INFO")) {
                  result = Debug.INFO; } else { Level result;
                  if (level.equalsIgnoreCase("OFF")) {
                    result = Debug.OFF; } else { Level result;
                    if (level.equalsIgnoreCase("SEVERE")) {
                      result = Debug.SEVERE; } else { Level result;
                      if (level.equalsIgnoreCase("WARNING")) {
                        result = Debug.WARNING;
                      } else
                        result = Debug.ALL;
                    } } } } } } } }
      return result;
    }
    




    public String getFilename()
    {
      return m_Filename;
    }
    




    public int getSize()
    {
      return m_Size;
    }
    




    public int getNumFiles()
    {
      return m_NumFiles;
    }
    





    public void log(Level level, String message)
    {
      log(level, "", message);
    }
    






    public void log(Level level, String sourceclass, String message)
    {
      log(level, sourceclass, "", message);
    }
    









    public void log(Level level, String sourceclass, String sourcemethod, String message)
    {
      Logger logger = getLogger();
      
      if (logger != null) {
        logger.logp(level, sourceclass, sourcemethod, message);
      } else {
        System.out.println(message);
      }
    }
    




    public void logSystemInfo()
    {
      log(Debug.INFO, "SystemInfo:\n" + new SystemInfo().toString());
    }
    






    public String toString()
    {
      String result = "Filename: " + getFilename() + ", " + "Size: " + getSize() + ", " + "# Files: " + getNumFiles();
      


      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 7519 $");
    }
  }
  






  public static class Random
    extends Random
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 1256846887618333956L;
    





    protected boolean m_Debug = false;
    

    protected long m_ID;
    

    protected static long m_CurrentID;
    

    protected Debug.Log m_Log = null;
    


    public Random()
    {
      this(false);
    }
    





    public Random(long seed)
    {
      this(seed, false);
    }
    





    public Random(boolean debug)
    {
      setDebug(debug);
      m_ID = nextID();
      if (getDebug()) {
        printStackTrace();
      }
    }
    





    public Random(long seed, boolean debug)
    {
      super();
      setDebug(debug);
      m_ID = nextID();
      if (getDebug()) {
        printStackTrace();
      }
    }
    



    public void setDebug(boolean value)
    {
      m_Debug = value;
    }
    




    public boolean getDebug()
    {
      return m_Debug;
    }
    




    public void setLog(Debug.Log value)
    {
      m_Log = value;
    }
    





    public Debug.Log getLog()
    {
      return m_Log;
    }
    




    protected static long nextID()
    {
      m_CurrentID += 1L;
      
      return m_CurrentID;
    }
    




    public long getID()
    {
      return m_ID;
    }
    





    protected void println(String msg)
    {
      if (getDebug()) {
        if (getLog() != null) {
          getLog().log(Level.INFO, m_ID + ": " + msg);
        } else {
          System.out.println(m_ID + ": " + msg);
        }
      }
    }
    




    public void printStackTrace()
    {
      StringWriter writer = new StringWriter();
      

      Throwable t = new Throwable();
      t.fillInStackTrace();
      t.printStackTrace(new PrintWriter(writer));
      
      println(writer.toString());
    }
    





    public boolean nextBoolean()
    {
      boolean result = super.nextBoolean();
      println("nextBoolean=" + result);
      return result;
    }
    




    public void nextBytes(byte[] bytes)
    {
      super.nextBytes(bytes);
      println("nextBytes=" + Utils.arrayToString(bytes));
    }
    





    public double nextDouble()
    {
      double result = super.nextDouble();
      println("nextDouble=" + result);
      return result;
    }
    





    public float nextFloat()
    {
      float result = super.nextFloat();
      println("nextFloat=" + result);
      return result;
    }
    






    public double nextGaussian()
    {
      double result = super.nextGaussian();
      println("nextGaussian=" + result);
      return result;
    }
    





    public int nextInt()
    {
      int result = super.nextInt();
      println("nextInt=" + result);
      return result;
    }
    







    public int nextInt(int n)
    {
      int result = super.nextInt(n);
      println("nextInt(" + n + ")=" + result);
      return result;
    }
    





    public long nextLong()
    {
      long result = super.nextLong();
      println("nextLong=" + result);
      return result;
    }
    




    public void setSeed(long seed)
    {
      super.setSeed(seed);
      println("setSeed(" + seed + ")");
    }
    




    public String toString()
    {
      return getClass().getName() + ": " + getID();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 7519 $");
    }
  }
  




  public static class DBO
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -5245628124742606784L;
    



    public boolean m_verboseOn = false;
    

    public Range m_outputTypes = new Range();
    
    public DBO() {}
    
    public void setVerboseOn()
    {
      m_verboseOn = true;
    }
    




    public void initializeRanges(int upper)
    {
      m_outputTypes.setUpper(upper);
    }
    





    public boolean outputTypeSet(int num)
    {
      return m_outputTypes.isInRange(num);
    }
    






    public boolean dl(int num)
    {
      return outputTypeSet(num);
    }
    




    public void setOutputTypes(String list)
    {
      if (list.length() > 0) {
        m_verboseOn = true;
        
        m_outputTypes.setRanges(list);
        m_outputTypes.setUpper(30);
      }
    }
    




    public String getOutputTypes()
    {
      return m_outputTypes.getRanges();
    }
    





    public void dpln(String text)
    {
      if (m_verboseOn) {
        System.out.println(text);
      }
    }
    






    public void dpln(int debugType, String text)
    {
      if (outputTypeSet(debugType)) {
        System.out.println(text);
      }
    }
    





    public void dp(String text)
    {
      if (m_verboseOn) {
        System.out.print(text);
      }
    }
    






    public void dp(int debugType, String text)
    {
      if (outputTypeSet(debugType)) {
        System.out.print(text);
      }
    }
    





    public static void pln(String text)
    {
      System.out.println(text);
    }
    





    public static void p(String text)
    {
      System.out.print(text);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 7519 $");
    }
  }
  


  public Debug()
  {
    this(null);
  }
  





  public Debug(String filename)
  {
    this(filename, 1000000, 1);
  }
  








  public Debug(String filename, int size, int numFiles)
  {
    m_Log = newLog(filename, size, numFiles);
  }
  






  public static Level stringToLevel(String level)
  {
    return Log.stringToLevel(level);
  }
  







  public static Log newLog(String filename, int size, int numFiles)
  {
    return new Log(filename, size, numFiles);
  }
  




  public void log(String message)
  {
    log(INFO, message);
  }
  





  public void log(Level level, String message)
  {
    log(level, "", message);
  }
  






  public void log(Level level, String sourceclass, String message)
  {
    log(level, sourceclass, "", message);
  }
  







  public void log(Level level, String sourceclass, String sourcemethod, String message)
  {
    if (getEnabled()) {
      m_Log.log(level, sourceclass, sourcemethod, message);
    }
  }
  



  public void setEnabled(boolean value)
  {
    m_Enabled = value;
  }
  




  public boolean getEnabled()
  {
    return m_Enabled;
  }
  




  public static Clock newClock()
  {
    return new Clock();
  }
  




  public Clock getClock()
  {
    return m_Clock;
  }
  


  public void startClock()
  {
    m_Clock.start();
  }
  






  public void stopClock(String message)
  {
    log(message + ": " + m_Clock);
  }
  





  public static Random newRandom()
  {
    return new Random(true);
  }
  






  public static Random newRandom(int seed)
  {
    return new Random(seed, true);
  }
  




  public static Timestamp newTimestamp()
  {
    return new Timestamp();
  }
  




  public static String getTempDir()
  {
    return System.getProperty("java.io.tmpdir");
  }
  




  public static String getHomeDir()
  {
    return System.getProperty("user.home");
  }
  




  public static String getCurrentDir()
  {
    return System.getProperty("user.dir");
  }
  







  public static boolean writeToFile(String filename, Object obj)
  {
    return writeToFile(filename, obj, true);
  }
  







  public static boolean writeToFile(String filename, String message)
  {
    return writeToFile(filename, message, true);
  }
  









  public static boolean writeToFile(String filename, Object obj, boolean append)
  {
    return writeToFile(filename, obj.toString(), append);
  }
  





  public static boolean writeToFile(String filename, String message, boolean append)
  {
    boolean result;
    



    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filename, append));
      writer.write(message);
      writer.newLine();
      writer.flush();
      writer.close();
      result = true;
    }
    catch (Exception e) {
      result = false;
    }
    
    return result;
  }
  




  public static boolean saveToFile(String filename, Object o)
  {
    boolean result;
    


    if (SerializationHelper.isSerializable(o.getClass())) {
      try {
        SerializationHelper.write(filename, o);
        result = true;
      }
      catch (Exception e) {
        boolean result = false;
      }
      
    } else {
      result = false;
    }
    
    return result;
  }
  



  public static Object loadFromFile(String filename)
  {
    Object result;
    


    try
    {
      result = SerializationHelper.read(filename);
    }
    catch (Exception e) {
      result = null;
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7519 $");
  }
}

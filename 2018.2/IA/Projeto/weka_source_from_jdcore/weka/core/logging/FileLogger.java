package weka.core.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import weka.core.RevisionUtils;































public class FileLogger
  extends ConsoleLogger
{
  protected File m_LogFile;
  protected String m_LineFeed;
  
  public FileLogger() {}
  
  protected void initialize()
  {
    super.initialize();
    

    m_LogFile = getLogFile();
    try
    {
      if ((m_LogFile != null) && (m_LogFile.exists())) {
        m_LogFile.delete();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    

    m_LineFeed = System.getProperty("line.separator");
  }
  







  protected File getLogFile()
  {
    String filename = m_Properties.getProperty("LogFile", "%h" + File.separator + "weka.log");
    
    filename = filename.replaceAll("%t", Matcher.quoteReplacement(System.getProperty("java.io.tmpdir")));
    filename = filename.replaceAll("%h", Matcher.quoteReplacement(System.getProperty("user.home")));
    filename = filename.replaceAll("%c", Matcher.quoteReplacement(System.getProperty("user.dir")));
    if ((System.getProperty("%") != null) && (System.getProperty("%").length() > 0)) {
      filename = filename.replaceAll("%%", Matcher.quoteReplacement(System.getProperty("%")));
    }
    
    File result = new File(filename);
    
    return result;
  }
  






  protected void append(String s)
  {
    if (m_LogFile == null) {
      return;
    }
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(m_LogFile, true));
      writer.write(s);
      writer.flush();
      writer.close();
    }
    catch (Exception e) {}
  }
  











  protected void doLog(Logger.Level level, String msg, String cls, String method, int lineno)
  {
    super.doLog(level, msg, cls, method, lineno);
    

    append(m_DateFormat.format(new Date()) + " " + cls + " " + method + m_LineFeed + level + ": " + msg + m_LineFeed);
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7462 $");
  }
}

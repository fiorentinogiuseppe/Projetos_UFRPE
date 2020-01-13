package weka.core.logging;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import weka.core.RevisionUtils;

































public class ConsoleLogger
  extends Logger
{
  public ConsoleLogger() {}
  
  protected void doLog(Logger.Level level, String msg, String cls, String method, int lineno)
  {
    System.err.println(m_DateFormat.format(new Date()) + " " + cls + " " + method + "\n" + level + ": " + msg);
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4716 $");
  }
}

package weka.gui;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;































public class SysErrLog
  implements Logger
{
  public SysErrLog() {}
  
  protected static String getTimestamp()
  {
    return new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());
  }
  






  public void logMessage(String message)
  {
    Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("SysErrLog_LogMessage_Text_First") + getTimestamp() + Messages.getString("SysErrLog_LogMessage_Text_Second") + message);
  }
  






  public void statusMessage(String message)
  {
    Messages.getInstance();System.err.println(Messages.getString("SysErrLog_StatusMessage_Text") + message);
  }
}

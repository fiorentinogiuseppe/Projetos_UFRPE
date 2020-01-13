package weka.gui;

import java.awt.Component;
import java.io.PrintStream;
import java.lang.reflect.Method;
import javax.swing.JOptionPane;


































public class BrowserHelper
{
  public static final String[] LINUX_BROWSERS = { "firefox", "google-chrome", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
  


  public BrowserHelper() {}
  

  public static void openURL(String url)
  {
    openURL(null, url);
  }
  





  public static void openURL(Component parent, String url)
  {
    openURL(parent, url, true);
  }
  







  public static void openURL(Component parent, String url, boolean showDialog)
  {
    String osName = System.getProperty("os.name");
    try
    {
      if (osName.startsWith("Mac OS")) {
        Class fileMgr = Class.forName("com.apple.eio.FileManager");
        Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
        openURL.invoke(null, new Object[] { url });

      }
      else if (osName.startsWith("Windows")) {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
      }
      else
      {
        String browser = null;
        for (int count = 0; (count < LINUX_BROWSERS.length) && (browser == null); count++)
        {
          if (Runtime.getRuntime().exec(new String[] { "which", LINUX_BROWSERS[count] }).waitFor() == 0) {
            browser = LINUX_BROWSERS[count];
            break;
          }
        }
        if (browser == null) {
          Messages.getInstance();throw new Exception(Messages.getString("BrowserHelper_Exception_Text"));
        }
        Runtime.getRuntime().exec(new String[] { browser, url });
      }
    }
    catch (Exception e) {
      Messages.getInstance();String errMsg = Messages.getString("BrowserHelper_Exception_ErrMsg_Text") + e.getMessage();
      
      if (showDialog) {
        JOptionPane.showMessageDialog(parent, errMsg);
      }
      else {
        System.err.println(errMsg);
      }
    }
  }
}

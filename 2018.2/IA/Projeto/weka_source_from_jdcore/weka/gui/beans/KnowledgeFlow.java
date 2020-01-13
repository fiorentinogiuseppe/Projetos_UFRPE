package weka.gui.beans;

import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.SplashWindow;


























public class KnowledgeFlow
{
  public KnowledgeFlow() {}
  
  public static void startApp()
  {
    KnowledgeFlowApp.addStartupListener(new StartUpListener()
    {

      public void startUpComplete() {}

    });
    SplashWindow.splash(ClassLoader.getSystemResource("weka/gui/beans/icons/splash.jpg"));
    

    Thread nt = new Thread() {
      public void run() {
        SplashWindow.invokeMethod("weka.gui.beans.KnowledgeFlowApp", "createSingleton", null);
      }
    };
    nt.start();
  }
  




  public static void main(String[] args)
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("KnowledgeFlow_Main_Logger_Text"));
    SplashWindow.splash(ClassLoader.getSystemResource("weka/gui/beans/icons/splash.jpg"));
    
    SplashWindow.invokeMain("weka.gui.beans.KnowledgeFlowApp", args);
    SplashWindow.disposeSplash();
  }
}

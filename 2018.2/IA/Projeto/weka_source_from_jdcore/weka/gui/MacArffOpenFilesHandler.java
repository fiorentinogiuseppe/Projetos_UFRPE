package weka.gui;

import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.OpenFilesHandler;
import java.io.File;
import java.io.PrintStream;
import java.util.List;




























public class MacArffOpenFilesHandler
  implements OpenFilesHandler
{
  public MacArffOpenFilesHandler() {}
  
  public void openFiles(AppEvent.OpenFilesEvent arg0)
  {
    System.out.println("Opening an arff/xrff file under Mac OS X...");
    File toOpen = (File)arg0.getFiles().get(0);
    
    if ((toOpen.toString().toLowerCase().endsWith(".arff")) || (toOpen.toString().toLowerCase().endsWith(".xrff")))
    {
      GUIChooser.createSingleton();
      GUIChooser.getSingleton().showExplorer(toOpen.toString());
    } else if ((toOpen.toString().toLowerCase().endsWith(".kf")) || (toOpen.toString().toLowerCase().endsWith(".kfml")))
    {
      GUIChooser.createSingleton();
      GUIChooser.getSingleton().showKnowledgeFlow(toOpen.toString());
    }
  }
}

package weka.gui.visualize.plugins;

import javax.swing.JMenuItem;

public abstract interface TreeVisualizePlugin
{
  public abstract JMenuItem getVisualizeMenuItem(String paramString1, String paramString2);
  
  public abstract String getMinVersion();
  
  public abstract String getMaxVersion();
  
  public abstract String getDesignVersion();
}

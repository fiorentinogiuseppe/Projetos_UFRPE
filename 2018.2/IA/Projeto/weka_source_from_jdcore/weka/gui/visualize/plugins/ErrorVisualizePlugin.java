package weka.gui.visualize.plugins;

import javax.swing.JMenuItem;
import weka.core.Instances;

public abstract interface ErrorVisualizePlugin
{
  public abstract JMenuItem getVisualizeMenuItem(Instances paramInstances);
  
  public abstract String getMinVersion();
  
  public abstract String getMaxVersion();
  
  public abstract String getDesignVersion();
}

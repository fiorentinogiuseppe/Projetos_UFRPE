package weka.gui.visualize.plugins;

import javax.swing.JMenuItem;
import weka.core.Attribute;
import weka.core.FastVector;

public abstract interface VisualizePlugin
{
  public abstract JMenuItem getVisualizeMenuItem(FastVector paramFastVector, Attribute paramAttribute);
  
  public abstract String getMinVersion();
  
  public abstract String getMaxVersion();
  
  public abstract String getDesignVersion();
}

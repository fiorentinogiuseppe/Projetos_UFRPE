package weka.gui.visualize;

import java.util.Hashtable;

public abstract interface PrintableHandler
{
  public abstract Hashtable getWriters();
  
  public abstract JComponentWriter getWriter(String paramString);
  
  public abstract void setSaveDialogTitle(String paramString);
  
  public abstract String getSaveDialogTitle();
  
  public abstract void setScale(double paramDouble1, double paramDouble2);
  
  public abstract double getXScale();
  
  public abstract double getYScale();
  
  public abstract void saveComponent();
}

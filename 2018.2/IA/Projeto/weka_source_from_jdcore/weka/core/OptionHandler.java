package weka.core;

import java.util.Enumeration;

public abstract interface OptionHandler
{
  public abstract Enumeration listOptions();
  
  public abstract void setOptions(String[] paramArrayOfString)
    throws Exception;
  
  public abstract String[] getOptions();
}

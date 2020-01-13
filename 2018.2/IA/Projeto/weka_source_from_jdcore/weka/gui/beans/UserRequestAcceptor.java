package weka.gui.beans;

import java.util.Enumeration;

public abstract interface UserRequestAcceptor
{
  public abstract Enumeration enumerateRequests();
  
  public abstract void performRequest(String paramString);
}

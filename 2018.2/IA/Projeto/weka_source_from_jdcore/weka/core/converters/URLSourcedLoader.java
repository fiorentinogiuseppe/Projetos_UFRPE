package weka.core.converters;

public abstract interface URLSourcedLoader
{
  public abstract void setURL(String paramString)
    throws Exception;
  
  public abstract String retrieveURL();
}

package weka.core.converters;

public abstract interface DatabaseConverter
{
  public abstract String getUrl();
  
  public abstract String getUser();
  
  public abstract void setUrl(String paramString);
  
  public abstract void setUser(String paramString);
  
  public abstract void setPassword(String paramString);
}

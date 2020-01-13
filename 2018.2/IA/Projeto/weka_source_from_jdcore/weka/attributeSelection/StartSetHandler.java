package weka.attributeSelection;

public abstract interface StartSetHandler
{
  public abstract void setStartSet(String paramString)
    throws Exception;
  
  public abstract String getStartSet();
}

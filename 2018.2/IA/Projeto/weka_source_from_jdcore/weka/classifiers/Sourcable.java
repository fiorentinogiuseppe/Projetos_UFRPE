package weka.classifiers;

public abstract interface Sourcable
{
  public abstract String toSource(String paramString)
    throws Exception;
}

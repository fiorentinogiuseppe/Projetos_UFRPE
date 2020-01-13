package weka.attributeSelection;

public abstract interface RankedOutputSearch
{
  public abstract double[][] rankedAttributes()
    throws Exception;
  
  public abstract void setThreshold(double paramDouble);
  
  public abstract double getThreshold();
  
  public abstract void setNumToSelect(int paramInt);
  
  public abstract int getNumToSelect();
  
  public abstract int getCalculatedNumToSelect();
  
  public abstract void setGenerateRanking(boolean paramBoolean);
  
  public abstract boolean getGenerateRanking();
}

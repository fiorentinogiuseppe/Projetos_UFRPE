package weka.clusterers.forOPTICSAndDBScan.DataObjects;

import weka.core.Instance;

public abstract interface DataObject
{
  public static final int UNCLASSIFIED = -1;
  public static final int NOISE = Integer.MIN_VALUE;
  public static final double UNDEFINED = 2.147483647E9D;
  
  public abstract boolean equals(DataObject paramDataObject);
  
  public abstract double distance(DataObject paramDataObject);
  
  public abstract Instance getInstance();
  
  public abstract String getKey();
  
  public abstract void setKey(String paramString);
  
  public abstract void setClusterLabel(int paramInt);
  
  public abstract int getClusterLabel();
  
  public abstract void setProcessed(boolean paramBoolean);
  
  public abstract boolean isProcessed();
  
  public abstract void setCoreDistance(double paramDouble);
  
  public abstract double getCoreDistance();
  
  public abstract void setReachabilityDistance(double paramDouble);
  
  public abstract double getReachabilityDistance();
}

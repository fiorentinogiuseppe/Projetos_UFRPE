package weka.clusterers.forOPTICSAndDBScan.Databases;

import java.util.Iterator;
import java.util.List;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.core.Instances;

public abstract interface Database
{
  public abstract DataObject getDataObject(String paramString);
  
  public abstract int size();
  
  public abstract Iterator keyIterator();
  
  public abstract Iterator dataObjectIterator();
  
  public abstract boolean contains(DataObject paramDataObject);
  
  public abstract void insert(DataObject paramDataObject);
  
  public abstract Instances getInstances();
  
  public abstract void setMinMaxValues();
  
  public abstract double[] getAttributeMinValues();
  
  public abstract double[] getAttributeMaxValues();
  
  public abstract List epsilonRangeQuery(double paramDouble, DataObject paramDataObject);
  
  public abstract List k_nextNeighbourQuery(int paramInt, double paramDouble, DataObject paramDataObject);
  
  public abstract List coreDistance(int paramInt, double paramDouble, DataObject paramDataObject);
}

package weka.clusterers.forOPTICSAndDBScan.DataObjects;

import java.io.Serializable;
import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;










































































public class EuclideanDataObject
  implements DataObject, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -4408119914898291075L;
  private Instance instance;
  private String key;
  private int clusterID;
  private boolean processed;
  private double c_dist;
  private double r_dist;
  private Database database;
  
  public EuclideanDataObject(Instance originalInstance, String key, Database database)
  {
    this.database = database;
    this.key = key;
    instance = originalInstance;
    clusterID = -1;
    processed = false;
    c_dist = 2.147483647E9D;
    r_dist = 2.147483647E9D;
  }
  









  public boolean equals(DataObject dataObject)
  {
    if (this == dataObject) { return true;
    }
    Instance firstInstance = getInstance();
    Instance secondInstance = dataObject.getInstance();
    int firstNumValues = firstInstance.numValues();
    int secondNumValues = secondInstance.numValues();
    int numAttributes = firstInstance.numAttributes();
    

    int p1 = 0; for (int p2 = 0; (p1 < firstNumValues) || (p2 < secondNumValues);) { int firstI;
      int firstI; if (p1 >= firstNumValues) {
        firstI = numAttributes;
      } else
        firstI = firstInstance.index(p1);
      int secondI;
      int secondI;
      if (p2 >= secondNumValues) {
        secondI = numAttributes;
      } else {
        secondI = secondInstance.index(p2);
      }
      
      if (firstI == secondI) {
        if (firstInstance.valueSparse(p1) != secondInstance.valueSparse(p2)) {
          return false;
        }
        p1++;
        p2++;
      } else if (firstI > secondI) {
        if (0.0D != secondInstance.valueSparse(p2)) {
          return false;
        }
        p2++;
      } else {
        if (0.0D != firstInstance.valueSparse(p1)) {
          return false;
        }
        p1++;
      }
    }
    return true;
  }
  





  public double distance(DataObject dataObject)
  {
    double dist = 0.0D;
    
    Instance firstInstance = getInstance();
    Instance secondInstance = dataObject.getInstance();
    int firstNumValues = firstInstance.numValues();
    int secondNumValues = secondInstance.numValues();
    int numAttributes = firstInstance.numAttributes();
    

    int p1 = 0; for (int p2 = 0; (p1 < firstNumValues) || (p2 < secondNumValues);) { int firstI;
      int firstI; if (p1 >= firstNumValues) {
        firstI = numAttributes;
      } else
        firstI = firstInstance.index(p1);
      int secondI;
      int secondI;
      if (p2 >= secondNumValues) {
        secondI = numAttributes;
      } else {
        secondI = secondInstance.index(p2);
      }
      
      double cDistance = 0.0D;
      if (firstI == secondI) {
        cDistance = computeDistance(firstI, firstInstance.valueSparse(p1), secondInstance.valueSparse(p2));
        
        p1++;
        p2++;
      } else if (firstI > secondI) {
        cDistance = computeDistance(secondI, 0.0D, secondInstance.valueSparse(p2));
        p2++;
      } else {
        cDistance = computeDistance(firstI, firstInstance.valueSparse(p1), 0.0D);
        p1++;
      }
      dist += cDistance * cDistance;
    }
    return Math.sqrt(dist);
  }
  






  private double computeDistance(int index, double v, double v1)
  {
    switch (getInstance().attribute(index).type()) {
    case 1: 
      return (Instance.isMissingValue(v)) || (Instance.isMissingValue(v1)) || ((int)v != (int)v1) ? 1.0D : 0.0D;
    

    case 0: 
      if ((Instance.isMissingValue(v)) || (Instance.isMissingValue(v1))) {
        if ((Instance.isMissingValue(v)) && (Instance.isMissingValue(v1))) {
          return 1.0D;
        }
        return Instance.isMissingValue(v) ? norm(v1, index) : norm(v, index);
      }
      

      return norm(v, index) - norm(v1, index);
    }
    
    return 0.0D;
  }
  






  private double norm(double x, int i)
  {
    if ((Double.isNaN(database.getAttributeMinValues()[i])) || (Utils.eq(database.getAttributeMaxValues()[i], database.getAttributeMinValues()[i])))
    {
      return 0.0D;
    }
    return (x - database.getAttributeMinValues()[i]) / (database.getAttributeMaxValues()[i] - database.getAttributeMinValues()[i]);
  }
  





  public Instance getInstance()
  {
    return instance;
  }
  



  public String getKey()
  {
    return key;
  }
  



  public void setKey(String key)
  {
    this.key = key;
  }
  



  public void setClusterLabel(int clusterID)
  {
    this.clusterID = clusterID;
  }
  



  public int getClusterLabel()
  {
    return clusterID;
  }
  



  public void setProcessed(boolean processed)
  {
    this.processed = processed;
  }
  



  public boolean isProcessed()
  {
    return processed;
  }
  



  public void setCoreDistance(double c_dist)
  {
    this.c_dist = c_dist;
  }
  



  public double getCoreDistance()
  {
    return c_dist;
  }
  


  public void setReachabilityDistance(double r_dist)
  {
    this.r_dist = r_dist;
  }
  


  public double getReachabilityDistance()
  {
    return r_dist;
  }
  
  public String toString() {
    return instance.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8108 $");
  }
}

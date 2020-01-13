package weka.clusterers.forOPTICSAndDBScan.OPTICS_GUI;

import java.io.Serializable;
import weka.core.FastVector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




















































public class SERObject
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -6022057864970639151L;
  private FastVector resultVector;
  private int databaseSize;
  private int numberOfAttributes;
  private double epsilon;
  private int minPoints;
  private boolean opticsOutputs;
  private String database_Type;
  private String database_distanceType;
  private int numberOfGeneratedClusters;
  private String elapsedTime;
  
  public SERObject(FastVector resultVector, int databaseSize, int numberOfAttributes, double epsilon, int minPoints, boolean opticsOutputs, String database_Type, String database_distanceType, int numberOfGeneratedClusters, String elapsedTime)
  {
    this.resultVector = resultVector;
    this.databaseSize = databaseSize;
    this.numberOfAttributes = numberOfAttributes;
    this.epsilon = epsilon;
    this.minPoints = minPoints;
    this.opticsOutputs = opticsOutputs;
    this.database_Type = database_Type;
    this.database_distanceType = database_distanceType;
    this.numberOfGeneratedClusters = numberOfGeneratedClusters;
    this.elapsedTime = elapsedTime;
  }
  







  public FastVector getResultVector()
  {
    return resultVector;
  }
  



  public int getDatabaseSize()
  {
    return databaseSize;
  }
  



  public int getNumberOfAttributes()
  {
    return numberOfAttributes;
  }
  



  public double getEpsilon()
  {
    return epsilon;
  }
  



  public int getMinPoints()
  {
    return minPoints;
  }
  



  public boolean isOpticsOutputs()
  {
    return opticsOutputs;
  }
  



  public String getDatabase_Type()
  {
    return database_Type;
  }
  



  public String getDatabase_distanceType()
  {
    return database_distanceType;
  }
  



  public int getNumberOfGeneratedClusters()
  {
    return numberOfGeneratedClusters;
  }
  



  public String getElapsedTime()
  {
    return elapsedTime + " sec";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

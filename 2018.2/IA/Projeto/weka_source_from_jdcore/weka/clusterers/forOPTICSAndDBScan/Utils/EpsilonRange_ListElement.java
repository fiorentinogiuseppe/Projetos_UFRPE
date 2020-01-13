package weka.clusterers.forOPTICSAndDBScan.Utils;

import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;


























































public class EpsilonRange_ListElement
  implements RevisionHandler
{
  private DataObject dataObject;
  private double distance;
  
  public EpsilonRange_ListElement(double distance, DataObject dataObject)
  {
    this.distance = distance;
    this.dataObject = dataObject;
  }
  









  public double getDistance()
  {
    return distance;
  }
  



  public DataObject getDataObject()
  {
    return dataObject;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

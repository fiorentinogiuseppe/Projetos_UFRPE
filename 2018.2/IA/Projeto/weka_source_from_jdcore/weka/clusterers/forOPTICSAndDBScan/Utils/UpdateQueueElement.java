package weka.clusterers.forOPTICSAndDBScan.Utils;

import weka.core.RevisionHandler;
import weka.core.RevisionUtils;



















































public class UpdateQueueElement
  implements RevisionHandler
{
  private double priority;
  private Object o;
  private String objectKey;
  
  public UpdateQueueElement(double priority, Object o, String objectKey)
  {
    this.priority = priority;
    this.o = o;
    this.objectKey = objectKey;
  }
  







  public double getPriority()
  {
    return priority;
  }
  



  public Object getObject()
  {
    return o;
  }
  



  public String getObjectKey()
  {
    return objectKey;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

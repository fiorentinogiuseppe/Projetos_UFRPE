package weka.clusterers.forOPTICSAndDBScan.Utils;

import weka.core.RevisionHandler;
import weka.core.RevisionUtils;















































public class PriorityQueueElement
  implements RevisionHandler
{
  private double priority;
  private Object o;
  
  public PriorityQueueElement(double priority, Object o)
  {
    this.priority = priority;
    this.o = o;
  }
  







  public double getPriority()
  {
    return priority;
  }
  



  public Object getObject()
  {
    return o;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

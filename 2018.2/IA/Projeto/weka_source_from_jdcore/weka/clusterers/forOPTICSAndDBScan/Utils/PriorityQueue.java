package weka.clusterers.forOPTICSAndDBScan.Utils;

import java.util.ArrayList;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;


















































public class PriorityQueue
  implements RevisionHandler
{
  private ArrayList queue;
  
  public PriorityQueue()
  {
    queue = new ArrayList();
  }
  








  public void add(double priority, Object o)
  {
    queue.add(new PriorityQueueElement(priority, o));
    heapValueUpwards();
  }
  




  public double getPriority(int index)
  {
    return ((PriorityQueueElement)queue.get(index)).getPriority();
  }
  


  private void heapValueUpwards()
  {
    int a = size();
    int c = a / 2;
    
    PriorityQueueElement recentlyInsertedElement = (PriorityQueueElement)queue.get(a - 1);
    
    while ((c > 0) && (getPriority(c - 1) < recentlyInsertedElement.getPriority())) {
      queue.set(a - 1, queue.get(c - 1));
      a = c;
      c = a / 2;
    }
    queue.set(a - 1, recentlyInsertedElement);
  }
  


  private void heapValueDownwards()
  {
    int a = 1;
    int c = 2 * a;
    
    PriorityQueueElement priorityQueueElement = (PriorityQueueElement)queue.get(a - 1);
    
    if ((c < size()) && (getPriority(c) > getPriority(c - 1))) c++;
    for (; 
        (c <= size()) && (getPriority(c - 1) > priorityQueueElement.getPriority()); 
        


        c++)
    {
      label47:
      queue.set(a - 1, queue.get(c - 1));
      a = c;
      c = 2 * a;
      if ((c >= size()) || (getPriority(c) <= getPriority(c - 1))) break label47;
    }
    queue.set(a - 1, priorityQueueElement);
  }
  



  public int size()
  {
    return queue.size();
  }
  



  public boolean hasNext()
  {
    return size() != 0;
  }
  



  public PriorityQueueElement next()
  {
    PriorityQueueElement next = (PriorityQueueElement)queue.get(0);
    queue.set(0, queue.get(size() - 1));
    queue.remove(size() - 1);
    if (hasNext()) {
      heapValueDownwards();
    }
    return next;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

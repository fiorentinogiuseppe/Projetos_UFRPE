package weka.clusterers.forOPTICSAndDBScan.Utils;

import java.util.ArrayList;
import java.util.TreeMap;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;






















































public class UpdateQueue
  implements RevisionHandler
{
  private ArrayList queue;
  private TreeMap objectPositionsInHeap;
  
  public UpdateQueue()
  {
    queue = new ArrayList();
    objectPositionsInHeap = new TreeMap();
  }
  









  public void add(double priority, Object o, String objectKey)
  {
    int objectPosition = 0;
    
    if (objectPositionsInHeap.containsKey(objectKey)) {
      objectPosition = ((Integer)objectPositionsInHeap.get(objectKey)).intValue();
      if (((UpdateQueueElement)queue.get(objectPosition)).getPriority() <= priority) return;
      queue.set(objectPosition++, new UpdateQueueElement(priority, o, objectKey));
    } else {
      queue.add(new UpdateQueueElement(priority, o, objectKey));
      objectPosition = size();
    }
    heapValueUpwards(objectPosition);
  }
  




  public double getPriority(int index)
  {
    return ((UpdateQueueElement)queue.get(index)).getPriority();
  }
  


  private void heapValueUpwards(int pos)
  {
    int a = pos;
    int c = a / 2;
    
    UpdateQueueElement recentlyInsertedElement = (UpdateQueueElement)queue.get(a - 1);
    

    while ((c > 0) && (getPriority(c - 1) > recentlyInsertedElement.getPriority())) {
      queue.set(a - 1, queue.get(c - 1));
      objectPositionsInHeap.put(((UpdateQueueElement)queue.get(a - 1)).getObjectKey(), new Integer(a - 1));
      a = c;
      c = a / 2;
    }
    queue.set(a - 1, recentlyInsertedElement);
    objectPositionsInHeap.put(((UpdateQueueElement)queue.get(a - 1)).getObjectKey(), new Integer(a - 1));
  }
  


  private void heapValueDownwards()
  {
    int a = 1;
    int c = 2 * a;
    
    UpdateQueueElement updateQueueElement = (UpdateQueueElement)queue.get(a - 1);
    
    if ((c < size()) && (getPriority(c) < getPriority(c - 1))) c++;
    for (; 
        (c <= size()) && (getPriority(c - 1) < updateQueueElement.getPriority()); 
        



        c++)
    {
      label47:
      queue.set(a - 1, queue.get(c - 1));
      objectPositionsInHeap.put(((UpdateQueueElement)queue.get(a - 1)).getObjectKey(), new Integer(a - 1));
      a = c;
      c = 2 * a;
      if ((c >= size()) || (getPriority(c) >= getPriority(c - 1))) break label47;
    }
    queue.set(a - 1, updateQueueElement);
    objectPositionsInHeap.put(((UpdateQueueElement)queue.get(a - 1)).getObjectKey(), new Integer(a - 1));
  }
  



  public int size()
  {
    return queue.size();
  }
  



  public boolean hasNext()
  {
    return queue.size() != 0;
  }
  



  public UpdateQueueElement next()
  {
    UpdateQueueElement next = (UpdateQueueElement)queue.get(0);
    queue.set(0, queue.get(size() - 1));
    queue.remove(size() - 1);
    objectPositionsInHeap.remove(next.getObjectKey());
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

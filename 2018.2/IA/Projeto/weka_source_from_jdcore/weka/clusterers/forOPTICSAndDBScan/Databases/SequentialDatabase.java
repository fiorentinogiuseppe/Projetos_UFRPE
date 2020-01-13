package weka.clusterers.forOPTICSAndDBScan.Databases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.clusterers.forOPTICSAndDBScan.Utils.EpsilonRange_ListElement;
import weka.clusterers.forOPTICSAndDBScan.Utils.PriorityQueue;
import weka.clusterers.forOPTICSAndDBScan.Utils.PriorityQueueElement;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




























































public class SequentialDatabase
  implements Database, Serializable, RevisionHandler
{
  private static final long serialVersionUID = 787245523118665778L;
  private TreeMap treeMap;
  private Instances instances;
  private double[] attributeMinValues;
  private double[] attributeMaxValues;
  
  public SequentialDatabase(Instances instances)
  {
    this.instances = instances;
    treeMap = new TreeMap();
  }
  








  public DataObject getDataObject(String key)
  {
    return (DataObject)treeMap.get(key);
  }
  



  public void setMinMaxValues()
  {
    attributeMinValues = new double[getInstances().numAttributes()];
    attributeMaxValues = new double[getInstances().numAttributes()];
    

    for (int i = 0; i < getInstances().numAttributes(); i++) {
      double tmp52_49 = NaN.0D;attributeMaxValues[i] = tmp52_49;attributeMinValues[i] = tmp52_49;
    }
    
    Iterator iterator = dataObjectIterator();
    while (iterator.hasNext()) {
      DataObject dataObject = (DataObject)iterator.next();
      for (int j = 0; j < getInstances().numAttributes(); j++) {
        if (Double.isNaN(attributeMinValues[j])) {
          attributeMinValues[j] = dataObject.getInstance().value(j);
          attributeMaxValues[j] = dataObject.getInstance().value(j);
        } else {
          if (dataObject.getInstance().value(j) < attributeMinValues[j])
            attributeMinValues[j] = dataObject.getInstance().value(j);
          if (dataObject.getInstance().value(j) > attributeMaxValues[j]) {
            attributeMaxValues[j] = dataObject.getInstance().value(j);
          }
        }
      }
    }
  }
  


  public double[] getAttributeMinValues()
  {
    return attributeMinValues;
  }
  



  public double[] getAttributeMaxValues()
  {
    return attributeMaxValues;
  }
  





  public List epsilonRangeQuery(double epsilon, DataObject queryDataObject)
  {
    ArrayList epsilonRange_List = new ArrayList();
    Iterator iterator = dataObjectIterator();
    while (iterator.hasNext()) {
      DataObject dataObject = (DataObject)iterator.next();
      double distance = queryDataObject.distance(dataObject);
      if (distance < epsilon) {
        epsilonRange_List.add(dataObject);
      }
    }
    
    return epsilonRange_List;
  }
  










  public List k_nextNeighbourQuery(int k, double epsilon, DataObject dataObject)
  {
    Iterator iterator = dataObjectIterator();
    
    List return_List = new ArrayList();
    List nextNeighbours_List = new ArrayList();
    List epsilonRange_List = new ArrayList();
    
    PriorityQueue priorityQueue = new PriorityQueue();
    
    while (iterator.hasNext()) {
      DataObject next_dataObject = (DataObject)iterator.next();
      double dist = dataObject.distance(next_dataObject);
      
      if (dist <= epsilon) { epsilonRange_List.add(new EpsilonRange_ListElement(dist, next_dataObject));
      }
      if (priorityQueue.size() < k) {
        priorityQueue.add(dist, next_dataObject);
      }
      else if (dist < priorityQueue.getPriority(0)) {
        priorityQueue.next();
        priorityQueue.add(dist, next_dataObject);
      }
    }
    

    while (priorityQueue.hasNext()) {
      nextNeighbours_List.add(0, priorityQueue.next());
    }
    
    return_List.add(nextNeighbours_List);
    return_List.add(epsilonRange_List);
    return return_List;
  }
  












  public List coreDistance(int minPoints, double epsilon, DataObject dataObject)
  {
    List list = k_nextNeighbourQuery(minPoints, epsilon, dataObject);
    
    if (((List)list.get(1)).size() < minPoints) {
      list.add(new Double(2.147483647E9D));
      return list;
    }
    List nextNeighbours_List = (List)list.get(0);
    PriorityQueueElement priorityQueueElement = (PriorityQueueElement)nextNeighbours_List.get(nextNeighbours_List.size() - 1);
    
    if (priorityQueueElement.getPriority() <= epsilon) {
      list.add(new Double(priorityQueueElement.getPriority()));
      return list;
    }
    list.add(new Double(2.147483647E9D));
    return list;
  }
  





  public int size()
  {
    return treeMap.size();
  }
  



  public Iterator keyIterator()
  {
    return treeMap.keySet().iterator();
  }
  



  public Iterator dataObjectIterator()
  {
    return treeMap.values().iterator();
  }
  




  public boolean contains(DataObject dataObject_Query)
  {
    Iterator iterator = dataObjectIterator();
    while (iterator.hasNext()) {
      DataObject dataObject = (DataObject)iterator.next();
      if (dataObject.equals(dataObject_Query)) return true;
    }
    return false;
  }
  



  public void insert(DataObject dataObject)
  {
    treeMap.put(dataObject.getKey(), dataObject);
  }
  



  public Instances getInstances()
  {
    return instances;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

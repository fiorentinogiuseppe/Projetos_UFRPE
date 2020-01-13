package weka.core.neighboursearch;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.AdditionalMeasureProducer;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;





































public abstract class NearestNeighbourSearch
  implements Serializable, OptionHandler, AdditionalMeasureProducer, RevisionHandler
{
  protected Instances m_Instances;
  protected int m_kNN;
  
  protected class MyHeap
    implements RevisionHandler
  {
    NearestNeighbourSearch.MyHeapElement[] m_heap = null;
    




    public MyHeap(int maxSize)
    {
      if (maxSize % 2 == 0) {
        maxSize++;
      }
      m_heap = new NearestNeighbourSearch.MyHeapElement[maxSize + 1];
      m_heap[0] = new NearestNeighbourSearch.MyHeapElement(NearestNeighbourSearch.this, 0, 0.0D);
    }
    




    public int size()
    {
      return m_heap[0].index;
    }
    




    public NearestNeighbourSearch.MyHeapElement peek()
    {
      return m_heap[1];
    }
    




    public NearestNeighbourSearch.MyHeapElement get()
      throws Exception
    {
      if (m_heap[0].index == 0)
        throw new Exception("No elements present in the heap");
      NearestNeighbourSearch.MyHeapElement r = m_heap[1];
      m_heap[1] = m_heap[m_heap[0].index];
      m_heap[0].index -= 1;
      downheap();
      return r;
    }
    





    public void put(int i, double d)
      throws Exception
    {
      if (m_heap[0].index + 1 > m_heap.length - 1) {
        throw new Exception("the number of elements cannot exceed the initially set maximum limit");
      }
      m_heap[0].index += 1;
      m_heap[m_heap[0].index] = new NearestNeighbourSearch.MyHeapElement(NearestNeighbourSearch.this, i, d);
      upheap();
    }
    







    public void putBySubstitute(int i, double d)
      throws Exception
    {
      NearestNeighbourSearch.MyHeapElement head = get();
      put(i, d);
      
      if (distance == m_heap[1].distance) {
        putKthNearest(index, distance);
      }
      else if (distance > m_heap[1].distance) {
        m_KthNearest = null;
        m_KthNearestSize = 0;
        initSize = 10;
      }
      else if (distance < m_heap[1].distance) {
        throw new Exception("The substituted element is smaller than the head element. put() should have been called in place of putBySubstitute()");
      }
    }
    



    NearestNeighbourSearch.MyHeapElement[] m_KthNearest = null;
    

    int m_KthNearestSize = 0;
    

    int initSize = 10;
    





    public int noOfKthNearest()
    {
      return m_KthNearestSize;
    }
    





    public void putKthNearest(int i, double d)
    {
      if (m_KthNearest == null) {
        m_KthNearest = new NearestNeighbourSearch.MyHeapElement[initSize];
      }
      if (m_KthNearestSize >= m_KthNearest.length) {
        initSize += initSize;
        NearestNeighbourSearch.MyHeapElement[] temp = new NearestNeighbourSearch.MyHeapElement[initSize];
        System.arraycopy(m_KthNearest, 0, temp, 0, m_KthNearest.length);
        m_KthNearest = temp;
      }
      m_KthNearest[(m_KthNearestSize++)] = new NearestNeighbourSearch.MyHeapElement(NearestNeighbourSearch.this, i, d);
    }
    




    public NearestNeighbourSearch.MyHeapElement getKthNearest()
    {
      if (m_KthNearestSize == 0)
        return null;
      m_KthNearestSize -= 1;
      return m_KthNearest[m_KthNearestSize];
    }
    



    protected void upheap()
    {
      int i = m_heap[0].index;
      
      while ((i > 1) && (m_heap[i].distance > m_heap[(i / 2)].distance)) {
        NearestNeighbourSearch.MyHeapElement temp = m_heap[i];
        m_heap[i] = m_heap[(i / 2)];
        i /= 2;
        m_heap[i] = temp;
      }
    }
    



    protected void downheap()
    {
      int i = 1;
      


      while (((2 * i <= m_heap[0].index) && (m_heap[i].distance < m_heap[(2 * i)].distance)) || ((2 * i + 1 <= m_heap[0].index) && (m_heap[i].distance < m_heap[(2 * i + 1)].distance)))
      {

        if (2 * i + 1 <= m_heap[0].index) {
          if (m_heap[(2 * i)].distance > m_heap[(2 * i + 1)].distance) {
            NearestNeighbourSearch.MyHeapElement temp = m_heap[i];
            m_heap[i] = m_heap[(2 * i)];
            i = 2 * i;
            m_heap[i] = temp;
          }
          else {
            NearestNeighbourSearch.MyHeapElement temp = m_heap[i];
            m_heap[i] = m_heap[(2 * i + 1)];
            i = 2 * i + 1;
            m_heap[i] = temp;
          }
        }
        else {
          NearestNeighbourSearch.MyHeapElement temp = m_heap[i];
          m_heap[i] = m_heap[(2 * i)];
          i = 2 * i;
          m_heap[i] = temp;
        }
      }
    }
    




    public int totalSize()
    {
      return size() + noOfKthNearest();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  





  protected class MyHeapElement
    implements RevisionHandler
  {
    public int index;
    




    public double distance;
    




    public MyHeapElement(int i, double d)
    {
      distance = d;
      index = i;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  





  protected class NeighborNode
    implements RevisionHandler
  {
    public Instance m_Instance;
    



    public double m_Distance;
    



    public NeighborNode m_Next;
    




    public NeighborNode(double distance, Instance instance, NeighborNode next)
    {
      m_Distance = distance;
      m_Instance = instance;
      m_Next = next;
    }
    






    public NeighborNode(double distance, Instance instance)
    {
      this(distance, instance, null);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  





  protected class NeighborList
    implements RevisionHandler
  {
    protected NearestNeighbourSearch.NeighborNode m_First;
    




    protected NearestNeighbourSearch.NeighborNode m_Last;
    



    protected int m_Length = 1;
    




    public NeighborList(int length)
    {
      m_Length = length;
    }
    




    public boolean isEmpty()
    {
      return m_First == null;
    }
    




    public int currentLength()
    {
      int i = 0;
      NearestNeighbourSearch.NeighborNode current = m_First;
      while (current != null) {
        i++;
        current = m_Next;
      }
      return i;
    }
    







    public void insertSorted(double distance, Instance instance)
    {
      if (isEmpty()) {
        m_First = (this.m_Last = new NearestNeighbourSearch.NeighborNode(NearestNeighbourSearch.this, distance, instance));
      } else {
        NearestNeighbourSearch.NeighborNode current = m_First;
        if (distance < m_First.m_Distance) {
          m_First = new NearestNeighbourSearch.NeighborNode(NearestNeighbourSearch.this, distance, instance, m_First);
        } else {
          while ((m_Next != null) && (m_Next.m_Distance < distance))
          {
            current = m_Next; }
          m_Next = new NearestNeighbourSearch.NeighborNode(NearestNeighbourSearch.this, distance, instance, m_Next);
          
          if (current.equals(m_Last)) {
            m_Last = m_Next;
          }
        }
        


        int valcount = 0;
        for (current = m_First; m_Next != null; 
            current = m_Next) {
          valcount++;
          if ((valcount >= m_Length) && (m_Distance != m_Next.m_Distance))
          {
            m_Last = current;
            m_Next = null;
            break;
          }
        }
      }
    }
    






    public void pruneToK(int k)
    {
      if (isEmpty()) {
        return;
      }
      if (k < 1) {
        k = 1;
      }
      int currentK = 0;
      double currentDist = m_First.m_Distance;
      for (NearestNeighbourSearch.NeighborNode current = m_First; 
          m_Next != null; current = m_Next) {
        currentK++;
        currentDist = m_Distance;
        if ((currentK >= k) && (currentDist != m_Next.m_Distance)) {
          m_Last = current;
          m_Next = null;
          break;
        }
      }
    }
    



    public void printList()
    {
      if (isEmpty()) {
        System.out.println("Empty list");
      } else {
        NearestNeighbourSearch.NeighborNode current = m_First;
        while (current != null) {
          System.out.println("Node: instance " + m_Instance + ", distance " + m_Distance);
          
          current = m_Next;
        }
        System.out.println();
      }
    }
    




    public NearestNeighbourSearch.NeighborNode getFirst()
    {
      return m_First;
    }
    




    public NearestNeighbourSearch.NeighborNode getLast()
    {
      return m_Last;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  







  protected DistanceFunction m_DistanceFunction = new EuclideanDistance();
  

  protected PerformanceStats m_Stats = null;
  

  protected boolean m_MeasurePerformance = false;
  


  public NearestNeighbourSearch()
  {
    if (m_MeasurePerformance) {
      m_Stats = new PerformanceStats();
    }
  }
  



  public NearestNeighbourSearch(Instances insts)
  {
    this();
    m_Instances = insts;
  }
  





  public String globalInfo()
  {
    return "Abstract class for nearest neighbour search. All algorithms (classes) that do nearest neighbour search should extend this class.";
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.add(new Option("\tDistance function to use.\n\t(default: weka.core.EuclideanDistance)", "A", 1, "-A <classname and options>"));
    



    newVector.add(new Option("\tCalculate performance statistics.", "P", 0, "-P"));
    


    return newVector.elements();
  }
  







  public void setOptions(String[] options)
    throws Exception
  {
    String nnSearchClass = Utils.getOption('A', options);
    if (nnSearchClass.length() != 0) {
      String[] nnSearchClassSpec = Utils.splitOptions(nnSearchClass);
      if (nnSearchClassSpec.length == 0) {
        throw new Exception("Invalid DistanceFunction specification string.");
      }
      String className = nnSearchClassSpec[0];
      nnSearchClassSpec[0] = "";
      
      setDistanceFunction((DistanceFunction)Utils.forName(DistanceFunction.class, className, nnSearchClassSpec));

    }
    else
    {
      setDistanceFunction(new EuclideanDistance());
    }
    
    setMeasurePerformance(Utils.getFlag('P', options));
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-A");
    result.add((m_DistanceFunction.getClass().getName() + " " + Utils.joinOptions(m_DistanceFunction.getOptions())).trim());
    

    if (getMeasurePerformance()) {
      result.add("-P");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String distanceFunctionTipText()
  {
    return "The distance function to use for finding neighbours (default: weka.core.EuclideanDistance). ";
  }
  





  public DistanceFunction getDistanceFunction()
  {
    return m_DistanceFunction;
  }
  




  public void setDistanceFunction(DistanceFunction df)
    throws Exception
  {
    m_DistanceFunction = df;
  }
  





  public String measurePerformanceTipText()
  {
    return "Whether to calculate performance statistics for the NN search or not";
  }
  





  public boolean getMeasurePerformance()
  {
    return m_MeasurePerformance;
  }
  




  public void setMeasurePerformance(boolean measurePerformance)
  {
    m_MeasurePerformance = measurePerformance;
    if (m_MeasurePerformance) {
      if (m_Stats == null) {
        m_Stats = new PerformanceStats();
      }
    } else {
      m_Stats = null;
    }
  }
  







  public abstract Instance nearestNeighbour(Instance paramInstance)
    throws Exception;
  







  public abstract Instances kNearestNeighbours(Instance paramInstance, int paramInt)
    throws Exception;
  







  public abstract double[] getDistances()
    throws Exception;
  






  public abstract void update(Instance paramInstance)
    throws Exception;
  






  public void addInstanceInfo(Instance ins) {}
  






  public void setInstances(Instances insts)
    throws Exception
  {
    m_Instances = insts;
  }
  




  public Instances getInstances()
  {
    return m_Instances;
  }
  





  public PerformanceStats getPerformanceStats()
  {
    return m_Stats;
  }
  

  public Enumeration enumerateMeasures()
  {
    Vector newVector;
    
    Vector newVector;
    
    if (m_Stats == null) {
      newVector = new Vector(0);
    }
    else {
      newVector = new Vector();
      Enumeration en = m_Stats.enumerateMeasures();
      while (en.hasMoreElements())
        newVector.add(en.nextElement());
    }
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if (m_Stats == null) {
      throw new IllegalArgumentException(additionalMeasureName + " not supported (NearestNeighbourSearch)");
    }
    
    return m_Stats.getMeasure(additionalMeasureName);
  }
  








  public static void combSort11(double[] arrayToSort, int[] linkedArray)
  {
    int gap = arrayToSort.length;
    int switches;
    do { gap = (int)(gap / 1.3D);
      switch (gap) {
      case 0: 
        gap = 1;
        break;
      case 9: 
      case 10: 
        gap = 11;
        break;
      }
      
      
      switches = 0;
      int top = arrayToSort.length - gap;
      for (int i = 0; i < top; i++) {
        int j = i + gap;
        if (arrayToSort[i] > arrayToSort[j]) {
          double hold1 = arrayToSort[i];
          int hold2 = linkedArray[i];
          arrayToSort[i] = arrayToSort[j];
          linkedArray[i] = linkedArray[j];
          arrayToSort[j] = hold1;
          linkedArray[j] = hold2;
          switches++;
        }
      }
    } while ((switches > 0) || (gap > 1));
  }
  









  protected static int partition(double[] arrayToSort, double[] linkedArray, int l, int r)
  {
    double pivot = arrayToSort[((l + r) / 2)];
    

    while (l < r) {
      while ((arrayToSort[l] < pivot) && (l < r)) {
        l++;
      }
      while ((arrayToSort[r] > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        double help = arrayToSort[l];
        arrayToSort[l] = arrayToSort[r];
        arrayToSort[r] = help;
        help = linkedArray[l];
        linkedArray[l] = linkedArray[r];
        linkedArray[r] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (arrayToSort[r] > pivot)) {
      r--;
    }
    
    return r;
  }
  







  public static void quickSort(double[] arrayToSort, double[] linkedArray, int left, int right)
  {
    if (left < right) {
      int middle = partition(arrayToSort, linkedArray, left, right);
      quickSort(arrayToSort, linkedArray, left, middle);
      quickSort(arrayToSort, linkedArray, middle + 1, right);
    }
  }
}

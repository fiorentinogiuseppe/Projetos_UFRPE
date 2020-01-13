package weka.core.neighboursearch;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;














































public class LinearNNSearch
  extends NearestNeighbourSearch
{
  private static final long serialVersionUID = 1915484723703917241L;
  protected double[] m_Distances;
  protected boolean m_SkipIdentical = false;
  






  public LinearNNSearch() {}
  





  public LinearNNSearch(Instances insts)
  {
    super(insts);
    m_DistanceFunction.setInstances(insts);
  }
  





  public String globalInfo()
  {
    return "Class implementing the brute force search algorithm for nearest neighbour search.";
  }
  






  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.add(new Option("\tSkip identical instances (distances equal to zero).\n", "S", 1, "-S"));
    


    return result.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    setSkipIdentical(Utils.getFlag('S', options));
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getSkipIdentical()) {
      result.add("-S");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String skipIdenticalTipText()
  {
    return "Whether to skip identical instances (with distance 0 to the target)";
  }
  





  public void setSkipIdentical(boolean skip)
  {
    m_SkipIdentical = skip;
  }
  




  public boolean getSkipIdentical()
  {
    return m_SkipIdentical;
  }
  







  public Instance nearestNeighbour(Instance target)
    throws Exception
  {
    return kNearestNeighbours(target, 1).instance(0);
  }
  









  public Instances kNearestNeighbours(Instance target, int kNN)
    throws Exception
  {
    boolean print = false;
    
    if (m_Stats != null) {
      m_Stats.searchStart();
    }
    NearestNeighbourSearch.MyHeap heap = new NearestNeighbourSearch.MyHeap(this, kNN);
    int firstkNN = 0;
    for (int i = 0; i < m_Instances.numInstances(); i++) {
      if (target != m_Instances.instance(i))
      {
        if (m_Stats != null)
          m_Stats.incrPointCount();
        if (firstkNN < kNN) {
          if (print)
            System.out.println("K(a): " + (heap.size() + heap.noOfKthNearest()));
          double distance = m_DistanceFunction.distance(target, m_Instances.instance(i), Double.POSITIVE_INFINITY, m_Stats);
          if ((distance == 0.0D) && (m_SkipIdentical)) {
            if (i >= m_Instances.numInstances() - 1)
            {

              heap.put(i, distance); }
          } else { heap.put(i, distance);
            firstkNN++;
          }
        } else {
          NearestNeighbourSearch.MyHeapElement temp = heap.peek();
          if (print)
            System.out.println("K(b): " + (heap.size() + heap.noOfKthNearest()));
          double distance = m_DistanceFunction.distance(target, m_Instances.instance(i), distance, m_Stats);
          if ((distance != 0.0D) || (!m_SkipIdentical))
          {
            if (distance < distance) {
              heap.putBySubstitute(i, distance);
            }
            else if (distance == distance) {
              heap.putKthNearest(i, distance);
            }
          }
        }
      }
    }
    Instances neighbours = new Instances(m_Instances, heap.size() + heap.noOfKthNearest());
    m_Distances = new double[heap.size() + heap.noOfKthNearest()];
    int[] indices = new int[heap.size() + heap.noOfKthNearest()];
    int i = 1;
    while (heap.noOfKthNearest() > 0) {
      NearestNeighbourSearch.MyHeapElement h = heap.getKthNearest();
      indices[(indices.length - i)] = index;
      m_Distances[(indices.length - i)] = distance;
      i++;
    }
    while (heap.size() > 0) {
      NearestNeighbourSearch.MyHeapElement h = heap.get();
      indices[(indices.length - i)] = index;
      m_Distances[(indices.length - i)] = distance;
      i++;
    }
    
    m_DistanceFunction.postProcessDistances(m_Distances);
    
    for (int k = 0; k < indices.length; k++) {
      neighbours.add(m_Instances.instance(indices[k]));
    }
    
    if (m_Stats != null) {
      m_Stats.searchFinish();
    }
    return neighbours;
  }
  















  public double[] getDistances()
    throws Exception
  {
    if (m_Distances == null) {
      throw new Exception("No distances available. Please call either kNearestNeighbours or nearestNeighbours first.");
    }
    return m_Distances;
  }
  






  public void setInstances(Instances insts)
    throws Exception
  {
    m_Instances = insts;
    m_DistanceFunction.setInstances(insts);
  }
  









  public void update(Instance ins)
    throws Exception
  {
    if (m_Instances == null) {
      throw new Exception("No instances supplied yet. Cannot update withoutsupplying a set of instances first.");
    }
    m_DistanceFunction.update(ins);
  }
  







  public void addInstanceInfo(Instance ins)
  {
    if (m_Instances != null) {
      try { update(ins);
      } catch (Exception ex) { ex.printStackTrace();
      }
    }
  }
  


  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

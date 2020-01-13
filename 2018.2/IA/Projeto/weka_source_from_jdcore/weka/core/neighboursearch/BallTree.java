package weka.core.neighboursearch;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.neighboursearch.balltrees.BallNode;
import weka.core.neighboursearch.balltrees.BallTreeConstructor;
import weka.core.neighboursearch.balltrees.TopDownConstructor;





















































































public class BallTree
  extends NearestNeighbourSearch
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 728763855952698328L;
  protected int[] m_InstList;
  protected int m_MaxInstancesInLeaf = 40;
  

  protected TreePerformanceStats m_TreeStats = null;
  

  protected BallNode m_Root;
  

  protected BallTreeConstructor m_TreeConstructor = new TopDownConstructor();
  



  protected double[] m_Distances;
  



  public BallTree()
  {
    if (getMeasurePerformance()) {
      m_Stats = (this.m_TreeStats = new TreePerformanceStats());
    }
  }
  




  public BallTree(Instances insts)
  {
    super(insts);
    if (getMeasurePerformance()) {
      m_Stats = (this.m_TreeStats = new TreePerformanceStats());
    }
  }
  




  public String globalInfo()
  {
    return "Class implementing the BallTree/Metric Tree algorithm for nearest neighbour search.\nThe connection to dataset is only a reference. For the tree structure the indexes are stored in an array.\nSee the implementing classes of different construction methods of the trees for details on its construction.\n\nFor more information see also:\n\n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.TECHREPORT);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Stephen M. Omohundro");
    result.setValue(TechnicalInformation.Field.YEAR, "1989");
    result.setValue(TechnicalInformation.Field.TITLE, "Five Balltree Construction Algorithms");
    result.setValue(TechnicalInformation.Field.MONTH, "December");
    result.setValue(TechnicalInformation.Field.NUMBER, "TR-89-063");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "International Computer Science Institute");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Jeffrey K. Uhlmann");
    additional.setValue(TechnicalInformation.Field.TITLE, "Satisfying general proximity/similarity queries with metric trees");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Information Processing Letters");
    additional.setValue(TechnicalInformation.Field.MONTH, "November");
    additional.setValue(TechnicalInformation.Field.YEAR, "1991");
    additional.setValue(TechnicalInformation.Field.NUMBER, "4");
    additional.setValue(TechnicalInformation.Field.VOLUME, "40");
    additional.setValue(TechnicalInformation.Field.PAGES, "175-179");
    
    return result;
  }
  










  protected void buildTree()
    throws Exception
  {
    if (m_Instances == null) {
      throw new Exception("No instances supplied yet. Have to call setInstances(instances) with a set of Instances first.");
    }
    
    m_InstList = new int[m_Instances.numInstances()];
    
    for (int i = 0; i < m_InstList.length; i++) {
      m_InstList[i] = i;
    }
    
    m_DistanceFunction.setInstances(m_Instances);
    m_TreeConstructor.setInstances(m_Instances);
    m_TreeConstructor.setInstanceList(m_InstList);
    m_TreeConstructor.setEuclideanDistanceFunction((EuclideanDistance)m_DistanceFunction);
    

    m_Root = m_TreeConstructor.buildTree();
  }
  











  public Instances kNearestNeighbours(Instance target, int k)
    throws Exception
  {
    NearestNeighbourSearch.MyHeap heap = new NearestNeighbourSearch.MyHeap(this, k);
    
    if (m_Stats != null) {
      m_Stats.searchStart();
    }
    nearestNeighbours(heap, m_Root, target, k);
    
    if (m_Stats != null) {
      m_Stats.searchFinish();
    }
    Instances neighbours = new Instances(m_Instances, heap.totalSize());
    m_Distances = new double[heap.totalSize()];
    int[] indices = new int[heap.totalSize()];
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
    
    for (i = 0; i < indices.length; i++) {
      neighbours.add(m_Instances.instance(indices[i]));
    }
    return neighbours;
  }
  












  protected void nearestNeighbours(NearestNeighbourSearch.MyHeap heap, BallNode node, Instance target, int k)
    throws Exception
  {
    double distance = Double.NEGATIVE_INFINITY;
    
    if (heap.totalSize() >= k) {
      distance = m_DistanceFunction.distance(target, node.getPivot());
    }
    
    if ((distance > -1.0E-6D) && (Math.sqrt(peekdistance) < distance - node.getRadius()))
    {
      return; }
    if ((m_Left != null) && (m_Right != null))
    {
      if (m_TreeStats != null) {
        m_TreeStats.incrIntNodeCount();
      }
      double leftPivotDist = Math.sqrt(m_DistanceFunction.distance(target, m_Left.getPivot(), Double.POSITIVE_INFINITY));
      
      double rightPivotDist = Math.sqrt(m_DistanceFunction.distance(target, m_Right.getPivot(), Double.POSITIVE_INFINITY));
      
      double leftBallDist = leftPivotDist - m_Left.getRadius();
      double rightBallDist = rightPivotDist - m_Right.getRadius();
      
      if ((leftBallDist < 0.0D) && (rightBallDist < 0.0D)) {
        if (leftPivotDist < rightPivotDist) {
          nearestNeighbours(heap, m_Left, target, k);
          nearestNeighbours(heap, m_Right, target, k);
        } else {
          nearestNeighbours(heap, m_Right, target, k);
          nearestNeighbours(heap, m_Left, target, k);

        }
        

      }
      else if (leftBallDist < rightBallDist) {
        nearestNeighbours(heap, m_Left, target, k);
        nearestNeighbours(heap, m_Right, target, k);
      } else {
        nearestNeighbours(heap, m_Right, target, k);
        nearestNeighbours(heap, m_Left, target, k);
      }
    } else {
      if ((m_Left != null) || (m_Right != null))
      {
        throw new Exception("Error: Only one leaf of the built ball tree is assigned. Please check code.");
      }
      if ((m_Left == null) && (m_Right == null))
      {
        if (m_TreeStats != null) {
          m_TreeStats.updatePointCount(node.numInstances());
          m_TreeStats.incrLeafCount();
        }
        for (int i = m_Start; i <= m_End; i++) {
          if (target != m_Instances.instance(m_InstList[i]))
          {
            if (heap.totalSize() < k) {
              distance = m_DistanceFunction.distance(target, m_Instances.instance(m_InstList[i]), Double.POSITIVE_INFINITY, m_Stats);
              
              heap.put(m_InstList[i], distance);
            } else {
              NearestNeighbourSearch.MyHeapElement head = heap.peek();
              distance = m_DistanceFunction.distance(target, m_Instances.instance(m_InstList[i]), distance, m_Stats);
              
              if (distance < distance) {
                heap.putBySubstitute(m_InstList[i], distance);
              } else if (distance == distance) {
                heap.putKthNearest(m_InstList[i], distance);
              }
            }
          }
        }
      }
    }
  }
  




  public Instance nearestNeighbour(Instance target)
    throws Exception
  {
    return kNearestNeighbours(target, 1).instance(0);
  }
  















  public double[] getDistances()
    throws Exception
  {
    if (m_Distances == null) {
      throw new Exception("No distances available. Please call either kNearestNeighbours or nearestNeighbours first.");
    }
    return m_Distances;
  }
  






  public void update(Instance ins)
    throws Exception
  {
    addInstanceInfo(ins);
    m_InstList = m_TreeConstructor.addInstance(m_Root, ins);
  }
  







  public void addInstanceInfo(Instance ins)
  {
    if (m_Instances != null) {
      m_DistanceFunction.update(ins);
    }
  }
  




  public void setInstances(Instances insts)
    throws Exception
  {
    super.setInstances(insts);
    buildTree();
  }
  





  public String ballTreeConstructorTipText()
  {
    return "The tree constructor being used.";
  }
  



  public BallTreeConstructor getBallTreeConstructor()
  {
    return m_TreeConstructor;
  }
  




  public void setBallTreeConstructor(BallTreeConstructor constructor)
  {
    m_TreeConstructor = constructor;
  }
  




  public double measureTreeSize()
  {
    return m_TreeConstructor.getNumNodes();
  }
  




  public double measureNumLeaves()
  {
    return m_TreeConstructor.getNumLeaves();
  }
  




  public double measureMaxDepth()
  {
    return m_TreeConstructor.getMaxDepth();
  }
  




  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    newVector.addElement("measureMaxDepth");
    Enumeration e; if (m_Stats != null) {
      for (e = m_Stats.enumerateMeasures(); e.hasMoreElements();) {
        newVector.addElement(e.nextElement());
      }
    }
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureMaxDepth") == 0)
      return measureMaxDepth();
    if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0)
      return measureTreeSize();
    if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0)
      return measureNumLeaves();
    if (m_Stats != null) {
      return m_Stats.getMeasure(additionalMeasureName);
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (BallTree)");
  }
  






  public void setMeasurePerformance(boolean measurePerformance)
  {
    m_MeasurePerformance = measurePerformance;
    if (m_MeasurePerformance) {
      if (m_Stats == null)
        m_Stats = (this.m_TreeStats = new TreePerformanceStats());
    } else {
      m_Stats = (this.m_TreeStats = null);
    }
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tThe construction method to employ. Either TopDown or BottomUp\n\t(default: weka.core.TopDownConstructor)", "C", 1, "-C <classname and options>"));
    



    return newVector.elements();
  }
  















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String optionString = Utils.getOption('C', options);
    if (optionString.length() != 0) {
      String[] constructorSpec = Utils.splitOptions(optionString);
      if (constructorSpec.length == 0) {
        throw new Exception("Invalid BallTreeConstructor specification string.");
      }
      String className = constructorSpec[0];
      constructorSpec[0] = "";
      
      setBallTreeConstructor((BallTreeConstructor)Utils.forName(BallTreeConstructor.class, className, constructorSpec));

    }
    else
    {
      setBallTreeConstructor(new TopDownConstructor());
    }
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-C");
    result.add((m_TreeConstructor.getClass().getName() + " " + Utils.joinOptions(m_TreeConstructor.getOptions())).trim());
    


    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

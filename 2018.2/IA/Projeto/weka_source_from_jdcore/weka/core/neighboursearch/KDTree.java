package weka.core.neighboursearch;

import java.io.PrintStream;
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
import weka.core.neighboursearch.kdtrees.KDTreeNode;
import weka.core.neighboursearch.kdtrees.KDTreeNodeSplitter;
import weka.core.neighboursearch.kdtrees.SlidingMidPointOfWidestSide;











































































































public class KDTree
  extends NearestNeighbourSearch
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 1505717283763272533L;
  protected double[] m_DistanceList;
  protected int[] m_InstList;
  protected KDTreeNode m_Root;
  protected KDTreeNodeSplitter m_Splitter = new SlidingMidPointOfWidestSide();
  
  protected int m_NumNodes;
  
  protected int m_NumLeaves;
  protected int m_MaxDepth;
  protected TreePerformanceStats m_TreeStats = null;
  

  public static final int MIN = 0;
  

  public static final int MAX = 1;
  

  public static final int WIDTH = 2;
  

  boolean m_NormalizeNodeWidth;
  
  protected EuclideanDistance m_EuclideanDistance;
  
  protected double m_MinBoxRelWidth;
  
  protected int m_MaxInstInLeaf;
  

  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Jerome H. Friedman and Jon Luis Bentley and Raphael Ari Finkel");
    result.setValue(TechnicalInformation.Field.YEAR, "1977");
    result.setValue(TechnicalInformation.Field.TITLE, "An Algorithm for Finding Best Matches in Logarithmic Expected Time");
    result.setValue(TechnicalInformation.Field.JOURNAL, "ACM Transactions on Mathematics Software");
    result.setValue(TechnicalInformation.Field.PAGES, "209-226");
    result.setValue(TechnicalInformation.Field.MONTH, "September");
    result.setValue(TechnicalInformation.Field.VOLUME, "3");
    result.setValue(TechnicalInformation.Field.NUMBER, "3");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.TECHREPORT);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Andrew Moore");
    additional.setValue(TechnicalInformation.Field.YEAR, "1991");
    additional.setValue(TechnicalInformation.Field.TITLE, "A tutorial on kd-trees");
    additional.setValue(TechnicalInformation.Field.HOWPUBLISHED, "Extract from PhD Thesis");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "University of Cambridge Computer Laboratory Technical Report No. 209");
    additional.setValue(TechnicalInformation.Field.HTTP, "Available from http://www.autonlab.org/autonweb/14665.html");
    
    return result;
  }
  


























































































































































































































































































































































































































































































































































































































































































































































































































































  public KDTree()
  {
    m_NormalizeNodeWidth = true;
    



    if ((m_DistanceFunction instanceof EuclideanDistance)) {
      m_EuclideanDistance = ((EuclideanDistance)m_DistanceFunction);
    } else {
      m_DistanceFunction = (this.m_EuclideanDistance = new EuclideanDistance());
    }
    

    m_MinBoxRelWidth = 0.01D;
    

    m_MaxInstInLeaf = 40;
    if (getMeasurePerformance()) {
      m_Stats = (this.m_TreeStats = new TreePerformanceStats());
    }
  }
  




  public KDTree(Instances insts)
  {
    super(insts);
    










































































































































































































































































































































































































































































































































































































































































































































































































































    m_NormalizeNodeWidth = true;
    



    if ((m_DistanceFunction instanceof EuclideanDistance)) {
      m_EuclideanDistance = ((EuclideanDistance)m_DistanceFunction);
    } else {
      m_DistanceFunction = (this.m_EuclideanDistance = new EuclideanDistance());
    }
    

    m_MinBoxRelWidth = 0.01D;
    

    m_MaxInstInLeaf = 40;
    if (getMeasurePerformance()) {
      m_Stats = (this.m_TreeStats = new TreePerformanceStats());
    }
  }
  









  protected void buildKDTree(Instances instances)
    throws Exception
  {
    checkMissing(instances);
    if (m_EuclideanDistance == null) {
      m_DistanceFunction = (this.m_EuclideanDistance = new EuclideanDistance(instances));
    }
    else {
      m_EuclideanDistance.setInstances(instances);
    }
    m_Instances = instances;
    int numInst = m_Instances.numInstances();
    

    m_InstList = new int[numInst];
    
    for (int i = 0; i < numInst; i++) {
      m_InstList[i] = i;
    }
    
    double[][] universe = m_EuclideanDistance.getRanges();
    

    m_Splitter.setInstances(m_Instances);
    m_Splitter.setInstanceList(m_InstList);
    m_Splitter.setEuclideanDistanceFunction(m_EuclideanDistance);
    m_Splitter.setNodeWidthNormalization(m_NormalizeNodeWidth);
    

    m_NumNodes = (this.m_NumLeaves = 1);
    m_MaxDepth = 0;
    m_Root = new KDTreeNode(m_NumNodes, 0, m_Instances.numInstances() - 1, universe);
    

    splitNodes(m_Root, universe, m_MaxDepth + 1);
  }
  













  protected void splitNodes(KDTreeNode node, double[][] universe, int depth)
    throws Exception
  {
    double[][] nodeRanges = m_EuclideanDistance.initializeRanges(m_InstList, m_Start, m_End);
    
    if ((node.numInstances() <= m_MaxInstInLeaf) || (getMaxRelativeNodeWidth(nodeRanges, universe) <= m_MinBoxRelWidth))
    {
      return;
    }
    
    m_NumLeaves -= 1;
    
    if (depth > m_MaxDepth) {
      m_MaxDepth = depth;
    }
    m_Splitter.splitNode(node, m_NumNodes, nodeRanges, universe);
    m_NumNodes += 2;
    m_NumLeaves += 2;
    
    splitNodes(m_Left, universe, depth + 1);
    splitNodes(m_Right, universe, depth + 1);
  }
  
















  protected void findNearestNeighbours(Instance target, KDTreeNode node, int k, NearestNeighbourSearch.MyHeap heap, double distanceToParents)
    throws Exception
  {
    if (node.isALeaf()) {
      if (m_TreeStats != null) {
        m_TreeStats.updatePointCount(node.numInstances());
        m_TreeStats.incrLeafCount();
      }
      

      for (int idx = m_Start; idx <= m_End; idx++) {
        if (target != m_Instances.instance(m_InstList[idx]))
        {


          if (heap.size() < k) {
            double distance = m_EuclideanDistance.distance(target, m_Instances.instance(m_InstList[idx]), Double.POSITIVE_INFINITY, m_Stats);
            
            heap.put(m_InstList[idx], distance);
          } else {
            NearestNeighbourSearch.MyHeapElement temp = heap.peek();
            double distance = m_EuclideanDistance.distance(target, m_Instances.instance(m_InstList[idx]), distance, m_Stats);
            
            if (distance < distance) {
              heap.putBySubstitute(m_InstList[idx], distance);
            } else if (distance == distance) {
              heap.putKthNearest(m_InstList[idx], distance);
            }
          }
        }
      }
    } else {
      if (m_TreeStats != null) {
        m_TreeStats.incrIntNodeCount();
      }
      
      boolean targetInLeft = m_EuclideanDistance.valueIsSmallerEqual(target, m_SplitDim, m_SplitValue);
      KDTreeNode further;
      KDTreeNode nearer;
      KDTreeNode further; if (targetInLeft) {
        KDTreeNode nearer = m_Left;
        further = m_Right;
      } else {
        nearer = m_Right;
        further = m_Left;
      }
      findNearestNeighbours(target, nearer, k, heap, distanceToParents);
      

      if (heap.size() < k) {
        double distanceToSplitPlane = distanceToParents + m_EuclideanDistance.sqDifference(m_SplitDim, target.value(m_SplitDim), m_SplitValue);
        

        findNearestNeighbours(target, further, k, heap, distanceToSplitPlane);
        return;
      }
      
      double distanceToSplitPlane = distanceToParents + m_EuclideanDistance.sqDifference(m_SplitDim, target.value(m_SplitDim), m_SplitValue);
      

      if (peekdistance >= distanceToSplitPlane) {
        findNearestNeighbours(target, further, k, heap, distanceToSplitPlane);
      }
    }
  }
  










  public Instances kNearestNeighbours(Instance target, int k)
    throws Exception
  {
    checkMissing(target);
    
    if (m_Stats != null) {
      m_Stats.searchStart();
    }
    NearestNeighbourSearch.MyHeap heap = new NearestNeighbourSearch.MyHeap(this, k);
    findNearestNeighbours(target, m_Root, k, heap, 0.0D);
    
    if (m_Stats != null) {
      m_Stats.searchFinish();
    }
    Instances neighbours = new Instances(m_Instances, heap.size() + heap.noOfKthNearest());
    
    m_DistanceList = new double[heap.size() + heap.noOfKthNearest()];
    int[] indices = new int[heap.size() + heap.noOfKthNearest()];
    int i = indices.length - 1;
    
    while (heap.noOfKthNearest() > 0) {
      NearestNeighbourSearch.MyHeapElement h = heap.getKthNearest();
      indices[i] = index;
      m_DistanceList[i] = distance;
      i--;
    }
    while (heap.size() > 0) {
      NearestNeighbourSearch.MyHeapElement h = heap.get();
      indices[i] = index;
      m_DistanceList[i] = distance;
      i--;
    }
    m_DistanceFunction.postProcessDistances(m_DistanceList);
    
    for (int idx = 0; idx < indices.length; idx++) {
      neighbours.add(m_Instances.instance(indices[idx]));
    }
    
    return neighbours;
  }
  








  public Instance nearestNeighbour(Instance target)
    throws Exception
  {
    return kNearestNeighbours(target, 1).instance(0);
  }
  









  public double[] getDistances()
    throws Exception
  {
    if ((m_Instances == null) || (m_DistanceList == null)) {
      throw new Exception("The tree has not been supplied with a set of instances or getDistances() has been called before calling kNearestNeighbours().");
    }
    
    return m_DistanceList;
  }
  






  public void setInstances(Instances instances)
    throws Exception
  {
    super.setInstances(instances);
    buildKDTree(instances);
  }
  








  public void update(Instance instance)
    throws Exception
  {
    if (m_Instances == null) {
      throw new Exception("No instances supplied yet. Have to call setInstances(instances) with a set of Instances first.");
    }
    
    addInstanceInfo(instance);
    addInstanceToTree(instance, m_Root);
  }
  













  protected void addInstanceToTree(Instance inst, KDTreeNode node)
    throws Exception
  {
    if (node.isALeaf()) {
      int[] instList = new int[m_Instances.numInstances()];
      try {
        System.arraycopy(m_InstList, 0, instList, 0, m_End + 1);
        
        if (m_End < m_InstList.length - 1) {
          System.arraycopy(m_InstList, m_End + 1, instList, m_End + 2, m_InstList.length - m_End - 1);
        }
        instList[(m_End + 1)] = (m_Instances.numInstances() - 1);
      } catch (ArrayIndexOutOfBoundsException ex) {
        System.err.println("m_InstList.length: " + m_InstList.length + " instList.length: " + instList.length + "node.m_End+1: " + (m_End + 1) + "m_InstList.length-node.m_End+1: " + (m_InstList.length - m_End - 1));
        


        throw ex;
      }
      m_InstList = instList;
      
      m_End += 1;
      m_NodeRanges = m_EuclideanDistance.updateRanges(inst, m_NodeRanges);
      

      m_Splitter.setInstanceList(m_InstList);
      

      double[][] universe = m_EuclideanDistance.getRanges();
      if ((node.numInstances() > m_MaxInstInLeaf) && (getMaxRelativeNodeWidth(m_NodeRanges, universe) > m_MinBoxRelWidth))
      {
        m_Splitter.splitNode(node, m_NumNodes, m_NodeRanges, universe);
        m_NumNodes += 2;
      }
    }
    else {
      if (m_EuclideanDistance.valueIsSmallerEqual(inst, m_SplitDim, m_SplitValue))
      {
        addInstanceToTree(inst, m_Left);
        afterAddInstance(m_Right);
      } else {
        addInstanceToTree(inst, m_Right);
      }
      m_End += 1;
      m_NodeRanges = m_EuclideanDistance.updateRanges(inst, m_NodeRanges);
    }
  }
  















  protected void afterAddInstance(KDTreeNode node)
  {
    m_Start += 1;
    m_End += 1;
    if (!node.isALeaf()) {
      afterAddInstance(m_Left);
      afterAddInstance(m_Right);
    }
  }
  






  public void addInstanceInfo(Instance instance)
  {
    m_EuclideanDistance.updateRanges(instance);
  }
  





  protected void checkMissing(Instances instances)
    throws Exception
  {
    for (int i = 0; i < instances.numInstances(); i++) {
      Instance ins = instances.instance(i);
      for (int j = 0; j < ins.numValues(); j++) {
        if ((ins.index(j) != ins.classIndex()) && 
          (ins.isMissingSparse(j))) {
          throw new Exception("ERROR: KDTree can not deal with missing values. Please run ReplaceMissingValues filter on the dataset before passing it on to the KDTree.");
        }
      }
    }
  }
  







  protected void checkMissing(Instance ins)
    throws Exception
  {
    for (int j = 0; j < ins.numValues(); j++) {
      if ((ins.index(j) != ins.classIndex()) && 
        (ins.isMissingSparse(j))) {
        throw new Exception("ERROR: KDTree can not deal with missing values. Please run ReplaceMissingValues filter on the dataset before passing it on to the KDTree.");
      }
    }
  }
  














  protected double getMaxRelativeNodeWidth(double[][] nodeRanges, double[][] universe)
  {
    int widest = widestDim(nodeRanges, universe);
    if (widest < 0) {
      return 0.0D;
    }
    return nodeRanges[widest][2] / universe[widest][2];
  }
  










  protected int widestDim(double[][] nodeRanges, double[][] universe)
  {
    int classIdx = m_Instances.classIndex();
    double widest = 0.0D;
    int w = -1;
    if (m_NormalizeNodeWidth) {
      for (int i = 0; i < nodeRanges.length; i++) {
        double newWidest = nodeRanges[i][2] / universe[i][2];
        if ((newWidest > widest) && 
          (i != classIdx))
        {
          widest = newWidest;
          w = i;
        }
      }
    } else {
      for (int i = 0; i < nodeRanges.length; i++) {
        if ((nodeRanges[i][2] > widest) && 
          (i != classIdx))
        {
          widest = nodeRanges[i][2];
          w = i;
        }
      }
    }
    return w;
  }
  




  public double measureTreeSize()
  {
    return m_NumNodes;
  }
  




  public double measureNumLeaves()
  {
    return m_NumLeaves;
  }
  




  public double measureMaxDepth()
  {
    return m_MaxDepth;
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
    throw new IllegalArgumentException(additionalMeasureName + " not supported (KDTree)");
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
  








  public void centerInstances(Instances centers, int[] assignments, double pc)
    throws Exception
  {
    int[] centList = new int[centers.numInstances()];
    for (int i = 0; i < centers.numInstances(); i++) {
      centList[i] = i;
    }
    determineAssignments(m_Root, centers, centList, assignments, pc);
  }
  












  protected void determineAssignments(KDTreeNode node, Instances centers, int[] candidates, int[] assignments, double pc)
    throws Exception
  {
    int[] owners = refineOwners(node, centers, candidates);
    

    if (owners.length == 1)
    {
      for (int i = m_Start; i <= m_End; i++) {
        assignments[m_InstList[i]] = owners[0];
      }
    }
    else if (!node.isALeaf())
    {
      determineAssignments(m_Left, centers, owners, assignments, pc);
      determineAssignments(m_Right, centers, owners, assignments, pc);
    }
    else
    {
      assignSubToCenters(node, centers, owners, assignments);
    }
  }
  









  protected int[] refineOwners(KDTreeNode node, Instances centers, int[] candidates)
    throws Exception
  {
    int[] owners = new int[candidates.length];
    double minDistance = Double.POSITIVE_INFINITY;
    int ownerIndex = -1;
    
    int numCand = candidates.length;
    double[] distance = new double[numCand];
    boolean[] inside = new boolean[numCand];
    for (int i = 0; i < numCand; i++) {
      distance[i] = distanceToHrect(node, centers.instance(candidates[i]));
      inside[i] = (distance[i] == 0.0D ? 1 : false);
      if (distance[i] < minDistance) {
        minDistance = distance[i];
        ownerIndex = i;
      }
    }
    Instance owner = new Instance(centers.instance(candidates[ownerIndex]));
    



    int index = 0;
    for (int i = 0; i < numCand; i++)
    {
      if ((inside[i] != 0) || (distance[i] == distance[ownerIndex]))
      {




        owners[(index++)] = candidates[i];
      }
      else {
        Instance competitor = new Instance(centers.instance(candidates[i]));
        if (!candidateIsFullOwner(node, owner, competitor))
        {






          owners[(index++)] = candidates[i];
        }
      }
    }
    int[] result = new int[index];
    for (int i = 0; i < index; i++)
      result[i] = owners[i];
    return result;
  }
  








  protected double distanceToHrect(KDTreeNode node, Instance x)
    throws Exception
  {
    double distance = 0.0D;
    
    Instance closestPoint = new Instance(x);
    
    boolean inside = clipToInsideHrect(node, closestPoint);
    if (!inside)
      distance = m_EuclideanDistance.distance(closestPoint, x);
    return distance;
  }
  











  protected boolean clipToInsideHrect(KDTreeNode node, Instance x)
  {
    boolean inside = true;
    for (int i = 0; i < m_Instances.numAttributes(); i++)
    {

      if (x.value(i) < m_NodeRanges[i][0]) {
        x.setValue(i, m_NodeRanges[i][0]);
        inside = false;
      } else if (x.value(i) > m_NodeRanges[i][1]) {
        x.setValue(i, m_NodeRanges[i][1]);
        inside = false;
      }
    }
    return inside;
  }
  

























  protected boolean candidateIsFullOwner(KDTreeNode node, Instance candidate, Instance competitor)
    throws Exception
  {
    Instance extreme = new Instance(candidate);
    for (int i = 0; i < m_Instances.numAttributes(); i++) {
      if (competitor.value(i) - candidate.value(i) > 0.0D) {
        extreme.setValue(i, m_NodeRanges[i][1]);
      } else {
        extreme.setValue(i, m_NodeRanges[i][0]);
      }
    }
    boolean isFullOwner = m_EuclideanDistance.distance(extreme, candidate) < m_EuclideanDistance.distance(extreme, competitor);
    

    return isFullOwner;
  }
  










  public void assignSubToCenters(KDTreeNode node, Instances centers, int[] centList, int[] assignments)
    throws Exception
  {
    int numCent = centList.length;
    


    if (assignments == null) {
      assignments = new int[m_Instances.numInstances()];
      for (int i = 0; i < assignments.length; i++) {
        assignments[i] = -1;
      }
    }
    

    for (int i = m_Start; i <= m_End; i++) {
      int instIndex = m_InstList[i];
      Instance inst = m_Instances.instance(instIndex);
      
      int newC = m_EuclideanDistance.closestPoint(inst, centers, centList);
      
      assignments[instIndex] = newC;
    }
  }
  






























  public String minBoxRelWidthTipText()
  {
    return "The minimum relative width of the box. A node is only made a leaf if the width of the split dimension of the instances in a node normalized over the width of the split dimension of all the instances is less than or equal to this minimum relative width.";
  }
  







  public void setMinBoxRelWidth(double i)
  {
    m_MinBoxRelWidth = i;
  }
  




  public double getMinBoxRelWidth()
  {
    return m_MinBoxRelWidth;
  }
  




  public String maxInstInLeafTipText()
  {
    return "The max number of instances in a leaf.";
  }
  




  public void setMaxInstInLeaf(int i)
  {
    m_MaxInstInLeaf = i;
  }
  




  public int getMaxInstInLeaf()
  {
    return m_MaxInstInLeaf;
  }
  




  public String normalizeNodeWidthTipText()
  {
    return "Whether if the widths of the KDTree node should be normalized by the width of the universe or not. Where, width of the node is the range of the split attribute based on the instances in that node, and width of the universe is the range of the split attribute based on all the instances (default: false).";
  }
  










  public void setNormalizeNodeWidth(boolean n)
  {
    m_NormalizeNodeWidth = n;
  }
  




  public boolean getNormalizeNodeWidth()
  {
    return m_NormalizeNodeWidth;
  }
  




  public DistanceFunction getDistanceFunction()
  {
    return m_EuclideanDistance;
  }
  




  public void setDistanceFunction(DistanceFunction df)
    throws Exception
  {
    if (!(df instanceof EuclideanDistance)) {
      throw new Exception("KDTree currently only works with EuclideanDistanceFunction.");
    }
    m_DistanceFunction = (this.m_EuclideanDistance = (EuclideanDistance)df);
  }
  





  public String nodeSplitterTipText()
  {
    return "The the splitting method to split the nodes of the KDTree.";
  }
  





  public KDTreeNodeSplitter getNodeSplitter()
  {
    return m_Splitter;
  }
  




  public void setNodeSplitter(KDTreeNodeSplitter splitter)
  {
    m_Splitter = splitter;
  }
  





  public String globalInfo()
  {
    return "Class implementing the KDTree search algorithm for nearest neighbour search.\nThe connection to dataset is only a reference. For the tree structure the indexes are stored in an array. \nBuilding the tree:\nIf a node has <maximal-inst-number> (option -L) instances no further splitting is done. Also if the split would leave one side empty, the branch is not split any further even if the instances in the resulting node are more than <maximal-inst-number> instances.\n**PLEASE NOTE:** The algorithm can not handle missing values, so it is advisable to run ReplaceMissingValues filter if there are any missing values in the dataset.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  



















  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.add(new Option("\tNode splitting method to use.\n\t(default: weka.core.neighboursearch.kdtrees.SlidingMidPointOfWidestSide)", "S", 1, "-S <classname and options>"));
    



    newVector.addElement(new Option("\tSet minimal width of a box\n\t(default: 1.0E-2).", "W", 0, "-W <value>"));
    



    newVector.addElement(new Option("\tMaximal number of instances in a leaf\n\t(default: 40).", "L", 0, "-L"));
    



    newVector.addElement(new Option("\tNormalizing will be done\n\t(Select dimension for split, with normalising to universe).", "N", 0, "-N"));
    



    return newVector.elements();
  }
  

























  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      String[] splitMethodSpec = Utils.splitOptions(optionString);
      if (splitMethodSpec.length == 0) {
        throw new Exception("Invalid DistanceFunction specification string.");
      }
      String className = splitMethodSpec[0];
      splitMethodSpec[0] = "";
      
      setNodeSplitter((KDTreeNodeSplitter)Utils.forName(KDTreeNodeSplitter.class, className, splitMethodSpec));
    }
    else
    {
      setNodeSplitter(new SlidingMidPointOfWidestSide());
    }
    
    optionString = Utils.getOption('W', options);
    if (optionString.length() != 0) {
      setMinBoxRelWidth(Double.parseDouble(optionString));
    } else {
      setMinBoxRelWidth(0.01D);
    }
    optionString = Utils.getOption('L', options);
    if (optionString.length() != 0) {
      setMaxInstInLeaf(Integer.parseInt(optionString));
    } else {
      setMaxInstInLeaf(40);
    }
    setNormalizeNodeWidth(Utils.getFlag('N', options));
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-S");
    result.add((m_Splitter.getClass().getName() + " " + Utils.joinOptions(m_Splitter.getOptions())).trim());
    


    result.add("-W");
    result.add("" + getMinBoxRelWidth());
    
    result.add("-L");
    result.add("" + getMaxInstInLeaf());
    
    if (getNormalizeNodeWidth()) {
      result.add("-N");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

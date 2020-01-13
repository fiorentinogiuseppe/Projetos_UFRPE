package weka.core.neighboursearch.balltrees;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.DistanceFunction;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;













































































public class MiddleOutConstructor
  extends BallTreeConstructor
  implements Randomizable, TechnicalInformationHandler
{
  private static final long serialVersionUID = -8523314263062524462L;
  protected int m_RSeed = 1;
  





  protected Random rand = new Random(m_RSeed);
  



  private double rootRadius = -1.0D;
  




  protected boolean m_RandomInitialAnchor = true;
  





  public MiddleOutConstructor() {}
  




  public String globalInfo()
  {
    return "The class that builds a BallTree middle out.\n\nFor more information see also:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Andrew W. Moore");
    result.setValue(TechnicalInformation.Field.TITLE, "The Anchors Hierarchy: Using the Triangle Inequality to Survive High Dimensional Data");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "UAI '00: Proceedings of the 16th Conference on Uncertainty in Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.PAGES, "397-405");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann Publishers Inc.");
    result.setValue(TechnicalInformation.Field.ADDRESS, "San Francisco, CA, USA");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.MASTERSTHESIS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Ashraf Masood Kibriya");
    additional.setValue(TechnicalInformation.Field.TITLE, "Fast Algorithms for Nearest Neighbour Search");
    additional.setValue(TechnicalInformation.Field.YEAR, "2007");
    additional.setValue(TechnicalInformation.Field.SCHOOL, "Department of Computer Science, School of Computing and Mathematical Sciences, University of Waikato");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    return result;
  }
  




  public BallNode buildTree()
    throws Exception
  {
    m_NumNodes = (this.m_MaxDepth = this.m_NumLeaves = 0);
    if (rootRadius == -1.0D) {
      rootRadius = BallNode.calcRadius(m_InstList, m_Instances, BallNode.calcCentroidPivot(m_InstList, m_Instances), m_DistanceFunction);
    }
    

    BallNode root = buildTreeMiddleOut(0, m_Instances.numInstances() - 1);
    return root;
  }
  















  protected BallNode buildTreeMiddleOut(int startIdx, int endIdx)
    throws Exception
  {
    int numInsts = endIdx - startIdx + 1;
    int numAnchors = (int)Math.round(Math.sqrt(numInsts));
    

    if (numAnchors > 1) {
      Instance pivot = BallNode.calcCentroidPivot(startIdx, endIdx, m_InstList, m_Instances);
      double radius = BallNode.calcRadius(startIdx, endIdx, m_InstList, m_Instances, pivot, m_DistanceFunction);
      
      if ((numInsts <= m_MaxInstancesInLeaf) || (rootRadius == 0.0D) || (radius / rootRadius < m_MaxRelLeafRadius))
      {
        BallNode node = new BallNode(startIdx, endIdx, m_NumNodes, pivot, radius);
        return node;
      }
      Vector anchors = new Vector(numAnchors);
      createAnchorsHierarchy(anchors, numAnchors, startIdx, endIdx);
      
      BallNode node = mergeNodes(anchors, startIdx, endIdx);
      
      buildLeavesMiddleOut(node);
      
      return node;
    }
    Instance pivot;
    BallNode node = new BallNode(startIdx, endIdx, m_NumNodes, pivot = BallNode.calcCentroidPivot(startIdx, endIdx, m_InstList, m_Instances), BallNode.calcRadius(startIdx, endIdx, m_InstList, m_Instances, pivot, m_DistanceFunction));
    





    return node;
  }
  
















  protected void createAnchorsHierarchy(Vector anchors, int numAnchors, int startIdx, int endIdx)
    throws Exception
  {
    TempNode anchr1 = m_RandomInitialAnchor ? getRandomAnchor(startIdx, endIdx) : getFurthestFromMeanAnchor(startIdx, endIdx);
    


    TempNode amax = anchr1;
    
    Vector anchorDistances = new Vector(numAnchors - 1);
    anchors.add(anchr1);
    

    while (anchors.size() < numAnchors)
    {
      TempNode newAnchor = new TempNode();
      points = new MyIdxList();
      Instance newpivot = m_Instances.instance(points.getFirst().idx);
      anchor = newpivot;
      idx = points.getFirst().idx;
      
      setInterAnchorDistances(anchors, newAnchor, anchorDistances);
      if (stealPoints(newAnchor, anchors, anchorDistances)) {
        radius = points.getFirst().distance;
      } else
        radius = 0.0D;
      anchors.add(newAnchor);
      

      amax = (TempNode)anchors.elementAt(0);
      for (int i = 1; i < anchors.size(); i++) {
        newAnchor = (TempNode)anchors.elementAt(i);
        if (radius > radius) {
          amax = newAnchor;
        }
      }
    }
  }
  












  protected void buildLeavesMiddleOut(BallNode node)
    throws Exception
  {
    if ((m_Left != null) && (m_Right != null)) {
      buildLeavesMiddleOut(m_Left);
      buildLeavesMiddleOut(m_Right);
    } else {
      if ((m_Left != null) || (m_Right != null)) {
        throw new Exception("Invalid leaf assignment. Please check code");
      }
      
      BallNode n2 = buildTreeMiddleOut(m_Start, m_End);
      if ((m_Left != null) && (m_Right != null)) {
        m_Left = m_Left;
        m_Right = m_Right;
        buildLeavesMiddleOut(node);


      }
      else if ((m_Left != null) || (m_Right != null)) {
        throw new Exception("Invalid leaf assignment. Please check code");
      }
    }
  }
  














  protected BallNode mergeNodes(Vector list, int startIdx, int endIdx)
    throws Exception
  {
    for (int i = 0; i < list.size(); i++) {
      TempNode n = (TempNode)list.get(i);
      anchor = calcPivot(points, new MyIdxList(), m_Instances);
      radius = calcRadius(points, new MyIdxList(), anchor, m_Instances);
    }
    
    Instance minPivot = null;
    int min1 = -1;int min2 = -1;
    
    while (list.size() > 1) {
      double minRadius = Double.POSITIVE_INFINITY;
      
      for (int i = 0; i < list.size(); i++) {
        TempNode first = (TempNode)list.get(i);
        for (int j = i + 1; j < list.size(); j++) {
          TempNode second = (TempNode)list.get(j);
          Instance pivot = calcPivot(first, second, m_Instances);
          double tmpRadius = calcRadius(first, second);
          if (tmpRadius < minRadius) {
            minRadius = tmpRadius;
            minPivot = pivot;
            min1 = i;min2 = j;
          }
        }
      }
      
      TempNode parent = new TempNode();
      left = ((TempNode)list.get(min1));
      right = ((TempNode)list.get(min2));
      anchor = minPivot;
      radius = calcRadius(left.points, right.points, minPivot, m_Instances);
      points = left.points.append(left.points, right.points);
      list.remove(min1);list.remove(min2 - 1);
      list.add(parent);
    }
    TempNode tmpRoot = (TempNode)list.get(list.size() - 1);
    
    if (endIdx - startIdx + 1 != points.length()) {
      throw new Exception("Root nodes instance list is of irregular length. Please check code. Length should be: " + (endIdx - startIdx + 1) + " whereas it is found to be: " + points.length());
    }
    


    for (int i = 0; i < points.length(); i++) {
      m_InstList[(startIdx + i)] = points.get(i).idx;
    }
    
    BallNode node = makeBallTreeNodes(tmpRoot, startIdx, endIdx, 0);
    
    return node;
  }
  
















  protected BallNode makeBallTreeNodes(TempNode node, int startidx, int endidx, int depth)
  {
    BallNode ball = null;
    
    if ((left != null) && (right != null)) {
      ball = new BallNode(startidx, endidx, m_NumNodes, anchor, radius);
      



      m_NumNodes += 1;
      m_Left = makeBallTreeNodes(left, startidx, startidx + left.points.length() - 1, depth + 1);
      m_Right = makeBallTreeNodes(right, startidx + left.points.length(), endidx, depth + 1);
      m_MaxDepth += 1;
    }
    else {
      ball = new BallNode(startidx, endidx, m_NumNodes, anchor, radius);
      


      m_NumNodes += 1;
      m_NumLeaves += 1;
    }
    return ball;
  }
  












  protected TempNode getFurthestFromMeanAnchor(int startIdx, int endIdx)
  {
    TempNode anchor = new TempNode();
    Instance centroid = BallNode.calcCentroidPivot(startIdx, endIdx, m_InstList, m_Instances);
    


    radius = Double.NEGATIVE_INFINITY;
    for (int i = startIdx; i <= endIdx; i++) {
      Instance temp = m_Instances.instance(m_InstList[i]);
      double tmpr = m_DistanceFunction.distance(centroid, temp);
      if (tmpr > radius) {
        idx = m_InstList[i];
        anchor = temp;
        radius = tmpr;
      }
    }
    
    setPoints(anchor, startIdx, endIdx, m_InstList);
    return anchor;
  }
  










  protected TempNode getRandomAnchor(int startIdx, int endIdx)
  {
    TempNode anchr1 = new TempNode();
    idx = m_InstList[(startIdx + rand.nextInt(endIdx - startIdx + 1))];
    anchor = m_Instances.instance(idx);
    setPoints(anchr1, startIdx, endIdx, m_InstList);
    radius = points.getFirst().distance;
    
    return anchr1;
  }
  















  public void setPoints(TempNode node, int startIdx, int endIdx, int[] indices)
  {
    points = new MyIdxList();
    
    for (int i = startIdx; i <= endIdx; i++) {
      Instance temp = m_Instances.instance(indices[i]);
      double dist = m_DistanceFunction.distance(anchor, temp);
      points.insertReverseSorted(indices[i], dist);
    }
  }
  











  public void setInterAnchorDistances(Vector anchors, TempNode newAnchor, Vector anchorDistances)
    throws Exception
  {
    double[] distArray = new double[anchors.size()];
    
    for (int i = 0; i < anchors.size(); i++) {
      Instance anchr = elementAtanchor;
      distArray[i] = m_DistanceFunction.distance(anchr, anchor);
    }
    anchorDistances.add(distArray);
  }
  














  public boolean stealPoints(TempNode newAnchor, Vector anchors, Vector anchorDistances)
  {
    int maxIdx = -1;
    double maxDist = Double.NEGATIVE_INFINITY;
    double[] distArray = (double[])anchorDistances.lastElement();
    
    for (int i = 0; i < distArray.length; i++) {
      if (maxDist < distArray[i]) {
        maxDist = distArray[i];maxIdx = i;
      }
    }
    boolean anyPointsStolen = false;boolean pointsStolen = false;
    

    Instance newAnchInst = anchor;
    for (int i = 0; i < anchors.size(); i++) {
      TempNode anchorI = (TempNode)anchors.elementAt(i);
      Instance anchIInst = anchor;
      
      pointsStolen = false;
      double interAnchMidDist = m_DistanceFunction.distance(newAnchInst, anchIInst) / 2.0D;
      for (int j = 0; j < points.length(); j++) {
        ListNode tmp = points.get(j);
        

        if (distance < interAnchMidDist) {
          break;
        }
        double newDist = m_DistanceFunction.distance(newAnchInst, m_Instances.instance(idx));
        
        double distI = distance;
        if (newDist < distI) {
          points.insertReverseSorted(idx, newDist);
          points.remove(j);
          anyPointsStolen = pointsStolen = 1;
        }
      }
      if (pointsStolen)
        radius = points.getFirst().distance;
    }
    return anyPointsStolen;
  }
  










  public Instance calcPivot(TempNode node1, TempNode node2, Instances insts)
  {
    int classIdx = m_Instances.classIndex();
    double[] attrVals = new double[insts.numAttributes()];
    
    double anchr1Ratio = points.length() / (points.length() + points.length());
    
    double anchr2Ratio = points.length() / (points.length() + points.length());
    
    for (int k = 0; k < anchor.numValues(); k++) {
      if (anchor.index(k) != classIdx)
      {
        attrVals[k] += anchor.valueSparse(k) * anchr1Ratio; }
    }
    for (int k = 0; k < anchor.numValues(); k++) {
      if (anchor.index(k) != classIdx)
      {
        attrVals[k] += anchor.valueSparse(k) * anchr2Ratio; }
    }
    Instance temp = new Instance(1.0D, attrVals);
    return temp;
  }
  










  public Instance calcPivot(MyIdxList list1, MyIdxList list2, Instances insts)
  {
    int classIdx = m_Instances.classIndex();
    double[] attrVals = new double[insts.numAttributes()];
    

    for (int i = 0; i < list1.length(); i++) {
      Instance temp = insts.instance(getidx);
      for (int k = 0; k < temp.numValues(); k++) {
        if (temp.index(k) != classIdx)
        {
          attrVals[k] += temp.valueSparse(k); }
      }
    }
    for (int j = 0; j < list2.length(); j++) {
      Instance temp = insts.instance(getidx);
      for (int k = 0; k < temp.numValues(); k++) {
        if (temp.index(k) != classIdx)
        {
          attrVals[k] += temp.valueSparse(k); }
      }
    }
    int j = 0;int numInsts = list1.length() + list2.length();
    for (; j < attrVals.length; j++) {
      attrVals[j] /= numInsts;
    }
    Instance temp = new Instance(1.0D, attrVals);
    return temp;
  }
  







  public double calcRadius(TempNode n1, TempNode n2)
  {
    Instance p1 = anchor;Instance p2 = anchor;
    double radius = radius + m_DistanceFunction.distance(p1, p2) + radius;
    return radius / 2.0D;
  }
  











  public double calcRadius(MyIdxList list1, MyIdxList list2, Instance pivot, Instances insts)
  {
    double radius = Double.NEGATIVE_INFINITY;
    
    for (int i = 0; i < list1.length(); i++) {
      double dist = m_DistanceFunction.distance(pivot, insts.instance(getidx));
      
      if (dist > radius)
        radius = dist;
    }
    for (int j = 0; j < list2.length(); j++) {
      double dist = m_DistanceFunction.distance(pivot, insts.instance(getidx));
      
      if (dist > radius)
        radius = dist;
    }
    return radius;
  }
  












  public int[] addInstance(BallNode node, Instance inst)
    throws Exception
  {
    throw new Exception("Addition of instances after the tree is built, not possible with MiddleOutConstructor.");
  }
  






  public void setMaxInstancesInLeaf(int num)
    throws Exception
  {
    if (num < 2) {
      throw new Exception("The maximum number of instances in a leaf for using MiddleOutConstructor must be >=2.");
    }
    super.setMaxInstancesInLeaf(num);
  }
  




  public void setInstances(Instances insts)
  {
    super.setInstances(insts);
    rootRadius = -1.0D;
  }
  






  public void setInstanceList(int[] instList)
  {
    super.setInstanceList(instList);
    rootRadius = -1.0D;
  }
  





  public String initialAnchorRandomTipText()
  {
    return "Whether the initial anchor is chosen randomly.";
  }
  



  public boolean isInitialAnchorRandom()
  {
    return m_RandomInitialAnchor;
  }
  





  public void setInitialAnchorRandom(boolean randomInitialAnchor)
  {
    m_RandomInitialAnchor = randomInitialAnchor;
  }
  





  public String seedTipText()
  {
    return "The seed value for the random number generator.";
  }
  



  public int getSeed()
  {
    return m_RSeed;
  }
  





  public void setSeed(int seed)
  {
    m_RSeed = seed;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tThe seed for the random number generator used\n\tin selecting random anchor.\n(default: 1)", "S", 1, "-S <num>"));
    




    newVector.addElement(new Option("\tUse randomly chosen initial anchors.", "R", 0, "-R"));
    


    return newVector.elements();
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String temp = Utils.getOption('S', options);
    if (temp.length() > 0) {
      setSeed(Integer.parseInt(temp));
    }
    else {
      setSeed(1);
    }
    
    setInitialAnchorRandom(Utils.getFlag('R', options));
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-S");
    result.add("" + getSeed());
    
    if (isInitialAnchorRandom()) {
      result.add("-R");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  














  public void checkIndicesList(MyIdxList list, int startidx, int endidx)
    throws Exception
  {
    for (int i = 0; i < list.size(); i++) {
      ListNode node = (ListNode)list.elementAt(i);
      boolean found = false;
      for (int j = startidx; j <= endidx; j++) {
        if (idx == m_InstList[j]) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Exception("Error: Element " + idx + " of the list not in " + "the array." + "\nArray: " + printInsts(startidx, endidx) + "\nList: " + printList(list));
      }
    }
  }
  












  public String printInsts(int startIdx, int endIdx)
  {
    StringBuffer bf = new StringBuffer();
    try {
      bf.append("i: ");
      for (int i = startIdx; i <= endIdx; i++) {
        if (i == startIdx) {
          bf.append("" + m_InstList[i]);
        } else
          bf.append(", " + m_InstList[i]);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return bf.toString();
  }
  





  public String printList(MyIdxList points)
  {
    if ((points == null) || (points.length() == 0)) return "";
    StringBuffer bf = new StringBuffer();
    try
    {
      for (int i = 0; i < points.size(); i++) {
        ListNode temp = (ListNode)points.elementAt(i);
        if (i == 0) {
          bf.append("" + idx);
        } else
          bf.append(", " + idx);
      }
    } catch (Exception ex) { ex.printStackTrace(); }
    return bf.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
  



  protected class TempNode
    implements RevisionHandler
  {
    Instance anchor;
    


    int idx;
    


    double radius;
    


    MiddleOutConstructor.MyIdxList points;
    


    TempNode left;
    


    TempNode right;
    



    protected TempNode() {}
    


    public String toString()
    {
      if ((points == null) || (points.length() == 0)) return idx + "";
      StringBuffer bf = new StringBuffer();
      try {
        bf.append(idx + " p: ");
        
        for (int i = 0; i < points.size(); i++) {
          MiddleOutConstructor.ListNode temp = (MiddleOutConstructor.ListNode)points.elementAt(i);
          if (i == 0) {
            bf.append("" + idx);
          } else
            bf.append(", " + idx);
        }
      } catch (Exception ex) { ex.printStackTrace(); }
      return bf.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.3 $");
    }
  }
  









  protected class ListNode
    implements RevisionHandler
  {
    int idx = -1;
    

    double distance = Double.NEGATIVE_INFINITY;
    





    public ListNode(int i, double d)
    {
      idx = i;
      distance = d;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.3 $");
    }
  }
  





  protected class MyIdxList
    extends FastVector
  {
    private static final long serialVersionUID = -2283869109722934927L;
    





    public MyIdxList() {}
    





    public MyIdxList(int size)
    {
      super();
    }
    



    public MiddleOutConstructor.ListNode getFirst()
    {
      return (MiddleOutConstructor.ListNode)elementAt(0);
    }
    







    public void insertReverseSorted(int idx, double distance)
    {
      Enumeration en = elements();
      int i = 0;
      while (en.hasMoreElements()) {
        MiddleOutConstructor.ListNode temp = (MiddleOutConstructor.ListNode)en.nextElement();
        if (distance < distance)
          break;
        i++;
      }
      insertElementAt(new MiddleOutConstructor.ListNode(MiddleOutConstructor.this, idx, distance), i);
    }
    






    public MiddleOutConstructor.ListNode get(int index)
    {
      return (MiddleOutConstructor.ListNode)elementAt(index);
    }
    





    public void remove(int index)
    {
      removeElementAt(index);
    }
    



    public int length()
    {
      return super.size();
    }
    








    public MyIdxList append(MyIdxList list1, MyIdxList list2)
    {
      MyIdxList temp = new MyIdxList(MiddleOutConstructor.this, list1.size() + list2.size());
      temp.appendElements(list1);
      temp.appendElements(list2);
      return temp;
    }
    





    public void checkSorting(MyIdxList list)
      throws Exception
    {
      Enumeration en = list.elements();
      MiddleOutConstructor.ListNode first = null;MiddleOutConstructor.ListNode second = null;
      while (en.hasMoreElements()) {
        if (first == null) {
          first = (MiddleOutConstructor.ListNode)en.nextElement();
        } else {
          second = (MiddleOutConstructor.ListNode)en.nextElement();
          if (distance < distance) {
            throw new Exception("List not sorted correctly. first.distance: " + distance + " second.distance: " + distance + " Please check code.");
          }
        }
      }
    }
    






    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.3 $");
    }
  }
}

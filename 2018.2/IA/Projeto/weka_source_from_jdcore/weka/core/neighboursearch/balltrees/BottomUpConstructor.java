package weka.core.neighboursearch.balltrees;

import java.io.PrintStream;
import weka.core.DistanceFunction;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
































































public class BottomUpConstructor
  extends BallTreeConstructor
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 5864250777657707687L;
  
  public String globalInfo()
  {
    return "The class that constructs a ball tree bottom up.";
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
    
    return result;
  }
  




  public BottomUpConstructor() {}
  




  public BallNode buildTree()
    throws Exception
  {
    FastVector list = new FastVector();
    
    for (int i = 0; i < m_InstList.length; i++) {
      TempNode n = new TempNode();
      points = new int[1];points[0] = m_InstList[i];
      anchor = m_Instances.instance(m_InstList[i]);
      radius = 0.0D;
      list.addElement(n);
    }
    
    return mergeNodes(list, 0, m_InstList.length - 1, m_InstList);
  }
  
















  protected BallNode mergeNodes(FastVector list, int startIdx, int endIdx, int[] instList)
    throws Exception
  {
    double minRadius = Double.POSITIVE_INFINITY;
    Instance minPivot = null;int min1 = -1;int min2 = -1;
    int[] minInstList = null;int merge = 1;
    

    while (list.size() > 1) {
      System.err.print("merge step: " + merge++ + "               \r");
      minRadius = Double.POSITIVE_INFINITY;
      min1 = -1;min2 = -1;
      
      for (int i = 0; i < list.size(); i++) {
        TempNode first = (TempNode)list.elementAt(i);
        for (int j = i + 1; j < list.size(); j++) {
          TempNode second = (TempNode)list.elementAt(j);
          Instance pivot = calcPivot(first, second, m_Instances);
          double tmpRadius = calcRadius(first, second);
          if (tmpRadius < minRadius) {
            minRadius = tmpRadius;
            min1 = i;min2 = j;
            minPivot = pivot;
          }
        }
      }
      TempNode parent = new TempNode();
      left = ((TempNode)list.elementAt(min1));
      right = ((TempNode)list.elementAt(min2));
      minInstList = new int[left.points.length + right.points.length];
      System.arraycopy(left.points, 0, minInstList, 0, left.points.length);
      System.arraycopy(right.points, 0, minInstList, left.points.length, right.points.length);
      
      points = minInstList;
      anchor = minPivot;
      radius = BallNode.calcRadius(points, m_Instances, minPivot, m_DistanceFunction);
      list.removeElementAt(min1);list.removeElementAt(min2 - 1);
      list.addElement(parent);
    }
    System.err.println("");
    TempNode tmpRoot = (TempNode)list.elementAt(0);
    
    if (m_InstList.length != points.length) {
      throw new Exception("Root nodes instance list is of irregular length. Please check code.");
    }
    System.arraycopy(points, 0, m_InstList, 0, points.length);
    
    m_NumNodes = (this.m_MaxDepth = this.m_NumLeaves = 0);
    double tmpRadius = BallNode.calcRadius(instList, m_Instances, anchor, m_DistanceFunction);
    BallNode node = makeBallTree(tmpRoot, startIdx, endIdx, instList, 0, tmpRadius);
    
    return node;
  }
  
















  protected BallNode makeBallTree(TempNode node, int startidx, int endidx, int[] instList, int depth, double rootRadius)
    throws Exception
  {
    BallNode ball = null;
    

    if (m_MaxDepth < depth) {
      m_MaxDepth = depth;
    }
    if ((points.length > m_MaxInstancesInLeaf) && (rootRadius != 0.0D) && (radius / rootRadius >= m_MaxRelLeafRadius) && (left != null) && (right != null))
    {
      Instance pivot;
      ball = new BallNode(startidx, endidx, m_NumNodes, pivot = BallNode.calcCentroidPivot(startidx, endidx, instList, m_Instances), BallNode.calcRadius(startidx, endidx, instList, m_Instances, pivot, m_DistanceFunction));
      




      m_NumNodes += 1;
      m_Left = makeBallTree(left, startidx, startidx + left.points.length - 1, instList, depth + 1, rootRadius);
      m_Right = makeBallTree(right, startidx + left.points.length, endidx, instList, depth + 1, rootRadius);
    } else {
      Instance pivot;
      ball = new BallNode(startidx, endidx, m_NumNodes, pivot = BallNode.calcCentroidPivot(startidx, endidx, instList, m_Instances), BallNode.calcRadius(startidx, endidx, instList, m_Instances, pivot, m_DistanceFunction));
      



      m_NumNodes += 1;
      m_NumLeaves += 1;
    }
    return ball;
  }
  









  public int[] addInstance(BallNode node, Instance inst)
    throws Exception
  {
    throw new Exception("BottomUpConstruction method does not allow addition of new Instances.");
  }
  











  public Instance calcPivot(TempNode node1, TempNode node2, Instances insts)
    throws Exception
  {
    int classIdx = m_Instances.classIndex();
    double[] attrVals = new double[insts.numAttributes()];
    
    double anchr1Ratio = points.length / (points.length + points.length);
    
    double anchr2Ratio = points.length / (points.length + points.length);
    
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
  







  public double calcRadius(TempNode n1, TempNode n2)
    throws Exception
  {
    Instance a1 = anchor;Instance a2 = anchor;
    double radius = radius + m_DistanceFunction.distance(a1, a2) + radius;
    return radius / 2.0D;
  }
  



  protected class TempNode
    implements RevisionHandler
  {
    Instance anchor;
    


    double radius;
    


    int[] points;
    


    TempNode left = null;
    
    TempNode right = null;
    

    protected TempNode() {}
    
    public String toString()
    {
      StringBuffer bf = new StringBuffer();
      bf.append("p: ");
      for (int i = 0; i < points.length; i++)
        if (i != 0) {
          bf.append(", " + points[i]);
        } else
          bf.append("" + points[i]);
      return bf.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.3 $");
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

package weka.core.neighboursearch.balltrees;

import java.io.Serializable;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

















































public class BallNode
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8289151861759883510L;
  public int m_Start;
  public int m_End;
  public int m_NumInstances;
  public int m_NodeNumber;
  public int m_SplitAttrib = -1;
  



  public double m_SplitVal = -1.0D;
  

  public BallNode m_Left = null;
  

  public BallNode m_Right = null;
  


  protected Instance m_Pivot;
  


  protected double m_Radius;
  



  public BallNode(int nodeNumber)
  {
    m_NodeNumber = nodeNumber;
  }
  







  public BallNode(int start, int end, int nodeNumber)
  {
    m_Start = start;
    m_End = end;
    m_NodeNumber = nodeNumber;
    m_NumInstances = (end - start + 1);
  }
  









  public BallNode(int start, int end, int nodeNumber, Instance pivot, double radius)
  {
    m_Start = start;
    m_End = end;
    m_NodeNumber = nodeNumber;
    m_Pivot = pivot;
    m_Radius = radius;
    m_NumInstances = (end - start + 1);
  }
  




  public boolean isALeaf()
  {
    return (m_Left == null) && (m_Right == null);
  }
  








  public void setStartEndIndices(int start, int end)
  {
    m_Start = start;
    m_End = end;
    m_NumInstances = (end - start + 1);
  }
  




  public void setPivot(Instance pivot)
  {
    m_Pivot = pivot;
  }
  




  public Instance getPivot()
  {
    return m_Pivot;
  }
  




  public void setRadius(double radius)
  {
    m_Radius = radius;
  }
  



  public double getRadius()
  {
    return m_Radius;
  }
  





  public int numInstances()
  {
    return m_End - m_Start + 1;
  }
  









  public static Instance calcCentroidPivot(int[] instList, Instances insts)
  {
    double[] attrVals = new double[insts.numAttributes()];
    

    for (int i = 0; i < instList.length; i++) {
      Instance temp = insts.instance(instList[i]);
      for (int j = 0; j < temp.numValues(); j++) {
        attrVals[j] += temp.valueSparse(j);
      }
    }
    int j = 0; for (int numInsts = instList.length; j < attrVals.length; j++) {
      attrVals[j] /= numInsts;
    }
    Instance temp = new Instance(1.0D, attrVals);
    return temp;
  }
  














  public static Instance calcCentroidPivot(int start, int end, int[] instList, Instances insts)
  {
    double[] attrVals = new double[insts.numAttributes()];
    
    for (int i = start; i <= end; i++) {
      Instance temp = insts.instance(instList[i]);
      for (int j = 0; j < temp.numValues(); j++) {
        attrVals[j] += temp.valueSparse(j);
      }
    }
    int j = 0; for (int numInsts = end - start + 1; j < attrVals.length; j++) {
      attrVals[j] /= numInsts;
    }
    
    Instance temp = new Instance(1.0D, attrVals);
    return temp;
  }
  














  public static double calcRadius(int[] instList, Instances insts, Instance pivot, DistanceFunction distanceFunction)
    throws Exception
  {
    return calcRadius(0, instList.length - 1, instList, insts, pivot, distanceFunction);
  }
  




















  public static double calcRadius(int start, int end, int[] instList, Instances insts, Instance pivot, DistanceFunction distanceFunction)
    throws Exception
  {
    double radius = Double.NEGATIVE_INFINITY;
    
    for (int i = start; i <= end; i++) {
      double dist = distanceFunction.distance(pivot, insts.instance(instList[i]), Double.POSITIVE_INFINITY);
      

      if (dist > radius)
        radius = dist;
    }
    return Math.sqrt(radius);
  }
  










  public static Instance calcPivot(BallNode child1, BallNode child2, Instances insts)
    throws Exception
  {
    Instance p1 = child1.getPivot();Instance p2 = child2.getPivot();
    double[] attrVals = new double[p1.numAttributes()];
    
    for (int j = 0; j < attrVals.length; j++) {
      attrVals[j] += p1.value(j);
      attrVals[j] += p2.value(j);
      attrVals[j] /= 2.0D;
    }
    
    p1 = new Instance(1.0D, attrVals);
    return p1;
  }
  













  public static double calcRadius(BallNode child1, BallNode child2, Instance pivot, DistanceFunction distanceFunction)
    throws Exception
  {
    Instance p1 = child1.getPivot();Instance p2 = child2.getPivot();
    
    double radius = child1.getRadius() + distanceFunction.distance(p1, p2) + child2.getRadius();
    

    return radius / 2.0D;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

package weka.core.neighboursearch.balltrees;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;






























public abstract class BallTreeConstructor
  implements OptionHandler, Serializable, RevisionHandler
{
  protected int m_MaxInstancesInLeaf = 40;
  



  protected double m_MaxRelLeafRadius = 0.001D;
  



  protected boolean m_FullyContainChildBalls = false;
  



  protected Instances m_Instances;
  



  protected DistanceFunction m_DistanceFunction;
  



  protected int m_NumNodes;
  



  protected int m_NumLeaves;
  



  protected int m_MaxDepth;
  



  protected int[] m_InstList;
  



  public BallTreeConstructor() {}
  



  public abstract BallNode buildTree()
    throws Exception;
  



  public abstract int[] addInstance(BallNode paramBallNode, Instance paramInstance)
    throws Exception;
  



  public String maxInstancesInLeafTipText()
  {
    return "The maximum number of instances allowed in a leaf.";
  }
  



  public int getMaxInstancesInLeaf()
  {
    return m_MaxInstancesInLeaf;
  }
  




  public void setMaxInstancesInLeaf(int num)
    throws Exception
  {
    if (num < 1) {
      throw new Exception("The maximum number of instances in a leaf must be >=1.");
    }
    m_MaxInstancesInLeaf = num;
  }
  





  public String maxRelativeLeafRadiusTipText()
  {
    return "The maximum relative radius allowed for a leaf node. Itis relative to the radius of the smallest ball enclosing all the data points (that were used to build the tree). This smallest ball would be the same as the root node's ball, if ContainChildBalls property is set to false (default).";
  }
  











  public double getMaxRelativeLeafRadius()
  {
    return m_MaxRelLeafRadius;
  }
  






  public void setMaxRelativeLeafRadius(double radius)
    throws Exception
  {
    if (radius < 0.0D)
      throw new Exception("The radius for the leaves should be >= 0.0");
    m_MaxRelLeafRadius = radius;
  }
  





  public String containChildBallsTipText()
  {
    return "Whether to contain fully the child balls.";
  }
  





  public boolean getContainChildBalls()
  {
    return m_FullyContainChildBalls;
  }
  





  public void setContainChildBalls(boolean containChildBalls)
  {
    m_FullyContainChildBalls = containChildBalls;
  }
  




  public void setInstances(Instances inst)
  {
    m_Instances = inst;
  }
  






  public void setInstanceList(int[] instList)
  {
    m_InstList = instList;
  }
  




  public void setEuclideanDistanceFunction(EuclideanDistance func)
  {
    m_DistanceFunction = func;
  }
  




  public int getNumNodes()
  {
    return m_NumNodes;
  }
  



  public int getNumLeaves()
  {
    return m_NumLeaves;
  }
  



  public int getMaxDepth()
  {
    return m_MaxDepth;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tSet maximum number of instances in a leaf node\n\t(default: 40)", "N", 0, "-N <value>"));
    



    newVector.addElement(new Option("\tSet internal nodes' radius to the sum \n\tof the child balls radii. So that it \ncontains the child balls.", "R", 0, "-R"));
    




    return newVector.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      setMaxInstancesInLeaf(Integer.parseInt(optionString));
    }
    else {
      setMaxInstancesInLeaf(40);
    }
    
    setContainChildBalls(Utils.getFlag('R', options));
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-N");
    result.add("" + getMaxInstancesInLeaf());
    
    if (getContainChildBalls()) {
      result.add("-R");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

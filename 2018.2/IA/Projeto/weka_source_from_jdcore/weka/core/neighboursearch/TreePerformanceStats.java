package weka.core.neighboursearch;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.RevisionUtils;


























































public class TreePerformanceStats
  extends PerformanceStats
{
  private static final long serialVersionUID = -6637636693340810373L;
  protected int m_MinLeaves;
  protected int m_MaxLeaves;
  protected int m_SumLeaves;
  protected int m_SumSqLeaves;
  protected int m_LeafCount;
  protected int m_MinIntNodes;
  protected int m_MaxIntNodes;
  protected int m_SumIntNodes;
  protected int m_SumSqIntNodes;
  protected int m_IntNodeCount;
  
  public TreePerformanceStats()
  {
    reset();
  }
  


  public void reset()
  {
    super.reset();
    
    m_SumLeaves = (this.m_SumSqLeaves = this.m_LeafCount = 0);
    m_MinLeaves = Integer.MAX_VALUE;
    m_MaxLeaves = Integer.MIN_VALUE;
    
    m_SumIntNodes = (this.m_SumSqIntNodes = this.m_IntNodeCount = 0);
    m_MinIntNodes = Integer.MAX_VALUE;
    m_MaxIntNodes = Integer.MIN_VALUE;
  }
  



  public void searchStart()
  {
    super.searchStart();
    m_LeafCount = 0;
    m_IntNodeCount = 0;
  }
  



  public void searchFinish()
  {
    super.searchFinish();
    
    m_SumLeaves += m_LeafCount;m_SumSqLeaves += m_LeafCount * m_LeafCount;
    if (m_LeafCount < m_MinLeaves) m_MinLeaves = m_LeafCount;
    if (m_LeafCount > m_MaxLeaves) { m_MaxLeaves = m_LeafCount;
    }
    m_SumIntNodes += m_IntNodeCount;m_SumSqIntNodes += m_IntNodeCount * m_IntNodeCount;
    if (m_IntNodeCount < m_MinIntNodes) m_MinIntNodes = m_IntNodeCount;
    if (m_IntNodeCount > m_MaxIntNodes) { m_MaxIntNodes = m_IntNodeCount;
    }
  }
  

  public void incrLeafCount()
  {
    m_LeafCount += 1;
  }
  


  public void incrIntNodeCount()
  {
    m_IntNodeCount += 1;
  }
  






  public int getTotalLeavesVisited()
  {
    return m_SumLeaves;
  }
  




  public double getMeanLeavesVisited()
  {
    return m_SumLeaves / m_NumQueries;
  }
  




  public double getStdDevLeavesVisited()
  {
    return Math.sqrt((m_SumSqLeaves - m_SumLeaves * m_SumLeaves / m_NumQueries) / (m_NumQueries - 1));
  }
  




  public int getMinLeavesVisited()
  {
    return m_MinLeaves;
  }
  




  public int getMaxLeavesVisited()
  {
    return m_MaxLeaves;
  }
  






  public int getTotalIntNodesVisited()
  {
    return m_SumIntNodes;
  }
  





  public double getMeanIntNodesVisited()
  {
    return m_SumIntNodes / m_NumQueries;
  }
  




  public double getStdDevIntNodesVisited()
  {
    return Math.sqrt((m_SumSqIntNodes - m_SumIntNodes * m_SumIntNodes / m_NumQueries) / (m_NumQueries - 1));
  }
  




  public int getMinIntNodesVisited()
  {
    return m_MinIntNodes;
  }
  




  public int getMaxIntNodesVisited()
  {
    return m_MaxIntNodes;
  }
  




  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    
    Enumeration en = super.enumerateMeasures();
    while (en.hasMoreElements()) {
      newVector.addElement(en.nextElement());
    }
    newVector.addElement("measureTotal_nodes_visited");
    newVector.addElement("measureMean_nodes_visited");
    newVector.addElement("measureStdDev_nodes_visited");
    newVector.addElement("measureMin_nodes_visited");
    newVector.addElement("measureMax_nodes_visited");
    
    newVector.addElement("measureTotal_leaves_visited");
    newVector.addElement("measureMean_leaves_visited");
    newVector.addElement("measureStdDev_leaves_visited");
    newVector.addElement("measureMin_leaves_visited");
    newVector.addElement("measureMax_leaves_visited");
    
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
    throws IllegalArgumentException
  {
    if (additionalMeasureName.compareToIgnoreCase("measureTotal_nodes_visited") == 0)
      return getTotalIntNodesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMean_nodes_visited") == 0)
      return getMeanIntNodesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureStdDev_nodes_visited") == 0)
      return getStdDevIntNodesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMin_nodes_visited") == 0)
      return getMinIntNodesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMax_nodes_visited") == 0) {
      return getMaxIntNodesVisited();
    }
    
    if (additionalMeasureName.compareToIgnoreCase("measureTotal_leaves_visited") == 0)
      return getTotalLeavesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMean_leaves_visited") == 0)
      return getMeanLeavesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureStdDev_leaves_visited") == 0)
      return getStdDevLeavesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMin_leaves_visited") == 0)
      return getMinLeavesVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMax_leaves_visited") == 0) {
      return getMaxLeavesVisited();
    }
    return super.getMeasure(additionalMeasureName);
  }
  





  public String getStats()
  {
    StringBuffer buf = new StringBuffer(super.getStats());
    
    buf.append("leaves:    " + getMinLeavesVisited() + ", " + getMaxLeavesVisited() + "," + getTotalLeavesVisited() + "," + getMeanLeavesVisited() + ", " + getStdDevLeavesVisited() + "\n");
    
    buf.append("Int nodes: " + getMinIntNodesVisited() + ", " + getMaxIntNodesVisited() + "," + getTotalIntNodesVisited() + "," + getMeanIntNodesVisited() + ", " + getStdDevIntNodesVisited() + "\n");
    

    return buf.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

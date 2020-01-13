package weka.core.neighboursearch;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.AdditionalMeasureProducer;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;






























































public class PerformanceStats
  implements AdditionalMeasureProducer, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -7215110351388368092L;
  protected int m_NumQueries;
  public double m_MinP;
  public double m_MaxP;
  public double m_SumP;
  public double m_SumSqP;
  public double m_PointCount;
  public double m_MinC;
  public double m_MaxC;
  public double m_SumC;
  public double m_SumSqC;
  public double m_CoordCount;
  
  public PerformanceStats()
  {
    reset();
  }
  


  public void reset()
  {
    m_NumQueries = 0;
    
    m_SumP = (this.m_SumSqP = this.m_PointCount = 0.0D);
    m_MinP = 2.147483647E9D;
    m_MaxP = -2.147483648E9D;
    
    m_SumC = (this.m_SumSqC = this.m_CoordCount = 0.0D);
    m_MinC = 2.147483647E9D;
    m_MaxC = -2.147483648E9D;
  }
  



  public void searchStart()
  {
    m_PointCount = 0.0D;
    m_CoordCount = 0.0D;
  }
  



  public void searchFinish()
  {
    m_NumQueries += 1;m_SumP += m_PointCount;m_SumSqP += m_PointCount * m_PointCount;
    if (m_PointCount < m_MinP) m_MinP = m_PointCount;
    if (m_PointCount > m_MaxP) { m_MaxP = m_PointCount;
    }
    double coordsPerPt = m_CoordCount / m_PointCount;
    m_SumC += coordsPerPt;m_SumSqC += coordsPerPt * coordsPerPt;
    if (coordsPerPt < m_MinC) m_MinC = coordsPerPt;
    if (coordsPerPt > m_MaxC) { m_MaxC = coordsPerPt;
    }
  }
  


  public void incrPointCount()
  {
    m_PointCount += 1.0D;
  }
  




  public void incrCoordCount()
  {
    m_CoordCount += 1.0D;
  }
  




  public void updatePointCount(int n)
  {
    m_PointCount += n;
  }
  




  public int getNumQueries()
  {
    return m_NumQueries;
  }
  




  public double getTotalPointsVisited()
  {
    return m_SumP;
  }
  




  public double getMeanPointsVisited()
  {
    return m_SumP / m_NumQueries;
  }
  




  public double getStdDevPointsVisited()
  {
    return Math.sqrt((m_SumSqP - m_SumP * m_SumP / m_NumQueries) / (m_NumQueries - 1));
  }
  




  public double getMinPointsVisited()
  {
    return m_MinP;
  }
  




  public double getMaxPointsVisited()
  {
    return m_MaxP;
  }
  






  public double getTotalCoordsPerPoint()
  {
    return m_SumC;
  }
  




  public double getMeanCoordsPerPoint()
  {
    return m_SumC / m_NumQueries;
  }
  




  public double getStdDevCoordsPerPoint()
  {
    return Math.sqrt((m_SumSqC - m_SumC * m_SumC / m_NumQueries) / (m_NumQueries - 1));
  }
  




  public double getMinCoordsPerPoint()
  {
    return m_MinC;
  }
  




  public double getMaxCoordsPerPoint()
  {
    return m_MaxC;
  }
  






  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    
    newVector.addElement("measureTotal_points_visited");
    newVector.addElement("measureMean_points_visited");
    newVector.addElement("measureStdDev_points_visited");
    newVector.addElement("measureMin_points_visited");
    newVector.addElement("measureMax_points_visited");
    
    newVector.addElement("measureTotalCoordsPerPoint");
    newVector.addElement("measureMeanCoordsPerPoint");
    newVector.addElement("measureStdDevCoordsPerPoint");
    newVector.addElement("measureMinCoordsPerPoint");
    newVector.addElement("measureMaxCoordsPerPoint");
    
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
    throws IllegalArgumentException
  {
    if (additionalMeasureName.compareToIgnoreCase("measureTotal_points_visited") == 0)
      return getTotalPointsVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMean_points_visited") == 0)
      return getMeanPointsVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureStdDev_points_visited") == 0)
      return getStdDevPointsVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMin_points_visited") == 0)
      return getMinPointsVisited();
    if (additionalMeasureName.compareToIgnoreCase("measureMax_points_visited") == 0) {
      return getMaxPointsVisited();
    }
    
    if (additionalMeasureName.compareToIgnoreCase("measureTotalCoordsPerPoint") == 0)
      return getTotalCoordsPerPoint();
    if (additionalMeasureName.compareToIgnoreCase("measureMeanCoordsPerPoint") == 0)
      return getMeanCoordsPerPoint();
    if (additionalMeasureName.compareToIgnoreCase("measureStdDevCoordsPerPoint") == 0)
      return getStdDevCoordsPerPoint();
    if (additionalMeasureName.compareToIgnoreCase("measureMinCoordsPerPoint") == 0)
      return getMinCoordsPerPoint();
    if (additionalMeasureName.compareToIgnoreCase("measureMaxCoordsPerPoint") == 0) {
      return getMaxCoordsPerPoint();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported by PerformanceStats.");
  }
  






  public String getStats()
  {
    StringBuffer buf = new StringBuffer();
    
    buf.append("           min, max, total, mean, stddev\n");
    buf.append("Points:    " + getMinPointsVisited() + ", " + getMaxPointsVisited() + "," + getTotalPointsVisited() + "," + getMeanPointsVisited() + ", " + getStdDevPointsVisited() + "\n");
    

    return buf.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

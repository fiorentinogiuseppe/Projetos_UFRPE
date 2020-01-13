package weka.gui.visualize;

import java.awt.Color;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
































public class PlotData2D
{
  protected Instances m_plotInstances = null;
  protected String m_plotName;
  
  public PlotData2D(Instances insts) { Messages.getInstance();m_plotName = Messages.getString("PlotData2D_PlotName_Text");
    




    m_plotNameHTML = null;
    

    m_useCustomColour = false;
    m_customColour = null;
    

    m_displayAllPoints = false;
    




    m_alwaysDisplayPointsOfThisSize = -1;
    













































    m_plotInstances = insts;
    m_xIndex = (this.m_yIndex = this.m_cIndex = 0);
    m_pointLookup = new double[m_plotInstances.numInstances()][4];
    m_shapeSize = new int[m_plotInstances.numInstances()];
    m_shapeType = new int[m_plotInstances.numInstances()];
    m_connectPoints = new boolean[m_plotInstances.numInstances()];
    for (int i = 0; i < m_plotInstances.numInstances(); i++) {
      m_shapeSize[i] = 2;
      m_shapeType[i] = -1;
    }
    determineBounds();
  }
  

  protected String m_plotNameHTML;
  public void addInstanceNumberAttribute()
  {
    String originalRelationName = m_plotInstances.relationName();
    int originalClassIndex = m_plotInstances.classIndex();
    try {
      Add addF = new Add();
      Messages.getInstance();addF.setAttributeName(Messages.getString("PlotData2D_AddInstanceNumberAttribute_AddF_SetAttributeName_Text"));
      Messages.getInstance();addF.setAttributeIndex(Messages.getString("PlotData2D_AddInstanceNumberAttribute_AddF_SetAttributeIndex_Text"));
      addF.setInputFormat(m_plotInstances);
      m_plotInstances = Filter.useFilter(m_plotInstances, addF);
      m_plotInstances.setClassIndex(originalClassIndex + 1);
      for (int i = 0; i < m_plotInstances.numInstances(); i++) {
        m_plotInstances.instance(i).setValue(0, i);
      }
      m_plotInstances.setRelationName(originalRelationName);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  

  public boolean m_useCustomColour;
  public Color m_customColour;
  public Instances getPlotInstances()
  {
    return new Instances(m_plotInstances);
  }
  



  public void setPlotName(String name)
  {
    m_plotName = name;
  }
  



  public String getPlotName()
  {
    return m_plotName;
  }
  
  public boolean m_displayAllPoints;
  public int m_alwaysDisplayPointsOfThisSize;
  protected double[][] m_pointLookup;
  protected int[] m_shapeSize;
  protected int[] m_shapeType;
  public void setPlotNameHTML(String name)
  {
    m_plotNameHTML = name;
  }
  
  protected boolean[] m_connectPoints;
  private int m_xIndex;
  private int m_yIndex;
  private int m_cIndex;
  protected double m_maxX;
  
  public String getPlotNameHTML()
  {
    if (m_plotNameHTML == null) {
      return m_plotName;
    }
    
    return m_plotNameHTML;
  }
  



  public void setShapeType(int[] st)
    throws Exception
  {
    m_shapeType = st;
    if (m_shapeType.length != m_plotInstances.numInstances()) {
      Messages.getInstance();throw new Exception(Messages.getString("PlotData2D_SetShapeType_Exception_Text_First"));
    }
    for (int i = 0; i < st.length; i++) {
      if (m_shapeType[i] == 1000) {
        m_shapeSize[i] = 3;
      }
    }
  }
  



  public void setShapeType(FastVector st)
    throws Exception
  {
    if (st.size() != m_plotInstances.numInstances()) {
      Messages.getInstance();throw new Exception(Messages.getString("PlotData2D_SetShapeType_Exception_Text_Second"));
    }
    m_shapeType = new int[st.size()];
    for (int i = 0; i < st.size(); i++) {
      m_shapeType[i] = ((Integer)st.elementAt(i)).intValue();
      if (m_shapeType[i] == 1000) {
        m_shapeSize[i] = 3;
      }
    }
  }
  


  public void setShapeSize(int[] ss)
    throws Exception
  {
    m_shapeSize = ss;
    if (m_shapeType.length != m_plotInstances.numInstances()) {
      Messages.getInstance();throw new Exception(Messages.getString("PlotData2D_SetShapeType_Exception_Text_Third"));
    }
  }
  


  public void setShapeSize(FastVector ss)
    throws Exception
  {
    if (ss.size() != m_plotInstances.numInstances()) {
      Messages.getInstance();throw new Exception(Messages.getString("PlotData2D_SetShapeType_Exception_Text_Fourth"));
    }
    
    m_shapeSize = new int[ss.size()];
    for (int i = 0; i < ss.size(); i++) {
      m_shapeSize[i] = ((Integer)ss.elementAt(i)).intValue();
    }
  }
  



  public void setConnectPoints(boolean[] cp)
    throws Exception
  {
    m_connectPoints = cp;
    if (m_connectPoints.length != m_plotInstances.numInstances()) {
      Messages.getInstance();throw new Exception(Messages.getString("PlotData2D_SetConnectPoints_Exception_Text_First"));
    }
    
    m_connectPoints[0] = false;
  }
  



  public void setConnectPoints(FastVector cp)
    throws Exception
  {
    if (cp.size() != m_plotInstances.numInstances()) {
      Messages.getInstance();throw new Exception(Messages.getString("PlotData2D_SetConnectPoints_Exception_Text_Second"));
    }
    
    m_shapeSize = new int[cp.size()];
    for (int i = 0; i < cp.size(); i++) {
      m_connectPoints[i] = ((Boolean)cp.elementAt(i)).booleanValue();
    }
    m_connectPoints[0] = false;
  }
  

  protected double m_minX;
  protected double m_maxY;
  protected double m_minY;
  protected double m_maxC;
  protected double m_minC;
  public void setCustomColour(Color c)
  {
    m_customColour = c;
    if (c != null) {
      m_useCustomColour = true;
    } else {
      m_useCustomColour = false;
    }
  }
  



  public void setXindex(int x)
  {
    m_xIndex = x;
    determineBounds();
  }
  



  public void setYindex(int y)
  {
    m_yIndex = y;
    determineBounds();
  }
  



  public void setCindex(int c)
  {
    m_cIndex = c;
    determineBounds();
  }
  



  public int getXindex()
  {
    return m_xIndex;
  }
  



  public int getYindex()
  {
    return m_yIndex;
  }
  



  public int getCindex()
  {
    return m_cIndex;
  }
  




  private void determineBounds()
  {
    if ((m_plotInstances != null) && (m_plotInstances.numAttributes() > 0) && (m_plotInstances.numInstances() > 0))
    {


      double min = Double.POSITIVE_INFINITY;
      double max = Double.NEGATIVE_INFINITY;
      if (m_plotInstances.attribute(m_xIndex).isNominal()) {
        m_minX = 0.0D;
        m_maxX = (m_plotInstances.attribute(m_xIndex).numValues() - 1);
      } else {
        for (int i = 0; i < m_plotInstances.numInstances(); i++) {
          if (!m_plotInstances.instance(i).isMissing(m_xIndex)) {
            double value = m_plotInstances.instance(i).value(m_xIndex);
            if (value < min) {
              min = value;
            }
            if (value > max) {
              max = value;
            }
          }
        }
        

        if (min == Double.POSITIVE_INFINITY) { min = max = 0.0D;
        }
        m_minX = min;m_maxX = max;
        if (min == max) {
          m_maxX += 0.05D;
          m_minX -= 0.05D;
        }
      }
      

      min = Double.POSITIVE_INFINITY;
      max = Double.NEGATIVE_INFINITY;
      if (m_plotInstances.attribute(m_yIndex).isNominal()) {
        m_minY = 0.0D;
        m_maxY = (m_plotInstances.attribute(m_yIndex).numValues() - 1);
      } else {
        for (int i = 0; i < m_plotInstances.numInstances(); i++) {
          if (!m_plotInstances.instance(i).isMissing(m_yIndex)) {
            double value = m_plotInstances.instance(i).value(m_yIndex);
            if (value < min) {
              min = value;
            }
            if (value > max) {
              max = value;
            }
          }
        }
        

        if (min == Double.POSITIVE_INFINITY) { min = max = 0.0D;
        }
        m_minY = min;m_maxY = max;
        if (min == max) {
          m_maxY += 0.05D;
          m_minY -= 0.05D;
        }
      }
      

      min = Double.POSITIVE_INFINITY;
      max = Double.NEGATIVE_INFINITY;
      
      for (int i = 0; i < m_plotInstances.numInstances(); i++) {
        if (!m_plotInstances.instance(i).isMissing(m_cIndex)) {
          double value = m_plotInstances.instance(i).value(m_cIndex);
          if (value < min) {
            min = value;
          }
          if (value > max) {
            max = value;
          }
        }
      }
      

      if (min == Double.POSITIVE_INFINITY) { min = max = 0.0D;
      }
      m_minC = min;m_maxC = max;
    }
  }
}

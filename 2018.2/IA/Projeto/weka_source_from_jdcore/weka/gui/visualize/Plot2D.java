package weka.gui.visualize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Properties;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;





























public class Plot2D
  extends JPanel
{
  private static final long serialVersionUID = -1673162410856660442L;
  public static final int MAX_SHAPES = 5;
  public static final int ERROR_SHAPE = 1000;
  public static final int MISSING_SHAPE = 2000;
  public static final int CONST_AUTOMATIC_SHAPE = -1;
  public static final int X_SHAPE = 0;
  public static final int PLUS_SHAPE = 1;
  public static final int DIAMOND_SHAPE = 2;
  public static final int TRIANGLEUP_SHAPE = 3;
  public static final int TRIANGLEDOWN_SHAPE = 4;
  public static final int DEFAULT_SHAPE_SIZE = 2;
  protected Color m_axisColour = Color.green;
  

  protected Color m_backgroundColour = Color.black;
  

  protected FastVector m_plots = new FastVector();
  

  protected PlotData2D m_masterPlot = null;
  protected String m_masterName;
  
  public Plot2D() { Messages.getInstance();m_masterName = Messages.getString("Plot2D_MasterName_Text");
    

    m_plotInstances = null;
    




    m_plotCompanion = null;
    

    m_InstanceInfo = null;
    m_InstanceInfoText = new JTextArea();
    




    m_DefaultColors = new Color[] { Color.blue, Color.red, Color.green, Color.cyan, Color.pink, new Color(255, 0, 255), Color.orange, new Color(255, 0, 0), new Color(0, 255, 0), Color.white };
    











    m_xIndex = 0;
    m_yIndex = 0;
    m_cIndex = 0;
    m_sIndex = 0;
    










    m_axisPad = 5;
    

    m_tickSize = 5;
    

    m_XaxisStart = 0;
    m_YaxisStart = 0;
    m_XaxisEnd = 0;
    m_YaxisEnd = 0;
    



    m_plotResize = true;
    

    m_axisChanged = false;
    









    m_labelMetrics = null;
    

    m_JitterVal = 0;
    

    m_JRand = new Random(0L);
    

    m_pointLookup = ((double[][])null);
    



    setProperties();
    setBackground(m_backgroundColour);
    m_InstanceInfoText.setFont(new Font("Monospaced", 0, 12));
    m_InstanceInfoText.setEditable(false);
    
    m_drawnPoints = new int[getWidth()][getHeight()];
    

    m_colorList = new FastVector(10);
    for (int noa = m_colorList.size(); noa < 10; noa++) {
      Color pc = m_DefaultColors[(noa % 10)];
      int ija = noa / 10;
      ija *= 2;
      for (int j = 0; j < ija; j++) {
        pc = pc.darker();
      }
      
      m_colorList.addElement(pc);
    }
  }
  


  private void setProperties()
  {
    if (VisualizeUtils.VISUALIZE_PROPERTIES != null) {
      String thisClass = getClass().getName();
      String axisKey = thisClass + ".axisColour";
      String backgroundKey = thisClass + ".backgroundColour";
      
      String axisColour = VisualizeUtils.VISUALIZE_PROPERTIES.getProperty(axisKey);
      
      if (axisColour != null)
      {





        m_axisColour = VisualizeUtils.processColour(axisColour, m_axisColour);
      }
      
      String backgroundColour = VisualizeUtils.VISUALIZE_PROPERTIES.getProperty(backgroundKey);
      
      if (backgroundColour != null)
      {





        m_backgroundColour = VisualizeUtils.processColour(backgroundColour, m_backgroundColour);
      }
    }
  }
  






  private boolean checkPoints(double x1, double y1)
  {
    if ((x1 < 0.0D) || (x1 > getSizewidth) || (y1 < 0.0D) || (y1 > getSizeheight))
    {
      return false;
    }
    return true;
  }
  






  public void setPlotCompanion(Plot2DCompanion p)
  {
    m_plotCompanion = p;
  }
  



  public void setJitter(int j)
  {
    if ((m_plotInstances.numAttributes() > 0) && (m_plotInstances.numInstances() > 0))
    {
      if (j >= 0) {
        m_JitterVal = j;
        m_JRand = new Random(m_JitterVal);
        
        m_drawnPoints = new int[m_XaxisEnd - m_XaxisStart + 1][m_YaxisEnd - m_YaxisStart + 1];
        
        updatePturb();
        
        repaint();
      }
    }
  }
  




  public void setColours(FastVector cols)
  {
    m_colorList = cols;
  }
  



  public void setXindex(int x)
  {
    m_xIndex = x;
    for (int i = 0; i < m_plots.size(); i++) {
      ((PlotData2D)m_plots.elementAt(i)).setXindex(m_xIndex);
    }
    determineBounds();
    if (m_JitterVal != 0) {
      updatePturb();
    }
    m_axisChanged = true;
    repaint();
  }
  



  public void setYindex(int y)
  {
    m_yIndex = y;
    for (int i = 0; i < m_plots.size(); i++) {
      ((PlotData2D)m_plots.elementAt(i)).setYindex(m_yIndex);
    }
    determineBounds();
    if (m_JitterVal != 0) {
      updatePturb();
    }
    m_axisChanged = true;
    repaint();
  }
  



  public void setCindex(int c)
  {
    m_cIndex = c;
    for (int i = 0; i < m_plots.size(); i++) {
      ((PlotData2D)m_plots.elementAt(i)).setCindex(m_cIndex);
    }
    determineBounds();
    m_axisChanged = true;
    repaint();
  }
  



  public FastVector getPlots()
  {
    return m_plots;
  }
  



  public PlotData2D getMasterPlot()
  {
    return m_masterPlot;
  }
  



  public double getMaxX()
  {
    return m_maxX;
  }
  



  public double getMaxY()
  {
    return m_maxY;
  }
  



  public double getMinX()
  {
    return m_minX;
  }
  



  public double getMinY()
  {
    return m_minY;
  }
  



  public double getMaxC()
  {
    return m_maxC;
  }
  



  public double getMinC()
  {
    return m_minC;
  }
  




  public void setInstances(Instances inst)
    throws Exception
  {
    PlotData2D tempPlot = new PlotData2D(inst);
    Messages.getInstance();tempPlot.setPlotName(Messages.getString("Plot2D_SetInstances_TempPlot_SetPlotName_Text"));
    setMasterPlot(tempPlot);
  }
  



  public void setMasterPlot(PlotData2D master)
    throws Exception
  {
    if (m_plotInstances == null) {
      Messages.getInstance();throw new Exception(Messages.getString("Plot2D_SetMasterPlot_Exception_Text"));
    }
    removeAllPlots();
    m_masterPlot = master;
    m_plots.addElement(m_masterPlot);
    m_plotInstances = m_masterPlot.m_plotInstances;
    
    m_xIndex = 0;
    m_yIndex = 0;
    m_cIndex = 0;
    
    determineBounds();
  }
  


  public void removeAllPlots()
  {
    m_masterPlot = null;
    m_plotInstances = null;
    m_plots = new FastVector();
    m_xIndex = 0;m_yIndex = 0;m_cIndex = 0;
  }
  



  public void addPlot(PlotData2D newPlot)
    throws Exception
  {
    if (m_plotInstances == null) {
      Messages.getInstance();throw new Exception(Messages.getString("Plot2D_AddPlot_Exception_Text_First"));
    }
    
    if (m_masterPlot != null) {
      if (!m_masterPlot.m_plotInstances.equalHeaders(m_plotInstances))
      {
        Messages.getInstance();throw new Exception(Messages.getString("Plot2D_AddPlot_Exception_Text_Second"));
      }
    } else {
      m_masterPlot = newPlot;
      m_plotInstances = m_masterPlot.m_plotInstances;
    }
    m_plots.addElement(newPlot);
    setXindex(m_xIndex);
    setYindex(m_yIndex);
    setCindex(m_cIndex);
  }
  



  private void setFonts(Graphics gx)
  {
    if (m_labelMetrics == null) {
      m_labelFont = new Font("Monospaced", 0, 12);
      m_labelMetrics = gx.getFontMetrics(m_labelFont);
    }
    gx.setFont(m_labelFont);
  }
  


  protected Instances m_plotInstances;
  

  protected Plot2DCompanion m_plotCompanion;
  

  public void searchPoints(int x, int y, final boolean newFrame)
  {
    if (m_masterPlot.m_plotInstances != null) {
      int longest = 0;
      for (int j = 0; j < m_masterPlot.m_plotInstances.numAttributes(); j++) {
        if (m_masterPlot.m_plotInstances.attribute(j).name().length() > longest)
        {
          longest = m_masterPlot.m_plotInstances.attribute(j).name().length();
        }
      }
      
      StringBuffer insts = new StringBuffer();
      for (int jj = 0; jj < m_plots.size(); jj++) {
        PlotData2D temp_plot = (PlotData2D)m_plots.elementAt(jj);
        
        for (int i = 0; i < m_plotInstances.numInstances(); i++) {
          if (m_pointLookup[i][0] != Double.NEGATIVE_INFINITY) {
            double px = m_pointLookup[i][0] + m_pointLookup[i][2];
            
            double py = m_pointLookup[i][1] + m_pointLookup[i][3];
            

            double size = m_shapeSize[i];
            if ((x >= px - size) && (x <= px + size) && (y >= py - size) && (y <= py + size))
            {

              Messages.getInstance();Messages.getInstance();Messages.getInstance();insts.append(Messages.getString("Plot2D_SearchPoints_Text_First") + m_plotName + Messages.getString("Plot2D_SearchPoints_Text_Second") + (i + 1) + Messages.getString("Plot2D_SearchPoints_Text_Third"));
              
              for (int j = 0; j < m_plotInstances.numAttributes(); j++) {
                for (int k = 0; k < longest - m_plotInstances.attribute(j).name().length(); 
                    
                    k++) {
                  insts.append(" ");
                }
                insts.append(m_plotInstances.attribute(j).name());
                Messages.getInstance();insts.append(Messages.getString("Plot2D_SearchPoints_Text_Fourth"));
                
                if (m_plotInstances.instance(i).isMissing(j)) {
                  Messages.getInstance();insts.append(Messages.getString("Plot2D_SearchPoints_Text_Fifth"));
                } else if (m_plotInstances.attribute(j).isNominal())
                {
                  insts.append(m_plotInstances.attribute(j).value((int)m_plotInstances.instance(i).value(j)));

                }
                else
                {
                  insts.append(m_plotInstances.instance(i).value(j));
                }
                
                Messages.getInstance();insts.append(Messages.getString("Plot2D_SearchPoints_Text_Sixth"));
              }
            }
          }
        }
      }
      

      if (insts.length() > 0)
      {
        if ((newFrame) || (m_InstanceInfo == null)) {
          JTextArea jt = new JTextArea();
          jt.setFont(new Font("Monospaced", 0, 12));
          jt.setEditable(false);
          jt.setText(insts.toString());
          Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("Plot2D_SearchPoints_JFrame_Text"));
          final JFrame testf = m_InstanceInfo;
          jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              if ((!newFrame) || (testf == null)) {
                m_InstanceInfo = null;
              }
              jf.dispose();
            }
          });
          jf.getContentPane().setLayout(new BorderLayout());
          jf.getContentPane().add(new JScrollPane(jt), "Center");
          jf.pack();
          jf.setSize(320, 400);
          jf.setVisible(true);
          if (m_InstanceInfo == null) {
            m_InstanceInfo = jf;
            m_InstanceInfoText = jt;
          }
        }
        else {
          m_InstanceInfoText.setText(insts.toString());
        }
      }
    }
  }
  






  public void determineBounds()
  {
    m_minX = m_plots.elementAt(0)).m_minX;
    m_maxX = m_plots.elementAt(0)).m_maxX;
    m_minY = m_plots.elementAt(0)).m_minY;
    m_maxY = m_plots.elementAt(0)).m_maxY;
    m_minC = m_plots.elementAt(0)).m_minC;
    m_maxC = m_plots.elementAt(0)).m_maxC;
    for (int i = 1; i < m_plots.size(); i++) {
      double value = m_plots.elementAt(i)).m_minX;
      if (value < m_minX) {
        m_minX = value;
      }
      value = m_plots.elementAt(i)).m_maxX;
      if (value > m_maxX) {
        m_maxX = value;
      }
      value = m_plots.elementAt(i)).m_minY;
      if (value < m_minY) {
        m_minY = value;
      }
      value = m_plots.elementAt(i)).m_maxY;
      if (value > m_maxY) {
        m_maxY = value;
      }
      value = m_plots.elementAt(i)).m_minC;
      if (value < m_minC) {
        m_minC = value;
      }
      value = m_plots.elementAt(i)).m_maxC;
      if (value > m_maxC) {
        m_maxC = value;
      }
    }
    
    fillLookup();
    repaint();
  }
  








  public double convertToAttribX(double scx)
  {
    double temp = m_XaxisEnd - m_XaxisStart;
    double temp2 = (scx - m_XaxisStart) * (m_maxX - m_minX) / temp;
    
    temp2 += m_minX;
    
    return temp2;
  }
  




  public double convertToAttribY(double scy)
  {
    double temp = m_YaxisEnd - m_YaxisStart;
    double temp2 = (scy - m_YaxisEnd) * (m_maxY - m_minY) / temp;
    
    temp2 = -(temp2 - m_minY);
    
    return temp2;
  }
  







  int pturbX(double xvalP, double xj)
  {
    int xpturb = 0;
    if (m_JitterVal > 0) {
      xpturb = (int)(m_JitterVal * (xj / 2.0D));
      if ((xvalP + xpturb < m_XaxisStart) || (xvalP + xpturb > m_XaxisEnd))
      {
        xpturb *= -1;
      }
    }
    return xpturb;
  }
  




  public double convertToPanelX(double xval)
  {
    double temp = (xval - m_minX) / (m_maxX - m_minX);
    double temp2 = temp * (m_XaxisEnd - m_XaxisStart);
    
    temp2 += m_XaxisStart;
    
    return temp2;
  }
  






  int pturbY(double yvalP, double yj)
  {
    int ypturb = 0;
    if (m_JitterVal > 0) {
      ypturb = (int)(m_JitterVal * (yj / 2.0D));
      if ((yvalP + ypturb < m_YaxisStart) || (yvalP + ypturb > m_YaxisEnd))
      {
        ypturb *= -1;
      }
    }
    return ypturb;
  }
  




  public double convertToPanelY(double yval)
  {
    double temp = (yval - m_minY) / (m_maxY - m_minY);
    double temp2 = temp * (m_YaxisEnd - m_YaxisStart);
    
    temp2 = m_YaxisEnd - temp2;
    
    return temp2;
  }
  






  private static void drawX(Graphics gx, double x, double y, int size)
  {
    gx.drawLine((int)(x - size), (int)(y - size), (int)(x + size), (int)(y + size));
    

    gx.drawLine((int)(x + size), (int)(y - size), (int)(x - size), (int)(y + size));
  }
  







  private static void drawPlus(Graphics gx, double x, double y, int size)
  {
    gx.drawLine((int)(x - size), (int)y, (int)(x + size), (int)y);
    

    gx.drawLine((int)x, (int)(y - size), (int)x, (int)(y + size));
  }
  







  private static void drawDiamond(Graphics gx, double x, double y, int size)
  {
    gx.drawLine((int)(x - size), (int)y, (int)x, (int)(y - size));
    

    gx.drawLine((int)x, (int)(y - size), (int)(x + size), (int)y);
    

    gx.drawLine((int)(x + size), (int)y, (int)x, (int)(y + size));
    

    gx.drawLine((int)x, (int)(y + size), (int)(x - size), (int)y);
  }
  








  private static void drawTriangleUp(Graphics gx, double x, double y, int size)
  {
    gx.drawLine((int)x, (int)(y - size), (int)(x - size), (int)(y + size));
    

    gx.drawLine((int)(x - size), (int)(y + size), (int)(x + size), (int)(y + size));
    

    gx.drawLine((int)(x + size), (int)(y + size), (int)x, (int)(y - size));
  }
  

  protected JFrame m_InstanceInfo;
  
  protected JTextArea m_InstanceInfoText;
  
  protected FastVector m_colorList;
  
  protected Color[] m_DefaultColors;
  
  private static void drawTriangleDown(Graphics gx, double x, double y, int size)
  {
    gx.drawLine((int)x, (int)(y + size), (int)(x - size), (int)(y - size));
    

    gx.drawLine((int)(x - size), (int)(y - size), (int)(x + size), (int)(y - size));
    

    gx.drawLine((int)(x + size), (int)(y - size), (int)x, (int)(y + size));
  }
  


  protected int m_xIndex;
  

  protected int m_yIndex;
  

  protected int m_cIndex;
  

  protected int m_sIndex;
  
  protected double m_maxX;
  
  protected double m_minX;
  
  protected double m_maxY;
  
  protected double m_minY;
  
  protected static void drawDataPoint(double x, double y, double xprev, double yprev, int size, int shape, Graphics gx)
  {
    drawDataPoint(x, y, size, shape, gx);
    

    gx.drawLine((int)x, (int)y, (int)xprev, (int)yprev);
  }
  















  protected static void drawDataPoint(double x, double y, int size, int shape, Graphics gx)
  {
    Font lf = new Font("Monospaced", 0, 12);
    FontMetrics fm = gx.getFontMetrics(lf);
    
    if (size == 0) {
      size = 1;
    }
    
    if ((shape != 1000) && (shape != 2000)) {
      shape %= 5;
    }
    
    switch (shape) {
    case 0: 
      drawX(gx, x, y, size);
      break;
    case 1: 
      drawPlus(gx, x, y, size);
      break;
    case 2: 
      drawDiamond(gx, x, y, size);
      break;
    case 3: 
      drawTriangleUp(gx, x, y, size);
      break;
    case 4: 
      drawTriangleDown(gx, x, y, size);
      break;
    case 1000: 
      gx.drawRect((int)(x - size), (int)(y - size), size * 2, size * 2);
      break;
    case 2000: 
      int hf = fm.getAscent();
      int width = fm.stringWidth("M");
      gx.drawString("M", (int)(x - width / 2), (int)(y + hf / 2));
    }
    
  }
  



  private void updatePturb()
  {
    double xj = 0.0D;
    double yj = 0.0D;
    for (int j = 0; j < m_plots.size(); j++) {
      PlotData2D temp_plot = (PlotData2D)m_plots.elementAt(j);
      for (int i = 0; i < m_plotInstances.numInstances(); i++) {
        if ((!m_plotInstances.instance(i).isMissing(m_xIndex)) && (!m_plotInstances.instance(i).isMissing(m_yIndex)))
        {

          if (m_JitterVal > 0) {
            xj = m_JRand.nextGaussian();
            yj = m_JRand.nextGaussian();
          }
          m_pointLookup[i][2] = pturbX(m_pointLookup[i][0], xj);
          
          m_pointLookup[i][3] = pturbY(m_pointLookup[i][1], yj);
        }
      }
    }
  }
  





  private void fillLookup()
  {
    for (int j = 0; j < m_plots.size(); j++) {
      PlotData2D temp_plot = (PlotData2D)m_plots.elementAt(j);
      
      if ((m_plotInstances.numInstances() > 0) && (m_plotInstances.numAttributes() > 0))
      {
        for (int i = 0; i < m_plotInstances.numInstances(); i++) {
          if ((m_plotInstances.instance(i).isMissing(m_xIndex)) || (m_plotInstances.instance(i).isMissing(m_yIndex)))
          {
            m_pointLookup[i][0] = Double.NEGATIVE_INFINITY;
            m_pointLookup[i][1] = Double.NEGATIVE_INFINITY;
          } else {
            double x = convertToPanelX(m_plotInstances.instance(i).value(m_xIndex));
            
            double y = convertToPanelY(m_plotInstances.instance(i).value(m_yIndex));
            
            m_pointLookup[i][0] = x;
            m_pointLookup[i][1] = y;
          }
        }
      }
    }
  }
  




  private void paintData(Graphics gx)
  {
    for (int j = 0; j < m_plots.size(); j++) {
      PlotData2D temp_plot = (PlotData2D)m_plots.elementAt(j);
      
      for (int i = 0; i < m_plotInstances.numInstances(); i++) {
        if ((!m_plotInstances.instance(i).isMissing(m_xIndex)) && (!m_plotInstances.instance(i).isMissing(m_yIndex)))
        {

          double x = m_pointLookup[i][0] + m_pointLookup[i][2];
          
          double y = m_pointLookup[i][1] + m_pointLookup[i][3];
          

          double prevx = 0.0D;
          double prevy = 0.0D;
          if (i > 0) {
            prevx = m_pointLookup[(i - 1)][0] + m_pointLookup[(i - 1)][2];
            
            prevy = m_pointLookup[(i - 1)][1] + m_pointLookup[(i - 1)][3];
          }
          

          int x_range = (int)x - m_XaxisStart;
          int y_range = (int)y - m_YaxisStart;
          
          if ((x_range >= 0) && (y_range >= 0) && (
            (m_drawnPoints[x_range][y_range] == i) || (m_drawnPoints[x_range][y_range] == 0) || (m_shapeSize[i] == m_alwaysDisplayPointsOfThisSize) || (m_displayAllPoints == true)))
          {


            m_drawnPoints[x_range][y_range] = i;
            if (m_plotInstances.attribute(m_cIndex).isNominal()) {
              if ((m_plotInstances.attribute(m_cIndex).numValues() > m_colorList.size()) && (!m_useCustomColour))
              {

                extendColourMap(m_plotInstances.attribute(m_cIndex).numValues());
              }
              
              Color ci;
              Color ci;
              if (m_plotInstances.instance(i).isMissing(m_cIndex))
              {
                ci = Color.gray;
              } else {
                int ind = (int)m_plotInstances.instance(i).value(m_cIndex);
                
                ci = (Color)m_colorList.elementAt(ind);
              }
              
              if (!m_useCustomColour) {
                gx.setColor(ci);
              } else {
                gx.setColor(m_customColour);
              }
              
              if (m_plotInstances.instance(i).isMissing(m_cIndex))
              {
                if (m_connectPoints[i] == 1) {
                  drawDataPoint(x, y, prevx, prevy, m_shapeSize[i], 2000, gx);
                }
                else {
                  drawDataPoint(x, y, m_shapeSize[i], 2000, gx);
                }
                
              }
              else if (m_shapeType[i] == -1) {
                if (m_connectPoints[i] == 1) {
                  drawDataPoint(x, y, prevx, prevy, m_shapeSize[i], j, gx);
                }
                else {
                  drawDataPoint(x, y, m_shapeSize[i], j, gx);
                }
              }
              else if (m_connectPoints[i] == 1) {
                drawDataPoint(x, y, prevx, prevy, m_shapeSize[i], m_shapeType[i], gx);
              }
              else {
                drawDataPoint(x, y, m_shapeSize[i], m_shapeType[i], gx);
              }
              

            }
            else
            {
              Color ci = null;
              if (!m_plotInstances.instance(i).isMissing(m_cIndex))
              {
                double r = (m_plotInstances.instance(i).value(m_cIndex) - m_minC) / (m_maxC - m_minC);
                
                r = r * 240.0D + 15.0D;
                ci = new Color((int)r, 150, (int)(255.0D - r));
              } else {
                ci = Color.gray;
              }
              if (!m_useCustomColour) {
                gx.setColor(ci);
              } else {
                gx.setColor(m_customColour);
              }
              if (m_plotInstances.instance(i).isMissing(m_cIndex))
              {
                if (m_connectPoints[i] == 1) {
                  drawDataPoint(x, y, prevx, prevy, m_shapeSize[i], 2000, gx);
                }
                else {
                  drawDataPoint(x, y, m_shapeSize[i], 2000, gx);
                }
                
              }
              else if (m_shapeType[i] == -1) {
                if (m_connectPoints[i] == 1) {
                  drawDataPoint(x, y, prevx, prevy, m_shapeSize[i], j, gx);
                }
                else {
                  drawDataPoint(x, y, m_shapeSize[i], j, gx);
                }
              }
              else if (m_connectPoints[i] == 1) {
                drawDataPoint(x, y, prevx, prevy, m_shapeSize[i], m_shapeType[i], gx);
              }
              else {
                drawDataPoint(x, y, m_shapeSize[i], m_shapeType[i], gx);
              }
            }
          }
        }
      }
    }
  }
  



  protected double m_maxC;
  


  protected double m_minC;
  


  protected final int m_axisPad = 5;
  


  protected final int m_tickSize = 5;
  


  protected int m_XaxisStart;
  


  protected int m_YaxisStart;
  


  protected int m_XaxisEnd;
  


  protected int m_YaxisEnd;
  


  protected boolean m_plotResize;
  


  protected boolean m_axisChanged;
  


  protected int[][] m_drawnPoints;
  


  protected Font m_labelFont;
  


  protected FontMetrics m_labelMetrics;
  

  protected int m_JitterVal;
  

  protected Random m_JRand;
  

  protected double[][] m_pointLookup;
  

  private void paintAxis(Graphics gx)
  {
    setFonts(gx);
    int mxs = m_XaxisStart;
    int mxe = m_XaxisEnd;
    int mys = m_YaxisStart;
    int mye = m_YaxisEnd;
    m_plotResize = false;
    
    int h = getHeight();
    int w = getWidth();
    int hf = m_labelMetrics.getAscent();
    int mswx = 0;
    int mswy = 0;
    

    int precisionXmax = 1;
    int precisionXmin = 1;
    int precisionXmid = 1;
    



    int whole = (int)Math.abs(m_maxX);
    double decimal = Math.abs(m_maxX) - whole;
    
    int nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    


    precisionXmax = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_maxX)) / Math.log(10.0D)) + 2 : 1;
    


    if (precisionXmax > VisualizeUtils.MAX_PRECISION) {
      precisionXmax = 1;
    }
    
    String maxStringX = Utils.doubleToString(m_maxX, nondecimal + 1 + precisionXmax, precisionXmax);
    


    whole = (int)Math.abs(m_minX);
    decimal = Math.abs(m_minX) - whole;
    nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    

    precisionXmin = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_minX)) / Math.log(10.0D)) + 2 : 1;
    


    if (precisionXmin > VisualizeUtils.MAX_PRECISION) {
      precisionXmin = 1;
    }
    
    String minStringX = Utils.doubleToString(m_minX, nondecimal + 1 + precisionXmin, precisionXmin);
    


    mswx = m_labelMetrics.stringWidth(maxStringX);
    
    int precisionYmax = 1;
    int precisionYmin = 1;
    int precisionYmid = 1;
    whole = (int)Math.abs(m_maxY);
    decimal = Math.abs(m_maxY) - whole;
    nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    

    precisionYmax = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_maxY)) / Math.log(10.0D)) + 2 : 1;
    


    if (precisionYmax > VisualizeUtils.MAX_PRECISION) {
      precisionYmax = 1;
    }
    
    String maxStringY = Utils.doubleToString(m_maxY, nondecimal + 1 + precisionYmax, precisionYmax);
    



    whole = (int)Math.abs(m_minY);
    decimal = Math.abs(m_minY) - whole;
    nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    

    precisionYmin = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_minY)) / Math.log(10.0D)) + 2 : 1;
    


    if (precisionYmin > VisualizeUtils.MAX_PRECISION) {
      precisionYmin = 1;
    }
    
    String minStringY = Utils.doubleToString(m_minY, nondecimal + 1 + precisionYmin, precisionYmin);
    


    if (m_plotInstances.attribute(m_yIndex).isNumeric()) {
      mswy = m_labelMetrics.stringWidth(maxStringY) > m_labelMetrics.stringWidth(minStringY) ? m_labelMetrics.stringWidth(maxStringY) : m_labelMetrics.stringWidth(minStringY);
      


      mswy += m_labelMetrics.stringWidth("M");
    } else {
      mswy = m_labelMetrics.stringWidth("MM");
    }
    
    m_YaxisStart = 5;
    m_XaxisStart = (10 + mswy);
    
    m_XaxisEnd = (w - 5 - mswx / 2);
    
    m_YaxisEnd = (h - 5 - 2 * hf - 5);
    

    gx.setColor(m_axisColour);
    if (m_plotInstances.attribute(m_xIndex).isNumeric()) {
      if (w > 2 * mswx)
      {
        gx.drawString(maxStringX, m_XaxisEnd - mswx / 2, m_YaxisEnd + hf + 5);
        


        mswx = m_labelMetrics.stringWidth(minStringX);
        gx.drawString(minStringX, m_XaxisStart - mswx / 2, m_YaxisEnd + hf + 5);
        



        if ((w > 3 * mswx) && (m_plotInstances.attribute(m_xIndex).isNumeric()))
        {
          double mid = m_minX + (m_maxX - m_minX) / 2.0D;
          whole = (int)Math.abs(mid);
          decimal = Math.abs(mid) - whole;
          nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
          

          precisionXmid = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(mid)) / Math.log(10.0D)) + 2 : 1;
          


          if (precisionXmid > VisualizeUtils.MAX_PRECISION) {
            precisionXmid = 1;
          }
          
          String maxString = Utils.doubleToString(mid, nondecimal + 1 + precisionXmid, precisionXmid);
          

          int sw = m_labelMetrics.stringWidth(maxString);
          double mx = m_XaxisStart + (m_XaxisEnd - m_XaxisStart) / 2.0D;
          gx.drawString(maxString, (int)(mx - sw / 2.0D), m_YaxisEnd + hf + 5);
          

          gx.drawLine((int)mx, m_YaxisEnd, (int)mx, m_YaxisEnd + 5);
        }
      }
    } else {
      int numValues = m_plotInstances.attribute(m_xIndex).numValues();
      int div = numValues % 2 > 0 ? numValues / 2 + 1 : numValues / 2;
      int maxXStringWidth = (m_XaxisEnd - m_XaxisStart) / numValues;
      
      for (int i = 0; i < numValues; i++) {
        String val = m_plotInstances.attribute(m_xIndex).value(i);
        int sw = m_labelMetrics.stringWidth(val);
        

        if (sw > maxXStringWidth) {
          int incr = sw / val.length();
          int rm = (sw - maxXStringWidth) / incr;
          if (rm == 0) {
            rm = 1;
          }
          val = val.substring(0, val.length() - rm);
          sw = m_labelMetrics.stringWidth(val);
        }
        if (i == 0) {
          gx.drawString(val, (int)convertToPanelX(i), m_YaxisEnd + hf + 5);
        }
        else if (i == numValues - 1) {
          if (i % 2 == 0) {
            gx.drawString(val, m_XaxisEnd - sw, m_YaxisEnd + hf + 5);
          }
          else
          {
            gx.drawString(val, m_XaxisEnd - sw, m_YaxisEnd + 2 * hf + 5);
          }
          

        }
        else if (i % 2 == 0) {
          gx.drawString(val, (int)convertToPanelX(i) - sw / 2, m_YaxisEnd + hf + 5);
        }
        else
        {
          gx.drawString(val, (int)convertToPanelX(i) - sw / 2, m_YaxisEnd + 2 * hf + 5);
        }
        


        gx.drawLine((int)convertToPanelX(i), m_YaxisEnd, (int)convertToPanelX(i), m_YaxisEnd + 5);
      }
    }
    





    if (m_plotInstances.attribute(m_yIndex).isNumeric()) {
      if (h > 2 * hf) {
        gx.drawString(maxStringY, m_XaxisStart - mswy - 5, m_YaxisStart + hf);
        


        gx.drawString(minStringY, m_XaxisStart - mswy - 5, m_YaxisEnd);
        



        if ((w > 3 * hf) && (m_plotInstances.attribute(m_yIndex).isNumeric()))
        {
          double mid = m_minY + (m_maxY - m_minY) / 2.0D;
          whole = (int)Math.abs(mid);
          decimal = Math.abs(mid) - whole;
          nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
          

          precisionYmid = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(mid)) / Math.log(10.0D)) + 2 : 1;
          


          if (precisionYmid > VisualizeUtils.MAX_PRECISION) {
            precisionYmid = 1;
          }
          
          String maxString = Utils.doubleToString(mid, nondecimal + 1 + precisionYmid, precisionYmid);
          

          int sw = m_labelMetrics.stringWidth(maxString);
          double mx = m_YaxisStart + (m_YaxisEnd - m_YaxisStart) / 2.0D;
          gx.drawString(maxString, m_XaxisStart - sw - 5 - 1, (int)(mx + hf / 2.0D));
          

          gx.drawLine(m_XaxisStart - 5, (int)mx, m_XaxisStart, (int)mx);
        }
      }
    } else {
      int numValues = m_plotInstances.attribute(m_yIndex).numValues();
      int div = numValues % 2 == 0 ? numValues / 2 : numValues / 2 + 1;
      int maxYStringHeight = (m_YaxisEnd - m_XaxisStart) / div;
      int sw = m_labelMetrics.stringWidth("M");
      for (int i = 0; i < numValues; i++)
      {
        if (maxYStringHeight >= 2 * hf) {
          String val = m_plotInstances.attribute(m_yIndex).value(i);
          int numPrint = maxYStringHeight / hf > val.length() ? val.length() : maxYStringHeight / hf;
          


          for (int j = 0; j < numPrint; j++) {
            String ll = val.substring(j, j + 1);
            if ((val.charAt(j) == '_') || (val.charAt(j) == '-')) {
              ll = "|";
            }
            if (i == 0) {
              gx.drawString(ll, m_XaxisStart - sw - 5 - 1, (int)convertToPanelY(i) - (numPrint - 1) * hf + j * hf + hf / 2);



            }
            else if (i == numValues - 1) {
              if (i % 2 == 0) {
                gx.drawString(ll, m_XaxisStart - sw - 5 - 1, (int)convertToPanelY(i) + j * hf + hf / 2);

              }
              else
              {
                gx.drawString(ll, m_XaxisStart - 2 * sw - 5 - 1, (int)convertToPanelY(i) + j * hf + hf / 2);

              }
              

            }
            else if (i % 2 == 0) {
              gx.drawString(ll, m_XaxisStart - sw - 5 - 1, (int)convertToPanelY(i) - (numPrint - 1) * hf / 2 + j * hf + hf / 2);

            }
            else
            {

              gx.drawString(ll, m_XaxisStart - 2 * sw - 5 - 1, (int)convertToPanelY(i) - (numPrint - 1) * hf / 2 + j * hf + hf / 2);
            }
          }
        }
        




        gx.drawLine(m_XaxisStart - 5, (int)convertToPanelY(i), m_XaxisStart, (int)convertToPanelY(i));
      }
    }
    



    gx.drawLine(m_XaxisStart, m_YaxisStart, m_XaxisStart, m_YaxisEnd);
    


    gx.drawLine(m_XaxisStart, m_YaxisEnd, m_XaxisEnd, m_YaxisEnd);
    



    if ((m_XaxisStart != mxs) || (m_XaxisEnd != mxe) || (m_YaxisStart != mys) || (m_YaxisEnd != mye))
    {
      m_plotResize = true;
    }
  }
  



  private void extendColourMap(int highest)
  {
    for (int i = m_colorList.size(); i < highest; i++) {
      Color pc = m_DefaultColors[(i % 10)];
      int ija = i / 10;
      ija *= 2;
      for (int j = 0; j < ija; j++) {
        pc = pc.brighter();
      }
      
      m_colorList.addElement(pc);
    }
  }
  



  public void paintComponent(Graphics gx)
  {
    super.paintComponent(gx);
    if ((m_plotInstances != null) && (m_plotInstances.numInstances() > 0) && (m_plotInstances.numAttributes() > 0))
    {

      if (m_plotCompanion != null) {
        m_plotCompanion.prePlot(gx);
      }
      
      m_JRand = new Random(m_JitterVal);
      paintAxis(gx);
      if ((m_axisChanged) || (m_plotResize)) {
        int x_range = m_XaxisEnd - m_XaxisStart;
        int y_range = m_YaxisEnd - m_YaxisStart;
        if (x_range < 10) {
          x_range = 10;
        }
        if (y_range < 10) {
          y_range = 10;
        }
        
        m_drawnPoints = new int[x_range + 1][y_range + 1];
        fillLookup();
        m_plotResize = false;
        m_axisChanged = false;
      }
      paintData(gx);
    }
  }
  
  protected static Color checkAgainstBackground(Color c, Color background) {
    if (background == null) {
      return c;
    }
    
    if (c.equals(background)) {
      int red = c.getRed();
      int blue = c.getBlue();
      int green = c.getGreen();
      red += (red < 128 ? (255 - red) / 2 : -(red / 2));
      blue += (blue < 128 ? (blue - red) / 2 : -(blue / 2));
      green += (green < 128 ? (255 - green) / 2 : -(green / 2));
      c = new Color(red, green, blue);
    }
    return c;
  }
  


  public static void main(String[] args)
  {
    try
    {
      if (args.length < 1) {
        Messages.getInstance();System.err.println(Messages.getString("Plot2D_Main_Error_Text_First"));
        System.exit(1);
      }
      
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("Plot2D_Main_JFrame_Text"));
      
      jf.setSize(500, 400);
      jf.getContentPane().setLayout(new BorderLayout());
      Plot2D p2 = new Plot2D();
      jf.getContentPane().add(p2, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
        
      });
      p2.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if ((e.getModifiers() & 0x10) == 16)
          {
            val$p2.searchPoints(e.getX(), e.getY(), false);
          } else {
            val$p2.searchPoints(e.getX(), e.getY(), true);
          }
          
        }
      });
      jf.setVisible(true);
      if (args.length >= 1) {
        for (int j = 0; j < args.length; j++) {
          Messages.getInstance();System.err.println(Messages.getString("Plot2D_Main_Error_Text_Second") + args[j]);
          Reader r = new BufferedReader(new FileReader(args[j]));
          
          Instances i = new Instances(r);
          i.setClassIndex(i.numAttributes() - 1);
          PlotData2D pd1 = new PlotData2D(i);
          
          if (j == 0) {
            Messages.getInstance();pd1.setPlotName(Messages.getString("Plot2D_Main_Pd1_SetPlotName_Text_First"));
            p2.setMasterPlot(pd1);
            p2.setXindex(2);
            p2.setYindex(3);
            p2.setCindex(i.classIndex());
          } else {
            Messages.getInstance();pd1.setPlotName(Messages.getString("Plot2D_Main_Pd1_SetPlotName_Text_Second") + (j + 1));
            m_useCustomColour = true;
            m_customColour = (j % 2 == 0 ? Color.red : Color.blue);
            p2.addPlot(pd1);
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

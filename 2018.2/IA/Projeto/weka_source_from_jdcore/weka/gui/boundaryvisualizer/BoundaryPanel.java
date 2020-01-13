package weka.gui.boundaryvisualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;






























public class BoundaryPanel
  extends JPanel
{
  private static final long serialVersionUID = -8499445518744770458L;
  public static final Color[] DEFAULT_COLORS = { Color.red, Color.green, Color.blue, new Color(0, 255, 255), new Color(255, 0, 255), new Color(255, 255, 0), new Color(255, 255, 255), new Color(0, 0, 0) };
  





  public static final double REMOVE_POINT_RADIUS = 7.0D;
  




  protected FastVector m_Colors = new FastVector();
  

  protected Instances m_trainingData;
  

  protected Classifier m_classifier;
  

  protected DataGenerator m_dataGenerator;
  

  private int m_classIndex = -1;
  
  protected int m_xAttribute;
  
  protected int m_yAttribute;
  
  protected double m_minX;
  
  protected double m_minY;
  
  protected double m_maxX;
  
  protected double m_maxY;
  
  private double m_rangeX;
  
  private double m_rangeY;
  protected double m_pixHeight;
  protected double m_pixWidth;
  protected Image m_osi = null;
  

  protected int m_panelWidth;
  
  protected int m_panelHeight;
  
  protected int m_numOfSamplesPerRegion = 2;
  
  protected int m_numOfSamplesPerGenerator;
  
  protected double m_samplesBase = 2.0D;
  

  private Vector m_listeners = new Vector();
  


  private class PlotPanel
    extends JPanel
  {
    private static final long serialVersionUID = 743629498352235060L;
    

    public PlotPanel()
    {
      setToolTipText("");
    }
    
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (m_osi != null) {
        g.drawImage(m_osi, 0, 0, this);
      }
    }
    
    public String getToolTipText(MouseEvent event) {
      if (m_probabilityCache == null) {
        return null;
      }
      
      if (m_probabilityCache[event.getY()][event.getX()] == null) {
        return null;
      }
      
      Messages.getInstance();Messages.getInstance();Messages.getInstance();String pVec = Messages.getString("BoundaryPanel_GetToolTipText_Text_First") + Utils.doubleToString(BoundaryPanel.this.convertFromPanelX(event.getX()), 2) + Messages.getString("BoundaryPanel_GetToolTipText_Text_Second") + Utils.doubleToString(BoundaryPanel.this.convertFromPanelY(event.getY()), 2) + Messages.getString("BoundaryPanel_GetToolTipText_Text_Third");
      



      for (int i = 0; i < m_trainingData.classAttribute().numValues(); i++) {
        pVec = pVec + Utils.doubleToString(m_probabilityCache[event.getY()][event.getX()][i], 3) + " ";
      }
      return pVec;
    }
  }
  

  private PlotPanel m_plotPanel = new PlotPanel();
  

  private Thread m_plotThread = null;
  

  protected boolean m_stopPlotting = false;
  

  protected boolean m_stopReplotting = false;
  

  private Double m_dummy = new Double(1.0D);
  private boolean m_pausePlotting = false;
  
  private int m_size = 1;
  

  private boolean m_initialTiling;
  
  private Random m_random = null;
  

  protected double[][][] m_probabilityCache;
  

  protected boolean m_plotTrainingData = true;
  





  public BoundaryPanel(int panelWidth, int panelHeight)
  {
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    m_panelWidth = panelWidth;
    m_panelHeight = panelHeight;
    setLayout(new BorderLayout());
    m_plotPanel.setMinimumSize(new Dimension(m_panelWidth, m_panelHeight));
    m_plotPanel.setPreferredSize(new Dimension(m_panelWidth, m_panelHeight));
    m_plotPanel.setMaximumSize(new Dimension(m_panelWidth, m_panelHeight));
    add(m_plotPanel, "Center");
    setPreferredSize(m_plotPanel.getPreferredSize());
    setMaximumSize(m_plotPanel.getMaximumSize());
    setMinimumSize(m_plotPanel.getMinimumSize());
    
    m_random = new Random(1L);
    for (int i = 0; i < DEFAULT_COLORS.length; i++) {
      m_Colors.addElement(new Color(DEFAULT_COLORS[i].getRed(), DEFAULT_COLORS[i].getGreen(), DEFAULT_COLORS[i].getBlue()));
    }
    

    m_probabilityCache = new double[m_panelHeight][m_panelWidth][];
  }
  






  public void setNumSamplesPerRegion(int num)
  {
    m_numOfSamplesPerRegion = num;
  }
  




  public int getNumSamplesPerRegion()
  {
    return m_numOfSamplesPerRegion;
  }
  





  public void setGeneratorSamplesBase(double ksb)
  {
    m_samplesBase = ksb;
  }
  





  public double getGeneratorSamplesBase()
  {
    return m_samplesBase;
  }
  


  protected void initialize()
  {
    int iwidth = m_plotPanel.getWidth();
    int iheight = m_plotPanel.getHeight();
    
    m_osi = m_plotPanel.createImage(iwidth, iheight);
    Graphics m = m_osi.getGraphics();
    m.fillRect(0, 0, iwidth, iheight);
  }
  


  public void stopPlotting()
  {
    m_stopPlotting = true;
    try {
      m_plotThread.join(100L);
    }
    catch (Exception e) {}
  }
  

  public void computeMinMaxAtts()
  {
    m_minX = Double.MAX_VALUE;
    m_minY = Double.MAX_VALUE;
    m_maxX = Double.MIN_VALUE;
    m_maxY = Double.MIN_VALUE;
    
    boolean allPointsLessThanOne = true;
    
    if (m_trainingData.numInstances() == 0) {
      m_minX = (this.m_minY = 0.0D);
      m_maxX = (this.m_maxY = 1.0D);
    }
    else
    {
      for (int i = 0; i < m_trainingData.numInstances(); i++) {
        Instance inst = m_trainingData.instance(i);
        double x = inst.value(m_xAttribute);
        double y = inst.value(m_yAttribute);
        if ((!Instance.isMissingValue(x)) && (!Instance.isMissingValue(y))) {
          if (x < m_minX) {
            m_minX = x;
          }
          if (x > m_maxX) {
            m_maxX = x;
          }
          
          if (y < m_minY) {
            m_minY = y;
          }
          if (y > m_maxY) {
            m_maxY = y;
          }
          if ((x > 1.0D) || (y > 1.0D)) {
            allPointsLessThanOne = false;
          }
        }
      }
    }
    if (m_minX == m_maxX)
      m_minX = 0.0D;
    if (m_minY == m_maxY)
      m_minY = 0.0D;
    if (m_minX == Double.MAX_VALUE)
      m_minX = 0.0D;
    if (m_minY == Double.MAX_VALUE)
      m_minY = 0.0D;
    if (m_maxX == Double.MIN_VALUE)
      m_maxX = 1.0D;
    if (m_maxY == Double.MIN_VALUE)
      m_maxY = 1.0D;
    if (allPointsLessThanOne)
    {
      m_maxX = (this.m_maxY = 1.0D);
    }
    


    m_rangeX = (m_maxX - m_minX);
    m_rangeY = (m_maxY - m_minY);
    
    m_pixWidth = (m_rangeX / m_panelWidth);
    m_pixHeight = (m_rangeY / m_panelHeight);
  }
  







  private double getRandomX(int pix)
  {
    double minPix = m_minX + pix * m_pixWidth;
    
    return minPix + m_random.nextDouble() * m_pixWidth;
  }
  







  private double getRandomY(int pix)
  {
    double minPix = m_minY + pix * m_pixHeight;
    
    return minPix + m_random.nextDouble() * m_pixHeight;
  }
  



  public void start()
    throws Exception
  {
    m_numOfSamplesPerGenerator = ((int)Math.pow(m_samplesBase, m_trainingData.numAttributes() - 3));
    

    m_stopReplotting = true;
    if (m_trainingData == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_Start_Error_NoTrainingDataSet_Text"));
    }
    if (m_classifier == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_Start_Error_NoClassifierSet_Text"));
    }
    if (m_dataGenerator == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_Start_Error_NoDataGeneratorSet_Text"));
    }
    if ((m_trainingData.attribute(m_xAttribute).isNominal()) || (m_trainingData.attribute(m_yAttribute).isNominal()))
    {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_Start_Error_VisualizationDimensionsMustBeNumeric_Text"));
    }
    
    computeMinMaxAtts();
    
    startPlotThread();
  }
  

  protected class PlotThread
    extends Thread
  {
    double[] m_weightingAttsValues;
    boolean[] m_attsToWeightOn;
    double[] m_vals;
    double[] m_dist;
    Instance m_predInst;
    
    protected PlotThread() {}
    
    public void run()
    {
      m_stopPlotting = false;
      try {
        initialize();
        repaint();
        

        m_probabilityCache = new double[m_panelHeight][m_panelWidth][];
        m_classifier.buildClassifier(m_trainingData);
        

        m_attsToWeightOn = new boolean[m_trainingData.numAttributes()];
        m_attsToWeightOn[m_xAttribute] = true;
        m_attsToWeightOn[m_yAttribute] = true;
        
        m_dataGenerator.setWeightingDimensions(m_attsToWeightOn);
        
        m_dataGenerator.buildGenerator(m_trainingData);
        

        m_weightingAttsValues = new double[m_attsToWeightOn.length];
        m_vals = new double[m_trainingData.numAttributes()];
        m_predInst = new Instance(1.0D, m_vals);
        m_predInst.setDataset(m_trainingData);
        

        m_size = 16;
        
        m_initialTiling = true;
        

        for (int i = 0; i <= m_panelHeight; i += m_size) {
          for (int j = 0; j <= m_panelWidth; j += m_size) {
            if (m_stopPlotting) {
              break label376;
            }
            if (m_pausePlotting) {
              synchronized (m_dummy) {
                try {
                  m_dummy.wait();
                } catch (InterruptedException ex) {
                  m_pausePlotting = false;
                }
              }
            }
            BoundaryPanel.this.plotPoint(j, i, m_size, m_size, calculateRegionProbs(j, i), j == 0);
          }
        }
        label376:
        if (!m_stopPlotting) {
          m_initialTiling = false;
        }
        

        int size2 = m_size / 2;
        
        while (m_size > 1) {
          for (int i = 0; i <= m_panelHeight; i += m_size) {
            for (int j = 0; j <= m_panelWidth; j += m_size) {
              if (m_stopPlotting) {
                break label646;
              }
              if (m_pausePlotting) {
                synchronized (m_dummy) {
                  try {
                    m_dummy.wait();
                  } catch (InterruptedException ex) {
                    m_pausePlotting = false;
                  }
                }
              }
              boolean update = (j == 0) && (i % 2 == 0);
              
              BoundaryPanel.this.plotPoint(j, i + size2, size2, size2, calculateRegionProbs(j, i + size2), update);
              
              BoundaryPanel.this.plotPoint(j + size2, i + size2, size2, size2, calculateRegionProbs(j + size2, i + size2), update);
              
              BoundaryPanel.this.plotPoint(j + size2, i, size2, size2, calculateRegionProbs(j + size2, i), update);
            }
          }
          

          m_size = size2;
          size2 /= 2; }
        label646:
        BoundaryPanel.this.update();
        















        if (m_plotTrainingData) {
          plotTrainingData();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("BoundaryPanel_PlotThread_JOptionPaneShowMessageDialog_Text_Front") + ex.getMessage() + Messages.getString("BoundaryPanel_PlotThread_JOptionPaneShowMessageDialog_Text_End"));
      } finally {
        m_plotThread = null;
        

        ActionEvent e = new ActionEvent(this, 0, "");
        Vector l; synchronized (this) {
          l = (Vector)m_listeners.clone();
        }
        for (int i = 0; i < l.size(); i++) {
          ActionListener al = (ActionListener)l.elementAt(i);
          al.actionPerformed(e);
        }
      }
    }
    
    private double[] calculateRegionProbs(int j, int i) throws Exception {
      double[] sumOfProbsForRegion = new double[m_trainingData.classAttribute().numValues()];
      

      for (int u = 0; u < m_numOfSamplesPerRegion; u++)
      {
        double[] sumOfProbsForLocation = new double[m_trainingData.classAttribute().numValues()];
        

        m_weightingAttsValues[m_xAttribute] = BoundaryPanel.this.getRandomX(j);
        m_weightingAttsValues[m_yAttribute] = BoundaryPanel.this.getRandomY(m_panelHeight - i - 1);
        
        m_dataGenerator.setWeightingValues(m_weightingAttsValues);
        
        double[] weights = m_dataGenerator.getWeights();
        double sumOfWeights = Utils.sum(weights);
        int[] indices = Utils.sort(weights);
        

        int[] newIndices = new int[indices.length];
        double sumSoFar = 0.0D;
        double criticalMass = 0.99D * sumOfWeights;
        int index = weights.length - 1;int counter = 0;
        for (int z = weights.length - 1; z >= 0; z--) {
          newIndices[(index--)] = indices[z];
          sumSoFar += weights[indices[z]];
          counter++;
          if (sumSoFar > criticalMass) {
            break;
          }
        }
        indices = new int[counter];
        System.arraycopy(newIndices, index + 1, indices, 0, counter);
        
        for (int z = 0; z < m_numOfSamplesPerGenerator; z++)
        {
          m_dataGenerator.setWeightingValues(m_weightingAttsValues);
          double[][] values = m_dataGenerator.generateInstances(indices);
          
          for (int q = 0; q < values.length; q++) {
            if (values[q] != null) {
              System.arraycopy(values[q], 0, m_vals, 0, m_vals.length);
              m_vals[m_xAttribute] = m_weightingAttsValues[m_xAttribute];
              m_vals[m_yAttribute] = m_weightingAttsValues[m_yAttribute];
              

              m_dist = m_classifier.distributionForInstance(m_predInst);
              for (int k = 0; k < sumOfProbsForLocation.length; k++) {
                sumOfProbsForLocation[k] += m_dist[k] * weights[q];
              }
            }
          }
        }
        
        for (int k = 0; k < sumOfProbsForRegion.length; k++) {
          sumOfProbsForRegion[k] += sumOfProbsForLocation[k] * sumOfWeights;
        }
      }
      


      Utils.normalize(sumOfProbsForRegion);
      

      if ((i < m_panelHeight) && (j < m_panelWidth)) {
        m_probabilityCache[i][j] = new double[sumOfProbsForRegion.length];
        System.arraycopy(sumOfProbsForRegion, 0, m_probabilityCache[i][j], 0, sumOfProbsForRegion.length);
      }
      

      return sumOfProbsForRegion;
    }
  }
  


  public void plotTrainingData()
  {
    Graphics2D osg = (Graphics2D)m_osi.getGraphics();
    Graphics g = m_plotPanel.getGraphics();
    osg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    double xval = 0.0D;double yval = 0.0D;
    
    for (int i = 0; i < m_trainingData.numInstances(); i++) {
      if ((!m_trainingData.instance(i).isMissing(m_xAttribute)) && (!m_trainingData.instance(i).isMissing(m_yAttribute)))
      {

        if (!m_trainingData.instance(i).isMissing(m_classIndex))
        {

          xval = m_trainingData.instance(i).value(m_xAttribute);
          yval = m_trainingData.instance(i).value(m_yAttribute);
          
          int panelX = convertToPanelX(xval);
          int panelY = convertToPanelY(yval);
          Color ColorToPlotWith = (Color)m_Colors.elementAt((int)m_trainingData.instance(i).value(m_classIndex) % m_Colors.size());
          


          if (ColorToPlotWith.equals(Color.white)) {
            osg.setColor(Color.black);
          } else {
            osg.setColor(Color.white);
          }
          osg.fillOval(panelX - 3, panelY - 3, 7, 7);
          osg.setColor(ColorToPlotWith);
          osg.fillOval(panelX - 2, panelY - 2, 5, 5);
        } }
    }
    g.drawImage(m_osi, 0, 0, m_plotPanel);
  }
  

  private int convertToPanelX(double xval)
  {
    double temp = (xval - m_minX) / m_rangeX;
    temp *= m_panelWidth;
    
    return (int)temp;
  }
  

  private int convertToPanelY(double yval)
  {
    double temp = (yval - m_minY) / m_rangeY;
    temp *= m_panelHeight;
    temp = m_panelHeight - temp;
    
    return (int)temp;
  }
  

  private double convertFromPanelX(double pX)
  {
    pX /= m_panelWidth;
    pX *= m_rangeX;
    return pX + m_minX;
  }
  

  private double convertFromPanelY(double pY)
  {
    pY = m_panelHeight - pY;
    pY /= m_panelHeight;
    pY *= m_rangeY;
    
    return pY + m_minY;
  }
  


  protected void plotPoint(int x, int y, double[] probs, boolean update)
  {
    plotPoint(x, y, 1, 1, probs, update);
  }
  




  private void plotPoint(int x, int y, int width, int height, double[] probs, boolean update)
  {
    Graphics osg = m_osi.getGraphics();
    if (update) {
      osg.setXORMode(Color.white);
      osg.drawLine(0, y, m_panelWidth - 1, y);
      update();
      osg.drawLine(0, y, m_panelWidth - 1, y);
    }
    

    osg.setPaintMode();
    float[] colVal = new float[3];
    
    float[] tempCols = new float[3];
    for (int k = 0; k < probs.length; k++) {
      Color curr = (Color)m_Colors.elementAt(k % m_Colors.size());
      
      curr.getRGBColorComponents(tempCols);
      for (int z = 0; z < 3; z++) {
        int tmp123_121 = z; float[] tmp123_119 = colVal;tmp123_119[tmp123_121] = ((float)(tmp123_119[tmp123_121] + probs[k] * tempCols[z]));
      }
    }
    
    for (int z = 0; z < 3; z++) {
      if (colVal[z] < 0.0F) {
        colVal[z] = 0.0F;
      } else if (colVal[z] > 1.0F) {
        colVal[z] = 1.0F;
      }
    }
    
    osg.setColor(new Color(colVal[0], colVal[1], colVal[2]));
    

    osg.fillRect(x, y, width, height);
  }
  

  private void update()
  {
    Graphics g = m_plotPanel.getGraphics();
    g.drawImage(m_osi, 0, 0, m_plotPanel);
  }
  





  public void setTrainingData(Instances trainingData)
    throws Exception
  {
    m_trainingData = trainingData;
    if (m_trainingData.classIndex() < 0) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SetTrainingData_Error_Text"));
    }
    m_classIndex = m_trainingData.classIndex();
  }
  


  public void addTrainingInstance(Instance instance)
  {
    if (m_trainingData == null)
    {
      Messages.getInstance();System.err.println(Messages.getString("BoundaryPanel_AddTrainingInstance_Error_Text"));
    }
    
    m_trainingData.add(instance);
  }
  




  public void addActionListener(ActionListener newListener)
  {
    m_listeners.add(newListener);
  }
  




  public void removeActionListener(ActionListener removeListener)
  {
    m_listeners.removeElement(removeListener);
  }
  




  public void setClassifier(Classifier classifier)
  {
    m_classifier = classifier;
  }
  




  public void setDataGenerator(DataGenerator dataGenerator)
  {
    m_dataGenerator = dataGenerator;
  }
  




  public void setXAttribute(int xatt)
    throws Exception
  {
    if (m_trainingData == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SetXAttribute_Error_Text_First"));
    }
    if ((xatt < 0) || (xatt > m_trainingData.numAttributes()))
    {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SetXAttribute_Error_Text_Second"));
    }
    if (m_trainingData.attribute(xatt).isNominal()) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SetXAttribute_Error_Text_Third"));
    }
    



    m_xAttribute = xatt;
  }
  




  public void setYAttribute(int yatt)
    throws Exception
  {
    if (m_trainingData == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SetYAttribute_Error_Text_First"));
    }
    if ((yatt < 0) || (yatt > m_trainingData.numAttributes()))
    {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SetYAttribute_Error_Text_Second"));
    }
    if (m_trainingData.attribute(yatt).isNominal()) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SetYAttribute_Error_Text_Third"));
    }
    



    m_yAttribute = yatt;
  }
  




  public void setColors(FastVector colors)
  {
    synchronized (m_Colors) {
      m_Colors = colors;
    }
    
    update();
  }
  





  public void setPlotTrainingData(boolean pg)
  {
    m_plotTrainingData = pg;
  }
  




  public boolean getPlotTrainingData()
  {
    return m_plotTrainingData;
  }
  




  public FastVector getColors()
  {
    return m_Colors;
  }
  


  public void replot()
  {
    if (m_probabilityCache[0][0] == null) {
      return;
    }
    m_stopReplotting = true;
    m_pausePlotting = true;
    try
    {
      Thread.sleep(300L);
    }
    catch (Exception ex) {}
    Thread replotThread = new Thread() {
      public void run() {
        m_stopReplotting = false;
        int size2 = m_size / 2;
        for (int i = 0; i < m_panelHeight; i += m_size) {
          for (int j = 0; j < m_panelWidth; j += m_size) {
            if ((m_probabilityCache[i][j] == null) || (m_stopReplotting)) {
              break label360;
            }
            
            boolean update = (j == 0) && (i % 2 == 0);
            if ((i < m_panelHeight) && (j < m_panelWidth))
            {
              if ((m_initialTiling) || (m_size == 1)) {
                if (m_probabilityCache[i][j] == null) {
                  break label360;
                }
                BoundaryPanel.this.plotPoint(j, i, m_size, m_size, m_probabilityCache[i][j], update);
              }
              else {
                if (m_probabilityCache[(i + size2)][j] == null) {
                  break label360;
                }
                BoundaryPanel.this.plotPoint(j, i + size2, size2, size2, m_probabilityCache[(i + size2)][j], update);
                
                if (m_probabilityCache[(i + size2)][(j + size2)] == null) {
                  break label360;
                }
                BoundaryPanel.this.plotPoint(j + size2, i + size2, size2, size2, m_probabilityCache[(i + size2)][(j + size2)], update);
                
                if (m_probabilityCache[i][(j + size2)] == null) {
                  break label360;
                }
                BoundaryPanel.this.plotPoint(j + size2, i, size2, size2, m_probabilityCache[(i + size2)][j], update);
              }
            }
          }
        }
        label360:
        BoundaryPanel.this.update();
        if (m_plotTrainingData) {
          plotTrainingData();
        }
        m_pausePlotting = false;
        if (!m_stopPlotting) {
          synchronized (m_dummy) {
            m_dummy.notifyAll();
          }
          
        }
      }
    };
    replotThread.start();
  }
  






  protected void saveImage(String fileName)
  {
    try
    {
      BufferedImage bi = new BufferedImage(m_panelWidth, m_panelHeight, 1);
      Graphics2D gr2 = bi.createGraphics();
      gr2.drawImage(m_osi, 0, 0, m_panelWidth, m_panelHeight, null);
      

      ImageWriter writer = null;
      Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
      if (iter.hasNext()) {
        writer = (ImageWriter)iter.next();
      } else {
        Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanel_SaveImage_Error_Text"));
      }
      
      ImageOutputStream ios = ImageIO.createImageOutputStream(new File(fileName));
      writer.setOutput(ios);
      

      ImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
      param.setCompressionMode(2);
      param.setCompressionQuality(1.0F);
      

      writer.write(null, new IIOImage(bi, null, null), param);
      

      ios.flush();
      writer.dispose();
      ios.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  








  public void addTrainingInstanceFromMouseLocation(int mouseX, int mouseY, int classAttIndex, double classValue)
  {
    double x = convertFromPanelX(mouseX);
    double y = convertFromPanelY(mouseY);
    

    Instance newInstance = new Instance(m_trainingData.numAttributes());
    for (int i = 0; i < newInstance.numAttributes(); i++) {
      if (i == classAttIndex) {
        newInstance.setValue(i, classValue);
      }
      else if (i == m_xAttribute) {
        newInstance.setValue(i, x);
      } else if (i == m_yAttribute)
        newInstance.setValue(i, y); else {
        newInstance.setMissing(i);
      }
    }
    
    addTrainingInstance(newInstance);
  }
  

  public void removeAllInstances()
  {
    if (m_trainingData != null)
    {
      m_trainingData.delete();
      try { initialize();
      }
      catch (Exception e) {}
    }
  }
  



  public void removeTrainingInstanceFromMouseLocation(int mouseX, int mouseY)
  {
    double x = convertFromPanelX(mouseX);
    double y = convertFromPanelY(mouseY);
    
    int bestIndex = -1;
    double bestDistanceBetween = 2.147483647E9D;
    

    for (int i = 0; i < m_trainingData.numInstances(); i++) {
      Instance current = m_trainingData.instance(i);
      double distanceBetween = (current.value(m_xAttribute) - x) * (current.value(m_xAttribute) - x) + (current.value(m_yAttribute) - y) * (current.value(m_yAttribute) - y);
      
      if (distanceBetween < bestDistanceBetween)
      {
        bestIndex = i;
        bestDistanceBetween = distanceBetween;
      }
    }
    if (bestIndex == -1)
      return;
    Instance best = m_trainingData.instance(bestIndex);
    double panelDistance = (convertToPanelX(best.value(m_xAttribute)) - mouseX) * (convertToPanelX(best.value(m_xAttribute)) - mouseX) + (convertToPanelY(best.value(m_yAttribute)) - mouseY) * (convertToPanelY(best.value(m_yAttribute)) - mouseY);
    
    if (panelDistance < 49.0D) {
      m_trainingData.delete(bestIndex);
    }
  }
  

  public void startPlotThread()
  {
    if (m_plotThread == null) {
      m_plotThread = new PlotThread();
      m_plotThread.setPriority(1);
      m_plotThread.start();
    }
  }
  

  public void addMouseListener(MouseListener l)
  {
    m_plotPanel.addMouseListener(l);
  }
  

  public double getMinXBound()
  {
    return m_minX;
  }
  

  public double getMinYBound()
  {
    return m_minY;
  }
  

  public double getMaxXBound()
  {
    return m_maxX;
  }
  

  public double getMaxYBound()
  {
    return m_maxY;
  }
  



  public static void main(String[] args)
  {
    try
    {
      if (args.length < 8) {
        Messages.getInstance();System.err.println(Messages.getString("BoundaryPanel_Main_Error_Text_First"));
        System.exit(1);
      }
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("BoundaryPanel_Main_Title_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      
      Messages.getInstance();System.err.println(Messages.getString("BoundaryPanel_Main_Error_Text_Second") + args[0]);
      Reader r = new BufferedReader(new FileReader(args[0]));
      
      final Instances i = new Instances(r);
      i.setClassIndex(Integer.parseInt(args[1]));
      

      final int xatt = Integer.parseInt(args[2]);
      final int yatt = Integer.parseInt(args[3]);
      int base = Integer.parseInt(args[4]);
      int loc = Integer.parseInt(args[5]);
      
      int bandWidth = Integer.parseInt(args[6]);
      int panelWidth = Integer.parseInt(args[7]);
      int panelHeight = Integer.parseInt(args[8]);
      
      String classifierName = args[9];
      final BoundaryPanel bv = new BoundaryPanel(panelWidth, panelHeight);
      bv.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String classifierNameNew = val$classifierName.substring(val$classifierName.lastIndexOf('.') + 1, val$classifierName.length());
          

          bv.saveImage(classifierNameNew + "_" + i.relationName() + "_X" + xatt + "_Y" + yatt + ".jpg");
        }
        

      });
      jf.getContentPane().add(bv, "Center");
      jf.setSize(bv.getMinimumSize());
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
        
      });
      jf.pack();
      jf.setVisible(true);
      
      bv.repaint();
      

      String[] argsR = null;
      if (args.length > 10) {
        argsR = new String[args.length - 10];
        for (int j = 10; j < args.length; j++) {
          argsR[(j - 10)] = args[j];
        }
      }
      Classifier c = Classifier.forName(args[9], argsR);
      KDDataGenerator dataGen = new KDDataGenerator();
      dataGen.setKernelBandwidth(bandWidth);
      bv.setDataGenerator(dataGen);
      bv.setNumSamplesPerRegion(loc);
      bv.setGeneratorSamplesBase(base);
      bv.setClassifier(c);
      bv.setTrainingData(i);
      bv.setXAttribute(xatt);
      bv.setYAttribute(yatt);
      
      try
      {
        FileInputStream fis = new FileInputStream("colors.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        FastVector colors = (FastVector)ois.readObject();
        bv.setColors(colors);
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("BoundaryPanel_Main_Error_Text_Third"));
      }
      bv.start();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

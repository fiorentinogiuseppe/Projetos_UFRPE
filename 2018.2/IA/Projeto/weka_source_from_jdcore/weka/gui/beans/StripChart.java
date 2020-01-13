package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.EventSetDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.gui.Logger;
import weka.gui.visualize.PrintableComponent;
import weka.gui.visualize.VisualizeUtils;






























public class StripChart
  extends JPanel
  implements ChartListener, InstanceListener, Visible, BeanCommon, UserRequestAcceptor
{
  private static final long serialVersionUID = 1483649041577695019L;
  protected Color[] m_colorList = { Color.green, Color.red, Color.blue, Color.cyan, Color.pink, new Color(255, 0, 255), Color.orange, new Color(255, 0, 0), new Color(0, 255, 0), Color.white };
  


  protected Color m_BackgroundColor;
  


  protected Color m_LegendPanelBorderColor;
  



  private class StripPlotter
    extends JPanel
  {
    private static final long serialVersionUID = -7056271598761675879L;
    



    private StripPlotter() {}
    


    public void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      if (m_osi != null) {
        g.drawImage(m_osi, 0, 0, this);
      }
    }
  }
  
  private transient JFrame m_outputFrame = null;
  private transient StripPlotter m_plotPanel = null;
  



  private transient Image m_osi = null;
  


  private int m_iheight;
  


  private int m_iwidth;
  

  private double m_max = 1.0D;
  



  private double m_min = 0.0D;
  



  private boolean m_yScaleUpdate = false;
  
  private double m_oldMax;
  private double m_oldMin;
  private Font m_labelFont = new Font("Monospaced", 0, 10);
  

  private FontMetrics m_labelMetrics;
  
  private Vector m_legendText = new Vector();
  

  private class ScalePanel
    extends JPanel
  {
    private static final long serialVersionUID = 6416998474984829434L;
    
    private ScalePanel() {}
    
    public void paintComponent(Graphics gx)
    {
      super.paintComponent(gx);
      if (m_labelMetrics == null) {
        m_labelMetrics = gx.getFontMetrics(m_labelFont);
      }
      gx.setFont(m_labelFont);
      int hf = m_labelMetrics.getAscent();
      String temp = "" + m_max;
      gx.setColor(m_colorList[(m_colorList.length - 1)]);
      gx.drawString(temp, 1, hf - 2);
      temp = "" + (m_min + (m_max - m_min) / 2.0D);
      gx.drawString(temp, 1, getHeight() / 2 + hf / 2);
      temp = "" + m_min;
      gx.drawString(temp, 1, getHeight() - 1);
    }
  }
  

  private ScalePanel m_scalePanel = new ScalePanel(null);
  

  private class LegendPanel
    extends JPanel
  {
    private static final long serialVersionUID = 7713986576833797583L;
    
    private LegendPanel() {}
    
    public void paintComponent(Graphics gx)
    {
      super.paintComponent(gx);
      
      if (m_labelMetrics == null) {
        m_labelMetrics = gx.getFontMetrics(m_labelFont);
      }
      int hf = m_labelMetrics.getAscent();
      int x = 10;int y = hf + 15;
      gx.setFont(m_labelFont);
      for (int i = 0; i < m_legendText.size(); i++) {
        String temp = (String)m_legendText.elementAt(i);
        gx.setColor(m_colorList[(i % m_colorList.length)]);
        gx.drawString(temp, x, y);
        y += hf;
      }
      revalidate();
    }
  }
  

  private LegendPanel m_legendPanel = new LegendPanel(null);
  




  private LinkedList m_dataList = new LinkedList();
  
  private double[] m_previousY = new double[1];
  
  private transient Thread m_updateHandler;
  
  protected BeanVisual m_visual = new BeanVisual("StripChart", "weka/gui/beans/icons/StripChart.gif", "weka/gui/beans/icons/StripChart_animated.gif");
  



  private Object m_listenee = null;
  private transient Logger m_log = null;
  



  private int m_xValFreq = 500;
  private int m_xCount = 0;
  



  private int m_refreshWidth = 1;
  



  private int m_refreshFrequency = 5;
  

  protected PrintableComponent m_Printer = null;
  


  public StripChart()
  {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
    

    initPlot();
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("StripChart_GlobalInfo_Text");
  }
  




  public String xLabelFreqTipText()
  {
    Messages.getInstance();return Messages.getString("StripChart_XLabelFreqTipText_Text");
  }
  




  public void setXLabelFreq(int freq)
  {
    m_xValFreq = freq;
    if (getGraphics() != null) {
      setRefreshWidth();
    }
  }
  



  public int getXLabelFreq()
  {
    return m_xValFreq;
  }
  




  public String refreshFreqTipText()
  {
    Messages.getInstance();return Messages.getString("StripChart_RefreshFreqTipText_Text");
  }
  




  public void setRefreshFreq(int freq)
  {
    m_refreshFrequency = freq;
    if (getGraphics() != null) {
      setRefreshWidth();
    }
  }
  



  public int getRefreshFreq()
  {
    return m_refreshFrequency;
  }
  
  private void setRefreshWidth() {
    m_refreshWidth = 1;
    if (m_labelMetrics == null) {
      getGraphics().setFont(m_labelFont);
      m_labelMetrics = getGraphics().getFontMetrics(m_labelFont);
    }
    
    int refWidth = m_labelMetrics.stringWidth("99000");
    
    int z = getXLabelFreq() / getRefreshFreq();
    if (z < 1) {
      z = 1;
    }
    
    if (z * m_refreshWidth < refWidth + 5) {
      m_refreshWidth *= ((refWidth + 5) / z + 1);
    }
  }
  






  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    try
    {
      ois.defaultReadObject();
      initPlot();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  








  private void setProperties()
  {
    String key = getClass().getName() + ".backgroundColour";
    String color = KnowledgeFlowApp.BEAN_PROPERTIES.getProperty(key);
    m_BackgroundColor = Color.BLACK;
    if (color != null) {
      m_BackgroundColor = VisualizeUtils.processColour(color, m_BackgroundColor);
    }
    
    key = m_legendPanel.getClass().getName() + ".borderColour";
    color = KnowledgeFlowApp.BEAN_PROPERTIES.getProperty(key);
    m_LegendPanelBorderColor = Color.BLUE;
    if (color != null)
      m_LegendPanelBorderColor = VisualizeUtils.processColour(color, m_LegendPanelBorderColor);
  }
  
  private void initPlot() {
    setProperties();
    m_plotPanel = new StripPlotter(null);
    m_plotPanel.setBackground(m_BackgroundColor);
    m_scalePanel.setBackground(m_BackgroundColor);
    m_legendPanel.setBackground(m_BackgroundColor);
    m_xCount = 0;
  }
  
  private void startHandler() {
    if (m_updateHandler == null) {
      m_updateHandler = new Thread() {
        private double[] dataPoint;
        
        public void run() {
          for (;;) { if (m_outputFrame != null) {
              synchronized (m_dataList) {
                while (m_dataList.isEmpty()) {
                  try
                  {
                    m_dataList.wait();
                  } catch (InterruptedException ex) {
                    return;
                  }
                }
                dataPoint = ((double[])m_dataList.remove(0));
              }
              
              if (m_outputFrame != null) {
                updateChart(dataPoint);
              }
              
            }
          }
        }
      };
      m_updateHandler.start();
    }
  }
  


  public void showChart()
  {
    if (m_outputFrame == null) {
      Messages.getInstance();m_outputFrame = new JFrame(Messages.getString("StripChart_ShowChart_OutputFrame_JFrame_Text"));
      m_outputFrame.getContentPane().setLayout(new BorderLayout());
      JPanel panel = new JPanel(new BorderLayout());
      new PrintableComponent(panel);
      m_outputFrame.getContentPane().add(panel, "Center");
      panel.add(m_legendPanel, "West");
      panel.add(m_plotPanel, "Center");
      panel.add(m_scalePanel, "East");
      m_legendPanel.setMinimumSize(new Dimension(100, getHeight()));
      m_legendPanel.setPreferredSize(new Dimension(100, getHeight()));
      m_scalePanel.setMinimumSize(new Dimension(30, getHeight()));
      m_scalePanel.setPreferredSize(new Dimension(30, getHeight()));
      Font lf = new Font("Monospaced", 0, 12);
      Messages.getInstance();m_legendPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(Color.gray, Color.darkGray), Messages.getString("StripChart_ShowChart_LegendPanel_SetBorder_BorderFactoryCreateEtchedBorder_Text"), 2, 0, lf, m_LegendPanelBorderColor));
      






      m_outputFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          if (m_updateHandler != null) {
            Messages.getInstance();System.err.println(Messages.getString("StripChart_ShowChart_WindowClosing_Error_Text"));
            m_updateHandler.interrupt();
            m_updateHandler = null;
          }
          synchronized (m_dataList) {
            m_dataList = new LinkedList();
          }
          m_outputFrame.dispose();
          m_outputFrame = null;
        }
      });
      m_outputFrame.pack();
      m_outputFrame.setSize(600, 150);
      m_outputFrame.setResizable(false);
      m_outputFrame.setVisible(true);
      m_outputFrame.setAlwaysOnTop(true);
      int iwidth = m_plotPanel.getWidth();
      int iheight = m_plotPanel.getHeight();
      m_osi = m_plotPanel.createImage(iwidth, iheight);
      Graphics m = m_osi.getGraphics();
      m.setColor(m_BackgroundColor);
      m.fillRect(0, 0, iwidth, iheight);
      m_previousY[0] = -1.0D;
      setRefreshWidth();
      if (m_updateHandler == null) {
        Messages.getInstance();System.err.println(Messages.getString("StripChart_ShowChart_Error_Text"));
        startHandler();
      }
    } else {
      m_outputFrame.toFront();
    }
  }
  
  private int convertToPanelY(double yval) {
    int height = m_plotPanel.getHeight();
    double temp = (yval - m_min) / (m_max - m_min);
    temp *= height;
    temp = height - temp;
    return (int)temp;
  }
  




  protected void updateChart(double[] dataPoint)
  {
    if (m_previousY[0] == -1.0D) {
      int iw = m_plotPanel.getWidth();
      int ih = m_plotPanel.getHeight();
      m_osi = m_plotPanel.createImage(iw, ih);
      Graphics m = m_osi.getGraphics();
      m.setColor(m_BackgroundColor);
      m.fillRect(0, 0, iw, ih);
      m_previousY[0] = convertToPanelY(0.0D);
      m_iheight = ih;m_iwidth = iw;
    }
    
    if (dataPoint.length - 1 != m_previousY.length) {
      m_previousY = new double[dataPoint.length - 1];
      
      for (int i = 0; i < dataPoint.length - 1; i++) {
        m_previousY[i] = convertToPanelY(0.0D);
      }
    }
    
    Graphics osg = m_osi.getGraphics();
    Graphics g = m_plotPanel.getGraphics();
    
    osg.copyArea(m_refreshWidth, 0, m_iwidth - m_refreshWidth, m_iheight, -m_refreshWidth, 0);
    
    osg.setColor(m_BackgroundColor);
    osg.fillRect(m_iwidth - m_refreshWidth, 0, m_iwidth, m_iheight);
    

    if (m_yScaleUpdate) {
      String maxVal = numToString(m_oldMax);
      String minVal = numToString(m_oldMin);
      String midVal = numToString((m_oldMax - m_oldMin) / 2.0D);
      if (m_labelMetrics == null) {
        m_labelMetrics = g.getFontMetrics(m_labelFont);
      }
      osg.setFont(m_labelFont);
      int wmx = m_labelMetrics.stringWidth(maxVal);
      int wmn = m_labelMetrics.stringWidth(minVal);
      int wmd = m_labelMetrics.stringWidth(midVal);
      
      int hf = m_labelMetrics.getAscent();
      osg.setColor(m_colorList[(m_colorList.length - 1)]);
      osg.drawString(maxVal, m_iwidth - wmx, hf - 2);
      osg.drawString(midVal, m_iwidth - wmd, m_iheight / 2 + hf / 2);
      osg.drawString(minVal, m_iwidth - wmn, m_iheight - 1);
      m_yScaleUpdate = false;
    }
    

    for (int i = 0; i < dataPoint.length - 1; i++) {
      osg.setColor(m_colorList[(i % m_colorList.length)]);
      double pos = convertToPanelY(dataPoint[i]);
      osg.drawLine(m_iwidth - m_refreshWidth, (int)m_previousY[i], m_iwidth - 1, (int)pos);
      
      m_previousY[i] = pos;
      if (dataPoint[(dataPoint.length - 1)] % m_xValFreq == 0.0D)
      {
        String val = numToString(dataPoint[i]);
        if (m_labelMetrics == null) {
          m_labelMetrics = g.getFontMetrics(m_labelFont);
        }
        int hf = m_labelMetrics.getAscent();
        if (pos - hf < 0.0D) {
          pos += hf;
        }
        int w = m_labelMetrics.stringWidth(val);
        osg.setFont(m_labelFont);
        osg.drawString(val, m_iwidth - w, (int)pos);
      }
    }
    

    if (dataPoint[(dataPoint.length - 1)] % m_xValFreq == 0.0D)
    {
      String xVal = "" + (int)dataPoint[(dataPoint.length - 1)];
      osg.setColor(m_colorList[(m_colorList.length - 1)]);
      int w = m_labelMetrics.stringWidth(xVal);
      osg.setFont(m_labelFont);
      osg.drawString(xVal, m_iwidth - w, m_iheight - 1);
    }
    g.drawImage(m_osi, 0, 0, m_plotPanel);
  }
  

  private static String numToString(double num)
  {
    int precision = 1;
    int whole = (int)Math.abs(num);
    double decimal = Math.abs(num) - whole;
    
    int nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    


    precision = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(num)) / Math.log(10.0D)) + 2 : 1;
    


    if (precision > 5) {
      precision = 1;
    }
    
    String numString = Utils.doubleToString(num, nondecimal + 1 + precision, precision);
    


    return numString;
  }
  
  ChartEvent m_ce = new ChartEvent(this);
  double[] m_dataPoint = null;
  
  public void acceptInstance(InstanceEvent e) { if (e.getStatus() == 0) {
      Instances structure = e.getStructure();
      m_legendText = new Vector();
      m_max = 1.0D;
      m_min = 0.0D;
      int i = 0;
      for (i = 0; i < structure.numAttributes(); i++) {
        if (i > 10) {
          i--;
          break;
        }
        m_legendText.addElement(structure.attribute(i).name());
        m_legendPanel.repaint();
        m_scalePanel.repaint();
      }
      m_dataPoint = new double[i];
      m_xCount = 0;
      return;
    }
    

    Instance inst = e.getInstance();
    for (int i = 0; i < m_dataPoint.length; i++) {
      if (!inst.isMissing(i)) {
        m_dataPoint[i] = inst.value(i);
      }
    }
    acceptDataPoint(m_dataPoint);
    m_xCount += 1;
  }
  




  public void acceptDataPoint(ChartEvent e)
  {
    if (e.getReset()) {
      m_xCount = 0;
      m_max = 1.0D;
      m_min = 0.0D;
    }
    if (m_outputFrame != null) {
      boolean refresh = false;
      if (((e.getLegendText() != null ? 1 : 0) & (e.getLegendText() != m_legendText ? 1 : 0)) != 0) {
        m_legendText = e.getLegendText();
        refresh = true;
      }
      
      if ((e.getMin() != m_min) || (e.getMax() != m_max)) {
        m_oldMax = m_max;m_oldMin = m_min;
        m_max = e.getMax();
        m_min = e.getMin();
        refresh = true;
        m_yScaleUpdate = true;
      }
      
      if (refresh) {
        m_legendPanel.repaint();
        m_scalePanel.repaint();
      }
      
      acceptDataPoint(e.getDataPoint());
    }
    m_xCount += 1;
  }
  





  public void acceptDataPoint(double[] dataPoint)
  {
    if ((m_outputFrame != null) && (m_xCount % m_refreshFrequency == 0)) {
      double[] dp = new double[dataPoint.length + 1];
      dp[(dp.length - 1)] = m_xCount;
      System.arraycopy(dataPoint, 0, dp, 0, dataPoint.length);
      
      for (int i = 0; i < dataPoint.length; i++) {
        if (dataPoint[i] < m_min) {
          m_oldMin = m_min;m_min = dataPoint[i];
          m_yScaleUpdate = true;
        }
        
        if (dataPoint[i] > m_max) {
          m_oldMax = m_max;m_max = dataPoint[i];
          m_yScaleUpdate = true;
        }
      }
      if (m_yScaleUpdate) {
        m_scalePanel.repaint();
        m_yScaleUpdate = false;
      }
      synchronized (m_dataList) {
        m_dataList.add(m_dataList.size(), dp);
        
        m_dataList.notifyAll();
      }
    }
  }
  









  public void setVisual(BeanVisual newVisual)
  {
    m_visual = newVisual;
  }
  


  public BeanVisual getVisual()
  {
    return m_visual;
  }
  


  public void useDefaultVisual()
  {
    m_visual.loadIcons("weka/gui/beans/icons/StripChart.gif", "weka/gui/beans/icons/StripChart_animated.gif");
  }
  




  public void stop()
  {
    if ((m_listenee instanceof BeanCommon)) {
      ((BeanCommon)m_listenee).stop();
    }
  }
  





  public boolean isBusy()
  {
    return m_updateHandler != null;
  }
  




  public void setLog(Logger logger)
  {
    m_log = logger;
  }
  






  public boolean connectionAllowed(String eventName)
  {
    if (m_listenee == null) {
      return true;
    }
    return false;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  








  public void connectionNotification(String eventName, Object source)
  {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }
  








  public void disconnectionNotification(String eventName, Object source)
  {
    m_listenee = null;
  }
  




  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    newVector.addElement("Show chart");
    return newVector.elements();
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Show chart") == 0) {
      showChart();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("StripChart_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  






  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("StripChart_Main_Jf_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      StripChart jd = new StripChart();
      jf.getContentPane().add(jd, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      jd.showChart();
      Random r = new Random(1L);
      for (int i = 0; i < 1020; i++) {
        double[] pos = new double[1];
        pos[0] = r.nextDouble();
        jd.acceptDataPoint(pos);
      }
      Messages.getInstance();System.err.println(Messages.getString("StripChart_Main_Error_Text"));
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

package weka.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.FileReader;
import java.io.PrintStream;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.experiment.Stats;
import weka.gui.visualize.PrintableComponent;
import weka.gui.visualize.PrintablePanel;













































































































public class AttributeVisualizationPanel
  extends PrintablePanel
{
  private static final long serialVersionUID = -8650490488825371193L;
  protected Instances m_data;
  protected AttributeStats m_as;
  protected AttributeStats[] m_asCache;
  protected int m_attribIndex;
  protected int m_maxValue;
  protected int[] m_histBarCounts;
  SparseInstance[] m_histBarClassCounts;
  protected double m_barRange;
  protected int m_classIndex;
  private Thread m_hc;
  private boolean m_threadRun = false;
  
  private boolean m_doneCurrentAttribute = false;
  private boolean m_displayCurrentAttribute = false;
  






  protected JComboBox m_colorAttrib;
  






  private FontMetrics m_fm;
  






  private Integer m_locker = new Integer(1);
  





  private FastVector m_colorList = new FastVector();
  

  private static final Color[] m_defaultColors = { Color.blue, Color.red, Color.cyan, new Color(75, 123, 130), Color.pink, Color.green, Color.orange, new Color(255, 0, 255), new Color(255, 0, 0), new Color(0, 255, 0) };
  













  public AttributeVisualizationPanel()
  {
    this(false);
  }
  






  public AttributeVisualizationPanel(boolean showColouringOption)
  {
    setFont(new Font("Default", 0, 9));
    m_fm = getFontMetrics(getFont());
    setToolTipText("");
    FlowLayout fl = new FlowLayout(0);
    setLayout(fl);
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent ce) {
        if (m_data != null) {}

      }
      

    });
    m_colorAttrib = new JComboBox();
    m_colorAttrib.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == 1) {
          m_classIndex = (m_colorAttrib.getSelectedIndex() - 1);
          if (m_as != null) {
            setAttribute(m_attribIndex);
          }
        }
      }
    });
    
    if (showColouringOption)
    {
      add(m_colorAttrib);
      validate();
    }
  }
  




  public void setInstances(Instances newins)
  {
    m_attribIndex = 0;
    m_as = null;
    m_data = new Instances(newins);
    if (m_colorAttrib != null) {
      m_colorAttrib.removeAllItems();
      Messages.getInstance();m_colorAttrib.addItem(Messages.getString("AttributeVisualizationPanel_SetInstances_ColorAttrib_Text_First"));
      for (int i = 0; i < m_data.numAttributes(); i++) {
        String type = "";
        switch (m_data.attribute(i).type()) {
        case 1: 
          Messages.getInstance();type = Messages.getString("AttributeVisualizationPanel_SetInstances_AttributeNOMINAL_Text");
          break;
        case 0: 
          Messages.getInstance();type = Messages.getString("AttributeVisualizationPanel_SetInstances_AttributeNUMERIC_Text");
          break;
        case 2: 
          Messages.getInstance();type = Messages.getString("AttributeVisualizationPanel_SetInstances_AttributeSTRING_Text");
          break;
        case 3: 
          Messages.getInstance();type = Messages.getString("AttributeVisualizationPanel_SetInstances_AttributeDATE_Text");
          break;
        case 4: 
          Messages.getInstance();type = Messages.getString("AttributeVisualizationPanel_SetInstances_AttributeRELATIONAL_Text");
          break;
        default: 
          Messages.getInstance();type = Messages.getString("AttributeVisualizationPanel_SetInstances_AttributeDEFAULT_Text");
        }
        Messages.getInstance();m_colorAttrib.addItem(new String(Messages.getString("AttributeVisualizationPanel_SetInstances_ColorAttrib_Text_Second") + m_data.attribute(i).name() + " " + type));
      }
      
      if (m_data.classIndex() >= 0) {
        m_colorAttrib.setSelectedIndex(m_data.classIndex() + 1);
      } else {
        m_colorAttrib.setSelectedIndex(m_data.numAttributes());
      }
    }
    


    if (m_data.classIndex() >= 0) {
      m_classIndex = m_data.classIndex();
    } else {
      m_classIndex = (m_data.numAttributes() - 1);
    }
    
    m_asCache = new AttributeStats[m_data.numAttributes()];
  }
  



  public JComboBox getColorBox()
  {
    return m_colorAttrib;
  }
  




  public int getColoringIndex()
  {
    return m_classIndex;
  }
  




  public void setColoringIndex(int ci)
  {
    m_classIndex = ci;
    if (m_colorAttrib != null) {
      m_colorAttrib.setSelectedIndex(ci + 1);
    } else {
      setAttribute(m_attribIndex);
    }
  }
  




  public void setAttribute(int index)
  {
    synchronized (m_locker)
    {
      m_threadRun = false;
      m_doneCurrentAttribute = false;
      m_displayCurrentAttribute = true;
      
      m_attribIndex = index;
      if (m_asCache[index] != null) {
        m_as = m_asCache[index];
      } else {
        m_asCache[index] = m_data.attributeStats(index);
        m_as = m_asCache[index];
      }
    }
    

    repaint();
  }
  





  public void calcGraph(int panelWidth, int panelHeight)
  {
    synchronized (m_locker) {
      m_threadRun = true;
      if (m_as.nominalCounts != null) {
        m_hc = new BarCalc(panelWidth, panelHeight);
        m_hc.setPriority(1);
        m_hc.start();
      }
      else if (m_as.numericStats != null) {
        m_hc = new HistCalc(null);
        m_hc.setPriority(1);
        m_hc.start();
      } else {
        m_histBarCounts = null;
        m_histBarClassCounts = null;
        m_doneCurrentAttribute = true;
        m_threadRun = false;
        repaint();
      }
    }
  }
  


  private class BarCalc
    extends Thread
  {
    private int m_panelWidth;
    
    private int m_panelHeight;
    

    public BarCalc(int panelWidth, int panelHeight)
    {
      m_panelWidth = panelWidth;
      m_panelHeight = panelHeight;
    }
    
    public void run() {
      synchronized (m_locker)
      {

        if (m_data.attribute(m_attribIndex).numValues() > m_panelWidth) {
          m_histBarClassCounts = null;
          m_threadRun = false;
          m_doneCurrentAttribute = true;
          m_displayCurrentAttribute = false;
          repaint();
          return;
        }
        
        if ((m_classIndex >= 0) && (m_data.attribute(m_classIndex).isNominal()))
        {

          SparseInstance[] histClassCounts = new SparseInstance[m_data.attribute(m_attribIndex).numValues()];
          



          if (m_as.nominalCounts.length > 0) {
            m_maxValue = m_as.nominalCounts[0];
            for (int i = 0; i < m_data.attribute(m_attribIndex).numValues(); i++) {
              if (m_as.nominalCounts[i] > m_maxValue) {
                m_maxValue = m_as.nominalCounts[i];
              }
            }
          } else {
            m_maxValue = 0;
          }
          
          if (m_colorList.size() == 0)
            m_colorList.addElement(Color.black);
          for (int i = m_colorList.size(); 
              i < m_data.attribute(m_classIndex).numValues() + 1; i++) {
            Color pc = AttributeVisualizationPanel.m_defaultColors[((i - 1) % 10)];
            int ija = (i - 1) / 10;
            ija *= 2;
            
            for (int j = 0; j < ija; j++) {
              pc = pc.darker();
            }
            
            m_colorList.addElement(pc);
          }
          

          m_data.sort(m_attribIndex);
          double[] tempClassCounts = null;
          int tempAttValueIndex = -1;
          
          for (int k = 0; k < m_data.numInstances(); k++)
          {



            if (!m_data.instance(k).isMissing(m_attribIndex))
            {
              if (m_data.instance(k).value(m_attribIndex) != tempAttValueIndex) {
                if (tempClassCounts != null)
                {
                  int numNonZero = 0;
                  for (int z = 0; z < tempClassCounts.length; z++) {
                    if (tempClassCounts[z] > 0.0D) {
                      numNonZero++;
                    }
                  }
                  double[] nonZeroVals = new double[numNonZero];
                  int[] nonZeroIndices = new int[numNonZero];
                  int count = 0;
                  for (int z = 0; z < tempClassCounts.length; z++) {
                    if (tempClassCounts[z] > 0.0D) {
                      nonZeroVals[count] = tempClassCounts[z];
                      nonZeroIndices[(count++)] = z;
                    }
                  }
                  SparseInstance tempS = new SparseInstance(1.0D, nonZeroVals, nonZeroIndices, tempClassCounts.length);
                  
                  histClassCounts[tempAttValueIndex] = tempS;
                }
                
                tempClassCounts = new double[m_data.attribute(m_classIndex).numValues() + 1];
                tempAttValueIndex = (int)m_data.instance(k).value(m_attribIndex);
              }
              


              if (m_data.instance(k).isMissing(m_classIndex))
              {

                tempClassCounts[0] += m_data.instance(k).weight();
              } else {
                tempClassCounts[((int)m_data.instance(k).value(m_classIndex) + 1)] += m_data.instance(k).weight();
              }
            }
          }
          





          if (tempClassCounts != null)
          {
            int numNonZero = 0;
            for (int z = 0; z < tempClassCounts.length; z++) {
              if (tempClassCounts[z] > 0.0D) {
                numNonZero++;
              }
            }
            double[] nonZeroVals = new double[numNonZero];
            int[] nonZeroIndices = new int[numNonZero];
            int count = 0;
            for (int z = 0; z < tempClassCounts.length; z++) {
              if (tempClassCounts[z] > 0.0D) {
                nonZeroVals[count] = tempClassCounts[z];
                nonZeroIndices[(count++)] = z;
              }
            }
            SparseInstance tempS = new SparseInstance(1.0D, nonZeroVals, nonZeroIndices, tempClassCounts.length);
            
            histClassCounts[tempAttValueIndex] = tempS;
          }
          























          m_threadRun = false;
          m_doneCurrentAttribute = true;
          m_displayCurrentAttribute = true;
          m_histBarClassCounts = histClassCounts;
          




          repaint();
        }
        else
        {
          int[] histCounts = new int[m_data.attribute(m_attribIndex).numValues()];
          
          if (m_as.nominalCounts.length > 0) {
            m_maxValue = m_as.nominalCounts[0];
            for (int i = 0; i < m_data.attribute(m_attribIndex).numValues(); i++) {
              if (m_as.nominalCounts[i] > m_maxValue) {
                m_maxValue = m_as.nominalCounts[i];
              }
            }
          } else {
            m_maxValue = 0;
          }
          
          for (int k = 0; k < m_data.numInstances(); k++) {
            if (!m_data.instance(k).isMissing(m_attribIndex))
              histCounts[((int)m_data.instance(k).value(m_attribIndex))] += 1;
          }
          m_threadRun = false;
          m_histBarCounts = histCounts;
          m_doneCurrentAttribute = true;
          m_displayCurrentAttribute = true;
          



          repaint();
        }
      }
    }
  }
  

  private class HistCalc
    extends Thread
  {
    private HistCalc() {}
    

    public void run()
    {
      synchronized (m_locker) {
        if ((m_classIndex >= 0) && (m_data.attribute(m_classIndex).isNominal()))
        {

          double intervalWidth = 0.0D;
          









          intervalWidth = 3.49D * m_as.numericStats.stdDev * Math.pow(m_data.numInstances(), -0.3333333333333333D);
          



          int intervals = Math.max(1, (int)Math.round((m_as.numericStats.max - m_as.numericStats.min) / intervalWidth));
          










          if (intervals > getWidth()) {
            intervals = getWidth() - 6;
            if (intervals < 1)
              intervals = 1;
          }
          int[][] histClassCounts = new int[intervals][m_data.attribute(m_classIndex).numValues() + 1];
          


          double barRange = (m_as.numericStats.max - m_as.numericStats.min) / histClassCounts.length;
          

          m_maxValue = 0;
          
          if (m_colorList.size() == 0)
            m_colorList.addElement(Color.black);
          for (int i = m_colorList.size(); 
              i < m_data.attribute(m_classIndex).numValues() + 1; i++) {
            Color pc = AttributeVisualizationPanel.m_defaultColors[((i - 1) % 10)];
            int ija = (i - 1) / 10;
            ija *= 2;
            for (int j = 0; j < ija; j++) {
              pc = pc.darker();
            }
            m_colorList.addElement(pc);
          }
          
          for (int k = 0; k < m_data.numInstances(); k++) {
            int t = 0;
            try
            {
              if (!m_data.instance(k).isMissing(m_attribIndex))
              {
                t = (int)Math.ceil((float)((m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) / barRange));
                

                if (t == 0) {
                  if (m_data.instance(k).isMissing(m_classIndex)) {
                    histClassCounts[t][0] += 1;
                  } else {
                    histClassCounts[t][((int)m_data.instance(k).value(m_classIndex) + 1)] += 1;
                  }
                  

                }
                else if (m_data.instance(k).isMissing(m_classIndex)) {
                  histClassCounts[(t - 1)][0] += 1;
                } else {
                  histClassCounts[(t - 1)][((int)m_data.instance(k).value(m_classIndex) + 1)] += 1;
                }
                
              }
            }
            catch (ArrayIndexOutOfBoundsException ae)
            {
              System.out.println("t:" + t + " barRange:" + barRange + " histLength:" + histClassCounts.length + " value:" + m_data.instance(k).value(m_attribIndex) + " min:" + m_as.numericStats.min + " sumResult:" + (m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) + " divideResult:" + (float)((m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) / barRange) + " finalResult:" + Math.ceil((float)((m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) / barRange)));
            }
          }
          












          for (int i = 0; i < histClassCounts.length; i++) {
            int sum = 0;
            for (int j = 0; j < histClassCounts[i].length; j++)
              sum += histClassCounts[i][j];
            if (m_maxValue < sum) {
              m_maxValue = sum;
            }
          }
          
          SparseInstance[] histClassCountsSparse = new SparseInstance[histClassCounts.length];
          

          for (int i = 0; i < histClassCounts.length; i++) {
            int numSparseValues = 0;
            for (int j = 0; j < histClassCounts[i].length; j++) {
              if (histClassCounts[i][j] > 0) {
                numSparseValues++;
              }
            }
            double[] sparseValues = new double[numSparseValues];
            int[] sparseIndices = new int[numSparseValues];
            int count = 0;
            for (int j = 0; j < histClassCounts[i].length; j++) {
              if (histClassCounts[i][j] > 0) {
                sparseValues[count] = histClassCounts[i][j];
                sparseIndices[(count++)] = j;
              }
            }
            
            SparseInstance tempS = new SparseInstance(1.0D, sparseValues, sparseIndices, histClassCounts[i].length);
            

            histClassCountsSparse[i] = tempS;
          }
          

          m_histBarClassCounts = histClassCountsSparse;
          
          m_barRange = barRange;










        }
        else
        {










          double intervalWidth = 3.49D * m_as.numericStats.stdDev * Math.pow(m_data.numInstances(), -0.3333333333333333D);
          



          int intervals = Math.max(1, (int)Math.round((m_as.numericStats.max - m_as.numericStats.min) / intervalWidth));
          





          if (intervals > getWidth()) {
            intervals = getWidth() - 6;
            if (intervals < 1) {
              intervals = 1;
            }
          }
          int[] histCounts = new int[intervals];
          double barRange = (m_as.numericStats.max - m_as.numericStats.min) / histCounts.length;
          

          m_maxValue = 0;
          
          for (int k = 0; k < m_data.numInstances(); k++) {
            int t = 0;
            

            if (!m_data.instance(k).isMissing(m_attribIndex))
            {
              try
              {

                t = (int)Math.ceil((float)((m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) / barRange));
                

                if (t == 0) {
                  histCounts[t] += 1;
                  if (histCounts[t] > m_maxValue) {
                    m_maxValue = histCounts[t];
                  }
                } else {
                  histCounts[(t - 1)] += 1;
                  if (histCounts[(t - 1)] > m_maxValue) {
                    m_maxValue = histCounts[(t - 1)];
                  }
                }
              } catch (ArrayIndexOutOfBoundsException ae) {
                ae.printStackTrace();
                Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_First") + t + Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_Second") + barRange + Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_Third") + histCounts.length + Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_Fourth") + m_data.instance(k).value(m_attribIndex) + Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_Fifth") + m_as.numericStats.min + Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_Sixth") + (m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) + Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_Seventh") + (float)((m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) / barRange) + Messages.getString("AttributeVisualizationPanel_HistCalc_Run_ArrayIndexOutOfBoundsException_Text_Eighth") + Math.ceil((float)((m_data.instance(k).value(m_attribIndex) - m_as.numericStats.min) / barRange)));
              }
            }
          }
          










          m_histBarCounts = histCounts;
          m_barRange = barRange;
        }
        
        m_threadRun = false;
        m_displayCurrentAttribute = true;
        m_doneCurrentAttribute = true;
        



        repaint();
      }
    }
  }
  























































































  public String getToolTipText(MouseEvent ev)
  {
    if ((m_as != null) && (m_as.nominalCounts != null))
    {
      float intervalWidth = getWidth() / m_as.nominalCounts.length;
      
      int x = 0;int y = 0;
      int barWidth;
      int barWidth;
      if (intervalWidth > 5.0F) {
        barWidth = (int)Math.floor(intervalWidth * 0.8F);
      } else {
        barWidth = 1;
      }
      


      x += (int)(Math.floor(intervalWidth * 0.1F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.1F));
      




      if (getWidth() - (m_as.nominalCounts.length * barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F)) * m_as.nominalCounts.length) > 2)
      {







        x += (getWidth() - (m_as.nominalCounts.length * barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F)) * m_as.nominalCounts.length)) / 2;
      }
      



      for (int i = 0; i < m_as.nominalCounts.length; i++) {
        float heightRatio = (getHeight() - m_fm.getHeight()) / m_maxValue;
        
        y = getHeight() - Math.round(m_as.nominalCounts[i] * heightRatio);
        


        if ((ev.getX() >= x) && (ev.getX() <= x + barWidth) && (ev.getY() >= getHeight() - Math.round(m_as.nominalCounts[i] * heightRatio)))
        {

          return m_data.attribute(m_attribIndex).value(i) + " [" + m_as.nominalCounts[i] + "]";
        }
        

        x = x + barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F));
      }
      
    }
    else if ((!m_threadRun) && ((m_histBarCounts != null) || (m_histBarClassCounts != null)))
    {


      int x = 0;int y = 0;
      double bar = m_as.numericStats.min;
      

      if ((m_classIndex >= 0) && (m_data.attribute(m_classIndex).isNominal()))
      {


        int barWidth = (getWidth() - 6) / m_histBarClassCounts.length < 1 ? 1 : (getWidth() - 6) / m_histBarClassCounts.length;
        



        x = 3;
        if (getWidth() - (x + m_histBarClassCounts.length * barWidth) > 5) {
          x += (getWidth() - (x + m_histBarClassCounts.length * barWidth)) / 2;
        }
        float heightRatio = (getHeight() - m_fm.getHeight()) / m_maxValue;
        
        if (ev.getX() - x >= 0)
        {

          int temp = (int)((ev.getX() - x) / (barWidth + 1.0E-10D));
          if (temp == 0) {
            int sum = 0;
            for (int k = 0; k < m_histBarClassCounts[0].numValues(); k++) {
              sum = (int)(sum + m_histBarClassCounts[0].valueSparse(k));
            }
            
            Messages.getInstance();Messages.getInstance();Messages.getInstance();return Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_First") + sum + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Second") + Utils.doubleToString(bar + m_barRange * temp, 3) + ", " + Utils.doubleToString(bar + m_barRange * (temp + 1), 3) + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Fourth");
          }
          


          if (temp < m_histBarClassCounts.length) {
            int sum = 0;
            for (int k = 0; k < m_histBarClassCounts[temp].numValues(); k++) {
              sum = (int)(sum + m_histBarClassCounts[temp].valueSparse(k));
            }
            
            Messages.getInstance();Messages.getInstance();Messages.getInstance();return Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Fifth") + sum + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Sixth") + Utils.doubleToString(bar + m_barRange * temp, 3) + ", " + Utils.doubleToString(bar + m_barRange * (temp + 1), 3) + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Eighth");
          }
          
        }
        
      }
      else
      {
        int barWidth = (getWidth() - 6) / m_histBarCounts.length < 1 ? 1 : (getWidth() - 6) / m_histBarCounts.length;
        



        x = 3;
        if (getWidth() - (x + m_histBarCounts.length * barWidth) > 5) {
          x += (getWidth() - (x + m_histBarCounts.length * barWidth)) / 2;
        }
        float heightRatio = (getHeight() - m_fm.getHeight()) / m_maxValue;
        
        if (ev.getX() - x >= 0)
        {
          int temp = (int)((ev.getX() - x) / (barWidth + 1.0E-10D));
          

          if (temp == 0) {
            Messages.getInstance();Messages.getInstance();Messages.getInstance();return Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Nineth") + m_histBarCounts[0] + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Texth") + Utils.doubleToString(bar + m_barRange * temp, 3) + ", " + Utils.doubleToString(bar + m_barRange * (temp + 1), 3) + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Twelveth");
          }
          

          if (temp < m_histBarCounts.length) {
            Messages.getInstance();Messages.getInstance();Messages.getInstance();return Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Thirteenth") + m_histBarCounts[temp] + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Fourteenth") + Utils.doubleToString(bar + m_barRange * temp, 3) + ", " + Utils.doubleToString(bar + m_barRange * (temp + 1), 3) + Messages.getString("AttributeVisualizationPanel_GetToolTipText_Text_Sixteenth");
          }
        }
      }
    }
    

    return PrintableComponent.getToolTipText(m_Printer);
  }
  





  public void paintComponent(Graphics g)
  {
    g.clearRect(0, 0, getWidth(), getHeight());
    
    if (m_as != null) {
      if ((!m_doneCurrentAttribute) && (!m_threadRun)) {
        calcGraph(getWidth(), getHeight());
      }
      
      if ((!m_threadRun) && (m_displayCurrentAttribute)) {
        int buttonHeight = 0;
        
        if (m_colorAttrib != null) {
          buttonHeight = m_colorAttrib.getHeight() + m_colorAttrib.getLocation().y;
        }
        
        if ((m_as.nominalCounts != null) && ((m_histBarClassCounts != null) || (m_histBarCounts != null)))
        {

          int x = 0;int y = 0;
          


          if ((m_classIndex >= 0) && (m_data.attribute(m_classIndex).isNominal()))
          {

            float intervalWidth = getWidth() / m_histBarClassCounts.length;
            
            int barWidth;
            
            int barWidth;
            
            if (intervalWidth > 5.0F) {
              barWidth = (int)Math.floor(intervalWidth * 0.8F);
            } else {
              barWidth = 1;
            }
            

            x += (int)(Math.floor(intervalWidth * 0.1F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.1F));
            



            if (getWidth() - (m_histBarClassCounts.length * barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F)) * m_histBarClassCounts.length) > 2)
            {








              x += (getWidth() - (m_histBarClassCounts.length * barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F)) * m_histBarClassCounts.length)) / 2;
            }
            






            int sum = 0;
            for (int i = 0; i < m_histBarClassCounts.length; i++)
            {



              float heightRatio = (getHeight() - m_fm.getHeight() - buttonHeight) / m_maxValue;
              
              y = getHeight();
              if (m_histBarClassCounts[i] != null) {
                for (int j = 0; j < m_histBarClassCounts[i].numAttributes(); j++) {
                  sum = (int)(sum + m_histBarClassCounts[i].value(j));
                  y = (int)(y - Math.round(m_histBarClassCounts[i].value(j) * heightRatio));
                  
                  g.setColor((Color)m_colorList.elementAt(j));
                  g.fillRect(x, y, barWidth, (int)Math.round(m_histBarClassCounts[i].value(j) * heightRatio));
                  
                  g.setColor(Color.black);
                }
              }
              

              if (m_fm.stringWidth(Integer.toString(sum)) < intervalWidth) {
                g.drawString(Integer.toString(sum), x, y - 1);
              }
              

              x = x + barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F));
              

              sum = 0;
            }
            
          }
          else
          {
            float intervalWidth = getWidth() / m_histBarCounts.length;
            
            int barWidth;
            int barWidth;
            if (intervalWidth > 5.0F) {
              barWidth = (int)Math.floor(intervalWidth * 0.8F);
            } else {
              barWidth = 1;
            }
            

            x += (int)(Math.floor(intervalWidth * 0.1F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.1F));
            


            if (getWidth() - (m_histBarCounts.length * barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F)) * m_histBarCounts.length) > 2)
            {


              x += (getWidth() - (m_histBarCounts.length * barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F)) * m_histBarCounts.length)) / 2;
            }
            



            for (int i = 0; i < m_histBarCounts.length; i++)
            {

              float heightRatio = (getHeight() - m_fm.getHeight() - buttonHeight) / m_maxValue;
              
              y = getHeight() - Math.round(m_histBarCounts[i] * heightRatio);
              g.fillRect(x, y, barWidth, Math.round(m_histBarCounts[i] * heightRatio));
              


              if (m_fm.stringWidth(Integer.toString(m_histBarCounts[i])) < intervalWidth)
              {
                g.drawString(Integer.toString(m_histBarCounts[i]), x, y - 1);
              }
              

              x = x + barWidth + (int)(Math.floor(intervalWidth * 0.2F) < 1.0D ? 1.0D : Math.floor(intervalWidth * 0.2F));
            }
            
          }
          

        }
        else if ((m_as.numericStats != null) && ((m_histBarClassCounts != null) || (m_histBarCounts != null)))
        {


          int x = 0;int y = 0;
          


          if ((m_classIndex >= 0) && (m_data.attribute(m_classIndex).isNominal()))
          {


            int barWidth = (getWidth() - 6) / m_histBarClassCounts.length < 1 ? 1 : (getWidth() - 6) / m_histBarClassCounts.length;
            


            x = 3;
            

            if (getWidth() - (x + m_histBarClassCounts.length * barWidth) > 5)
            {







              x += (getWidth() - (x + m_histBarClassCounts.length * barWidth)) / 2;
            }
            

            for (int i = 0; i < m_histBarClassCounts.length; i++) {
              if (m_histBarClassCounts[i] != null)
              {

                float heightRatio = (getHeight() - m_fm.getHeight() - buttonHeight - 19.0F) / m_maxValue;
                
                y = getHeight() - 19;
                
                int sum = 0;
                for (int j = 0; j < m_histBarClassCounts[i].numValues(); j++) {
                  y = (int)(y - Math.round(m_histBarClassCounts[i].valueSparse(j) * heightRatio));
                  



                  g.setColor((Color)m_colorList.elementAt(m_histBarClassCounts[i].index(j)));
                  
                  if (barWidth > 1) {
                    g.fillRect(x, y, barWidth, (int)Math.round(m_histBarClassCounts[i].valueSparse(j) * heightRatio));


                  }
                  else if (m_histBarClassCounts[i].valueSparse(j) * heightRatio > 0.0D) {
                    g.drawLine(x, y, x, (int)(y + Math.round(m_histBarClassCounts[i].valueSparse(j) * heightRatio)));
                  }
                  g.setColor(Color.black);
                  sum = (int)(sum + m_histBarClassCounts[i].valueSparse(j));
                }
                
                if (m_fm.stringWidth(" " + Integer.toString(sum)) < barWidth) {
                  g.drawString(" " + Integer.toString(sum), x, y - 1);
                }
                x += barWidth;
              }
            }
            


            x = 3;
            if (getWidth() - (x + m_histBarClassCounts.length * barWidth) > 5)
            {
              x += (getWidth() - (x + m_histBarClassCounts.length * barWidth)) / 2;
            }
            
            g.drawLine(x, getHeight() - 17, barWidth == 1 ? x + barWidth * m_histBarClassCounts.length - 1 : x + barWidth * m_histBarClassCounts.length, getHeight() - 17);
            


            g.drawLine(x, getHeight() - 16, x, getHeight() - 12);
            
            g.drawString(Utils.doubleToString(m_as.numericStats.min, 2), x, getHeight() - 12 + m_fm.getHeight());
            

            g.drawLine(x + barWidth * m_histBarClassCounts.length / 2, getHeight() - 16, x + barWidth * m_histBarClassCounts.length / 2, getHeight() - 12);
            





            g.drawString(Utils.doubleToString(m_as.numericStats.max / 2.0D + m_as.numericStats.min / 2.0D, 2), x + barWidth * m_histBarClassCounts.length / 2 - m_fm.stringWidth(Utils.doubleToString(m_as.numericStats.max / 2.0D + m_as.numericStats.min / 2.0D, 2)) / 2, getHeight() - 12 + m_fm.getHeight());
            


            g.drawLine(barWidth == 1 ? x + barWidth * m_histBarClassCounts.length - 1 : x + barWidth * m_histBarClassCounts.length, getHeight() - 16, barWidth == 1 ? x + barWidth * m_histBarClassCounts.length - 1 : x + barWidth * m_histBarClassCounts.length, getHeight() - 12);
            




            g.drawString(Utils.doubleToString(m_as.numericStats.max, 2), barWidth == 1 ? x + barWidth * m_histBarClassCounts.length - m_fm.stringWidth(Utils.doubleToString(m_as.numericStats.max, 2)) - 1 : x + barWidth * m_histBarClassCounts.length - m_fm.stringWidth(Utils.doubleToString(m_as.numericStats.max, 2)), getHeight() - 12 + m_fm.getHeight());


          }
          else
          {


            int barWidth = (getWidth() - 6) / m_histBarCounts.length < 1 ? 1 : (getWidth() - 6) / m_histBarCounts.length;
            


            x = 3;
            if (getWidth() - (x + m_histBarCounts.length * barWidth) > 5) {
              x += (getWidth() - (x + m_histBarCounts.length * barWidth)) / 2;
            }
            
            for (int i = 0; i < m_histBarCounts.length; i++)
            {


              float heightRatio = (getHeight() - m_fm.getHeight() - buttonHeight - 19.0F) / m_maxValue;
              
              y = getHeight() - Math.round(m_histBarCounts[i] * heightRatio) - 19;
              



              if (barWidth > 1) {
                g.drawRect(x, y, barWidth, Math.round(m_histBarCounts[i] * heightRatio));
              }
              else if (m_histBarCounts[i] * heightRatio > 0.0F) {
                g.drawLine(x, y, x, y + Math.round(m_histBarCounts[i] * heightRatio));
              }
              if (m_fm.stringWidth(" " + Integer.toString(m_histBarCounts[i])) < barWidth)
              {
                g.drawString(" " + Integer.toString(m_histBarCounts[i]), x, y - 1);
              }
              x += barWidth;
            }
            

            x = 3;
            if (getWidth() - (x + m_histBarCounts.length * barWidth) > 5) {
              x += (getWidth() - (x + m_histBarCounts.length * barWidth)) / 2;
            }
            

            g.drawLine(x, getHeight() - 17, barWidth == 1 ? x + barWidth * m_histBarCounts.length - 1 : x + barWidth * m_histBarCounts.length, getHeight() - 17);
            


            g.drawLine(x, getHeight() - 16, x, getHeight() - 12);
            
            g.drawString(Utils.doubleToString(m_as.numericStats.min, 2), x, getHeight() - 12 + m_fm.getHeight());
            

            g.drawLine(x + barWidth * m_histBarCounts.length / 2, getHeight() - 16, x + barWidth * m_histBarCounts.length / 2, getHeight() - 12);
            


            g.drawString(Utils.doubleToString(m_as.numericStats.max / 2.0D + m_as.numericStats.min / 2.0D, 2), x + barWidth * m_histBarCounts.length / 2 - m_fm.stringWidth(Utils.doubleToString(m_as.numericStats.max / 2.0D + m_as.numericStats.min / 2.0D, 2)) / 2, getHeight() - 12 + m_fm.getHeight());
            


            g.drawLine(barWidth == 1 ? x + barWidth * m_histBarCounts.length - 1 : x + barWidth * m_histBarCounts.length, getHeight() - 16, barWidth == 1 ? x + barWidth * m_histBarCounts.length - 1 : x + barWidth * m_histBarCounts.length, getHeight() - 12);
            




            g.drawString(Utils.doubleToString(m_as.numericStats.max, 2), barWidth == 1 ? x + barWidth * m_histBarCounts.length - m_fm.stringWidth(Utils.doubleToString(m_as.numericStats.max, 2)) - 1 : x + barWidth * m_histBarCounts.length - m_fm.stringWidth(Utils.doubleToString(m_as.numericStats.max, 2)), getHeight() - 12 + m_fm.getHeight());

          }
          


        }
        else
        {

          g.clearRect(0, 0, getWidth(), getHeight());
          Messages.getInstance();Messages.getInstance();g.drawString(Messages.getString("AttributeVisualizationPanel_PaintComponent_G_DrawString_Text_First"), getWidth() / 2 - m_fm.stringWidth(Messages.getString("AttributeVisualizationPanel_PaintComponent_StringWidth_Text_First")) / 2, getHeight() / 2 - m_fm.getHeight() / 2);

        }
        

      }
      else if (m_displayCurrentAttribute) {
        g.clearRect(0, 0, getWidth(), getHeight());
        Messages.getInstance();Messages.getInstance();g.drawString(Messages.getString("AttributeVisualizationPanel_PaintComponent_G_DrawString_Text_Second"), getWidth() / 2 - m_fm.stringWidth(Messages.getString("AttributeVisualizationPanel_PaintComponent_StringWidth_Text_Second")) / 2, getHeight() / 2 - m_fm.getHeight() / 2);


      }
      else if (!m_displayCurrentAttribute) {
        g.clearRect(0, 0, getWidth(), getHeight());
        Messages.getInstance();Messages.getInstance();g.drawString(Messages.getString("AttributeVisualizationPanel_PaintComponent_G_DrawString_Text_Third"), getWidth() / 2 - m_fm.stringWidth(Messages.getString("AttributeVisualizationPanel_PaintComponent_StringWidth_Text_Third")) / 2, getHeight() / 2 - m_fm.getHeight() / 2);
      }
    }
  }
  







  public static void main(String[] args)
  {
    if (args.length != 3) {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AttributeVisualizationPanel_Main_JFrame_Text"));
      AttributeVisualizationPanel ap = new AttributeVisualizationPanel();
      try {
        Instances ins = new Instances(new FileReader(args[0]));
        ap.setInstances(ins);
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("AttributeVisualizationPanel_Main_Text_First") + args[0] + Messages.getString("AttributeVisualizationPanel_Main_Text_Second") + m_data.relationName() + Messages.getString("AttributeVisualizationPanel_Main_Text_Third") + m_data.numAttributes());
        

        ap.setAttribute(Integer.parseInt(args[1]));
      } catch (Exception ex) {
        ex.printStackTrace();System.exit(-1); }
      Messages.getInstance();System.out.println(Messages.getString("AttributeVisualizationPanel_Main_Text_Fourth"));
      for (int i = 0; i < m_data.numAttributes(); i++) {
        System.out.println(m_data.attribute(i).name());
      }
      jf.setSize(500, 300);
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(ap, "Center");
      jf.setDefaultCloseOperation(3);
      jf.setVisible(true);
    }
    else {
      Messages.getInstance();System.out.println(Messages.getString("AttributeVisualizationPanel_Main_Text_Fifth"));
    }
  }
}

package weka.gui.visualize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;



























public class AttributePanel
  extends JScrollPane
{
  private static final long serialVersionUID = 3533330317806757814L;
  protected Instances m_plotInstances = null;
  
  protected double m_maxC;
  
  protected double m_minC;
  
  protected int m_cIndex;
  
  protected int m_xIndex;
  
  protected int m_yIndex;
  
  protected FastVector m_colorList;
  protected Color[] m_DefaultColors = { Color.blue, Color.red, Color.green, Color.cyan, Color.pink, new Color(255, 0, 255), Color.orange, new Color(255, 0, 0), new Color(0, 255, 0), Color.white };
  













  protected Color m_backgroundColor = null;
  

  protected FastVector m_Listeners = new FastVector();
  



  protected int[] m_heights;
  


  protected JPanel m_span = null;
  


  protected Color m_barColour = Color.black;
  



  protected class AttributeSpacing
    extends JPanel
  {
    private static final long serialVersionUID = 7220615894321679898L;
    


    protected double m_maxVal;
    


    protected double m_minVal;
    


    protected Attribute m_attrib;
    


    protected int m_attribIndex;
    


    protected int[] m_cached;
    

    protected boolean[][] m_pointDrawn;
    

    protected int m_oldWidth = 56536;
    









    public AttributeSpacing(Attribute a, int aind)
    {
      m_attrib = a;
      m_attribIndex = aind;
      setBackground(m_barColour);
      setPreferredSize(new Dimension(0, 20));
      setMinimumSize(new Dimension(0, 20));
      m_cached = new int[m_plotInstances.numInstances()];
      


      double min = Double.POSITIVE_INFINITY;
      double max = Double.NEGATIVE_INFINITY;
      
      if (m_plotInstances.attribute(m_attribIndex).isNominal()) {
        m_minVal = 0.0D;
        m_maxVal = (m_plotInstances.attribute(m_attribIndex).numValues() - 1);
      } else {
        for (int i = 0; i < m_plotInstances.numInstances(); i++) {
          if (!m_plotInstances.instance(i).isMissing(m_attribIndex)) {
            double value = m_plotInstances.instance(i).value(m_attribIndex);
            if (value < min) {
              min = value;
            }
            if (value > max) {
              max = value;
            }
          }
        }
        m_minVal = min;m_maxVal = max;
        if (min == max) {
          m_maxVal += 0.05D;
          m_minVal -= 0.05D;
        }
      }
      
      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if ((e.getModifiers() & 0x10) == 16) {
            setX(m_attribIndex);
            if (m_Listeners.size() > 0) {
              for (int i = 0; i < m_Listeners.size(); i++) {
                AttributePanelListener l = (AttributePanelListener)m_Listeners.elementAt(i);
                
                l.attributeSelectionChange(new AttributePanelEvent(true, false, m_attribIndex));
              }
              
            }
          }
          else
          {
            setY(m_attribIndex);
            if (m_Listeners.size() > 0) {
              for (int i = 0; i < m_Listeners.size(); i++) {
                AttributePanelListener l = (AttributePanelListener)m_Listeners.elementAt(i);
                
                l.attributeSelectionChange(new AttributePanelEvent(false, true, m_attribIndex));
              }
            }
          }
        }
      });
    }
    





    private double convertToPanel(double val)
    {
      double temp = (val - m_minVal) / (m_maxVal - m_minVal);
      double temp2 = temp * (getWidth() - 10);
      
      return temp2 + 4.0D;
    }
    




    public void paintComponent(Graphics gx)
    {
      super.paintComponent(gx);
      
      int h = getWidth();
      if ((m_plotInstances != null) && (m_plotInstances.numAttributes() > 0) && (m_plotInstances.numInstances() > 0))
      {


        if (m_oldWidth != h) {
          m_pointDrawn = new boolean[h][20];
          for (int noa = 0; noa < m_plotInstances.numInstances(); noa++) {
            if ((!m_plotInstances.instance(noa).isMissing(m_attribIndex)) && (!m_plotInstances.instance(noa).isMissing(m_cIndex)))
            {
              m_cached[noa] = ((int)convertToPanel(m_plotInstances.instance(noa).value(m_attribIndex)));
              


              if (m_pointDrawn[(m_cached[noa] % h)][m_heights[noa]] != 0) {
                m_cached[noa] = 56536;
              }
              else {
                m_pointDrawn[(m_cached[noa] % h)][m_heights[noa]] = 1;
              }
            }
            else
            {
              m_cached[noa] = 56536;
            }
          }
          
          m_oldWidth = h;
        }
        
        if (m_plotInstances.attribute(m_cIndex).isNominal()) {
          for (int noa = 0; noa < m_plotInstances.numInstances(); noa++)
          {
            if (m_cached[noa] != 56536) {
              int xp = m_cached[noa];
              int yp = m_heights[noa];
              if (m_plotInstances.attribute(m_attribIndex).isNominal())
              {
                xp += (int)(Math.random() * 5.0D) - 2;
              }
              int ci = (int)m_plotInstances.instance(noa).value(m_cIndex);
              
              gx.setColor((Color)m_colorList.elementAt(ci % m_colorList.size()));
              
              gx.drawRect(xp, yp, 1, 1);
            }
            
          }
          
        } else {
          for (int noa = 0; noa < m_plotInstances.numInstances(); noa++) {
            if (m_cached[noa] != 56536)
            {
              double r = (m_plotInstances.instance(noa).value(m_cIndex) - m_minC) / (m_maxC - m_minC);
              

              r = r * 240.0D + 15.0D;
              
              gx.setColor(new Color((int)r, 150, (int)(255.0D - r)));
              
              int xp = m_cached[noa];
              int yp = m_heights[noa];
              if (m_plotInstances.attribute(m_attribIndex).isNominal())
              {
                xp += (int)(Math.random() * 5.0D) - 2;
              }
              gx.drawRect(xp, yp, 1, 1);
            }
          }
        }
      }
    }
  }
  


  private void setProperties()
  {
    if (VisualizeUtils.VISUALIZE_PROPERTIES != null) {
      String thisClass = getClass().getName();
      String barKey = thisClass + ".barColour";
      
      String barC = VisualizeUtils.VISUALIZE_PROPERTIES.getProperty(barKey);
      
      if (barC != null)
      {






        m_barColour = VisualizeUtils.processColour(barC, m_barColour);
      }
    }
  }
  
  public AttributePanel() {
    this(null);
  }
  


  public AttributePanel(Color background)
  {
    m_backgroundColor = background;
    
    setProperties();
    setBackground(Color.blue);
    setVerticalScrollBarPolicy(22);
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
  



  public void addAttributePanelListener(AttributePanelListener a)
  {
    m_Listeners.addElement(a);
  }
  







  public void setCindex(int c, double h, double l)
  {
    m_cIndex = c;
    m_maxC = h;
    m_minC = l;
    
    if (m_span != null) {
      if ((m_plotInstances.numAttributes() > 0) && (m_cIndex < m_plotInstances.numAttributes()))
      {
        if ((m_plotInstances.attribute(m_cIndex).isNominal()) && 
          (m_plotInstances.attribute(m_cIndex).numValues() > m_colorList.size()))
        {
          extendColourMap();
        }
      }
      
      repaint();
    }
  }
  





  public void setCindex(int c)
  {
    m_cIndex = c;
    


    if (m_span != null) {
      if ((m_cIndex < m_plotInstances.numAttributes()) && (m_plotInstances.attribute(m_cIndex).isNumeric()))
      {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        

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
        
        m_minC = min;m_maxC = max;
      }
      else if (m_plotInstances.attribute(m_cIndex).numValues() > m_colorList.size())
      {
        extendColourMap();
      }
      

      repaint();
    }
  }
  


  private void extendColourMap()
  {
    if (m_plotInstances.attribute(m_cIndex).isNominal()) {
      for (int i = m_colorList.size(); 
          i < m_plotInstances.attribute(m_cIndex).numValues(); 
          i++) {
        Color pc = m_DefaultColors[(i % 10)];
        int ija = i / 10;
        ija *= 2;
        for (int j = 0; j < ija; j++) {
          pc = pc.brighter();
        }
        
        if (m_backgroundColor != null) {
          pc = Plot2D.checkAgainstBackground(pc, m_backgroundColor);
        }
        
        m_colorList.addElement(pc);
      }
    }
  }
  



  public void setColours(FastVector cols)
  {
    m_colorList = cols;
  }
  
  protected void setDefaultColourList(Color[] list) {
    m_DefaultColors = list;
  }
  


  public void setInstances(Instances ins)
    throws Exception
  {
    if (ins.numAttributes() > 512) {
      Messages.getInstance();throw new Exception(Messages.getString("AttributePanel_SetInstances_Exception_Text"));
    }
    
    if (m_span == null) {
      m_span = new JPanel() {
        private static final long serialVersionUID = 7107576557995451922L;
        
        public void paintComponent(Graphics gx) {
          super.paintComponent(gx);
          gx.setColor(Color.red);
          if (m_yIndex != m_xIndex) {
            Messages.getInstance();gx.drawString(Messages.getString("AttributePanel_SetInstances_PaintComponent_DrawString_Text_First"), 5, m_xIndex * 20 + 16);
            Messages.getInstance();gx.drawString(Messages.getString("AttributePanel_SetInstances_PaintComponent_DrawString_Text_Second"), 5, m_yIndex * 20 + 16);
          }
          else {
            Messages.getInstance();gx.drawString(Messages.getString("AttributePanel_SetInstances_PaintComponent_DrawString_Text_Third"), 5, m_xIndex * 20 + 16);
          }
        }
      };
    }
    
    m_span.removeAll();
    m_plotInstances = ins;
    if ((ins.numInstances() > 0) && (ins.numAttributes() > 0)) {
      JPanel padder = new JPanel();
      JPanel padd2 = new JPanel();
      




      m_heights = new int[ins.numInstances()];
      
      m_cIndex = (ins.numAttributes() - 1);
      for (int noa = 0; noa < ins.numInstances(); noa++) {
        m_heights[noa] = ((int)(Math.random() * 19.0D));
      }
      m_span.setPreferredSize(new Dimension(m_span.getPreferredSize().width, (m_cIndex + 1) * 20));
      
      m_span.setMaximumSize(new Dimension(m_span.getMaximumSize().width, (m_cIndex + 1) * 20));
      


      GridBagLayout gb = new GridBagLayout();
      GridBagLayout gb2 = new GridBagLayout();
      GridBagConstraints constraints = new GridBagConstraints();
      


      padder.setLayout(gb);
      m_span.setLayout(gb2);
      anchor = 10;
      gridx = 0;gridy = 0;weightx = 5.0D;
      fill = 2;
      gridwidth = 1;gridheight = 1;
      insets = new Insets(0, 0, 0, 0);
      padder.add(m_span, constraints);
      gridx = 0;gridy = 1;weightx = 5.0D;
      fill = 1;
      gridwidth = 1;gridheight = 1;weighty = 5.0D;
      insets = new Insets(0, 0, 0, 0);
      padder.add(padd2, constraints);
      weighty = 0.0D;
      setViewportView(padder);
      


      anchor = 10;
      gridx = 0;gridy = 0;weightx = 5.0D;
      fill = 2;
      gridwidth = 1;gridheight = 1;weighty = 5.0D;
      insets = new Insets(2, 20, 2, 4);
      
      for (int noa = 0; noa < ins.numAttributes(); noa++) {
        AttributeSpacing tmp = new AttributeSpacing(ins.attribute(noa), noa);
        
        gridy = noa;
        m_span.add(tmp, constraints);
      }
    }
  }
  



  public void setX(int x)
  {
    if (m_span != null) {
      m_xIndex = x;
      m_span.repaint();
    }
  }
  



  public void setY(int y)
  {
    if (m_span != null) {
      m_yIndex = y;
      m_span.repaint();
    }
  }
  



  public static void main(String[] args)
  {
    try
    {
      if (args.length < 1) {
        Messages.getInstance();System.err.println(Messages.getString("AttributePanel_Main_Error_Text_First"));
        System.exit(1);
      }
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AttributePanel_Main_JFrame_Text"));
      
      jf.setSize(100, 100);
      jf.getContentPane().setLayout(new BorderLayout());
      AttributePanel p2 = new AttributePanel();
      p2.addAttributePanelListener(new AttributePanelListener() {
        public void attributeSelectionChange(AttributePanelEvent e) {
          if (m_xChange) {
            Messages.getInstance();System.err.println(Messages.getString("AttributePanel_Main_Error_Text_Second") + m_indexVal);
          } else {
            Messages.getInstance();System.err.println(Messages.getString("AttributePanel_Main_Error_Text_Third") + m_indexVal);
          }
        }
      });
      jf.getContentPane().add(p2, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      if (args.length >= 1) {
        Messages.getInstance();System.err.println(Messages.getString("AttributePanel_Main_Error_Text_Fourth") + args[0]);
        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances i = new Instances(r);
        i.setClassIndex(i.numAttributes() - 1);
        p2.setInstances(i);
      }
      if (args.length > 1) {
        p2.setCindex(Integer.parseInt(args[1]) - 1);
      } else {
        p2.setCindex(0);
      }
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

package weka.gui.visualize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;






























public class ClassPanel
  extends JPanel
{
  private static final long serialVersionUID = -7969401840501661430L;
  private boolean m_isEnabled = false;
  

  private boolean m_isNumeric = false;
  

  private final int m_spectrumHeight = 5;
  

  private double m_maxC;
  

  private double m_minC;
  

  private final int m_tickSize = 5;
  

  private FontMetrics m_labelMetrics = null;
  

  private Font m_labelFont = null;
  

  private int m_HorizontalPad = 0;
  

  private int m_precisionC;
  

  private int m_fieldWidthC;
  

  private int m_oldWidth = 56536;
  

  private Instances m_Instances = null;
  


  private int m_cIndex;
  


  private FastVector m_colorList;
  


  private FastVector m_Repainters = new FastVector();
  


  private FastVector m_ColourChangeListeners = new FastVector();
  

  protected Color[] m_DefaultColors = { Color.blue, Color.red, Color.green, Color.cyan, Color.pink, new Color(255, 0, 255), Color.orange, new Color(255, 0, 0), new Color(0, 255, 0), Color.white };
  













  protected Color m_backgroundColor = null;
  



  private class NomLabel
    extends JLabel
  {
    private static final long serialVersionUID = -4686613106474820655L;
    

    private int m_index = 0;
    




    public NomLabel(String name, int id)
    {
      super();
      m_index = id;
      
      addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e) {
          if ((e.getModifiers() & 0x10) == 16) {
            Color tmp = JColorChooser.showDialog(ClassPanel.this, "Select new Color", (Color)m_colorList.elementAt(m_index));
            


            if (tmp != null) {
              m_colorList.setElementAt(tmp, m_index);
              m_oldWidth = 56536;
              repaint();
              if (m_Repainters.size() > 0) {
                for (int i = 0; i < m_Repainters.size(); i++) {
                  ((Component)m_Repainters.elementAt(i)).repaint();
                }
              }
              
              if (m_ColourChangeListeners.size() > 0) {
                for (int i = 0; i < m_ColourChangeListeners.size(); i++) {
                  ((ActionListener)m_ColourChangeListeners.elementAt(i)).actionPerformed(new ActionEvent(this, 0, ""));
                }
              }
            }
          }
        }
      });
    }
  }
  
  public ClassPanel()
  {
    this(null);
  }
  
  public ClassPanel(Color background) {
    m_backgroundColor = background;
    

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
  




  public void addRepaintNotify(Component c)
  {
    m_Repainters.addElement(c);
  }
  





  public void addActionListener(ActionListener a)
  {
    m_ColourChangeListeners.addElement(a);
  }
  



  private void setFonts(Graphics gx)
  {
    if (m_labelMetrics == null) {
      m_labelFont = new Font("Monospaced", 0, 12);
      m_labelMetrics = gx.getFontMetrics(m_labelFont);
      int hf = m_labelMetrics.getAscent();
      if (getHeight() < 3 * hf) {
        m_labelFont = new Font("Monospaced", 0, 11);
        m_labelMetrics = gx.getFontMetrics(m_labelFont);
      }
    }
    gx.setFont(m_labelFont);
  }
  



  public void setOn(boolean e)
  {
    m_isEnabled = e;
  }
  



  public void setInstances(Instances insts)
  {
    m_Instances = insts;
  }
  



  public void setCindex(int cIndex)
  {
    if (m_Instances.numAttributes() > 0) {
      m_cIndex = cIndex;
      if (m_Instances.attribute(m_cIndex).isNumeric()) {
        setNumeric();
      } else {
        if (m_Instances.attribute(m_cIndex).numValues() > m_colorList.size()) {
          extendColourMap();
        }
        setNominal();
      }
    }
  }
  



  private void extendColourMap()
  {
    if (m_Instances.attribute(m_cIndex).isNominal()) {
      for (int i = m_colorList.size(); 
          i < m_Instances.attribute(m_cIndex).numValues(); 
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
  
  protected void setDefaultColourList(Color[] list) {
    m_DefaultColors = list;
  }
  



  public void setColours(FastVector cols)
  {
    m_colorList = cols;
  }
  


  protected void setNominal()
  {
    m_isNumeric = false;
    m_HorizontalPad = 0;
    setOn(true);
    m_oldWidth = 56536;
    
    repaint();
  }
  


  protected void setNumeric()
  {
    m_isNumeric = true;
    


    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    

    for (int i = 0; i < m_Instances.numInstances(); i++) {
      if (!m_Instances.instance(i).isMissing(m_cIndex)) {
        double value = m_Instances.instance(i).value(m_cIndex);
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
    
    int whole = (int)Math.abs(m_maxC);
    double decimal = Math.abs(m_maxC) - whole;
    
    int nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    


    m_precisionC = (decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_maxC)) / Math.log(10.0D)) + 2 : 1);
    


    if (m_precisionC > VisualizeUtils.MAX_PRECISION) {
      m_precisionC = 1;
    }
    
    String maxStringC = Utils.doubleToString(m_maxC, nondecimal + 1 + m_precisionC, m_precisionC);
    

    if (m_labelMetrics != null) {
      m_HorizontalPad = m_labelMetrics.stringWidth(maxStringC);
    }
    
    whole = (int)Math.abs(m_minC);
    decimal = Math.abs(m_minC) - whole;
    nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    


    m_precisionC = (decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_minC)) / Math.log(10.0D)) + 2 : 1);
    


    if (m_precisionC > VisualizeUtils.MAX_PRECISION) {
      m_precisionC = 1;
    }
    
    maxStringC = Utils.doubleToString(m_minC, nondecimal + 1 + m_precisionC, m_precisionC);
    

    if ((m_labelMetrics != null) && 
      (m_labelMetrics.stringWidth(maxStringC) > m_HorizontalPad)) {
      m_HorizontalPad = m_labelMetrics.stringWidth(maxStringC);
    }
    

    setOn(true);
    repaint();
  }
  



  protected void paintNominal(Graphics gx)
  {
    setFonts(gx);
    




    int numClasses = m_Instances.attribute(m_cIndex).numValues();
    
    int maxLabelLen = 0;
    int idx = 0;
    
    int w = getWidth();
    int hf = m_labelMetrics.getAscent();
    

    for (int i = 0; i < numClasses; i++) {
      if (m_Instances.attribute(m_cIndex).value(i).length() > maxLabelLen)
      {
        maxLabelLen = m_Instances.attribute(m_cIndex).value(i).length();
        
        idx = i;
      }
    }
    
    maxLabelLen = m_labelMetrics.stringWidth(m_Instances.attribute(m_cIndex).value(idx));
    
    int legendHeight;
    int legendHeight;
    if ((w - 2 * m_HorizontalPad) / (maxLabelLen + 5) >= numClasses) {
      legendHeight = 1;
    } else {
      legendHeight = 2;
    }
    
    int x = m_HorizontalPad;
    int y = 1 + hf;
    



    int numToDo = legendHeight == 1 ? numClasses : numClasses / 2;
    for (int i = 0; i < numToDo; i++)
    {
      gx.setColor((Color)m_colorList.elementAt(i));
      
      if (numToDo * maxLabelLen > w - m_HorizontalPad * 2)
      {
        String val = m_Instances.attribute(m_cIndex).value(i);
        
        int sw = m_labelMetrics.stringWidth(val);
        int rm = 0;
        
        if (sw > (w - m_HorizontalPad * 2) / numToDo) {
          int incr = sw / val.length();
          rm = (sw - (w - m_HorizontalPad * 2) / numToDo) / incr;
          if (rm <= 0) {
            rm = 0;
          }
          if (rm >= val.length()) {
            rm = val.length() - 1;
          }
          val = val.substring(0, val.length() - rm);
          sw = m_labelMetrics.stringWidth(val);
        }
        NomLabel jj = new NomLabel(val, i);
        jj.setFont(gx.getFont());
        
        jj.setSize(m_labelMetrics.stringWidth(jj.getText()), m_labelMetrics.getAscent() + 4);
        
        add(jj);
        jj.setLocation(x, y);
        jj.setForeground((Color)m_colorList.elementAt(i % m_colorList.size()));
        

        x += sw + 2;
      }
      else
      {
        NomLabel jj = new NomLabel(m_Instances.attribute(m_cIndex).value(i), i);
        
        jj.setFont(gx.getFont());
        
        jj.setSize(m_labelMetrics.stringWidth(jj.getText()), m_labelMetrics.getAscent() + 4);
        
        add(jj);
        jj.setLocation(x, y);
        jj.setForeground((Color)m_colorList.elementAt(i % m_colorList.size()));
        



        x += (w - m_HorizontalPad * 2) / numToDo;
      }
    }
    
    x = m_HorizontalPad;
    y = 1 + hf + 5 + hf;
    for (int i = numToDo; i < numClasses; i++)
    {
      gx.setColor((Color)m_colorList.elementAt(i));
      if ((numClasses - numToDo + 1) * maxLabelLen > w - m_HorizontalPad * 2)
      {

        String val = m_Instances.attribute(m_cIndex).value(i);
        
        int sw = m_labelMetrics.stringWidth(val);
        int rm = 0;
        
        if (sw > (w - m_HorizontalPad * 2) / (numClasses - numToDo + 1)) {
          int incr = sw / val.length();
          rm = (sw - (w - m_HorizontalPad * 2) / (numClasses - numToDo)) / incr;
          
          if (rm <= 0) {
            rm = 0;
          }
          if (rm >= val.length()) {
            rm = val.length() - 1;
          }
          val = val.substring(0, val.length() - rm);
          sw = m_labelMetrics.stringWidth(val);
        }
        
        NomLabel jj = new NomLabel(val, i);
        jj.setFont(gx.getFont());
        
        jj.setSize(m_labelMetrics.stringWidth(jj.getText()), m_labelMetrics.getAscent() + 4);
        

        add(jj);
        jj.setLocation(x, y);
        jj.setForeground((Color)m_colorList.elementAt(i % m_colorList.size()));
        

        x += sw + 2;
      }
      else
      {
        NomLabel jj = new NomLabel(m_Instances.attribute(m_cIndex).value(i), i);
        
        jj.setFont(gx.getFont());
        
        jj.setSize(m_labelMetrics.stringWidth(jj.getText()), m_labelMetrics.getAscent() + 4);
        
        add(jj);
        jj.setLocation(x, y);
        jj.setForeground((Color)m_colorList.elementAt(i % m_colorList.size()));
        

        x += (w - m_HorizontalPad * 2) / (numClasses - numToDo);
      }
    }
  }
  





  protected void paintNumeric(Graphics gx)
  {
    setFonts(gx);
    if (m_HorizontalPad == 0) {
      setCindex(m_cIndex);
    }
    
    int w = getWidth();
    double rs = 15.0D;
    double incr = 240.0D / (w - m_HorizontalPad * 2);
    int hf = m_labelMetrics.getAscent();
    
    for (int i = m_HorizontalPad; i < w - m_HorizontalPad; 
        i++) {
      Color c = new Color((int)rs, 150, (int)(255.0D - rs));
      gx.setColor(c);
      gx.drawLine(i, 0, i, 5);
      
      rs += incr;
    }
    
    int whole = (int)Math.abs(m_maxC);
    double decimal = Math.abs(m_maxC) - whole;
    
    int nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
    


    m_precisionC = (decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_maxC)) / Math.log(10.0D)) + 2 : 1);
    


    if (m_precisionC > VisualizeUtils.MAX_PRECISION) {
      m_precisionC = 1;
    }
    
    String maxStringC = Utils.doubleToString(m_maxC, nondecimal + 1 + m_precisionC, m_precisionC);
    



    int mswc = m_labelMetrics.stringWidth(maxStringC);
    int tmsc = mswc;
    if (w > 2 * tmsc) {
      gx.setColor(Color.black);
      gx.drawLine(m_HorizontalPad, 10, w - m_HorizontalPad, 10);
      



      gx.drawLine(w - m_HorizontalPad, 10, w - m_HorizontalPad, 15);
      



      gx.drawString(maxStringC, w - m_HorizontalPad - mswc / 2, 15 + hf);
      


      gx.drawLine(m_HorizontalPad, 10, m_HorizontalPad, 15);
      



      whole = (int)Math.abs(m_minC);
      decimal = Math.abs(m_minC) - whole;
      nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
      


      m_precisionC = (decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_minC)) / Math.log(10.0D)) + 2 : 1);
      



      if (m_precisionC > VisualizeUtils.MAX_PRECISION) {
        m_precisionC = 1;
      }
      
      maxStringC = Utils.doubleToString(m_minC, nondecimal + 1 + m_precisionC, m_precisionC);
      


      mswc = m_labelMetrics.stringWidth(maxStringC);
      gx.drawString(maxStringC, m_HorizontalPad - mswc / 2, 15 + hf);
      



      if (w > 3 * tmsc) {
        double mid = m_minC + (m_maxC - m_minC) / 2.0D;
        gx.drawLine(m_HorizontalPad + (w - 2 * m_HorizontalPad) / 2, 10, m_HorizontalPad + (w - 2 * m_HorizontalPad) / 2, 15);
        



        whole = (int)Math.abs(mid);
        decimal = Math.abs(mid) - whole;
        nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
        


        m_precisionC = (decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(mid)) / Math.log(10.0D)) + 2 : 1);
        


        if (m_precisionC > VisualizeUtils.MAX_PRECISION) {
          m_precisionC = 1;
        }
        
        maxStringC = Utils.doubleToString(mid, nondecimal + 1 + m_precisionC, m_precisionC);
        


        mswc = m_labelMetrics.stringWidth(maxStringC);
        gx.drawString(maxStringC, m_HorizontalPad + (w - 2 * m_HorizontalPad) / 2 - mswc / 2, 15 + hf);
      }
    }
  }
  





  public void paintComponent(Graphics gx)
  {
    super.paintComponent(gx);
    if (m_isEnabled) {
      if (m_isNumeric) {
        m_oldWidth = 56536;
        
        removeAll();
        paintNumeric(gx);
      }
      else if ((m_Instances != null) && (m_Instances.numInstances() > 0) && (m_Instances.numAttributes() > 0))
      {

        if (m_oldWidth != getWidth()) {
          removeAll();
          m_oldWidth = getWidth();
          paintNominal(gx);
        }
      }
    }
  }
  




  public static void main(String[] args)
  {
    try
    {
      if (args.length < 1) {
        Messages.getInstance();System.err.println(Messages.getString("ClassPanel_Main_Error_Text_First"));
        System.exit(1);
      }
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("ClassPanel_Main_JFrame_Text"));
      
      jf.setSize(500, 100);
      jf.getContentPane().setLayout(new BorderLayout());
      ClassPanel p2 = new ClassPanel();
      jf.getContentPane().add(p2, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      
      if (args.length >= 1) {
        Messages.getInstance();System.err.println(Messages.getString("ClassPanel_Main_Error_Text_Second") + args[0]);
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

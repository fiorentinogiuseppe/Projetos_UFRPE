package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import weka.core.Attribute;
import weka.core.Instances;
import weka.gui.AttributeVisualizationPanel;



























public class AttributeSummarizer
  extends DataVisualizer
{
  private static final long serialVersionUID = -294354961169372758L;
  protected int m_gridWidth = 4;
  



  protected int m_maxPlots = 100;
  



  protected int m_coloringIndex = -1;
  


  public AttributeSummarizer()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      appearanceFinal();
    }
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("AttributeSummarizer_GlobalInfo_Text");
  }
  




  public void setColoringIndex(int ci)
  {
    m_coloringIndex = ci;
  }
  




  public int getColoringIndex()
  {
    return m_coloringIndex;
  }
  




  public void setGridWidth(int gw)
  {
    if (gw > 0) {
      m_bcSupport.firePropertyChange("gridWidth", new Integer(m_gridWidth), new Integer(gw));
      
      m_gridWidth = gw;
    }
  }
  




  public int getGridWidth()
  {
    return m_gridWidth;
  }
  




  public void setMaxPlots(int mp)
  {
    if (mp > 0) {
      m_bcSupport.firePropertyChange("maxPlots", new Integer(m_maxPlots), new Integer(mp));
      
      m_maxPlots = mp;
    }
  }
  




  public int getMaxPlots()
  {
    return m_maxPlots;
  }
  





  public void setDesign(boolean design)
  {
    m_design = true;
    appearanceDesign();
  }
  
  protected void appearanceDesign() {
    removeAll();
    m_visual = new BeanVisual("AttributeSummarizer", "weka/gui/beans/icons/AttributeSummarizer.gif", "weka/gui/beans/icons/AttributeSummarizer_animated.gif");
    


    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  
  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
  }
  
  protected void setUpFinal() {
    removeAll();
    JScrollPane hp = makePanel();
    add(hp, "Center");
  }
  


  public void useDefaultVisual()
  {
    m_visual.loadIcons("weka/gui/beans/icons/DefaultDataVisualizer.gif", "weka/gui/beans/icons/DefaultDataVisualizer_animated.gif");
  }
  






  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_visualizeDataSet != null) {
      newVector.addElement("Show summaries");
    }
    return newVector.elements();
  }
  
  private JScrollPane makePanel() {
    String fontFamily = getFont().getFamily();
    Font newFont = new Font(fontFamily, 0, 10);
    JPanel hp = new JPanel();
    hp.setFont(newFont);
    int numPlots = Math.min(m_visualizeDataSet.numAttributes(), m_maxPlots);
    int gridHeight = numPlots / m_gridWidth;
    
    if (numPlots % m_gridWidth != 0) {
      gridHeight++;
    }
    hp.setLayout(new GridLayout(gridHeight, 4));
    for (int i = 0; i < numPlots; i++) {
      JPanel temp = new JPanel();
      temp.setLayout(new BorderLayout());
      temp.setBorder(BorderFactory.createTitledBorder(m_visualizeDataSet.attribute(i).name()));
      

      AttributeVisualizationPanel ap = new AttributeVisualizationPanel();
      ap.setInstances(m_visualizeDataSet);
      if ((m_coloringIndex < 0) && (m_visualizeDataSet.classIndex() >= 0)) {
        ap.setColoringIndex(m_visualizeDataSet.classIndex());
      } else {
        ap.setColoringIndex(m_coloringIndex);
      }
      temp.add(ap, "Center");
      ap.setAttribute(i);
      hp.add(temp);
    }
    
    Dimension d = new Dimension(830, gridHeight * 100);
    hp.setMinimumSize(d);
    hp.setMaximumSize(d);
    hp.setPreferredSize(d);
    
    JScrollPane scroller = new JScrollPane(hp);
    
    return scroller;
  }
  


















  public void setInstances(Instances inst)
    throws Exception
  {
    if (m_design) {
      Messages.getInstance();throw new Exception(Messages.getString("AttributeSummarizer_SetInstances_Exception_Text"));
    }
    m_visualizeDataSet = inst;
    setUpFinal();
  }
  





  public void performRequest(String request)
  {
    if (!m_design) {
      setUpFinal();
      return;
    }
    if (request.compareTo("Show summaries") == 0)
    {
      try {
        if (!m_framePoppedUp) {
          m_framePoppedUp = true;
          JScrollPane holderP = makePanel();
          
          Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("AttributeSummarizer_PerformRequest_Jf_JFrame_Text"));
          
          jf.setSize(800, 600);
          jf.getContentPane().setLayout(new BorderLayout());
          jf.getContentPane().add(holderP, "Center");
          jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              jf.dispose();
              m_framePoppedUp = false;
            }
          });
          jf.setVisible(true);
          m_popupFrame = jf;
        } else {
          m_popupFrame.toFront();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        m_framePoppedUp = false;
      }
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("AttributeSummarizer_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  
  public static void main(String[] args)
  {
    try {
      if (args.length != 1) {
        Messages.getInstance();System.err.println(Messages.getString("AttributeSummarizer_Main_Error_Text_First"));
        System.exit(1);
      }
      Reader r = new BufferedReader(new FileReader(args[0]));
      
      Instances inst = new Instances(r);
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      AttributeSummarizer as = new AttributeSummarizer();
      as.setInstances(inst);
      
      jf.getContentPane().add(as, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.setSize(830, 600);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

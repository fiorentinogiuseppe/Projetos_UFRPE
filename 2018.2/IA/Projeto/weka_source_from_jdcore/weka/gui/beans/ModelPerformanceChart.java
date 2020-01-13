package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import weka.core.Instances;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.VisualizePanel;






























public class ModelPerformanceChart
  extends JPanel
  implements ThresholdDataListener, VisualizableErrorListener, Visible, UserRequestAcceptor, Serializable, BeanContextChild
{
  private static final long serialVersionUID = -4602034200071195924L;
  protected BeanVisual m_visual;
  protected transient PlotData2D m_masterPlot;
  protected transient JFrame m_popupFrame;
  protected boolean m_framePoppedUp = false;
  



  protected boolean m_design;
  



  protected transient BeanContext m_beanContext = null;
  


  private transient VisualizePanel m_visPanel;
  

  protected BeanContextChildSupport m_bcSupport = new BeanContextChildSupport(this);
  
  public ModelPerformanceChart()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      appearanceFinal();
    }
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("ModelPerformanceChart_GlobalInfo_Text");
  }
  
  protected void appearanceDesign()
  {
    removeAll();
    m_visual = new BeanVisual("ModelPerformanceChart", "weka/gui/beans/icons/ModelPerformanceChart.gif", "weka/gui/beans/icons/ModelPerformanceChart_animated.gif");
    

    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  
  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }
  
  protected void setUpFinal() {
    if (m_visPanel == null) {
      m_visPanel = new VisualizePanel();
    }
    add(m_visPanel, "Center");
  }
  




  public synchronized void acceptDataSet(ThresholdDataEvent e)
  {
    if (!GraphicsEnvironment.isHeadless()) {
      if (m_visPanel == null) {
        m_visPanel = new VisualizePanel();
      }
      if (m_masterPlot == null) {
        m_masterPlot = e.getDataSet();
      }
      try
      {
        if (!m_masterPlot.getPlotInstances().relationName().equals(e.getDataSet().getPlotInstances().relationName()))
        {


          m_masterPlot = e.getDataSet();
          m_visPanel.setMasterPlot(m_masterPlot);
          m_visPanel.validate();
          m_visPanel.repaint();
        }
        else {
          m_visPanel.addPlot(e.getDataSet());
          m_visPanel.validate();
          m_visPanel.repaint();
        }
        m_visPanel.setXIndex(4);
        m_visPanel.setYIndex(5);
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("ModelPerformanceChart_AcceptDataSet_Error_Text"));
        
        ex.printStackTrace();
      }
    }
  }
  




  public synchronized void acceptDataSet(VisualizableErrorEvent e)
  {
    if (!GraphicsEnvironment.isHeadless()) {
      if (m_visPanel == null) {
        m_visPanel = new VisualizePanel();
      }
      
      m_masterPlot = e.getDataSet();
      try
      {
        m_visPanel.setMasterPlot(m_masterPlot);
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("ModelPerformanceChart_AcceptDataSet_Error_Text"));
        
        ex.printStackTrace();
      }
      m_visPanel.validate();
      m_visPanel.repaint();
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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultDataVisualizer.gif", "weka/gui/beans/icons/DefaultDataVisualizer_animated.gif");
  }
  





  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_masterPlot != null) {
      newVector.addElement("Show chart");
      newVector.addElement("?Clear all plots");
    }
    return newVector.elements();
  }
  






  public void addPropertyChangeListener(String name, PropertyChangeListener pcl)
  {
    m_bcSupport.addPropertyChangeListener(name, pcl);
  }
  







  public void removePropertyChangeListener(String name, PropertyChangeListener pcl)
  {
    m_bcSupport.removePropertyChangeListener(name, pcl);
  }
  





  public void addVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.addVetoableChangeListener(name, vcl);
  }
  






  public void removeVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.removeVetoableChangeListener(name, vcl);
  }
  




  public void setBeanContext(BeanContext bc)
  {
    m_beanContext = bc;
    m_design = m_beanContext.isDesignTime();
    if (m_design) {
      appearanceDesign();
    } else {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      
      if (!GraphicsEnvironment.isHeadless()) {
        appearanceFinal();
      }
    }
  }
  




  public BeanContext getBeanContext()
  {
    return m_beanContext;
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Show chart") == 0)
    {
      try {
        if (!m_framePoppedUp) {
          m_framePoppedUp = true;
          
          Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("ModelPerformanceChart_PerformRequest_Jf_JFRame_Text"));
          

          jf.setSize(800, 600);
          jf.getContentPane().setLayout(new BorderLayout());
          jf.getContentPane().add(m_visPanel, "Center");
          jf.addWindowListener(new WindowAdapter()
          {
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
    } else if (request.equals("Clear all plots")) {
      m_visPanel.removeAllPlots();
      m_visPanel.validate();
      m_visPanel.repaint();
      m_visPanel = null;
      m_masterPlot = null;
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("ModelPerformanceChart_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  



  public static void main(String[] args)
  {
    try
    {
      if (args.length != 1) {
        Messages.getInstance();System.err.println(Messages.getString("ModelPerformanceChart_Main_Error_Text"));
        
        System.exit(1);
      }
      Reader r = new BufferedReader(new FileReader(args[0]));
      
      Instances inst = new Instances(r);
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      ModelPerformanceChart as = new ModelPerformanceChart();
      PlotData2D pd = new PlotData2D(inst);
      pd.setPlotName(inst.relationName());
      ThresholdDataEvent roc = new ThresholdDataEvent(as, pd);
      as.acceptDataSet(roc);
      
      jf.getContentPane().add(as, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.setSize(800, 600);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

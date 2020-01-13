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





























public class DataVisualizer
  extends JPanel
  implements DataSourceListener, TrainingSetListener, TestSetListener, Visible, UserRequestAcceptor, Serializable, BeanContextChild
{
  private static final long serialVersionUID = 1949062132560159028L;
  protected BeanVisual m_visual;
  protected transient Instances m_visualizeDataSet;
  protected transient JFrame m_popupFrame;
  protected boolean m_framePoppedUp = false;
  



  protected boolean m_design;
  



  protected transient BeanContext m_beanContext = null;
  


  private VisualizePanel m_visPanel;
  

  private Vector m_dataSetListeners = new Vector();
  



  protected BeanContextChildSupport m_bcSupport = new BeanContextChildSupport(this);
  
  public DataVisualizer()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      appearanceFinal();
    }
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("DataVisualizer_GlobalInfo_Text");
  }
  
  protected void appearanceDesign() {
    m_visPanel = null;
    removeAll();
    m_visual = new BeanVisual("DataVisualizer", "weka/gui/beans/icons/DefaultDataVisualizer.gif", "weka/gui/beans/icons/DefaultDataVisualizer_animated.gif");
    


    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  
  protected void appearanceFinal() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    

    removeAll();
    if (!GraphicsEnvironment.isHeadless()) {
      setLayout(new BorderLayout());
      setUpFinal();
    }
  }
  
  protected void setUpFinal() {
    if (m_visPanel == null) {
      m_visPanel = new VisualizePanel();
    }
    add(m_visPanel, "Center");
  }
  




  public void acceptTrainingSet(TrainingSetEvent e)
  {
    Instances trainingSet = e.getTrainingSet();
    DataSetEvent dse = new DataSetEvent(this, trainingSet);
    acceptDataSet(dse);
  }
  




  public void acceptTestSet(TestSetEvent e)
  {
    Instances testSet = e.getTestSet();
    DataSetEvent dse = new DataSetEvent(this, testSet);
    acceptDataSet(dse);
  }
  





  public synchronized void acceptDataSet(DataSetEvent e)
  {
    if (e.isStructureOnly()) {
      return;
    }
    m_visualizeDataSet = new Instances(e.getDataSet());
    if (m_visualizeDataSet.classIndex() < 0) {
      m_visualizeDataSet.setClassIndex(m_visualizeDataSet.numAttributes() - 1);
    }
    if (!m_design) {
      try {
        setInstances(m_visualizeDataSet);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    

    notifyDataSetListeners(e);
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
    if (m_visualizeDataSet != null) {
      newVector.addElement("Show plot");
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
  





  public void setInstances(Instances inst)
    throws Exception
  {
    if (m_design) {
      Messages.getInstance();throw new Exception(Messages.getString("DataVisualizer_SetInstances_Exception_Text"));
    }
    m_visualizeDataSet = inst;
    PlotData2D pd1 = new PlotData2D(m_visualizeDataSet);
    pd1.setPlotName(m_visualizeDataSet.relationName());
    try {
      m_visPanel.setMasterPlot(pd1);
    } catch (Exception ex) {
      Messages.getInstance();System.err.println(Messages.getString("DataVisualizer_SetInstances_Error_Text"));
      ex.printStackTrace();
    }
  }
  


  private void notifyDataSetListeners(DataSetEvent ge)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_dataSetListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((DataSourceListener)l.elementAt(i)).acceptDataSet(ge);
      }
    }
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Show plot") == 0)
    {
      try {
        if (!m_framePoppedUp) {
          m_framePoppedUp = true;
          VisualizePanel vis = new VisualizePanel();
          PlotData2D pd1 = new PlotData2D(m_visualizeDataSet);
          pd1.setPlotName(m_visualizeDataSet.relationName());
          try {
            vis.setMasterPlot(pd1);
          } catch (Exception ex) {
            Messages.getInstance();System.err.println(Messages.getString("DataVisualizer_PerformRequest_Error_Text"));
            ex.printStackTrace();
          }
          Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("DataVisualizer_PerformRequest_Jf_JFrame_Text"));
          jf.setSize(800, 600);
          jf.getContentPane().setLayout(new BorderLayout());
          jf.getContentPane().add(vis, "Center");
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
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("DataVisualizer_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  





  public synchronized void addDataSourceListener(DataSourceListener dsl)
  {
    m_dataSetListeners.addElement(dsl);
  }
  




  public synchronized void removeDataSourceListener(DataSourceListener dsl)
  {
    m_dataSetListeners.remove(dsl);
  }
  
  public static void main(String[] args) {
    try {
      if (args.length != 1) {
        Messages.getInstance();System.err.println(Messages.getString("DataVisualizer_Main_Error_Text_First"));
        System.exit(1);
      }
      Reader r = new BufferedReader(new FileReader(args[0]));
      
      Instances inst = new Instances(r);
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      DataVisualizer as = new DataVisualizer();
      as.setInstances(inst);
      
      jf.getContentPane().add(as, "Center");
      jf.addWindowListener(new WindowAdapter() {
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

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import weka.core.FastVector;
import weka.gui.ResultHistoryPanel;
import weka.gui.ResultHistoryPanel.RMouseAdapter;
import weka.gui.graphvisualizer.BIFFormatException;
import weka.gui.graphvisualizer.GraphVisualizer;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;































public class GraphViewer
  extends JPanel
  implements Visible, GraphListener, UserRequestAcceptor, Serializable, BeanContextChild
{
  private static final long serialVersionUID = -5183121972114900617L;
  protected BeanVisual m_visual;
  private transient JFrame m_resultsFrame = null;
  


  protected transient ResultHistoryPanel m_history;
  

  protected transient BeanContext m_beanContext = null;
  



  protected BeanContextChildSupport m_bcSupport = new BeanContextChildSupport(this);
  



  protected boolean m_design;
  




  public GraphViewer()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      appearanceFinal();
    }
  }
  
  protected void appearanceDesign() {
    setUpResultHistory();
    removeAll();
    m_visual = new BeanVisual("GraphViewer", "weka/gui/beans/icons/DefaultGraph.gif", "weka/gui/beans/icons/DefaultGraph_animated.gif");
    


    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  
  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }
  
  protected void setUpFinal() {
    setUpResultHistory();
    add(m_history, "Center");
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("GraphViewer_GlobalInfo_Text");
  }
  
  private void setUpResultHistory() {
    if (m_history == null) {
      m_history = new ResultHistoryPanel(null);
    }
    Messages.getInstance();m_history.setBorder(BorderFactory.createTitledBorder(Messages.getString("GraphViewer_SetUpResultHistory_History_SetBorder_BorderFactory_CreateTitledBorder_Text")));
    m_history.setHandleRightClicks(false);
    m_history.getList().addMouseListener(new ResultHistoryPanel.RMouseAdapter()
    {
      private static final long serialVersionUID = -4984130887963944249L;
      
      public void mouseClicked(MouseEvent e)
      {
        int index = m_history.getList().locationToIndex(e.getPoint());
        if (index != -1) {
          String name = m_history.getNameAtIndex(index);
          GraphViewer.this.doPopup(name);
        }
      }
    });
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
  






  public void addVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.addVetoableChangeListener(name, vcl);
  }
  






  public void removeVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.removeVetoableChangeListener(name, vcl);
  }
  





  public synchronized void acceptGraph(GraphEvent e)
  {
    FastVector graphInfo = new FastVector();
    
    if (m_history == null) {
      setUpResultHistory();
    }
    String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
    
    name = name + e.getGraphTitle();
    graphInfo.addElement(new Integer(e.getGraphType()));
    graphInfo.addElement(e.getGraphString());
    m_history.addResult(name, new StringBuffer());
    m_history.addObject(name, graphInfo);
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
    m_visual.loadIcons("weka/gui/beans/icons/DefaultGraph.gif", "weka/gui/beans/icons/DefaultGraph_animated.gif");
  }
  




  public void showResults()
  {
    if (m_resultsFrame == null) {
      if (m_history == null) {
        setUpResultHistory();
      }
      Messages.getInstance();m_resultsFrame = new JFrame(Messages.getString("GraphViewer_ShowResult_ResultsFrame_JFrame_Text"));
      m_resultsFrame.getContentPane().setLayout(new BorderLayout());
      m_resultsFrame.getContentPane().add(m_history, "Center");
      m_resultsFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          m_resultsFrame.dispose();
          m_resultsFrame = null;
        }
      });
      m_resultsFrame.pack();
      m_resultsFrame.setVisible(true);
    } else {
      m_resultsFrame.toFront();
    }
  }
  




  private void doPopup(String name)
  {
    FastVector graph = (FastVector)m_history.getNamedObject(name);
    int grphType = ((Integer)graph.firstElement()).intValue();
    String grphString = (String)graph.lastElement();
    
    if (grphType == 1) {
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("GraphViewer_DoPopup_Jf_JFrame_Text_First") + name);
      
      jf.setSize(500, 400);
      jf.getContentPane().setLayout(new BorderLayout());
      TreeVisualizer tv = new TreeVisualizer(null, grphString, new PlaceNode2());
      


      jf.getContentPane().add(tv, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          jf.dispose();
        }
        
      });
      jf.setVisible(true);
    }
    if (grphType == 2) {
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("GraphViewer_DoPopup_Jf_JFrame_Text_Second") + name);
      
      jf.setSize(500, 400);
      jf.getContentPane().setLayout(new BorderLayout());
      GraphVisualizer gv = new GraphVisualizer();
      try
      {
        gv.readBIF(grphString);
      }
      catch (BIFFormatException be) {
        Messages.getInstance();System.err.println(Messages.getString("GraphViewer_DoPopup_Error_Text"));be.printStackTrace();
      }
      gv.layoutGraph();
      jf.getContentPane().add(gv, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          jf.dispose();
        }
        
      });
      jf.setVisible(true);
    }
  }
  




  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    newVector.addElement("Show results");
    
    return newVector.elements();
  }
  






  public void performRequest(String request)
  {
    if (request.compareTo("Show results") == 0) {
      showResults();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("GraphViewer_PerformRequest_IllegalArgumentException_Text"));
    }
  }
}

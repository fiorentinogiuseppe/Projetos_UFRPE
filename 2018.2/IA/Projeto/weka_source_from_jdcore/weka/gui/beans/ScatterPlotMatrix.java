package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.JFrame;
import weka.core.Instances;
import weka.gui.visualize.MatrixPanel;























public class ScatterPlotMatrix
  extends DataVisualizer
{
  private static final long serialVersionUID = -657856527563507491L;
  protected MatrixPanel m_matrixPanel;
  
  public ScatterPlotMatrix()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      appearanceFinal();
    }
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("ScatterPlotMatrix_GlobalInfo_Text");
  }
  
  protected void appearanceDesign() {
    m_matrixPanel = null;
    removeAll();
    m_visual = new BeanVisual("ScatterPlotMatrix", "weka/gui/beans/icons/ScatterPlotMatrix.gif", "weka/gui/beans/icons/ScatterPlotMatrix_animated.gif");
    


    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  
  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }
  
  protected void setUpFinal() {
    if (m_matrixPanel == null) {
      m_matrixPanel = new MatrixPanel();
    }
    add(m_matrixPanel, "Center");
  }
  





  public void setInstances(Instances inst)
    throws Exception
  {
    if (m_design) {
      Messages.getInstance();throw new Exception(Messages.getString("ScatterPlotMatrix_SetInstances_Exception_Text"));
    }
    m_visualizeDataSet = inst;
    m_matrixPanel.setInstances(m_visualizeDataSet);
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Show plot") == 0)
    {
      try {
        if (!m_framePoppedUp) {
          m_framePoppedUp = true;
          MatrixPanel vis = new MatrixPanel();
          vis.setInstances(m_visualizeDataSet);
          
          Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("ScatterPlotMatrix_PerformRequest_Jf_JFrame_Text"));
          
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
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("ScatterPlotMatrix_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  
  public static void main(String[] args)
  {
    try {
      if (args.length != 1) {
        Messages.getInstance();System.err.println(Messages.getString("ScatterPlotMatrix_Main_Error_Text"));
        System.exit(1);
      }
      Reader r = new BufferedReader(new FileReader(args[0]));
      
      Instances inst = new Instances(r);
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      ScatterPlotMatrix as = new ScatterPlotMatrix();
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

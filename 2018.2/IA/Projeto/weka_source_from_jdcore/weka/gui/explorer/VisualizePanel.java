package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.JFrame;
import weka.core.Instances;
import weka.gui.visualize.MatrixPanel;


























public class VisualizePanel
  extends MatrixPanel
  implements Explorer.ExplorerPanel
{
  private static final long serialVersionUID = 6084015036853918846L;
  protected Explorer m_Explorer = null;
  


  public VisualizePanel() {}
  

  public void setExplorer(Explorer parent)
  {
    m_Explorer = parent;
  }
  




  public Explorer getExplorer()
  {
    return m_Explorer;
  }
  




  public String getTabTitle()
  {
    Messages.getInstance();return Messages.getString("VisualizePanel_GetTabTitle_Text");
  }
  




  public String getTabTitleToolTip()
  {
    Messages.getInstance();return Messages.getString("VisualizePanel_GetTabTitleToolTip_Text");
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("VisualizePanel_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      VisualizePanel sp = new VisualizePanel();
      jf.getContentPane().add(sp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setSize(800, 600);
      jf.setVisible(true);
      if (args.length == 1) {
        Messages.getInstance();System.err.println(Messages.getString("VisualizePanel_Main_Error_Text") + args[0]);
        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances i = new Instances(r);
        sp.setInstances(i);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

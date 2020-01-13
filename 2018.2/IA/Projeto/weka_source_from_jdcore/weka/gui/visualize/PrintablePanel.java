package weka.gui.visualize;

import java.util.Hashtable;
import javax.swing.JPanel;






































public class PrintablePanel
  extends JPanel
  implements PrintableHandler
{
  private static final long serialVersionUID = 6281532227633417538L;
  protected PrintableComponent m_Printer = null;
  



  public PrintablePanel()
  {
    m_Printer = new PrintableComponent(this);
  }
  






  public Hashtable getWriters()
  {
    return m_Printer.getWriters();
  }
  






  public JComponentWriter getWriter(String name)
  {
    return m_Printer.getWriter(name);
  }
  


  public void setSaveDialogTitle(String title)
  {
    m_Printer.setSaveDialogTitle(title);
  }
  


  public String getSaveDialogTitle()
  {
    return m_Printer.getSaveDialogTitle();
  }
  




  public void setScale(double x, double y)
  {
    m_Printer.setScale(x, y);
  }
  


  public double getXScale()
  {
    return m_Printer.getXScale();
  }
  


  public double getYScale()
  {
    return m_Printer.getYScale();
  }
  


  public void saveComponent()
  {
    m_Printer.saveComponent();
  }
}

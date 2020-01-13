package weka.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;





































public class SimpleCLI
  extends Frame
{
  static final long serialVersionUID = -50661410800566036L;
  
  public SimpleCLI()
    throws Exception
  {
    Messages.getInstance();setTitle(Messages.getString("SimpleCL_SetTitle_Text"));
    setLayout(new BorderLayout());
    add(new SimpleCLIPanel());
    pack();
    setSize(600, 500);
    setVisible(true);
  }
  




  public static void main(String[] args)
  {
    try
    {
      SimpleCLI frame = new SimpleCLI();
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent param1) {
          Messages.getInstance();System.err.println(Messages.getString("SimpleCL_Main_Error_Text"));
          val$frame.dispose();
        }
      });
      frame.setVisible(true);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.exit(0);
    }
  }
}

package weka.gui.streams;

import java.awt.Color;
import java.io.PrintStream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weka.core.Instance;
import weka.core.Instances;































public class InstanceCounter
  extends JPanel
  implements InstanceListener
{
  private static final long serialVersionUID = -6084967152645935934L;
  private JLabel m_Count_Lab;
  private int m_Count;
  private boolean m_Debug;
  
  public void input(Instance instance)
    throws Exception
  {
    if (m_Debug) {
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("InstanceCounter_Input_Error_Text_First") + instance + Messages.getString("InstanceCounter_Input_Error_Text_Second"));
    }
    m_Count += 1;
    Messages.getInstance();m_Count_Lab.setText("" + m_Count + Messages.getString("InstanceCounter_Input_Count_Lab_SetText_Text_Second"));
    repaint();
  }
  
  public void inputFormat(Instances instanceInfo)
  {
    if (m_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceCounter_InputFormat_Error_Text_First"));
    }
    Instances inputInstances = new Instances(instanceInfo, 0);
    m_Count = 0;
    Messages.getInstance();m_Count_Lab.setText("" + m_Count + Messages.getString("InstanceCounter_InputFormat_Count_Lab_SetText_Text_Second"));
  }
  
  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  
  public boolean getDebug()
  {
    return m_Debug;
  }
  
  public InstanceCounter()
  {
    m_Count = 0;
    Messages.getInstance();m_Count_Lab = new JLabel(Messages.getString("InstanceCounter_Count_Lab_JLabel_Text"));
    add(m_Count_Lab);
    
    setBackground(Color.lightGray);
  }
  
  public void instanceProduced(InstanceEvent e)
  {
    Object source = e.getSource();
    if ((source instanceof InstanceProducer)) {
      try {
        InstanceProducer a = (InstanceProducer)source;
        switch (e.getID()) {
        case 1: 
          inputFormat(a.outputFormat());
          break;
        case 2: 
          input(a.outputPeek());
          break;
        case 3: 
          if (m_Debug) {
            Messages.getInstance();System.err.println(Messages.getString("InstanceCounter_InstanceProduced_InstanceEventBATCH_FINISHED_Error_Text"));
          }
          break;
        default:  Messages.getInstance();System.err.println(Messages.getString("InstanceCounter_InstanceProduced_InstanceEventDEFAULT_Error_Text"));
        }
      }
      catch (Exception ex) {
        System.err.println(ex.getMessage());
      }
    } else {
      Messages.getInstance();System.err.println(Messages.getString("InstanceCounter_InstanceProduced_Error_Text"));
    }
  }
}

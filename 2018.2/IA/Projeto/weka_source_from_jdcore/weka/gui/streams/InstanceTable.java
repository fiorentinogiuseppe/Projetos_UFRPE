package weka.gui.streams;

import java.awt.BorderLayout;
import java.io.PrintStream;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
































public class InstanceTable
  extends JPanel
  implements InstanceListener
{
  private static final long serialVersionUID = -2462533698100834803L;
  private JTable m_InstanceTable;
  private boolean m_Debug;
  private boolean m_Clear;
  private String m_UpdateString;
  private Instances m_Instances;
  
  public void inputFormat(Instances instanceInfo)
  {
    if (m_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceTable_InputFormat_Error_Text") + instanceInfo.toString());
    }
    
    m_Instances = instanceInfo;
  }
  
  public void input(Instance instance) throws Exception
  {
    if (m_Debug) {
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("InstanceTable_Input_Error_Text_First") + instance + Messages.getString("InstanceTable_Input_Error_Text_Second"));
    }
    m_Instances.add(instance);
  }
  
  public void batchFinished()
  {
    TableModel newModel = new AbstractTableModel() {
      private static final long serialVersionUID = 5447106383000555291L;
      
      public String getColumnName(int col) {
        return m_Instances.attribute(col).name();
      }
      
      public Class getColumnClass(int col) { return "".getClass(); }
      
      public int getColumnCount() {
        return m_Instances.numAttributes();
      }
      
      public int getRowCount() { return m_Instances.numInstances(); }
      
      public Object getValueAt(int row, int col) {
        return new String(m_Instances.instance(row).toString(col));
      }
    };
    m_InstanceTable.setModel(newModel);
    if (m_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceTable_BatchFinished_Error_Text"));
    }
  }
  
  public InstanceTable()
  {
    setLayout(new BorderLayout());
    m_InstanceTable = new JTable();
    add("Center", new JScrollPane(m_InstanceTable));
  }
  
  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  
  public boolean getDebug()
  {
    return m_Debug;
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
          batchFinished();
          break;
        default: 
          Messages.getInstance();System.err.println(Messages.getString("InstanceTable_InstanceProduced_InstanceEventDEFAULT_Error_Text"));
        }
      }
      catch (Exception ex) {
        System.err.println(ex.getMessage());
      }
    } else {
      Messages.getInstance();System.err.println(Messages.getString("InstanceTable_InstanceProduced_Error_Text"));
    }
  }
}

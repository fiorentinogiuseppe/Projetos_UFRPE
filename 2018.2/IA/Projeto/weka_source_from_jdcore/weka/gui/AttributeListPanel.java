package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import weka.core.Attribute;
import weka.core.Instances;




































public class AttributeListPanel
  extends JPanel
{
  private static final long serialVersionUID = -2030706987910400362L;
  
  class AttributeTableModel
    extends AbstractTableModel
  {
    private static final long serialVersionUID = -7345701953670327707L;
    protected Instances m_Instances;
    
    public AttributeTableModel(Instances instances)
    {
      setInstances(instances);
    }
    





    public void setInstances(Instances instances)
    {
      m_Instances = instances;
    }
    





    public int getRowCount()
    {
      return m_Instances.numAttributes();
    }
    





    public int getColumnCount()
    {
      return 2;
    }
    







    public Object getValueAt(int row, int column)
    {
      switch (column) {
      case 0: 
        return new Integer(row + 1);
      case 1: 
        return m_Instances.attribute(row).name();
      }
      return null;
    }
    







    public String getColumnName(int column)
    {
      switch (column) {
      case 0: 
        Messages.getInstance();return new String(Messages.getString("AttributeListPanel_getColumnName_Number_Text"));
      case 1: 
        Messages.getInstance();return new String(Messages.getString("AttributeListPanel_getColumnName_Name_Text"));
      }
      return null;
    }
    






    public Class getColumnClass(int col)
    {
      return getValueAt(0, col).getClass();
    }
    







    public boolean isCellEditable(int row, int col)
    {
      return false;
    }
  }
  

  protected JTable m_Table = new JTable();
  


  protected AttributeTableModel m_Model;
  


  public AttributeListPanel()
  {
    m_Table.setSelectionMode(0);
    m_Table.setColumnSelectionAllowed(false);
    m_Table.setPreferredScrollableViewportSize(new Dimension(250, 150));
    
    setLayout(new BorderLayout());
    add(new JScrollPane(m_Table), "Center");
  }
  





  public void setInstances(Instances newInstances)
  {
    if (m_Model == null) {
      m_Model = new AttributeTableModel(newInstances);
      m_Table.setModel(m_Model);
      TableColumnModel tcm = m_Table.getColumnModel();
      tcm.getColumn(0).setMaxWidth(60);
      tcm.getColumn(1).setMinWidth(100);
    } else {
      m_Model.setInstances(newInstances);
    }
    m_Table.sizeColumnsToFit(-1);
    m_Table.revalidate();
    m_Table.repaint();
  }
  





  public ListSelectionModel getSelectionModel()
  {
    return m_Table.getSelectionModel();
  }
  




  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        Messages.getInstance();throw new Exception(Messages.getString("AttributeListPanel_Main_Error_Text"));
      }
      Instances i = new Instances(new BufferedReader(new FileReader(args[0])));
      
      AttributeListPanel asp = new AttributeListPanel();
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AttributeListPanel_Main_AttributeListPanel_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(asp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      asp.setInstances(i);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import weka.core.Attribute;
import weka.core.Instances;









































public class AttributeSelectionPanel
  extends JPanel
{
  private static final long serialVersionUID = 627131485290359194L;
  protected JButton m_IncludeAll;
  protected JButton m_RemoveAll;
  protected JButton m_Invert;
  protected JButton m_Pattern;
  protected JTable m_Table;
  protected AttributeTableModel m_Model;
  protected String m_PatternRegEx;
  
  class AttributeTableModel
    extends AbstractTableModel
  {
    private static final long serialVersionUID = -4152987434024338064L;
    protected Instances m_Instances;
    protected boolean[] m_Selected;
    
    public AttributeTableModel(Instances instances)
    {
      setInstances(instances);
    }
    





    public void setInstances(Instances instances)
    {
      m_Instances = instances;
      m_Selected = new boolean[m_Instances.numAttributes()];
    }
    





    public int getRowCount()
    {
      return m_Selected.length;
    }
    





    public int getColumnCount()
    {
      return 3;
    }
    







    public Object getValueAt(int row, int column)
    {
      switch (column) {
      case 0: 
        return new Integer(row + 1);
      case 1: 
        return new Boolean(m_Selected[row]);
      case 2: 
        return m_Instances.attribute(row).name();
      }
      return null;
    }
    







    public String getColumnName(int column)
    {
      switch (column) {
      case 0: 
        Messages.getInstance();return new String(Messages.getString("AttributeSelectionPanel_GetColumnName_Text_First"));
      case 1: 
        Messages.getInstance();return new String(Messages.getString("AttributeSelectionPanel_GetColumnName_Text_Second"));
      case 2: 
        Messages.getInstance();return new String(Messages.getString("AttributeSelectionPanel_GetColumnName_Text_Third"));
      }
      return null;
    }
    








    public void setValueAt(Object value, int row, int col)
    {
      if (col == 1) {
        m_Selected[row] = ((Boolean)value).booleanValue();
        fireTableRowsUpdated(0, m_Selected.length);
      }
    }
    





    public Class getColumnClass(int col)
    {
      return getValueAt(0, col).getClass();
    }
    







    public boolean isCellEditable(int row, int col)
    {
      if (col == 1) {
        return true;
      }
      return false;
    }
    





    public int[] getSelectedAttributes()
    {
      int[] r1 = new int[getRowCount()];
      int selCount = 0;
      for (int i = 0; i < getRowCount(); i++) {
        if (m_Selected[i] != 0) {
          r1[(selCount++)] = i;
        }
      }
      int[] result = new int[selCount];
      System.arraycopy(r1, 0, result, 0, selCount);
      return result;
    }
    



    public void includeAll()
    {
      for (int i = 0; i < m_Selected.length; i++) {
        m_Selected[i] = true;
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }
    



    public void removeAll()
    {
      for (int i = 0; i < m_Selected.length; i++) {
        m_Selected[i] = false;
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }
    



    public void invert()
    {
      for (int i = 0; i < m_Selected.length; i++) {
        m_Selected[i] = (m_Selected[i] == 0 ? 1 : false);
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }
    




    public void pattern(String pattern)
    {
      for (int i = 0; i < m_Selected.length; i++) {
        m_Selected[i] = Pattern.matches(pattern, m_Instances.attribute(i).name());
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }
    
    public void setSelectedAttributes(boolean[] selected) throws Exception {
      if (selected.length != m_Selected.length) {
        throw new Exception("Supplied array does not have the same number of elements as there are attributes!");
      }
      

      for (int i = 0; i < selected.length; i++) {
        m_Selected[i] = selected[i];
      }
      fireTableRowsUpdated(0, m_Selected.length);
    }
  }
  























  public AttributeSelectionPanel()
  {
    this(true, true, true, true);
  }
  
  public AttributeSelectionPanel(boolean include, boolean remove, boolean invert, boolean pattern)
  {
    Messages.getInstance();m_IncludeAll = new JButton(Messages.getString("AttributeSelectionPanel_IncludeAll_JButton_Text"));
    

    Messages.getInstance();m_RemoveAll = new JButton(Messages.getString("AttributeSelectionPanel_RemoveAll_JButton_Text"));
    

    Messages.getInstance();m_Invert = new JButton(Messages.getString("AttributeSelectionPanel_Invert_JButton_Text"));
    

    Messages.getInstance();m_Pattern = new JButton(Messages.getString("AttributeSelectionPanel_Pattern_JButton_Text"));
    

    m_Table = new JTable();
    




    m_PatternRegEx = "";
    

















    Messages.getInstance();m_IncludeAll.setToolTipText(Messages.getString("AttributeSelectionPanel_IncludeAll_SetToolTipText_Text"));
    m_IncludeAll.setEnabled(false);
    m_IncludeAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Model.includeAll();
      }
    });
    Messages.getInstance();m_RemoveAll.setToolTipText(Messages.getString("AttributeSelectionPanel_RemoveAll_SetToolTipText_Text"));
    m_RemoveAll.setEnabled(false);
    m_RemoveAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Model.removeAll();
      }
    });
    Messages.getInstance();m_Invert.setToolTipText(Messages.getString("AttributeSelectionPanel_Invert_SetToolTipText_Text"));
    m_Invert.setEnabled(false);
    m_Invert.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Model.invert();
      }
    });
    Messages.getInstance();m_Pattern.setToolTipText(Messages.getString("AttributeSelectionPanel_Pattern_SetToolTipText_Text"));
    m_Pattern.setEnabled(false);
    m_Pattern.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance();String pattern = JOptionPane.showInputDialog(m_Pattern.getParent(), Messages.getString("AttributeSelectionPanel_Pattern_JOptionPaneShowInputDialog_Text"), m_PatternRegEx);
        


        if (pattern != null) {
          try {
            Pattern.compile(pattern);
            m_PatternRegEx = pattern;
            m_Model.pattern(pattern);
          }
          catch (Exception ex) {
            Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Pattern.getParent(), Messages.getString("AttributeSelectionPanel_Exception_JOptionPaneShowMessageDialog_Text_First") + pattern + Messages.getString("AttributeSelectionPanel_Exception_JOptionPaneShowMessageDialog_Text_Second") + ex, Messages.getString("AttributeSelectionPanel_Exception_JOptionPaneShowMessageDialog_Text_Third"), 0);
          }
          
        }
        
      }
      

    });
    m_Table.setSelectionMode(0);
    m_Table.setColumnSelectionAllowed(false);
    m_Table.setPreferredScrollableViewportSize(new Dimension(250, 150));
    

    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    p1.setLayout(new GridLayout(1, 4, 5, 5));
    if (include) {
      p1.add(m_IncludeAll);
    }
    if (remove) {
      p1.add(m_RemoveAll);
    }
    if (invert) {
      p1.add(m_Invert);
    }
    if (pattern) {
      p1.add(m_Pattern);
    }
    
    setLayout(new BorderLayout());
    if ((include) || (remove) || (invert) || (pattern)) {
      add(p1, "North");
    }
    add(new JScrollPane(m_Table), "Center");
  }
  
  public Dimension getPreferredScrollableViewportSize() {
    return m_Table.getPreferredScrollableViewportSize();
  }
  
  public void setPreferredScrollableViewportSize(Dimension d) {
    m_Table.setPreferredScrollableViewportSize(d);
  }
  





  public void setInstances(Instances newInstances)
  {
    if (m_Model == null) {
      m_Model = new AttributeTableModel(newInstances);
      m_Table.setModel(m_Model);
      TableColumnModel tcm = m_Table.getColumnModel();
      tcm.getColumn(0).setMaxWidth(60);
      tcm.getColumn(1).setMaxWidth(tcm.getColumn(1).getMinWidth());
      tcm.getColumn(2).setMinWidth(100);
    } else {
      m_Model.setInstances(newInstances);
      m_Table.clearSelection();
    }
    m_IncludeAll.setEnabled(true);
    m_RemoveAll.setEnabled(true);
    m_Invert.setEnabled(true);
    m_Pattern.setEnabled(true);
    m_Table.sizeColumnsToFit(2);
    m_Table.revalidate();
    m_Table.repaint();
  }
  





  public int[] getSelectedAttributes()
  {
    return m_Model == null ? null : m_Model.getSelectedAttributes();
  }
  







  public void setSelectedAttributes(boolean[] selected)
    throws Exception
  {
    if (m_Model != null) {
      m_Model.setSelectedAttributes(selected);
    }
  }
  






  public TableModel getTableModel()
  {
    return m_Model;
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
        Messages.getInstance();throw new Exception(Messages.getString("AttributeSelectionPanel_Main_Exception_Text"));
      }
      Instances i = new Instances(new BufferedReader(new FileReader(args[0])));
      
      AttributeSelectionPanel asp = new AttributeSelectionPanel();
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AttributeSelectionPanel_Main_JFrame_Text"));
      
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

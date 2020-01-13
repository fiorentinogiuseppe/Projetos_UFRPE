package weka.gui.arffviewer;

import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import weka.core.Attribute;
import weka.core.Instances;
import weka.gui.ComponentHelper;
import weka.gui.JTableHelper;
import weka.gui.ViewerDialog;















































public class ArffTable
  extends JTable
{
  static final long serialVersionUID = -2016200506908637967L;
  private String m_SearchString;
  private HashSet m_ChangeListeners;
  
  protected class RelationalCellEditor
    extends AbstractCellEditor
    implements TableCellEditor
  {
    private static final long serialVersionUID = 657969163293205963L;
    protected JButton m_Button;
    protected Instances m_CurrentInst;
    protected int m_RowIndex;
    protected int m_ColumnIndex;
    
    public RelationalCellEditor(int rowIndex, int columnIndex)
    {
      m_CurrentInst = getInstancesAt(rowIndex, columnIndex);
      m_RowIndex = rowIndex;
      m_ColumnIndex = columnIndex;
      
      Messages.getInstance();m_Button = new JButton(Messages.getString("ArffTable_RelationalCellEditor_RelationalCellEditor_JButton_Text"));
      m_Button.addActionListener(new ActionListener()
      {

        public void actionPerformed(ActionEvent evt)
        {
          ViewerDialog dialog = new ViewerDialog(null);
          Messages.getInstance();dialog.setTitle(Messages.getString("ArffTable_RelationalCellEditor_RelationalCellEditor_ViewerDialog_Text") + ((ArffSortedTableModel)getModel()).getInstances().attribute(m_ColumnIndex - 1).name());
          

          int result = dialog.showDialog(m_CurrentInst);
          if (result == 0) {
            m_CurrentInst = dialog.getInstances();
            fireEditingStopped();
          }
          else {
            fireEditingCanceled();
          }
        }
      });
    }
    










    protected Instances getInstancesAt(int rowIndex, int columnIndex)
    {
      ArffSortedTableModel model = (ArffSortedTableModel)getModel();
      double value = model.getInstancesValueAt(rowIndex, columnIndex);
      Instances result = model.getInstances().attribute(columnIndex - 1).relation((int)value);
      
      return result;
    }
    















    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
      return m_Button;
    }
    




    public Object getCellEditorValue()
    {
      return m_CurrentInst;
    }
  }
  







  public ArffTable()
  {
    this(new ArffSortedTableModel(""));
  }
  




  public ArffTable(TableModel model)
  {
    super(model);
    
    setAutoResizeMode(0);
  }
  







  public void setModel(TableModel model)
  {
    m_SearchString = null;
    

    if (m_ChangeListeners == null) {
      m_ChangeListeners = new HashSet();
    }
    super.setModel(model);
    
    if (model == null) {
      return;
    }
    if (!(model instanceof ArffSortedTableModel)) {
      return;
    }
    ArffSortedTableModel arffModel = (ArffSortedTableModel)model;
    arffModel.addMouseListenerToHeader(this);
    arffModel.addTableModelListener(this);
    arffModel.sort(0);
    setLayout();
    setSelectedColumn(0);
    

    if (getTableHeader() != null) {
      getTableHeader().setReorderingAllowed(false);
    }
  }
  


  public TableCellEditor getCellEditor(int row, int column)
  {
    TableCellEditor result;
    

    TableCellEditor result;
    

    if (((getModel() instanceof ArffSortedTableModel)) && (((ArffSortedTableModel)getModel()).getType(column) == 4))
    {
      result = new RelationalCellEditor(row, column);
    }
    else {
      result = super.getCellEditor(row, column);
    }
    return result;
  }
  




  public boolean isReadOnly()
  {
    return ((ArffSortedTableModel)getModel()).isReadOnly();
  }
  




  public void setReadOnly(boolean value)
  {
    ((ArffSortedTableModel)getModel()).setReadOnly(value);
  }
  







  private void setLayout()
  {
    ArffSortedTableModel arffModel = (ArffSortedTableModel)getModel();
    
    for (int i = 0; i < getColumnCount(); i++)
    {
      JTableHelper.setOptimalHeaderWidth(this, i);
      

      getColumnModel().getColumn(i).setCellRenderer(new ArffTableCellRenderer());
      


      if (i > 0) {
        if (arffModel.getType(i) == 1) {
          JComboBox combo = new JComboBox();
          combo.addItem(null);
          Enumeration enm = arffModel.getInstances().attribute(i - 1).enumerateValues();
          while (enm.hasMoreElements())
            combo.addItem(enm.nextElement());
          getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(combo));
        }
        else {
          getColumnModel().getColumn(i).setCellEditor(null);
        }
      }
    }
  }
  









  public String getPlainColumnName(int columnIndex)
  {
    String result = "";
    
    if (getModel() == null)
      return result;
    if (!(getModel() instanceof ArffSortedTableModel)) {
      return result;
    }
    ArffSortedTableModel arffModel = (ArffSortedTableModel)getModel();
    
    if ((columnIndex >= 0) && (columnIndex < getColumnCount())) {
      if (columnIndex == 0) {
        Messages.getInstance();result = Messages.getString("ArffTable_GetPlainColumnName_Result_Text");
      } else {
        result = arffModel.getAttributeAt(columnIndex).name();
      }
    }
    return result;
  }
  












  public StringSelection getStringSelection()
  {
    StringSelection result = null;
    

    if (getSelectedRow() == -1)
    {
      Messages.getInstance();Messages.getInstance(); if (ComponentHelper.showMessageBox(getParent(), Messages.getString("ArffTable_GetStringSelection_ComponentHelperShowMessageBox_Text_First"), Messages.getString("ArffTable_GetStringSelection_ComponentHelperShowMessageBox_Text_Second"), 0, 3) != 0)
      {




        return result;
      }
      int[] indices = new int[getRowCount()];
      for (int i = 0; i < indices.length; i++) {
        indices[i] = i;
      }
    }
    int[] indices = getSelectedRows();
    


    StringBuffer tmp = new StringBuffer();
    for (int i = 0; i < getColumnCount(); i++) {
      if (i > 0)
        tmp.append("\t");
      tmp.append(getPlainColumnName(i));
    }
    tmp.append("\n");
    

    for (i = 0; i < indices.length; i++) {
      for (int n = 0; n < getColumnCount(); n++) {
        if (n > 0)
          tmp.append("\t");
        tmp.append(getValueAt(indices[i], n).toString());
      }
      tmp.append("\n");
    }
    
    result = new StringSelection(tmp.toString());
    
    return result;
  }
  





  public void setSearchString(String searchString)
  {
    m_SearchString = searchString;
    repaint();
  }
  




  public String getSearchString()
  {
    return m_SearchString;
  }
  




  public void setSelectedColumn(int index)
  {
    getColumnModel().getSelectionModel().clearSelection();
    getColumnModel().getSelectionModel().setSelectionInterval(index, index);
    resizeAndRepaint();
    if (getTableHeader() != null) {
      getTableHeader().resizeAndRepaint();
    }
  }
  




  public void tableChanged(TableModelEvent e)
  {
    super.tableChanged(e);
    
    setLayout();
    notifyListener();
  }
  




  private void notifyListener()
  {
    Iterator iter = m_ChangeListeners.iterator();
    while (iter.hasNext()) {
      ((ChangeListener)iter.next()).stateChanged(new ChangeEvent(this));
    }
  }
  



  public void addChangeListener(ChangeListener l)
  {
    m_ChangeListeners.add(l);
  }
  




  public void removeChangeListener(ChangeListener l)
  {
    m_ChangeListeners.remove(l);
  }
}

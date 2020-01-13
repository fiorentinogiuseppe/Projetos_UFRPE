package weka.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import weka.core.ClassDiscovery;










































public class SortedTableModel
  extends AbstractTableModel
  implements TableModelListener
{
  static final long serialVersionUID = 4030907921461127548L;
  protected TableModel mModel;
  protected int[] mIndices;
  protected int mSortColumn;
  protected boolean mAscending;
  
  public static class SortContainer
    implements Comparable<SortContainer>
  {
    protected Comparable m_Value;
    protected int m_Index;
    
    public SortContainer(Comparable value, int index)
    {
      m_Value = value;
      m_Index = index;
    }
    




    public Comparable getValue()
    {
      return m_Value;
    }
    




    public int getIndex()
    {
      return m_Index;
    }
    











    public int compareTo(SortContainer o)
    {
      if ((m_Value == null) || (o.getValue() == null)) {
        if (m_Value == o.getValue())
          return 0;
        if (m_Value == null) {
          return -1;
        }
        return 1;
      }
      
      return m_Value.compareTo(o.getValue());
    }
    









    public boolean equals(Object obj)
    {
      return compareTo((SortContainer)obj) == 0;
    }
    




    public String toString()
    {
      Messages.getInstance();Messages.getInstance();return Messages.getString("SortedTableModel_ToString_Text_First") + m_Value + Messages.getString("SortedTableModel_ToString_Text_Second") + m_Index;
    }
  }
  














  public SortedTableModel()
  {
    this(null);
  }
  




  public SortedTableModel(TableModel model)
  {
    setModel(model);
  }
  




  public void setModel(TableModel value)
  {
    mModel = value;
    

    if (mModel == null) {
      mIndices = null;
    }
    else {
      initializeIndices();
      mSortColumn = -1;
      mAscending = true;
      mModel.addTableModelListener(this);
    }
  }
  




  protected void initializeIndices()
  {
    mIndices = new int[mModel.getRowCount()];
    for (int i = 0; i < mIndices.length; i++) {
      mIndices[i] = i;
    }
  }
  



  public TableModel getModel()
  {
    return mModel;
  }
  




  public boolean isSorted()
  {
    return mSortColumn > -1;
  }
  





  protected boolean isInitialized()
  {
    return getModel() != null;
  }
  






  public int getActualRow(int visibleRow)
  {
    if (!isInitialized()) {
      return -1;
    }
    return mIndices[visibleRow];
  }
  






  public Class getColumnClass(int columnIndex)
  {
    if (!isInitialized()) {
      return null;
    }
    return getModel().getColumnClass(columnIndex);
  }
  




  public int getColumnCount()
  {
    if (!isInitialized()) {
      return 0;
    }
    return getModel().getColumnCount();
  }
  





  public String getColumnName(int columnIndex)
  {
    if (!isInitialized()) {
      return null;
    }
    return getModel().getColumnName(columnIndex);
  }
  




  public int getRowCount()
  {
    if (!isInitialized()) {
      return 0;
    }
    return getModel().getRowCount();
  }
  






  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if (!isInitialized()) {
      return null;
    }
    return getModel().getValueAt(mIndices[rowIndex], columnIndex);
  }
  






  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    if (!isInitialized()) {
      return false;
    }
    return getModel().isCellEditable(mIndices[rowIndex], columnIndex);
  }
  






  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    if (isInitialized()) {
      getModel().setValueAt(aValue, mIndices[rowIndex], columnIndex);
    }
  }
  



  public void sort(int columnIndex)
  {
    sort(columnIndex, true);
  }
  












  public void sort(int columnIndex, boolean ascending)
  {
    if ((!isInitialized()) || (getModel().getRowCount() != mIndices.length))
    {

      Messages.getInstance();System.out.println(getClass().getName() + Messages.getString("SortedTableModel_Sort_Text_Firt"));
      

      return;
    }
    

    mSortColumn = columnIndex;
    mAscending = ascending;
    initializeIndices();
    int columnType;
    int columnType;
    if (ClassDiscovery.hasInterface(Comparable.class, getColumnClass(mSortColumn))) {
      columnType = 1;
    } else {
      columnType = 0;
    }
    
    ArrayList<SortContainer> sorted = new ArrayList();
    for (int i = 0; i < getRowCount(); i++) {
      Object value = mModel.getValueAt(mIndices[i], mSortColumn);
      SortContainer cont; SortContainer cont; if (columnType == 0) {
        cont = new SortContainer(value == null ? null : value.toString(), mIndices[i]);
      } else
        cont = new SortContainer((Comparable)value, mIndices[i]);
      sorted.add(cont);
    }
    Collections.sort(sorted);
    
    for (i = 0; i < sorted.size(); i++) {
      if (mAscending) {
        mIndices[i] = ((SortContainer)sorted.get(i)).getIndex();
      } else {
        mIndices[i] = ((SortContainer)sorted.get(sorted.size() - 1 - i)).getIndex();
      }
    }
    sorted.clear();
    sorted = null;
  }
  





  public void tableChanged(TableModelEvent e)
  {
    initializeIndices();
    if (isSorted()) {
      sort(mSortColumn, mAscending);
    }
    fireTableChanged(e);
  }
  





  public void addMouseListenerToHeader(JTable table)
  {
    final SortedTableModel modelFinal = this;
    final JTable tableFinal = table;
    tableFinal.setColumnSelectionAllowed(false);
    JTableHeader header = tableFinal.getTableHeader();
    
    if (header != null) {
      MouseAdapter listMouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          TableColumnModel columnModel = tableFinal.getColumnModel();
          int viewColumn = columnModel.getColumnIndexAtX(e.getX());
          int column = tableFinal.convertColumnIndexToModel(viewColumn);
          if ((e.getButton() == 1) && (e.getClickCount() == 1) && (!e.isAltDown()) && (column != -1))
          {


            int shiftPressed = e.getModifiers() & 0x1;
            boolean ascending = shiftPressed == 0;
            modelFinal.sort(column, ascending);
          }
          
        }
      };
      header.addMouseListener(listMouseListener);
    }
  }
}

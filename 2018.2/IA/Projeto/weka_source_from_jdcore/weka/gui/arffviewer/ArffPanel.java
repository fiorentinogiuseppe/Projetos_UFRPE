package weka.gui.arffviewer;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Undoable;
import weka.core.Utils;
import weka.gui.ComponentHelper;
import weka.gui.JTableHelper;
import weka.gui.ListSelectorDialog;

































public class ArffPanel
  extends JPanel
  implements ActionListener, ChangeListener, MouseListener, Undoable
{
  static final long serialVersionUID = -4697041150989513939L;
  
  static { Messages.getInstance(); } public static final String TAB_INSTANCES = Messages.getString("ArffPanel_TabInstanses_Text");
  
  private ArffTable m_TableArff;
  
  private JPopupMenu m_PopupHeader;
  
  private JPopupMenu m_PopupRows;
  
  private JLabel m_LabelName;
  
  private JMenuItem menuItemMean;
  
  private JMenuItem menuItemSetAllValues;
  
  private JMenuItem menuItemSetMissingValues;
  
  private JMenuItem menuItemReplaceValues;
  
  private JMenuItem menuItemRenameAttribute;
  
  private JMenuItem menuItemAttributeAsClass;
  
  private JMenuItem menuItemDeleteAttribute;
  
  private JMenuItem menuItemDeleteAttributes;
  
  private JMenuItem menuItemSortInstances;
  
  private JMenuItem menuItemDeleteSelectedInstance;
  
  private JMenuItem menuItemDeleteAllSelectedInstances;
  
  private JMenuItem menuItemSearch;
  
  private JMenuItem menuItemClearSearch;
  
  private JMenuItem menuItemUndo;
  
  private JMenuItem menuItemCopy;
  private JMenuItem menuItemOptimalColWidth;
  private JMenuItem menuItemOptimalColWidths;
  private String m_Filename;
  private String m_Title;
  private int m_CurrentCol;
  private boolean m_Changed;
  private HashSet m_ChangeListeners;
  private String m_LastSearch;
  private String m_LastReplace;
  
  public ArffPanel()
  {
    initialize();
    createPanel();
  }
  




  public ArffPanel(String filename)
  {
    this();
    
    loadFile(filename);
  }
  




  public ArffPanel(Instances data)
  {
    this();
    
    m_Filename = "";
    
    setInstances(data);
  }
  


  protected void initialize()
  {
    m_Filename = "";
    m_Title = "";
    m_CurrentCol = -1;
    m_LastSearch = "";
    m_LastReplace = "";
    m_Changed = false;
    m_ChangeListeners = new HashSet();
  }
  




  protected void createPanel()
  {
    setLayout(new BorderLayout());
    
    Messages.getInstance();menuItemMean = new JMenuItem(Messages.getString("ArffPanel_GetMean_JMenuItem_Text"));
    menuItemMean.addActionListener(this);
    Messages.getInstance();menuItemSetAllValues = new JMenuItem(Messages.getString("ArffPanel_SetAllValuesTo_JMenuItem_Text"));
    menuItemSetAllValues.addActionListener(this);
    Messages.getInstance();menuItemSetMissingValues = new JMenuItem(Messages.getString("ArffPanel_SetMissingValues_JMenuItem_Text"));
    menuItemSetMissingValues.addActionListener(this);
    Messages.getInstance();menuItemReplaceValues = new JMenuItem(Messages.getString("ArffPanel_ReplaceValues_JMenuItem_Text"));
    menuItemReplaceValues.addActionListener(this);
    Messages.getInstance();menuItemRenameAttribute = new JMenuItem(Messages.getString("ArffPanel_RenameAttribute_JMenuItem_Text"));
    menuItemRenameAttribute.addActionListener(this);
    Messages.getInstance();menuItemAttributeAsClass = new JMenuItem(Messages.getString("ArffPanel_AttributeAsClass_JMenuItem_Text"));
    menuItemAttributeAsClass.addActionListener(this);
    Messages.getInstance();menuItemDeleteAttribute = new JMenuItem(Messages.getString("ArffPanel_DeleteAttribute_JMenuItem_Text"));
    menuItemDeleteAttribute.addActionListener(this);
    Messages.getInstance();menuItemDeleteAttributes = new JMenuItem(Messages.getString("ArffPanel_DeleteAttributes_JMenuItem_Text"));
    menuItemDeleteAttributes.addActionListener(this);
    Messages.getInstance();menuItemSortInstances = new JMenuItem(Messages.getString("ArffPanel_SortInstances_JMenuItem_Text"));
    menuItemSortInstances.addActionListener(this);
    Messages.getInstance();menuItemOptimalColWidth = new JMenuItem(Messages.getString("ArffPanel_OptimalColWidth_JMenuItem_Text"));
    menuItemOptimalColWidth.addActionListener(this);
    Messages.getInstance();menuItemOptimalColWidths = new JMenuItem(Messages.getString("ArffPanel_OptimalColWidths_JMenuItem_Text"));
    menuItemOptimalColWidths.addActionListener(this);
    

    Messages.getInstance();menuItemUndo = new JMenuItem(Messages.getString("ArffPanel_Undo_JMenuItem_Text"));
    menuItemUndo.addActionListener(this);
    Messages.getInstance();menuItemCopy = new JMenuItem(Messages.getString("ArffPanel_Copy_JMenuItem_Text"));
    menuItemCopy.addActionListener(this);
    Messages.getInstance();menuItemSearch = new JMenuItem(Messages.getString("ArffPanel_Search_JMenuItem_Text"));
    menuItemSearch.addActionListener(this);
    Messages.getInstance();menuItemClearSearch = new JMenuItem(Messages.getString("ArffPanel_ClearSearch_JMenuItem_Text"));
    menuItemClearSearch.addActionListener(this);
    Messages.getInstance();menuItemDeleteSelectedInstance = new JMenuItem(Messages.getString("ArffPanel_DeleteSelectedInstance_JMenuItem_Text"));
    menuItemDeleteSelectedInstance.addActionListener(this);
    Messages.getInstance();menuItemDeleteAllSelectedInstances = new JMenuItem(Messages.getString("ArffPanel_DeleteAllSelectedInstances_JMenuItem_Text"));
    menuItemDeleteAllSelectedInstances.addActionListener(this);
    

    m_TableArff = new ArffTable();
    Messages.getInstance();m_TableArff.setToolTipText(Messages.getString("ArffPanel_ToolTipText_Text"));
    m_TableArff.getTableHeader().addMouseListener(this);
    Messages.getInstance();m_TableArff.getTableHeader().setToolTipText(Messages.getString("ArffPanel_TableHeader_ToolTipText_Text") + "<html><b>Sort view:</b> left click = ascending / Shift + left click = descending<br><b>Menu:</b> right click (or left+alt)</html>");
    m_TableArff.getTableHeader().setDefaultRenderer(new ArffTableCellRenderer());
    m_TableArff.addChangeListener(this);
    m_TableArff.addMouseListener(this);
    JScrollPane pane = new JScrollPane(m_TableArff);
    add(pane, "Center");
    

    m_LabelName = new JLabel();
    add(m_LabelName, "North");
  }
  



  private void initPopupMenus()
  {
    m_PopupHeader = new JPopupMenu();
    m_PopupHeader.addMouseListener(this);
    m_PopupHeader.add(menuItemMean);
    if (!isReadOnly()) {
      m_PopupHeader.addSeparator();
      m_PopupHeader.add(menuItemSetAllValues);
      m_PopupHeader.add(menuItemSetMissingValues);
      m_PopupHeader.add(menuItemReplaceValues);
      m_PopupHeader.addSeparator();
      m_PopupHeader.add(menuItemRenameAttribute);
      m_PopupHeader.add(menuItemAttributeAsClass);
      m_PopupHeader.add(menuItemDeleteAttribute);
      m_PopupHeader.add(menuItemDeleteAttributes);
      m_PopupHeader.add(menuItemSortInstances);
    }
    m_PopupHeader.addSeparator();
    m_PopupHeader.add(menuItemOptimalColWidth);
    m_PopupHeader.add(menuItemOptimalColWidths);
    

    m_PopupRows = new JPopupMenu();
    m_PopupRows.addMouseListener(this);
    if (!isReadOnly()) {
      m_PopupRows.add(menuItemUndo);
      m_PopupRows.addSeparator();
    }
    m_PopupRows.add(menuItemCopy);
    m_PopupRows.addSeparator();
    m_PopupRows.add(menuItemSearch);
    m_PopupRows.add(menuItemClearSearch);
    if (!isReadOnly()) {
      m_PopupRows.addSeparator();
      m_PopupRows.add(menuItemDeleteSelectedInstance);
      m_PopupRows.add(menuItemDeleteAllSelectedInstances);
    }
  }
  









  private void setMenu()
  {
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    boolean isNull = model.getInstances() == null;
    boolean hasColumns = (!isNull) && (model.getInstances().numAttributes() > 0);
    boolean hasRows = (!isNull) && (model.getInstances().numInstances() > 0);
    boolean attSelected = (hasColumns) && (m_CurrentCol > 0);
    boolean isNumeric = (attSelected) && (model.getAttributeAt(m_CurrentCol).isNumeric());
    
    menuItemUndo.setEnabled(canUndo());
    menuItemCopy.setEnabled(true);
    menuItemSearch.setEnabled(true);
    menuItemClearSearch.setEnabled(true);
    menuItemMean.setEnabled(isNumeric);
    menuItemSetAllValues.setEnabled(attSelected);
    menuItemSetMissingValues.setEnabled(attSelected);
    menuItemReplaceValues.setEnabled(attSelected);
    menuItemRenameAttribute.setEnabled(attSelected);
    menuItemDeleteAttribute.setEnabled(attSelected);
    menuItemDeleteAttributes.setEnabled(attSelected);
    menuItemSortInstances.setEnabled((hasRows) && (attSelected));
    menuItemDeleteSelectedInstance.setEnabled((hasRows) && (m_TableArff.getSelectedRow() > -1));
    menuItemDeleteAllSelectedInstances.setEnabled((hasRows) && (m_TableArff.getSelectedRows().length > 0));
  }
  




  public ArffTable getTable()
  {
    return m_TableArff;
  }
  




  public String getTitle()
  {
    return m_Title;
  }
  




  public String getFilename()
  {
    return m_Filename;
  }
  




  public void setFilename(String filename)
  {
    m_Filename = filename;
    createTitle();
  }
  






  public Instances getInstances()
  {
    Instances result = null;
    
    if (m_TableArff.getModel() != null) {
      result = ((ArffSortedTableModel)m_TableArff.getModel()).getInstances();
    }
    return result;
  }
  











  public void setInstances(Instances data)
  {
    m_Filename = TAB_INSTANCES;
    
    createTitle();
    ArffSortedTableModel model = new ArffSortedTableModel(data);
    
    m_TableArff.setModel(model);
    clearUndo();
    setChanged(false);
    createName();
  }
  







  public Vector getAttributes()
  {
    Vector result = new Vector();
    for (int i = 0; i < getInstances().numAttributes(); i++)
      result.add(getInstances().attribute(i).name());
    Collections.sort(result);
    
    return result;
  }
  




  public void setChanged(boolean changed)
  {
    if (!changed) {
      m_Changed = changed;
      createTitle();
    }
  }
  




  public boolean isChanged()
  {
    return m_Changed;
  }
  




  public boolean isReadOnly()
  {
    if (m_TableArff == null) {
      return true;
    }
    return ((ArffSortedTableModel)m_TableArff.getModel()).isReadOnly();
  }
  




  public void setReadOnly(boolean value)
  {
    if (m_TableArff != null) {
      ((ArffSortedTableModel)m_TableArff.getModel()).setReadOnly(value);
    }
  }
  



  public boolean isUndoEnabled()
  {
    return ((ArffSortedTableModel)m_TableArff.getModel()).isUndoEnabled();
  }
  




  public void setUndoEnabled(boolean enabled)
  {
    ((ArffSortedTableModel)m_TableArff.getModel()).setUndoEnabled(enabled);
  }
  


  public void clearUndo()
  {
    ((ArffSortedTableModel)m_TableArff.getModel()).clearUndo();
  }
  




  public boolean canUndo()
  {
    return ((ArffSortedTableModel)m_TableArff.getModel()).canUndo();
  }
  


  public void undo()
  {
    if (canUndo()) {
      ((ArffSortedTableModel)m_TableArff.getModel()).undo();
      

      notifyListener();
    }
  }
  


  public void addUndoPoint()
  {
    ((ArffSortedTableModel)m_TableArff.getModel()).addUndoPoint();
    

    setMenu();
  }
  




  private void createTitle()
  {
    if (m_Filename.equals("")) {
      Messages.getInstance();m_Title = Messages.getString("ArffPanel_CreateTitle_Title_Text");
    }
    else if (m_Filename.equals(TAB_INSTANCES)) {
      m_Title = TAB_INSTANCES;
    }
    else {
      try {
        File file = new File(m_Filename);
        m_Title = file.getName();
      }
      catch (Exception e) {
        Messages.getInstance();m_Title = Messages.getString("ArffPanel_CreateTitle_Title_Text");
      }
    }
    
    if (isChanged()) {
      m_Title += " *";
    }
  }
  



  private void createName()
  {
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    if ((model != null) && (model.getInstances() != null)) {
      Messages.getInstance();m_LabelName.setText(Messages.getString("ArffPanel_CreateName_Text") + model.getInstances().relationName());
    } else {
      m_LabelName.setText("");
    }
  }
  





  private void loadFile(String filename)
  {
    m_Filename = filename;
    
    createTitle();
    ArffSortedTableModel model;
    ArffSortedTableModel model; if (filename.equals("")) {
      model = null;
    } else {
      model = new ArffSortedTableModel(filename);
    }
    m_TableArff.setModel(model);
    setChanged(false);
    createName();
  }
  







  private void calcMean()
  {
    if (m_CurrentCol == -1) {
      return;
    }
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    

    if (!model.getAttributeAt(m_CurrentCol).isNumeric()) {
      return;
    }
    double mean = 0.0D;
    for (int i = 0; i < model.getRowCount(); i++)
      mean += model.getInstances().instance(i).value(m_CurrentCol - 1);
    mean /= model.getRowCount();
    

    Messages.getInstance();Messages.getInstance();Messages.getInstance();ComponentHelper.showMessageBox(getParent(), Messages.getString("ArffPanel_CalcMean_Text_First"), Messages.getString("ArffPanel_CalcMean_Text_Second") + m_TableArff.getPlainColumnName(m_CurrentCol) + Messages.getString("ArffPanel_CalcMean_Text_Third") + Utils.doubleToString(mean, 3), 2, -1);
  }
  


















  private void setValues(Object o)
  {
    String value = "";
    String valueNew = "";
    String msg;
    if (o == menuItemSetMissingValues) {
      Messages.getInstance();String title = Messages.getString("ArffPanel_SetValues_Title_Text_First");
      Messages.getInstance();msg = Messages.getString("ArffPanel_SetValues_Message_Text_First");
    } else { String msg;
      if (o == menuItemSetAllValues) {
        Messages.getInstance();String title = Messages.getString("ArffPanel_SetValues_Title_Text_Second");
        Messages.getInstance();msg = Messages.getString("ArffPanel_SetValues_Message_Text_Second");
      } else { String msg;
        if (o == menuItemReplaceValues) {
          Messages.getInstance();String title = Messages.getString("ArffPanel_SetValues_Title_Text_Third");
          Messages.getInstance();msg = Messages.getString("ArffPanel_SetValues_Message_Text_Third");
        } else { return;
        } } }
    String title;
    String msg;
    value = ComponentHelper.showInputBox(m_TableArff.getParent(), title, msg, m_LastSearch);
    

    if (value == null) {
      return;
    }
    m_LastSearch = value;
    

    if (o == menuItemReplaceValues) {
      Messages.getInstance();valueNew = ComponentHelper.showInputBox(m_TableArff.getParent(), title, Messages.getString("ArffPanel_SetValues_ComponentHelperShowInputBox_Text"), m_LastReplace);
      if (valueNew == null)
        return;
      m_LastReplace = valueNew;
    }
    
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    model.setNotificationEnabled(false);
    

    addUndoPoint();
    model.setUndoEnabled(false);
    String valueCopy = value;
    String valueNewCopy = valueNew;
    
    for (int i = 0; i < m_TableArff.getRowCount(); i++) {
      if (o == menuItemSetAllValues) {
        if ((valueCopy.equals("NaN")) || (valueCopy.equals("?"))) {
          value = null;
        }
        model.setValueAt(value, i, m_CurrentCol);

      }
      else if ((o == menuItemSetMissingValues) && (model.isMissingAt(i, m_CurrentCol)))
      {
        model.setValueAt(value, i, m_CurrentCol);
      } else if ((o == menuItemReplaceValues) && (model.getValueAt(i, m_CurrentCol).toString().equals(value)))
      {
        if ((valueNewCopy.equals("NaN")) || (valueNewCopy.equals("?"))) {
          valueNew = null;
        }
        model.setValueAt(valueNew, i, m_CurrentCol);
      }
    }
    model.setUndoEnabled(true);
    model.setNotificationEnabled(true);
    model.notifyListener(new TableModelEvent(model, 0, model.getRowCount(), m_CurrentCol, 0));
    

    m_TableArff.repaint();
  }
  





  public void deleteAttribute()
  {
    if (m_CurrentCol == -1) {
      return;
    }
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    

    if (model.getAttributeAt(m_CurrentCol) == null) {
      return;
    }
    
    Messages.getInstance();Messages.getInstance();Messages.getInstance(); if (ComponentHelper.showMessageBox(getParent(), Messages.getString("ArffPanel_DeleteAttribute_ComponentHelperShowMessageBox_Text_First"), Messages.getString("ArffPanel_DeleteAttribute_ComponentHelperShowMessageBox_Text_Second") + model.getAttributeAt(m_CurrentCol).name() + Messages.getString("ArffPanel_DeleteAttribute_ComponentHelperShowMessageBox_Text_Third"), 0, 3) != 0)
    {





      return;
    }
    setCursor(Cursor.getPredefinedCursor(3));
    model.deleteAttributeAt(m_CurrentCol);
    setCursor(Cursor.getPredefinedCursor(0));
  }
  










  public void deleteAttributes()
  {
    JList list = new JList(getAttributes());
    ListSelectorDialog dialog = new ListSelectorDialog(null, list);
    int result = dialog.showDialog();
    
    if (result != 0) {
      return;
    }
    Object[] atts = list.getSelectedValues();
    

    Messages.getInstance();Messages.getInstance();Messages.getInstance(); if (ComponentHelper.showMessageBox(getParent(), Messages.getString("ArffPanel_DeleteAttributes_ComponentHelperShowMessageBox_Text_First"), Messages.getString("ArffPanel_DeleteAttributes_ComponentHelperShowMessageBox_Text_Second") + atts.length + Messages.getString("ArffPanel_DeleteAttributes_ComponentHelperShowMessageBox_Text_Third"), 0, 3) != 0)
    {





      return;
    }
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    int[] indices = new int[atts.length];
    for (int i = 0; i < atts.length; i++) {
      indices[i] = model.getAttributeColumn(atts[i].toString());
    }
    setCursor(Cursor.getPredefinedCursor(3));
    model.deleteAttributes(indices);
    setCursor(Cursor.getPredefinedCursor(0));
  }
  






  public void attributeAsClass()
  {
    if (m_CurrentCol == -1) {
      return;
    }
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    

    if (model.getAttributeAt(m_CurrentCol) == null) {
      return;
    }
    setCursor(Cursor.getPredefinedCursor(3));
    model.attributeAsClassAt(m_CurrentCol);
    setCursor(Cursor.getPredefinedCursor(0));
  }
  






  public void renameAttribute()
  {
    if (m_CurrentCol == -1) {
      return;
    }
    ArffSortedTableModel model = (ArffSortedTableModel)m_TableArff.getModel();
    

    if (model.getAttributeAt(m_CurrentCol) == null) {
      return;
    }
    Messages.getInstance();Messages.getInstance();String newName = ComponentHelper.showInputBox(getParent(), Messages.getString("ArffPanel_RenameAttribute_ComponentHelperShowInputBox_Text_First"), Messages.getString("ArffPanel_RenameAttribute_ComponentHelperShowInputBox_Text_Second"), model.getAttributeAt(m_CurrentCol).name());
    
    if (newName == null) {
      return;
    }
    setCursor(Cursor.getPredefinedCursor(3));
    model.renameAttributeAt(m_CurrentCol, newName);
    setCursor(Cursor.getPredefinedCursor(0));
  }
  




  public void deleteInstance()
  {
    int index = m_TableArff.getSelectedRow();
    if (index == -1) {
      return;
    }
    ((ArffSortedTableModel)m_TableArff.getModel()).deleteInstanceAt(index);
  }
  




  public void deleteInstances()
  {
    if (m_TableArff.getSelectedRow() == -1) {
      return;
    }
    int[] indices = m_TableArff.getSelectedRows();
    ((ArffSortedTableModel)m_TableArff.getModel()).deleteInstances(indices);
  }
  


  public void sortInstances()
  {
    if (m_CurrentCol == -1) {
      return;
    }
    ((ArffSortedTableModel)m_TableArff.getModel()).sortInstances(m_CurrentCol);
  }
  





  public void copyContent()
  {
    StringSelection selection = getTable().getStringSelection();
    if (selection == null) {
      return;
    }
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, selection);
  }
  





  public void search()
  {
    Messages.getInstance();Messages.getInstance();String searchString = ComponentHelper.showInputBox(getParent(), Messages.getString("ArffPanel_Search_ComponentHelperShowInputBox_Text_First"), Messages.getString("ArffPanel_Search_ComponentHelperShowInputBox_Text_Second"), m_LastSearch);
    
    if (searchString != null) {
      m_LastSearch = searchString;
    }
    getTable().setSearchString(searchString);
  }
  


  public void clearSearch()
  {
    getTable().setSearchString("");
  }
  



  public void setOptimalColWidth()
  {
    if (m_CurrentCol == -1) {
      return;
    }
    JTableHelper.setOptimalColumnWidth(getTable(), m_CurrentCol);
  }
  


  public void setOptimalColWidths()
  {
    JTableHelper.setOptimalColumnWidth(getTable());
  }
  






  public void actionPerformed(ActionEvent e)
  {
    Object o = e.getSource();
    
    if (o == menuItemMean) {
      calcMean();
    } else if (o == menuItemSetAllValues) {
      setValues(menuItemSetAllValues);
    } else if (o == menuItemSetMissingValues) {
      setValues(menuItemSetMissingValues);
    } else if (o == menuItemReplaceValues) {
      setValues(menuItemReplaceValues);
    } else if (o == menuItemRenameAttribute) {
      renameAttribute();
    } else if (o == menuItemAttributeAsClass) {
      attributeAsClass();
    } else if (o == menuItemDeleteAttribute) {
      deleteAttribute();
    } else if (o == menuItemDeleteAttributes) {
      deleteAttributes();
    } else if (o == menuItemDeleteSelectedInstance) {
      deleteInstance();
    } else if (o == menuItemDeleteAllSelectedInstances) {
      deleteInstances();
    } else if (o == menuItemSortInstances) {
      sortInstances();
    } else if (o == menuItemSearch) {
      search();
    } else if (o == menuItemClearSearch) {
      clearSearch();
    } else if (o == menuItemUndo) {
      undo();
    } else if (o == menuItemCopy) {
      copyContent();
    } else if (o == menuItemOptimalColWidth) {
      setOptimalColWidth();
    } else if (o == menuItemOptimalColWidths) {
      setOptimalColWidths();
    }
  }
  






  public void mouseClicked(MouseEvent e)
  {
    int col = m_TableArff.columnAtPoint(e.getPoint());
    boolean popup = ((e.getButton() == 3) && (e.getClickCount() == 1)) || ((e.getButton() == 1) && (e.getClickCount() == 1) && (e.isAltDown()) && (!e.isControlDown()) && (!e.isShiftDown()));
    
    popup = (popup) && (getInstances() != null);
    
    if (e.getSource() == m_TableArff.getTableHeader()) {
      m_CurrentCol = col;
      

      if (popup) {
        e.consume();
        setMenu();
        initPopupMenus();
        m_PopupHeader.show(e.getComponent(), e.getX(), e.getY());
      }
    }
    else if (e.getSource() == m_TableArff)
    {
      if (popup) {
        e.consume();
        setMenu();
        initPopupMenus();
        m_PopupRows.show(e.getComponent(), e.getX(), e.getY());
      }
    }
    

    if ((e.getButton() == 1) && (e.getClickCount() == 1) && (!e.isAltDown()) && (col > -1))
    {


      m_TableArff.setSelectedColumn(col);
    }
  }
  




































  public void stateChanged(ChangeEvent e)
  {
    m_Changed = true;
    createTitle();
    notifyListener();
  }
  




  public void notifyListener()
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
  
  public void mouseEntered(MouseEvent e) {}
  
  public void mouseExited(MouseEvent e) {}
  
  public void mousePressed(MouseEvent e) {}
  
  public void mouseReleased(MouseEvent e) {}
}

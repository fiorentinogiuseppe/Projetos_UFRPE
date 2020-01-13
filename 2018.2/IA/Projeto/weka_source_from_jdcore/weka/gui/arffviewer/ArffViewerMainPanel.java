package weka.gui.arffviewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.AbstractSaver;
import weka.core.converters.ConverterUtils;
import weka.gui.ComponentHelper;
import weka.gui.ConverterFileChooser;
import weka.gui.JTableHelper;
import weka.gui.ListSelectorDialog;















































public class ArffViewerMainPanel
  extends JPanel
  implements ActionListener, ChangeListener
{
  static final long serialVersionUID = -8763161167586738753L;
  public static final int DEFAULT_WIDTH = -1;
  public static final int DEFAULT_HEIGHT = -1;
  public static final int DEFAULT_LEFT = -1;
  public static final int DEFAULT_TOP = -1;
  public static final int WIDTH = 800;
  public static final int HEIGHT = 600;
  protected Container parent;
  protected JTabbedPane tabbedPane;
  protected JMenuBar menuBar;
  protected JMenu menuFile;
  protected JMenuItem menuFileOpen;
  protected JMenuItem menuFileSave;
  protected JMenuItem menuFileSaveAs;
  protected JMenuItem menuFileClose;
  protected JMenuItem menuFileCloseAll;
  protected JMenuItem menuFileProperties;
  protected JMenuItem menuFileExit;
  protected JMenu menuEdit;
  protected JMenuItem menuEditUndo;
  protected JMenuItem menuEditCopy;
  protected JMenuItem menuEditSearch;
  protected JMenuItem menuEditClearSearch;
  protected JMenuItem menuEditDeleteAttribute;
  protected JMenuItem menuEditDeleteAttributes;
  protected JMenuItem menuEditRenameAttribute;
  protected JMenuItem menuEditAttributeAsClass;
  protected JMenuItem menuEditDeleteInstance;
  protected JMenuItem menuEditDeleteInstances;
  protected JMenuItem menuEditSortInstances;
  protected JMenu menuView;
  protected JMenuItem menuViewAttributes;
  protected JMenuItem menuViewValues;
  protected JMenuItem menuViewOptimalColWidths;
  protected ConverterFileChooser fileChooser;
  protected String frameTitle;
  protected boolean confirmExit;
  protected int width;
  protected int height;
  protected int top;
  protected int left;
  protected boolean exitOnClose;
  
  public ArffViewerMainPanel(Container parentFrame)
  {
    parent = parentFrame;
    Messages.getInstance();frameTitle = Messages.getString("ArffViewerMainPanel_Title_Text");
    
    createPanel();
  }
  



  protected void createPanel()
  {
    setSize(800, 600);
    
    setConfirmExit(false);
    setLayout(new BorderLayout());
    

    fileChooser = new ConverterFileChooser(new File(System.getProperty("user.dir")));
    
    fileChooser.setMultiSelectionEnabled(true);
    

    menuBar = new JMenuBar();
    Messages.getInstance();menuFile = new JMenu(Messages.getString("ArffViewerMainPanel_CreatePanel_File_JMenu_Text"));
    
    Messages.getInstance();menuFileOpen = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_FileOpen_JMenuItem_Text"), ComponentHelper.getImageIcon("open.gif"));
    

    menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(79, 2));
    
    menuFileOpen.addActionListener(this);
    Messages.getInstance();menuFileSave = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_Save_JMenuItem_Text"), ComponentHelper.getImageIcon("save.gif"));
    

    menuFileSave.setAccelerator(KeyStroke.getKeyStroke(83, 2));
    
    menuFileSave.addActionListener(this);
    Messages.getInstance();menuFileSaveAs = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_SaveAs_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(83, 3));
    
    menuFileSaveAs.addActionListener(this);
    Messages.getInstance();menuFileClose = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_Close_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuFileClose.setAccelerator(KeyStroke.getKeyStroke(87, 2));
    
    menuFileClose.addActionListener(this);
    Messages.getInstance();menuFileCloseAll = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_CloseAll_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuFileCloseAll.addActionListener(this);
    Messages.getInstance();menuFileProperties = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_Properties_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuFileProperties.setAccelerator(KeyStroke.getKeyStroke(10, 2));
    
    menuFileProperties.addActionListener(this);
    Messages.getInstance();menuFileExit = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_Exit_JMenuItem_Text"), ComponentHelper.getImageIcon("forward.gif"));
    

    menuFileExit.setAccelerator(KeyStroke.getKeyStroke(88, 8));
    
    menuFileExit.addActionListener(this);
    menuFile.add(menuFileOpen);
    menuFile.add(menuFileSave);
    menuFile.add(menuFileSaveAs);
    menuFile.add(menuFileClose);
    menuFile.add(menuFileCloseAll);
    menuFile.addSeparator();
    menuFile.add(menuFileProperties);
    menuFile.addSeparator();
    menuFile.add(menuFileExit);
    menuBar.add(menuFile);
    
    Messages.getInstance();menuEdit = new JMenu(Messages.getString("ArffViewerMainPanel_CreatePanel_Edit_JMenuItem_Text"));
    
    Messages.getInstance();menuEditUndo = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_Undo_JMenuItem_Text"), ComponentHelper.getImageIcon("undo.gif"));
    

    menuEditUndo.setAccelerator(KeyStroke.getKeyStroke(90, 2));
    
    menuEditUndo.addActionListener(this);
    Messages.getInstance();menuEditCopy = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_Copy_JMenuItem_Text"), ComponentHelper.getImageIcon("copy.gif"));
    

    menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(155, 2));
    
    menuEditCopy.addActionListener(this);
    Messages.getInstance();menuEditSearch = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_Search_JMenuItem_Text"), ComponentHelper.getImageIcon("find.gif"));
    

    menuEditSearch.setAccelerator(KeyStroke.getKeyStroke(70, 2));
    
    menuEditSearch.addActionListener(this);
    Messages.getInstance();menuEditClearSearch = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_ClearSearch_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuEditClearSearch.setAccelerator(KeyStroke.getKeyStroke(70, 3));
    
    menuEditClearSearch.addActionListener(this);
    Messages.getInstance();menuEditRenameAttribute = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_RenameAttribute_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuEditRenameAttribute.addActionListener(this);
    Messages.getInstance();menuEditAttributeAsClass = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_AttributeAsClass_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuEditAttributeAsClass.addActionListener(this);
    Messages.getInstance();menuEditDeleteAttribute = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_DeleteAttribute_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuEditDeleteAttribute.addActionListener(this);
    Messages.getInstance();menuEditDeleteAttributes = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_DeleteAttributes_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuEditDeleteAttributes.addActionListener(this);
    Messages.getInstance();menuEditDeleteInstance = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_DeleteInstance_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuEditDeleteInstance.addActionListener(this);
    Messages.getInstance();menuEditDeleteInstances = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_DeleteInstances_JMenuItem_Text"), ComponentHelper.getImageIcon("empty.gif"));
    

    menuEditDeleteInstances.addActionListener(this);
    Messages.getInstance();menuEditSortInstances = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_SortDataAscending_JMenuItem_Text"), ComponentHelper.getImageIcon("sort.gif"));
    

    menuEditSortInstances.addActionListener(this);
    menuEdit.add(menuEditUndo);
    menuEdit.addSeparator();
    menuEdit.add(menuEditCopy);
    menuEdit.addSeparator();
    menuEdit.add(menuEditSearch);
    menuEdit.add(menuEditClearSearch);
    menuEdit.addSeparator();
    menuEdit.add(menuEditRenameAttribute);
    menuEdit.add(menuEditAttributeAsClass);
    menuEdit.add(menuEditDeleteAttribute);
    menuEdit.add(menuEditDeleteAttributes);
    menuEdit.addSeparator();
    menuEdit.add(menuEditDeleteInstance);
    menuEdit.add(menuEditDeleteInstances);
    menuEdit.add(menuEditSortInstances);
    menuBar.add(menuEdit);
    
    Messages.getInstance();menuView = new JMenu(Messages.getString("ArffViewerMainPanel_CreatePanel_View_JMenu_Text"));
    
    Messages.getInstance();menuViewAttributes = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_ViewAttributes_JMenuItem_Text"), ComponentHelper.getImageIcon("objects.gif"));
    

    menuViewAttributes.setAccelerator(KeyStroke.getKeyStroke(65, 3));
    
    menuViewAttributes.addActionListener(this);
    Messages.getInstance();menuViewValues = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_ViewValues_JMenuItem_Text"), ComponentHelper.getImageIcon("properties.gif"));
    

    menuViewValues.setAccelerator(KeyStroke.getKeyStroke(86, 3));
    
    menuViewValues.addActionListener(this);
    Messages.getInstance();menuViewOptimalColWidths = new JMenuItem(Messages.getString("ArffViewerMainPanel_CreatePanel_ViewOptimalColWidths_JMenuItem_Text"), ComponentHelper.getImageIcon("resize.gif"));
    

    menuViewOptimalColWidths.addActionListener(this);
    menuView.add(menuViewAttributes);
    menuView.add(menuViewValues);
    menuView.addSeparator();
    menuView.add(menuViewOptimalColWidths);
    menuBar.add(menuView);
    

    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(this);
    add(tabbedPane, "Center");
    
    updateMenu();
    updateFrameTitle();
  }
  




  public JFrame getParentFrame()
  {
    if ((parent instanceof JFrame)) {
      return (JFrame)parent;
    }
    return null;
  }
  




  public JInternalFrame getParentInternalFrame()
  {
    if ((parent instanceof JInternalFrame)) {
      return (JInternalFrame)parent;
    }
    return null;
  }
  




  public void setParent(Container value)
  {
    parent = value;
  }
  




  public JMenuBar getMenu()
  {
    return menuBar;
  }
  




  public JTabbedPane getTabbedPane()
  {
    return tabbedPane;
  }
  




  public void setConfirmExit(boolean confirm)
  {
    confirmExit = confirm;
  }
  





  public boolean getConfirmExit()
  {
    return confirmExit;
  }
  




  public void setExitOnClose(boolean value)
  {
    exitOnClose = value;
  }
  




  public boolean getExitOnClose()
  {
    return exitOnClose;
  }
  


  public void refresh()
  {
    validate();
    repaint();
  }
  




  public String getFrameTitle()
  {
    if (getCurrentFilename().equals("")) {
      return frameTitle;
    }
    Messages.getInstance();return frameTitle + Messages.getString("ArffViewerMainPanel_GetFrameTitle_Text") + getCurrentFilename();
  }
  




  public void updateFrameTitle()
  {
    if (getParentFrame() != null)
      getParentFrame().setTitle(getFrameTitle());
    if (getParentInternalFrame() != null) {
      getParentInternalFrame().setTitle(getFrameTitle());
    }
  }
  





  protected void updateMenu()
  {
    boolean fileOpen = getCurrentPanel() != null;
    boolean isChanged = (fileOpen) && (getCurrentPanel().isChanged());
    boolean canUndo = (fileOpen) && (getCurrentPanel().canUndo());
    

    menuFileOpen.setEnabled(true);
    menuFileSave.setEnabled(isChanged);
    menuFileSaveAs.setEnabled(fileOpen);
    menuFileClose.setEnabled(fileOpen);
    menuFileCloseAll.setEnabled(fileOpen);
    menuFileProperties.setEnabled(fileOpen);
    menuFileExit.setEnabled(true);
    
    menuEditUndo.setEnabled(canUndo);
    menuEditCopy.setEnabled(fileOpen);
    menuEditSearch.setEnabled(fileOpen);
    menuEditClearSearch.setEnabled(fileOpen);
    menuEditAttributeAsClass.setEnabled(fileOpen);
    menuEditRenameAttribute.setEnabled(fileOpen);
    menuEditDeleteAttribute.setEnabled(fileOpen);
    menuEditDeleteAttributes.setEnabled(fileOpen);
    menuEditDeleteInstance.setEnabled(fileOpen);
    menuEditDeleteInstances.setEnabled(fileOpen);
    menuEditSortInstances.setEnabled(fileOpen);
    
    menuViewAttributes.setEnabled(fileOpen);
    menuViewValues.setEnabled(fileOpen);
    menuViewOptimalColWidths.setEnabled(fileOpen);
  }
  






  protected void setTabTitle(JComponent component)
  {
    if (!(component instanceof ArffPanel)) {
      return;
    }
    int index = tabbedPane.indexOfComponent(component);
    if (index == -1) {
      return;
    }
    tabbedPane.setTitleAt(index, ((ArffPanel)component).getTitle());
    updateFrameTitle();
  }
  




  public int getPanelCount()
  {
    return tabbedPane.getTabCount();
  }
  





  public ArffPanel getPanel(int index)
  {
    if ((index >= 0) && (index < getPanelCount())) {
      return (ArffPanel)tabbedPane.getComponentAt(index);
    }
    return null;
  }
  




  public int getCurrentIndex()
  {
    return tabbedPane.getSelectedIndex();
  }
  




  public ArffPanel getCurrentPanel()
  {
    return getPanel(getCurrentIndex());
  }
  




  public boolean isPanelSelected()
  {
    return getCurrentPanel() != null;
  }
  








  public String getFilename(int index)
  {
    String result = "";
    ArffPanel panel = getPanel(index);
    
    if (panel != null) {
      result = panel.getFilename();
    }
    return result;
  }
  




  public String getCurrentFilename()
  {
    return getFilename(getCurrentIndex());
  }
  







  public void setFilename(int index, String filename)
  {
    ArffPanel panel = getPanel(index);
    
    if (panel != null) {
      panel.setFilename(filename);
      setTabTitle(panel);
    }
  }
  




  public void setCurrentFilename(String filename)
  {
    setFilename(getCurrentIndex(), filename);
  }
  





  protected boolean saveChanges()
  {
    return saveChanges(true);
  }
  









  protected boolean saveChanges(boolean showCancel)
  {
    if (!isPanelSelected()) {
      return true;
    }
    boolean result = !getCurrentPanel().isChanged();
    
    if (getCurrentPanel().isChanged()) { int button;
      try { int button;
        if (showCancel) {
          Messages.getInstance();Messages.getInstance();button = ComponentHelper.showMessageBox(this, Messages.getString("ArffViewerMainPanel_SaveChanges_ComponentHelperShowMessageBox_Text_First"), Messages.getString("ArffViewerMainPanel_SaveChanges_ComponentHelperShowMessageBox_Text_Second"), 1, 3);





        }
        else
        {





          Messages.getInstance();Messages.getInstance();button = ComponentHelper.showMessageBox(this, Messages.getString("ArffViewerMainPanel_SaveChanges_ComponentHelperShowMessageBox_Text_Third"), Messages.getString("ArffViewerMainPanel_SaveChanges_ComponentHelperShowMessageBox_Text_Fourth"), 0, 3);


        }
        



      }
      catch (Exception e)
      {


        button = 2;
      }
      
      switch (button) {
      case 0: 
        saveFile();
        result = !getCurrentPanel().isChanged();
        break;
      case 1: 
        result = true;
        break;
      case 2: 
        result = false;
      }
      
    }
    
    return result;
  }
  






  public void loadFile(String filename)
  {
    ArffPanel panel = new ArffPanel(filename);
    panel.addChangeListener(this);
    tabbedPane.addTab(panel.getTitle(), panel);
    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
  }
  






  public void loadFile()
  {
    int retVal = fileChooser.showOpenDialog(this);
    if (retVal != 0) {
      return;
    }
    setCursor(Cursor.getPredefinedCursor(3));
    
    for (int i = 0; i < fileChooser.getSelectedFiles().length; i++) {
      String filename = fileChooser.getSelectedFiles()[i].getAbsolutePath();
      loadFile(filename);
    }
    
    setCursor(Cursor.getPredefinedCursor(0));
  }
  







  public void saveFile()
  {
    ArffPanel panel = getCurrentPanel();
    if (panel == null) {
      return;
    }
    String filename = panel.getFilename();
    if (filename.equals(ArffPanel.TAB_INSTANCES)) {
      saveFileAs();
    } else {
      AbstractSaver saver = ConverterUtils.getSaverForFile(filename);
      try {
        saver.setInstances(panel.getInstances());
        saver.setFile(new File(filename));
        saver.writeBatch();
        panel.setChanged(false);
        setCurrentFilename(filename);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  






  public void saveFileAs()
  {
    ArffPanel panel = getCurrentPanel();
    if (panel == null) {
      Messages.getInstance();System.out.println(Messages.getString("ArffViewerMainPanel_SaveFileAs_Text"));
      
      return;
    }
    
    if (!getCurrentFilename().equals("")) {
      try {
        fileChooser.setSelectedFile(new File(getCurrentFilename()));
      }
      catch (Exception e) {}
    }
    

    try
    {
      fileChooser.setCapabilitiesFilter(Capabilities.forInstances(panel.getInstances()));
    }
    catch (Exception e) {
      fileChooser.setCapabilitiesFilter(null);
    }
    
    int retVal = fileChooser.showSaveDialog(this);
    if (retVal != 0) {
      return;
    }
    panel.setChanged(false);
    setCurrentFilename(fileChooser.getSelectedFile().getAbsolutePath());
    

    AbstractFileSaver saver = fileChooser.getSaver();
    saver.setInstances(panel.getInstances());
    try {
      saver.writeBatch();
      panel.setChanged(false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  


  public void closeFile()
  {
    closeFile(true);
  }
  






  public void closeFile(boolean showCancel)
  {
    if (getCurrentIndex() == -1) {
      return;
    }
    if (!saveChanges(showCancel)) {
      return;
    }
    tabbedPane.removeTabAt(getCurrentIndex());
    updateFrameTitle();
    System.gc();
  }
  


  public void closeAllFiles()
  {
    while (tabbedPane.getTabCount() > 0) {
      if (!saveChanges(true)) {
        return;
      }
      tabbedPane.removeTabAt(getCurrentIndex());
      updateFrameTitle();
      System.gc();
    }
  }
  







  public void showProperties()
  {
    ArffPanel panel = getCurrentPanel();
    if (panel == null) {
      return;
    }
    Instances inst = panel.getInstances();
    if (inst == null)
      return;
    if (inst.classIndex() < 0) {
      inst.setClassIndex(inst.numAttributes() - 1);
    }
    
    Vector props = new Vector();
    Messages.getInstance();props.add(Messages.getString("ArffViewerMainPanel_ShowProperties_Filename_Text") + panel.getFilename());
    

    Messages.getInstance();props.add(Messages.getString("ArffViewerMainPanel_ShowProperties_Filename_Text") + inst.relationName());
    

    Messages.getInstance();props.add(Messages.getString("ArffViewerMainPanel_ShowProperties_Instances_Text") + inst.numInstances());
    

    Messages.getInstance();props.add(Messages.getString("ArffViewerMainPanel_ShowProperties_Attributes_Text") + inst.numAttributes());
    

    Messages.getInstance();props.add(Messages.getString("ArffViewerMainPanel_ShowProperties_ClassAttribute_Text") + inst.classAttribute().name());
    

    Messages.getInstance();props.add(Messages.getString("ArffViewerMainPanel_ShowProperties_ClassLabels_Text") + inst.numClasses());
    


    ListSelectorDialog dialog = new ListSelectorDialog(getParentFrame(), new JList(props));
    dialog.showDialog();
  }
  



  public void close()
  {
    if (getParentInternalFrame() != null) {
      getParentInternalFrame().doDefaultCloseAction();
    } else if (getParentFrame() != null) {
      getParentFrame().dispatchEvent(new WindowEvent(getParentFrame(), 201));
    }
  }
  


  public void undo()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().undo();
  }
  


  public void copyContent()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().copyContent();
  }
  


  public void search()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().search();
  }
  


  public void clearSearch()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().clearSearch();
  }
  


  public void renameAttribute()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().renameAttribute();
  }
  



  public void attributeAsClass()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().attributeAsClass();
  }
  




  public void deleteAttribute(boolean multiple)
  {
    if (!isPanelSelected()) {
      return;
    }
    if (multiple) {
      getCurrentPanel().deleteAttributes();
    } else {
      getCurrentPanel().deleteAttribute();
    }
  }
  



  public void deleteInstance(boolean multiple)
  {
    if (!isPanelSelected()) {
      return;
    }
    if (multiple) {
      getCurrentPanel().deleteInstances();
    } else {
      getCurrentPanel().deleteInstance();
    }
  }
  

  public void sortInstances()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().sortInstances();
  }
  











  public String showAttributes()
  {
    if (!isPanelSelected()) {
      return null;
    }
    JList list = new JList(getCurrentPanel().getAttributes());
    ListSelectorDialog dialog = new ListSelectorDialog(getParentFrame(), list);
    int result = dialog.showDialog();
    
    if (result == 0) {
      ArffSortedTableModel model = (ArffSortedTableModel)getCurrentPanel().getTable().getModel();
      String name = list.getSelectedValue().toString();
      int i = model.getAttributeColumn(name);
      JTableHelper.scrollToVisible(getCurrentPanel().getTable(), 0, i);
      getCurrentPanel().getTable().setSelectedColumn(i);
      return name;
    }
    return null;
  }
  














  public void showValues()
  {
    String attribute = showAttributes();
    if (attribute == null) {
      return;
    }
    ArffTable table = getCurrentPanel().getTable();
    ArffSortedTableModel model = (ArffSortedTableModel)table.getModel();
    

    int col = -1;
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (table.getPlainColumnName(i).equals(attribute)) {
        col = i;
        break;
      }
    }
    
    if (col == -1) {
      return;
    }
    
    HashSet values = new HashSet();
    Vector items = new Vector();
    for (i = 0; i < model.getRowCount(); i++)
      values.add(model.getValueAt(i, col).toString());
    if (values.isEmpty())
      return;
    Iterator iter = values.iterator();
    while (iter.hasNext())
      items.add(iter.next());
    Collections.sort(items);
    
    ListSelectorDialog dialog = new ListSelectorDialog(getParentFrame(), new JList(items));
    dialog.showDialog();
  }
  


  public void setOptimalColWidths()
  {
    if (!isPanelSelected()) {
      return;
    }
    getCurrentPanel().setOptimalColWidths();
  }
  







  public void actionPerformed(ActionEvent e)
  {
    Object o = e.getSource();
    
    if (o == menuFileOpen) {
      loadFile();
    } else if (o == menuFileSave) {
      saveFile();
    } else if (o == menuFileSaveAs) {
      saveFileAs();
    } else if (o == menuFileClose) {
      closeFile();
    } else if (o == menuFileCloseAll) {
      closeAllFiles();
    } else if (o == menuFileProperties) {
      showProperties();
    } else if (o == menuFileExit) {
      close();
    } else if (o == menuEditUndo) {
      undo();
    } else if (o == menuEditCopy) {
      copyContent();
    } else if (o == menuEditSearch) {
      search();
    } else if (o == menuEditClearSearch) {
      clearSearch();
    } else if (o == menuEditDeleteAttribute) {
      deleteAttribute(false);
    } else if (o == menuEditDeleteAttributes) {
      deleteAttribute(true);
    } else if (o == menuEditRenameAttribute) {
      renameAttribute();
    } else if (o == menuEditAttributeAsClass) {
      attributeAsClass();
    } else if (o == menuEditDeleteInstance) {
      deleteInstance(false);
    } else if (o == menuEditDeleteInstances) {
      deleteInstance(true);
    } else if (o == menuEditSortInstances) {
      sortInstances();
    } else if (o == menuViewAttributes) {
      showAttributes();
    } else if (o == menuViewValues) {
      showValues();
    } else if (o == menuViewOptimalColWidths) {
      setOptimalColWidths();
    }
    updateMenu();
  }
  





  public void stateChanged(ChangeEvent e)
  {
    updateFrameTitle();
    updateMenu();
    

    if ((e.getSource() instanceof JComponent)) {
      setTabTitle((JComponent)e.getSource());
    }
  }
  




  public String toString()
  {
    return getClass().getName();
  }
}

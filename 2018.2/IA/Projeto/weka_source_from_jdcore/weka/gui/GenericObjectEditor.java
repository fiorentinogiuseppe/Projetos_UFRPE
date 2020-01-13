package weka.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.ClassDiscovery;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;





















































public class GenericObjectEditor
  implements PropertyEditor, CustomPanelSupplier
{
  protected Object m_Object;
  protected Object m_Backup;
  protected PropertyChangeSupport m_Support = new PropertyChangeSupport(this);
  

  protected Class m_ClassType;
  

  protected Hashtable m_ObjectNames;
  

  protected GOEPanel m_EditorComponent;
  

  protected boolean m_Enabled = true;
  

  protected static String PROPERTY_FILE = "weka/gui/GenericObjectEditor.props";
  

  protected static Properties EDITOR_PROPERTIES;
  

  public static final String GUIEDITORS_PROPERTY_FILE = "weka/gui/GUIEditors.props";
  

  protected GOETreeNode m_treeNodeOfCurrentObject;
  

  protected PropertyPanel m_ObjectPropertyPanel;
  

  protected boolean m_canChangeClassInDialog;
  

  protected static boolean m_EditorsRegistered;
  

  protected Capabilities m_CapabilitiesFilter = null;
  






  static
  {
    try
    {
      GenericPropertiesCreator creator = new GenericPropertiesCreator();
      

      if (creator.useDynamic()) {
        try {
          creator.execute(false);
          EDITOR_PROPERTIES = creator.getOutputProperties();
        } catch (Exception e) {
          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("GenericObjectEditor_JOptionPaneShowMessageDialog_Text_First") + e.toString(), Messages.getString("GenericObjectEditor_JOptionPaneShowMessageDialog_Text_Second"), 0);

        }
        

      }
      else
      {

        try
        {

          EDITOR_PROPERTIES = Utils.readProperties(PROPERTY_FILE);
          Enumeration keys = EDITOR_PROPERTIES.propertyNames();
          if (!keys.hasMoreElements()) {
            Messages.getInstance();throw new Exception(Messages.getString("GenericObjectEditor_Exception_Text"));
          }
        }
        catch (Exception ex) {
          Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("GenericObjectEditor_Exception_JOptionPaneShowMessageDialog_Text_First") + PROPERTY_FILE + Messages.getString("GenericObjectEditor_Exception_JOptionPaneShowMessageDialog_Text_Second") + System.getProperties().getProperty("user.home") + Messages.getString("GenericObjectEditor_Exception_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("GenericObjectEditor_Exception_JOptionPaneShowMessageDialog_Text_Fourth"), 0);




        }
        




      }
      





    }
    catch (Exception e)
    {




      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("GenericObjectEditor_Exception_JOptionPaneShowMessageDialog_Text_Fifth") + e.toString(), Messages.getString("GenericObjectEditor_Exception_JOptionPaneShowMessageDialog_Text_Sixth"), 0);
    }
  }
  






  public class GOETreeNode
    extends DefaultMutableTreeNode
  {
    static final long serialVersionUID = -1707872446682150133L;
    




    public static final String NO_SUPPORT = "silver";
    




    public static final String MAYBE_SUPPORT = "blue";
    




    protected Capabilities m_Capabilities = null;
    




    protected String m_toolTipText;
    





    public GOETreeNode() {}
    




    public GOETreeNode(Object userObject)
    {
      super();
    }
    








    public GOETreeNode(Object userObject, boolean allowsChildren)
    {
      super(allowsChildren);
    }
    




    public void setToolTipText(String tip)
    {
      m_toolTipText = tip;
    }
    




    public String getToolTipText()
    {
      return m_toolTipText;
    }
    






    protected void initCapabilities()
    {
      if (m_Capabilities != null) {
        return;
      }
      if (!isLeaf()) {
        return;
      }
      
      String classname = getClassnameFromPath(new TreePath(getPath()));
      try {
        Class cls = Class.forName(classname);
        if (!ClassDiscovery.hasInterface(CapabilitiesHandler.class, cls)) {
          return;
        }
        
        Object obj = cls.newInstance();
        m_Capabilities = ((CapabilitiesHandler)obj).getCapabilities();
      }
      catch (Exception e) {}
    }
    








    public String toString()
    {
      String result = super.toString();
      
      if (m_CapabilitiesFilter != null) {
        initCapabilities();
        if (m_Capabilities != null) {
          if ((m_Capabilities.supportsMaybe(m_CapabilitiesFilter)) && (!m_Capabilities.supports(m_CapabilitiesFilter)))
          {
            result = "<html><font color=\"blue\">" + result + "</font><html>";



          }
          else if (!m_Capabilities.supports(m_CapabilitiesFilter)) {
            result = "<html><font color=\"silver\">" + result + "</font><html>";
          }
        }
      }
      




      return result;
    }
  }
  



  public class CapabilitiesFilterDialog
    extends JDialog
  {
    static final long serialVersionUID = -7845503345689646266L;
    

    protected JDialog m_Self;
    

    protected JPopupMenu m_Popup = null;
    

    protected Capabilities m_Capabilities = new Capabilities(null);
    

    protected JLabel m_InfoLabel = new JLabel();
    

    protected CheckBoxList m_List = new CheckBoxList();
    protected JButton m_OkButton;
    
    public CapabilitiesFilterDialog() { Messages.getInstance();m_OkButton = new JButton(Messages.getString("GenericObjectEditor_CapabilitiesFilterDialog_OkButton_JButton_Text"));
      



      Messages.getInstance();m_CancelButton = new JButton(Messages.getString("GenericObjectEditor_CapabilitiesFilterDialog_CancelButton_JButton_Text"));
      










      m_Self = this;
      
      initGUI();
    }
    





    protected void initGUI()
    {
      Messages.getInstance();setTitle(Messages.getString("GenericObjectEditor_InitGUI_SetTitle_Text"));
      
      setLayout(new BorderLayout());
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      getContentPane().add(panel, "North");
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();m_InfoLabel.setText(Messages.getString("GenericObjectEditor_InitGUI_InfoLabel_SetTitle_Text_First") + m_ClassType.getName().replaceAll(".*\\.", "") + Messages.getString("GenericObjectEditor_InitGUI_InfoLabel_SetTitle_Text_Second") + "silver" + Messages.getString("GenericObjectEditor_InitGUI_InfoLabel_SetTitle_Text_Third") + "silver" + Messages.getString("GenericObjectEditor_InitGUI_InfoLabel_SetTitle_Text_Fourth") + "blue" + Messages.getString("GenericObjectEditor_InitGUI_InfoLabel_SetTitle_Text_Fifth") + "blue" + Messages.getString("GenericObjectEditor_InitGUI_InfoLabel_SetTitle_Text_Sixth"));
      















      panel.add(m_InfoLabel, "Center");
      

      getContentPane().add(new JScrollPane(m_List), "Center");
      CheckBoxList.CheckBoxListModel model = (CheckBoxList.CheckBoxListModel)m_List.getModel();
      for (Capabilities.Capability cap : Capabilities.Capability.values()) {
        model.addElement(cap);
      }
      

      panel = new JPanel(new FlowLayout(1));
      getContentPane().add(panel, "South");
      
      m_OkButton.setMnemonic('O');
      m_OkButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          updateCapabilities();
          if (m_CapabilitiesFilter == null) {
            m_CapabilitiesFilter = new Capabilities(null);
          }
          m_CapabilitiesFilter.assign(m_Capabilities);
          m_Self.setVisible(false);
          showPopup();
        }
      });
      panel.add(m_OkButton);
      
      m_CancelButton.setMnemonic('C');
      m_CancelButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_Self.setVisible(false);
          showPopup();
        }
      });
      panel.add(m_CancelButton);
      pack();
    }
    







    protected void updateList()
    {
      CheckBoxList.CheckBoxListModel model = (CheckBoxList.CheckBoxListModel)m_List.getModel();
      
      for (Capabilities.Capability cap : Capabilities.Capability.values()) {
        model.setChecked(model.indexOf(cap), m_Capabilities.handles(cap));
      }
    }
    




    protected JButton m_CancelButton;
    


    protected void updateCapabilities()
    {
      CheckBoxList.CheckBoxListModel model = (CheckBoxList.CheckBoxListModel)m_List.getModel();
      
      for (Capabilities.Capability cap : Capabilities.Capability.values()) {
        if (model.getChecked(model.indexOf(cap))) {
          m_Capabilities.enable(cap);
        } else {
          m_Capabilities.disable(cap);
        }
      }
    }
    




    public void setCapabilities(Capabilities value)
    {
      if (value != null) {
        m_Capabilities.assign(value);
      } else {
        m_Capabilities = new Capabilities(null);
      }
      
      updateList();
    }
    




    public Capabilities getCapabilities()
    {
      return m_Capabilities;
    }
    




    public void setPopup(JPopupMenu value)
    {
      m_Popup = value;
    }
    




    public JPopupMenu getPopup()
    {
      return m_Popup;
    }
    



    public void showPopup()
    {
      if (getPopup() != null) {
        getPopup().setVisible(true);
      }
    }
  }
  

  public class JTreePopupMenu
    extends JPopupMenu
  {
    static final long serialVersionUID = -3404546329655057387L;
    
    private final JPopupMenu m_Self;
    
    private final JTree m_tree;
    
    private final JScrollPane m_scroller;
    
    private final JButton m_FilterButton;
    
    private final JButton m_RemoveFilterButton;
    private final JButton m_CloseButton;
    
    public JTreePopupMenu(JTree tree)
    {
      Messages.getInstance();m_FilterButton = new JButton(Messages.getString("GenericObjectEditor_JTreePopupMenu_FilterButton_JButton_Text"));
      



      Messages.getInstance();m_RemoveFilterButton = new JButton(Messages.getString("GenericObjectEditor_JTreePopupMenu_RemoveFilterButton_JButton_Text"));
      



      Messages.getInstance();m_CloseButton = new JButton(Messages.getString("GenericObjectEditor_JTreePopupMenu_CloseButton_JButton_Text"));
      








      m_Self = this;
      
      setLayout(new BorderLayout());
      JPanel panel = new JPanel(new FlowLayout(2));
      add(panel, "South");
      
      if (ClassDiscovery.hasInterface(CapabilitiesHandler.class, m_ClassType))
      {
        m_FilterButton.setMnemonic('F');
        m_FilterButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e) {
            if (e.getSource() == m_FilterButton) {
              GenericObjectEditor.CapabilitiesFilterDialog dialog = new GenericObjectEditor.CapabilitiesFilterDialog(GenericObjectEditor.this);
              dialog.setCapabilities(m_CapabilitiesFilter);
              dialog.setPopup(m_Self);
              dialog.setVisible(true);
              m_Support.firePropertyChange("", null, null);
              repaint();
            }
          }
        });
        panel.add(m_FilterButton);
        

        m_RemoveFilterButton.setMnemonic('R');
        m_RemoveFilterButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e) {
            if (e.getSource() == m_RemoveFilterButton) {
              m_CapabilitiesFilter = null;
              m_Support.firePropertyChange("", null, null);
              repaint();
            }
          }
        });
        panel.add(m_RemoveFilterButton);
      }
      

      m_CloseButton.setMnemonic('C');
      m_CloseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == m_CloseButton) {
            m_Self.setVisible(false);
          }
        }
      });
      panel.add(m_CloseButton);
      
      m_tree = tree;
      
      JPanel treeView = new JPanel();
      treeView.setLayout(new BorderLayout());
      treeView.add(m_tree, "North");
      

      treeView.setBackground(m_tree.getBackground());
      
      m_scroller = new JScrollPane(treeView);
      
      m_scroller.setPreferredSize(new Dimension(300, 400));
      m_scroller.getVerticalScrollBar().setUnitIncrement(20);
      
      add(m_scroller);
    }
    








    public void show(Component invoker, int x, int y)
    {
      super.show(invoker, x, y);
      

      Point location = getLocationOnScreen();
      Dimension screenSize = getToolkit().getScreenSize();
      int maxWidth = (int)(screenSize.getWidth() - location.getX());
      int maxHeight = (int)(screenSize.getHeight() - location.getY());
      

      Dimension scrollerSize = m_scroller.getPreferredSize();
      int height = (int)scrollerSize.getHeight();
      int width = (int)scrollerSize.getWidth();
      if (width > maxWidth) {
        width = maxWidth;
      }
      if (height > maxHeight) {
        height = maxHeight;
      }
      

      m_scroller.setPreferredSize(new Dimension(width, height));
      revalidate();
      pack();
    }
  }
  


  public class GOEPanel
    extends JPanel
  {
    static final long serialVersionUID = 3656028520876011335L;
    

    protected PropertySheetPanel m_ChildPropertySheet;
    

    protected JLabel m_ClassNameLabel;
    

    protected JButton m_OpenBut;
    

    protected JButton m_SaveBut;
    

    protected JButton m_okBut;
    

    protected JButton m_cancelBut;
    

    protected JFileChooser m_FileChooser;
    


    public GOEPanel()
    {
      m_Backup = copyObject(m_Object);
      
      Messages.getInstance();m_ClassNameLabel = new JLabel(Messages.getString("GenericObjectEditor_GOEPanel_ClassNameLabel_JLabel_Text"));
      
      m_ClassNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      
      m_ChildPropertySheet = new PropertySheetPanel();
      m_ChildPropertySheet.addPropertyChangeListener(new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent evt)
        {
          m_Support.firePropertyChange("", null, null);
        }
        
      });
      Messages.getInstance();m_OpenBut = new JButton(Messages.getString("GenericObjectEditor_OpenBut_JButton_Text"));
      
      Messages.getInstance();m_OpenBut.setToolTipText(Messages.getString("GenericObjectEditor_OpenBut_SetToolTipText_Text"));
      
      m_OpenBut.setEnabled(true);
      m_OpenBut.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          Object object = openObject();
          if (object != null)
          {

            setValue(object);
            

            setValue(object);
          }
          
        }
      });
      Messages.getInstance();m_SaveBut = new JButton(Messages.getString("GenericObjectEditor_SaveBut_JButton_Text"));
      
      Messages.getInstance();m_SaveBut.setToolTipText(Messages.getString("GenericObjectEditor_SaveBut_SetToolTipText_Text"));
      
      m_SaveBut.setEnabled(true);
      m_SaveBut.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          saveObject(m_Object);
        }
        
      });
      Messages.getInstance();m_okBut = new JButton(Messages.getString("GenericObjectEditor_OkBut_JButton_Text"));
      
      m_okBut.setEnabled(true);
      m_okBut.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          m_Backup = copyObject(m_Object);
          if ((getTopLevelAncestor() != null) && ((getTopLevelAncestor() instanceof Window)))
          {
            Window w = (Window)getTopLevelAncestor();
            w.dispose();
          }
          
        }
      });
      Messages.getInstance();m_cancelBut = new JButton(Messages.getString("GenericObjectEditor_CancelBut_JButton_Text"));
      
      m_cancelBut.setEnabled(true);
      m_cancelBut.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          if (m_Backup != null)
          {
            m_Object = copyObject(m_Backup);
            

            m_Support.firePropertyChange("", null, null);
            m_ObjectNames = getClassesFromProperties();
            updateObjectNames();
            updateChildPropertySheet();
          }
          if ((getTopLevelAncestor() != null) && ((getTopLevelAncestor() instanceof Window)))
          {
            Window w = (Window)getTopLevelAncestor();
            w.dispose();
          }
          
        }
      });
      setLayout(new BorderLayout());
      JButton chooseButton;
      if (m_canChangeClassInDialog) {
        chooseButton = createChooseClassButton();
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        top.add(chooseButton, "West");
        top.add(m_ClassNameLabel, "Center");
        add(top, "North");
      } else {
        add(m_ClassNameLabel, "North");
      }
      
      add(m_ChildPropertySheet, "Center");
      



      JPanel okcButs = new JPanel();
      okcButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      okcButs.setLayout(new GridLayout(1, 4, 5, 5));
      okcButs.add(m_OpenBut);
      okcButs.add(m_SaveBut);
      okcButs.add(m_okBut);
      okcButs.add(m_cancelBut);
      add(okcButs, "South");
      
      if (m_ClassType != null) {
        m_ObjectNames = getClassesFromProperties();
        if (m_Object != null) {
          updateObjectNames();
          updateChildPropertySheet();
        }
      }
    }
    





    protected void setCancelButton(boolean flag)
    {
      if (m_cancelBut != null) {
        m_cancelBut.setEnabled(flag);
      }
    }
    





    protected Object openObject()
    {
      if (m_FileChooser == null) {
        createFileChooser();
      }
      int returnVal = m_FileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        File selected = m_FileChooser.getSelectedFile();
        try {
          ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
          
          Object obj = oi.readObject();
          oi.close();
          if (!m_ClassType.isAssignableFrom(obj.getClass())) {
            Messages.getInstance();throw new Exception(Messages.getString("GenericObjectEditor_OpenObject_Exception_Text") + m_ClassType.getName());
          }
          

          return obj;
        } catch (Exception ex) {
          Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("GenericObjectEditor_OpenObject_Exception_JOptionPaneShowMessageDialog_Text") + selected.getName() + Messages.getString("GenericObjectEditor_OpenObject_Exception_JOptionPaneShowMessageDialog_Text") + ex.getMessage(), Messages.getString("GenericObjectEditor_OpenObject_Exception_JOptionPaneShowMessageDialog_Text"), 0);
        }
      }
      
















      return null;
    }
    





    protected void saveObject(Object object)
    {
      if (m_FileChooser == null) {
        createFileChooser();
      }
      int returnVal = m_FileChooser.showSaveDialog(this);
      if (returnVal == 0) {
        File sFile = m_FileChooser.getSelectedFile();
        try {
          ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
          
          oo.writeObject(object);
          oo.close();
        } catch (Exception ex) {
          Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("GenericObjectEditor_SaveObject_Exception_JOptionPaneShowMessageDialog_Text") + sFile.getName() + Messages.getString("GenericObjectEditor_SaveObject_Exception_JOptionPaneShowMessageDialog_Text") + ex.getMessage(), Messages.getString("GenericObjectEditor_SaveObject_Exception_JOptionPaneShowMessageDialog_Text"), 0);
        }
      }
    }
    




















    protected void createFileChooser()
    {
      m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
      m_FileChooser.setFileSelectionMode(0);
    }
    






    protected Object copyObject(Object source)
    {
      Object result = null;
      try {
        result = GenericObjectEditor.makeCopy(source);
        setCancelButton(true);
      }
      catch (Exception ex) {
        setCancelButton(false);
        Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_CopyObject_Error_Text"));
        
        System.err.println(ex);
      }
      return result;
    }
    





    public void setOkButtonText(String newLabel)
    {
      m_okBut.setText(newLabel);
    }
    





    public void addOkListener(ActionListener a)
    {
      m_okBut.addActionListener(a);
    }
    





    public void addCancelListener(ActionListener a)
    {
      m_cancelBut.addActionListener(a);
    }
    





    public void removeOkListener(ActionListener a)
    {
      m_okBut.removeActionListener(a);
    }
    





    public void removeCancelListener(ActionListener a)
    {
      m_cancelBut.removeActionListener(a);
    }
    




    public void updateChildPropertySheet()
    {
      Messages.getInstance();String className = Messages.getString("GenericObjectEditor_UpdateChildPropertySheet_ClassName_Text");
      
      if (m_Object != null) {
        className = m_Object.getClass().getName();
      }
      m_ClassNameLabel.setText(className);
      

      m_ChildPropertySheet.setTarget(m_Object);
      

      if ((getTopLevelAncestor() != null) && ((getTopLevelAncestor() instanceof Window)))
      {
        ((Window)getTopLevelAncestor()).pack();
      }
    }
  }
  



  public GenericObjectEditor()
  {
    this(false);
  }
  






  public GenericObjectEditor(boolean canChangeClassInDialog)
  {
    m_canChangeClassInDialog = canChangeClassInDialog;
  }
  









  public static void registerEditors()
  {
    if (m_EditorsRegistered) {
      return;
    }
    
    Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_RegisterEditors_Error_Text"));
    
    m_EditorsRegistered = true;
    Properties props;
    try
    {
      props = Utils.readProperties("weka/gui/GUIEditors.props");
    } catch (Exception e) {
      props = new Properties();
      e.printStackTrace();
    }
    
    Enumeration enm = props.propertyNames();
    while (enm.hasMoreElements()) {
      String name = enm.nextElement().toString();
      String value = props.getProperty(name, "");
      try { Class cls;
        Class cls;
        if (name.endsWith("[]")) {
          Class baseCls = Class.forName(name.substring(0, name.indexOf("[]")));
          cls = Array.newInstance(baseCls, 1).getClass();
        } else {
          cls = Class.forName(name);
        }
        
        PropertyEditorManager.registerEditor(cls, Class.forName(value));
      } catch (Exception e) {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_RegisterEditors_Exception_Error_Text_First") + name + Messages.getString("GenericObjectEditor_RegisterEditors_Exception_Error_Text_Second") + value + Messages.getString("GenericObjectEditor_RegisterEditors_Exception_Error_Text_Third") + e);
      }
    }
  }
  












  public void setCanChangeClassInDialog(boolean value)
  {
    m_canChangeClassInDialog = value;
  }
  




  public boolean getCanChangeClassInDialog()
  {
    return m_canChangeClassInDialog;
  }
  




  public Object getBackup()
  {
    return m_Backup;
  }
  







  protected static String getRootFromClass(String clsname, String separator)
  {
    if (clsname.indexOf(separator) > -1) {
      return clsname.substring(0, clsname.indexOf(separator));
    }
    return null;
  }
  























  public static Hashtable sortClassesByRoot(String classes)
  {
    if (classes == null) {
      return null;
    }
    
    Hashtable roots = new Hashtable();
    HierarchyPropertyParser hpp = new HierarchyPropertyParser();
    String separator = hpp.getSeperator();
    


    StringTokenizer tok = new StringTokenizer(classes, ", ");
    while (tok.hasMoreElements()) {
      String clsname = tok.nextToken();
      String root = getRootFromClass(clsname, separator);
      if (root != null)
      {
        Vector list;
        

        if (!roots.containsKey(root)) {
          Vector list = new Vector();
          roots.put(root, list);
        } else {
          list = (Vector)roots.get(root);
        }
        
        list.add(clsname);
      }
    }
    
    Hashtable result = new Hashtable();
    Enumeration enm = roots.keys();
    while (enm.hasMoreElements()) {
      String root = (String)enm.nextElement();
      Vector list = (Vector)roots.get(root);
      String tmpStr = "";
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) {
          tmpStr = tmpStr + ",";
        }
        tmpStr = tmpStr + (String)list.get(i);
      }
      result.put(root, tmpStr);
    }
    
    return result;
  }
  






  protected Hashtable getClassesFromProperties()
  {
    Hashtable hpps = new Hashtable();
    String className = m_ClassType.getName();
    Hashtable typeOptions = sortClassesByRoot(EDITOR_PROPERTIES.getProperty(className));
    
    if (typeOptions != null)
    {

      try
      {


        Enumeration enm = typeOptions.keys();
        while (enm.hasMoreElements()) {
          String root = (String)enm.nextElement();
          String typeOption = (String)typeOptions.get(root);
          HierarchyPropertyParser hpp = new HierarchyPropertyParser();
          hpp.build(typeOption, ", ");
          hpps.put(root, hpp);
        }
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_GetClassesFromProperties_Exception_Error_Text") + typeOptions);
      }
    }
    

    return hpps;
  }
  




  protected void updateObjectNames()
  {
    if (m_ObjectNames == null) {
      m_ObjectNames = getClassesFromProperties();
    }
    
    if (m_Object != null) {
      String className = m_Object.getClass().getName();
      String root = getRootFromClass(className, new HierarchyPropertyParser().getSeperator());
      
      HierarchyPropertyParser hpp = (HierarchyPropertyParser)m_ObjectNames.get(root);
      
      if ((hpp != null) && 
        (!hpp.contains(className))) {
        hpp.add(className);
      }
    }
  }
  







  public void setEnabled(boolean newVal)
  {
    if (newVal != m_Enabled) {
      m_Enabled = newVal;
    }
  }
  





  public void setClassType(Class type)
  {
    m_ClassType = type;
    m_ObjectNames = getClassesFromProperties();
  }
  




  public void setDefaultValue()
  {
    if (m_ClassType == null) {
      Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_SetDefaultValue_Error_Text"));
      
      return;
    }
    
    Hashtable hpps = getClassesFromProperties();
    HierarchyPropertyParser hpp = null;
    Enumeration enm = hpps.elements();
    try
    {
      while (enm.hasMoreElements()) {
        hpp = (HierarchyPropertyParser)enm.nextElement();
        if (hpp.depth() > 0) {
          hpp.goToRoot();
          while (!hpp.isLeafReached()) {
            hpp.goToChild(0);
          }
          
          String defaultValue = hpp.fullValue();
          setValue(Class.forName(defaultValue).newInstance());
        }
      }
    } catch (Exception ex) {
      Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_SetDefaultValue_Exception_Error_Text") + hpp.fullValue());
      

      ex.printStackTrace();
    }
  }
  







  public void setValue(Object o)
  {
    if (m_ClassType == null) {
      Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_SetValue_Error_Text_First"));
      
      return;
    }
    if (!m_ClassType.isAssignableFrom(o.getClass())) {
      Messages.getInstance();System.err.println(Messages.getString("GenericObjectEditor_SetValue_Error_Text_Second"));
      
      return;
    }
    
    setObject(o);
    
    if (m_EditorComponent != null) {
      m_EditorComponent.repaint();
    }
    
    updateObjectNames();
  }
  


  protected void setObject(Object c)
  {
    boolean trueChange;
    

    boolean trueChange;
    
    if (getValue() != null) {
      trueChange = !c.equals(getValue());
    } else {
      trueChange = true;
    }
    
    m_Backup = m_Object;
    
    m_Object = c;
    
    if (m_EditorComponent != null) {
      m_EditorComponent.updateChildPropertySheet();
    }
    if (trueChange) {
      m_Support.firePropertyChange("", null, null);
    }
  }
  






  public Object getValue()
  {
    Object result = null;
    try {
      result = makeCopy(m_Object);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }
  









  public String getJavaInitializationString()
  {
    return "new " + m_Object.getClass().getName() + "()";
  }
  






  public boolean isPaintable()
  {
    return true;
  }
  







  public void paintValue(Graphics gfx, Rectangle box)
  {
    if (m_Enabled) { String rep;
      String rep;
      if (m_Object != null) {
        rep = m_Object.getClass().getName();
      } else {
        Messages.getInstance();rep = Messages.getString("GenericObjectEditor_PaintValue_Rep_Text");
      }
      
      int dotPos = rep.lastIndexOf('.');
      if (dotPos != -1) {
        rep = rep.substring(dotPos + 1);
      }
      



      Font originalFont = gfx.getFont();
      gfx.setFont(originalFont.deriveFont(1));
      
      FontMetrics fm = gfx.getFontMetrics();
      int vpad = height - fm.getHeight();
      gfx.drawString(rep, 2, fm.getAscent() + vpad);
      int repwidth = fm.stringWidth(rep);
      
      gfx.setFont(originalFont);
      if ((m_Object instanceof OptionHandler)) {
        gfx.drawString(" " + Utils.joinOptions(((OptionHandler)m_Object).getOptions()), repwidth + 2, fm.getAscent() + vpad);
      }
    }
  }
  








  public String getAsText()
  {
    return null;
  }
  








  public void setAsText(String text)
  {
    throw new IllegalArgumentException(text);
  }
  






  public String[] getTags()
  {
    return null;
  }
  






  public boolean supportsCustomEditor()
  {
    return true;
  }
  






  public Component getCustomEditor()
  {
    if (m_EditorComponent == null) {
      m_EditorComponent = new GOEPanel();
    }
    return m_EditorComponent;
  }
  






  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.addPropertyChangeListener(l);
  }
  






  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.removePropertyChangeListener(l);
  }
  





  public JPanel getCustomPanel()
  {
    final JButton chooseButton = createChooseClassButton();
    m_ObjectPropertyPanel = new PropertyPanel(this, true);
    
    JPanel customPanel = new JPanel()
    {
      public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        chooseButton.setEnabled(enabled);
      }
    };
    customPanel.setLayout(new BorderLayout());
    customPanel.add(chooseButton, "West");
    customPanel.add(m_ObjectPropertyPanel, "Center");
    return customPanel;
  }
  






  protected JButton createChooseClassButton()
  {
    Messages.getInstance();JButton setButton = new JButton(Messages.getString("GenericObjectEditor_CreateChooseClassButton_SetButton_JButton_Text"));
    



    setButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        JPopupMenu popup = getChooseClassPopupMenu();
        

        if ((e.getSource() instanceof Component)) {
          Component comp = (Component)e.getSource();
          popup.show(comp, comp.getX(), comp.getY());
          popup.pack();
          popup.repaint();
        }
        
      }
    });
    return setButton;
  }
  





  protected String getClassnameFromPath(TreePath path)
  {
    StringBuffer classname = new StringBuffer();
    

    int start = 0;
    if (m_ObjectNames.size() > 1) {
      start = 1;
    }
    
    for (int i = start; i < path.getPathCount(); i++) {
      if (i > start) {
        classname.append(".");
      }
      classname.append((String)((GOETreeNode)path.getPathComponent(i)).getUserObject());
    }
    

    return classname.toString();
  }
  





  public JPopupMenu getChooseClassPopupMenu()
  {
    updateObjectNames();
    

    m_treeNodeOfCurrentObject = null;
    final JTree tree = createTree(m_ObjectNames);
    if (m_treeNodeOfCurrentObject != null) {
      tree.setSelectionPath(new TreePath(m_treeNodeOfCurrentObject.getPath()));
    }
    tree.getSelectionModel().setSelectionMode(1);
    


    final JPopupMenu popup = new JTreePopupMenu(tree);
    

    tree.addTreeSelectionListener(new TreeSelectionListener()
    {
      public void valueChanged(TreeSelectionEvent e) {
        GenericObjectEditor.GOETreeNode node = (GenericObjectEditor.GOETreeNode)tree.getLastSelectedPathComponent();
        
        if (node == null) {
          return;
        }
        
        if (node.isLeaf())
        {




          classSelected(getClassnameFromPath(tree.getSelectionPath()));
          popup.setVisible(false);
        }
        
      }
    });
    return popup;
  }
  



  protected JTree createTree(Hashtable hpps)
  {
    GOETreeNode superRoot;
    

    GOETreeNode superRoot;
    

    if (hpps.size() > 1) {
      Messages.getInstance();superRoot = new GOETreeNode(Messages.getString("GenericObjectEditor_CreateTree_GOETreeNode_Text"));
    }
    else {
      superRoot = null;
    }
    
    Enumeration enm = hpps.elements();
    while (enm.hasMoreElements()) {
      HierarchyPropertyParser hpp = (HierarchyPropertyParser)enm.nextElement();
      hpp.goToRoot();
      GOETreeNode root = new GOETreeNode(hpp.getValue());
      addChildrenToTree(root, hpp);
      
      if (superRoot == null) {
        superRoot = root;
      } else {
        superRoot.add(root);
      }
    }
    
    JTree tree = new JTree(superRoot)
    {
      private static final long serialVersionUID = 6991903188102450549L;
      

      public String getToolTipText(MouseEvent e)
      {
        if (getRowForLocation(e.getX(), e.getY()) == -1) {
          return null;
        }
        TreePath currPath = getPathForLocation(e.getX(), e.getY());
        if ((currPath.getLastPathComponent() instanceof DefaultMutableTreeNode)) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)currPath.getLastPathComponent();
          

          if (node.isLeaf())
          {
            return ((GenericObjectEditor.GOETreeNode)node).getToolTipText();
          }
        }
        return null;
      }
    };
    tree.setToolTipText("");
    
    return tree;
  }
  







  protected void addChildrenToTree(GOETreeNode tree, HierarchyPropertyParser hpp)
  {
    try
    {
      for (int i = 0; i < hpp.numChildren(); i++) {
        hpp.goToChild(i);
        GOETreeNode child = new GOETreeNode(hpp.getValue());
        if ((m_Object != null) && (m_Object.getClass().getName().equals(hpp.fullValue())))
        {
          m_treeNodeOfCurrentObject = child;
        }
        tree.add(child);
        
        if (hpp.isLeafReached()) {
          String algName = hpp.fullValue();
          try {
            Object alg = Class.forName(algName).newInstance();
            String toolTip = Utils.getGlobalInfo(alg, true);
            if (toolTip != null) {
              child.setToolTipText(toolTip);
            }
          }
          catch (Exception ex) {}
        }
        
        addChildrenToTree(child, hpp);
        hpp.goToParent();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  protected void classSelected(String className)
  {
    try
    {
      if ((m_Object != null) && (m_Object.getClass().getName().equals(className))) {
        return;
      }
      
      setValue(Class.forName(className).newInstance());
      
      if (m_EditorComponent != null) {
        m_EditorComponent.updateChildPropertySheet();
      }
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("GenericObjectEditor_ClassSelected_Exception_JOptionPaneShowMessageDialog_Text_First") + className + Messages.getString("GenericObjectEditor_ClassSelected_Exception_JOptionPaneShowMessageDialog_Text_Second"), Messages.getString("GenericObjectEditor_ClassSelected_Exception_JOptionPaneShowMessageDialog_Text_Third"), 0);
      















      ex.printStackTrace();
      try {
        if (m_Backup != null) {
          setValue(m_Backup);
        } else {
          setDefaultValue();
        }
      } catch (Exception e) {
        System.err.println(ex.getMessage());
        ex.printStackTrace();
      }
    }
  }
  




  public void setCapabilitiesFilter(Capabilities value)
  {
    m_CapabilitiesFilter = new Capabilities(null);
    m_CapabilitiesFilter.assign(value);
  }
  




  public Capabilities getCapabilitiesFilter()
  {
    return m_CapabilitiesFilter;
  }
  


  public void removeCapabilitiesFilter()
  {
    m_CapabilitiesFilter = null;
  }
  





  public static Object makeCopy(Object source)
    throws Exception
  {
    SerializedObject so = new SerializedObject(source);
    Object result = so.getObject();
    return result;
  }
  










  public static Vector<String> getClassnames(String property)
  {
    Vector<String> result = new Vector();
    
    String value = EDITOR_PROPERTIES.getProperty(property, "").replaceAll(" ", "").trim();
    
    if (value.length() > 0) {
      String[] items = value.split(",");
      for (int i = 0; i < items.length; i++) {
        result.add(items[i]);
      }
    }
    
    return result;
  }
  




  public static void main(String[] args)
  {
    try
    {
      registerEditors();
      GenericObjectEditor ce = new GenericObjectEditor(true);
      ce.setClassType(Classifier.class);
      Object initial = new ZeroR();
      if (args.length > 0) {
        ce.setClassType(Class.forName(args[0]));
        if (args.length > 1) {
          initial = Class.forName(args[1]).newInstance();
          ce.setValue(initial);
        } else {
          ce.setDefaultValue();
        }
      } else {
        ce.setValue(initial);
      }
      
      PropertyDialog pd = new PropertyDialog((Frame)null, ce, 100, 100);
      pd.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          PropertyEditor pe = ((PropertyDialog)e.getSource()).getEditor();
          Object c = pe.getValue();
          String options = "";
          if ((c instanceof OptionHandler)) {
            options = Utils.joinOptions(((OptionHandler)c).getOptions());
          }
          System.out.println(c.getClass().getName() + " " + options);
          System.exit(0);
        }
      });
      pd.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

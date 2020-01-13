package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.Customizer;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.filechooser.FileFilter;
import weka.core.ClassloaderUtil;
import weka.core.Copyright;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Memory;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.core.xml.KOML;
import weka.core.xml.XStream;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.GenericPropertiesCreator;
import weka.gui.HierarchyPropertyParser;
import weka.gui.LookAndFeel;
import weka.gui.beans.xml.XMLBeans;
import weka.gui.visualize.PrintablePanel;



























public class KnowledgeFlowApp
  extends JPanel
  implements PropertyChangeListener
{
  private static final long serialVersionUID = -7064906770289728431L;
  protected static String PROPERTY_FILE = "weka/gui/beans/Beans.props";
  


  protected static Properties BEAN_PROPERTIES;
  


  private static ArrayList<Properties> BEAN_PLUGINS_PROPERTIES;
  

  private static Vector TOOLBARS = new Vector();
  FontMetrics m_fontM;
  protected static final int NONE = 0;
  protected static final int MOVING = 1;
  
  public static void loadProperties() {
    if (BEAN_PROPERTIES == null) {
      Messages.getInstance();System.out.println(Messages.getString("KnowledgeFlowApp_LoadProperties_Text_First"));
      


      try
      {
        BEAN_PROPERTIES = Utils.readProperties(PROPERTY_FILE);
        Enumeration keys = BEAN_PROPERTIES.propertyNames();
        if (!keys.hasMoreElements()) {
          Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();throw new Exception(Messages.getString("KnowledgeFlowApp_LoadProperties_Exception_Text_First") + Messages.getString("KnowledgeFlowApp_LoadProperties_Exception_Text_Second") + PROPERTY_FILE + Messages.getString("KnowledgeFlowApp_LoadProperties_Exception_Text_Third") + System.getProperties().getProperty("user.home") + Messages.getString("KnowledgeFlowApp_LoadProperties_Exception_Text_Fourth"));


        }
        


      }
      catch (Exception ex)
      {


        Messages.getInstance();JOptionPane.showMessageDialog(null, ex.getMessage(), Messages.getString("KnowledgeFlowApp_LoadProperties_Exception_JOptionPaneShowMessageDialog_Text"), 0);
      }
      









      File pluginDir = new File(System.getProperty("user.home") + File.separator + ".knowledgeFlow" + File.separator + "plugins");
      
      if ((pluginDir.exists()) && (pluginDir.isDirectory())) {
        BEAN_PLUGINS_PROPERTIES = new ArrayList();
        
        File[] contents = pluginDir.listFiles();
        for (int i = 0; i < contents.length; i++) {
          if ((contents[i].isDirectory()) && (contents[i].listFiles().length > 0)) {
            try {
              Properties tempP = new Properties();
              File propFile = new File(contents[i].getPath() + File.separator + "Beans.props");
              
              tempP.load(new FileInputStream(propFile));
              BEAN_PLUGINS_PROPERTIES.add(tempP);
              


              File[] anyJars = contents[i].listFiles();
              for (int j = 0; j < anyJars.length; j++) {
                if (anyJars[j].getPath().endsWith(".jar")) {
                  Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("KnowledgeFlowApp_LoadProperties_Text_Second") + anyJars[j].getPath() + Messages.getString("KnowledgeFlowApp_LoadProperties_Text_Third"));
                  



                  ClassloaderUtil.addFile(anyJars[j].getPath());
                }
              }
            }
            catch (Exception ex) {
              Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_LoadProperties_Error_Text_First") + contents[i].getPath());
            }
            
          }
          
        }
        
      }
      else
      {
        pluginDir.mkdir();
      }
    }
  }
  


  private static void init()
  {
    Messages.getInstance();System.out.println(Messages.getString("KnowledgeFlowApp_Init_Text_First"));
    
    try
    {
      TreeMap wrapList = new TreeMap();
      GenericPropertiesCreator creator = new GenericPropertiesCreator();
      Properties GEOProps = null;
      
      if (creator.useDynamic()) {
        creator.execute(false);
        




        GEOProps = creator.getOutputProperties();
      }
      else {
        GEOProps = Utils.readProperties("weka/gui/GenericObjectEditor.props");
      }
      Enumeration en = GEOProps.propertyNames();
      while (en.hasMoreElements()) {
        String geoKey = (String)en.nextElement();
        

        String beanCompName = BEAN_PROPERTIES.getProperty(geoKey);
        if (beanCompName != null)
        {

          Vector newV = new Vector();
          
          String toolBarNameAlias = BEAN_PROPERTIES.getProperty(geoKey + ".alias");
          
          String toolBarName = toolBarNameAlias != null ? toolBarNameAlias : geoKey.substring(geoKey.lastIndexOf('.') + 1, geoKey.length());
          


          String order = BEAN_PROPERTIES.getProperty(geoKey + ".order");
          Integer intOrder = order != null ? new Integer(order) : new Integer(0);
          


          newV.addElement(toolBarName);
          
          newV.addElement(beanCompName);
          

          String rootPackage = geoKey.substring(0, geoKey.lastIndexOf('.'));
          
          newV.addElement(rootPackage);
          

          String wekaAlgs = GEOProps.getProperty(geoKey);
          
          Hashtable roots = GenericObjectEditor.sortClassesByRoot(wekaAlgs);
          Hashtable hpps = new Hashtable();
          Enumeration enm = roots.keys();
          while (enm.hasMoreElements()) {
            String root = (String)enm.nextElement();
            String classes = (String)roots.get(root);
            HierarchyPropertyParser hpp = new HierarchyPropertyParser();
            hpp.build(classes, ", ");
            
            hpps.put(root, hpp);
          }
          









          newV.addElement(hpps);
          
          StringTokenizer st = new StringTokenizer(wekaAlgs, ", ");
          while (st.hasMoreTokens()) {
            String current = st.nextToken().trim();
            newV.addElement(current);
          }
          wrapList.put(intOrder, newV);
        }
      }
      
      Iterator keysetIt = wrapList.keySet().iterator();
      while (keysetIt.hasNext()) {
        Integer key = (Integer)keysetIt.next();
        Vector newV = (Vector)wrapList.get(key);
        if (newV != null) {
          TOOLBARS.addElement(newV);
        }
      }
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("KnowledgeFlowApp_Init_Exception_JOptionPaneShowMessageDialog_Text_First") + System.getProperties().getProperty("user.home") + Messages.getString("KnowledgeFlowApp_Init_Exception_JOptionPaneShowMessageDialog_Text_Second"), Messages.getString("KnowledgeFlowApp_Init_Exception_JOptionPaneShowMessageDialog_Text_Third"), 0);
    }
    















    try
    {
      String standardToolBarNames = BEAN_PROPERTIES.getProperty("weka.gui.beans.KnowledgeFlow.standardToolBars");
      
      StringTokenizer st = new StringTokenizer(standardToolBarNames, ", ");
      while (st.hasMoreTokens()) {
        String tempBarName = st.nextToken().trim();
        
        Vector newV = new Vector();
        
        newV.addElement(tempBarName);
        

        newV.addElement("null");
        String toolBarContents = BEAN_PROPERTIES.getProperty("weka.gui.beans.KnowledgeFlow." + tempBarName);
        
        StringTokenizer st2 = new StringTokenizer(toolBarContents, ", ");
        while (st2.hasMoreTokens()) {
          String tempBeanName = st2.nextToken().trim();
          newV.addElement(tempBeanName);
        }
        TOOLBARS.addElement(newV);
      }
    } catch (Exception ex) {
      Messages.getInstance();JOptionPane.showMessageDialog(null, ex.getMessage(), Messages.getString("KnowledgeFlowApp_Init_Exception_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
    }
  }
  





  protected class BeanLayout
    extends PrintablePanel
  {
    private static final long serialVersionUID = -146377012429662757L;
    





    protected BeanLayout() {}
    




    public void paintComponent(Graphics gx)
    {
      super.paintComponent(gx);
      BeanInstance.paintLabels(gx);
      BeanConnection.paintConnections(gx);
      
      if (m_mode == 2) {
        gx.drawLine(m_startX, m_startY, m_oldX, m_oldY);
      } else if (m_mode == 4) {
        gx.drawRect(m_startX < m_oldX ? m_startX : m_oldX, m_startY < m_oldY ? m_startY : m_oldY, Math.abs(m_oldX - m_startX), Math.abs(m_oldY - m_startY));
      }
    }
    


    public void doLayout()
    {
      super.doLayout();
      Vector comps = BeanInstance.getBeanInstances();
      for (int i = 0; i < comps.size(); i++) {
        BeanInstance bi = (BeanInstance)comps.elementAt(i);
        JComponent c = (JComponent)bi.getBean();
        Dimension d = c.getPreferredSize();
        c.setBounds(bi.getX(), bi.getY(), width, height);
        c.revalidate();
      }
    }
  }
  



  protected static final int CONNECTING = 2;
  

  protected static final int ADDING = 3;
  

  protected static final int SELECTING = 4;
  

  private int m_mode = 0;
  


  protected static final String USERCOMPONENTS_XML_EXTENSION = ".xml";
  


  private final ButtonGroup m_toolBarGroup = new ButtonGroup();
  



  private Object m_toolBarBean;
  



  private final BeanLayout m_beanLayout = new BeanLayout();
  



  private final JTabbedPane m_toolBars = new JTabbedPane();
  



  private JToolBar m_pluginsToolBar = null;
  private Box m_pluginsBoxPanel = null;
  



  private JToolBar m_userToolBar = null;
  private Box m_userBoxPanel = null;
  private final Vector m_userComponents = new Vector();
  private boolean m_firstUserComponentOpp = true;
  

  private JToggleButton m_pointerB;
  
  private JButton m_saveB;
  
  private JButton m_loadB;
  
  private JButton m_stopB;
  
  private JButton m_helpB;
  
  private JButton m_newB;
  
  private BeanInstance m_editElement;
  
  private EventSetDescriptor m_sourceEventSetDescriptor;
  
  private int m_oldX;
  
  private int m_oldY;
  
  private int m_startX;
  
  private int m_startY;
  
  protected JFileChooser m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  

  protected LogPanel m_logPanel = new LogPanel();
  
  protected BeanContextSupport m_bcSupport = new BeanContextSupport();
  




  public static final String FILE_EXTENSION = ".kf";
  



  public static final String FILE_EXTENSION_XML = ".kfml";
  



  protected FileFilter m_KfFilter;
  



  protected FileFilter m_KOMLFilter;
  



  protected FileFilter m_XStreamFilter;
  



  protected FileFilter m_XMLFilter;
  



  protected int m_ScrollBarIncrementLayout;
  



  protected int m_ScrollBarIncrementComponents;
  



  protected int m_FlowWidth;
  



  protected int m_FlowHeight;
  



  protected String m_PreferredExtension;
  



  protected boolean m_UserComponentsInXML;
  



  protected Environment m_flowEnvironment;
  



  private static KnowledgeFlowApp m_knowledgeFlow;
  




  public void setEnvironment(Environment env)
  {
    m_flowEnvironment = env;
    setEnvironment();
  }
  

  private void setEnvironment()
  {
    Vector beans = BeanInstance.getBeanInstances();
    for (int i = 0; i < beans.size(); i++) {
      Object temp = ((BeanInstance)beans.elementAt(i)).getBean();
      
      if ((temp instanceof EnvironmentHandler)) {
        ((EnvironmentHandler)temp).setEnvironment(m_flowEnvironment);
      }
    }
  }
  
  public KnowledgeFlowApp(boolean showFileMenu)
  {
    Messages.getInstance();Messages.getInstance();m_KfFilter = new ExtensionFileFilter(".kf", Messages.getString("KnowledgeFlowApp_KfFilter_Text_First") + ".kf" + Messages.getString("KnowledgeFlowApp_KfFilter_Text_Second"));
    








    Messages.getInstance();Messages.getInstance();m_KOMLFilter = new ExtensionFileFilter(".komlkf", Messages.getString("KnowledgeFlowApp_KOMLFilter_Text_Second") + ".koml" + Messages.getString("KnowledgeFlowApp_KOMLFilter_Text_Third"));
    









    Messages.getInstance();Messages.getInstance();m_XStreamFilter = new ExtensionFileFilter(".xstreamkf", Messages.getString("KnowledgeFlowApp_XStreamFilter_Text_Second") + ".xstream" + Messages.getString("KnowledgeFlowApp_XStreamFilter_Text_Third"));
    









    Messages.getInstance();Messages.getInstance();m_XMLFilter = new ExtensionFileFilter(".kfml", Messages.getString("KnowledgeFlowApp_XMLFilter_Text_First") + ".kfml" + Messages.getString("KnowledgeFlowApp_XMLFilter_Text_Second"));
    






    m_ScrollBarIncrementLayout = 20;
    

    m_ScrollBarIncrementComponents = 50;
    

    m_FlowWidth = 1024;
    

    m_FlowHeight = 768;
    

    m_PreferredExtension = ".kf";
    

    m_UserComponentsInXML = false;
    

    m_flowEnvironment = new Environment();
    




























































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































    m_showFileMenu = true;
    if (BEAN_PROPERTIES == null) {
      loadProperties();
      init();
    }
    
    m_showFileMenu = showFileMenu;
    


    JWindow temp = new JWindow();
    temp.setVisible(true);
    temp.getGraphics().setFont(new Font(null, 0, 9));
    m_fontM = temp.getGraphics().getFontMetrics();
    temp.setVisible(false);
    
    try
    {
      m_ScrollBarIncrementLayout = Integer.parseInt(BEAN_PROPERTIES.getProperty("ScrollBarIncrementLayout", "" + m_ScrollBarIncrementLayout));
      

      m_ScrollBarIncrementComponents = Integer.parseInt(BEAN_PROPERTIES.getProperty("ScrollBarIncrementComponents", "" + m_ScrollBarIncrementComponents));
      

      m_FlowWidth = Integer.parseInt(BEAN_PROPERTIES.getProperty("FlowWidth", "" + m_FlowWidth));
      
      m_FlowHeight = Integer.parseInt(BEAN_PROPERTIES.getProperty("FlowHeight", "" + m_FlowHeight));
      
      m_PreferredExtension = BEAN_PROPERTIES.getProperty("PreferredExtension", m_PreferredExtension);
      
      m_UserComponentsInXML = Boolean.valueOf(BEAN_PROPERTIES.getProperty("UserComponentsInXML", "" + m_UserComponentsInXML)).booleanValue();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    

    m_FileChooser.addChoosableFileFilter(m_KfFilter);
    if (KOML.isPresent()) {
      m_FileChooser.addChoosableFileFilter(m_KOMLFilter);
    }
    if (XStream.isPresent()) {
      m_FileChooser.addChoosableFileFilter(m_XStreamFilter);
    }
    
    m_FileChooser.addChoosableFileFilter(m_XMLFilter);
    
    if (m_PreferredExtension.equals(".kfml")) {
      m_FileChooser.setFileFilter(m_XMLFilter);
    } else if ((KOML.isPresent()) && (m_PreferredExtension.equals(".komlkf")))
    {
      m_FileChooser.setFileFilter(m_KOMLFilter);
    } else if ((XStream.isPresent()) && (m_PreferredExtension.equals(".xstreamkf")))
    {
      m_FileChooser.setFileFilter(m_XStreamFilter);
    } else {
      m_FileChooser.setFileFilter(m_KfFilter);
    }
    m_FileChooser.setFileSelectionMode(0);
    
    m_bcSupport.setDesignTime(true);
    m_beanLayout.setLayout(null);
    

    m_beanLayout.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent me)
      {
        if ((m_toolBarBean == null) && 
          ((me.getModifiers() & 0x10) == 16) && (m_mode == 0))
        {
          BeanInstance bi = BeanInstance.findInstance(me.getPoint());
          JComponent bc = null;
          if (bi != null) {
            bc = (JComponent)bi.getBean();
          }
          if ((bc != null) && ((bc instanceof Visible))) {
            m_editElement = bi;
            m_oldX = me.getX();
            m_oldY = me.getY();
            m_mode = 1;
          }
          if (m_mode != 1) {
            m_mode = 4;
            m_oldX = me.getX();
            m_oldY = me.getY();
            m_startX = m_oldX;
            m_startY = m_oldY;
            Graphics2D gx = (Graphics2D)m_beanLayout.getGraphics();
            gx.setXORMode(Color.white);
            

            gx.dispose();
            m_mode = 4;
          }
        }
      }
      

      public void mouseReleased(MouseEvent me)
      {
        if ((m_editElement != null) && (m_mode == 1)) {
          m_editElement = null;
          revalidate();
          m_beanLayout.repaint();
          m_mode = 0;
        }
        if (m_mode == 4) {
          revalidate();
          m_beanLayout.repaint();
          m_mode = 0;
          
          KnowledgeFlowApp.this.checkSubFlow(m_startX, m_startY, me.getX(), me.getY());
        }
      }
      
      public void mouseClicked(MouseEvent me)
      {
        BeanInstance bi = BeanInstance.findInstance(me.getPoint());
        if ((m_mode == 3) || (m_mode == 0))
        {

          if (bi != null) {
            JComponent bc = (JComponent)bi.getBean();
            

            if ((me.getClickCount() == 2) && (!(bc instanceof MetaBean))) {
              try {
                Class custClass = Introspector.getBeanInfo(bc.getClass()).getBeanDescriptor().getCustomizerClass();
                
                if (custClass != null) {
                  if ((bc instanceof BeanCommon)) {
                    if (!((BeanCommon)bc).isBusy()) {
                      KnowledgeFlowApp.this.popupCustomizer(custClass, bc);
                    }
                  } else {
                    KnowledgeFlowApp.this.popupCustomizer(custClass, bc);
                  }
                }
              } catch (IntrospectionException ex) {
                ex.printStackTrace();
              }
            } else if (((me.getModifiers() & 0x10) != 16) || (me.isAltDown()))
            {
              KnowledgeFlowApp.this.doPopup(me.getPoint(), bi, me.getX(), me.getY());
            }
          }
          else if (((me.getModifiers() & 0x10) != 16) || (me.isAltDown()))
          {

            int delta = 10;
            KnowledgeFlowApp.this.deleteConnectionPopup(BeanConnection.getClosestConnections(new Point(me.getX(), me.getY()), delta), me.getX(), me.getY());


          }
          else if (m_toolBarBean != null)
          {

            KnowledgeFlowApp.this.addComponent(me.getX(), me.getY());
          }
        }
        

        if (m_mode == 2)
        {
          m_beanLayout.repaint();
          Vector beanInstances = BeanInstance.getBeanInstances();
          for (int i = 0; i < beanInstances.size(); i++) {
            JComponent bean = (JComponent)((BeanInstance)beanInstances.elementAt(i)).getBean();
            
            if ((bean instanceof Visible)) {
              ((Visible)bean).getVisual().setDisplayConnectors(false);
            }
          }
          
          if (bi != null) {
            boolean doConnection = false;
            if (!(bi.getBean() instanceof BeanCommon)) {
              doConnection = true;


            }
            else if (((BeanCommon)bi.getBean()).connectionAllowed(m_sourceEventSetDescriptor))
            {

              doConnection = true;
            }
            BeanConnection bc;
            if (doConnection)
            {

              if ((bi.getBean() instanceof MetaBean)) {
                BeanConnection.doMetaConnection(m_editElement, bi, m_sourceEventSetDescriptor, m_beanLayout);
              }
              else {
                bc = new BeanConnection(m_editElement, bi, m_sourceEventSetDescriptor);
              }
            }
            
            m_beanLayout.repaint();
          }
          m_mode = 0;
          m_editElement = null;
          m_sourceEventSetDescriptor = null;
        }
        
      }
    });
    m_beanLayout.addMouseMotionListener(new MouseMotionAdapter()
    {
      public void mouseDragged(MouseEvent me)
      {
        if ((m_editElement != null) && (m_mode == 1)) {
          ImageIcon ic = ((Visible)m_editElement.getBean()).getVisual().getStaticIcon();
          
          int width = ic.getIconWidth() / 2;
          int height = ic.getIconHeight() / 2;
          





          m_editElement.setXY(m_oldX - width, m_oldY - height);
          m_beanLayout.repaint();
          

          m_oldX = me.getX();
          m_oldY = me.getY();
        }
        if (m_mode == 4) {
          m_beanLayout.repaint();
          m_oldX = me.getX();
          m_oldY = me.getY();
        }
      }
      
      public void mouseMoved(MouseEvent e)
      {
        if (m_mode == 2) {
          m_beanLayout.repaint();
          
          m_oldX = e.getX();
          m_oldY = e.getY();
        }
        
      }
    });
    String date = new SimpleDateFormat("EEEE, d MMMM yyyy").format(new Date());
    
    Messages.getInstance();m_logPanel.logMessage(Messages.getString("KnowledgeFlowApp_MouseClicked_LogPanel_LogMessage_Text_First"));
    
    Messages.getInstance();m_logPanel.logMessage(Messages.getString("KnowledgeFlowApp_MouseClicked_LogPanel_LogMessage_Text_Second"));
    
    Messages.getInstance();m_logPanel.logMessage(Messages.getString("KnowledgeFlowApp_MouseClicked_LogPanel_LogMessage_Text_Third") + Copyright.getToYear() + " " + Copyright.getOwner() + ", " + Copyright.getAddress());
    





    Messages.getInstance();m_logPanel.logMessage(Messages.getString("KnowledgeFlowApp_MouseClicked_LogPanel_LogMessage_Text_Fourth") + Copyright.getURL());
    

    m_logPanel.logMessage(date);
    Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_MouseClicked_LogPanel_StatusMessage_Text_First"));
    
    m_logPanel.getStatusTable().addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e) {
        if ((m_logPanel.getStatusTable().rowAtPoint(e.getPoint()) == 0) && (
          ((e.getModifiers() & 0x10) != 16) || (e.isAltDown())))
        {
          System.gc();
          Runtime currR = Runtime.getRuntime();
          long freeM = currR.freeMemory();
          long totalM = currR.totalMemory();
          long maxM = currR.maxMemory();
          Messages.getInstance();m_logPanel.logMessage(Messages.getString("KnowledgeFlowApp_MouseClicked_LogPanel_LogMessage_Text_Fifth") + String.format("%,d", new Object[] { Long.valueOf(freeM) }) + " / " + String.format("%,d", new Object[] { Long.valueOf(totalM) }) + " / " + String.format("%,d", new Object[] { Long.valueOf(maxM) }));
          





          Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_MouseClicked_LogPanel_StatusMessage_Text_Second") + String.format("%,d", new Object[] { Long.valueOf(freeM) }) + " / " + String.format("%,d", new Object[] { Long.valueOf(totalM) }) + " / " + String.format("%,d", new Object[] { Long.valueOf(maxM) }));



        }
        



      }
      



    });
    JPanel p1 = new JPanel();
    p1.setLayout(new BorderLayout());
    Messages.getInstance();p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("KnowledgeFlowApp_P1_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    





    JScrollPane js = new JScrollPane(m_beanLayout);
    p1.add(js, "Center");
    js.getVerticalScrollBar().setUnitIncrement(m_ScrollBarIncrementLayout);
    js.getHorizontalScrollBar().setUnitIncrement(m_ScrollBarIncrementLayout);
    
    setLayout(new BorderLayout());
    
    add(p1, "Center");
    m_beanLayout.setSize(m_FlowWidth, m_FlowHeight);
    Dimension d = m_beanLayout.getPreferredSize();
    m_beanLayout.setMinimumSize(d);
    m_beanLayout.setMaximumSize(d);
    m_beanLayout.setPreferredSize(d);
    
    Dimension d2 = new Dimension(100, 170);
    m_logPanel.setPreferredSize(d2);
    m_logPanel.setMinimumSize(d2);
    add(m_logPanel, "South");
    
    setUpToolBars();
    loadUserComponents();
  }
  
  private Image loadImage(String path) {
    Image pic = null;
    

    URL imageURL = getClass().getClassLoader().getResource(path);
    

    if (imageURL != null)
    {

      pic = Toolkit.getDefaultToolkit().getImage(imageURL);
    }
    return pic;
  }
  


  private void setUpToolBars()
  {
    JPanel toolBarPanel = new JPanel();
    toolBarPanel.setLayout(new BorderLayout());
    


    if (m_showFileMenu) {
      JToolBar fixedTools = new JToolBar();
      fixedTools.setOrientation(1);
      m_saveB = new JButton(new ImageIcon(loadImage("weka/gui/beans/icons/Save24.gif")));
      
      Messages.getInstance();m_saveB.setToolTipText(Messages.getString("KnowledgeFlowApp_SaveB_SetToolTipText_Text"));
      
      m_loadB = new JButton(new ImageIcon(loadImage("weka/gui/beans/icons/Open24.gif")));
      
      Messages.getInstance();m_loadB.setToolTipText(Messages.getString("KnowledgeFlowApp_LoadB_SetToolTipText_Text"));
      
      m_newB = new JButton(new ImageIcon(loadImage("weka/gui/beans/icons/New24.gif")));
      
      Messages.getInstance();m_newB.setToolTipText(Messages.getString("KnowledgeFlowApp_NewB_SetToolTipText_Text"));
      
      fixedTools.add(m_newB);
      fixedTools.add(m_saveB);
      fixedTools.add(m_loadB);
      
      m_saveB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          KnowledgeFlowApp.this.saveLayout();
        }
        
      });
      m_loadB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_flowEnvironment = new Environment();
          KnowledgeFlowApp.this.loadLayout();
        }
        
      });
      m_newB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          clearLayout();
        }
        
      });
      fixedTools.setFloatable(false);
      toolBarPanel.add(fixedTools, "West");
    }
    
    m_stopB = new JButton(new ImageIcon(loadImage("weka/gui/beans/icons/Stop24.gif")));
    
    m_helpB = new JButton(new ImageIcon(loadImage("weka/gui/beans/icons/Help24.gif")));
    
    Messages.getInstance();m_stopB.setToolTipText(Messages.getString("KnowledgeFlowApp_StopB_SetToolTipText_Text"));
    
    Messages.getInstance();m_helpB.setToolTipText(Messages.getString("KnowledgeFlowApp_HelpB_SetToolTipText_Text"));
    

    Image tempI = loadImage("weka/gui/beans/icons/Pointer.gif");
    m_pointerB = new JToggleButton(new ImageIcon(tempI));
    m_pointerB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_toolBarBean = null;
        m_mode = 0;
        setCursor(Cursor.getPredefinedCursor(0));



      }
      



    });
    m_toolBarGroup.add(m_pointerB);
    
    JToolBar fixedTools2 = new JToolBar();
    fixedTools2.setOrientation(1);
    fixedTools2.setFloatable(false);
    fixedTools2.add(m_pointerB);
    fixedTools2.add(m_helpB);
    fixedTools2.add(m_stopB);
    

    m_helpB.setSize(m_pointerB.getSize().width, m_pointerB.getSize().height);
    toolBarPanel.add(fixedTools2, "East");
    
    m_stopB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_StopB_LogPanel_StatusMessage_Text_First"));
        
        KnowledgeFlowApp.this.stopFlow();
        Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_StopB_LogPanel_StatusMessage_Text_Second"));
      }
      

    });
    m_helpB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        KnowledgeFlowApp.this.popupHelp();
      }
      
    });
    int STANDARD_TOOLBAR = 0;
    int WEKAWRAPPER_TOOLBAR = 1;
    
    int toolBarType = 0;
    

    for (int i = 0; i < TOOLBARS.size(); i++) {
      Vector tempBarSpecs = (Vector)TOOLBARS.elementAt(i);
      

      String tempBarName = (String)tempBarSpecs.elementAt(0);
      

      Box singletonHolderPanel = null;
      

      String tempBeanCompName = (String)tempBarSpecs.elementAt(1);
      





      String rootPackage = "";
      HierarchyPropertyParser hpp = null;
      Hashtable hpps = null;
      

      if (tempBeanCompName.compareTo("null") != 0) {
        JPanel tempBean = null;
        toolBarType = 1;
        rootPackage = (String)tempBarSpecs.elementAt(2);
        
        hpps = (Hashtable)tempBarSpecs.elementAt(3);
        

        try
        {
          Beans.instantiate(getClass().getClassLoader(), tempBeanCompName);

        }
        catch (Exception ex)
        {
          Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_Error_Text") + tempBeanCompName);
          


          break;
        }
      } else {
        toolBarType = 0;
      }
      

      JToolBar tempToolBar = new JToolBar();
      


      int z = 2;
      
      if (toolBarType == 1) {
        Enumeration enm = hpps.keys();
        
        while (enm.hasMoreElements()) {
          String root = (String)enm.nextElement();
          String userPrefix = "";
          hpp = (HierarchyPropertyParser)hpps.get(root);
          
          if (!hpp.goTo(rootPackage)) {
            Messages.getInstance();System.out.println(Messages.getString("KnowledgeFlowApp_Text_First"));
            

            userPrefix = root + ".";
          }
          
          String[] primaryPackages = hpp.childrenValues();
          
          for (int kk = 0; kk < primaryPackages.length; kk++) {
            hpp.goToChild(primaryPackages[kk]);
            


            if (hpp.isLeafReached()) {
              if (singletonHolderPanel == null) {
                singletonHolderPanel = Box.createHorizontalBox();
                singletonHolderPanel.setBorder(BorderFactory.createTitledBorder(tempBarName));
              }
              

              String algName = hpp.fullValue();
              JPanel tempBean = instantiateToolBarBean(true, tempBeanCompName, algName);
              
              if (tempBean != null)
              {
                singletonHolderPanel.add(tempBean);
              }
              
              hpp.goToParent();

            }
            else
            {
              Box holderPanel = Box.createHorizontalBox();
              holderPanel.setBorder(BorderFactory.createTitledBorder(userPrefix + primaryPackages[kk]));
              
              processPackage(holderPanel, tempBeanCompName, hpp);
              tempToolBar.add(holderPanel);
            }
          }
          
          if (singletonHolderPanel != null) {
            tempToolBar.add(singletonHolderPanel);
            singletonHolderPanel = null;
          }
        }
      } else {
        Box holderPanel = Box.createHorizontalBox();
        holderPanel.setBorder(BorderFactory.createTitledBorder(tempBarName));
        

        for (int j = z; j < tempBarSpecs.size(); j++) {
          JPanel tempBean = null;
          tempBeanCompName = (String)tempBarSpecs.elementAt(j);
          tempBean = instantiateToolBarBean(toolBarType == 1, tempBeanCompName, "");
          

          if (tempBean != null)
          {

            holderPanel.add(tempBean);
          }
        }
        
        tempToolBar.add(holderPanel);
      }
      
      JScrollPane tempJScrollPane = createScrollPaneForToolBar(tempToolBar);
      
      m_toolBars.addTab(tempBarName, null, tempJScrollPane, tempBarName);
    }
    

    if ((BEAN_PLUGINS_PROPERTIES != null) && (BEAN_PLUGINS_PROPERTIES.size() > 0)) {
      for (int i = 0; i < BEAN_PLUGINS_PROPERTIES.size(); i++) {
        Properties tempP = (Properties)BEAN_PLUGINS_PROPERTIES.get(i);
        JPanel tempBean = null;
        String components = tempP.getProperty("weka.gui.beans.KnowledgeFlow.Plugins");
        
        StringTokenizer st2 = new StringTokenizer(components, ", ");
        
        while (st2.hasMoreTokens()) {
          String tempBeanCompName = st2.nextToken().trim();
          tempBean = instantiateToolBarBean(false, tempBeanCompName, "");
          if (m_pluginsToolBar == null)
          {
            setUpPluginsToolBar();
          }
          m_pluginsBoxPanel.add(tempBean);
        }
      }
    }
    
    toolBarPanel.add(m_toolBars, "Center");
    

    add(toolBarPanel, "North");
  }
  
  private void stopFlow() {
    Vector components = BeanInstance.getBeanInstances();
    
    for (int i = 0; i < components.size(); i++) {
      Object temp = ((BeanInstance)components.elementAt(i)).getBean();
      
      if ((temp instanceof BeanCommon)) {
        ((BeanCommon)temp).stop();
      }
    }
  }
  
  private JScrollPane createScrollPaneForToolBar(JToolBar tb) {
    JScrollPane tempJScrollPane = new JScrollPane(tb, 21, 32);
    


    Dimension d = tb.getPreferredSize();
    tempJScrollPane.setMinimumSize(new Dimension((int)d.getWidth(), (int)(d.getHeight() + 15.0D)));
    
    tempJScrollPane.setPreferredSize(new Dimension((int)d.getWidth(), (int)(d.getHeight() + 15.0D)));
    
    tempJScrollPane.getHorizontalScrollBar().setUnitIncrement(m_ScrollBarIncrementComponents);
    

    return tempJScrollPane;
  }
  
  private void processPackage(JComponent holderPanel, String tempBeanCompName, HierarchyPropertyParser hpp)
  {
    if (hpp.isLeafReached())
    {

      String algName = hpp.fullValue();
      JPanel tempBean = instantiateToolBarBean(true, tempBeanCompName, algName);
      if (tempBean != null) {
        holderPanel.add(tempBean);
      }
      hpp.goToParent();
      return;
    }
    String[] children = hpp.childrenValues();
    for (int i = 0; i < children.length; i++) {
      hpp.goToChild(children[i]);
      processPackage(holderPanel, tempBeanCompName, hpp);
    }
    hpp.goToParent();
  }
  





  private JPanel instantiateToolBarBean(boolean wekawrapper, String tempBeanCompName, String algName)
  {
    Object tempBean;
    



    if (wekawrapper)
    {
      Object tempBean;
      try {
        tempBean = Beans.instantiate(getClass().getClassLoader(), tempBeanCompName);

      }
      catch (Exception ex)
      {
        Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_InstantiateToolBarBean_Error_Text_First") + tempBeanCompName + "KnowledgeFlowApp.instantiateToolBarBean()");
        

        return null;
      }
      if ((tempBean instanceof WekaWrapper))
      {
        Class c = null;
        try {
          c = Class.forName(algName);
        } catch (Exception ex) {
          Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_InstantiateToolBarBean_Error_Text_Third") + algName);
          

          return null;
        }
        try {
          Object o = c.newInstance();
          ((WekaWrapper)tempBean).setWrappedAlgorithm(o);
        } catch (Exception ex) {
          Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_InstantiateToolBarBean_Error_Text_Fourth") + tempBeanCompName + Messages.getString("KnowledgeFlowApp_InstantiateToolBarBean_Error_Text_Fifth") + algName);
          




          return null;
        }
      }
    }
    else
    {
      try {
        tempBean = Beans.instantiate(getClass().getClassLoader(), tempBeanCompName);

      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_InstantiateToolBarBean_Error_Text_Sixth") + tempBeanCompName + "KnowledgeFlowApp.setUpToolBars()");
        

        return null;
      }
    }
    
    if ((tempBean instanceof BeanContextChild)) {
      m_bcSupport.add(tempBean);
    }
    if ((tempBean instanceof Visible)) {
      ((Visible)tempBean).getVisual().scale(3);
    }
    
    return makeHolderPanelForToolBarBean(tempBeanCompName, tempBean, wekawrapper, algName, false);
  }
  







  private JPanel instantiateToolBarMetaBean(MetaBean bean)
  {
    bean.getVisual().removePropertyChangeListener(this);
    bean.removePropertyChangeListenersSubFlow(this);
    Object copy = null;
    try {
      SerializedObject so = new SerializedObject(bean);
      copy = so.getObject();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
    bean.getVisual().addPropertyChangeListener(this);
    bean.addPropertyChangeListenersSubFlow(this);
    
    String displayName = "";
    
    if ((copy instanceof Visible)) {
      ((Visible)copy).getVisual().scale(3);
      displayName = ((Visible)copy).getVisual().getText();
    }
    return makeHolderPanelForToolBarBean(displayName, copy, false, null, true);
  }
  



  private JPanel makeHolderPanelForToolBarBean(final String tempName, Object tempBean, boolean wekawrapper, String algName, final boolean metabean)
  {
    final JPanel tempP = new JPanel();
    JLabel tempL = new JLabel();
    tempL.setFont(new Font(null, 0, 9));
    
    String labelName = wekawrapper == true ? algName : tempName;
    labelName = labelName.substring(labelName.lastIndexOf('.') + 1, labelName.length());
    
    tempL.setText(" " + labelName + " ");
    tempL.setHorizontalAlignment(0);
    tempP.setLayout(new BorderLayout());
    JToggleButton tempButton;
    if ((tempBean instanceof Visible)) {
      BeanVisual bv = ((Visible)tempBean).getVisual();
      
      JToggleButton tempButton = new JToggleButton(bv.getStaticIcon());
      int width = bv.getStaticIcon().getIconWidth();
      int height = bv.getStaticIcon().getIconHeight();
      
      JPanel labelPanel = multiLineLabelPanel(labelName, width);
      tempP.add(labelPanel, "South");
    } else {
      tempButton = new JToggleButton();
      tempP.add(tempL, "South");
    }
    tempP.add(tempButton, "North");
    



    m_toolBarGroup.add(tempButton);
    

    final Object tempBN = tempBean;
    final JToggleButton fButton = tempButton;
    
    tempButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean changeCursor = true;
        try {
          m_toolBarBean = null;
          if (metabean) {
            if ((e.getModifiers() & 0x1) != 0) {
              changeCursor = false;
              m_toolBarGroup.remove(fButton);
              m_userBoxPanel.remove(tempP);
              m_userBoxPanel.revalidate();
              m_userComponents.remove(tempBN);
              if (m_firstUserComponentOpp) {
                KnowledgeFlowApp.this.installWindowListenerForSavingUserBeans();
                m_firstUserComponentOpp = false;
              }
              if (m_userComponents.size() == 0) {
                m_toolBars.removeTabAt(m_toolBars.getTabCount() - 1);
                m_userToolBar = null;
                KnowledgeFlowApp.this.notifyIsDirty();
              }
            } else {
              SerializedObject so = new SerializedObject(tempBN);
              MetaBean copy = (MetaBean)so.getObject();
              



              copy.addPropertyChangeListenersSubFlow(KnowledgeFlowApp.this);
              m_toolBarBean = copy;
            }
            
          }
          else {
            m_toolBarBean = Beans.instantiate(getClass().getClassLoader(), tempName);
          }
          


          if ((m_toolBarBean instanceof WekaWrapper)) {
            Object wrappedAlg = ((WekaWrapper)tempBN).getWrappedAlgorithm();
            
            ((WekaWrapper)m_toolBarBean).setWrappedAlgorithm(wrappedAlg.getClass().newInstance());
          }
          

          if (changeCursor) {
            setCursor(Cursor.getPredefinedCursor(1));
            m_mode = 3;
          }
        } catch (Exception ex) {
          Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_MakeHolderPanelForToolBarBean_Error_Text"));
          
          ex.printStackTrace();
        }
        KnowledgeFlowApp.this.notifyIsDirty();
      }
    });
    
    if ((tempBean instanceof MetaBean)) {
      Messages.getInstance();tempButton.setToolTipText(Messages.getString("KnowledgeFlowApp_MakeHolderPanelForToolBarBean_TempButton_SetToolTipText_Text"));
      



      m_userComponents.add(tempBean);
    }
    else {
      String summary = getGlobalInfo(tempBean);
      if (summary != null) {
        int ci = summary.indexOf('.');
        if (ci != -1) {
          summary = summary.substring(0, ci + 1);
        }
        tempButton.setToolTipText(summary);
      }
    }
    

    return tempP;
  }
  
  private JPanel multiLineLabelPanel(String sourceL, int splitWidth) {
    JPanel jp = new JPanel();
    Vector v = new Vector();
    
    int labelWidth = m_fontM.stringWidth(sourceL);
    
    if (labelWidth < splitWidth) {
      v.addElement(sourceL);
    }
    else {
      int mid = sourceL.length() / 2;
      

      int closest = sourceL.length();
      int closestI = -1;
      for (int i = 0; i < sourceL.length(); i++) {
        if ((sourceL.charAt(i) < 'a') && 
          (Math.abs(mid - i) < closest)) {
          closest = Math.abs(mid - i);
          closestI = i;
        }
      }
      
      if (closestI != -1) {
        String left = sourceL.substring(0, closestI);
        String right = sourceL.substring(closestI, sourceL.length());
        if ((left.length() > 1) && (right.length() > 1)) {
          v.addElement(left);
          v.addElement(right);
        } else {
          v.addElement(sourceL);
        }
      } else {
        v.addElement(sourceL);
      }
    }
    
    jp.setLayout(new GridLayout(v.size(), 1));
    for (int i = 0; i < v.size(); i++) {
      JLabel temp = new JLabel();
      temp.setFont(new Font(null, 0, 9));
      temp.setText(" " + (String)v.elementAt(i) + " ");
      temp.setHorizontalAlignment(0);
      jp.add(temp);
    }
    return jp;
  }
  
  private void setUpUserToolBar() {
    m_userBoxPanel = Box.createHorizontalBox();
    Messages.getInstance();m_userBoxPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("KnowledgeFlowApp_SetUpUserToolBar_SetBorder_BorderFactory_CreateTitledBorder_Text")));
    




    m_userToolBar = new JToolBar();
    m_userToolBar.add(m_userBoxPanel);
    JScrollPane tempJScrollPane = createScrollPaneForToolBar(m_userToolBar);
    

    Messages.getInstance();Messages.getInstance();m_toolBars.addTab(Messages.getString("KnowledgeFlowApp_SetUpUserToolBar_AddTab_Text_First"), null, tempJScrollPane, Messages.getString("KnowledgeFlowApp_SetUpUserToolBar_AddTab_Text_Second"));
  }
  





  private void setUpPluginsToolBar()
  {
    m_pluginsBoxPanel = Box.createHorizontalBox();
    Messages.getInstance();m_pluginsBoxPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("KnowledgeFlowApp_SetUpPluginsToolBar_PluginsBoxPanel_SetBorder_BorderFactory_CreateTitledBorder_Text")));
    




    m_pluginsToolBar = new JToolBar();
    m_pluginsToolBar.add(m_pluginsBoxPanel);
    JScrollPane tempJScrollPane = createScrollPaneForToolBar(m_pluginsToolBar);
    

    Messages.getInstance();Messages.getInstance();m_toolBars.addTab(Messages.getString("KnowledgeFlowApp_SetUpUserToolBar_AddTab_Text_Third"), null, tempJScrollPane, Messages.getString("KnowledgeFlowApp_SetUpUserToolBar_AddTab_Text_Fourth"));
  }
  








  private void popupHelp()
  {
    final JButton tempB = m_helpB;
    try {
      tempB.setEnabled(false);
      



      InputStream inR = getClass().getClassLoader().getResourceAsStream("weka/gui/beans/README_KnowledgeFlow");
      


      StringBuffer helpHolder = new StringBuffer();
      LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inR));
      
      String line;
      
      while ((line = lnr.readLine()) != null) {
        helpHolder.append(line + "\n");
      }
      
      lnr.close();
      final JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      JTextArea ta = new JTextArea(helpHolder.toString());
      ta.setFont(new Font("Monospaced", 0, 12));
      ta.setEditable(false);
      JScrollPane sp = new JScrollPane(ta);
      jf.getContentPane().add(sp, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          tempB.setEnabled(true);
          jf.dispose();
        }
      });
      jf.setSize(600, 600);
      jf.setVisible(true);
    }
    catch (Exception ex) {
      tempB.setEnabled(true);
    }
  }
  
  public void clearLayout() {
    stopFlow();
    BeanInstance.reset(m_beanLayout);
    BeanConnection.reset();
    m_beanLayout.revalidate();
    m_beanLayout.repaint();
    m_logPanel.clearStatus();
    Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_ClearLayout_StatusMessage_Text"));
  }
  












  private void doPopup(Point pt, final BeanInstance bi, int x, int y)
  {
    final JComponent bc = (JComponent)bi.getBean();
    final int xx = x;
    final int yy = y;
    int menuItemCount = 0;
    

    PopupMenu beanContextMenu = new PopupMenu();
    





    Messages.getInstance();MenuItem edit = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_Edit_MenuItem_Text"));
    
    edit.setEnabled(false);
    beanContextMenu.insert(edit, menuItemCount);
    menuItemCount++;
    
    if ((bc instanceof MetaBean))
    {
      Messages.getInstance();MenuItem ungroupItem = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_UngroupItem_MenuItem_Text"));
      
      ungroupItem.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          bi.removeBean(m_beanLayout);
          
          Vector group = ((MetaBean)bc).getBeansInSubFlow();
          Vector associatedConnections = ((MetaBean)bc).getAssociatedConnections();
          
          ((MetaBean)bc).restoreBeans();
          
          for (int i = 0; i < group.size(); i++) {
            BeanInstance tbi = (BeanInstance)group.elementAt(i);
            KnowledgeFlowApp.this.addComponent(tbi, false);
            tbi.addBean(m_beanLayout);
          }
          
          for (int i = 0; i < associatedConnections.size(); i++) {
            BeanConnection tbc = (BeanConnection)associatedConnections.elementAt(i);
            
            tbc.setHidden(false);
          }
          
          m_beanLayout.repaint();
          KnowledgeFlowApp.this.notifyIsDirty();
        }
      });
      beanContextMenu.add(ungroupItem);
      menuItemCount++;
      


      Messages.getInstance();MenuItem addToUserTabItem = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_AddToUserTabItem_MenuItem_Text"));
      
      addToUserTabItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          KnowledgeFlowApp.this.addToUserToolBar((MetaBean)bi.getBean(), true);
          KnowledgeFlowApp.this.notifyIsDirty();
        }
      });
      beanContextMenu.add(addToUserTabItem);
      menuItemCount++;
    }
    

    Messages.getInstance();MenuItem deleteItem = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_DeleteItem_MenuItem_Text"));
    
    deleteItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BeanConnection.removeConnections(bi);
        bi.removeBean(m_beanLayout);
        if ((bc instanceof BeanCommon)) {
          String key = ((BeanCommon)bc).getCustomName() + "$" + bc.hashCode();
          m_logPanel.statusMessage(key + "|remove");
        }
        revalidate();
        KnowledgeFlowApp.this.notifyIsDirty();
      }
    });
    if (((bc instanceof BeanCommon)) && 
      (((BeanCommon)bc).isBusy())) {
      deleteItem.setEnabled(false);
    }
    
    beanContextMenu.add(deleteItem);
    menuItemCount++;
    
    if ((bc instanceof BeanCommon)) {
      Messages.getInstance();MenuItem nameItem = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_NameItem_MenuItem_Text"));
      
      nameItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String oldName = ((BeanCommon)bc).getCustomName();
          Messages.getInstance();String name = JOptionPane.showInputDialog(KnowledgeFlowApp.this, Messages.getString("KnowledgeFlowApp_DoPopup_Name_JOptionPane_ShowInputDialog_Text"), oldName);
          






          if (name != null) {
            ((BeanCommon)bc).setCustomName(name);
          }
        }
      });
      if (((bc instanceof BeanCommon)) && 
        (((BeanCommon)bc).isBusy())) {
        nameItem.setEnabled(false);
      }
      
      beanContextMenu.add(nameItem);
      menuItemCount++;
    }
    

    try
    {
      Vector compInfo = new Vector(1);
      Vector associatedBeans = null;
      Vector outputBeans = null;
      Vector compInfoOutputs = null;
      
      if ((bc instanceof MetaBean)) {
        compInfo = ((MetaBean)bc).getBeanInfoSubFlow();
        associatedBeans = ((MetaBean)bc).getBeansInSubFlow();
        
        outputBeans = ((MetaBean)bc).getBeansInOutputs();
        compInfoOutputs = ((MetaBean)bc).getBeanInfoOutputs();
      } else {
        compInfo.add(Introspector.getBeanInfo(bc.getClass()));
        compInfoOutputs = compInfo;
      }
      
      final Vector tempAssociatedBeans = associatedBeans;
      
      if (compInfo == null) {
        Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_DoPopup_Error_Text_First"));
      }
      else
      {
        for (int zz = 0; zz < compInfo.size(); zz++) {
          final int tt = zz;
          final Class custClass = ((BeanInfo)compInfo.elementAt(zz)).getBeanDescriptor().getCustomizerClass();
          

          if (custClass != null)
          {


            MenuItem custItem = null;
            boolean customizationEnabled = true;
            
            if (!(bc instanceof MetaBean))
            {
              Messages.getInstance();custItem = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_CustItem_MenuItem_Text_First"));
              
              if ((bc instanceof BeanCommon)) {
                customizationEnabled = !((BeanCommon)bc).isBusy();
              }
            } else {
              String custName = custClass.getName();
              BeanInstance tbi = (BeanInstance)associatedBeans.elementAt(zz);
              if ((tbi.getBean() instanceof BeanCommon)) {
                custName = ((BeanCommon)tbi.getBean()).getCustomName();
              } else {
                if ((tbi.getBean() instanceof WekaWrapper)) {
                  custName = ((WekaWrapper)tbi.getBean()).getWrappedAlgorithm().getClass().getName();
                }
                else {
                  custName = custName.substring(0, custName.indexOf("Customizer"));
                }
                

                custName = custName.substring(custName.lastIndexOf('.') + 1, custName.length());
              }
              

              Messages.getInstance();custItem = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_CustItem_MenuItem_Text_Second") + custName);
              

              if ((tbi.getBean() instanceof BeanCommon)) {
                customizationEnabled = !((BeanCommon)tbi.getBean()).isBusy();
              }
            }
            
            custItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                if ((bc instanceof MetaBean)) {
                  KnowledgeFlowApp.this.popupCustomizer(custClass, (JComponent)((BeanInstance)tempAssociatedBeans.elementAt(tt)).getBean());
                }
                else
                {
                  KnowledgeFlowApp.this.popupCustomizer(custClass, bc);
                }
                
                KnowledgeFlowApp.this.notifyIsDirty();
              }
            });
            custItem.setEnabled(customizationEnabled);
            beanContextMenu.add(custItem);
            menuItemCount++;
          } else {
            Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_DoPopup_Error_Text_Second"));
          }
        }
        

        Vector esdV = new Vector();
        

        for (int i = 0; i < compInfo.size(); i++) {
          EventSetDescriptor[] temp = ((BeanInfo)compInfo.elementAt(i)).getEventSetDescriptors();
          


          if ((temp != null) && (temp.length > 0)) {
            esdV.add(temp);
          }
        }
        


        if (esdV.size() > 0)
        {


          Messages.getInstance();MenuItem connections = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_Connection_MenuItem_Text"));
          
          connections.setEnabled(false);
          beanContextMenu.insert(connections, menuItemCount);
          menuItemCount++;
        }
        

        final Vector finalOutputs = associatedBeans;
        
        for (int j = 0; j < esdV.size(); j++) {
          final int fj = j;
          String sourceBeanName = "";
          
          if ((bc instanceof MetaBean))
          {

            Object sourceBean = ((BeanInstance)associatedBeans.elementAt(j)).getBean();
            
            if ((sourceBean instanceof BeanCommon)) {
              sourceBeanName = ((BeanCommon)sourceBean).getCustomName();
            } else {
              if ((sourceBean instanceof WekaWrapper)) {
                sourceBeanName = ((WekaWrapper)sourceBean).getWrappedAlgorithm().getClass().getName();
              }
              else {
                sourceBeanName = sourceBean.getClass().getName();
              }
              
              sourceBeanName = sourceBeanName.substring(sourceBeanName.lastIndexOf('.') + 1, sourceBeanName.length());
            }
            
            sourceBeanName = sourceBeanName + ": ";
          }
          
          EventSetDescriptor[] esds = (EventSetDescriptor[])esdV.elementAt(j);
          
          for (int i = 0; i < esds.length; i++)
          {



            MenuItem evntItem = new MenuItem(sourceBeanName + esds[i].getName());
            final EventSetDescriptor esd = esds[i];
            

            boolean ok = true;
            
            if ((bc instanceof EventConstraints)) {
              ok = ((EventConstraints)bc).eventGeneratable(esd.getName());
            }
            
            if (ok) {
              evntItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  KnowledgeFlowApp.this.connectComponents(esd, (bc instanceof MetaBean) ? (BeanInstance)finalOutputs.elementAt(fj) : bi, xx, yy);
                  


                  KnowledgeFlowApp.this.notifyIsDirty();
                }
              });
            } else {
              evntItem.setEnabled(false);
            }
            
            beanContextMenu.add(evntItem);
            menuItemCount++;
          }
        }
      }
    } catch (IntrospectionException ie) {
      ie.printStackTrace();
    }
    


    if (((bc instanceof UserRequestAcceptor)) || ((bc instanceof Startable))) {
      Enumeration req = null;
      
      if ((bc instanceof UserRequestAcceptor)) {
        req = ((UserRequestAcceptor)bc).enumerateRequests();
      }
      
      if (((bc instanceof Startable)) || ((req != null) && (req.hasMoreElements())))
      {


        Messages.getInstance();MenuItem actions = new MenuItem(Messages.getString("KnowledgeFlowApp_DoPopup_Actions_Text"));
        
        actions.setEnabled(false);
        beanContextMenu.insert(actions, menuItemCount);
        menuItemCount++;
      }
      
      if ((bc instanceof Startable)) {
        String tempS = ((Startable)bc).getStartMessage();
        insertUserOrStartableMenuItem(bc, true, tempS, beanContextMenu);
      }
      
      while ((req != null) && (req.hasMoreElements())) {
        String tempS = (String)req.nextElement();
        insertUserOrStartableMenuItem(bc, false, tempS, beanContextMenu);
        menuItemCount++;
      }
    }
    


    if (menuItemCount > 0)
    {
      m_beanLayout.add(beanContextMenu);
      beanContextMenu.show(m_beanLayout, x, y);
    }
  }
  

  private void insertUserOrStartableMenuItem(final JComponent bc, final boolean startable, String tempS, PopupMenu beanContextMenu)
  {
    boolean disabled = false;
    boolean confirmRequest = false;
    

    if (tempS.charAt(0) == '$') {
      tempS = tempS.substring(1, tempS.length());
      disabled = true;
    }
    

    if (tempS.charAt(0) == '?') {
      tempS = tempS.substring(1, tempS.length());
      confirmRequest = true;
    }
    
    final String tempS2 = tempS;
    

    MenuItem custItem = new MenuItem(tempS2);
    if (confirmRequest) {
      custItem.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          Messages.getInstance();int result = JOptionPane.showConfirmDialog(KnowledgeFlowApp.this, tempS2, Messages.getString("KnowledgeFlowApp_InsertUserOrStartableMenuItem_Result_JOptionPane_ShowConfirmDialog_Text"), 0);
          







          if (result == 0) {
            Thread startPointThread = new Thread()
            {
              public void run() {
                try {
                  if (val$startable) {
                    boolean proceed = true;
                    if (KnowledgeFlowApp.m_Memory.memoryIsLow()) {
                      proceed = KnowledgeFlowApp.m_Memory.showMemoryIsLow();
                    }
                    if (proceed) {
                      ((Startable)val$bc).start();
                    }
                  } else if ((val$bc instanceof UserRequestAcceptor)) {
                    ((UserRequestAcceptor)val$bc).performRequest(val$tempS2);
                  }
                  KnowledgeFlowApp.this.notifyIsDirty();
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
            };
            startPointThread.setPriority(1);
            startPointThread.start();
          }
        }
      });
    } else {
      custItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Thread startPointThread = new Thread()
          {
            public void run() {
              try {
                if (val$startable) {
                  boolean proceed = true;
                  if (KnowledgeFlowApp.m_Memory.memoryIsLow()) {
                    proceed = KnowledgeFlowApp.m_Memory.showMemoryIsLow();
                  }
                  if (proceed) {
                    ((Startable)val$bc).start();
                  }
                } else if ((val$bc instanceof UserRequestAcceptor)) {
                  ((UserRequestAcceptor)val$bc).performRequest(val$tempS2);
                }
                KnowledgeFlowApp.this.notifyIsDirty();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          };
          startPointThread.setPriority(1);
          startPointThread.start();
        }
      });
    }
    
    if (disabled) {
      custItem.setEnabled(false);
    }
    
    beanContextMenu.add(custItem);
  }
  





  private void popupCustomizer(Class custClass, JComponent bc)
  {
    try
    {
      final Object customizer = custClass.newInstance();
      
      if ((customizer instanceof EnvironmentHandler)) {
        ((EnvironmentHandler)customizer).setEnvironment(m_flowEnvironment);
      }
      ((Customizer)customizer).setObject(bc);
      final JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add((JComponent)customizer, "Center");
      if ((customizer instanceof CustomizerCloseRequester)) {
        ((CustomizerCloseRequester)customizer).setParentFrame(jf);
      }
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          if ((customizer instanceof CustomizerClosingListener)) {
            ((CustomizerClosingListener)customizer).customizerClosing();
          }
          jf.dispose();
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  







  private void addToUserToolBar(MetaBean bean, boolean installListener)
  {
    if (m_userToolBar == null)
    {
      setUpUserToolBar();
    }
    



    Vector tempRemovedConnections = new Vector();
    Vector allConnections = BeanConnection.getConnections();
    Vector inputs = bean.getInputs();
    Vector outputs = bean.getOutputs();
    Vector allComps = bean.getSubFlow();
    
    for (int i = 0; i < inputs.size(); i++) {
      BeanInstance temp = (BeanInstance)inputs.elementAt(i);
      
      for (int j = 0; j < allConnections.size(); j++) {
        BeanConnection tempC = (BeanConnection)allConnections.elementAt(j);
        if (tempC.getTarget() == temp) {
          tempRemovedConnections.add(tempC);
        }
        


        if ((tempC.getSource() == temp) && (!bean.subFlowContains(tempC.getTarget())))
        {
          tempRemovedConnections.add(tempC);
        }
      }
    }
    
    for (int i = 0; i < outputs.size(); i++) {
      BeanInstance temp = (BeanInstance)outputs.elementAt(i);
      
      for (int j = 0; j < allConnections.size(); j++) {
        BeanConnection tempC = (BeanConnection)allConnections.elementAt(j);
        if (tempC.getSource() == temp) {
          tempRemovedConnections.add(tempC);
        }
      }
    }
    
    for (int i = 0; i < tempRemovedConnections.size(); i++) {
      BeanConnection temp = (BeanConnection)tempRemovedConnections.elementAt(i);
      
      temp.remove();
    }
    

    JPanel tempUser = instantiateToolBarMetaBean(bean);
    m_userBoxPanel.add(tempUser);
    if ((installListener) && (m_firstUserComponentOpp)) {
      try {
        installWindowListenerForSavingUserBeans();
        m_firstUserComponentOpp = false;
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    
    BeanConnection newC;
    for (int i = 0; i < tempRemovedConnections.size(); i++) {
      BeanConnection temp = (BeanConnection)tempRemovedConnections.elementAt(i);
      
      newC = new BeanConnection(temp.getSource(), temp.getTarget(), temp.getSourceEventSetDescriptor());
    }
  }
  










  private void deleteConnectionPopup(Vector closestConnections, int x, int y)
  {
    if (closestConnections.size() > 0) {
      int menuItemCount = 0;
      


      PopupMenu deleteConnectionMenu = new PopupMenu();
      



      Messages.getInstance();MenuItem deleteConnection = new MenuItem(Messages.getString("KnowledgeFlowApp_DeleteConnectionPopup_DeleteConnection_MenuItem_Text"));
      



      deleteConnection.setEnabled(false);
      deleteConnectionMenu.insert(deleteConnection, menuItemCount);
      menuItemCount++;
      
      for (int i = 0; i < closestConnections.size(); i++) {
        final BeanConnection bc = (BeanConnection)closestConnections.elementAt(i);
        
        String connName = bc.getSourceEventSetDescriptor().getName();
        

        String targetName = "";
        if ((bc.getTarget().getBean() instanceof BeanCommon)) {
          targetName = ((BeanCommon)bc.getTarget().getBean()).getCustomName();
        } else {
          targetName = bc.getTarget().getBean().getClass().getName();
          targetName = targetName.substring(targetName.lastIndexOf('.') + 1, targetName.length());
        }
        
        MenuItem deleteItem = new MenuItem(connName + "-->" + targetName);
        deleteItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            bc.remove();
            m_beanLayout.revalidate();
            m_beanLayout.repaint();
            KnowledgeFlowApp.this.notifyIsDirty();
          }
        });
        deleteConnectionMenu.add(deleteItem);
        menuItemCount++;
      }
      

      m_beanLayout.add(deleteConnectionMenu);
      deleteConnectionMenu.show(m_beanLayout, x, y);
    }
  }
  









  private void connectComponents(EventSetDescriptor esd, BeanInstance bi, int x, int y)
  {
    m_sourceEventSetDescriptor = esd;
    
    Class listenerClass = esd.getListenerType();
    JComponent source = (JComponent)bi.getBean();
    

    int targetCount = 0;
    Vector beanInstances = BeanInstance.getBeanInstances();
    for (int i = 0; i < beanInstances.size(); i++) {
      JComponent bean = (JComponent)((BeanInstance)beanInstances.elementAt(i)).getBean();
      
      boolean connectable = false;
      boolean canContinue = false;
      if (bean != source) {
        if ((bean instanceof MetaBean)) {
          if (((MetaBean)bean).canAcceptConnection(listenerClass)) {
            canContinue = true;
          }
        } else if ((listenerClass.isInstance(bean)) && (bean != source)) {
          canContinue = true;
        }
      }
      if (canContinue) {
        if (!(bean instanceof BeanCommon)) {
          connectable = true;



        }
        else if (((BeanCommon)bean).connectionAllowed(esd))
        {

          connectable = true;
        }
        
        if ((connectable) && 
          ((bean instanceof Visible))) {
          targetCount++;
          ((Visible)bean).getVisual().setDisplayConnectors(true);
        }
      }
    }
    


    if (targetCount > 0)
    {
      if ((source instanceof Visible)) {
        ((Visible)source).getVisual().setDisplayConnectors(true);
      }
      
      m_editElement = bi;
      Point closest = ((Visible)source).getVisual().getClosestConnectorPoint(new Point(x, y));
      

      m_startX = ((int)closest.getX());
      m_startY = ((int)closest.getY());
      m_oldX = m_startX;
      m_oldY = m_startY;
      
      Graphics2D gx = (Graphics2D)m_beanLayout.getGraphics();
      gx.setXORMode(Color.white);
      gx.drawLine(m_startX, m_startY, m_startX, m_startY);
      gx.dispose();
      m_mode = 2;
    }
  }
  
  private void addComponent(BeanInstance comp, boolean repaint) {
    if ((comp.getBean() instanceof Visible)) {
      ((Visible)comp.getBean()).getVisual().addPropertyChangeListener(this);
    }
    if ((comp.getBean() instanceof BeanCommon)) {
      ((BeanCommon)comp.getBean()).setLog(m_logPanel);
    }
    if ((comp.getBean() instanceof MetaBean))
    {


      Vector list = ((MetaBean)comp.getBean()).getInputs();
      for (int i = 0; i < list.size(); i++) {
        ((BeanInstance)list.get(i)).setX(comp.getX());
        ((BeanInstance)list.get(i)).setY(comp.getY());
      }
      
      list = ((MetaBean)comp.getBean()).getOutputs();
      for (int i = 0; i < list.size(); i++) {
        ((BeanInstance)list.get(i)).setX(comp.getX());
        ((BeanInstance)list.get(i)).setY(comp.getY());
      }
    }
    setCursor(Cursor.getPredefinedCursor(0));
    if (repaint) {
      m_beanLayout.repaint();
    }
    m_pointerB.setSelected(true);
    m_mode = 0;
  }
  
  private void addComponent(int x, int y) {
    if ((m_toolBarBean instanceof MetaBean))
    {

      Vector associatedConnections = ((MetaBean)m_toolBarBean).getAssociatedConnections();
      
      BeanConnection.getConnections().addAll(associatedConnections);
    }
    
    if ((m_toolBarBean instanceof BeanContextChild)) {
      m_bcSupport.add(m_toolBarBean);
    }
    BeanInstance bi = new BeanInstance(m_beanLayout, m_toolBarBean, x, y);
    
    m_toolBarBean = null;
    addComponent(bi, true);
  }
  





  private void checkSubFlow(int startX, int startY, int endX, int endY)
  {
    Rectangle r = new Rectangle(startX < endX ? startX : endX, startY < endY ? startY : endY, Math.abs(startX - endX), Math.abs(startY - endY));
    


    Vector selected = BeanInstance.findInstances(r);
    

    Vector inputs = BeanConnection.inputs(selected);
    Vector outputs = BeanConnection.outputs(selected);
    

    if ((inputs.size() == 0) || (outputs.size() == 0)) {
      return;
    }
    


    for (int i = 0; i < selected.size(); i++) {
      BeanInstance temp = (BeanInstance)selected.elementAt(i);
      if ((temp.getBean() instanceof MetaBean)) {
        return;
      }
    }
    

    for (int i = 0; i < selected.size(); i++) {
      BeanInstance temp = (BeanInstance)selected.elementAt(i);
      if ((temp.getBean() instanceof Visible)) {
        ((Visible)temp.getBean()).getVisual().setDisplayConnectors(true);
      }
    }
    

    for (int i = 0; i < inputs.size(); i++) {
      BeanInstance temp = (BeanInstance)inputs.elementAt(i);
      if ((temp.getBean() instanceof Visible)) {
        ((Visible)temp.getBean()).getVisual().setDisplayConnectors(true, Color.red);
      }
    }
    


    for (int i = 0; i < outputs.size(); i++) {
      BeanInstance temp = (BeanInstance)outputs.elementAt(i);
      if ((temp.getBean() instanceof Visible)) {
        ((Visible)temp.getBean()).getVisual().setDisplayConnectors(true, Color.green);
      }
    }
    

    BufferedImage subFlowPreview = null;
    try {
      subFlowPreview = createImage(m_beanLayout, r);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    


    Messages.getInstance();Messages.getInstance();int result = JOptionPane.showConfirmDialog(this, Messages.getString("KnowledgeFlowApp_CheckSubFlow_Result_JOptionPane_ShowConfirmDialog_Text_First"), Messages.getString("KnowledgeFlowApp_CheckSubFlow_Result_JOptionPane_ShowConfirmDialog_Text_Second"), 0);
    










    if (result == 0) {
      Vector associatedConnections = BeanConnection.associatedConnections(selected);
      

      Messages.getInstance();Messages.getInstance();String name = JOptionPane.showInputDialog(this, Messages.getString("KnowledgeFlowApp_CheckSubFlow_Result_Name_ShowConfirmDialog_Text_First"), Messages.getString("KnowledgeFlowApp_CheckSubFlow_Result_Name_ShowConfirmDialog_Text_Second"));
      









      if (name != null) {
        MetaBean group = new MetaBean();
        group.setSubFlow(selected);
        group.setAssociatedConnections(associatedConnections);
        group.setInputs(inputs);
        group.setOutputs(outputs);
        group.setSubFlowPreview(new ImageIcon(subFlowPreview));
        if (name.length() > 0)
        {
          group.setCustomName(name);
        }
        
        if ((group instanceof BeanContextChild)) {
          m_bcSupport.add(group);
        }
        BeanInstance bi = new BeanInstance(m_beanLayout, group, (int)r.getX() + (int)(r.getWidth() / 2.0D), (int)r.getY() + (int)(r.getHeight() / 2.0D));
        

        for (int i = 0; i < selected.size(); i++) {
          BeanInstance temp = (BeanInstance)selected.elementAt(i);
          temp.removeBean(m_beanLayout);
          if ((temp.getBean() instanceof Visible)) {
            ((Visible)temp.getBean()).getVisual().removePropertyChangeListener(this);
          }
        }
        
        for (int i = 0; i < associatedConnections.size(); i++) {
          BeanConnection temp = (BeanConnection)associatedConnections.elementAt(i);
          
          temp.setHidden(true);
        }
        group.shiftBeans(bi, true);
        
        addComponent(bi, true);
      }
    }
    

    for (int i = 0; i < selected.size(); i++) {
      BeanInstance temp = (BeanInstance)selected.elementAt(i);
      if ((temp.getBean() instanceof Visible)) {
        ((Visible)temp.getBean()).getVisual().setDisplayConnectors(false);
      }
    }
  }
  




  public void propertyChange(PropertyChangeEvent e)
  {
    revalidate();
    m_beanLayout.repaint();
  }
  


  private void loadLayout()
  {
    m_loadB.setEnabled(false);
    m_saveB.setEnabled(false);
    int returnVal = m_FileChooser.showOpenDialog(this);
    if (returnVal == 0) {
      stopFlow();
      

      File oFile = m_FileChooser.getSelectedFile();
      
      m_flowEnvironment.addVariable("Internal.knowledgeflow.directory", oFile.getParent());
      


      if (m_FileChooser.getFileFilter() == m_KfFilter) {
        if (!oFile.getName().toLowerCase().endsWith(".kf")) {
          oFile = new File(oFile.getParent(), oFile.getName() + ".kf");
        }
      } else if (m_FileChooser.getFileFilter() == m_KOMLFilter) {
        if (!oFile.getName().toLowerCase().endsWith(".komlkf")) {
          oFile = new File(oFile.getParent(), oFile.getName() + ".koml" + "kf");
        }
      }
      else if (m_FileChooser.getFileFilter() == m_XMLFilter) {
        if (!oFile.getName().toLowerCase().endsWith(".kfml")) {
          oFile = new File(oFile.getParent(), oFile.getName() + ".kfml");
        }
      }
      else if ((m_FileChooser.getFileFilter() == m_XStreamFilter) && 
        (!oFile.getName().toLowerCase().endsWith(".xstreamkf")))
      {
        oFile = new File(oFile.getParent(), oFile.getName() + ".xstream" + "kf");
      }
      

      try
      {
        Vector beans = new Vector();
        Vector connections = new Vector();
        

        if ((KOML.isPresent()) && (oFile.getAbsolutePath().toLowerCase().endsWith(".komlkf")))
        {

          Vector v = (Vector)KOML.read(oFile.getAbsolutePath());
          beans = (Vector)v.get(0);
          connections = (Vector)v.get(1);
        } else if ((XStream.isPresent()) && (oFile.getAbsolutePath().toLowerCase().endsWith(".xstreamkf")))
        {

          Vector v = (Vector)XStream.read(oFile.getAbsolutePath());
          beans = (Vector)v.get(0);
          connections = (Vector)v.get(1);
        } else if (oFile.getAbsolutePath().toLowerCase().endsWith(".kfml"))
        {
          XMLBeans xml = new XMLBeans(m_beanLayout, m_bcSupport);
          Vector v = (Vector)xml.read(oFile);
          beans = (Vector)v.get(0);
          connections = (Vector)v.get(1);
        }
        else {
          InputStream is = new FileInputStream(oFile);
          ObjectInputStream ois = new ObjectInputStream(is);
          beans = (Vector)ois.readObject();
          connections = (Vector)ois.readObject();
          ois.close();
        }
        
        integrateFlow(beans, connections);
        setEnvironment();
        m_logPanel.clearStatus();
        Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_LoadLayout_StatusMessage_Text_First"));
      }
      catch (Exception ex) {
        Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_LoadLayout_StatusMessage_Text_Second"));
        
        Messages.getInstance();Messages.getInstance();m_logPanel.logMessage(Messages.getString("KnowledgeFlowApp_LoadLayout_LogMessage_Text_First") + ex.getMessage() + Messages.getString("KnowledgeFlowApp_LoadLayout_LogMessage_Text_Second"));
        



        ex.printStackTrace();
      }
    }
    m_loadB.setEnabled(true);
    m_saveB.setEnabled(true);
  }
  
  private void integrateFlow(Vector beans, Vector connections)
  {
    Color bckC = getBackground();
    m_bcSupport = new BeanContextSupport();
    m_bcSupport.setDesignTime(true);
    


    for (int i = 0; i < beans.size(); i++) {
      BeanInstance tempB = (BeanInstance)beans.elementAt(i);
      if ((tempB.getBean() instanceof Visible)) {
        ((Visible)tempB.getBean()).getVisual().addPropertyChangeListener(this);
        



        ((Visible)tempB.getBean()).getVisual().setBackground(bckC);
        ((JComponent)tempB.getBean()).setBackground(bckC);
      }
      if ((tempB.getBean() instanceof BeanCommon)) {
        ((BeanCommon)tempB.getBean()).setLog(m_logPanel);
      }
      if ((tempB.getBean() instanceof BeanContextChild)) {
        m_bcSupport.add(tempB.getBean());
      }
    }
    BeanInstance.setBeanInstances(beans, m_beanLayout);
    BeanConnection.setConnections(connections);
    m_beanLayout.revalidate();
    m_beanLayout.repaint();
  }
  








  public void setFlow(Vector v)
    throws Exception
  {
    clearLayout();
    SerializedObject so = new SerializedObject(v);
    Vector copy = (Vector)so.getObject();
    
    Vector beans = (Vector)copy.elementAt(0);
    Vector connections = (Vector)copy.elementAt(1);
    

    m_flowEnvironment = new Environment();
    integrateFlow(beans, connections);
  }
  





  public Vector getFlow()
    throws Exception
  {
    Vector v = new Vector();
    Vector beans = BeanInstance.getBeanInstances();
    Vector connections = BeanConnection.getConnections();
    detachFromLayout(beans);
    v.add(beans);
    v.add(connections);
    
    SerializedObject so = new SerializedObject(v);
    Vector copy = (Vector)so.getObject();
    


    integrateFlow(beans, connections);
    return copy;
  }
  







  protected static BufferedImage createImage(JComponent component, Rectangle region)
    throws IOException
  {
    boolean opaqueValue = component.isOpaque();
    component.setOpaque(true);
    BufferedImage image = new BufferedImage(width, height, 1);
    
    Graphics2D g2d = image.createGraphics();
    g2d.translate(-region.getX(), -region.getY());
    
    component.paint(g2d);
    g2d.dispose();
    component.setOpaque(opaqueValue);
    
    return image;
  }
  

  private void detachFromLayout(Vector beans)
  {
    for (int i = 0; i < beans.size(); i++) {
      BeanInstance tempB = (BeanInstance)beans.elementAt(i);
      if ((tempB.getBean() instanceof Visible)) {
        ((Visible)tempB.getBean()).getVisual().removePropertyChangeListener(this);
        

        if ((tempB.getBean() instanceof MetaBean)) {
          ((MetaBean)tempB.getBean()).removePropertyChangeListenersSubFlow(this);
        }
        






        ((Visible)tempB.getBean()).getVisual().setBackground(Color.white);
        
        ((JComponent)tempB.getBean()).setBackground(Color.white);
      }
    }
  }
  




  private void saveLayout()
  {
    int returnVal = m_FileChooser.showSaveDialog(this);
    Color bckC = getBackground();
    if (returnVal == 0)
    {


      Vector beans = BeanInstance.getBeanInstances();
      detachFromLayout(beans);
      

      File sFile = m_FileChooser.getSelectedFile();
      

      if (m_FileChooser.getFileFilter() == m_KfFilter) {
        if (!sFile.getName().toLowerCase().endsWith(".kf")) {
          sFile = new File(sFile.getParent(), sFile.getName() + ".kf");
        }
      } else if (m_FileChooser.getFileFilter() == m_KOMLFilter) {
        if (!sFile.getName().toLowerCase().endsWith(".komlkf")) {
          sFile = new File(sFile.getParent(), sFile.getName() + ".koml" + "kf");
        }
      }
      else if (m_FileChooser.getFileFilter() == m_XStreamFilter) {
        if (!sFile.getName().toLowerCase().endsWith(".xstreamkf"))
        {
          sFile = new File(sFile.getParent(), sFile.getName() + ".xstream" + "kf");
        }
      }
      else if ((m_FileChooser.getFileFilter() == m_XMLFilter) && 
        (!sFile.getName().toLowerCase().endsWith(".kfml"))) {
        sFile = new File(sFile.getParent(), sFile.getName() + ".kfml");
      }
      



      try
      {
        if ((KOML.isPresent()) && (sFile.getAbsolutePath().toLowerCase().endsWith(".komlkf")))
        {

          Vector v = new Vector();
          v.setSize(2);
          v.set(0, beans);
          v.set(1, BeanConnection.getConnections());
          KOML.write(sFile.getAbsolutePath(), v);
        } else if ((XStream.isPresent()) && (sFile.getAbsolutePath().toLowerCase().endsWith(".xstreamkf")))
        {

          Vector v = new Vector();
          v.setSize(2);
          v.set(0, beans);
          v.set(1, BeanConnection.getConnections());
          XStream.write(sFile.getAbsolutePath(), v);
        } else if (sFile.getAbsolutePath().toLowerCase().endsWith(".kfml"))
        {
          Vector v = new Vector();
          v.setSize(2);
          v.set(0, beans);
          v.set(1, BeanConnection.getConnections());
          XMLBeans xml = new XMLBeans(m_beanLayout, m_bcSupport);
          
          BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sFile), "UTF-8"));
          
          xml.write(br, v);
        } else {
          OutputStream os = new FileOutputStream(sFile);
          ObjectOutputStream oos = new ObjectOutputStream(os);
          oos.writeObject(beans);
          oos.writeObject(BeanConnection.getConnections());
          oos.flush();
          oos.close();
        }
        Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_SaveLayout_StatusMessage_Text_First"));
        



        m_flowEnvironment.addVariable("Internal.knowledgeflow.directory", sFile.getParent());
        
        setEnvironment();
      } catch (Exception ex) {
        Messages.getInstance();m_logPanel.statusMessage(Messages.getString("KnowledgeFlowApp_SaveLayout_StatusMessage_Text_Second"));
        
        Messages.getInstance();Messages.getInstance();m_logPanel.logMessage(Messages.getString("KnowledgeFlowApp_SaveLayout_LogMessage_Text_First") + ex.getMessage() + Messages.getString("KnowledgeFlowApp_SaveLayout_LogMessage_Text_Second"));
        



        ex.printStackTrace();
      }
      finally {
        for (int i = 0; i < beans.size(); i++) {
          BeanInstance tempB = (BeanInstance)beans.elementAt(i);
          if ((tempB.getBean() instanceof Visible)) {
            ((Visible)tempB.getBean()).getVisual().addPropertyChangeListener(this);
            

            if ((tempB.getBean() instanceof MetaBean)) {
              ((MetaBean)tempB.getBean()).addPropertyChangeListenersSubFlow(this);
            }
            

            ((Visible)tempB.getBean()).getVisual().setBackground(bckC);
            ((JComponent)tempB.getBean()).setBackground(bckC);
          }
        }
      }
    }
  }
  









  public void saveLayout(OutputStream out)
  {
    Vector beans = BeanInstance.getBeanInstances();
    
    for (int i = 0; i < beans.size(); i++) {
      BeanInstance tempB = (BeanInstance)beans.elementAt(i);
      
      if ((tempB.getBean() instanceof Visible)) {
        ((Visible)tempB.getBean()).getVisual().removePropertyChangeListener(this);
        

        if ((tempB.getBean() instanceof MetaBean)) {
          ((MetaBean)tempB.getBean()).removePropertyChangeListenersSubFlow(this);
        }
      }
    }
    

    try
    {
      Vector v = new Vector();
      v.setSize(2);
      v.set(0, beans);
      v.set(1, BeanConnection.getConnections());
      
      XMLBeans xml = new XMLBeans(m_beanLayout, m_bcSupport);
      xml.write(out, v);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    finally {
      for (int i = 0; i < beans.size(); i++) {
        BeanInstance tempB = (BeanInstance)beans.elementAt(i);
        
        if ((tempB.getBean() instanceof Visible)) {
          ((Visible)tempB.getBean()).getVisual().addPropertyChangeListener(this);
          

          if ((tempB.getBean() instanceof MetaBean)) {
            ((MetaBean)tempB.getBean()).addPropertyChangeListenersSubFlow(this);
          }
        }
      }
    }
  }
  
  private void loadUserComponents()
  {
    Vector tempV = null;
    String ext = "";
    if (m_UserComponentsInXML)
      ext = ".xml";
    File sFile = new File(System.getProperty("user.home") + File.separator + ".knowledgeFlow" + File.separator + "userComponents" + ext);
    
    if (sFile.exists()) {
      try {
        if (m_UserComponentsInXML) {
          XMLBeans xml = new XMLBeans(m_beanLayout, m_bcSupport, 1);
          
          tempV = (Vector)xml.read(sFile);
        } else {
          InputStream is = new FileInputStream(sFile);
          ObjectInputStream ois = new ObjectInputStream(is);
          tempV = (Vector)ois.readObject();
          ois.close();
        }
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_LoadUserComponents_Error_Text"));
        
        ex.printStackTrace();
        return;
      }
      if (tempV.size() > 0)
      {
        for (int i = 0; i < tempV.size(); i++) {
          MetaBean tempB = (MetaBean)tempV.elementAt(i);
          addToUserToolBar(tempB, false);
        }
      }
    }
  }
  
  private void installWindowListenerForSavingUserBeans() {
    ((Window)getTopLevelAncestor()).addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        Messages.getInstance();System.out.println(Messages.getString("KnowledgeFlowApp_InstallWindowListenerForSavingUserBeans_Text"));
        



        File sFile = new File(System.getProperty("user.home") + File.separator + ".knowledgeFlow");
        
        if (!sFile.exists()) {
          if (!sFile.mkdir()) {
            Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_InstallWindowListenerForSavingUserBeans_Error_Text_First"));


          }
          else
          {

            sFile = new File(sFile.toString() + File.separator + "plugins");
            sFile.mkdir();
          }
        }
        try {
          String ext = "";
          if (m_UserComponentsInXML)
            ext = ".xml";
          File sFile2 = new File(sFile.getAbsolutePath() + File.separator + "userComponents" + ext);
          

          if (m_UserComponentsInXML) {
            XMLBeans xml = new XMLBeans(m_beanLayout, m_bcSupport, 1);
            
            xml.write(sFile2, m_userComponents);
          } else {
            OutputStream os = new FileOutputStream(sFile2);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(m_userComponents);
            oos.flush();
            oos.close();
          }
        } catch (Exception ex) {
          Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_InstallWindowListenerForSavingUserBeans_Error_Text_Second"));
          



          ex.printStackTrace();
        }
      }
    });
  }
  








  public static String getGlobalInfo(Object tempBean)
  {
    String gi = null;
    try {
      BeanInfo bi = Introspector.getBeanInfo(tempBean.getClass());
      MethodDescriptor[] methods = bi.getMethodDescriptors();
      for (int i = 0; i < methods.length; i++) {
        String name = methods[i].getDisplayName();
        Method meth = methods[i].getMethod();
        if ((name.equals("globalInfo")) && 
          (meth.getReturnType().equals(String.class))) {
          Object[] args = new Object[0];
          String globalInfo = (String)meth.invoke(tempBean, args);
          gi = globalInfo;
          break;
        }
      }
    }
    catch (Exception ex) {}
    

    return gi;
  }
  








  private static Memory m_Memory = new Memory(true);
  


  public static Vector s_startupListeners = new Vector();
  





  private boolean m_showFileMenu;
  





  public static void createSingleton(String[] args)
  {
    String fileName = null;
    boolean showFileMenu = true;
    
    if ((args != null) && (args.length > 0)) {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        
        if (arg.startsWith("file=")) {
          fileName = arg.substring("file=".length());
        } else if (arg.startsWith("showFileMenu=")) {
          showFileMenu = Boolean.parseBoolean(arg.substring("showFileMenu=".length()));
        }
      }
    }
    

    if (m_knowledgeFlow == null) {
      m_knowledgeFlow = new KnowledgeFlowApp(showFileMenu);
    }
    



    for (int i = 0; i < s_startupListeners.size(); i++) {
      ((StartUpListener)s_startupListeners.elementAt(i)).startUpComplete();
    }
    

    if (fileName != null) {
      m_knowledgeFlow.loadInitialLayout(fileName);
    }
  }
  






  public static KnowledgeFlowApp getSingleton()
  {
    return m_knowledgeFlow;
  }
  




  public static void addStartupListener(StartUpListener s)
  {
    s_startupListeners.add(s);
  }
  





  public void loadInitialLayout(String fileName)
  {
    stopFlow();
    
    File oFile = new File(fileName);
    
    if ((oFile.exists()) && (oFile.isFile())) {
      m_FileChooser.setSelectedFile(oFile);
      
      int index = fileName.lastIndexOf('.');
      
      if (index != -1) {
        String extension = fileName.substring(index);
        
        if (".kfml".equalsIgnoreCase(extension)) {
          m_FileChooser.setFileFilter(m_knowledgeFlowm_XMLFilter);
        } else if (".kf".equalsIgnoreCase(extension)) {
          m_FileChooser.setFileFilter(m_knowledgeFlowm_KfFilter);
        }
      }
    } else {
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_LoadInitialLayout_Error_Text_First") + fileName + Messages.getString("KnowledgeFlowApp_LoadInitialLayout_Error_Text_Second"));
    }
    



    try
    {
      Vector beans = new Vector();
      Vector connections = new Vector();
      

      if ((KOML.isPresent()) && (oFile.getAbsolutePath().toLowerCase().endsWith(".koml")))
      {

        Vector v = (Vector)KOML.read(oFile.getAbsolutePath());
        beans = (Vector)v.get(0);
        connections = (Vector)v.get(1);
      } else if (oFile.getAbsolutePath().toLowerCase().endsWith(".kfml"))
      {
        XMLBeans xml = new XMLBeans(m_beanLayout, m_bcSupport);
        Vector v = (Vector)xml.read(oFile);
        beans = (Vector)v.get(0);
        connections = (Vector)v.get(1);
      }
      else
      {
        InputStream is = new FileInputStream(oFile);
        ObjectInputStream ois = new ObjectInputStream(is);
        beans = (Vector)ois.readObject();
        connections = (Vector)ois.readObject();
        ois.close();
      }
      
      Color bckC = getBackground();
      m_bcSupport = new BeanContextSupport();
      m_bcSupport.setDesignTime(true);
      


      for (int i = 0; i < beans.size(); i++) {
        BeanInstance tempB = (BeanInstance)beans.elementAt(i);
        
        if ((tempB.getBean() instanceof Visible)) {
          ((Visible)tempB.getBean()).getVisual().addPropertyChangeListener(this);
          



          ((Visible)tempB.getBean()).getVisual().setBackground(bckC);
          ((JComponent)tempB.getBean()).setBackground(bckC);
        }
        
        if ((tempB.getBean() instanceof BeanCommon)) {
          ((BeanCommon)tempB.getBean()).setLog(m_logPanel);
        }
        
        if ((tempB.getBean() instanceof BeanContextChild)) {
          m_bcSupport.add(tempB.getBean());
        }
      }
      
      BeanInstance.setBeanInstances(beans, m_beanLayout);
      BeanConnection.setConnections(connections);
      File fullPath = new File(oFile.getAbsolutePath().toString());
      m_flowEnvironment.addVariable("Internal.knowledgeflow.directory", fullPath.getParent());
      
      setEnvironment();
      
      m_beanLayout.revalidate();
      m_beanLayout.repaint();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  








  private void notifyIsDirty()
  {
    firePropertyChange("PROP_DIRTY", null, null);
  }
  





  public static void main(String[] args)
  {
    LookAndFeel.setLookAndFeel();
    


    try
    {
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      


      for (int i = 0; i < args.length; i++) {
        if ((args[i].toLowerCase().endsWith(".kf")) || (args[i].toLowerCase().endsWith(".kfml")))
        {
          args[i] = ("file=" + args[i]);
        }
      }
      
      createSingleton(args);
      
      Image icon = Toolkit.getDefaultToolkit().getImage(m_knowledgeFlow.getClass().getClassLoader().getResource("weka/gui/weka_icon_new_48.png"));
      

      jf.setIconImage(icon);
      
      jf.getContentPane().add(m_knowledgeFlow, "Center");
      jf.setDefaultCloseOperation(3);
      
      jf.setSize(1000, 750);
      jf.setVisible(true);
      
      Thread memMonitor = new Thread()
      {
        public void run()
        {
          try {
            for (;;) {
              sleep(4000L);
              
              System.gc();
              
              if (KnowledgeFlowApp.m_Memory.isOutOfMemory())
              {
                val$jf.dispose();
                KnowledgeFlowApp.access$3002(null);
                System.gc();
                

                Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_Main_Error_Text_First"));
                
                KnowledgeFlowApp.m_Memory.showOutOfMemory();
                Messages.getInstance();System.err.println(Messages.getString("KnowledgeFlowApp_Main_Error_Text_Second"));
                
                System.exit(-1);
              }
            }
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
          
        }
        
      };
      memMonitor.setPriority(5);
      memMonitor.start();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

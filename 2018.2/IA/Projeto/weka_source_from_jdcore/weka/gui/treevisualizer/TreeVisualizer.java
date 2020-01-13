package weka.gui.treevisualizer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.Timer;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.visualize.PrintablePanel;
import weka.gui.visualize.VisualizePanel;
import weka.gui.visualize.VisualizeUtils;



































































































































































public class TreeVisualizer
  extends PrintablePanel
  implements MouseMotionListener, MouseListener, ActionListener, ItemListener
{
  private static final long serialVersionUID = -8668637962504080749L;
  public static final String PROPERTIES_FILE = "weka/gui/treevisualizer/TreeVisualizer.props";
  private NodePlace m_placer;
  private Node m_topNode;
  private Dimension m_viewPos;
  private Dimension m_viewSize;
  private Font m_currentFont;
  private FontMetrics m_fontSize;
  private int m_numNodes;
  private int m_numLevels;
  private NodeInfo[] m_nodes;
  private EdgeInfo[] m_edges;
  private Timer m_frameLimiter;
  private int m_mouseState;
  private Dimension m_oldMousePos;
  private Dimension m_newMousePos;
  private boolean m_clickAvailable;
  private Dimension m_nViewPos;
  private Dimension m_nViewSize;
  private int m_scaling;
  private JPopupMenu m_winMenu;
  private JMenuItem m_topN;
  private JMenuItem m_fitToScreen;
  private JMenuItem m_autoScale;
  private JMenu m_selectFont;
  private ButtonGroup m_selectFontGroup;
  private JRadioButtonMenuItem m_size24;
  private JRadioButtonMenuItem m_size22;
  private JRadioButtonMenuItem m_size20;
  private JRadioButtonMenuItem m_size18;
  private JRadioButtonMenuItem m_size16;
  private JRadioButtonMenuItem m_size14;
  private JRadioButtonMenuItem m_size12;
  private JRadioButtonMenuItem m_size10;
  private JRadioButtonMenuItem m_size8;
  private JRadioButtonMenuItem m_size6;
  private JRadioButtonMenuItem m_size4;
  private JRadioButtonMenuItem m_size2;
  private JRadioButtonMenuItem m_size1;
  private JMenuItem m_accept;
  private JPopupMenu m_nodeMenu;
  private JMenuItem m_visualise;
  private JMenuItem m_addChildren;
  private JMenuItem m_remChildren;
  private JMenuItem m_classifyChild;
  private JMenuItem m_sendInstances;
  private int m_focusNode;
  private int m_highlightNode;
  private TreeDisplayListener m_listener;
  private JTextField m_searchString;
  private JDialog m_searchWin;
  private JRadioButton m_caseSen;
  protected Color m_FontColor = null;
  

  protected Color m_BackgroundColor = null;
  

  protected Color m_NodeColor = null;
  

  protected Color m_LineColor = null;
  

  protected Color m_ZoomBoxColor = null;
  

  protected Color m_ZoomBoxXORColor = null;
  

  protected boolean m_ShowBorder = true;
  














  public TreeVisualizer(TreeDisplayListener tdl, String dot, NodePlace p)
  {
    initialize();
    

    if (m_ShowBorder) {
      Messages.getInstance();setBorder(BorderFactory.createTitledBorder(Messages.getString("TreeVisualizer_BorderFactoryCreateTitledBorder_Text_First"))); }
    m_listener = tdl;
    
    TreeBuild builder = new TreeBuild();
    
    Node n = null;
    NodePlace arrange = new PlaceNode2();
    n = builder.create(new StringReader(dot));
    

    m_highlightNode = 5;
    m_topNode = n;
    m_placer = p;
    m_placer.place(m_topNode);
    m_viewPos = new Dimension(0, 0);
    m_viewSize = new Dimension(800, 600);
    


    m_nViewPos = new Dimension(0, 0);
    m_nViewSize = new Dimension(800, 600);
    
    m_scaling = 0;
    
    m_numNodes = Node.getCount(m_topNode, 0);
    


    m_numLevels = Node.getHeight(m_topNode, 0);
    
    m_nodes = new NodeInfo[m_numNodes];
    m_edges = new EdgeInfo[m_numNodes - 1];
    

    arrayFill(m_topNode, m_nodes, m_edges);
    
    changeFontSize(12);
    
    m_mouseState = 0;
    m_oldMousePos = new Dimension(0, 0);
    m_newMousePos = new Dimension(0, 0);
    m_frameLimiter = new Timer(120, this);
    


    m_winMenu = new JPopupMenu();
    Messages.getInstance();m_topN = new JMenuItem(Messages.getString("TreeVisualizer_TopN_JMenuItem_Text_First"));
    
    Messages.getInstance();m_topN.setActionCommand(Messages.getString("TreeVisualizer_TopN_JMenuItem_SetActionCommand_Text_First"));
    
    Messages.getInstance();m_fitToScreen = new JMenuItem(Messages.getString("TreeVisualizer_FitToScreen_JMenuItem_Text_First"));
    Messages.getInstance();m_fitToScreen.setActionCommand(Messages.getString("TreeVisualizer_FitToScreen_JMenuItem_SetActionCommand_Text_First"));
    
    Messages.getInstance();m_selectFont = new JMenu(Messages.getString("TreeVisualizer_SelectFont_JMenu_Text_First"));
    Messages.getInstance();m_selectFont.setActionCommand(Messages.getString("TreeVisualizer_SelectFont_JMenu_SetActionCommand_Text_First"));
    Messages.getInstance();m_autoScale = new JMenuItem(Messages.getString("TreeVisualizer_AutoScale_JMenuItem_Text_First"));
    Messages.getInstance();m_autoScale.setActionCommand(Messages.getString("TreeVisualizer_AutoScale_JMenuItem_SetActionCommand_Text_First"));
    m_selectFontGroup = new ButtonGroup();
    
    Messages.getInstance();m_accept = new JMenuItem(Messages.getString("TreeVisualizer_Accept_JMenuItem_Text_First"));
    Messages.getInstance();m_accept.setActionCommand(Messages.getString("TreeVisualizer_Accept_JMenuItem_SetActionCommand_Text_First"));
    
    m_winMenu.add(m_topN);
    m_winMenu.addSeparator();
    m_winMenu.add(m_fitToScreen);
    m_winMenu.add(m_autoScale);
    

    m_winMenu.addSeparator();
    m_winMenu.add(m_selectFont);
    
    if (m_listener != null) {
      m_winMenu.addSeparator();
      m_winMenu.add(m_accept);
    }
    
    m_topN.addActionListener(this);
    m_fitToScreen.addActionListener(this);
    
    m_autoScale.addActionListener(this);
    m_accept.addActionListener(this);
    
    Messages.getInstance();m_size24 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size24_Text_First"), false);
    Messages.getInstance();m_size22 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size22_JRadioButtonMenuItem_Size22_Text_First"), false);
    Messages.getInstance();m_size20 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size20_JRadioButtonMenuItem_Size20_Text_First"), false);
    Messages.getInstance();m_size18 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size18_JRadioButtonMenuItem_Size18_Text_First"), false);
    Messages.getInstance();m_size16 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size16_JRadioButtonMenuItem_Size16_Text_First"), false);
    Messages.getInstance();m_size14 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size14_JRadioButtonMenuItem_Size14_Text_First"), false);
    Messages.getInstance();m_size12 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size12_JRadioButtonMenuItem_Size12_Text_First"), true);
    Messages.getInstance();m_size10 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size10_JRadioButtonMenuItem_Size10_Text_First"), false);
    Messages.getInstance();m_size8 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size8_JRadioButtonMenuItem_Size8_Text_First"), false);
    Messages.getInstance();m_size6 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size6_JRadioButtonMenuItem_Size6_Text_First"), false);
    Messages.getInstance();m_size4 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size4_JRadioButtonMenuItem_Size4_Text_First"), false);
    Messages.getInstance();m_size2 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size2_JRadioButtonMenuItem_Size2_Text_First"), false);
    Messages.getInstance();m_size1 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_Size1_Text_First"), false);
    
    Messages.getInstance();m_size24.setActionCommand(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_SetActionCommand_Size24_Text_First"));
    Messages.getInstance();m_size22.setActionCommand(Messages.getString("TreeVisualizer_Size22_JRadioButtonMenuItem_SetActionCommand_Size22_Text_First"));
    Messages.getInstance();m_size20.setActionCommand(Messages.getString("TreeVisualizer_Size20_JRadioButtonMenuItem_SetActionCommand_Size20_Text_First"));
    Messages.getInstance();m_size18.setActionCommand(Messages.getString("TreeVisualizer_Size18_JRadioButtonMenuItem_SetActionCommand_Size18_Text_First"));
    Messages.getInstance();m_size16.setActionCommand(Messages.getString("TreeVisualizer_Size16_JRadioButtonMenuItem_SetActionCommand_Size16_Text_First"));
    Messages.getInstance();m_size14.setActionCommand(Messages.getString("TreeVisualizer_Size14_JRadioButtonMenuItem_SetActionCommand_Size14_Text_First"));
    Messages.getInstance();m_size12.setActionCommand(Messages.getString("TreeVisualizer_Size12_JRadioButtonMenuItem_SetActionCommand_Size12_Text_First"));
    Messages.getInstance();m_size10.setActionCommand(Messages.getString("TreeVisualizer_Size10_JRadioButtonMenuItem_SetActionCommand_Size10_Text_First"));
    Messages.getInstance();m_size8.setActionCommand(Messages.getString("TreeVisualizer_Size8_JRadioButtonMenuItem_SetActionCommand_Size8_Text_First"));
    Messages.getInstance();m_size6.setActionCommand(Messages.getString("TreeVisualizer_Size6_JRadioButtonMenuItem_SetActionCommand_Size6_Text_First"));
    Messages.getInstance();m_size4.setActionCommand(Messages.getString("TreeVisualizer_Size4_JRadioButtonMenuItem_SetActionCommand_Size4_Text_First"));
    Messages.getInstance();m_size2.setActionCommand(Messages.getString("TreeVisualizer_Size2_JRadioButtonMenuItem_SetActionCommand_Size2_Text_First"));
    Messages.getInstance();m_size1.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size1_Text_First"));
    

    m_selectFontGroup.add(m_size24);
    m_selectFontGroup.add(m_size22);
    m_selectFontGroup.add(m_size20);
    m_selectFontGroup.add(m_size18);
    m_selectFontGroup.add(m_size16);
    m_selectFontGroup.add(m_size14);
    m_selectFontGroup.add(m_size12);
    m_selectFontGroup.add(m_size10);
    m_selectFontGroup.add(m_size8);
    m_selectFontGroup.add(m_size6);
    m_selectFontGroup.add(m_size4);
    m_selectFontGroup.add(m_size2);
    m_selectFontGroup.add(m_size1);
    

    m_selectFont.add(m_size24);
    m_selectFont.add(m_size22);
    m_selectFont.add(m_size20);
    m_selectFont.add(m_size18);
    m_selectFont.add(m_size16);
    m_selectFont.add(m_size14);
    m_selectFont.add(m_size12);
    m_selectFont.add(m_size10);
    m_selectFont.add(m_size8);
    m_selectFont.add(m_size6);
    m_selectFont.add(m_size4);
    m_selectFont.add(m_size2);
    m_selectFont.add(m_size1);
    

    m_size24.addItemListener(this);
    m_size22.addItemListener(this);
    m_size20.addItemListener(this);
    m_size18.addItemListener(this);
    m_size16.addItemListener(this);
    m_size14.addItemListener(this);
    m_size12.addItemListener(this);
    m_size10.addItemListener(this);
    m_size8.addItemListener(this);
    m_size6.addItemListener(this);
    m_size4.addItemListener(this);
    m_size2.addItemListener(this);
    m_size1.addItemListener(this);
    























    m_nodeMenu = new JPopupMenu();
    
    Messages.getInstance();m_visualise = new JMenuItem(Messages.getString("TreeVisualizer_Visualise_JMenuItem_Text_First"));
    Messages.getInstance();m_visualise.setActionCommand(Messages.getString("TreeVisualizer_Visualise_JMenuItem_SetActionCommand_Text_First"));
    m_visualise.addActionListener(this);
    m_nodeMenu.add(m_visualise);
    
    if (m_listener != null) {
      Messages.getInstance();m_remChildren = new JMenuItem(Messages.getString("TreeVisualizer_RemChildren_JMenuItem_Text_First"));
      Messages.getInstance();m_remChildren.setActionCommand(Messages.getString("TreeVisualizer_RemChildren_JMenuItem_SetActionCommand_Text_First"));
      m_remChildren.addActionListener(this);
      m_nodeMenu.add(m_remChildren);
      

      Messages.getInstance();m_classifyChild = new JMenuItem(Messages.getString("TreeVisualizer_ClassifyChild_JMenuItem_Text_First"));
      Messages.getInstance();m_classifyChild.setActionCommand(Messages.getString("TreeVisualizer_ClassifyChild_JMenuItem_SetActionCommand_Text_First"));
      m_classifyChild.addActionListener(this);
      m_nodeMenu.add(m_classifyChild);
    }
    






    m_focusNode = -1;
    m_highlightNode = -1;
    
    addMouseMotionListener(this);
    addMouseListener(this);
    

    m_frameLimiter.setRepeats(false);
    m_frameLimiter.start();
  }
  








  public TreeVisualizer(TreeDisplayListener tdl, Node n, NodePlace p)
  {
    initialize();
    

    if (m_ShowBorder) {
      Messages.getInstance();setBorder(BorderFactory.createTitledBorder(Messages.getString("TreeVisualizer_BorderFactoryCreateTitledBorder_Text_Second"))); }
    m_listener = tdl;
    m_topNode = n;
    m_placer = p;
    m_placer.place(m_topNode);
    m_viewPos = new Dimension(0, 0);
    m_viewSize = new Dimension(800, 600);
    


    m_nViewPos = new Dimension(0, 0);
    m_nViewSize = new Dimension(800, 600);
    
    m_scaling = 0;
    
    m_numNodes = Node.getCount(m_topNode, 0);
    


    m_numLevels = Node.getHeight(m_topNode, 0);
    
    m_nodes = new NodeInfo[m_numNodes];
    m_edges = new EdgeInfo[m_numNodes - 1];
    
    arrayFill(m_topNode, m_nodes, m_edges);
    
    changeFontSize(12);
    
    m_mouseState = 0;
    m_oldMousePos = new Dimension(0, 0);
    m_newMousePos = new Dimension(0, 0);
    m_frameLimiter = new Timer(120, this);
    
    m_winMenu = new JPopupMenu();
    Messages.getInstance();m_topN = new JMenuItem(Messages.getString("TreeVisualizer_TopN_JMenuItem_Text_Second"));
    
    Messages.getInstance();m_topN.setActionCommand(Messages.getString("TreeVisualizer_TopN_JMenuItem_SetActionCommand_Text_Second"));
    
    Messages.getInstance();m_fitToScreen = new JMenuItem(Messages.getString("TreeVisualizer_FitToScreen_JMenuItem_Text_Second"));
    Messages.getInstance();m_fitToScreen.setActionCommand(Messages.getString("TreeVisualizer_FitToScreen_JMenuItem_SetActionCommand_Text_Second"));
    
    Messages.getInstance();m_selectFont = new JMenu(Messages.getString("TreeVisualizer_SelectFont_JMenu_Text_Second"));
    Messages.getInstance();m_selectFont.setActionCommand(Messages.getString("TreeVisualizer_SelectFont_JMenu_SetActionCommand_Text_Second"));
    Messages.getInstance();m_autoScale = new JMenuItem(Messages.getString("TreeVisualizer_AutoScale_JMenuItem_Text_Second"));
    Messages.getInstance();m_autoScale.setActionCommand(Messages.getString("TreeVisualizer_AutoScale_JMenuItem_SetActionCommand_Text_Second"));
    m_selectFontGroup = new ButtonGroup();
    
    Messages.getInstance();m_accept = new JMenuItem(Messages.getString("TreeVisualizer_Accept_JMenuItem_Text_Second"));
    Messages.getInstance();m_accept.setActionCommand(Messages.getString("TreeVisualizer_Accept_JMenuItem_SetActionCommand_Text_Second"));
    
    m_winMenu.add(m_topN);
    m_winMenu.addSeparator();
    m_winMenu.add(m_fitToScreen);
    m_winMenu.add(m_autoScale);
    m_winMenu.addSeparator();
    
    m_winMenu.addSeparator();
    m_winMenu.add(m_selectFont);
    m_winMenu.addSeparator();
    
    if (m_listener != null) {
      m_winMenu.add(m_accept);
    }
    
    m_topN.addActionListener(this);
    m_fitToScreen.addActionListener(this);
    
    m_autoScale.addActionListener(this);
    m_accept.addActionListener(this);
    
    Messages.getInstance();m_size24 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size24_Text_Second"), false);
    Messages.getInstance();m_size22 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size22_Text_Second"), false);
    Messages.getInstance();m_size20 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size20_Text_Second"), false);
    Messages.getInstance();m_size18 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size18_Text_Second"), false);
    Messages.getInstance();m_size16 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size16_Text_Second"), false);
    Messages.getInstance();m_size14 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size14_Text_Second"), false);
    Messages.getInstance();m_size12 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size12_Text_Second"), true);
    Messages.getInstance();m_size10 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size10_Text_Second"), false);
    Messages.getInstance();m_size8 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size8_Text_Second"), false);
    Messages.getInstance();m_size6 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size6_Text_Second"), false);
    Messages.getInstance();m_size4 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size4_Text_Second"), false);
    Messages.getInstance();m_size2 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size2_Text_Second"), false);
    Messages.getInstance();m_size1 = new JRadioButtonMenuItem(Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size1_Text_Second"), false);
    
    Messages.getInstance();m_size24.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size24_Text_Second"));
    Messages.getInstance();m_size22.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size22_Text_Second"));
    Messages.getInstance();m_size20.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size20_Text_Second"));
    Messages.getInstance();m_size18.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size18_Text_Second"));
    Messages.getInstance();m_size16.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size16_Text_Second"));
    Messages.getInstance();m_size14.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size14_Text_Second"));
    Messages.getInstance();m_size12.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size12_Text_Second"));
    Messages.getInstance();m_size10.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size10_Text_Second"));
    Messages.getInstance();m_size8.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size8_Text_Second"));
    Messages.getInstance();m_size6.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size6_Text_Second"));
    Messages.getInstance();m_size4.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size4_Text_Second"));
    Messages.getInstance();m_size2.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size2_Text_Second"));
    Messages.getInstance();m_size1.setActionCommand(Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size1_Text_Second"));
    




    m_selectFontGroup.add(m_size24);
    m_selectFontGroup.add(m_size22);
    m_selectFontGroup.add(m_size20);
    m_selectFontGroup.add(m_size18);
    m_selectFontGroup.add(m_size16);
    m_selectFontGroup.add(m_size14);
    m_selectFontGroup.add(m_size12);
    m_selectFontGroup.add(m_size10);
    m_selectFontGroup.add(m_size8);
    m_selectFontGroup.add(m_size6);
    m_selectFontGroup.add(m_size4);
    m_selectFontGroup.add(m_size2);
    m_selectFontGroup.add(m_size1);
    



    m_selectFont.add(m_size24);
    m_selectFont.add(m_size22);
    m_selectFont.add(m_size20);
    m_selectFont.add(m_size18);
    m_selectFont.add(m_size16);
    m_selectFont.add(m_size14);
    m_selectFont.add(m_size12);
    m_selectFont.add(m_size10);
    m_selectFont.add(m_size8);
    m_selectFont.add(m_size6);
    m_selectFont.add(m_size4);
    m_selectFont.add(m_size2);
    m_selectFont.add(m_size1);
    

    m_size24.addItemListener(this);
    m_size22.addItemListener(this);
    m_size20.addItemListener(this);
    m_size18.addItemListener(this);
    m_size16.addItemListener(this);
    m_size14.addItemListener(this);
    m_size12.addItemListener(this);
    m_size10.addItemListener(this);
    m_size8.addItemListener(this);
    m_size6.addItemListener(this);
    m_size4.addItemListener(this);
    m_size2.addItemListener(this);
    m_size1.addItemListener(this);
    



























    m_nodeMenu = new JPopupMenu();
    
    Messages.getInstance();m_visualise = new JMenuItem(Messages.getString("TreeVisualizer_Visualise_JMenuItem_Text_Second"));
    Messages.getInstance();m_visualise.setActionCommand(Messages.getString("TreeVisualizer_Visualise_JMenuItem_SetActionCommand_Text_Second"));
    m_visualise.addActionListener(this);
    m_nodeMenu.add(m_visualise);
    
    if (m_listener != null) {
      Messages.getInstance();m_remChildren = new JMenuItem(Messages.getString("TreeVisualizer_RemChildren_JMenuItem_Text_Second"));
      Messages.getInstance();m_remChildren.setActionCommand(Messages.getString("TreeVisualizer_RemChildren_JMenuItem_SetActionCommand_Text_Second"));
      m_remChildren.addActionListener(this);
      m_nodeMenu.add(m_remChildren);
      
      Messages.getInstance();m_classifyChild = new JMenuItem(Messages.getString("TreeVisualizer_ClassifyChild_JMenuItem_Text_Second"));
      Messages.getInstance();m_classifyChild.setActionCommand(Messages.getString("TreeVisualizer_ClassifyChild_JMenuItem_SetActionCommand_Text_Second"));
      m_classifyChild.addActionListener(this);
      m_nodeMenu.add(m_classifyChild);
      
      Messages.getInstance();m_sendInstances = new JMenuItem(Messages.getString("TreeVisualizer_SendInstances_JMenuItem_Text"));
      Messages.getInstance();m_sendInstances.setActionCommand(Messages.getString("TreeVisualizer_SendInstances_JMenuItem_SetActionCommand_Text"));
      m_sendInstances.addActionListener(this);
      m_nodeMenu.add(m_sendInstances);
    }
    


    m_focusNode = -1;
    m_highlightNode = -1;
    

    addMouseMotionListener(this);
    addMouseListener(this);
    




    m_frameLimiter.setRepeats(false);
    m_frameLimiter.start();
  }
  







  protected Color getColor(String colorStr)
  {
    Color result = null;
    
    if ((colorStr != null) && (colorStr.length() > 0)) {
      result = VisualizeUtils.processColour(colorStr, result);
    }
    return result;
  }
  

  protected void initialize()
  {
    Properties props;
    
    try
    {
      props = Utils.readProperties("weka/gui/treevisualizer/TreeVisualizer.props");
    }
    catch (Exception e) {
      e.printStackTrace();
      props = new Properties();
    }
    
    m_FontColor = getColor(props.getProperty("FontColor", ""));
    m_BackgroundColor = getColor(props.getProperty("BackgroundColor", ""));
    m_NodeColor = getColor(props.getProperty("NodeColor", ""));
    m_LineColor = getColor(props.getProperty("LineColor", ""));
    m_ZoomBoxColor = getColor(props.getProperty("ZoomBoxColor", ""));
    m_ZoomBoxXORColor = getColor(props.getProperty("ZoomBoxXORColor", ""));
    m_ShowBorder = Boolean.parseBoolean(props.getProperty("ShowBorder", "true"));
  }
  





  public void fitToScreen()
  {
    getScreenFit(m_viewPos, m_viewSize);
    repaint();
  }
  



  private void getScreenFit(Dimension np, Dimension ns)
  {
    int leftmost = 1000000;int rightmost = -1000000;
    int leftCenter = 1000000;int rightCenter = -1000000;int rightNode = 0;
    int highest = -1000000;int highTop = -1000000;
    for (int noa = 0; noa < m_numNodes; noa++) {
      calcScreenCoords(noa);
      if (m_nodes[noa].m_center - m_nodes[noa].m_side < leftmost) {
        leftmost = m_nodes[noa].m_center - m_nodes[noa].m_side;
      }
      if (m_nodes[noa].m_center < leftCenter) {
        leftCenter = m_nodes[noa].m_center;
      }
      
      if (m_nodes[noa].m_center + m_nodes[noa].m_side > rightmost) {
        rightmost = m_nodes[noa].m_center + m_nodes[noa].m_side;
      }
      if (m_nodes[noa].m_center > rightCenter) {
        rightCenter = m_nodes[noa].m_center;
        rightNode = noa;
      }
      if (m_nodes[noa].m_top + m_nodes[noa].m_height > highest) {
        highest = m_nodes[noa].m_top + m_nodes[noa].m_height;
      }
      if (m_nodes[noa].m_top > highTop) {
        highTop = m_nodes[noa].m_top;
      }
    }
    
    width = getWidth();
    width -= leftCenter - leftmost + rightmost - rightCenter + 30;
    height = (getHeight() - highest + highTop - 40);
    
    if ((m_nodes[rightNode].m_node.getCenter() != 0.0D) && (leftCenter != rightCenter))
    {
      Dimension tmp353_352 = ns;353352width = ((int)(353352width / m_nodes[rightNode].m_node.getCenter()));
    }
    if (width < 10)
    {
      width = 10;
    }
    if (height < 10)
    {
      height = 10;
    }
    
    width = ((leftCenter - leftmost + rightmost - rightCenter) / 2 + 15);
    height = ((highest - highTop) / 2 + 20);
  }
  







  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand() == null) {
      if (m_scaling == 0) {
        repaint();
      }
      else {
        animateScaling(m_nViewPos, m_nViewSize, m_scaling);
      }
    } else {
      Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_FitToScreen_Text")))
      {
        Dimension np = new Dimension();
        Dimension ns = new Dimension();
        
        getScreenFit(np, ns);
        
        animateScaling(np, ns, 10);
      }
      else {
        Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_CenterOnTopNode_Text")))
        {
          int tpx = (int)(m_topNode.getCenter() * m_viewSize.width);
          
          int tpy = (int)(m_topNode.getTop() * m_viewSize.height);
          


          Dimension np = new Dimension(getSizewidth / 2 - tpx, getSizewidth / 6 - tpy);
          

          animateScaling(np, m_viewSize, 10);
        }
        else {
          Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_AutoScale_JMenuItem_SetActionCommand_Text_Second"))) {
            autoScale();
          }
          else {
            Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_VisualizeTheNode_Text")))
            {
              if (m_focusNode >= 0) {
                Instances inst;
                if ((inst = m_nodes[m_focusNode].m_node.getInstances()) != null) {
                  VisualizePanel pan = new VisualizePanel();
                  pan.setInstances(inst);
                  JFrame nf = new JFrame();
                  nf.setSize(400, 300);
                  nf.getContentPane().add(pan);
                  nf.setVisible(true);
                }
                else {
                  Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Second"), 2);
                }
                
              }
              else
              {
                Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
              }
            }
            else
            {
              Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_CreateChildNodes_Text"))) {
                if (m_focusNode >= 0) {
                  if (m_listener != null)
                  {
                    m_listener.userCommand(new TreeDisplayEvent(1, m_nodes[m_focusNode].m_node.getRefer()));

                  }
                  else
                  {
                    Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Sixth"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Seventh"), 2);
                  }
                  
                }
                else
                {
                  Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eighth"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eighth"), 0);
                }
              }
              else
              {
                Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_RemoveChildNodes_Text"))) {
                  if (m_focusNode >= 0) {
                    if (m_listener != null)
                    {
                      m_listener.userCommand(new TreeDisplayEvent(2, m_nodes[m_focusNode].m_node.getRefer()));

                    }
                    else
                    {
                      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Nineth"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Tenth"), 2);
                    }
                    
                  }
                  else
                  {
                    Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eleventh"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Twelveth"), 0);
                  }
                }
                else
                {
                  Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_Classify_Child_Text"))) {
                    if (m_focusNode >= 0) {
                      if (m_listener != null)
                      {
                        m_listener.userCommand(new TreeDisplayEvent(4, m_nodes[m_focusNode].m_node.getRefer()));

                      }
                      else
                      {
                        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Thirteenth"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Fourteenth"), 2);
                      }
                      
                    }
                    else
                    {
                      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Fifteenth"), "\t\t\t" + Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Sixteenth"), 0);
                    }
                  }
                  else
                  {
                    Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_Send_Instances_Text"))) {
                      if (m_focusNode >= 0) {
                        if (m_listener != null)
                        {
                          m_listener.userCommand(new TreeDisplayEvent(5, m_nodes[m_focusNode].m_node.getRefer()));

                        }
                        else
                        {
                          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Seventeenth"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eighteenth"), 2);
                        }
                        
                      }
                      else
                      {
                        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Nineteenth"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Twentyth"), 0);
                      }
                    }
                    else
                    {
                      Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("TreeVisualizer_ActionPerformed_AcceptTheTree_Text")))
                        if (m_listener != null)
                        {
                          m_listener.userCommand(new TreeDisplayEvent(3, null));
                        }
                        else
                        {
                          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_TwentyFirst"), Messages.getString("TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_TwentySecond"), 2);
                        }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  public void itemStateChanged(ItemEvent e) {
    JRadioButtonMenuItem c = (JRadioButtonMenuItem)e.getSource();
    Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size24_Text"))) {
      changeFontSize(24);
    } else {
      Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size22_Text"))) {
        changeFontSize(22);
      } else {
        Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size20_Text"))) {
          changeFontSize(20);
        } else {
          Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size18_Text"))) {
            changeFontSize(18);
          } else {
            Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size16_Text"))) {
              changeFontSize(16);
            } else {
              Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size14_Text"))) {
                changeFontSize(14);
              } else {
                Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size12_Text"))) {
                  changeFontSize(12);
                } else {
                  Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size10_Text"))) {
                    changeFontSize(10);
                  } else {
                    Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size8_Text"))) {
                      changeFontSize(8);
                    } else {
                      Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size6_Text"))) {
                        changeFontSize(6);
                      } else {
                        Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size4_Text"))) {
                          changeFontSize(4);
                        } else {
                          Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size2_Text"))) {
                            changeFontSize(2);
                          } else {
                            Messages.getInstance(); if (c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size1_Text"))) {
                              changeFontSize(1);
                            } else {
                              Messages.getInstance(); if (!c.getActionCommand().equals(Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_HideDescendants_Text"))) {}
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        } } } }
  
  public void mouseClicked(MouseEvent e) { if (m_clickAvailable)
    {
      int s = -1;
      
      for (int noa = 0; noa < m_numNodes; noa++) {
        if (m_nodes[noa].m_quad == 18)
        {
          calcScreenCoords(noa);
          if ((e.getX() <= m_nodes[noa].m_center + m_nodes[noa].m_side) && (e.getX() >= m_nodes[noa].m_center - m_nodes[noa].m_side) && (e.getY() >= m_nodes[noa].m_top) && (e.getY() <= m_nodes[noa].m_top + m_nodes[noa].m_height))
          {




            s = noa;
          }
          m_nodes[noa].m_top = 32000;
        }
      }
      m_focusNode = s;
      
      if (m_focusNode != -1) {
        if (m_listener != null)
        {
          Messages.getInstance();actionPerformed(new ActionEvent(this, 32000, Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_ActionPerformed_Text_First")));

        }
        else
        {
          Messages.getInstance();actionPerformed(new ActionEvent(this, 32000, Messages.getString("TreeVisualizer_ItemStateChanged_GetActionCommand_ActionPerformed_Text_Second")));
        }
      }
    }
  }
  
  public void mousePressed(MouseEvent e)
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 49	weka/gui/treevisualizer/TreeVisualizer:m_frameLimiter	Ljavax/swing/Timer;
    //   4: iconst_1
    //   5: invokevirtual 138	javax/swing/Timer:setRepeats	(Z)V
    //   8: aload_1
    //   9: invokevirtual 302	java/awt/event/MouseEvent:getModifiers	()I
    //   12: aload_1
    //   13: pop
    //   14: bipush 16
    //   16: iand
    //   17: ifeq +294 -> 311
    //   20: aload_1
    //   21: invokevirtual 303	java/awt/event/MouseEvent:isAltDown	()Z
    //   24: ifne +287 -> 311
    //   27: aload_0
    //   28: getfield 44	weka/gui/treevisualizer/TreeVisualizer:m_mouseState	I
    //   31: ifne +280 -> 311
    //   34: aload_0
    //   35: getfield 33	weka/gui/treevisualizer/TreeVisualizer:m_scaling	I
    //   38: ifne +273 -> 311
    //   41: aload_1
    //   42: invokevirtual 302	java/awt/event/MouseEvent:getModifiers	()I
    //   45: aload_1
    //   46: pop
    //   47: iconst_2
    //   48: iand
    //   49: ifeq +22 -> 71
    //   52: aload_1
    //   53: invokevirtual 302	java/awt/event/MouseEvent:getModifiers	()I
    //   56: aload_1
    //   57: pop
    //   58: iconst_1
    //   59: iand
    //   60: ifne +11 -> 71
    //   63: aload_0
    //   64: iconst_2
    //   65: putfield 44	weka/gui/treevisualizer/TreeVisualizer:m_mouseState	I
    //   68: goto +293 -> 361
    //   71: aload_1
    //   72: invokevirtual 302	java/awt/event/MouseEvent:getModifiers	()I
    //   75: aload_1
    //   76: pop
    //   77: iconst_1
    //   78: iand
    //   79: ifeq +173 -> 252
    //   82: aload_1
    //   83: invokevirtual 302	java/awt/event/MouseEvent:getModifiers	()I
    //   86: aload_1
    //   87: pop
    //   88: iconst_2
    //   89: iand
    //   90: ifne +162 -> 252
    //   93: aload_0
    //   94: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   97: aload_1
    //   98: invokevirtual 295	java/awt/event/MouseEvent:getX	()I
    //   101: putfield 216	java/awt/Dimension:width	I
    //   104: aload_0
    //   105: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   108: aload_1
    //   109: invokevirtual 296	java/awt/event/MouseEvent:getY	()I
    //   112: putfield 218	java/awt/Dimension:height	I
    //   115: aload_0
    //   116: getfield 46	weka/gui/treevisualizer/TreeVisualizer:m_newMousePos	Ljava/awt/Dimension;
    //   119: aload_1
    //   120: invokevirtual 295	java/awt/event/MouseEvent:getX	()I
    //   123: putfield 216	java/awt/Dimension:width	I
    //   126: aload_0
    //   127: getfield 46	weka/gui/treevisualizer/TreeVisualizer:m_newMousePos	Ljava/awt/Dimension;
    //   130: aload_1
    //   131: invokevirtual 296	java/awt/event/MouseEvent:getY	()I
    //   134: putfield 218	java/awt/Dimension:height	I
    //   137: aload_0
    //   138: iconst_3
    //   139: putfield 44	weka/gui/treevisualizer/TreeVisualizer:m_mouseState	I
    //   142: aload_0
    //   143: invokevirtual 304	weka/gui/treevisualizer/TreeVisualizer:getGraphics	()Ljava/awt/Graphics;
    //   146: astore_2
    //   147: aload_0
    //   148: getfield 6	weka/gui/treevisualizer/TreeVisualizer:m_ZoomBoxColor	Ljava/awt/Color;
    //   151: ifnonnull +13 -> 164
    //   154: aload_2
    //   155: getstatic 305	java/awt/Color:black	Ljava/awt/Color;
    //   158: invokevirtual 306	java/awt/Graphics:setColor	(Ljava/awt/Color;)V
    //   161: goto +11 -> 172
    //   164: aload_2
    //   165: aload_0
    //   166: getfield 6	weka/gui/treevisualizer/TreeVisualizer:m_ZoomBoxColor	Ljava/awt/Color;
    //   169: invokevirtual 306	java/awt/Graphics:setColor	(Ljava/awt/Color;)V
    //   172: aload_0
    //   173: getfield 7	weka/gui/treevisualizer/TreeVisualizer:m_ZoomBoxXORColor	Ljava/awt/Color;
    //   176: ifnonnull +13 -> 189
    //   179: aload_2
    //   180: getstatic 307	java/awt/Color:white	Ljava/awt/Color;
    //   183: invokevirtual 308	java/awt/Graphics:setXORMode	(Ljava/awt/Color;)V
    //   186: goto +11 -> 197
    //   189: aload_2
    //   190: aload_0
    //   191: getfield 7	weka/gui/treevisualizer/TreeVisualizer:m_ZoomBoxXORColor	Ljava/awt/Color;
    //   194: invokevirtual 308	java/awt/Graphics:setXORMode	(Ljava/awt/Color;)V
    //   197: aload_2
    //   198: aload_0
    //   199: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   202: getfield 216	java/awt/Dimension:width	I
    //   205: aload_0
    //   206: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   209: getfield 218	java/awt/Dimension:height	I
    //   212: aload_0
    //   213: getfield 46	weka/gui/treevisualizer/TreeVisualizer:m_newMousePos	Ljava/awt/Dimension;
    //   216: getfield 216	java/awt/Dimension:width	I
    //   219: aload_0
    //   220: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   223: getfield 216	java/awt/Dimension:width	I
    //   226: isub
    //   227: aload_0
    //   228: getfield 46	weka/gui/treevisualizer/TreeVisualizer:m_newMousePos	Ljava/awt/Dimension;
    //   231: getfield 218	java/awt/Dimension:height	I
    //   234: aload_0
    //   235: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   238: getfield 218	java/awt/Dimension:height	I
    //   241: isub
    //   242: invokevirtual 309	java/awt/Graphics:drawRect	(IIII)V
    //   245: aload_2
    //   246: invokevirtual 310	java/awt/Graphics:dispose	()V
    //   249: goto +112 -> 361
    //   252: aload_0
    //   253: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   256: aload_1
    //   257: invokevirtual 295	java/awt/event/MouseEvent:getX	()I
    //   260: putfield 216	java/awt/Dimension:width	I
    //   263: aload_0
    //   264: getfield 45	weka/gui/treevisualizer/TreeVisualizer:m_oldMousePos	Ljava/awt/Dimension;
    //   267: aload_1
    //   268: invokevirtual 296	java/awt/event/MouseEvent:getY	()I
    //   271: putfield 218	java/awt/Dimension:height	I
    //   274: aload_0
    //   275: getfield 46	weka/gui/treevisualizer/TreeVisualizer:m_newMousePos	Ljava/awt/Dimension;
    //   278: aload_1
    //   279: invokevirtual 295	java/awt/event/MouseEvent:getX	()I
    //   282: putfield 216	java/awt/Dimension:width	I
    //   285: aload_0
    //   286: getfield 46	weka/gui/treevisualizer/TreeVisualizer:m_newMousePos	Ljava/awt/Dimension;
    //   289: aload_1
    //   290: invokevirtual 296	java/awt/event/MouseEvent:getY	()I
    //   293: putfield 218	java/awt/Dimension:height	I
    //   296: aload_0
    //   297: iconst_1
    //   298: putfield 44	weka/gui/treevisualizer/TreeVisualizer:m_mouseState	I
    //   301: aload_0
    //   302: getfield 49	weka/gui/treevisualizer/TreeVisualizer:m_frameLimiter	Ljavax/swing/Timer;
    //   305: invokevirtual 139	javax/swing/Timer:start	()V
    //   308: goto +53 -> 361
    //   311: aload_1
    //   312: invokevirtual 311	java/awt/event/MouseEvent:getButton	()I
    //   315: iconst_1
    //   316: if_icmpne +31 -> 347
    //   319: aload_1
    //   320: invokevirtual 303	java/awt/event/MouseEvent:isAltDown	()Z
    //   323: ifeq +24 -> 347
    //   326: aload_1
    //   327: invokevirtual 312	java/awt/event/MouseEvent:isShiftDown	()Z
    //   330: ifeq +17 -> 347
    //   333: aload_1
    //   334: invokevirtual 313	java/awt/event/MouseEvent:isControlDown	()Z
    //   337: ifne +10 -> 347
    //   340: aload_0
    //   341: invokevirtual 314	weka/gui/treevisualizer/TreeVisualizer:saveComponent	()V
    //   344: goto +17 -> 361
    //   347: aload_0
    //   348: getfield 44	weka/gui/treevisualizer/TreeVisualizer:m_mouseState	I
    //   351: ifne +10 -> 361
    //   354: aload_0
    //   355: getfield 33	weka/gui/treevisualizer/TreeVisualizer:m_scaling	I
    //   358: ifne +3 -> 361
    //   361: return
    // Line number table:
    //   Java source line #1113	-> byte code offset #0
    //   Java source line #1114	-> byte code offset #8
    //   Java source line #1120	-> byte code offset #41
    //   Java source line #1122	-> byte code offset #63
    //   Java source line #1124	-> byte code offset #71
    //   Java source line #1127	-> byte code offset #93
    //   Java source line #1128	-> byte code offset #104
    //   Java source line #1129	-> byte code offset #115
    //   Java source line #1130	-> byte code offset #126
    //   Java source line #1131	-> byte code offset #137
    //   Java source line #1133	-> byte code offset #142
    //   Java source line #1134	-> byte code offset #147
    //   Java source line #1135	-> byte code offset #154
    //   Java source line #1137	-> byte code offset #164
    //   Java source line #1138	-> byte code offset #172
    //   Java source line #1139	-> byte code offset #179
    //   Java source line #1141	-> byte code offset #189
    //   Java source line #1142	-> byte code offset #197
    //   Java source line #1145	-> byte code offset #245
    //   Java source line #1146	-> byte code offset #249
    //   Java source line #1149	-> byte code offset #252
    //   Java source line #1150	-> byte code offset #263
    //   Java source line #1151	-> byte code offset #274
    //   Java source line #1152	-> byte code offset #285
    //   Java source line #1153	-> byte code offset #296
    //   Java source line #1154	-> byte code offset #301
    //   Java source line #1159	-> byte code offset #311
    //   Java source line #1160	-> byte code offset #340
    //   Java source line #1162	-> byte code offset #347
    //   Java source line #1167	-> byte code offset #361
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	362	0	this	TreeVisualizer
    //   0	362	1	e	MouseEvent
    //   146	100	2	g	Graphics
  }
  
  public void mouseReleased(MouseEvent e)
  {
    if (m_mouseState == 1)
    {

      m_clickAvailable = true;

    }
    else
    {
      m_clickAvailable = false;
    }
    if ((m_mouseState == 2) && (mouseInBounds(e)))
    {
      m_mouseState = 0;
      Dimension ns = new Dimension(m_viewSize.width / 2, m_viewSize.height / 2);
      
      if (width < 10) {
        width = 10;
      }
      if (height < 10) {
        height = 10;
      }
      
      Dimension d = getSize();
      Dimension np = new Dimension((int)(width / 2 - (width / 2.0D - m_viewPos.width) / 2.0D), (int)(height / 2 - (height / 2.0D - m_viewPos.height) / 2.0D));
      





      animateScaling(np, ns, 10);




    }
    else if (m_mouseState == 3)
    {
      m_mouseState = 0;
      Graphics g = getGraphics();
      if (m_ZoomBoxColor == null) {
        g.setColor(Color.black);
      } else
        g.setColor(m_ZoomBoxColor);
      if (m_ZoomBoxXORColor == null) {
        g.setXORMode(Color.white);
      } else
        g.setXORMode(m_ZoomBoxXORColor);
      g.drawRect(m_oldMousePos.width, m_oldMousePos.height, m_newMousePos.width - m_oldMousePos.width, m_newMousePos.height - m_oldMousePos.height);
      

      g.dispose();
      


      int cw = m_newMousePos.width - m_oldMousePos.width;
      int ch = m_newMousePos.height - m_oldMousePos.height;
      if ((cw >= 1) && (ch >= 1) && 
        (mouseInBounds(e)) && (getSizewidth / cw <= 6) && (getSizeheight / ch <= 6))
      {



        Dimension ns = new Dimension();
        Dimension np = new Dimension();
        double nvsw = getSizewidth / cw;
        double nvsh = getSizeheight / ch;
        width = ((int)((m_oldMousePos.width - m_viewPos.width) * -nvsw));
        height = ((int)((m_oldMousePos.height - m_viewPos.height) * -nvsh));
        width = ((int)(m_viewSize.width * nvsw));
        height = ((int)(m_viewSize.height * nvsh));
        
        animateScaling(np, ns, 10);

      }
      

    }
    else if ((m_mouseState == 0) && (m_scaling == 0))
    {
      m_mouseState = 0;
      setFont(new Font("A Name", 0, 12));
      
      int s = -1;
      
      for (int noa = 0; noa < m_numNodes; noa++) {
        if (m_nodes[noa].m_quad == 18)
        {
          calcScreenCoords(noa);
          if ((e.getX() <= m_nodes[noa].m_center + m_nodes[noa].m_side) && (e.getX() >= m_nodes[noa].m_center - m_nodes[noa].m_side) && (e.getY() >= m_nodes[noa].m_top) && (e.getY() <= m_nodes[noa].m_top + m_nodes[noa].m_height))
          {




            s = noa;
          }
          m_nodes[noa].m_top = 32000;
        }
      }
      if (s == -1)
      {
        m_winMenu.show(this, e.getX(), e.getY());
      }
      else
      {
        m_focusNode = s;
        m_nodeMenu.show(this, e.getX(), e.getY());
      }
      
      setFont(m_currentFont);
    }
    else if (m_mouseState == 1)
    {
      m_mouseState = 0;
      m_frameLimiter.stop();
      repaint();
    }
  }
  









  private boolean mouseInBounds(MouseEvent e)
  {
    if ((e.getX() < 0) || (e.getY() < 0) || (e.getX() > getSizewidth) || (e.getY() > getSizeheight))
    {
      return false;
    }
    return true;
  }
  






  public void mouseDragged(MouseEvent e)
  {
    if (m_mouseState == 1)
    {
      m_oldMousePos.width = m_newMousePos.width;
      m_oldMousePos.height = m_newMousePos.height;
      m_newMousePos.width = e.getX();
      m_newMousePos.height = e.getY();
      m_viewPos.width += m_newMousePos.width - m_oldMousePos.width;
      m_viewPos.height += m_newMousePos.height - m_oldMousePos.height;


    }
    else if (m_mouseState == 3)
    {

      Graphics g = getGraphics();
      if (m_ZoomBoxColor == null) {
        g.setColor(Color.black);
      } else
        g.setColor(m_ZoomBoxColor);
      if (m_ZoomBoxXORColor == null) {
        g.setXORMode(Color.white);
      } else
        g.setXORMode(m_ZoomBoxXORColor);
      g.drawRect(m_oldMousePos.width, m_oldMousePos.height, m_newMousePos.width - m_oldMousePos.width, m_newMousePos.height - m_oldMousePos.height);
      


      m_newMousePos.width = e.getX();
      m_newMousePos.height = e.getY();
      
      g.drawRect(m_oldMousePos.width, m_oldMousePos.height, m_newMousePos.width - m_oldMousePos.width, m_newMousePos.height - m_oldMousePos.height);
      

      g.dispose();
    }
  }
  







  public void mouseMoved(MouseEvent e) {}
  






  public void mouseEntered(MouseEvent e) {}
  






  public void mouseExited(MouseEvent e) {}
  






  public void setHighlight(String id)
  {
    for (int noa = 0; noa < m_numNodes; noa++) {
      if (id.equals(m_nodes[noa].m_node.getRefer()))
      {
        m_highlightNode = noa;
      }
    }
    

    repaint();
  }
  





  public void paintComponent(Graphics g)
  {
    Color oldBackground = ((Graphics2D)g).getBackground();
    if (m_BackgroundColor != null)
      ((Graphics2D)g).setBackground(m_BackgroundColor);
    g.clearRect(0, 0, getSizewidth, getSizeheight);
    ((Graphics2D)g).setBackground(oldBackground);
    g.setClip(3, 7, getWidth() - 6, getHeight() - 10);
    painter(g);
    g.setClip(0, 0, getWidth(), getHeight());
  }
  




























  private void painter(Graphics g)
  {
    double left_clip = (-m_viewPos.width - 50) / m_viewSize.width;
    double right_clip = (getSizewidth - m_viewPos.width + 50) / m_viewSize.width;
    
    double top_clip = (-m_viewPos.height - 50) / m_viewSize.height;
    double bottom_clip = (getSizeheight - m_viewPos.height + 50) / m_viewSize.height;
    














    int row = 0;int col = 0;
    for (int noa = 0; noa < m_numNodes; noa++) {
      Node r = m_nodes[noa].m_node;
      if (m_nodes[noa].m_change)
      {
        double ntop = r.getTop();
        if (ntop < top_clip) {
          row = 8;
        }
        else if (ntop > bottom_clip) {
          row = 32;
        }
        else {
          row = 16;
        }
      }
      

      double ncent = r.getCenter();
      if (ncent < left_clip) {
        col = 4;
      }
      else if (ncent > right_clip) {
        col = 1;
      }
      else {
        col = 2;
      }
      
      m_nodes[noa].m_quad = (row | col);
      
      if (m_nodes[noa].m_parent >= 0)
      {



        int pq = m_nodes[m_edges[m_nodes[noa].m_parent].m_parent].m_quad;
        int cq = m_nodes[noa].m_quad;
        

        if ((cq & 0x8) != 8)
        {

          if ((pq & 0x20) != 32)
          {

            if (((cq & 0x4) != 4) || ((pq & 0x4) != 4))
            {

              if (((cq & 0x1) != 1) || ((pq & 0x1) != 1))
              {



                drawLine(m_nodes[noa].m_parent, g);
              }
            }
          }
        }
      }
    }
    for (int noa = 0; noa < m_numNodes; noa++) {
      if (m_nodes[noa].m_quad == 18)
      {
        drawNode(noa, g);
      }
    }
    
    if ((m_highlightNode >= 0) && (m_highlightNode < m_numNodes))
    {
      if (m_nodes[m_highlightNode].m_quad == 18) { Color acol;
        Color acol;
        if (m_NodeColor == null) {
          acol = m_nodes[m_highlightNode].m_node.getColor();
        } else
          acol = m_NodeColor;
        g.setColor(new Color((acol.getRed() + 125) % 256, (acol.getGreen() + 125) % 256, (acol.getBlue() + 125) % 256));
        


        if (m_nodes[m_highlightNode].m_node.getShape() == 1) {
          g.drawRect(m_nodes[m_highlightNode].m_center - m_nodes[m_highlightNode].m_side, m_nodes[m_highlightNode].m_top, m_nodes[m_highlightNode].m_width, m_nodes[m_highlightNode].m_height);
          




          g.drawRect(m_nodes[m_highlightNode].m_center - m_nodes[m_highlightNode].m_side + 1, m_nodes[m_highlightNode].m_top + 1, m_nodes[m_highlightNode].m_width - 2, m_nodes[m_highlightNode].m_height - 2);




        }
        else if (m_nodes[m_highlightNode].m_node.getShape() == 2) {
          g.drawOval(m_nodes[m_highlightNode].m_center - m_nodes[m_highlightNode].m_side, m_nodes[m_highlightNode].m_top, m_nodes[m_highlightNode].m_width, m_nodes[m_highlightNode].m_height);
          




          g.drawOval(m_nodes[m_highlightNode].m_center - m_nodes[m_highlightNode].m_side + 1, m_nodes[m_highlightNode].m_top + 1, m_nodes[m_highlightNode].m_width - 2, m_nodes[m_highlightNode].m_height - 2);
        }
      }
    }
    




    for (int noa = 0; noa < m_numNodes; noa++)
    {





      m_nodes[noa].m_top = 32000;
    }
  }
  







  private void drawNode(int n, Graphics g)
  {
    if (m_NodeColor == null) {
      g.setColor(m_nodes[n].m_node.getColor());
    } else
      g.setColor(m_NodeColor);
    g.setPaintMode();
    calcScreenCoords(n);
    int x = m_nodes[n].m_center - m_nodes[n].m_side;
    int y = m_nodes[n].m_top;
    if (m_nodes[n].m_node.getShape() == 1) {
      g.fill3DRect(x, y, m_nodes[n].m_width, m_nodes[n].m_height, true);
      drawText(x, y, n, false, g);

    }
    else if (m_nodes[n].m_node.getShape() == 2)
    {
      g.fillOval(x, y, m_nodes[n].m_width, m_nodes[n].m_height);
      drawText(x, y + (int)(m_nodes[n].m_height * 0.15D), n, false, g);
    }
  }
  













  private void drawLine(int e, Graphics g)
  {
    int p = m_edges[e].m_parent;
    int c = m_edges[e].m_child;
    calcScreenCoords(c);
    calcScreenCoords(p);
    
    if (m_LineColor == null) {
      g.setColor(Color.black);
    } else
      g.setColor(m_LineColor);
    g.setPaintMode();
    
    if (m_currentFont.getSize() < 2)
    {
      g.drawLine(m_nodes[p].m_center, m_nodes[p].m_top + m_nodes[p].m_height, m_nodes[c].m_center, m_nodes[c].m_top);

    }
    else
    {

      int e_width = m_nodes[c].m_center - m_nodes[p].m_center;
      int e_height = m_nodes[c].m_top - (m_nodes[p].m_top + m_nodes[p].m_height);
      
      int e_width2 = e_width / 2;
      int e_height2 = e_height / 2;
      int e_centerx = m_nodes[p].m_center + e_width2;
      int e_centery = m_nodes[p].m_top + m_nodes[p].m_height + e_height2;
      int e_offset = m_edges[e].m_tb;
      
      int tmp = (int)(e_width / e_height * (e_height2 - e_offset)) + m_nodes[p].m_center;
      




      drawText(e_centerx - m_edges[e].m_side, e_centery - e_offset, e, true, g);
      


      if ((tmp > e_centerx - m_edges[e].m_side) && (tmp < e_centerx + m_edges[e].m_side))
      {

        g.drawLine(m_nodes[p].m_center, m_nodes[p].m_top + m_nodes[p].m_height, tmp, e_centery - e_offset);
        
        g.drawLine(e_centerx * 2 - tmp, e_centery + e_offset, m_nodes[c].m_center, m_nodes[c].m_top);
      }
      else
      {
        e_offset = m_edges[e].m_side;
        if (e_width < 0) {
          e_offset *= -1;
        }
        
        tmp = (int)(e_height / e_width * (e_width2 - e_offset)) + m_nodes[p].m_top + m_nodes[p].m_height;
        

        g.drawLine(m_nodes[p].m_center, m_nodes[p].m_top + m_nodes[p].m_height, e_centerx - e_offset, tmp);
        
        g.drawLine(e_centerx + e_offset, e_centery * 2 - tmp, m_nodes[c].m_center, m_nodes[c].m_top);
      }
    }
  }
  
















  private void drawText(int x1, int y1, int s, boolean e_or_n, Graphics g)
  {
    Color oldColor = g.getColor();
    
    g.setPaintMode();
    if (m_FontColor == null) {
      g.setColor(Color.black);
    } else {
      g.setColor(m_FontColor);
    }
    if (e_or_n)
    {
      Edge e = m_edges[s].m_edge;
      String st; for (int noa = 0; (st = e.getLine(noa)) != null; noa++) {
        g.drawString(st, (m_edges[s].m_width - m_fontSize.stringWidth(st)) / 2 + x1, y1 + (noa + 1) * m_fontSize.getHeight());
      }
      

    }
    else
    {
      Node e = m_nodes[s].m_node;
      String st; for (int noa = 0; (st = e.getLine(noa)) != null; noa++) {
        g.drawString(st, (m_nodes[s].m_width - m_fontSize.stringWidth(st)) / 2 + x1, y1 + (noa + 1) * m_fontSize.getHeight());
      }
    }
    



    g.setColor(oldColor);
  }
  








  private void calcScreenCoords(int n)
  {
    if (m_nodes[n].m_top == 32000) {
      m_nodes[n].m_top = ((int)(m_nodes[n].m_node.getTop() * m_viewSize.height) + m_viewPos.height);
      
      m_nodes[n].m_center = ((int)(m_nodes[n].m_node.getCenter() * m_viewSize.width) + m_viewPos.width);
    }
  }
  











  private void autoScale()
  {
    Dimension temp = new Dimension(10, 10);
    
    if (m_numNodes <= 1) {
      return;
    }
    

    int dist = (m_nodes[0].m_height + 40) * m_numLevels;
    if (dist > height) {
      height = dist;
    }
    
    for (int noa = 0; noa < m_numNodes - 1; noa++) {
      calcScreenCoords(noa);
      calcScreenCoords(noa + 1);
      if (!m_nodes[(noa + 1)].m_change)
      {



        dist = m_nodes[(noa + 1)].m_center - m_nodes[noa].m_center;
        
        if (dist <= 0) {
          dist = 1;
        }
        dist = (6 + m_nodes[noa].m_side + m_nodes[(noa + 1)].m_side) * m_viewSize.width / dist;
        

        if (dist > width)
        {
          width = dist;
        }
      }
      

      dist = (m_nodes[(noa + 1)].m_height + 40) * m_numLevels;
      if (dist > height)
      {
        height = dist;
      }
    }
    


    int y1 = m_nodes[m_edges[0].m_parent].m_top;
    int y2 = m_nodes[m_edges[0].m_child].m_top;
    
    dist = y2 - y1;
    if (dist <= 0) {
      dist = 1;
    }
    dist = (60 + m_edges[0].m_height + m_nodes[m_edges[0].m_parent].m_height) * m_viewSize.height / dist;
    
    if (dist > height)
    {
      height = dist;
    }
    
    for (int noa = 0; noa < m_numNodes - 2; noa++)
    {
      if (!m_nodes[m_edges[(noa + 1)].m_child].m_change)
      {




        int xa = m_nodes[m_edges[noa].m_child].m_center - m_nodes[m_edges[noa].m_parent].m_center;
        
        xa /= 2;
        xa += m_nodes[m_edges[noa].m_parent].m_center;
        
        int xb = m_nodes[m_edges[(noa + 1)].m_child].m_center - m_nodes[m_edges[(noa + 1)].m_parent].m_center;
        
        xb /= 2;
        xb += m_nodes[m_edges[(noa + 1)].m_parent].m_center;
        
        dist = xb - xa;
        if (dist <= 0) {
          dist = 1;
        }
        dist = (12 + m_edges[noa].m_side + m_edges[(noa + 1)].m_side) * m_viewSize.width / dist;
        

        if (dist > width)
        {
          width = dist;
        }
      }
      
      y1 = m_nodes[m_edges[(noa + 1)].m_parent].m_top;
      y2 = m_nodes[m_edges[(noa + 1)].m_child].m_top;
      
      dist = y2 - y1;
      if (dist <= 0)
      {
        dist = 1;
      }
      dist = (60 + m_edges[(noa + 1)].m_height + m_nodes[m_edges[(noa + 1)].m_parent].m_height) * m_viewSize.height / dist;
      


      if (dist > height)
      {
        height = dist;
      }
    }
    
    Dimension e = getSize();
    
    Dimension np = new Dimension();
    width = ((int)(width / 2 - (width / 2.0D - m_viewPos.width) / m_viewSize.width * width));
    
    height = ((int)(height / 2 - (height / 2.0D - m_viewPos.height) / m_viewSize.height * height));
    



    for (int noa = 0; noa < m_numNodes; noa++)
    {





      m_nodes[noa].m_top = 32000;
    }
    
    animateScaling(np, temp, 10);
  }
  















  private void animateScaling(Dimension n_pos, Dimension n_size, int frames)
  {
    if (frames == 0) {
      Messages.getInstance();System.out.println(Messages.getString("TreeVisualizer_AnimateScaling_Text"));
      m_scaling = 0;
    }
    else {
      if (m_scaling == 0)
      {

        m_frameLimiter.start();
        m_nViewPos.width = width;
        m_nViewPos.height = height;
        m_nViewSize.width = width;
        m_nViewSize.height = height;
        
        m_scaling = frames;
      }
      
      int s_w = (width - m_viewSize.width) / frames;
      int s_h = (height - m_viewSize.height) / frames;
      int p_w = (width - m_viewPos.width) / frames;
      int p_h = (height - m_viewPos.height) / frames;
      
      m_viewSize.width += s_w;
      m_viewSize.height += s_h;
      
      m_viewPos.width += p_w;
      m_viewPos.height += p_h;
      
      repaint();
      
      m_scaling -= 1;
      if (m_scaling == 0)
      {
        m_frameLimiter.stop();
      }
    }
  }
  









  private void changeFontSize(int s)
  {
    setFont(this.m_currentFont = new Font("A Name", 0, s));
    
    m_fontSize = getFontMetrics(getFont());
    


    for (int noa = 0; noa < m_numNodes; noa++)
    {

      Dimension d = m_nodes[noa].m_node.stringSize(m_fontSize);
      
      if (m_nodes[noa].m_node.getShape() == 1) {
        m_nodes[noa].m_height = (height + 10);
        m_nodes[noa].m_width = (width + 8);
        m_nodes[noa].m_side = (m_nodes[noa].m_width / 2);
      }
      else if (m_nodes[noa].m_node.getShape() == 2) {
        m_nodes[noa].m_height = ((int)((height + 2) * 1.6D));
        m_nodes[noa].m_width = ((int)((width + 2) * 1.6D));
        m_nodes[noa].m_side = (m_nodes[noa].m_width / 2);
      }
      
      if (noa < m_numNodes - 1)
      {

        d = m_edges[noa].m_edge.stringSize(m_fontSize);
        
        m_edges[noa].m_height = (height + 8);
        m_edges[noa].m_width = (width + 8);
        m_edges[noa].m_side = (m_edges[noa].m_width / 2);
        m_edges[noa].m_tb = (m_edges[noa].m_height / 2);
      }
    }
  }
  
















  private void arrayFill(Node t, NodeInfo[] l, EdgeInfo[] k)
  {
    if ((t == null) || (l == null)) {
      System.exit(1);
    }
    



    l[0] = new NodeInfo(null);
    0m_node = t;
    0m_parent = -1;
    0m_change = true;
    







    int free_space = 1;
    
    double height = t.getTop();
    



    for (int floater = 0; floater < free_space; floater++) {
      Node r = m_node;
      Edge e; for (int noa = 0; (e = r.getChild(noa)) != null; noa++)
      {


        Node s = e.getTarget();
        l[free_space] = new NodeInfo(null);
        m_node = s;
        m_parent = (free_space - 1);
        
        k[(free_space - 1)] = new EdgeInfo(null);
        1m_edge = e;
        1m_parent = floater;
        1m_child = free_space;
        




        if (height != s.getTop()) {
          m_change = true;
          height = s.getTop();
        }
        else {
          m_change = false;
        }
        free_space++;
      }
    } }
  
  private class EdgeInfo { int m_parent;
    int m_child;
    int m_side;
    int m_tb;
    int m_width;
    int m_height;
    Edge m_edge;
    
    private EdgeInfo() {} }
  
  private class NodeInfo { int m_top = 32000;
    






    int m_center;
    






    int m_side;
    






    int m_width;
    






    int m_height;
    






    boolean m_change;
    





    int m_parent;
    





    int m_quad;
    





    Node m_node;
    






    private NodeInfo() {}
  }
  






  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("TreeVisualizer_Main_Logger_Text"));
      

      TreeBuild builder = new TreeBuild();
      Node top = null;
      NodePlace arrange = new PlaceNode2();
      
      top = builder.create(new FileReader(args[0]));
      
      int num = Node.getCount(top, 0);
      

      TreeVisualizer a = new TreeVisualizer(null, top, arrange);
      a.setSize(800, 600);
      

      JFrame f = new JFrame();
      


      Container contentPane = f.getContentPane();
      contentPane.add(a);
      f.setDefaultCloseOperation(2);
      f.setSize(800, 600);
      f.setVisible(true);
    }
    catch (IOException e) {}
  }
}

package weka.gui.graphvisualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import weka.core.FastVector;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.ExtensionFileFilter;
import weka.gui.visualize.PrintablePanel;
















































public class GraphVisualizer
  extends JPanel
  implements GraphConstants, LayoutCompleteEventListener
{
  private static final long serialVersionUID = -2038911085935515624L;
  protected FastVector m_nodes = new FastVector();
  
  protected FastVector m_edges = new FastVector();
  


  protected LayoutEngine m_le;
  


  protected GraphPanel m_gp;
  

  protected String graphID;
  

  protected JButton m_jBtSave;
  

  private final String ICONPATH = "weka/gui/graphvisualizer/icons/";
  
  private FontMetrics fm = getFontMetrics(getFont());
  private double scale = 1.0D;
  private int nodeHeight = 2 * fm.getHeight(); private int nodeWidth = 24;
  private int paddedNodeWidth = 32;
  
  private final JTextField jTfNodeWidth = new JTextField(3);
  
  private final JTextField jTfNodeHeight = new JTextField(3);
  

  private final JButton jBtLayout;
  

  private int maxStringWidth = 0;
  
  private int[] zoomPercents = { 10, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 350, 400, 450, 500, 550, 600, 650, 700, 800, 900, 999 };
  


  JScrollPane m_js;
  



  public GraphVisualizer()
  {
    m_gp = new GraphPanel();
    m_js = new JScrollPane(m_gp);
    


    m_le = new HierarchicalBCEngine(m_nodes, m_edges, paddedNodeWidth, nodeHeight);
    
    m_le.addLayoutCompleteEventListener(this);
    
    m_jBtSave = new JButton();
    URL tempURL = ClassLoader.getSystemResource("weka/gui/graphvisualizer/icons/save.gif");
    if (tempURL != null) {
      m_jBtSave.setIcon(new ImageIcon(tempURL));
    } else {
      Messages.getInstance();System.err.println("weka/gui/graphvisualizer/icons/" + Messages.getString("GraphVisualizer_Error_Text_First")); }
    Messages.getInstance();m_jBtSave.setToolTipText(Messages.getString("GraphVisualizer_JBtSave_SetToolTipText_Text"));
    m_jBtSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        Messages.getInstance();ExtensionFileFilter ef1 = new ExtensionFileFilter(".dot", Messages.getString("GraphVisualizer_EF1_ExtensionFileFilter_Text"));
        Messages.getInstance();ExtensionFileFilter ef2 = new ExtensionFileFilter(".xml", Messages.getString("GraphVisualizer_EF2_ExtensionFileFilter_Text"));
        
        fc.addChoosableFileFilter(ef1);
        fc.addChoosableFileFilter(ef2);
        Messages.getInstance();fc.setDialogTitle(Messages.getString("GraphVisualizer_FC_SetDialogTitle_Text"));
        int rval = fc.showSaveDialog(GraphVisualizer.this);
        
        if (rval == 0)
        {

          if (fc.getFileFilter() == ef2) {
            String filename = fc.getSelectedFile().toString();
            if (!filename.endsWith(".xml"))
              filename = filename.concat(".xml");
            BIFParser.writeXMLBIF03(filename, graphID, m_nodes, m_edges);
          }
          else {
            String filename = fc.getSelectedFile().toString();
            if (!filename.endsWith(".dot"))
              filename = filename.concat(".dot");
            DotParser.writeDOT(filename, graphID, m_nodes, m_edges);
          }
          
        }
      }
    });
    final JButton jBtZoomIn = new JButton();
    tempURL = ClassLoader.getSystemResource("weka/gui/graphvisualizer/icons/zoomin.gif");
    if (tempURL != null) {
      jBtZoomIn.setIcon(new ImageIcon(tempURL));
    } else {
      Messages.getInstance();System.err.println("weka/gui/graphvisualizer/icons/" + Messages.getString("GraphVisualizer_Error_Text_Second"));
    }
    Messages.getInstance();jBtZoomIn.setToolTipText(Messages.getString("GraphVisualizer_JBtZoomIn_SetToolTipText_Text"));
    
    final JButton jBtZoomOut = new JButton();
    tempURL = ClassLoader.getSystemResource("weka/gui/graphvisualizer/icons/zoomout.gif");
    if (tempURL != null) {
      jBtZoomOut.setIcon(new ImageIcon(tempURL));
    } else {
      Messages.getInstance();System.err.println("weka/gui/graphvisualizer/icons/" + Messages.getString("GraphVisualizer_Error_Text_Third"));
    }
    Messages.getInstance();jBtZoomOut.setToolTipText(Messages.getString("GraphVisualizer_JBtZoomOut_SetToolTipText_Text"));
    
    final JTextField jTfZoom = new JTextField("100%");
    jTfZoom.setMinimumSize(jTfZoom.getPreferredSize());
    jTfZoom.setHorizontalAlignment(0);
    Messages.getInstance();jTfZoom.setToolTipText(Messages.getString("GraphVisualizer_JBtZoom_SetToolTipText_Text"));
    
    jTfZoom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        JTextField jt = (JTextField)ae.getSource();
        try {
          int i = -1;
          i = jt.getText().indexOf('%');
          if (i == -1) {
            i = Integer.parseInt(jt.getText());
          } else {
            i = Integer.parseInt(jt.getText().substring(0, i));
          }
          if (i <= 999) {
            scale = (i / 100.0D);
          }
          jt.setText((int)(scale * 100.0D) + "%");
          
          if (scale > 0.1D) {
            if (!jBtZoomOut.isEnabled()) {
              jBtZoomOut.setEnabled(true);
            }
          } else
            jBtZoomOut.setEnabled(false);
          if (scale < 9.99D) {
            if (!jBtZoomIn.isEnabled()) {
              jBtZoomIn.setEnabled(true);
            }
          } else {
            jBtZoomIn.setEnabled(false);
          }
          setAppropriateSize();
          
          m_gp.repaint();
          m_gp.invalidate();
          m_js.revalidate();
        } catch (NumberFormatException ne) {
          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(getParent(), Messages.getString("GraphVisualizer_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("GraphVisualizer_JOptionPaneShowMessageDialog_Text_Second"), 0);
          


          jt.setText(scale * 100.0D + "%");
        }
        
      }
      
    });
    jBtZoomIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        int i = 0;int s = (int)(scale * 100.0D);
        if (s < 300) {
          i = s / 25;
        } else if (s < 700) {
          i = 6 + s / 50;
        } else {
          i = 13 + s / 100;
        }
        if (s >= 999) {
          JButton b = (JButton)ae.getSource();
          b.setEnabled(false);
          return;
        }
        if (s >= 10) {
          if (i >= 22) {
            JButton b = (JButton)ae.getSource();
            b.setEnabled(false);
          }
          if ((s == 10) && (!jBtZoomOut.isEnabled())) {
            jBtZoomOut.setEnabled(true);
          }
          jTfZoom.setText(zoomPercents[(i + 1)] + "%");
          scale = (zoomPercents[(i + 1)] / 100.0D);
        }
        else {
          if (!jBtZoomOut.isEnabled()) {
            jBtZoomOut.setEnabled(true);
          }
          jTfZoom.setText(zoomPercents[0] + "%");
          scale = (zoomPercents[0] / 100.0D);
        }
        setAppropriateSize();
        m_gp.repaint();
        m_gp.invalidate();
        m_js.revalidate();
      }
      

    });
    jBtZoomOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        int i = 0;int s = (int)(scale * 100.0D);
        if (s < 300) {
          i = (int)Math.ceil(s / 25.0D);
        } else if (s < 700) {
          i = 6 + (int)Math.ceil(s / 50.0D);
        } else {
          i = 13 + (int)Math.ceil(s / 100.0D);
        }
        if (s <= 10) {
          JButton b = (JButton)ae.getSource();
          b.setEnabled(false);
        }
        else if (s < 999) {
          if (i <= 1) {
            JButton b = (JButton)ae.getSource();
            b.setEnabled(false);
          }
          
          jTfZoom.setText(zoomPercents[(i - 1)] + "%");
          scale = (zoomPercents[(i - 1)] / 100.0D);
        }
        else {
          if (!jBtZoomIn.isEnabled()) {
            jBtZoomIn.setEnabled(true);
          }
          jTfZoom.setText(zoomPercents[22] + "%");
          scale = (zoomPercents[22] / 100.0D);
        }
        setAppropriateSize();
        m_gp.repaint();
        m_gp.invalidate();
        m_js.revalidate();

      }
      

    });
    JButton jBtExtraControls = new JButton();
    tempURL = ClassLoader.getSystemResource("weka/gui/graphvisualizer/icons/extra.gif");
    if (tempURL != null) {
      jBtExtraControls.setIcon(new ImageIcon(tempURL));
    } else {
      Messages.getInstance();System.err.println("weka/gui/graphvisualizer/icons/" + Messages.getString("GraphVisualizer_Error_Text_Fourth"));
    }
    Messages.getInstance();jBtExtraControls.setToolTipText(Messages.getString("GraphVisualizer_JBtExtraControls_SetToolTipText_Text"));
    

    Messages.getInstance();final JCheckBox jCbCustomNodeSize = new JCheckBox(Messages.getString("GraphVisualizer_JCbCustomNodeSize_JCheckBox_Text"));
    Messages.getInstance();final JLabel jLbNodeWidth = new JLabel(Messages.getString("GraphVisualizer_JLbNodeWidth_JLabel_Text"));
    Messages.getInstance();final JLabel jLbNodeHeight = new JLabel(Messages.getString("GraphVisualizer_JLbNodeHeight_JLabel_Text"));
    
    jTfNodeWidth.setHorizontalAlignment(0);
    jTfNodeWidth.setText("" + nodeWidth);
    jTfNodeHeight.setHorizontalAlignment(0);
    jTfNodeHeight.setText("" + nodeHeight);
    jLbNodeWidth.setEnabled(false);
    jTfNodeWidth.setEnabled(false);
    jLbNodeHeight.setEnabled(false);
    jTfNodeHeight.setEnabled(false);
    
    jCbCustomNodeSize.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (((JCheckBox)ae.getSource()).isSelected()) {
          jLbNodeWidth.setEnabled(true);
          jTfNodeWidth.setEnabled(true);
          jLbNodeHeight.setEnabled(true);
          jTfNodeHeight.setEnabled(true);
        }
        else {
          jLbNodeWidth.setEnabled(false);
          jTfNodeWidth.setEnabled(false);
          jLbNodeHeight.setEnabled(false);
          jTfNodeHeight.setEnabled(false);
          setAppropriateNodeSize();
        }
        
      }
      
    });
    Messages.getInstance();jBtLayout = new JButton(Messages.getString("GraphVisualizer_JBtLayout_JButton_Text"));
    jBtLayout.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        if (jCbCustomNodeSize.isSelected()) { int tmpW;
          try { tmpW = Integer.parseInt(jTfNodeWidth.getText());
          } catch (NumberFormatException ne) {
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(getParent(), Messages.getString("GraphVisualizer_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("GraphVisualizer_JOptionPaneShowMessageDialog_Text_Second"), 0);
            


            tmpW = nodeWidth;
            jTfNodeWidth.setText("" + nodeWidth);
          }
          int tmpH;
          try { tmpH = Integer.parseInt(jTfNodeHeight.getText());
          } catch (NumberFormatException ne) {
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(getParent(), Messages.getString("GraphVisualizer_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("GraphVisualizer_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
            


            tmpH = nodeHeight;
            jTfNodeWidth.setText("" + nodeHeight);
          }
          
          if ((tmpW != nodeWidth) || (tmpH != nodeHeight)) {
            nodeWidth = tmpW;paddedNodeWidth = (nodeWidth + 8);nodeHeight = tmpH;
          }
        }
        JButton bt = (JButton)ae.getSource();
        bt.setEnabled(false);
        m_le.setNodeSize(paddedNodeWidth, nodeHeight);
        m_le.layoutGraph();
      }
      

    });
    GridBagConstraints gbc = new GridBagConstraints();
    
    final JPanel p = new JPanel(new GridBagLayout());
    gridwidth = 0;
    anchor = 18;
    fill = 0;
    p.add(m_le.getControlPanel(), gbc);
    gridwidth = 1;
    insets = new Insets(8, 0, 0, 0);
    anchor = 18;
    gridwidth = 0;
    
    p.add(jCbCustomNodeSize, gbc);
    insets = new Insets(0, 0, 0, 0);
    gridwidth = 0;
    Container c = new Container();
    c.setLayout(new GridBagLayout());
    gridwidth = -1;
    c.add(jLbNodeWidth, gbc);
    gridwidth = 0;
    c.add(jTfNodeWidth, gbc);
    gridwidth = -1;
    c.add(jLbNodeHeight, gbc);
    gridwidth = 0;
    c.add(jTfNodeHeight, gbc);
    fill = 2;
    p.add(c, gbc);
    
    anchor = 18;
    insets = new Insets(8, 0, 0, 0);
    fill = 2;
    p.add(jBtLayout, gbc);
    fill = 0;
    Messages.getInstance();p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("GraphVisualizer_P_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    


    p.setPreferredSize(new Dimension(0, 0));
    
    final JToolBar jTbTools = new JToolBar();
    jTbTools.setFloatable(false);
    jTbTools.setLayout(new GridBagLayout());
    anchor = 18;
    gridwidth = 0;
    insets = new Insets(0, 0, 0, 0);
    jTbTools.add(p, gbc);
    gridwidth = 1;
    jTbTools.add(m_jBtSave, gbc);
    jTbTools.addSeparator(new Dimension(2, 2));
    jTbTools.add(jBtZoomIn, gbc);
    
    fill = 3;
    weighty = 1.0D;
    JPanel p2 = new JPanel(new BorderLayout());
    p2.setPreferredSize(jTfZoom.getPreferredSize());
    p2.setMinimumSize(jTfZoom.getPreferredSize());
    p2.add(jTfZoom, "Center");
    jTbTools.add(p2, gbc);
    weighty = 0.0D;
    fill = 0;
    
    jTbTools.add(jBtZoomOut, gbc);
    jTbTools.addSeparator(new Dimension(2, 2));
    jTbTools.add(jBtExtraControls, gbc);
    jTbTools.addSeparator(new Dimension(4, 2));
    weightx = 1.0D;
    fill = 1;
    jTbTools.add(m_le.getProgressBar(), gbc);
    
    jBtExtraControls.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Dimension d = p.getPreferredSize();
        if ((width == 0) || (height == 0)) {
          LayoutManager lm = p.getLayout();
          Dimension d2 = lm.preferredLayoutSize(p);
          p.setPreferredSize(d2);jTbTools.revalidate();

















        }
        else
        {
















          p.setPreferredSize(new Dimension(0, 0));
          jTbTools.revalidate();









        }
        









      }
      









    });
    setLayout(new BorderLayout());
    add(jTbTools, "North");
    add(m_js, "Center");
  }
  







  protected void setAppropriateNodeSize()
  {
    if (maxStringWidth == 0)
      for (int i = 0; i < m_nodes.size(); i++) {
        int strWidth = fm.stringWidth(m_nodes.elementAt(i)).lbl);
        if (strWidth > maxStringWidth)
          maxStringWidth = strWidth;
      }
    nodeWidth = (maxStringWidth + 4);
    paddedNodeWidth = (nodeWidth + 8);
    jTfNodeWidth.setText("" + nodeWidth);
    
    nodeHeight = (2 * fm.getHeight());
    jTfNodeHeight.setText("" + nodeHeight);
  }
  



  protected void setAppropriateSize()
  {
    int maxX = 0;int maxY = 0;
    
    m_gp.setScale(scale, scale);
    
    for (int i = 0; i < m_nodes.size(); i++) {
      GraphNode n = (GraphNode)m_nodes.elementAt(i);
      if (maxX < x)
        maxX = x;
      if (maxY < y) {
        maxY = y;
      }
    }
    


    m_gp.setPreferredSize(new Dimension((int)((maxX + paddedNodeWidth + 2) * scale), (int)((maxY + nodeHeight + 2) * scale)));
  }
  








  public void layoutCompleted(LayoutCompleteEvent le)
  {
    setAppropriateSize();
    
    m_gp.invalidate();
    m_js.revalidate();
    m_gp.repaint();
    jBtLayout.setEnabled(true);
  }
  







  public void layoutGraph()
  {
    if (m_le != null) {
      m_le.layoutGraph();
    }
  }
  





  public void readBIF(String instring)
    throws BIFFormatException
  {
    BIFParser bp = new BIFParser(instring, m_nodes, m_edges);
    try {
      graphID = bp.parse();
    } catch (BIFFormatException bf) {
      Messages.getInstance();System.out.println(Messages.getString("GraphVisualizer_ReadBIF_Error_Text_First"));
      bf.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();return;
    }
    setAppropriateNodeSize();
    if (m_le != null) {
      m_le.setNodeSize(paddedNodeWidth, nodeHeight);
    }
  }
  





  public void readBIF(InputStream instream)
    throws BIFFormatException
  {
    BIFParser bp = new BIFParser(instream, m_nodes, m_edges);
    try {
      graphID = bp.parse();
    } catch (BIFFormatException bf) {
      Messages.getInstance();System.out.println(Messages.getString("GraphVisualizer_ReadBIF_Error_Text_Second"));
      bf.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();return;
    }
    setAppropriateNodeSize();
    if (m_le != null) {
      m_le.setNodeSize(paddedNodeWidth, nodeHeight);
    }
    setAppropriateSize();
  }
  







  public void readDOT(Reader input)
  {
    DotParser dp = new DotParser(input, m_nodes, m_edges);
    graphID = dp.parse();
    
    setAppropriateNodeSize();
    if (m_le != null) {
      m_le.setNodeSize(paddedNodeWidth, nodeHeight);
      jBtLayout.setEnabled(false);
      layoutGraph();
    }
  }
  


  private class GraphPanel
    extends PrintablePanel
  {
    private static final long serialVersionUID = -3562813603236753173L;
    


    public GraphPanel()
    {
      addMouseListener(new GraphVisualizer.GraphVisualizerMouseListener(GraphVisualizer.this, null));
      addMouseMotionListener(new GraphVisualizer.GraphVisualizerMouseMotionListener(GraphVisualizer.this, null));
      setToolTipText("");
    }
    


    public String getToolTipText(MouseEvent me)
    {
      Dimension d = m_gp.getPreferredSize();
      int ny;
      int nx;
      int y; int x = y = nx = ny = 0;
      
      if (width < m_gp.getWidth())
        nx = (int)((nx + m_gp.getWidth() / 2 - width / 2) / scale);
      if (height < m_gp.getHeight()) {
        ny = (int)((ny + m_gp.getHeight() / 2 - height / 2) / scale);
      }
      Rectangle r = new Rectangle(0, 0, (int)(paddedNodeWidth * scale), (int)(nodeHeight * scale));
      
      x += me.getX();y += me.getY();
      

      for (int i = 0; i < m_nodes.size(); i++) {
        GraphNode n = (GraphNode)m_nodes.elementAt(i);
        if (nodeType != 3)
          return null;
        x = ((int)((nx + x) * scale));y = ((int)((ny + y) * scale));
        if (r.contains(x, y)) {
          if (probs == null) {
            return lbl;
          }
          Messages.getInstance();return lbl + Messages.getString("GraphVisualizer_GetToolTipText_Text");
        }
      }
      return null;
    }
    
    public void paintComponent(Graphics gr)
    {
      Graphics2D g = (Graphics2D)gr;
      RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      g.setRenderingHints(rh);
      g.scale(scale, scale);
      Rectangle r = g.getClipBounds();
      g.clearRect(x, y, width, height);
      

      int x = 0;int y = 0;
      Dimension d = getPreferredSize();
      




      if (width < getWidth())
        x = (int)((x + getWidth() / 2 - width / 2) / scale);
      if (height < getHeight()) {
        y = (int)((y + getHeight() / 2 - height / 2) / scale);
      }
      for (int index = 0; index < m_nodes.size(); index++) {
        GraphNode n = (GraphNode)m_nodes.elementAt(index);
        if (nodeType == 3) {
          g.setColor(getBackground().darker().darker());
          g.fillOval(x + x + paddedNodeWidth - nodeWidth - (paddedNodeWidth - nodeWidth) / 2, y + y, nodeWidth, nodeHeight);
          



          g.setColor(Color.white);
          











          if (fm.stringWidth(lbl) <= nodeWidth) {
            g.drawString(lbl, x + x + paddedNodeWidth / 2 - fm.stringWidth(lbl) / 2, y + y + nodeHeight / 2 + fm.getHeight() / 2 - 2);


          }
          else if (fm.stringWidth(ID) <= nodeWidth) {
            g.drawString(ID, x + x + paddedNodeWidth / 2 - fm.stringWidth(ID) / 2, y + y + nodeHeight / 2 + fm.getHeight() / 2 - 2);


          }
          else if (fm.stringWidth(Integer.toString(index)) <= nodeWidth) {
            g.drawString(Integer.toString(index), x + x + paddedNodeWidth / 2 - fm.stringWidth(Integer.toString(index)) / 2, y + y + nodeHeight / 2 + fm.getHeight() / 2 - 2);
          }
          


          g.setColor(Color.black);



        }
        else
        {


          g.drawLine(x + x + paddedNodeWidth / 2, y + y, x + x + paddedNodeWidth / 2, y + y + nodeHeight);
        }
        








        if (edges != null) {
          for (int k = 0; k < edges.length; k++) {
            if (edges[k][1] > 0) {
              GraphNode n2 = (GraphNode)m_nodes.elementAt(edges[k][0]);
              
              int x1 = x + paddedNodeWidth / 2;int y1 = y + nodeHeight;
              int x2 = x + paddedNodeWidth / 2;int y2 = y;
              g.drawLine(x + x1, y + y1, x + x2, y + y2);
              if (edges[k][1] == 1) {
                if (nodeType == 3) {
                  drawArrow(g, x + x1, y + y1, x + x2, y + y2);
                }
              } else if (edges[k][1] == 2) {
                if (nodeType == 3) {
                  drawArrow(g, x + x2, y + y2, x + x1, y + y1);
                }
              } else if (edges[k][1] == 3) {
                if (nodeType == 3)
                  drawArrow(g, x + x2, y + y2, x + x1, y + y1);
                if (nodeType == 3) {
                  drawArrow(g, x + x1, y + y1, x + x2, y + y2);
                }
              }
            }
          }
        }
      }
    }
    






    protected void drawArrow(Graphics g, int x1, int y1, int x2, int y2)
    {
      if (x1 == x2) {
        if (y1 < y2) {
          g.drawLine(x2, y2, x2 + 4, y2 - 8);
          g.drawLine(x2, y2, x2 - 4, y2 - 8);
        }
        else {
          g.drawLine(x2, y2, x2 + 4, y2 + 8);
          g.drawLine(x2, y2, x2 - 4, y2 + 8);
        }
      }
      else
      {
        double hyp = 0.0D;double base = 0.0D;double perp = 0.0D;
        int x3 = 0;int y3 = 0;
        double theta;
        double theta; if (x2 < x1) {
          base = x1 - x2;hyp = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
          theta = Math.acos(base / hyp);
        }
        else {
          base = x1 - x2;hyp = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
          theta = Math.acos(base / hyp);
        }
        double beta = 0.5235987755982988D;
        


        hyp = 8.0D;
        base = Math.cos(theta - beta) * hyp;
        perp = Math.sin(theta - beta) * hyp;
        
        x3 = (int)(x2 + base);
        if (y1 < y2) {
          y3 = (int)(y2 - perp);
        } else {
          y3 = (int)(y2 + perp);
        }
        



        g.drawLine(x2, y2, x3, y3);
        
        base = Math.cos(theta + beta) * hyp;
        perp = Math.sin(theta + beta) * hyp;
        
        x3 = (int)(x2 + base);
        if (y1 < y2) {
          y3 = (int)(y2 - perp);
        } else {
          y3 = (int)(y2 + perp);
        }
        

        g.drawLine(x2, y2, x3, y3);
      }
    }
    



    public void highLight(GraphNode n)
    {
      Graphics2D g = (Graphics2D)getGraphics();
      RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      g.setRenderingHints(rh);
      g.setPaintMode();
      g.scale(scale, scale);
      int x = 0;int y = 0;
      Dimension d = getPreferredSize();
      




      if (width < getWidth())
        x = (int)((x + getWidth() / 2 - width / 2) / scale);
      if (height < getHeight()) {
        y = (int)((y + getHeight() / 2 - height / 2) / scale);
      }
      
      if (nodeType == 3)
      {
        g.setXORMode(Color.green);
        
        g.fillOval(x + x + paddedNodeWidth - nodeWidth - (paddedNodeWidth - nodeWidth) / 2, y + y, nodeWidth, nodeHeight);
        

        g.setXORMode(Color.red);
        




        if (fm.stringWidth(lbl) <= nodeWidth) {
          g.drawString(lbl, x + x + paddedNodeWidth / 2 - fm.stringWidth(lbl) / 2, y + y + nodeHeight / 2 + fm.getHeight() / 2 - 2);


        }
        else if (fm.stringWidth(ID) <= nodeWidth) {
          g.drawString(ID, x + x + paddedNodeWidth / 2 - fm.stringWidth(ID) / 2, y + y + nodeHeight / 2 + fm.getHeight() / 2 - 2);


        }
        else if (fm.stringWidth(Integer.toString(m_nodes.indexOf(n))) <= nodeWidth)
        {
          g.drawString(Integer.toString(m_nodes.indexOf(n)), x + x + paddedNodeWidth / 2 - fm.stringWidth(Integer.toString(m_nodes.indexOf(n))) / 2, y + y + nodeHeight / 2 + fm.getHeight() / 2 - 2);
        }
        


        g.setXORMode(Color.green);
        




        if (edges != null)
        {
          for (int k = 0; k < edges.length; k++) {
            if ((edges[k][1] == 1) || (edges[k][1] == 3)) {
              GraphNode n2 = (GraphNode)m_nodes.elementAt(edges[k][0]);
              
              int x1 = x + paddedNodeWidth / 2;int y1 = y + nodeHeight;
              int x2 = x + paddedNodeWidth / 2;int y2 = y;
              g.drawLine(x + x1, y + y1, x + x2, y + y2);
              if (edges[k][1] == 1) {
                if (nodeType == 3) {
                  drawArrow(g, x + x1, y + y1, x + x2, y + y2);
                }
              } else if (edges[k][1] == 3) {
                if (nodeType == 3)
                  drawArrow(g, x + x2, y + y2, x + x1, y + y1);
                if (nodeType == 3)
                  drawArrow(g, x + x1, y + y1, x + x2, y + y2);
              }
              if (nodeType == 3) {
                g.fillOval(x + x + paddedNodeWidth - nodeWidth - (paddedNodeWidth - nodeWidth) / 2, y + y, nodeWidth, nodeHeight);
              }
              






              Vector t = new Vector();
              while ((nodeType != 3) || (t.size() > 0))
              {
                if (t.size() > 0) {
                  n2 = (GraphNode)t.elementAt(0);
                  t.removeElementAt(0); }
                if (nodeType != 3) {
                  g.drawLine(x + x + paddedNodeWidth / 2, y + y, x + x + paddedNodeWidth / 2, y + y + nodeHeight);
                  
                  x1 = x + paddedNodeWidth / 2;y1 = y + nodeHeight;
                  
                  for (int m = 0; m < edges.length; m++)
                  {

                    if (edges[m][1] > 0) {
                      GraphNode n3 = (GraphNode)m_nodes.elementAt(edges[m][0]);
                      
                      g.drawLine(x + x1, y + y1, x + x + paddedNodeWidth / 2, y + y);
                      
                      if (nodeType == 3) {
                        g.fillOval(x + x + paddedNodeWidth - nodeWidth - (paddedNodeWidth - nodeWidth) / 2, y + y, nodeWidth, nodeHeight);
                        

                        drawArrow(g, x + x1, y + y1, x + x + paddedNodeWidth / 2, y + y);
                      }
                      

                      t.addElement(n3);
                    }
                    
                  }
                }
              }
            }
            else if ((edges[k][1] == -2) || (edges[k][1] == -3))
            {

              GraphNode n2 = (GraphNode)m_nodes.elementAt(edges[k][0]);
              
              int x1 = x + paddedNodeWidth / 2;int y1 = y;
              int x2 = x + paddedNodeWidth / 2;int y2 = y + nodeHeight;
              g.drawLine(x + x1, y + y1, x + x2, y + y2);
              
              if (edges[k][1] == -3) {
                drawArrow(g, x + x2, y + y2, x + x1, y + y1);
                if (nodeType != 1) {
                  drawArrow(g, x + x1, y + y1, x + x2, y + y2);
                }
              }
              int tmpIndex = k;
              while (nodeType != 3) {
                g.drawLine(x + x + paddedNodeWidth / 2, y + y + nodeHeight, x + x + paddedNodeWidth / 2, y + y);
                
                x1 = x + paddedNodeWidth / 2;y1 = y;
                for (int m = 0; m < edges.length; m++) {
                  if (edges[m][1] < 0) {
                    n2 = (GraphNode)m_nodes.elementAt(edges[m][0]);
                    g.drawLine(x + x1, y + y1, x + x + paddedNodeWidth / 2, y + y + nodeHeight);
                    
                    tmpIndex = m;
                    if (nodeType == 1) break;
                    drawArrow(g, x + x1, y + y1, x + x + paddedNodeWidth / 2, y + y + nodeHeight); break;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  


  private class GraphVisualizerTableModel
    extends AbstractTableModel
  {
    private static final long serialVersionUID = -4789813491347366596L;
    

    final String[] columnNames;
    

    final double[][] data;
    


    public GraphVisualizerTableModel(double[][] d, String[] c)
    {
      data = d;
      columnNames = c;
    }
    
    public int getColumnCount() {
      return columnNames.length;
    }
    
    public int getRowCount() {
      return data.length;
    }
    
    public String getColumnName(int col) {
      return columnNames[col];
    }
    
    public Object getValueAt(int row, int col) {
      return new Double(data[row][col]);
    }
    



    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }
    


    public boolean isCellEditable(int row, int col)
    {
      return false;
    }
  }
  

  private class GraphVisualizerMouseListener
    extends MouseAdapter
  {
    int x;
    
    int y;
    int nx;
    int ny;
    Rectangle r;
    
    private GraphVisualizerMouseListener() {}
    
    public void mouseClicked(MouseEvent me)
    {
      Dimension d = m_gp.getPreferredSize();
      

      x = (this.y = this.nx = this.ny = 0);
      
      if (width < m_gp.getWidth())
        nx = ((int)((nx + m_gp.getWidth() / 2 - width / 2) / scale));
      if (height < m_gp.getHeight()) {
        ny = ((int)((ny + m_gp.getHeight() / 2 - height / 2) / scale));
      }
      r = new Rectangle(0, 0, (int)(paddedNodeWidth * scale), (int)(nodeHeight * scale));
      
      x += me.getX();y += me.getY();
      

      for (int i = 0; i < m_nodes.size(); i++) {
        GraphNode n = (GraphNode)m_nodes.elementAt(i);
        r.x = ((int)((nx + x) * scale));r.y = ((int)((ny + y) * scale));
        if (r.contains(x, y)) {
          if (probs == null) {
            return;
          }
          int noOfPrntsOutcomes = 1;
          if (prnts != null) {
            for (int j = 0; j < prnts.length; j++) {
              GraphNode n2 = (GraphNode)m_nodes.elementAt(prnts[j]);
              noOfPrntsOutcomes *= outcomes.length;
            }
            if (noOfPrntsOutcomes > 511) {
              Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("GraphVisualizer_GraphVisualizerMouseListener_MouseClicked_Error_Text_First") + noOfPrntsOutcomes + Messages.getString("GraphVisualizer_GraphVisualizerMouseListener_MouseClicked_Error_Text_Second"));
              
              return;
            }
          }
          
          GraphVisualizer.GraphVisualizerTableModel tm = new GraphVisualizer.GraphVisualizerTableModel(GraphVisualizer.this, probs, outcomes);
          

          JTable jTblProbs = new JTable(tm);
          
          JScrollPane js = new JScrollPane(jTblProbs);
          
          if (prnts != null) {
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel jPlRowHeader = new JPanel(new GridBagLayout());
            

            int[] idx = new int[prnts.length];
            
            int[] lengths = new int[prnts.length];
            












            anchor = 18;
            fill = 2;
            insets = new Insets(0, 1, 0, 0);
            int addNum = 0;int temp = 0;
            boolean dark = false;
            for (;;)
            {
              gridwidth = 1;
              for (int k = 0; k < prnts.length; k++) {
                GraphNode n2 = (GraphNode)m_nodes.elementAt(prnts[k]);
                JLabel lb = new JLabel(outcomes[idx[k]]);
                lb.setFont(new Font("Dialog", 0, 12));
                lb.setOpaque(true);
                lb.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
                lb.setHorizontalAlignment(0);
                if (dark) {
                  lb.setBackground(lb.getBackground().darker());
                  lb.setForeground(Color.white);
                }
                else {
                  lb.setForeground(Color.black);
                }
                temp = getPreferredSizewidth;
                

                lb.setPreferredSize(new Dimension(temp, jTblProbs.getRowHeight()));
                

                if (lengths[k] < temp)
                  lengths[k] = temp;
                temp = 0;
                
                if (k == prnts.length - 1) {
                  gridwidth = 0;
                  dark = dark != true;
                }
                jPlRowHeader.add(lb, gbc);
                addNum++;
              }
              
              for (int k = prnts.length - 1; k >= 0; k--) {
                GraphNode n2 = (GraphNode)m_nodes.elementAt(prnts[k]);
                if ((idx[k] == outcomes.length - 1) && (k != 0)) {
                  idx[k] = 0;
                }
                else
                {
                  idx[k] += 1;
                  break;
                }
              }
              
              GraphNode n2 = (GraphNode)m_nodes.elementAt(prnts[0]);
              if (idx[0] == outcomes.length) {
                JLabel lb = (JLabel)jPlRowHeader.getComponent(addNum - 1);
                jPlRowHeader.remove(addNum - 1);
                lb.setPreferredSize(new Dimension(getPreferredSizewidth, jTblProbs.getRowHeight()));
                
                gridwidth = 0;
                weighty = 1.0D;
                jPlRowHeader.add(lb, gbc);
                weighty = 0.0D;
                break;
              }
            }
            
            gridwidth = 1;
            


            JPanel jPlRowNames = new JPanel(new GridBagLayout());
            for (int j = 0; j < prnts.length; j++)
            {
              JLabel lb1 = new JLabel(m_nodes.elementAt(prnts[j])).lbl);
              
              lb1.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
              Dimension tempd = lb1.getPreferredSize();
              

              if (width < lengths[j]) {
                lb1.setPreferredSize(new Dimension(lengths[j], height));
                lb1.setHorizontalAlignment(0);
                lb1.setMinimumSize(new Dimension(lengths[j], height));
              }
              else if (width > lengths[j]) {
                JLabel lb2 = (JLabel)jPlRowHeader.getComponent(j);
                lb2.setPreferredSize(new Dimension(width, getPreferredSizeheight));
              }
              
              jPlRowNames.add(lb1, gbc);
            }
            
            js.setRowHeaderView(jPlRowHeader);
            js.setCorner("UPPER_LEFT_CORNER", jPlRowNames);
          }
          
          Messages.getInstance();JDialog jd = new JDialog((Frame)getTopLevelAncestor(), Messages.getString("GraphVisualizer_Main_Jd_JDialog_Text") + lbl, Dialog.ModalityType.DOCUMENT_MODAL);
          

          jd.setSize(500, 400);
          jd.setLocation(getLocation().x + getWidth() / 2 - 250, getLocation().y + getHeight() / 2 - 200);
          



          jd.getContentPane().setLayout(new BorderLayout());
          jd.getContentPane().add(js, "Center");
          jd.setVisible(true);
          
          return;
        }
      }
    }
  }
  
  private class GraphVisualizerMouseMotionListener extends MouseMotionAdapter
  {
    int x;
    int y;
    int nx;
    int ny;
    Rectangle r;
    GraphNode lastNode;
    
    private GraphVisualizerMouseMotionListener() {}
    
    public void mouseMoved(MouseEvent me)
    {
      Dimension d = m_gp.getPreferredSize();
      

      x = (this.y = this.nx = this.ny = 0);
      
      if (width < m_gp.getWidth())
        nx = ((int)((nx + m_gp.getWidth() / 2 - width / 2) / scale));
      if (height < m_gp.getHeight()) {
        ny = ((int)((ny + m_gp.getHeight() / 2 - height / 2) / scale));
      }
      r = new Rectangle(0, 0, (int)(paddedNodeWidth * scale), (int)(nodeHeight * scale));
      
      x += me.getX();y += me.getY();
      

      for (int i = 0; i < m_nodes.size(); i++) {
        GraphNode n = (GraphNode)m_nodes.elementAt(i);
        r.x = ((int)((nx + x) * scale));r.y = ((int)((ny + y) * scale));
        if (r.contains(x, y)) {
          if (n == lastNode) break;
          m_gp.highLight(n);
          if (lastNode != null)
            m_gp.highLight(lastNode);
          lastNode = n; break;
        }
      }
      

      if ((i == m_nodes.size()) && (lastNode != null)) {
        m_gp.repaint();
        
        lastNode = null;
      }
    }
  }
  




  public static void main(String[] args)
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("GraphVisualizer_Main_Logger_Text"));
    Messages.getInstance();JFrame jf = new JFrame(Messages.getString("GraphVisualizer_Main_JFrame_Text"));
    GraphVisualizer g = new GraphVisualizer();
    try
    {
      if (args[0].endsWith(".xml"))
      {







        g.readBIF(new FileInputStream(args[0]));
      }
      else
      {
        g.readDOT(new FileReader(args[0]));
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (BIFFormatException bf) { bf.printStackTrace();System.exit(-1);
    }
    jf.getContentPane().add(g);
    
    jf.setDefaultCloseOperation(3);
    jf.setSize(800, 600);
    
    jf.setVisible(true);
  }
}

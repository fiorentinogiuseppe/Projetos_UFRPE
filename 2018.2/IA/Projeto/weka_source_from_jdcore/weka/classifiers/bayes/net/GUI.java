package weka.classifiers.bayes.net;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Random;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.ConverterFileChooser;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.LookAndFeel;
import weka.gui.PropertyDialog;
import weka.gui.graphvisualizer.BIFFormatException;
import weka.gui.graphvisualizer.BIFParser;
import weka.gui.graphvisualizer.GraphNode;
import weka.gui.graphvisualizer.HierarchicalBCEngine;
import weka.gui.graphvisualizer.LayoutCompleteEvent;
import weka.gui.graphvisualizer.LayoutCompleteEventListener;
import weka.gui.graphvisualizer.LayoutEngine;
import weka.gui.visualize.PrintablePanel;
































public class GUI
  extends JPanel
  implements LayoutCompleteEventListener
{
  private static final long serialVersionUID = -2038911085935515624L;
  protected LayoutEngine m_layoutEngine;
  protected GraphPanel m_GraphPanel;
  EditableBayesNet m_BayesNet = new EditableBayesNet(true);
  

  protected String m_sFileName = "";
  
  MarginCalculator m_marginCalculator = null;
  




  MarginCalculator m_marginCalculatorWithEvidence = null;
  



  boolean m_bViewMargins = false;
  boolean m_bViewCliques = false;
  

  private JMenuBar m_menuBar;
  

  Instances m_Instances = null;
  

  final JTextField m_jTfZoom;
  
  final JToolBar m_jTbTools;
  
  final JLabel m_jStatusBar;
  
  private final JTextField m_jTfNodeWidth = new JTextField(3);
  
  private final JTextField m_jTfNodeHeight = new JTextField(3);
  

  JScrollPane m_jScrollPane;
  
  private final String ICONPATH = "weka/classifiers/bayes/net/icons/";
  

  private double m_fScale = 1.0D;
  

  private int m_nNodeHeight = 2 * getFontMetrics(getFont()).getHeight();
  
  static final int DEFAULT_NODE_WIDTH = 50;
  private int m_nNodeWidth = 50;
  
  static final int PADDING = 10;
  private int m_nPaddedNodeWidth = 60;
  


  private int[] m_nZoomPercents = { 10, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 350, 400, 450, 500, 550, 600, 650, 700, 800, 900, 999 };
  


  Action a_new = new ActionNew();
  
  Action a_quit = new ActionQuit();
  Action a_save = new ActionSave();
  ActionExport a_export = new ActionExport();
  ActionPrint a_print = new ActionPrint();
  Action a_load = new ActionLoad();
  Action a_zoomin = new ActionZoomIn();
  Action a_zoomout = new ActionZoomOut();
  Action a_layout = new ActionLayout();
  
  Action a_saveas = new ActionSaveAs();
  
  Action a_viewtoolbar = new ActionViewToolbar();
  
  Action a_viewstatusbar = new ActionViewStatusbar();
  
  Action a_networkgenerator = new ActionGenerateNetwork();
  
  Action a_datagenerator = new ActionGenerateData();
  
  Action a_datasetter = new ActionSetData();
  
  Action a_learn = new ActionLearn();
  Action a_learnCPT = new ActionLearnCPT();
  
  Action a_help = new ActionHelp();
  
  Action a_about = new ActionAbout();
  
  ActionAddNode a_addnode = new ActionAddNode();
  
  Action a_delnode = new ActionDeleteNode();
  Action a_cutnode = new ActionCutNode();
  Action a_copynode = new ActionCopyNode();
  Action a_pastenode = new ActionPasteNode();
  Action a_selectall = new ActionSelectAll();
  
  Action a_addarc = new ActionAddArc();
  
  Action a_delarc = new ActionDeleteArc();
  
  Action a_undo = new ActionUndo();
  
  Action a_redo = new ActionRedo();
  
  Action a_alignleft = new ActionAlignLeft();
  Action a_alignright = new ActionAlignRight();
  Action a_aligntop = new ActionAlignTop();
  Action a_alignbottom = new ActionAlignBottom();
  Action a_centerhorizontal = new ActionCenterHorizontal();
  Action a_centervertical = new ActionCenterVertical();
  Action a_spacehorizontal = new ActionSpaceHorizontal();
  Action a_spacevertical = new ActionSpaceVertical();
  

  int m_nCurrentNode = -1;
  
  Selection m_Selection = new Selection();
  
  Rectangle m_nSelectedRect = null;
  

  class Selection {
    FastVector m_selected;
    
    public Selection() { m_selected = new FastVector(); }
    
    public FastVector getSelected() { return m_selected; }
    
    void updateGUI() { if (m_selected.size() > 0) {
        a_cutnode.setEnabled(true);
        a_copynode.setEnabled(true);
      } else {
        a_cutnode.setEnabled(false);
        a_copynode.setEnabled(false);
      }
      if (m_selected.size() > 1) {
        a_alignleft.setEnabled(true);
        a_alignright.setEnabled(true);
        a_aligntop.setEnabled(true);
        a_alignbottom.setEnabled(true);
        a_centerhorizontal.setEnabled(true);
        a_centervertical.setEnabled(true);
        a_spacehorizontal.setEnabled(true);
        a_spacevertical.setEnabled(true);
      } else {
        a_alignleft.setEnabled(false);
        a_alignright.setEnabled(false);
        a_aligntop.setEnabled(false);
        a_alignbottom.setEnabled(false);
        a_centerhorizontal.setEnabled(false);
        a_centervertical.setEnabled(false);
        a_spacehorizontal.setEnabled(false);
        a_spacevertical.setEnabled(false);
      }
    }
    
    public void addToSelection(int nNode) {
      for (int iNode = 0; iNode < m_selected.size(); iNode++) {
        if (nNode == ((Integer)m_selected.elementAt(iNode)).intValue()) {
          return;
        }
      }
      m_selected.addElement(Integer.valueOf(nNode));
      updateGUI();
    }
    
    public void addToSelection(int[] iNodes) {
      for (int iNode = 0; iNode < iNodes.length; iNode++) {
        addToSelection(iNodes[iNode]);
      }
      updateGUI();
    }
    
    public void addToSelection(Rectangle selectedRect) {
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        if (contains(selectedRect, iNode)) {
          addToSelection(iNode);
        }
      }
    }
    
    public void selectAll() {
      m_selected.removeAllElements();
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        m_selected.addElement(Integer.valueOf(iNode));
      }
      updateGUI();
    }
    
    boolean contains(Rectangle rect, int iNode) {
      return rect.intersects(m_BayesNet.getPositionX(iNode) * m_fScale, m_BayesNet.getPositionY(iNode) * m_fScale, m_nPaddedNodeWidth * m_fScale, m_nNodeHeight * m_fScale);
    }
    

    public void removeFromSelection(int nNode)
    {
      for (int iNode = 0; iNode < m_selected.size(); iNode++) {
        if (nNode == ((Integer)m_selected.elementAt(iNode)).intValue()) {
          m_selected.removeElementAt(iNode);
        }
      }
      updateGUI();
    }
    
    public void toggleSelection(int nNode) {
      for (int iNode = 0; iNode < m_selected.size(); iNode++) {
        if (nNode == ((Integer)m_selected.elementAt(iNode)).intValue()) {
          m_selected.removeElementAt(iNode);
          updateGUI();
          return;
        }
      }
      addToSelection(nNode);
    }
    
    public void toggleSelection(Rectangle selectedRect) {
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        if (contains(selectedRect, iNode)) {
          toggleSelection(iNode);
        }
      }
    }
    
    public void clear() {
      m_selected.removeAllElements();
      updateGUI();
    }
    
    public void draw(Graphics g) {
      if (m_selected.size() == 0) {
        return;
      }
      
      for (int iNode = 0; iNode < m_selected.size(); iNode++) {
        int nNode = ((Integer)m_selected.elementAt(iNode)).intValue();
        int nPosX = m_BayesNet.getPositionX(nNode);
        int nPosY = m_BayesNet.getPositionY(nNode);
        g.setColor(Color.BLACK);
        int nXRC = nPosX + m_nPaddedNodeWidth - m_nNodeWidth - (m_nPaddedNodeWidth - m_nNodeWidth) / 2;
        int nYRC = nPosY;
        int d = 5;
        g.fillRect(nXRC, nYRC, d, d);
        g.fillRect(nXRC, nYRC + m_nNodeHeight, d, d);
        g.fillRect(nXRC + m_nNodeWidth, nYRC, d, d);
        g.fillRect(nXRC + m_nNodeWidth, nYRC + m_nNodeHeight, d, d);
      }
    }
  }
  
  ClipBoard m_clipboard = new ClipBoard();
  
  class ClipBoard {
    String m_sText = null;
    
    public ClipBoard() { if (a_pastenode != null)
        a_pastenode.setEnabled(false);
    }
    
    public boolean hasText() { return m_sText != null; }
    
    public String getText() { return m_sText; }
    
    public void setText(String sText) {
      m_sText = sText;
      a_pastenode.setEnabled(true);
    }
  }
  

  class MyAction
    extends AbstractAction
  {
    private static final long serialVersionUID = -2038911111935517L;
    
    public MyAction(String sName, String sToolTipText, String sIcon, String sAcceleratorKey)
    {
      super();
      
      putValue("ShortDescription", sToolTipText);
      putValue("LongDescription", sToolTipText);
      KeyStroke keyStroke; if (sAcceleratorKey.length() > 0) {
        keyStroke = KeyStroke.getKeyStroke(sAcceleratorKey);
        putValue("AcceleratorKey", keyStroke);
      }
      putValue("MnemonicKey", Integer.valueOf(sName.charAt(0)));
      URL tempURL = ClassLoader.getSystemResource("weka/classifiers/bayes/net/icons/" + sIcon + ".png");
      if (tempURL != null) {
        putValue("SmallIcon", new ImageIcon(tempURL));
      } else {
        putValue("SmallIcon", new ImageIcon(new BufferedImage(20, 20, 6)));
      }
    }
    

    public void actionPerformed(ActionEvent ae) {}
  }
  

  class ActionGenerateNetwork
    extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038911085935517L;
    

    public ActionGenerateNetwork()
    {
      super("Generate Network", "Generate Random Bayesian Network", "generate.network", "ctrl N");
    }
    
    int m_nNrOfNodes = 10;
    
    int m_nNrOfArcs = 15;
    
    int m_nCardinality = 2;
    
    int m_nSeed = 123;
    
    JDialog dlg = null;
    
    public void actionPerformed(ActionEvent ae) {
      if (dlg == null) {
        dlg = new JDialog();
        dlg.setTitle("Generate Random Bayesian Network Options");
        
        JLabel jLbNrOfNodes = new JLabel("Nr of nodes");
        final JTextField jTfNrOfNodes = new JTextField(3);
        jTfNrOfNodes.setHorizontalAlignment(0);
        jTfNrOfNodes.setText("" + m_nNrOfNodes);
        JLabel jLbNrOfArcs = new JLabel("Nr of arcs");
        final JTextField jTfNrOfArcs = new JTextField(3);
        jTfNrOfArcs.setHorizontalAlignment(0);
        jTfNrOfArcs.setText("" + m_nNrOfArcs);
        JLabel jLbCardinality = new JLabel("Cardinality");
        final JTextField jTfCardinality = new JTextField(3);
        jTfCardinality.setHorizontalAlignment(0);
        jTfCardinality.setText("" + m_nCardinality);
        JLabel jLbSeed = new JLabel("Random seed");
        final JTextField jTfSeed = new JTextField(3);
        jTfSeed.setHorizontalAlignment(0);
        jTfSeed.setText("" + m_nSeed);
        

        JButton jBtGo = new JButton("Generate Network");
        
        jBtGo.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            try {
              BayesNetGenerator generator = new BayesNetGenerator();
              m_BayesNet = generator;
              m_BayesNet.clearUndoStack();
              
              String[] options = new String[8];
              options[0] = "-N";
              options[1] = ("" + jTfNrOfNodes.getText());
              options[2] = "-A";
              options[3] = ("" + jTfNrOfArcs.getText());
              options[4] = "-C";
              options[5] = ("" + jTfCardinality.getText());
              options[6] = "-S";
              options[7] = ("" + jTfSeed.getText());
              generator.setOptions(options);
              generator.generateRandomNetwork();
              

              BIFReader bifReader = new BIFReader();
              bifReader.processString(m_BayesNet.toXMLBIF03());
              m_BayesNet = new EditableBayesNet(bifReader);
              
              updateStatus();
              layoutGraph();
              a_datagenerator.setEnabled(true);
              m_Instances = null;
              a_learn.setEnabled(false);
              a_learnCPT.setEnabled(false);
              
              dlg.setVisible(false);
            } catch (Exception e) {
              e.printStackTrace();
            }
            
          }
          
        });
        JButton jBtCancel = new JButton("Cancel");
        jBtCancel.setMnemonic('C');
        jBtCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            dlg.setVisible(false);
          }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        dlg.setLayout(new GridBagLayout());
        
        Container c = new Container();
        c.setLayout(new GridBagLayout());
        gridwidth = 2;
        insets = new Insets(8, 0, 0, 0);
        anchor = 18;
        gridwidth = -1;
        fill = 2;
        c.add(jLbNrOfNodes, gbc);
        gridwidth = 0;
        c.add(jTfNrOfNodes, gbc);
        gridwidth = -1;
        c.add(jLbNrOfArcs, gbc);
        gridwidth = 0;
        c.add(jTfNrOfArcs, gbc);
        gridwidth = -1;
        c.add(jLbCardinality, gbc);
        gridwidth = 0;
        c.add(jTfCardinality, gbc);
        gridwidth = -1;
        c.add(jLbSeed, gbc);
        gridwidth = 0;
        c.add(jTfSeed, gbc);
        
        fill = 2;
        dlg.add(c, gbc);
        dlg.add(jBtGo);
        gridwidth = 0;
        dlg.add(jBtCancel);
      }
      dlg.pack();
      dlg.setLocation(100, 100);
      dlg.setVisible(true);
      dlg.setSize(dlg.getPreferredSize());
      dlg.setVisible(false);
      dlg.setVisible(true);
      dlg.repaint();
    }
  }
  
  class ActionGenerateData extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038911085935516L;
    
    public ActionGenerateData() {
      super("Generate Data", "Generate Random Instances from Network", "generate.data", "ctrl D");
    }
    
    int m_nNrOfInstances = 100;
    
    int m_nSeed = 1234;
    
    String m_sFile = "";
    
    JDialog dlg = null;
    
    public void actionPerformed(ActionEvent ae) {
      if (dlg == null) {
        dlg = new JDialog();
        dlg.setTitle("Generate Random Data Options");
        
        JLabel jLbNrOfInstances = new JLabel("Nr of instances");
        final JTextField jTfNrOfInstances = new JTextField(3);
        jTfNrOfInstances.setHorizontalAlignment(0);
        jTfNrOfInstances.setText("" + m_nNrOfInstances);
        JLabel jLbSeed = new JLabel("Random seed");
        JTextField jTfSeed = new JTextField(3);
        jTfSeed.setHorizontalAlignment(0);
        jTfSeed.setText("" + m_nSeed);
        JLabel jLbFile = new JLabel("Output file (optional)");
        final JTextField jTfFile = new JTextField(12);
        jTfFile.setHorizontalAlignment(0);
        jTfFile.setText(m_sFile);
        

        JButton jBtGo = new JButton("Generate Data");
        
        jBtGo.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            try {
              String tmpfilename = "tmp.bif.file.xml";
              BayesNetGenerator generator = new BayesNetGenerator();
              String[] options = new String[4];
              options[0] = "-M";
              options[1] = ("" + jTfNrOfInstances.getText());
              options[2] = "-F";
              options[3] = tmpfilename;
              FileWriter outfile = new FileWriter(tmpfilename);
              StringBuffer text = new StringBuffer();
              if (m_marginCalculator == null) {
                m_marginCalculator = new MarginCalculator();
                m_marginCalculator.calcMargins(m_BayesNet);
              }
              text.append(m_marginCalculator.toXMLBIF03());
              outfile.write(text.toString());
              outfile.close();
              
              generator.setOptions(options);
              generator.generateRandomNetwork();
              generator.generateInstances();
              m_Instances = m_Instances;
              a_learn.setEnabled(true);
              a_learnCPT.setEnabled(true);
              
              m_sFile = jTfFile.getText();
              if ((m_sFile != null) && (!m_sFile.equals(""))) {
                AbstractFileSaver saver = ConverterUtils.getSaverForFile(m_sFile);
                
                if (saver == null)
                  saver = new ArffSaver();
                saver.setFile(new File(m_sFile));
                saver.setInstances(m_Instances);
                saver.writeBatch();
              }
            }
            catch (Exception e) {
              e.printStackTrace();
            }
            dlg.setVisible(false);
          }
          
        });
        JButton jBtFile = new JButton("Browse");
        jBtFile.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            ConverterFileChooser fc = new ConverterFileChooser(System.getProperty("user.dir"));
            fc.setDialogTitle("Save Instances As");
            int rval = fc.showSaveDialog(GUI.this);
            
            if (rval == 0) {
              String filename = fc.getSelectedFile().toString();
              jTfFile.setText(filename);
            }
            dlg.setVisible(true);
          }
          
        });
        JButton jBtCancel = new JButton("Cancel");
        jBtCancel.setMnemonic('C');
        jBtCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            dlg.setVisible(false);
          }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        dlg.setLayout(new GridBagLayout());
        
        Container c = new Container();
        c.setLayout(new GridBagLayout());
        gridwidth = 2;
        insets = new Insets(8, 0, 0, 0);
        anchor = 18;
        gridwidth = -1;
        fill = 2;
        c.add(jLbNrOfInstances, gbc);
        gridwidth = 0;
        c.add(jTfNrOfInstances, gbc);
        gridwidth = -1;
        c.add(jLbSeed, gbc);
        gridwidth = 0;
        c.add(jTfSeed, gbc);
        gridwidth = -1;
        c.add(jLbFile, gbc);
        gridwidth = 0;
        c.add(jTfFile, gbc);
        gridwidth = 0;
        c.add(jBtFile, gbc);
        
        fill = 2;
        dlg.add(c, gbc);
        dlg.add(jBtGo);
        gridwidth = 0;
        dlg.add(jBtCancel);
      }
      dlg.setLocation(100, 100);
      dlg.setVisible(true);
      dlg.setSize(dlg.getPreferredSize());
      dlg.setVisible(false);
      dlg.setVisible(true);
      dlg.repaint();
    }
  }
  
  class ActionLearn extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038911085935516L;
    
    public ActionLearn()
    {
      super("Learn Network", "Learn Bayesian Network", "learn", "ctrl L");
      setEnabled(false);
    }
    
    JDialog dlg = null;
    
    public void actionPerformed(ActionEvent ae) {
      if (dlg == null) {
        dlg = new JDialog();
        dlg.setTitle("Learn Bayesian Network");
        
        JButton jBtOptions = new JButton("Options");
        jBtOptions.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ae) {
            try {
              GenericObjectEditor.registerEditors();
              GenericObjectEditor ce = new GenericObjectEditor(true);
              ce.setClassType(Classifier.class);
              ce.setValue(m_BayesNet);
              PropertyDialog pd;
              PropertyDialog pd;
              if (PropertyDialog.getParentDialog(GUI.this) != null) {
                pd = new PropertyDialog(PropertyDialog.getParentDialog(GUI.this), ce, 100, 100);
              } else
                pd = new PropertyDialog(PropertyDialog.getParentFrame(GUI.this), ce, 100, 100);
              pd.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                  PropertyEditor pe = ((PropertyDialog)e.getSource()).getEditor();
                  Object c = pe.getValue();
                  String options = "";
                  if ((c instanceof OptionHandler)) {
                    options = Utils.joinOptions(((OptionHandler)c).getOptions());
                    try {
                      m_BayesNet.setOptions(((OptionHandler)c).getOptions());
                    } catch (Exception e2) {
                      e2.printStackTrace();
                    }
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
            m_BayesNet.clearUndoStack();
            a_undo.setEnabled(false);
            a_redo.setEnabled(false);
          }
          
        });
        JTextField jTfOptions = new JTextField(40);
        jTfOptions.setHorizontalAlignment(0);
        jTfOptions.setText("" + Utils.joinOptions(m_BayesNet.getOptions()));
        

        JButton jBtGo = new JButton("Learn");
        
        jBtGo.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            try {
              m_BayesNet.buildClassifier(m_Instances);
              layoutGraph();
              updateStatus();
              m_BayesNet.clearUndoStack();
              
              dlg.setVisible(false);
            } catch (Exception e) {
              e.printStackTrace();
            }
            dlg.setVisible(false);
          }
          

        });
        JButton jBtCancel = new JButton("Cancel");
        jBtCancel.setMnemonic('C');
        jBtCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            dlg.setVisible(false);
          }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        dlg.setLayout(new GridBagLayout());
        
        Container c = new Container();
        c.setLayout(new GridBagLayout());
        gridwidth = 2;
        insets = new Insets(8, 0, 0, 0);
        anchor = 18;
        gridwidth = -1;
        fill = 2;
        c.add(jBtOptions, gbc);
        gridwidth = 0;
        c.add(jTfOptions, gbc);
        
        fill = 2;
        dlg.add(c, gbc);
        dlg.add(jBtGo);
        gridwidth = 0;
        dlg.add(jBtCancel);
      }
      dlg.setLocation(100, 100);
      dlg.setVisible(true);
      dlg.setSize(dlg.getPreferredSize());
      dlg.setVisible(false);
      dlg.setVisible(true);
      dlg.repaint();
    }
  }
  
  class ActionLearnCPT extends GUI.MyAction
  {
    private static final long serialVersionUID = -2022211085935516L;
    
    public ActionLearnCPT() {
      super("Learn CPT", "Learn conditional probability tables", "learncpt", "");
      setEnabled(false);
    }
    
    public void actionPerformed(ActionEvent ae) {
      if (m_Instances == null) {
        JOptionPane.showMessageDialog(null, "Select instances to learn from first (menu Tools/Set Data)");
        return;
      }
      try {
        m_BayesNet.setData(m_Instances);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Data set is not compatible with network.\n" + e.getMessage() + "\nChoose other instances (menu Tools/Set Data)");
        return;
      }
      try {
        m_BayesNet.estimateCPTs();
        m_BayesNet.clearUndoStack();
      } catch (Exception e) {
        e.printStackTrace();
      }
      updateStatus();
    }
  }
  
  class ActionSetData extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038911085935519L;
    
    public ActionSetData() {
      super("Set Data", "Set Data File", "setdata", "ctrl A");
    }
    
    public void actionPerformed(ActionEvent ae) {
      ConverterFileChooser fc = new ConverterFileChooser(System.getProperty("user.dir"));
      fc.setDialogTitle("Set Data File");
      int rval = fc.showOpenDialog(GUI.this);
      
      if (rval == 0) {
        AbstractFileLoader loader = fc.getLoader();
        try {
          if (loader != null)
            m_Instances = loader.getDataSet();
          if (m_Instances.classIndex() == -1)
            m_Instances.setClassIndex(m_Instances.numAttributes() - 1);
          a_learn.setEnabled(true);
          a_learnCPT.setEnabled(true);
          repaint();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  class ActionUndo extends GUI.MyAction
  {
    private static final long serialVersionUID = -3038910085935519L;
    
    public ActionUndo() {
      super("Undo", "Undo", "undo", "ctrl Z");
      setEnabled(false);
    }
    
    public boolean isEnabled() {
      return m_BayesNet.canUndo();
    }
    
    public void actionPerformed(ActionEvent ae) {
      String sMsg = m_BayesNet.undo();
      m_jStatusBar.setText("Undo action performed: " + sMsg);
      


      a_redo.setEnabled(m_BayesNet.canRedo());
      a_undo.setEnabled(m_BayesNet.canUndo());
      m_Selection.clear();
      updateStatus();
      repaint();
    }
  }
  
  class ActionRedo extends GUI.MyAction
  {
    private static final long serialVersionUID = -4038910085935519L;
    
    public ActionRedo() {
      super("Redo", "Redo", "redo", "ctrl Y");
      setEnabled(false);
    }
    
    public boolean isEnabled() {
      return m_BayesNet.canRedo();
    }
    
    public void actionPerformed(ActionEvent ae) {
      String sMsg = m_BayesNet.redo();
      m_jStatusBar.setText("Redo action performed: " + sMsg);
      


      m_Selection.clear();
      updateStatus();
      repaint();
    }
  }
  
  class ActionAddNode extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038910085935519L;
    
    public ActionAddNode() {
      super("Add Node", "Add Node", "addnode", "");
    }
    
    JDialog dlg = null;
    
    JTextField jTfName = new JTextField(20);
    
    JTextField jTfCard = new JTextField(3);
    
    int m_X = Integer.MAX_VALUE;
    int m_Y;
    
    public void addNode(int nX, int nY) { m_X = nX;
      m_Y = nY;
      addNode();
    }
    
    void addNode() {
      if (dlg == null) {
        dlg = new JDialog();
        dlg.setTitle("Add node");
        JLabel jLbName = new JLabel("Name");
        jTfName.setHorizontalAlignment(0);
        JLabel jLbCard = new JLabel("Cardinality");
        jTfCard.setHorizontalAlignment(0);
        jTfCard.setText("2");
        

        JButton jBtCancel = new JButton("Cancel");
        jBtCancel.setMnemonic('C');
        jBtCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            dlg.setVisible(false);
          }
        });
        JButton jBtOk = new JButton("Ok");
        jBtOk.setMnemonic('O');
        jBtOk.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            String sName = jTfName.getText();
            if (sName.length() <= 0) {
              JOptionPane.showMessageDialog(null, "Name should have at least one character");
              return;
            }
            int nCard = new Integer(jTfCard.getText()).intValue();
            if (nCard <= 1) {
              JOptionPane.showMessageDialog(null, "Cardinality should be larger than 1");
              return;
            }
            try {
              if (m_X < Integer.MAX_VALUE) {
                m_BayesNet.addNode(sName, nCard, m_X, m_Y);
              } else {
                m_BayesNet.addNode(sName, nCard);
              }
              m_jStatusBar.setText(m_BayesNet.lastActionMsg());
              a_undo.setEnabled(true);
              a_redo.setEnabled(false);


            }
            catch (Exception e)
            {


              e.printStackTrace();
            }
            repaint();
            dlg.setVisible(false);
          }
        });
        dlg.setLayout(new GridLayout(3, 2, 10, 10));
        dlg.add(jLbName);
        dlg.add(jTfName);
        dlg.add(jLbCard);
        dlg.add(jTfCard);
        dlg.add(jBtOk);
        dlg.add(jBtCancel);
        dlg.setSize(dlg.getPreferredSize());
      }
      jTfName.setText("Node" + (m_BayesNet.getNrOfNodes() + 1));
      dlg.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_X = Integer.MAX_VALUE;
      addNode();
    }
  }
  
  class ActionDeleteNode extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038912085935519L;
    
    public ActionDeleteNode() {
      super("Delete Node", "Delete Node", "delnode", "DELETE");
    }
    
    public void actionPerformed(ActionEvent ae) {
      if (m_Selection.getSelected().size() > 0) {
        m_BayesNet.deleteSelection(m_Selection.getSelected());
        m_jStatusBar.setText(m_BayesNet.lastActionMsg());
        m_Selection.clear();
        updateStatus();
        repaint();
      } else {
        String[] options = new String[m_BayesNet.getNrOfNodes()];
        for (int i = 0; i < options.length; i++) {
          options[i] = m_BayesNet.getNodeName(i);
        }
        String sResult = (String)JOptionPane.showInputDialog(null, "Select node to delete", "Nodes", 0, null, options, options[0]);
        
        if ((sResult != null) && (!sResult.equals(""))) {
          int iNode = m_BayesNet.getNode2(sResult);
          deleteNode(iNode);
        }
      }
    }
  }
  
  class ActionCopyNode extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038732085935519L;
    
    public ActionCopyNode() {
      super("Copy", "Copy Nodes", "copy", "ctrl C");
    }
    
    public ActionCopyNode(String sName, String sToolTipText, String sIcon, String sAcceleratorKey) {
      super(sName, sToolTipText, sIcon, sAcceleratorKey);
    }
    
    public void actionPerformed(ActionEvent ae) {
      copy();
    }
    
    public void copy() {
      String sXML = m_BayesNet.toXMLBIF03(m_Selection.getSelected());
      m_clipboard.setText(sXML);
    }
  }
  
  class ActionCutNode extends GUI.ActionCopyNode
  {
    private static final long serialVersionUID = -2038822085935519L;
    
    public ActionCutNode() {
      super("Cut", "Cut Nodes", "cut", "ctrl X");
    }
    
    public void actionPerformed(ActionEvent ae) {
      copy();
      m_BayesNet.deleteSelection(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      m_Selection.clear();
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionPasteNode extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038732085935519L;
    
    public ActionPasteNode() {
      super("Paste", "Paste Nodes", "paste", "ctrl V");
    }
    
    public void actionPerformed(ActionEvent ae) {
      try {
        m_BayesNet.paste(m_clipboard.getText());
        updateStatus();
        m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public boolean isEnabled() { return m_clipboard.hasText(); }
  }
  
  class ActionSelectAll extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038642085935519L;
    
    public ActionSelectAll()
    {
      super("Select All", "Select All Nodes", "selectall", "ctrl A");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_Selection.selectAll();
      repaint();
    }
  }
  
  class ActionExport extends GUI.MyAction {
    boolean m_bIsExporting = false;
    private static final long serialVersionUID = -3027642085935519L;
    
    public ActionExport()
    {
      super("Export", "Export to graphics file", "export", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_bIsExporting = true;
      m_GraphPanel.saveComponent();
      m_bIsExporting = false;
      repaint(); }
    
    public boolean isExporting() { return m_bIsExporting; }
  }
  
  class ActionAlignLeft extends GUI.MyAction
  {
    private static final long serialVersionUID = -3138642085935519L;
    
    public ActionAlignLeft() {
      super("Align Left", "Align Left", "alignleft", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.alignLeft(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionAlignRight extends GUI.MyAction
  {
    private static final long serialVersionUID = -4238642085935519L;
    
    public ActionAlignRight() {
      super("Align Right", "Align Right", "alignright", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.alignRight(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionAlignTop extends GUI.MyAction
  {
    private static final long serialVersionUID = -5338642085935519L;
    
    public ActionAlignTop() {
      super("Align Top", "Align Top", "aligntop", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.alignTop(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionAlignBottom extends GUI.MyAction
  {
    private static final long serialVersionUID = -6438642085935519L;
    
    public ActionAlignBottom() {
      super("Align Bottom", "Align Bottom", "alignbottom", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.alignBottom(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionCenterHorizontal extends GUI.MyAction
  {
    private static final long serialVersionUID = -7538642085935519L;
    
    public ActionCenterHorizontal() {
      super("Center Horizontal", "Center Horizontal", "centerhorizontal", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.centerHorizontal(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionCenterVertical extends GUI.MyAction
  {
    private static final long serialVersionUID = -8638642085935519L;
    
    public ActionCenterVertical() {
      super("Center Vertical", "Center Vertical", "centervertical", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.centerVertical(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionSpaceHorizontal extends GUI.MyAction
  {
    private static final long serialVersionUID = -9738642085935519L;
    
    public ActionSpaceHorizontal() {
      super("Space Horizontal", "Space Horizontal", "spacehorizontal", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.spaceHorizontal(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionSpaceVertical extends GUI.MyAction
  {
    private static final long serialVersionUID = -838642085935519L;
    
    public ActionSpaceVertical() {
      super("Space Vertical", "Space Vertical", "spacevertical", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_BayesNet.spaceVertical(m_Selection.getSelected());
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      a_undo.setEnabled(true);
      a_redo.setEnabled(false);
      repaint();
    }
  }
  
  class ActionAddArc extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038913085935519L;
    
    public ActionAddArc() {
      super("Add Arc", "Add Arc", "addarc", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      try {
        String[] options = new String[m_BayesNet.getNrOfNodes()];
        for (int i = 0; i < options.length; i++) {
          options[i] = m_BayesNet.getNodeName(i);
        }
        String sChild = (String)JOptionPane.showInputDialog(null, "Select child node", "Nodes", 0, null, options, options[0]);
        
        if ((sChild == null) || (sChild.equals(""))) {
          return;
        }
        int iChild = m_BayesNet.getNode(sChild);
        addArcInto(iChild);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  class ActionDeleteArc extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038914085935519L;
    
    public ActionDeleteArc() {
      super("Delete Arc", "Delete Arc", "delarc", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      int nEdges = 0;
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        nEdges += m_BayesNet.getNrOfParents(iNode);
      }
      String[] options = new String[nEdges];
      int i = 0;
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(iNode); iParent++) {
          int nParent = m_BayesNet.getParent(iNode, iParent);
          String sEdge = m_BayesNet.getNodeName(nParent);
          sEdge = sEdge + " -> ";
          sEdge = sEdge + m_BayesNet.getNodeName(iNode);
          options[(i++)] = sEdge;
        }
      }
      
      deleteArc(options);
    }
  }
  
  class ActionNew extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038911085935515L;
    
    public ActionNew() {
      super("New", "New Network", "new", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_sFileName = "";
      m_BayesNet = new EditableBayesNet(true);
      updateStatus();
      layoutGraph();
      a_datagenerator.setEnabled(false);
      m_BayesNet.clearUndoStack();
      m_jStatusBar.setText("New Network");
      m_Selection = new GUI.Selection(GUI.this);
      repaint();
    }
  }
  
  class ActionLoad extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038911085935515L;
    
    public ActionLoad() {
      super("Load", "Load Graph", "open", "ctrl O");
    }
    
    public void actionPerformed(ActionEvent ae) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      ExtensionFileFilter ef1 = new ExtensionFileFilter(".arff", "ARFF files");
      ExtensionFileFilter ef2 = new ExtensionFileFilter(".xml", "XML BIF files");
      fc.addChoosableFileFilter(ef1);
      fc.addChoosableFileFilter(ef2);
      fc.setDialogTitle("Load Graph");
      int rval = fc.showOpenDialog(GUI.this);
      
      if (rval == 0) {
        String sFileName = fc.getSelectedFile().toString();
        if (sFileName.endsWith(ef1.getExtensions()[0])) {
          initFromArffFile(sFileName);
        } else {
          try {
            readBIFFromFile(sFileName);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        m_jStatusBar.setText("Loaded " + sFileName);
        updateStatus();
      }
    }
  }
  
  class ActionViewStatusbar extends GUI.MyAction
  {
    private static final long serialVersionUID = -20389330812354L;
    
    public ActionViewStatusbar()
    {
      super("View statusbar", "View statusbar", "statusbar", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_jStatusBar.setVisible(!m_jStatusBar.isVisible());
    }
  }
  
  class ActionViewToolbar extends GUI.MyAction
  {
    private static final long serialVersionUID = -20389110812354L;
    
    public ActionViewToolbar() {
      super("View toolbar", "View toolbar", "toolbar", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      m_jTbTools.setVisible(!m_jTbTools.isVisible());
    }
  }
  
  class ActionSave extends GUI.MyAction
  {
    private static final long serialVersionUID = -20389110859355156L;
    
    public ActionSave() {
      super("Save", "Save Graph", "save", "ctrl S");
    }
    
    public ActionSave(String sName, String sToolTipText, String sIcon, String sAcceleratorKey) {
      super(sName, sToolTipText, sIcon, sAcceleratorKey);
    }
    
    public void actionPerformed(ActionEvent ae) {
      if (!m_sFileName.equals("")) {
        saveFile(m_sFileName);
        m_BayesNet.isSaved();
        m_jStatusBar.setText("Saved as " + m_sFileName);
      }
      else if (saveAs()) {
        m_BayesNet.isSaved();
        m_jStatusBar.setText("Saved as " + m_sFileName);
      }
    }
    


    ExtensionFileFilter ef1 = new ExtensionFileFilter(".xml", "XML BIF files");
    
    boolean saveAs() {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.addChoosableFileFilter(ef1);
      fc.setDialogTitle("Save Graph As");
      if (!m_sFileName.equals(""))
      {
        fc.setSelectedFile(new File(m_sFileName));
      }
      int rval = fc.showSaveDialog(GUI.this);
      
      if (rval == 0)
      {

        String sFileName = fc.getSelectedFile().toString();
        if (!sFileName.endsWith(".xml"))
          sFileName = sFileName.concat(".xml");
        saveFile(sFileName);
        return true;
      }
      return false;
    }
    
    protected void saveFile(String sFileName) {
      try {
        FileWriter outfile = new FileWriter(sFileName);
        outfile.write(m_BayesNet.toXMLBIF03());
        outfile.close();
        m_sFileName = sFileName;
        m_jStatusBar.setText("Saved as " + m_sFileName);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  class ActionSaveAs extends GUI.ActionSave
  {
    private static final long serialVersionUID = -20389110859354L;
    
    public ActionSaveAs() {
      super("Save As", "Save Graph As", "saveas", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      saveAs();
    }
  }
  
  class ActionPrint extends GUI.ActionSave
  {
    private static final long serialVersionUID = -20389001859354L;
    boolean m_bIsPrinting = false;
    
    public ActionPrint() { super("Print", "Print Graph", "print", "ctrl P"); }
    
    public void actionPerformed(ActionEvent ae)
    {
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPrintable(m_GraphPanel);
      if (printJob.printDialog())
        try {
          m_bIsPrinting = true;
          printJob.print();
          m_bIsPrinting = false;
        } catch (PrinterException pe) {
          m_jStatusBar.setText("Error printing: " + pe);
          m_bIsPrinting = false;
        }
      m_jStatusBar.setText("Print"); }
    
    public boolean isPrinting() { return m_bIsPrinting; }
  }
  
  class ActionQuit extends GUI.ActionSave
  {
    private static final long serialVersionUID = -2038911085935515L;
    
    public ActionQuit()
    {
      super("Exit", "Exit Program", "exit", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      if (m_BayesNet.isChanged()) {
        int result = JOptionPane.showConfirmDialog(null, "Network changed. Do you want to save it?", "Save before closing?", 1);
        if (result == 2) {
          return;
        }
        if ((result == 0) && 
          (!saveAs())) {
          return;
        }
      }
      
      System.exit(0);
    }
  }
  
  class ActionHelp extends GUI.MyAction
  {
    private static final long serialVersionUID = -20389110859354L;
    
    public ActionHelp() {
      super("Help", "Bayesian Network Workbench Help", "help", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      JOptionPane.showMessageDialog(null, "See Weka Homepage\nhttp://www.cs.waikato.ac.nz/ml", "Help Message", -1);
    }
  }
  
  class ActionAbout extends GUI.MyAction
  {
    private static final long serialVersionUID = -20389110859353L;
    
    public ActionAbout()
    {
      super("About", "Help about", "about", "");
    }
    
    public void actionPerformed(ActionEvent ae) {
      JOptionPane.showMessageDialog(null, "Bayesian Network Workbench\nPart of Weka\n2007", "About Message", -1);
    }
  }
  
  class ActionZoomIn extends GUI.MyAction
  {
    private static final long serialVersionUID = -2038911085935515L;
    
    public ActionZoomIn()
    {
      super("Zoom in", "Zoom in", "zoomin", "+");
    }
    
    public void actionPerformed(ActionEvent ae) {
      int i = 0;int s = (int)(m_fScale * 100.0D);
      if (s < 300) {
        i = s / 25;
      } else if (s < 700) {
        i = 6 + s / 50;
      } else {
        i = 13 + s / 100;
      }
      if (s >= 999) {
        setEnabled(false);
        return; }
      if (s >= 10) {
        if (i >= 22) {
          setEnabled(false);
        }
        if ((s == 10) && (!a_zoomout.isEnabled())) {
          a_zoomout.setEnabled(true);
        }
        m_jTfZoom.setText(m_nZoomPercents[(i + 1)] + "%");
        m_fScale = (m_nZoomPercents[(i + 1)] / 100.0D);
      } else {
        if (!a_zoomout.isEnabled())
          a_zoomout.setEnabled(true);
        m_jTfZoom.setText(m_nZoomPercents[0] + "%");
        m_fScale = (m_nZoomPercents[0] / 100.0D);
      }
      setAppropriateSize();
      m_GraphPanel.repaint();
      m_GraphPanel.invalidate();
      m_jScrollPane.revalidate();
      m_jStatusBar.setText("Zooming in");
    }
  }
  
  class ActionZoomOut extends GUI.MyAction
  {
    private static final long serialVersionUID = -203891108593551L;
    
    public ActionZoomOut() {
      super("Zoom out", "Zoom out", "zoomout", "-");
    }
    
    public void actionPerformed(ActionEvent ae) {
      int i = 0;int s = (int)(m_fScale * 100.0D);
      if (s < 300) {
        i = (int)Math.ceil(s / 25.0D);
      } else if (s < 700) {
        i = 6 + (int)Math.ceil(s / 50.0D);
      } else {
        i = 13 + (int)Math.ceil(s / 100.0D);
      }
      if (s <= 10) {
        setEnabled(false);
      } else if (s < 999) {
        if (i <= 1) {
          setEnabled(false);
        }
        m_jTfZoom.setText(m_nZoomPercents[(i - 1)] + "%");
        m_fScale = (m_nZoomPercents[(i - 1)] / 100.0D);
      } else {
        if (!a_zoomin.isEnabled())
          a_zoomin.setEnabled(true);
        m_jTfZoom.setText(m_nZoomPercents[22] + "%");
        m_fScale = (m_nZoomPercents[22] / 100.0D);
      }
      setAppropriateSize();
      m_GraphPanel.repaint();
      m_GraphPanel.invalidate();
      m_jScrollPane.revalidate();
      m_jStatusBar.setText("Zooming out");
    }
  }
  
  class ActionLayout extends GUI.MyAction
  {
    private static final long serialVersionUID = -203891108593551L;
    
    public ActionLayout() {
      super("Layout", "Layout Graph", "layout", "ctrl L");
    }
    
    JDialog dlg = null;
    
    public void actionPerformed(ActionEvent ae) {
      if (dlg == null) {
        dlg = new JDialog();
        dlg.setTitle("Graph Layout Options");
        final JCheckBox jCbCustomNodeSize = new JCheckBox("Custom Node Size");
        final JLabel jLbNodeWidth = new JLabel("Width");
        final JLabel jLbNodeHeight = new JLabel("Height");
        
        m_jTfNodeWidth.setHorizontalAlignment(0);
        m_jTfNodeWidth.setText("" + m_nNodeWidth);
        m_jTfNodeHeight.setHorizontalAlignment(0);
        m_jTfNodeHeight.setText("" + m_nNodeHeight);
        jLbNodeWidth.setEnabled(false);
        m_jTfNodeWidth.setEnabled(false);
        jLbNodeHeight.setEnabled(false);
        m_jTfNodeHeight.setEnabled(false);
        
        jCbCustomNodeSize.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            if (((JCheckBox)ae.getSource()).isSelected()) {
              jLbNodeWidth.setEnabled(true);
              m_jTfNodeWidth.setEnabled(true);
              jLbNodeHeight.setEnabled(true);
              m_jTfNodeHeight.setEnabled(true);
            } else {
              jLbNodeWidth.setEnabled(false);
              m_jTfNodeWidth.setEnabled(false);
              jLbNodeHeight.setEnabled(false);
              m_jTfNodeHeight.setEnabled(false);
              setAppropriateSize();
              setAppropriateNodeSize();
            }
            
          }
        });
        JButton jBtLayout = new JButton("Layout Graph");
        jBtLayout.setMnemonic('L');
        
        jBtLayout.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ae)
          {
            if (jCbCustomNodeSize.isSelected()) {
              int tmpW;
              try { tmpW = Integer.parseInt(m_jTfNodeWidth.getText());
              } catch (NumberFormatException ne) {
                JOptionPane.showMessageDialog(getParent(), "Invalid integer entered for node width.", "Error", 0);
                
                tmpW = m_nNodeWidth;
                m_jTfNodeWidth.setText("" + m_nNodeWidth);
              }
              int tmpH;
              try {
                tmpH = Integer.parseInt(m_jTfNodeHeight.getText());
              } catch (NumberFormatException ne) {
                JOptionPane.showMessageDialog(getParent(), "Invalid integer entered for node height.", "Error", 0);
                
                tmpH = m_nNodeHeight;
                m_jTfNodeWidth.setText("" + m_nNodeHeight);
              }
              
              if ((tmpW != m_nNodeWidth) || (tmpH != m_nNodeHeight)) {
                m_nNodeWidth = tmpW;
                m_nPaddedNodeWidth = (m_nNodeWidth + 10);
                m_nNodeHeight = tmpH;
              }
            }
            

            dlg.setVisible(false);
            updateStatus();
            layoutGraph();
            m_jStatusBar.setText("Laying out Bayes net");
          }
          
        });
        JButton jBtCancel = new JButton("Cancel");
        jBtCancel.setMnemonic('C');
        jBtCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            dlg.setVisible(false);
          }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        dlg.setLayout(new GridBagLayout());
        

        Container c = new Container();
        c.setLayout(new GridBagLayout());
        
        gridwidth = 1;
        insets = new Insets(8, 0, 0, 0);
        anchor = 18;
        gridwidth = 0;
        c.add(jCbCustomNodeSize, gbc);
        gridwidth = -1;
        c.add(jLbNodeWidth, gbc);
        gridwidth = 0;
        c.add(m_jTfNodeWidth, gbc);
        gridwidth = -1;
        c.add(jLbNodeHeight, gbc);
        gridwidth = 0;
        c.add(m_jTfNodeHeight, gbc);
        fill = 2;
        dlg.add(c, gbc);
        dlg.add(jBtLayout);
        gridwidth = 0;
        dlg.add(jBtCancel);
      }
      dlg.setLocation(100, 100);
      dlg.setVisible(true);
      dlg.setSize(dlg.getPreferredSize());
      dlg.setVisible(false);
      dlg.setVisible(true);
      dlg.repaint();
    }
  }
  




  public GUI()
  {
    m_GraphPanel = new GraphPanel();
    m_jScrollPane = new JScrollPane(m_GraphPanel);
    



    m_jTfZoom = new JTextField("100%");
    m_jTfZoom.setMinimumSize(m_jTfZoom.getPreferredSize());
    m_jTfZoom.setHorizontalAlignment(0);
    m_jTfZoom.setToolTipText("Zoom");
    
    m_jTfZoom.addActionListener(new ActionListener() {
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
            m_fScale = (i / 100.0D);
          }
          jt.setText((int)(m_fScale * 100.0D) + "%");
          if (m_fScale > 0.1D) {
            if (!a_zoomout.isEnabled())
              a_zoomout.setEnabled(true);
          } else
            a_zoomout.setEnabled(false);
          if (m_fScale < 9.99D) {
            if (!a_zoomin.isEnabled())
              a_zoomin.setEnabled(true);
          } else
            a_zoomin.setEnabled(false);
          setAppropriateSize();
          
          m_GraphPanel.repaint();
          m_GraphPanel.invalidate();
          m_jScrollPane.revalidate();
        } catch (NumberFormatException ne) {
          JOptionPane.showMessageDialog(getParent(), "Invalid integer entered for zoom.", "Error", 0);
          
          jt.setText(m_fScale * 100.0D + "%");
        }
        
      }
    });
    GridBagConstraints gbc = new GridBagConstraints();
    
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("ExtraControls"), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    
    p.setPreferredSize(new Dimension(0, 0));
    
    m_jTbTools = new JToolBar();
    m_jTbTools.setFloatable(false);
    m_jTbTools.setLayout(new GridBagLayout());
    anchor = 18;
    gridwidth = 0;
    insets = new Insets(0, 0, 0, 0);
    m_jTbTools.add(p, gbc);
    gridwidth = 1;
    

    m_jTbTools.add(a_new);
    m_jTbTools.add(a_save);
    m_jTbTools.add(a_load);
    m_jTbTools.addSeparator(new Dimension(2, 2));
    m_jTbTools.add(a_cutnode);
    m_jTbTools.add(a_copynode);
    m_jTbTools.add(a_pastenode);
    m_jTbTools.addSeparator(new Dimension(2, 2));
    m_jTbTools.add(a_undo);
    m_jTbTools.add(a_redo);
    m_jTbTools.addSeparator(new Dimension(2, 2));
    m_jTbTools.add(a_alignleft);
    m_jTbTools.add(a_alignright);
    m_jTbTools.add(a_aligntop);
    m_jTbTools.add(a_alignbottom);
    m_jTbTools.add(a_centerhorizontal);
    m_jTbTools.add(a_centervertical);
    m_jTbTools.add(a_spacehorizontal);
    m_jTbTools.add(a_spacevertical);
    

    m_jTbTools.addSeparator(new Dimension(2, 2));
    m_jTbTools.add(a_zoomin);
    
    fill = 3;
    weighty = 1.0D;
    JPanel p2 = new JPanel(new BorderLayout());
    p2.setPreferredSize(m_jTfZoom.getPreferredSize());
    p2.setMinimumSize(m_jTfZoom.getPreferredSize());
    p2.add(m_jTfZoom, "Center");
    m_jTbTools.add(p2, gbc);
    weighty = 0.0D;
    fill = 0;
    
    m_jTbTools.add(a_zoomout);
    m_jTbTools.addSeparator(new Dimension(2, 2));
    

    m_jTbTools.add(a_layout);
    m_jTbTools.addSeparator(new Dimension(4, 2));
    weightx = 1.0D;
    fill = 1;
    
    m_jStatusBar = new JLabel("Status bar");
    
    setLayout(new BorderLayout());
    add(m_jTbTools, "North");
    add(m_jScrollPane, "Center");
    add(m_jStatusBar, "South");
    
    updateStatus();
    a_datagenerator.setEnabled(false);
    
    makeMenuBar();
  }
  




  public JMenuBar getMenuBar()
  {
    return m_menuBar;
  }
  
  private void makeMenuBar() {
    m_menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    
    m_menuBar.add(fileMenu);
    fileMenu.add(a_new);
    fileMenu.add(a_load);
    fileMenu.add(a_save);
    fileMenu.add(a_saveas);
    fileMenu.addSeparator();
    fileMenu.add(a_print);
    fileMenu.add(a_export);
    fileMenu.addSeparator();
    fileMenu.add(a_quit);
    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic('E');
    m_menuBar.add(editMenu);
    editMenu.add(a_undo);
    editMenu.add(a_redo);
    editMenu.addSeparator();
    editMenu.add(a_selectall);
    editMenu.add(a_delnode);
    editMenu.add(a_cutnode);
    editMenu.add(a_copynode);
    editMenu.add(a_pastenode);
    editMenu.addSeparator();
    editMenu.add(a_addnode);
    editMenu.add(a_addarc);
    editMenu.add(a_delarc);
    editMenu.addSeparator();
    editMenu.add(a_alignleft);
    editMenu.add(a_alignright);
    editMenu.add(a_aligntop);
    editMenu.add(a_alignbottom);
    editMenu.add(a_centerhorizontal);
    editMenu.add(a_centervertical);
    editMenu.add(a_spacehorizontal);
    editMenu.add(a_spacevertical);
    
    JMenu toolMenu = new JMenu("Tools");
    toolMenu.setMnemonic('T');
    toolMenu.add(a_networkgenerator);
    toolMenu.add(a_datagenerator);
    toolMenu.add(a_datasetter);
    toolMenu.add(a_learn);
    toolMenu.add(a_learnCPT);
    toolMenu.addSeparator();
    toolMenu.add(a_layout);
    toolMenu.addSeparator();
    final JCheckBoxMenuItem viewMargins = new JCheckBoxMenuItem("Show Margins", false);
    viewMargins.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        boolean bPrev = m_bViewMargins;
        m_bViewMargins = viewMargins.getState();
        if ((!bPrev) && (viewMargins.getState() == true)) {
          updateStatus();
        }
        repaint();
      }
    });
    toolMenu.add(viewMargins);
    final JCheckBoxMenuItem viewCliques = new JCheckBoxMenuItem("Show Cliques", false);
    viewCliques.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        boolean bPrev = m_bViewCliques;
        m_bViewCliques = viewCliques.getState();
        if ((!bPrev) && (viewCliques.getState() == true)) {
          updateStatus();
        }
        repaint();
      }
    });
    toolMenu.add(viewCliques);
    
    m_menuBar.add(toolMenu);
    JMenu viewMenu = new JMenu("View");
    viewMenu.setMnemonic('V');
    m_menuBar.add(viewMenu);
    viewMenu.add(a_zoomin);
    viewMenu.add(a_zoomout);
    viewMenu.addSeparator();
    viewMenu.add(a_viewtoolbar);
    viewMenu.add(a_viewstatusbar);
    
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic('H');
    m_menuBar.add(helpMenu);
    helpMenu.add(a_help);
    helpMenu.add(a_about);
  }
  





  protected void setAppropriateNodeSize()
  {
    FontMetrics fm = getFontMetrics(getFont());
    int nMaxStringWidth = 50;
    if (nMaxStringWidth == 0)
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        int strWidth = fm.stringWidth(m_BayesNet.getNodeName(iNode));
        if (strWidth > nMaxStringWidth)
          nMaxStringWidth = strWidth;
      }
    m_nNodeWidth = (nMaxStringWidth + 4);
    m_nPaddedNodeWidth = (m_nNodeWidth + 10);
    m_jTfNodeWidth.setText("" + m_nNodeWidth);
    
    m_nNodeHeight = (2 * fm.getHeight());
    m_jTfNodeHeight.setText("" + m_nNodeHeight);
  }
  



  public void setAppropriateSize()
  {
    int maxX = 0;int maxY = 0;
    
    m_GraphPanel.setScale(m_fScale, m_fScale);
    
    for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
      int nPosX = m_BayesNet.getPositionX(iNode);
      int nPosY = m_BayesNet.getPositionY(iNode);
      if (maxX < nPosX)
        maxX = nPosX + 100;
      if (maxY < nPosY)
        maxY = nPosY;
    }
    m_GraphPanel.setPreferredSize(new Dimension((int)((maxX + m_nPaddedNodeWidth + 2) * m_fScale), (int)((maxY + m_nNodeHeight + 2) * m_fScale)));
    
    m_GraphPanel.revalidate();
  }
  






  public void layoutCompleted(LayoutCompleteEvent le)
  {
    LayoutEngine layoutEngine = m_layoutEngine;
    FastVector nPosX = new FastVector(m_BayesNet.getNrOfNodes());
    FastVector nPosY = new FastVector(m_BayesNet.getNrOfNodes());
    for (int iNode = 0; iNode < layoutEngine.getNodes().size(); iNode++) {
      GraphNode gNode = (GraphNode)layoutEngine.getNodes().elementAt(iNode);
      if (nodeType == 3) {
        nPosX.addElement(Integer.valueOf(x));
        nPosY.addElement(Integer.valueOf(y));
      }
    }
    m_BayesNet.layoutGraph(nPosX, nPosY);
    m_jStatusBar.setText("Graph layed out");
    a_undo.setEnabled(true);
    a_redo.setEnabled(false);
    setAppropriateSize();
    m_GraphPanel.invalidate();
    m_jScrollPane.revalidate();
    m_GraphPanel.repaint();
  }
  




  public void readBIFFromFile(String sFileName)
    throws BIFFormatException, IOException
  {
    m_sFileName = sFileName;
    try
    {
      BIFReader bayesNet = new BIFReader();
      bayesNet.processFile(sFileName);
      m_BayesNet = new EditableBayesNet(bayesNet);
      updateStatus();
      a_datagenerator.setEnabled(m_BayesNet.getNrOfNodes() > 0);
      m_BayesNet.clearUndoStack();
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    
    setAppropriateNodeSize();
    setAppropriateSize();
  }
  


  void initFromArffFile(String sFileName)
  {
    try
    {
      Instances instances = new Instances(new FileReader(sFileName));
      m_BayesNet = new EditableBayesNet(instances);
      m_Instances = instances;
      a_learn.setEnabled(true);
      a_learnCPT.setEnabled(true);
      setAppropriateNodeSize();
      setAppropriateSize();
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
  }
  

  private class GraphPanel
    extends PrintablePanel
    implements Printable
  {
    private static final long serialVersionUID = -3562813603236753173L;
    
    static final int HIGHLIGHTED = 1;
    
    static final int NORMAL = 0;
    

    public GraphPanel()
    {
      addMouseListener(new GUI.GraphVisualizerMouseListener(GUI.this, null));
      addMouseMotionListener(new GUI.GraphVisualizerMouseMotionListener(GUI.this, null));
      setToolTipText("");
    }
    


    public String getToolTipText(MouseEvent me)
    {
      int y;
      

      int x = y = 0;
      
      Rectangle r = new Rectangle(0, 0, (int)(m_nPaddedNodeWidth * m_fScale), (int)(m_nNodeHeight * m_fScale));
      x += me.getX();
      y += me.getY();
      
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        x = ((int)(m_BayesNet.getPositionX(iNode) * m_fScale));
        y = ((int)(m_BayesNet.getPositionY(iNode) * m_fScale));
        if (r.contains(x, y)) {
          return m_BayesNet.getNodeName(iNode) + " (right click to manipulate this node)";
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
      g.scale(m_fScale, m_fScale);
      Rectangle r = g.getClipBounds();
      g.clearRect(x, y, width, height);
      
      if (m_bViewCliques) {
        m_nClique = 1;
        viewCliques(g, m_marginCalculator.m_root);
      }
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        drawNode(g, iNode, 0);
      }
      if ((!a_export.isExporting()) && (!a_print.isPrinting())) {
        m_Selection.draw(g);
      }
      if (m_nSelectedRect != null) {
        g.drawRect((int)(m_nSelectedRect.x / m_fScale), (int)(m_nSelectedRect.y / m_fScale), (int)(m_nSelectedRect.width / m_fScale), (int)(m_nSelectedRect.height / m_fScale));
      }
    }
    




    int m_nClique = 1;
    


    void viewCliques(Graphics g, MarginCalculator.JunctionTreeNode node)
    {
      int[] nodes = m_nNodes;
      g.setColor(new Color(m_nClique % 7 * 256 / 7, m_nClique % 2 * 256 / 2, m_nClique % 3 * 256 / 3));
      



      int dX = m_nPaddedNodeWidth / 2 + m_nClique;
      int dY = m_nNodeHeight / 2;
      int nPosX = 0;
      int nPosY = 0;
      String sStr = "";
      for (int j = 0; j < nodes.length; j++) {
        nPosX += m_BayesNet.getPositionX(nodes[j]);
        nPosY += m_BayesNet.getPositionY(nodes[j]);
        sStr = sStr + " " + nodes[j];
        for (int k = j + 1; k < nodes.length; k++) {
          g.drawLine(m_BayesNet.getPositionX(nodes[j]) + dX, m_BayesNet.getPositionY(nodes[j]) + dY, m_BayesNet.getPositionX(nodes[k]) + dX, m_BayesNet.getPositionY(nodes[k]) + dY);
        }
      }
      




      m_nClique += 1;
      nPosX /= nodes.length;
      nPosY /= nodes.length;
      g.drawString("Clique " + m_nClique + "(" + sStr + ")", nPosX, nPosY);
      for (int iChild = 0; iChild < m_children.size(); iChild++) {
        viewCliques(g, (MarginCalculator.JunctionTreeNode)m_children.elementAt(iChild));
      }
    }
    



    protected void drawNode(Graphics g, int iNode, int mode)
    {
      int nPosX = m_BayesNet.getPositionX(iNode);
      int nPosY = m_BayesNet.getPositionY(iNode);
      g.setColor(getBackground().darker().darker());
      FontMetrics fm = getFontMetrics(getFont());
      
      if (mode == 1) {
        g.setXORMode(Color.green);
      }
      g.fillOval(nPosX + m_nPaddedNodeWidth - m_nNodeWidth - (m_nPaddedNodeWidth - m_nNodeWidth) / 2, nPosY, m_nNodeWidth, m_nNodeHeight);
      
      g.setColor(Color.white);
      if (mode == 1) {
        g.setXORMode(Color.red);
      }
      



      if (fm.stringWidth(m_BayesNet.getNodeName(iNode)) <= m_nNodeWidth) {
        g.drawString(m_BayesNet.getNodeName(iNode), nPosX + m_nPaddedNodeWidth / 2 - fm.stringWidth(m_BayesNet.getNodeName(iNode)) / 2, nPosY + m_nNodeHeight / 2 + fm.getHeight() / 2 - 2);

      }
      else if (fm.stringWidth("" + iNode) <= m_nNodeWidth) {
        g.drawString("" + iNode, nPosX + m_nPaddedNodeWidth / 2 - fm.stringWidth("" + iNode) / 2, nPosY + m_nNodeHeight / 2 + fm.getHeight() / 2 - 2);
      }
      

      if (mode == 1) {
        g.setXORMode(Color.green);
      }
      
      if (m_bViewMargins) {
        if (m_BayesNet.getEvidence(iNode) < 0) {
          g.setColor(new Color(0, 128, 0));
        } else {
          g.setColor(new Color(128, 0, 0));
        }
        double[] P = m_BayesNet.getMargin(iNode);
        for (int iValue = 0; iValue < P.length; iValue++) {
          String sP = P[iValue] + "";
          if (sP.charAt(0) == '0') {
            sP = sP.substring(1);
          }
          if (sP.length() > 5) {
            sP = sP.substring(1, 5);
          }
          g.fillRect(nPosX + m_nPaddedNodeWidth, nPosY + iValue * 10 + 2, (int)(P[iValue] * 100.0D), 8);
          g.drawString(m_BayesNet.getNodeValue(iNode, iValue) + " " + sP, nPosX + m_nPaddedNodeWidth + (int)(P[iValue] * 100.0D), nPosY + iValue * 10 + 10);
        }
      }
      

      if (m_bViewCliques) {
        return;
      }
      g.setColor(Color.black);
      
      for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(iNode); iParent++) {
        int nParent = m_BayesNet.getParent(iNode, iParent);
        int nPosX1 = nPosX + m_nPaddedNodeWidth / 2;
        int nPosY1 = nPosY + m_nNodeHeight;
        int nPosX2 = m_BayesNet.getPositionX(nParent);
        int nPosY2 = m_BayesNet.getPositionY(nParent);
        int nPosX2b = nPosX2 + m_nPaddedNodeWidth / 2;
        int nPosY2b = nPosY2;
        
        double phi = Math.atan2((nPosX2b - nPosX1 + 0.0D) * m_nNodeHeight, (nPosY2b - nPosY1 + 0.0D) * m_nNodeWidth);
        nPosX1 = (int)(nPosX + m_nPaddedNodeWidth / 2 + Math.sin(phi) * m_nNodeWidth / 2.0D);
        nPosY1 = (int)(nPosY + m_nNodeHeight / 2 + Math.cos(phi) * m_nNodeHeight / 2.0D);
        nPosX2b = (int)(nPosX2 + m_nPaddedNodeWidth / 2 - Math.sin(phi) * m_nNodeWidth / 2.0D);
        nPosY2b = (int)(nPosY2 + m_nNodeHeight / 2 - Math.cos(phi) * m_nNodeHeight / 2.0D);
        drawArrow(g, nPosX2b, nPosY2b, nPosX1, nPosY1);
      }
      if (mode == 1) {
        FastVector children = m_BayesNet.getChildren(iNode);
        for (int iChild = 0; iChild < children.size(); iChild++) {
          int nChild = ((Integer)children.elementAt(iChild)).intValue();
          int nPosX1 = nPosX + m_nPaddedNodeWidth / 2;
          int nPosY1 = nPosY;
          int nPosX2 = m_BayesNet.getPositionX(nChild);
          int nPosY2 = m_BayesNet.getPositionY(nChild);
          int nPosX2b = nPosX2 + m_nPaddedNodeWidth / 2;
          int nPosY2b = nPosY2 + m_nNodeHeight;
          
          double phi = Math.atan2((nPosX2b - nPosX1 + 0.0D) * m_nNodeHeight, (nPosY2b - nPosY1 + 0.0D) * m_nNodeWidth);
          nPosX1 = (int)(nPosX + m_nPaddedNodeWidth / 2 + Math.sin(phi) * m_nNodeWidth / 2.0D);
          nPosY1 = (int)(nPosY + m_nNodeHeight / 2 + Math.cos(phi) * m_nNodeHeight / 2.0D);
          nPosX2b = (int)(nPosX2 + m_nPaddedNodeWidth / 2 - Math.sin(phi) * m_nNodeWidth / 2.0D);
          nPosY2b = (int)(nPosY2 + m_nNodeHeight / 2 - Math.cos(phi) * m_nNodeHeight / 2.0D);
          drawArrow(g, nPosX1, nPosY1, nPosX2b, nPosY2b);
        }
      }
    }
    







    protected void drawArrow(Graphics g, int nPosX1, int nPosY1, int nPosX2, int nPosY2)
    {
      g.drawLine(nPosX1, nPosY1, nPosX2, nPosY2);
      
      if (nPosX1 == nPosX2) {
        if (nPosY1 < nPosY2) {
          g.drawLine(nPosX2, nPosY2, nPosX2 + 4, nPosY2 - 8);
          g.drawLine(nPosX2, nPosY2, nPosX2 - 4, nPosY2 - 8);
        } else {
          g.drawLine(nPosX2, nPosY2, nPosX2 + 4, nPosY2 + 8);
          g.drawLine(nPosX2, nPosY2, nPosX2 - 4, nPosY2 + 8);
        }
      }
      else
      {
        double hyp = 0.0D;double base = 0.0D;double perp = 0.0D;
        int nPosX3 = 0;int nPosY3 = 0;
        double theta;
        double theta; if (nPosX2 < nPosX1) {
          base = nPosX1 - nPosX2;
          hyp = Math.sqrt((nPosX2 - nPosX1) * (nPosX2 - nPosX1) + (nPosY2 - nPosY1) * (nPosY2 - nPosY1));
          theta = Math.acos(base / hyp);
        } else {
          base = nPosX1 - nPosX2;
          hyp = Math.sqrt((nPosX2 - nPosX1) * (nPosX2 - nPosX1) + (nPosY2 - nPosY1) * (nPosY2 - nPosY1));
          theta = Math.acos(base / hyp);
        }
        double beta = 0.5235987755982988D;
        
        hyp = 8.0D;
        base = Math.cos(theta - beta) * hyp;
        perp = Math.sin(theta - beta) * hyp;
        
        nPosX3 = (int)(nPosX2 + base);
        if (nPosY1 < nPosY2) {
          nPosY3 = (int)(nPosY2 - perp);
        } else {
          nPosY3 = (int)(nPosY2 + perp);
        }
        g.drawLine(nPosX2, nPosY2, nPosX3, nPosY3);
        
        base = Math.cos(theta + beta) * hyp;
        perp = Math.sin(theta + beta) * hyp;
        
        nPosX3 = (int)(nPosX2 + base);
        if (nPosY1 < nPosY2) {
          nPosY3 = (int)(nPosY2 - perp);
        } else
          nPosY3 = (int)(nPosY2 + perp);
        g.drawLine(nPosX2, nPosY2, nPosX3, nPosY3);
      }
    }
    


    public void highLight(int iNode)
    {
      Graphics2D g = (Graphics2D)getGraphics();
      RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      g.setRenderingHints(rh);
      g.setPaintMode();
      g.scale(m_fScale, m_fScale);
      drawNode(g, iNode, 1);
    }
    


    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
    {
      if (pageIndex > 0) {
        return 1;
      }
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      double fHeight = pageFormat.getImageableHeight();
      double fWidth = pageFormat.getImageableWidth();
      int xMax = 1;
      int yMax = 1;
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        if (xMax < m_BayesNet.getPositionX(iNode)) {
          xMax = m_BayesNet.getPositionX(iNode);
        }
        if (yMax < m_BayesNet.getPositionY(iNode)) {
          yMax = m_BayesNet.getPositionY(iNode);
        }
      }
      double fCurrentScale = m_fScale;
      xMax += m_nPaddedNodeWidth + 100;
      if (fWidth / xMax < fHeight / yMax) {
        m_fScale = (fWidth / xMax);
      } else {
        m_fScale = (fHeight / yMax);
      }
      

      paint(g2d);
      m_fScale = fCurrentScale;
      
      return 0;
    }
  }
  


  private class GraphVisualizerTableModel
    extends AbstractTableModel
  {
    private static final long serialVersionUID = -4789813491347366596L;
    

    final String[] m_sColumnNames;
    
    final double[][] m_fProbs;
    
    int m_iNode;
    

    public GraphVisualizerTableModel(int iNode)
    {
      m_iNode = iNode;
      double[][] probs = m_BayesNet.getDistribution(iNode);
      m_fProbs = new double[probs.length][probs[0].length];
      for (int i = 0; i < probs.length; i++) {
        for (int j = 0; j < probs[0].length; j++) {
          m_fProbs[i][j] = probs[i][j];
        }
      }
      m_sColumnNames = m_BayesNet.getValues(iNode);
    }
    

    public void randomize()
    {
      int nProbs = m_fProbs[0].length;
      Random random = new Random();
      for (int i = 0; i < m_fProbs.length; i++)
      {
        for (int j = 0; j < nProbs - 1; j++) {
          m_fProbs[i][j] = random.nextDouble();
        }
        
        for (int j = 0; j < nProbs - 1; j++) {
          for (int k = j + 1; k < nProbs - 1; k++) {
            if (m_fProbs[i][j] > m_fProbs[i][k]) {
              double h = m_fProbs[i][j];
              m_fProbs[i][j] = m_fProbs[i][k];
              m_fProbs[i][k] = h;
            }
          }
        }
        double sum = m_fProbs[i][0];
        for (int j = 1; j < nProbs - 1; j++) {
          m_fProbs[i][j] -= sum;
          sum += m_fProbs[i][j];
        }
        m_fProbs[i][(nProbs - 1)] = (1.0D - sum);
      }
    }
    
    public void setData() {}
    
    public int getColumnCount()
    {
      return m_sColumnNames.length;
    }
    
    public int getRowCount()
    {
      return m_fProbs.length;
    }
    


    public String getColumnName(int iCol)
    {
      return m_sColumnNames[iCol];
    }
    


    public Object getValueAt(int iRow, int iCol)
    {
      return new Double(m_fProbs[iRow][iCol]);
    }
    








    public void setValueAt(Object oProb, int iRow, int iCol)
    {
      Double fProb = (Double)oProb;
      if ((fProb.doubleValue() < 0.0D) || (fProb.doubleValue() > 1.0D)) {
        return;
      }
      m_fProbs[iRow][iCol] = fProb.doubleValue();
      double sum = 0.0D;
      for (int i = 0; i < m_fProbs[iRow].length; i++) {
        sum += m_fProbs[iRow][i];
      }
      
      if (sum > 1.0D)
      {
        int i = m_fProbs[iRow].length - 1;
        while (sum > 1.0D) {
          if (i != iCol) {
            if (m_fProbs[iRow][i] > sum - 1.0D) {
              m_fProbs[iRow][i] -= sum - 1.0D;
              sum = 1.0D;
            } else {
              sum -= m_fProbs[iRow][i];
              m_fProbs[iRow][i] = 0.0D;
            }
          }
          i--;
        }
      }
      else {
        int i = m_fProbs[iRow].length - 1;
        while (sum < 1.0D) {
          if (i != iCol) {
            m_fProbs[iRow][i] += 1.0D - sum;
            sum = 1.0D;
          }
          i--;
        }
      }
      
      validate();
    }
    



    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }
    


    public boolean isCellEditable(int row, int col)
    {
      return true;
    }
  }
  




  private class GraphVisualizerMouseListener
    extends MouseAdapter
  {
    private GraphVisualizerMouseListener() {}
    



    public void mouseClicked(MouseEvent me)
    {
      Rectangle r = new Rectangle(0, 0, (int)(m_nPaddedNodeWidth * m_fScale), (int)(m_nNodeHeight * m_fScale));
      int x = me.getX();
      int y = me.getY();
      
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        x = ((int)(m_BayesNet.getPositionX(iNode) * m_fScale));
        y = ((int)(m_BayesNet.getPositionY(iNode) * m_fScale));
        if (r.contains(x, y)) {
          m_nCurrentNode = iNode;
          if (me.getButton() == 3) {
            handleRightNodeClick(me);
          }
          if (me.getButton() == 1) {
            if ((me.getModifiersEx() & 0x80) != 0) {
              m_Selection.toggleSelection(m_nCurrentNode);
            } else if ((me.getModifiersEx() & 0x40) != 0) {
              m_Selection.addToSelection(m_nCurrentNode);
            } else {
              m_Selection.clear();
              m_Selection.addToSelection(m_nCurrentNode);
            }
            repaint();
          }
          return;
        }
      }
      if (me.getButton() == 3) {
        handleRightClick(me, (int)(x / m_fScale), (int)(y / m_fScale));
      }
    }
    



    public void mouseReleased(MouseEvent me)
    {
      if (m_nSelectedRect != null) {
        if ((me.getModifiersEx() & 0x80) != 0) {
          m_Selection.toggleSelection(m_nSelectedRect);
        } else if ((me.getModifiersEx() & 0x40) != 0) {
          m_Selection.addToSelection(m_nSelectedRect);
        } else {
          m_Selection.clear();
          m_Selection.addToSelection(m_nSelectedRect);
        }
        m_nSelectedRect = null;
        repaint();
      }
    }
    

    int m_nPosY = 0; int m_nPosX = 0;
    


    void handleRightClick(MouseEvent me, int nPosX, int nPosY)
    {
      ActionListener act = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if (ae.getActionCommand().equals("Add node")) {
            a_addnode.addNode(m_nPosX, m_nPosY);
            return;
          }
          repaint();
        }
      };
      JPopupMenu popupMenu = new JPopupMenu("Choose a value");
      
      JMenuItem addNodeItem = new JMenuItem("Add node");
      addNodeItem.addActionListener(act);
      popupMenu.add(addNodeItem);
      
      FastVector selected = m_Selection.getSelected();
      JMenu addArcMenu = new JMenu("Add parent");
      popupMenu.add(addArcMenu);
      if (selected.size() == 0) {
        addArcMenu.setEnabled(false);
      } else {
        int nNodes = m_BayesNet.getNrOfNodes();
        boolean[] isNotAllowedAsParent = new boolean[nNodes];
        
        for (int iNode = 0; iNode < selected.size(); iNode++) {
          isNotAllowedAsParent[((Integer)selected.elementAt(iNode)).intValue()] = true;
        }
        
        for (int i = 0; i < nNodes; i++) {
          for (int iNode = 0; iNode < nNodes; iNode++) {
            for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(iNode); iParent++) {
              if (isNotAllowedAsParent[m_BayesNet.getParent(iNode, iParent)] != 0) {
                isNotAllowedAsParent[iNode] = true;
              }
            }
          }
        }
        
        for (int iNode = 0; iNode < selected.size(); iNode++) {
          int nNode = ((Integer)selected.elementAt(iNode)).intValue();
          for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(nNode); iParent++) {
            isNotAllowedAsParent[m_BayesNet.getParent(nNode, iParent)] = true;
          }
        }
        ActionListener addParentAction = new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            try {
              m_BayesNet.addArc(ae.getActionCommand(), m_Selection.getSelected());
              m_jStatusBar.setText(m_BayesNet.lastActionMsg());
              updateStatus();
            } catch (Exception e) {
              e.printStackTrace();
            }
            
          }
        };
        int nCandidates = 0;
        for (int i = 0; i < nNodes; i++) {
          if (isNotAllowedAsParent[i] == 0) {
            JMenuItem item = new JMenuItem(m_BayesNet.getNodeName(i));
            item.addActionListener(addParentAction);
            addArcMenu.add(item);
            nCandidates++;
          }
        }
        if (nCandidates == 0) {
          addArcMenu.setEnabled(false);
        }
      }
      m_nPosX = nPosX;
      m_nPosY = nPosY;
      popupMenu.setLocation(me.getX(), me.getY());
      popupMenu.show(m_GraphPanel, me.getX(), me.getY());
    }
    

    void handleRightNodeClick(MouseEvent me)
    {
      m_Selection.clear();
      repaint();
      ActionListener renameValueAction = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          renameValue(m_nCurrentNode, ae.getActionCommand());
        }
      };
      ActionListener delValueAction = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          delValue(m_nCurrentNode, ae.getActionCommand());
        }
      };
      ActionListener addParentAction = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            m_BayesNet.addArc(ae.getActionCommand(), m_BayesNet.getNodeName(m_nCurrentNode));
            m_jStatusBar.setText(m_BayesNet.lastActionMsg());
            updateStatus();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      ActionListener delParentAction = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          deleteArc(m_nCurrentNode, ae.getActionCommand());
        }
      };
      ActionListener delChildAction = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          deleteArc(ae.getActionCommand(), m_nCurrentNode);
        }
      };
      ActionListener setAvidenceAction = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            String[] outcomes = m_BayesNet.getValues(m_nCurrentNode);
            int iValue = 0;
            while ((iValue < outcomes.length) && (!outcomes[iValue].equals(ae.getActionCommand()))) {
              iValue++;
            }
            
            if (iValue == outcomes.length) {
              iValue = -1;
            }
            if (iValue < outcomes.length) {
              m_jStatusBar.setText("Set evidence for " + m_BayesNet.getNodeName(m_nCurrentNode));
              if ((m_BayesNet.getEvidence(m_nCurrentNode) < 0) && (iValue >= 0)) {
                m_BayesNet.setEvidence(m_nCurrentNode, iValue);
                m_marginCalculatorWithEvidence.setEvidence(m_nCurrentNode, iValue);
              } else {
                m_BayesNet.setEvidence(m_nCurrentNode, iValue);
                SerializedObject so = new SerializedObject(m_marginCalculator);
                m_marginCalculatorWithEvidence = ((MarginCalculator)so.getObject());
                for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
                  if (m_BayesNet.getEvidence(iNode) >= 0) {
                    m_marginCalculatorWithEvidence.setEvidence(iNode, m_BayesNet.getEvidence(iNode));
                  }
                }
              }
              for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
                m_BayesNet.setMargin(iNode, m_marginCalculatorWithEvidence.getMargin(iNode));
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          repaint();
        }
        
      };
      ActionListener act = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if (ae.getActionCommand().equals("Rename")) {
            renameNode(m_nCurrentNode);
            return;
          }
          if (ae.getActionCommand().equals("Add parent")) {
            addArcInto(m_nCurrentNode);
            return;
          }
          if (ae.getActionCommand().equals("Add value")) {
            addValue();
            return;
          }
          if (ae.getActionCommand().equals("Delete node")) {
            deleteNode(m_nCurrentNode);
            return;
          }
          if (ae.getActionCommand().equals("Edit CPT")) {
            editCPT(m_nCurrentNode);
            return;
          }
          repaint();
        }
      };
      try {
        JPopupMenu popupMenu = new JPopupMenu("Choose a value");
        
        JMenu setEvidenceMenu = new JMenu("Set evidence");
        String[] outcomes = m_BayesNet.getValues(m_nCurrentNode);
        for (int iValue = 0; iValue < outcomes.length; iValue++) {
          JMenuItem item = new JMenuItem(outcomes[iValue]);
          item.addActionListener(setAvidenceAction);
          setEvidenceMenu.add(item);
        }
        setEvidenceMenu.addSeparator();
        JMenuItem item = new JMenuItem("Clear");
        item.addActionListener(setAvidenceAction);
        setEvidenceMenu.add(item);
        popupMenu.add(setEvidenceMenu);
        
        setEvidenceMenu.setEnabled(m_bViewMargins);
        
        popupMenu.addSeparator();
        
        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(act);
        popupMenu.add(renameItem);
        
        JMenuItem delNodeItem = new JMenuItem("Delete node");
        delNodeItem.addActionListener(act);
        popupMenu.add(delNodeItem);
        
        JMenuItem editCPTItem = new JMenuItem("Edit CPT");
        editCPTItem.addActionListener(act);
        popupMenu.add(editCPTItem);
        
        popupMenu.addSeparator();
        
        JMenu addArcMenu = new JMenu("Add parent");
        popupMenu.add(addArcMenu);
        int nNodes = m_BayesNet.getNrOfNodes();
        boolean[] isNotAllowedAsParent = new boolean[nNodes];
        
        isNotAllowedAsParent[m_nCurrentNode] = true;
        
        for (int i = 0; i < nNodes; i++) {
          for (int iNode = 0; iNode < nNodes; iNode++) {
            for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(iNode); iParent++) {
              if (isNotAllowedAsParent[m_BayesNet.getParent(iNode, iParent)] != 0) {
                isNotAllowedAsParent[iNode] = true;
              }
            }
          }
        }
        
        for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(m_nCurrentNode); iParent++) {
          isNotAllowedAsParent[m_BayesNet.getParent(m_nCurrentNode, iParent)] = true;
        }
        
        int nCandidates = 0;
        for (int i = 0; i < nNodes; i++) {
          if (isNotAllowedAsParent[i] == 0) {
            item = new JMenuItem(m_BayesNet.getNodeName(i));
            item.addActionListener(addParentAction);
            addArcMenu.add(item);
            nCandidates++;
          }
        }
        if (nCandidates == 0) {
          addArcMenu.setEnabled(false);
        }
        
        JMenu delArcMenu = new JMenu("Delete parent");
        popupMenu.add(delArcMenu);
        if (m_BayesNet.getNrOfParents(m_nCurrentNode) == 0) {
          delArcMenu.setEnabled(false);
        }
        for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(m_nCurrentNode); iParent++) {
          item = new JMenuItem(m_BayesNet.getNodeName(m_BayesNet.getParent(m_nCurrentNode, iParent)));
          item.addActionListener(delParentAction);
          delArcMenu.add(item);
        }
        JMenu delChildMenu = new JMenu("Delete child");
        popupMenu.add(delChildMenu);
        FastVector nChildren = m_BayesNet.getChildren(m_nCurrentNode);
        if (nChildren.size() == 0) {
          delChildMenu.setEnabled(false);
        }
        for (int iChild = 0; iChild < nChildren.size(); iChild++) {
          item = new JMenuItem(m_BayesNet.getNodeName(((Integer)nChildren.elementAt(iChild)).intValue()));
          item.addActionListener(delChildAction);
          delChildMenu.add(item);
        }
        
        popupMenu.addSeparator();
        
        JMenuItem addValueItem = new JMenuItem("Add value");
        addValueItem.addActionListener(act);
        popupMenu.add(addValueItem);
        
        JMenu renameValue = new JMenu("Rename value");
        popupMenu.add(renameValue);
        for (int iValue = 0; iValue < outcomes.length; iValue++) {
          item = new JMenuItem(outcomes[iValue]);
          item.addActionListener(renameValueAction);
          renameValue.add(item);
        }
        
        JMenu delValue = new JMenu("Delete value");
        popupMenu.add(delValue);
        if (m_BayesNet.getCardinality(m_nCurrentNode) <= 2) {
          delValue.setEnabled(false);
        }
        for (int iValue = 0; iValue < outcomes.length; iValue++) {
          JMenuItem delValueItem = new JMenuItem(outcomes[iValue]);
          delValueItem.addActionListener(delValueAction);
          delValue.add(delValueItem);
        }
        
        popupMenu.setLocation(me.getX(), me.getY());
        popupMenu.show(m_GraphPanel, me.getX(), me.getY());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  




  private class GraphVisualizerMouseMotionListener
    extends MouseMotionAdapter
  {
    int m_nLastNode = -1;
    int m_nPosX;
    int m_nPosY;
    
    private GraphVisualizerMouseMotionListener() {}
    
    int getGraphNode(MouseEvent me)
    {
      m_nPosX = (this.m_nPosY = 0);
      
      Rectangle r = new Rectangle(0, 0, (int)(m_nPaddedNodeWidth * m_fScale), (int)(m_nNodeHeight * m_fScale));
      m_nPosX += me.getX();
      m_nPosY += me.getY();
      
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        x = ((int)(m_BayesNet.getPositionX(iNode) * m_fScale));
        y = ((int)(m_BayesNet.getPositionY(iNode) * m_fScale));
        if (r.contains(m_nPosX, m_nPosY)) {
          return iNode;
        }
      }
      return -1;
    }
    



    public void mouseDragged(MouseEvent me)
    {
      if (m_nSelectedRect != null) {
        m_nSelectedRect.width = (getPointx - m_nSelectedRect.x);
        m_nSelectedRect.height = (getPointy - m_nSelectedRect.y);
        repaint();
        return;
      }
      int iNode = getGraphNode(me);
      if (iNode >= 0) {
        if (m_Selection.getSelected().size() > 0) {
          if (m_Selection.getSelected().contains(Integer.valueOf(iNode))) {
            m_BayesNet.setPosition(iNode, (int)(m_nPosX / m_fScale - m_nPaddedNodeWidth / 2), (int)(m_nPosY / m_fScale - m_nNodeHeight / 2), m_Selection.getSelected());
          }
          else {
            m_Selection.clear();
            m_BayesNet.setPosition(iNode, (int)(m_nPosX / m_fScale - m_nPaddedNodeWidth / 2), (int)(m_nPosY / m_fScale - m_nNodeHeight / 2));
          }
          
          repaint();
        } else {
          m_BayesNet.setPosition(iNode, (int)(m_nPosX / m_fScale - m_nPaddedNodeWidth / 2), (int)(m_nPosY / m_fScale - m_nNodeHeight / 2));
        }
        
        m_jStatusBar.setText(m_BayesNet.lastActionMsg());
        a_undo.setEnabled(true);
        a_redo.setEnabled(false);
        m_GraphPanel.highLight(iNode);
      }
      if (iNode < 0) {
        if (m_nLastNode >= 0) {
          m_GraphPanel.repaint();
          m_nLastNode = -1;
        } else {
          m_nSelectedRect = new Rectangle(getPointx, getPointy, 1, 1);
          m_GraphPanel.repaint();
        }
      }
    }
    



    public void mouseMoved(MouseEvent me)
    {
      int iNode = getGraphNode(me);
      if ((iNode >= 0) && 
        (iNode != m_nLastNode)) {
        m_GraphPanel.highLight(iNode);
        if (m_nLastNode >= 0) {
          m_GraphPanel.highLight(m_nLastNode);
        }
        m_nLastNode = iNode;
      }
      
      if ((iNode < 0) && (m_nLastNode >= 0)) {
        m_GraphPanel.repaint();
        m_nLastNode = -1;
      }
    }
  }
  


  void layoutGraph()
  {
    if (m_BayesNet.getNrOfNodes() == 0) {
      return;
    }
    try {
      FastVector m_nodes = new FastVector();
      FastVector m_edges = new FastVector();
      BIFParser bp = new BIFParser(m_BayesNet.toXMLBIF03(), m_nodes, m_edges);
      bp.parse();
      updateStatus();
      m_layoutEngine = new HierarchicalBCEngine(m_nodes, m_edges, m_nPaddedNodeWidth, m_nNodeHeight);
      m_layoutEngine.addLayoutCompleteEventListener(this);
      m_layoutEngine.layoutGraph();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  



  void updateStatus()
  {
    a_undo.setEnabled(m_BayesNet.canUndo());
    a_redo.setEnabled(m_BayesNet.canRedo());
    
    a_datagenerator.setEnabled(m_BayesNet.getNrOfNodes() > 0);
    
    if ((!m_bViewMargins) && (!m_bViewCliques)) {
      repaint();
      return;
    }
    try
    {
      m_marginCalculator = new MarginCalculator();
      m_marginCalculator.calcMargins(m_BayesNet);
      SerializedObject so = new SerializedObject(m_marginCalculator);
      m_marginCalculatorWithEvidence = ((MarginCalculator)so.getObject());
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        if (m_BayesNet.getEvidence(iNode) >= 0) {
          m_marginCalculatorWithEvidence.setEvidence(iNode, m_BayesNet.getEvidence(iNode));
        }
      }
      for (int iNode = 0; iNode < m_BayesNet.getNrOfNodes(); iNode++) {
        m_BayesNet.setMargin(iNode, m_marginCalculatorWithEvidence.getMargin(iNode));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    repaint();
  }
  





  void addArcInto(int iChild)
  {
    String sChild = m_BayesNet.getNodeName(iChild);
    try {
      int nNodes = m_BayesNet.getNrOfNodes();
      boolean[] isNotAllowedAsParent = new boolean[nNodes];
      
      isNotAllowedAsParent[iChild] = true;
      
      for (int i = 0; i < nNodes; i++) {
        for (int iNode = 0; iNode < nNodes; iNode++) {
          for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(iNode); iParent++) {
            if (isNotAllowedAsParent[m_BayesNet.getParent(iNode, iParent)] != 0) {
              isNotAllowedAsParent[iNode] = true;
            }
          }
        }
      }
      
      for (int iParent = 0; iParent < m_BayesNet.getNrOfParents(iChild); iParent++) {
        isNotAllowedAsParent[m_BayesNet.getParent(iChild, iParent)] = true;
      }
      
      int nCandidates = 0;
      for (int i = 0; i < nNodes; i++) {
        if (isNotAllowedAsParent[i] == 0) {
          nCandidates++;
        }
      }
      if (nCandidates == 0) {
        JOptionPane.showMessageDialog(null, "No potential parents available for this node (" + sChild + "). Choose another node as child node.");
        
        return;
      }
      String[] options = new String[nCandidates];
      int k = 0;
      for (int i = 0; i < nNodes; i++) {
        if (isNotAllowedAsParent[i] == 0) {
          options[(k++)] = m_BayesNet.getNodeName(i);
        }
      }
      String sParent = (String)JOptionPane.showInputDialog(null, "Select parent node for " + sChild, "Nodes", 0, null, options, options[0]);
      
      if ((sParent == null) || (sParent.equals(""))) {
        return;
      }
      
      m_BayesNet.addArc(sParent, sChild);
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      updateStatus();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  

  void deleteArc(int iChild, String sParent)
  {
    try
    {
      m_BayesNet.deleteArc(m_BayesNet.getNode(sParent), iChild);
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
    } catch (Exception e) {
      e.printStackTrace();
    }
    updateStatus();
  }
  

  void deleteArc(String sChild, int iParent)
  {
    try
    {
      m_BayesNet.deleteArc(iParent, m_BayesNet.getNode(sChild));
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
    } catch (Exception e) {
      e.printStackTrace();
    }
    updateStatus();
  }
  


  void deleteArc(String[] options)
  {
    String sResult = (String)JOptionPane.showInputDialog(null, "Select arc to delete", "Arcs", 0, null, options, options[0]);
    
    if ((sResult != null) && (!sResult.equals(""))) {
      int nPos = sResult.indexOf(" -> ");
      String sParent = sResult.substring(0, nPos);
      String sChild = sResult.substring(nPos + 4);
      try {
        m_BayesNet.deleteArc(sParent, sChild);
        m_jStatusBar.setText(m_BayesNet.lastActionMsg());
      } catch (Exception e) {
        e.printStackTrace();
      }
      updateStatus();
    }
  }
  


  void renameNode(int nTargetNode)
  {
    String sName = JOptionPane.showInputDialog(null, m_BayesNet.getNodeName(nTargetNode), "New name for node", 2);
    
    if ((sName == null) || (sName.equals(""))) {
      return;
    }
    try {
      while (m_BayesNet.getNode2(sName) >= 0) {
        sName = JOptionPane.showInputDialog(null, "Cannot rename to " + sName + ".\nNode with that name already exists.");
        
        if ((sName == null) || (sName.equals(""))) {
          return;
        }
      }
      m_BayesNet.setNodeName(nTargetNode, sName);
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
    } catch (Exception e) {
      e.printStackTrace();
    }
    repaint();
  }
  


  void renameValue(int nTargetNode, String sValue)
  {
    String sNewValue = JOptionPane.showInputDialog(null, "New name for value " + sValue, "Node " + m_BayesNet.getNodeName(nTargetNode), 2);
    
    if ((sNewValue == null) || (sNewValue.equals(""))) {
      return;
    }
    m_BayesNet.renameNodeValue(nTargetNode, sValue, sNewValue);
    m_jStatusBar.setText(m_BayesNet.lastActionMsg());
    a_undo.setEnabled(true);
    a_redo.setEnabled(false);
    repaint();
  }
  
  void deleteNode(int iNode)
  {
    try {
      m_BayesNet.deleteNode(iNode);
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
    } catch (Exception e) {
      e.printStackTrace();
    }
    updateStatus();
  }
  



  void addValue()
  {
    String sValue = "Value" + (m_BayesNet.getCardinality(m_nCurrentNode) + 1);
    String sNewValue = JOptionPane.showInputDialog(null, "New value " + sValue, "Node " + m_BayesNet.getNodeName(m_nCurrentNode), 2);
    
    if ((sNewValue == null) || (sNewValue.equals(""))) {
      return;
    }
    try {
      m_BayesNet.addNodeValue(m_nCurrentNode, sNewValue);
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());


    }
    catch (Exception e)
    {

      e.printStackTrace();
    }
    updateStatus();
  }
  
  void delValue(int nTargetNode, String sValue)
  {
    try
    {
      m_BayesNet.delNodeValue(nTargetNode, sValue);
      m_jStatusBar.setText(m_BayesNet.lastActionMsg());
    } catch (Exception e) {
      e.printStackTrace();
    }
    updateStatus();
  }
  


  void editCPT(int nTargetNode)
  {
    m_nCurrentNode = nTargetNode;
    final GraphVisualizerTableModel tm = new GraphVisualizerTableModel(nTargetNode);
    
    JTable jTblProbs = new JTable(tm);
    
    JScrollPane js = new JScrollPane(jTblProbs);
    
    int nParents = m_BayesNet.getNrOfParents(nTargetNode);
    if (nParents > 0) {
      GridBagConstraints gbc = new GridBagConstraints();
      JPanel jPlRowHeader = new JPanel(new GridBagLayout());
      

      int[] idx = new int[nParents];
      
      int[] lengths = new int[nParents];
      

      anchor = 18;
      fill = 2;
      insets = new Insets(0, 1, 0, 0);
      int addNum = 0;int temp = 0;
      boolean dark = false;
      for (;;) {
        gridwidth = 1;
        for (int k = 0; k < nParents; k++) {
          int iParent2 = m_BayesNet.getParent(nTargetNode, k);
          JLabel lb = new JLabel(m_BayesNet.getValueName(iParent2, idx[k]));
          lb.setFont(new Font("Dialog", 0, 12));
          lb.setOpaque(true);
          lb.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
          lb.setHorizontalAlignment(0);
          if (dark) {
            lb.setBackground(lb.getBackground().darker());
            lb.setForeground(Color.white);
          } else {
            lb.setForeground(Color.black);
          }
          temp = getPreferredSizewidth;
          lb.setPreferredSize(new Dimension(temp, jTblProbs.getRowHeight()));
          if (lengths[k] < temp)
            lengths[k] = temp;
          temp = 0;
          
          if (k == nParents - 1) {
            gridwidth = 0;
            dark = dark != true;
          }
          jPlRowHeader.add(lb, gbc);
          addNum++;
        }
        
        for (int k = nParents - 1; k >= 0; k--) {
          int iParent2 = m_BayesNet.getParent(m_nCurrentNode, k);
          if ((idx[k] == m_BayesNet.getCardinality(iParent2) - 1) && (k != 0)) {
            idx[k] = 0;
          }
          else {
            idx[k] += 1;
            break;
          }
        }
        
        int iParent2 = m_BayesNet.getParent(m_nCurrentNode, 0);
        if (idx[0] == m_BayesNet.getCardinality(iParent2)) {
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
      for (int j = 0; j < nParents; j++)
      {
        JLabel lb1 = new JLabel(m_BayesNet.getNodeName(m_BayesNet.getParent(nTargetNode, j)));
        lb1.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
        Dimension tempd = lb1.getPreferredSize();
        if (width < lengths[j]) {
          lb1.setPreferredSize(new Dimension(lengths[j], height));
          lb1.setHorizontalAlignment(0);
          lb1.setMinimumSize(new Dimension(lengths[j], height));
        } else if (width > lengths[j]) {
          JLabel lb2 = (JLabel)jPlRowHeader.getComponent(j);
          lb2.setPreferredSize(new Dimension(width, getPreferredSizeheight));
        }
        jPlRowNames.add(lb1, gbc);
      }
      js.setRowHeaderView(jPlRowHeader);
      js.setCorner("UPPER_LEFT_CORNER", jPlRowNames);
    }
    
    final JDialog dlg = new JDialog((Frame)getTopLevelAncestor(), "Probability Distribution Table For " + m_BayesNet.getNodeName(nTargetNode), true);
    
    dlg.setSize(500, 400);
    dlg.setLocation(getLocationx + getWidth() / 2 - 250, getLocationy + getHeight() / 2 - 200);
    


    dlg.getContentPane().setLayout(new BorderLayout());
    dlg.getContentPane().add(js, "Center");
    
    JButton jBtRandomize = new JButton("Randomize");
    jBtRandomize.setMnemonic('R');
    jBtRandomize.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        tm.randomize();
        dlg.repaint();
      }
      
    });
    JButton jBtOk = new JButton("Ok");
    jBtOk.setMnemonic('O');
    jBtOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        tm.setData();
        try {
          m_BayesNet.setDistribution(m_nCurrentNode, tmm_fProbs);
          m_jStatusBar.setText(m_BayesNet.lastActionMsg());
          updateStatus();
        } catch (Exception e) {
          e.printStackTrace();
        }
        dlg.setVisible(false);
      }
    });
    JButton jBtCancel = new JButton("Cancel");
    jBtCancel.setMnemonic('C');
    jBtCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        dlg.setVisible(false);
      }
    });
    Container c = new Container();
    c.setLayout(new GridBagLayout());
    c.add(jBtRandomize);
    c.add(jBtOk);
    c.add(jBtCancel);
    
    dlg.getContentPane().add(c, "South");
    dlg.setVisible(true);
  }
  



  public static void main(String[] args)
  {
    Logger.log(Logger.Level.INFO, "Logging started");
    
    LookAndFeel.setLookAndFeel();
    
    JFrame jf = new JFrame("Bayes Network Editor");
    GUI g = new GUI();
    JMenuBar menuBar = g.getMenuBar();
    
    if (args.length > 0) {
      try {
        g.readBIFFromFile(args[0]);
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (BIFFormatException bf) {
        bf.printStackTrace();
        System.exit(-1);
      }
    }
    



    jf.setJMenuBar(menuBar);
    jf.getContentPane().add(g);
    jf.setDefaultCloseOperation(3);
    jf.setSize(800, 600);
    jf.setVisible(true);
    m_Selection.updateGUI();
    GenericObjectEditor.registerEditors();
  }
}

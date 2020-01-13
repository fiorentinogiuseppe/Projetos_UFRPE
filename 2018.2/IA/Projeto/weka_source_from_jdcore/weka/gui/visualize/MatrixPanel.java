package weka.gui.visualize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.ExtensionFileFilter;

































public class MatrixPanel
  extends JPanel
{
  private static final long serialVersionUID = -1232642719869188740L;
  private final Plot m_plotsPanel;
  protected final ClassPanel m_cp = new ClassPanel();
  


  protected JPanel optionsPanel;
  


  protected JSplitPane jp;
  


  protected JButton m_updateBt;
  


  protected JButton m_selAttrib;
  


  protected Instances m_data;
  


  protected JList m_attribList;
  


  protected final JScrollPane m_js;
  


  protected JComboBox m_classAttrib;
  


  protected JSlider m_plotSize;
  


  protected JSlider m_pointSize;
  

  protected JSlider m_jitter;
  

  private Random rnd;
  

  private int[][] jitterVals;
  

  private int datapointSize;
  

  protected JTextField m_resamplePercent;
  

  protected JButton m_resampleBt;
  

  protected JTextField m_rseed;
  

  private final JLabel m_plotSizeLb;
  

  private final JLabel m_pointSizeLb;
  

  private int[] m_selectedAttribs;
  

  private int m_classIndex;
  

  private int[][] m_points;
  

  private int[] m_pointColors;
  

  private boolean[][] m_missing;
  

  private int[] m_type;
  

  private Dimension m_plotLBSizeD;
  

  private Dimension m_pointLBSizeD;
  

  private FastVector m_colorList;
  

  private static final Color[] m_defaultColors = { Color.blue, Color.red, Color.cyan, new Color(75, 123, 130), Color.pink, Color.green, Color.orange, new Color(255, 0, 255), new Color(255, 0, 0), new Color(0, 255, 0), Color.black };
  private final Color fontColor;
  private final Font f;
  
  public MatrixPanel()
  {
    Messages.getInstance();m_updateBt = new JButton(Messages.getString("MatrixPanel_UpdateBt_JButton_Text"));
    

    Messages.getInstance();m_selAttrib = new JButton(Messages.getString("MatrixPanel_SelAttrib_JButton_Text"));
    

    m_data = null;
    

    m_attribList = new JList();
    

    m_js = new JScrollPane();
    

    m_classAttrib = new JComboBox();
    

    m_plotSize = new JSlider(50, 500, 100);
    

    m_pointSize = new JSlider(1, 10, 1);
    

    m_jitter = new JSlider(0, 20, 0);
    

    rnd = new Random();
    




    datapointSize = 1;
    

    m_resamplePercent = new JTextField(5);
    

    Messages.getInstance();m_resampleBt = new JButton(Messages.getString("MatrixPanel_ResampleBt_JButton_Text"));
    

    m_rseed = new JTextField(5);
    

    Messages.getInstance();m_plotSizeLb = new JLabel(Messages.getString("MatrixPanel_PlotSizeLb_JLabel_Text"));
    

    Messages.getInstance();m_pointSizeLb = new JLabel(Messages.getString("MatrixPanel_PointSizeLb_JLabel_Text"));
    


































    m_colorList = new FastVector();
    














    fontColor = new Color(98, 101, 156);
    

    f = new Font("Dialog", 1, 11);
    






    m_rseed.setText("1");
    

    m_selAttrib.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Messages.getInstance();final JDialog jd = new JDialog((JFrame)getTopLevelAncestor(), Messages.getString("MatrixPanel_Jd_JDialog_Text"), Dialog.ModalityType.DOCUMENT_MODAL);
        


        JPanel jp = new JPanel();
        JScrollPane js = new JScrollPane(m_attribList);
        Messages.getInstance();JButton okBt = new JButton(Messages.getString("MatrixPanel_OkBt_JButton_Text"));
        Messages.getInstance();JButton cancelBt = new JButton(Messages.getString("MatrixPanel_CancelBt_JButton_Text"));
        final int[] savedSelection = m_attribList.getSelectedIndices();
        
        okBt.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            jd.dispose();
          }
        });
        cancelBt.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            m_attribList.setSelectedIndices(savedSelection);
            jd.dispose();
          } });
        jd.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            m_attribList.setSelectedIndices(savedSelection);
            jd.dispose();
          } });
        jp.add(okBt);
        jp.add(cancelBt);
        
        jd.getContentPane().add(js, "Center");
        jd.getContentPane().add(jp, "South");
        
        if (getPreferredSizewidth < 200) {
          jd.setSize(250, 250);
        } else {
          jd.setSize(getPreferredSizewidth + 10, 250);
        }
        jd.setLocation(m_selAttrib.getLocationOnScreen().x, m_selAttrib.getLocationOnScreen().y - jd.getHeight());
        
        jd.setVisible(true);
      }
      
    });
    m_updateBt.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        initInternalFields();
        
        MatrixPanel.Plot a = m_plotsPanel;
        a.setCellSize(m_plotSize.getValue());
        Dimension d = new Dimension(m_selectedAttribs.length * (cellSize + extpad) + 2, m_selectedAttribs.length * (cellSize + extpad) + 2);
        




        a.setPreferredSize(d);
        a.setSize(a.getPreferredSize());
        a.setJitter(m_jitter.getValue());
        
        m_js.revalidate();
        m_cp.setColours(m_colorList);
        m_cp.setCindex(m_classIndex);
        
        repaint();
      }
    });
    m_updateBt.setPreferredSize(m_selAttrib.getPreferredSize());
    
    m_plotSize.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ce) {
        Messages.getInstance();Messages.getInstance();m_plotSizeLb.setText(Messages.getString("MatrixPanel_StateChanged_PlotSizeLb_Text_First") + m_plotSize.getValue() + Messages.getString("MatrixPanel_StateChanged_PlotSizeLb_Text_Second"));
        m_plotSizeLb.setPreferredSize(m_plotLBSizeD);
        m_jitter.setMaximum(m_plotSize.getValue() / 5);
      }
      
    });
    m_pointSize.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ce) {
        Messages.getInstance();Messages.getInstance();m_pointSizeLb.setText(Messages.getString("MatrixPanel_StateChanged_PointSizeLb_Text_First") + m_pointSize.getValue() + Messages.getString("MatrixPanel_StateChanged_PointSizeLb_Text_Second"));
        m_pointSizeLb.setPreferredSize(m_pointLBSizeD);
        datapointSize = m_pointSize.getValue();
      }
      
    });
    m_resampleBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance();JLabel rseedLb = new JLabel(Messages.getString("MatrixPanel_ActionPerformed_RseedLb_JLabel_Text"));
        JTextField rseedTxt = m_rseed;
        Messages.getInstance();JLabel percentLb = new JLabel(Messages.getString("MatrixPanel_ActionPerformed_PercentLb_JLabel_Text"));
        Messages.getInstance();JLabel percent2Lb = new JLabel(Messages.getString("MatrixPanel_ActionPerformed_Percent2Lb_JLabel_Text"));
        final JTextField percentTxt = new JTextField(5);
        percentTxt.setText(m_resamplePercent.getText());
        Messages.getInstance();JButton doneBt = new JButton(Messages.getString("MatrixPanel_ActionPerformed_DoneBt_JButton_Text"));
        
        Messages.getInstance();final JDialog jd = new JDialog((JFrame)getTopLevelAncestor(), Messages.getString("MatrixPanel_ActionPerformed_Jd_JDialog_Text"), Dialog.ModalityType.DOCUMENT_MODAL)
        {
          private static final long serialVersionUID = -269823533147146296L;
          
          public void dispose()
          {
            m_resamplePercent.setText(percentTxt.getText());
            super.dispose();
          }
        };
        jd.setDefaultCloseOperation(2);
        
        doneBt.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            jd.dispose();
          }
        });
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel p1 = new JPanel(gbl);
        anchor = 17;fill = 2;
        insets = new Insets(0, 2, 2, 2);
        gridwidth = -1;
        p1.add(rseedLb, gbc);weightx = 0.0D;
        gridwidth = 0;weightx = 1.0D;
        p1.add(rseedTxt, gbc);
        insets = new Insets(8, 2, 0, 2);weightx = 0.0D;
        p1.add(percentLb, gbc);
        insets = new Insets(0, 2, 2, 2);gridwidth = -1;
        p1.add(percent2Lb, gbc);
        gridwidth = 0;weightx = 1.0D;
        p1.add(percentTxt, gbc);
        insets = new Insets(8, 2, 2, 2);
        
        JPanel p3 = new JPanel(gbl);
        fill = 2;gridwidth = 0;
        weightx = 1.0D;weighty = 0.0D;
        p3.add(p1, gbc);
        insets = new Insets(8, 4, 8, 4);
        p3.add(doneBt, gbc);
        
        jd.getContentPane().setLayout(new BorderLayout());
        jd.getContentPane().add(p3, "North");
        jd.pack();
        jd.setLocation(m_resampleBt.getLocationOnScreen().x, m_resampleBt.getLocationOnScreen().y - jd.getHeight());
        
        jd.setVisible(true);
      }
      
    });
    optionsPanel = new JPanel(new GridBagLayout());
    JPanel p2 = new JPanel(new BorderLayout());
    JPanel p3 = new JPanel(new GridBagLayout());
    JPanel p4 = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    
    m_plotLBSizeD = m_plotSizeLb.getPreferredSize();
    m_pointLBSizeD = m_pointSizeLb.getPreferredSize();
    Messages.getInstance();m_pointSizeLb.setText(Messages.getString("MatrixPanel_ActionPerformed_PointSizeLb_Text"));
    m_pointSizeLb.setPreferredSize(m_pointLBSizeD);
    m_resampleBt.setPreferredSize(m_selAttrib.getPreferredSize());
    
    fill = 2;
    anchor = 18;
    insets = new Insets(2, 2, 2, 2);
    p4.add(m_plotSizeLb, gbc);
    weightx = 1.0D;gridwidth = 0;
    p4.add(m_plotSize, gbc);
    weightx = 0.0D;gridwidth = -1;
    p4.add(m_pointSizeLb, gbc);
    weightx = 1.0D;gridwidth = 0;
    p4.add(m_pointSize, gbc);
    weightx = 0.0D;gridwidth = -1;
    Messages.getInstance();p4.add(new JLabel(Messages.getString("MatrixPanel_ActionPerformed_P4_Text")), gbc);
    weightx = 1.0D;gridwidth = 0;
    p4.add(m_jitter, gbc);
    p4.add(m_classAttrib, gbc);
    
    gridwidth = 0;
    weightx = 1.0D;
    fill = 0;
    p3.add(m_updateBt, gbc);
    p3.add(m_selAttrib, gbc);
    gridwidth = -1;
    weightx = 0.0D;
    fill = 3;
    anchor = 17;
    p3.add(m_resampleBt, gbc);
    gridwidth = 0;
    p3.add(m_resamplePercent, gbc);
    
    Messages.getInstance();p2.setBorder(BorderFactory.createTitledBorder(Messages.getString("MatrixPanel_ActionPerformed_P2_BorderFactoryCreateTitledBorder_Text")));
    p2.add(m_cp, "South");
    
    insets = new Insets(8, 5, 2, 5);
    anchor = 16;fill = 2;weightx = 1.0D;
    gridwidth = -1;
    optionsPanel.add(p4, gbc);
    gridwidth = 0;
    optionsPanel.add(p3, gbc);
    optionsPanel.add(p2, gbc);
    
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent cv) {
        m_js.setMinimumSize(new Dimension(getWidth(), getHeight() - optionsPanel.getPreferredSize().height - 10));
        

        jp.setDividerLocation(getHeight() - optionsPanel.getPreferredSize().height - 10);
      }
      
    });
    optionsPanel.setMinimumSize(new Dimension(0, 0));
    jp = new JSplitPane(0, m_js, optionsPanel);
    jp.setOneTouchExpandable(true);
    jp.setResizeWeight(1.0D);
    setLayout(new BorderLayout());
    add(jp, "Center");
    

    for (int i = 0; i < m_defaultColors.length - 1; i++) {
      m_colorList.addElement(m_defaultColors[i]);
    }
    
    m_selectedAttribs = m_attribList.getSelectedIndices();
    m_plotsPanel = new Plot();
    m_plotsPanel.setLayout(null);
    m_js.getHorizontalScrollBar().setUnitIncrement(10);
    m_js.getVerticalScrollBar().setUnitIncrement(10);
    m_js.setViewportView(m_plotsPanel);
    m_js.setColumnHeaderView(m_plotsPanel.getColHeader());
    m_js.setRowHeaderView(m_plotsPanel.getRowHeader());
    Messages.getInstance();JLabel lb = new JLabel(Messages.getString("MatrixPanel_ActionPerformed_Lb_JLabel_Text"));
    lb.setFont(f);lb.setForeground(fontColor);
    lb.setHorizontalTextPosition(0);
    m_js.setCorner("UPPER_LEFT_CORNER", lb);
    m_cp.setInstances(m_data);
    m_cp.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
    m_cp.addRepaintNotify(m_plotsPanel);
  }
  




  public void initInternalFields()
  {
    Instances inst = m_data;
    m_classIndex = m_classAttrib.getSelectedIndex();
    m_selectedAttribs = m_attribList.getSelectedIndices();
    double minC = 0.0D;double maxC = 0.0D;
    

    if (Double.parseDouble(m_resamplePercent.getText()) < 100.0D) {
      inst = new Instances(m_data, 0, m_data.numInstances());
      inst.randomize(new Random(Integer.parseInt(m_rseed.getText())));
      







      inst = new Instances(inst, 0, (int)Math.round(Double.parseDouble(m_resamplePercent.getText()) / 100.0D * inst.numInstances()));
    }
    




    m_points = new int[inst.numInstances()][m_selectedAttribs.length];
    m_pointColors = new int[inst.numInstances()];
    m_missing = new boolean[inst.numInstances()][m_selectedAttribs.length + 1];
    m_type = new int[2];
    jitterVals = new int[inst.numInstances()][2];
    

    if (!inst.attribute(m_classIndex).isNumeric())
    {
      for (int i = m_colorList.size(); i < inst.attribute(m_classIndex).numValues() + 1; i++) {
        Color pc = m_defaultColors[(i % 10)];
        int ija = i / 10;
        ija *= 2;
        for (int j = 0; j < ija; j++) {
          pc = pc.darker();
        }
        m_colorList.addElement(pc);
      }
      
      for (int i = 0; i < inst.numInstances(); i++)
      {
        if (inst.instance(i).isMissing(m_classIndex)) {
          m_pointColors[i] = (m_defaultColors.length - 1);
        } else {
          m_pointColors[i] = ((int)inst.instance(i).value(m_classIndex));
        }
        jitterVals[i][0] = (rnd.nextInt(m_jitter.getValue() + 1) - m_jitter.getValue() / 2);
        
        jitterVals[i][1] = (rnd.nextInt(m_jitter.getValue() + 1) - m_jitter.getValue() / 2);
      }
      

    }
    else
    {
      for (int i = 0; i < inst.numInstances(); i++) {
        if (!inst.instance(i).isMissing(m_classIndex)) {
          minC = maxC = inst.instance(i).value(m_classIndex);
          break;
        }
      }
      
      for (int i = 1; i < inst.numInstances(); i++) {
        if (!inst.instance(i).isMissing(m_classIndex)) {
          if (minC > inst.instance(i).value(m_classIndex))
            minC = inst.instance(i).value(m_classIndex);
          if (maxC < inst.instance(i).value(m_classIndex)) {
            maxC = inst.instance(i).value(m_classIndex);
          }
        }
      }
      for (int i = 0; i < inst.numInstances(); i++) {
        double r = (inst.instance(i).value(m_classIndex) - minC) / (maxC - minC);
        r = r * 240.0D + 15.0D;
        m_pointColors[i] = ((int)r);
        
        jitterVals[i][0] = (rnd.nextInt(m_jitter.getValue() + 1) - m_jitter.getValue() / 2);
        
        jitterVals[i][1] = (rnd.nextInt(m_jitter.getValue() + 1) - m_jitter.getValue() / 2);
      }
    }
    


    double[] min = new double[m_selectedAttribs.length];double max = 0.0D;
    double[] ratio = new double[m_selectedAttribs.length];
    double cellSize = m_plotSize.getValue();double temp1 = 0.0D;double temp2 = 0.0D;
    
    for (int j = 0; j < m_selectedAttribs.length; j++)
    {
      for (int i = 0; i < inst.numInstances(); i++) {
        double tmp791_790 = 0.0D;max = tmp791_790;min[j] = tmp791_790;
        if (!inst.instance(i).isMissing(m_selectedAttribs[j])) {
          double tmp834_831 = inst.instance(i).value(m_selectedAttribs[j]);max = tmp834_831;min[j] = tmp834_831;
          break;
        }
      }
      for (i = i; i < inst.numInstances(); i++) {
        if (!inst.instance(i).isMissing(m_selectedAttribs[j])) {
          if (inst.instance(i).value(m_selectedAttribs[j]) < min[j])
            min[j] = inst.instance(i).value(m_selectedAttribs[j]);
          if (inst.instance(i).value(m_selectedAttribs[j]) > max)
            max = inst.instance(i).value(m_selectedAttribs[j]);
        }
      }
      ratio[j] = (cellSize / (max - min[j]));
    }
    
    boolean classIndexProcessed = false;
    for (int j = 0; j < m_selectedAttribs.length; j++) {
      if ((inst.attribute(m_selectedAttribs[j]).isNominal()) || (inst.attribute(m_selectedAttribs[j]).isString()))
      {

        temp1 = cellSize / inst.attribute(m_selectedAttribs[j]).numValues();
        temp2 = temp1 / 2.0D;
        for (int i = 0; i < inst.numInstances(); i++) {
          m_points[i][j] = ((int)Math.round(temp2 + temp1 * inst.instance(i).value(m_selectedAttribs[j])));
          if (inst.instance(i).isMissing(m_selectedAttribs[j])) {
            m_missing[i][j] = 1;
            if (m_selectedAttribs[j] == m_classIndex) {
              m_missing[i][(m_missing[0].length - 1)] = 1;
              classIndexProcessed = true;
            }
          }
        }
      }
      else
      {
        for (int i = 0; i < inst.numInstances(); i++) {
          m_points[i][j] = ((int)Math.round((inst.instance(i).value(m_selectedAttribs[j]) - min[j]) * ratio[j]));
          
          if (inst.instance(i).isMissing(m_selectedAttribs[j])) {
            m_missing[i][j] = 1;
            if (m_selectedAttribs[j] == m_classIndex) {
              m_missing[i][(m_missing[0].length - 1)] = 1;
              classIndexProcessed = true;
            }
          }
        }
      }
    }
    
    if ((inst.attribute(m_classIndex).isNominal()) || (inst.attribute(m_classIndex).isString())) {
      m_type[0] = 1;m_type[1] = inst.attribute(m_classIndex).numValues();
    }
    else {
      int tmp1390_1389 = 0;m_type[1] = tmp1390_1389;m_type[0] = tmp1390_1389;
    }
    if (!classIndexProcessed) {
      for (int i = 0; i < inst.numInstances(); i++) {
        if (inst.instance(i).isMissing(m_classIndex)) {
          m_missing[i][(m_missing[0].length - 1)] = 1;
        }
      }
    }
    m_cp.setColours(m_colorList);
  }
  

  public void setupAttribLists()
  {
    String[] tempAttribNames = new String[m_data.numAttributes()];
    

    m_classAttrib.removeAllItems();
    for (int i = 0; i < tempAttribNames.length; i++) { String type;
      switch (m_data.attribute(i).type()) {
      case 1: 
        Messages.getInstance();type = Messages.getString("MatrixPanel_SetupAttribLists_Type_AttributeNOMINAL_Text");
        break;
      case 0: 
        Messages.getInstance();type = Messages.getString("MatrixPanel_SetupAttribLists_Type_AttributeNUMERIC_Text");
        break;
      case 2: 
        Messages.getInstance();type = Messages.getString("MatrixPanel_SetupAttribLists_Type_AttributeSTRING_Text");
        break;
      case 3: 
        Messages.getInstance();type = Messages.getString("MatrixPanel_SetupAttribLists_Type_AttributeDATE_Text");
        break;
      case 4: 
        Messages.getInstance();type = Messages.getString("MatrixPanel_SetupAttribLists_Type_AttributeRELATIONAL_Text");
        break;
      default: 
        Messages.getInstance();type = Messages.getString("MatrixPanel_SetupAttribLists_Type_AttributeDEFAULT_Text");
      }
      Messages.getInstance();tempAttribNames[i] = new String(Messages.getString("MatrixPanel_SetupAttribLists_Text_First") + m_data.attribute(i).name() + " " + type);
      m_classAttrib.addItem(tempAttribNames[i]);
    }
    if (m_data.classIndex() == -1) {
      m_classAttrib.setSelectedIndex(tempAttribNames.length - 1);
    } else
      m_classAttrib.setSelectedIndex(m_data.classIndex());
    m_attribList.setListData(tempAttribNames);
    m_attribList.setSelectionInterval(0, tempAttribNames.length - 1);
  }
  

  public void setPercent()
  {
    if (m_data.numInstances() > 700) {
      double percnt = 500.0D / m_data.numInstances() * 100.0D;
      percnt *= 100.0D;
      percnt = Math.round(percnt);
      percnt /= 100.0D;
      
      m_resamplePercent.setText("" + percnt);
    }
    else {
      Messages.getInstance();m_resamplePercent.setText(Messages.getString("MatrixPanel_SetPercent_Text_First"));
    }
  }
  




  public void setInstances(Instances newInst)
  {
    m_data = newInst;
    setPercent();
    setupAttribLists();
    m_rseed.setText("1");
    initInternalFields();
    m_cp.setInstances(m_data);
    m_cp.setCindex(m_classIndex);
    m_updateBt.doClick();
  }
  



  public static void main(String[] args)
  {
    Messages.getInstance();JFrame jf = new JFrame(Messages.getString("MatrixPanel_Main_JFRame_Text"));
    Messages.getInstance();JButton setBt = new JButton(Messages.getString("MatrixPanel_Main_SetBt_JButton_Text"));
    Instances data = null;
    try {
      if (args.length == 1) {
        data = new Instances(new BufferedReader(new FileReader(args[0])));
      } else {
        Messages.getInstance();System.out.println(Messages.getString("MatrixPanel_Main_Text"));
        System.exit(-1);
      }
    } catch (IOException ex) { ex.printStackTrace();System.exit(-1);
    }
    final MatrixPanel mp = new MatrixPanel();
    mp.setInstances(data);
    setBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        Messages.getInstance();Messages.getInstance();ExtensionFileFilter myfilter = new ExtensionFileFilter(Messages.getString("MatrixPanel_Main_ActionPerformed_ExtensionFileFilter_Text_First"), Messages.getString("MatrixPanel_Main_ActionPerformed_ExtensionFileFilter_Text_Second"));
        chooser.setFileFilter(myfilter);
        int returnVal = chooser.showOpenDialog(val$jf);
        
        if (returnVal == 0) {
          try
          {
            Messages.getInstance();System.out.println(Messages.getString("MatrixPanel_Main_ActionPerformed_Text_First") + chooser.getSelectedFile().getName());
            Instances in = new Instances(new FileReader(chooser.getSelectedFile().getAbsolutePath()));
            mp.setInstances(in);
          } catch (Exception ex) {
            ex.printStackTrace();

          }
          
        }
        
      }
      

    });
    jf.getContentPane().setLayout(new BorderLayout());
    jf.getContentPane().add(mp, "Center");
    jf.getContentPane().add(setBt, "South");
    jf.getContentPane().setFont(new Font("SansSerif", 0, 11));
    jf.setDefaultCloseOperation(3);
    jf.setSize(800, 600);
    jf.setVisible(true);
    jf.repaint();
  }
  

  private class Plot
    extends JPanel
    implements MouseMotionListener, MouseListener
  {
    JPanel jPlRowHeader;
    
    JPanel jPlColHeader;
    
    int lastypos;
    int lastxpos;
    FontMetrics fm;
    Rectangle r;
    int jitter = 0; int lasty = 0; int lastx = 0; int cellRange = 100; int cellSize = 100; int intpad = 4; int extpad = 3;
    


    private static final long serialVersionUID = -1721245738439420882L;
    



    public Plot()
    {
      Messages.getInstance();setToolTipText(Messages.getString("MatrixPanel_Plot_SetToolTipText_Text"));
      addMouseMotionListener(this);
      addMouseListener(this);
      initialize();
    }
    
    public void initialize()
    {
      lastxpos = (this.lastypos = 0);
      cellRange = cellSize;cellSize = (cellRange + 2 * intpad);
      
      jPlColHeader = new JPanel() {
        private static final long serialVersionUID = -9098547751937467506L;
        Rectangle r;
        
        public void paint(Graphics g) { r = g.getClipBounds();
          g.setColor(getBackground());
          g.fillRect(r.x, r.y, r.width, r.height);
          g.setFont(f);
          fm = g.getFontMetrics();
          int xpos = 0;int ypos = 0;int attribWidth = 0;
          
          g.setColor(fontColor);
          xpos = extpad;
          ypos = extpad + fm.getHeight();
          
          for (int i = 0; i < m_selectedAttribs.length; i++)
            if (xpos + cellSize < r.x) {
              xpos += cellSize + extpad;
            } else { if (xpos > r.x + r.width) {
                break;
              }
              attribWidth = fm.stringWidth(m_data.attribute(m_selectedAttribs[i]).name());
              g.drawString(m_data.attribute(m_selectedAttribs[i]).name(), attribWidth < cellSize ? xpos + (cellSize / 2 - attribWidth / 2) : xpos, ypos);
              


              xpos += cellSize + extpad;
            }
          fm = null;r = null;
        }
        
        public Dimension getPreferredSize() {
          fm = getFontMetrics(getFont());
          return new Dimension(m_selectedAttribs.length * (cellSize + extpad), 2 * extpad + fm.getHeight());
        }
        

      };
      jPlRowHeader = new JPanel() {
        private static final long serialVersionUID = 8474957069309552844L;
        Rectangle r;
        
        public void paint(Graphics g) {
          r = g.getClipBounds();
          g.setColor(getBackground());
          g.fillRect(r.x, r.y, r.width, r.height);
          g.setFont(f);
          fm = g.getFontMetrics();
          int xpos = 0;int ypos = 0;
          
          g.setColor(fontColor);
          xpos = extpad;
          ypos = extpad;
          
          for (int j = m_selectedAttribs.length - 1; j >= 0; j--)
            if (ypos + cellSize < r.y) {
              ypos += cellSize + extpad;
            } else { if (ypos > r.y + r.height) {
                break;
              }
              g.drawString(m_data.attribute(m_selectedAttribs[j]).name(), xpos + extpad, ypos + cellSize / 2);
              
              xpos = extpad;
              ypos += cellSize + extpad;
            }
          r = null;
        }
        
        public Dimension getPreferredSize() {
          return new Dimension(100 + extpad, m_selectedAttribs.length * (cellSize + extpad));
        }
        

      };
      jPlColHeader.setFont(f);
      jPlRowHeader.setFont(f);
      setFont(f);
    }
    
    public JPanel getRowHeader() {
      return jPlRowHeader;
    }
    
    public JPanel getColHeader() {
      return jPlColHeader;
    }
    
    public void mouseMoved(MouseEvent e) {
      Graphics g = getGraphics();
      int xpos = extpad;int ypos = extpad;
      
      for (int j = m_selectedAttribs.length - 1; j >= 0; j--) {
        for (int i = 0; i < m_selectedAttribs.length; i++) {
          if ((e.getX() >= xpos) && (e.getX() <= xpos + cellSize + extpad) && 
            (e.getY() >= ypos) && (e.getY() <= ypos + cellSize + extpad)) {
            if ((xpos != lastxpos) || (ypos != lastypos)) {
              g.setColor(Color.red);
              g.drawRect(xpos - 1, ypos - 1, cellSize + 1, cellSize + 1);
              if ((lastxpos != 0) && (lastypos != 0)) {
                g.setColor(getBackground().darker());
                g.drawRect(lastxpos - 1, lastypos - 1, cellSize + 1, cellSize + 1); }
              lastxpos = xpos;lastypos = ypos;
            }
            return;
          }
          xpos += cellSize + extpad;
        }
        xpos = extpad;
        ypos += cellSize + extpad;
      }
      if ((lastxpos != 0) && (lastypos != 0)) {
        g.setColor(getBackground().darker());
        g.drawRect(lastxpos - 1, lastypos - 1, cellSize + 1, cellSize + 1); }
      lastxpos = (this.lastypos = 0);
    }
    
    public void mouseDragged(MouseEvent e) {}
    
    public void mouseClicked(MouseEvent e) {
      int i = 0;int j = 0;int found = 0;
      
      int xpos = extpad;int ypos = extpad;
      for (j = m_selectedAttribs.length - 1; j >= 0; j--) {
        for (i = 0; i < m_selectedAttribs.length; i++) {
          if ((e.getX() >= xpos) && (e.getX() <= xpos + cellSize + extpad) && 
            (e.getY() >= ypos) && (e.getY() <= ypos + cellSize + extpad)) {
            found = 1; break;
          }
          xpos += cellSize + extpad;
        }
        if (found == 1)
          break;
        xpos = extpad;
        ypos += cellSize + extpad;
      }
      if (found == 0) {
        return;
      }
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("MatrixPanel_Plot_JFrame_Text") + m_data.relationName());
      VisualizePanel vp = new VisualizePanel();
      try {
        PlotData2D pd = new PlotData2D(m_data);
        Messages.getInstance();pd.setPlotName(Messages.getString("MatrixPanel_Plot_Pd_SetPlotName_Text"));
        vp.setMasterPlot(pd);
        
        vp.setXIndex(m_selectedAttribs[i]);
        vp.setYIndex(m_selectedAttribs[j]);
        m_ColourCombo.setSelectedIndex(m_classIndex);
      } catch (Exception ex) {
        ex.printStackTrace(); }
      jf.getContentPane().add(vp);
      jf.setSize(800, 600);
      jf.setVisible(true);
    }
    
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {}
    
    public void setJitter(int newjitter) { jitter = newjitter; }
    


    public void setCellSize(int newCellSize)
    {
      cellSize = newCellSize;
      initialize();
    }
    


    public String getToolTipText(MouseEvent event)
    {
      int xpos = extpad;int ypos = extpad;
      
      for (int j = m_selectedAttribs.length - 1; j >= 0; j--) {
        for (int i = 0; i < m_selectedAttribs.length; i++) {
          if ((event.getX() >= xpos) && (event.getX() <= xpos + cellSize + extpad) && 
            (event.getY() >= ypos) && (event.getY() <= ypos + cellSize + extpad)) {
            Messages.getInstance();Messages.getInstance();Messages.getInstance();return Messages.getString("MatrixPanel_Plot_GetToolTipText_Text_First") + m_data.attribute(m_selectedAttribs[i]).name() + Messages.getString("MatrixPanel_Plot_GetToolTipText_Text_Second") + m_data.attribute(m_selectedAttribs[j]).name() + Messages.getString("MatrixPanel_Plot_GetToolTipText_Text_Third");
          }
          
          xpos += cellSize + extpad;
        }
        xpos = extpad;
        ypos += cellSize + extpad;
      }
      Messages.getInstance();return Messages.getString("MatrixPanel_Plot_GetToolTipText_Text_Fourth");
    }
    



    public void paintGraph(Graphics g, int xattrib, int yattrib, int xpos, int ypos)
    {
      g.setColor(getBackground().darker().darker());
      g.drawRect(xpos - 1, ypos - 1, cellSize + 1, cellSize + 1);
      g.setColor(Color.white);
      g.fillRect(xpos, ypos, cellSize, cellSize);
      for (int i = 0; i < m_points.length; i++)
      {
        if ((m_missing[i][yattrib] == 0) && (m_missing[i][xattrib] == 0))
        {
          if (m_type[0] == 0) {
            if (m_missing[i][(m_missing[0].length - 1)] != 0) {
              g.setColor(MatrixPanel.m_defaultColors[(MatrixPanel.m_defaultColors.length - 1)]);
            } else
              g.setColor(new Color(m_pointColors[i], 150, 255 - m_pointColors[i]));
          } else
            g.setColor((Color)m_colorList.elementAt(m_pointColors[i]));
          int y;
          int x; int y; if ((m_points[i][xattrib] + jitterVals[i][0] < 0) || (m_points[i][xattrib] + jitterVals[i][0] > cellRange)) { int y;
            if ((cellRange - m_points[i][yattrib] + jitterVals[i][1] < 0) || (cellRange - m_points[i][yattrib] + jitterVals[i][1] > cellRange))
            {
              int x = intpad + m_points[i][xattrib];
              y = intpad + (cellRange - m_points[i][yattrib]);
            }
            else
            {
              int x = intpad + m_points[i][xattrib];
              y = intpad + (cellRange - m_points[i][yattrib]) + jitterVals[i][1];
            } } else { int y;
            if ((cellRange - m_points[i][yattrib] + jitterVals[i][1] < 0) || (cellRange - m_points[i][yattrib] + jitterVals[i][1] > cellRange))
            {
              int x = intpad + m_points[i][xattrib] + jitterVals[i][0];
              y = intpad + (cellRange - m_points[i][yattrib]);
            }
            else
            {
              x = intpad + m_points[i][xattrib] + jitterVals[i][0];
              y = intpad + (cellRange - m_points[i][yattrib]) + jitterVals[i][1];
            } }
          if (datapointSize == 1) {
            g.drawLine(x + xpos, y + ypos, x + xpos, y + ypos);
          } else
            g.drawOval(x + xpos - datapointSize / 2, y + ypos - datapointSize / 2, datapointSize, datapointSize);
        }
      }
      g.setColor(fontColor);
    }
    



    public void paintME(Graphics g)
    {
      r = g.getClipBounds();
      
      g.setColor(getBackground());
      g.fillRect(r.x, r.y, r.width, r.height);
      g.setColor(fontColor);
      
      int xpos = 0;int ypos = 0;
      
      xpos = extpad;
      ypos = extpad;
      

      for (int j = m_selectedAttribs.length - 1; j >= 0; j--) {
        if (ypos + cellSize < r.y) {
          ypos += cellSize + extpad;
        } else { if (ypos > r.y + r.height) {
            break;
          }
          for (int i = 0; i < m_selectedAttribs.length; i++) {
            if (xpos + cellSize < r.x) {
              xpos += cellSize + extpad;
            } else { if (xpos > r.x + r.width) {
                break;
              }
              paintGraph(g, i, j, xpos, ypos);
              xpos += cellSize + extpad;
            }
          }
          xpos = extpad;
          ypos += cellSize + extpad;
        }
      }
    }
    
    public void paintComponent(Graphics g)
    {
      paintME(g);
    }
  }
}

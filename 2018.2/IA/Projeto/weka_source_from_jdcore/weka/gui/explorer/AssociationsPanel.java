package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import weka.associations.Associator;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.Memory;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.GenericObjectEditor;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;




























public class AssociationsPanel
  extends JPanel
  implements Explorer.CapabilitiesFilterChangeListener, Explorer.ExplorerPanel, Explorer.LogHandler
{
  static final long serialVersionUID = -6867871711865476971L;
  protected Explorer m_Explorer = null;
  

  protected GenericObjectEditor m_AssociatorEditor = new GenericObjectEditor();
  


  protected PropertyPanel m_CEPanel = new PropertyPanel(m_AssociatorEditor);
  

  protected JTextArea m_OutText = new JTextArea(20, 40);
  

  protected Logger m_Log = new SysErrLog();
  

  protected SaveBuffer m_SaveOut = new SaveBuffer(m_Log, this);
  

  protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);
  protected JButton m_StartBut;
  
  public AssociationsPanel() { Messages.getInstance();m_StartBut = new JButton(Messages.getString("AssociationsPanel_StartBut_JButton_Text"));
    

    Messages.getInstance();m_StopBut = new JButton(Messages.getString("AssociationsPanel_StopBut_JButton_Text"));
    




















    m_OutText.setEditable(false);
    m_OutText.setFont(new Font("Monospaced", 0, 12));
    m_OutText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    m_OutText.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & 0x10) != 16)
        {
          m_OutText.selectAll();
        }
      }
    });
    Messages.getInstance();m_History.setBorder(BorderFactory.createTitledBorder(Messages.getString("AssociationsPanel_MouseClicked_History_BorderFactoryCreateTitledBorder_Text")));
    m_History.setHandleRightClicks(false);
    
    m_History.getList().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (((e.getModifiers() & 0x10) != 16) || (e.isAltDown()))
        {
          int index = m_History.getList().locationToIndex(e.getPoint());
          if (index != -1) {
            String name = m_History.getNameAtIndex(index);
            historyRightClickPopup(name, e.getX(), e.getY());
          } else {
            historyRightClickPopup(null, e.getX(), e.getY());
          }
          
        }
      }
    });
    m_AssociatorEditor.setClassType(Associator.class);
    m_AssociatorEditor.setValue(ExplorerDefaults.getAssociator());
    m_AssociatorEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        m_StartBut.setEnabled(true);
        
        Capabilities currentFilter = m_AssociatorEditor.getCapabilitiesFilter();
        Associator associator = (Associator)m_AssociatorEditor.getValue();
        Capabilities currentSchemeCapabilities = null;
        if ((associator != null) && (currentFilter != null) && ((associator instanceof CapabilitiesHandler)))
        {
          currentSchemeCapabilities = ((CapabilitiesHandler)associator).getCapabilities();
          
          if ((!currentSchemeCapabilities.supportsMaybe(currentFilter)) && (!currentSchemeCapabilities.supports(currentFilter)))
          {
            m_StartBut.setEnabled(false);
          }
        }
        repaint();
      }
      
    });
    Messages.getInstance();m_StartBut.setToolTipText(Messages.getString("AssociationsPanel_StartBut_SetToolTipText_Text"));
    Messages.getInstance();m_StopBut.setToolTipText(Messages.getString("AssociationsPanel_StopBut_SetToolTipText_Text"));
    m_StartBut.setEnabled(false);
    m_StopBut.setEnabled(false);
    m_StartBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean proceed = true;
        if (Explorer.m_Memory.memoryIsLow()) {
          proceed = Explorer.m_Memory.showMemoryIsLow();
        }
        
        if (proceed) {
          startAssociator();
        }
      }
    });
    m_StopBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopAssociator();
      }
      

    });
    JPanel p1 = new JPanel();
    Messages.getInstance();p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("AssociationsPanel_P1_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    p1.setLayout(new BorderLayout());
    p1.add(m_CEPanel, "North");
    
    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(1, 2));
    JPanel ssButs = new JPanel();
    ssButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    ssButs.setLayout(new GridLayout(1, 2, 5, 5));
    ssButs.add(m_StartBut);
    ssButs.add(m_StopBut);
    buttons.add(ssButs);
    
    JPanel p3 = new JPanel();
    Messages.getInstance();p3.setBorder(BorderFactory.createTitledBorder(Messages.getString("AssociationsPanel_P3_JPanel_BorderFactoryCreateTitledBorder_Text")));
    p3.setLayout(new BorderLayout());
    JScrollPane js = new JScrollPane(m_OutText);
    p3.add(js, "Center");
    js.getViewport().addChangeListener(new ChangeListener() {
      private int lastHeight;
      
      public void stateChanged(ChangeEvent e) { JViewport vp = (JViewport)e.getSource();
        int h = getViewSizeheight;
        if (h != lastHeight) {
          lastHeight = h;
          int x = h - getExtentSizeheight;
          vp.setViewPosition(new Point(0, x));
        }
        
      }
    });
    GridBagLayout gbL = new GridBagLayout();
    GridBagConstraints gbC = new GridBagConstraints();
    JPanel mondo = new JPanel();
    gbL = new GridBagLayout();
    mondo.setLayout(gbL);
    gbC = new GridBagConstraints();
    anchor = 11;
    fill = 2;
    gridy = 1;gridx = 0;
    gbL.setConstraints(buttons, gbC);
    mondo.add(buttons);
    gbC = new GridBagConstraints();
    fill = 1;
    gridy = 2;gridx = 0;weightx = 0.0D;
    gbL.setConstraints(m_History, gbC);
    mondo.add(m_History);
    gbC = new GridBagConstraints();
    fill = 1;
    gridy = 0;gridx = 1;
    gridheight = 3;
    weightx = 100.0D;weighty = 100.0D;
    gbL.setConstraints(p3, gbC);
    mondo.add(p3);
    
    setLayout(new BorderLayout());
    add(p1, "North");
    add(mondo, "Center");
  }
  





  public void setLog(Logger newLog)
  {
    m_Log = newLog;
  }
  





  public void setInstances(Instances inst)
  {
    m_Instances = inst;
    String[] attribNames = new String[m_Instances.numAttributes()];
    for (int i = 0; i < attribNames.length; i++) {
      String type = "";
      switch (m_Instances.attribute(i).type()) {
      case 1: 
        Messages.getInstance();type = Messages.getString("AssociationsPanel_SetInstances_AttributeNOMINAL_Text");
        break;
      case 0: 
        Messages.getInstance();type = Messages.getString("AssociationsPanel_SetInstances_AttributeNUMERIC_Text");
        break;
      case 2: 
        Messages.getInstance();type = Messages.getString("AssociationsPanel_SetInstances_AttributeSTRING_Text");
        break;
      case 3: 
        Messages.getInstance();type = Messages.getString("AssociationsPanel_SetInstances_AttributeDATE_Text");
        break;
      case 4: 
        Messages.getInstance();type = Messages.getString("AssociationsPanel_SetInstances_AttributeRELATIONAL_Text");
        break;
      default: 
        Messages.getInstance();type = Messages.getString("AssociationsPanel_SetInstances_AttributeDEFAULT_Text");
      }
      attribNames[i] = (type + m_Instances.attribute(i).name());
    }
    m_StartBut.setEnabled(m_RunThread == null);
    m_StopBut.setEnabled(m_RunThread != null);
  }
  



  protected JButton m_StopBut;
  

  protected void startAssociator()
  {
    if (m_RunThread == null) {
      m_StartBut.setEnabled(false);
      m_StopBut.setEnabled(true);
      m_RunThread = new Thread()
      {
        public void run() {
          Messages.getInstance();m_Log.statusMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_StatusMessage_Text_First"));
          Instances inst = new Instances(m_Instances);
          
          Associator associator = (Associator)m_AssociatorEditor.getValue();
          StringBuffer outBuff = new StringBuffer();
          String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
          
          String cname = associator.getClass().getName();
          if (cname.startsWith("weka.associations.")) {
            name = name + cname.substring("weka.associations.".length());
          } else {
            name = name + cname;
          }
          String cmd = m_AssociatorEditor.getValue().getClass().getName();
          if ((m_AssociatorEditor.getValue() instanceof OptionHandler)) {
            cmd = cmd + " " + Utils.joinOptions(((OptionHandler)m_AssociatorEditor.getValue()).getOptions());
          }
          try
          {
            Messages.getInstance();m_Log.logMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_LogMessage_Text_First") + cname);
            Messages.getInstance();m_Log.logMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_LogMessage_Text_Second") + cmd);
            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskStarted();
            }
            Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_First"));
            Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_Second") + cname);
            if ((associator instanceof OptionHandler)) {
              String[] o = ((OptionHandler)associator).getOptions();
              outBuff.append(" " + Utils.joinOptions(o));
            }
            Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_Third"));
            Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_Fourth") + inst.relationName() + '\n');
            Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_Sixth") + inst.numInstances() + '\n');
            Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_Seventh") + inst.numAttributes() + '\n');
            if (inst.numAttributes() < 100) {
              for (int i = 0; i < inst.numAttributes(); i++) {
                outBuff.append("              " + inst.attribute(i).name() + '\n');
              }
            }
            else {
              Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_Eighth"));
            }
            m_History.addResult(name, outBuff);
            m_History.setSingle(name);
            

            Messages.getInstance();m_Log.statusMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_StatusMessage_Text_Second"));
            associator.buildAssociations(inst);
            Messages.getInstance();outBuff.append(Messages.getString("AssociationsPanel_StartAssociator_Run_OutBuffer_Text_Nineth"));
            outBuff.append(associator.toString() + '\n');
            m_History.updateResult(name);
            Messages.getInstance();m_Log.logMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_LogMessage_Text_Third") + cname);
            Messages.getInstance();m_Log.statusMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_StatusMessage_Text_Third"));
          } catch (Exception ex) {
            m_Log.logMessage(ex.getMessage());
            Messages.getInstance();m_Log.statusMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_StatusMessage_Text_Fourth"));
          } finally {
            if (isInterrupted()) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_LogMessage_Text_Fourth") + cname);
              Messages.getInstance();m_Log.statusMessage(Messages.getString("AssociationsPanel_StartAssociator_Run_Log_StatusMessage_Text_Fifth"));
            }
            m_RunThread = null;
            m_StartBut.setEnabled(true);
            m_StopBut.setEnabled(false);
            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskFinished();
            }
          }
        }
      };
      m_RunThread.setPriority(1);
      m_RunThread.start();
    }
  }
  



  protected void stopAssociator()
  {
    if (m_RunThread != null) {
      m_RunThread.interrupt();
      

      m_RunThread.stop();
    }
  }
  




  protected void saveBuffer(String name)
  {
    StringBuffer sb = m_History.getNamedBuffer(name);
    if ((sb != null) && 
      (m_SaveOut.save(sb))) {
      Messages.getInstance();m_Log.logMessage(Messages.getString("AssociationsPanel_SaveBuffer_Log_LogMessage_Text"));
    }
  }
  

  protected Instances m_Instances;
  
  protected Instances m_TestInstances;
  
  protected Thread m_RunThread;
  
  protected void historyRightClickPopup(String name, int x, int y)
  {
    final String selectedName = name;
    JPopupMenu resultListMenu = new JPopupMenu();
    
    Messages.getInstance();JMenuItem visMainBuffer = new JMenuItem(Messages.getString("AssociationsPanel_HistoryRightClickPopup_VisMainBuffer_JPopupMenu_Text"));
    if (selectedName != null) {
      visMainBuffer.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_History.setSingle(selectedName);
        }
      });
    } else {
      visMainBuffer.setEnabled(false);
    }
    resultListMenu.add(visMainBuffer);
    
    Messages.getInstance();JMenuItem visSepBuffer = new JMenuItem(Messages.getString("AssociationsPanel_HistoryRightClickPopup_VisSepBuffer_JMenuItem_Text"));
    if (selectedName != null) {
      visSepBuffer.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_History.openFrame(selectedName);
        }
      });
    } else {
      visSepBuffer.setEnabled(false);
    }
    resultListMenu.add(visSepBuffer);
    
    Messages.getInstance();JMenuItem saveOutput = new JMenuItem(Messages.getString("AssociationsPanel_HistoryRightClickPopup_SaveOutput_JMenuItem_Text"));
    if (selectedName != null) {
      saveOutput.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveBuffer(selectedName);
        }
      });
    } else {
      saveOutput.setEnabled(false);
    }
    resultListMenu.add(saveOutput);
    
    Messages.getInstance();JMenuItem deleteOutput = new JMenuItem(Messages.getString("AssociationsPanel_HistoryRightClickPopup_DeleteOutput_JMenuItem_Text"));
    if (selectedName != null) {
      deleteOutput.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_History.removeResult(selectedName);
        }
      });
    } else {
      deleteOutput.setEnabled(false);
    }
    resultListMenu.add(deleteOutput);
    
    resultListMenu.show(m_History.getList(), x, y);
  }
  







  protected void updateCapabilitiesFilter(Capabilities filter)
  {
    if (filter == null) {
      m_AssociatorEditor.setCapabilitiesFilter(new Capabilities(null)); return;
    }
    Instances tempInst;
    Instances tempInst;
    if (!ExplorerDefaults.getInitGenericObjectEditorFilter()) {
      tempInst = new Instances(m_Instances, 0);
    } else
      tempInst = new Instances(m_Instances);
    tempInst.setClassIndex(-1);
    Capabilities filterClass;
    try {
      filterClass = Capabilities.forInstances(tempInst);
    }
    catch (Exception e) {
      filterClass = new Capabilities(null);
    }
    
    m_AssociatorEditor.setCapabilitiesFilter(filterClass);
    
    m_StartBut.setEnabled(true);
    
    Capabilities currentFilter = m_AssociatorEditor.getCapabilitiesFilter();
    Associator associator = (Associator)m_AssociatorEditor.getValue();
    Capabilities currentSchemeCapabilities = null;
    if ((associator != null) && (currentFilter != null) && ((associator instanceof CapabilitiesHandler)))
    {
      currentSchemeCapabilities = ((CapabilitiesHandler)associator).getCapabilities();
      
      if ((!currentSchemeCapabilities.supportsMaybe(currentFilter)) && (!currentSchemeCapabilities.supports(currentFilter)))
      {
        m_StartBut.setEnabled(false);
      }
    }
  }
  




  public void capabilitiesFilterChanged(Explorer.CapabilitiesFilterChangeEvent e)
  {
    if (e.getFilter() == null) {
      updateCapabilitiesFilter(null);
    } else {
      updateCapabilitiesFilter((Capabilities)e.getFilter().clone());
    }
  }
  




  public void setExplorer(Explorer parent)
  {
    m_Explorer = parent;
  }
  




  public Explorer getExplorer()
  {
    return m_Explorer;
  }
  




  public String getTabTitle()
  {
    Messages.getInstance();return Messages.getString("AssociationsPanel_GetTabTitle_Text");
  }
  




  public String getTabTitleToolTip()
  {
    Messages.getInstance();return Messages.getString("AssociationsPanel_GetTabTitleToolTip_Text");
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AssociationsPanel_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      AssociationsPanel sp = new AssociationsPanel();
      jf.getContentPane().add(sp, "Center");
      LogPanel lp = new LogPanel();
      sp.setLog(lp);
      jf.getContentPane().add(lp, "South");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      if (args.length == 1) {
        Messages.getInstance();System.err.println(Messages.getString("AssociationsPanel_Main_Error_Text") + args[0]);
        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances i = new Instances(r);
        sp.setInstances(i);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
  static {}
}

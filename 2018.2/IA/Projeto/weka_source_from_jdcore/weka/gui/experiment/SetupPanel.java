package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import weka.core.Utils;
import weka.core.xml.KOML;
import weka.experiment.Experiment;
import weka.experiment.PropertyNode;
import weka.experiment.RemoteExperiment;
import weka.experiment.ResultListener;
import weka.experiment.ResultProducer;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;













































































































public class SetupPanel
  extends JPanel
{
  private static final long serialVersionUID = 6552671886903170033L;
  protected Experiment m_Exp;
  protected JButton m_OpenBut;
  protected JButton m_SaveBut;
  protected JButton m_NewBut;
  protected FileFilter m_ExpFilter;
  protected FileFilter m_KOMLFilter;
  protected FileFilter m_XMLFilter;
  protected JFileChooser m_FileChooser;
  protected GenericObjectEditor m_RPEditor;
  protected PropertyPanel m_RPEditorPanel;
  protected GenericObjectEditor m_RLEditor;
  protected PropertyPanel m_RLEditorPanel;
  protected GeneratorPropertyIteratorPanel m_GeneratorPropertyPanel;
  protected RunNumberPanel m_RunNumberPanel;
  protected DistributeExperimentPanel m_DistributeExperimentPanel;
  protected DatasetListPanel m_DatasetListPanel;
  protected JButton m_NotesButton;
  protected JFrame m_NotesFrame;
  protected JTextArea m_NotesText;
  protected PropertyChangeSupport m_Support;
  protected JRadioButton m_advanceDataSetFirst;
  protected JRadioButton m_advanceIteratorFirst;
  ActionListener m_RadioListener;
  
  public SetupPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  
  public SetupPanel()
  {
    Messages.getInstance();m_OpenBut = new JButton(Messages.getString("SetupPanel_OpenBut_JButton_Text"));
    

    Messages.getInstance();m_SaveBut = new JButton(Messages.getString("SetupPanel_SaveBut_JButton_Text"));
    

    Messages.getInstance();m_NewBut = new JButton(Messages.getString("SetupPanel_NewBut_JButton_Text"));
    

    Messages.getInstance();Messages.getInstance();m_ExpFilter = new ExtensionFileFilter(Experiment.FILE_EXTENSION, Messages.getString("SetupPanel_ExpFilter_ExtensionFileFilter_Text_First") + Experiment.FILE_EXTENSION + Messages.getString("SetupPanel_ExpFilter_ExtensionFileFilter_Text_Second"));
    



    Messages.getInstance();Messages.getInstance();m_KOMLFilter = new ExtensionFileFilter(".koml", Messages.getString("SetupPanel_KOMLFilter_ExtensionFileFilter_Text_First") + ".koml" + Messages.getString("SetupPanel_KOMLFilter_ExtensionFileFilter_Text_Second"));
    



    Messages.getInstance();m_XMLFilter = new ExtensionFileFilter(".xml", Messages.getString("SetupPanel_XMLFilter_ExtensionFileFilter_Text"));
    



    m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    

    m_RPEditor = new GenericObjectEditor();
    

    m_RPEditorPanel = new PropertyPanel(m_RPEditor);
    

    m_RLEditor = new GenericObjectEditor();
    

    m_RLEditorPanel = new PropertyPanel(m_RLEditor);
    

    m_GeneratorPropertyPanel = new GeneratorPropertyIteratorPanel();
    


    m_RunNumberPanel = new RunNumberPanel();
    

    m_DistributeExperimentPanel = new DistributeExperimentPanel();
    


    m_DatasetListPanel = new DatasetListPanel();
    

    Messages.getInstance();m_NotesButton = new JButton(Messages.getString("SetupPanel_NotesButton_JButton_Text"));
    

    Messages.getInstance();m_NotesFrame = new JFrame(Messages.getString("SetupPanel_NotesFrame_JFrame_Text"));
    

    m_NotesText = new JTextArea(null, 10, 0);
    




    m_Support = new PropertyChangeSupport(this);
    

    Messages.getInstance();m_advanceDataSetFirst = new JRadioButton(Messages.getString("SetupPanel_AdvanceDataSetFirst_JRadioButton_Text"));
    


    Messages.getInstance();m_advanceIteratorFirst = new JRadioButton(Messages.getString("SetupPanel_AdvanceIteratorFirst_JRadioButton_Text"));
    


    m_RadioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SetupPanel.this.updateRadioLinks();










      }
      











    };
    m_DistributeExperimentPanel.addCheckBoxActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_DistributeExperimentPanel.distributedExperimentSelected()) {
          if (!(m_Exp instanceof RemoteExperiment)) {
            try {
              RemoteExperiment re = new RemoteExperiment(m_Exp);
              setExperiment(re);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        }
        else if ((m_Exp instanceof RemoteExperiment)) {
          setExperiment(((RemoteExperiment)m_Exp).getBaseExperiment());
        }
        
      }
      
    });
    m_NewBut.setMnemonic('N');
    m_NewBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setExperiment(new Experiment());
      }
    });
    m_SaveBut.setMnemonic('S');
    m_SaveBut.setEnabled(false);
    m_SaveBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SetupPanel.this.saveExperiment();
      }
    });
    m_OpenBut.setMnemonic('O');
    m_OpenBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SetupPanel.this.openExperiment();
      }
    });
    m_FileChooser.addChoosableFileFilter(m_ExpFilter);
    if (KOML.isPresent())
      m_FileChooser.addChoosableFileFilter(m_KOMLFilter);
    m_FileChooser.addChoosableFileFilter(m_XMLFilter);
    m_FileChooser.setFileFilter(m_ExpFilter);
    m_FileChooser.setFileSelectionMode(0);
    
    m_GeneratorPropertyPanel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SetupPanel.this.updateRadioLinks();
      }
      
    });
    m_RPEditor.setClassType(ResultProducer.class);
    m_RPEditor.setEnabled(false);
    m_RPEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        m_Exp.setResultProducer((ResultProducer)m_RPEditor.getValue());
        m_Exp.setUsePropertyIterator(false);
        m_Exp.setPropertyArray(null);
        m_Exp.setPropertyPath(null);
        m_GeneratorPropertyPanel.setExperiment(m_Exp);
        repaint();
      }
      
    });
    m_RLEditor.setClassType(ResultListener.class);
    m_RLEditor.setEnabled(false);
    m_RLEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        m_Exp.setResultListener((ResultListener)m_RLEditor.getValue());
        m_Support.firePropertyChange("", null, null);
        repaint();
      }
      
    });
    m_NotesFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        m_NotesButton.setEnabled(true);
      }
    });
    m_NotesFrame.getContentPane().add(new JScrollPane(m_NotesText));
    m_NotesFrame.setSize(600, 400);
    
    m_NotesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_NotesButton.setEnabled(false);
        m_NotesFrame.setVisible(true);
      }
    });
    m_NotesButton.setEnabled(false);
    
    m_NotesText.setEditable(true);
    
    m_NotesText.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        m_Exp.setNotes(m_NotesText.getText());
      }
    });
    m_NotesText.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        m_Exp.setNotes(m_NotesText.getText());
      }
      

    });
    JPanel buttons = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    buttons.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    
    buttons.setLayout(gb);
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    buttons.add(m_OpenBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    buttons.add(m_SaveBut, constraints);
    gridx = 2;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    buttons.add(m_NewBut, constraints);
    
    JPanel src = new JPanel();
    src.setLayout(new BorderLayout());
    Messages.getInstance();src.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SetupPanel_Src_JPane_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    src.add(m_RPEditorPanel, "North");
    m_RPEditorPanel.setEnabled(false);
    
    JPanel dest = new JPanel();
    dest.setLayout(new BorderLayout());
    Messages.getInstance();dest.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SetupPanel_Dest_JPane_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    dest.add(m_RLEditorPanel, "North");
    m_RLEditorPanel.setEnabled(false);
    
    m_advanceDataSetFirst.setEnabled(false);
    m_advanceIteratorFirst.setEnabled(false);
    Messages.getInstance();m_advanceDataSetFirst.setToolTipText(Messages.getString("SetupPanel_AdvanceDataSetFirst_SetToolTipText_Text"));
    
    Messages.getInstance();m_advanceIteratorFirst.setToolTipText(Messages.getString("SetupPanel_AdvanceIteratorFirst_SetToolTipText_Text"));
    
    m_advanceDataSetFirst.setSelected(true);
    ButtonGroup bg = new ButtonGroup();
    bg.add(m_advanceDataSetFirst);
    bg.add(m_advanceIteratorFirst);
    m_advanceDataSetFirst.addActionListener(m_RadioListener);
    m_advanceIteratorFirst.addActionListener(m_RadioListener);
    
    JPanel radioButs = new JPanel();
    Messages.getInstance();radioButs.setBorder(BorderFactory.createTitledBorder(Messages.getString("SetupPanel_RadioButs_JPanel_BorderFactoryCreateTitledBorder_Text")));
    
    radioButs.setLayout(new GridLayout(1, 2));
    radioButs.add(m_advanceDataSetFirst);
    radioButs.add(m_advanceIteratorFirst);
    
    JPanel simpleIterators = new JPanel();
    simpleIterators.setLayout(new BorderLayout());
    
    JPanel tmp = new JPanel();
    tmp.setLayout(new GridBagLayout());
    
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    tmp.add(m_RunNumberPanel, constraints);
    
    gridx = 1;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 2;
    tmp.add(m_DistributeExperimentPanel, constraints);
    
    JPanel tmp2 = new JPanel();
    
    tmp2.setLayout(new GridBagLayout());
    
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    tmp2.add(tmp, constraints);
    
    gridx = 0;gridy = 1;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    tmp2.add(radioButs, constraints);
    

    simpleIterators.add(tmp2, "North");
    simpleIterators.add(m_DatasetListPanel, "Center");
    JPanel iterators = new JPanel();
    iterators.setLayout(new GridLayout(1, 2));
    iterators.add(simpleIterators);
    iterators.add(m_GeneratorPropertyPanel);
    
    JPanel top = new JPanel();
    top.setLayout(new GridLayout(2, 1));
    top.add(dest);
    top.add(src);
    
    JPanel notes = new JPanel();
    notes.setLayout(new BorderLayout());
    notes.add(m_NotesButton, "Center");
    
    JPanel p2 = new JPanel();
    
    p2.setLayout(new BorderLayout());
    p2.add(iterators, "Center");
    p2.add(notes, "South");
    
    JPanel p3 = new JPanel();
    p3.setLayout(new BorderLayout());
    p3.add(buttons, "North");
    p3.add(top, "South");
    setLayout(new BorderLayout());
    add(p3, "North");
    add(p2, "Center");
  }
  


  protected void removeNotesFrame()
  {
    m_NotesFrame.setVisible(false);
  }
  





  public void setExperiment(Experiment exp)
  {
    boolean iteratorOn = exp.getUsePropertyIterator();
    Object propArray = exp.getPropertyArray();
    PropertyNode[] propPath = exp.getPropertyPath();
    
    m_Exp = exp;
    m_SaveBut.setEnabled(true);
    m_RPEditor.setValue(m_Exp.getResultProducer());
    m_RPEditor.setEnabled(true);
    m_RPEditorPanel.setEnabled(true);
    m_RPEditorPanel.repaint();
    m_RLEditor.setValue(m_Exp.getResultListener());
    m_RLEditor.setEnabled(true);
    m_RLEditorPanel.setEnabled(true);
    m_RLEditorPanel.repaint();
    
    m_NotesText.setText(exp.getNotes());
    m_NotesButton.setEnabled(true);
    
    m_advanceDataSetFirst.setSelected(m_Exp.getAdvanceDataSetFirst());
    m_advanceIteratorFirst.setSelected(!m_Exp.getAdvanceDataSetFirst());
    m_advanceDataSetFirst.setEnabled(true);
    m_advanceIteratorFirst.setEnabled(true);
    
    exp.setPropertyPath(propPath);
    exp.setPropertyArray(propArray);
    exp.setUsePropertyIterator(iteratorOn);
    
    m_GeneratorPropertyPanel.setExperiment(m_Exp);
    m_RunNumberPanel.setExperiment(m_Exp);
    m_DatasetListPanel.setExperiment(m_Exp);
    m_DistributeExperimentPanel.setExperiment(m_Exp);
    m_Support.firePropertyChange("", null, null);
  }
  





  public Experiment getExperiment()
  {
    return m_Exp;
  }
  



  private void openExperiment()
  {
    int returnVal = m_FileChooser.showOpenDialog(this);
    if (returnVal != 0) {
      return;
    }
    File expFile = m_FileChooser.getSelectedFile();
    

    if (m_FileChooser.getFileFilter() == m_ExpFilter) {
      if (!expFile.getName().toLowerCase().endsWith(Experiment.FILE_EXTENSION)) {
        expFile = new File(expFile.getParent(), expFile.getName() + Experiment.FILE_EXTENSION);
      }
    } else if (m_FileChooser.getFileFilter() == m_KOMLFilter) {
      if (!expFile.getName().toLowerCase().endsWith(".koml")) {
        expFile = new File(expFile.getParent(), expFile.getName() + ".koml");
      }
    } else if ((m_FileChooser.getFileFilter() == m_XMLFilter) && 
      (!expFile.getName().toLowerCase().endsWith(".xml"))) {
      expFile = new File(expFile.getParent(), expFile.getName() + ".xml");
    }
    try
    {
      Experiment exp = Experiment.read(expFile.getAbsolutePath());
      setExperiment(exp);
      Messages.getInstance();System.err.println(Messages.getString("SetupPanel_OpenExperiment_Error_Text") + m_Exp);
    } catch (Exception ex) {
      ex.printStackTrace();
      Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SetupPanel_OpenExperiment_Exception_JOptionPaneShowMessageDialog_Text_First") + expFile + Messages.getString("SetupPanel_OpenExperiment_Exception_JOptionPaneShowMessageDialog_Text_Second") + ex.getMessage(), Messages.getString("SetupPanel_OpenExperiment_Exception_JOptionPaneShowMessageDialog_Text_Third"), 0);
    }
  }
  









  private void saveExperiment()
  {
    int returnVal = m_FileChooser.showSaveDialog(this);
    if (returnVal != 0) {
      return;
    }
    File expFile = m_FileChooser.getSelectedFile();
    

    if (m_FileChooser.getFileFilter() == m_ExpFilter) {
      if (!expFile.getName().toLowerCase().endsWith(Experiment.FILE_EXTENSION)) {
        expFile = new File(expFile.getParent(), expFile.getName() + Experiment.FILE_EXTENSION);
      }
    } else if (m_FileChooser.getFileFilter() == m_KOMLFilter) {
      if (!expFile.getName().toLowerCase().endsWith(".koml")) {
        expFile = new File(expFile.getParent(), expFile.getName() + ".koml");
      }
    } else if ((m_FileChooser.getFileFilter() == m_XMLFilter) && 
      (!expFile.getName().toLowerCase().endsWith(".xml"))) {
      expFile = new File(expFile.getParent(), expFile.getName() + ".xml");
    }
    try
    {
      Experiment.write(expFile.getAbsolutePath(), m_Exp);
      Messages.getInstance();System.err.println(Messages.getString("SetupPanel_SaveExperiment_Error_Text") + m_Exp);
    } catch (Exception ex) {
      ex.printStackTrace();
      Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SetupPanel_SaveExperiment_Exception_JOptionPaneShowMessageDialog_Text_First") + expFile + Messages.getString("SetupPanel_SaveExperiment_Exception_JOptionPaneShowMessageDialog_Text_Second") + ex.getMessage(), Messages.getString("SetupPanel_SaveExperiment_Exception_JOptionPaneShowMessageDialog_Text_Third"), 0);
    }
  }
  








  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.addPropertyChangeListener(l);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.removePropertyChangeListener(l);
  }
  



  private void updateRadioLinks()
  {
    m_advanceDataSetFirst.setEnabled(m_GeneratorPropertyPanel.getEditorActive());
    
    m_advanceIteratorFirst.setEnabled(m_GeneratorPropertyPanel.getEditorActive());
    

    if (m_Exp != null) {
      if (!m_GeneratorPropertyPanel.getEditorActive()) {
        m_Exp.setAdvanceDataSetFirst(true);
      } else {
        m_Exp.setAdvanceDataSetFirst(m_advanceDataSetFirst.isSelected());
      }
    }
  }
  




  public static void main(String[] args)
  {
    try
    {
      boolean readExp = Utils.getFlag('l', args);
      final boolean writeExp = Utils.getFlag('s', args);
      final String expFile = Utils.getOption('f', args);
      if (((readExp) || (writeExp)) && (expFile.length() == 0)) {
        Messages.getInstance();throw new Exception(Messages.getString("SetupPanel_Main_Exception_Text"));
      }
      Experiment exp = null;
      if (readExp) {
        FileInputStream fi = new FileInputStream(expFile);
        ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
        
        exp = (Experiment)oi.readObject();
        oi.close();
      } else {
        exp = new Experiment();
      }
      Messages.getInstance();System.err.println(Messages.getString("SetupPanel_Main_Error_Text_First") + exp.toString());
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("SetupPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      SetupPanel sp = new SetupPanel();
      
      jf.getContentPane().add(sp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          Messages.getInstance();System.err.println(Messages.getString("SetupPanel_Main_WindowClosing_Error_Text_First") + val$sp.m_Exp.toString());
          

          if (writeExp) {
            try {
              FileOutputStream fo = new FileOutputStream(expFile);
              ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(fo));
              
              oo.writeObject(val$sp.m_Exp);
              oo.close();
            } catch (Exception ex) {
              ex.printStackTrace();
              Messages.getInstance();System.err.println(Messages.getString("SetupPanel_Main_WindowClosing_Error_Text_Second") + expFile + '\n' + ex.getMessage());
            }
          }
          
          jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      Messages.getInstance();System.err.println(Messages.getString("SetupPanel_Main_Error_Text_Second"));
      Thread.currentThread();Thread.sleep(3000L);
      Messages.getInstance();System.err.println(Messages.getString("SetupPanel_Main_Error_Text_Third"));
      sp.setExperiment(exp);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
  static {}
}

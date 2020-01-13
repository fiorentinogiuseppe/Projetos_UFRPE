package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.xml.XMLClassifier;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.experiment.Experiment;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.GenericObjectEditor.GOEPanel;
import weka.gui.JListHelper;
import weka.gui.PropertyDialog;



































public class AlgorithmListPanel
  extends JPanel
  implements ActionListener
{
  private static final long serialVersionUID = -7204528834764898671L;
  protected Experiment m_Exp;
  protected JList m_List;
  protected JButton m_AddBut;
  protected JButton m_EditBut;
  protected JButton m_DeleteBut;
  protected JButton m_LoadOptionsBut;
  protected JButton m_SaveOptionsBut;
  protected JButton m_UpBut;
  protected JButton m_DownBut;
  protected JFileChooser m_FileChooser;
  protected FileFilter m_XMLFilter;
  protected boolean m_Editing;
  protected GenericObjectEditor m_ClassifierEditor;
  protected PropertyDialog m_PD;
  protected DefaultListModel m_AlgorithmListModel;
  
  public class ObjectCellRenderer
    extends DefaultListCellRenderer
  {
    private static final long serialVersionUID = -5067138526587433808L;
    
    public ObjectCellRenderer() {}
    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      String rep = value.getClass().getName();
      int dotPos = rep.lastIndexOf('.');
      if (dotPos != -1) {
        rep = rep.substring(dotPos + 1);
      }
      if ((value instanceof OptionHandler)) {
        rep = rep + " " + Utils.joinOptions(((OptionHandler)value).getOptions());
      }
      
      setText(rep);
      return c;
    }
  }
  



























































  public AlgorithmListPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  
  public AlgorithmListPanel()
  {
    Messages.getInstance();m_AddBut = new JButton(Messages.getString("AlgorithmListPanel_AddBut_JButton_Text"));
    

    Messages.getInstance();m_EditBut = new JButton(Messages.getString("AlgorithmListPanel_EditBut_JButton_Text"));
    

    Messages.getInstance();m_DeleteBut = new JButton(Messages.getString("AlgorithmListPanel_DeleteBut_JButton_Text"));
    

    Messages.getInstance();m_LoadOptionsBut = new JButton(Messages.getString("AlgorithmListPanel_LoadOptionsBut_JButton_Text"));
    

    Messages.getInstance();m_SaveOptionsBut = new JButton(Messages.getString("AlgorithmListPanel_SaveOptionsBut_JButton_Text"));
    

    Messages.getInstance();m_UpBut = new JButton(Messages.getString("AlgorithmListPanel_UpBut_JButton_Text"));
    

    Messages.getInstance();m_DownBut = new JButton(Messages.getString("AlgorithmListPanel_DownBut_JButton_Text"));
    

    m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    


    Messages.getInstance();m_XMLFilter = new ExtensionFileFilter(".xml", Messages.getString("AlgorithmListPanel_XMLFilter_Text_End"));
    



    m_Editing = false;
    

    m_ClassifierEditor = new GenericObjectEditor(true);
    





    m_AlgorithmListModel = new DefaultListModel();
    




















    final AlgorithmListPanel self = this;
    m_List = new JList();
    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        final int index = m_List.locationToIndex(e.getPoint());
        
        if ((e.getClickCount() == 2) && (e.getButton() == 1))
        {



          if (index > -1) {
            actionPerformed(new ActionEvent(m_EditBut, 0, ""));
          }
        } else if ((e.getClickCount() == 1) && (
          (e.getButton() == 3) || ((e.getButton() == 1) && (e.isAltDown()) && (e.isShiftDown()))))
        {
          JPopupMenu menu = new JPopupMenu();
          

          Messages.getInstance();JMenuItem item = new JMenuItem(Messages.getString("AlgorithmListPanel_Item_JMenuItem_Text_First"));
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              Messages.getInstance();String str = JOptionPane.showInputDialog(val$self, Messages.getString("AlgorithmListPanel_JOptionPaneShowInputDialog_Text"));
              

              if (str != null) {
                try {
                  String[] options = Utils.splitOptions(str);
                  String classname = options[0];
                  options[0] = "";
                  Object obj = Utils.forName(Object.class, classname, options);
                  m_AlgorithmListModel.addElement(obj);
                  AlgorithmListPanel.this.updateExperiment();
                }
                catch (Exception ex) {
                  ex.printStackTrace();
                  Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(val$self, Messages.getString("AlgorithmListPanel_Error_JOptionPaneShowMessageDialog_Text_Front") + ex, Messages.getString("AlgorithmListPanel_Error_JOptionPaneShowMessageDialog_Text_End"), 0);
                }
                
              }
              
            }
            

          });
          menu.add(item);
          
          if (m_List.getSelectedValue() != null) {
            menu.addSeparator();
            
            Messages.getInstance();item = new JMenuItem(Messages.getString("AlgorithmListPanel_Item_JMenuItem_Text_Second"));
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                val$self.actionPerformed(new ActionEvent(m_EditBut, 0, ""));
              }
            });
            menu.add(item);
            
            Messages.getInstance();item = new JMenuItem(Messages.getString("AlgorithmListPanel_Item_JMenuItem_Text_Third"));
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                String str = m_List.getSelectedValue().getClass().getName();
                if ((m_List.getSelectedValue() instanceof OptionHandler))
                  str = str + " " + Utils.joinOptions(((OptionHandler)m_List.getSelectedValue()).getOptions());
                StringSelection selection = new StringSelection(str.trim());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
              }
            });
            menu.add(item);
            
            Messages.getInstance();item = new JMenuItem(Messages.getString("AlgorithmListPanel_Item_JMenuItem_Text_Fourth"));
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Messages.getInstance();String str = JOptionPane.showInputDialog(val$self, Messages.getString("AlgorithmListPanel_ActionPerformed_JOptionPaneShowInputDialog_Text"));
                

                if (str != null) {
                  try {
                    String[] options = Utils.splitOptions(str);
                    String classname = options[0];
                    options[0] = "";
                    Object obj = Utils.forName(Object.class, classname, options);
                    m_AlgorithmListModel.setElementAt(obj, index);
                    AlgorithmListPanel.this.updateExperiment();
                  }
                  catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(val$self, Messages.getString("AlgorithmListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_First") + ex, Messages.getString("AlgorithmListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_Second"), 0);
                  }
                  
                }
                
              }
              

            });
            menu.add(item);
          }
          
          menu.show(m_List, e.getX(), e.getY());
        }
        
      }
    };
    m_List.addMouseListener(mouseListener);
    
    m_ClassifierEditor.setClassType(Classifier.class);
    m_ClassifierEditor.setValue(new ZeroR());
    m_ClassifierEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        repaint();
      }
    });
    ((GenericObjectEditor.GOEPanel)m_ClassifierEditor.getCustomEditor()).addOkListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Classifier newCopy = (Classifier)copyObject(m_ClassifierEditor.getValue());
        
        AlgorithmListPanel.this.addNewAlgorithm(newCopy);
      }
      
    });
    m_DeleteBut.setEnabled(false);
    m_DeleteBut.addActionListener(this);
    m_AddBut.setEnabled(false);
    m_AddBut.addActionListener(this);
    m_EditBut.setEnabled(false);
    m_EditBut.addActionListener(this);
    m_LoadOptionsBut.setEnabled(false);
    m_LoadOptionsBut.addActionListener(this);
    m_SaveOptionsBut.setEnabled(false);
    m_SaveOptionsBut.addActionListener(this);
    m_UpBut.setEnabled(false);
    m_UpBut.addActionListener(this);
    m_DownBut.setEnabled(false);
    m_DownBut.addActionListener(this);
    
    m_List.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        AlgorithmListPanel.this.setButtons(e);
      }
      
    });
    m_FileChooser.addChoosableFileFilter(m_XMLFilter);
    m_FileChooser.setFileSelectionMode(0);
    
    setLayout(new BorderLayout());
    Messages.getInstance();setBorder(BorderFactory.createTitledBorder(Messages.getString("AlgorithmListPanel_SetBorder_Text")));
    JPanel topLab = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    topLab.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    topLab.setLayout(gb);
    
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    topLab.add(m_AddBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    topLab.add(m_EditBut, constraints);
    gridx = 2;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    topLab.add(m_DeleteBut, constraints);
    
    JPanel bottomLab = new JPanel();
    gb = new GridBagLayout();
    constraints = new GridBagConstraints();
    bottomLab.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    bottomLab.setLayout(gb);
    
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    bottomLab.add(m_LoadOptionsBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    bottomLab.add(m_SaveOptionsBut, constraints);
    gridx = 2;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    bottomLab.add(m_UpBut, constraints);
    gridx = 3;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    bottomLab.add(m_DownBut, constraints);
    
    add(topLab, "North");
    add(new JScrollPane(m_List), "Center");
    add(bottomLab, "South");
  }
  





  public void setExperiment(Experiment exp)
  {
    m_Exp = exp;
    m_AddBut.setEnabled(true);
    m_List.setModel(m_AlgorithmListModel);
    m_List.setCellRenderer(new ObjectCellRenderer());
    m_AlgorithmListModel.removeAllElements();
    if ((m_Exp.getPropertyArray() instanceof Classifier[])) {
      Classifier[] algorithms = (Classifier[])m_Exp.getPropertyArray();
      for (int i = 0; i < algorithms.length; i++) {
        m_AlgorithmListModel.addElement(algorithms[i]);
      }
    }
    m_EditBut.setEnabled(m_AlgorithmListModel.size() > 0);
    m_DeleteBut.setEnabled(m_AlgorithmListModel.size() > 0);
    m_LoadOptionsBut.setEnabled(m_AlgorithmListModel.size() > 0);
    m_SaveOptionsBut.setEnabled(m_AlgorithmListModel.size() > 0);
    m_UpBut.setEnabled(JListHelper.canMoveUp(m_List));
    m_DownBut.setEnabled(JListHelper.canMoveDown(m_List));
  }
  




  private void addNewAlgorithm(Classifier newScheme)
  {
    if (!m_Editing) {
      m_AlgorithmListModel.addElement(newScheme);
    } else {
      m_AlgorithmListModel.setElementAt(newScheme, m_List.getSelectedIndex());
    }
    updateExperiment();
    
    m_Editing = false;
  }
  


  private void updateExperiment()
  {
    Classifier[] cArray = new Classifier[m_AlgorithmListModel.size()];
    for (int i = 0; i < cArray.length; i++) {
      cArray[i] = ((Classifier)m_AlgorithmListModel.elementAt(i));
    }
    m_Exp.setPropertyArray(cArray);
  }
  





  private void setButtons(ListSelectionEvent e)
  {
    if (e.getSource() == m_List) {
      m_DeleteBut.setEnabled(m_List.getSelectedIndex() > -1);
      m_AddBut.setEnabled(true);
      m_EditBut.setEnabled(m_List.getSelectedIndices().length == 1);
      m_LoadOptionsBut.setEnabled(m_List.getSelectedIndices().length == 1);
      m_SaveOptionsBut.setEnabled(m_List.getSelectedIndices().length == 1);
      m_UpBut.setEnabled(JListHelper.canMoveUp(m_List));
      m_DownBut.setEnabled(JListHelper.canMoveDown(m_List));
    }
  }
  





  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == m_AddBut) {
      m_Editing = false;
      if (m_PD == null) {
        int x = getLocationOnScreenx;
        int y = getLocationOnScreeny;
        if (PropertyDialog.getParentDialog(this) != null) {
          m_PD = new PropertyDialog(PropertyDialog.getParentDialog(this), m_ClassifierEditor, x, y);
        }
        else
        {
          m_PD = new PropertyDialog(PropertyDialog.getParentFrame(this), m_ClassifierEditor, x, y);
        }
        
        m_PD.setVisible(true);
      } else {
        m_PD.setVisible(true);
      }
    }
    else if (e.getSource() == m_EditBut) {
      if (m_List.getSelectedValue() != null) {
        m_ClassifierEditor.setClassType(Classifier.class);
        
        m_ClassifierEditor.setValue(m_List.getSelectedValue());
        m_Editing = true;
        if (m_PD == null) {
          int x = getLocationOnScreenx;
          int y = getLocationOnScreeny;
          if (PropertyDialog.getParentDialog(this) != null) {
            m_PD = new PropertyDialog(PropertyDialog.getParentDialog(this), m_ClassifierEditor, x, y);
          }
          else
          {
            m_PD = new PropertyDialog(PropertyDialog.getParentFrame(this), m_ClassifierEditor, x, y);
          }
          
          m_PD.setVisible(true);
        } else {
          m_PD.setVisible(true);
        }
      }
    }
    else if (e.getSource() == m_DeleteBut)
    {
      int[] selected = m_List.getSelectedIndices();
      if (selected != null) {
        for (int i = selected.length - 1; i >= 0; i--) {
          int current = selected[i];
          m_AlgorithmListModel.removeElementAt(current);
          if (m_Exp.getDatasets().size() > current) {
            m_List.setSelectedIndex(current);
          } else {
            m_List.setSelectedIndex(current - 1);
          }
        }
      }
      if (m_List.getSelectedIndex() == -1) {
        m_EditBut.setEnabled(false);
        m_DeleteBut.setEnabled(false);
        m_LoadOptionsBut.setEnabled(false);
        m_SaveOptionsBut.setEnabled(false);
        m_UpBut.setEnabled(false);
        m_DownBut.setEnabled(false);
      }
      
      updateExperiment();
    } else if (e.getSource() == m_LoadOptionsBut) {
      if (m_List.getSelectedValue() != null) {
        int returnVal = m_FileChooser.showOpenDialog(this);
        if (returnVal == 0) {
          try {
            File file = m_FileChooser.getSelectedFile();
            if (!file.getAbsolutePath().toLowerCase().endsWith(".xml"))
              file = new File(file.getAbsolutePath() + ".xml");
            XMLClassifier xmlcls = new XMLClassifier();
            Classifier c = (Classifier)xmlcls.read(file);
            m_AlgorithmListModel.setElementAt(c, m_List.getSelectedIndex());
            updateExperiment();
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    } else if (e.getSource() == m_SaveOptionsBut) {
      if (m_List.getSelectedValue() != null) {
        int returnVal = m_FileChooser.showSaveDialog(this);
        if (returnVal == 0) {
          try {
            File file = m_FileChooser.getSelectedFile();
            if (!file.getAbsolutePath().toLowerCase().endsWith(".xml"))
              file = new File(file.getAbsolutePath() + ".xml");
            XMLClassifier xmlcls = new XMLClassifier();
            xmlcls.write(file, m_List.getSelectedValue());
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    }
    else if (e.getSource() == m_UpBut) {
      JListHelper.moveUp(m_List);
      updateExperiment();
    }
    else if (e.getSource() == m_DownBut) {
      JListHelper.moveDown(m_List);
      updateExperiment();
    }
  }
  





  protected Object copyObject(Object source)
  {
    Object result = null;
    try {
      SerializedObject so = new SerializedObject(source);
      result = so.getObject();
    } catch (Exception ex) {
      Messages.getInstance();System.err.println(Messages.getString("AlgorithmListPanel_CopyObject_Error_Text"));
      System.err.println(ex);
    }
    return result;
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AlgorithmListPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      AlgorithmListPanel dp = new AlgorithmListPanel();
      jf.getContentPane().add(dp, "Center");
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      Messages.getInstance();System.err.println(Messages.getString("AlgorithmListPanel_Main_Error_Text_First"));
      Thread.currentThread();Thread.sleep(3000L);
      Messages.getInstance();System.err.println(Messages.getString("AlgorithmListPanel_Main_Error_Text_Second"));
      dp.setExperiment(new Experiment());
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
  static {}
}

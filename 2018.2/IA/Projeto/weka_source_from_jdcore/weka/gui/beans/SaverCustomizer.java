package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import weka.core.converters.DatabaseConverter;
import weka.core.converters.DatabaseSaver;
import weka.core.converters.FileSourcedConverter;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertySheetPanel;





































public class SaverCustomizer
  extends JPanel
  implements Customizer, CustomizerCloseRequester
{
  private static final long serialVersionUID = -4874208115942078471L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private Saver m_dsSaver;
  
  private PropertySheetPanel m_SaverEditor = new PropertySheetPanel();
  

  private JFileChooser m_fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  

  private JFrame m_parentFrame;
  

  private JTextField m_dbaseURLText;
  
  private JTextField m_userNameText;
  
  private JPasswordField m_passwordText;
  
  private JTextField m_tableText;
  
  private JComboBox m_idBox;
  
  private JComboBox m_tabBox;
  
  private JTextField m_prefixText;
  
  private JCheckBox m_relativeFilePath;
  
  private JCheckBox m_relationNameForFilename;
  

  public SaverCustomizer()
  {
    try
    {
      m_SaverEditor.addPropertyChangeListener(new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e) {
          repaint();
          if (m_dsSaver != null) {
            Messages.getInstance();System.err.println(Messages.getString("SaverCustomizer_Error_Text"));
            m_dsSaver.setSaverTemplate(m_dsSaver.getSaverTemplate());
          }
        }
      });
      repaint();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    setLayout(new BorderLayout());
    m_fileChooser.setDialogType(1);
    m_fileChooser.setFileSelectionMode(1);
    Messages.getInstance();m_fileChooser.setApproveButtonText(Messages.getString("SaverCustomizer_FileChooser_SetApproveButtonText_Text"));
    m_fileChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ApproveSelection")) {
          try {
            m_dsSaver.getSaverTemplate().setFilePrefix(m_prefixText.getText());
            m_dsSaver.getSaverTemplate().setDir(m_fileChooser.getSelectedFile().getPath());
            m_dsSaver.setRelationNameForFilename(m_relationNameForFilename.isSelected());

          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
        
        if (m_parentFrame != null) {
          m_parentFrame.dispose();
        }
      }
    });
  }
  
  public void setParentFrame(JFrame parent) {
    m_parentFrame = parent;
  }
  


  private void setUpOther()
  {
    removeAll();
    add(m_SaverEditor, "Center");
    validate();
    repaint();
  }
  

  private void setUpDatabase()
  {
    removeAll();
    JPanel db = new JPanel();
    db.setLayout(new GridLayout(7, 1));
    m_dbaseURLText = new JTextField(((DatabaseConverter)m_dsSaver.getSaverTemplate()).getUrl(), 50);
    Messages.getInstance();JLabel dbaseURLLab = new JLabel(Messages.getString("SaverCustomizer_SetUpDatabase_DbaseURLLab_JLabel_Text"), 2);
    dbaseURLLab.setFont(new Font("Monospaced", 0, 12));
    
    m_userNameText = new JTextField(((DatabaseConverter)m_dsSaver.getSaverTemplate()).getUser(), 50);
    Messages.getInstance();JLabel userNameLab = new JLabel(Messages.getString("SaverCustomizer_SetUpDatabase_UserNameLab_JLabel_Text"), 2);
    userNameLab.setFont(new Font("Monospaced", 0, 12));
    
    m_passwordText = new JPasswordField(50);
    m_passwordText.setText(((DatabaseSaver)m_dsSaver.getSaverTemplate()).getPassword());
    Messages.getInstance();JLabel passwordLab = new JLabel(Messages.getString("SaverCustomizer_SetUpDatabase_PasswordLab_JLabel_Text"), 2);
    passwordLab.setFont(new Font("Monospaced", 0, 12));
    
    m_tableText = new JTextField(((DatabaseSaver)m_dsSaver.getSaverTemplate()).getTableName(), 50);
    m_tableText.setEditable(!((DatabaseSaver)m_dsSaver.getSaverTemplate()).getRelationForTableName());
    Messages.getInstance();JLabel tableLab = new JLabel(Messages.getString("SaverCustomizer_SetUpDatabase_TableLab_JLabel_Text"), 2);
    tableLab.setFont(new Font("Monospaced", 0, 12));
    
    m_tabBox = new JComboBox();
    m_tabBox.addItem(new Boolean(true));
    m_tabBox.addItem(new Boolean(false));
    if (!((DatabaseSaver)m_dsSaver.getSaverTemplate()).getRelationForTableName()) {
      m_tabBox.setSelectedIndex(1);
    } else
      m_tabBox.setSelectedIndex(0);
    m_tabBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        m_tableText.setEditable(!((Boolean)m_tabBox.getSelectedItem()).booleanValue());
      }
      
    });
    Messages.getInstance();JLabel tabLab = new JLabel(Messages.getString("SaverCustomizer_SetUpDatabase_TabLab_JLabel_Text"), 2);
    tabLab.setFont(new Font("Monospaced", 0, 12));
    
    m_idBox = new JComboBox();
    m_idBox.addItem(new Boolean(true));
    m_idBox.addItem(new Boolean(false));
    if (!((DatabaseSaver)m_dsSaver.getSaverTemplate()).getAutoKeyGeneration()) {
      m_idBox.setSelectedIndex(1);
    } else
      m_idBox.setSelectedIndex(0);
    Messages.getInstance();JLabel idLab = new JLabel(Messages.getString("SaverCustomizer_SetUpDatabase_IdLab_JLabel_Text"), 2);
    idLab.setFont(new Font("Monospaced", 0, 12));
    
    JPanel urlP = new JPanel();
    
    urlP.setLayout(new FlowLayout(0));
    urlP.add(dbaseURLLab);
    urlP.add(m_dbaseURLText);
    db.add(urlP);
    
    JPanel usernameP = new JPanel();
    usernameP.setLayout(new FlowLayout(0));
    usernameP.add(userNameLab);
    usernameP.add(m_userNameText);
    db.add(usernameP);
    
    JPanel passwordP = new JPanel();
    passwordP.setLayout(new FlowLayout(0));
    passwordP.add(passwordLab);
    passwordP.add(m_passwordText);
    db.add(passwordP);
    
    JPanel tabP = new JPanel();
    
    tabP.setLayout(new FlowLayout(0));
    tabP.add(tabLab);
    tabP.add(m_tabBox);
    db.add(tabP);
    
    JPanel tableP = new JPanel();
    
    tableP.setLayout(new FlowLayout(0));
    tableP.add(tableLab);
    tableP.add(m_tableText);
    db.add(tableP);
    
    JPanel keyP = new JPanel();
    
    keyP.setLayout(new FlowLayout(0));
    keyP.add(idLab);
    keyP.add(m_idBox);
    db.add(keyP);
    
    JPanel buttonsP = new JPanel();
    buttonsP.setLayout(new FlowLayout());
    
    Messages.getInstance(); JButton ok; buttonsP.add(ok = new JButton(Messages.getString("SaverCustomizer_SetUpDatabase_ButtonsP_Ok_JButton_Text")));
    Messages.getInstance(); JButton cancel; buttonsP.add(cancel = new JButton(Messages.getString("SaverCustomizer_SetUpDatabase_ButtonsP_Cancel_JButton_Text")));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ((DatabaseSaver)m_dsSaver.getSaverTemplate()).resetStructure();
        ((DatabaseConverter)m_dsSaver.getSaverTemplate()).setUrl(m_dbaseURLText.getText());
        ((DatabaseConverter)m_dsSaver.getSaverTemplate()).setUser(m_userNameText.getText());
        ((DatabaseConverter)m_dsSaver.getSaverTemplate()).setPassword(new String(m_passwordText.getPassword()));
        if (!((Boolean)m_tabBox.getSelectedItem()).booleanValue())
          ((DatabaseSaver)m_dsSaver.getSaverTemplate()).setTableName(m_tableText.getText());
        ((DatabaseSaver)m_dsSaver.getSaverTemplate()).setAutoKeyGeneration(((Boolean)m_idBox.getSelectedItem()).booleanValue());
        ((DatabaseSaver)m_dsSaver.getSaverTemplate()).setRelationForTableName(((Boolean)m_tabBox.getSelectedItem()).booleanValue());
        if (m_parentFrame != null) {
          m_parentFrame.dispose();
        }
      }
    });
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (m_parentFrame != null) {
          m_parentFrame.dispose();
        }
        
      }
    });
    db.add(buttonsP);
    JPanel about = m_SaverEditor.getAboutPanel();
    if (about != null) {
      add(about, "North");
    }
    add(db, "South");
  }
  
  public void setUpFile()
  {
    removeAll();
    m_fileChooser.setFileFilter(new FileFilter()
    {
      public boolean accept(File f) { return f.isDirectory(); }
      
      public String getDescription() { Messages.getInstance();return Messages.getString("SaverCustomizer_SetUpFile_FileChooser_SetFileFilter_GetDescription_Text");
      } });
    m_fileChooser.setAcceptAllFileFilterUsed(false);
    try {
      if (!m_dsSaver.getSaverTemplate().retrieveDir().equals("")) {
        File tmp = new File(m_dsSaver.getSaverTemplate().retrieveDir());
        tmp = new File(tmp.getAbsolutePath());
        m_fileChooser.setCurrentDirectory(tmp);
      }
    } catch (Exception ex) {
      System.out.println(ex);
    }
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    try {
      m_prefixText = new JTextField(m_dsSaver.getSaverTemplate().filePrefix(), 25);
      Messages.getInstance();m_prefixText.setToolTipText(Messages.getString("SaverCustomizer_SetUpFile_PrefixText_SetToolTipText_Text"));
      Messages.getInstance();final JLabel prefixLab = new JLabel(Messages.getString("SaverCustomizer_SetUpFile_PrefixLab_JLabel_Text"), 2);
      

      Messages.getInstance();m_relationNameForFilename = new JCheckBox(Messages.getString("SaverCustomizer_SetUpFile_RelationNameForFilename_JCheckBox_Text"));
      m_relationNameForFilename.setSelected(m_dsSaver.getRelationNameForFilename());
      m_relationNameForFilename.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (m_relationNameForFilename.isSelected()) {
            Messages.getInstance();prefixLab.setText(Messages.getString("SaverCustomizer_SetUpFile_PrefixLab_SetText_Text_First"));
            Messages.getInstance();m_fileChooser.setApproveButtonText(Messages.getString("SaverCustomizer_SetUpFile_FileChooser_SetApproveButtonText_Text_First"));
          } else {
            Messages.getInstance();prefixLab.setText(Messages.getString("SaverCustomizer_SetUpFile_PrefixLab_SetText_Text_Second"));
            Messages.getInstance();m_fileChooser.setApproveButtonText(Messages.getString("SaverCustomizer_SetUpFile_FileChooser_SetApproveButtonText_Text_Second"));
          }
          
        }
      });
      JPanel prefixP = new JPanel();
      prefixP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      
      prefixP.setLayout(new BorderLayout());
      prefixP.add(prefixLab, "West");
      prefixP.add(m_prefixText, "Center");
      prefixP.add(m_relationNameForFilename, "South");
      innerPanel.add(prefixP, "South");
    }
    catch (Exception ex) {}
    
    JPanel about = m_SaverEditor.getAboutPanel();
    if (about != null) {
      innerPanel.add(about, "North");
    }
    add(innerPanel, "North");
    add(m_fileChooser, "Center");
    
    Messages.getInstance();m_relativeFilePath = new JCheckBox(Messages.getString("SaverCustomizer_SetUpFile_RelativeFilePath_JCheckBox_Text"));
    m_relativeFilePath.setSelected(((FileSourcedConverter)m_dsSaver.getSaverTemplate()).getUseRelativePath());
    

    m_relativeFilePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ((FileSourcedConverter)m_dsSaver.getSaverTemplate()).setUseRelativePath(m_relativeFilePath.isSelected());
      }
      
    });
    JPanel holderPanel = new JPanel();
    holderPanel.setLayout(new FlowLayout());
    holderPanel.add(m_relativeFilePath);
    add(holderPanel, "South");
  }
  




  public void setObject(Object object)
  {
    m_dsSaver = ((Saver)object);
    m_SaverEditor.setTarget(m_dsSaver.getSaverTemplate());
    if ((m_dsSaver.getSaverTemplate() instanceof DatabaseConverter)) {
      setUpDatabase();

    }
    else if ((m_dsSaver.getSaverTemplate() instanceof FileSourcedConverter)) {
      setUpFile();
    } else {
      setUpOther();
    }
  }
  





  public void addPropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.addPropertyChangeListener(pcl);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.removePropertyChangeListener(pcl);
  }
  
  static {}
}

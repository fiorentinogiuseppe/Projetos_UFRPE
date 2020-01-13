package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import weka.core.Tag;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertySheetPanel;










































public class SerializedModelSaverCustomizer
  extends JPanel
  implements Customizer, CustomizerCloseRequester
{
  private static final long serialVersionUID = -4874208115942078471L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private SerializedModelSaver m_smSaver;
  
  private PropertySheetPanel m_SaverEditor = new PropertySheetPanel();
  

  private JFileChooser m_fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  

  private JFrame m_parentFrame;
  

  private JTextField m_prefixText;
  
  private JComboBox m_fileFormatBox;
  
  private JCheckBox m_relativeFilePath;
  

  public SerializedModelSaverCustomizer()
  {
    try
    {
      m_SaverEditor.addPropertyChangeListener(new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e) {
          repaint();
          if (m_smSaver != null) {
            Messages.getInstance();System.err.println(Messages.getString("SerializedModelSaverCustomizer_Error_Text"));
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
    Messages.getInstance();m_fileChooser.setApproveButtonText(Messages.getString("SerializedModelSaverCustomizer_FileChooser_SetApproveButtonText_Text"));
    
    m_fileChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ApproveSelection")) {
          try {
            m_smSaver.setPrefix(m_prefixText.getText());
            m_smSaver.setDirectory(m_fileChooser.getSelectedFile());
          } catch (Exception ex) {
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
  
  private void setUpOther() {
    removeAll();
    add(m_SaverEditor, "Center");
    validate();
    repaint();
  }
  
  public void setUpFile()
  {
    removeAll();
    m_fileChooser.setFileFilter(new FileFilter()
    {
      public boolean accept(File f) { return f.isDirectory(); }
      
      public String getDescription() { Messages.getInstance();return Messages.getString("SerializedModelSaverCustomizer_SetUpFile_GetDescription_Text");
      }
    });
    m_fileChooser.setAcceptAllFileFilterUsed(false);
    try
    {
      if (!m_smSaver.getDirectory().getPath().equals("")) {
        File tmp = m_smSaver.getDirectory();
        tmp = new File(tmp.getAbsolutePath());
        m_fileChooser.setCurrentDirectory(tmp);
      }
    } catch (Exception ex) {
      System.out.println(ex);
    }
    
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    try {
      m_prefixText = new JTextField(m_smSaver.getPrefix(), 25);
      Messages.getInstance();JLabel prefixLab = new JLabel(Messages.getString("SerializedModelSaverCustomizer_SetUpFile_PrefixLab_JLabel_Text"), 2);
      JPanel prefixP = new JPanel();
      prefixP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      
      prefixP.setLayout(new BorderLayout());
      prefixP.add(prefixLab, "West");
      prefixP.add(m_prefixText, "Center");
      innerPanel.add(prefixP, "South");
      
      JPanel ffP = new JPanel();
      ffP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      ffP.setLayout(new BorderLayout());
      Messages.getInstance();ffP.add(new JLabel(Messages.getString("SerializedModelSaverCustomizer_SetUpFile_FfP_JLabel_Text")), "West");
      setUpFileFormatComboBox();
      ffP.add(m_fileFormatBox, "Center");
      innerPanel.add(ffP, "Center");
    }
    catch (Exception ex) {}
    
    JPanel about = m_SaverEditor.getAboutPanel();
    if (about != null) {
      innerPanel.add(about, "North");
    }
    add(innerPanel, "North");
    add(m_fileChooser, "Center");
    
    Messages.getInstance();m_relativeFilePath = new JCheckBox(Messages.getString("SerializedModelSaverCustomizer_SetUpFile_RelativeFilePath_JCheckBox_Text"));
    m_relativeFilePath.setSelected(m_smSaver.getUseRelativePath());
    

    m_relativeFilePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_smSaver.setUseRelativePath(m_relativeFilePath.isSelected());
      }
      
    });
    JPanel holderPanel = new JPanel();
    holderPanel.setLayout(new FlowLayout());
    holderPanel.add(m_relativeFilePath);
    add(holderPanel, "South");
  }
  




  public void setObject(Object object)
  {
    m_smSaver = ((SerializedModelSaver)object);
    m_SaverEditor.setTarget(m_smSaver);
    
    setUpFile();
  }
  
  private void setUpFileFormatComboBox() {
    m_fileFormatBox = new JComboBox();
    for (int i = 0; i < SerializedModelSaver.s_fileFormatsAvailable.size(); i++) {
      Tag temp = (Tag)SerializedModelSaver.s_fileFormatsAvailable.get(i);
      m_fileFormatBox.addItem(temp);
    }
    
    Tag result = m_smSaver.validateFileFormat(m_smSaver.getFileFormat());
    if (result == null) {
      m_fileFormatBox.setSelectedIndex(0);
    } else {
      m_fileFormatBox.setSelectedItem(result);
    }
    
    m_fileFormatBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Tag selected = (Tag)m_fileFormatBox.getSelectedItem();
        if (selected != null) {
          m_smSaver.setFileFormat(selected);
        }
      }
    });
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

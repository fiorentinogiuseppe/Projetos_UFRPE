package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import weka.core.Attribute;
import weka.core.Instances;
































public class ClassValuePickerCustomizer
  extends JPanel
  implements Customizer, CustomizerClosingListener, CustomizerCloseRequester
{
  private static final long serialVersionUID = 8213423053861600469L;
  private boolean m_displayValNames = false;
  
  private ClassValuePicker m_classValuePicker;
  
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private JComboBox m_ClassValueCombo = new JComboBox();
  private JPanel m_holderP = new JPanel();
  
  public ClassValuePickerCustomizer() { Messages.getInstance();m_messageLabel = new JLabel(Messages.getString("ClassValuePickerCustomizer_MessageLabel_JLabel_Text"));
    



    m_textBoxEntryMode = false;
    



    setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    
    setLayout(new BorderLayout());
    Messages.getInstance();add(new JLabel(Messages.getString("ClassValuePickerCustomizer_JLabel_Text")), "North");
    
    m_holderP.setLayout(new BorderLayout());
    Messages.getInstance();m_holderP.setBorder(BorderFactory.createTitledBorder(Messages.getString("ClassValuePickerCustomizer_HolderP_SetBorder_BorderFactory_CreateTitledBorder_Text")));
    m_holderP.add(m_ClassValueCombo, "Center");
    m_ClassValueCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_classValuePicker != null) {
          m_classValuePicker.setClassValue(m_ClassValueCombo.getSelectedItem().toString());
        }
        
      }
      
    });
    add(m_messageLabel, "Center");
    addButtons();
  }
  
  private void addButtons() {
    JButton okBut = new JButton("OK");
    JButton cancelBut = new JButton("Cancel");
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 2));
    butHolder.add(okBut);butHolder.add(cancelBut);
    add(butHolder, "South");
    
    okBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_textBoxEntryMode) {
          m_classValuePicker.setClassValue(m_valueTextBox.getText().trim());
        }
        
        if (m_parent != null) {
          m_parent.dispose();
        }
        
      }
    });
    cancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_classValuePicker.setClassValue(m_backup);
        
        customizerClosing();
        if (m_parent != null) {
          m_parent.dispose();
        }
      }
    });
  }
  
  private void setUpNoCustPossible() {
    if (m_displayValNames == true) {
      remove(m_holderP);
      add(m_messageLabel, "Center");
      m_displayValNames = false;
    }
    validate();repaint();
  }
  
  private void setupTextBoxSelection() {
    m_textBoxEntryMode = true;
    
    JPanel holderPanel = new JPanel();
    holderPanel.setLayout(new BorderLayout());
    holderPanel.setBorder(BorderFactory.createTitledBorder("Specify class label"));
    JLabel label = new JLabel("Class label ", 4);
    holderPanel.add(label, "West");
    m_valueTextBox = new JTextField(15);
    m_valueTextBox.setToolTipText("Class label. /first, /last and /<num> can be used to specify the first, last or specific index of the label to use respectively.");
    


    holderPanel.add(m_valueTextBox, "Center");
    JPanel holder2 = new JPanel();
    holder2.setLayout(new BorderLayout());
    holder2.add(holderPanel, "North");
    add(holder2, "Center");
  }
  
  private void setUpValueSelection(Instances format) {
    if ((format.classIndex() < 0) || (format.classAttribute().isNumeric()))
    {
      m_messageLabel.setText(format.classIndex() < 0 ? "EROR: no class attribute set" : "ERROR: class is numeric");
      
      return;
    }
    
    if (!m_displayValNames) {
      remove(m_messageLabel);
    }
    
    m_textBoxEntryMode = false;
    
    if (format.classAttribute().numValues() == 0)
    {




      setupTextBoxSelection();
      validate();repaint();
      return;
    }
    
    String existingClassVal = m_classValuePicker.getClassValue();
    if (existingClassVal == null) {
      existingClassVal = "";
    }
    int classValIndex = format.classAttribute().indexOfValue(existingClassVal);
    

    if (existingClassVal.startsWith("/")) {
      existingClassVal = existingClassVal.substring(1);
      if (existingClassVal.equalsIgnoreCase("first")) {
        classValIndex = 0;
      } else if (existingClassVal.equalsIgnoreCase("last")) {
        classValIndex = format.classAttribute().numValues() - 1;
      }
      else {
        classValIndex = Integer.parseInt(existingClassVal);
        classValIndex--;
      }
    }
    
    if (classValIndex < 0) {
      classValIndex = 0;
    }
    String[] attribValNames = new String[format.classAttribute().numValues()];
    for (int i = 0; i < attribValNames.length; i++) {
      attribValNames[i] = format.classAttribute().value(i);
    }
    m_ClassValueCombo.setModel(new DefaultComboBoxModel(attribValNames));
    if (attribValNames.length > 0)
    {
      m_ClassValueCombo.setSelectedIndex(classValIndex);
    }
    
    if (!m_displayValNames) {
      add(m_holderP, "Center");
      m_displayValNames = true;
    }
    validate();repaint();
  }
  


  private JLabel m_messageLabel;
  
  public void setObject(Object object)
  {
    if (m_classValuePicker != (ClassValuePicker)object)
    {



      m_classValuePicker = ((ClassValuePicker)object);
      

      if (m_classValuePicker.getConnectedFormat() != null) {
        setUpValueSelection(m_classValuePicker.getConnectedFormat());
      }
      m_backup = m_classValuePicker.getClassValue();
    }
  }
  




  public void customizerClosing()
  {
    m_classValuePicker.setClassValue(m_backup);
  }
  


  private JFrame m_parent;
  

  private String m_backup;
  

  private boolean m_textBoxEntryMode;
  
  private JTextField m_valueTextBox;
  
  public void addPropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.addPropertyChangeListener(pcl);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.removePropertyChangeListener(pcl);
  }
  
  public void setParentFrame(JFrame parent) {
    m_parent = parent;
  }
}

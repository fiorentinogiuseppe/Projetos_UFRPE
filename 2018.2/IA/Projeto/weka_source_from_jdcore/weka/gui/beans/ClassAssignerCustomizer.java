package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weka.core.Attribute;
import weka.core.Instances;
import weka.gui.PropertySheetPanel;

































public class ClassAssignerCustomizer
  extends JPanel
  implements Customizer, CustomizerClosingListener, CustomizerCloseRequester, DataFormatListener
{
  private static final long serialVersionUID = 476539385765301907L;
  private boolean m_displayColNames = false;
  
  private ClassAssigner m_classAssigner;
  
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private PropertySheetPanel m_caEditor = new PropertySheetPanel();
  

  private JComboBox m_ClassCombo = new JComboBox();
  private JPanel m_holderP = new JPanel();
  
  private transient JFrame m_parent;
  private transient String m_backup;
  
  public ClassAssignerCustomizer()
  {
    setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    
    setLayout(new BorderLayout());
    Messages.getInstance();add(new JLabel(Messages.getString("ClassAssignerCustomizer_JLabel_Text")), "North");
    
    m_holderP.setLayout(new BorderLayout());
    Messages.getInstance();m_holderP.setBorder(BorderFactory.createTitledBorder(Messages.getString("ClassAssignerCustomizer_HolderP_SetBorder_BorderFactoryCreateTitledBorder_Text")));
    m_holderP.add(m_ClassCombo, "Center");
    m_ClassCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ((m_classAssigner != null) && (m_displayColNames == true)) {
          m_classAssigner.setClassColumn("" + m_ClassCombo.getSelectedIndex());
        }
      }
    });
    add(m_caEditor, "Center");
    addButtons();
  }
  
  private void addButtons() {
    JButton okBut = new JButton("OK");
    JButton cancelBut = new JButton("Cancel");
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 2));
    butHolder.add(okBut);butHolder.add(cancelBut);
    add(butHolder, "South");
    
    okBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_parent != null) {
          m_parent.dispose();
        }
        
      }
    });
    cancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        customizerClosing();
        if (m_parent != null) {
          m_parent.dispose();
        }
      }
    });
  }
  
  private void setUpStandardSelection() {
    if (m_displayColNames == true) {
      remove(m_holderP);
      m_caEditor.setTarget(m_classAssigner);
      add(m_caEditor, "Center");
      m_displayColNames = false;
    }
    validate();repaint();
  }
  
  private void setUpColumnSelection(Instances format) {
    if (!m_displayColNames) {
      remove(m_caEditor);
    }
    
    int existingClassCol = 0;
    
    String classColString = m_classAssigner.getClassColumn();
    if ((classColString.trim().toLowerCase().compareTo("last") == 0) || (classColString.equalsIgnoreCase("/last")))
    {
      existingClassCol = format.numAttributes() - 1;
    } else if ((classColString.trim().toLowerCase().compareTo("first") != 0) && (!classColString.equalsIgnoreCase("/first")))
    {



      Attribute classAtt = format.attribute(classColString);
      if (classAtt != null) {
        existingClassCol = classAtt.index();
      }
      else {
        existingClassCol = Integer.parseInt(classColString);
        if (existingClassCol < 0) {
          existingClassCol = -1;
        } else if (existingClassCol > format.numAttributes() - 1) {
          existingClassCol = format.numAttributes() - 1;
        } else {
          existingClassCol--;
        }
      }
    }
    




    String[] attribNames = new String[format.numAttributes() + 1];
    Messages.getInstance();attribNames[0] = Messages.getString("ClassAssignerCustomizer_SetUpColumnSelection_AttribNames0_Text");
    for (int i = 1; i < attribNames.length; i++) {
      String type = "";
      switch (format.attribute(i - 1).type()) {
      case 1: 
        Messages.getInstance();type = Messages.getString("ClassAssignerCustomizer_SetUpColumnSelection_AttributeNOMINAL_Text");
        break;
      case 0: 
        Messages.getInstance();type = Messages.getString("ClassAssignerCustomizer_SetUpColumnSelection_AttributeNUMERIC_Text");
        break;
      case 2: 
        Messages.getInstance();type = Messages.getString("ClassAssignerCustomizer_SetUpColumnSelection_AttributeSTRING_Text");
        break;
      case 3: 
        Messages.getInstance();type = Messages.getString("ClassAssignerCustomizer_SetUpColumnSelection_AttributeDATE_Text");
        break;
      case 4: 
        Messages.getInstance();type = Messages.getString("ClassAssignerCustomizer_SetUpColumnSelection_AttributeRELATIONAL_Text");
        break;
      default: 
        Messages.getInstance();type = Messages.getString("ClassAssignerCustomizer_SetUpColumnSelection_AttributeDEFAULT_Text");
      }
      attribNames[i] = (type + format.attribute(i - 1).name());
    }
    m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
    if (attribNames.length > 0) {
      m_ClassCombo.setSelectedIndex(existingClassCol + 1);
    }
    if (!m_displayColNames) {
      add(m_holderP, "Center");
      m_displayColNames = true;
    }
    validate();repaint();
  }
  




  public void setObject(Object object)
  {
    if (m_classAssigner != (ClassAssigner)object)
    {



      m_classAssigner = ((ClassAssigner)object);
      

      m_caEditor.setTarget(m_classAssigner);
      if (m_classAssigner.getConnectedFormat() != null) {
        setUpColumnSelection(m_classAssigner.getConnectedFormat());
      }
      m_backup = m_classAssigner.getClassColumn();
    }
  }
  
  public void customizerClosing()
  {
    if (m_classAssigner != null) {
      Messages.getInstance();System.err.println(Messages.getString("ClassAssignerCustomizer_CustomizerClosing_Error_Text"));
      m_classAssigner.removeDataFormatListener(this);
    }
    
    if (m_backup != null) {
      m_classAssigner.setClassColumn(m_backup);
    }
  }
  
  public void newDataFormat(DataSetEvent dse) {
    if (dse.getDataSet() != null)
    {
      setUpColumnSelection(m_classAssigner.getConnectedFormat());
    } else {
      setUpStandardSelection();
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
  
  public void setParentFrame(JFrame parent) {
    m_parent = parent;
  }
}

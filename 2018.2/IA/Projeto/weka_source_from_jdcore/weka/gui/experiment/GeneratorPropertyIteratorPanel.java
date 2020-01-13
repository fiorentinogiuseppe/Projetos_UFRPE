package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.io.PrintStream;
import java.lang.reflect.Array;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import weka.core.FastVector;
import weka.experiment.Experiment;
import weka.experiment.PropertyNode;
import weka.gui.GenericArrayEditor;
import weka.gui.PropertySelectorDialog;

























public class GeneratorPropertyIteratorPanel
  extends JPanel
  implements ActionListener
{
  private static final long serialVersionUID = -6026938995241632139L;
  protected JButton m_ConfigureBut;
  protected JComboBox m_StatusBox;
  protected GenericArrayEditor m_ArrayEditor;
  protected Experiment m_Exp;
  protected FastVector m_Listeners;
  
  public GeneratorPropertyIteratorPanel()
  {
    Messages.getInstance();m_ConfigureBut = new JButton(Messages.getString("GeneratorPropertyIteratorPanel_ConfigureBut_JButton_Text"));
    

    m_StatusBox = new JComboBox();
    

    m_ArrayEditor = new GenericArrayEditor();
    





    m_Listeners = new FastVector(); String[] 
    




      tmp61_58 = new String[2];Messages.getInstance();tmp61_58[0] = Messages.getString("GeneratorPropertyIteratorPanel_Options_Disabled_Text"); String[] tmp73_61 = tmp61_58;Messages.getInstance();tmp73_61[1] = Messages.getString("GeneratorPropertyIteratorPanel_Options_Enabled_Text");String[] options = tmp73_61;
    ComboBoxModel cbm = new DefaultComboBoxModel(options);
    m_StatusBox.setModel(cbm);
    m_StatusBox.setSelectedIndex(0);
    m_StatusBox.addActionListener(this);
    m_StatusBox.setEnabled(false);
    m_ConfigureBut.setEnabled(false);
    m_ConfigureBut.addActionListener(this);
    JPanel buttons = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    buttons.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    
    buttons.setLayout(gb);
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    buttons.add(m_StatusBox, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    buttons.add(m_ConfigureBut, constraints);
    buttons.setMaximumSize(new Dimension(getMaximumSizewidth, getMinimumSizeheight));
    
    setBorder(BorderFactory.createTitledBorder("Generator properties"));
    setLayout(new BorderLayout());
    add(buttons, "North");
    
    m_ArrayEditor.setBorder(BorderFactory.createEtchedBorder());
    m_ArrayEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        Messages.getInstance();System.err.println(Messages.getString("GeneratorPropertyIteratorPanel_PropertyChange_Error_Text"));
        m_Exp.setPropertyArray(m_ArrayEditor.getValue());
      }
    });
    add(m_ArrayEditor, "Center");
  }
  





  public GeneratorPropertyIteratorPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  




  public boolean getEditorActive()
  {
    if (m_StatusBox.getSelectedIndex() == 0) {
      return false;
    }
    
    return true;
  }
  





  public void setExperiment(Experiment exp)
  {
    m_Exp = exp;
    m_StatusBox.setEnabled(true);
    m_ArrayEditor.setValue(m_Exp.getPropertyArray());
    if (m_Exp.getPropertyArray() == null) {
      m_StatusBox.setSelectedIndex(0);
      m_ConfigureBut.setEnabled(false);
    } else {
      m_StatusBox.setSelectedIndex(m_Exp.getUsePropertyIterator() ? 1 : 0);
      m_ConfigureBut.setEnabled(m_Exp.getUsePropertyIterator());
    }
    validate();
  }
  






  protected int selectProperty()
  {
    PropertySelectorDialog jd = new PropertySelectorDialog(null, m_Exp.getResultProducer());
    
    jd.setLocationRelativeTo(this);
    int result = jd.showDialog();
    if (result == 0) {
      Messages.getInstance();System.err.println(Messages.getString("GeneratorPropertyIteratorPanel_SelectProperty_Error_Text_First"));
      PropertyNode[] path = jd.getPath();
      Object value = length1value;
      PropertyDescriptor property = length1property;
      
      Class propertyClass = property.getPropertyType();
      m_Exp.setPropertyPath(path);
      m_Exp.setPropertyArray(Array.newInstance(propertyClass, 1));
      Array.set(m_Exp.getPropertyArray(), 0, value);
      
      m_ArrayEditor.setValue(m_Exp.getPropertyArray());
      m_ArrayEditor.repaint();
      Messages.getInstance();System.err.println(Messages.getString("GeneratorPropertyIteratorPanel_SelectProperty_Error_Text_Second"));
    } else {
      Messages.getInstance();System.err.println(Messages.getString("GeneratorPropertyIteratorPanel_SelectProperty_Error_Text_Third"));
    }
    return result;
  }
  





  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == m_ConfigureBut) {
      selectProperty();
    } else if (e.getSource() == m_StatusBox)
    {
      for (int i = 0; i < m_Listeners.size(); i++) {
        ActionListener temp = (ActionListener)m_Listeners.elementAt(i);
        Messages.getInstance();temp.actionPerformed(new ActionEvent(this, 1001, Messages.getString("GeneratorPropertyIteratorPanel_ActionPerformed_Text")));
      }
      



      if (m_StatusBox.getSelectedIndex() == 0) {
        m_Exp.setUsePropertyIterator(false);
        m_ConfigureBut.setEnabled(false);
        m_ArrayEditor.setEnabled(false);
        m_ArrayEditor.setValue(null);
        validate();
      } else {
        if (m_Exp.getPropertyArray() == null) {
          selectProperty();
        }
        if (m_Exp.getPropertyArray() == null) {
          m_StatusBox.setSelectedIndex(0);
        } else {
          m_Exp.setUsePropertyIterator(true);
          m_ConfigureBut.setEnabled(true);
          m_ArrayEditor.setEnabled(true);
        }
        validate();
      }
    }
  }
  



  public void addActionListener(ActionListener newA)
  {
    m_Listeners.addElement(newA);
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("GeneratorPropertyIteratorPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      GeneratorPropertyIteratorPanel gp = new GeneratorPropertyIteratorPanel();
      jf.getContentPane().add(gp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      Messages.getInstance();System.err.println(Messages.getString("GeneratorPropertyIteratorPanel_Main_Error_Text_First"));
      Thread.currentThread();Thread.sleep(3000L);
      Messages.getInstance();System.err.println(Messages.getString("GeneratorPropertyIteratorPanel_Main_Error_Text_Second"));
      gp.setExperiment(new Experiment());
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

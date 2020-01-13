package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertySheetPanel;





































public class AssociatorCustomizer
  extends JPanel
  implements Customizer, CustomizerCloseRequester
{
  private static final long serialVersionUID = 5767664969353495974L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private Associator m_dsAssociator;
  

  private PropertySheetPanel m_AssociatorEditor = new PropertySheetPanel();
  
  protected JFrame m_parentFrame;
  
  private weka.associations.Associator m_backup;
  

  public AssociatorCustomizer()
  {
    setLayout(new BorderLayout());
    add(m_AssociatorEditor, "Center");
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 2));
    Messages.getInstance();JButton OKBut = new JButton(Messages.getString("AssociatorCustomizer_OKBut_JButton_Text"));
    OKBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_parentFrame.dispose();
      }
      
    });
    Messages.getInstance();JButton CancelBut = new JButton(Messages.getString("AssociatorCustomizer_CancelBut_JButton_Text"));
    CancelBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (m_backup != null) {
          m_dsAssociator.setAssociator(m_backup);
        }
        m_parentFrame.dispose();
      }
      
    });
    butHolder.add(OKBut);
    butHolder.add(CancelBut);
    add(butHolder, "South");
  }
  




  public void setObject(Object object)
  {
    m_dsAssociator = ((Associator)object);
    try
    {
      m_backup = ((weka.associations.Associator)GenericObjectEditor.makeCopy(m_dsAssociator.getAssociator()));
    }
    catch (Exception ex) {}
    


    m_AssociatorEditor.setTarget(m_dsAssociator.getAssociator());
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
    m_parentFrame = parent;
  }
  
  static {}
}

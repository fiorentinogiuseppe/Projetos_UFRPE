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





































public class ClustererCustomizer
  extends JPanel
  implements Customizer, CustomizerCloseRequester
{
  private static final long serialVersionUID = -2035688458149534161L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private Clusterer m_dsClusterer;
  
  private PropertySheetPanel m_ClustererEditor = new PropertySheetPanel();
  

  private JFrame m_parentFrame;
  

  private weka.clusterers.Clusterer m_backup;
  

  public ClustererCustomizer()
  {
    setLayout(new BorderLayout());
    add(m_ClustererEditor, "Center");
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 2));
    Messages.getInstance();JButton OKBut = new JButton(Messages.getString("ClustererCustomizer_OKBut_JButton_Text"));
    OKBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_parentFrame.dispose();
      }
      
    });
    Messages.getInstance();JButton CancelBut = new JButton(Messages.getString("ClustererCustomizer_CancelBut_JButton_Text"));
    CancelBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (m_backup != null) {
          m_dsClusterer.setClusterer(m_backup);
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
    m_dsClusterer = ((Clusterer)object);
    try {
      m_backup = ((weka.clusterers.Clusterer)GenericObjectEditor.makeCopy(m_dsClusterer.getClusterer()));
    }
    catch (Exception ex) {}
    


    m_ClustererEditor.setTarget(m_dsClusterer.getClusterer());
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

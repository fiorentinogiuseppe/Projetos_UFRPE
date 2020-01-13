package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weka.gui.PropertySheetPanel;































public class TrainTestSplitMakerCustomizer
  extends JPanel
  implements Customizer
{
  private static final long serialVersionUID = -1684662340241807260L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private PropertySheetPanel m_splitEditor = new PropertySheetPanel();
  
  public TrainTestSplitMakerCustomizer()
  {
    setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    
    setLayout(new BorderLayout());
    add(m_splitEditor, "Center");
    Messages.getInstance();add(new JLabel(Messages.getString("TrainTestSplitMakerCustomizer_JLabel_Text")), "North");
  }
  





  public void setObject(Object object)
  {
    m_splitEditor.setTarget((TrainTestSplitMaker)object);
  }
  




  public void addPropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.addPropertyChangeListener(pcl);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.removePropertyChangeListener(pcl);
  }
}

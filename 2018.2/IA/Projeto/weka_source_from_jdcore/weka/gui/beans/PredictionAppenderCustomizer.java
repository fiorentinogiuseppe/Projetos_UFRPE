package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weka.gui.PropertySheetPanel;
































public class PredictionAppenderCustomizer
  extends JPanel
  implements Customizer
{
  private static final long serialVersionUID = 6884933202506331888L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private PropertySheetPanel m_paEditor = new PropertySheetPanel();
  
  public PredictionAppenderCustomizer()
  {
    setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    
    setLayout(new BorderLayout());
    add(m_paEditor, "Center");
    Messages.getInstance();add(new JLabel(Messages.getString("PredictionAppenderCustomizer_JLabel_Text")), "North");
  }
  





  public void setObject(Object object)
  {
    m_paEditor.setTarget((PredictionAppender)object);
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

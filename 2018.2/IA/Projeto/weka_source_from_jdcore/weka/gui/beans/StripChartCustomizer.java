package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weka.gui.PropertySheetPanel;































public class StripChartCustomizer
  extends JPanel
  implements Customizer
{
  private static final long serialVersionUID = 7441741530975984608L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private PropertySheetPanel m_cvEditor = new PropertySheetPanel();
  
  public StripChartCustomizer()
  {
    setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    
    setLayout(new BorderLayout());
    add(m_cvEditor, "Center");
    Messages.getInstance();add(new JLabel(Messages.getString("StripChartCustomizer_JLabel_Text")), "North");
  }
  





  public void setObject(Object object)
  {
    m_cvEditor.setTarget((StripChart)object);
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

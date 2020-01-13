package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertySheetPanel;







































public class FilterCustomizer
  extends JPanel
  implements Customizer, CustomizerCloseRequester
{
  private static final long serialVersionUID = 2049895469240109738L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  


  private Filter m_filter;
  

  private weka.filters.Filter m_backup;
  

  private PropertySheetPanel m_filterEditor = new PropertySheetPanel();
  
  private JFrame m_parentFrame;
  
  public FilterCustomizer()
  {
    Messages.getInstance();m_filterEditor.setBorder(BorderFactory.createTitledBorder(Messages.getString("FilterCustomizer_FilterEditor_SetBorder_BorderFactory_CreateTitledBorder_Text")));
    



    setLayout(new BorderLayout());
    add(m_filterEditor, "Center");
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 2));
    Messages.getInstance();JButton OKBut = new JButton(Messages.getString("FilterCustomizer_OKBut_JButton_Text"));
    OKBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_parentFrame.dispose();
      }
      
    });
    Messages.getInstance();JButton CancelBut = new JButton(Messages.getString("FilterCustomizer_CancelBut_JButton_Text"));
    CancelBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (m_backup != null) {
          m_filter.setFilter(m_backup);
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
    m_filter = ((Filter)object);
    try {
      m_backup = ((weka.filters.Filter)GenericObjectEditor.makeCopy(m_filter.getFilter()));
    }
    catch (Exception ex) {}
    

    m_filterEditor.setTarget(m_filter.getFilter());
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

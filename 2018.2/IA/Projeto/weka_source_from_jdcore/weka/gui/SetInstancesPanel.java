package weka.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.FileSourcedConverter;
import weka.core.converters.IncrementalConverter;
import weka.core.converters.Loader;
import weka.core.converters.URLSourcedLoader;












































































public class SetInstancesPanel
  extends JPanel
{
  private static final long serialVersionUID = -384804041420453735L;
  protected JButton m_OpenFileBut;
  protected JButton m_OpenURLBut;
  protected JButton m_CloseBut;
  protected InstancesSummaryPanel m_Summary;
  protected ConverterFileChooser m_FileChooser;
  protected String m_LastURL;
  protected Thread m_IOThread;
  protected PropertyChangeSupport m_Support;
  protected Instances m_Instances;
  protected Loader m_Loader;
  protected JFrame m_ParentFrame;
  protected JPanel m_CloseButPanel;
  protected boolean m_readIncrementally;
  protected boolean m_showZeroInstancesAsUnknown;
  
  public SetInstancesPanel()
  {
    this(false, null);
  }
  
  public SetInstancesPanel(boolean showZeroInstancesAsUnknown, ConverterFileChooser chooser)
  {
    Messages.getInstance();m_OpenFileBut = new JButton(Messages.getString("SetInstancesPanel_OpenFileBut_JButton_Text"));
    


    Messages.getInstance();m_OpenURLBut = new JButton(Messages.getString("SetInstancesPanel_OpenURLBut_JButton_Text"));
    


    Messages.getInstance();m_CloseBut = new JButton(Messages.getString("SetInstancesPanel_CloseBut_JButton_Text"));
    


    m_Summary = new InstancesSummaryPanel();
    

    m_FileChooser = new ConverterFileChooser(new File(System.getProperty("user.dir")));
    


    m_LastURL = "http://";
    







    m_Support = new PropertyChangeSupport(this);
    







    m_ParentFrame = null;
    

    m_CloseButPanel = null;
    

    m_readIncrementally = true;
    

    m_showZeroInstancesAsUnknown = false;
    

















    m_showZeroInstancesAsUnknown = showZeroInstancesAsUnknown;
    if (chooser != null) {
      m_FileChooser = chooser;
    }
    
    Messages.getInstance();m_OpenFileBut.setToolTipText(Messages.getString("SetInstancesPanel_OpenFileBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OpenURLBut.setToolTipText(Messages.getString("SetInstancesPanel_OpenURLBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_CloseBut.setToolTipText(Messages.getString("SetInstancesPanel_CloseBut_SetToolTipText_Text"));
    
    m_FileChooser.setFileSelectionMode(0);
    m_OpenURLBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setInstancesFromURLQ();
      }
    });
    m_OpenFileBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setInstancesFromFileQ();
      }
    });
    m_CloseBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeFrame();
      }
    });
    m_Summary.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(1, 2));
    buttons.add(m_OpenFileBut);
    buttons.add(m_OpenURLBut);
    
    m_CloseButPanel = new JPanel();
    m_CloseButPanel.setLayout(new FlowLayout(2));
    m_CloseButPanel.add(m_CloseBut);
    m_CloseButPanel.setVisible(false);
    
    JPanel buttonsAll = new JPanel();
    buttonsAll.setLayout(new BorderLayout());
    buttonsAll.add(buttons, "Center");
    buttonsAll.add(m_CloseButPanel, "South");
    
    setLayout(new BorderLayout());
    add(m_Summary, "Center");
    add(buttonsAll, "South");
  }
  





  public void setParentFrame(JFrame parent)
  {
    m_ParentFrame = parent;
    m_CloseButPanel.setVisible(m_ParentFrame != null);
  }
  





  public JFrame getParentFrame()
  {
    return m_ParentFrame;
  }
  


  public void closeFrame()
  {
    if (m_ParentFrame != null) {
      m_ParentFrame.setVisible(false);
    }
  }
  




  public void setInstancesFromFileQ()
  {
    if (m_IOThread == null) {
      int returnVal = m_FileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        final File selected = m_FileChooser.getSelectedFile();
        m_IOThread = new Thread()
        {
          public void run() {
            setInstancesFromFile(selected);
            m_IOThread = null;
          }
        };
        m_IOThread.setPriority(1);
        m_IOThread.start();
      }
    } else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text_Second"), 2);
    }
  }
  
















  public void setInstancesFromURLQ()
  {
    if (m_IOThread == null) {
      try {
        Messages.getInstance();Messages.getInstance();String urlName = (String)JOptionPane.showInputDialog(this, Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_UrlName_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_UrlName_JOptionPaneShowMessageDialog_Text_Second"), 3, null, null, m_LastURL);
        










        if (urlName != null) {
          m_LastURL = urlName;
          final URL url = new URL(urlName);
          m_IOThread = new Thread()
          {
            public void run() {
              setInstancesFromURL(url);
              m_IOThread = null;
            }
          };
          m_IOThread.setPriority(1);
          m_IOThread.start();
        }
      } catch (Exception ex) {
        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_Exception_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_Exception_JOptionPaneShowMessageDialog_Text_Second"), 0);



      }
      



    }
    else
    {



      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_Exception_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("SetInstancesPanel_SetInstancesFromFileQ_Exception_JOptionPaneShowMessageDialog_Text_Fourth"), 2);
    }
  }
  















  protected void setInstancesFromFile(File f)
  {
    boolean incremental = m_readIncrementally;
    try
    {
      m_Loader = ConverterUtils.getLoaderForFile(f);
      if (m_Loader == null) {
        Messages.getInstance();throw new Exception(Messages.getString("SetInstancesPanel_SetInstancesFromFile_Exception_Text_First") + f);
      }
      


      if (!(m_Loader instanceof IncrementalConverter)) {
        incremental = false;
      }
      
      ((FileSourcedConverter)m_Loader).setFile(f);
      if (incremental) {
        m_Summary.setShowZeroInstancesAsUnknown(m_showZeroInstancesAsUnknown);
        setInstances(m_Loader.getStructure());
      } else {
        m_Summary.setShowZeroInstancesAsUnknown(false);
        setInstances(m_Loader.getDataSet());
      }
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SetInstancesPanel_SetInstancesFromFile_Exception_JOptionPaneShowMessageDialog_Text_First") + f.getName(), Messages.getString("SetInstancesPanel_SetInstancesFromFile_Exception_JOptionPaneShowMessageDialog_Text_Second"), 0);
    }
  }
  
















  protected void setInstancesFromURL(URL u)
  {
    boolean incremental = m_readIncrementally;
    try
    {
      m_Loader = ConverterUtils.getURLLoaderForFile(u.toString());
      if (m_Loader == null) {
        Messages.getInstance();throw new Exception(Messages.getString("SetInstancesPanel_SetInstancesFromURL_Exception_Text_First") + u);
      }
      


      if (!(m_Loader instanceof IncrementalConverter)) {
        incremental = false;
      }
      
      ((URLSourcedLoader)m_Loader).setURL(u.toString());
      if (incremental) {
        m_Summary.setShowZeroInstancesAsUnknown(m_showZeroInstancesAsUnknown);
        setInstances(m_Loader.getStructure());
      } else {
        m_Summary.setShowZeroInstancesAsUnknown(false);
        setInstances(m_Loader.getDataSet());
      }
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SetInstancesPanel_SetInstancesFromURL_Exception_JOptionPaneShowMessageDialog_Text_First") + u, Messages.getString("SetInstancesPanel_SetInstancesFromURL_Exception_JOptionPaneShowMessageDialog_Text_Second"), 0);
    }
  }
  

















  public void setInstances(Instances i)
  {
    m_Instances = i;
    m_Summary.setInstances(m_Instances);
    
    m_Support.firePropertyChange("", null, null);
  }
  





  public Instances getInstances()
  {
    return m_Instances;
  }
  




  public Loader getLoader()
  {
    return m_Loader;
  }
  




  public InstancesSummaryPanel getSummary()
  {
    return m_Summary;
  }
  











  public void setReadIncrementally(boolean incremental)
  {
    m_readIncrementally = incremental;
  }
  




  public boolean getReadIncrementally()
  {
    return m_readIncrementally;
  }
  





  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.addPropertyChangeListener(l);
  }
  





  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.removePropertyChangeListener(l);
  }
}

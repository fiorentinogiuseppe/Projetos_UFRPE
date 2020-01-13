package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import weka.core.Instances;
import weka.gui.Logger;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;





























public class TextViewer
  extends JPanel
  implements TextListener, DataSourceListener, TrainingSetListener, TestSetListener, Visible, UserRequestAcceptor, BeanContextChild, BeanCommon, EventConstraints
{
  private static final long serialVersionUID = 104838186352536832L;
  protected BeanVisual m_visual;
  private transient JFrame m_resultsFrame = null;
  



  private transient JTextArea m_outText = null;
  



  protected transient ResultHistoryPanel m_history;
  



  protected boolean m_design;
  



  protected transient BeanContext m_beanContext = null;
  



  protected BeanContextChildSupport m_bcSupport = new BeanContextChildSupport(this);
  




  private final Vector m_textListeners = new Vector();
  



  public TextViewer()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      appearanceFinal();
    }
  }
  
  protected void appearanceDesign() {
    setUpResultHistory();
    removeAll();
    m_visual = new BeanVisual("TextViewer", "weka/gui/beans/icons/DefaultText.gif", "weka/gui/beans/icons/DefaultText_animated.gif");
    

    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  
  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }
  
  protected void setUpFinal() {
    setUpResultHistory();
    JPanel holder = new JPanel();
    holder.setLayout(new BorderLayout());
    JScrollPane js = new JScrollPane(m_outText);
    Messages.getInstance();js.setBorder(BorderFactory.createTitledBorder(Messages.getString("TextViewer_SetUpFinal_JScrollPane_BorderFactoryCreateTitledBorder_Text")));
    



    holder.add(js, "Center");
    holder.add(m_history, "West");
    
    add(holder, "Center");
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("TextViewer_GlobalInfo_Text");
  }
  
  private void setUpResultHistory() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      if (m_outText == null) {
        m_outText = new JTextArea(20, 80);
        m_history = new ResultHistoryPanel(m_outText);
      }
      m_outText.setEditable(false);
      m_outText.setFont(new Font("Monospaced", 0, 12));
      m_outText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      Messages.getInstance();m_history.setBorder(BorderFactory.createTitledBorder(Messages.getString("TextViewer_SetUpResultHistory_BorderFactoryCreateTitledBorder_Text")));
      




      m_history.setHandleRightClicks(false);
      m_history.getList().addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e) {
          if (((e.getModifiers() & 0x10) != 16) || (e.isAltDown()))
          {
            int index = m_history.getList().locationToIndex(e.getPoint());
            if (index != -1) {
              String name = m_history.getNameAtIndex(index);
              visualize(name, e.getX(), e.getY());
            } else {
              visualize(null, e.getX(), e.getY());
            }
          }
        }
      });
    }
  }
  







  protected void visualize(String name, int x, int y)
  {
    final JPanel panel = this;
    final String selectedName = name;
    JPopupMenu resultListMenu = new JPopupMenu();
    
    Messages.getInstance();JMenuItem visMainBuffer = new JMenuItem(Messages.getString("TextViewer_Visualize_VisMainBuffer_JMenuItem_Text"));
    

    if (selectedName != null) {
      visMainBuffer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_history.setSingle(selectedName);
        }
      });
    } else {
      visMainBuffer.setEnabled(false);
    }
    resultListMenu.add(visMainBuffer);
    
    Messages.getInstance();JMenuItem visSepBuffer = new JMenuItem(Messages.getString("TextViewer_Visualize_VisSepBuffer_JMenuItem_Text"));
    

    if (selectedName != null) {
      visSepBuffer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_history.openFrame(selectedName);
        }
      });
    } else {
      visSepBuffer.setEnabled(false);
    }
    resultListMenu.add(visSepBuffer);
    
    Messages.getInstance();JMenuItem saveOutput = new JMenuItem(Messages.getString("TextViewer_Visualize_SaveOutput_JMenuItem_Text"));
    

    if (selectedName != null) {
      saveOutput.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          SaveBuffer m_SaveOut = new SaveBuffer(null, panel);
          StringBuffer sb = m_history.getNamedBuffer(selectedName);
          if (sb != null) {
            m_SaveOut.save(sb);
          }
        }
      });
    } else {
      saveOutput.setEnabled(false);
    }
    resultListMenu.add(saveOutput);
    
    Messages.getInstance();JMenuItem deleteOutput = new JMenuItem(Messages.getString("TextViewer_Visualize_DeleteOutput_JMenuItem_Text"));
    

    if (selectedName != null) {
      deleteOutput.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_history.removeResult(selectedName);
        }
      });
    } else {
      deleteOutput.setEnabled(false);
    }
    resultListMenu.add(deleteOutput);
    
    resultListMenu.show(m_history.getList(), x, y);
  }
  





  public synchronized void acceptDataSet(DataSetEvent e)
  {
    TextEvent nt = new TextEvent(e.getSource(), e.getDataSet().toString(), e.getDataSet().relationName());
    

    acceptText(nt);
  }
  





  public synchronized void acceptTrainingSet(TrainingSetEvent e)
  {
    TextEvent nt = new TextEvent(e.getSource(), e.getTrainingSet().toString(), e.getTrainingSet().relationName());
    

    acceptText(nt);
  }
  





  public synchronized void acceptTestSet(TestSetEvent e)
  {
    TextEvent nt = new TextEvent(e.getSource(), e.getTestSet().toString(), e.getTestSet().relationName());
    

    acceptText(nt);
  }
  





  public synchronized void acceptText(TextEvent e)
  {
    if (m_outText == null) {
      setUpResultHistory();
    }
    StringBuffer result = new StringBuffer();
    result.append(e.getText());
    

    String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
    name = name + e.getTextTitle();
    
    if (m_outText != null)
    {

      int mod = 2;
      String nameOrig = new String(name);
      while (m_history.getNamedBuffer(name) != null) {
        name = new String(nameOrig + "" + mod);
        mod++;
      }
      m_history.addResult(name, result);
      m_history.setSingle(name);
    }
    

    notifyTextListeners(e);
  }
  





  public void setVisual(BeanVisual newVisual)
  {
    m_visual = newVisual;
  }
  



  public BeanVisual getVisual()
  {
    return m_visual;
  }
  



  public void useDefaultVisual()
  {
    m_visual.loadIcons("weka/gui/beans/icons/DefaultText.gif", "weka/gui/beans/icons/DefaultText_animated.gif");
  }
  



  public void showResults()
  {
    if (m_resultsFrame == null) {
      if (m_outText == null) {
        setUpResultHistory();
      }
      Messages.getInstance();m_resultsFrame = new JFrame(Messages.getString("TextViewer_ShowResults_ResultsFrame_JFrame_Text"));
      

      m_resultsFrame.getContentPane().setLayout(new BorderLayout());
      JScrollPane js = new JScrollPane(m_outText);
      Messages.getInstance();js.setBorder(BorderFactory.createTitledBorder(Messages.getString("TextViewer_ShowResults_Js_SetBorder_BorderFactoryCreateTitledBorder_Text")));
      




      JSplitPane p2 = new JSplitPane(1, m_history, js);
      
      m_resultsFrame.getContentPane().add(p2, "Center");
      

      m_resultsFrame.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          m_resultsFrame.dispose();
          m_resultsFrame = null;
        }
      });
      m_resultsFrame.pack();
      m_resultsFrame.setVisible(true);
    } else {
      m_resultsFrame.toFront();
    }
  }
  





  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    
    newVector.addElement("Show results");
    
    newVector.addElement("?Clear results");
    return newVector.elements();
  }
  






  public void performRequest(String request)
  {
    if (request.compareTo("Show results") == 0) {
      showResults();
    } else if (request.compareTo("Clear results") == 0) {
      m_outText.setText("");
      m_history.clearResults();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("TextViewer_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  








  public void addPropertyChangeListener(String name, PropertyChangeListener pcl)
  {
    m_bcSupport.addPropertyChangeListener(name, pcl);
  }
  







  public void removePropertyChangeListener(String name, PropertyChangeListener pcl)
  {
    m_bcSupport.removePropertyChangeListener(name, pcl);
  }
  






  public void addVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.addVetoableChangeListener(name, vcl);
  }
  







  public void removeVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.removeVetoableChangeListener(name, vcl);
  }
  





  public void setBeanContext(BeanContext bc)
  {
    m_beanContext = bc;
    m_design = m_beanContext.isDesignTime();
    if (m_design) {
      appearanceDesign();
    } else {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      
      if (!GraphicsEnvironment.isHeadless()) {
        appearanceFinal();
      }
    }
  }
  


  private void notifyTextListeners(TextEvent ge)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_textListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((TextListener)l.elementAt(i)).acceptText(ge);
      }
    }
  }
  





  public BeanContext getBeanContext()
  {
    return m_beanContext;
  }
  






  public void stop() {}
  





  public boolean isBusy()
  {
    return false;
  }
  






  public void setLog(Logger logger) {}
  






  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  





  public String getCustomName()
  {
    return m_visual.getText();
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  







  public boolean connectionAllowed(String eventName)
  {
    return true;
  }
  










  public void connectionNotification(String eventName, Object source) {}
  










  public void disconnectionNotification(String eventName, Object source) {}
  









  public boolean eventGeneratable(String eventName)
  {
    if (eventName.equals("text")) {
      return true;
    }
    return false;
  }
  




  public synchronized void addTextListener(TextListener cl)
  {
    m_textListeners.addElement(cl);
  }
  




  public synchronized void removeTextListener(TextListener cl)
  {
    m_textListeners.remove(cl);
  }
  
  public static void main(String[] args) {
    try {
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      
      TextViewer tv = new TextViewer();
      
      Messages.getInstance();Messages.getInstance();tv.acceptText(new TextEvent(tv, Messages.getString("TextViewer_Main_TextEvent_Text_First"), Messages.getString("TextViewer_Main_TextEvent_Text_Second")));
      

      jf.getContentPane().add(tv, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.setSize(800, 600);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

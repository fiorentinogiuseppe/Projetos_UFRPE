package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import weka.gui.visualize.PrintableComponent;









































public class ResultHistoryPanel
  extends JPanel
{
  static final long serialVersionUID = 4297069440135326829L;
  protected JTextComponent m_SingleText;
  protected String m_SingleName;
  protected DefaultListModel m_Model = new DefaultListModel();
  

  protected JList m_List = new JList(m_Model);
  

  protected Hashtable m_Results = new Hashtable();
  

  protected Hashtable m_FramedOutput = new Hashtable();
  

  protected Hashtable m_Objs = new Hashtable();
  


  protected boolean m_HandleRightClicks = true;
  

  protected PrintableComponent m_Printer = null;
  

























  public ResultHistoryPanel(JTextComponent text)
  {
    m_SingleText = text;
    if (text != null) {
      m_Printer = new PrintableComponent(m_SingleText);
    }
    m_List.setSelectionMode(0);
    m_List.addMouseListener(new RMouseAdapter() {
      private static final long serialVersionUID = -9015397020486290479L;
      
      public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & 0x10) == 16)
        {
          if (((e.getModifiers() & 0x40) == 0) && ((e.getModifiers() & 0x80) == 0))
          {
            int index = m_List.locationToIndex(e.getPoint());
            if ((index != -1) && (m_SingleText != null)) {
              setSingle((String)m_Model.elementAt(index));
            }
            
          }
          
        }
        else if (m_HandleRightClicks) {
          int index = m_List.locationToIndex(e.getPoint());
          if (index != -1) {
            String name = (String)m_Model.elementAt(index);
            openFrame(name);
          }
          
        }
        
      }
    });
    m_List.addKeyListener(new RKeyAdapter() {
      private static final long serialVersionUID = 7910681776999302344L;
      
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 127) {
          int selected = m_List.getSelectedIndex();
          if (selected != -1) {
            removeResult((String)m_Model.elementAt(selected));
          }
        }
      }
    });
    m_List.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          ListSelectionModel lm = (ListSelectionModel)e.getSource();
          for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
            if (lm.isSelectedIndex(i))
            {
              if ((i == -1) || (m_SingleText == null)) break;
              setSingle((String)m_Model.elementAt(i)); break;
            }
            
          }
          
        }
        
      }
      
    });
    setLayout(new BorderLayout());
    
    JScrollPane js = new JScrollPane(m_List);
    js.getViewport().addChangeListener(new ChangeListener() {
      private int lastHeight;
      
      public void stateChanged(ChangeEvent e) { JViewport vp = (JViewport)e.getSource();
        int h = getViewSizeheight;
        if (h != lastHeight) {
          lastHeight = h;
          int x = h - getExtentSizeheight;
          vp.setViewPosition(new Point(0, x));
        }
      }
    });
    add(js, "Center");
  }
  






  public void addResult(String name, StringBuffer result)
  {
    m_Model.addElement(name);
    m_Results.put(name, result);
  }
  






  public void removeResult(String name)
  {
    StringBuffer buff = (StringBuffer)m_Results.get(name);
    if (buff != null) {
      m_Results.remove(name);
      m_Model.removeElement(name);
      m_Objs.remove(name);
      System.gc();
    }
  }
  



  public void clearResults()
  {
    m_Results.clear();
    m_Model.clear();
    m_Objs.clear();
    System.gc();
  }
  




  public void addObject(String name, Object o)
  {
    m_Objs.put(name, o);
  }
  





  public Object getNamedObject(String name)
  {
    Object v = null;
    v = m_Objs.get(name);
    return v;
  }
  






  public Object getSelectedObject()
  {
    Object v = null;
    int index = m_List.getSelectedIndex();
    if (index != -1) {
      String name = (String)m_Model.elementAt(index);
      v = m_Objs.get(name);
    }
    
    return v;
  }
  




  public StringBuffer getNamedBuffer(String name)
  {
    StringBuffer b = null;
    b = (StringBuffer)m_Results.get(name);
    return b;
  }
  





  public StringBuffer getSelectedBuffer()
  {
    StringBuffer b = null;
    int index = m_List.getSelectedIndex();
    if (index != -1) {
      String name = (String)m_Model.elementAt(index);
      b = (StringBuffer)m_Results.get(name);
    }
    return b;
  }
  




  public String getSelectedName()
  {
    int index = m_List.getSelectedIndex();
    if (index != -1) {
      return (String)m_Model.elementAt(index);
    }
    return null;
  }
  



  public String getNameAtIndex(int index)
  {
    if (index != -1) {
      return (String)m_Model.elementAt(index);
    }
    return null;
  }
  





  public void setSingle(String name)
  {
    StringBuffer buff = (StringBuffer)m_Results.get(name);
    if (buff != null) {
      m_SingleName = name;
      m_SingleText.setText(buff.toString());
      m_List.setSelectedValue(name, true);
    }
  }
  





  public void openFrame(String name)
  {
    StringBuffer buff = (StringBuffer)m_Results.get(name);
    JTextComponent currentText = (JTextComponent)m_FramedOutput.get(name);
    if ((buff != null) && (currentText == null))
    {
      JTextArea ta = new JTextArea();
      ta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      ta.setFont(new Font("Monospaced", 0, 12));
      ta.setEditable(false);
      ta.setText(buff.toString());
      m_FramedOutput.put(name, ta);
      final JFrame jf = new JFrame(name);
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          m_FramedOutput.remove(jf.getTitle());
          jf.dispose();
        }
      });
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(new JScrollPane(ta), "Center");
      jf.pack();
      jf.setSize(450, 350);
      jf.setVisible(true);
    }
  }
  






  public void updateResult(String name)
  {
    StringBuffer buff = (StringBuffer)m_Results.get(name);
    if (buff == null) {
      return;
    }
    if (m_SingleName == name) {
      m_SingleText.setText(buff.toString());
    }
    JTextComponent currentText = (JTextComponent)m_FramedOutput.get(name);
    if (currentText != null) {
      currentText.setText(buff.toString());
    }
  }
  





  public ListSelectionModel getSelectionModel()
  {
    return m_List.getSelectionModel();
  }
  



  public JList getList()
  {
    return m_List;
  }
  




  public void setHandleRightClicks(boolean tf)
  {
    m_HandleRightClicks = tf;
  }
  





  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("ResultHistoryPanel_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      ResultHistoryPanel jd = new ResultHistoryPanel(null);
      Messages.getInstance();Messages.getInstance();jd.addResult(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_First"), new StringBuffer(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_Second")));
      Messages.getInstance();Messages.getInstance();jd.addResult(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_Third"), new StringBuffer(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_Fourth")));
      Messages.getInstance();Messages.getInstance();jd.addResult(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_Fifth"), new StringBuffer(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_Sixth")));
      Messages.getInstance();Messages.getInstance();jd.addResult(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_Seventh"), new StringBuffer(Messages.getString("ResultHistoryPanel_Main_Jd_JFrame_AddResult_Text_Eighth")));
      jf.getContentPane().add(jd, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
  public static class RKeyAdapter
    extends KeyAdapter
    implements Serializable
  {
    static final long serialVersionUID = -8675332541861828079L;
    
    public RKeyAdapter() {}
  }
  
  public static class RMouseAdapter
    extends MouseAdapter
    implements Serializable
  {
    static final long serialVersionUID = -8991922650552358669L;
    
    public RMouseAdapter() {}
  }
}

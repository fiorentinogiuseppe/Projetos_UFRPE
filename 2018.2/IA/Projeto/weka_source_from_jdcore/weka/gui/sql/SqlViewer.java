package weka.gui.sql;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import weka.core.Memory;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.LookAndFeel;
import weka.gui.sql.event.ConnectionEvent;
import weka.gui.sql.event.ConnectionListener;
import weka.gui.sql.event.HistoryChangedEvent;
import weka.gui.sql.event.HistoryChangedListener;
import weka.gui.sql.event.QueryExecuteEvent;
import weka.gui.sql.event.QueryExecuteListener;
import weka.gui.sql.event.ResultChangedEvent;
import weka.gui.sql.event.ResultChangedListener;



























































public class SqlViewer
  extends JPanel
  implements ConnectionListener, HistoryChangedListener, QueryExecuteListener, ResultChangedListener
{
  private static final long serialVersionUID = -4395028775566514329L;
  protected static final String HISTORY_FILE = "SqlViewerHistory.props";
  public static final String WIDTH = "width";
  public static final String HEIGHT = "height";
  protected JFrame m_Parent;
  protected ConnectionPanel m_ConnectionPanel;
  protected QueryPanel m_QueryPanel;
  protected ResultPanel m_ResultPanel;
  protected InfoPanel m_InfoPanel;
  protected String m_URL;
  protected String m_User;
  protected String m_Password;
  protected String m_Query;
  protected Properties m_History;
  
  public SqlViewer(JFrame parent)
  {
    m_Parent = parent;
    m_URL = "";
    m_User = "";
    m_Password = "";
    m_Query = "";
    m_History = new Properties();
    
    createPanel();
  }
  





  protected void createPanel()
  {
    setLayout(new BorderLayout());
    

    m_ConnectionPanel = new ConnectionPanel(m_Parent);
    JPanel panel = new JPanel(new BorderLayout());
    add(panel, "North");
    Messages.getInstance();panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SqlViewer_CreatePanel_Panel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    panel.add(m_ConnectionPanel, "Center");
    

    m_QueryPanel = new QueryPanel(m_Parent);
    panel = new JPanel(new BorderLayout());
    add(panel, "Center");
    JPanel panel2 = new JPanel(new BorderLayout());
    Messages.getInstance();panel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SqlViewer_CreatePanel_Panel2_BorderFactoryCreateTitledBorder_Text_First")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    






    panel2.add(m_QueryPanel, "North");
    panel.add(panel2, "North");
    

    m_ResultPanel = new ResultPanel(m_Parent);
    m_ResultPanel.setQueryPanel(m_QueryPanel);
    panel2 = new JPanel(new BorderLayout());
    Messages.getInstance();panel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SqlViewer_CreatePanel_Panel2_BorderFactoryCreateTitledBorder_Text_Second")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    






    panel2.add(m_ResultPanel, "Center");
    panel.add(panel2, "Center");
    

    m_InfoPanel = new InfoPanel(m_Parent);
    panel = new JPanel(new BorderLayout());
    add(panel, "South");
    Messages.getInstance();panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SqlViewer_CreatePanel_Panel_BorderFactoryCreateTitledBorder_Text_Second")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    






    panel.add(m_InfoPanel, "Center");
    

    addConnectionListener(this);
    addConnectionListener(m_QueryPanel);
    addQueryExecuteListener(this);
    addQueryExecuteListener(m_ResultPanel);
    addResultChangedListener(this);
    addHistoryChangedListener(this);
    

    loadHistory(true);
  }
  






  public void connectionChange(ConnectionEvent evt)
  {
    if (evt.getType() == 1) {
      Messages.getInstance();m_InfoPanel.append(Messages.getString("SqlViewer_ConnectionChange_InfoPanel_Text_First") + evt.getDbUtils().getDatabaseURL(), "information_small.gif");

    }
    else
    {
      Messages.getInstance();m_InfoPanel.append(Messages.getString("SqlViewer_ConnectionChange_InfoPanel_Text_Second") + evt.getDbUtils().getDatabaseURL() + " = " + evt.isConnected(), "information_small.gif");
    }
    





    if (evt.getException() != null) {
      Messages.getInstance();m_InfoPanel.append(Messages.getString("SqlViewer_ConnectionChange_InfoPanel_Text_Third") + evt.getException(), "error_small.gif");
    }
    




    if (evt.isConnected()) {
      m_QueryPanel.setFocus();
    } else {
      m_ConnectionPanel.setFocus();
    }
  }
  







  public void queryExecuted(QueryExecuteEvent evt)
  {
    if (evt.failed()) {
      Messages.getInstance();m_InfoPanel.append(Messages.getString("SqlViewer_QueryExecuted_InfoPanel_Text_First") + evt.getQuery(), "error_small.gif");
      


      Messages.getInstance();m_InfoPanel.append(Messages.getString("SqlViewer_QueryExecuted_InfoPanel_Text_Second") + evt.getException(), "error_small.gif");

    }
    else
    {
      Messages.getInstance();m_InfoPanel.append(Messages.getString("SqlViewer_QueryExecuted_InfoPanel_Text_Third") + evt.getQuery(), "information_small.gif");
      

      try
      {
        if (evt.hasResult()) {
          ResultSetHelper helper = new ResultSetHelper(evt.getResultSet());
          if ((evt.getMaxRows() > 0) && (helper.getRowCount() >= evt.getMaxRows()))
          {
            Messages.getInstance();Messages.getInstance();m_InfoPanel.append(helper.getRowCount() + Messages.getString("SqlViewer_QueryExecuted_InfoPanel_Text_Fourth") + evt.getMaxRows() + Messages.getString("SqlViewer_QueryExecuted_InfoPanel_Text_Fifth"), "information_small.gif");






          }
          else if (helper.getRowCount() == -1) {
            Messages.getInstance();m_InfoPanel.append(Messages.getString("SqlViewer_QueryExecuted_InfoPanel_Text_Sixth"), "information_small.gif");

          }
          else
          {
            Messages.getInstance();m_InfoPanel.append(helper.getRowCount() + Messages.getString("SqlViewer_QueryExecuted_InfoPanel_Text_Seventh"), "information_small.gif");
          }
        }
        





        loadHistory(false);
        m_History.setProperty("max_rows", Integer.toString(evt.getMaxRows()));
        
        saveHistory();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  





  public void resultChanged(ResultChangedEvent evt)
  {
    m_URL = evt.getURL();
    m_User = evt.getUser();
    m_Password = evt.getPassword();
    m_Query = evt.getQuery();
  }
  







  public void historyChanged(HistoryChangedEvent evt)
  {
    loadHistory(false);
    
    m_History.setProperty(evt.getHistoryName(), modelToString(evt.getHistory()));
    


    saveHistory();
  }
  




  protected String getHistoryFilename()
  {
    return System.getProperties().getProperty("user.home") + File.separatorChar + "SqlViewerHistory.props";
  }
  














  protected DefaultListModel stringToModel(String s)
  {
    DefaultListModel result = new DefaultListModel();
    

    String[] find = { "\"\"", "\\n", "\\r", "\\t" };
    String[] replace = { "\"", "\n", "\r", "\t" };
    for (int i = 0; i < find.length; i++) {
      String tmpStr = "";
      while (s.length() > 0) {
        int index = s.indexOf(find[i]);
        if (index > -1) {
          tmpStr = tmpStr + s.substring(0, index) + replace[i];
          s = s.substring(index + 2);
        } else {
          tmpStr = tmpStr + s;
          s = "";
        }
      }
      s = tmpStr;
    }
    
    boolean quote = false;
    String tmpStr = "";
    for (i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '"') {
        quote = !quote;
        tmpStr = tmpStr + "" + s.charAt(i);
      } else if (s.charAt(i) == ',') {
        if (quote) {
          tmpStr = tmpStr + "" + s.charAt(i);
        } else {
          if (tmpStr.startsWith("\"")) {
            tmpStr = tmpStr.substring(1, tmpStr.length() - 1);
          }
          result.addElement(tmpStr);
          tmpStr = "";
        }
      } else {
        tmpStr = tmpStr + "" + s.charAt(i);
      }
    }
    

    if (!tmpStr.equals("")) {
      if (tmpStr.startsWith("\"")) {
        tmpStr = tmpStr.substring(1, tmpStr.length() - 1);
      }
      result.addElement(tmpStr);
    }
    
    return result;
  }
  











  protected String modelToString(DefaultListModel m)
  {
    String result = "";
    
    for (int i = 0; i < m.size(); i++) {
      if (i > 0) {
        result = result + ",";
      }
      
      String tmpStr = m.get(i).toString();
      boolean quote = (tmpStr.indexOf(",") > -1) || (tmpStr.indexOf(" ") > -1);
      
      if (quote) {
        result = result + "\"";
      }
      
      for (int n = 0; n < tmpStr.length(); n++)
      {
        if (tmpStr.charAt(n) == '"') {
          result = result + "\"\"";
        }
        else {
          result = result + "" + tmpStr.charAt(n);
        }
      }
      
      if (quote) {
        result = result + "\"";
      }
    }
    
    return result;
  }
  









  protected void loadHistory(boolean set)
  {
    try
    {
      File file = new File(getHistoryFilename());
      if (file.exists()) {
        BufferedInputStream str = new BufferedInputStream(new FileInputStream(getHistoryFilename()));
        m_History.load(str);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    

    if (set) {
      m_ConnectionPanel.setHistory(stringToModel(m_History.getProperty("connection", "")));
      
      m_QueryPanel.setHistory(stringToModel(m_History.getProperty("query", "")));
      
      m_QueryPanel.setMaxRows(Integer.parseInt(m_History.getProperty("max_rows", "100")));
      

      int width = Integer.parseInt(m_History.getProperty("width", "0"));
      int height = Integer.parseInt(m_History.getProperty("height", "0"));
      if ((width != 0) && (height != 0)) {
        setPreferredSize(new Dimension(width, height));
      }
    }
  }
  





  protected void saveHistory()
  {
    try
    {
      BufferedOutputStream str = new BufferedOutputStream(new FileOutputStream(getHistoryFilename()));
      m_History.store(str, "SQL-Viewer-History");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public void saveSize()
  {
    m_History.setProperty("width", "" + getSizewidth);
    m_History.setProperty("height", "" + getSizeheight);
    
    saveHistory();
  }
  



  public void clear()
  {
    m_ConnectionPanel.clear();
    m_QueryPanel.clear();
    m_ResultPanel.clear();
    m_InfoPanel.clear();
  }
  






  public String getURL()
  {
    return m_URL;
  }
  






  public String getUser()
  {
    return m_User;
  }
  






  public String getPassword()
  {
    return m_Password;
  }
  






  public String getQuery()
  {
    return m_Query;
  }
  




  public void addConnectionListener(ConnectionListener l)
  {
    m_ConnectionPanel.addConnectionListener(l);
  }
  




  public void removeConnectionListener(ConnectionListener l)
  {
    m_ConnectionPanel.removeConnectionListener(l);
  }
  




  public void addQueryExecuteListener(QueryExecuteListener l)
  {
    m_QueryPanel.addQueryExecuteListener(l);
  }
  




  public void removeQueryExecuteListener(QueryExecuteListener l)
  {
    m_QueryPanel.removeQueryExecuteListener(l);
  }
  




  public void addResultChangedListener(ResultChangedListener l)
  {
    m_ResultPanel.addResultChangedListener(l);
  }
  




  public void removeResultChangedListener(ResultChangedListener l)
  {
    m_ResultPanel.removeResultChangedListener(l);
  }
  




  public void addHistoryChangedListener(HistoryChangedListener l)
  {
    m_ConnectionPanel.addHistoryChangedListener(l);
    m_QueryPanel.addHistoryChangedListener(l);
  }
  




  public void removeHistoryChangedListener(HistoryChangedListener l)
  {
    m_ConnectionPanel.removeHistoryChangedListener(l);
    m_QueryPanel.removeHistoryChangedListener(l);
  }
  

  private static Memory m_Memory = new Memory(true);
  


  private static SqlViewer m_Viewer;
  



  public static void main(String[] args)
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("SqlViewer_Main_Log_Text"));
    
    LookAndFeel.setLookAndFeel();
    


    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("SqlViewer_Main_JFrame_Text"));
      
      m_Viewer = new SqlViewer(jf);
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(m_Viewer, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          SqlViewer.m_Viewer.saveSize();
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setSize(800, 600);
      jf.setVisible(true);
      
      Thread memMonitor = new Thread()
      {

        public void run()
        {

          for (;;)
          {

            if (SqlViewer.m_Memory.isOutOfMemory())
            {
              val$jf.dispose();
              SqlViewer.access$002(null);
              System.gc();
              

              Messages.getInstance();System.err.println(Messages.getString("SqlViewer_Main_Error_Text_First"));
              
              SqlViewer.m_Memory.showOutOfMemory();
              Messages.getInstance();System.err.println(Messages.getString("SqlViewer_Main_Error_Text_Second"));
              
              System.exit(-1);

            }
            

          }
          
        }
        

      };
      memMonitor.setPriority(10);
      memMonitor.start();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

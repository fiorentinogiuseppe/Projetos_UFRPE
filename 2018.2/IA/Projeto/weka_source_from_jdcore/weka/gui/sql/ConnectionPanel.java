package weka.gui.sql;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import weka.gui.DatabaseConnectionDialog;
import weka.gui.ListSelectorDialog;
import weka.gui.sql.event.ConnectionEvent;
import weka.gui.sql.event.ConnectionListener;
import weka.gui.sql.event.HistoryChangedEvent;
import weka.gui.sql.event.HistoryChangedListener;







































public class ConnectionPanel
  extends JPanel
  implements CaretListener
{
  static final long serialVersionUID = 3499317023969723490L;
  public static final String HISTORY_NAME = "connection";
  protected JFrame m_Parent = null;
  

  protected DatabaseConnectionDialog m_DbDialog;
  

  protected String m_URL = "";
  

  protected String m_User = "";
  

  protected String m_Password = "";
  protected JLabel m_LabelURL;
  
  public ConnectionPanel(JFrame parent) { Messages.getInstance();m_LabelURL = new JLabel(Messages.getString("ConnectionPanel_LabelURL_JLabel_Text"));
    

    m_TextURL = new JTextField(40);
    

    Messages.getInstance();m_ButtonDatabase = new JButton(Messages.getString("ConnectionPanel_ButtonDatabase_JButton_Text"));
    

    Messages.getInstance();m_ButtonConnect = new JButton(Messages.getString("ConnectionPanel_ButtonConnect_JButton_Text"));
    

    Messages.getInstance();m_ButtonHistory = new JButton(Messages.getString("ConnectionPanel_ButtonHistory_JButton_Text"));
    










    m_History = new DefaultListModel();
    








    m_Parent = parent;
    m_ConnectionListeners = new HashSet();
    m_HistoryChangedListeners = new HashSet();
    try
    {
      m_DbUtils = new DbUtils();
      m_URL = m_DbUtils.getDatabaseURL();
      m_User = m_DbUtils.getUsername();
      m_Password = m_DbUtils.getPassword();
    }
    catch (Exception e) {
      e.printStackTrace();
      m_URL = "";
      m_User = "";
      m_Password = "";
    }
    
    createPanel();
  }
  


  protected JTextField m_TextURL;
  

  protected void createPanel()
  {
    setLayout(new BorderLayout());
    JPanel panel2 = new JPanel(new FlowLayout());
    add(panel2, "West");
    

    m_LabelURL.setLabelFor(m_ButtonDatabase);
    m_LabelURL.setDisplayedMnemonic('U');
    panel2.add(m_LabelURL);
    

    m_TextURL.setText(m_URL);
    m_TextURL.addCaretListener(this);
    panel2.add(m_TextURL);
    

    JPanel panel = new JPanel(new FlowLayout());
    panel2.add(panel);
    
    m_ButtonDatabase.setMnemonic('s');
    m_ButtonDatabase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showDialog();
      }
    });
    panel.add(m_ButtonDatabase);
    
    m_ButtonConnect.setMnemonic('n');
    m_ButtonConnect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        connect();
      }
    });
    panel.add(m_ButtonConnect);
    
    m_ButtonHistory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showHistory();
      }
    });
    panel.add(m_ButtonHistory);
    
    setButtons();
  }
  




  protected void setButtons()
  {
    boolean isEmpty = m_TextURL.getText().equals("");
    
    m_ButtonConnect.setEnabled(!isEmpty);
    m_ButtonDatabase.setEnabled(!isEmpty);
    m_ButtonHistory.setEnabled(m_History.size() > 0);
  }
  


  public void clear()
  {
    setURL(m_DbUtils.getDatabaseURL());
    setUser(m_DbUtils.getUsername());
    setPassword(m_DbUtils.getPassword());
  }
  


  public void setFocus()
  {
    m_TextURL.requestFocus();
  }
  




  public void setURL(String url)
  {
    m_URL = url;
    m_TextURL.setText(url);
  }
  




  public String getURL()
  {
    m_URL = m_TextURL.getText();
    return m_URL;
  }
  




  public void setUser(String user)
  {
    m_User = user;
  }
  




  public String getUser()
  {
    return m_User;
  }
  




  public void setPassword(String pw)
  {
    m_Password = pw;
  }
  




  public String getPassword()
  {
    return m_Password;
  }
  




  protected void addHistory(String s)
  {
    if (s.equals("")) {
      return;
    }
    
    if (m_History.contains(s)) {
      m_History.removeElement(s);
    }
    m_History.add(0, s);
    

    notifyHistoryChangedListeners();
  }
  


  protected JButton m_ButtonDatabase;
  
  protected JButton m_ButtonConnect;
  
  public void setHistory(DefaultListModel history)
  {
    m_History.clear();
    for (int i = 0; i < history.size(); i++) {
      m_History.addElement(history.get(i));
    }
    setButtons();
  }
  




  public DefaultListModel getHistory()
  {
    return m_History;
  }
  


  protected void showDialog()
  {
    m_DbDialog = new DatabaseConnectionDialog(m_Parent, getURL(), getUser(), false);
    m_DbDialog.setVisible(true);
    if (m_DbDialog.getReturnValue() == 0) {
      setURL(m_DbDialog.getURL());
      setUser(m_DbDialog.getUsername());
      setPassword(m_DbDialog.getPassword());
    }
    
    setButtons();
  }
  



  protected void connect()
  {
    if (m_DbUtils.isConnected()) {
      try {
        m_DbUtils.disconnectFromDatabase();
        notifyConnectionListeners(1);
      }
      catch (Exception e) {
        e.printStackTrace();
        notifyConnectionListeners(1, e);
      }
    }
    
    try
    {
      m_DbUtils.setDatabaseURL(getURL());
      m_DbUtils.setUsername(getUser());
      m_DbUtils.setPassword(getPassword());
      m_DbUtils.connectToDatabase();
      notifyConnectionListeners(0);
      
      addHistory(getUser() + "@" + getURL());
    }
    catch (Exception e) {
      e.printStackTrace();
      notifyConnectionListeners(0, e);
    }
    
    setButtons();
  }
  






  public void showHistory()
  {
    JList list = new JList(m_History);
    ListSelectorDialog dialog = new ListSelectorDialog(m_Parent, list);
    
    if ((dialog.showDialog() == 0) && 
      (list.getSelectedValue() != null)) {
      String tmpStr = list.getSelectedValue().toString();
      if (tmpStr.indexOf("@") > -1) {
        setUser(tmpStr.substring(0, tmpStr.indexOf("@")));
        setURL(tmpStr.substring(tmpStr.indexOf("@") + 1));
        showDialog();
      }
      else {
        setUser("");
        setURL(tmpStr);
      }
    }
    

    setButtons();
  }
  




  public void addConnectionListener(ConnectionListener l)
  {
    m_ConnectionListeners.add(l);
  }
  




  public void removeConnectionListener(ConnectionListener l)
  {
    m_ConnectionListeners.remove(l);
  }
  




  protected void notifyConnectionListeners(int type)
  {
    notifyConnectionListeners(type, null);
  }
  

  protected JButton m_ButtonHistory;
  
  protected HashSet m_ConnectionListeners;
  
  protected HashSet m_HistoryChangedListeners;
  protected DbUtils m_DbUtils;
  protected DefaultListModel m_History;
  protected void notifyConnectionListeners(int type, Exception ex)
  {
    Iterator iter = m_ConnectionListeners.iterator();
    while (iter.hasNext()) {
      ConnectionListener l = (ConnectionListener)iter.next();
      l.connectionChange(new ConnectionEvent(this, type, m_DbUtils, ex));
    }
  }
  





  public void addHistoryChangedListener(HistoryChangedListener l)
  {
    m_HistoryChangedListeners.add(l);
  }
  




  public void removeHistoryChangedListener(HistoryChangedListener l)
  {
    m_HistoryChangedListeners.remove(l);
  }
  





  protected void notifyHistoryChangedListeners()
  {
    Iterator iter = m_HistoryChangedListeners.iterator();
    while (iter.hasNext()) {
      HistoryChangedListener l = (HistoryChangedListener)iter.next();
      l.historyChanged(new HistoryChangedEvent(this, "connection", getHistory()));
    }
  }
  





  public void caretUpdate(CaretEvent event)
  {
    setButtons();
  }
}

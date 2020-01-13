package weka.gui.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import weka.gui.ListSelectorDialog;
import weka.gui.sql.event.ConnectionEvent;
import weka.gui.sql.event.ConnectionListener;
import weka.gui.sql.event.HistoryChangedEvent;
import weka.gui.sql.event.HistoryChangedListener;
import weka.gui.sql.event.QueryExecuteEvent;
import weka.gui.sql.event.QueryExecuteListener;






























public class QueryPanel
  extends JPanel
  implements ConnectionListener, CaretListener
{
  private static final long serialVersionUID = 4348967824619706636L;
  public static final String HISTORY_NAME = "query";
  public static final String MAX_ROWS = "max_rows";
  protected JFrame m_Parent;
  protected JTextArea m_TextQuery;
  protected JButton m_ButtonExecute;
  protected JButton m_ButtonClear;
  protected JButton m_ButtonHistory;
  protected JSpinner m_SpinnerMaxRows;
  protected HashSet m_QueryExecuteListeners;
  protected HashSet m_HistoryChangedListeners;
  protected DbUtils m_DbUtils;
  protected boolean m_Connected;
  protected DefaultListModel m_History;
  
  public QueryPanel(JFrame parent)
  {
    Messages.getInstance();m_ButtonExecute = new JButton(Messages.getString("QueryPanel_ButtonExecute_JButton_Text"));
    

    Messages.getInstance();m_ButtonClear = new JButton(Messages.getString("QueryPanel_ButtonClear_JButton_Text"));
    

    Messages.getInstance();m_ButtonHistory = new JButton(Messages.getString("QueryPanel_ButtonHistory_JButton_Text"));
    

    m_SpinnerMaxRows = new JSpinner();
    













    m_History = new DefaultListModel();
    








    m_Parent = parent;
    m_QueryExecuteListeners = new HashSet();
    m_HistoryChangedListeners = new HashSet();
    m_DbUtils = null;
    m_Connected = false;
    
    createPanel();
  }
  







  protected void createPanel()
  {
    setLayout(new BorderLayout());
    

    m_TextQuery = new JTextArea();
    m_TextQuery.addCaretListener(this);
    m_TextQuery.setFont(new Font("Monospaced", 0, m_TextQuery.getFont().getSize()));
    
    add(new JScrollPane(m_TextQuery), "Center");
    

    JPanel panel = new JPanel(new BorderLayout());
    add(panel, "East");
    m_ButtonExecute.setMnemonic('E');
    m_ButtonExecute.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execute();
      }
    });
    panel.add(m_ButtonExecute, "North");
    JPanel panel2 = new JPanel(new BorderLayout());
    panel.add(panel2, "Center");
    m_ButtonClear.setMnemonic('r');
    m_ButtonClear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clear();
      }
    });
    panel2.add(m_ButtonClear, "North");
    JPanel panel3 = new JPanel(new BorderLayout());
    panel2.add(panel3, "Center");
    m_ButtonHistory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showHistory();
      }
    });
    panel3.add(m_ButtonHistory, "North");
    

    panel3 = new JPanel(new FlowLayout());
    Messages.getInstance();panel3.add(new JLabel(Messages.getString("QueryPanel_CreatePanel_Panel3_JLabel_Text")));
    panel3.add(m_SpinnerMaxRows);
    panel2.add(panel3, "South");
    SpinnerNumberModel model = (SpinnerNumberModel)m_SpinnerMaxRows.getModel();
    model.setMaximum(new Integer(Integer.MAX_VALUE));
    model.setMinimum(new Integer(0));
    model.setValue(new Integer(100));
    model.setStepSize(new Integer(100));
    m_SpinnerMaxRows.setMinimumSize(new Dimension(50, m_SpinnerMaxRows.getHeight()));
    
    Messages.getInstance();m_SpinnerMaxRows.setToolTipText(Messages.getString("QueryPanel_CreatePanel_SpinnerMaxRows_SetToolTipText_Text"));
    

    setButtons();
  }
  


  public void setFocus()
  {
    m_TextQuery.requestFocus();
  }
  




  protected void setButtons()
  {
    boolean isEmpty = m_TextQuery.getText().trim().equals("");
    
    m_ButtonExecute.setEnabled((m_Connected) && (!isEmpty));
    m_ButtonClear.setEnabled(!isEmpty);
    m_ButtonHistory.setEnabled(m_History.size() > 0);
  }
  





  public void connectionChange(ConnectionEvent evt)
  {
    m_Connected = evt.isConnected();
    m_DbUtils = evt.getDbUtils();
    setButtons();
  }
  






  public void execute()
  {
    if (!m_ButtonExecute.isEnabled()) {
      return;
    }
    
    if (m_TextQuery.getText().trim().equals("")) {
      return;
    }
    try
    {
      if (m_DbUtils.getResultSet() != null) {
        m_DbUtils.close();
      }
    }
    catch (Exception e) {}
    

    Exception ex = null;
    ResultSet rs = null;
    try
    {
      if (m_DbUtils.execute(getQuery())) {
        rs = m_DbUtils.getResultSet();
        
        addHistory(getQuery());
      }
    }
    catch (Exception e) {
      ex = new Exception(e.getMessage());
    }
    
    notifyQueryExecuteListeners(rs, ex);
    
    setButtons();
  }
  


  public void clear()
  {
    m_TextQuery.setText("");
    m_SpinnerMaxRows.setValue(new Integer(100));
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
  





  public void showHistory()
  {
    JList list = new JList(m_History);
    ListSelectorDialog dialog = new ListSelectorDialog(m_Parent, list);
    
    if ((dialog.showDialog() == 0) && 
      (list.getSelectedValue() != null)) {
      setQuery(list.getSelectedValue().toString());
    }
    
    setButtons();
  }
  




  public void setQuery(String query)
  {
    m_TextQuery.setText(query);
  }
  




  public String getQuery()
  {
    return m_TextQuery.getText();
  }
  




  public void setMaxRows(int rows)
  {
    if (rows >= 0) {
      m_SpinnerMaxRows.setValue(new Integer(rows));
    }
  }
  




  public int getMaxRows()
  {
    return ((Integer)m_SpinnerMaxRows.getValue()).intValue();
  }
  




  public void addQueryExecuteListener(QueryExecuteListener l)
  {
    m_QueryExecuteListeners.add(l);
  }
  




  public void removeQueryExecuteListener(QueryExecuteListener l)
  {
    m_QueryExecuteListeners.remove(l);
  }
  








  protected void notifyQueryExecuteListeners(ResultSet rs, Exception ex)
  {
    Iterator iter = m_QueryExecuteListeners.iterator();
    while (iter.hasNext()) {
      QueryExecuteListener l = (QueryExecuteListener)iter.next();
      l.queryExecuted(new QueryExecuteEvent(this, m_DbUtils, getQuery(), getMaxRows(), rs, ex));
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
      l.historyChanged(new HistoryChangedEvent(this, "query", getHistory()));
    }
  }
  





  public void caretUpdate(CaretEvent event)
  {
    setButtons();
  }
}

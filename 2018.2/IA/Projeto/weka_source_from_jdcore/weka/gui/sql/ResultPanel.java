package weka.gui.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import weka.gui.JTableHelper;
import weka.gui.sql.event.QueryExecuteEvent;
import weka.gui.sql.event.QueryExecuteListener;
import weka.gui.sql.event.ResultChangedEvent;
import weka.gui.sql.event.ResultChangedListener;

































public class ResultPanel
  extends JPanel
  implements QueryExecuteListener, ChangeListener
{
  private static final long serialVersionUID = 278654800344034571L;
  protected JFrame m_Parent;
  protected HashSet m_Listeners;
  protected QueryPanel m_QueryPanel;
  protected JTabbedPane m_TabbedPane;
  protected JButton m_ButtonClose;
  protected JButton m_ButtonCloseAll;
  protected JButton m_ButtonCopyQuery;
  protected JButton m_ButtonOptWidth;
  protected int m_NameCounter;
  
  public ResultPanel(JFrame parent)
  {
    Messages.getInstance();m_ButtonClose = new JButton(Messages.getString("ResultPanel_ButtonClose_JButton_Text"));
    

    Messages.getInstance();m_ButtonCloseAll = new JButton(Messages.getString("ResultPanel_ButtonCloseAll_JButton_Text"));
    

    Messages.getInstance();m_ButtonCopyQuery = new JButton(Messages.getString("ResultPanel_ButtonCopyQuery_JButton_Text"));
    

    Messages.getInstance();m_ButtonOptWidth = new JButton(Messages.getString("ResultPanel_ButtonOptWidth_JButton_Text"));
    










    m_Parent = parent;
    m_QueryPanel = null;
    m_NameCounter = 0;
    m_Listeners = new HashSet();
    
    createPanel();
  }
  







  protected void createPanel()
  {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(0, 200));
    

    m_TabbedPane = new JTabbedPane(3);
    m_TabbedPane.addChangeListener(this);
    add(m_TabbedPane, "Center");
    

    JPanel panel = new JPanel(new BorderLayout());
    add(panel, "East");
    JPanel panel2 = new JPanel(new BorderLayout());
    panel.add(panel2, "Center");
    JPanel panel3 = new JPanel(new BorderLayout());
    panel2.add(panel3, "Center");
    JPanel panel4 = new JPanel(new BorderLayout());
    panel3.add(panel4, "Center");
    
    m_ButtonClose.setMnemonic('l');
    m_ButtonClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    panel.add(m_ButtonClose, "North");
    
    m_ButtonCloseAll.setMnemonic('a');
    m_ButtonCloseAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeAll();
      }
    });
    panel2.add(m_ButtonCloseAll, "North");
    
    m_ButtonCopyQuery.setMnemonic('Q');
    Messages.getInstance();m_ButtonCopyQuery.setToolTipText(Messages.getString("ResultPanel_CreatePanel_ButtonCopyQuery_SetToolTipText_Text"));
    m_ButtonCopyQuery.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyQuery();
      }
    });
    panel3.add(m_ButtonCopyQuery, "North");
    
    m_ButtonOptWidth.setMnemonic('p');
    Messages.getInstance();m_ButtonOptWidth.setToolTipText(Messages.getString("ResultPanel_CreatePanel_ButtonOptWidth_SetToolTipText_Text"));
    m_ButtonOptWidth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcOptimalWidth();
      }
    });
    panel4.add(m_ButtonOptWidth, "North");
    


    panel4.add(new JLabel(" "), "Center");
    panel4.add(new JLabel(" "), "South");
    

    setButtons();
  }
  


  public void clear()
  {
    closeAll();
  }
  


  public void setFocus()
  {
    m_TabbedPane.requestFocus();
  }
  




  protected void setButtons()
  {
    int index = m_TabbedPane.getSelectedIndex();
    
    m_ButtonClose.setEnabled(index > -1);
    m_ButtonCloseAll.setEnabled(m_TabbedPane.getTabCount() > 0);
    m_ButtonCopyQuery.setEnabled(index > -1);
    m_ButtonOptWidth.setEnabled(index > -1);
  }
  


  protected String getNextTabName()
  {
    m_NameCounter += 1;
    Messages.getInstance();return Messages.getString("ResultPanel_GetNextTabName_Text") + m_NameCounter;
  }
  





  public void queryExecuted(QueryExecuteEvent evt)
  {
    if (evt.failed()) {
      return;
    }
    
    if (!evt.hasResult()) {
      return;
    }
    try {
      ResultSetTable table = new ResultSetTable(evt.getDbUtils().getDatabaseURL(), evt.getDbUtils().getUsername(), evt.getDbUtils().getPassword(), evt.getQuery(), new ResultSetTableModel(evt.getResultSet(), evt.getMaxRows()));
      




      m_TabbedPane.addTab(getNextTabName(), new JScrollPane(table));
      

      m_TabbedPane.setSelectedIndex(m_TabbedPane.getTabCount() - 1);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    

    setButtons();
  }
  



  public void stateChanged(ChangeEvent e)
  {
    setButtons();
    

    if (getCurrentTable() != null) {
      notifyListeners(getCurrentTable().getURL(), getCurrentTable().getUser(), getCurrentTable().getPassword(), getCurrentTable().getQuery());
    }
  }
  





  public QueryPanel getQueryPanel()
  {
    return m_QueryPanel;
  }
  



  public void setQueryPanel(QueryPanel panel)
  {
    m_QueryPanel = panel;
  }
  








  protected ResultSetTable getCurrentTable()
  {
    ResultSetTable table = null;
    
    int index = m_TabbedPane.getSelectedIndex();
    if (index > -1) {
      JScrollPane pane = (JScrollPane)m_TabbedPane.getComponentAt(index);
      JViewport port = (JViewport)pane.getComponent(0);
      table = (ResultSetTable)port.getComponent(0);
    }
    
    return table;
  }
  




  protected void close()
  {
    int index = m_TabbedPane.getSelectedIndex();
    
    if (index > -1) {
      try {
        getCurrentTable().finalize();
      }
      catch (Throwable t) {
        System.out.println(t);
      }
      m_TabbedPane.removeTabAt(index);
    }
    

    setButtons();
  }
  


  protected void closeAll()
  {
    while (m_TabbedPane.getTabCount() > 0) {
      m_TabbedPane.setSelectedIndex(0);
      try {
        getCurrentTable().finalize();
      }
      catch (Throwable t) {
        System.out.println(t);
      }
      m_TabbedPane.removeTabAt(0);
    }
    

    setButtons();
  }
  


  protected void copyQuery()
  {
    if ((getCurrentTable() != null) && (getQueryPanel() != null)) {
      getQueryPanel().setQuery(getCurrentTable().getQuery());
    }
  }
  

  protected void calcOptimalWidth()
  {
    if (getCurrentTable() != null) {
      JTableHelper.setOptimalColumnWidth(getCurrentTable());
    }
  }
  


  public void addResultChangedListener(ResultChangedListener l)
  {
    m_Listeners.add(l);
  }
  



  public void removeResultChangedListener(ResultChangedListener l)
  {
    m_Listeners.remove(l);
  }
  










  protected void notifyListeners(String url, String user, String pw, String query)
  {
    Iterator iter = m_Listeners.iterator();
    while (iter.hasNext()) {
      ResultChangedListener l = (ResultChangedListener)iter.next();
      l.resultChanged(new ResultChangedEvent(this, url, user, pw, query));
    }
  }
}

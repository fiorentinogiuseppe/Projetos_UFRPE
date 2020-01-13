package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import weka.gui.Logger;

























public class LogPanel
  extends JPanel
  implements Logger
{
  private final HashMap<String, Integer> m_tableIndexes = new HashMap();
  



  private final HashMap<String, Timer> m_timers = new HashMap();
  



  private final DefaultTableModel m_tableModel;
  



  private JTable m_table;
  



  private final JTabbedPane m_tabs = new JTabbedPane();
  



  private final DecimalFormat m_formatter = new DecimalFormat("00");
  



  private final weka.gui.LogPanel m_logPanel = new weka.gui.LogPanel(null, false, true, false);
  

  public LogPanel()
  {
    String[] tmp69_66 = new String[4];Messages.getInstance();tmp69_66[0] = Messages.getString("LogPanel_ColumnNames_Text_Index0"); String[] tmp81_69 = tmp69_66;Messages.getInstance();tmp81_69[1] = Messages.getString("LogPanel_ColumnNames_Text_Index1"); String[] tmp93_81 = tmp81_69;Messages.getInstance();tmp93_81[2] = Messages.getString("LogPanel_ColumnNames_Text_Index2"); String[] tmp105_93 = tmp93_81;Messages.getInstance();tmp105_93[3] = Messages.getString("LogPanel_ColumnNames_Text_Index3");String[] columnNames = tmp105_93;
    



    m_tableModel = new DefaultTableModel(columnNames, 0);
    

    m_table = new JTable()
    {
      public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
      

      public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
      {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!c.getBackground().equals(getSelectionBackground())) {
          String type = (String)getModel().getValueAt(row, 3);
          Color backgroundIndicator = null;
          if (type.startsWith("ERROR")) {
            backgroundIndicator = Color.RED;
          } else if (type.startsWith("WARNING")) {
            backgroundIndicator = Color.YELLOW;
          } else if (type.startsWith("INTERRUPTED")) {
            backgroundIndicator = Color.MAGENTA;
          }
          c.setBackground(backgroundIndicator);
        }
        return c;
      }
      

    };m_table.setModel(m_tableModel);
    m_table.getColumnModel().getColumn(0).setPreferredWidth(100);
    m_table.getColumnModel().getColumn(1).setPreferredWidth(150);
    m_table.getColumnModel().getColumn(2).setPreferredWidth(30);
    m_table.getColumnModel().getColumn(3).setPreferredWidth(500);
    m_table.setShowVerticalLines(true);
    
    JPanel statusPan = new JPanel();
    statusPan.setLayout(new BorderLayout());
    statusPan.add(new JScrollPane(m_table), "Center");
    Messages.getInstance();m_tabs.addTab(Messages.getString("LogPanel_Tabs_AddTab_Text_First"), statusPan);
    

    Messages.getInstance();m_tabs.addTab(Messages.getString("LogPanel_Tabs_AddTab_Text_Second"), m_logPanel);
    


    setLayout(new BorderLayout());
    add(m_tabs, "Center");
  }
  




  public void clearStatus()
  {
    Iterator<Timer> i = m_timers.values().iterator();
    while (i.hasNext()) {
      ((Timer)i.next()).stop();
    }
    

    m_timers.clear();
    m_tableIndexes.clear();
    

    while (m_tableModel.getRowCount() > 0) {
      m_tableModel.removeRow(0);
    }
  }
  





  public JTable getStatusTable()
  {
    return m_table;
  }
  






  public synchronized void logMessage(String message)
  {
    m_logPanel.logMessage(message);
  }
  











  public synchronized void statusMessage(String message)
  {
    boolean hasDelimiters = message.indexOf('|') > 0;
    String stepName = "";
    String stepHash = "";
    String stepParameters = "";
    String stepStatus = "";
    
    if (!hasDelimiters) {
      Messages.getInstance();stepName = Messages.getString("LogPanel_StatusMessage_StepName_Text");
      
      Messages.getInstance();stepHash = Messages.getString("LogPanel_StatusMessage_StepHash_Text");
      
      stepStatus = message;
    }
    else {
      stepHash = message.substring(0, message.indexOf('|'));
      message = message.substring(message.indexOf('|') + 1, message.length());
      
      if (stepHash.indexOf('$') > 0)
      {
        stepName = stepHash.substring(0, stepHash.indexOf('$'));
      } else {
        stepName = stepHash;
      }
      

      if (message.indexOf('|') >= 0) {
        stepParameters = message.substring(0, message.indexOf('|'));
        stepStatus = message.substring(message.indexOf('|') + 1, message.length());
      }
      else
      {
        stepStatus = message;
      }
    }
    

    if (m_tableIndexes.containsKey(stepHash))
    {
      final Integer rowNum = (Integer)m_tableIndexes.get(stepHash);
      if ((stepStatus.trim().equalsIgnoreCase("remove")) || (stepStatus.trim().equalsIgnoreCase("remove.")))
      {


        m_tableIndexes.remove(stepHash);
        ((Timer)m_timers.get(stepHash)).stop();
        m_timers.remove(stepHash);
        


        Iterator<String> i = m_tableIndexes.keySet().iterator();
        while (i.hasNext()) {
          String nextKey = (String)i.next();
          int index = ((Integer)m_tableIndexes.get(nextKey)).intValue();
          if (index > rowNum.intValue()) {
            index--;
            

            m_tableIndexes.put(nextKey, Integer.valueOf(index));
          }
        }
        



        if (!SwingUtilities.isEventDispatchThread()) {
          try {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                m_tableModel.removeRow(rowNum.intValue());
              }
            });
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        } else {
          m_tableModel.removeRow(rowNum.intValue());
        }
      } else {
        final String stepNameCopy = stepName;
        final String stepStatusCopy = stepStatus;
        final String stepParametersCopy = stepParameters;
        
        if (!SwingUtilities.isEventDispatchThread()) {
          try {
            SwingUtilities.invokeLater(new Runnable()
            {
              public void run() {
                if ((!stepStatusCopy.startsWith("INTERRUPTED")) || (!((String)m_tableModel.getValueAt(rowNum.intValue(), 3)).startsWith("ERROR")))
                {
                  m_tableModel.setValueAt(stepNameCopy, rowNum.intValue(), 0);
                  m_tableModel.setValueAt(stepParametersCopy, rowNum.intValue(), 1);
                  
                  m_tableModel.setValueAt(m_table.getValueAt(rowNum.intValue(), 2), rowNum.intValue(), 2);
                  

                  m_tableModel.setValueAt(stepStatusCopy, rowNum.intValue(), 3);
                }
              }
            });
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        else if ((!stepStatusCopy.startsWith("INTERRUPTED")) || (!((String)m_tableModel.getValueAt(rowNum.intValue(), 3)).startsWith("ERROR")))
        {
          m_tableModel.setValueAt(stepNameCopy, rowNum.intValue(), 0);
          m_tableModel.setValueAt(stepParametersCopy, rowNum.intValue(), 1);
          m_tableModel.setValueAt(m_table.getValueAt(rowNum.intValue(), 2), rowNum.intValue(), 2);
          
          m_tableModel.setValueAt(stepStatusCopy, rowNum.intValue(), 3);
        }
        
        if ((stepStatus.startsWith("ERROR")) || (stepStatus.startsWith("INTERRUPTED")) || (stepStatus.trim().equalsIgnoreCase("finished")) || (stepStatus.trim().equalsIgnoreCase("finished.")) || (stepStatus.trim().equalsIgnoreCase("done")) || (stepStatus.trim().equalsIgnoreCase("done.")))
        {





          ((Timer)m_timers.get(stepHash)).stop();
        } else if (!((Timer)m_timers.get(stepHash)).isRunning())
        {

          installTimer(stepHash);
        }
      }
    }
    else if ((!stepStatus.trim().equalsIgnoreCase("Remove")) && (!stepStatus.trim().equalsIgnoreCase("Remove.")))
    {

      int numKeys = m_tableIndexes.keySet().size();
      m_tableIndexes.put(stepHash, Integer.valueOf(numKeys));
      

      final Object[] newRow = new Object[4];
      newRow[0] = stepName;
      newRow[1] = stepParameters;
      newRow[2] = "-";
      newRow[3] = stepStatus;
      String stepHashCopy = stepHash;
      try {
        if (!SwingUtilities.isEventDispatchThread()) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              m_tableModel.addRow(newRow);
            }
            
          });
        } else {
          m_tableModel.addRow(newRow);
        }
        
        installTimer(stepHashCopy);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  private void installTimer(final String stepHash) {
    final long startTime = System.currentTimeMillis();
    Timer newTimer = new Timer(1000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        synchronized (LogPanel.this) {
          if (m_tableIndexes.containsKey(stepHash)) {
            Integer rn = (Integer)m_tableIndexes.get(stepHash);
            long elapsed = System.currentTimeMillis() - startTime;
            long seconds = elapsed / 1000L;
            long minutes = seconds / 60L;
            final long hours = minutes / 60L;
            seconds -= minutes * 60L;
            minutes -= hours * 60L;
            final long seconds2 = seconds;
            long minutes2 = minutes;
            if (!SwingUtilities.isEventDispatchThread()) {
              try {
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    m_tableModel.setValueAt("" + m_formatter.format(hours) + ":" + m_formatter.format(seconds2) + ":" + m_formatter.format(val$seconds2), val$rn.intValue(), 2);
                  }
                });
              }
              catch (Exception ex)
              {
                ex.printStackTrace();
              }
            } else {
              m_tableModel.setValueAt("" + m_formatter.format(hours) + ":" + m_formatter.format(minutes2) + ":" + m_formatter.format(seconds2), rn.intValue(), 2);
            }
            
          }
          
        }
        
      }
    });
    m_timers.put(stepHash, newTimer);
    newTimer.start();
  }
  



  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("LogPanel_Main_Jf_JFrame_Text"));
      

      jf.getContentPane().setLayout(new BorderLayout());
      LogPanel lp = new LogPanel();
      jf.getContentPane().add(lp, "Center");
      
      jf.getContentPane().add(lp, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_First"));
      
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_Second"));
      
      Thread.sleep(3000L);
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_Third"));
      
      Thread.sleep(3000L);
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_Fourth"));
      

      Thread.sleep(3000L);
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_Sixth"));
      
      Thread.sleep(3000L);
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_Seventh"));
      
      Thread.sleep(3000L);
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_Eighth"));
      
      Thread.sleep(3000L);
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_StatusMessage_Nineth"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}

package weka.gui.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import weka.gui.ComponentHelper;















































public class InfoPanel
  extends JPanel
{
  private static final long serialVersionUID = -7701133696481997973L;
  protected JFrame m_Parent;
  protected JList m_Info;
  protected DefaultListModel m_Model;
  protected JButton m_ButtonClear;
  protected JButton m_ButtonCopy;
  
  public InfoPanel(JFrame parent)
  {
    m_Parent = parent;
    createPanel();
  }
  





  protected void createPanel()
  {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(0, 80));
    

    m_Model = new DefaultListModel();
    m_Info = new JList(m_Model);
    m_Info.setCellRenderer(new InfoPanelCellRenderer());
    m_Info.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        setButtons(e);
      }
    });
    add(new JScrollPane(m_Info), "Center");
    

    JPanel panel = new JPanel(new BorderLayout());
    add(panel, "East");
    Messages.getInstance();m_ButtonClear = new JButton(Messages.getString("InfoPanel_CreatePanel_ButtonClear_JButton_Text"));
    m_ButtonClear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clear();
      }
    });
    panel.add(m_ButtonClear, "North");
    

    JPanel panel2 = new JPanel(new BorderLayout());
    panel.add(panel2, "Center");
    Messages.getInstance();m_ButtonCopy = new JButton(Messages.getString("InfoPanel_CreatePanel_ButtonCopy_JButton_Text"));
    m_ButtonCopy.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyToClipboard();
      }
    });
    panel2.add(m_ButtonCopy, "North");
  }
  



  protected void setButtons(ListSelectionEvent e)
  {
    if ((e == null) || (e.getSource() == m_Info)) {
      m_ButtonClear.setEnabled(m_Model.getSize() > 0);
      m_ButtonCopy.setEnabled(m_Info.getSelectedIndices().length == 1);
    }
  }
  


  public void setFocus()
  {
    m_Info.requestFocus();
  }
  


  public void clear()
  {
    m_Model.clear();
    setButtons(null);
  }
  







  public boolean copyToClipboard()
  {
    if (m_Info.getSelectedIndices().length != 1) {
      return false;
    }
    StringSelection selection = new StringSelection(((JLabel)m_Info.getSelectedValue()).getText());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, selection);
    return true;
  }
  





  public void append(String msg, String icon)
  {
    append(new JLabel(msg, ComponentHelper.getImageIcon(icon), 2));
  }
  



  public void append(Object msg)
  {
    if ((msg instanceof String)) {
      append(msg.toString(), "empty_small.gif");
      return;
    }
    
    m_Model.addElement(msg);
    m_Info.setSelectedIndex(m_Model.getSize() - 1);
    m_Info.ensureIndexIsVisible(m_Info.getSelectedIndex());
    
    setButtons(null);
  }
}

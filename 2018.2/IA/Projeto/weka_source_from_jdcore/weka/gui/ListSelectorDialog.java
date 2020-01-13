package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;



















































public class ListSelectorDialog
  extends JDialog
{
  private static final long serialVersionUID = 906147926840288895L;
  protected JButton m_SelectBut;
  protected JButton m_CancelBut;
  protected JButton m_PatternBut;
  protected JList m_List;
  protected int m_Result;
  public static final int APPROVE_OPTION = 0;
  public static final int CANCEL_OPTION = 1;
  protected String m_PatternRegEx;
  
  public ListSelectorDialog(Frame parentFrame, JList userList)
  {
    super(parentFrame, Messages.getString("ListSelectorDialog_Text"), Dialog.ModalityType.DOCUMENT_MODAL);Messages.getInstance();m_SelectBut = new JButton(Messages.getString("ListSelectorDialog_SelectBut_JButton_Text"));Messages.getInstance();m_CancelBut = new JButton(Messages.getString("ListSelectorDialog_CancelBut_JButton_Text"));Messages.getInstance();m_PatternBut = new JButton(Messages.getString("ListSelectorDialog_PatternBut_JButton_Text"));m_PatternRegEx = ".*";
    m_List = userList;
    m_CancelBut.setMnemonic('C');
    m_CancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Result = 1;
        setVisible(false);
      }
    });
    m_SelectBut.setMnemonic('S');
    m_SelectBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Result = 0;
        setVisible(false);
      }
    });
    m_PatternBut.setMnemonic('P');
    m_PatternBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPattern();
      }
      
    });
    Container c = getContentPane();
    c.setLayout(new BorderLayout());
    
    Box b1 = new Box(0);
    b1.add(m_SelectBut);
    b1.add(Box.createHorizontalStrut(10));
    b1.add(m_PatternBut);
    b1.add(Box.createHorizontalStrut(10));
    b1.add(m_CancelBut);
    c.add(b1, "South");
    c.add(new JScrollPane(m_List), "Center");
    
    getRootPane().setDefaultButton(m_SelectBut);
    
    pack();
    

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int width = getWidth() > screen.getWidth() ? (int)screen.getWidth() : getWidth();
    
    int height = getHeight() > screen.getHeight() ? (int)screen.getHeight() : getHeight();
    
    setSize(width, height);
  }
  





  public int showDialog()
  {
    m_Result = 1;
    int[] origSelected = m_List.getSelectedIndices();
    setVisible(true);
    if (m_Result == 1) {
      m_List.setSelectedIndices(origSelected);
    }
    return m_Result;
  }
  



  protected void selectPattern()
  {
    Messages.getInstance();String pattern = JOptionPane.showInputDialog(m_PatternBut.getParent(), Messages.getString("ListSelectorDialog_SelectPattern_Pattern_JOptionPaneShowInputDialog_Text"), m_PatternRegEx);
    


    if (pattern != null) {
      try {
        Pattern.compile(pattern);
        m_PatternRegEx = pattern;
        m_List.clearSelection();
        for (int i = 0; i < m_List.getModel().getSize(); i++) {
          if (Pattern.matches(pattern, m_List.getModel().getElementAt(i).toString()))
          {
            m_List.addSelectionInterval(i, i);
          }
        }
      } catch (Exception ex) {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_PatternBut.getParent(), Messages.getString("ListSelectorDialog_SelectPattern_Exception_JOptionPaneShowInputDialog_Text_First") + pattern + Messages.getString("ListSelectorDialog_SelectPattern_Exception_JOptionPaneShowInputDialog_Text_Second") + ex, Messages.getString("ListSelectorDialog_SelectPattern_Exception_JOptionPaneShowInputDialog_Text_Third"), 0);
      }
    }
  }
  








  public static void main(String[] args)
  {
    try
    {
      DefaultListModel lm = new DefaultListModel();
      Messages.getInstance();lm.addElement(Messages.getString("ListSelectorDialog_Main_DefaultListModel_AddElement_Text_First"));
      Messages.getInstance();lm.addElement(Messages.getString("ListSelectorDialog_Main_DefaultListModel_AddElement_Text_Second"));
      Messages.getInstance();lm.addElement(Messages.getString("ListSelectorDialog_Main_DefaultListModel_AddElement_Text_Third"));
      Messages.getInstance();lm.addElement(Messages.getString("ListSelectorDialog_Main_DefaultListModel_AddElement_Text_Fourth"));
      Messages.getInstance();lm.addElement(Messages.getString("ListSelectorDialog_Main_DefaultListModel_AddElement_Text_Fifth"));
      JList jl = new JList(lm);
      ListSelectorDialog jd = new ListSelectorDialog(null, jl);
      int result = jd.showDialog();
      if (result == 0) {
        Messages.getInstance();System.err.println(Messages.getString("ListSelectorDialog_Main_DefaultListModel_Error_Text_First"));
        int[] selected = jl.getSelectedIndices();
        for (int i = 0; i < selected.length; i++) {
          System.err.println("" + selected[i] + " " + lm.elementAt(selected[i]));
        }
      }
      else {
        Messages.getInstance();System.err.println(Messages.getString("ListSelectorDialog_Main_DefaultListModel_Error_Text_Second"));
      }
      System.exit(0);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

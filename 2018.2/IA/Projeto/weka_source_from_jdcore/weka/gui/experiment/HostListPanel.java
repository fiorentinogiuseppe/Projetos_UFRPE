package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import weka.experiment.RemoteExperiment;











































public class HostListPanel
  extends JPanel
  implements ActionListener
{
  private static final long serialVersionUID = 7182791134585882197L;
  protected RemoteExperiment m_Exp;
  protected JList m_List;
  protected JButton m_DeleteBut;
  protected JTextField m_HostField;
  
  public HostListPanel(RemoteExperiment exp)
  {
    this();
    setExperiment(exp);
  }
  
  public HostListPanel()
  {
    Messages.getInstance();m_DeleteBut = new JButton(Messages.getString("HostListPanel_DeleteBut_JButton_Text"));
    

    m_HostField = new JTextField(25);
    














    m_List = new JList();
    m_List.setModel(new DefaultListModel());
    m_DeleteBut.setEnabled(false);
    m_DeleteBut.addActionListener(this);
    m_HostField.addActionListener(this);
    setLayout(new BorderLayout());
    Messages.getInstance();setBorder(BorderFactory.createTitledBorder(Messages.getString("HostListPanel_BorderFactoryCreateTitledBorder_Text")));
    
    JPanel topLab = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    topLab.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    
    topLab.setLayout(gb);
    
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    topLab.add(m_DeleteBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    topLab.add(m_HostField, constraints);
    
    add(topLab, "North");
    add(new JScrollPane(m_List), "Center");
  }
  




  public void setExperiment(RemoteExperiment exp)
  {
    m_Exp = exp;
    m_List.setModel(m_Exp.getRemoteHosts());
    if (((DefaultListModel)m_List.getModel()).size() > 0) {
      m_DeleteBut.setEnabled(true);
    }
  }
  





  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == m_HostField) {
      ((DefaultListModel)m_List.getModel()).addElement(m_HostField.getText());
      
      m_DeleteBut.setEnabled(true);
    } else if (e.getSource() == m_DeleteBut) {
      int[] selected = m_List.getSelectedIndices();
      if (selected != null) {
        for (int i = selected.length - 1; i >= 0; i--) {
          int current = selected[i];
          ((DefaultListModel)m_List.getModel()).removeElementAt(current);
          if (((DefaultListModel)m_List.getModel()).size() > current) {
            m_List.setSelectedIndex(current);
          } else {
            m_List.setSelectedIndex(current - 1);
          }
        }
      }
      if (((DefaultListModel)m_List.getModel()).size() == 0) {
        m_DeleteBut.setEnabled(false);
      }
    }
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("HostListPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      HostListPanel dp = new HostListPanel();
      jf.getContentPane().add(dp, "Center");
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);

    }
    catch (Exception ex)
    {

      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

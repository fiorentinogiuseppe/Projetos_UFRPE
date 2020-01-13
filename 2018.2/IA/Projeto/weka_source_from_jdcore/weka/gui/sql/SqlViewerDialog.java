package weka.gui.sql;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import weka.gui.sql.event.ResultChangedEvent;
import weka.gui.sql.event.ResultChangedListener;






















































public class SqlViewerDialog
  extends JDialog
  implements ResultChangedListener
{
  private static final long serialVersionUID = -31619864037233099L;
  protected JFrame m_Parent;
  protected SqlViewer m_Viewer;
  protected JPanel m_PanelButtons;
  protected JButton m_ButtonOK;
  protected JButton m_ButtonCancel;
  protected JLabel m_LabelQuery;
  protected int m_ReturnValue;
  protected String m_URL;
  protected String m_User;
  protected String m_Password;
  protected String m_Query;
  
  public SqlViewerDialog(JFrame parent)
  {
    super(parent, Messages.getString("SqlViewerDialog_SQL_Viewer_Text"), Dialog.ModalityType.DOCUMENT_MODAL);Messages.getInstance();m_ButtonOK = new JButton(Messages.getString("SqlViewerDialog_ButtonOK_JButton_Text"));Messages.getInstance();m_ButtonCancel = new JButton(Messages.getString("SqlViewerDialog_ButtonCancel_JButton_Text"));m_LabelQuery = new JLabel("");m_ReturnValue = 2;
    
    m_Parent = parent;
    m_URL = "";
    m_User = "";
    m_Password = "";
    m_Query = "";
    
    createDialog();
  }
  






  protected void createDialog()
  {
    final SqlViewerDialog dialog = this;
    setLayout(new BorderLayout());
    

    m_Viewer = new SqlViewer(m_Parent);
    add(m_Viewer, "Center");
    
    JPanel panel2 = new JPanel(new BorderLayout());
    add(panel2, "South");
    

    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    panel2.add(panel, "East");
    m_ButtonOK.setMnemonic('O');
    panel.add(m_ButtonOK);
    m_ButtonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        m_ReturnValue = 0;
        

        m_Viewer.removeResultChangedListener(dialog);
        m_Viewer.saveSize();
        dialog.dispose();
      }
    });
    m_ButtonCancel.setMnemonic('C');
    panel.add(m_ButtonCancel);
    m_ButtonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        m_ReturnValue = 2;
        

        m_Viewer.removeResultChangedListener(dialog);
        m_Viewer.saveSize();
        dialog.dispose();
      }
      
    });
    addWindowListener(new WindowAdapter()
    {

      public void windowClosing(WindowEvent e)
      {
        m_Viewer.saveSize();
      }
      

    });
    panel = new JPanel(new FlowLayout());
    panel2.add(panel, "Center");
    panel.add(m_LabelQuery);
    
    pack();
    getRootPane().setDefaultButton(m_ButtonOK);
    setResizable(true);
    

    m_Viewer.addResultChangedListener(this);
  }
  


  public void setVisible(boolean b)
  {
    if (b) {
      m_ReturnValue = 2;
    }
    super.setVisible(b);
    

    if (b) {
      m_Viewer.clear();
    }
  }
  




  public int getReturnValue()
  {
    return m_ReturnValue;
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
  


  public void resultChanged(ResultChangedEvent evt)
  {
    m_URL = evt.getURL();
    m_User = evt.getUser();
    m_Password = evt.getPassword();
    m_Query = evt.getQuery();
    Messages.getInstance();m_LabelQuery.setText(Messages.getString("SqlViewerDialog_ResultChanged_Text") + m_Query);
  }
  




  public static void main(String[] args)
  {
    SqlViewerDialog dialog = new SqlViewerDialog(null);
    dialog.setDefaultCloseOperation(2);
    dialog.setVisible(true);
    Messages.getInstance();System.out.println(Messages.getString("SqlViewerDialog_Main_Text_First") + dialog.getReturnValue());
    if (dialog.getReturnValue() == 0) {
      Messages.getInstance();System.out.println(Messages.getString("SqlViewerDialog_Main_Text_Second") + dialog.getURL());
      Messages.getInstance();System.out.println(Messages.getString("SqlViewerDialog_Main_Text_Third") + dialog.getUser());
      Messages.getInstance();System.out.println(Messages.getString("SqlViewerDialog_Main_Text_Fourth") + dialog.getPassword().replaceAll(".", "*"));
      Messages.getInstance();System.out.println(Messages.getString("SqlViewerDialog_Main_Text_Fifth") + dialog.getQuery());
    }
  }
}

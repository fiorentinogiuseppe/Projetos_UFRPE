package weka.gui;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;











































public class DatabaseConnectionDialog
  extends JDialog
{
  private static final long serialVersionUID = -1081946748666245054L;
  protected JTextField m_DbaseURLText;
  protected JLabel m_DbaseURLLab;
  protected JTextField m_UserNameText;
  protected JLabel m_UserNameLab;
  protected JPasswordField m_PasswordText;
  protected JLabel m_PasswordLab;
  protected JCheckBox m_DebugCheckBox;
  protected JLabel m_DebugLab;
  protected int m_returnValue;
  
  public DatabaseConnectionDialog(Frame parentFrame)
  {
    this(parentFrame, "", "");
  }
  






  public DatabaseConnectionDialog(Frame parentFrame, String url, String uname)
  {
    this(parentFrame, url, uname, true);
  }
  







  public DatabaseConnectionDialog(Frame parentFrame, String url, String uname, boolean debug)
  {
    super(parentFrame, Messages.getString("DatabaseConnectionDialog_Text"), true);
    DbConnectionDialog(url, uname, debug);
  }
  




  public String getURL()
  {
    return m_DbaseURLText.getText();
  }
  




  public String getUsername()
  {
    return m_UserNameText.getText();
  }
  




  public String getPassword()
  {
    return new String(m_PasswordText.getPassword());
  }
  




  public boolean getDebug()
  {
    return m_DebugCheckBox.isSelected();
  }
  




  public int getReturnValue()
  {
    return m_returnValue;
  }
  





  public void DbConnectionDialog(String url, String uname)
  {
    DbConnectionDialog(url, uname, true);
  }
  







  public void DbConnectionDialog(String url, String uname, boolean debug)
  {
    JPanel DbP = new JPanel();
    if (debug) {
      DbP.setLayout(new GridLayout(5, 1));
    } else {
      DbP.setLayout(new GridLayout(4, 1));
    }
    m_DbaseURLText = new JTextField(url, 50);
    Messages.getInstance();m_DbaseURLLab = new JLabel(Messages.getString("DbConnectionDialog_DbaseURLLab_JLabel_Text"), 2);
    m_DbaseURLLab.setFont(new Font("Monospaced", 0, 12));
    m_DbaseURLLab.setDisplayedMnemonic('D');
    m_DbaseURLLab.setLabelFor(m_DbaseURLText);
    
    m_UserNameText = new JTextField(uname, 25);
    Messages.getInstance();m_UserNameLab = new JLabel(Messages.getString("DbConnectionDialog_UserNameLab_JLabel_Text"), 2);
    m_UserNameLab.setFont(new Font("Monospaced", 0, 12));
    m_UserNameLab.setDisplayedMnemonic('U');
    m_UserNameLab.setLabelFor(m_UserNameText);
    
    m_PasswordText = new JPasswordField(25);
    Messages.getInstance();m_PasswordLab = new JLabel(Messages.getString("DbConnectionDialog_PasswordLab_JLabel_Text"), 2);
    m_PasswordLab.setFont(new Font("Monospaced", 0, 12));
    m_PasswordLab.setDisplayedMnemonic('P');
    m_PasswordLab.setLabelFor(m_PasswordText);
    
    m_DebugCheckBox = new JCheckBox();
    Messages.getInstance();m_DebugLab = new JLabel(Messages.getString("DbConnectionDialog_DebugLab_JLabel_Text"), 2);
    m_DebugLab.setFont(new Font("Monospaced", 0, 12));
    m_DebugLab.setDisplayedMnemonic('P');
    m_DebugLab.setLabelFor(m_DebugCheckBox);
    
    JPanel urlP = new JPanel();
    urlP.setLayout(new FlowLayout(0));
    urlP.add(m_DbaseURLLab);
    urlP.add(m_DbaseURLText);
    DbP.add(urlP);
    
    JPanel usernameP = new JPanel();
    usernameP.setLayout(new FlowLayout(0));
    usernameP.add(m_UserNameLab);
    usernameP.add(m_UserNameText);
    DbP.add(usernameP);
    
    JPanel passwordP = new JPanel();
    passwordP.setLayout(new FlowLayout(0));
    passwordP.add(m_PasswordLab);
    passwordP.add(m_PasswordText);
    DbP.add(passwordP);
    
    if (debug) {
      JPanel debugP = new JPanel();
      debugP.setLayout(new FlowLayout(0));
      debugP.add(m_DebugLab);
      debugP.add(m_DebugCheckBox);
      DbP.add(debugP);
    }
    
    JPanel buttonsP = new JPanel();
    buttonsP.setLayout(new FlowLayout());
    
    Messages.getInstance(); JButton ok; buttonsP.add(ok = new JButton(Messages.getString("DbConnectionDialog_ButtonP_JButton_OK_Text")));
    Messages.getInstance(); JButton cancel; buttonsP.add(cancel = new JButton(Messages.getString("DbConnectionDialog_ButtonP_JButton_Cancel_Text")));
    ok.setMnemonic('O');
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        m_returnValue = 0;
        dispose();
      }
    });
    cancel.setMnemonic('C');
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        m_returnValue = -1;
        dispose();
      }
      

    });
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        Messages.getInstance();System.err.println(Messages.getString("DbConnectionDialog_WindowClosing_Text"));
        m_returnValue = -1;
      }
      
    });
    DbP.add(buttonsP);
    getContentPane().add(DbP, "Center");
    pack();
    getRootPane().setDefaultButton(ok);
    setResizable(false);
  }
  


  public static void main(String[] args)
  {
    Messages.getInstance();Messages.getInstance();DatabaseConnectionDialog dbd = new DatabaseConnectionDialog(null, Messages.getString("DbConnectionDialog_Main_URL_Text"), Messages.getString("DbConnectionDialog_Main_Username_Text"));
    dbd.setVisible(true);
    System.out.println(dbd.getReturnValue() + ":" + dbd.getUsername() + ":" + dbd.getPassword() + ":" + dbd.getURL());
  }
}

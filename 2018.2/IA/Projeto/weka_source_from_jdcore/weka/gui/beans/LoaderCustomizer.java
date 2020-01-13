package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.PrintStream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import weka.core.converters.DatabaseConverter;
import weka.core.converters.DatabaseLoader;
import weka.core.converters.FileSourcedConverter;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertySheetPanel;





































public class LoaderCustomizer
  extends JPanel
  implements Customizer, CustomizerCloseRequester
{
  private static final long serialVersionUID = 6990446313118930298L;
  private PropertyChangeSupport m_pcSupport = new PropertyChangeSupport(this);
  

  private Loader m_dsLoader;
  
  private PropertySheetPanel m_LoaderEditor = new PropertySheetPanel();
  

  private JFileChooser m_fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  


  private JFrame m_parentFrame;
  


  private JTextField m_dbaseURLText;
  


  private JTextField m_userNameText;
  

  private JTextField m_queryText;
  

  private JTextField m_keyText;
  

  private JPasswordField m_passwordText;
  

  private JCheckBox m_relativeFilePath;
  


  public LoaderCustomizer()
  {
    try
    {
      m_LoaderEditor.addPropertyChangeListener(new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e) {
          repaint();
          if (m_dsLoader != null) {
            Messages.getInstance();System.err.println(Messages.getString("LoaderCustomizer_Error_Text_First"));
            m_dsLoader.setLoader(m_dsLoader.getLoader());
          }
        }
      });
      repaint();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    setLayout(new BorderLayout());
    

    m_fileChooser.setDialogType(0);
    m_fileChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ApproveSelection")) {
          try {
            File selectedFile = m_fileChooser.getSelectedFile();
            ((FileSourcedConverter)m_dsLoader.getLoader()).setFile(selectedFile);
            



            m_dsLoader.newFileSelected();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        
        if (m_parentFrame != null) {
          m_parentFrame.dispose();
        }
      }
    });
  }
  
  public void setParentFrame(JFrame parent) {
    m_parentFrame = parent;
  }
  
  private void setUpOther() {
    removeAll();
    add(m_LoaderEditor, "Center");
    validate();
    repaint();
  }
  


  private void setUpDatabase()
  {
    removeAll();
    
    JPanel db = new JPanel();
    db.setLayout(new GridLayout(6, 1));
    m_dbaseURLText = new JTextField(((DatabaseConverter)m_dsLoader.getLoader()).getUrl(), 50);
    Messages.getInstance();JLabel dbaseURLLab = new JLabel(Messages.getString("LoaderCustomizer_SetUpDatabase_DbaseURLLab_JLabel_Text"), 2);
    dbaseURLLab.setFont(new Font("Monospaced", 0, 12));
    
    m_userNameText = new JTextField(((DatabaseConverter)m_dsLoader.getLoader()).getUser(), 50);
    Messages.getInstance();JLabel userNameLab = new JLabel(Messages.getString("LoaderCustomizer_SetUpDatabase_UserNameLab_JLabel_Text"), 2);
    userNameLab.setFont(new Font("Monospaced", 0, 12));
    
    m_passwordText = new JPasswordField(50);
    m_passwordText.setText(((DatabaseLoader)m_dsLoader.getLoader()).getPassword());
    Messages.getInstance();JLabel passwordLab = new JLabel(Messages.getString("LoaderCustomizer_SetUpDatabase_PasswordLab_JLabel_Text"), 2);
    passwordLab.setFont(new Font("Monospaced", 0, 12));
    
    m_queryText = new JTextField(((DatabaseLoader)m_dsLoader.getLoader()).getQuery(), 50);
    Messages.getInstance();JLabel queryLab = new JLabel(Messages.getString("LoaderCustomizer_SetUpDatabase_QueryLab_JLabel_Text"), 2);
    queryLab.setFont(new Font("Monospaced", 0, 12));
    
    m_keyText = new JTextField(((DatabaseLoader)m_dsLoader.getLoader()).getKeys(), 50);
    Messages.getInstance();JLabel keyLab = new JLabel(Messages.getString("LoaderCustomizer_SetUpDatabase_KeyLab_JLabel_Text"), 2);
    keyLab.setFont(new Font("Monospaced", 0, 12));
    
    JPanel urlP = new JPanel();
    
    urlP.setLayout(new FlowLayout(0));
    urlP.add(dbaseURLLab);
    urlP.add(m_dbaseURLText);
    db.add(urlP);
    
    JPanel usernameP = new JPanel();
    
    usernameP.setLayout(new FlowLayout(0));
    usernameP.add(userNameLab);
    usernameP.add(m_userNameText);
    db.add(usernameP);
    
    JPanel passwordP = new JPanel();
    
    passwordP.setLayout(new FlowLayout(0));
    passwordP.add(passwordLab);
    passwordP.add(m_passwordText);
    db.add(passwordP);
    
    JPanel queryP = new JPanel();
    
    queryP.setLayout(new FlowLayout(0));
    queryP.add(queryLab);
    queryP.add(m_queryText);
    db.add(queryP);
    
    JPanel keyP = new JPanel();
    
    keyP.setLayout(new FlowLayout(0));
    keyP.add(keyLab);
    keyP.add(m_keyText);
    db.add(keyP);
    
    JPanel buttonsP = new JPanel();
    buttonsP.setLayout(new FlowLayout());
    
    Messages.getInstance(); JButton ok; buttonsP.add(ok = new JButton(Messages.getString("LoaderCustomizer_SetUpDatabase_ButtonsP_Ok_JButton_Text")));
    Messages.getInstance(); JButton cancel; buttonsP.add(cancel = new JButton(Messages.getString("LoaderCustomizer_SetUpDatabase_ButtonsP_Cancel_JButton_Text")));
    ok.addActionListener(new ActionListener()
    {



      public void actionPerformed(ActionEvent evt)
      {


        if (LoaderCustomizer.this.resetAndUpdateDatabaseLoaderIfChanged())
        {
          try
          {
            m_dsLoader.setDB(true);
          }
          catch (Exception ex) {}
        }
        if (m_parentFrame != null) {
          m_parentFrame.dispose();
        }
        
      }
    });
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (m_parentFrame != null) {
          m_parentFrame.dispose();
        }
        
      }
    });
    db.add(buttonsP);
    JPanel about = m_LoaderEditor.getAboutPanel();
    if (about != null) {
      add(about, "North");
    }
    add(db, "South");
  }
  
  private boolean resetAndUpdateDatabaseLoaderIfChanged() {
    DatabaseLoader dbl = (DatabaseLoader)m_dsLoader.getLoader();
    String url = dbl.getUrl();
    String user = dbl.getUser();
    String password = dbl.getPassword();
    String query = dbl.getQuery();
    String keys = dbl.getKeys();
    
    boolean update = (!url.equals(m_dbaseURLText.getText())) || (!user.equals(m_userNameText.getText())) || (!password.equals(m_passwordText.getText())) || (!query.equalsIgnoreCase(m_queryText.getText())) || (!keys.equals(m_keyText.getText()));
    




    if (update) {
      dbl.resetStructure();
      dbl.setUrl(m_dbaseURLText.getText());
      dbl.setUser(m_userNameText.getText());
      dbl.setPassword(new String(m_passwordText.getPassword()));
      dbl.setQuery(m_queryText.getText());
      dbl.setKeys(m_keyText.getText());
    }
    
    return update;
  }
  
  public void setUpFile() {
    removeAll();
    
    File tmp = ((FileSourcedConverter)m_dsLoader.getLoader()).retrieveFile();
    tmp = new File(tmp.getAbsolutePath());
    if (tmp.isDirectory()) {
      m_fileChooser.setCurrentDirectory(tmp);
    } else {
      m_fileChooser.setSelectedFile(tmp);
    }
    FileSourcedConverter loader = (FileSourcedConverter)m_dsLoader.getLoader();
    String[] ext = loader.getFileExtensions();
    ExtensionFileFilter firstFilter = null;
    for (int i = 0; i < ext.length; i++) {
      ExtensionFileFilter ff = new ExtensionFileFilter(ext[i], loader.getFileDescription() + " (*" + ext[i] + ")");
      

      if (i == 0)
        firstFilter = ff;
      m_fileChooser.addChoosableFileFilter(ff);
    }
    if (firstFilter != null)
      m_fileChooser.setFileFilter(firstFilter);
    JPanel about = m_LoaderEditor.getAboutPanel();
    if (about != null) {
      add(about, "North");
    }
    add(m_fileChooser, "Center");
    
    Messages.getInstance();m_relativeFilePath = new JCheckBox(Messages.getString("LoaderCustomizer_SetUpDatabase_RelativeFilePath_JCheckBox_Text"));
    m_relativeFilePath.setSelected(((FileSourcedConverter)m_dsLoader.getLoader()).getUseRelativePath());
    

    m_relativeFilePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ((FileSourcedConverter)m_dsLoader.getLoader()).setUseRelativePath(m_relativeFilePath.isSelected());
      }
      
    });
    JPanel holderPanel = new JPanel();
    holderPanel.setLayout(new FlowLayout());
    holderPanel.add(m_relativeFilePath);
    add(holderPanel, "South");
  }
  




  public void setObject(Object object)
  {
    m_dsLoader = ((Loader)object);
    m_LoaderEditor.setTarget(m_dsLoader.getLoader());
    
    if ((m_dsLoader.getLoader() instanceof FileSourcedConverter)) {
      setUpFile();
    }
    else if ((m_dsLoader.getLoader() instanceof DatabaseConverter)) {
      setUpDatabase();
    }
    else {
      setUpOther();
    }
  }
  




  public void addPropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.addPropertyChangeListener(pcl);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcSupport.removePropertyChangeListener(pcl);
  }
  
  static {}
}

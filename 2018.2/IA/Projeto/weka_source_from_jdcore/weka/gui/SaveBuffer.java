package weka.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

































public class SaveBuffer
{
  private Logger m_Log;
  private Component m_parentComponent;
  private String m_lastvisitedDirectory = null;
  




  public SaveBuffer(Logger log, Component parent)
  {
    m_Log = log;
    m_parentComponent = parent;
  }
  




  public boolean save(StringBuffer buf)
  {
    if (buf != null) { JFileChooser fileChooser;
      JFileChooser fileChooser;
      if (m_lastvisitedDirectory == null) {
        fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
      }
      else {
        fileChooser = new JFileChooser(m_lastvisitedDirectory);
      }
      
      fileChooser.setFileSelectionMode(0);
      int returnVal = fileChooser.showSaveDialog(m_parentComponent);
      if (returnVal == 0) {
        File sFile = fileChooser.getSelectedFile();
        m_lastvisitedDirectory = sFile.getPath();
        
        if (sFile.exists()) {
          Object[] options = new String[4];
          Messages.getInstance();options[0] = Messages.getString("SaveBuffer_Save_Options_0_Text");
          Messages.getInstance();options[1] = Messages.getString("SaveBuffer_Save_Options_1_Text");
          Messages.getInstance();options[2] = Messages.getString("SaveBuffer_Save_Options_2_Text");
          Messages.getInstance();options[3] = Messages.getString("SaveBuffer_Save_Options_3_Text");
          
          Messages.getInstance();JOptionPane jop = new JOptionPane(Messages.getString("SaveBuffer_Save_JOptionPane_Text"), 3, 1, null, options);
          



          Messages.getInstance();JDialog dialog = jop.createDialog(m_parentComponent, Messages.getString("SaveBuffer_Save_Dialog_JopCreateDialog_Text"));
          dialog.setVisible(true);
          Object selectedValue = jop.getValue();
          if (selectedValue != null)
          {
            for (int i = 0; i < 4; i++) {
              if (options[i].equals(selectedValue)) {
                switch (i)
                {
                case 0: 
                  return saveOverwriteAppend(buf, sFile, true);
                
                case 1: 
                  return saveOverwriteAppend(buf, sFile, false);
                
                case 2: 
                  return save(buf);
                }
              }
            }
          }
        }
        else
        {
          saveOverwriteAppend(buf, sFile, false);
        }
      } else {
        return false;
      }
    }
    return false;
  }
  








  private boolean saveOverwriteAppend(StringBuffer buf, File sFile, boolean append)
  {
    try
    {
      String path = sFile.getPath();
      if (m_Log != null) {
        if (append) {
          Messages.getInstance();m_Log.statusMessage(Messages.getString("SaveBuffer_SaveOverwriteAppend_Log_StatusMessage_Text_First"));
        } else {
          Messages.getInstance();m_Log.statusMessage(Messages.getString("SaveBuffer_SaveOverwriteAppend_Log_StatusMessage_Text_Second"));
        }
      }
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, append)));
      

      out.write(buf.toString(), 0, buf.toString().length());
      out.close();
      if (m_Log != null) {
        Messages.getInstance();m_Log.statusMessage(Messages.getString("SaveBuffer_SaveOverwriteAppend_Log_StatusMessage_Text_Third"));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      if (m_Log != null) {
        m_Log.logMessage(ex.getMessage());
      }
      return false;
    }
    
    return true;
  }
  

  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("SaveBuffer_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      LogPanel lp = new LogPanel();
      Messages.getInstance();JButton jb = new JButton(Messages.getString("SaveBuffer_Main_Jb_JButton_Text"));
      jf.getContentPane().add(jb, "South");
      jf.getContentPane().add(lp, "Center");
      SaveBuffer svb = new SaveBuffer(lp, jf);
      jb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Messages.getInstance();val$svb.save(new StringBuffer(Messages.getString("SaveBuffer_Main_Svb_Save_Text")));
        }
        
      });
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

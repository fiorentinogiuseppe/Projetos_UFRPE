package weka.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import javax.swing.JFileChooser;
































public class FileEditor
  extends PropertyEditorSupport
{
  protected JFileChooser m_FileChooser;
  
  public FileEditor() {}
  
  public String getJavaInitializationString()
  {
    File f = (File)getValue();
    if (f == null) {
      return "null";
    }
    return "new File(\"" + f.getName() + "\")";
  }
  




  public boolean supportsCustomEditor()
  {
    return true;
  }
  





  public Component getCustomEditor()
  {
    if (m_FileChooser == null) {
      File currentFile = (File)getValue();
      if (currentFile != null) {
        m_FileChooser = new JFileChooser();
        
        m_FileChooser.setSelectedFile(currentFile);
      } else {
        m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
      }
      
      Messages.getInstance();m_FileChooser.setApproveButtonText(Messages.getString("FileEditor_GetCustomEditor_FileChooser_SetApproveButtonText_Text"));
      m_FileChooser.setApproveButtonMnemonic('S');
      m_FileChooser.setFileSelectionMode(2);
      m_FileChooser.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String cmdString = e.getActionCommand();
          if (cmdString.equals("ApproveSelection")) {
            File newVal = m_FileChooser.getSelectedFile();
            setValue(newVal);
          }
          closeDialog();
        }
      });
    }
    return m_FileChooser;
  }
  




  public boolean isPaintable()
  {
    return true;
  }
  






  public void paintValue(Graphics gfx, Rectangle box)
  {
    FontMetrics fm = gfx.getFontMetrics();
    int vpad = (height - fm.getHeight()) / 2;
    File f = (File)getValue();
    Messages.getInstance();String val = Messages.getString("FileEditor_PaintValue_Val_Text");
    if (f != null) {
      val = f.getName();
    }
    gfx.drawString(val, 2, fm.getHeight() + vpad);
  }
  


  protected void closeDialog()
  {
    if ((m_FileChooser instanceof Container)) {
      Dialog dlg = PropertyDialog.getParentDialog(m_FileChooser);
      if (dlg != null) {
        dlg.setVisible(false);
      }
    }
  }
}

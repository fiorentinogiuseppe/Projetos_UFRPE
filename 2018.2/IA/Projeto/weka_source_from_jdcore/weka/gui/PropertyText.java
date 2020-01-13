package weka.gui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
import javax.swing.JTextField;









































class PropertyText
  extends JTextField
{
  private static final long serialVersionUID = -3915342928825822730L;
  private PropertyEditor m_Editor;
  
  PropertyText(PropertyEditor pe)
  {
    super(pe.getAsText().equals("null") ? "" : pe.getAsText());
    m_Editor = pe;
    





    addKeyListener(new KeyAdapter()
    {
      public void keyReleased(KeyEvent e) {
        updateEditor();
      }
      
    });
    addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        updateEditor();
      }
    });
  }
  

  protected void updateUs()
  {
    try
    {
      setText(m_Editor.getAsText());
    }
    catch (IllegalArgumentException ex) {}
  }
  


  protected void updateEditor()
  {
    try
    {
      m_Editor.setAsText(getText());
    }
    catch (IllegalArgumentException ex) {}
  }
}

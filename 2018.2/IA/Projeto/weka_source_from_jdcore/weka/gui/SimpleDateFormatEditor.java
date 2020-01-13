package weka.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


















































public class SimpleDateFormatEditor
  implements PropertyEditor
{
  public static final String DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  private SimpleDateFormat m_Format;
  private PropertyChangeSupport m_propSupport;
  private CustomEditor m_customEditor;
  
  private class CustomEditor
    extends JPanel
    implements ActionListener, DocumentListener
  {
    private static final long serialVersionUID = -4018834274636309987L;
    private JTextField m_FormatText;
    private JButton m_DefaultButton;
    private JButton m_ApplyButton;
    
    public CustomEditor()
    {
      m_FormatText = new JTextField(20);
      Messages.getInstance();m_DefaultButton = new JButton(Messages.getString("SimpleDateFormatEditor_DefaultButton_JButton_Text"));
      Messages.getInstance();m_ApplyButton = new JButton(Messages.getString("SimpleDateFormatEditor_ApplyButton_JButton_Text"));
      
      m_DefaultButton.setMnemonic('D');
      m_ApplyButton.setMnemonic('A');
      
      m_FormatText.getDocument().addDocumentListener(this);
      m_DefaultButton.addActionListener(this);
      m_ApplyButton.addActionListener(this);
      
      setLayout(new FlowLayout());
      Messages.getInstance();add(new JLabel(Messages.getString("SimpleDateFormatEditor_JLabel_Text")));
      add(m_FormatText);
      add(m_DefaultButton);
      add(m_ApplyButton);
    }
    




    public void actionPerformed(ActionEvent e)
    {
      if (e.getSource() == m_DefaultButton) {
        defaultFormat();
      } else if (e.getSource() == m_ApplyButton) {
        applyFormat();
      }
    }
    

    public void defaultFormat()
    {
      m_FormatText.setText("yyyy-MM-dd'T'HH:mm:ss");
      formatChanged();
    }
    




    protected boolean isValidFormat()
    {
      boolean result = false;
      try
      {
        new SimpleDateFormat(m_FormatText.getText());
        result = true;
      }
      catch (Exception e) {}
      


      return result;
    }
    


    public void applyFormat()
    {
      if (isValidFormat()) {
        m_Format = new SimpleDateFormat(m_FormatText.getText());
        m_propSupport.firePropertyChange(null, null, null);
      }
      else {
        Messages.getInstance();Messages.getInstance();throw new IllegalArgumentException(Messages.getString("SimpleDateFormatEditor_ApplyFormat_IllegalArgumentException_Text_First") + m_FormatText.getText() + Messages.getString("SimpleDateFormatEditor_ApplyFormat_IllegalArgumentException_Text_Second"));
      }
    }
    





    public void formatChanged()
    {
      m_FormatText.setText(m_Format.toPattern());
      m_propSupport.firePropertyChange(null, null, null);
    }
    


    public void changedUpdate(DocumentEvent e)
    {
      m_ApplyButton.setEnabled(isValidFormat());
    }
    


    public void insertUpdate(DocumentEvent e)
    {
      m_ApplyButton.setEnabled(isValidFormat());
    }
    


    public void removeUpdate(DocumentEvent e)
    {
      m_ApplyButton.setEnabled(isValidFormat());
    }
  }
  



  public SimpleDateFormatEditor()
  {
    m_propSupport = new PropertyChangeSupport(this);
    m_customEditor = new CustomEditor();
  }
  




  public void setValue(Object value)
  {
    m_Format = ((SimpleDateFormat)value);
    m_customEditor.formatChanged();
  }
  




  public Object getValue()
  {
    return m_Format;
  }
  





  public boolean isPaintable()
  {
    return true;
  }
  







  public void paintValue(Graphics gfx, Rectangle box)
  {
    gfx.drawString(m_Format.toPattern(), x, y + height);
  }
  




  public String getJavaInitializationString()
  {
    return "new SimpleDateFormat(" + m_Format.toPattern() + ")";
  }
  




  public String getAsText()
  {
    return m_Format.toPattern();
  }
  




  public void setAsText(String text)
  {
    m_Format = new SimpleDateFormat(text);
  }
  




  public String[] getTags()
  {
    return null;
  }
  




  public Component getCustomEditor()
  {
    return m_customEditor;
  }
  




  public boolean supportsCustomEditor()
  {
    return true;
  }
  





  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    m_propSupport.addPropertyChangeListener(listener);
  }
  





  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    m_propSupport.removePropertyChangeListener(listener);
  }
}

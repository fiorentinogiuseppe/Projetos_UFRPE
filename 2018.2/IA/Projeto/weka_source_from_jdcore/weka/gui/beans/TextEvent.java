package weka.gui.beans;

import java.util.EventObject;











































public class TextEvent
  extends EventObject
{
  private static final long serialVersionUID = 4196810607402973744L;
  protected String m_text;
  protected String m_textTitle;
  
  public TextEvent(Object source, String text, String textTitle)
  {
    super(source);
    
    m_text = text;
    m_textTitle = textTitle;
  }
  




  public String getText()
  {
    return m_text;
  }
  




  public String getTextTitle()
  {
    return m_textTitle;
  }
}

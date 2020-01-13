package weka.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditor;
import javax.swing.JDialog;








































public class PropertyDialog
  extends JDialog
{
  private static final long serialVersionUID = -2314850859392433539L;
  private PropertyEditor m_Editor;
  private Component m_EditorComponent;
  
  /**
   * @deprecated
   */
  public PropertyDialog(PropertyEditor pe, int x, int y)
  {
    this((Frame)null, pe, x, y);
    setVisible(true);
  }
  






  public PropertyDialog(Dialog owner, PropertyEditor pe)
  {
    this(owner, pe, -1, -1);
  }
  








  public PropertyDialog(Dialog owner, PropertyEditor pe, int x, int y)
  {
    super(owner, pe.getClass().getName(), Dialog.ModalityType.DOCUMENT_MODAL);
    initialize(pe, x, y);
  }
  






  public PropertyDialog(Frame owner, PropertyEditor pe)
  {
    this(owner, pe, -1, -1);
  }
  








  public PropertyDialog(Frame owner, PropertyEditor pe, int x, int y)
  {
    super(owner, pe.getClass().getName(), Dialog.ModalityType.DOCUMENT_MODAL);
    
    initialize(pe, x, y);
  }
  






  protected void initialize(PropertyEditor pe, int x, int y)
  {
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        e.getWindow().dispose();
      }
    });
    getContentPane().setLayout(new BorderLayout());
    
    m_Editor = pe;
    m_EditorComponent = pe.getCustomEditor();
    getContentPane().add(m_EditorComponent, "Center");
    
    pack();
    
    int screenWidth = getGraphicsConfigurationgetBoundswidth;
    int screenHeight = getGraphicsConfigurationgetBoundsheight;
    

    if (getHeight() > screenHeight * 0.95D) {
      setSize(getWidth(), (int)(screenHeight * 0.95D));
    }
    if ((x == -1) && (y == -1)) {
      setLocationRelativeTo(null);
    }
    else
    {
      if (x + getWidth() > screenWidth)
        x = screenWidth - getWidth();
      if (y + getHeight() > screenHeight)
        y = screenHeight - getHeight();
      setLocation(x, y);
    }
  }
  




  public PropertyEditor getEditor()
  {
    return m_Editor;
  }
  








  public static Frame getParentFrame(Container c)
  {
    Frame result = null;
    
    Container parent = c;
    while (parent != null) {
      if ((parent instanceof Frame)) {
        result = (Frame)parent;
        break;
      }
      
      parent = parent.getParent();
    }
    

    return result;
  }
  








  public static Dialog getParentDialog(Container c)
  {
    Dialog result = null;
    
    Container parent = c;
    while (parent != null) {
      if ((parent instanceof Dialog)) {
        result = (Dialog)parent;
        break;
      }
      
      parent = parent.getParent();
    }
    

    return result;
  }
}

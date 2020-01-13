package weka.gui;

import java.awt.Component;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

































public class ComponentHelper
{
  public static final String[] IMAGES = { "weka/gui/", "weka/gui/images/" };
  




  public ComponentHelper() {}
  




  public static ImageIcon getImageIcon(String dir, String filename)
  {
    ImageIcon result = null;
    URL url = Loader.getURL(dir, filename);
    

    if (url == null) {
      for (int i = 0; i < IMAGES.length; i++) {
        url = Loader.getURL(IMAGES[i], filename);
        if (url != null) {
          break;
        }
      }
    }
    if (url != null) {
      result = new ImageIcon(url);
    }
    return result;
  }
  





  public static ImageIcon getImageIcon(String filename)
  {
    return getImageIcon("", filename);
  }
  









  public static Image getImage(String dir, String filename)
  {
    Image result = null;
    ImageIcon img = getImageIcon(dir, filename);
    
    if (img != null) {
      result = img.getImage();
    }
    return result;
  }
  








  public static Image getImage(String filename)
  {
    Image result = null;
    ImageIcon img = getImageIcon(filename);
    
    if (img != null) {
      result = img.getImage();
    }
    return result;
  }
  






  public static int showMessageBox(Component parent, String title, String msg, int buttons, int messageType)
  {
    String icon;
    





    switch (messageType) {
    case 0: 
      icon = "weka/gui/images/error.gif";
      break;
    case 1: 
      icon = "weka/gui/images/information.gif";
      break;
    case 2: 
      icon = "weka/gui/images/information.gif";
      break;
    case 3: 
      icon = "weka/gui/images/question.gif";
      break;
    default: 
      icon = "weka/gui/images/information.gif";
    }
    
    
    return JOptionPane.showConfirmDialog(parent, msg, title, buttons, messageType, getImageIcon(icon));
  }
  










  public static String showInputBox(Component parent, String title, String msg, Object initialValue)
  {
    if (title == null) {
      Messages.getInstance();title = Messages.getString("ComponentHelper_ShowInputBox_Title_Text");
    }
    Object result = JOptionPane.showInputDialog(parent, msg, title, 3, getImageIcon("question.gif"), null, initialValue);
    

    if (result != null) {
      return result.toString();
    }
    return null;
  }
}

package weka.gui;

import java.io.PrintStream;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import weka.core.Utils;


































public class LookAndFeel
{
  public static String PROPERTY_FILE = "weka/gui/LookAndFeel.props";
  protected static Properties LOOKANDFEEL_PROPERTIES;
  
  static
  {
    try
    {
      LOOKANDFEEL_PROPERTIES = Utils.readProperties(PROPERTY_FILE);
    }
    catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("LookAndFeel_Exception_JOptionPaneShowMessageDialog_Text_First") + PROPERTY_FILE + Messages.getString("LookAndFeel_Exception_JOptionPaneShowMessageDialog_Text_Second") + System.getProperties().getProperty("user.home") + Messages.getString("LookAndFeel_Exception_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("LookAndFeel_Exception_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
    }
  }
  






  public static boolean setLookAndFeel(String classname)
  {
    boolean result;
    





    try
    {
      UIManager.setLookAndFeel(classname);
      result = true;
    }
    catch (Exception e) {
      e.printStackTrace();
      result = false;
    }
    
    return result;
  }
  







  public static boolean setLookAndFeel()
  {
    Messages.getInstance();String classname = LOOKANDFEEL_PROPERTIES.getProperty(Messages.getString("LookAndFeel_SetLookAndFeel_ClassName_Text"), "");
    if (classname.equals(""))
    {


      if (System.getProperty("os.name").equalsIgnoreCase("linux")) {
        return true;
      }
      
      classname = getSystemLookAndFeel();
    }
    

    return setLookAndFeel(classname);
  }
  




  public static String getSystemLookAndFeel()
  {
    return UIManager.getSystemLookAndFeelClassName();
  }
  








  public static String[] getInstalledLookAndFeels()
  {
    UIManager.LookAndFeelInfo[] laf = UIManager.getInstalledLookAndFeels();
    String[] result = new String[laf.length];
    for (int i = 0; i < laf.length; i++) {
      result[i] = laf[i].getClassName();
    }
    return result;
  }
  







  public static void main(String[] args)
  {
    Messages.getInstance();System.out.println(Messages.getString("LookAndFeel_Main_Text_First"));
    String[] list = getInstalledLookAndFeels();
    for (int i = 0; i < list.length; i++) {
      System.out.println(i + 1 + ". " + list[i]);
    }
    Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("LookAndFeel_Main_Text_Second") + PROPERTY_FILE + Messages.getString("LookAndFeel_Main_Text_Third"));
  }
  
  public LookAndFeel() {}
}

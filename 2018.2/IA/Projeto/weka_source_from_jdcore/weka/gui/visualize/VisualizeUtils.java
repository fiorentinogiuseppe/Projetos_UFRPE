package weka.gui.visualize;

import java.awt.Color;
import java.io.PrintStream;
import java.util.Properties;
import javax.swing.JOptionPane;
import weka.core.Utils;

































public class VisualizeUtils
{
  protected static String PROPERTY_FILE = "weka/gui/visualize/Visualize.props";
  

  protected static Properties VISUALIZE_PROPERTIES;
  

  protected static int MAX_PRECISION = 10;
  
  static
  {
    try
    {
      VISUALIZE_PROPERTIES = Utils.readProperties(PROPERTY_FILE);
      String precision = VISUALIZE_PROPERTIES.getProperty("weka.gui.visualize.precision");
      
      if (precision != null)
      {





        MAX_PRECISION = Integer.parseInt(precision);
      }
    }
    catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("VisualizeUtils_JOptionPaneShowMessageDialog_Text_First") + PROPERTY_FILE + Messages.getString("VisualizeUtils_JOptionPaneShowMessageDialog_Text_Second") + System.getProperties().getProperty("user.home") + Messages.getString("VisualizeUtils_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("VisualizeUtils_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
    }
  }
  











  public static Color processColour(String colourDef, Color defaultColour)
  {
    String colourDefBack = new String(colourDef);
    Color retC = defaultColour;
    if (colourDef.indexOf(",") >= 0)
    {
      try {
        int index = colourDef.indexOf(",");
        int R = Integer.parseInt(colourDef.substring(0, index));
        colourDef = colourDef.substring(index + 1, colourDef.length());
        index = colourDef.indexOf(",");
        int G = Integer.parseInt(colourDef.substring(0, index));
        colourDef = colourDef.substring(index + 1, colourDef.length());
        int B = Integer.parseInt(colourDef);
        
        retC = new Color(R, G, B);
      } catch (Exception ex) {
        Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("VisualizeUtils_ProcessColour_Error_Text_First") + colourDefBack + Messages.getString("VisualizeUtils_ProcessColour_Error_Text_Second"));
      }
      

    }
    else if (colourDef.compareTo("black") == 0) {
      retC = Color.black;
    } else if (colourDef.compareTo("blue") == 0) {
      retC = Color.blue;
    } else if (colourDef.compareTo("cyan") == 0) {
      retC = Color.cyan;
    } else if (colourDef.compareTo("darkGray") == 0) {
      retC = Color.darkGray;
    } else if (colourDef.compareTo("gray") == 0) {
      retC = Color.gray;
    } else if (colourDef.compareTo("green") == 0) {
      retC = Color.green;
    } else if (colourDef.compareTo("lightGray") == 0) {
      retC = Color.lightGray;
    } else if (colourDef.compareTo("magenta") == 0) {
      retC = Color.magenta;
    } else if (colourDef.compareTo("orange") == 0) {
      retC = Color.orange;
    } else if (colourDef.compareTo("pink") == 0) {
      retC = Color.pink;
    } else if (colourDef.compareTo("red") == 0) {
      retC = Color.red;
    } else if (colourDef.compareTo("white") == 0) {
      retC = Color.white;
    } else if (colourDef.compareTo("yellow") == 0) {
      retC = Color.yellow;
    } else {
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("VisualizeUtils_ProcessColour_Error_Text_Third") + colourDefBack + Messages.getString("VisualizeUtils_ProcessColour_Error_Text_Fourth"));
    }
    
    return retC;
  }
  
  public VisualizeUtils() {}
}

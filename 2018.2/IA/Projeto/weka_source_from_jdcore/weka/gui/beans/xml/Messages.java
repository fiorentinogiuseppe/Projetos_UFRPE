package weka.gui.beans.xml;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;





























public class Messages
{
  private static Messages instance;
  private static Locale locale = ;
  private static String packageLocation = Messages.class.getPackage().getName();
  private static String DEFAULT_FILE_NAME = ".messages.messages";
  

  public Messages() {}
  
  public static Messages getInstance()
  {
    if (instance == null) {
      instance = new Messages();
    }
    return instance;
  }
  



  public static String getString(String key)
  {
    try
    {
      return ResourceBundle.getBundle(packageLocation + DEFAULT_FILE_NAME + "_" + locale.getLanguage()).getString(key);
    } catch (MissingResourceException e) {
      try {
        return ResourceBundle.getBundle(packageLocation + DEFAULT_FILE_NAME).getString(key);
      } catch (MissingResourceException missingResourceException) {} }
    return null;
  }
  






  public static String getString(String key, Locale locale)
  {
    try
    {
      return ResourceBundle.getBundle(packageLocation + DEFAULT_FILE_NAME + "_" + locale.getLanguage()).getString(key);
    } catch (MissingResourceException e) {
      try {
        return ResourceBundle.getBundle(packageLocation + DEFAULT_FILE_NAME).getString(key);
      } catch (MissingResourceException missingResourceException) {} }
    return null;
  }
}

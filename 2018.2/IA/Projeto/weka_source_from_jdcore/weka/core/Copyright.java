package weka.core;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Properties;

































public class Copyright
{
  public static final String PROPERTY_FILE = "weka/core/Copyright.props";
  protected static Properties PROPERTIES = new Properties();
  
  static {
    try {
      PROPERTIES.load(new Copyright().getClass().getClassLoader().getResourceAsStream("weka/core/Copyright.props"));
    }
    catch (Exception e)
    {
      System.err.println("Could not read configuration file for the copyright information - using default.");
    }
  }
  






  public static String getFromYear()
  {
    return PROPERTIES.getProperty("FromYear", "1999");
  }
  




  public static String getToYear()
  {
    return PROPERTIES.getProperty("ToYear", "" + Calendar.getInstance().get(1));
  }
  




  public static String getOwner()
  {
    return PROPERTIES.getProperty("Owner", "The University of Waikato");
  }
  




  public static String getAddress()
  {
    return PROPERTIES.getProperty("Address", "Hamilton, New Zealand");
  }
  




  public static String getURL()
  {
    return PROPERTIES.getProperty("URL", "http://www.cs.waikato.ac.nz/~ml/");
  }
  




  public static void main(String[] args)
  {
    System.out.println(PROPERTIES);
  }
  
  public Copyright() {}
}

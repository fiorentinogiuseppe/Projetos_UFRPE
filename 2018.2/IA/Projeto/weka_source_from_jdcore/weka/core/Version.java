package weka.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;



































public class Version
  implements Comparable, RevisionHandler
{
  public static final String VERSION_FILE = "weka/core/version.txt";
  public static int MAJOR = 3;
  

  public static int MINOR = 4;
  

  public static int REVISION = 3;
  

  public static boolean SNAPSHOT = false;
  protected static final String SNAPSHOT_STRING = "-SNAPSHOT";
  
  static
  {
    try {
      InputStream inR = new Version().getClass().getClassLoader().getResourceAsStream("weka/core/version.txt");
      

      LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inR));
      
      String line = lnr.readLine();
      int[] maj = new int[1];
      int[] min = new int[1];
      int[] rev = new int[1];
      SNAPSHOT = parseVersion(line, maj, min, rev);
      MAJOR = maj[0];
      MINOR = min[0];
      REVISION = rev[0];
      lnr.close();
    } catch (Exception e) {
      System.err.println(Version.class.getName() + ": Unable to load version information!");
    }
  }
  


  public static String VERSION = MAJOR + "." + MINOR + "." + REVISION + (SNAPSHOT ? "-SNAPSHOT" : "");
  









  private static boolean parseVersion(String version, int[] maj, int[] min, int[] rev)
  {
    int major = 0;
    int minor = 0;
    int revision = 0;
    boolean isSnapshot = false;
    try
    {
      String tmpStr = version;
      if (tmpStr.toLowerCase().endsWith("-snapshot")) {
        tmpStr = tmpStr.substring(0, tmpStr.toLowerCase().indexOf("-snapshot"));
        isSnapshot = true;
      }
      tmpStr = tmpStr.replace('-', '.');
      if (tmpStr.indexOf(".") > -1) {
        major = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(".")));
        tmpStr = tmpStr.substring(tmpStr.indexOf(".") + 1);
        if (tmpStr.indexOf(".") > -1) {
          minor = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(".")));
          tmpStr = tmpStr.substring(tmpStr.indexOf(".") + 1);
          if (!tmpStr.equals("")) {
            revision = Integer.parseInt(tmpStr);
          } else {
            revision = 0;
          }
        } else if (!tmpStr.equals("")) {
          minor = Integer.parseInt(tmpStr);
        } else {
          minor = 0;
        }
      }
      else if (!tmpStr.equals("")) {
        major = Integer.parseInt(tmpStr);
      } else {
        major = 0;
      }
    } catch (Exception e) {
      e.printStackTrace();
      major = -1;
      minor = -1;
      revision = -1;
    } finally {
      maj[0] = major;
      min[0] = minor;
      rev[0] = revision;
    }
    return isSnapshot;
  }
  










  public int compareTo(Object o)
  {
    int[] maj = new int[1];
    int[] min = new int[1];
    int[] rev = new int[1];
    int revision;
    int major;
    int minor; int revision; if ((o instanceof String)) {
      parseVersion((String)o, maj, min, rev);
      int major = maj[0];
      int minor = min[0];
      revision = rev[0];
    } else {
      System.out.println(getClass().getName() + ": no version-string for comparTo povided!");
      
      major = -1;
      minor = -1;
      revision = -1; }
    int result;
    int result;
    if (MAJOR < major) {
      result = -1; } else { int result;
      if (MAJOR == major) { int result;
        if (MINOR < minor) {
          result = -1; } else { int result;
          if (MINOR == minor) { int result;
            if (REVISION < revision) {
              result = -1; } else { int result;
              if (REVISION == revision) {
                result = 0;
              } else
                result = 1;
            }
          } else {
            result = 1;
          }
        }
      } else { result = 1;
      }
    }
    return result;
  }
  






  public boolean equals(Object o)
  {
    return compareTo(o) == 0;
  }
  






  public boolean isOlder(Object o)
  {
    return compareTo(o) == -1;
  }
  






  public boolean isNewer(Object o)
  {
    return compareTo(o) == 1;
  }
  





  public String toString()
  {
    return VERSION;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  








  public static void main(String[] args)
  {
    System.out.println(VERSION + "\n");
    

    Version v = new Version();
    System.out.println("-1? " + v.compareTo("5.0.1"));
    System.out.println(" 0? " + v.compareTo(VERSION));
    System.out.println("+1? " + v.compareTo("3.4.0"));
    
    String tmpStr = "5.0.1";
    System.out.println("\ncomparing with " + tmpStr);
    System.out.println("isOlder? " + v.isOlder(tmpStr));
    System.out.println("equals ? " + v.equals(tmpStr));
    System.out.println("isNewer? " + v.isNewer(tmpStr));
    
    tmpStr = VERSION;
    System.out.println("\ncomparing with " + tmpStr);
    System.out.println("isOlder? " + v.isOlder(tmpStr));
    System.out.println("equals ? " + v.equals(tmpStr));
    System.out.println("isNewer? " + v.isNewer(tmpStr));
    
    tmpStr = "3.4.0";
    System.out.println("\ncomparing with " + tmpStr);
    System.out.println("isOlder? " + v.isOlder(tmpStr));
    System.out.println("equals ? " + v.equals(tmpStr));
    System.out.println("isNewer? " + v.isNewer(tmpStr));
    
    tmpStr = "3.4";
    System.out.println("\ncomparing with " + tmpStr);
    System.out.println("isOlder? " + v.isOlder(tmpStr));
    System.out.println("equals ? " + v.equals(tmpStr));
    System.out.println("isNewer? " + v.isNewer(tmpStr));
    
    tmpStr = "5";
    System.out.println("\ncomparing with " + tmpStr);
    System.out.println("isOlder? " + v.isOlder(tmpStr));
    System.out.println("equals ? " + v.equals(tmpStr));
    System.out.println("isNewer? " + v.isNewer(tmpStr));
  }
  
  public Version() {}
}

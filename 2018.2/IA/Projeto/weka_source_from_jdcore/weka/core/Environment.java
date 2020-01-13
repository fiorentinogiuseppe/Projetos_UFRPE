package weka.core;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;





























public class Environment
  implements RevisionHandler
{
  private static Environment m_systemWide = new Environment();
  

  private Map<String, String> m_envVars = new TreeMap();
  
  public Environment()
  {
    Map<String, String> env = System.getenv();
    Set<String> keys = env.keySet();
    Iterator<String> i = keys.iterator();
    while (i.hasNext()) {
      String kv = (String)i.next();
      String value = (String)env.get(kv);
      m_envVars.put(kv, value);
    }
    

    Properties jvmProps = System.getProperties();
    Enumeration pKeys = jvmProps.propertyNames();
    while (pKeys.hasMoreElements()) {
      String kv = (String)pKeys.nextElement();
      String value = jvmProps.getProperty(kv);
      m_envVars.put(kv, value);
    }
    m_envVars.put("weka.version", Version.VERSION);
  }
  






  public static Environment getSystemWide()
  {
    return m_systemWide;
  }
  






  public static boolean containsEnvVariables(String source)
  {
    return source.indexOf("${") >= 0;
  }
  






  public String substitute(String source)
    throws Exception
  {
    int index = source.indexOf("${");
    
    while (index >= 0) {
      index += 2;
      int endIndex = source.indexOf('}');
      if ((endIndex < 0) || (endIndex <= index + 1)) break;
      String key = source.substring(index, endIndex);
      

      String replace = (String)m_envVars.get(key);
      if (replace != null) {
        String toReplace = "${" + key + "}";
        source = source.replace(toReplace, replace);
      } else {
        throw new Exception("[Environment] Variable " + key + " doesn't seem to be set.");
      }
      



      index = source.indexOf("${");
    }
    return source;
  }
  





  public void addVariable(String key, String value)
  {
    m_envVars.put(key, value);
  }
  




  public void removeVariable(String key)
  {
    m_envVars.remove(key);
  }
  





  public Set<String> getVariableNames()
  {
    return m_envVars.keySet();
  }
  






  public String getVariableValue(String key)
  {
    return (String)m_envVars.get(key);
  }
  





  public static void main(String[] args)
  {
    Environment t = new Environment();
    

    if (args.length == 0) {
      System.err.println("Usage: java weka.core.Environment <string> <string> ...");
    } else {
      try {
        for (int i = 0; i < args.length; i++) {
          String newS = t.substitute(args[i]);
          System.out.println("Original string:\n" + args[i] + "\n\nNew string:\n" + newS);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
}

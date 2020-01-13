package weka.core;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import weka.gui.LookAndFeel;






























public class SystemInfo
  implements RevisionHandler
{
  private Hashtable m_Info = null;
  


  public SystemInfo()
  {
    m_Info = new Hashtable();
    readProperties();
  }
  










  private void readProperties()
  {
    m_Info.clear();
    

    Properties props = System.getProperties();
    Enumeration enm = props.propertyNames();
    while (enm.hasMoreElements()) {
      Object name = enm.nextElement();
      m_Info.put(name, props.get(name));
    }
    

    m_Info.put("weka.version", Version.VERSION);
    

    String[] laf = LookAndFeel.getInstalledLookAndFeels();
    String tmpStr = "";
    for (int i = 0; i < laf.length; i++) {
      if (i > 0)
        tmpStr = tmpStr + ",";
      tmpStr = tmpStr + laf[i];
    }
    m_Info.put("ui.installedLookAndFeels", tmpStr);
    m_Info.put("ui.currentLookAndFeel", LookAndFeel.getSystemLookAndFeel());
    

    Memory mem = new Memory();
    m_Info.put("memory.initial", "" + Utils.doubleToString(Memory.toMegaByte(mem.getInitial()), 1) + "MB" + " (" + mem.getInitial() + ")");
    


    m_Info.put("memory.max", "" + Utils.doubleToString(Memory.toMegaByte(mem.getMax()), 1) + "MB" + " (" + mem.getMax() + ")");
  }
  






  public Hashtable getSystemInfo()
  {
    return (Hashtable)m_Info.clone();
  }
  









  public String toString()
  {
    String result = "";
    Vector keys = new Vector();
    

    Enumeration enm = m_Info.keys();
    while (enm.hasMoreElements())
      keys.add(enm.nextElement());
    Collections.sort(keys);
    

    for (int i = 0; i < keys.size(); i++) {
      String key = keys.get(i).toString();
      String value = m_Info.get(key).toString();
      if (key.equals("line.separator"))
        value = Utils.backQuoteChars(value);
      result = result + key + ": " + value + "\n";
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  


  public static void main(String[] args)
  {
    System.out.println(new SystemInfo());
  }
}

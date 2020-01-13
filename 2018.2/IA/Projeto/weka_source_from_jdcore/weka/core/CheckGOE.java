package weka.core;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import weka.classifiers.rules.ZeroR;






















































public class CheckGOE
  extends Check
{
  protected Object m_Object = new ZeroR();
  


  protected boolean m_Success;
  

  protected HashSet<String> m_IgnoredProperties = new HashSet();
  




  public CheckGOE()
  {
    try
    {
      setOptions(new String[0]);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tSkipped properties.\n\t(default: capabilities,options)", "ignored", 1, "-ignored <comma-separated list of properties>"));
    



    result.addElement(new Option("\tFull name of the class analysed.\n\teg: weka.classifiers.rules.ZeroR\n\t(default weka.classifiers.rules.ZeroR)", "W", 1, "-W"));
    




    return result.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() == 0)
      tmpStr = ZeroR.class.getName();
    setObject(Utils.forName(Object.class, tmpStr, null));
    
    tmpStr = Utils.getOption("ignored", options);
    if (tmpStr.length() == 0)
      tmpStr = "capabilities,options";
    setIgnoredProperties(tmpStr);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-ignored");
    result.add(getIgnoredProperties());
    
    if (getObject() != null) {
      result.add("-W");
      result.add(getObject().getClass().getName());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setObject(Object value)
  {
    m_Object = value;
  }
  




  public Object getObject()
  {
    return m_Object;
  }
  








  public void setIgnoredProperties(String value)
  {
    m_IgnoredProperties.clear();
    String[] props = value.split(",");
    for (int i = 0; i < props.length; i++) {
      m_IgnoredProperties.add(props[i]);
    }
  }
  










  public String getIgnoredProperties()
  {
    Vector<String> list = new Vector();
    Iterator iter = m_IgnoredProperties.iterator();
    while (iter.hasNext()) {
      list.add((String)iter.next());
    }
    
    if (list.size() > 1) {
      Collections.sort(list);
    }
    String result = "";
    for (int i = 0; i < list.size(); i++) {
      if (i > 0)
        result = result + ",";
      result = result + (String)list.get(i);
    }
    
    return result;
  }
  




  public boolean getSuccess()
  {
    return m_Success;
  }
  







  public boolean checkGlobalInfo()
  {
    print("Global info...");
    
    boolean result = true;
    Class cls = getObject().getClass();
    
    try
    {
      cls.getMethod("globalInfo", (Class[])null);
    }
    catch (Exception e) {
      result = false;
    }
    
    if (result) {
      println("yes");
    } else {
      println("no");
    }
    return result;
  }
  













  public boolean checkToolTips()
  {
    print("Tool tips...");
    
    boolean result = true;
    String suffix = "TipText";
    Class cls = getObject().getClass();
    PropertyDescriptor[] desc;
    try
    {
      BeanInfo info = Introspector.getBeanInfo(cls, Object.class);
      desc = info.getPropertyDescriptors();
    }
    catch (Exception e) {
      e.printStackTrace();
      desc = null;
    }
    

    if (desc != null) {
      Vector<String> missing = new Vector();
      
      for (int i = 0; i < desc.length; i++)
      {
        if (!m_IgnoredProperties.contains(desc[i].getName()))
        {
          if ((desc[i].getReadMethod() != null) && (desc[i].getWriteMethod() != null))
          {
            try
            {
              cls.getMethod(desc[i].getName() + suffix, (Class[])null);
            }
            catch (Exception e) {
              result = false;
              missing.add(desc[i].getName() + suffix);
            } }
        }
      }
      if (result) {
        println("yes");
      } else {
        println("no (missing: " + missing + ")");
      }
    }
    else {
      println("maybe");
    }
    
    return result;
  }
  



  public void doTests()
  {
    println("Object: " + m_Object.getClass().getName() + "\n");
    
    println("--> Tests");
    
    m_Success = checkGlobalInfo();
    
    if (m_Success) {
      m_Success = checkToolTips();
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  




  public static void main(String[] args)
  {
    CheckGOE check = new CheckGOE();
    runCheck(check, args);
    if (check.getSuccess()) {
      System.exit(0);
    } else {
      System.exit(1);
    }
  }
}

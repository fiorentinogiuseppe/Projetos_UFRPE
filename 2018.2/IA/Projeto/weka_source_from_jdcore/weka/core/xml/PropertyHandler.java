package weka.core.xml;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;







































public class PropertyHandler
  implements RevisionHandler
{
  protected Hashtable m_Ignored = null;
  










  protected Hashtable m_Allowed = null;
  




  public PropertyHandler()
  {
    m_Ignored = new Hashtable();
    m_Allowed = new Hashtable();
  }
  








  public Enumeration ignored()
  {
    return m_Ignored.keys();
  }
  










  public void addIgnored(String displayName)
  {
    HashSet list = new HashSet();
    list.add(displayName);
    
    m_Ignored.put(displayName, list);
  }
  



  public void addIgnored(Class c, String displayName)
  {
    HashSet list;
    


    HashSet list;
    

    if (m_Ignored.contains(c)) {
      list = (HashSet)m_Ignored.get(c);
    }
    else {
      list = new HashSet();
      m_Ignored.put(c, list);
    }
    
    list.add(displayName);
  }
  







  public boolean removeIgnored(String displayName)
  {
    return m_Ignored.remove(displayName) != null;
  }
  




  public boolean removeIgnored(Class c, String displayName)
  {
    HashSet list;
    


    HashSet list;
    


    if (m_Ignored.contains(c)) {
      list = (HashSet)m_Ignored.get(c);
    } else {
      list = new HashSet();
    }
    return list.remove(displayName);
  }
  






  public boolean isIgnored(String displayName)
  {
    return m_Ignored.containsKey(displayName);
  }
  




  public boolean isIgnored(Class c, String displayName)
  {
    HashSet list;
    



    HashSet list;
    



    if (m_Ignored.containsKey(c)) {
      list = (HashSet)m_Ignored.get(c);
    } else {
      list = new HashSet();
    }
    return list.contains(displayName);
  }
  
















  public boolean isIgnored(Object o, String displayName)
  {
    boolean result = false;
    
    Enumeration enm = ignored();
    while (enm.hasMoreElements()) {
      Object element = enm.nextElement();
      

      if ((element instanceof Class))
      {

        Class c = (Class)element;
        

        if (c.isInstance(o)) {
          HashSet list = (HashSet)m_Ignored.get(c);
          result = list.contains(displayName);
        }
      }
    }
    
    return result;
  }
  





  public Enumeration allowed()
  {
    return m_Allowed.keys();
  }
  










  public void addAllowed(Class c, String displayName)
  {
    HashSet list = (HashSet)m_Allowed.get(c);
    if (list == null) {
      list = new HashSet();
      m_Allowed.put(c, list);
    }
    

    list.add(displayName);
  }
  











  public boolean removeAllowed(Class c, String displayName)
  {
    boolean result = false;
    

    HashSet list = (HashSet)m_Allowed.get(c);
    

    if (list != null) {
      result = list.remove(displayName);
    }
    return result;
  }
  














  public boolean isAllowed(Class c, String displayName)
  {
    boolean result = true;
    

    HashSet list = (HashSet)m_Allowed.get(c);
    

    if (list != null) {
      result = list.contains(displayName);
    }
    return result;
  }
  














  public boolean isAllowed(Object o, String displayName)
  {
    boolean result = true;
    
    Enumeration enm = allowed();
    while (enm.hasMoreElements()) {
      Class c = (Class)enm.nextElement();
      

      if (c.isInstance(o)) {
        HashSet list = (HashSet)m_Allowed.get(c);
        result = list.contains(displayName);
      }
    }
    

    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

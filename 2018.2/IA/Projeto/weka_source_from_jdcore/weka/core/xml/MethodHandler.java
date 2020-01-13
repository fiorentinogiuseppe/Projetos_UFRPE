package weka.core.xml;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;











































public class MethodHandler
  implements RevisionHandler
{
  protected Hashtable m_Methods = null;
  



  public MethodHandler()
  {
    m_Methods = new Hashtable();
  }
  






  public Enumeration keys()
  {
    return m_Methods.keys();
  }
  








  public void add(String displayName, Method method)
  {
    if (method != null) {
      m_Methods.put(displayName, method);
    }
  }
  






  public void add(Class c, Method method)
  {
    if (method != null) {
      m_Methods.put(c, method);
    }
  }
  







  public boolean remove(String displayName)
  {
    return m_Methods.remove(displayName) != null;
  }
  






  public boolean remove(Class c)
  {
    return m_Methods.remove(c) != null;
  }
  






  public boolean contains(String displayName)
  {
    return m_Methods.containsKey(displayName);
  }
  






  public boolean contains(Class c)
  {
    return m_Methods.containsKey(c);
  }
  







  public Method get(String displayName)
  {
    return (Method)m_Methods.get(displayName);
  }
  






  public Method get(Class c)
  {
    return (Method)m_Methods.get(c);
  }
  




  public int size()
  {
    return m_Methods.size();
  }
  


  public void clear()
  {
    m_Methods.clear();
  }
  





  public String toString()
  {
    return m_Methods.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}

package weka.core;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

































public class ProtectedProperties
  extends Properties
  implements RevisionHandler
{
  private static final long serialVersionUID = 3876658672657323985L;
  private boolean closed = false;
  






  public ProtectedProperties(Properties props)
  {
    Enumeration propEnum = props.propertyNames();
    while (propEnum.hasMoreElements()) {
      String propName = (String)propEnum.nextElement();
      String propValue = props.getProperty(propName);
      super.setProperty(propName, propValue);
    }
    closed = true;
  }
  







  public Object setProperty(String key, String value)
  {
    if (closed) {
      throw new UnsupportedOperationException("ProtectedProperties cannot be modified!");
    }
    return super.setProperty(key, value);
  }
  





  public void load(InputStream inStream)
  {
    throw new UnsupportedOperationException("ProtectedProperties cannot be modified!");
  }
  






  public void clear()
  {
    throw new UnsupportedOperationException("ProtectedProperties cannot be modified!");
  }
  








  public Object put(Object key, Object value)
  {
    if (closed) {
      throw new UnsupportedOperationException("ProtectedProperties cannot be modified!");
    }
    return super.put(key, value);
  }
  





  public void putAll(Map t)
  {
    throw new UnsupportedOperationException("ProtectedProperties cannot be modified!");
  }
  







  public Object remove(Object key)
  {
    throw new UnsupportedOperationException("ProtectedProperties cannot be modified!");
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

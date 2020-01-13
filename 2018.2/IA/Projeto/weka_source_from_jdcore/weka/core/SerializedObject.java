package weka.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;











































public class SerializedObject
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 6635502953928860434L;
  private byte[] m_storedObjectArray;
  private boolean m_isCompressed;
  private boolean m_isJython;
  
  public SerializedObject(Object toStore)
    throws Exception
  {
    this(toStore, false);
  }
  






  public SerializedObject(Object toStore, boolean compress)
    throws Exception
  {
    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
    OutputStream os = ostream;
    ObjectOutputStream p;
    ObjectOutputStream p; if (!compress) {
      p = new ObjectOutputStream(new BufferedOutputStream(os));
    } else
      p = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(os)));
    p.writeObject(toStore);
    p.flush();
    p.close();
    m_storedObjectArray = ostream.toByteArray();
    
    m_isCompressed = compress;
    m_isJython = (toStore instanceof JythonSerializableObject);
  }
  






  public final boolean equals(Object compareTo)
  {
    if (compareTo == null) return false;
    if (!compareTo.getClass().equals(getClass())) return false;
    byte[] compareArray = m_storedObjectArray;
    if (compareArray.length != m_storedObjectArray.length) return false;
    for (int i = 0; i < compareArray.length; i++) {
      if (compareArray[i] != m_storedObjectArray[i]) return false;
    }
    return true;
  }
  





  public int hashCode()
  {
    return m_storedObjectArray.length;
  }
  








  public Object getObject()
  {
    try
    {
      ByteArrayInputStream istream = new ByteArrayInputStream(m_storedObjectArray);
      
      Object toReturn = null;
      if (m_isJython) {
        if (!m_isCompressed) {
          toReturn = Jython.deserialize(new BufferedInputStream(istream));
        } else
          toReturn = Jython.deserialize(new BufferedInputStream(new GZIPInputStream(istream)));
      } else { ObjectInputStream p;
        ObjectInputStream p;
        if (!m_isCompressed) {
          p = new ObjectInputStream(new BufferedInputStream(istream));
        } else
          p = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(istream)));
        toReturn = p.readObject();
      }
      istream.close();
      return toReturn;
    } catch (Exception e) {
      e.printStackTrace(); }
    return null;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.12 $");
  }
}

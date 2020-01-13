package weka.experiment;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;









































public class PropertyNode
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8718165742572631384L;
  public Object value;
  public Class parentClass;
  public PropertyDescriptor property;
  
  public PropertyNode(Object pValue)
  {
    this(pValue, null, null);
  }
  







  public PropertyNode(Object pValue, PropertyDescriptor prop, Class pClass)
  {
    value = pValue;
    property = prop;
    parentClass = pClass;
  }
  





  public String toString()
  {
    if (property == null) {
      return "Available properties";
    }
    return property.getDisplayName();
  }
  


  private void writeObject(ObjectOutputStream out)
    throws IOException
  {
    try
    {
      out.writeObject(value);
    } catch (Exception ex) {
      throw new IOException("Can't serialize object: " + ex.getMessage());
    }
    out.writeObject(parentClass);
    out.writeObject(property.getDisplayName());
    out.writeObject(property.getReadMethod().getName());
    out.writeObject(property.getWriteMethod().getName());
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    value = in.readObject();
    parentClass = ((Class)in.readObject());
    String name = (String)in.readObject();
    String getter = (String)in.readObject();
    String setter = (String)in.readObject();
    





    try
    {
      property = new PropertyDescriptor(name, parentClass, getter, setter);
    } catch (IntrospectionException ex) {
      throw new ClassNotFoundException("Couldn't create property descriptor: " + parentClass.getName() + "::" + name);
    }
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

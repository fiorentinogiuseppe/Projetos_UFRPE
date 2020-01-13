package weka.core.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

































public class XStream
  implements RevisionHandler
{
  protected static boolean m_Present = false;
  
  public static final String FILE_EXTENSION = ".xstream";
  

  static
  {
    checkForXStream();
  }
  

  private static void checkForXStream()
  {
    try
    {
      Class.forName("com.thoughtworks.xstream.XStream");
      m_Present = true;
    }
    catch (Exception e) {
      m_Present = false;
    }
  }
  





  public static boolean isPresent()
  {
    return m_Present;
  }
  








  public static String serialize(Object toSerialize)
    throws Exception
  {
    Class[] serializeArgsClasses = new Class[1];
    Object[] serializeArgs = new Object[1];
    


    Class xstreamClass = Class.forName("com.thoughtworks.xstream.XStream");
    Constructor constructor = xstreamClass.getConstructor(new Class[0]);
    Object xstream = constructor.newInstance(new Object[0]);
    
    serializeArgsClasses[0] = Object.class;
    serializeArgs[0] = toSerialize;
    Method methodSerialize = xstreamClass.getMethod("toXML", serializeArgsClasses);
    String result;
    try
    {
      result = (String)methodSerialize.invoke(xstream, serializeArgs);
    } catch (Exception ex) {
      result = null;
    }
    
    return result;
  }
  





  public static boolean write(String filename, Object o)
    throws Exception
  {
    return write(new File(filename), o);
  }
  





  public static boolean write(File file, Object o)
    throws Exception
  {
    return write(new BufferedOutputStream(new FileOutputStream(file)), o);
  }
  









  public static boolean write(OutputStream stream, Object o)
    throws Exception
  {
    Class[] serializeArgsClasses = new Class[2];
    Object[] serializeArgs = new Object[2];
    
    boolean result = false;
    
    Class xstreamClass = Class.forName("com.thoughtworks.xstream.XStream");
    Constructor constructor = xstreamClass.getConstructor(new Class[0]);
    Object xstream = constructor.newInstance(new Object[0]);
    
    serializeArgsClasses[0] = Object.class;
    serializeArgsClasses[1] = OutputStream.class;
    serializeArgs[0] = o;
    serializeArgs[1] = stream;
    Method methodSerialize = xstreamClass.getMethod("toXML", serializeArgsClasses);
    
    try
    {
      methodSerialize.invoke(xstream, serializeArgs);
      result = true;
    } catch (Exception ex) {
      result = false;
    }
    
    return result;
  }
  








  public static boolean write(Writer writer, Object toSerialize)
    throws Exception
  {
    Class[] serializeArgsClasses = new Class[2];
    Object[] serializeArgs = new Object[2];
    
    boolean result = false;
    
    Class xstreamClass = Class.forName("com.thoughtworks.xstream.XStream");
    Constructor constructor = xstreamClass.getConstructor(new Class[0]);
    Object xstream = constructor.newInstance(new Object[0]);
    
    serializeArgsClasses[0] = Object.class;
    serializeArgsClasses[1] = Writer.class;
    serializeArgs[0] = toSerialize;
    serializeArgs[1] = writer;
    Method methodSerialize = xstreamClass.getMethod("toXML", serializeArgsClasses);
    
    try
    {
      methodSerialize.invoke(xstream, serializeArgs);
      result = true;
    } catch (Exception ex) {
      result = false;
    }
    
    return result;
  }
  




  public static Object read(String filename)
    throws Exception
  {
    return read(new File(filename));
  }
  




  public static Object read(File file)
    throws Exception
  {
    return read(new BufferedInputStream(new FileInputStream(file)));
  }
  








  public static Object read(InputStream stream)
    throws Exception
  {
    Class[] deSerializeArgsClasses = new Class[1];
    Object[] deSerializeArgs = new Object[1];
    


    Class xstreamClass = Class.forName("com.thoughtworks.xstream.XStream");
    Constructor constructor = xstreamClass.getConstructor(new Class[0]);
    Object xstream = constructor.newInstance(new Object[0]);
    
    deSerializeArgsClasses[0] = InputStream.class;
    deSerializeArgs[0] = stream;
    Method methodDeSerialize = xstreamClass.getMethod("fromXML", deSerializeArgsClasses);
    Object result;
    try
    {
      result = methodDeSerialize.invoke(xstream, deSerializeArgs);
    } catch (Exception ex) {
      ex.printStackTrace();
      result = null;
    }
    
    return result;
  }
  








  public static Object read(Reader r)
    throws Exception
  {
    Class[] deSerializeArgsClasses = new Class[1];
    Object[] deSerializeArgs = new Object[1];
    


    Class xstreamClass = Class.forName("com.thoughtworks.xstream.XStream");
    Constructor constructor = xstreamClass.getConstructor(new Class[0]);
    Object xstream = constructor.newInstance(new Object[0]);
    
    deSerializeArgsClasses[0] = Reader.class;
    deSerializeArgs[0] = r;
    Method methodDeSerialize = xstreamClass.getMethod("fromXML", deSerializeArgsClasses);
    Object result;
    try
    {
      result = methodDeSerialize.invoke(xstream, deSerializeArgs);
    } catch (Exception ex) {
      ex.printStackTrace();
      result = null;
    }
    
    return result;
  }
  








  public static Object deSerialize(String xmlString)
    throws Exception
  {
    Class[] deSerializeArgsClasses = new Class[1];
    Object[] deSerializeArgs = new Object[1];
    


    Class xstreamClass = Class.forName("com.thoughtworks.xstream.XStream");
    Constructor constructor = xstreamClass.getConstructor(new Class[0]);
    Object xstream = constructor.newInstance(new Object[0]);
    
    deSerializeArgsClasses[0] = String.class;
    deSerializeArgs[0] = xmlString;
    Method methodDeSerialize = xstreamClass.getMethod("fromXML", deSerializeArgsClasses);
    Object result;
    try
    {
      result = methodDeSerialize.invoke(xstream, deSerializeArgs);
    } catch (Exception ex) {
      ex.printStackTrace();
      result = null;
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
  
  public XStream() {}
}

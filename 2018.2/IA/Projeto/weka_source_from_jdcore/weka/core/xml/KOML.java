package weka.core.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
































public class KOML
  implements RevisionHandler
{
  protected static boolean m_Present = false;
  
  public static final String FILE_EXTENSION = ".koml";
  

  static
  {
    checkForKOML();
  }
  

  private static void checkForKOML()
  {
    try
    {
      Class.forName("fr.dyade.koala.xml.koml.KOMLSerializer");
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
  




  public static Object read(String filename)
    throws Exception
  {
    return read(new FileInputStream(filename));
  }
  




  public static Object read(File file)
    throws Exception
  {
    return read(new FileInputStream(file));
  }
  

















  public static Object read(InputStream stream)
    throws Exception
  {
    Object result = null;
    

    Class komlClass = Class.forName("fr.dyade.koala.xml.koml.KOMLDeserializer");
    Class[] komlClassArgs = new Class[2];
    komlClassArgs[0] = InputStream.class;
    komlClassArgs[1] = Boolean.TYPE;
    Object[] komlArgs = new Object[2];
    komlArgs[0] = stream;
    komlArgs[1] = new Boolean(false);
    Constructor constructor = komlClass.getConstructor(komlClassArgs);
    Object koml = constructor.newInstance(komlArgs);
    Class[] readArgsClasses = new Class[0];
    Method methodRead = komlClass.getMethod("readObject", readArgsClasses);
    Object[] readArgs = new Object[0];
    Class[] closeArgsClasses = new Class[0];
    Method methodClose = komlClass.getMethod("close", closeArgsClasses);
    Object[] closeArgs = new Object[0];
    
    try
    {
      result = methodRead.invoke(koml, readArgs);
    }
    catch (Exception e) {
      result = null;
    }
    finally {
      methodClose.invoke(koml, closeArgs);
    }
    
    return result;
  }
  





  public static boolean write(String filename, Object o)
    throws Exception
  {
    return write(new FileOutputStream(filename), o);
  }
  





  public static boolean write(File file, Object o)
    throws Exception
  {
    return write(new FileOutputStream(file), o);
  }
  


















  public static boolean write(OutputStream stream, Object o)
    throws Exception
  {
    boolean result = false;
    

    Class komlClass = Class.forName("fr.dyade.koala.xml.koml.KOMLSerializer");
    Class[] komlClassArgs = new Class[2];
    komlClassArgs[0] = OutputStream.class;
    komlClassArgs[1] = Boolean.TYPE;
    Object[] komlArgs = new Object[2];
    komlArgs[0] = stream;
    komlArgs[1] = new Boolean(false);
    Constructor constructor = komlClass.getConstructor(komlClassArgs);
    Object koml = constructor.newInstance(komlArgs);
    Class[] addArgsClasses = new Class[1];
    addArgsClasses[0] = Object.class;
    Method methodAdd = komlClass.getMethod("addObject", addArgsClasses);
    Object[] addArgs = new Object[1];
    addArgs[0] = o;
    Class[] closeArgsClasses = new Class[0];
    Method methodClose = komlClass.getMethod("close", closeArgsClasses);
    Object[] closeArgs = new Object[0];
    
    try
    {
      methodAdd.invoke(koml, addArgs);
      result = true;
    }
    catch (Exception e) {
      result = false;
    }
    finally {
      methodClose.invoke(koml, closeArgs);
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  
  public KOML() {}
}

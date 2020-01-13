package weka.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;



































public class SerializationHelper
  implements RevisionHandler
{
  public static final String SERIAL_VERSION_UID = "serialVersionUID";
  
  public SerializationHelper() {}
  
  public static boolean isSerializable(String classname)
  {
    boolean result;
    try
    {
      result = isSerializable(Class.forName(classname));
    }
    catch (Exception e) {
      result = false;
    }
    
    return result;
  }
  






  public static boolean isSerializable(Class c)
  {
    return ClassDiscovery.hasInterface(Serializable.class, c);
  }
  




  public static boolean hasUID(String classname)
  {
    boolean result;
    


    try
    {
      result = hasUID(Class.forName(classname));
    }
    catch (Exception e) {
      result = false;
    }
    
    return result;
  }
  









  public static boolean hasUID(Class c)
  {
    boolean result = false;
    
    if (isSerializable(c)) {
      try {
        c.getDeclaredField("serialVersionUID");
        result = true;
      }
      catch (Exception e) {
        result = false;
      }
    }
    
    return result;
  }
  




  public static boolean needsUID(String classname)
  {
    boolean result;
    



    try
    {
      result = needsUID(Class.forName(classname));
    }
    catch (Exception e) {
      result = false;
    }
    
    return result;
  }
  



  public static boolean needsUID(Class c)
  {
    boolean result;
    

    boolean result;
    

    if (isSerializable(c)) {
      result = !hasUID(c);
    } else {
      result = false;
    }
    return result;
  }
  



  public static long getUID(String classname)
  {
    long result;
    


    try
    {
      result = getUID(Class.forName(classname));
    }
    catch (Exception e) {
      result = 0L;
    }
    
    return result;
  }
  





  public static long getUID(Class c)
  {
    return ObjectStreamClass.lookup(c).getSerialVersionUID();
  }
  





  public static void write(String filename, Object o)
    throws Exception
  {
    write(new FileOutputStream(filename), o);
  }
  







  public static void write(OutputStream stream, Object o)
    throws Exception
  {
    if (!(stream instanceof BufferedOutputStream)) {
      stream = new BufferedOutputStream(stream);
    }
    ObjectOutputStream oos = new ObjectOutputStream(stream);
    oos.writeObject(o);
    oos.flush();
    oos.close();
  }
  





  public static void writeAll(String filename, Object[] o)
    throws Exception
  {
    writeAll(new FileOutputStream(filename), o);
  }
  








  public static void writeAll(OutputStream stream, Object[] o)
    throws Exception
  {
    if (!(stream instanceof BufferedOutputStream)) {
      stream = new BufferedOutputStream(stream);
    }
    ObjectOutputStream oos = new ObjectOutputStream(stream);
    for (int i = 0; i < o.length; i++)
      oos.writeObject(o[i]);
    oos.flush();
    oos.close();
  }
  





  public static Object read(String filename)
    throws Exception
  {
    return read(new FileInputStream(filename));
  }
  








  public static Object read(InputStream stream)
    throws Exception
  {
    if (!(stream instanceof BufferedInputStream)) {
      stream = new BufferedInputStream(stream);
    }
    ObjectInputStream ois = new ObjectInputStream(stream);
    Object result = ois.readObject();
    ois.close();
    
    return result;
  }
  





  public static Object[] readAll(String filename)
    throws Exception
  {
    return readAll(new FileInputStream(filename));
  }
  








  public static Object[] readAll(InputStream stream)
    throws Exception
  {
    if (!(stream instanceof BufferedInputStream)) {
      stream = new BufferedInputStream(stream);
    }
    ObjectInputStream ois = new ObjectInputStream(stream);
    Vector<Object> result = new Vector();
    try {
      for (;;) {
        result.add(ois.readObject());
      }
      
    }
    catch (IOException e)
    {
      ois.close();
    }
    return result.toArray(new Object[result.size()]);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8597 $");
  }
  





  public static void main(String[] args)
    throws Exception
  {
    if (args.length == 0) {
      System.out.println("\nUsage: " + SerializationHelper.class.getName() + " classname [classname [classname [...]]]\n");
      System.exit(1);
    }
    

    System.out.println();
    for (int i = 0; i < args.length; i++) {
      System.out.println(args[i]);
      System.out.println("- is serializable: " + isSerializable(args[i]));
      System.out.println("- has serialVersionUID: " + hasUID(args[i]));
      System.out.println("- needs serialVersionUID: " + needsUID(args[i]));
      System.out.println("- serialVersionUID: private static final long serialVersionUID = " + getUID(args[i]) + "L;");
      System.out.println();
    }
  }
}

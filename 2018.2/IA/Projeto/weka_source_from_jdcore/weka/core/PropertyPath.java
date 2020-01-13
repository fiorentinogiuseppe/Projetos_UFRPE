package weka.core;

import java.beans.PropertyDescriptor;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;



















































public class PropertyPath
  implements RevisionHandler
{
  public PropertyPath() {}
  
  public static class PathElement
    implements Cloneable, RevisionHandler
  {
    protected String m_Name;
    protected int m_Index;
    
    public PathElement(String property)
    {
      if (property.indexOf("[") > -1) {
        m_Name = property.replaceAll("\\[.*$", "");
        m_Index = Integer.parseInt(property.replaceAll(".*\\[", "").replaceAll("\\].*", ""));
      }
      else
      {
        m_Name = property;
        m_Index = -1;
      }
    }
    




    public Object clone()
    {
      return new PathElement(toString());
    }
    




    public String getName()
    {
      return m_Name;
    }
    




    public boolean hasIndex()
    {
      return getIndex() > -1;
    }
    





    public int getIndex()
    {
      return m_Index;
    }
    






    public String toString()
    {
      String result = getName();
      if (hasIndex()) {
        result = result + "[" + getIndex() + "]";
      }
      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 4742 $");
    }
  }
  






  public static class Path
    implements RevisionHandler
  {
    protected Vector m_Elements;
    





    protected Path()
    {
      m_Elements = new Vector();
    }
    




    public Path(String path)
    {
      this();
      
      m_Elements = breakUp(path);
    }
    




    public Path(Vector elements)
    {
      this();
      
      for (int i = 0; i < elements.size(); i++) {
        m_Elements.add(((PropertyPath.PathElement)elements.get(i)).clone());
      }
    }
    



    public Path(String[] elements)
    {
      this();
      
      for (int i = 0; i < elements.length; i++) {
        m_Elements.add(new PropertyPath.PathElement(elements[i]));
      }
    }
    







    protected Vector breakUp(String path)
    {
      Vector result = new Vector();
      
      StringTokenizer tok = new StringTokenizer(path, ".");
      while (tok.hasMoreTokens()) {
        result.add(new PropertyPath.PathElement(tok.nextToken()));
      }
      return result;
    }
    





    public PropertyPath.PathElement get(int index)
    {
      return (PropertyPath.PathElement)m_Elements.get(index);
    }
    




    public int size()
    {
      return m_Elements.size();
    }
    





    public static Path parsePath(String path)
    {
      return new Path(path);
    }
    






    public Path subpath(int startIndex)
    {
      return subpath(startIndex, size());
    }
    












    public Path subpath(int startIndex, int endIndex)
    {
      Vector list = new Vector();
      for (int i = startIndex; i < endIndex; i++) {
        list.add(get(i));
      }
      return new Path(list);
    }
    







    public String toString()
    {
      String result = "";
      
      for (int i = 0; i < m_Elements.size(); i++) {
        if (i > 0)
          result = result + ".";
        result = result + m_Elements.get(i);
      }
      
      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 4742 $");
    }
  }
  






  protected static class PropertyContainer
    implements RevisionHandler
  {
    protected PropertyDescriptor m_Descriptor;
    




    protected Object m_Object;
    





    public PropertyContainer(PropertyDescriptor desc, Object obj)
    {
      m_Descriptor = desc;
      m_Object = obj;
    }
    




    public PropertyDescriptor getDescriptor()
    {
      return m_Descriptor;
    }
    




    public Object getObject()
    {
      return m_Object;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 4742 $");
    }
  }
  















  public static PropertyContainer find(Object src, Path path)
  {
    PathElement part = path.get(0);
    PropertyDescriptor desc;
    try { desc = new PropertyDescriptor(part.getName(), src.getClass());
    }
    catch (Exception e) {
      desc = null;
      e.printStackTrace();
    }
    

    if (desc == null)
      return null;
    PropertyContainer result;
    PropertyContainer result;
    if (path.size() == 1) {
      result = new PropertyContainer(desc, src);
    }
    else {
      try
      {
        Method method = desc.getReadMethod();
        Object methodResult = method.invoke(src, (Object[])null);
        Object newSrc; Object newSrc; if (part.hasIndex()) {
          newSrc = Array.get(methodResult, part.getIndex());
        } else
          newSrc = methodResult;
        result = find(newSrc, path.subpath(1));
      }
      catch (Exception e) {
        result = null;
        e.printStackTrace();
      }
    }
    
    return result;
  }
  









  public static PropertyDescriptor getPropertyDescriptor(Object src, Path path)
  {
    PropertyContainer cont = find(src, path);
    
    if (cont == null) {
      return null;
    }
    return cont.getDescriptor();
  }
  






  public static PropertyDescriptor getPropertyDescriptor(Object src, String path)
  {
    return getPropertyDescriptor(src, new Path(path));
  }
  












  public static Object getValue(Object src, Path path)
  {
    Object result = null;
    
    PropertyContainer cont = find(src, path);
    
    if (cont == null) {
      return null;
    }
    try
    {
      PathElement part = path.get(path.size() - 1);
      Method method = cont.getDescriptor().getReadMethod();
      Object methodResult = method.invoke(cont.getObject(), (Object[])null);
      if (part.hasIndex()) {
        result = Array.get(methodResult, part.getIndex());
      } else {
        result = methodResult;
      }
    } catch (Exception e) {
      result = null;
      e.printStackTrace();
    }
    
    return result;
  }
  






  public static Object getValue(Object src, String path)
  {
    return getValue(src, new Path(path));
  }
  














  public static boolean setValue(Object src, Path path, Object value)
  {
    boolean result = false;
    
    PropertyContainer cont = find(src, path);
    
    if (cont == null) {
      return result;
    }
    try
    {
      PathElement part = path.get(path.size() - 1);
      Method methodRead = cont.getDescriptor().getReadMethod();
      Method methodWrite = cont.getDescriptor().getWriteMethod();
      if (part.hasIndex()) {
        Object methodResult = methodRead.invoke(cont.getObject(), (Object[])null);
        Array.set(methodResult, part.getIndex(), value);
        methodWrite.invoke(cont.getObject(), new Object[] { methodResult });
      }
      else {
        methodWrite.invoke(cont.getObject(), new Object[] { value });
      }
      result = true;
    }
    catch (Exception e) {
      result = false;
      e.printStackTrace();
    }
    
    return result;
  }
  






  public static void setValue(Object src, String path, Object value)
  {
    setValue(src, new Path(path), value);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4742 $");
  }
  





  public static void main(String[] args)
    throws Exception
  {
    Path path = new Path("hello.world[2].nothing");
    System.out.println("Path: " + path);
    System.out.println(" -size: " + path.size());
    System.out.println(" -elements:");
    for (int i = 0; i < path.size(); i++) {
      System.out.println("  " + i + ". " + path.get(i).getName() + " -> " + path.get(i).getIndex());
    }
  }
}

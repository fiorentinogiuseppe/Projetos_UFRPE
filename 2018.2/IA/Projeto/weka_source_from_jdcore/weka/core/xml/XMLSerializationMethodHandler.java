package weka.core.xml;

import java.lang.reflect.Method;
import org.w3c.dom.Element;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;









































public class XMLSerializationMethodHandler
  implements RevisionHandler
{
  protected MethodHandler m_ReadMethods = null;
  

  protected MethodHandler m_WriteMethods = null;
  

  protected Object owner = null;
  








  public XMLSerializationMethodHandler(Object owner)
    throws Exception
  {
    this.owner = owner;
    m_ReadMethods = new MethodHandler();
    m_WriteMethods = new MethodHandler();
    
    clear();
  }
  












  protected void addMethods(MethodHandler handler, Method template, Method[] methods)
  {
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      

      if (!template.equals(method))
      {



        if (template.getReturnType().equals(method.getReturnType()))
        {


          if (template.getParameterTypes().length == method.getParameterTypes().length)
          {

            boolean equal = true;
            for (int n = 0; n < template.getParameterTypes().length; n++) {
              if (!template.getParameterTypes()[n].equals(method.getParameterTypes()[n])) {
                equal = false;
                break;
              }
            }
            

            if (equal) {
              String name = method.getName();
              name = name.replaceAll("read|write", "");
              name = name.substring(0, 1).toLowerCase() + name.substring(1);
              handler.add(name, method);
            }
          }
        }
      }
    }
  }
  









  protected void addMethods()
    throws Exception
  {
    Class[] params = new Class[1];
    params[0] = Element.class;
    Method method = owner.getClass().getMethod("readFromXML", params);
    addMethods(m_ReadMethods, method, owner.getClass().getMethods());
    

    params = new Class[3];
    params[0] = Element.class;
    params[1] = Object.class;
    params[2] = String.class;
    method = owner.getClass().getMethod("writeToXML", params);
    addMethods(m_WriteMethods, method, owner.getClass().getMethods());
  }
  












  public static Method findReadMethod(Object o, String name)
  {
    Method result = null;
    
    Class[] params = new Class[1];
    params[0] = Element.class;
    try {
      result = o.getClass().getMethod(name, params);
    }
    catch (Exception e) {
      result = null;
    }
    
    return result;
  }
  












  public static Method findWriteMethod(Object o, String name)
  {
    Method result = null;
    
    Class[] params = new Class[3];
    params[0] = Element.class;
    params[1] = Object.class;
    params[2] = String.class;
    try {
      result = o.getClass().getMethod(name, params);
    }
    catch (Exception e) {
      result = null;
    }
    
    return result;
  }
  



  public void clear()
  {
    m_ReadMethods.clear();
    m_WriteMethods.clear();
    try
    {
      addMethods();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public MethodHandler read()
  {
    return m_ReadMethods;
  }
  




  public MethodHandler write()
  {
    return m_WriteMethods;
  }
  







  public void register(Object handler, Class cls, String name)
  {
    read().add(cls, findReadMethod(handler, "read" + name));
    write().add(cls, findWriteMethod(handler, "write" + name));
  }
  




  public String toString()
  {
    return "Read Methods:\n" + read() + "\n\n" + "Write Methods:\n" + write();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

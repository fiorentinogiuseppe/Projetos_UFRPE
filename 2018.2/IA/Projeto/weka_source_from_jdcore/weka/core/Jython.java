package weka.core;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;


































public class Jython
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -6972298704460209252L;
  public static final String CLASS_PYTHONINERPRETER = "org.python.util.PythonInterpreter";
  public static final String CLASS_PYTHONOBJECTINPUTSTREAM = "org.python.util.PythonObjectInputStream";
  protected static boolean m_Present = false;
  protected Object m_Interpreter;
  
  static { try { Class.forName("org.python.util.PythonInterpreter");
      m_Present = true;
    }
    catch (Exception e) {
      m_Present = false;
    }
  }
  





  public Jython()
  {
    m_Interpreter = newInterpreter();
  }
  




  public Object getInterpreter()
  {
    return m_Interpreter;
  }
  












  public Object invoke(String methodName, Class[] paramClasses, Object[] paramValues)
  {
    Object result = null;
    if (getInterpreter() != null) {
      result = invoke(getInterpreter(), methodName, paramClasses, paramValues);
    }
    return result;
  }
  





  public static boolean isPresent()
  {
    return m_Present;
  }
  






  public static Object newInterpreter()
  {
    Object result = null;
    
    if (isPresent()) {
      try {
        result = Class.forName("org.python.util.PythonInterpreter").newInstance();
      }
      catch (Exception e) {
        e.printStackTrace();
        result = null;
      }
    }
    
    return result;
  }
  







  public static Object newInstance(File file, Class template)
  {
    return newInstance(file, template, new File[0]);
  }
  




















  public static Object newInstance(File file, Class template, File[] paths)
  {
    Object result = null;
    
    if (!isPresent()) {
      return result;
    }
    Object interpreter = newInterpreter();
    if (interpreter == null) {
      return result;
    }
    
    if (paths.length > 0) {
      invoke(interpreter, "exec", new Class[] { String.class }, new Object[] { "import sys" });
      

      String instanceName = "syspath";
      invoke(interpreter, "exec", new Class[] { String.class }, new Object[] { instanceName + " = sys.path" });
      HashSet<String> currentPaths = new HashSet();
      try {
        String[] tmpPaths = (String[])invoke(interpreter, "get", new Class[] { String.class, Class.class }, new Object[] { instanceName, [Ljava.lang.String.class });
        for (i = 0; i < tmpPaths.length; i++) {
          currentPaths.add(tmpPaths[i]);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      

      for (int i = 0; i < paths.length; i++) {
        if (!currentPaths.contains(paths[i].getAbsolutePath())) {
          invoke(interpreter, "exec", new Class[] { String.class }, new Object[] { "sys.path.append('" + paths[i].getAbsolutePath() + "')" });
        }
      }
    }
    
    String filename = file.getAbsolutePath();
    invoke(interpreter, "execfile", new Class[] { String.class }, new Object[] { filename });
    String tempName = filename.substring(filename.lastIndexOf("/") + 1);
    tempName = tempName.substring(0, tempName.indexOf("."));
    String instanceName = tempName.toLowerCase();
    String javaClassName = tempName.substring(0, 1).toUpperCase() + tempName.substring(1);
    String objectDef = "=" + javaClassName + "()";
    invoke(interpreter, "exec", new Class[] { String.class }, new Object[] { instanceName + objectDef });
    try {
      result = invoke(interpreter, "get", new Class[] { String.class, Class.class }, new Object[] { instanceName, template });
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    
    return result;
  }
  












  public static Object invoke(Object o, String methodName, Class[] paramClasses, Object[] paramValues)
  {
    Object result = null;
    try
    {
      Method m = o.getClass().getMethod(methodName, paramClasses);
      result = m.invoke(o, paramValues);
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  












  public static Object deserialize(InputStream in)
  {
    Object result = null;
    try
    {
      Class cls = Class.forName("org.python.util.PythonObjectInputStream");
      Class[] paramTypes = { InputStream.class };
      Constructor constr = cls.getConstructor(paramTypes);
      Object[] arglist = { in };
      Object obj = constr.newInstance(arglist);
      result = invoke(obj, "readObject", new Class[0], new Object[0]);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
  





  public static void main(String[] args)
  {
    if (args.length == 0) {
      System.out.println("Jython present: " + isPresent());
    }
    else {
      Jython jython = new Jython();
      if (jython.getInterpreter() == null) {
        System.err.println("Cannot instantiate Python Interpreter!");
      } else {
        jython.invoke("execfile", new Class[] { String.class }, new Object[] { args[0] });
      }
    }
  }
}

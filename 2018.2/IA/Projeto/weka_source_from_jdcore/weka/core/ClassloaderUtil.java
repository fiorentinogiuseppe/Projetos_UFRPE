package weka.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;





























public class ClassloaderUtil
  implements RevisionHandler
{
  private static final Class[] parameters = { URL.class };
  

  public ClassloaderUtil() {}
  
  public static void addFile(String s)
    throws IOException
  {
    File f = new File(s);
    addFile(f);
  }
  



  public static void addFile(File f)
    throws IOException
  {
    addURL(f.toURL());
  }
  



  public static void addURL(URL u)
    throws IOException
  {
    ClassloaderUtil clu = new ClassloaderUtil();
    
    URLClassLoader sysLoader = (URLClassLoader)clu.getClass().getClassLoader();
    URL[] urls = sysLoader.getURLs();
    for (int i = 0; i < urls.length; i++) {
      if (urls[i].toString().toLowerCase().equals(u.toString().toLowerCase())) {
        System.err.println("URL " + u + " is already in the CLASSPATH");
        return;
      }
    }
    Class sysclass = URLClassLoader.class;
    try {
      Method method = sysclass.getDeclaredMethod("addURL", parameters);
      method.setAccessible(true);
      method.invoke(sysLoader, new Object[] { u });
    } catch (Throwable t) {
      t.printStackTrace();
      throw new IOException("Error, could not add URL to system classloader");
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
}

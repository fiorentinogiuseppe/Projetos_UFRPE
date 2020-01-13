package weka.core;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;











































public class ClassDiscovery
  implements RevisionHandler
{
  public static final boolean VERBOSE = false;
  protected static Hashtable<String, Vector> m_Cache;
  
  public ClassDiscovery() {}
  
  public static boolean isSubclass(String superclass, String otherclass)
  {
    try
    {
      return isSubclass(Class.forName(superclass), Class.forName(otherclass));
    }
    catch (Exception e) {}
    return false;
  }
  











  public static boolean isSubclass(Class superclass, Class otherclass)
  {
    boolean result = false;
    Class currentclass = otherclass;
    do {
      result = currentclass.equals(superclass);
      

      if (currentclass.equals(Object.class)) {
        break;
      }
      if (!result) {
        currentclass = currentclass.getSuperclass();
      }
    } while (!result);
    
    return result;
  }
  





  public static boolean hasInterface(String intf, String cls)
  {
    try
    {
      return hasInterface(Class.forName(intf), Class.forName(cls));
    }
    catch (Exception e) {}
    return false;
  }
  












  public static boolean hasInterface(Class intf, Class cls)
  {
    boolean result = false;
    Class currentclass = cls;
    do
    {
      Class[] intfs = currentclass.getInterfaces();
      for (int i = 0; i < intfs.length; i++) {
        if (intfs[i].equals(intf)) {
          result = true;
          break;
        }
      }
      

      if (!result) {
        currentclass = currentclass.getSuperclass();
        

        if ((currentclass == null) || (currentclass.equals(Object.class))) {
          break;
        }
      }
    } while (!result);
    
    return result;
  }
  















  protected static URL getURL(String classpathPart, String pkgname)
  {
    URL result = null;
    String urlStr = null;
    try
    {
      File classpathFile = new File(classpathPart);
      

      if (classpathFile.isDirectory())
      {
        File file = new File(classpathPart + pkgname);
        if (file.exists()) {
          urlStr = "file:" + classpathPart + pkgname;
        }
      }
      else {
        JarFile jarfile = new JarFile(classpathPart);
        Enumeration enm = jarfile.entries();
        String pkgnameTmp = pkgname.substring(1);
        while (enm.hasMoreElements()) {
          if (enm.nextElement().toString().startsWith(pkgnameTmp)) {
            urlStr = "jar:file:" + classpathPart + "!" + pkgname;
          }
        }
      }
    }
    catch (Exception e) {}
    




    if (urlStr != null) {
      try {
        result = new URL(urlStr);
      }
      catch (Exception e) {
        System.err.println("Trying to create URL from '" + urlStr + "' generates this exception:\n" + e);
        

        result = null;
      }
    }
    
    return result;
  }
  










  public static Vector find(String classname, String[] pkgnames)
  {
    Vector result = new Vector();
    try
    {
      Class cls = Class.forName(classname);
      result = find(cls, pkgnames);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result;
  }
  










  public static Vector find(String classname, String pkgname)
  {
    Vector result = new Vector();
    try
    {
      Class cls = Class.forName(classname);
      result = find(cls, pkgname);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result;
  }
  











  public static Vector find(Class cls, String[] pkgnames)
  {
    Vector result = new Vector();
    
    HashSet names = new HashSet();
    for (int i = 0; i < pkgnames.length; i++) {
      names.addAll(find(cls, pkgnames[i]));
    }
    
    result.addAll(names);
    Collections.sort(result, new StringCompare());
    
    return result;
  }
  























  public static Vector find(Class cls, String pkgname)
  {
    Vector result = getCache(cls, pkgname);
    
    if (result == null) {
      result = new Vector();
      





      String pkgpath = pkgname.replaceAll("\\.", "/");
      


      StringTokenizer tok = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
      


      while (tok.hasMoreTokens()) {
        String part = tok.nextToken();
        



        URL url = getURL(part, "/" + pkgpath);
        





        if (url != null)
        {


          File dir = new File(part + "/" + pkgpath);
          String classname; if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
              if ((files[i].isFile()) && (files[i].getName().endsWith(".class")))
              {
                try
                {

                  classname = pkgname + "." + files[i].getName().replaceAll(".*/", "").replaceAll("\\.class", "");
                  

                  result.add(classname);
                }
                catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
          } else {
            try {
              JarFile jar = new JarFile(part);
              Enumeration enm = jar.entries();
              while (enm.hasMoreElements()) {
                JarEntry entry = (JarEntry)enm.nextElement();
                

                if ((!entry.isDirectory()) && (entry.getName().endsWith(".class")))
                {


                  classname = entry.getName().replaceAll("\\.class", "");
                  

                  if ((classname.startsWith(pkgpath)) && 
                  


                    (classname.substring(pkgpath.length() + 1).indexOf("/") <= -1))
                  {

                    result.add(classname.replaceAll("/", ".")); }
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
      
      int i = 0;
      while (i < result.size()) {
        try {
          Class clsNew = Class.forName((String)result.get(i));
          

          if (Modifier.isAbstract(clsNew.getModifiers())) {
            result.remove(i);
          }
          else if ((cls.isInterface()) && (!hasInterface(cls, clsNew))) {
            result.remove(i);
          }
          else if ((!cls.isInterface()) && (!isSubclass(cls, clsNew))) {
            result.remove(i);
          } else {
            i++;
          }
        } catch (Throwable e) {
          System.err.println("Checking class: " + result.get(i));
          e.printStackTrace();
          result.remove(i);
        }
      }
      

      Collections.sort(result, new StringCompare());
      

      addCache(cls, pkgname, result);
    }
    
    return result;
  }
  




  protected static HashSet getSubDirectories(String prefix, File dir, HashSet list)
  {
    String newPrefix;
    


    String newPrefix;
    


    if (prefix == null) {
      newPrefix = ""; } else { String newPrefix;
      if (prefix.length() == 0) {
        newPrefix = dir.getName();
      } else
        newPrefix = prefix + "." + dir.getName();
    }
    if (newPrefix.length() != 0) {
      list.add(newPrefix);
    }
    
    File[] files = dir.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          list = getSubDirectories(newPrefix, files[i], list);
        }
      }
    }
    return list;
  }
  













  public static Vector findPackages()
  {
    Vector result = new Vector();
    HashSet set = new HashSet();
    


    StringTokenizer tok = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
    


    while (tok.hasMoreTokens()) {
      String part = tok.nextToken();
      



      File file = new File(part);
      if (file.isDirectory()) {
        set = getSubDirectories(null, file, set);
      }
      else if (file.exists()) {
        try {
          JarFile jar = new JarFile(part);
          Enumeration enm = jar.entries();
          while (enm.hasMoreElements()) {
            JarEntry entry = (JarEntry)enm.nextElement();
            

            if (entry.isDirectory()) {
              set.add(entry.getName().replaceAll("/", ".").replaceAll("\\.$", ""));
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    

    set.remove("META-INF");
    result.addAll(set);
    Collections.sort(result, new StringCompare());
    
    return result;
  }
  


  protected static void initCache()
  {
    if (m_Cache == null) {
      m_Cache = new Hashtable();
    }
  }
  





  protected static void addCache(Class cls, String pkgname, Vector classnames)
  {
    initCache();
    m_Cache.put(cls.getName() + "-" + pkgname, classnames);
  }
  







  protected static Vector getCache(Class cls, String pkgname)
  {
    initCache();
    return (Vector)m_Cache.get(cls.getName() + "-" + pkgname);
  }
  


  public static void clearCache()
  {
    initCache();
    m_Cache.clear();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5377 $");
  }
  






  public static void main(String[] args)
  {
    Vector list;
    





    int i;
    




    if ((args.length == 1) && (args[0].equals("packages"))) {
      list = findPackages();
      for (i = 0; i < list.size();) {
        System.out.println(list.get(i));i++; continue;
        
        if (args.length == 2)
        {
          Vector packages = new Vector();
          StringTokenizer tok = new StringTokenizer(args[1], ",");
          while (tok.hasMoreTokens()) {
            packages.add(tok.nextToken());
          }
          
          Vector list = find(args[0], (String[])packages.toArray(new String[packages.size()]));
          



          System.out.println("Searching for '" + args[0] + "' in '" + args[1] + "':\n" + "  " + list.size() + " found.");
          

          for (int i = 0; i < list.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + list.get(i));
          }
        }
        System.out.println("\nUsage:");
        System.out.println(ClassDiscovery.class.getName() + " packages");
        
        System.out.println("\tlists all packages in the classpath");
        System.out.println(ClassDiscovery.class.getName() + " <classname> <packagename(s)>");
        
        System.out.println("\tlists classes derived from/implementing 'classname' that");
        System.out.println("\tcan be found in 'packagename(s)' (comma-separated list");
        System.out.println();
        System.exit(1);
      }
    }
  }
  






  public static class StringCompare
    implements Comparator, RevisionHandler
  {
    public StringCompare() {}
    






    private String fillUp(String s, int len)
    {
      while (s.length() < len)
        s = s + " ";
      return s;
    }
    







    private int charGroup(char c)
    {
      int result = 0;
      
      if ((c >= 'a') && (c <= 'z')) {
        result = 2;
      } else if ((c >= '0') && (c <= '9')) {
        result = 1;
      }
      return result;
    }
    













    public int compare(Object o1, Object o2)
    {
      int result = 0;
      

      String s1 = o1.toString().toLowerCase();
      String s2 = o2.toString().toLowerCase();
      

      s1 = fillUp(s1, s2.length());
      s2 = fillUp(s2, s1.length());
      
      for (int i = 0; i < s1.length(); i++)
      {
        if (s1.charAt(i) == s2.charAt(i)) {
          result = 0;
        }
        else {
          int v1 = charGroup(s1.charAt(i));
          int v2 = charGroup(s2.charAt(i));
          

          if (v1 != v2) {
            if (v1 < v2) {
              result = -1; break;
            }
            result = 1; break;
          }
          
          if (s1.charAt(i) < s2.charAt(i)) {
            result = -1; break;
          }
          result = 1;
          

          break;
        }
      }
      
      return result;
    }
    





    public boolean equals(Object obj)
    {
      return obj instanceof StringCompare;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5377 $");
    }
  }
}

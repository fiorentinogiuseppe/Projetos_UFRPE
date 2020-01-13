package weka.gui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

































public class Loader
{
  private String dir;
  
  public Loader(String dir)
  {
    this.dir = dir;
  }
  


  public String getDir()
  {
    return dir;
  }
  



  public String processFilename(String filename)
  {
    if (!filename.startsWith(getDir())) {
      filename = getDir() + filename;
    }
    return filename;
  }
  




  public static URL getURL(String dir, String filename)
  {
    Loader loader = new Loader(dir);
    return loader.getURL(filename);
  }
  


  public URL getURL(String filename)
  {
    filename = processFilename(filename);
    return Loader.class.getClassLoader().getResource(filename);
  }
  





  public static InputStream getInputStream(String dir, String filename)
  {
    Loader loader = new Loader(dir);
    return loader.getInputStream(filename);
  }
  


  public InputStream getInputStream(String filename)
  {
    filename = processFilename(filename);
    return Loader.class.getResourceAsStream(filename);
  }
  




  public static Reader getReader(String dir, String filename)
  {
    Loader loader = new Loader(dir);
    return loader.getReader(filename);
  }
  




  public Reader getReader(String filename)
  {
    InputStream in = getInputStream(filename);
    
    if (in == null) {
      return null;
    }
    return new InputStreamReader(in);
  }
}

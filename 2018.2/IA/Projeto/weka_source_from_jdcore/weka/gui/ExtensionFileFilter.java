package weka.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import javax.swing.filechooser.FileFilter;








































public class ExtensionFileFilter
  extends FileFilter
  implements FilenameFilter, Serializable
{
  protected String m_Description;
  protected String[] m_Extension;
  
  public ExtensionFileFilter(String extension, String description)
  {
    m_Extension = new String[1];
    m_Extension[0] = extension;
    m_Description = description;
  }
  






  public ExtensionFileFilter(String[] extensions, String description)
  {
    m_Extension = extensions;
    m_Description = description;
  }
  





  public String getDescription()
  {
    return m_Description;
  }
  




  public String[] getExtensions()
  {
    return (String[])m_Extension.clone();
  }
  







  public boolean accept(File file)
  {
    String name = file.getName().toLowerCase();
    if (file.isDirectory()) {
      return true;
    }
    for (int i = 0; i < m_Extension.length; i++) {
      if (name.endsWith(m_Extension[i])) {
        return true;
      }
    }
    return false;
  }
  







  public boolean accept(File dir, String name)
  {
    return accept(new File(dir, name));
  }
}

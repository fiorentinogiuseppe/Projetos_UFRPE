package weka.core.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;































public abstract class AbstractFileLoader
  extends AbstractLoader
  implements FileSourcedConverter, EnvironmentHandler
{
  protected String m_File = new File(System.getProperty("user.dir")).getAbsolutePath();
  

  protected transient Instances m_structure = null;
  

  protected File m_sourceFile = null;
  

  public static String FILE_EXTENSION_COMPRESSED = ".gz";
  

  protected boolean m_useRelativePath = false;
  

  protected transient Environment m_env;
  

  public AbstractFileLoader() {}
  

  public File retrieveFile()
  {
    return new File(m_File);
  }
  




  public void setFile(File file)
    throws IOException
  {
    m_structure = null;
    setRetrieval(0);
    

    setSource(file);
  }
  




  public void setEnvironment(Environment env)
  {
    m_env = env;
    


    try
    {
      reset();
    }
    catch (IOException ex) {}
  }
  




  public void reset()
    throws IOException
  {
    m_structure = null;
    setRetrieval(0);
  }
  





  public void setSource(File file)
    throws IOException
  {
    File original = file;
    m_structure = null;
    
    setRetrieval(0);
    
    if (file == null) {
      throw new IOException("Source file object is null!");
    }
    
    String fName = file.getPath();
    try {
      if (m_env == null) {
        m_env = Environment.getSystemWide();
      }
      fName = m_env.substitute(fName);
    }
    catch (Exception e) {}
    




    file = new File(fName);
    
    if (file.exists()) {
      if (file.getName().endsWith(getFileExtension() + FILE_EXTENSION_COMPRESSED)) {
        setSource(new GZIPInputStream(new FileInputStream(file)));
      } else {
        setSource(new FileInputStream(file));
      }
    }
    




    if (m_useRelativePath) {
      try {
        m_sourceFile = Utils.convertToRelativePath(original);
        m_File = m_sourceFile.getPath();
      }
      catch (Exception ex) {
        m_sourceFile = original;
        m_File = m_sourceFile.getPath();
      }
    } else {
      m_sourceFile = original;
      m_File = m_sourceFile.getPath();
    }
  }
  






























  public String useRelativePathTipText()
  {
    return "Use relative rather than absolute paths";
  }
  




  public void setUseRelativePath(boolean rp)
  {
    m_useRelativePath = rp;
  }
  




  public boolean getUseRelativePath()
  {
    return m_useRelativePath;
  }
  









  protected static String makeOptionStr(AbstractFileLoader loader)
  {
    StringBuffer result = new StringBuffer("\nUsage:\n");
    result.append("\t" + loader.getClass().getName().replaceAll(".*\\.", ""));
    if ((loader instanceof OptionHandler))
      result.append(" [options]");
    result.append(" <");
    String[] ext = loader.getFileExtensions();
    for (int i = 0; i < ext.length; i++) {
      if (i > 0)
        result.append(" | ");
      result.append("file" + ext[i]);
    }
    result.append(">\n");
    
    if ((loader instanceof OptionHandler)) {
      result.append("\nOptions:\n\n");
      Enumeration enm = ((OptionHandler)loader).listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        result.append(option.synopsis() + "\n");
        result.append(option.description() + "\n");
      }
    }
    
    return result.toString();
  }
  






  public static void runFileLoader(AbstractFileLoader loader, String[] options)
  {
    try
    {
      String[] tmpOptions = (String[])options.clone();
      if (Utils.getFlag('h', tmpOptions)) {
        System.err.println("\nHelp requested\n" + makeOptionStr(loader));
        return;
      }
    }
    catch (Exception e) {}
    


    if (options.length > 0) {
      if ((loader instanceof OptionHandler)) {
        try
        {
          ((OptionHandler)loader).setOptions(options);
          
          for (int i = 0; i < options.length; i++) {
            if (options[i].length() > 0) {
              options = new String[] { options[i] };
              break;
            }
          }
        }
        catch (Exception ex) {
          System.err.println(makeOptionStr(loader));
          System.exit(1);
        }
      }
      try
      {
        loader.setFile(new File(options[0]));
        
        if ((loader instanceof IncrementalConverter)) {
          Instances structure = loader.getStructure();
          System.out.println(structure);
          Instance temp;
          do {
            temp = loader.getNextInstance(structure);
            if (temp != null) {
              System.out.println(temp);
            }
          } while (temp != null);
        }
        else
        {
          System.out.println(loader.getDataSet());
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    else {
      System.err.println(makeOptionStr(loader));
    }
  }
}

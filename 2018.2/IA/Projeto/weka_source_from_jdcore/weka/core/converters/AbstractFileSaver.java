package weka.core.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;














































public abstract class AbstractFileSaver
  extends AbstractSaver
  implements OptionHandler, FileSourcedConverter, EnvironmentHandler
{
  private File m_outputFile;
  private transient BufferedWriter m_writer;
  private String FILE_EXTENSION;
  private String FILE_EXTENSION_COMPRESSED = ".gz";
  

  private String m_prefix;
  

  private String m_dir;
  

  protected int m_incrementalCounter;
  

  protected boolean m_useRelativePath = false;
  

  protected transient Environment m_env;
  


  public AbstractFileSaver() {}
  

  public void resetOptions()
  {
    super.resetOptions();
    m_outputFile = null;
    m_writer = null;
    m_prefix = "";
    m_dir = "";
    m_incrementalCounter = 0;
  }
  







  public BufferedWriter getWriter()
  {
    return m_writer;
  }
  

  public void resetWriter()
  {
    m_writer = null;
  }
  





  public String getFileExtension()
  {
    return FILE_EXTENSION;
  }
  




  public String[] getFileExtensions()
  {
    return new String[] { getFileExtension() };
  }
  






  protected void setFileExtension(String ext)
  {
    FILE_EXTENSION = ext;
  }
  






  public File retrieveFile()
  {
    return m_outputFile;
  }
  



  public void setFile(File outputFile)
    throws IOException
  {
    m_outputFile = outputFile;
    setDestination(outputFile);
  }
  





  public void setFilePrefix(String prefix)
  {
    m_prefix = prefix;
  }
  



  public String filePrefix()
  {
    return m_prefix;
  }
  



  public void setDir(String dir)
  {
    m_dir = dir;
  }
  



  public String retrieveDir()
  {
    return m_dir;
  }
  




  public void setEnvironment(Environment env)
  {
    m_env = env;
    if (m_outputFile != null) {
      try
      {
        setFile(m_outputFile);
      }
      catch (IOException ex) {}
    }
  }
  







  public Enumeration listOptions()
  {
    Vector<Option> newVector = new Vector();
    
    newVector.addElement(new Option("\tThe input file", "i", 1, "-i <the input file>"));
    


    newVector.addElement(new Option("\tThe output file", "o", 1, "-o <the output file>"));
    


    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    String outputString = Utils.getOption('o', options);
    String inputString = Utils.getOption('i', options);
    
    ArffLoader loader = new ArffLoader();
    
    resetOptions();
    
    if (inputString.length() != 0) {
      try {
        File input = new File(inputString);
        loader.setFile(input);
        setInstances(loader.getDataSet());
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new IOException("No data set loaded. Data set has to be in ARFF format.");
      }
    }
    if (outputString.length() != 0) {
      boolean validExt = false;
      for (String ext : getFileExtensions()) {
        if (outputString.endsWith(ext)) {
          validExt = true;
          break;
        }
      }
      
      if (!validExt) {
        if (outputString.lastIndexOf('.') != -1) {
          outputString = outputString.substring(0, outputString.lastIndexOf('.')) + FILE_EXTENSION;
        } else
          outputString = outputString + FILE_EXTENSION;
      }
      try {
        File output = new File(outputString);
        setFile(output);
      } catch (Exception ex) {
        throw new IOException("Cannot create output file (Reason: " + ex.toString() + "). Standard out is used.");
      }
    }
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (m_outputFile != null) {
      result.add("-o");
      result.add("" + m_outputFile);
    }
    
    if (getInstances() != null) {
      result.add("-i");
      result.add("" + getInstances().relationName());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  


  public void cancel()
  {
    if (getWriteMode() == 2) {
      if ((m_outputFile != null) && (m_outputFile.exists()) && 
        (m_outputFile.delete())) {
        System.out.println("File deleted.");
      }
      resetOptions();
    }
  }
  





  public void setDestination(File file)
    throws IOException
  {
    boolean success = false;
    String tempOut = file.getPath();
    try {
      if (m_env == null) {
        m_env = Environment.getSystemWide();
      }
      tempOut = m_env.substitute(tempOut);
    }
    catch (Exception ex) {}
    

    file = new File(tempOut);
    String out = file.getAbsolutePath();
    if (m_outputFile != null) {
      try {
        if ((file.exists()) && 
          (!file.delete())) {
          throw new IOException("File already exists.");
        }
        if (out.lastIndexOf(File.separatorChar) == -1) {
          success = file.createNewFile();
        }
        else {
          String outPath = out.substring(0, out.lastIndexOf(File.separatorChar));
          File dir = new File(outPath);
          if (dir.exists()) {
            success = file.createNewFile();
          } else {
            dir.mkdirs();
            success = file.createNewFile();
          }
        }
        if (success) {
          if (m_useRelativePath) {
            try {
              m_outputFile = Utils.convertToRelativePath(file);
            } catch (Exception e) {
              m_outputFile = file;
            }
          } else {
            m_outputFile = file;
          }
          setDestination(new FileOutputStream(m_outputFile));
        }
      } catch (Exception ex) {
        throw new IOException("Cannot create a new output file (Reason: " + ex.toString() + "). Standard out is used.");
      } finally {
        if (!success) {
          System.err.println("Cannot create a new output file. Standard out is used.");
          m_outputFile = null;
        }
      }
    }
  }
  




  public void setDestination(OutputStream output)
    throws IOException
  {
    m_writer = new BufferedWriter(new OutputStreamWriter(output));
  }
  





  public void setDirAndPrefix(String relationName, String add)
  {
    try
    {
      if (m_dir.equals("")) {
        setDir(System.getProperty("user.dir"));
      }
      if (m_prefix.equals("")) {
        if (relationName.length() == 0) {
          throw new IOException("[Saver] Empty filename!!");
        }
        String concat = m_dir + File.separator + relationName + add + FILE_EXTENSION;
        if ((!concat.toLowerCase().endsWith(FILE_EXTENSION)) && (!concat.toLowerCase().endsWith(FILE_EXTENSION + FILE_EXTENSION_COMPRESSED)))
        {
          concat = concat + FILE_EXTENSION;
        }
        setFile(new File(concat));
      } else {
        if (relationName.length() > 0) {
          relationName = "_" + relationName;
        }
        String concat = m_dir + File.separator + m_prefix + relationName + add;
        if ((!concat.toLowerCase().endsWith(FILE_EXTENSION)) && (!concat.toLowerCase().endsWith(FILE_EXTENSION + FILE_EXTENSION_COMPRESSED)))
        {
          concat = concat + FILE_EXTENSION;
        }
        setFile(new File(concat));
      }
    } catch (Exception ex) {
      System.err.println("File prefix and/or directory could not have been set.");
      ex.printStackTrace();
    }
  }
  





  public abstract String getFileDescription();
  




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
  









  protected static String makeOptionStr(AbstractFileSaver saver)
  {
    StringBuffer result = new StringBuffer();
    

    result.append("\n");
    result.append(saver.getClass().getName().replaceAll(".*\\.", ""));
    result.append(" options:\n\n");
    Enumeration enm = saver.listOptions();
    while (enm.hasMoreElements()) {
      Option option = (Option)enm.nextElement();
      result.append(option.synopsis() + "\n");
      result.append(option.description() + "\n");
    }
    
    return result.toString();
  }
  





  public static void runFileSaver(AbstractFileSaver saver, String[] options)
  {
    try
    {
      String[] tmpOptions = (String[])options.clone();
      if (Utils.getFlag('h', tmpOptions)) {
        System.err.println("\nHelp requested\n" + makeOptionStr(saver));
        return;
      }
    }
    catch (Exception e) {}
    

    try
    {
      try
      {
        saver.setOptions(options);
      }
      catch (Exception ex) {
        System.err.println(makeOptionStr(saver));
        System.exit(1);
      }
      
      saver.writeBatch();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

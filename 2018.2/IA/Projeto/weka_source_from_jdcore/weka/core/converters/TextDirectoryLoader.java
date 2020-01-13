package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;






























































public class TextDirectoryLoader
  extends AbstractLoader
  implements BatchConverter, OptionHandler
{
  private static final long serialVersionUID = 2592118773712247647L;
  protected Instances m_structure = null;
  

  protected File m_sourceFile = new File(System.getProperty("user.dir"));
  

  protected boolean m_Debug = false;
  

  protected boolean m_OutputFilename = false;
  




  protected String m_charSet = "";
  



  public TextDirectoryLoader()
  {
    setRetrieval(0);
  }
  





  public String globalInfo()
  {
    return "Loads all text files in a directory and uses the subdirectory names as class labels. The content of the text files will be stored in a String attribute, the filename can be stored as well.";
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.add(new Option("\tEnables debug output.\n\t(default: off)", "D", 0, "-D"));
    



    result.add(new Option("\tStores the filename in an additional attribute.\n\t(default: off)", "F", 0, "-F"));
    



    result.add(new Option("\tThe directory to work on.\n\t(default: current directory)", "dir", 0, "-dir <directory>"));
    



    result.add(new Option("\tThe character set to use, e.g UTF-8.\n\t(default: use the default character set)", "charset", 1, "-charset <charset name>"));
    


    return result.elements();
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag("D", options));
    
    setOutputFilename(Utils.getFlag("F", options));
    
    String dir = Utils.getOption("dir", options);
    if (dir.length() > 0) {
      setDirectory(new File(dir));
    }
    
    String charSet = Utils.getOption("charset", options);
    m_charSet = "";
    if (charSet.length() > 0) {
      m_charSet = charSet;
    }
  }
  




  public String[] getOptions()
  {
    Vector options = new Vector();
    
    if (getDebug()) {
      options.add("-D");
    }
    if (getOutputFilename()) {
      options.add("-F");
    }
    options.add("-dir");
    options.add(getDirectory().getAbsolutePath());
    
    if ((m_charSet != null) && (m_charSet.length() > 0)) {
      options.add("-charset");
      options.add(m_charSet);
    }
    
    return (String[])options.toArray(new String[options.size()]);
  }
  




  public String charSetTipText()
  {
    return "The character set to use when reading text files (eg UTF-8) - leave blank to use the default character set.";
  }
  






  public void setCharSet(String charSet)
  {
    m_charSet = charSet;
  }
  






  public String getCharSet()
  {
    return m_charSet;
  }
  




  public void setDebug(boolean value)
  {
    m_Debug = value;
  }
  




  public boolean getDebug()
  {
    return m_Debug;
  }
  




  public String debugTipText()
  {
    return "Whether to print additional debug information to the console.";
  }
  





  public void setOutputFilename(boolean value)
  {
    m_OutputFilename = value;
    reset();
  }
  




  public boolean getOutputFilename()
  {
    return m_OutputFilename;
  }
  




  public String outputFilenameTipText()
  {
    return "Whether to store the filename in an additional attribute.";
  }
  




  public String getFileDescription()
  {
    return "Directories";
  }
  




  public File getDirectory()
  {
    return new File(m_sourceFile.getAbsolutePath());
  }
  




  public void setDirectory(File dir)
    throws IOException
  {
    setSource(dir);
  }
  


  public void reset()
  {
    m_structure = null;
    setRetrieval(0);
  }
  





  public void setSource(File dir)
    throws IOException
  {
    reset();
    
    if (dir == null) {
      throw new IOException("Source directory object is null!");
    }
    
    m_sourceFile = dir;
    if ((!dir.exists()) || (!dir.isDirectory())) {
      throw new IOException("Directory '" + dir + "' not found");
    }
  }
  





  public Instances getStructure()
    throws IOException
  {
    if (getDirectory() == null) {
      throw new IOException("No directory/source has been specified");
    }
    

    if (m_structure == null) {
      String directoryPath = getDirectory().getAbsolutePath();
      FastVector atts = new FastVector();
      FastVector classes = new FastVector();
      
      File dir = new File(directoryPath);
      String[] subdirs = dir.list();
      
      for (int i = 0; i < subdirs.length; i++) {
        File subdir = new File(directoryPath + File.separator + subdirs[i]);
        if (subdir.isDirectory()) {
          classes.addElement(subdirs[i]);
        }
      }
      atts.addElement(new Attribute("text", (FastVector)null));
      if (m_OutputFilename) {
        atts.addElement(new Attribute("filename", (FastVector)null));
      }
      
      atts.addElement(new Attribute("@@class@@", classes));
      
      String relName = directoryPath.replaceAll("/", "_");
      relName = relName.replaceAll("\\\\", "_").replaceAll(":", "_");
      m_structure = new Instances(relName, atts, 0);
      m_structure.setClassIndex(m_structure.numAttributes() - 1);
    }
    
    return m_structure;
  }
  






  public Instances getDataSet()
    throws IOException
  {
    if (getDirectory() == null) {
      throw new IOException("No directory/source has been specified");
    }
    String directoryPath = getDirectory().getAbsolutePath();
    FastVector classes = new FastVector();
    Enumeration enm = getStructure().classAttribute().enumerateValues();
    while (enm.hasMoreElements()) {
      classes.addElement(enm.nextElement());
    }
    Instances data = getStructure();
    int fileCount = 0;
    for (int k = 0; k < classes.size(); k++) {
      String subdirPath = (String)classes.elementAt(k);
      File subdir = new File(directoryPath + File.separator + subdirPath);
      String[] files = subdir.list();
      for (int j = 0; j < files.length; j++) {
        try {
          fileCount++;
          if (getDebug()) {
            System.err.println("processing " + fileCount + " : " + subdirPath + " : " + files[j]);
          }
          
          double[] newInst = null;
          if (m_OutputFilename) {
            newInst = new double[3];
          } else
            newInst = new double[2];
          File txt = new File(directoryPath + File.separator + subdirPath + File.separator + files[j]);
          BufferedReader is;
          BufferedReader is; if ((m_charSet == null) || (m_charSet.length() == 0)) {
            is = new BufferedReader(new InputStreamReader(new FileInputStream(txt)));
          } else {
            is = new BufferedReader(new InputStreamReader(new FileInputStream(txt), m_charSet));
          }
          
          StringBuffer txtStr = new StringBuffer();
          int c;
          while ((c = is.read()) != -1) {
            txtStr.append((char)c);
          }
          
          newInst[0] = data.attribute(0).addStringValue(txtStr.toString());
          if (m_OutputFilename)
            newInst[1] = data.attribute(1).addStringValue(subdirPath + File.separator + files[j]);
          newInst[data.classIndex()] = k;
          data.add(new Instance(1.0D, newInst));
          is.close();
        }
        catch (Exception e) {
          System.err.println("failed to convert file: " + directoryPath + File.separator + subdirPath + File.separator + files[j]);
        }
      }
    }
    
    return data;
  }
  






  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    throw new IOException("TextDirectoryLoader can't read data sets incrementally.");
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11199 $");
  }
  




  public static void main(String[] args)
  {
    if (args.length > 0) {
      try {
        TextDirectoryLoader loader = new TextDirectoryLoader();
        loader.setOptions(args);
        System.out.println(loader.getDataSet());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      System.err.println("\nUsage:\n\tTextDirectoryLoader [options]\n\nOptions:\n");
      




      Enumeration enm = new TextDirectoryLoader().listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        System.err.println(option.synopsis());
        System.err.println(option.description());
      }
      
      System.err.println();
    }
  }
}

package weka.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;





























public abstract class Javadoc
  implements OptionHandler, RevisionHandler
{
  protected String[] m_StartTag = null;
  

  protected String[] m_EndTag = null;
  

  protected String m_Classname = Javadoc.class.getName();
  

  protected boolean m_UseStars = true;
  

  protected String m_Dir = "";
  

  protected boolean m_Silent = false;
  

  public Javadoc() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe class to load.", "W", 1, "-W <classname>"));
    


    result.addElement(new Option("\tSuppresses the '*' in the Javadoc.", "nostars", 0, "-nostars"));
    


    result.addElement(new Option("\tThe directory above the package hierarchy of the class.", "dir", 1, "-dir <dir>"));
    


    result.addElement(new Option("\tSuppresses printing in the console.", "silent", 0, "-silent"));
    


    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() > 0) {
      setClassname(tmpStr);
    } else {
      setClassname(getClass().getName());
    }
    setUseStars(!Utils.getFlag("nostars", options));
    
    setDir(Utils.getOption("dir", options));
    
    setSilent(Utils.getFlag("silent", options));
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-W");
    result.add(getClassname());
    
    if (!getUseStars()) {
      result.add("-nostars");
    }
    if (getDir().length() != 0) {
      result.add("-dir");
      result.add(getDir());
    }
    
    if (getSilent()) {
      result.add("-silent");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setClassname(String value)
  {
    m_Classname = value;
  }
  




  public String getClassname()
  {
    return m_Classname;
  }
  




  public void setUseStars(boolean value)
  {
    m_UseStars = value;
  }
  




  public boolean getUseStars()
  {
    return m_UseStars;
  }
  





  public void setDir(String value)
  {
    m_Dir = value;
  }
  





  public String getDir()
  {
    return m_Dir;
  }
  




  public void setSilent(boolean value)
  {
    m_Silent = value;
  }
  




  public boolean getSilent()
  {
    return m_Silent;
  }
  




  protected void println(Object o)
  {
    if (!getSilent()) {
      System.err.println(o.toString());
    }
  }
  







  protected boolean canInstantiateClass()
  {
    boolean result = true;
    Class cls = null;
    try
    {
      cls = Class.forName(getClassname());
    }
    catch (Exception e) {
      result = false;
      println("Cannot instantiate '" + getClassname() + "'! Class in CLASSPATH?");
    }
    
    if (result) {
      try {
        cls.newInstance();
      }
      catch (Exception e) {
        result = false;
        println("Cannot instantiate '" + getClassname() + "'! Missing default constructor?");
      }
    }
    
    return result;
  }
  







  protected Object getInstance()
  {
    Object result = null;
    try
    {
      Class cls = Class.forName(getClassname());
      result = cls.newInstance();
    }
    catch (Exception e) {
      result = null;
    }
    
    return result;
  }
  








  protected String toHTML(String s)
  {
    String result = s;
    
    result = result.replaceAll("&", "&amp;");
    result = result.replaceAll("<", "&lt;");
    result = result.replaceAll(">", "&gt;");
    result = result.replaceAll("@", "&#64;");
    result = result.replaceAll("\n", "<br/>\n");
    
    return result;
  }
  











  protected String indent(String content, int count, String indentStr)
  {
    StringTokenizer tok = new StringTokenizer(content, "\n", true);
    String result = "";
    while (tok.hasMoreTokens()) {
      if ((result.endsWith("\n")) || (result.length() == 0)) {
        for (int i = 0; i < count; i++)
          result = result + indentStr;
      }
      result = result + tok.nextToken();
    }
    
    return result;
  }
  







  protected abstract String generateJavadoc(int paramInt)
    throws Exception;
  






  protected String generateJavadoc()
    throws Exception
  {
    String result = "";
    
    for (int i = 0; i < m_StartTag.length; i++) {
      if (i > 0)
        result = result + "\n\n";
      result = result + generateJavadoc(i).trim();
    }
    
    return result;
  }
  



  protected String getIndentionString(String str)
  {
    String result;
    


    String result;
    

    if (str.replaceAll(" ", "").length() == 0) {
      result = " ";
    } else { String result;
      if (str.replaceAll("\t", "").length() == 0) {
        result = "\t";
      } else
        result = str;
    }
    return result;
  }
  



  protected int getIndentionLength(String str)
  {
    int result;
    

    int result;
    

    if (str.replaceAll(" ", "").length() == 0) {
      result = str.length();
    } else { int result;
      if (str.replaceAll("\t", "").length() == 0) {
        result = str.length();
      } else
        result = 1;
    }
    return result;
  }
  













  protected String updateJavadoc(String content, int index)
    throws Exception
  {
    if ((content.indexOf(m_StartTag[index]) == -1) || (content.indexOf(m_EndTag[index]) == -1))
    {
      println("No start and/or end tags found: " + m_StartTag[index] + "/" + m_EndTag[index]);
      

      return content;
    }
    

    StringBuffer resultBuf = new StringBuffer();
    while (content.length() > 0) {
      if (content.indexOf(m_StartTag[index]) > -1) {
        String part = content.substring(0, content.indexOf(m_StartTag[index]));
        
        if (part.endsWith("\"")) {
          resultBuf.append(part);
          resultBuf.append(m_StartTag[index]);
          content = content.substring(part.length() + m_StartTag[index].length());
        }
        else {
          String tmpStr = part.substring(part.lastIndexOf("\n") + 1);
          int indentionLen = getIndentionLength(tmpStr);
          String indentionStr = getIndentionString(tmpStr);
          part = part.substring(0, part.lastIndexOf("\n") + 1);
          resultBuf.append(part);
          resultBuf.append(indent(m_StartTag[index], indentionLen, indentionStr) + "\n");
          resultBuf.append(indent(generateJavadoc(index), indentionLen, indentionStr));
          resultBuf.append(indent(m_EndTag[index], indentionLen, indentionStr));
          content = content.substring(content.indexOf(m_EndTag[index]));
          content = content.substring(m_EndTag[index].length());
        }
      }
      else {
        resultBuf.append(content);
        content = "";
      }
    }
    
    return resultBuf.toString().trim();
  }
  








  protected String updateJavadoc(String content)
    throws Exception
  {
    String result = content;
    
    for (int i = 0; i < m_StartTag.length; i++) {
      result = updateJavadoc(result, i);
    }
    
    return result;
  }
  











  public String updateJavadoc()
    throws Exception
  {
    String result = "";
    

    File file = new File(getDir() + "/" + getClassname().replaceAll("\\.", "/") + ".java");
    if (!file.exists()) {
      println("File '" + file.getAbsolutePath() + "' doesn't exist!");
      return result;
    }
    
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      StringBuffer contentBuf = new StringBuffer();
      String line; while ((line = reader.readLine()) != null) {
        contentBuf.append(line + "\n");
      }
      reader.close();
      result = updateJavadoc(contentBuf.toString());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result.trim();
  }
  






  public String generate()
    throws Exception
  {
    if (getDir().length() == 0) {
      return generateJavadoc();
    }
    return updateJavadoc();
  }
  








  public String generateHelp()
  {
    String result = getClass().getName().replaceAll(".*\\.", "") + " Options:\n\n";
    Enumeration enm = listOptions();
    while (enm.hasMoreElements()) {
      Option option = (Option)enm.nextElement();
      result = result + option.synopsis() + "\n" + option.description() + "\n";
    }
    
    return result;
  }
  



  protected static void runJavadoc(Javadoc javadoc, String[] options)
  {
    try
    {
      try
      {
        if (Utils.getFlag('h', options)) {
          throw new Exception("Help requested");
        }
        javadoc.setOptions(options);
        Utils.checkForRemainingOptions(options);
        

        if (javadoc.getDir().length() == 0) {
          throw new Exception("No directory provided!");
        }
      } catch (Exception ex) {
        String result = "\n" + ex.getMessage() + "\n\n" + javadoc.generateHelp();
        throw new Exception(result);
      }
      
      System.out.println(javadoc.generate() + "\n");
    }
    catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
}

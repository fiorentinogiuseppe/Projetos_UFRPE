package weka.core;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;




























public class ListOptions
  implements OptionHandler, RevisionHandler
{
  protected String m_Classname = ListOptions.class.getName();
  

  public ListOptions() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe class to load.", "W", 1, "-W <classname>"));
    


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
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-W");
    result.add(getClassname());
    
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
  









  public String generate()
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    
    OptionHandler handler = (OptionHandler)Class.forName(getClassname()).newInstance();
    
    Enumeration enm = handler.listOptions();
    while (enm.hasMoreElements()) {
      Option option = (Option)enm.nextElement();
      result.append(option.synopsis() + '\n');
      result.append(option.description() + "\n");
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
  




  public static void main(String[] options)
  {
    ListOptions list = new ListOptions();
    try
    {
      try {
        if (Utils.getFlag('h', options)) {
          throw new Exception("Help requested");
        }
        list.setOptions(options);
        Utils.checkForRemainingOptions(options);
      }
      catch (Exception ex) {
        String result = "\n" + ex.getMessage() + "\n\n" + list.generateHelp();
        throw new Exception(result);
      }
      
      System.out.println("\n" + list.generate());
    }
    catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
}

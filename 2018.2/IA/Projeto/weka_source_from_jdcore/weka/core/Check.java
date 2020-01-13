package weka.core;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;





























public abstract class Check
  implements OptionHandler, RevisionHandler
{
  protected boolean m_Debug = false;
  

  protected boolean m_Silent = false;
  

  public Check() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    


    result.addElement(new Option("\tSilent mode - prints nothing to stdout.", "S", 0, "-S"));
    


    return result.elements();
  }
  




  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    setSilent(Utils.getFlag('S', options));
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    if (getSilent()) {
      result.add("-S");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  

















  protected Object forName(String prefix, Class cls, String classname, String[] options)
    throws Exception
  {
    Object result = null;
    try
    {
      result = Utils.forName(cls, classname, options);
    }
    catch (Exception e)
    {
      if (e.getMessage().toLowerCase().indexOf("can't find") > -1) {
        try {
          result = Utils.forName(cls, prefix + "." + classname, options);
        }
        catch (Exception ex) {
          if (e.getMessage().toLowerCase().indexOf("can't find") > -1) {
            throw new Exception("Can't find class called '" + classname + "' or '" + prefix + "." + classname + "'!");
          }
          


          throw new Exception(ex);
        }
        
      }
      else {
        throw new Exception(e);
      }
    }
    
    return result;
  }
  




  public abstract void doTests();
  



  public void setDebug(boolean debug)
  {
    m_Debug = debug;
    
    if (getDebug()) {
      setSilent(false);
    }
  }
  



  public boolean getDebug()
  {
    return m_Debug;
  }
  




  public void setSilent(boolean value)
  {
    m_Silent = value;
  }
  




  public boolean getSilent()
  {
    return m_Silent;
  }
  




  protected void print(Object msg)
  {
    if (!getSilent()) {
      System.out.print(msg);
    }
  }
  



  protected void println(Object msg)
  {
    print(msg + "\n");
  }
  


  protected void println()
  {
    print("\n");
  }
  



  protected static void runCheck(Check check, String[] options)
  {
    try
    {
      try
      {
        check.setOptions(options);
        Utils.checkForRemainingOptions(options);
      }
      catch (Exception ex) {
        String result = ex.getMessage() + "\n\n" + check.getClass().getName().replaceAll(".*\\.", "") + " Options:\n\n";
        Enumeration enm = check.listOptions();
        while (enm.hasMoreElements()) {
          Option option = (Option)enm.nextElement();
          result = result + option.synopsis() + "\n" + option.description() + "\n";
        }
        throw new Exception(result);
      }
      
      check.doTests();
    }
    catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
}

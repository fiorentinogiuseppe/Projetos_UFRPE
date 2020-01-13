package weka.core.logging;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import weka.core.RevisionUtils;
import weka.core.Tee;




































public class OutputLogger
  extends FileLogger
{
  protected OutputPrintStream m_StreamOut;
  protected OutputPrintStream m_StreamErr;
  protected Tee m_StdOut;
  protected Tee m_StdErr;
  public OutputLogger() {}
  
  public static class OutputPrintStream
    extends PrintStream
  {
    protected OutputLogger m_Owner;
    protected String m_LineFeed;
    
    public OutputPrintStream(OutputLogger owner, PrintStream stream)
      throws Exception
    {
      super();
      
      m_Owner = owner;
      m_LineFeed = System.getProperty("line.separator");
    }
    




    public void flush() {}
    




    public void print(int x)
    {
      m_Owner.append("" + x);
    }
    




    public void print(boolean x)
    {
      m_Owner.append("" + x);
    }
    




    public void print(String x)
    {
      m_Owner.append("" + x);
    }
    




    public void print(Object x)
    {
      m_Owner.append("" + x);
    }
    


    public void println()
    {
      m_Owner.append(m_LineFeed);
    }
    




    public void println(int x)
    {
      m_Owner.append(x + m_LineFeed);
    }
    




    public void println(boolean x)
    {
      m_Owner.append(x + m_LineFeed);
    }
    




    public void println(String x)
    {
      m_Owner.append(x + m_LineFeed);
    }
    





    public void println(Object x)
    {
      m_Owner.append(x + m_LineFeed);
    }
  }
  














  protected void initialize()
  {
    super.initialize();
    try
    {
      m_StdOut = new Tee(System.out);
      System.setOut(m_StdOut);
      m_StreamOut = new OutputPrintStream(this, m_StdOut.getDefault());
      m_StdOut.add(m_StreamOut);
      
      m_StdErr = new Tee(System.err);
      System.setErr(m_StdErr);
      m_StreamErr = new OutputPrintStream(this, m_StdErr.getDefault());
      m_StdErr.add(m_StreamErr);
    }
    catch (Exception e) {}
  }
  











  protected void doLog(Logger.Level level, String msg, String cls, String method, int lineno)
  {
    append(m_DateFormat.format(new Date()) + " " + cls + " " + method + m_LineFeed + level + ": " + msg + m_LineFeed);
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4716 $");
  }
}

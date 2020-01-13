package weka.core;

import java.io.PrintStream;
import java.util.Date;
import java.util.Vector;

















































public class Tee
  extends PrintStream
  implements RevisionHandler
{
  protected Vector m_Streams = new Vector();
  

  protected Vector m_Timestamps = new Vector();
  

  protected Vector m_Prefixes = new Vector();
  

  protected PrintStream m_Default = null;
  


  public Tee()
  {
    this(null);
  }
  





  public Tee(PrintStream def)
  {
    super(def);
    
    m_Default = def;
    clear();
  }
  





  public void clear()
  {
    m_Streams.clear();
    m_Timestamps.clear();
    m_Prefixes.clear();
    
    if (getDefault() != null) {
      add(getDefault());
    }
  }
  




  public PrintStream getDefault()
  {
    return m_Default;
  }
  





  public void add(PrintStream p)
  {
    add(p, false);
  }
  





  public void add(PrintStream p, boolean timestamp)
  {
    add(p, timestamp, "");
  }
  






  public void add(PrintStream p, boolean timestamp, String prefix)
  {
    if (m_Streams.contains(p)) {
      remove(p);
    }
    
    if (prefix == null) {
      prefix = "";
    }
    m_Streams.add(p);
    m_Timestamps.add(new Boolean(timestamp));
    m_Prefixes.add(prefix);
  }
  





  public PrintStream get(int index)
  {
    if ((index >= 0) && (index < size())) {
      return (PrintStream)m_Streams.get(index);
    }
    return null;
  }
  







  public PrintStream remove(PrintStream p)
  {
    if (contains(p)) {
      int index = m_Streams.indexOf(p);
      m_Timestamps.remove(index);
      m_Prefixes.remove(index);
      return (PrintStream)m_Streams.remove(index);
    }
    
    return null;
  }
  






  public PrintStream remove(int index)
  {
    if ((index >= 0) && (index < size())) {
      m_Timestamps.remove(index);
      m_Prefixes.remove(index);
      return (PrintStream)m_Streams.remove(index);
    }
    
    return null;
  }
  






  public boolean contains(PrintStream p)
  {
    return m_Streams.contains(p);
  }
  




  public int size()
  {
    return m_Streams.size();
  }
  



  private void printHeader()
  {
    for (int i = 0; i < size(); i++)
    {
      if (!((String)m_Prefixes.get(i)).equals("")) {
        ((PrintStream)m_Streams.get(i)).print("[" + m_Prefixes.get(i) + "]\t");
      }
      
      if (((Boolean)m_Timestamps.get(i)).booleanValue()) {
        ((PrintStream)m_Streams.get(i)).print("[" + new Date() + "]\t");
      }
    }
  }
  

  public void flush()
  {
    for (int i = 0; i < size(); i++) {
      ((PrintStream)m_Streams.get(i)).flush();
    }
  }
  



  public void print(int x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(long x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(float x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(double x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(boolean x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(char x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(char[] x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(String x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  




  public void print(Object x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).print(x);
    flush();
  }
  


  public void println()
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println();
    flush();
  }
  




  public void println(int x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  




  public void println(long x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  




  public void println(float x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  




  public void println(double x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  




  public void println(boolean x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  




  public void println(char x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  




  public void println(char[] x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  




  public void println(String x)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  










  public void println(Object x)
  {
    if ((x instanceof Throwable)) {
      Throwable t = (Throwable)x;
      StackTraceElement[] trace = t.getStackTrace();
      String line = t.toString() + "\n";
      for (int i = 0; i < trace.length; i++)
        line = line + "\t" + trace[i].toString() + "\n";
      x = line;
    }
    
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).println(x);
    flush();
  }
  













  public void write(byte[] buf, int off, int len)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).write(buf, off, len);
    flush();
  }
  













  public void write(int b)
  {
    printHeader();
    for (int i = 0; i < size(); i++)
      ((PrintStream)m_Streams.get(i)).write(b);
    flush();
  }
  




  public String toString()
  {
    return getClass().getName() + ": " + m_Streams.size();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5057 $");
  }
}

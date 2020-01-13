package weka.core;

import java.io.PrintStream;
import java.io.Serializable;









































public class SingleIndex
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = 5285169134430839303L;
  protected String m_IndexString = "";
  

  protected int m_SelectedIndex = -1;
  


  protected int m_Upper = -1;
  









  public SingleIndex() {}
  









  public SingleIndex(String index)
  {
    setSingleIndex(index);
  }
  








  public void setUpper(int newUpper)
  {
    if (newUpper >= 0) {
      m_Upper = newUpper;
      setValue();
    }
  }
  






  public String getSingleIndex()
  {
    return m_IndexString;
  }
  










  public void setSingleIndex(String index)
  {
    m_IndexString = index;
    m_SelectedIndex = -1;
  }
  








  public String toString()
  {
    if (m_IndexString.equals("")) {
      return "No index set";
    }
    if (m_Upper == -1) {
      throw new RuntimeException("Upper limit has not been specified");
    }
    return m_IndexString;
  }
  









  public int getIndex()
  {
    if (m_IndexString.equals("")) {
      throw new RuntimeException("No index set");
    }
    if (m_Upper == -1) {
      throw new RuntimeException("No upper limit has been specified for index");
    }
    return m_SelectedIndex;
  }
  









  public static String indexToString(int index)
  {
    return "" + (index + 1);
  }
  




  protected void setValue()
  {
    if (m_IndexString.equals("")) {
      throw new RuntimeException("No index set");
    }
    if (m_IndexString.toLowerCase().equals("first")) {
      m_SelectedIndex = 0;
    } else if (m_IndexString.toLowerCase().equals("last")) {
      m_SelectedIndex = m_Upper;
    } else {
      m_SelectedIndex = (Integer.parseInt(m_IndexString) - 1);
      if (m_SelectedIndex < 0) {
        m_IndexString = "";
        throw new IllegalArgumentException("Index must be greater than zero");
      }
      if (m_SelectedIndex > m_Upper) {
        m_IndexString = "";
        throw new IllegalArgumentException("Index is too large");
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  





  public static void main(String[] argv)
  {
    try
    {
      if (argv.length == 0) {
        throw new Exception("Usage: SingleIndex <indexspec>");
      }
      SingleIndex singleIndex = new SingleIndex();
      singleIndex.setSingleIndex(argv[0]);
      singleIndex.setUpper(9);
      System.out.println("Input: " + argv[0] + "\n" + singleIndex.toString());
      
      int selectedIndex = singleIndex.getIndex();
      System.out.println(selectedIndex + "");
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
  }
}

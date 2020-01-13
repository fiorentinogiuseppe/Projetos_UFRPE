package weka.core;

import java.io.Serializable;






































public class Tag
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 3326379903447135320L;
  protected int m_ID;
  protected String m_IDStr;
  protected String m_Readable;
  
  public Tag()
  {
    this(0, "A new tag", "A new tag", true);
  }
  





  public Tag(int ident, String readable)
  {
    this(ident, "", readable);
  }
  






  public Tag(int ident, String identStr, String readable)
  {
    this(ident, identStr, readable, true);
  }
  
  public Tag(int ident, String identStr, String readable, boolean upperCase) {
    m_ID = ident;
    if (identStr.length() == 0) {
      m_IDStr = ("" + ident);
    } else {
      m_IDStr = identStr;
      if (upperCase) {
        m_IDStr = identStr.toUpperCase();
      }
    }
    m_Readable = readable;
  }
  




  public int getID()
  {
    return m_ID;
  }
  




  public void setID(int id)
  {
    m_ID = id;
  }
  




  public String getIDStr()
  {
    return m_IDStr;
  }
  




  public void setIDStr(String str)
  {
    m_IDStr = str;
  }
  




  public String getReadable()
  {
    return m_Readable;
  }
  




  public void setReadable(String r)
  {
    m_Readable = r;
  }
  




  public String toString()
  {
    return m_IDStr;
  }
  









  public static String toOptionList(Tag[] tags)
  {
    String result = "<";
    for (int i = 0; i < tags.length; i++) {
      if (i > 0)
        result = result + "|";
      result = result + tags[i];
    }
    result = result + ">";
    
    return result;
  }
  









  public static String toOptionSynopsis(Tag[] tags)
  {
    String result = "";
    for (int i = 0; i < tags.length; i++) {
      result = result + "\t\t" + tags[i].getIDStr() + " = " + tags[i].getReadable() + "\n";
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}

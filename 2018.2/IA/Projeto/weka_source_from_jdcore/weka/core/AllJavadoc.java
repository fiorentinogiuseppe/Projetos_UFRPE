package weka.core;

import java.util.HashSet;
import java.util.Vector;















































public class AllJavadoc
  extends Javadoc
{
  protected static Vector m_Javadocs;
  
  static
  {
    HashSet<String> set = new HashSet(ClassDiscovery.find(Javadoc.class, Javadoc.class.getPackage().getName()));
    if (set.contains(AllJavadoc.class.getName())) {
      set.remove(AllJavadoc.class.getName());
    }
    
    m_Javadocs = new Vector();
    for (String classname : set) {
      try {
        Class cls = Class.forName(classname);
        m_Javadocs.add((Javadoc)cls.newInstance());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  




  public void setClassname(String value)
  {
    super.setClassname(value);
    for (int i = 0; i < m_Javadocs.size(); i++) {
      ((Javadoc)m_Javadocs.get(i)).setClassname(value);
    }
  }
  



  public void setUseStars(boolean value)
  {
    super.setUseStars(value);
    for (int i = 0; i < m_Javadocs.size(); i++) {
      ((Javadoc)m_Javadocs.get(i)).setUseStars(value);
    }
  }
  



  public void setSilent(boolean value)
  {
    super.setSilent(value);
    for (int i = 0; i < m_Javadocs.size(); i++) {
      ((Javadoc)m_Javadocs.get(i)).setSilent(value);
    }
  }
  




  protected String generateJavadoc(int index)
    throws Exception
  {
    throw new Exception("Not used!");
  }
  









  protected String updateJavadoc(String content)
    throws Exception
  {
    String result = content;
    
    for (int i = 0; i < m_Javadocs.size(); i++) {
      result = ((Javadoc)m_Javadocs.get(i)).updateJavadoc(result);
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6110 $");
  }
  




  public static void main(String[] args)
  {
    runJavadoc(new AllJavadoc(), args);
  }
  
  public AllJavadoc() {}
}

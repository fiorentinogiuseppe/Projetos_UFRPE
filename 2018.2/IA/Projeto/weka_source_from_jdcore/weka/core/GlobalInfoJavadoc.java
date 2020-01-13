package weka.core;

import java.lang.reflect.Method;




























































public class GlobalInfoJavadoc
  extends Javadoc
{
  public static final String GLOBALINFO_METHOD = "globalInfo";
  public static final String GLOBALINFO_STARTTAG = "<!-- globalinfo-start -->";
  public static final String GLOBALINFO_ENDTAG = "<!-- globalinfo-end -->";
  
  public GlobalInfoJavadoc()
  {
    m_StartTag = new String[1];
    m_EndTag = new String[1];
    m_StartTag[0] = "<!-- globalinfo-start -->";
    m_EndTag[0] = "<!-- globalinfo-end -->";
  }
  








  protected String generateJavadoc(int index)
    throws Exception
  {
    String result = "";
    
    if (index == 0) {
      if (!canInstantiateClass())
        return result;
      Method method;
      try {
        method = getInstance().getClass().getMethod("globalInfo", (Class[])null);
      }
      catch (Exception e)
      {
        return result;
      }
      

      result = toHTML((String)method.invoke(getInstance(), (Object[])null));
      result = result.trim() + "\n<p/>\n";
      

      if (getUseStars()) {
        result = indent(result, 1, "* ");
      }
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
  




  public static void main(String[] args)
  {
    runJavadoc(new GlobalInfoJavadoc(), args);
  }
}

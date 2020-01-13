package weka.core;











public class Option
  implements RevisionHandler
{
  private String m_Description;
  









  private String m_Synopsis;
  








  private String m_Name;
  








  private int m_NumArguments;
  









  public Option(String description, String name, int numArguments, String synopsis)
  {
    m_Description = description;
    m_Name = name;
    m_NumArguments = numArguments;
    m_Synopsis = synopsis;
  }
  





  public String description()
  {
    return m_Description;
  }
  





  public String name()
  {
    return m_Name;
  }
  





  public int numArguments()
  {
    return m_NumArguments;
  }
  





  public String synopsis()
  {
    return m_Synopsis;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

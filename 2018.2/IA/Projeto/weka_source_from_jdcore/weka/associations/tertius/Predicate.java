package weka.associations.tertius;

import java.io.Serializable;
import java.util.ArrayList;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




































public class Predicate
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8374702481965026640L;
  private ArrayList m_literals;
  private String m_name;
  private int m_index;
  private boolean m_isClass;
  
  public Predicate(String name, int index, boolean isClass)
  {
    m_literals = new ArrayList();
    m_name = name;
    m_index = index;
    m_isClass = isClass;
  }
  
  public void addLiteral(Literal lit)
  {
    m_literals.add(lit);
  }
  
  public Literal getLiteral(int index)
  {
    return (Literal)m_literals.get(index);
  }
  
  public int getIndex()
  {
    return m_index;
  }
  
  public int indexOf(Literal lit)
  {
    int index = m_literals.indexOf(lit);
    return index != -1 ? index : m_literals.indexOf(lit.getNegation());
  }
  


  public int numLiterals()
  {
    return m_literals.size();
  }
  
  public boolean isClass()
  {
    return m_isClass;
  }
  
  public String toString()
  {
    return m_name;
  }
  
  public String description()
  {
    StringBuffer text = new StringBuffer();
    text.append(toString() + "\n");
    for (int i = 0; i < numLiterals(); i++) {
      Literal lit = getLiteral(i);
      Literal neg = lit.getNegation();
      text.append("\t" + lit + "\t" + neg + "\n");
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

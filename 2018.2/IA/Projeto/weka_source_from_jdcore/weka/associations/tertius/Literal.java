package weka.associations.tertius;

import java.io.Serializable;
import weka.core.Instance;
import weka.core.RevisionHandler;







































public abstract class Literal
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 2675363669503575771L;
  private Predicate m_predicate;
  public static final int NEG = 0;
  public static final int POS = 1;
  private int m_sign;
  private Literal m_negation;
  protected int m_missing;
  
  public Literal(Predicate predicate, int sign, int missing)
  {
    m_predicate = predicate;
    m_sign = sign;
    m_negation = null;
    m_missing = missing;
  }
  
  public Predicate getPredicate()
  {
    return m_predicate;
  }
  
  public Literal getNegation()
  {
    return m_negation;
  }
  
  public void setNegation(Literal negation)
  {
    m_negation = negation;
  }
  
  public boolean positive()
  {
    return m_sign == 1;
  }
  
  public boolean negative()
  {
    return m_sign == 0;
  }
  
  public abstract boolean satisfies(Instance paramInstance);
  
  public abstract boolean negationSatisfies(Instance paramInstance);
  
  public abstract String toString();
}

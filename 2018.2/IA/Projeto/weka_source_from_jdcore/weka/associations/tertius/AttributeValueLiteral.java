package weka.associations.tertius;

import weka.core.Instance;
import weka.core.RevisionUtils;



































public class AttributeValueLiteral
  extends Literal
{
  private static final long serialVersionUID = 4077436297281456239L;
  private String m_value;
  private int m_index;
  
  public AttributeValueLiteral(Predicate predicate, String value, int index, int sign, int missing)
  {
    super(predicate, sign, missing);
    m_value = value;
    m_index = index;
  }
  
  public boolean satisfies(Instance instance)
  {
    if (m_index == -1) {
      if (positive()) {
        return instance.isMissing(getPredicate().getIndex());
      }
      return !instance.isMissing(getPredicate().getIndex());
    }
    if (instance.isMissing(getPredicate().getIndex())) {
      if (positive()) {
        return false;
      }
      return m_missing != 0;
    }
    
    if (positive()) {
      return instance.value(getPredicate().getIndex()) == m_index;
    }
    return instance.value(getPredicate().getIndex()) != m_index;
  }
  


  public boolean negationSatisfies(Instance instance)
  {
    if (m_index == -1) {
      if (positive()) {
        return !instance.isMissing(getPredicate().getIndex());
      }
      return instance.isMissing(getPredicate().getIndex());
    }
    if (instance.isMissing(getPredicate().getIndex())) {
      if (positive()) {
        return m_missing != 0;
      }
      return false;
    }
    
    if (positive()) {
      return instance.value(getPredicate().getIndex()) != m_index;
    }
    return instance.value(getPredicate().getIndex()) == m_index;
  }
  


  public String toString()
  {
    StringBuffer text = new StringBuffer();
    if (negative()) {
      text.append("not ");
    }
    text.append(getPredicate().toString() + " = " + m_value);
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}

package weka.datagenerators;

import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;













































































public class Test
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = -8890645875887157782L;
  int m_AttIndex;
  double m_Split;
  boolean m_Not;
  Instances m_Dataset;
  
  public Test(int i, double s, Instances dataset)
  {
    m_AttIndex = i;
    m_Split = s;
    m_Dataset = dataset;
    
    m_Not = false;
  }
  







  public Test(int i, double s, Instances dataset, boolean n)
  {
    m_AttIndex = i;
    m_Split = s;
    m_Dataset = dataset;
    m_Not = n;
  }
  




  public Test getNot()
  {
    return new Test(m_AttIndex, m_Split, m_Dataset, !m_Not);
  }
  





  public boolean passesTest(Instance inst)
    throws Exception
  {
    if (inst.isMissing(m_AttIndex)) { return false;
    }
    boolean isNominal = inst.attribute(m_AttIndex).isNominal();
    double attribVal = inst.value(m_AttIndex);
    if (!m_Not) {
      if (isNominal) {
        if ((int)attribVal != (int)m_Split) return false;
      }
      else if (attribVal >= m_Split) return false;
    }
    else if (isNominal) {
      if ((int)attribVal == (int)m_Split) return false;
    }
    else if (attribVal < m_Split) { return false;
    }
    return true;
  }
  




  public String toString()
  {
    return m_Dataset.attribute(m_AttIndex).name() + " " + testComparisonString();
  }
  





  public String toPrologString()
  {
    Attribute att = m_Dataset.attribute(m_AttIndex);
    StringBuffer str = new StringBuffer();
    String attName = m_Dataset.attribute(m_AttIndex).name();
    if (att.isNumeric()) {
      str = str.append(attName + " ");
      if (m_Not) str = str.append(">= " + Utils.doubleToString(m_Split, 3)); else
        str = str.append("< " + Utils.doubleToString(m_Split, 3));
    } else {
      String value = att.value((int)m_Split);
      
      if (value == "false") str = str.append("not(" + attName + ")"); else
        str = str.append(attName);
    }
    return str.toString();
  }
  





  private String testComparisonString()
  {
    Attribute att = m_Dataset.attribute(m_AttIndex);
    if (att.isNumeric()) {
      return (m_Not ? ">= " : "< ") + Utils.doubleToString(m_Split, 3);
    }
    
    if (att.numValues() != 2)
      return (m_Not ? "!= " : "= ") + att.value((int)m_Split);
    return "= " + (m_Not ? att.value((int)m_Split == 0 ? 1 : 0) : att.value((int)m_Split));
  }
  








  private String testPrologComparisonString()
  {
    Attribute att = m_Dataset.attribute(m_AttIndex);
    if (att.isNumeric()) {
      return (m_Not ? ">= " : "< ") + Utils.doubleToString(m_Split, 3);
    }
    
    if (att.numValues() != 2)
      return (m_Not ? "!= " : "= ") + att.value((int)m_Split);
    return "= " + (m_Not ? att.value((int)m_Split == 0 ? 1 : 0) : att.value((int)m_Split));
  }
  








  public boolean equalTo(Test t)
  {
    return (m_AttIndex == m_AttIndex) && (m_Split == m_Split) && (m_Not == m_Not);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

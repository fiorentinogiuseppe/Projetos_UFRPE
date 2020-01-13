package weka.classifiers.bayes.net;

import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;









































public class ParentSet
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = 4155021284407181838L;
  private int[] m_nParents;
  
  public int getParent(int iParent) { return m_nParents[iParent]; }
  
  public int[] getParents() { return m_nParents; }
  





  public void SetParent(int iParent, int nNode)
  {
    m_nParents[iParent] = nNode;
  }
  




  private int m_nNrOfParents = 0;
  



  public int getNrOfParents()
  {
    return m_nNrOfParents;
  }
  




  public boolean contains(int iNode)
  {
    for (int iParent = 0; iParent < m_nNrOfParents; iParent++) {
      if (m_nParents[iParent] == iNode) {
        return true;
      }
    }
    return false;
  }
  


  private int m_nCardinalityOfParents = 1;
  




  public int getCardinalityOfParents()
  {
    return m_nCardinalityOfParents;
  }
  




  public int getFreshCardinalityOfParents(Instances _Instances)
  {
    m_nCardinalityOfParents = 1;
    for (int iParent = 0; iParent < m_nNrOfParents; iParent++) {
      m_nCardinalityOfParents *= _Instances.attribute(m_nParents[iParent]).numValues();
    }
    return m_nCardinalityOfParents;
  }
  

  public ParentSet()
  {
    m_nParents = new int[10];
    m_nNrOfParents = 0;
    m_nCardinalityOfParents = 1;
  }
  



  public ParentSet(int nMaxNrOfParents)
  {
    m_nParents = new int[nMaxNrOfParents];
    m_nNrOfParents = 0;
    m_nCardinalityOfParents = 1;
  }
  



  public ParentSet(ParentSet other)
  {
    m_nNrOfParents = m_nNrOfParents;
    m_nCardinalityOfParents = m_nCardinalityOfParents;
    m_nParents = new int[m_nNrOfParents];
    
    for (int iParent = 0; iParent < m_nNrOfParents; iParent++) {
      m_nParents[iParent] = m_nParents[iParent];
    }
  }
  




  public void maxParentSetSize(int nSize)
  {
    m_nParents = new int[nSize];
  }
  





  public void addParent(int nParent, Instances _Instances)
  {
    if (m_nNrOfParents == m_nParents.length)
    {
      int[] nParents = new int[2 * m_nParents.length];
      for (int i = 0; i < m_nNrOfParents; i++) {
        nParents[i] = m_nParents[i];
      }
      m_nParents = nParents;
    }
    m_nParents[m_nNrOfParents] = nParent;
    m_nNrOfParents += 1;
    m_nCardinalityOfParents *= _Instances.attribute(nParent).numValues();
  }
  







  public void addParent(int nParent, int iParent, Instances _Instances)
  {
    if (m_nNrOfParents == m_nParents.length)
    {
      int[] nParents = new int[2 * m_nParents.length];
      for (int i = 0; i < m_nNrOfParents; i++) {
        nParents[i] = m_nParents[i];
      }
      m_nParents = nParents;
    }
    for (int iParent2 = m_nNrOfParents; iParent2 > iParent; iParent2--) {
      m_nParents[iParent2] = m_nParents[(iParent2 - 1)];
    }
    m_nParents[iParent] = nParent;
    m_nNrOfParents += 1;
    m_nCardinalityOfParents *= _Instances.attribute(nParent).numValues();
  }
  





  public int deleteParent(int nParent, Instances _Instances)
  {
    int iParent = 0;
    while ((m_nParents[iParent] != nParent) && (iParent < m_nNrOfParents)) {
      iParent++;
    }
    int iParent2 = -1;
    if (iParent < m_nNrOfParents) {
      iParent2 = iParent;
    }
    if (iParent < m_nNrOfParents) {
      while (iParent < m_nNrOfParents - 1) {
        m_nParents[iParent] = m_nParents[(iParent + 1)];
        iParent++;
      }
      m_nNrOfParents -= 1;
      m_nCardinalityOfParents /= _Instances.attribute(nParent).numValues();
    }
    return iParent2;
  }
  




  public void deleteLastParent(Instances _Instances)
  {
    m_nNrOfParents -= 1;
    m_nCardinalityOfParents /= _Instances.attribute(m_nParents[m_nNrOfParents]).numValues();
  }
  





  public void copy(ParentSet other)
  {
    m_nCardinalityOfParents = m_nCardinalityOfParents;
    m_nNrOfParents = m_nNrOfParents;
    for (int iParent = 0; iParent < m_nNrOfParents; iParent++) {
      m_nParents[iParent] = m_nParents[iParent];
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6943 $");
  }
}

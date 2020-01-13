package weka.classifiers.functions.supportVector;

import java.io.PrintStream;
import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;











































public class SMOset
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8364829283188675777L;
  private int m_number;
  private int m_first;
  private boolean[] m_indicators;
  private int[] m_next;
  private int[] m_previous;
  
  public SMOset(int size)
  {
    m_indicators = new boolean[size];
    m_next = new int[size];
    m_previous = new int[size];
    m_number = 0;
    m_first = -1;
  }
  



  public boolean contains(int index)
  {
    return m_indicators[index];
  }
  



  public void delete(int index)
  {
    if (m_indicators[index] != 0) {
      if (m_first == index) {
        m_first = m_next[index];
      } else {
        m_next[m_previous[index]] = m_next[index];
      }
      if (m_next[index] != -1) {
        m_previous[m_next[index]] = m_previous[index];
      }
      m_indicators[index] = false;
      m_number -= 1;
    }
  }
  



  public void insert(int index)
  {
    if (m_indicators[index] == 0) {
      if (m_number == 0) {
        m_first = index;
        m_next[index] = -1;
        m_previous[index] = -1;
      } else {
        m_previous[m_first] = index;
        m_next[index] = m_first;
        m_previous[index] = -1;
        m_first = index;
      }
      m_indicators[index] = true;
      m_number += 1;
    }
  }
  



  public int getNext(int index)
  {
    if (index == -1) {
      return m_first;
    }
    return m_next[index];
  }
  




  public void printElements()
  {
    for (int i = getNext(-1); i != -1; i = getNext(i)) {
      System.err.print(i + " ");
    }
    System.err.println();
    for (int i = 0; i < m_indicators.length; i++) {
      if (m_indicators[i] != 0) {
        System.err.print(i + " ");
      }
    }
    System.err.println();
    System.err.println(m_number);
  }
  



  public int numElements()
  {
    return m_number;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

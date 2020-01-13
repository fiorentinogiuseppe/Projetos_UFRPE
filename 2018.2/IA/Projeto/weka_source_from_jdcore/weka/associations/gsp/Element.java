package weka.associations.gsp;

import java.io.Serializable;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;








































public class Element
  implements Cloneable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -7900701276019516371L;
  protected int[] m_Events;
  
  public Element() {}
  
  public Element(int size)
  {
    m_Events = new int[size];
  }
  







  public static FastVector getOneElements(Instances instances)
  {
    FastVector setOfOneElements = new FastVector();
    

    for (int i = 0; i < instances.numAttributes(); i++) {
      for (int j = 0; j < instances.attribute(i).numValues(); j++) {
        Element curElement = new Element();
        curElement.setEvents(new int[instances.numAttributes()]);
        for (int k = 0; k < instances.numAttributes(); k++) {
          curElement.getEvents()[k] = -1;
        }
        curElement.getEvents()[i] = j;
        setOfOneElements.addElement(curElement);
      }
    }
    return setOfOneElements;
  }
  






  public static Element merge(Element element1, Element element2)
  {
    int[] element1Events = element1.getEvents();
    int[] element2Events = element2.getEvents();
    Element resultElement = new Element(element1Events.length);
    int[] resultEvents = resultElement.getEvents();
    
    for (int i = 0; i < element1Events.length; i++) {
      if (element2Events[i] > -1) {
        resultEvents[i] = element2Events[i];
      } else {
        resultEvents[i] = element1Events[i];
      }
    }
    resultElement.setEvents(resultEvents);
    
    return resultElement;
  }
  



  public Element clone()
  {
    try
    {
      Element clone = (Element)super.clone();
      int[] cloneEvents = new int[m_Events.length];
      
      for (int i = 0; i < m_Events.length; i++) {
        cloneEvents[i] = m_Events[i];
      }
      clone.setEvents(cloneEvents);
      
      return clone;
    } catch (CloneNotSupportedException exc) {
      exc.printStackTrace();
    }
    return null;
  }
  




  public boolean containsOverOneEvent()
  {
    int numEvents = 0;
    for (int i = 0; i < m_Events.length; i++) {
      if (m_Events[i] > -1) {
        numEvents++;
      }
      if (numEvents == 2) {
        return true;
      }
    }
    return false;
  }
  




  public void deleteEvent(String position)
  {
    if (position.equals("first"))
    {
      for (int i = 0; i < m_Events.length; i++) {
        if (m_Events[i] > -1) {
          m_Events[i] = -1;
          break;
        }
      }
    }
    if (position.equals("last"))
    {
      for (int i = m_Events.length - 1; i >= 0; i--) {
        if (m_Events[i] > -1) {
          m_Events[i] = -1;
          break;
        }
      }
    }
  }
  




  public boolean equals(Object obj)
  {
    Element element2 = (Element)obj;
    
    for (int i = 0; i < m_Events.length; i++) {
      if (m_Events[i] != element2.getEvents()[i]) {
        return false;
      }
    }
    return true;
  }
  




  public int[] getEvents()
  {
    return m_Events;
  }
  





  public boolean isContainedBy(Instance instance)
  {
    for (int i = 0; i < instance.numAttributes(); i++) {
      if (m_Events[i] > -1) {
        if (instance.isMissing(i)) {
          return false;
        }
        if (m_Events[i] != (int)instance.value(i)) {
          return false;
        }
      }
    }
    return true;
  }
  




  public boolean isEmpty()
  {
    for (int i = 0; i < m_Events.length; i++) {
      if (m_Events[i] > -1) {
        return false;
      }
    }
    return true;
  }
  




  protected void setEvents(int[] events)
  {
    m_Events = events;
  }
  






  public String toNominalString(Instances dataSet)
  {
    StringBuffer result = new StringBuffer();
    int addedValues = 0;
    
    result.append("{");
    
    for (int i = 0; i < m_Events.length; i++) {
      if (m_Events[i] > -1) {
        result.append(dataSet.attribute(i).value(m_Events[i]) + ",");
        addedValues++;
      }
    }
    result.deleteCharAt(result.length() - 1);
    result.append("}");
    
    return result.toString();
  }
  




  public String toString()
  {
    String result = "";
    
    result = result + "{";
    
    for (int i = 0; i < m_Events.length; i++) {
      result = result + m_Events[i];
      if (i + 1 < m_Events.length) {
        result = result + ",";
      }
    }
    result = result + "}";
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}

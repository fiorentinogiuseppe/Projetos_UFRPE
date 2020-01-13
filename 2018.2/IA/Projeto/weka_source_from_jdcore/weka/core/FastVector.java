package weka.core;

import java.io.Serializable;
import java.util.Enumeration;



















































public class FastVector
  implements Copyable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -2173635135622930169L;
  private Object[] m_Objects;
  
  public class FastVectorEnumeration
    implements Enumeration, RevisionHandler
  {
    private int m_Counter;
    private FastVector m_Vector;
    private int m_SpecialElement;
    
    public FastVectorEnumeration(FastVector vector)
    {
      m_Counter = 0;
      m_Vector = vector;
      m_SpecialElement = -1;
    }
    








    public FastVectorEnumeration(FastVector vector, int special)
    {
      m_Vector = vector;
      m_SpecialElement = special;
      if (special == 0) {
        m_Counter = 1;
      } else {
        m_Counter = 0;
      }
    }
    






    public final boolean hasMoreElements()
    {
      if (m_Counter < m_Vector.size()) {
        return true;
      }
      return false;
    }
    






    public final Object nextElement()
    {
      Object result = m_Vector.elementAt(m_Counter);
      
      m_Counter += 1;
      if (m_Counter == m_SpecialElement) {
        m_Counter += 1;
      }
      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.16 $");
    }
  }
  






  private int m_Size = 0;
  



  private int m_CapacityIncrement = 1;
  


  private int m_CapacityMultiplier = 2;
  








  public FastVector()
  {
    m_Objects = new Object[0];
  }
  






  public FastVector(int capacity)
  {
    m_Objects = new Object[capacity];
  }
  








  public final void addElement(Object element)
  {
    if (m_Size == m_Objects.length) {
      Object[] newObjects = new Object[m_CapacityMultiplier * (m_Objects.length + m_CapacityIncrement)];
      

      System.arraycopy(m_Objects, 0, newObjects, 0, m_Size);
      m_Objects = newObjects;
    }
    m_Objects[m_Size] = element;
    m_Size += 1;
  }
  






  public final int capacity()
  {
    return m_Objects.length;
  }
  





  public final Object copy()
  {
    FastVector copy = new FastVector(m_Objects.length);
    
    m_Size = m_Size;
    m_CapacityIncrement = m_CapacityIncrement;
    m_CapacityMultiplier = m_CapacityMultiplier;
    System.arraycopy(m_Objects, 0, m_Objects, 0, m_Size);
    return copy;
  }
  






  public final Object copyElements()
  {
    FastVector copy = new FastVector(m_Objects.length);
    
    m_Size = m_Size;
    m_CapacityIncrement = m_CapacityIncrement;
    m_CapacityMultiplier = m_CapacityMultiplier;
    for (int i = 0; i < m_Size; i++) {
      m_Objects[i] = ((Copyable)m_Objects[i]).copy();
    }
    return copy;
  }
  








  public final Object elementAt(int index)
  {
    return m_Objects[index];
  }
  





  public final Enumeration elements()
  {
    return new FastVectorEnumeration(this);
  }
  








  public final Enumeration elements(int index)
  {
    return new FastVectorEnumeration(this, index);
  }
  


  public boolean contains(Object o)
  {
    if (o == null) {
      return false;
    }
    for (int i = 0; i < m_Objects.length; i++) {
      if (o.equals(m_Objects[i]))
        return true;
    }
    return false;
  }
  







  public final Object firstElement()
  {
    return m_Objects[0];
  }
  








  public final int indexOf(Object element)
  {
    for (int i = 0; i < m_Size; i++) {
      if (element.equals(m_Objects[i])) {
        return i;
      }
    }
    return -1;
  }
  








  public final void insertElementAt(Object element, int index)
  {
    if (m_Size < m_Objects.length) {
      System.arraycopy(m_Objects, index, m_Objects, index + 1, m_Size - index);
      
      m_Objects[index] = element;
    } else {
      Object[] newObjects = new Object[m_CapacityMultiplier * (m_Objects.length + m_CapacityIncrement)];
      

      System.arraycopy(m_Objects, 0, newObjects, 0, index);
      newObjects[index] = element;
      System.arraycopy(m_Objects, index, newObjects, index + 1, m_Size - index);
      
      m_Objects = newObjects;
    }
    m_Size += 1;
  }
  






  public final Object lastElement()
  {
    return m_Objects[(m_Size - 1)];
  }
  






  public final void removeElementAt(int index)
  {
    System.arraycopy(m_Objects, index + 1, m_Objects, index, m_Size - index - 1);
    


    m_Objects[(m_Size - 1)] = null;
    
    m_Size -= 1;
  }
  




  public final void removeAllElements()
  {
    m_Objects = new Object[m_Objects.length];
    m_Size = 0;
  }
  





  public final void appendElements(FastVector toAppend)
  {
    setCapacity(size() + toAppend.size());
    System.arraycopy(m_Objects, 0, m_Objects, size(), toAppend.size());
    m_Size = m_Objects.length;
  }
  





  public final Object[] toArray()
  {
    Object[] newObjects = new Object[size()];
    System.arraycopy(m_Objects, 0, newObjects, 0, size());
    return newObjects;
  }
  





  public final void setCapacity(int capacity)
  {
    Object[] newObjects = new Object[capacity];
    
    System.arraycopy(m_Objects, 0, newObjects, 0, Math.min(capacity, m_Size));
    m_Objects = newObjects;
    if (m_Objects.length < m_Size) {
      m_Size = m_Objects.length;
    }
  }
  






  public final void setElementAt(Object element, int index)
  {
    m_Objects[index] = element;
  }
  






  public final int size()
  {
    return m_Size;
  }
  








  public final void swap(int first, int second)
  {
    Object help = m_Objects[first];
    
    m_Objects[first] = m_Objects[second];
    m_Objects[second] = help;
  }
  



  public final void trimToSize()
  {
    Object[] newObjects = new Object[m_Size];
    
    System.arraycopy(m_Objects, 0, newObjects, 0, m_Size);
    m_Objects = newObjects;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.16 $");
  }
}

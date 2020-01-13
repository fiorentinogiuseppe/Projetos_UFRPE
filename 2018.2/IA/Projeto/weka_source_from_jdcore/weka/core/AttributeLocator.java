package weka.core;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Vector;
































public class AttributeLocator
  implements Serializable, Comparable<AttributeLocator>, RevisionHandler
{
  private static final long serialVersionUID = -2932848827681070345L;
  protected int[] m_AllowedIndices = null;
  

  protected Vector<Boolean> m_Attributes = null;
  

  protected BitSet m_AttributesEfficient = null;
  

  protected Vector<AttributeLocator> m_Locators = null;
  

  protected int m_Type = -1;
  

  protected Instances m_Data = null;
  

  protected int[] m_Indices = null;
  

  protected int[] m_LocatorIndices = null;
  






  public AttributeLocator(Instances data, int type)
  {
    this(data, type, 0, data.numAttributes() - 1);
  }
  










  public AttributeLocator(Instances data, int type, int fromIndex, int toIndex)
  {
    int[] indices = new int[toIndex - fromIndex + 1];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = (fromIndex + i);
    }
    initialize(data, type, indices);
  }
  









  public AttributeLocator(Instances data, int type, int[] indices)
  {
    initialize(data, type, indices);
  }
  






  protected void initialize(Instances data, int type, int[] indices)
  {
    m_Data = new Instances(data, 0);
    m_Type = type;
    
    m_AllowedIndices = new int[indices.length];
    System.arraycopy(indices, 0, m_AllowedIndices, 0, indices.length);
    
    locate();
    
    m_Indices = find(true);
    m_LocatorIndices = find(false);
  }
  




  public int getType()
  {
    return m_Type;
  }
  




  public int[] getAllowedIndices()
  {
    return m_AllowedIndices;
  }
  




  protected void locate()
  {
    m_Attributes = null;
    m_AttributesEfficient = new BitSet(m_AllowedIndices.length);
    m_Locators = new Vector();
    
    for (int i = 0; i < m_AllowedIndices.length; i++) {
      if (m_Data.attribute(m_AllowedIndices[i]).type() == 4) {
        m_Locators.add(new AttributeLocator(m_Data.attribute(m_AllowedIndices[i]).relation(), getType()));
      } else {
        m_Locators.add(null);
      }
      m_AttributesEfficient.set(i, m_Data.attribute(m_AllowedIndices[i]).type() == getType());
    }
  }
  




  public Instances getData()
  {
    return m_Data;
  }
  











  protected int[] find(boolean findAtts)
  {
    if (m_AttributesEfficient == null) {
      moveFromBooleanVectorToBitSet();
    }
    
    Vector<Integer> indices = new Vector();
    if (findAtts) {
      for (int i = 0; i < m_AttributesEfficient.size(); i++) {
        if (Boolean.valueOf(m_AttributesEfficient.get(i)).booleanValue()) {
          indices.add(new Integer(i));
        }
      }
    }
    for (int i = 0; i < m_Locators.size(); i++) {
      if (m_Locators.get(i) != null) {
        indices.add(new Integer(i));
      }
    }
    

    int[] result = new int[indices.size()];
    for (i = 0; i < indices.size(); i++) {
      result[i] = ((Integer)indices.get(i)).intValue();
    }
    return result;
  }
  





  public int getActualIndex(int index)
  {
    return m_AllowedIndices[index];
  }
  







  public int[] getAttributeIndices()
  {
    return m_Indices;
  }
  







  public int[] getLocatorIndices()
  {
    return m_LocatorIndices;
  }
  






  public AttributeLocator getLocator(int index)
  {
    return (AttributeLocator)m_Locators.get(index);
  }
  












  public int compareTo(AttributeLocator o)
  {
    int result = 0;
    

    if (getType() < o.getType()) {
      result = -1;
    }
    else if (getType() > o.getType()) {
      result = 1;


    }
    else if (getAllowedIndices().length < o.getAllowedIndices().length) {
      result = -1;
    }
    else if (getAllowedIndices().length > o.getAllowedIndices().length) {
      result = 1;
    }
    else {
      for (int i = 0; i < getAllowedIndices().length; i++) {
        if (getAllowedIndices()[i] < o.getAllowedIndices()[i]) {
          result = -1;
          break;
        }
        if (getAllowedIndices()[i] > o.getAllowedIndices()[i]) {
          result = 1;
          break;
        }
        
        result = 0;
      }
    }
    


    return result;
  }
  







  public boolean equals(Object o)
  {
    return compareTo((AttributeLocator)o) == 0;
  }
  





  public String toString()
  {
    if (m_AttributesEfficient == null) {
      moveFromBooleanVectorToBitSet();
    }
    return m_AttributesEfficient.toString();
  }
  



  private void moveFromBooleanVectorToBitSet()
  {
    m_AttributesEfficient = new BitSet(m_Attributes.size());
    
    for (int i = 0; i < m_Attributes.size(); i++) {
      if (((Boolean)m_Attributes.get(i)).booleanValue()) {
        m_AttributesEfficient.set(i, true);
      }
    }
    m_Attributes = null;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8034 $");
  }
}

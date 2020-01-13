package weka.associations;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;












































public class ItemSet
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 2724000045282835791L;
  protected int[] m_items;
  protected int m_counter;
  protected int m_totalTransactions;
  
  public ItemSet(int totalTrans)
  {
    m_totalTransactions = totalTrans;
  }
  





  public ItemSet(int totalTrans, int[] array)
  {
    m_totalTransactions = totalTrans;
    m_items = array;
    m_counter = 1;
  }
  



  public ItemSet(int[] array)
  {
    m_items = array;
    m_counter = 0;
  }
  






  public boolean containedBy(Instance instance)
  {
    for (int i = 0; i < instance.numAttributes(); i++)
      if (m_items[i] > -1) {
        if (instance.isMissing(i))
          return false;
        if (m_items[i] != (int)instance.value(i))
          return false;
      }
    return true;
  }
  








  public static FastVector deleteItemSets(FastVector itemSets, int minSupport, int maxSupport)
  {
    FastVector newVector = new FastVector(itemSets.size());
    
    for (int i = 0; i < itemSets.size(); i++) {
      ItemSet current = (ItemSet)itemSets.elementAt(i);
      if ((m_counter >= minSupport) && (m_counter <= maxSupport))
      {
        newVector.addElement(current); }
    }
    return newVector;
  }
  






  public boolean equals(Object itemSet)
  {
    if ((itemSet == null) || (!itemSet.getClass().equals(getClass()))) {
      return false;
    }
    if (m_items.length != m_items.length)
      return false;
    for (int i = 0; i < m_items.length; i++)
      if (m_items[i] != m_items[i])
        return false;
    return true;
  }
  







  public static Hashtable getHashtable(FastVector itemSets, int initialSize)
  {
    Hashtable hashtable = new Hashtable(initialSize);
    
    for (int i = 0; i < itemSets.size(); i++) {
      ItemSet current = (ItemSet)itemSets.elementAt(i);
      hashtable.put(current, new Integer(m_counter));
    }
    return hashtable;
  }
  





  public int hashCode()
  {
    long result = 0L;
    
    for (int i = m_items.length - 1; i >= 0; i--)
      result += i * m_items[i];
    return (int)result;
  }
  








  public static FastVector mergeAllItemSets(FastVector itemSets, int size, int totalTrans)
  {
    FastVector newVector = new FastVector();
    
    label268:
    
    for (int i = 0; i < itemSets.size(); i++) {
      ItemSet first = (ItemSet)itemSets.elementAt(i);
      
      for (int j = i + 1; j < itemSets.size(); j++) {
        ItemSet second = (ItemSet)itemSets.elementAt(j);
        ItemSet result = new ItemSet(totalTrans);
        m_items = new int[m_items.length];
        

        int numFound = 0;
        int k = 0;
        while (numFound < size) {
          if (m_items[k] != m_items[k]) break label268;
          if (m_items[k] != -1)
            numFound++;
          m_items[k] = m_items[k];
          

          k++;
        }
        

        while ((k < m_items.length) && (
          (m_items[k] == -1) || (m_items[k] == -1)))
        {

          if (m_items[k] != -1) {
            m_items[k] = m_items[k];
          } else {
            m_items[k] = m_items[k];
          }
          k++;
        }
        if (k == m_items.length) {
          m_counter = 0;
          
          newVector.addElement(result);
        }
      }
    }
    return newVector;
  }
  







  public static FastVector pruneItemSets(FastVector toPrune, Hashtable kMinusOne)
  {
    FastVector newVector = new FastVector(toPrune.size());
    

    for (int i = 0; i < toPrune.size(); i++) {
      ItemSet current = (ItemSet)toPrune.elementAt(i);
      for (int j = 0; j < m_items.length; j++) {
        if (m_items[j] != -1) {
          int help = m_items[j];
          m_items[j] = -1;
          if (kMinusOne.get(current) == null) {
            m_items[j] = help;
            break;
          }
          m_items[j] = help;
        }
      }
      if (j == m_items.length)
        newVector.addElement(current);
    }
    return newVector;
  }
  







  public static void pruneRules(FastVector[] rules, double minConfidence)
  {
    FastVector newPremises = new FastVector(rules[0].size());
    FastVector newConsequences = new FastVector(rules[1].size());
    FastVector newConf = new FastVector(rules[2].size());
    
    for (int i = 0; i < rules[0].size(); i++)
      if (((Double)rules[2].elementAt(i)).doubleValue() >= minConfidence)
      {
        newPremises.addElement(rules[0].elementAt(i));
        newConsequences.addElement(rules[1].elementAt(i));
        newConf.addElement(rules[2].elementAt(i));
      }
    rules[0] = newPremises;
    rules[1] = newConsequences;
    rules[2] = newConf;
  }
  








  public static FastVector singletons(Instances instances)
    throws Exception
  {
    FastVector setOfItemSets = new FastVector();
    

    for (int i = 0; i < instances.numAttributes(); i++) {
      if (instances.attribute(i).isNumeric())
        throw new Exception("Can't handle numeric attributes!");
      for (int j = 0; j < instances.attribute(i).numValues(); j++) {
        ItemSet current = new ItemSet(instances.numInstances());
        m_items = new int[instances.numAttributes()];
        for (int k = 0; k < instances.numAttributes(); k++)
          m_items[k] = -1;
        m_items[i] = j;
        
        setOfItemSets.addElement(current);
      }
    }
    return setOfItemSets;
  }
  





  public int support()
  {
    return m_counter;
  }
  






  public String toString(Instances instances)
  {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < instances.numAttributes(); i++)
      if (m_items[i] != -1) {
        text.append(instances.attribute(i).name() + '=');
        text.append(instances.attribute(i).value(m_items[i]) + ' ');
      }
    text.append(m_counter);
    return text.toString();
  }
  





  public void upDateCounter(Instance instance)
  {
    if (containedBy(instance)) {
      m_counter += 1;
    }
  }
  





  public static void upDateCounters(FastVector itemSets, Instances instances)
  {
    for (int i = 0; i < instances.numInstances(); i++) {
      Enumeration enu = itemSets.elements();
      while (enu.hasMoreElements()) {
        ((ItemSet)enu.nextElement()).upDateCounter(instances.instance(i));
      }
    }
  }
  


  public int counter()
  {
    return m_counter;
  }
  



  public int[] items()
  {
    return m_items;
  }
  




  public int itemAt(int k)
  {
    return m_items[k];
  }
  



  public void setCounter(int count)
  {
    m_counter = count;
  }
  



  public void setItem(int[] items)
  {
    m_items = items;
  }
  




  public void setItemAt(int value, int k)
  {
    m_items[k] = value;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}

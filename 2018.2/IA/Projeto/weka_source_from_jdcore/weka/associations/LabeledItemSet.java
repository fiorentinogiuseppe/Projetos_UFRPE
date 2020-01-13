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















































public class LabeledItemSet
  extends ItemSet
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 4158771925518299903L;
  protected int m_classLabel;
  protected int m_ruleSupCounter;
  
  public LabeledItemSet(int totalTrans, int classLabel)
  {
    super(totalTrans);
    m_classLabel = classLabel;
  }
  











  public static FastVector deleteItemSets(FastVector itemSets, int minSupport, int maxSupport)
  {
    FastVector newVector = new FastVector(itemSets.size());
    
    for (int i = 0; i < itemSets.size(); i++) {
      LabeledItemSet current = (LabeledItemSet)itemSets.elementAt(i);
      if ((m_ruleSupCounter >= minSupport) && (m_ruleSupCounter <= maxSupport))
      {
        newVector.addElement(current); }
    }
    return newVector;
  }
  






  public final boolean equals(Object itemSet)
  {
    if (!equalCondset(itemSet))
      return false;
    if (m_classLabel != m_classLabel) {
      return false;
    }
    return true;
  }
  





  public final boolean equalCondset(Object itemSet)
  {
    if ((itemSet == null) || (!itemSet.getClass().equals(getClass()))) {
      return false;
    }
    if (m_items.length != ((ItemSet)itemSet).items().length)
      return false;
    for (int i = 0; i < m_items.length; i++)
      if (m_items[i] != ((ItemSet)itemSet).itemAt(i))
        return false;
    return true;
  }
  







  public static Hashtable getHashtable(FastVector itemSets, int initialSize)
  {
    Hashtable hashtable = new Hashtable(initialSize);
    for (int i = 0; i < itemSets.size(); i++) {
      LabeledItemSet current = (LabeledItemSet)itemSets.elementAt(i);
      hashtable.put(current, new Integer(m_classLabel));
    }
    
    return hashtable;
  }
  











  public static FastVector mergeAllItemSets(FastVector itemSets, int size, int totalTrans)
  {
    FastVector newVector = new FastVector();
    
    label321:
    
    for (int i = 0; i < itemSets.size(); i++) {
      LabeledItemSet first = (LabeledItemSet)itemSets.elementAt(i);
      
      for (int j = i + 1; j < itemSets.size(); j++) {
        LabeledItemSet second = (LabeledItemSet)itemSets.elementAt(j);
        while (m_classLabel != m_classLabel) {
          j++;
          if (j == itemSets.size())
            break label321;
          second = (LabeledItemSet)itemSets.elementAt(j);
        }
        LabeledItemSet result = new LabeledItemSet(totalTrans, m_classLabel);
        m_items = new int[m_items.length];
        

        int numFound = 0;
        int k = 0;
        while (numFound < size) {
          if (m_items[k] != m_items[k]) break label321;
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
          m_ruleSupCounter = 0;
          m_counter = 0;
          newVector.addElement(result);
        }
      }
    }
    
    return newVector;
  }
  






  public static Instances divide(Instances instances, boolean invert)
    throws Exception
  {
    Instances newInstances = new Instances(instances);
    if (instances.classIndex() < 0)
      throw new Exception("For class association rule mining a class attribute has to be specified.");
    if (invert) {
      for (int i = 0; i < newInstances.numAttributes(); i++) {
        if (i != newInstances.classIndex()) {
          newInstances.deleteAttributeAt(i);
          i--;
        }
      }
      return newInstances;
    }
    
    newInstances.setClassIndex(-1);
    newInstances.deleteAttributeAt(instances.classIndex());
    return newInstances;
  }
  











  public static FastVector singletons(Instances instancesNoClass, Instances classes)
    throws Exception
  {
    FastVector setOfItemSets = new FastVector();
    



    for (int i = 0; i < instancesNoClass.numAttributes(); i++) {
      if (instancesNoClass.attribute(i).isNumeric())
        throw new Exception("Can't handle numeric attributes!");
      for (int j = 0; j < instancesNoClass.attribute(i).numValues(); j++) {
        for (int k = 0; k < classes.attribute(0).numValues(); k++) {
          LabeledItemSet current = new LabeledItemSet(instancesNoClass.numInstances(), k);
          m_items = new int[instancesNoClass.numAttributes()];
          for (int l = 0; l < instancesNoClass.numAttributes(); l++)
            m_items[l] = -1;
          m_items[i] = j;
          setOfItemSets.addElement(current);
        }
      }
    }
    return setOfItemSets;
  }
  










  public static FastVector pruneItemSets(FastVector toPrune, Hashtable kMinusOne)
  {
    FastVector newVector = new FastVector(toPrune.size());
    


    for (int i = 0; i < toPrune.size(); i++) {
      LabeledItemSet current = (LabeledItemSet)toPrune.elementAt(i);
      
      for (int j = 0; j < m_items.length; j++) {
        if (m_items[j] != -1) {
          int help = m_items[j];
          m_items[j] = -1;
          if ((kMinusOne.get(current) != null) && (m_classLabel == ((Integer)kMinusOne.get(current)).intValue())) {
            m_items[j] = help;
          } else {
            m_items[j] = help;
            break;
          }
        }
      }
      if (j == m_items.length)
        newVector.addElement(current);
    }
    return newVector;
  }
  






  public final int support()
  {
    return m_ruleSupCounter;
  }
  








  public final void upDateCounter(Instance instanceNoClass, Instance instanceClass)
  {
    if (containedBy(instanceNoClass)) {
      m_counter += 1;
      if (m_classLabel == instanceClass.value(0)) {
        m_ruleSupCounter += 1;
      }
    }
  }
  





  public static void upDateCounters(FastVector itemSets, Instances instancesNoClass, Instances instancesClass)
  {
    for (int i = 0; i < instancesNoClass.numInstances(); i++) {
      Enumeration enu = itemSets.elements();
      while (enu.hasMoreElements()) {
        ((LabeledItemSet)enu.nextElement()).upDateCounter(instancesNoClass.instance(i), instancesClass.instance(i));
      }
    }
  }
  






  public final FastVector[] generateRules(double minConfidence, boolean noPrune)
  {
    FastVector premises = new FastVector();FastVector consequences = new FastVector();
    FastVector conf = new FastVector();
    FastVector[] rules = new FastVector[3];
    


    ItemSet premise = new ItemSet(m_totalTransactions);
    ItemSet consequence = new ItemSet(m_totalTransactions);
    int[] premiseItems = new int[m_items.length];
    int[] consequenceItems = new int[1];
    System.arraycopy(m_items, 0, premiseItems, 0, m_items.length);
    consequence.setItem(consequenceItems);
    premise.setItem(premiseItems);
    consequence.setItemAt(m_classLabel, 0);
    consequence.setCounter(m_ruleSupCounter);
    premise.setCounter(m_counter);
    premises.addElement(premise);
    consequences.addElement(consequence);
    conf.addElement(new Double(m_ruleSupCounter / m_counter));
    
    rules[0] = premises;
    rules[1] = consequences;
    rules[2] = conf;
    if (!noPrune) {
      pruneRules(rules, minConfidence);
    }
    
    return rules;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

package weka.associations;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.TreeSet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.UnassignedClassException;














































public class CaRuleGeneration
  extends RuleGeneration
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 3065752149646517703L;
  
  public CaRuleGeneration(ItemSet itemSet)
  {
    super(itemSet);
  }
  















  public TreeSet generateRules(int numRules, double[] midPoints, Hashtable priors, double expectation, Instances instances, TreeSet best, int genTime)
  {
    boolean redundant = false;
    FastVector consequences = new FastVector();
    
    RuleItem current = null;RuleItem old = null;
    


    m_change = false;
    m_midPoints = midPoints;
    m_priors = priors;
    m_best = best;
    m_expectation = expectation;
    m_count = genTime;
    m_instances = instances;
    

    ItemSet premise = null;
    premise = new ItemSet(m_totalTransactions);
    int[] premiseItems = new int[m_items.length];
    System.arraycopy(m_items, 0, premiseItems, 0, m_items.length);
    premise.setItem(premiseItems);
    premise.setCounter(m_counter);
    
    consequences = singleConsequence(instances);
    
    do
    {
      if ((premise == null) || (consequences.size() == 0))
        return m_best;
      m_minRuleCount = 1;
      while (expectation(m_minRuleCount, premise.counter(), m_midPoints, m_priors) <= m_expectation) {
        m_minRuleCount += 1;
        if (m_minRuleCount > premise.counter())
          return m_best;
      }
      redundant = false;
      

      FastVector allRuleItems = new FastVector();
      int h = 0;
      while (h < consequences.size()) {
        RuleItem dummie = new RuleItem();
        m_count += 1;
        current = dummie.generateRuleItem(premise, (ItemSet)consequences.elementAt(h), instances, m_count, m_minRuleCount, m_midPoints, m_priors);
        if (current != null)
          allRuleItems.addElement(current);
        h++;
      }
      

      for (h = 0; h < allRuleItems.size(); h++) {
        current = (RuleItem)allRuleItems.elementAt(h);
        if (m_best.size() < numRules) {
          m_change = true;
          redundant = removeRedundant(current);
        }
        else {
          m_expectation = ((RuleItem)m_best.first()).accuracy();
          if (current.accuracy() > m_expectation) {
            boolean remove = m_best.remove(m_best.first());
            m_change = true;
            redundant = removeRedundant(current);
            m_expectation = ((RuleItem)m_best.first()).accuracy();
            while (expectation(m_minRuleCount, current.premise().counter(), m_midPoints, m_priors) < m_expectation) {
              m_minRuleCount += 1;
              if (m_minRuleCount > current.premise().counter())
                break;
            }
          }
        }
      }
    } while (redundant);
    return m_best;
  }
  










  public static boolean aSubsumesB(RuleItem a, RuleItem b)
  {
    if (!a.consequence().equals(b.consequence()))
      return false;
    if (a.accuracy() < b.accuracy())
      return false;
    for (int k = 0; k < a.premise().items().length; k++) {
      if ((a.premise().itemAt(k) != b.premise().itemAt(k)) && (
        ((a.premise().itemAt(k) != -1) && (b.premise().itemAt(k) != -1)) || (b.premise().itemAt(k) == -1))) {
        return false;
      }
    }
    



    return true;
  }
  









  public static FastVector singletons(Instances instances)
    throws Exception
  {
    FastVector setOfItemSets = new FastVector();
    

    if (instances.classIndex() == -1)
      throw new UnassignedClassException("Class index is negative (not set)!");
    Attribute att = instances.classAttribute();
    for (int i = 0; i < instances.numAttributes(); i++) {
      if (instances.attribute(i).isNumeric())
        throw new Exception("Can't handle numeric attributes!");
      if (i != instances.classIndex()) {
        for (int j = 0; j < instances.attribute(i).numValues(); j++) {
          ItemSet current = new ItemSet(instances.numInstances());
          int[] currentItems = new int[instances.numAttributes()];
          for (int k = 0; k < instances.numAttributes(); k++)
            currentItems[k] = -1;
          currentItems[i] = j;
          current.setItem(currentItems);
          setOfItemSets.addElement(current);
        }
      }
    }
    return setOfItemSets;
  }
  






  public static FastVector singleConsequence(Instances instances)
  {
    FastVector consequences = new FastVector();
    
    for (int j = 0; j < instances.classAttribute().numValues(); j++) {
      ItemSet consequence = new ItemSet(instances.numInstances());
      int[] consequenceItems = new int[instances.numAttributes()];
      consequence.setItem(consequenceItems);
      for (int k = 0; k < instances.numAttributes(); k++)
        consequence.setItemAt(-1, k);
      consequence.setItemAt(j, instances.classIndex());
      consequences.addElement(consequence);
    }
    return consequences;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

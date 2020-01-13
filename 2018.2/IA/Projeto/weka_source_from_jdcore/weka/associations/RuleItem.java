package weka.associations;

import java.io.Serializable;
import java.util.Hashtable;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




















































public class RuleItem
  implements Comparable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -3761299128347476534L;
  protected ItemSet m_premise;
  protected ItemSet m_consequence;
  protected double m_accuracy;
  protected int m_genTime;
  
  public RuleItem() {}
  
  public RuleItem(RuleItem toCopy)
  {
    m_premise = m_premise;
    m_consequence = m_consequence;
    m_accuracy = m_accuracy;
    m_genTime = m_genTime;
  }
  










  public RuleItem(ItemSet premise, ItemSet consequence, int genTime, int ruleSupport, double[] m_midPoints, Hashtable m_priors)
  {
    m_premise = premise;
    m_consequence = consequence;
    m_accuracy = RuleGeneration.expectation(ruleSupport, m_premise.m_counter, m_midPoints, m_priors);
    
    if ((Double.isNaN(m_accuracy)) || (m_accuracy < 0.0D)) {
      m_accuracy = Double.MIN_VALUE;
    }
    m_consequence.m_counter = ruleSupport;
    m_genTime = genTime;
  }
  










  public RuleItem generateRuleItem(ItemSet premise, ItemSet consequence, Instances instances, int genTime, int minRuleCount, double[] m_midPoints, Hashtable m_priors)
  {
    ItemSet rule = new ItemSet(instances.numInstances());
    m_items = new int[m_items.length];
    System.arraycopy(m_items, 0, m_items, 0, m_items.length);
    for (int k = 0; k < m_items.length; k++) {
      if (m_items[k] != -1)
        m_items[k] = m_items[k];
    }
    for (int i = 0; i < instances.numInstances(); i++)
      rule.upDateCounter(instances.instance(i));
    int ruleSupport = rule.support();
    if (ruleSupport > minRuleCount) {
      RuleItem newRule = new RuleItem(premise, consequence, genTime, ruleSupport, m_midPoints, m_priors);
      return newRule;
    }
    return null;
  }
  








  public int compareTo(Object o)
  {
    if (m_accuracy == m_accuracy) {
      if (m_genTime == m_genTime)
        return 0;
      if (m_genTime > m_genTime)
        return -1;
      if (m_genTime < m_genTime)
        return 1;
    }
    if (m_accuracy < m_accuracy)
      return -1;
    return 1;
  }
  





  public boolean equals(Object o)
  {
    if (o == null)
      return false;
    if ((m_premise.equals(m_premise)) && (m_consequence.equals(m_consequence)))
      return true;
    return false;
  }
  




  public double accuracy()
  {
    return m_accuracy;
  }
  




  public ItemSet premise()
  {
    return m_premise;
  }
  




  public ItemSet consequence()
  {
    return m_consequence;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

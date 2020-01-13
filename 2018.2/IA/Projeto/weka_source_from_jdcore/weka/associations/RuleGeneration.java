package weka.associations;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.TreeSet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;















































public class RuleGeneration
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8927041669872491432L;
  protected int[] m_items;
  protected int m_counter;
  protected int m_totalTransactions;
  protected boolean m_change = false;
  


  protected double m_expectation;
  


  protected static final int MAX_N = 300;
  


  protected int m_minRuleCount;
  


  protected double[] m_midPoints;
  

  protected Hashtable m_priors;
  

  protected TreeSet m_best;
  

  protected int m_count;
  

  protected Instances m_instances;
  


  public RuleGeneration(ItemSet itemSet)
  {
    m_totalTransactions = m_totalTransactions;
    m_counter = m_counter;
    m_items = m_items;
  }
  












  public static final double binomialDistribution(double accuracy, double ruleCount, double premiseCount)
  {
    if (premiseCount < 300.0D) {
      return Math.pow(2.0D, Utils.log2(Math.pow(accuracy, ruleCount)) + Utils.log2(Math.pow(1.0D - accuracy, premiseCount - ruleCount)) + PriorEstimation.logbinomialCoefficient((int)premiseCount, (int)ruleCount));
    }
    double mu = premiseCount * accuracy;
    double sigma = Math.sqrt(premiseCount * (1.0D - accuracy) * accuracy);
    return Statistics.normalProbability((ruleCount + 0.5D - mu) / (sigma * Math.sqrt(2.0D)));
  }
  









  public static final double expectation(double ruleCount, int premiseCount, double[] midPoints, Hashtable priors)
  {
    double numerator = 0.0D;double denominator = 0.0D;
    for (int i = 0; i < midPoints.length; i++) {
      Double actualPrior = (Double)priors.get(new Double(midPoints[i]));
      if ((actualPrior != null) && 
        (actualPrior.doubleValue() != 0.0D)) {
        double addend = actualPrior.doubleValue() * binomialDistribution(midPoints[i], ruleCount, premiseCount);
        denominator += addend;
        numerator += addend * midPoints[i];
      }
    }
    
    if ((denominator <= 0.0D) || (Double.isNaN(denominator)))
      System.out.println("RuleItem denominator: " + denominator);
    if ((numerator <= 0.0D) || (Double.isNaN(numerator)))
      System.out.println("RuleItem numerator: " + numerator);
    return numerator / denominator;
  }
  















  public TreeSet generateRules(int numRules, double[] midPoints, Hashtable priors, double expectation, Instances instances, TreeSet best, int genTime)
  {
    boolean redundant = false;
    FastVector consequences = new FastVector();FastVector consequencesMinusOne = new FastVector();
    
    int s = 0;
    RuleItem current = null;
    


    m_change = false;
    m_midPoints = midPoints;
    m_priors = priors;
    m_best = best;
    m_expectation = expectation;
    m_count = genTime;
    m_instances = instances;
    

    ItemSet premise = null;
    premise = new ItemSet(m_totalTransactions);
    m_items = new int[m_items.length];
    System.arraycopy(m_items, 0, m_items, 0, m_items.length);
    m_counter = m_counter;
    
    do
    {
      m_minRuleCount = 1;
      while (expectation(m_minRuleCount, m_counter, m_midPoints, m_priors) <= m_expectation) {
        m_minRuleCount += 1;
        if (m_minRuleCount > m_counter)
          return m_best;
      }
      redundant = false;
      for (int i = 0; i < instances.numAttributes(); i++) {
        if (i == 0) {
          for (int j = 0; j < m_items.length; j++)
            if (m_items[j] == -1)
              consequences = singleConsequence(instances, j, consequences);
          if ((premise == null) || (consequences.size() == 0))
            return m_best;
        }
        FastVector allRuleItems = new FastVector();
        int index = 0;
        do {
          int h = 0;
          while (h < consequences.size()) {
            RuleItem dummie = new RuleItem();
            current = dummie.generateRuleItem(premise, (ItemSet)consequences.elementAt(h), instances, m_count, m_minRuleCount, m_midPoints, m_priors);
            if (current != null) {
              allRuleItems.addElement(current);
              h++;
            }
            else {
              consequences.removeElementAt(h);
            } }
          if (index == i)
            break;
          consequencesMinusOne = consequences;
          consequences = ItemSet.mergeAllItemSets(consequencesMinusOne, index, instances.numInstances());
          Hashtable hashtable = ItemSet.getHashtable(consequencesMinusOne, consequencesMinusOne.size());
          consequences = ItemSet.pruneItemSets(consequences, hashtable);
          index++;
        } while (consequences.size() > 0);
        for (int h = 0; h < allRuleItems.size(); h++) {
          current = (RuleItem)allRuleItems.elementAt(h);
          m_count += 1;
          if (m_best.size() < numRules) {
            m_change = true;
            redundant = removeRedundant(current);

          }
          else if (current.accuracy() > m_expectation) {
            m_expectation = ((RuleItem)m_best.first()).accuracy();
            boolean remove = m_best.remove(m_best.first());
            m_change = true;
            redundant = removeRedundant(current);
            m_expectation = ((RuleItem)m_best.first()).accuracy();
            while (expectation(m_minRuleCount, premisem_counter, m_midPoints, m_priors) < m_expectation) {
              m_minRuleCount += 1;
              if (m_minRuleCount > premisem_counter) {
                break;
              }
              
            }
          }
        }
      }
    } while (redundant);
    return m_best;
  }
  










  public static boolean aSubsumesB(RuleItem a, RuleItem b)
  {
    if (m_accuracy < m_accuracy)
      return false;
    for (int k = 0; k < premisem_items.length; k++) {
      if ((premisem_items[k] != premisem_items[k]) && (
        ((premisem_items[k] != -1) && (premisem_items[k] != -1)) || (premisem_items[k] == -1))) {
        return false;
      }
      if ((consequencem_items[k] != consequencem_items[k]) && (
        ((consequencem_items[k] != -1) && (consequencem_items[k] != -1)) || (consequencem_items[k] == -1))) {
        return false;
      }
    }
    return true;
  }
  










  public static FastVector singleConsequence(Instances instances, int attNum, FastVector consequences)
  {
    for (int i = 0; i < instances.numAttributes(); i++) {
      if (i == attNum) {
        for (int j = 0; j < instances.attribute(i).numValues(); j++) {
          ItemSet consequence = new ItemSet(instances.numInstances());
          m_items = new int[instances.numAttributes()];
          for (int k = 0; k < instances.numAttributes(); k++)
            m_items[k] = -1;
          m_items[i] = j;
          consequences.addElement(consequence);
        }
      }
    }
    return consequences;
  }
  









  public boolean removeRedundant(RuleItem toInsert)
  {
    boolean redundant = false;boolean fSubsumesT = false;boolean tSubsumesF = false;
    
    int subsumes = 0;
    Object[] best = m_best.toArray();
    for (int i = 0; i < best.length; i++) {
      RuleItem first = (RuleItem)best[i];
      fSubsumesT = aSubsumesB(first, toInsert);
      tSubsumesF = aSubsumesB(toInsert, first);
      if (fSubsumesT) {
        subsumes = 1;
        break;
      }
      
      if (tSubsumesF) {
        boolean remove = m_best.remove(first);
        subsumes = 2;
        redundant = true;
      }
    }
    
    if ((subsumes == 0) || (subsumes == 2))
      m_best.add(toInsert);
    return redundant;
  }
  




  public int count()
  {
    return m_count;
  }
  




  public boolean change()
  {
    return m_change;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

package weka.associations;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import weka.core.Attribute;
import weka.core.ContingencyTables;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;





































public class AprioriItemSet
  extends ItemSet
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = 7684467755712672058L;
  
  public AprioriItemSet(int totalTrans)
  {
    super(totalTrans);
  }
  








  public static double confidenceForRule(AprioriItemSet premise, AprioriItemSet consequence)
  {
    return m_counter / m_counter;
  }
  










  public double liftForRule(AprioriItemSet premise, AprioriItemSet consequence, int consequenceCount)
  {
    double confidence = confidenceForRule(premise, consequence);
    
    return confidence / (consequenceCount / m_totalTransactions);
  }
  













  public double leverageForRule(AprioriItemSet premise, AprioriItemSet consequence, int premiseCount, int consequenceCount)
  {
    double coverageForItemSet = m_counter / m_totalTransactions;
    
    double expectedCoverageIfIndependent = premiseCount / m_totalTransactions * (consequenceCount / m_totalTransactions);
    
    double lev = coverageForItemSet - expectedCoverageIfIndependent;
    return lev;
  }
  












  public double convictionForRule(AprioriItemSet premise, AprioriItemSet consequence, int premiseCount, int consequenceCount)
  {
    double num = premiseCount * (m_totalTransactions - consequenceCount) / m_totalTransactions;
    

    double denom = premiseCount - m_counter + 1;
    
    if ((num < 0.0D) || (denom < 0.0D)) {
      System.err.println("*** " + num + " " + denom);
      System.err.println("premis count: " + premiseCount + " consequence count " + consequenceCount + " total trans " + m_totalTransactions);
    }
    

    return num / denom;
  }
  










  public FastVector[] generateRules(double minConfidence, FastVector hashtables, int numItemsInSet)
  {
    FastVector premises = new FastVector();FastVector consequences = new FastVector();FastVector conf = new FastVector();
    FastVector[] rules = new FastVector[3];
    
    Hashtable hashtable = (Hashtable)hashtables.elementAt(numItemsInSet - 2);
    

    for (int i = 0; i < m_items.length; i++)
      if (m_items[i] != -1) {
        AprioriItemSet premise = new AprioriItemSet(m_totalTransactions);
        AprioriItemSet consequence = new AprioriItemSet(m_totalTransactions);
        m_items = new int[m_items.length];
        m_items = new int[m_items.length];
        m_counter = m_counter;
        
        for (int j = 0; j < m_items.length; j++)
          m_items[j] = -1;
        System.arraycopy(m_items, 0, m_items, 0, m_items.length);
        m_items[i] = -1;
        
        m_items[i] = m_items[i];
        m_counter = ((Integer)hashtable.get(premise)).intValue();
        premises.addElement(premise);
        consequences.addElement(consequence);
        conf.addElement(new Double(confidenceForRule(premise, consequence)));
      }
    rules[0] = premises;
    rules[1] = consequences;
    rules[2] = conf;
    pruneRules(rules, minConfidence);
    

    FastVector[] moreResults = moreComplexRules(rules, numItemsInSet, 1, minConfidence, hashtables);
    
    if (moreResults != null)
      for (int i = 0; i < moreResults[0].size(); i++) {
        rules[0].addElement(moreResults[0].elementAt(i));
        rules[1].addElement(moreResults[1].elementAt(i));
        rules[2].addElement(moreResults[2].elementAt(i));
      }
    return rules;
  }
  















  public final FastVector[] generateRulesBruteForce(double minMetric, int metricType, FastVector hashtables, int numItemsInSet, int numTransactions, double significanceLevel)
    throws Exception
  {
    FastVector premises = new FastVector();FastVector consequences = new FastVector();FastVector conf = new FastVector();FastVector lift = new FastVector();FastVector lev = new FastVector();FastVector conv = new FastVector();
    FastVector[] rules = new FastVector[6];
    


    double[][] contingencyTable = new double[2][2];
    double chiSquared = 0.0D;
    


    int max = (int)Math.pow(2.0D, numItemsInSet);
    for (int j = 1; j < max; j++) {
      int numItemsInPremise = 0;
      int help = j;
      while (help > 0) {
        if (help % 2 == 1)
          numItemsInPremise++;
        help /= 2;
      }
      if (numItemsInPremise < numItemsInSet) {
        Hashtable hashtableForPremise = (Hashtable)hashtables.elementAt(numItemsInPremise - 1);
        
        Hashtable hashtableForConsequence = (Hashtable)hashtables.elementAt(numItemsInSet - numItemsInPremise - 1);
        
        AprioriItemSet premise = new AprioriItemSet(m_totalTransactions);
        AprioriItemSet consequence = new AprioriItemSet(m_totalTransactions);
        m_items = new int[m_items.length];
        
        m_items = new int[m_items.length];
        m_counter = m_counter;
        help = j;
        for (int i = 0; i < m_items.length; i++)
          if (m_items[i] != -1) {
            if (help % 2 == 1) {
              m_items[i] = m_items[i];
              m_items[i] = -1;
            } else {
              m_items[i] = -1;
              m_items[i] = m_items[i];
            }
            help /= 2;
          } else {
            m_items[i] = -1;
            m_items[i] = -1;
          }
        m_counter = ((Integer)hashtableForPremise.get(premise)).intValue();
        
        int consequenceUnconditionedCounter = ((Integer)hashtableForConsequence.get(consequence)).intValue();
        

        if (significanceLevel != -1.0D) {
          contingencyTable[0][0] = m_counter;
          contingencyTable[0][1] = (m_counter - m_counter);
          contingencyTable[1][0] = (consequenceUnconditionedCounter - m_counter);
          contingencyTable[1][1] = (numTransactions - m_counter - consequenceUnconditionedCounter + m_counter);
          
          chiSquared = ContingencyTables.chiSquared(contingencyTable, false);
        }
        
        if (metricType == 0)
        {
          double metric = confidenceForRule(premise, consequence);
          
          if ((metric >= minMetric) && ((significanceLevel == -1.0D) || (chiSquared <= significanceLevel)))
          {
            premises.addElement(premise);
            consequences.addElement(consequence);
            conf.addElement(new Double(metric));
            lift.addElement(new Double(liftForRule(premise, consequence, consequenceUnconditionedCounter)));
            
            lev.addElement(new Double(leverageForRule(premise, consequence, m_counter, consequenceUnconditionedCounter)));
            
            conv.addElement(new Double(convictionForRule(premise, consequence, m_counter, consequenceUnconditionedCounter)));
          }
        }
        else {
          double tempConf = confidenceForRule(premise, consequence);
          double tempLift = liftForRule(premise, consequence, consequenceUnconditionedCounter);
          
          double tempLev = leverageForRule(premise, consequence, m_counter, consequenceUnconditionedCounter);
          
          double tempConv = convictionForRule(premise, consequence, m_counter, consequenceUnconditionedCounter);
          double metric;
          switch (metricType) {
          case 1: 
            metric = tempLift;
            break;
          case 2: 
            metric = tempLev;
            break;
          case 3: 
            metric = tempConv;
            break;
          default: 
            throw new Exception("ItemSet: Unknown metric type!");
          }
          if ((metric >= minMetric) && ((significanceLevel == -1.0D) || (chiSquared <= significanceLevel)))
          {
            premises.addElement(premise);
            consequences.addElement(consequence);
            conf.addElement(new Double(tempConf));
            lift.addElement(new Double(tempLift));
            lev.addElement(new Double(tempLev));
            conv.addElement(new Double(tempConv));
          }
        }
      }
    }
    rules[0] = premises;
    rules[1] = consequences;
    rules[2] = conf;
    rules[3] = lift;
    rules[4] = lev;
    rules[5] = conv;
    return rules;
  }
  







  public final AprioriItemSet subtract(AprioriItemSet toSubtract)
  {
    AprioriItemSet result = new AprioriItemSet(m_totalTransactions);
    
    m_items = new int[m_items.length];
    
    for (int i = 0; i < m_items.length; i++)
      if (m_items[i] == -1) {
        m_items[i] = m_items[i];
      } else
        m_items[i] = -1;
    m_counter = 0;
    return result;
  }
  
















  private final FastVector[] moreComplexRules(FastVector[] rules, int numItemsInSet, int numItemsInConsequence, double minConfidence, FastVector hashtables)
  {
    FastVector newPremises = new FastVector();FastVector newConf = new FastVector();
    

    if (numItemsInSet > numItemsInConsequence + 1) {
      Hashtable hashtable = (Hashtable)hashtables.elementAt(numItemsInSet - numItemsInConsequence - 2);
      
      FastVector newConsequences = mergeAllItemSets(rules[1], numItemsInConsequence - 1, m_totalTransactions);
      
      Enumeration enu = newConsequences.elements();
      while (enu.hasMoreElements()) {
        AprioriItemSet current = (AprioriItemSet)enu.nextElement();
        m_counter = m_counter;
        AprioriItemSet newPremise = subtract(current);
        m_counter = ((Integer)hashtable.get(newPremise)).intValue();
        newPremises.addElement(newPremise);
        newConf.addElement(new Double(confidenceForRule(newPremise, current)));
      }
      FastVector[] result = new FastVector[3];
      result[0] = newPremises;
      result[1] = newConsequences;
      result[2] = newConf;
      pruneRules(result, minConfidence);
      FastVector[] moreResults = moreComplexRules(result, numItemsInSet, numItemsInConsequence + 1, minConfidence, hashtables);
      
      if (moreResults != null)
        for (int i = 0; i < moreResults[0].size(); i++) {
          result[0].addElement(moreResults[0].elementAt(i));
          result[1].addElement(moreResults[1].elementAt(i));
          result[2].addElement(moreResults[2].elementAt(i));
        }
      return result;
    }
    return null;
  }
  







  public final String toString(Instances instances)
  {
    return super.toString(instances);
  }
  








  public static FastVector singletons(Instances instances)
    throws Exception
  {
    FastVector setOfItemSets = new FastVector();
    

    for (int i = 0; i < instances.numAttributes(); i++) {
      if (instances.attribute(i).isNumeric())
        throw new Exception("Can't handle numeric attributes!");
      for (int j = 0; j < instances.attribute(i).numValues(); j++) {
        ItemSet current = new AprioriItemSet(instances.numInstances());
        m_items = new int[instances.numAttributes()];
        for (int k = 0; k < instances.numAttributes(); k++)
          m_items[k] = -1;
        m_items[i] = j;
        setOfItemSets.addElement(current);
      }
    }
    return setOfItemSets;
  }
  










  public static FastVector mergeAllItemSets(FastVector itemSets, int size, int totalTrans)
  {
    FastVector newVector = new FastVector();
    
    label268:
    
    for (int i = 0; i < itemSets.size(); i++) {
      ItemSet first = (ItemSet)itemSets.elementAt(i);
      for (int j = i + 1; j < itemSets.size(); j++) {
        ItemSet second = (ItemSet)itemSets.elementAt(j);
        ItemSet result = new AprioriItemSet(totalTrans);
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
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9096 $");
  }
}

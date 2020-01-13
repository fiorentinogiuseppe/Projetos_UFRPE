package weka.associations;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Random;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SpecialFunctions;
import weka.core.Utils;




































































public class PriorEstimation
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 5570863216522496271L;
  protected int m_numRandRules;
  protected int m_numIntervals;
  protected static final int SEED = 0;
  protected static final int MAX_N = 1024;
  protected Random m_randNum;
  protected Instances m_instances;
  protected boolean m_CARs;
  protected Hashtable m_distribution;
  protected Hashtable m_priors;
  protected double m_sum;
  protected double[] m_midPoints;
  
  public PriorEstimation(Instances instances, int numRules, int numIntervals, boolean car)
  {
    m_instances = instances;
    m_CARs = car;
    m_numRandRules = numRules;
    m_numIntervals = numIntervals;
    m_randNum = m_instances.getRandomNumberGenerator(0L);
  }
  




  public final void generateDistribution()
    throws Exception
  {
    int maxLength = m_instances.numAttributes();int count = 0;int count1 = 0;
    
    m_distribution = new Hashtable(maxLength * m_numIntervals);
    


    if (m_instances.numAttributes() == 0)
      throw new Exception("Dataset has no attributes!");
    if (m_instances.numAttributes() >= 1024)
      throw new Exception("Dataset has to many attributes for prior estimation!");
    if (m_instances.numInstances() == 0)
      throw new Exception("Dataset has no instances!");
    for (int h = 0; h < maxLength; h++) {
      if (m_instances.attribute(h).isNumeric())
        throw new Exception("Can't handle numeric attributes!");
    }
    if ((m_numIntervals == 0) || (m_numRandRules == 0)) {
      throw new Exception("Prior initialisation impossible");
    }
    
    midPoints();
    

    for (int i = 1; i <= maxLength; i++) {
      m_sum = 0.0D;
      int j = 0;
      count = 0;
      count1 = 0;
      while (j < m_numRandRules) {
        count++;
        boolean jump = false;
        RuleItem current; int[] itemArray; RuleItem current; if (!m_CARs) {
          int[] itemArray = randomRule(maxLength, i, m_randNum);
          current = splitItemSet(m_randNum.nextInt(i), itemArray);
        }
        else {
          itemArray = randomCARule(maxLength, i, m_randNum);
          current = addCons(itemArray);
        }
        int[] ruleItem = new int[maxLength];
        for (int k = 0; k < itemArray.length; k++) {
          if (m_premise.m_items[k] != -1) {
            ruleItem[k] = m_premise.m_items[k];
          }
          else if (m_consequence.m_items[k] != -1) {
            ruleItem[k] = m_consequence.m_items[k];
          } else
            ruleItem[k] = -1;
        }
        ItemSet rule = new ItemSet(ruleItem);
        updateCounters(rule);
        int ruleCounter = m_counter;
        if (ruleCounter > 0)
          jump = true;
        updateCounters(m_premise);
        j++;
        if (jump) {
          buildDistribution(ruleCounter / m_premise.m_counter, i);
        }
      }
      

      if (m_sum > 0.0D) {
        for (int w = 0; w < m_midPoints.length; w++) {
          String key = String.valueOf(m_midPoints[w]).concat(String.valueOf(i));
          Double oldValue = (Double)m_distribution.remove(key);
          if (oldValue == null) {
            m_distribution.put(key, new Double(1.0D / m_numIntervals));
            m_sum += 1.0D / m_numIntervals;
          }
          else {
            m_distribution.put(key, oldValue);
          } }
        for (int w = 0; w < m_midPoints.length; w++) {
          double conf = 0.0D;
          String key = String.valueOf(m_midPoints[w]).concat(String.valueOf(i));
          Double oldValue = (Double)m_distribution.remove(key);
          if (oldValue != null) {
            conf = oldValue.doubleValue() / m_sum;
            m_distribution.put(key, new Double(conf));
          }
        }
      }
      else {
        for (int w = 0; w < m_midPoints.length; w++) {
          String key = String.valueOf(m_midPoints[w]).concat(String.valueOf(i));
          m_distribution.put(key, new Double(1.0D / m_numIntervals));
        }
      }
    }
  }
  









  public final int[] randomRule(int maxLength, int actualLength, Random randNum)
  {
    int[] itemArray = new int[maxLength];
    for (int k = 0; k < itemArray.length; k++)
      itemArray[k] = -1;
    int help = actualLength;
    if (help == maxLength) {
      help = 0;
      for (int h = 0; h < itemArray.length; h++) {
        itemArray[h] = m_randNum.nextInt(m_instances.attribute(h).numValues());
      }
    }
    while (help > 0) {
      int mark = randNum.nextInt(maxLength);
      if (itemArray[mark] == -1) {
        help--;
        itemArray[mark] = m_randNum.nextInt(m_instances.attribute(mark).numValues());
      }
    }
    return itemArray;
  }
  









  public final int[] randomCARule(int maxLength, int actualLength, Random randNum)
  {
    int[] itemArray = new int[maxLength];
    for (int k = 0; k < itemArray.length; k++)
      itemArray[k] = -1;
    if (actualLength == 1)
      return itemArray;
    int help = actualLength - 1;
    if (help == maxLength - 1) {
      help = 0;
      for (int h = 0; h < itemArray.length; h++) {
        if (h != m_instances.classIndex()) {
          itemArray[h] = m_randNum.nextInt(m_instances.attribute(h).numValues());
        }
      }
    }
    while (help > 0) {
      int mark = randNum.nextInt(maxLength);
      if ((itemArray[mark] == -1) && (mark != m_instances.classIndex())) {
        help--;
        itemArray[mark] = m_randNum.nextInt(m_instances.attribute(mark).numValues());
      }
    }
    return itemArray;
  }
  








  public final void buildDistribution(double conf, double length)
  {
    double mPoint = findIntervall(conf);
    String key = String.valueOf(mPoint).concat(String.valueOf(length));
    m_sum += conf;
    Double oldValue = (Double)m_distribution.remove(key);
    if (oldValue != null)
      conf += oldValue.doubleValue();
    m_distribution.put(key, new Double(conf));
  }
  






  public final double findIntervall(double conf)
  {
    if (conf == 1.0D)
      return m_midPoints[(m_midPoints.length - 1)];
    int end = m_midPoints.length - 1;
    int start = 0;
    while (Math.abs(end - start) > 1) {
      int mid = (start + end) / 2;
      if (conf > m_midPoints[mid])
        start = mid + 1;
      if (conf < m_midPoints[mid])
        end = mid - 1;
      if (conf == m_midPoints[mid])
        return m_midPoints[mid];
    }
    if (Math.abs(conf - m_midPoints[start]) <= Math.abs(conf - m_midPoints[end])) {
      return m_midPoints[start];
    }
    return m_midPoints[end];
  }
  







  public final double calculatePriorSum(boolean weighted, double mPoint)
  {
    double sum = 0.0D;double max = logbinomialCoefficient(m_instances.numAttributes(), m_instances.numAttributes() / 2);
    

    for (int i = 1; i <= m_instances.numAttributes(); i++)
    {
      if (weighted) {
        String key = String.valueOf(mPoint).concat(String.valueOf(i));
        Double hashValue = (Double)m_distribution.get(key);
        double distr;
        double distr; if (hashValue != null) {
          distr = hashValue.doubleValue();
        } else {
          distr = 0.0D;
        }
        if (distr != 0.0D) {
          double addend = Utils.log2(distr) - max + Utils.log2(Math.pow(2.0D, i) - 1.0D) + logbinomialCoefficient(m_instances.numAttributes(), i);
          sum += Math.pow(2.0D, addend);
        }
      }
      else {
        double addend = Utils.log2(Math.pow(2.0D, i) - 1.0D) - max + logbinomialCoefficient(m_instances.numAttributes(), i);
        sum += Math.pow(2.0D, addend);
      }
    }
    return sum;
  }
  





  public static final double logbinomialCoefficient(int upperIndex, int lowerIndex)
  {
    double result = 1.0D;
    if ((upperIndex == lowerIndex) || (lowerIndex == 0))
      return result;
    result = SpecialFunctions.log2Binomial(upperIndex, lowerIndex);
    return result;
  }
  






  public final Hashtable estimatePrior()
    throws Exception
  {
    Hashtable m_priors = new Hashtable(m_numIntervals);
    double denominator = calculatePriorSum(false, 1.0D);
    generateDistribution();
    for (int i = 0; i < m_numIntervals; i++) {
      double mPoint = m_midPoints[i];
      double prior = calculatePriorSum(true, mPoint) / denominator;
      m_priors.put(new Double(mPoint), new Double(prior));
    }
    return m_priors;
  }
  



  public final void midPoints()
  {
    m_midPoints = new double[m_numIntervals];
    for (int i = 0; i < m_numIntervals; i++) {
      m_midPoints[i] = midPoint(1.0D / m_numIntervals, i);
    }
  }
  






  public double midPoint(double size, int number)
  {
    return size * number + size / 2.0D;
  }
  




  public final double[] getMidPoints()
  {
    return m_midPoints;
  }
  









  public final RuleItem splitItemSet(int premiseLength, int[] itemArray)
  {
    int[] cons = new int[m_instances.numAttributes()];
    System.arraycopy(itemArray, 0, cons, 0, itemArray.length);
    int help = premiseLength;
    while (help > 0) {
      int mark = m_randNum.nextInt(itemArray.length);
      if (cons[mark] != -1) {
        help--;
        cons[mark] = -1;
      }
    }
    if (premiseLength == 0) {
      for (int i = 0; i < itemArray.length; i++)
        itemArray[i] = -1;
    } else
      for (int i = 0; i < itemArray.length; i++)
        if (cons[i] != -1)
          itemArray[i] = -1;
    ItemSet premise = new ItemSet(itemArray);
    ItemSet consequence = new ItemSet(cons);
    RuleItem current = new RuleItem();
    m_premise = premise;
    m_consequence = consequence;
    return current;
  }
  






  public final RuleItem addCons(int[] itemArray)
  {
    ItemSet premise = new ItemSet(itemArray);
    int[] cons = new int[itemArray.length];
    for (int i = 0; i < itemArray.length; i++)
      cons[i] = -1;
    cons[m_instances.classIndex()] = m_randNum.nextInt(m_instances.attribute(m_instances.classIndex()).numValues());
    ItemSet consequence = new ItemSet(cons);
    RuleItem current = new RuleItem();
    m_premise = premise;
    m_consequence = consequence;
    return current;
  }
  




  public final void updateCounters(ItemSet itemSet)
  {
    for (int i = 0; i < m_instances.numInstances(); i++) {
      itemSet.upDateCounter(m_instances.instance(i));
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}

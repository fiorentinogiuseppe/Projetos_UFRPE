package weka.associations;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;













































































































public class PredictiveApriori
  extends AbstractAssociator
  implements OptionHandler, CARuleMiner, TechnicalInformationHandler
{
  static final long serialVersionUID = 8109088846865075341L;
  protected int m_premiseCount;
  protected int m_numRules;
  protected static final int m_numRandRules = 1000;
  protected static final int m_numIntervals = 100;
  protected FastVector m_Ls;
  protected FastVector m_hashtables;
  protected FastVector[] m_allTheRules;
  protected Instances m_instances;
  protected Hashtable m_priors;
  protected double[] m_midPoints;
  protected double m_expectation;
  protected TreeSet m_best;
  protected boolean m_bestChanged;
  protected int m_count;
  protected PriorEstimation m_priorEstimator;
  protected int m_classIndex;
  protected boolean m_car;
  
  public String globalInfo()
  {
    return "Class implementing the predictive apriori algorithm to mine association rules.\nIt searches with an increasing support threshold for the best 'n' rules concerning a support-based corrected confidence value.\n\nFor more information see:\n\n" + getTechnicalInformation().toString() + "\n\n" + "The implementation follows the paper expect for adding a rule to the " + "output of the 'n' best rules. A rule is added if:\n" + "the expected predictive accuracy of this rule is among the 'n' best " + "and it is not subsumed by a rule with at least the same expected " + "predictive accuracy (out of an unpublished manuscript from T. " + "Scheffer).";
  }
  




















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Tobias Scheffer");
    result.setValue(TechnicalInformation.Field.TITLE, "Finding Association Rules That Trade Support Optimally against Confidence");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "5th European Conference on Principles of Data Mining and Knowledge Discovery");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.PAGES, "424-435");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  





  public PredictiveApriori()
  {
    resetOptions();
  }
  



  public void resetOptions()
  {
    m_numRules = 105;
    m_premiseCount = 1;
    m_best = new TreeSet();
    m_bestChanged = false;
    m_expectation = 0.0D;
    m_count = 1;
    m_car = false;
    m_classIndex = -1;
    m_priors = new Hashtable();
  }
  







  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NO_CLASS);
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildAssociations(Instances instances)
    throws Exception
  {
    int temp = m_premiseCount;int exactNumber = m_numRules - 5;
    
    m_premiseCount = 1;
    m_best = new TreeSet();
    m_bestChanged = false;
    m_expectation = 0.0D;
    m_count = 1;
    m_instances = new Instances(instances);
    
    if (m_classIndex == -1) {
      m_instances.setClassIndex(m_instances.numAttributes() - 1);
    } else if ((m_classIndex < m_instances.numAttributes()) && (m_classIndex >= 0)) {
      m_instances.setClassIndex(m_classIndex);
    } else {
      throw new Exception("Invalid class index.");
    }
    
    getCapabilities().testWithFail(m_instances);
    

    m_priorEstimator = new PriorEstimation(m_instances, 1000, 100, m_car);
    m_priors = m_priorEstimator.estimatePrior();
    m_midPoints = m_priorEstimator.getMidPoints();
    
    m_Ls = new FastVector();
    m_hashtables = new FastVector();
    
    for (int i = 1; i < m_instances.numAttributes(); i++) {
      m_bestChanged = false;
      if (!m_car)
      {
        findLargeItemSets(i);
        

        findRulesQuickly();
      }
      else {
        findLargeCarItemSets(i);
        findCaRulesQuickly();
      }
      
      if (m_bestChanged) {
        temp = m_premiseCount;
        while (RuleGeneration.expectation(m_premiseCount, m_premiseCount, m_midPoints, m_priors) <= m_expectation) {
          m_premiseCount += 1;
          if (m_premiseCount > m_instances.numInstances())
            break;
        }
      }
      if (m_premiseCount > m_instances.numInstances())
      {

        m_allTheRules = new FastVector[3];
        m_allTheRules[0] = new FastVector();
        m_allTheRules[1] = new FastVector();
        m_allTheRules[2] = new FastVector();
        
        int k = 0;
        while ((m_best.size() > 0) && (exactNumber > 0)) {
          m_allTheRules[0].insertElementAt(((RuleItem)m_best.last()).premise(), k);
          m_allTheRules[1].insertElementAt(((RuleItem)m_best.last()).consequence(), k);
          m_allTheRules[2].insertElementAt(new Double(((RuleItem)m_best.last()).accuracy()), k);
          m_best.remove(m_best.last());
          k++;
          exactNumber--;
        }
        return;
      }
      
      if ((temp != m_premiseCount) && (m_Ls.size() > 0)) {
        FastVector kSets = (FastVector)m_Ls.lastElement();
        m_Ls.removeElementAt(m_Ls.size() - 1);
        kSets = ItemSet.deleteItemSets(kSets, m_premiseCount, Integer.MAX_VALUE);
        m_Ls.addElement(kSets);
      }
    }
    

    m_allTheRules = new FastVector[3];
    m_allTheRules[0] = new FastVector();
    m_allTheRules[1] = new FastVector();
    m_allTheRules[2] = new FastVector();
    
    int k = 0;
    while ((m_best.size() > 0) && (exactNumber > 0)) {
      m_allTheRules[0].insertElementAt(((RuleItem)m_best.last()).premise(), k);
      m_allTheRules[1].insertElementAt(((RuleItem)m_best.last()).consequence(), k);
      m_allTheRules[2].insertElementAt(new Double(((RuleItem)m_best.last()).accuracy()), k);
      m_best.remove(m_best.last());
      k++;
      exactNumber--;
    }
  }
  





  public FastVector[] mineCARs(Instances data)
    throws Exception
  {
    m_car = true;
    m_best = new TreeSet();
    m_premiseCount = 1;
    m_bestChanged = false;
    m_expectation = 0.0D;
    m_count = 1;
    buildAssociations(data);
    FastVector[] allCARRules = new FastVector[3];
    allCARRules[0] = new FastVector();
    allCARRules[1] = new FastVector();
    allCARRules[2] = new FastVector();
    for (int k = 0; k < m_allTheRules[0].size(); k++) {
      int[] newPremiseArray = new int[m_instances.numAttributes() - 1];
      int help = 0;
      for (int j = 0; j < m_instances.numAttributes(); j++) {
        if (j != m_instances.classIndex()) {
          newPremiseArray[help] = ((ItemSet)m_allTheRules[0].elementAt(k)).itemAt(j);
          help++;
        }
      }
      ItemSet newPremise = new ItemSet(m_instances.numInstances(), newPremiseArray);
      newPremise.setCounter(((ItemSet)m_allTheRules[0].elementAt(k)).counter());
      allCARRules[0].addElement(newPremise);
      int[] newConsArray = new int[1];
      newConsArray[0] = ((ItemSet)m_allTheRules[1].elementAt(k)).itemAt(m_instances.classIndex());
      ItemSet newCons = new ItemSet(m_instances.numInstances(), newConsArray);
      newCons.setCounter(((ItemSet)m_allTheRules[1].elementAt(k)).counter());
      allCARRules[1].addElement(newCons);
      allCARRules[2].addElement(m_allTheRules[2].elementAt(k));
    }
    
    return allCARRules;
  }
  




  public Instances getInstancesNoClass()
  {
    Instances noClass = null;
    try {
      noClass = LabeledItemSet.divide(m_instances, false);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println("\n" + e.getMessage());
    }
    
    return noClass;
  }
  




  public Instances getInstancesOnlyClass()
  {
    Instances onlyClass = null;
    try {
      onlyClass = LabeledItemSet.divide(m_instances, true);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println("\n" + e.getMessage());
    }
    return onlyClass;
  }
  






  public Enumeration listOptions()
  {
    String string1 = "\tThe required number of rules. (default = " + (m_numRules - 5) + ")";
    String string2 = "\tIf set class association rules are mined. (default = no)";
    String string3 = "\tThe class index. (default = last)";
    FastVector newVector = new FastVector(3);
    
    newVector.addElement(new Option(string1, "N", 1, "-N <required number of rules output>"));
    
    newVector.addElement(new Option(string2, "A", 0, "-A"));
    
    newVector.addElement(new Option(string3, "c", 1, "-c <the class index>"));
    
    return newVector.elements();
  }
  




















  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    String numRulesString = Utils.getOption('N', options);
    if (numRulesString.length() != 0) {
      m_numRules = (Integer.parseInt(numRulesString) + 5);
    } else {
      m_numRules = Integer.MAX_VALUE;
    }
    String classIndexString = Utils.getOption('c', options);
    if (classIndexString.length() != 0) {
      m_classIndex = Integer.parseInt(classIndexString);
    }
    m_car = Utils.getFlag('A', options);
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-N");
    result.add("" + (m_numRules - 5));
    
    if (m_car) {
      result.add("-A");
    }
    result.add("-c");
    result.add("" + m_classIndex);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  






  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_allTheRules[0].size() == 0)
      return "\nNo large itemsets and rules found!\n";
    text.append("\nPredictiveApriori\n===================\n\n");
    text.append("\nBest rules found:\n\n");
    
    for (int i = 0; i < m_allTheRules[0].size(); i++) {
      text.append(Utils.doubleToString(i + 1.0D, (int)(Math.log(m_numRules) / Math.log(10.0D) + 1.0D), 0) + ". " + ((ItemSet)m_allTheRules[0].elementAt(i)).toString(m_instances) + " ==> " + ((ItemSet)m_allTheRules[1].elementAt(i)).toString(m_instances) + "    acc:(" + Utils.doubleToString(((Double)m_allTheRules[2].elementAt(i)).doubleValue(), 5) + ")");
      







      text.append('\n');
    }
    

    return text.toString();
  }
  





  public String numRulesTipText()
  {
    return "Number of rules to find.";
  }
  





  public int getNumRules()
  {
    return m_numRules - 5;
  }
  





  public void setNumRules(int v)
  {
    m_numRules = (v + 5);
  }
  




  public void setClassIndex(int index)
  {
    m_classIndex = index;
  }
  




  public int getClassIndex()
  {
    return m_classIndex;
  }
  




  public String classIndexTipText()
  {
    return "Index of the class attribute.\n If set to -1, the last attribute will be taken as the class attribute.";
  }
  




  public void setCar(boolean flag)
  {
    m_car = flag;
  }
  




  public boolean getCar()
  {
    return m_car;
  }
  




  public String carTipText()
  {
    return "If enabled class association rules are mined instead of (general) association rules.";
  }
  






  public String metricString()
  {
    return "acc";
  }
  






  private void findLargeItemSets(int index)
    throws Exception
  {
    FastVector kSets = new FastVector();
    
    int i = 0;
    

    if (index == 1) {
      kSets = ItemSet.singletons(m_instances);
      ItemSet.upDateCounters(kSets, m_instances);
      kSets = ItemSet.deleteItemSets(kSets, m_premiseCount, Integer.MAX_VALUE);
      if (kSets.size() == 0)
        return;
      m_Ls.addElement(kSets);
    }
    
    if (index > 1) {
      if (m_Ls.size() > 0)
        kSets = (FastVector)m_Ls.lastElement();
      m_Ls.removeAllElements();
      i = index - 2;
      FastVector kMinusOneSets = kSets;
      kSets = ItemSet.mergeAllItemSets(kMinusOneSets, i, m_instances.numInstances());
      Hashtable hashtable = ItemSet.getHashtable(kMinusOneSets, kMinusOneSets.size());
      m_hashtables.addElement(hashtable);
      kSets = ItemSet.pruneItemSets(kSets, hashtable);
      ItemSet.upDateCounters(kSets, m_instances);
      kSets = ItemSet.deleteItemSets(kSets, m_premiseCount, Integer.MAX_VALUE);
      if (kSets.size() == 0)
        return;
      m_Ls.addElement(kSets);
    }
  }
  










  private void findRulesQuickly()
    throws Exception
  {
    for (int j = 0; j < m_Ls.size(); j++) {
      FastVector currentItemSets = (FastVector)m_Ls.elementAt(j);
      Enumeration enumItemSets = currentItemSets.elements();
      while (enumItemSets.hasMoreElements()) {
        RuleGeneration currentItemSet = new RuleGeneration((ItemSet)enumItemSets.nextElement());
        m_best = currentItemSet.generateRules(m_numRules - 5, m_midPoints, m_priors, m_expectation, m_instances, m_best, m_count);
        

        m_count = m_count;
        if ((!m_bestChanged) && (m_change)) {
          m_bestChanged = true;
        }
        if (m_best.size() >= m_numRules - 5)
          m_expectation = ((RuleItem)m_best.first()).accuracy(); else {
          m_expectation = 0.0D;
        }
      }
    }
  }
  




  private void findLargeCarItemSets(int index)
    throws Exception
  {
    FastVector kSets = new FastVector();
    
    int i = 0;
    
    if (index == 1) {
      kSets = CaRuleGeneration.singletons(m_instances);
      ItemSet.upDateCounters(kSets, m_instances);
      kSets = ItemSet.deleteItemSets(kSets, m_premiseCount, Integer.MAX_VALUE);
      if (kSets.size() == 0)
        return;
      m_Ls.addElement(kSets);
    }
    
    if (index > 1) {
      if (m_Ls.size() > 0)
        kSets = (FastVector)m_Ls.lastElement();
      m_Ls.removeAllElements();
      i = index - 2;
      FastVector kMinusOneSets = kSets;
      kSets = ItemSet.mergeAllItemSets(kMinusOneSets, i, m_instances.numInstances());
      Hashtable hashtable = ItemSet.getHashtable(kMinusOneSets, kMinusOneSets.size());
      m_hashtables.addElement(hashtable);
      kSets = ItemSet.pruneItemSets(kSets, hashtable);
      ItemSet.upDateCounters(kSets, m_instances);
      kSets = ItemSet.deleteItemSets(kSets, m_premiseCount, Integer.MAX_VALUE);
      if (kSets.size() == 0)
        return;
      m_Ls.addElement(kSets);
    }
  }
  






  private void findCaRulesQuickly()
    throws Exception
  {
    for (int j = 0; j < m_Ls.size(); j++) {
      FastVector currentItemSets = (FastVector)m_Ls.elementAt(j);
      Enumeration enumItemSets = currentItemSets.elements();
      while (enumItemSets.hasMoreElements()) {
        CaRuleGeneration currentLItemSet = new CaRuleGeneration((ItemSet)enumItemSets.nextElement());
        m_best = currentLItemSet.generateRules(m_numRules - 5, m_midPoints, m_priors, m_expectation, m_instances, m_best, m_count);
        
        m_count = currentLItemSet.count();
        if ((!m_bestChanged) && (currentLItemSet.change()))
          m_bestChanged = true;
        if (m_best.size() == m_numRules - 5) {
          m_expectation = ((RuleItem)m_best.first()).accuracy();
        } else {
          m_expectation = 0.0D;
        }
      }
    }
  }
  




  public FastVector[] getAllTheRules()
  {
    return m_allTheRules;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6365 $");
  }
  




  public static void main(String[] args)
  {
    runAssociator(new PredictiveApriori(), args);
  }
}

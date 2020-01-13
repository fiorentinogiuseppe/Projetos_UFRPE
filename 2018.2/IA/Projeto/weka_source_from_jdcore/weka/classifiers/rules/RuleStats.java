package weka.classifiers.rules;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;



















































public class RuleStats
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = -5708153367675298624L;
  private Instances m_Data;
  private FastVector m_Ruleset;
  private FastVector m_SimpleStats;
  private FastVector m_Filtered;
  private double m_Total;
  private static double REDUNDANCY_FACTOR = 0.5D;
  

  private double MDL_THEORY_WEIGHT = 1.0D;
  
  private FastVector m_Distributions;
  

  public RuleStats()
  {
    m_Data = null;
    m_Ruleset = null;
    m_SimpleStats = null;
    m_Filtered = null;
    m_Distributions = null;
    m_Total = -1.0D;
  }
  






  public RuleStats(Instances data, FastVector rules)
  {
    this();
    m_Data = data;
    m_Ruleset = rules;
  }
  


  public void cleanUp()
  {
    m_Data = null;
    m_Filtered = null;
  }
  







  public void setNumAllConds(double total)
  {
    if (total < 0.0D) {
      m_Total = numAllConditions(m_Data);
    } else {
      m_Total = total;
    }
  }
  



  public void setData(Instances data)
  {
    m_Data = data;
  }
  




  public Instances getData()
  {
    return m_Data;
  }
  





  public void setRuleset(FastVector rules)
  {
    m_Ruleset = rules;
  }
  





  public FastVector getRuleset()
  {
    return m_Ruleset;
  }
  




  public int getRulesetSize()
  {
    return m_Ruleset.size();
  }
  







  public double[] getSimpleStats(int index)
  {
    if ((m_SimpleStats != null) && (index < m_SimpleStats.size())) {
      return (double[])m_SimpleStats.elementAt(index);
    }
    return null;
  }
  







  public Instances[] getFiltered(int index)
  {
    if ((m_Filtered != null) && (index < m_Filtered.size())) {
      return (Instances[])m_Filtered.elementAt(index);
    }
    return null;
  }
  







  public double[] getDistributions(int index)
  {
    if ((m_Distributions != null) && (index < m_Distributions.size())) {
      return (double[])m_Distributions.elementAt(index);
    }
    return null;
  }
  




  public void setMDLTheoryWeight(double weight)
  {
    MDL_THEORY_WEIGHT = weight;
  }
  









  public static double numAllConditions(Instances data)
  {
    double total = 0.0D;
    Enumeration attEnum = data.enumerateAttributes();
    while (attEnum.hasMoreElements()) {
      Attribute att = (Attribute)attEnum.nextElement();
      if (att.isNominal()) {
        total += att.numValues();
      } else
        total += 2.0D * data.numDistinctValues(att);
    }
    return total;
  }
  





  public void countData()
  {
    if ((m_Filtered != null) || (m_Ruleset == null) || (m_Data == null))
    {

      return;
    }
    int size = m_Ruleset.size();
    m_Filtered = new FastVector(size);
    m_SimpleStats = new FastVector(size);
    m_Distributions = new FastVector(size);
    Instances data = new Instances(m_Data);
    
    for (int i = 0; i < size; i++) {
      double[] stats = new double[6];
      double[] classCounts = new double[m_Data.classAttribute().numValues()];
      Instances[] filtered = computeSimpleStats(i, data, stats, classCounts);
      m_Filtered.addElement(filtered);
      m_SimpleStats.addElement(stats);
      m_Distributions.addElement(classCounts);
      data = filtered[1];
    }
  }
  














  public void countData(int index, Instances uncovered, double[][] prevRuleStats)
  {
    if ((m_Filtered != null) || (m_Ruleset == null))
    {
      return;
    }
    int size = m_Ruleset.size();
    m_Filtered = new FastVector(size);
    m_SimpleStats = new FastVector(size);
    Instances[] data = new Instances[2];
    data[1] = uncovered;
    
    for (int i = 0; i < index; i++) {
      m_SimpleStats.addElement(prevRuleStats[i]);
      if (i + 1 == index) {
        m_Filtered.addElement(data);
      } else {
        m_Filtered.addElement(new Object());
      }
    }
    for (int j = index; j < size; j++) {
      double[] stats = new double[6];
      Instances[] filtered = computeSimpleStats(j, data[1], stats, null);
      m_Filtered.addElement(filtered);
      m_SimpleStats.addElement(stats);
      data = filtered;
    }
  }
  













  private Instances[] computeSimpleStats(int index, Instances insts, double[] stats, double[] dist)
  {
    Rule rule = (Rule)m_Ruleset.elementAt(index);
    
    Instances[] data = new Instances[2];
    data[0] = new Instances(insts, insts.numInstances());
    data[1] = new Instances(insts, insts.numInstances());
    
    for (int i = 0; i < insts.numInstances(); i++) {
      Instance datum = insts.instance(i);
      double weight = datum.weight();
      if (rule.covers(datum)) {
        data[0].add(datum);
        stats[0] += weight;
        if ((int)datum.classValue() == (int)rule.getConsequent()) {
          stats[2] += weight;
        } else
          stats[4] += weight;
        if (dist != null) {
          dist[((int)datum.classValue())] += weight;
        }
      } else {
        data[1].add(datum);
        stats[1] += weight;
        if ((int)datum.classValue() != (int)rule.getConsequent()) {
          stats[3] += weight;
        } else {
          stats[5] += weight;
        }
      }
    }
    return data;
  }
  





  public void addAndUpdate(Rule lastRule)
  {
    if (m_Ruleset == null)
      m_Ruleset = new FastVector();
    m_Ruleset.addElement(lastRule);
    
    Instances data = m_Filtered == null ? m_Data : ((Instances[])(Instances[])m_Filtered.lastElement())[1];
    
    double[] stats = new double[6];
    double[] classCounts = new double[m_Data.classAttribute().numValues()];
    Instances[] filtered = computeSimpleStats(m_Ruleset.size() - 1, data, stats, classCounts);
    

    if (m_Filtered == null)
      m_Filtered = new FastVector();
    m_Filtered.addElement(filtered);
    
    if (m_SimpleStats == null)
      m_SimpleStats = new FastVector();
    m_SimpleStats.addElement(stats);
    
    if (m_Distributions == null)
      m_Distributions = new FastVector();
    m_Distributions.addElement(classCounts);
  }
  











  public static double subsetDL(double t, double k, double p)
  {
    double rt = Utils.gr(p, 0.0D) ? -k * Utils.log2(p) : 0.0D;
    rt -= (t - k) * Utils.log2(1.0D - p);
    return rt;
  }
  














  public double theoryDL(int index)
  {
    double k = ((Rule)m_Ruleset.elementAt(index)).size();
    
    if (k == 0.0D) {
      return 0.0D;
    }
    double tdl = Utils.log2(k);
    if (k > 1.0D)
      tdl += 2.0D * Utils.log2(tdl);
    tdl += subsetDL(m_Total, k, k / m_Total);
    
    return MDL_THEORY_WEIGHT * REDUNDANCY_FACTOR * tdl;
  }
  













  public static double dataDL(double expFPOverErr, double cover, double uncover, double fp, double fn)
  {
    double totalBits = Utils.log2(cover + uncover + 1.0D);
    double uncoverBits;
    double coverBits;
    double uncoverBits;
    if (Utils.gr(cover, uncover)) {
      double expErr = expFPOverErr * (fp + fn);
      double coverBits = subsetDL(cover, fp, expErr / cover);
      uncoverBits = Utils.gr(uncover, 0.0D) ? subsetDL(uncover, fn, fn / uncover) : 0.0D;
    }
    else
    {
      double expErr = (1.0D - expFPOverErr) * (fp + fn);
      coverBits = Utils.gr(cover, 0.0D) ? subsetDL(cover, fp, fp / cover) : 0.0D;
      
      uncoverBits = subsetDL(uncover, fn, expErr / uncover);
    }
    






    return totalBits + coverBits + uncoverBits;
  }
  



























  public double potential(int index, double expFPOverErr, double[] rulesetStat, double[] ruleStat, boolean checkErr)
  {
    double pcov = rulesetStat[0] - ruleStat[0];
    double puncov = rulesetStat[1] + ruleStat[0];
    double pfp = rulesetStat[4] - ruleStat[4];
    double pfn = rulesetStat[5] + ruleStat[2];
    
    double dataDLWith = dataDL(expFPOverErr, rulesetStat[0], rulesetStat[1], rulesetStat[4], rulesetStat[5]);
    

    double theoryDLWith = theoryDL(index);
    double dataDLWithout = dataDL(expFPOverErr, pcov, puncov, pfp, pfn);
    
    double potential = dataDLWith + theoryDLWith - dataDLWithout;
    double err = ruleStat[4] / ruleStat[0];
    



    boolean overErr = Utils.grOrEq(err, 0.5D);
    if (!checkErr) {
      overErr = false;
    }
    if ((Utils.grOrEq(potential, 0.0D)) || (overErr))
    {
      rulesetStat[0] = pcov;
      rulesetStat[1] = puncov;
      rulesetStat[4] = pfp;
      rulesetStat[5] = pfn;
      return potential;
    }
    
    return NaN.0D;
  }
  












  public double minDataDLIfDeleted(int index, double expFPRate, boolean checkErr)
  {
    double[] rulesetStat = new double[6];
    int more = m_Ruleset.size() - 1 - index;
    FastVector indexPlus = new FastVector(more);
    

    for (int j = 0; j < index; j++)
    {
      rulesetStat[0] += ((double[])(double[])m_SimpleStats.elementAt(j))[0];
      rulesetStat[2] += ((double[])(double[])m_SimpleStats.elementAt(j))[2];
      rulesetStat[4] += ((double[])(double[])m_SimpleStats.elementAt(j))[4];
    }
    

    Instances data = index == 0 ? m_Data : ((Instances[])(Instances[])m_Filtered.elementAt(index - 1))[1];
    


    for (int j = index + 1; j < m_Ruleset.size(); j++) {
      double[] stats = new double[6];
      Instances[] split = computeSimpleStats(j, data, stats, null);
      indexPlus.addElement(stats);
      rulesetStat[0] += stats[0];
      rulesetStat[2] += stats[2];
      rulesetStat[4] += stats[4];
      data = split[1];
    }
    
    if (more > 0) {
      rulesetStat[1] = ((double[])(double[])indexPlus.lastElement())[1];
      rulesetStat[3] = ((double[])(double[])indexPlus.lastElement())[3];
      rulesetStat[5] = ((double[])(double[])indexPlus.lastElement())[5];
    }
    else if (index > 0) {
      rulesetStat[1] = ((double[])(double[])m_SimpleStats.elementAt(index - 1))[1];
      
      rulesetStat[3] = ((double[])(double[])m_SimpleStats.elementAt(index - 1))[3];
      
      rulesetStat[5] = ((double[])(double[])m_SimpleStats.elementAt(index - 1))[5];
    }
    else
    {
      rulesetStat[1] = (((double[])(double[])m_SimpleStats.elementAt(0))[0] + ((double[])(double[])m_SimpleStats.elementAt(0))[1]);
      
      rulesetStat[3] = (((double[])(double[])m_SimpleStats.elementAt(0))[3] + ((double[])(double[])m_SimpleStats.elementAt(0))[4]);
      
      rulesetStat[5] = (((double[])(double[])m_SimpleStats.elementAt(0))[2] + ((double[])(double[])m_SimpleStats.elementAt(0))[5]);
    }
    


    double potential = 0.0D;
    for (int k = index + 1; k < m_Ruleset.size(); k++) {
      double[] ruleStat = (double[])indexPlus.elementAt(k - index - 1);
      double ifDeleted = potential(k, expFPRate, rulesetStat, ruleStat, checkErr);
      
      if (!Double.isNaN(ifDeleted)) {
        potential += ifDeleted;
      }
    }
    


    double dataDLWithout = dataDL(expFPRate, rulesetStat[0], rulesetStat[1], rulesetStat[4], rulesetStat[5]);
    




    return dataDLWithout - potential;
  }
  












  public double minDataDLIfExists(int index, double expFPRate, boolean checkErr)
  {
    double[] rulesetStat = new double[6];
    for (int j = 0; j < m_SimpleStats.size(); j++)
    {
      rulesetStat[0] += ((double[])(double[])m_SimpleStats.elementAt(j))[0];
      rulesetStat[2] += ((double[])(double[])m_SimpleStats.elementAt(j))[2];
      rulesetStat[4] += ((double[])(double[])m_SimpleStats.elementAt(j))[4];
      if (j == m_SimpleStats.size() - 1) {
        rulesetStat[1] = ((double[])(double[])m_SimpleStats.elementAt(j))[1];
        rulesetStat[3] = ((double[])(double[])m_SimpleStats.elementAt(j))[3];
        rulesetStat[5] = ((double[])(double[])m_SimpleStats.elementAt(j))[5];
      }
    }
    

    double potential = 0.0D;
    for (int k = index + 1; k < m_SimpleStats.size(); k++) {
      double[] ruleStat = (double[])getSimpleStats(k);
      double ifDeleted = potential(k, expFPRate, rulesetStat, ruleStat, checkErr);
      
      if (!Double.isNaN(ifDeleted)) {
        potential += ifDeleted;
      }
    }
    


    double dataDLWith = dataDL(expFPRate, rulesetStat[0], rulesetStat[1], rulesetStat[4], rulesetStat[5]);
    



    return dataDLWith - potential;
  }
  















  public double relativeDL(int index, double expFPRate, boolean checkErr)
  {
    return minDataDLIfExists(index, expFPRate, checkErr) + theoryDL(index) - minDataDLIfDeleted(index, expFPRate, checkErr);
  }
  









  public void reduceDL(double expFPRate, boolean checkErr)
  {
    boolean needUpdate = false;
    double[] rulesetStat = new double[6];
    for (int j = 0; j < m_SimpleStats.size(); j++)
    {
      rulesetStat[0] += ((double[])(double[])m_SimpleStats.elementAt(j))[0];
      rulesetStat[2] += ((double[])(double[])m_SimpleStats.elementAt(j))[2];
      rulesetStat[4] += ((double[])(double[])m_SimpleStats.elementAt(j))[4];
      if (j == m_SimpleStats.size() - 1) {
        rulesetStat[1] = ((double[])(double[])m_SimpleStats.elementAt(j))[1];
        rulesetStat[3] = ((double[])(double[])m_SimpleStats.elementAt(j))[3];
        rulesetStat[5] = ((double[])(double[])m_SimpleStats.elementAt(j))[5];
      }
    }
    

    for (int k = m_SimpleStats.size() - 1; k >= 0; k--)
    {
      double[] ruleStat = (double[])m_SimpleStats.elementAt(k);
      

      double ifDeleted = potential(k, expFPRate, rulesetStat, ruleStat, checkErr);
      
      if (!Double.isNaN(ifDeleted))
      {






        if (k == m_SimpleStats.size() - 1) {
          removeLast();
        } else {
          m_Ruleset.removeElementAt(k);
          needUpdate = true;
        }
      }
    }
    
    if (needUpdate) {
      m_Filtered = null;
      m_SimpleStats = null;
      countData();
    }
  }
  




  public void removeLast()
  {
    int last = m_Ruleset.size() - 1;
    m_Ruleset.removeElementAt(last);
    m_Filtered.removeElementAt(last);
    m_SimpleStats.removeElementAt(last);
    if (m_Distributions != null) {
      m_Distributions.removeElementAt(last);
    }
  }
  









  public static Instances rmCoveredBySuccessives(Instances data, FastVector rules, int index)
  {
    Instances rt = new Instances(data, 0);
    
    for (int i = 0; i < data.numInstances(); i++) {
      Instance datum = data.instance(i);
      boolean covered = false;
      
      for (int j = index + 1; j < rules.size(); j++) {
        Rule rule = (Rule)rules.elementAt(j);
        if (rule.covers(datum)) {
          covered = true;
          break;
        }
      }
      
      if (!covered)
        rt.add(datum);
    }
    return rt;
  }
  










  public static final Instances stratify(Instances data, int folds, Random rand)
  {
    if (!data.classAttribute().isNominal()) {
      return data;
    }
    Instances result = new Instances(data, 0);
    Instances[] bagsByClasses = new Instances[data.numClasses()];
    
    for (int i = 0; i < bagsByClasses.length; i++) {
      bagsByClasses[i] = new Instances(data, 0);
    }
    
    for (int j = 0; j < data.numInstances(); j++) {
      Instance datum = data.instance(j);
      bagsByClasses[((int)datum.classValue())].add(datum);
    }
    

    for (int j = 0; j < bagsByClasses.length; j++) {
      bagsByClasses[j].randomize(rand);
    }
    for (int k = 0; k < folds; k++) {
      int offset = k;int bag = 0;
      for (;;)
      {
        if (offset >= bagsByClasses[bag].numInstances()) {
          offset -= bagsByClasses[bag].numInstances();
          bag++; if (bag >= bagsByClasses.length) {
            break;
          }
        } else {
          result.add(bagsByClasses[bag].instance(offset));
          offset += folds;
        }
      }
    }
    return result;
  }
  








  public double combinedDL(double expFPRate, double predicted)
  {
    double rt = 0.0D;
    
    if (getRulesetSize() > 0) {
      double[] stats = (double[])m_SimpleStats.lastElement();
      for (int j = getRulesetSize() - 2; j >= 0; j--) {
        stats[0] += getSimpleStats(j)[0];
        stats[2] += getSimpleStats(j)[2];
        stats[4] += getSimpleStats(j)[4];
      }
      rt += dataDL(expFPRate, stats[0], stats[1], stats[4], stats[5]);
    }
    else
    {
      double fn = 0.0D;
      for (int j = 0; j < m_Data.numInstances(); j++)
        if ((int)m_Data.instance(j).classValue() == (int)predicted)
          fn += m_Data.instance(j).weight();
      rt += dataDL(expFPRate, 0.0D, m_Data.sumOfWeights(), 0.0D, fn);
    }
    
    for (int i = 0; i < getRulesetSize(); i++) {
      rt += theoryDL(i);
    }
    return rt;
  }
  








  public static final Instances[] partition(Instances data, int numFolds)
  {
    Instances[] rt = new Instances[2];
    int splits = data.numInstances() * (numFolds - 1) / numFolds;
    
    rt[0] = new Instances(data, 0, splits);
    rt[1] = new Instances(data, splits, data.numInstances() - splits);
    
    return rt;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4608 $");
  }
}

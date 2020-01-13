package weka.classifiers.rules;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;




























































public class ConjunctiveRule
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler
{
  static final long serialVersionUID = -5938309903225087198L;
  private int m_Folds;
  private Attribute m_ClassAttribute;
  protected FastVector m_Antds;
  protected double[] m_DefDstr;
  protected double[] m_Cnsqt;
  private int m_NumClasses;
  private long m_Seed;
  private Random m_Random;
  private FastVector m_Targets;
  private boolean m_IsExclude;
  private double m_MinNo;
  private int m_NumAntds;
  
  public ConjunctiveRule()
  {
    m_Folds = 3;
    




    m_Antds = null;
    

    m_DefDstr = null;
    

    m_Cnsqt = null;
    

    m_NumClasses = 0;
    

    m_Seed = 1L;
    

    m_Random = null;
    




    m_IsExclude = false;
    

    m_MinNo = 2.0D;
    

    m_NumAntds = -1;
  }
  




  public String globalInfo()
  {
    return "This class implements a single conjunctive rule learner that can predict for numeric and nominal class labels.\n\nA rule consists of antecedents \"AND\"ed together and the consequent (class value) for the classification/regression.  In this case, the consequent is the distribution of the available classes (or mean for a numeric value) in the dataset. If the test instance is not covered by this rule, then it's predicted using the default class distributions/value of the data not covered by the rule in the training data.This learner selects an antecedent by computing the Information Gain of each antecendent and prunes the generated rule using Reduced Error Prunning (REP) or simple pre-pruning based on the number of antecedents.\n\nFor classification, the Information of one antecedent is the weighted average of the entropies of both the data covered and not covered by the rule.\nFor regression, the Information is the weighted average of the mean-squared errors of both the data covered and not covered by the rule.\n\nIn pruning, weighted average of the accuracy rates on the pruning data is used for classification while the weighted average of the mean-squared errors on the pruning data is used for regression.\n\n";
  }
  




  private abstract class Antd
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -8729076306737827571L;
    



    protected Attribute att;
    



    protected double value;
    



    protected double maxInfoGain;
    



    protected double inform;
    



    protected double uncoverWtSq;
    


    protected double uncoverWtVl;
    


    protected double uncoverSum;
    


    protected double[] uncover;
    



    public Antd(Attribute a, double[] unc)
    {
      att = a;
      value = NaN.0D;
      maxInfoGain = 0.0D;
      inform = NaN.0D;
      uncover = unc;
    }
    



    public Antd(Attribute a, double uncoveredWtSq, double uncoveredWtVl, double uncoveredWts)
    {
      att = a;
      value = NaN.0D;
      maxInfoGain = 0.0D;
      inform = NaN.0D;
      uncoverWtSq = uncoveredWtSq;
      uncoverWtVl = uncoveredWtVl;
      uncoverSum = uncoveredWts;
    }
    
    public abstract Instances[] splitData(Instances paramInstances, double paramDouble);
    
    public abstract boolean isCover(Instance paramInstance);
    
    public abstract String toString();
    
    public Attribute getAttr() { return att; }
    public double getAttrValue() { return value; }
    public double getMaxInfoGain() { return maxInfoGain; }
    public double getInfo() { return inform; }
    









    protected double wtMeanSqErr(double weightedSq, double weightedValue, double sum)
    {
      if (Utils.smOrEq(sum, 1.0E-6D))
        return 0.0D;
      return weightedSq - weightedValue * weightedValue / sum;
    }
    








    protected double entropy(double[] value, double sum)
    {
      if (Utils.smOrEq(sum, 1.0E-6D)) {
        return 0.0D;
      }
      double entropy = 0.0D;
      for (int i = 0; i < value.length; i++) {
        if (!Utils.eq(value[i], 0.0D))
          entropy -= value[i] * Utils.log2(value[i]);
      }
      entropy += sum * Utils.log2(sum);
      entropy /= sum;
      return entropy;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9835 $");
    }
  }
  



  private class NumericAntd
    extends ConjunctiveRule.Antd
  {
    static final long serialVersionUID = -7957266498918210436L;
    


    private double splitPoint;
    


    public NumericAntd(Attribute a, double[] unc)
    {
      super(a, unc);
      splitPoint = NaN.0D;
    }
    


    public NumericAntd(Attribute a, double sq, double vl, double wts)
    {
      super(a, sq, vl, wts);
      splitPoint = NaN.0D;
    }
    




    public double getSplitPoint()
    {
      return splitPoint;
    }
    










    public Instances[] splitData(Instances insts, double defInfo)
    {
      Instances data = new Instances(insts);
      data.sort(att);
      int total = data.numInstances();
      
      maxInfoGain = 0.0D;
      value = 0.0D;
      
      double minSplit;
      
      if (m_ClassAttribute.isNominal()) {
        double minSplit = 0.1D * data.sumOfWeights() / m_ClassAttribute.numValues();
        
        if (Utils.smOrEq(minSplit, m_MinNo)) {
          minSplit = m_MinNo;
        } else if (Utils.gr(minSplit, 25.0D)) {
          minSplit = 25.0D;
        }
      } else {
        minSplit = m_MinNo;
      }
      double[] fst = null;double[] snd = null;double[] missing = null;
      if (m_ClassAttribute.isNominal()) {
        fst = new double[m_NumClasses];
        snd = new double[m_NumClasses];
        missing = new double[m_NumClasses];
        
        for (int v = 0; v < m_NumClasses; v++) {
          double tmp212_211 = (missing[v] = 0.0D);snd[v] = tmp212_211;fst[v] = tmp212_211;
        } }
      double fstCover = 0.0D;double sndCover = 0.0D;double fstWtSq = 0.0D;double sndWtSq = 0.0D;double fstWtVl = 0.0D;double sndWtVl = 0.0D;
      
      int split = 1;
      int prev = 0;
      int finalSplit = split;
      
      for (int x = 0; x < data.numInstances(); x++) {
        Instance inst = data.instance(x);
        if (inst.isMissing(att)) {
          total = x;
          break;
        }
        
        sndCover += inst.weight();
        if (m_ClassAttribute.isNominal()) {
          snd[((int)inst.classValue())] += inst.weight();
        } else {
          sndWtSq += inst.weight() * inst.classValue() * inst.classValue();
          sndWtVl += inst.weight() * inst.classValue();
        }
      }
      


      if (Utils.sm(sndCover, 2.0D * minSplit)) {
        return null;
      }
      double msingWtSq = 0.0D;double msingWtVl = 0.0D;
      Instances missingData = new Instances(data, 0);
      for (int y = total; y < data.numInstances(); y++) {
        Instance inst = data.instance(y);
        missingData.add(inst);
        if (m_ClassAttribute.isNominal()) {
          missing[((int)inst.classValue())] += inst.weight();
        } else {
          msingWtSq += inst.weight() * inst.classValue() * inst.classValue();
          msingWtVl += inst.weight() * inst.classValue();
        }
      }
      
      if (total == 0) { return null;
      }
      splitPoint = data.instance(total - 1).value(att);
      for (; 
          split < total; split++) {
        if (!Utils.eq(data.instance(split).value(att), data.instance(prev).value(att)))
        {


          for (int y = prev; y < split; y++) {
            Instance inst = data.instance(y);
            fstCover += inst.weight();sndCover -= inst.weight();
            if (m_ClassAttribute.isNominal()) {
              fst[((int)inst.classValue())] += inst.weight();
              snd[((int)inst.classValue())] -= inst.weight();
            }
            else {
              fstWtSq += inst.weight() * inst.classValue() * inst.classValue();
              fstWtVl += inst.weight() * inst.classValue();
              sndWtSq -= inst.weight() * inst.classValue() * inst.classValue();
              sndWtVl -= inst.weight() * inst.classValue();
            }
          }
          
          if ((Utils.sm(fstCover, minSplit)) || (Utils.sm(sndCover, minSplit))) {
            prev = split;
          }
          else
          {
            double fstEntp = 0.0D;double sndEntp = 0.0D;
            
            if (m_ClassAttribute.isNominal()) {
              fstEntp = entropy(fst, fstCover);
              sndEntp = entropy(snd, sndCover);
            }
            else {
              fstEntp = wtMeanSqErr(fstWtSq, fstWtVl, fstCover) / fstCover;
              sndEntp = wtMeanSqErr(sndWtSq, sndWtVl, sndCover) / sndCover;
            }
            double sndInfoGain;
            double fstInfo;
            double fstInfoGain;
            double sndInfo;
            double sndInfoGain;
            if (m_ClassAttribute.isNominal()) {
              double sum = data.sumOfWeights();
              double whole = sum + Utils.sum(uncover);
              double[] other = null;
              

              other = new double[m_NumClasses];
              for (int z = 0; z < m_NumClasses; z++)
                other[z] = (uncover[z] + snd[z] + missing[z]);
              double otherCover = whole - fstCover;
              double otherEntropy = entropy(other, otherCover);
              
              double fstInfo = (fstEntp * fstCover + otherEntropy * otherCover) / whole;
              double fstInfoGain = defInfo - fstInfo;
              

              other = new double[m_NumClasses];
              for (int z = 0; z < m_NumClasses; z++)
                other[z] = (uncover[z] + fst[z] + missing[z]);
              otherCover = whole - sndCover;
              otherEntropy = entropy(other, otherCover);
              
              double sndInfo = (sndEntp * sndCover + otherEntropy * otherCover) / whole;
              sndInfoGain = defInfo - sndInfo;
            }
            else {
              double sum = data.sumOfWeights();
              double otherWtSq = sndWtSq + msingWtSq + uncoverWtSq;
              double otherWtVl = sndWtVl + msingWtVl + uncoverWtVl;
              double otherCover = sum - fstCover + uncoverSum;
              
              fstInfo = Utils.eq(fstCover, 0.0D) ? 0.0D : fstEntp * fstCover;
              fstInfo += wtMeanSqErr(otherWtSq, otherWtVl, otherCover);
              fstInfoGain = defInfo - fstInfo;
              
              otherWtSq = fstWtSq + msingWtSq + uncoverWtSq;
              otherWtVl = fstWtVl + msingWtVl + uncoverWtVl;
              otherCover = sum - sndCover + uncoverSum;
              sndInfo = Utils.eq(sndCover, 0.0D) ? 0.0D : sndEntp * sndCover;
              sndInfo += wtMeanSqErr(otherWtSq, otherWtVl, otherCover);
              sndInfoGain = defInfo - sndInfo; }
            double info;
            boolean isFirst;
            double infoGain; double info; if ((Utils.gr(fstInfoGain, sndInfoGain)) || ((Utils.eq(fstInfoGain, sndInfoGain)) && (Utils.sm(fstEntp, sndEntp))))
            {
              boolean isFirst = true;
              double infoGain = fstInfoGain;
              info = fstInfo;
            }
            else {
              isFirst = false;
              infoGain = sndInfoGain;
              info = sndInfo;
            }
            
            boolean isUpdate = Utils.gr(infoGain, maxInfoGain);
            

            if (isUpdate) {
              splitPoint = ((data.instance(split).value(att) + data.instance(prev).value(att)) / 2.0D);
              value = (isFirst ? 0 : 1);
              inform = info;
              maxInfoGain = infoGain;
              finalSplit = split;
            }
            prev = split;
          }
        }
      }
      
      Instances[] splitData = new Instances[3];
      splitData[0] = new Instances(data, 0, finalSplit);
      splitData[1] = new Instances(data, finalSplit, total - finalSplit);
      splitData[2] = new Instances(missingData);
      
      return splitData;
    }
    






    public boolean isCover(Instance inst)
    {
      boolean isCover = false;
      if (!inst.isMissing(att)) {
        if (Utils.eq(value, 0.0D)) {
          if (Utils.smOrEq(inst.value(att), splitPoint)) {
            isCover = true;
          }
        } else if (Utils.gr(inst.value(att), splitPoint))
          isCover = true;
      }
      return isCover;
    }
    




    public String toString()
    {
      String symbol = Utils.eq(value, 0.0D) ? " <= " : " > ";
      return att.name() + symbol + Utils.doubleToString(splitPoint, 6);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9835 $");
    }
  }
  


  class NominalAntd
    extends ConjunctiveRule.Antd
  {
    static final long serialVersionUID = -5949864163376447424L;
    

    private double[][] stats;
    

    private double[] coverage;
    

    private boolean isIn;
    

    public NominalAntd(Attribute a, double[] unc)
    {
      super(a, unc);
      int bag = att.numValues();
      stats = new double[bag][m_NumClasses];
      coverage = new double[bag];
      isIn = true;
    }
    


    public NominalAntd(Attribute a, double sq, double vl, double wts)
    {
      super(a, sq, vl, wts);
      int bag = att.numValues();
      stats = ((double[][])null);
      coverage = new double[bag];
      isIn = true;
    }
    










    public Instances[] splitData(Instances data, double defInfo)
    {
      int bag = att.numValues();
      Instances[] splitData = new Instances[bag + 1];
      double[] wSq = new double[bag];
      double[] wVl = new double[bag];
      double totalWS = 0.0D;double totalWV = 0.0D;double msingWS = 0.0D;double msingWV = 0.0D;double sum = data.sumOfWeights();
      double[] all = new double[m_NumClasses];
      double[] missing = new double[m_NumClasses];
      
      for (int w = 0; w < m_NumClasses; w++) {
        double tmp94_93 = 0.0D;missing[w] = tmp94_93;all[w] = tmp94_93;
      }
      for (int x = 0; x < bag; x++) {
        double tmp130_129 = (wVl[x] = 0.0D);wSq[x] = tmp130_129;coverage[x] = tmp130_129;
        if (stats != null)
          for (int y = 0; y < m_NumClasses; y++)
            stats[x][y] = 0.0D;
        splitData[x] = new Instances(data, data.numInstances());
      }
      splitData[bag] = new Instances(data, data.numInstances());
      

      for (int x = 0; x < data.numInstances(); x++) {
        Instance inst = data.instance(x);
        if (!inst.isMissing(att)) {
          int v = (int)inst.value(att);
          splitData[v].add(inst);
          coverage[v] += inst.weight();
          if (m_ClassAttribute.isNominal()) {
            stats[v][((int)inst.classValue())] += inst.weight();
            all[((int)inst.classValue())] += inst.weight();
          }
          else {
            wSq[v] += inst.weight() * inst.classValue() * inst.classValue();
            wVl[v] += inst.weight() * inst.classValue();
            totalWS += inst.weight() * inst.classValue() * inst.classValue();
            totalWV += inst.weight() * inst.classValue();
          }
        }
        else {
          splitData[bag].add(inst);
          if (m_ClassAttribute.isNominal()) {
            all[((int)inst.classValue())] += inst.weight();
            missing[((int)inst.classValue())] += inst.weight();
          }
          else {
            totalWS += inst.weight() * inst.classValue() * inst.classValue();
            totalWV += inst.weight() * inst.classValue();
            msingWS += inst.weight() * inst.classValue() * inst.classValue();
            msingWV += inst.weight() * inst.classValue();
          }
        }
      }
      
      double whole;
      double whole;
      if (m_ClassAttribute.isNominal()) {
        whole = sum + Utils.sum(uncover);
      } else {
        whole = sum + uncoverSum;
      }
      
      double minEntrp = Double.MAX_VALUE;
      maxInfoGain = 0.0D;
      

      int count = 0;
      for (int x = 0; x < bag; x++) {
        if (Utils.grOrEq(coverage[x], m_MinNo))
          count++;
      }
      if (count < 2) {
        maxInfoGain = 0.0D;
        inform = defInfo;
        value = NaN.0D;
        return null;
      }
      
      for (int x = 0; x < bag; x++) {
        double t = coverage[x];
        
        if (!Utils.sm(t, m_MinNo)) { double infoGain;
          double entrp;
          double infoGain;
          if (m_ClassAttribute.isNominal()) {
            double[] other = new double[m_NumClasses];
            for (int y = 0; y < m_NumClasses; y++)
              other[y] = (all[y] - stats[x][y] + uncover[y]);
            double otherCover = whole - t;
            

            double entrp = entropy(stats[x], t);
            double uncEntp = entropy(other, otherCover);
            
            if (m_Debug) {
              System.err.println(defInfo + " " + entrp + " " + t + " " + uncEntp + " " + otherCover + " " + whole);
            }
            


            infoGain = defInfo - (entrp * t + uncEntp * otherCover) / whole;
          }
          else {
            double weight = whole - t;
            entrp = wtMeanSqErr(wSq[x], wVl[x], t) / t;
            infoGain = defInfo - entrp * t - wtMeanSqErr(totalWS - wSq[x] + uncoverWtSq, totalWV - wVl[x] + uncoverWtVl, weight);
          }
          




          boolean isWithin = true;
          if (m_IsExclude) { double infoGain2;
            double entrp2;
            double infoGain2; if (m_ClassAttribute.isNominal()) {
              double[] other2 = new double[m_NumClasses];
              double[] notIn = new double[m_NumClasses];
              for (int y = 0; y < m_NumClasses; y++) {
                other2[y] = (stats[x][y] + missing[y] + uncover[y]);
                notIn[y] = (all[y] - stats[x][y] - missing[y]);
              }
              
              double msSum = Utils.sum(missing);
              double otherCover2 = t + msSum + Utils.sum(uncover);
              
              double entrp2 = entropy(notIn, sum - t - msSum);
              double uncEntp2 = entropy(other2, otherCover2);
              infoGain2 = defInfo - (entrp2 * (sum - t - msSum) + uncEntp2 * otherCover2) / whole;
            }
            else
            {
              double msWts = splitData[bag].sumOfWeights();
              double weight2 = t + uncoverSum + msWts;
              
              entrp2 = wtMeanSqErr(totalWS - wSq[x] - msingWS, totalWV - wVl[x] - msingWV, sum - t - msWts) / (sum - t - msWts);
              

              infoGain2 = defInfo - entrp2 * (sum - t - msWts) - wtMeanSqErr(wSq[x] + uncoverWtSq + msingWS, wVl[x] + uncoverWtVl + msingWV, weight2);
            }
            




            if ((Utils.gr(infoGain2, infoGain)) || ((Utils.eq(infoGain2, infoGain)) && (Utils.sm(entrp2, entrp))))
            {
              infoGain = infoGain2;
              entrp = entrp2;
              isWithin = false;
            }
          }
          

          if ((Utils.gr(infoGain, maxInfoGain)) || ((Utils.eq(infoGain, maxInfoGain)) && (Utils.sm(entrp, minEntrp))))
          {
            value = x;
            maxInfoGain = infoGain;
            inform = (-(maxInfoGain - defInfo));
            minEntrp = entrp;
            isIn = isWithin;
          }
        }
      }
      return splitData;
    }
    






    public boolean isCover(Instance inst)
    {
      boolean isCover = false;
      if (!inst.isMissing(att)) {
        if (isIn) {
          if (Utils.eq(inst.value(att), value)) {
            isCover = true;
          }
        } else if (!Utils.eq(inst.value(att), value))
          isCover = true;
      }
      return isCover;
    }
    






    public boolean isIn()
    {
      return isIn;
    }
    




    public String toString()
    {
      String symbol = isIn ? " = " : " != ";
      return att.name() + symbol + att.value((int)value);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9835 $");
    }
  }
  






























  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    Enumeration enumer = super.listOptions();
    while (enumer.hasMoreElements()) {
      newVector.add(enumer.nextElement());
    }
    
    newVector.addElement(new Option("\tSet number of folds for REP\n\tOne fold is used as pruning set.\n\t(default 3)", "N", 1, "-N <number of folds>"));
    


    newVector.addElement(new Option("\tSet if NOT uses randomization\n\t(default:use randomization)", "R", 0, "-R"));
    

    newVector.addElement(new Option("\tSet whether consider the exclusive\n\texpressions for nominal attributes\n\t(default false)", "E", 0, "-E"));
    


    newVector.addElement(new Option("\tSet the minimal weights of instances\n\twithin a split.\n\t(default 2.0)", "M", 1, "-M <min. weights>"));
    


    newVector.addElement(new Option("\tSet number of antecedents for pre-pruning\n\tif -1, then REP is used\n\t(default -1)", "P", 1, "-P <number of antecedents>"));
    


    newVector.addElement(new Option("\tSet the seed of randomization\n\t(default 1)", "S", 1, "-S <seed>"));
    

    return newVector.elements();
  }
  






































  public void setOptions(String[] options)
    throws Exception
  {
    String numFoldsString = Utils.getOption('N', options);
    if (numFoldsString.length() != 0) {
      m_Folds = Integer.parseInt(numFoldsString);
    } else {
      m_Folds = 3;
    }
    String minNoString = Utils.getOption('M', options);
    if (minNoString.length() != 0) {
      m_MinNo = Double.parseDouble(minNoString);
    } else {
      m_MinNo = 2.0D;
    }
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      m_Seed = Integer.parseInt(seedString);
    } else {
      m_Seed = 1L;
    }
    String numAntdsString = Utils.getOption('P', options);
    if (numAntdsString.length() != 0) {
      m_NumAntds = Integer.parseInt(numAntdsString);
    } else {
      m_NumAntds = -1;
    }
    m_IsExclude = Utils.getFlag('E', options);
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] supersOptions = super.getOptions();
    String[] options = new String[9 + supersOptions.length];
    System.arraycopy(supersOptions, 0, options, 0, supersOptions.length);
    int current = supersOptions.length;
    options[(current++)] = "-N";options[(current++)] = ("" + m_Folds);
    options[(current++)] = "-M";options[(current++)] = ("" + m_MinNo);
    options[(current++)] = "-P";options[(current++)] = ("" + m_NumAntds);
    options[(current++)] = "-S";options[(current++)] = ("" + m_Seed);
    
    if (m_IsExclude) {
      options[(current++)] = "-E";
    }
    while (current < options.length)
      options[(current++)] = "";
    return options;
  }
  






  public String foldsTipText()
  {
    return "Determines the amount of data used for pruning. One fold is used for pruning, the rest for growing the rules.";
  }
  





  public void setFolds(int folds)
  {
    m_Folds = folds;
  }
  




  public int getFolds()
  {
    return m_Folds;
  }
  




  public String seedTipText()
  {
    return "The seed used for randomizing the data.";
  }
  




  public void setSeed(long s)
  {
    m_Seed = s;
  }
  




  public long getSeed()
  {
    return m_Seed;
  }
  




  public String exclusiveTipText()
  {
    return "Set whether to consider exclusive expressions for nominal attribute splits.";
  }
  







  public boolean getExclusive()
  {
    return m_IsExclude;
  }
  






  public void setExclusive(boolean e)
  {
    m_IsExclude = e;
  }
  




  public String minNoTipText()
  {
    return "The minimum total weight of the instances in a rule.";
  }
  




  public void setMinNo(double m)
  {
    m_MinNo = m;
  }
  




  public double getMinNo()
  {
    return m_MinNo;
  }
  




  public String numAntdsTipText()
  {
    return "Set the number of antecedents allowed in the rule if pre-pruning is used.  If this value is other than -1, then pre-pruning will be used, otherwise the rule uses reduced-error pruning.";
  }
  







  public void setNumAntds(int n)
  {
    m_NumAntds = n;
  }
  




  public int getNumAntds()
  {
    return m_NumAntds;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  









  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    Instances data = new Instances(instances);
    data.deleteWithMissingClass();
    
    if (data.numInstances() < m_Folds) {
      throw new Exception("Not enough data for REP.");
    }
    m_ClassAttribute = data.classAttribute();
    if (m_ClassAttribute.isNominal()) {
      m_NumClasses = m_ClassAttribute.numValues();
    } else {
      m_NumClasses = 1;
    }
    m_Antds = new FastVector();
    m_DefDstr = new double[m_NumClasses];
    m_Cnsqt = new double[m_NumClasses];
    m_Targets = new FastVector();
    m_Random = new Random(m_Seed);
    
    if (m_NumAntds != -1) {
      grow(data);
    }
    else
    {
      data.randomize(m_Random);
      

      data.stratify(m_Folds);
      
      Instances growData = data.trainCV(m_Folds, m_Folds - 1, m_Random);
      Instances pruneData = data.testCV(m_Folds, m_Folds - 1);
      
      grow(growData);
      prune(pruneData);
    }
    
    if (m_ClassAttribute.isNominal()) {
      Utils.normalize(m_Cnsqt);
      if (Utils.gr(Utils.sum(m_DefDstr), 0.0D)) {
        Utils.normalize(m_DefDstr);
      }
    }
  }
  




  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (instance == null) {
      throw new Exception("Testing instance is NULL!");
    }
    if (isCover(instance)) {
      return m_Cnsqt;
    }
    return m_DefDstr;
  }
  





  public boolean isCover(Instance datum)
  {
    boolean isCover = true;
    
    for (int i = 0; i < m_Antds.size(); i++) {
      Antd antd = (Antd)m_Antds.elementAt(i);
      if (!antd.isCover(datum)) {
        isCover = false;
        break;
      }
    }
    
    return isCover;
  }
  




  public boolean hasAntds()
  {
    if (m_Antds == null) {
      return false;
    }
    return m_Antds.size() > 0;
  }
  




  private void grow(Instances data)
  {
    Instances growData = new Instances(data);
    
    double whole = data.sumOfWeights();
    
    if (m_NumAntds != 0)
    {

      double[][] classDstr = new double[2][m_NumClasses];
      

      for (int j = 0; j < m_NumClasses; j++) {
        classDstr[0][j] = 0.0D;
        classDstr[1][j] = 0.0D; }
      double defInfo;
      double defInfo; if (m_ClassAttribute.isNominal()) {
        for (int i = 0; i < growData.numInstances(); i++) {
          Instance datum = growData.instance(i);
          classDstr[0][((int)datum.classValue())] += datum.weight();
        }
        defInfo = ContingencyTables.entropy(classDstr[0]);
      }
      else {
        for (int i = 0; i < growData.numInstances(); i++) {
          Instance datum = growData.instance(i);
          classDstr[0][0] += datum.weight() * datum.classValue();
        }
        


        double defMean = classDstr[0][0] / whole;
        defInfo = meanSquaredError(growData, defMean) * growData.sumOfWeights();
      }
      

      double[][] tmp = new double[2][m_NumClasses];
      for (int y = 0; y < m_NumClasses; y++) {
        if (m_ClassAttribute.isNominal()) {
          tmp[0][y] = classDstr[0][y];
          tmp[1][y] = classDstr[1][y];
        }
        else {
          classDstr[0][y] /= whole;
          tmp[1][y] = classDstr[1][y];
        }
      }
      m_Targets.addElement(tmp);
      

      boolean[] used = new boolean[growData.numAttributes()];
      for (int k = 0; k < used.length; k++)
        used[k] = false;
      int numUnused = used.length;
      double uncoveredWtSq = 0.0D;double uncoveredWtVl = 0.0D;double uncoveredWts = 0.0D;
      boolean isContinue = true;
      
      while (isContinue) {
        double maxInfoGain = 0.0D;
        

        Antd oneAntd = null;
        Instances coverData = null;Instances uncoverData = null;
        Enumeration enumAttr = growData.enumerateAttributes();
        int index = -1;
        
        if (m_Debug) {
          System.out.println("Growing data: " + growData);
        }
        

        while (enumAttr.hasMoreElements()) {
          Attribute att = (Attribute)enumAttr.nextElement();
          index++;
          
          Antd antd = null;
          if (m_ClassAttribute.isNominal()) {
            if (att.isNumeric()) {
              antd = new NumericAntd(att, classDstr[1]);
            } else {
              antd = new NominalAntd(att, classDstr[1]);
            }
          }
          else if (att.isNumeric()) {
            antd = new NumericAntd(att, uncoveredWtSq, uncoveredWtVl, uncoveredWts);
          } else {
            antd = new NominalAntd(att, uncoveredWtSq, uncoveredWtVl, uncoveredWts);
          }
          if (used[index] == 0)
          {



            Instances[] coveredData = computeInfoGain(growData, defInfo, antd);
            
            if (coveredData != null) {
              double infoGain = antd.getMaxInfoGain();
              boolean isUpdate = Utils.gr(infoGain, maxInfoGain);
              
              if (m_Debug) {
                System.err.println(antd);
                System.err.println("Info gain: " + infoGain);
                System.err.println("Max info gain: " + maxInfoGain);
              }
              
              if (isUpdate) {
                oneAntd = antd;
                coverData = coveredData[0];
                uncoverData = coveredData[1];
                maxInfoGain = infoGain;
              }
            }
          }
        }
        
        if (oneAntd == null) {
          break;
        }
        if (m_Debug) {
          System.err.println("Adding antecedent: ");
          System.err.println(oneAntd);
          System.err.println("Covered data: ");
          System.err.println(coverData);
          System.err.println("Uncovered data: ");
          System.err.println(uncoverData);
        }
        

        if (!oneAntd.getAttr().isNumeric()) {
          used[oneAntd.getAttr().index()] = true;
          numUnused--;
        }
        
        m_Antds.addElement(oneAntd);
        growData = coverData;
        
        for (int x = 0; x < uncoverData.numInstances(); x++) {
          Instance datum = uncoverData.instance(x);
          if (m_ClassAttribute.isNumeric()) {
            uncoveredWtSq += datum.weight() * datum.classValue() * datum.classValue();
            uncoveredWtVl += datum.weight() * datum.classValue();
            uncoveredWts += datum.weight();
            classDstr[0][0] -= datum.weight() * datum.classValue();
            classDstr[1][0] += datum.weight() * datum.classValue();
          }
          else {
            classDstr[0][((int)datum.classValue())] -= datum.weight();
            classDstr[1][((int)datum.classValue())] += datum.weight();
          }
        }
        

        tmp = new double[2][m_NumClasses];
        for (int y = 0; y < m_NumClasses; y++) {
          if (m_ClassAttribute.isNominal()) {
            tmp[0][y] = classDstr[0][y];
            tmp[1][y] = classDstr[1][y];
          }
          else {
            classDstr[0][y] /= (whole - uncoveredWts);
            classDstr[1][y] /= uncoveredWts;
          }
        }
        m_Targets.addElement(tmp);
        
        defInfo = oneAntd.getInfo();
        if (m_Debug) {
          System.err.println("Default info: " + defInfo);
        }
        int numAntdsThreshold = m_NumAntds == -1 ? Integer.MAX_VALUE : m_NumAntds;
        
        if ((Utils.eq(growData.sumOfWeights(), 0.0D)) || (numUnused == 0) || (m_Antds.size() >= numAntdsThreshold))
        {

          isContinue = false;
        }
      }
    }
    m_Cnsqt = ((double[][])(double[][])m_Targets.lastElement())[0];
    m_DefDstr = ((double[][])(double[][])m_Targets.lastElement())[1];
  }
  







  private Instances[] computeInfoGain(Instances instances, double defInfo, Antd antd)
  {
    Instances data = new Instances(instances);
    


    Instances[] splitData = antd.splitData(data, defInfo);
    Instances[] coveredData = new Instances[2];
    

    Instances tmp1 = new Instances(data, 0);
    Instances tmp2 = new Instances(data, 0);
    
    if (splitData == null) {
      return null;
    }
    for (int x = 0; x < splitData.length - 1; x++) {
      if (x == (int)antd.getAttrValue()) {
        tmp1 = splitData[x];
      } else {
        for (int y = 0; y < splitData[x].numInstances(); y++) {
          tmp2.add(splitData[x].instance(y));
        }
      }
    }
    if (antd.getAttr().isNominal()) {
      if (((NominalAntd)antd).isIn()) {
        coveredData[0] = new Instances(tmp1);
        coveredData[1] = new Instances(tmp2);
      }
      else {
        coveredData[0] = new Instances(tmp2);
        coveredData[1] = new Instances(tmp1);
      }
    }
    else {
      coveredData[0] = new Instances(tmp1);
      coveredData[1] = new Instances(tmp2);
    }
    

    for (int z = 0; z < splitData[(splitData.length - 1)].numInstances(); z++) {
      coveredData[1].add(splitData[(splitData.length - 1)].instance(z));
    }
    return coveredData;
  }
  






  private void prune(Instances pruneData)
  {
    Instances data = new Instances(pruneData);
    Instances otherData = new Instances(data, 0);
    double total = data.sumOfWeights();
    
    double defAccu;
    double defAccu;
    if (m_ClassAttribute.isNumeric()) {
      defAccu = meanSquaredError(pruneData, ((double[][])(double[][])m_Targets.firstElement())[0][0]);
    }
    else {
      int predict = Utils.maxIndex(((double[][])(double[][])m_Targets.firstElement())[0]);
      defAccu = computeAccu(pruneData, predict) / total;
    }
    
    int size = m_Antds.size();
    if (size == 0) {
      m_Cnsqt = ((double[][])(double[][])m_Targets.lastElement())[0];
      m_DefDstr = ((double[][])(double[][])m_Targets.lastElement())[1];
      return;
    }
    
    double[] worthValue = new double[size];
    

    for (int x = 0; x < size; x++) {
      Antd antd = (Antd)m_Antds.elementAt(x);
      Instances newData = new Instances(data);
      if (Utils.eq(newData.sumOfWeights(), 0.0D)) {
        break;
      }
      data = new Instances(newData, newData.numInstances());
      
      for (int y = 0; y < newData.numInstances(); y++) {
        Instance ins = newData.instance(y);
        if (antd.isCover(ins)) {
          data.add(ins);
        } else {
          otherData.add(ins);
        }
      }
      
      double[][] classes = (double[][])m_Targets.elementAt(x + 1);
      double other;
      double covered; double other; if (m_ClassAttribute.isNominal()) {
        int coverClass = Utils.maxIndex(classes[0]);
        int otherClass = Utils.maxIndex(classes[1]);
        
        double covered = computeAccu(data, coverClass);
        other = computeAccu(otherData, otherClass);
      }
      else {
        double coverClass = classes[0][0];
        double otherClass = classes[1][0];
        covered = data.sumOfWeights() * meanSquaredError(data, coverClass);
        other = otherData.sumOfWeights() * meanSquaredError(otherData, otherClass);
      }
      
      worthValue[x] = ((covered + other) / total);
    }
    

    for (int z = size - 1; z > 0; z--) {
      double valueDelta;
      double valueDelta;
      if (m_ClassAttribute.isNominal()) { double valueDelta;
        if (Utils.sm(worthValue[z], 1.0D)) {
          valueDelta = (worthValue[z] - worthValue[(z - 1)]) / worthValue[z];
        } else
          valueDelta = worthValue[z] - worthValue[(z - 1)];
      } else {
        double valueDelta;
        if (Utils.sm(worthValue[z], 1.0D)) {
          valueDelta = (worthValue[(z - 1)] - worthValue[z]) / worthValue[z];
        } else {
          valueDelta = worthValue[(z - 1)] - worthValue[z];
        }
      }
      if (!Utils.smOrEq(valueDelta, 0.0D)) break;
      m_Antds.removeElementAt(z);
      m_Targets.removeElementAt(z + 1);
    }
    



    if (m_Antds.size() == 1) { double valueDelta;
      double valueDelta;
      if (m_ClassAttribute.isNominal()) { double valueDelta;
        if (Utils.sm(worthValue[0], 1.0D)) {
          valueDelta = (worthValue[0] - defAccu) / worthValue[0];
        } else
          valueDelta = worthValue[0] - defAccu;
      } else {
        double valueDelta;
        if (Utils.sm(worthValue[0], 1.0D)) {
          valueDelta = (defAccu - worthValue[0]) / worthValue[0];
        } else {
          valueDelta = defAccu - worthValue[0];
        }
      }
      if (Utils.smOrEq(valueDelta, 0.0D)) {
        m_Antds.removeAllElements();
        m_Targets.removeElementAt(1);
      }
    }
    
    m_Cnsqt = ((double[][])(double[][])m_Targets.lastElement())[0];
    m_DefDstr = ((double[][])(double[][])m_Targets.lastElement())[1];
  }
  







  private double computeAccu(Instances data, int clas)
  {
    double accu = 0.0D;
    for (int i = 0; i < data.numInstances(); i++) {
      Instance inst = data.instance(i);
      if ((int)inst.classValue() == clas)
        accu += inst.weight();
    }
    return accu;
  }
  








  private double meanSquaredError(Instances data, double mean)
  {
    if (Utils.eq(data.sumOfWeights(), 0.0D)) {
      return 0.0D;
    }
    double mSqErr = 0.0D;double sum = data.sumOfWeights();
    for (int i = 0; i < data.numInstances(); i++) {
      Instance datum = data.instance(i);
      mSqErr += datum.weight() * (datum.classValue() - mean) * (datum.classValue() - mean);
    }
    


    return mSqErr / sum;
  }
  






  public String toString(String att, String cl)
  {
    StringBuffer text = new StringBuffer();
    if (m_Antds.size() > 0) {
      for (int j = 0; j < m_Antds.size() - 1; j++)
        text.append("(" + ((Antd)m_Antds.elementAt(j)).toString() + ") and ");
      text.append("(" + ((Antd)m_Antds.lastElement()).toString() + ")");
    }
    text.append(" => " + att + " = " + cl);
    
    return text.toString();
  }
  




  public String toString()
  {
    String title = "\n\nSingle conjunctive rule learner:\n--------------------------------\n";
    
    String body = null;
    StringBuffer text = new StringBuffer();
    if (m_ClassAttribute != null)
      if (m_ClassAttribute.isNominal()) {
        body = toString(m_ClassAttribute.name(), m_ClassAttribute.value(Utils.maxIndex(m_Cnsqt)));
        
        text.append("\n\nClass distributions:\nCovered by the rule:\n");
        for (int k = 0; k < m_Cnsqt.length; k++)
          text.append(m_ClassAttribute.value(k) + "\t");
        text.append('\n');
        for (int l = 0; l < m_Cnsqt.length; l++) {
          text.append(Utils.doubleToString(m_Cnsqt[l], 6) + "\t");
        }
        text.append("\n\nNot covered by the rule:\n");
        for (int k = 0; k < m_DefDstr.length; k++)
          text.append(m_ClassAttribute.value(k) + "\t");
        text.append('\n');
        for (int l = 0; l < m_DefDstr.length; l++) {
          text.append(Utils.doubleToString(m_DefDstr[l], 6) + "\t");
        }
      } else {
        body = toString(m_ClassAttribute.name(), Utils.doubleToString(m_Cnsqt[0], 6));
      }
    return title + body + text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9835 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new ConjunctiveRule(), args);
  }
}

package weka.classifiers.rules;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.UnsupportedClassTypeException;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;







































































public class Ridor
  extends Classifier
  implements AdditionalMeasureProducer, WeightedInstancesHandler
{
  static final long serialVersionUID = -7261533075088314436L;
  private int m_Folds;
  private int m_Shuffle;
  private Random m_Random;
  private int m_Seed;
  private boolean m_IsAllErr;
  private boolean m_IsMajority;
  private Ridor_node m_Root;
  private Attribute m_Class;
  private double m_Cover;
  private double m_Err;
  private double m_MinNo;
  
  public Ridor()
  {
    m_Folds = 3;
    

    m_Shuffle = 1;
    

    m_Random = null;
    

    m_Seed = 1;
    

    m_IsAllErr = false;
    

    m_IsMajority = false;
    

    m_Root = null;
    







    m_MinNo = 2.0D;
  }
  



  public String globalInfo()
  {
    return "An implementation of a RIpple-DOwn Rule learner.\n\nIt generates a default rule first and then the exceptions for the default rule with the least (weighted) error rate.  Then it generates the \"best\" exceptions for each exception and iterates until pure.  Thus it performs a tree-like expansion of exceptions.The exceptions are a set of rules that predict classes other than the default. IREP is used to generate the exceptions.\n\nFor more information about Ripple-Down Rules, see:\n\n";
  }
  








  private class Ridor_node
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -581370560157467677L;
    






    private double defClass = NaN.0D;
    



    private Ridor.RidorRule[] rules = null;
    

    private Ridor_node[] excepts = null;
    

    private int level;
    

    private Ridor_node() {}
    

    public double getDefClass()
    {
      return defClass;
    }
    




    public Ridor.RidorRule[] getRules()
    {
      return rules;
    }
    




    public Ridor_node[] getExcepts()
    {
      return excepts;
    }
    






    public void findRules(Instances[] dataByClass, int lvl)
      throws Exception
    {
      Vector finalRules = null;
      int clas = -1;
      double[] isPure = new double[dataByClass.length];
      int numMajority = 0;
      
      level = (lvl + 1);
      
      for (int h = 0; h < dataByClass.length; h++) {
        isPure[h] = dataByClass[h].sumOfWeights();
        if (Utils.grOrEq(isPure[h], m_Folds)) {
          numMajority++;
        }
      }
      if (numMajority <= 1) {
        defClass = Utils.maxIndex(isPure);
        return;
      }
      double total = Utils.sum(isPure);
      
      if (m_IsMajority) {
        defClass = Utils.maxIndex(isPure);
        Instances data = new Instances(dataByClass[((int)defClass)]);
        int index = data.classIndex();
        
        for (int j = 0; j < data.numInstances(); j++) {
          data.instance(j).setClassValue(1.0D);
        }
        for (int k = 0; k < dataByClass.length; k++) {
          if (k != (int)defClass) {
            if (data.numInstances() >= dataByClass[k].numInstances())
              data = append(data, dataByClass[k]); else
              data = append(dataByClass[k], data);
          }
        }
        data.setClassIndex(index);
        
        double classCount = total - isPure[((int)defClass)];
        finalRules = new Vector();
        buildRuleset(data, classCount, finalRules);
        if (finalRules.size() == 0) {
          return;
        }
      } else {
        double maxAcRt = isPure[Utils.maxIndex(isPure)] / total;
        

        for (int i = 0; i < dataByClass.length; i++) {
          if (isPure[i] >= m_Folds) {
            Instances data = new Instances(dataByClass[i]);
            int index = data.classIndex();
            
            for (int j = 0; j < data.numInstances(); j++) {
              data.instance(j).setClassValue(1.0D);
            }
            for (int k = 0; k < dataByClass.length; k++) {
              if (k != i) {
                if (data.numInstances() >= dataByClass[k].numInstances())
                  data = append(data, dataByClass[k]); else
                  data = append(dataByClass[k], data);
              }
            }
            data.setClassIndex(index);
            

            double classCount = data.sumOfWeights() - isPure[i];
            Vector ruleset = new Vector();
            double wAcRt = buildRuleset(data, classCount, ruleset);
            
            if (Utils.gr(wAcRt, maxAcRt)) {
              finalRules = ruleset;
              maxAcRt = wAcRt;
              clas = i;
            }
          }
        }
        
        if (finalRules == null) {
          defClass = Utils.maxIndex(isPure);
          return;
        }
        
        defClass = clas;
      }
      

      int size = finalRules.size();
      rules = new Ridor.RidorRule[size];
      excepts = new Ridor_node[size];
      for (int l = 0; l < size; l++) {
        rules[l] = ((Ridor.RidorRule)finalRules.elementAt(l));
      }
      
      Instances[] uncovered = dataByClass;
      if (level == 1) {
        m_Err = (total - uncovered[((int)defClass)].sumOfWeights());
      }
      uncovered[((int)defClass)] = new Instances(uncovered[((int)defClass)], 0);
      
      for (int m = 0; m < size; m++)
      {
        Instances[][] dvdData = divide(rules[m], uncovered);
        Instances[] covered = dvdData[0];
        
        excepts[m] = new Ridor_node(Ridor.this);
        excepts[m].findRules(covered, level);
      }
    }
    










    private double buildRuleset(Instances insts, double classCount, Vector ruleset)
      throws Exception
    {
      Instances data = new Instances(insts);
      double wAcRt = 0.0D;
      double total = data.sumOfWeights();
      
      while (classCount >= m_Folds) {
        Ridor.RidorRule bestRule = null;
        double bestWorthRate = -1.0D;
        double bestWorth = -1.0D;
        
        Ridor.RidorRule rule = new Ridor.RidorRule(Ridor.this, null);
        rule.setPredictedClass(0.0D);
        
        for (int j = 0; j < m_Shuffle; j++) {
          if (m_Shuffle > 1) {
            data.randomize(m_Random);
          }
          rule.buildClassifier(data);
          double w;
          double wr;
          double w; if (m_IsAllErr) {
            double wr = (rule.getWorth() + rule.getAccuG()) / (rule.getCoverP() + rule.getCoverG());
            
            w = rule.getWorth() + rule.getAccuG();
          }
          else {
            wr = rule.getWorthRate();
            w = rule.getWorth();
          }
          
          if ((Utils.gr(wr, bestWorthRate)) || ((Utils.eq(wr, bestWorthRate)) && (Utils.gr(w, bestWorth))))
          {
            bestRule = rule;
            bestWorthRate = wr;
            bestWorth = w;
          }
        }
        
        if (bestRule == null) {
          throw new Exception("Something wrong here inside findRule()!");
        }
        if ((Utils.sm(bestWorthRate, 0.5D)) || (!bestRule.hasAntds())) {
          break;
        }
        Instances newData = new Instances(data);
        data = new Instances(newData, 0);
        classCount = 0.0D;
        double cover = 0.0D;
        
        for (int l = 0; l < newData.numInstances(); l++) {
          Instance datum = newData.instance(l);
          if (!bestRule.isCover(datum)) {
            data.add(datum);
            if (Utils.eq(datum.classValue(), 0.0D))
              classCount += datum.weight();
          } else {
            cover += datum.weight();
          }
        }
        wAcRt += computeWeightedAcRt(bestWorthRate, cover, total);
        ruleset.addElement(bestRule);
      }
      

      double wDefAcRt = (data.sumOfWeights() - classCount) / total;
      wAcRt += wDefAcRt;
      
      return wAcRt;
    }
    






    private Instances append(Instances data1, Instances data2)
    {
      Instances data = new Instances(data1);
      for (int i = 0; i < data2.numInstances(); i++) {
        data.add(data2.instance(i));
      }
      return data;
    }
    
















    private double computeWeightedAcRt(double worthRt, double cover, double total)
    {
      return worthRt * (cover / total);
    }
    










    private Instances[][] divide(Ridor.RidorRule rule, Instances[] dataByClass)
    {
      int len = dataByClass.length;
      Instances[][] dataBags = new Instances[2][len];
      
      for (int i = 0; i < len; i++) {
        Instances[] dvdData = rule.coveredByRule(dataByClass[i]);
        dataBags[0][i] = dvdData[0];
        dataBags[1][i] = dvdData[1];
      }
      
      return dataBags;
    }
    




    public int size()
    {
      int size = 0;
      if (rules != null) {
        for (int i = 0; i < rules.length; i++)
          size += excepts[i].size();
        size += rules.length;
      }
      return size;
    }
    




    public String toString()
    {
      StringBuffer text = new StringBuffer();
      
      if (level == 1) {
        text.append(m_Class.name() + " = " + m_Class.value((int)getDefClass()) + "  (" + m_Cover + "/" + m_Err + ")\n");
      }
      if (rules != null) {
        for (int i = 0; i < rules.length; i++) {
          for (int j = 0; j < level; j++)
            text.append("         ");
          String cl = m_Class.value((int)excepts[i].getDefClass());
          text.append("  Except " + rules[i].toString(m_Class.name(), cl) + "\n" + excepts[i].toString());
        }
      }
      


      return text.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5529 $");
    }
  }
  




  private class RidorRule
    implements WeightedInstancesHandler, Serializable, RevisionHandler
  {
    static final long serialVersionUID = 4375199423973848157L;
    



    private RidorRule() {}
    



    private double m_Class = -1.0D;
    

    private Attribute m_ClassAttribute;
    

    protected FastVector m_Antds = null;
    

    private double m_WorthRate = 0.0D;
    

    private double m_Worth = 0.0D;
    

    private double m_CoverP = 0.0D;
    

    private double m_CoverG = 0.0D; private double m_AccuG = 0.0D;
    

    public void setPredictedClass(double cl) { m_Class = cl; }
    public double getPredictedClass() { return m_Class; }
    






    public void buildClassifier(Instances instances)
      throws Exception
    {
      m_ClassAttribute = instances.classAttribute();
      if (!m_ClassAttribute.isNominal())
        throw new UnsupportedClassTypeException(" Only nominal class, please.");
      if (instances.numClasses() != 2) {
        throw new Exception(" Only 2 classes, please.");
      }
      Instances data = new Instances(instances);
      if (Utils.eq(data.sumOfWeights(), 0.0D)) {
        throw new Exception(" No training data.");
      }
      data.deleteWithMissingClass();
      if (Utils.eq(data.sumOfWeights(), 0.0D)) {
        throw new Exception(" The class labels of all the training data are missing.");
      }
      if (data.numInstances() < m_Folds) {
        throw new Exception(" Not enough data for REP.");
      }
      m_Antds = new FastVector();
      

      m_Random = new Random(m_Seed);
      data.randomize(m_Random);
      data.stratify(m_Folds);
      Instances growData = data.trainCV(m_Folds, m_Folds - 1, m_Random);
      Instances pruneData = data.testCV(m_Folds, m_Folds - 1);
      
      grow(growData);
      
      prune(pruneData);
    }
    







    public Instances[] coveredByRule(Instances insts)
    {
      Instances[] data = new Instances[2];
      data[0] = new Instances(insts, insts.numInstances());
      data[1] = new Instances(insts, insts.numInstances());
      
      for (int i = 0; i < insts.numInstances(); i++) {
        Instance datum = insts.instance(i);
        if (isCover(datum)) {
          data[0].add(datum);
        } else {
          data[1].add(datum);
        }
      }
      return data;
    }
    





    public boolean isCover(Instance datum)
    {
      boolean isCover = true;
      
      for (int i = 0; i < m_Antds.size(); i++) {
        Ridor.Antd antd = (Ridor.Antd)m_Antds.elementAt(i);
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
      
      m_AccuG = computeDefAccu(growData);
      m_CoverG = growData.sumOfWeights();
      
      double defAcRt = m_AccuG / m_CoverG;
      

      boolean[] used = new boolean[growData.numAttributes()];
      for (int k = 0; k < used.length; k++)
        used[k] = false;
      int numUnused = used.length;
      

      boolean isContinue = true;
      
      while (isContinue) {
        double maxInfoGain = 0.0D;
        

        Ridor.Antd oneAntd = null;
        Instances coverData = null;
        Enumeration enumAttr = growData.enumerateAttributes();
        int index = -1;
        

        while (enumAttr.hasMoreElements()) {
          Attribute att = (Attribute)enumAttr.nextElement();
          index++;
          
          Ridor.Antd antd = null;
          if (att.isNumeric()) {
            antd = new Ridor.NumericAntd(Ridor.this, att);
          } else {
            antd = new Ridor.NominalAntd(Ridor.this, att);
          }
          if (used[index] == 0)
          {


            Instances coveredData = computeInfoGain(growData, defAcRt, antd);
            if (coveredData != null) {
              double infoGain = antd.getMaxInfoGain();
              if (Utils.gr(infoGain, maxInfoGain)) {
                oneAntd = antd;
                coverData = coveredData;
                maxInfoGain = infoGain;
              }
            }
          }
        }
        
        if (oneAntd == null) { return;
        }
        
        if (!oneAntd.getAttr().isNumeric()) {
          used[oneAntd.getAttr().index()] = true;
          numUnused--;
        }
        
        m_Antds.addElement(oneAntd);
        growData = coverData;
        
        defAcRt = oneAntd.getAccuRate();
        

        if ((Utils.eq(growData.sumOfWeights(), 0.0D)) || (Utils.eq(defAcRt, 1.0D)) || (numUnused == 0)) {
          isContinue = false;
        }
      }
    }
    






    private Instances computeInfoGain(Instances instances, double defAcRt, Ridor.Antd antd)
    {
      Instances data = new Instances(instances);
      


      Instances[] splitData = antd.splitData(data, defAcRt, m_Class);
      

      if (splitData != null)
        return splitData[((int)antd.getAttrValue())];
      return null;
    }
    





    private void prune(Instances pruneData)
    {
      Instances data = new Instances(pruneData);
      
      double total = data.sumOfWeights();
      

      double defAccu = 0.0D;double defAccuRate = 0.0D;
      
      int size = m_Antds.size();
      if (size == 0) { return;
      }
      double[] worthRt = new double[size];
      double[] coverage = new double[size];
      double[] worthValue = new double[size];
      for (int w = 0; w < size; w++) {
        double tmp78_77 = (worthValue[w] = 0.0D);coverage[w] = tmp78_77;worthRt[w] = tmp78_77;
      }
      

      for (int x = 0; x < size; x++) {
        Ridor.Antd antd = (Ridor.Antd)m_Antds.elementAt(x);
        Attribute attr = antd.getAttr();
        Instances newData = new Instances(data);
        data = new Instances(newData, newData.numInstances());
        
        for (int y = 0; y < newData.numInstances(); y++) {
          Instance ins = newData.instance(y);
          if ((!ins.isMissing(attr)) && 
            (antd.isCover(ins))) {
            coverage[x] += ins.weight();
            data.add(ins);
            if (Utils.eq(ins.classValue(), m_Class)) {
              worthValue[x] += ins.weight();
            }
          }
        }
        
        if (coverage[x] != 0.0D) {
          worthValue[x] /= coverage[x];
        }
      }
      
      for (int z = size - 1; z > 0; z--) {
        if (!Utils.sm(worthRt[z], worthRt[(z - 1)])) break;
        m_Antds.removeElementAt(z);
      }
      

      if (m_Antds.size() == 1) {
        defAccu = computeDefAccu(pruneData);
        defAccuRate = defAccu / total;
        if (Utils.sm(worthRt[0], defAccuRate)) {
          m_Antds.removeAllElements();
        }
      }
      

      int antdsSize = m_Antds.size();
      if (antdsSize != 0) {
        m_Worth = worthValue[(antdsSize - 1)];
        m_WorthRate = worthRt[(antdsSize - 1)];
        m_CoverP = coverage[(antdsSize - 1)];
        Ridor.Antd last = (Ridor.Antd)m_Antds.lastElement();
        m_CoverG = last.getCover();
        m_AccuG = last.getAccu();
      }
      else {
        m_Worth = defAccu;
        m_WorthRate = defAccuRate;
        m_CoverP = total;
      }
    }
    






    private double computeDefAccu(Instances data)
    {
      double defAccu = 0.0D;
      for (int i = 0; i < data.numInstances(); i++) {
        Instance inst = data.instance(i);
        if (Utils.eq(inst.classValue(), m_Class))
          defAccu += inst.weight();
      }
      return defAccu;
    }
    

    public double getWorthRate() { return m_WorthRate; }
    public double getWorth() { return m_Worth; }
    public double getCoverP() { return m_CoverP; }
    public double getCoverG() { return m_CoverG; }
    public double getAccuG() { return m_AccuG; }
    






    public String toString(String att, String cl)
    {
      StringBuffer text = new StringBuffer();
      if (m_Antds.size() > 0) {
        for (int j = 0; j < m_Antds.size() - 1; j++)
          text.append("(" + ((Ridor.Antd)m_Antds.elementAt(j)).toString() + ") and ");
        text.append("(" + ((Ridor.Antd)m_Antds.lastElement()).toString() + ")");
      }
      text.append(" => " + att + " = " + cl);
      text.append("  (" + m_CoverG + "/" + (m_CoverG - m_AccuG) + ") [" + m_CoverP + "/" + (m_CoverP - m_Worth) + "]");
      
      return text.toString();
    }
    




    public String toString()
    {
      return toString(m_ClassAttribute.name(), m_ClassAttribute.value((int)m_Class));
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5529 $");
    }
  }
  



  private abstract class Antd
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 5317379013858933369L;
    


    protected Attribute att;
    


    protected double value;
    


    protected double maxInfoGain;
    

    protected double accuRate;
    

    protected double cover;
    

    protected double accu;
    


    public Antd(Attribute a)
    {
      att = a;
      value = NaN.0D;
      maxInfoGain = 0.0D;
      accuRate = NaN.0D;
      cover = NaN.0D;
      accu = NaN.0D;
    }
    
    public abstract Instances[] splitData(Instances paramInstances, double paramDouble1, double paramDouble2);
    
    public abstract boolean isCover(Instance paramInstance);
    
    public abstract String toString();
    
    public Attribute getAttr() { return att; }
    public double getAttrValue() { return value; }
    public double getMaxInfoGain() { return maxInfoGain; }
    public double getAccuRate() { return accuRate; }
    public double getAccu() { return accu; }
    public double getCover() { return cover; }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5529 $");
    }
  }
  


  private class NumericAntd
    extends Ridor.Antd
  {
    static final long serialVersionUID = 1968761518014492214L;
    

    private double splitPoint;
    


    public NumericAntd(Attribute a)
    {
      super(a);
      splitPoint = NaN.0D;
    }
    
    public double getSplitPoint() {
      return splitPoint;
    }
    









    public Instances[] splitData(Instances insts, double defAcRt, double cl)
    {
      Instances data = new Instances(insts);
      data.sort(att);
      int total = data.numInstances();
      

      int split = 1;
      int prev = 0;
      int finalSplit = split;
      maxInfoGain = 0.0D;
      value = 0.0D;
      

      double minSplit = 0.1D * data.sumOfWeights() / 2.0D;
      if (Utils.smOrEq(minSplit, m_MinNo)) {
        minSplit = m_MinNo;
      } else if (Utils.gr(minSplit, 25.0D)) {
        minSplit = 25.0D;
      }
      double fstCover = 0.0D;double sndCover = 0.0D;double fstAccu = 0.0D;double sndAccu = 0.0D;
      
      for (int x = 0; x < data.numInstances(); x++) {
        Instance inst = data.instance(x);
        if (inst.isMissing(att)) {
          total = x;
          break;
        }
        
        sndCover += inst.weight();
        if (Utils.eq(inst.classValue(), cl)) {
          sndAccu += inst.weight();
        }
      }
      
      if (Utils.sm(sndCover, 2.0D * minSplit)) {
        return null;
      }
      if (total == 0) return null;
      splitPoint = data.instance(total - 1).value(att);
      for (; 
          split < total; split++) {
        if (!Utils.eq(data.instance(split).value(att), data.instance(prev).value(att)))
        {

          for (int y = prev; y < split; y++) {
            Instance inst = data.instance(y);
            fstCover += inst.weight();sndCover -= inst.weight();
            if (Utils.eq(data.instance(y).classValue(), cl)) {
              fstAccu += inst.weight();
              sndAccu -= inst.weight();
            }
          }
          
          if ((Utils.sm(fstCover, minSplit)) || (Utils.sm(sndCover, minSplit))) {
            prev = split;
          }
          else
          {
            double fstAccuRate = 0.0D;double sndAccuRate = 0.0D;
            if (!Utils.eq(fstCover, 0.0D))
              fstAccuRate = fstAccu / fstCover;
            if (!Utils.eq(sndCover, 0.0D)) {
              sndAccuRate = sndAccu / sndCover;
            }
            




            double fstInfoGain = Utils.eq(fstAccuRate, 0.0D) ? 0.0D : fstAccu * (Utils.log2(fstAccuRate) - Utils.log2(defAcRt));
            
            double sndInfoGain = Utils.eq(sndAccuRate, 0.0D) ? 0.0D : sndAccu * (Utils.log2(sndAccuRate) - Utils.log2(defAcRt));
            double coverage;
            boolean isFirst; double infoGain; double accRate; double accurate; double coverage; if ((Utils.gr(fstInfoGain, sndInfoGain)) || ((Utils.eq(fstInfoGain, sndInfoGain)) && (Utils.grOrEq(fstAccuRate, sndAccuRate))))
            {
              boolean isFirst = true;
              double infoGain = fstInfoGain;
              double accRate = fstAccuRate;
              double accurate = fstAccu;
              coverage = fstCover;
            }
            else {
              isFirst = false;
              infoGain = sndInfoGain;
              accRate = sndAccuRate;
              accurate = sndAccu;
              coverage = sndCover;
            }
            
            boolean isUpdate = Utils.gr(infoGain, maxInfoGain);
            

            if (isUpdate) {
              splitPoint = ((data.instance(split).value(att) + data.instance(prev).value(att)) / 2.0D);
              
              value = (isFirst ? 0 : 1);
              accuRate = accRate;
              accu = accurate;
              cover = coverage;
              maxInfoGain = infoGain;
              finalSplit = split;
            }
            prev = split;
          }
        }
      }
      
      Instances[] splitData = new Instances[2];
      splitData[0] = new Instances(data, 0, finalSplit);
      splitData[1] = new Instances(data, finalSplit, total - finalSplit);
      
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
      return RevisionUtils.extract("$Revision: 5529 $");
    }
  }
  


  private class NominalAntd
    extends Ridor.Antd
  {
    static final long serialVersionUID = -256386137196078004L;
    

    private double[] accurate;
    
    private double[] coverage;
    
    private double[] infoGain;
    

    public NominalAntd(Attribute a)
    {
      super(a);
      int bag = att.numValues();
      accurate = new double[bag];
      coverage = new double[bag];
      infoGain = new double[bag];
    }
    










    public Instances[] splitData(Instances data, double defAcRt, double cl)
    {
      int bag = att.numValues();
      Instances[] splitData = new Instances[bag];
      
      for (int x = 0; x < bag; x++) {
        double tmp47_46 = (infoGain[x] = 0.0D);coverage[x] = tmp47_46;accurate[x] = tmp47_46;
        splitData[x] = new Instances(data, data.numInstances());
      }
      
      for (int x = 0; x < data.numInstances(); x++) {
        Instance inst = data.instance(x);
        if (!inst.isMissing(att)) {
          int v = (int)inst.value(att);
          splitData[v].add(inst);
          coverage[v] += inst.weight();
          if (Utils.eq(inst.classValue(), cl)) {
            accurate[v] += inst.weight();
          }
        }
      }
      
      int count = 0;
      for (int x = 0; x < bag; x++) {
        double t = coverage[x];
        if (Utils.grOrEq(t, m_MinNo)) {
          double p = accurate[x];
          
          if (!Utils.eq(t, 0.0D))
            infoGain[x] = (p * (Utils.log2(p / t) - Utils.log2(defAcRt)));
          count++;
        }
      }
      
      if (count < 2) {
        return null;
      }
      value = Utils.maxIndex(infoGain);
      
      cover = coverage[((int)value)];
      accu = accurate[((int)value)];
      
      if (!Utils.eq(cover, 0.0D))
        accuRate = (accu / cover); else {
        accuRate = 0.0D;
      }
      maxInfoGain = infoGain[((int)value)];
      
      return splitData;
    }
    






    public boolean isCover(Instance inst)
    {
      boolean isCover = false;
      if ((!inst.isMissing(att)) && 
        (Utils.eq(inst.value(att), value))) {
        isCover = true;
      }
      return isCover;
    }
    




    public String toString()
    {
      return att.name() + " = " + att.value((int)value);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5529 $");
    }
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
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    Instances data = new Instances(instances);
    data.deleteWithMissingClass();
    
    int numCl = data.numClasses();
    m_Root = new Ridor_node(null);
    m_Class = instances.classAttribute();
    
    int index = data.classIndex();
    m_Cover = data.sumOfWeights();
    
    m_Random = new Random(m_Seed);
    

    FastVector binary_values = new FastVector(2);
    binary_values.addElement("otherClasses");
    binary_values.addElement("defClass");
    Attribute attr = new Attribute("newClass", binary_values);
    data.insertAttributeAt(attr, index);
    data.setClassIndex(index);
    

    Instances[] dataByClass = new Instances[numCl];
    for (int i = 0; i < numCl; i++)
      dataByClass[i] = new Instances(data, data.numInstances());
    for (int i = 0; i < data.numInstances(); i++) {
      Instance inst = data.instance(i);
      inst.setClassValue(0.0D);
      dataByClass[((int)inst.value(index + 1))].add(inst);
    }
    
    for (int i = 0; i < numCl; i++) {
      dataByClass[i].deleteAttributeAt(index + 1);
    }
    m_Root.findRules(dataByClass, 0);
  }
  






  public double classifyInstance(Instance datum)
  {
    return classify(m_Root, datum);
  }
  






  private double classify(Ridor_node node, Instance datum)
  {
    double classValue = node.getDefClass();
    RidorRule[] rules = node.getRules();
    
    if (rules != null) {
      Ridor_node[] excepts = node.getExcepts();
      for (int i = 0; i < excepts.length; i++) {
        if (rules[i].isCover(datum)) {
          classValue = classify(excepts[i], datum);
          break;
        }
      }
    }
    
    return classValue;
  }
  


























  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tSet number of folds for IREP\n\tOne fold is used as pruning set.\n\t(default 3)", "F", 1, "-F <number of folds>"));
    

    newVector.addElement(new Option("\tSet number of shuffles to randomize\n\tthe data in order to get better rule.\n\t(default 10)", "S", 1, "-S <number of shuffles>"));
    

    newVector.addElement(new Option("\tSet flag of whether use the error rate \n\tof all the data to select the default class\n\tin each step. If not set, the learner will only use\tthe error rate in the pruning data", "A", 0, "-A"));
    


    newVector.addElement(new Option("\t Set flag of whether use the majority class as\n\tthe default class in each step instead of \n\tchoosing default class based on the error rate\n\t(if the flag is not set)", "M", 0, "-M"));
    


    newVector.addElement(new Option("\tSet the minimal weights of instances\n\twithin a split.\n\t(default 2.0)", "N", 1, "-N <min. weights>"));
    

    return newVector.elements();
  }
  




































  public void setOptions(String[] options)
    throws Exception
  {
    String numFoldsString = Utils.getOption('F', options);
    if (numFoldsString.length() != 0) {
      m_Folds = Integer.parseInt(numFoldsString);
    } else {
      m_Folds = 3;
    }
    String numShuffleString = Utils.getOption('S', options);
    if (numShuffleString.length() != 0) {
      m_Shuffle = Integer.parseInt(numShuffleString);
    } else {
      m_Shuffle = 1;
    }
    String seedString = Utils.getOption('s', options);
    if (seedString.length() != 0) {
      m_Seed = Integer.parseInt(seedString);
    } else {
      m_Seed = 1;
    }
    String minNoString = Utils.getOption('N', options);
    if (minNoString.length() != 0) {
      m_MinNo = Double.parseDouble(minNoString);
    } else {
      m_MinNo = 2.0D;
    }
    m_IsAllErr = Utils.getFlag('A', options);
    m_IsMajority = Utils.getFlag('M', options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[8];
    int current = 0;
    options[(current++)] = "-F";options[(current++)] = ("" + m_Folds);
    options[(current++)] = "-S";options[(current++)] = ("" + m_Shuffle);
    options[(current++)] = "-N";options[(current++)] = ("" + m_MinNo);
    
    if (m_IsAllErr)
      options[(current++)] = "-A";
    if (m_IsMajority)
      options[(current++)] = "-M";
    while (current < options.length)
      options[(current++)] = "";
    return options;
  }
  






  public String foldsTipText()
  {
    return "Determines the amount of data used for pruning. One fold is used for pruning, the rest for growing the rules.";
  }
  

  public void setFolds(int fold) { m_Folds = fold; }
  public int getFolds() { return m_Folds; }
  




  public String shuffleTipText()
  {
    return "Determines how often the data is shuffled before a rule is chosen. If > 1, a rule is learned multiple times and the most accurate rule is chosen.";
  }
  


  public void setShuffle(int sh) { m_Shuffle = sh; }
  public int getShuffle() { return m_Shuffle; }
  




  public String seedTipText()
  {
    return "The seed used for randomizing the data.";
  }
  
  public void setSeed(int s) { m_Seed = s; }
  public int getSeed() { return m_Seed; }
  




  public String wholeDataErrTipText()
  {
    return "Whether worth of rule is computed based on all the data or just based on data covered by rule.";
  }
  

  public void setWholeDataErr(boolean a) { m_IsAllErr = a; }
  public boolean getWholeDataErr() { return m_IsAllErr; }
  






  public String majorityClassTipText() { return "Whether the majority class is used as default."; }
  
  public void setMajorityClass(boolean m) { m_IsMajority = m; }
  public boolean getMajorityClass() { return m_IsMajority; }
  




  public String minNoTipText()
  {
    return "The minimum total weight of the instances in a rule.";
  }
  
  public void setMinNo(double m) { m_MinNo = m; }
  public double getMinNo() { return m_MinNo; }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(1);
    newVector.addElement("measureNumRules");
    return newVector.elements();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0) {
      return numRules();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (Ripple down rule learner)");
  }
  




  private double numRules()
  {
    int size = 0;
    if (m_Root != null) {
      size = m_Root.size();
    }
    return size + 1;
  }
  




  public String toString()
  {
    if (m_Root == null) {
      return "RIpple DOwn Rule Learner(Ridor): No model built yet.";
    }
    return "RIpple DOwn Rule Learner(Ridor) rules\n--------------------------------------\n\n" + m_Root.toString() + "\nTotal number of rules (incl. the default rule): " + (int)numRules();
  }
  







  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5529 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new Ridor(), args);
  }
}

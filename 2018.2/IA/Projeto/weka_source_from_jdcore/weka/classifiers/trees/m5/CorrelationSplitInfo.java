package weka.classifiers.trees.m5;

import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.experiment.PairedStats;
import weka.experiment.Stats;



























































public final class CorrelationSplitInfo
  implements Cloneable, Serializable, SplitEvaluate, RevisionHandler
{
  private static final long serialVersionUID = 4212734895125452770L;
  private int m_first;
  private int m_last;
  private int m_position;
  private double m_maxImpurity;
  private int m_splitAttr;
  private double m_splitValue;
  private int m_number;
  
  public CorrelationSplitInfo(int low, int high, int attr)
  {
    initialize(low, high, attr);
  }
  

  public final SplitEvaluate copy()
    throws Exception
  {
    CorrelationSplitInfo s = (CorrelationSplitInfo)clone();
    
    return s;
  }
  






  public final void initialize(int low, int high, int attr)
  {
    m_number = (high - low + 1);
    m_first = low;
    m_last = high;
    m_position = -1;
    m_maxImpurity = -1.7976931348623157E308D;
    m_splitAttr = attr;
    m_splitValue = 0.0D;
  }
  








  public final void attrSplit(int attr, Instances inst)
    throws Exception
  {
    int low = 0;
    int high = inst.numInstances() - 1;
    PairedStats full = new PairedStats(0.01D);
    PairedStats leftSubset = new PairedStats(0.01D);
    PairedStats rightSubset = new PairedStats(0.01D);
    int classIndex = inst.classIndex();
    

    double order = 2.0D;
    
    initialize(low, high, attr);
    
    if (m_number < 4) {
      return;
    }
    
    int len = high - low + 1 < 5 ? 1 : (high - low + 1) / 5;
    m_position = low;
    int part = low + len - 1;
    

    for (int i = low; i < len; i++) {
      full.add(inst.instance(i).value(attr), inst.instance(i).value(classIndex));
      
      leftSubset.add(inst.instance(i).value(attr), inst.instance(i).value(classIndex));
    }
    

    for (i = len; i < inst.numInstances(); i++) {
      full.add(inst.instance(i).value(attr), inst.instance(i).value(classIndex));
      
      rightSubset.add(inst.instance(i).value(attr), inst.instance(i).value(classIndex));
    }
    

    full.calculateDerived();
    
    double allVar = yStats.stdDev * yStats.stdDev;
    allVar = Math.abs(allVar);
    allVar = Math.pow(allVar, 1.0D / order);
    
    for (i = low + len; i < high - len - 1; i++) {
      rightSubset.subtract(inst.instance(i).value(attr), inst.instance(i).value(classIndex));
      
      leftSubset.add(inst.instance(i).value(attr), inst.instance(i).value(classIndex));
      

      if (!Utils.eq(inst.instance(i + 1).value(attr), inst.instance(i).value(attr)))
      {
        leftSubset.calculateDerived();
        rightSubset.calculateDerived();
        
        double leftCorr = Math.abs(correlation);
        double rightCorr = Math.abs(correlation);
        double leftVar = yStats.stdDev * yStats.stdDev;
        leftVar = Math.abs(leftVar);
        leftVar = Math.pow(leftVar, 1.0D / order);
        double rightVar = yStats.stdDev * yStats.stdDev;
        rightVar = Math.abs(rightVar);
        rightVar = Math.pow(rightVar, 1.0D / order);
        
        double score = allVar - count / count * leftVar - count / count * rightVar;
        


        leftCorr = count / count * leftCorr;
        rightCorr = count / count * rightCorr;
        
        double c_score = leftCorr + rightCorr - Math.abs(correlation);
        

        if ((!Utils.eq(score, 0.0D)) && 
          (score > m_maxImpurity)) {
          m_maxImpurity = score;
          m_splitValue = ((inst.instance(i).value(attr) + inst.instance(i + 1).value(attr)) * 0.5D);
          

          m_position = i;
        }
      }
    }
  }
  





  public double maxImpurity()
  {
    return m_maxImpurity;
  }
  




  public int splitAttr()
  {
    return m_splitAttr;
  }
  





  public int position()
  {
    return m_position;
  }
  




  public double splitValue()
  {
    return m_splitValue;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

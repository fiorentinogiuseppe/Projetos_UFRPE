package weka.classifiers.evaluation;

import weka.core.RevisionHandler;
import weka.core.RevisionUtils;






























public class TwoClassStats
  implements RevisionHandler
{
  private static final String[] CATEGORY_NAMES = { "negative", "positive" };
  



  private double m_TruePos;
  


  private double m_FalsePos;
  


  private double m_TrueNeg;
  


  private double m_FalseNeg;
  



  public TwoClassStats(double tp, double fp, double tn, double fn)
  {
    setTruePositive(tp);
    setFalsePositive(fp);
    setTrueNegative(tn);
    setFalseNegative(fn);
  }
  
  public void setTruePositive(double tp) {
    m_TruePos = tp;
  }
  
  public void setFalsePositive(double fp) { m_FalsePos = fp; }
  
  public void setTrueNegative(double tn) {
    m_TrueNeg = tn;
  }
  
  public void setFalseNegative(double fn) { m_FalseNeg = fn; }
  
  public double getTruePositive() {
    return m_TruePos;
  }
  
  public double getFalsePositive() { return m_FalsePos; }
  
  public double getTrueNegative() {
    return m_TrueNeg;
  }
  
  public double getFalseNegative() { return m_FalseNeg; }
  










  public double getTruePositiveRate()
  {
    if (0.0D == m_TruePos + m_FalseNeg) {
      return 0.0D;
    }
    return m_TruePos / (m_TruePos + m_FalseNeg);
  }
  











  public double getFalsePositiveRate()
  {
    if (0.0D == m_FalsePos + m_TrueNeg) {
      return 0.0D;
    }
    return m_FalsePos / (m_FalsePos + m_TrueNeg);
  }
  











  public double getPrecision()
  {
    if (0.0D == m_TruePos + m_FalsePos) {
      return 0.0D;
    }
    return m_TruePos / (m_TruePos + m_FalsePos);
  }
  











  public double getRecall()
  {
    return getTruePositiveRate();
  }
  










  public double getFMeasure()
  {
    double precision = getPrecision();
    double recall = getRecall();
    if (precision + recall == 0.0D) {
      return 0.0D;
    }
    return 2.0D * precision * recall / (precision + recall);
  }
  










  public double getFallout()
  {
    if (0.0D == m_TruePos + m_FalsePos) {
      return 0.0D;
    }
    return m_FalsePos / (m_TruePos + m_FalsePos);
  }
  







  public ConfusionMatrix getConfusionMatrix()
  {
    ConfusionMatrix cm = new ConfusionMatrix(CATEGORY_NAMES);
    cm.setElement(0, 0, m_TrueNeg);
    cm.setElement(0, 1, m_FalsePos);
    cm.setElement(1, 0, m_FalseNeg);
    cm.setElement(1, 1, m_TruePos);
    return cm;
  }
  




  public String toString()
  {
    StringBuffer res = new StringBuffer();
    res.append(getTruePositive()).append(' ');
    res.append(getFalseNegative()).append(' ');
    res.append(getTrueNegative()).append(' ');
    res.append(getFalsePositive()).append(' ');
    res.append(getFalsePositiveRate()).append(' ');
    res.append(getTruePositiveRate()).append(' ');
    res.append(getPrecision()).append(' ');
    res.append(getRecall()).append(' ');
    res.append(getFMeasure()).append(' ');
    res.append(getFallout()).append(' ');
    return res.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}

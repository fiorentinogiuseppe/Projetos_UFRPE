package weka.experiment;

import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;












































public class PairedStatsCorrected
  extends PairedStats
{
  protected double m_testTrainRatio;
  
  public PairedStatsCorrected(double sig, double testTrainRatio)
  {
    super(sig);
    m_testTrainRatio = testTrainRatio;
  }
  



  public void calculateDerived()
  {
    xStats.calculateDerived();
    yStats.calculateDerived();
    differencesStats.calculateDerived();
    
    correlation = NaN.0D;
    if ((!Double.isNaN(xStats.stdDev)) && (!Double.isNaN(yStats.stdDev)) && (!Utils.eq(xStats.stdDev, 0.0D)))
    {
      double slope = (xySum - xStats.sum * yStats.sum / count) / (xStats.sumSq - xStats.sum * xStats.mean);
      
      if (!Utils.eq(yStats.stdDev, 0.0D)) {
        correlation = (slope * xStats.stdDev / yStats.stdDev);
      } else {
        correlation = 1.0D;
      }
    }
    
    if (Utils.gr(differencesStats.stdDev, 0.0D))
    {
      double tval = differencesStats.mean / Math.sqrt((1.0D / count + m_testTrainRatio) * differencesStats.stdDev * differencesStats.stdDev);
      


      if (count > 1.0D) {
        differencesProbability = Statistics.FProbability(tval * tval, 1, (int)count - 1);
      } else {
        differencesProbability = 1.0D;
      }
    } else if (differencesStats.sumSq == 0.0D) {
      differencesProbability = 1.0D;
    } else {
      differencesProbability = 0.0D;
    }
    
    differencesSignificance = 0;
    if (differencesProbability <= sigLevel) {
      if (xStats.mean > yStats.mean) {
        differencesSignificance = 1;
      } else {
        differencesSignificance = -1;
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

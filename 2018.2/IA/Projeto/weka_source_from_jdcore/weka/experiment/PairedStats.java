package weka.experiment;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;


















































public class PairedStats
  implements RevisionHandler
{
  public Stats xStats;
  public Stats yStats;
  public Stats differencesStats;
  public double differencesProbability;
  public double correlation;
  public double xySum;
  public double count;
  public int differencesSignificance;
  public double sigLevel;
  protected int m_degreesOfFreedom = 0;
  





  public PairedStats(double sig)
  {
    xStats = new Stats();
    yStats = new Stats();
    differencesStats = new Stats();
    sigLevel = sig;
  }
  



  public void setDegreesOfFreedom(int d)
  {
    if (d <= 0) {
      throw new IllegalArgumentException("PairedStats: degrees of freedom must be >= 1");
    }
    m_degreesOfFreedom = d;
  }
  



  public int getDegreesOfFreedom()
  {
    return m_degreesOfFreedom;
  }
  






  public void add(double value1, double value2)
  {
    xStats.add(value1);
    yStats.add(value2);
    differencesStats.add(value1 - value2);
    xySum += value1 * value2;
    count += 1.0D;
  }
  






  public void subtract(double value1, double value2)
  {
    xStats.subtract(value1);
    yStats.subtract(value2);
    differencesStats.subtract(value1 - value2);
    xySum -= value1 * value2;
    count -= 1.0D;
  }
  






  public void add(double[] value1, double[] value2)
  {
    if ((value1 == null) || (value2 == null)) {
      throw new NullPointerException();
    }
    if (value1.length != value2.length) {
      throw new IllegalArgumentException("Arrays must be of the same length");
    }
    for (int i = 0; i < value1.length; i++) {
      add(value1[i], value2[i]);
    }
  }
  






  public void subtract(double[] value1, double[] value2)
  {
    if ((value1 == null) || (value2 == null)) {
      throw new NullPointerException();
    }
    if (value1.length != value2.length) {
      throw new IllegalArgumentException("Arrays must be of the same length");
    }
    for (int i = 0; i < value1.length; i++) {
      subtract(value1[i], value2[i]);
    }
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
    
    if (Utils.gr(differencesStats.stdDev, 0.0D)) {
      double tval = differencesStats.mean * Math.sqrt(count) / differencesStats.stdDev;
      


      if (m_degreesOfFreedom >= 1) {
        differencesProbability = Statistics.FProbability(tval * tval, 1, m_degreesOfFreedom);

      }
      else if (count > 1.0D) {
        differencesProbability = Statistics.FProbability(tval * tval, 1, (int)count - 1);
      }
      else {
        differencesProbability = 1.0D;
      }
      
    }
    else if (differencesStats.sumSq == 0.0D) {
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
  





  public String toString()
  {
    return "Analysis for " + Utils.doubleToString(count, 0) + " points:\n" + "                " + "         Column 1" + "         Column 2" + "       Difference\n" + "Minimums        " + Utils.doubleToString(xStats.min, 17, 4) + Utils.doubleToString(yStats.min, 17, 4) + Utils.doubleToString(differencesStats.min, 17, 4) + '\n' + "Maximums        " + Utils.doubleToString(xStats.max, 17, 4) + Utils.doubleToString(yStats.max, 17, 4) + Utils.doubleToString(differencesStats.max, 17, 4) + '\n' + "Sums            " + Utils.doubleToString(xStats.sum, 17, 4) + Utils.doubleToString(yStats.sum, 17, 4) + Utils.doubleToString(differencesStats.sum, 17, 4) + '\n' + "SumSquares      " + Utils.doubleToString(xStats.sumSq, 17, 4) + Utils.doubleToString(yStats.sumSq, 17, 4) + Utils.doubleToString(differencesStats.sumSq, 17, 4) + '\n' + "Means           " + Utils.doubleToString(xStats.mean, 17, 4) + Utils.doubleToString(yStats.mean, 17, 4) + Utils.doubleToString(differencesStats.mean, 17, 4) + '\n' + "SDs             " + Utils.doubleToString(xStats.stdDev, 17, 4) + Utils.doubleToString(yStats.stdDev, 17, 4) + Utils.doubleToString(differencesStats.stdDev, 17, 4) + '\n' + "Prob(differences) " + Utils.doubleToString(differencesProbability, 4) + " (sigflag " + differencesSignificance + ")\n" + "Correlation       " + Utils.doubleToString(correlation, 4) + "\n";
  }
  






































  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  





  public static void main(String[] args)
  {
    try
    {
      PairedStats ps = new PairedStats(0.05D);
      LineNumberReader r = new LineNumberReader(new InputStreamReader(System.in));
      
      String line;
      while ((line = r.readLine()) != null) {
        line = line.trim();
        if ((!line.equals("")) && (!line.startsWith("@")) && (!line.startsWith("%")))
        {

          StringTokenizer s = new StringTokenizer(line, " ,\t\n\r\f");
          
          int count = 0;
          double v1 = 0.0D;double v2 = 0.0D;
          while (s.hasMoreTokens()) {
            double val = new Double(s.nextToken()).doubleValue();
            if (count == 0) {
              v1 = val;
            } else if (count == 1) {
              v2 = val;
            } else {
              System.err.println("MSG: Too many values in line \"" + line + "\", skipped.");
              
              break;
            }
            count++;
          }
          if (count == 2)
            ps.add(v1, v2);
        }
      }
      ps.calculateDerived();
      System.err.println(ps);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

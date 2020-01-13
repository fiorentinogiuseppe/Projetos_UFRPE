package weka.experiment;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;





























public class Stats
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8610544539090024102L;
  public double count = 0.0D;
  

  public double sum = 0.0D;
  

  public double sumSq = 0.0D;
  

  public double stdDev = NaN.0D;
  

  public double mean = NaN.0D;
  

  public double min = NaN.0D;
  

  public double max = NaN.0D;
  


  public Stats() {}
  

  public void add(double value)
  {
    add(value, 1.0D);
  }
  






  public void add(double value, double n)
  {
    sum += value * n;
    sumSq += value * value * n;
    count += n;
    if (Double.isNaN(min)) {
      min = (this.max = value);
    } else if (value < min) {
      min = value;
    } else if (value > max) {
      max = value;
    }
  }
  





  public void subtract(double value)
  {
    subtract(value, 1.0D);
  }
  





  public void subtract(double value, double n)
  {
    sum -= value * n;
    sumSq -= value * value * n;
    count -= n;
  }
  





  public void calculateDerived()
  {
    mean = NaN.0D;
    stdDev = NaN.0D;
    if (count > 0.0D) {
      mean = (sum / count);
      stdDev = Double.POSITIVE_INFINITY;
      if (count > 1.0D) {
        stdDev = (sumSq - sum * sum / count);
        stdDev /= (count - 1.0D);
        if (stdDev < 0.0D)
        {

          stdDev = 0.0D;
        }
        stdDev = Math.sqrt(stdDev);
      }
    }
  }
  





  public String toString()
  {
    calculateDerived();
    return "Count   " + Utils.doubleToString(count, 8) + '\n' + "Min     " + Utils.doubleToString(min, 8) + '\n' + "Max     " + Utils.doubleToString(max, 8) + '\n' + "Sum     " + Utils.doubleToString(sum, 8) + '\n' + "SumSq   " + Utils.doubleToString(sumSq, 8) + '\n' + "Mean    " + Utils.doubleToString(mean, 8) + '\n' + "StdDev  " + Utils.doubleToString(stdDev, 8) + '\n';
  }
  











  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.12 $");
  }
  





  public static void main(String[] args)
  {
    try
    {
      Stats ps = new Stats();
      LineNumberReader r = new LineNumberReader(new InputStreamReader(System.in));
      
      String line;
      while ((line = r.readLine()) != null) {
        line = line.trim();
        if ((!line.equals("")) && (!line.startsWith("@")) && (!line.startsWith("%")))
        {

          StringTokenizer s = new StringTokenizer(line, " ,\t\n\r\f");
          
          int count = 0;
          double v1 = 0.0D;
          while (s.hasMoreTokens()) {
            double val = new Double(s.nextToken()).doubleValue();
            if (count == 0) {
              v1 = val;
            } else {
              System.err.println("MSG: Too many values in line \"" + line + "\", skipped.");
              
              break;
            }
            count++;
          }
          if (count == 1)
            ps.add(v1);
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

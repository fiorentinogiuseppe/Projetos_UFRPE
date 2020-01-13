package weka.estimators;

import java.io.PrintStream;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.matrix.Matrix;








































public class MahalanobisEstimator
  extends Estimator
  implements IncrementalEstimator
{
  private static final long serialVersionUID = 8950225468990043868L;
  private Matrix m_CovarianceInverse;
  private double m_Determinant;
  private double m_ConstDelta;
  private double m_ValueMean;
  private static double TWO_PI = 6.283185307179586D;
  







  private double normalKernel(double x)
  {
    Matrix thisPoint = new Matrix(1, 2);
    thisPoint.set(0, 0, x);
    thisPoint.set(0, 1, m_ConstDelta);
    return Math.exp(-thisPoint.times(m_CovarianceInverse).times(thisPoint.transpose()).get(0, 0) / 2.0D) / (Math.sqrt(TWO_PI) * m_Determinant);
  }
  










  public MahalanobisEstimator(Matrix covariance, double constDelta, double valueMean)
  {
    m_CovarianceInverse = null;
    if ((covariance.getRowDimension() == 2) && (covariance.getColumnDimension() == 2)) {
      double a = covariance.get(0, 0);
      double b = covariance.get(0, 1);
      double c = covariance.get(1, 0);
      double d = covariance.get(1, 1);
      if (a == 0.0D) {
        a = c;c = 0.0D;
        double temp = b;
        b = d;d = temp;
      }
      if (a == 0.0D) {
        return;
      }
      double denom = d - c * b / a;
      if (denom == 0.0D) {
        return;
      }
      m_Determinant = (covariance.get(0, 0) * covariance.get(1, 1) - covariance.get(1, 0) * covariance.get(0, 1));
      
      m_CovarianceInverse = new Matrix(2, 2);
      m_CovarianceInverse.set(0, 0, 1.0D / a + b * c / a / a / denom);
      m_CovarianceInverse.set(0, 1, -b / a / denom);
      m_CovarianceInverse.set(1, 0, -c / a / denom);
      m_CovarianceInverse.set(1, 1, 1.0D / denom);
      m_ConstDelta = constDelta;
      m_ValueMean = valueMean;
    }
  }
  








  public void addValue(double data, double weight) {}
  







  public double getProbability(double data)
  {
    double delta = data - m_ValueMean;
    if (m_CovarianceInverse == null) {
      return 0.0D;
    }
    return normalKernel(delta);
  }
  

  public String toString()
  {
    if (m_CovarianceInverse == null) {
      return "No covariance inverse\n";
    }
    return "Mahalanovis Distribution. Mean = " + Utils.doubleToString(m_ValueMean, 4, 2) + "  ConditionalOffset = " + Utils.doubleToString(m_ConstDelta, 4, 2) + "\n" + "Covariance Matrix: Determinant = " + m_Determinant + "  Inverse:\n" + m_CovarianceInverse;
  }
  









  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    if (!m_noClass) {
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
      result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    } else {
      result.enable(Capabilities.Capability.NO_CLASS);
    }
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5540 $");
  }
  




  public static void main(String[] argv)
  {
    try
    {
      double delta = 0.5D;
      double xmean = 0.0D;
      double lower = 0.0D;
      double upper = 10.0D;
      Matrix covariance = new Matrix(2, 2);
      covariance.set(0, 0, 2.0D);
      covariance.set(0, 1, -3.0D);
      covariance.set(1, 0, -4.0D);
      covariance.set(1, 1, 5.0D);
      if (argv.length > 0) {
        covariance.set(0, 0, Double.valueOf(argv[0]).doubleValue());
      }
      if (argv.length > 1) {
        covariance.set(0, 1, Double.valueOf(argv[1]).doubleValue());
      }
      if (argv.length > 2) {
        covariance.set(1, 0, Double.valueOf(argv[2]).doubleValue());
      }
      if (argv.length > 3) {
        covariance.set(1, 1, Double.valueOf(argv[3]).doubleValue());
      }
      if (argv.length > 4) {
        delta = Double.valueOf(argv[4]).doubleValue();
      }
      if (argv.length > 5) {
        xmean = Double.valueOf(argv[5]).doubleValue();
      }
      
      MahalanobisEstimator newEst = new MahalanobisEstimator(covariance, delta, xmean);
      
      if (argv.length > 6) {
        lower = Double.valueOf(argv[6]).doubleValue();
        if (argv.length > 7) {
          upper = Double.valueOf(argv[7]).doubleValue();
        }
        double increment = (upper - lower) / 50.0D;
        for (double current = lower; current <= upper; current += increment)
          System.out.println(current + "  " + newEst.getProbability(current));
      } else {
        System.out.println("Covariance Matrix\n" + covariance);
        System.out.println(newEst);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}

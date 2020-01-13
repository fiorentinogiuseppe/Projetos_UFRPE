package weka.classifiers.functions.pace;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Random;
import weka.core.RevisionUtils;
import weka.core.matrix.DoubleVector;
import weka.core.matrix.FlexibleDecimalFormat;
import weka.core.matrix.IntVector;
import weka.core.matrix.Maths;
import weka.core.matrix.Matrix;













































public class PaceMatrix
  extends Matrix
{
  static final long serialVersionUID = 2699925616857843973L;
  
  public PaceMatrix(int m, int n)
  {
    super(m, n);
  }
  




  public PaceMatrix(int m, int n, double s)
  {
    super(m, n, s);
  }
  



  public PaceMatrix(double[][] A)
  {
    super(A);
  }
  




  public PaceMatrix(double[][] A, int m, int n)
  {
    super(A, m, n);
  }
  




  public PaceMatrix(double[] vals, int m)
  {
    super(vals, m);
  }
  


  public PaceMatrix(DoubleVector v)
  {
    this(v.size(), 1);
    setMatrix(0, v.size() - 1, 0, v);
  }
  


  public PaceMatrix(Matrix X)
  {
    super(X.getRowDimension(), X.getColumnDimension());
    A = X.getArray();
  }
  







  public void setRowDimension(int rowDimension)
  {
    m = rowDimension;
  }
  



  public void setColumnDimension(int columnDimension)
  {
    n = columnDimension;
  }
  




  public Object clone()
  {
    PaceMatrix X = new PaceMatrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = A[i][j];
      }
    }
    return X;
  }
  




  public void setPlus(int i, int j, double s)
  {
    A[i][j] += s;
  }
  




  public void setTimes(int i, int j, double s)
  {
    A[i][j] *= s;
  }
  





  public void setMatrix(int i0, int i1, int j0, int j1, double s)
  {
    try
    {
      for (int i = i0; i <= i1; i++) {
        for (int j = j0; j <= j1; j++) {
          A[i][j] = s;
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Index out of bounds");
    }
  }
  





  public void setMatrix(int i0, int i1, int j, DoubleVector v)
  {
    for (int i = i0; i <= i1; i++) {
      A[i][j] = v.get(i - i0);
    }
  }
  



  public void setMatrix(double[] v, boolean columnFirst)
  {
    try
    {
      if (v.length != m * n)
        throw new IllegalArgumentException("sizes not match.");
      int count = 0;
      if (columnFirst) {
        for (int i = 0; i < m; i++) {
          for (int j = 0; j < n; j++) {
            A[i][j] = v[count];
            count++;
          }
        }
      }
      
      for (int j = 0; j < n; j++) {
        for (int i = 0; i < m; i++) {
          A[i][j] = v[count];
          count++;
        }
      }
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }
  


  public double maxAbs()
  {
    double ma = Math.abs(A[0][0]);
    for (int j = 0; j < n; j++) {
      for (int i = 0; i < m; i++) {
        ma = Math.max(ma, Math.abs(A[i][j]));
      }
    }
    return ma;
  }
  





  public double maxAbs(int i0, int i1, int j)
  {
    double m = Math.abs(A[i0][j]);
    for (int i = i0 + 1; i <= i1; i++) {
      m = Math.max(m, Math.abs(A[i][j]));
    }
    return m;
  }
  






  public double minAbs(int i0, int i1, int column)
  {
    double m = Math.abs(A[i0][column]);
    for (int i = i0 + 1; i <= i1; i++) {
      m = Math.min(m, Math.abs(A[i][column]));
    }
    return m;
  }
  


  public boolean isEmpty()
  {
    if ((m == 0) || (n == 0)) return true;
    if (A == null) return true;
    return false;
  }
  



  public DoubleVector getColumn(int j)
  {
    DoubleVector v = new DoubleVector(m);
    double[] a = v.getArray();
    for (int i = 0; i < m; i++)
      a[i] = A[i][j];
    return v;
  }
  






  public DoubleVector getColumn(int i0, int i1, int j)
  {
    DoubleVector v = new DoubleVector(i1 - i0 + 1);
    double[] a = v.getArray();
    int count = 0;
    for (int i = i0; i <= i1; i++) {
      a[count] = A[i][j];
      count++;
    }
    return v;
  }
  









  public double times(int i, int j0, int j1, PaceMatrix B, int l)
  {
    double s = 0.0D;
    for (int j = j0; j <= j1; j++) {
      s += A[i][j] * A[j][l];
    }
    return s;
  }
  


  protected DecimalFormat[] format()
  {
    return format(0, m - 1, 0, n - 1, 7, false);
  }
  



  protected DecimalFormat[] format(int digits)
  {
    return format(0, m - 1, 0, n - 1, digits, false);
  }
  




  protected DecimalFormat[] format(int digits, boolean trailing)
  {
    return format(0, m - 1, 0, n - 1, digits, trailing);
  }
  








  protected DecimalFormat format(int i0, int i1, int j, int digits, boolean trailing)
  {
    FlexibleDecimalFormat df = new FlexibleDecimalFormat(digits, trailing);
    df.grouping(true);
    for (int i = i0; i <= i1; i++)
      df.update(A[i][j]);
    return df;
  }
  









  protected DecimalFormat[] format(int i0, int i1, int j0, int j1, int digits, boolean trailing)
  {
    DecimalFormat[] f = new DecimalFormat[j1 - j0 + 1];
    for (int j = j0; j <= j1; j++) {
      f[j] = format(i0, i1, j, digits, trailing);
    }
    return f;
  }
  




  public String toString()
  {
    return toString(5, false);
  }
  







  public String toString(int digits, boolean trailing)
  {
    if (isEmpty()) { return "null matrix";
    }
    StringBuffer text = new StringBuffer();
    DecimalFormat[] nf = format(digits, trailing);
    int numCols = 0;
    int count = 0;
    int width = 80;
    

    int[] nCols = new int[n];
    int nk = 0;
    for (int j = 0; j < n; j++) {
      int lenNumber = nf[j].format(A[0][j]).length();
      if (count + 1 + lenNumber > width - 1) {
        nCols[(nk++)] = numCols;
        count = 0;
        numCols = 0;
      }
      count += 1 + lenNumber;
      numCols++;
    }
    nCols[nk] = numCols;
    
    nk = 0;
    for (int k = 0; k < n;) {
      for (int i = 0; i < m; i++) {
        for (int j = k; j < k + nCols[nk]; j++)
          text.append(" " + nf[j].format(A[i][j]));
        text.append("\n");
      }
      k += nCols[nk];
      nk++;
      text.append("\n");
    }
    
    return text.toString();
  }
  






  public double sum2(int j, int i0, int i1, boolean col)
  {
    double s2 = 0.0D;
    if (col) {
      for (int i = i0; i <= i1; i++) {
        s2 += A[i][j] * A[i][j];
      }
    } else {
      for (int i = i0; i <= i1; i++)
        s2 += A[j][i] * A[j][i];
    }
    return s2;
  }
  



  public double[] sum2(boolean col)
  {
    int l = col ? n : m;
    int p = col ? m : n;
    double[] s2 = new double[l];
    for (int i = 0; i < l; i++)
      s2[i] = sum2(i, 0, p - 1, col);
    return s2;
  }
  





  public double[] h1(int j, int k)
  {
    double[] dq = new double[2];
    double s2 = sum2(j, k, m - 1, true);
    dq[0] = (A[k][j] >= 0.0D ? -Math.sqrt(s2) : Math.sqrt(s2));
    A[k][j] -= dq[0];
    dq[1] = (A[k][j] * dq[0]);
    return dq;
  }
  







  public void h2(int j, int k, double q, PaceMatrix b, int l)
  {
    double s = 0.0D;
    for (int i = k; i < m; i++)
      s += A[i][j] * A[i][l];
    double alpha = s / q;
    for (int i = k; i < m; i++) {
      A[i][l] += alpha * A[i][j];
    }
  }
  



  public double[] g1(double a, double b)
  {
    double[] cs = new double[2];
    double r = Maths.hypot(a, b);
    if (r == 0.0D) {
      cs[0] = 1.0D;
      cs[1] = 0.0D;
    }
    else {
      cs[0] = (a / r);
      cs[1] = (b / r);
    }
    return cs;
  }
  





  public void g2(double[] cs, int i0, int i1, int j)
  {
    double w = cs[0] * A[i0][j] + cs[1] * A[i1][j];
    A[i1][j] = (-cs[1] * A[i0][j] + cs[0] * A[i1][j]);
    A[i0][j] = w;
  }
  







  public void forward(PaceMatrix b, IntVector pvt, int k0)
  {
    for (int j = k0; j < Math.min(pvt.size(), m); j++) {
      steplsqr(b, pvt, j, mostExplainingColumn(b, pvt, j), true);
    }
  }
  









  public int mostExplainingColumn(PaceMatrix b, IntVector pvt, int ks)
  {
    int[] p = pvt.getArray();
    double ma = columnResponseExplanation(b, pvt, ks, ks);
    int jma = ks;
    for (int i = ks + 1; i < pvt.size(); i++) {
      double val = columnResponseExplanation(b, pvt, i, ks);
      if (val > ma) {
        ma = val;
        jma = i;
      }
    }
    return jma;
  }
  










  public void backward(PaceMatrix b, IntVector pvt, int ks, int k0)
  {
    for (int j = ks; j > k0; j--) {
      steplsqr(b, pvt, j, leastExplainingColumn(b, pvt, j, k0), false);
    }
  }
  











  public int leastExplainingColumn(PaceMatrix b, IntVector pvt, int ks, int k0)
  {
    int[] p = pvt.getArray();
    double mi = columnResponseExplanation(b, pvt, ks - 1, ks);
    int jmi = ks - 1;
    for (int i = k0; i < ks - 1; i++) {
      double val = columnResponseExplanation(b, pvt, i, ks);
      if (val <= mi) {
        mi = val;
        jmi = i;
      }
    }
    return jmi;
  }
  
























  public double columnResponseExplanation(PaceMatrix b, IntVector pvt, int j, int ks)
  {
    double[] xxx = new double[n];
    int[] p = pvt.getArray();
    double val;
    double val;
    if (j == ks - 1) { val = A[j][0]; } else { double val;
      if (j > ks - 1) {
        int jm = Math.min(n - 1, j);
        DoubleVector u = getColumn(ks, jm, p[j]);
        DoubleVector v = b.getColumn(ks, jm, 0);
        val = v.innerProduct(u) / u.norm2();
      }
      else {
        for (int k = j + 1; k < ks; k++)
          xxx[k] = A[j][p[k]];
        val = A[j][0];
        
        for (k = j + 1; k < ks; k++) {
          double[] cs = g1(xxx[k], A[k][p[k]]);
          for (int l = k + 1; l < ks; l++)
            xxx[l] = (-cs[1] * xxx[l] + cs[0] * A[k][p[l]]);
          val = -cs[1] * val + cs[0] * A[k][0];
        }
      } }
    return val * val;
  }
  













  public void lsqr(PaceMatrix b, IntVector pvt, int k0)
  {
    double TINY = 1.0E-15D;
    int[] p = pvt.getArray();
    int ks = 0;
    for (int j = 0; j < k0; j++) {
      if (sum2(p[j], ks, m - 1, true) > 1.0E-15D) {
        steplsqr(b, pvt, ks, j, true);
        ks++;
      }
      else {
        pvt.shiftToEnd(j);
        pvt.setSize(pvt.size() - 1);
        k0--;
        j--;
      }
    }
    
    for (int j = k0; j < Math.min(pvt.size(), m); j++) {
      if (sum2(p[j], ks, m - 1, true) > 1.0E-15D) {
        steplsqr(b, pvt, ks, j, true);
        ks++;
      }
      else {
        pvt.shiftToEnd(j);
        pvt.setSize(pvt.size() - 1);
        j--;
      }
    }
    
    m = (this.m = ks);
    pvt.setSize(ks);
  }
  











  public void lsqrSelection(PaceMatrix b, IntVector pvt, int k0)
  {
    int numObs = m;
    int numXs = pvt.size();
    
    lsqr(b, pvt, k0);
    
    if ((numXs > 200) || (numXs > numObs)) {
      forward(b, pvt, k0);
    }
    backward(b, pvt, pvt.size(), k0);
  }
  






  public void positiveDiagonal(PaceMatrix Y, IntVector pvt)
  {
    int[] p = pvt.getArray();
    for (int i = 0; i < pvt.size(); i++) {
      if (A[i][p[i]] < 0.0D) {
        for (int j = i; j < pvt.size(); j++)
          A[i][p[j]] = (-A[i][p[j]]);
        A[i][0] = (-A[i][0]);
      }
    }
  }
  







  public void steplsqr(PaceMatrix b, IntVector pvt, int ks, int j, boolean adjoin)
  {
    int kp = pvt.size();
    int[] p = pvt.getArray();
    
    if (adjoin) {
      int pj = p[j];
      pvt.swap(ks, j);
      double[] dq = h1(pj, ks);
      
      for (int k = ks + 1; k < kp; k++) {
        int pk = p[k];
        h2(pj, ks, dq[1], this, pk);
      }
      h2(pj, ks, dq[1], b, 0);
      A[ks][pj] = dq[0];
      for (int k = ks + 1; k < m; k++) {
        A[k][pj] = 0.0D;
      }
    } else {
      int pj = p[j];
      for (int i = j; i < ks - 1; i++)
        p[i] = p[(i + 1)];
      p[(ks - 1)] = pj;
      
      for (int i = j; i < ks - 1; i++) {
        double[] cs = g1(A[i][p[i]], A[(i + 1)][p[i]]);
        for (int l = i; l < kp; l++)
          g2(cs, i, i + 1, p[l]);
        for (int l = 0; l < n; l++) {
          b.g2(cs, i, i + 1, l);
        }
      }
    }
  }
  





  public void rsolve(PaceMatrix b, IntVector pvt, int kp)
  {
    if (kp == 0) { m = 0;
    }
    int[] p = pvt.getArray();
    
    double[][] ba = b.getArray();
    for (int k = 0; k < n; k++) {
      ba[(kp - 1)][k] /= A[(kp - 1)][p[(kp - 1)]];
      for (int i = kp - 2; i >= 0; i--) {
        double s = 0.0D;
        for (int j = i + 1; j < kp; j++)
          s += A[i][p[j]] * ba[j][k];
        ba[i][k] -= s;
        ba[i][k] /= A[i][p[i]];
      }
    }
    m = kp;
  }
  



  public PaceMatrix rbind(PaceMatrix b)
  {
    if (n != n)
      throw new IllegalArgumentException("unequal numbers of rows.");
    PaceMatrix c = new PaceMatrix(m + m, n);
    c.setMatrix(0, m - 1, 0, n - 1, this);
    c.setMatrix(m, m + m - 1, 0, n - 1, b);
    return c;
  }
  



  public PaceMatrix cbind(PaceMatrix b)
  {
    if (m != m) {
      throw new IllegalArgumentException("unequal numbers of rows: " + m + " and " + m);
    }
    PaceMatrix c = new PaceMatrix(m, n + n);
    c.setMatrix(0, m - 1, 0, n - 1, this);
    c.setMatrix(0, m - 1, n, n + n - 1, b);
    return c;
  }
  








  public DoubleVector nnls(PaceMatrix b, IntVector pvt)
  {
    int counter = 0;int jm = -1;int n = pvt.size();
    
    int[] p = pvt.getArray();
    DoubleVector x = new DoubleVector(n);
    double[] xA = x.getArray();
    PaceMatrix z = new PaceMatrix(n, 1);
    


    int kp = 0;
    
    counter++; if (counter > 3 * n)
      throw new RuntimeException("Does not converge");
    int t = -1;
    double max = 0.0D;
    PaceMatrix bt = new PaceMatrix(b.transpose());
    for (int j = kp; j <= n - 1; j++) {
      double wj = bt.times(0, kp, m - 1, this, p[j]);
      if (wj > max) {
        max = wj;
        t = j;
      }
    }
    

    if (t != -1)
    {

      pvt.swap(kp, t);
      kp++;
      xA[(kp - 1)] = 0.0D;
      steplsqr(b, pvt, kp - 1, kp - 1, true);
      
      double ma = 0.0D;
      while (ma < 1.5D) {
        for (j = 0; j <= kp - 1; j++) A[j][0] = A[j][0];
        rsolve(z, pvt, kp);
        ma = 2.0D;jm = -1;
        for (j = 0; j <= kp - 1; j++) {
          if (A[j][0] <= 0.0D) {
            double alpha = xA[j] / (xA[j] - A[j][0]);
            if (alpha < ma) {
              ma = alpha;jm = j;
            }
          }
        }
        if (ma > 1.5D) {
          for (j = 0; j <= kp - 1; j++) xA[j] = A[j][0];
        } else {
          for (j = kp - 1; j >= 0; j--)
          {

            if (j == jm) {
              xA[j] = 0.0D;
              steplsqr(b, pvt, kp, j, false);
              kp--;
            } else {
              xA[j] += ma * (A[j][0] - xA[j]);
            } }
        }
      }
    }
    x.setSize(kp);
    pvt.setSize(kp);
    return x;
  }
  










  public DoubleVector nnlse(PaceMatrix b, PaceMatrix c, PaceMatrix d, IntVector pvt)
  {
    double eps = 1.0E-10D * Math.max(c.maxAbs(), d.maxAbs()) / Math.max(maxAbs(), b.maxAbs());
    

    PaceMatrix e = c.rbind(new PaceMatrix(times(eps)));
    PaceMatrix f = d.rbind(new PaceMatrix(b.times(eps)));
    
    return e.nnls(f, pvt);
  }
  







  public DoubleVector nnlse1(PaceMatrix b, IntVector pvt)
  {
    PaceMatrix c = new PaceMatrix(1, n, 1.0D);
    PaceMatrix d = new PaceMatrix(1, n, 1.0D);
    
    return nnlse(b, c, d, pvt);
  }
  



  public static Matrix randomNormal(int m, int n)
  {
    Random random = new Random();
    
    Matrix A = new Matrix(m, n);
    double[][] X = A.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        X[i][j] = random.nextGaussian();
      }
    }
    return A;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
  




  public static void main(String[] args)
  {
    System.out.println("===========================================================");
    
    System.out.println("To test the pace estimators of linear model\ncoefficients.\n");
    

    double sd = 2.0D;
    int n = 200;
    double beta0 = 100.0D;
    int k1 = 20;
    double beta1 = 0.0D;
    int k2 = 20;
    double beta2 = 5.0D;
    int k = 1 + k1 + k2;
    
    DoubleVector beta = new DoubleVector(1 + k1 + k2);
    beta.set(0, beta0);
    beta.set(1, k1, beta1);
    beta.set(k1 + 1, k1 + k2, beta2);
    
    System.out.println("The data set contains " + n + " observations plus " + (k1 + k2) + " variables.\n\nThe coefficients of the true model" + " are:\n\n" + beta);
    



    System.out.println("\nThe standard deviation of the error term is " + sd);
    

    System.out.println("===========================================================");
    

    PaceMatrix X = new PaceMatrix(n, k1 + k2 + 1);
    X.setMatrix(0, n - 1, 0, 0, 1.0D);
    X.setMatrix(0, n - 1, 1, k1 + k2, random(n, k1 + k2));
    
    PaceMatrix Y = new PaceMatrix(X.times(new PaceMatrix(beta)).plusEquals(randomNormal(n, 1).times(sd)));
    


    IntVector pvt = IntVector.seq(0, k1 + k2);
    



    X.lsqrSelection(Y, pvt, 1);
    X.positiveDiagonal(Y, pvt);
    
    PaceMatrix sol = (PaceMatrix)Y.clone();
    X.rsolve(sol, pvt, pvt.size());
    DoubleVector betaHat = sol.getColumn(0).unpivoting(pvt, k);
    System.out.println("\nThe OLS estimate (through lsqr()) is: \n\n" + betaHat);
    

    System.out.println("\nQuadratic loss of the OLS estimate (||X b - X bHat||^2) = " + new PaceMatrix(X.times(new PaceMatrix(beta.minus(betaHat)))).getColumn(0).sum2());
    



    System.out.println("===========================================================");
    
    System.out.println("             *** Pace estimation *** \n");
    DoubleVector r = Y.getColumn(pvt.size(), n - 1, 0);
    double sde = Math.sqrt(r.sum2() / r.size());
    
    System.out.println("Estimated standard deviation = " + sde);
    
    DoubleVector aHat = Y.getColumn(0, pvt.size() - 1, 0).times(1.0D / sde);
    System.out.println("\naHat = \n" + aHat);
    
    System.out.println("\n========= Based on chi-square mixture ============");
    
    ChisqMixture d2 = new ChisqMixture();
    int method = 1;
    DoubleVector AHat = aHat.square();
    d2.fit(AHat, method);
    System.out.println("\nEstimated mixing distribution is:\n" + d2);
    
    DoubleVector ATilde = d2.pace2(AHat);
    DoubleVector aTilde = ATilde.sqrt().times(aHat.sign());
    PaceMatrix YTilde = new PaceMatrix(new PaceMatrix(aTilde).times(sde));
    
    X.rsolve(YTilde, pvt, pvt.size());
    DoubleVector betaTilde = YTilde.getColumn(0).unpivoting(pvt, k);
    
    System.out.println("\nThe pace2 estimate of coefficients = \n" + betaTilde);
    
    System.out.println("Quadratic loss = " + new PaceMatrix(X.times(new PaceMatrix(beta.minus(betaTilde)))).getColumn(0).sum2());
    



    ATilde = d2.pace4(AHat);
    aTilde = ATilde.sqrt().times(aHat.sign());
    YTilde = new PaceMatrix(new PaceMatrix(aTilde).times(sde));
    X.rsolve(YTilde, pvt, pvt.size());
    betaTilde = YTilde.getColumn(0).unpivoting(pvt, k);
    System.out.println("\nThe pace4 estimate of coefficients = \n" + betaTilde);
    
    System.out.println("Quadratic loss = " + new PaceMatrix(X.times(new PaceMatrix(beta.minus(betaTilde)))).getColumn(0).sum2());
    



    ATilde = d2.pace6(AHat);
    aTilde = ATilde.sqrt().times(aHat.sign());
    YTilde = new PaceMatrix(new PaceMatrix(aTilde).times(sde));
    X.rsolve(YTilde, pvt, pvt.size());
    betaTilde = YTilde.getColumn(0).unpivoting(pvt, k);
    System.out.println("\nThe pace6 estimate of coefficients = \n" + betaTilde);
    
    System.out.println("Quadratic loss = " + new PaceMatrix(X.times(new PaceMatrix(beta.minus(betaTilde)))).getColumn(0).sum2());
    



    System.out.println("\n========= Based on normal mixture ============");
    
    NormalMixture d = new NormalMixture();
    d.fit(aHat, method);
    System.out.println("\nEstimated mixing distribution is:\n" + d);
    
    aTilde = d.nestedEstimate(aHat);
    YTilde = new PaceMatrix(new PaceMatrix(aTilde).times(sde));
    X.rsolve(YTilde, pvt, pvt.size());
    betaTilde = YTilde.getColumn(0).unpivoting(pvt, k);
    System.out.println("The nested estimate of coefficients = \n" + betaTilde);
    
    System.out.println("Quadratic loss = " + new PaceMatrix(X.times(new PaceMatrix(beta.minus(betaTilde)))).getColumn(0).sum2());
    




    aTilde = d.subsetEstimate(aHat);
    YTilde = new PaceMatrix(new PaceMatrix(aTilde).times(sde));
    X.rsolve(YTilde, pvt, pvt.size());
    betaTilde = YTilde.getColumn(0).unpivoting(pvt, k);
    
    System.out.println("\nThe subset estimate of coefficients = \n" + betaTilde);
    
    System.out.println("Quadratic loss = " + new PaceMatrix(X.times(new PaceMatrix(beta.minus(betaTilde)))).getColumn(0).sum2());
    



    aTilde = d.empiricalBayesEstimate(aHat);
    YTilde = new PaceMatrix(new PaceMatrix(aTilde).times(sde));
    X.rsolve(YTilde, pvt, pvt.size());
    betaTilde = YTilde.getColumn(0).unpivoting(pvt, k);
    System.out.println("\nThe empirical Bayes estimate of coefficients = \n" + betaTilde);
    

    System.out.println("Quadratic loss = " + new PaceMatrix(X.times(new PaceMatrix(beta.minus(betaTilde)))).getColumn(0).sum2());
  }
}

package weka.core.matrix;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;



























































public class SingularValueDecomposition
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8738089610999867951L;
  private double[][] U;
  private double[][] V;
  private double[] s;
  private int m;
  private int n;
  
  public SingularValueDecomposition(Matrix Arg)
  {
    double[][] A = (double[][])null;
    m = Arg.getRowDimension();
    n = Arg.getColumnDimension();
    






    boolean usingTranspose = false;
    if (m < n)
    {


      A = Arg.transpose().getArrayCopy();
      usingTranspose = true;
      int temp = m;
      m = n;
      n = temp;
    } else {
      A = Arg.getArrayCopy();
    }
    
    int nu = Math.min(m, n);
    s = new double[Math.min(m + 1, n)];
    U = new double[m][m];
    V = new double[n][n];
    double[] e = new double[n];
    double[] work = new double[m];
    boolean wantu = true;
    boolean wantv = true;
    



    int nct = Math.min(m - 1, n);
    int nrt = Math.max(0, Math.min(n - 2, m));
    for (int k = 0; k < Math.max(nct, nrt); k++) {
      if (k < nct)
      {



        s[k] = 0.0D;
        for (int i = k; i < m; i++) {
          s[k] = Maths.hypot(s[k], A[i][k]);
        }
        if (s[k] != 0.0D) {
          if (A[k][k] < 0.0D) {
            s[k] = (-s[k]);
          }
          for (int i = k; i < m; i++) {
            A[i][k] /= s[k];
          }
          A[k][k] += 1.0D;
        }
        s[k] = (-s[k]);
      }
      for (int j = k + 1; j < n; j++) {
        if (((k < nct ? 1 : 0) & (s[k] != 0.0D ? 1 : 0)) != 0)
        {


          double t = 0.0D;
          for (int i = k; i < m; i++) {
            t += A[i][k] * A[i][j];
          }
          t = -t / A[k][k];
          for (int i = k; i < m; i++) {
            A[i][j] += t * A[i][k];
          }
        }
        



        e[j] = A[k][j];
      }
      if ((wantu & k < nct))
      {



        for (int i = k; i < m; i++) {
          U[i][k] = A[i][k];
        }
      }
      if (k < nrt)
      {



        e[k] = 0.0D;
        for (int i = k + 1; i < n; i++) {
          e[k] = Maths.hypot(e[k], e[i]);
        }
        if (e[k] != 0.0D) {
          if (e[(k + 1)] < 0.0D) {
            e[k] = (-e[k]);
          }
          for (int i = k + 1; i < n; i++) {
            e[i] /= e[k];
          }
          e[(k + 1)] += 1.0D;
        }
        e[k] = (-e[k]);
        if (((k + 1 < m ? 1 : 0) & (e[k] != 0.0D ? 1 : 0)) != 0)
        {


          for (int i = k + 1; i < m; i++) {
            work[i] = 0.0D;
          }
          for (int j = k + 1; j < n; j++) {
            for (int i = k + 1; i < m; i++) {
              work[i] += e[j] * A[i][j];
            }
          }
          for (int j = k + 1; j < n; j++) {
            double t = -e[j] / e[(k + 1)];
            for (int i = k + 1; i < m; i++) {
              A[i][j] += t * work[i];
            }
          }
        }
        if (wantv)
        {



          for (int i = k + 1; i < n; i++) {
            V[i][k] = e[i];
          }
        }
      }
    }
    


    int p = Math.min(n, m + 1);
    if (nct < n) {
      s[nct] = A[nct][nct];
    }
    if (m < p) {
      s[(p - 1)] = 0.0D;
    }
    if (nrt + 1 < p) {
      e[nrt] = A[nrt][(p - 1)];
    }
    e[(p - 1)] = 0.0D;
    


    if (wantu) {
      for (int j = nct; j < nu; j++) {
        for (int i = 0; i < m; i++) {
          U[i][j] = 0.0D;
        }
        U[j][j] = 1.0D;
      }
      for (int k = nct - 1; k >= 0; k--) {
        if (s[k] != 0.0D) {
          for (int j = k + 1; j < nu; j++) {
            double t = 0.0D;
            for (int i = k; i < m; i++) {
              t += U[i][k] * U[i][j];
            }
            t = -t / U[k][k];
            for (int i = k; i < m; i++) {
              U[i][j] += t * U[i][k];
            }
          }
          for (int i = k; i < m; i++) {
            U[i][k] = (-U[i][k]);
          }
          U[k][k] = (1.0D + U[k][k]);
          for (int i = 0; i < k - 1; i++) {
            U[i][k] = 0.0D;
          }
        } else {
          for (int i = 0; i < m; i++) {
            U[i][k] = 0.0D;
          }
          U[k][k] = 1.0D;
        }
      }
    }
    


    if (wantv) {
      for (int k = n - 1; k >= 0; k--) {
        if (((k < nrt ? 1 : 0) & (e[k] != 0.0D ? 1 : 0)) != 0) {
          for (int j = k + 1; j < nu; j++) {
            double t = 0.0D;
            for (int i = k + 1; i < n; i++) {
              t += V[i][k] * V[i][j];
            }
            t = -t / V[(k + 1)][k];
            for (int i = k + 1; i < n; i++) {
              V[i][j] += t * V[i][k];
            }
          }
        }
        for (int i = 0; i < n; i++) {
          V[i][k] = 0.0D;
        }
        V[k][k] = 1.0D;
      }
    }
    


    int pp = p - 1;
    int iter = 0;
    double eps = Math.pow(2.0D, -52.0D);
    double tiny = Math.pow(2.0D, -966.0D);
    while (p > 0)
    {













      for (int k = p - 2; k >= -1; k--) {
        if (k == -1) {
          break;
        }
        if (Math.abs(e[k]) <= tiny + eps * (Math.abs(s[k]) + Math.abs(s[(k + 1)])))
        {
          e[k] = 0.0D;
          break; } }
      int kase;
      int kase;
      if (k == p - 2) {
        kase = 4;
      }
      else {
        for (int ks = p - 1; ks >= k; ks--) {
          if (ks == k) {
            break;
          }
          double t = (ks != p ? Math.abs(e[ks]) : 0.0D) + (ks != k + 1 ? Math.abs(e[(ks - 1)]) : 0.0D);
          
          if (Math.abs(s[ks]) <= tiny + eps * t) {
            s[ks] = 0.0D;
            break;
          } }
        int kase;
        if (ks == k) {
          kase = 3; } else { int kase;
          if (ks == p - 1) {
            kase = 1;
          } else {
            kase = 2;
            k = ks;
          }
        } }
      k++;
      


      switch (kase)
      {


      case 1: 
        double f = e[(p - 2)];
        e[(p - 2)] = 0.0D;
        for (int j = p - 2; j >= k; j--) {
          double t = Maths.hypot(s[j], f);
          double cs = s[j] / t;
          double sn = f / t;
          s[j] = t;
          if (j != k) {
            f = -sn * e[(j - 1)];
            e[(j - 1)] = (cs * e[(j - 1)]);
          }
          if (wantv) {
            for (int i = 0; i < n; i++) {
              t = cs * V[i][j] + sn * V[i][(p - 1)];
              V[i][(p - 1)] = (-sn * V[i][j] + cs * V[i][(p - 1)]);
              V[i][j] = t;
            }
          }
        }
        
        break;
      


      case 2: 
        double f = e[(k - 1)];
        e[(k - 1)] = 0.0D;
        for (int j = k; j < p; j++) {
          double t = Maths.hypot(s[j], f);
          double cs = s[j] / t;
          double sn = f / t;
          s[j] = t;
          f = -sn * e[j];
          e[j] = (cs * e[j]);
          if (wantu) {
            for (int i = 0; i < m; i++) {
              t = cs * U[i][j] + sn * U[i][(k - 1)];
              U[i][(k - 1)] = (-sn * U[i][j] + cs * U[i][(k - 1)]);
              U[i][j] = t;
            }
          }
        }
        
        break;
      





      case 3: 
        double scale = Math.max(Math.max(Math.max(Math.max(Math.abs(s[(p - 1)]), Math.abs(s[(p - 2)])), Math.abs(e[(p - 2)])), Math.abs(s[k])), Math.abs(e[k]));
        

        double sp = s[(p - 1)] / scale;
        double spm1 = s[(p - 2)] / scale;
        double epm1 = e[(p - 2)] / scale;
        double sk = s[k] / scale;
        double ek = e[k] / scale;
        double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0D;
        double c = sp * epm1 * (sp * epm1);
        double shift = 0.0D;
        if (((b != 0.0D ? 1 : 0) | (c != 0.0D ? 1 : 0)) != 0) {
          shift = Math.sqrt(b * b + c);
          if (b < 0.0D) {
            shift = -shift;
          }
          shift = c / (b + shift);
        }
        double f = (sk + sp) * (sk - sp) + shift;
        double g = sk * ek;
        


        for (int j = k; j < p - 1; j++) {
          double t = Maths.hypot(f, g);
          double cs = f / t;
          double sn = g / t;
          if (j != k) {
            e[(j - 1)] = t;
          }
          f = cs * s[j] + sn * e[j];
          e[j] = (cs * e[j] - sn * s[j]);
          g = sn * s[(j + 1)];
          s[(j + 1)] = (cs * s[(j + 1)]);
          if (wantv) {
            for (int i = 0; i < n; i++) {
              t = cs * V[i][j] + sn * V[i][(j + 1)];
              V[i][(j + 1)] = (-sn * V[i][j] + cs * V[i][(j + 1)]);
              V[i][j] = t;
            }
          }
          t = Maths.hypot(f, g);
          cs = f / t;
          sn = g / t;
          s[j] = t;
          f = cs * e[j] + sn * s[(j + 1)];
          s[(j + 1)] = (-sn * e[j] + cs * s[(j + 1)]);
          g = sn * e[(j + 1)];
          e[(j + 1)] = (cs * e[(j + 1)]);
          if ((wantu) && (j < m - 1)) {
            for (int i = 0; i < m; i++) {
              t = cs * U[i][j] + sn * U[i][(j + 1)];
              U[i][(j + 1)] = (-sn * U[i][j] + cs * U[i][(j + 1)]);
              U[i][j] = t;
            }
          }
        }
        e[(p - 2)] = f;
        iter += 1;
        
        break;
      





      case 4: 
        if (s[k] <= 0.0D) {
          s[k] = (s[k] < 0.0D ? -s[k] : 0.0D);
          if (wantv) {
            for (int i = 0; i <= pp; i++) {
              V[i][k] = (-V[i][k]);
            }
          }
        }
        


        while ((k < pp) && 
          (s[k] < s[(k + 1)]))
        {

          double t = s[k];
          s[k] = s[(k + 1)];
          s[(k + 1)] = t;
          if ((wantv) && (k < n - 1)) {
            for (int i = 0; i < n; i++) {
              t = V[i][(k + 1)];V[i][(k + 1)] = V[i][k];V[i][k] = t;
            }
          }
          if ((wantu) && (k < m - 1)) {
            for (int i = 0; i < m; i++) {
              t = U[i][(k + 1)];U[i][(k + 1)] = U[i][k];U[i][k] = t;
            }
          }
          k++;
        }
        iter = 0;
        p--;
      }
      
    }
    

    if (usingTranspose) {
      int temp = m;
      m = n;
      n = temp;
      double[][] tempA = U;
      U = V;
      V = tempA;
    }
  }
  



  public Matrix getU()
  {
    return new Matrix(U, m, m);
  }
  



  public Matrix getV()
  {
    return new Matrix(V, n, n);
  }
  



  public double[] getSingularValues()
  {
    return s;
  }
  



  public Matrix getS()
  {
    Matrix X = new Matrix(m, n);
    double[][] S = X.getArray();
    for (int i = 0; i < Math.min(m, n); i++) {
      S[i][i] = s[i];
    }
    return X;
  }
  



  public double norm2()
  {
    return s[0];
  }
  



  public double cond()
  {
    return s[0] / s[(Math.min(m, n) - 1)];
  }
  



  public int rank()
  {
    double eps = Math.pow(2.0D, -52.0D);
    double tol = Math.max(m, n) * s[0] * eps;
    int r = 0;
    for (int i = 0; i < s.length; i++) {
      if (s[i] > tol) {
        r++;
      }
    }
    return r;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11815 $");
  }
}

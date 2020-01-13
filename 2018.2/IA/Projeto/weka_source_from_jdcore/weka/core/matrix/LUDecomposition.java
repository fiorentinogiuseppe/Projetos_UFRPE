package weka.core.matrix;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

























































public class LUDecomposition
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -2731022568037808629L;
  private double[][] LU;
  private int m;
  private int n;
  private int pivsign;
  private int[] piv;
  
  public LUDecomposition(Matrix A)
  {
    LU = A.getArrayCopy();
    m = A.getRowDimension();
    n = A.getColumnDimension();
    piv = new int[m];
    for (int i = 0; i < m; i++) {
      piv[i] = i;
    }
    pivsign = 1;
    
    double[] LUcolj = new double[m];
    


    for (int j = 0; j < n; j++)
    {


      for (int i = 0; i < m; i++) {
        LUcolj[i] = LU[i][j];
      }
      


      for (int i = 0; i < m; i++) {
        double[] LUrowi = LU[i];
        


        int kmax = Math.min(i, j);
        double s = 0.0D;
        for (int k = 0; k < kmax; k++) {
          s += LUrowi[k] * LUcolj[k];
        }
        
        int tmp185_183 = i; double[] tmp185_182 = LUcolj; double tmp190_189 = (tmp185_182[tmp185_183] - s);tmp185_182[tmp185_183] = tmp190_189;LUrowi[j] = tmp190_189;
      }
      


      int p = j;
      for (int i = j + 1; i < m; i++) {
        if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
          p = i;
        }
      }
      if (p != j) {
        for (int k = 0; k < n; k++) {
          double t = LU[p][k];LU[p][k] = LU[j][k];LU[j][k] = t;
        }
        int k = piv[p];piv[p] = piv[j];piv[j] = k;
        pivsign = (-pivsign);
      }
      


      if (((j < m ? 1 : 0) & (LU[j][j] != 0.0D ? 1 : 0)) != 0) {
        for (int i = j + 1; i < m; i++) {
          LU[i][j] /= LU[j][j];
        }
      }
    }
  }
  



  public boolean isNonsingular()
  {
    for (int j = 0; j < n; j++) {
      if (LU[j][j] == 0.0D)
        return false;
    }
    return true;
  }
  



  public Matrix getL()
  {
    Matrix X = new Matrix(m, n);
    double[][] L = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        if (i > j) {
          L[i][j] = LU[i][j];
        } else if (i == j) {
          L[i][j] = 1.0D;
        } else {
          L[i][j] = 0.0D;
        }
      }
    }
    return X;
  }
  



  public Matrix getU()
  {
    Matrix X = new Matrix(n, n);
    double[][] U = X.getArray();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i <= j) {
          U[i][j] = LU[i][j];
        } else {
          U[i][j] = 0.0D;
        }
      }
    }
    return X;
  }
  



  public int[] getPivot()
  {
    int[] p = new int[m];
    for (int i = 0; i < m; i++) {
      p[i] = piv[i];
    }
    return p;
  }
  



  public double[] getDoublePivot()
  {
    double[] vals = new double[m];
    for (int i = 0; i < m; i++) {
      vals[i] = piv[i];
    }
    return vals;
  }
  




  public double det()
  {
    if (m != n) {
      throw new IllegalArgumentException("Matrix must be square.");
    }
    double d = pivsign;
    for (int j = 0; j < n; j++) {
      d *= LU[j][j];
    }
    return d;
  }
  






  public Matrix solve(Matrix B)
  {
    if (B.getRowDimension() != m) {
      throw new IllegalArgumentException("Matrix row dimensions must agree.");
    }
    if (!isNonsingular()) {
      throw new RuntimeException("Matrix is singular.");
    }
    

    int nx = B.getColumnDimension();
    Matrix Xmat = B.getMatrix(piv, 0, nx - 1);
    double[][] X = Xmat.getArray();
    

    for (int k = 0; k < n; k++) {
      for (int i = k + 1; i < n; i++) {
        for (int j = 0; j < nx; j++) {
          X[i][j] -= X[k][j] * LU[i][k];
        }
      }
    }
    
    for (int k = n - 1; k >= 0; k--) {
      for (int j = 0; j < nx; j++) {
        X[k][j] /= LU[k][k];
      }
      for (int i = 0; i < k; i++) {
        for (int j = 0; j < nx; j++) {
          X[i][j] -= X[k][j] * LU[i][k];
        }
      }
    }
    return Xmat;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

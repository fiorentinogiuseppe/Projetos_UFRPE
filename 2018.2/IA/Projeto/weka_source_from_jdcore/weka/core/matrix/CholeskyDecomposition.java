package weka.core.matrix;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;





















































public class CholeskyDecomposition
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -8739775942782694701L;
  private double[][] L;
  private int n;
  private boolean isspd;
  
  public CholeskyDecomposition(Matrix Arg)
  {
    double[][] A = Arg.getArray();
    n = Arg.getRowDimension();
    L = new double[n][n];
    isspd = (Arg.getColumnDimension() == n);
    
    for (int j = 0; j < n; j++) {
      double[] Lrowj = L[j];
      double d = 0.0D;
      for (int k = 0; k < j; k++) {
        double[] Lrowk = L[k];
        double s = 0.0D;
        for (int i = 0; i < k; i++) {
          s += Lrowk[i] * Lrowj[i];
        }
        double tmp151_150 = ((A[j][k] - s) / L[k][k]);s = tmp151_150;Lrowj[k] = tmp151_150;
        d += s * s;
        isspd &= A[k][j] == A[j][k];
      }
      d = A[j][j] - d;
      isspd &= d > 0.0D;
      L[j][j] = Math.sqrt(Math.max(d, 0.0D));
      for (int k = j + 1; k < n; k++) {
        L[j][k] = 0.0D;
      }
    }
  }
  



  public boolean isSPD()
  {
    return isspd;
  }
  



  public Matrix getL()
  {
    return new Matrix(L, n, n);
  }
  






  public Matrix solve(Matrix B)
  {
    if (B.getRowDimension() != n) {
      throw new IllegalArgumentException("Matrix row dimensions must agree.");
    }
    if (!isspd) {
      throw new RuntimeException("Matrix is not symmetric positive definite.");
    }
    

    double[][] X = B.getArrayCopy();
    int nx = B.getColumnDimension();
    

    for (int k = 0; k < n; k++) {
      for (int j = 0; j < nx; j++) {
        for (int i = 0; i < k; i++) {
          X[k][j] -= X[i][j] * L[k][i];
        }
        X[k][j] /= L[k][k];
      }
    }
    

    for (int k = n - 1; k >= 0; k--) {
      for (int j = 0; j < nx; j++) {
        for (int i = k + 1; i < n; i++) {
          X[k][j] -= X[i][j] * L[i][k];
        }
        X[k][j] /= L[k][k];
      }
    }
    
    return new Matrix(X, n, nx);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}

package weka.core.matrix;

import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
























public class LinearRegression
  implements RevisionHandler
{
  protected double[] m_Coefficients = null;
  







  public LinearRegression(Matrix a, Matrix y, double ridge)
  {
    calculate(a, y, ridge);
  }
  










  public LinearRegression(Matrix a, Matrix y, double[] w, double ridge)
  {
    if (w.length != a.getRowDimension())
      throw new IllegalArgumentException("Incorrect number of weights provided");
    Matrix weightedThis = new Matrix(a.getRowDimension(), a.getColumnDimension());
    
    Matrix weightedDep = new Matrix(a.getRowDimension(), 1);
    for (int i = 0; i < w.length; i++) {
      double sqrt_weight = Math.sqrt(w[i]);
      for (int j = 0; j < a.getColumnDimension(); j++)
        weightedThis.set(i, j, a.get(i, j) * sqrt_weight);
      weightedDep.set(i, 0, y.get(i, 0) * sqrt_weight);
    }
    
    calculate(weightedThis, weightedDep, ridge);
  }
  








  protected void calculate(Matrix a, Matrix y, double ridge)
  {
    if (y.getColumnDimension() > 1) {
      throw new IllegalArgumentException("Only one dependent variable allowed");
    }
    int nc = a.getColumnDimension();
    m_Coefficients = new double[nc];
    

    Matrix ss = aTa(a);
    Matrix bb = aTy(a, y);
    
    boolean success = true;
    
    do
    {
      Matrix ssWithRidge = ss.copy();
      for (int i = 0; i < nc; i++) {
        ssWithRidge.set(i, i, ssWithRidge.get(i, i) + ridge);
      }
      try
      {
        Matrix solution = ssWithRidge.solve(bb);
        for (int i = 0; i < nc; i++)
          m_Coefficients[i] = solution.get(i, 0);
        success = true;
      } catch (Exception ex) {
        ridge *= 10.0D;
        success = false;
      }
    } while (!success);
  }
  


  private static Matrix aTa(Matrix a)
  {
    int cols = a.getColumnDimension();
    double[][] A = a.getArray();
    Matrix x = new Matrix(cols, cols);
    double[][] X = x.getArray();
    double[] Acol = new double[a.getRowDimension()];
    for (int col1 = 0; col1 < cols; col1++)
    {
      for (int row = 0; row < Acol.length; row++) {
        Acol[row] = A[row][col1];
      }
      
      double[] Xrow = X[col1];
      for (int row = 0; row < Acol.length; row++) {
        double[] Arow = A[row];
        for (int col2 = col1; col2 < Xrow.length; col2++) {
          Xrow[col2] += Acol[row] * Arow[col2];
        }
      }
      
      for (int col2 = col1 + 1; col2 < Xrow.length; col2++) {
        X[col2][col1] = Xrow[col2];
      }
    }
    return x;
  }
  


  private static Matrix aTy(Matrix a, Matrix y)
  {
    double[][] A = a.getArray();
    double[][] Y = y.getArray();
    Matrix x = new Matrix(a.getColumnDimension(), 1);
    double[][] X = x.getArray();
    for (int row = 0; row < A.length; row++)
    {
      double[] Arow = A[row];
      double[] Yrow = Y[row];
      for (int col = 0; col < Arow.length; col++) {
        X[col][0] += Arow[col] * Yrow[0];
      }
    }
    return x;
  }
  




  public final double[] getCoefficients()
  {
    return m_Coefficients;
  }
  


  public String toString()
  {
    return Utils.arrayToString(getCoefficients());
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9768 $");
  }
}

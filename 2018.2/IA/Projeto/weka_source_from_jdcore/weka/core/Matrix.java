package weka.core;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import weka.core.matrix.EigenvalueDecomposition;
import weka.core.matrix.LUDecomposition;
import weka.core.matrix.LinearRegression;
import weka.core.matrix.Maths;
































/**
 * @deprecated
 */
public class Matrix
  implements Cloneable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -3604757095849145838L;
  protected weka.core.matrix.Matrix m_Matrix = null;
  





  public Matrix(int nr, int nc)
  {
    m_Matrix = new weka.core.matrix.Matrix(nr, nc);
  }
  



  public Matrix(double[][] array)
    throws Exception
  {
    m_Matrix = new weka.core.matrix.Matrix(array);
  }
  






  public Matrix(Reader r)
    throws Exception
  {
    m_Matrix = new weka.core.matrix.Matrix(r);
  }
  




  public Object clone()
  {
    try
    {
      return new Matrix(m_Matrix.getArrayCopy());
    }
    catch (Exception e) {
      e.printStackTrace(); }
    return null;
  }
  





  public void write(Writer w)
    throws Exception
  {
    m_Matrix.write(w);
  }
  



  protected weka.core.matrix.Matrix getMatrix()
  {
    return m_Matrix;
  }
  






  public final double getElement(int rowIndex, int columnIndex)
  {
    return m_Matrix.get(rowIndex, columnIndex);
  }
  






  public final void addElement(int rowIndex, int columnIndex, double value)
  {
    m_Matrix.set(rowIndex, columnIndex, m_Matrix.get(rowIndex, columnIndex) + value);
  }
  





  public final int numRows()
  {
    return m_Matrix.getRowDimension();
  }
  




  public final int numColumns()
  {
    return m_Matrix.getColumnDimension();
  }
  






  public final void setElement(int rowIndex, int columnIndex, double value)
  {
    m_Matrix.set(rowIndex, columnIndex, value);
  }
  





  public final void setRow(int index, double[] newRow)
  {
    for (int i = 0; i < newRow.length; i++) {
      m_Matrix.set(index, i, newRow[i]);
    }
  }
  




  public double[] getRow(int index)
  {
    double[] newRow = new double[numColumns()];
    for (int i = 0; i < newRow.length; i++) {
      newRow[i] = getElement(index, i);
    }
    return newRow;
  }
  





  public double[] getColumn(int index)
  {
    double[] newColumn = new double[numRows()];
    for (int i = 0; i < newColumn.length; i++) {
      newColumn[i] = getElement(i, index);
    }
    return newColumn;
  }
  





  public final void setColumn(int index, double[] newColumn)
  {
    for (int i = 0; i < numRows(); i++) {
      m_Matrix.set(i, index, newColumn[i]);
    }
  }
  



  public String toString()
  {
    return m_Matrix.toString();
  }
  



  public final Matrix add(Matrix other)
  {
    try
    {
      return new Matrix(m_Matrix.plus(other.getMatrix()).getArrayCopy());
    }
    catch (Exception e) {
      e.printStackTrace(); }
    return null;
  }
  




  public final Matrix transpose()
  {
    try
    {
      return new Matrix(m_Matrix.transpose().getArrayCopy());
    }
    catch (Exception e) {
      e.printStackTrace(); }
    return null;
  }
  





  public boolean isSymmetric()
  {
    return m_Matrix.isSymmetric();
  }
  




  public final Matrix multiply(Matrix b)
  {
    try
    {
      return new Matrix(getMatrix().times(b.getMatrix()).getArrayCopy());
    }
    catch (Exception e) {
      e.printStackTrace(); }
    return null;
  }
  








  public final double[] regression(Matrix y, double ridge)
  {
    return getMatrix().regression(y.getMatrix(), ridge).getCoefficients();
  }
  









  public final double[] regression(Matrix y, double[] w, double ridge)
  {
    return getMatrix().regression(y.getMatrix(), w, ridge).getCoefficients();
  }
  





  public Matrix getL()
    throws Exception
  {
    int nr = numRows();
    int nc = numColumns();
    double[][] ld = new double[nr][nc];
    
    for (int i = 0; i < nr; i++) {
      for (int j = 0; (j < i) && (j < nc); j++) {
        ld[i][j] = getElement(i, j);
      }
      if (i < nc) ld[i][i] = 1.0D;
    }
    Matrix l = new Matrix(ld);
    return l;
  }
  





  public Matrix getU()
    throws Exception
  {
    int nr = numRows();
    int nc = numColumns();
    double[][] ud = new double[nr][nc];
    
    for (int i = 0; i < nr; i++) {
      for (int j = i; j < nc; j++) {
        ud[i][j] = getElement(i, j);
      }
    }
    Matrix u = new Matrix(ud);
    return u;
  }
  





  public int[] LUDecomposition()
    throws Exception
  {
    LUDecomposition lu = m_Matrix.lu();
    

    if (!lu.isNonsingular()) {
      throw new Exception("Matrix is singular");
    }
    weka.core.matrix.Matrix u = lu.getU();
    weka.core.matrix.Matrix l = lu.getL();
    

    int nr = numRows();
    int nc = numColumns();
    for (int i = 0; i < nr; i++) {
      for (int j = 0; j < nc; j++) {
        if (j < i) {
          setElement(i, j, l.get(i, j));
        } else {
          setElement(i, j, u.get(i, j));
        }
      }
    }
    u = null;
    l = null;
    
    return lu.getPivot();
  }
  







  public void solve(double[] bb)
    throws Exception
  {
    weka.core.matrix.Matrix x = m_Matrix.solve(new weka.core.matrix.Matrix(bb, bb.length));
    


    int nr = x.getRowDimension();
    for (int i = 0; i < nr; i++) {
      bb[i] = x.get(i, 0);
    }
  }
  











  public void eigenvalueDecomposition(double[][] V, double[] d)
    throws Exception
  {
    if (!isSymmetric()) {
      throw new Exception("EigenvalueDecomposition: Matrix must be symmetric.");
    }
    
    EigenvalueDecomposition eig = m_Matrix.eig();
    weka.core.matrix.Matrix v = eig.getV();
    double[] d2 = eig.getRealEigenvalues();
    

    int nr = numRows();
    int nc = numColumns();
    for (int i = 0; i < nr; i++) {
      for (int j = 0; j < nc; j++)
        V[i][j] = v.get(i, j);
    }
    for (int i = 0; i < d2.length; i++) {
      d[i] = d2[i];
    }
  }
  





  protected static double hypot(double a, double b)
  {
    return Maths.hypot(a, b);
  }
  





  public String toMatlab()
  {
    return getMatrix().toMatlab();
  }
  




  public static Matrix parseMatlab(String matlab)
    throws Exception
  {
    return new Matrix(weka.core.matrix.Matrix.parseMatlab(matlab).getArray());
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.25 $");
  }
  



  public static void main(String[] ops)
  {
    double[] first = { 2.3D, 1.2D, 5.0D };
    double[] second = { 5.2D, 1.4D, 9.0D };
    double[] response = { 4.0D, 7.0D, 8.0D };
    double[] weights = { 1.0D, 2.0D, 3.0D };
    
    try
    {
      double[][] m = { { 1.0D, 2.0D, 3.0D }, { 2.0D, 5.0D, 6.0D }, { 3.0D, 6.0D, 9.0D } };
      Matrix M = new Matrix(m);
      int n = M.numRows();
      double[][] V = new double[n][n];
      double[] d = new double[n];
      double[] e = new double[n];
      M.eigenvalueDecomposition(V, d);
      Matrix v = new Matrix(V);
      


      Matrix a = new Matrix(2, 3);
      Matrix b = new Matrix(3, 2);
      System.out.println("Number of columns for a: " + a.numColumns());
      System.out.println("Number of rows for a: " + a.numRows());
      a.setRow(0, first);
      a.setRow(1, second);
      b.setColumn(0, first);
      b.setColumn(1, second);
      System.out.println("a:\n " + a);
      System.out.println("b:\n " + b);
      System.out.println("a (0, 0): " + a.getElement(0, 0));
      System.out.println("a transposed:\n " + a.transpose());
      System.out.println("a * b:\n " + a.multiply(b));
      Matrix r = new Matrix(3, 1);
      r.setColumn(0, response);
      System.out.println("r:\n " + r);
      System.out.println("Coefficients of regression of b on r: ");
      double[] coefficients = b.regression(r, 1.0E-8D);
      for (int i = 0; i < coefficients.length; i++) {
        System.out.print(coefficients[i] + " ");
      }
      System.out.println();
      System.out.println("Weights: ");
      for (int i = 0; i < weights.length; i++) {
        System.out.print(weights[i] + " ");
      }
      System.out.println();
      System.out.println("Coefficients of weighted regression of b on r: ");
      coefficients = b.regression(r, weights, 1.0E-8D);
      for (int i = 0; i < coefficients.length; i++) {
        System.out.print(coefficients[i] + " ");
      }
      System.out.println();
      a.setElement(0, 0, 6.0D);
      System.out.println("a with (0, 0) set to 6:\n " + a);
      a.write(new FileWriter("main.matrix"));
      System.out.println("wrote matrix to \"main.matrix\"\n" + a);
      a = new Matrix(new FileReader("main.matrix"));
      System.out.println("read matrix from \"main.matrix\"\n" + a);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

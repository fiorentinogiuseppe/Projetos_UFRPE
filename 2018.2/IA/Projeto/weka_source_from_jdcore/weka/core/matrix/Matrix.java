package weka.core.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;












































































public class Matrix
  implements Cloneable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = 7856794138418366180L;
  protected double[][] A;
  protected int m;
  protected int n;
  
  public Matrix(int m, int n)
  {
    this.m = m;
    this.n = n;
    A = new double[m][n];
  }
  





  public Matrix(int m, int n, double s)
  {
    this.m = m;
    this.n = n;
    A = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] = s;
      }
    }
  }
  





  public Matrix(double[][] A)
  {
    m = A.length;
    n = A[0].length;
    for (int i = 0; i < m; i++) {
      if (A[i].length != n) {
        throw new IllegalArgumentException("All rows must have the same length.");
      }
    }
    this.A = A;
  }
  





  public Matrix(double[][] A, int m, int n)
  {
    this.A = A;
    this.m = m;
    this.n = n;
  }
  






  public Matrix(double[] vals, int m)
  {
    this.m = m;
    n = (m != 0 ? vals.length / m : 0);
    if (m * n != vals.length) {
      throw new IllegalArgumentException("Array length must be a multiple of m.");
    }
    A = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] = vals[(i + j * m)];
      }
    }
  }
  








  public Matrix(Reader r)
    throws Exception
  {
    LineNumberReader lnr = new LineNumberReader(r);
    
    int currentRow = -1;
    String line;
    while ((line = lnr.readLine()) != null)
    {

      if (!line.startsWith("%"))
      {

        StringTokenizer st = new StringTokenizer(line);
        
        if (st.hasMoreTokens())
        {

          if (currentRow < 0) {
            int rows = Integer.parseInt(st.nextToken());
            if (!st.hasMoreTokens()) {
              throw new Exception("Line " + lnr.getLineNumber() + ": expected number of columns");
            }
            
            int cols = Integer.parseInt(st.nextToken());
            A = new double[rows][cols];
            m = rows;
            n = cols;
            currentRow++;

          }
          else
          {
            if (currentRow == getRowDimension()) {
              throw new Exception("Line " + lnr.getLineNumber() + ": too many rows provided");
            }
            
            for (int i = 0; i < getColumnDimension(); i++) {
              if (!st.hasMoreTokens()) {
                throw new Exception("Line " + lnr.getLineNumber() + ": too few matrix elements provided");
              }
              
              set(currentRow, i, Double.valueOf(st.nextToken()).doubleValue());
            }
            currentRow++;
          } }
      }
    }
    if (currentRow == -1) {
      throw new Exception("Line " + lnr.getLineNumber() + ": expected number of rows");
    }
    if (currentRow != getRowDimension()) {
      throw new Exception("Line " + lnr.getLineNumber() + ": too few rows provided");
    }
  }
  




  public static Matrix constructWithCopy(double[][] A)
  {
    int m = A.length;
    int n = A[0].length;
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      if (A[i].length != n) {
        throw new IllegalArgumentException("All rows must have the same length.");
      }
      
      for (int j = 0; j < n; j++) {
        C[i][j] = A[i][j];
      }
    }
    return X;
  }
  


  public Matrix copy()
  {
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = A[i][j];
      }
    }
    return X;
  }
  


  public Object clone()
  {
    return copy();
  }
  



  public double[][] getArray()
  {
    return A;
  }
  



  public double[][] getArrayCopy()
  {
    double[][] C = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = A[i][j];
      }
    }
    return C;
  }
  



  public double[] getColumnPackedCopy()
  {
    double[] vals = new double[m * n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        vals[(i + j * m)] = A[i][j];
      }
    }
    return vals;
  }
  



  public double[] getRowPackedCopy()
  {
    double[] vals = new double[m * n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        vals[(i * n + j)] = A[i][j];
      }
    }
    return vals;
  }
  



  public int getRowDimension()
  {
    return m;
  }
  



  public int getColumnDimension()
  {
    return n;
  }
  






  public double get(int i, int j)
  {
    return A[i][j];
  }
  








  public Matrix getMatrix(int i0, int i1, int j0, int j1)
  {
    Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
    double[][] B = X.getArray();
    try {
      for (int i = i0; i <= i1; i++) {
        for (int j = j0; j <= j1; j++) {
          B[(i - i0)][(j - j0)] = A[i][j];
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }
  






  public Matrix getMatrix(int[] r, int[] c)
  {
    Matrix X = new Matrix(r.length, c.length);
    double[][] B = X.getArray();
    try {
      for (int i = 0; i < r.length; i++) {
        for (int j = 0; j < c.length; j++) {
          B[i][j] = A[r[i]][c[j]];
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }
  







  public Matrix getMatrix(int i0, int i1, int[] c)
  {
    Matrix X = new Matrix(i1 - i0 + 1, c.length);
    double[][] B = X.getArray();
    try {
      for (int i = i0; i <= i1; i++) {
        for (int j = 0; j < c.length; j++) {
          B[(i - i0)][j] = A[i][c[j]];
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }
  







  public Matrix getMatrix(int[] r, int j0, int j1)
  {
    Matrix X = new Matrix(r.length, j1 - j0 + 1);
    double[][] B = X.getArray();
    try {
      for (int i = 0; i < r.length; i++) {
        for (int j = j0; j <= j1; j++) {
          B[i][(j - j0)] = A[r[i]][j];
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }
  






  public void set(int i, int j, double s)
  {
    A[i][j] = s;
  }
  







  public void setMatrix(int i0, int i1, int j0, int j1, Matrix X)
  {
    try
    {
      for (int i = i0; i <= i1; i++) {
        for (int j = j0; j <= j1; j++) {
          A[i][j] = X.get(i - i0, j - j0);
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }
  





  public void setMatrix(int[] r, int[] c, Matrix X)
  {
    try
    {
      for (int i = 0; i < r.length; i++) {
        for (int j = 0; j < c.length; j++) {
          A[r[i]][c[j]] = X.get(i, j);
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }
  






  public void setMatrix(int[] r, int j0, int j1, Matrix X)
  {
    try
    {
      for (int i = 0; i < r.length; i++) {
        for (int j = j0; j <= j1; j++) {
          A[r[i]][j] = X.get(i, j - j0);
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }
  






  public void setMatrix(int i0, int i1, int[] c, Matrix X)
  {
    try
    {
      for (int i = i0; i <= i1; i++) {
        for (int j = 0; j < c.length; j++) {
          A[i][c[j]] = X.get(i - i0, j);
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }
  





  public boolean isSymmetric()
  {
    int nr = A.length;int nc = A[0].length;
    if (nr != nc) {
      return false;
    }
    for (int i = 0; i < nc; i++) {
      for (int j = 0; j < i; j++) {
        if (A[i][j] != A[j][i])
          return false;
      }
    }
    return true;
  }
  




  public boolean isSquare()
  {
    return getRowDimension() == getColumnDimension();
  }
  



  public Matrix transpose()
  {
    Matrix X = new Matrix(n, m);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[j][i] = A[i][j];
      }
    }
    return X;
  }
  



  public double norm1()
  {
    double f = 0.0D;
    for (int j = 0; j < n; j++) {
      double s = 0.0D;
      for (int i = 0; i < m; i++) {
        s += Math.abs(A[i][j]);
      }
      f = Math.max(f, s);
    }
    return f;
  }
  



  public double norm2()
  {
    return new SingularValueDecomposition(this).norm2();
  }
  



  public double normInf()
  {
    double f = 0.0D;
    for (int i = 0; i < m; i++) {
      double s = 0.0D;
      for (int j = 0; j < n; j++) {
        s += Math.abs(A[i][j]);
      }
      f = Math.max(f, s);
    }
    return f;
  }
  



  public double normF()
  {
    double f = 0.0D;
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        f = Maths.hypot(f, A[i][j]);
      }
    }
    return f;
  }
  



  public Matrix uminus()
  {
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = (-A[i][j]);
      }
    }
    return X;
  }
  




  public Matrix plus(Matrix B)
  {
    checkMatrixDimensions(B);
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = (A[i][j] + A[i][j]);
      }
    }
    return X;
  }
  




  public Matrix plusEquals(Matrix B)
  {
    checkMatrixDimensions(B);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] += A[i][j];
      }
    }
    return this;
  }
  




  public Matrix minus(Matrix B)
  {
    checkMatrixDimensions(B);
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = (A[i][j] - A[i][j]);
      }
    }
    return X;
  }
  




  public Matrix minusEquals(Matrix B)
  {
    checkMatrixDimensions(B);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] -= A[i][j];
      }
    }
    return this;
  }
  




  public Matrix arrayTimes(Matrix B)
  {
    checkMatrixDimensions(B);
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = (A[i][j] * A[i][j]);
      }
    }
    return X;
  }
  




  public Matrix arrayTimesEquals(Matrix B)
  {
    checkMatrixDimensions(B);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] *= A[i][j];
      }
    }
    return this;
  }
  




  public Matrix arrayRightDivide(Matrix B)
  {
    checkMatrixDimensions(B);
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = (A[i][j] / A[i][j]);
      }
    }
    return X;
  }
  




  public Matrix arrayRightDivideEquals(Matrix B)
  {
    checkMatrixDimensions(B);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] /= A[i][j];
      }
    }
    return this;
  }
  




  public Matrix arrayLeftDivide(Matrix B)
  {
    checkMatrixDimensions(B);
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = (A[i][j] / A[i][j]);
      }
    }
    return X;
  }
  




  public Matrix arrayLeftDivideEquals(Matrix B)
  {
    checkMatrixDimensions(B);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] /= A[i][j];
      }
    }
    return this;
  }
  




  public Matrix times(double s)
  {
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        C[i][j] = (s * A[i][j]);
      }
    }
    return X;
  }
  




  public Matrix timesEquals(double s)
  {
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        A[i][j] = (s * A[i][j]);
      }
    }
    return this;
  }
  





  public Matrix times(Matrix B)
  {
    if (m != n) {
      throw new IllegalArgumentException("Matrix inner dimensions must agree.");
    }
    Matrix X = new Matrix(m, n);
    double[][] C = X.getArray();
    double[] Bcolj = new double[n];
    for (int j = 0; j < n; j++) {
      for (int k = 0; k < n; k++) {
        Bcolj[k] = A[k][j];
      }
      for (int i = 0; i < m; i++) {
        double[] Arowi = A[i];
        double s = 0.0D;
        for (int k = 0; k < n; k++) {
          s += Arowi[k] * Bcolj[k];
        }
        C[i][j] = s;
      }
    }
    return X;
  }
  




  public LUDecomposition lu()
  {
    return new LUDecomposition(this);
  }
  




  public QRDecomposition qr()
  {
    return new QRDecomposition(this);
  }
  




  public CholeskyDecomposition chol()
  {
    return new CholeskyDecomposition(this);
  }
  




  public SingularValueDecomposition svd()
  {
    return new SingularValueDecomposition(this);
  }
  




  public EigenvalueDecomposition eig()
  {
    return new EigenvalueDecomposition(this);
  }
  




  public Matrix solve(Matrix B)
  {
    return m == n ? new LUDecomposition(this).solve(B) : new QRDecomposition(this).solve(B);
  }
  





  public Matrix solveTranspose(Matrix B)
  {
    return transpose().solve(B.transpose());
  }
  



  public Matrix inverse()
  {
    return solve(identity(m, m));
  }
  
































































  public Matrix sqrt()
  {
    Matrix result = null;
    


    EigenvalueDecomposition evd = eig();
    Matrix v = evd.getV();
    Matrix d = evd.getD();
    

    Matrix s = new Matrix(d.getRowDimension(), d.getColumnDimension());
    for (int i = 0; i < s.getRowDimension(); i++) {
      for (int n = 0; n < s.getColumnDimension(); n++) {
        s.set(i, n, StrictMath.sqrt(d.get(i, n)));
      }
    }
    




















    Matrix a = v.inverse();
    Matrix b = v.times(s).inverse();
    v = null;
    d = null;
    evd = null;
    s = null;
    result = a.solve(b).inverse();
    
    return result;
  }
  








  public LinearRegression regression(Matrix y, double ridge)
  {
    return new LinearRegression(this, y, ridge);
  }
  










  public final LinearRegression regression(Matrix y, double[] w, double ridge)
  {
    return new LinearRegression(this, y, w, ridge);
  }
  



  public double det()
  {
    return new LUDecomposition(this).det();
  }
  



  public int rank()
  {
    return new SingularValueDecomposition(this).rank();
  }
  



  public double cond()
  {
    return new SingularValueDecomposition(this).cond();
  }
  



  public double trace()
  {
    double t = 0.0D;
    for (int i = 0; i < Math.min(m, n); i++) {
      t += A[i][i];
    }
    return t;
  }
  





  public static Matrix random(int m, int n)
  {
    Matrix A = new Matrix(m, n);
    double[][] X = A.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        X[i][j] = Math.random();
      }
    }
    return A;
  }
  





  public static Matrix identity(int m, int n)
  {
    Matrix A = new Matrix(m, n);
    double[][] X = A.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        X[i][j] = (i == j ? 1.0D : 0.0D);
      }
    }
    return A;
  }
  





  public void print(int w, int d)
  {
    print(new PrintWriter(System.out, true), w, d);
  }
  






  public void print(PrintWriter output, int w, int d)
  {
    DecimalFormat format = new DecimalFormat();
    format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    format.setMinimumIntegerDigits(1);
    format.setMaximumFractionDigits(d);
    format.setMinimumFractionDigits(d);
    format.setGroupingUsed(false);
    print(output, format, w + 2);
  }
  









  public void print(NumberFormat format, int width)
  {
    print(new PrintWriter(System.out, true), format, width);
  }
  















  public void print(PrintWriter output, NumberFormat format, int width)
  {
    output.println();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        String s = format.format(A[i][j]);
        int padding = Math.max(1, width - s.length());
        for (int k = 0; k < padding; k++)
          output.print(' ');
        output.print(s);
      }
      output.println();
    }
    output.println();
  }
  













  public static Matrix read(BufferedReader input)
    throws IOException
  {
    StreamTokenizer tokenizer = new StreamTokenizer(input);
    






    tokenizer.resetSyntax();
    tokenizer.wordChars(0, 255);
    tokenizer.whitespaceChars(0, 32);
    tokenizer.eolIsSignificant(true);
    Vector v = new Vector();
    

    while (tokenizer.nextToken() == 10) {}
    if (ttype == -1)
      throw new IOException("Unexpected EOF on matrix read.");
    do {
      v.addElement(Double.valueOf(sval));
    } while (tokenizer.nextToken() == -3);
    
    int n = v.size();
    double[] row = new double[n];
    for (int j = 0; j < n; j++)
      row[j] = ((Double)v.elementAt(j)).doubleValue();
    v.removeAllElements();
    v.addElement(row);
    while (tokenizer.nextToken() == -3)
    {
      v.addElement(row = new double[n]);
      int j = 0;
      do {
        if (j >= n) { throw new IOException("Row " + v.size() + " is too long.");
        }
        row[(j++)] = Double.valueOf(sval).doubleValue();
      } while (tokenizer.nextToken() == -3);
      if (j < n) { throw new IOException("Row " + v.size() + " is too short.");
      }
    }
    int m = v.size();
    double[][] A = new double[m][];
    v.copyInto(A);
    return new Matrix(A);
  }
  



  private void checkMatrixDimensions(Matrix B)
  {
    if ((m != m) || (n != n)) {
      throw new IllegalArgumentException("Matrix dimensions must agree.");
    }
  }
  







  public void write(Writer w)
    throws Exception
  {
    w.write("% Rows\tColumns\n");
    w.write("" + getRowDimension() + "\t" + getColumnDimension() + "\n");
    w.write("% Matrix elements\n");
    for (int i = 0; i < getRowDimension(); i++) {
      for (int j = 0; j < getColumnDimension(); j++)
        w.write("" + get(i, j) + "\t");
      w.write("\n");
    }
    w.flush();
  }
  







  public String toString()
  {
    double maxval = 0.0D;
    boolean fractional = false;
    for (int i = 0; i < getRowDimension(); i++) {
      for (int j = 0; j < getColumnDimension(); j++) {
        double current = get(i, j);
        if (current < 0.0D)
          current *= -11.0D;
        if (current > maxval)
          maxval = current;
        double fract = Math.abs(current - Math.rint(current));
        if ((!fractional) && (Math.log(fract) / Math.log(10.0D) >= -2.0D))
        {
          fractional = true;
        }
      }
    }
    int width = (int)(Math.log(maxval) / Math.log(10.0D) + (fractional ? 4 : 1));
    

    StringBuffer text = new StringBuffer();
    for (int i = 0; i < getRowDimension(); i++) {
      for (int j = 0; j < getColumnDimension(); j++) {
        text.append(" ").append(Utils.doubleToString(get(i, j), width, fractional ? 2 : 0));
      }
      text.append("\n");
    }
    
    return text.toString();
  }
  









  public String toMatlab()
  {
    StringBuffer result = new StringBuffer();
    
    result.append("[");
    
    for (int i = 0; i < getRowDimension(); i++) {
      if (i > 0) {
        result.append("; ");
      }
      for (int n = 0; n < getColumnDimension(); n++) {
        if (n > 0)
          result.append(" ");
        result.append(Double.toString(get(i, n)));
      }
    }
    
    result.append("]");
    
    return result.toString();
  }
  












  public static Matrix parseMatlab(String matlab)
    throws Exception
  {
    String cells = matlab.substring(matlab.indexOf("[") + 1, matlab.indexOf("]")).trim();
    


    StringTokenizer tokRow = new StringTokenizer(cells, ";");
    int rows = tokRow.countTokens();
    StringTokenizer tokCol = new StringTokenizer(tokRow.nextToken(), " ");
    int cols = tokCol.countTokens();
    

    Matrix result = new Matrix(rows, cols);
    tokRow = new StringTokenizer(cells, ";");
    rows = 0;
    while (tokRow.hasMoreTokens()) {
      tokCol = new StringTokenizer(tokRow.nextToken(), " ");
      cols = 0;
      while (tokCol.hasMoreTokens()) {
        result.set(rows, cols, Double.parseDouble(tokCol.nextToken()));
        cols++;
      }
      rows++;
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  






  public static void main(String[] args)
  {
    try
    {
      System.out.println("\nIdentity\n");
      Matrix I = identity(3, 5);
      System.out.println("I(3,5)\n" + I);
      

      System.out.println("\nbasic operations - square\n");
      Matrix A = random(3, 3);
      Matrix B = random(3, 3);
      System.out.println("A\n" + A);
      System.out.println("B\n" + B);
      System.out.println("A'\n" + A.inverse());
      System.out.println("A^T\n" + A.transpose());
      System.out.println("A+B\n" + A.plus(B));
      System.out.println("A*B\n" + A.times(B));
      System.out.println("X from A*X=B\n" + A.solve(B));
      

      System.out.println("\nbasic operations - non square\n");
      A = random(2, 3);
      B = random(3, 4);
      System.out.println("A\n" + A);
      System.out.println("B\n" + B);
      System.out.println("A*B\n" + A.times(B));
      

      System.out.println("\nsqrt (1)\n");
      A = new Matrix(new double[][] { { 5.0D, -4.0D, 1.0D, 0.0D, 0.0D }, { -4.0D, 6.0D, -4.0D, 1.0D, 0.0D }, { 1.0D, -4.0D, 6.0D, -4.0D, 1.0D }, { 0.0D, 1.0D, -4.0D, 6.0D, -4.0D }, { 0.0D, 0.0D, 1.0D, -4.0D, 5.0D } });
      System.out.println("A\n" + A);
      System.out.println("sqrt(A)\n" + A.sqrt());
      

      System.out.println("\nsqrt (2)\n");
      A = new Matrix(new double[][] { { 7.0D, 10.0D }, { 15.0D, 22.0D } });
      System.out.println("A\n" + A);
      System.out.println("sqrt(A)\n" + A.sqrt());
      System.out.println("det(A)\n" + A.det() + "\n");
      

      System.out.println("\nEigenvalue Decomposition\n");
      EigenvalueDecomposition evd = A.eig();
      System.out.println("[V,D] = eig(A)");
      System.out.println("- V\n" + evd.getV());
      System.out.println("- D\n" + evd.getD());
      

      System.out.println("\nLU Decomposition\n");
      LUDecomposition lud = A.lu();
      System.out.println("[L,U,P] = lu(A)");
      System.out.println("- L\n" + lud.getL());
      System.out.println("- U\n" + lud.getU());
      System.out.println("- P\n" + Utils.arrayToString(lud.getPivot()) + "\n");
      

      System.out.println("\nRegression\n");
      B = new Matrix(new double[][] { { 3.0D }, { 2.0D } });
      double ridge = 0.5D;
      double[] weights = { 0.3D, 0.7D };
      LinearRegression lr = A.regression(B, ridge);
      System.out.println("A\n" + A);
      System.out.println("B\n" + B);
      System.out.println("ridge = " + ridge + "\n");
      System.out.println("weights = " + Utils.arrayToString(weights) + "\n");
      System.out.println("A.regression(B, ridge)\n" + A.regression(B, ridge) + "\n");
      
      System.out.println("A.regression(B, weights, ridge)\n" + A.regression(B, weights, ridge) + "\n");
      


      System.out.println("\nWriter/Reader\n");
      StringWriter writer = new StringWriter();
      A.write(writer);
      System.out.println("A.write(Writer)\n" + writer);
      A = new Matrix(new StringReader(writer.toString()));
      System.out.println("A = new Matrix.read(Reader)\n" + A);
      

      System.out.println("\nMatlab-Format\n");
      String matlab = "[ 1   2;3 4 ]";
      System.out.println("Matlab: " + matlab);
      System.out.println("from Matlab:\n" + parseMatlab(matlab));
      System.out.println("to Matlab:\n" + parseMatlab(matlab).toMatlab());
      matlab = "[1 2 3 4;3 4 5 6;7 8 9 10]";
      System.out.println("Matlab: " + matlab);
      System.out.println("from Matlab:\n" + parseMatlab(matlab));
      System.out.println("to Matlab:\n" + parseMatlab(matlab).toMatlab() + "\n");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

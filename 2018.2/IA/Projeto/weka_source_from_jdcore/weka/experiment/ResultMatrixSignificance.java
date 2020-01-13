package weka.experiment;

import java.io.PrintStream;
import weka.core.RevisionUtils;


































public class ResultMatrixSignificance
  extends ResultMatrix
{
  private static final long serialVersionUID = -1280545644109764206L;
  
  public ResultMatrixSignificance()
  {
    this(1, 1);
  }
  


  public ResultMatrixSignificance(int cols, int rows)
  {
    super(cols, rows);
  }
  



  public ResultMatrixSignificance(ResultMatrix matrix)
  {
    super(matrix);
  }
  


  public String getDisplayName()
  {
    return "Significance only";
  }
  


  public void clear()
  {
    super.clear();
    setPrintColNames(false);
    setRowNameWidth(40);
    super.setShowStdDev(false);
  }
  








  public void setShowStdDev(boolean show) {}
  








  public String toStringMatrix()
  {
    StringBuffer result = new StringBuffer();
    String[][] cells = toArray();
    

    int nameWidth = getColSize(cells, 0);
    for (int i = 0; i < cells.length - 1; i++) {
      cells[i][0] = padString(cells[i][0], nameWidth);
    }
    
    int rows = cells.length - 1;
    if (getShowAverage()) {
      rows--;
    }
    for (i = 0; i < rows; i++) {
      String line = "";
      String colStr = "";
      
      for (int n = 0; n < cells[i].length; n++)
      {
        if ((isMean(n)) || (isRowName(n))) {
          colStr = cells[0][n];
        }
        if ((n <= 1) || (isSignificance(n)))
        {


          if (n > 0) {
            line = line + " ";
          }
          if ((i > 0) && (n > 1)) {
            line = line + " ";
          }
          if (i == 0) {
            line = line + colStr;

          }
          else if (n == 0) {
            line = line + cells[i][n];
          }
          else if (n == 1) {
            line = line + colStr.replaceAll(".", " ");
          }
          else {
            line = line + cells[i][n];
            
            line = line + colStr.replaceAll(".", " ").substring(2);
          }
        }
      }
      result.append(line + "\n");
      

      if (i == 0) {
        result.append(line.replaceAll(".", "-") + "\n");
      }
    }
    return result.toString();
  }
  




  public String toStringHeader()
  {
    return new ResultMatrixPlainText(this).toStringHeader();
  }
  



  public String toStringKey()
  {
    return new ResultMatrixPlainText(this).toStringKey();
  }
  


  public String toStringSummary()
  {
    return new ResultMatrixPlainText(this).toStringSummary();
  }
  


  public String toStringRanking()
  {
    return new ResultMatrixPlainText(this).toStringRanking();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  






  public static void main(String[] args)
  {
    ResultMatrix matrix = new ResultMatrixSignificance(3, 3);
    

    matrix.addHeader("header1", "value1");
    matrix.addHeader("header2", "value2");
    matrix.addHeader("header2", "value3");
    

    for (int i = 0; i < matrix.getRowCount(); i++) {
      for (int n = 0; n < matrix.getColCount(); n++) {
        matrix.setMean(n, i, (i + 1) * n);
        matrix.setStdDev(n, i, (i + 1) * n / 100.0D);
        if (i == n) {
          if (i % 2 == 1) {
            matrix.setSignificance(n, i, 1);
          } else {
            matrix.setSignificance(n, i, 2);
          }
        }
      }
    }
    System.out.println("\n\n--> " + matrix.getDisplayName());
    
    System.out.println("\n1. complete\n");
    System.out.println(matrix.toStringHeader() + "\n");
    System.out.println(matrix.toStringMatrix() + "\n");
    System.out.println(matrix.toStringKey());
    
    System.out.println("\n2. complete with std deviations\n");
    matrix.setShowStdDev(true);
    System.out.println(matrix.toStringMatrix());
    
    System.out.println("\n3. cols numbered\n");
    matrix.setPrintColNames(false);
    System.out.println(matrix.toStringMatrix());
    
    System.out.println("\n4. second col missing\n");
    matrix.setColHidden(1, true);
    System.out.println(matrix.toStringMatrix());
    
    System.out.println("\n5. last row missing, rows numbered too\n");
    matrix.setRowHidden(2, true);
    matrix.setPrintRowNames(false);
    System.out.println(matrix.toStringMatrix());
    
    System.out.println("\n6. mean prec to 3\n");
    matrix.setMeanPrec(3);
    matrix.setPrintRowNames(false);
    System.out.println(matrix.toStringMatrix());
  }
}

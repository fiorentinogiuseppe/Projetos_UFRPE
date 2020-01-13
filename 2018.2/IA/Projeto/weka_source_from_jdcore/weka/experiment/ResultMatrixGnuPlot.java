package weka.experiment;

import java.io.PrintStream;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.Version;

































public class ResultMatrixGnuPlot
  extends ResultMatrix
{
  private static final long serialVersionUID = -234648254944790097L;
  
  public ResultMatrixGnuPlot()
  {
    this(1, 1);
  }
  


  public ResultMatrixGnuPlot(int cols, int rows)
  {
    super(cols, rows);
  }
  



  public ResultMatrixGnuPlot(ResultMatrix matrix)
  {
    super(matrix);
  }
  


  public String getDisplayName()
  {
    return "GNUPlot";
  }
  


  public void clear()
  {
    super.clear();
    setRowNameWidth(50);
    setColNameWidth(50);
    setEnumerateRowNames(false);
    setEnumerateColNames(false);
    LEFT_PARENTHESES = "";
    RIGHT_PARENTHESES = "";
  }
  




  public String toStringHeader()
  {
    return new ResultMatrixPlainText(this).toStringHeader();
  }
  










  public String toStringMatrix()
  {
    StringBuffer result = new StringBuffer();
    String[][] cells = toArray();
    

    String generated = "# generated by WEKA " + Version.VERSION + "\n";
    

    result.append("\n");
    result.append("##################\n");
    result.append("# file: plot.dat #\n");
    result.append("##################\n");
    result.append(generated);
    result.append("# contains the data for the plot\n");
    
    result.append("\n");
    result.append("# key for the x-axis\n");
    for (int i = 1; i < cells.length - 1; i++) {
      result.append("# " + i + " - " + cells[i][0] + "\n");
    }
    result.append("\n");
    result.append("# data for the plot\n");
    for (i = 1; i < cells.length - 1; i++) {
      result.append(Integer.toString(i));
      for (int n = 1; n < cells[i].length; n++)
        if (!isSignificance(n))
        {
          result.append(" ");
          result.append(Utils.quote(cells[i][n]));
        }
      result.append("\n");
    }
    result.append("#######\n");
    result.append("# end #\n");
    result.append("#######\n");
    

    result.append("\n");
    result.append("##################\n");
    result.append("# file: plot.scr #\n");
    result.append("##################\n");
    result.append(generated);
    result.append("# script to plot the data\n");
    result.append("\n");
    result.append("# display it in a window:\n");
    result.append("set terminal x11\n");
    result.append("set output\n");
    result.append("\n");
    result.append("# to display all data rows:\n");
    result.append("set xrange [0:" + (cells.length - 2 + 1) + "]\n");
    result.append("\n");
    result.append("# axis labels, e.g.:\n");
    result.append("#set xlabel \"Datasets\"\n");
    result.append("#set ylabel \"Accuracy in %\"\n");
    result.append("\n");
    result.append("# the plot commands\n");
    int n = 1;
    i = 0;
    while (i < cells[0].length - 1) {
      i++;
      
      if (!isSignificance(i))
      {

        n++;
        
        String line;
        if (i == 1) {
          line = "plot";
        } else
          line = "replot";
        String line = line + " \"plot.dat\"";
        

        String title = "title \"" + cells[0][i] + "\"";
        

        line = line + " using 1:" + n;
        if (getShowStdDev()) {
          n++;
          i++;
          
          line = line + ":" + n;
        }
        

        line = line + " with";
        if (getShowStdDev()) {
          line = line + " yerrorbars";
        } else
          line = line + " lines";
        line = line + " " + title;
        
        result.append(line + "\n");
      } }
    result.append("\n");
    result.append("# generate ps:\n");
    result.append("#set terminal postscript\n");
    result.append("#set output \"plot.ps\"\n");
    result.append("#replot\n");
    result.append("\n");
    result.append("# generate png:\n");
    result.append("#set terminal png size 800,600\n");
    result.append("#set output \"plot.png\"\n");
    result.append("#replot\n");
    result.append("\n");
    result.append("# wait for user to hit <Return>\n");
    result.append("pause -1\n");
    result.append("#######\n");
    result.append("# end #\n");
    result.append("#######\n");
    
    return result.toString();
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
    ResultMatrix matrix = new ResultMatrixGnuPlot(3, 3);
    

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
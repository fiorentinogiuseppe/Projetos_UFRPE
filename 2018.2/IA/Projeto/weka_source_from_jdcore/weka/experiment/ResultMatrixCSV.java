package weka.experiment;

import java.io.PrintStream;
import weka.core.RevisionUtils;
import weka.core.Utils;

































public class ResultMatrixCSV
  extends ResultMatrix
{
  private static final long serialVersionUID = -171838863135042743L;
  
  public ResultMatrixCSV()
  {
    this(1, 1);
  }
  


  public ResultMatrixCSV(int cols, int rows)
  {
    super(cols, rows);
  }
  



  public ResultMatrixCSV(ResultMatrix matrix)
  {
    super(matrix);
  }
  


  public String getDisplayName()
  {
    return "CSV";
  }
  


  public void clear()
  {
    super.clear();
    setRowNameWidth(25);
    setPrintColNames(false);
    setEnumerateColNames(true);
    LEFT_PARENTHESES = "[";
    RIGHT_PARENTHESES = "]";
  }
  




  public String toStringHeader()
  {
    return new ResultMatrixPlainText(this).toStringHeader();
  }
  







  public String toStringMatrix()
  {
    StringBuffer result = new StringBuffer();
    String[][] cells = toArray();
    
    for (int i = 0; i < cells.length; i++) {
      for (int n = 0; n < cells[i].length; n++) {
        if (n > 0)
          result.append(",");
        result.append(Utils.quote(cells[i][n]));
      }
      result.append("\n");
    }
    
    return result.toString();
  }
  






  public String toStringKey()
  {
    String result = "Key,\n";
    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        result = result + LEFT_PARENTHESES + (i + 1) + RIGHT_PARENTHESES + "," + Utils.quote(removeFilterName(m_ColNames[i])) + "\n";
      }
    }
    
    return result;
  }
  









  public String toStringSummary()
  {
    if (m_NonSigWins == null) {
      return "-summary data not set-";
    }
    String result = "";
    String titles = "";
    int resultsetLength = 1 + Math.max((int)(Math.log(getColCount()) / Math.log(10.0D)), (int)(Math.log(getRowCount()) / Math.log(10.0D)));
    

    for (int i = 0; i < getColCount(); i++)
      if (!getColHidden(i))
      {
        if (!titles.equals(""))
          titles = titles + ",";
        titles = titles + getSummaryTitle(i);
      }
    result = result + titles + ",'(No. of datasets where [col] >> [row])'\n";
    
    for (i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        String line = "";
        for (int j = 0; j < getColCount(); j++) {
          if (!getColHidden(j))
          {

            if (!line.equals("")) {
              line = line + ",";
            }
            if (j == i) {
              line = line + "-";
            } else {
              line = line + m_NonSigWins[i][j] + " (" + m_Wins[i][j] + ")";
            }
          }
        }
        result = result + line + "," + getSummaryTitle(i) + " = " + removeFilterName(m_ColNames[i]) + '\n';
      }
    }
    return result;
  }
  









  public String toStringRanking()
  {
    if (m_RankingWins == null) {
      return "-ranking data not set-";
    }
    int biggest = Math.max(m_RankingWins[Utils.maxIndex(m_RankingWins)], m_RankingLosses[Utils.maxIndex(m_RankingLosses)]);
    
    int width = Math.max(2 + (int)(Math.log(biggest) / Math.log(10.0D)), ">-<".length());
    
    String result = ">-<,>,<,Resultset\n";
    
    int[] ranking = Utils.sort(m_RankingDiff);
    
    for (int i = getColCount() - 1; i >= 0; i--) {
      int curr = ranking[i];
      
      if (!getColHidden(curr))
      {

        result = result + m_RankingDiff[curr] + "," + m_RankingWins[curr] + "," + m_RankingLosses[curr] + "," + removeFilterName(m_ColNames[curr]) + "\n";
      }
    }
    


    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  






  public static void main(String[] args)
  {
    ResultMatrix matrix = new ResultMatrixCSV(3, 3);
    

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

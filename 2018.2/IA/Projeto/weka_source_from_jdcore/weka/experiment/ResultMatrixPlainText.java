package weka.experiment;

import java.io.PrintStream;
import java.util.Vector;
import weka.core.RevisionUtils;
import weka.core.Utils;
































public class ResultMatrixPlainText
  extends ResultMatrix
{
  private static final long serialVersionUID = 1502934525382357937L;
  
  public ResultMatrixPlainText()
  {
    this(1, 1);
  }
  


  public ResultMatrixPlainText(int cols, int rows)
  {
    super(cols, rows);
  }
  



  public ResultMatrixPlainText(ResultMatrix matrix)
  {
    super(matrix);
  }
  


  public String getDisplayName()
  {
    return "Plain Text";
  }
  


  public void clear()
  {
    super.clear();
    setRowNameWidth(25);
    setCountWidth(5);
  }
  









  public String toStringHeader()
  {
    String result = "";
    

    String[][] data = new String[m_HeaderKeys.size()][2];
    for (int i = 0; i < m_HeaderKeys.size(); i++) {
      data[i][0] = (m_HeaderKeys.get(i).toString() + ":");
      data[i][1] = m_HeaderValues.get(i).toString();
    }
    

    int size = getColSize(data, 0);
    for (i = 0; i < data.length; i++) {
      data[i][0] = padString(data[i][0], size);
    }
    
    for (i = 0; i < data.length; i++) {
      result = result + data[i][0] + " " + data[i][1] + "\n";
    }
    return result;
  }
  



















  public String toStringMatrix()
  {
    StringBuffer result = new StringBuffer();
    StringBuffer head = new StringBuffer();
    StringBuffer body = new StringBuffer();
    StringBuffer foot = new StringBuffer();
    String[][] cells = toArray();
    int[] startMeans = new int[getColCount()];
    int[] startSigs = new int[getColCount() - 1];
    int maxLength = 0;
    

    for (int n = 1; n < cells[0].length; n++) {
      int size = getColSize(cells, n, true, true);
      for (int i = 1; i < cells.length - 1; i++) {
        cells[i][n] = padString(cells[i][n], size, true);
      }
    }
    
    int indexBase = 1;
    if (getShowStdDev()) {
      indexBase++;
    }
    
    int indexSecond = indexBase + 1;
    if (getShowStdDev()) {
      indexSecond++;
    }
    
    int j = 0;
    int k = 0;
    for (int i = 1; i < cells.length - 1; i++) {
      if (isAverage(i))
        body.append(padString("", maxLength).replaceAll(".", "-") + "\n");
      String line = "";
      
      for (n = 0; n < cells[0].length; n++)
      {
        if (i == 1) {
          if (isMean(n)) {
            startMeans[j] = line.length();
            j++;
          }
          
          if (isSignificance(n)) {
            startSigs[k] = line.length();
            k++;
          }
        }
        
        if (n == 0) {
          line = line + padString(cells[i][n], getRowNameWidth());
          if (!isAverage(i)) {
            line = line + padString(new StringBuilder().append("(").append(Utils.doubleToString(getCount(getDisplayRow(i - 1)), 0)).append(")").toString(), getCountWidth(), true);
          }
          else
          {
            line = line + padString("", getCountWidth(), true);
          }
        }
        else {
          if (isMean(n)) {
            line = line + "  ";
          }
          
          if (getShowStdDev()) {
            if (isMean(n - 1)) {
              if (!cells[i][n].trim().equals("")) {
                line = line + "(" + cells[i][n] + ")";
              } else {
                line = line + " " + cells[i][n] + " ";
              }
            } else {
              line = line + " " + cells[i][n];
            }
          } else {
            line = line + " " + cells[i][n];
          }
        }
        

        if (n == indexBase) {
          line = line + " |";
        }
      }
      
      if (i == 1) {
        maxLength = line.length();
      }
      body.append(line + "\n");
    }
    

    String line = padString(cells[0][0], startMeans[0]);
    i = -1;
    for (n = 1; n < cells[0].length; n++) {
      if (isMean(n)) {
        i++;
        
        if (i == 0) {
          line = padString(line, startMeans[i] - getCountWidth());
        } else if (i == 1) {
          line = padString(line, startMeans[i] - " |".length());
        } else if (i > 1) {
          line = padString(line, startMeans[i]);
        }
        if (i == 1) {
          line = line + " |";
        }
        line = line + " " + cells[0][n];
      }
    }
    line = padString(line, maxLength);
    head.append(line + "\n");
    head.append(line.replaceAll(".", "-") + "\n");
    body.append(line.replaceAll(".", "-") + "\n");
    

    if (getColCount() > 1) {
      line = padString(cells[(cells.length - 1)][0], startMeans[1] - 2, true) + " |";
      i = 0;
      for (n = 1; n < cells[(cells.length - 1)].length; n++) {
        if (isSignificance(n)) {
          line = padString(line, startSigs[i] + 1 - cells[(cells.length - 1)][n].length());
          
          line = line + " " + cells[(cells.length - 1)][n];
          i++;
        }
      }
      line = padString(line, maxLength);
    }
    else {
      line = padString(cells[(cells.length - 1)][0], line.length() - 2) + " |";
    }
    foot.append(line + "\n");
    

    result.append(head.toString());
    result.append(body.toString());
    result.append(foot.toString());
    
    return result.toString();
  }
  






  public String toStringKey()
  {
    String result = "Key:\n";
    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        result = result + LEFT_PARENTHESES + (i + 1) + RIGHT_PARENTHESES + " " + removeFilterName(m_ColNames[i]) + "\n";
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
    

    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {
        titles = titles + " " + Utils.padLeft(new StringBuilder().append("").append(getSummaryTitle(i)).toString(), resultsetLength * 2 + 3);
      }
    }
    result = result + titles + "  (No. of datasets where [col] >> [row])\n";
    
    for (i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        for (int j = 0; j < getColCount(); j++) {
          if (!getColHidden(j))
          {

            result = result + " ";
            if (j == i) {
              result = result + Utils.padLeft("-", resultsetLength * 2 + 3);
            } else {
              result = result + Utils.padLeft(new StringBuilder().append("").append(m_NonSigWins[i][j]).append(" (").append(m_Wins[i][j]).append(")").toString(), resultsetLength * 2 + 3);
            }
          }
        }
        
        result = result + " | " + getSummaryTitle(i) + " = " + getColName(i) + '\n';
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
    
    String result = Utils.padLeft(">-<", width) + ' ' + Utils.padLeft(">", width) + ' ' + Utils.padLeft("<", width) + " Resultset\n";
    


    int[] ranking = Utils.sort(m_RankingDiff);
    
    for (int i = getColCount() - 1; i >= 0; i--) {
      int curr = ranking[i];
      
      if (!getColHidden(curr))
      {

        result = result + Utils.padLeft(new StringBuilder().append("").append(m_RankingDiff[curr]).toString(), width) + ' ' + Utils.padLeft(new StringBuilder().append("").append(m_RankingWins[curr]).toString(), width) + ' ' + Utils.padLeft(new StringBuilder().append("").append(m_RankingLosses[curr]).toString(), width) + ' ' + removeFilterName(m_ColNames[curr]) + '\n';
      }
    }
    


    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  






  public static void main(String[] args)
  {
    ResultMatrix matrix = new ResultMatrixPlainText(3, 3);
    

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

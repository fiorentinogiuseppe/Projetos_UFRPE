package weka.experiment;

import java.io.PrintStream;
import weka.core.RevisionUtils;
import weka.core.Utils;

































public class ResultMatrixHTML
  extends ResultMatrix
{
  private static final long serialVersionUID = 6672380422544799990L;
  
  public ResultMatrixHTML()
  {
    this(1, 1);
  }
  


  public ResultMatrixHTML(int cols, int rows)
  {
    super(cols, rows);
  }
  



  public ResultMatrixHTML(ResultMatrix matrix)
  {
    super(matrix);
  }
  


  public String getDisplayName()
  {
    return "HTML";
  }
  


  public void clear()
  {
    super.clear();
    setRowNameWidth(25);
    setPrintColNames(false);
    setEnumerateColNames(true);
  }
  




  public String toStringHeader()
  {
    return new ResultMatrixPlainText(this).toStringHeader();
  }
  








  public String toStringMatrix()
  {
    StringBuffer result = new StringBuffer();
    String[][] cells = toArray();
    
    result.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
    

    result.append("   <tr>");
    for (int n = 0; n < cells[0].length; n++) {
      if (isRowName(n)) {
        result.append("<td><b>" + cells[0][n] + "</b></td>");
      }
      else if (isMean(n)) { int cols;
        int cols; if (n == 1) {
          cols = 1;
        } else
          cols = 2;
        if (getShowStdDev())
          cols++;
        result.append("<td align=\"center\" colspan=\"" + cols + "\">");
        result.append("<b>" + cells[0][n] + "</b>");
        result.append("</td>");
      }
    }
    result.append("</tr>\n");
    

    for (int i = 1; i < cells.length; i++) {
      result.append("   <tr>");
      for (n = 0; n < cells[i].length; n++) {
        if (isRowName(n)) {
          result.append("<td>");
        } else if ((isMean(n)) || (isStdDev(n))) {
          result.append("<td align=\"right\">");
        } else if (isSignificance(n)) {
          result.append("<td align=\"center\">");
        } else {
          result.append("<td>");
        }
        
        if (cells[i][n].trim().equals("")) {
          result.append("&nbsp;");
        } else if (isStdDev(n)) {
          result.append("&plusmn;&nbsp;" + cells[i][n]);
        } else {
          result.append(cells[i][n]);
        }
        result.append("</td>");
      }
      result.append("</tr>\n");
    }
    result.append("</table>\n");
    
    return result.toString();
  }
  






  public String toStringKey()
  {
    String result = "<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n   <tr><td colspan=\"2\"><b>Key</b></td></tr>\n";
    
    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        result = result + "   <tr><td><b>(" + (i + 1) + ")</b></td>" + "<td>" + removeFilterName(m_ColNames[i]) + "</td>" + "</tr>\n";
      }
    }
    


    result = result + "</table>\n";
    
    return result;
  }
  









  public String toStringSummary()
  {
    if (m_NonSigWins == null) {
      return "-summary data not set-";
    }
    String result = "<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n";
    String titles = "   <tr>";
    int resultsetLength = 1 + Math.max((int)(Math.log(getColCount()) / Math.log(10.0D)), (int)(Math.log(getRowCount()) / Math.log(10.0D)));
    

    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {
        titles = titles + "<td align=\"center\"><b>" + getSummaryTitle(i) + "</b></td>"; }
    }
    result = result + titles + "<td><b>(No. of datasets where [col] &gt;&gt; [row])</b></td></tr>\n";
    

    for (i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        result = result + "   <tr>";
        
        for (int j = 0; j < getColCount(); j++) {
          if (!getColHidden(j)) {
            String content;
            String content;
            if (j == i) {
              content = Utils.padLeft("-", resultsetLength * 2 + 3);
            } else {
              content = Utils.padLeft("" + m_NonSigWins[i][j] + " (" + m_Wins[i][j] + ")", resultsetLength * 2 + 3);
            }
            
            result = result + "<td>" + content.replaceAll(" ", "&nbsp;") + "</td>";
          }
        }
        result = result + "<td><b>" + getSummaryTitle(i) + "</b> = " + removeFilterName(m_ColNames[i]) + "</td></tr>\n";
      }
    }
    result = result + "</table>\n";
    
    return result;
  }
  









  public String toStringRanking()
  {
    if (m_RankingWins == null) {
      return "-ranking data not set-";
    }
    int biggest = Math.max(m_RankingWins[Utils.maxIndex(m_RankingWins)], m_RankingLosses[Utils.maxIndex(m_RankingLosses)]);
    
    int width = Math.max(2 + (int)(Math.log(biggest) / Math.log(10.0D)), ">-<".length());
    
    String result = "<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n";
    result = result + "   <tr><td align=\"center\"><b>&gt;-&lt;</b></td><td align=\"center\"><b>&gt;</b></td><td align=\"center\"><b>&lt;</b></td><td><b>Resultset</b></td></tr>\n";
    





    int[] ranking = Utils.sort(m_RankingDiff);
    
    for (int i = getColCount() - 1; i >= 0; i--) {
      int curr = ranking[i];
      
      if (!getColHidden(curr))
      {

        result = result + "   <tr><td align=\"right\">" + m_RankingDiff[curr] + "</td>" + "<td align=\"right\">" + m_RankingWins[curr] + "</td>" + "<td align=\"right\">" + m_RankingLosses[curr] + "</td>" + "<td>" + removeFilterName(m_ColNames[curr]) + "</td>" + "<tr>\n";
      }
    }
    




    result = result + "</table>\n";
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  






  public static void main(String[] args)
  {
    ResultMatrix matrix = new ResultMatrixHTML(3, 3);
    

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

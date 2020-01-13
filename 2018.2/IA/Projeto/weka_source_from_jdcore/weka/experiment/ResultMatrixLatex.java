package weka.experiment;

import java.io.PrintStream;
import weka.core.RevisionUtils;
import weka.core.Utils;

































public class ResultMatrixLatex
  extends ResultMatrix
{
  private static final long serialVersionUID = 777690788447600978L;
  
  public ResultMatrixLatex()
  {
    this(1, 1);
  }
  


  public ResultMatrixLatex(int cols, int rows)
  {
    super(cols, rows);
  }
  



  public ResultMatrixLatex(ResultMatrix matrix)
  {
    super(matrix);
  }
  


  public String getDisplayName()
  {
    return "LaTeX";
  }
  


  public void clear()
  {
    super.clear();
    setPrintColNames(false);
    setEnumerateColNames(true);
    TIE_STRING = " ";
    WIN_STRING = "$\\circ$";
    LOSS_STRING = "$\\bullet$";
  }
  




  public String toStringHeader()
  {
    return new ResultMatrixPlainText(this).toStringHeader();
  }
  









  public String toStringMatrix()
  {
    StringBuffer result = new StringBuffer();
    String[][] cells = toArray();
    
    result.append("\\begin{table}[thb]\n\\caption{\\label{labelname}Table Caption}\n");
    
    if (!getShowStdDev()) {
      result.append("\\footnotesize\n");
    } else {
      result.append("\\scriptsize\n");
    }
    

    if (!getShowStdDev()) {
      result.append("{\\centering \\begin{tabular}{lr");


    }
    else
    {

      result.append("{\\centering \\begin{tabular}{lr@{\\hspace{0cm}}c@{\\hspace{0cm}}r");
    }
    








    for (int j = 1; j < getColCount(); j++) {
      if (!getColHidden(j))
      {
        if (!getShowStdDev()) {
          result.append("r@{\\hspace{0.1cm}}c");

        }
        else
        {
          result.append("r@{\\hspace{0cm}}c@{\\hspace{0cm}}r@{\\hspace{0.1cm}}c");
        }
      }
    }
    




    result.append("}\n\\\\\n\\hline\n");
    if (!getShowStdDev()) {
      result.append("Dataset & " + cells[0][1]);
    } else {
      result.append("Dataset & \\multicolumn{3}{c}{" + cells[0][1] + "}");
    }
    
    for (j = 2; j < cells[0].length; j++) {
      if (isMean(j))
      {
        if (!getShowStdDev()) {
          result.append("& " + cells[0][j] + " & ");
        } else
          result.append("& \\multicolumn{4}{c}{" + cells[0][j] + "} "); }
    }
    result.append("\\\\\n\\hline\n");
    

    for (int i = 1; i < cells.length; i++) {
      cells[i][0] = cells[i][0].replace('_', '-');
    }
    
    for (int n = 1; n < cells[0].length; n++) {
      int size = getColSize(cells, n);
      for (i = 1; i < cells.length; i++) {
        cells[i][n] = padString(cells[i][n], size, true);
      }
    }
    
    for (i = 1; i < cells.length - 1; i++) {
      if (isAverage(i))
        result.append("\\hline\n");
      for (n = 0; n < cells[0].length; n++) {
        if (n == 0) {
          result.append(padString(cells[i][n], getRowNameWidth()));
        }
        else {
          if (getShowStdDev()) {
            if (isMean(n - 1)) {
              if (!cells[i][n].trim().equals("")) {
                result.append(" & $\\pm$ & ");
              } else {
                result.append(" &       & ");
              }
            } else {
              result.append(" & ");
            }
          } else {
            result.append(" & ");
          }
          result.append(cells[i][n]);
        }
      }
      
      result.append("\\\\\n");
    }
    
    result.append("\\hline\n\\multicolumn{" + cells[0].length + "}{c}{$\\circ$, $\\bullet$" + " statistically significant improvement or degradation}" + "\\\\\n\\end{tabular} ");
    

    if (!getShowStdDev()) {
      result.append("\\footnotesize ");
    } else {
      result.append("\\scriptsize ");
    }
    result.append("\\par}\n\\end{table}\n");
    

    return result.toString();
  }
  






  public String toStringKey()
  {
    String result = "\\begin{table}[thb]\n\\caption{\\label{labelname}Table Caption (Key)}\n";
    
    result = result + "\\scriptsize\n";
    result = result + "{\\centering\n";
    result = result + "\\begin{tabular}{cl}\\\\\n";
    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        result = result + LEFT_PARENTHESES + (i + 1) + RIGHT_PARENTHESES + " & " + removeFilterName(m_ColNames[i]).replace('_', '-').replaceAll("\\\\", "\\\\textbackslash") + " \\\\\n";
      }
    }
    

    result = result + "\\end{tabular}\n";
    result = result + "}\n";
    result = result + "\\end{table}\n";
    
    return result;
  }
  








  public String toStringSummary()
  {
    if (m_NonSigWins == null) {
      return "-summary data not set-";
    }
    int resultsetLength = 1 + Math.max((int)(Math.log(getColCount()) / Math.log(10.0D)), (int)(Math.log(getRowCount()) / Math.log(10.0D)));
    
    String result = "";
    String titles = "";
    
    result = result + "{\\centering\n";
    result = result + "\\begin{table}[thb]\n\\caption{\\label{labelname}Table Caption}\n";
    
    result = result + "\\footnotesize\n";
    result = result + "\\begin{tabular}{l";
    
    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        titles = titles + " &";
        result = result + "c";
        titles = titles + ' ' + Utils.padLeft(new StringBuilder().append("").append(getSummaryTitle(i)).toString(), resultsetLength * 2 + 3);
      }
    }
    result = result + "}\\\\\n\\hline\n";
    result = result + titles + " \\\\\n\\hline\n";
    
    for (i = 0; i < getColCount(); i++) {
      if (!getColHidden(i))
      {

        for (int j = 0; j < getColCount(); j++)
          if (!getColHidden(j))
          {

            if (j == 0) {
              result = result + (char)(97 + i % 26);
            }
            if (j == i) {
              result = result + " & - ";
            } else
              result = result + "& " + m_NonSigWins[i][j] + " (" + m_Wins[i][j] + ") ";
          }
        result = result + "\\\\\n";
      }
    }
    result = result + "\\hline\n\\end{tabular} \\footnotesize \\par\n\\end{table}}";
    
    return result;
  }
  









  public String toStringRanking()
  {
    if (m_RankingWins == null) {
      return "-ranking data not set-";
    }
    int biggest = Math.max(m_RankingWins[Utils.maxIndex(m_RankingWins)], m_RankingLosses[Utils.maxIndex(m_RankingLosses)]);
    
    int width = Math.max(2 + (int)(Math.log(biggest) / Math.log(10.0D)), ">-<".length());
    
    String result = "\\begin{table}[thb]\n\\caption{\\label{labelname}Table Caption}\n\\footnotesize\n{\\centering \\begin{tabular}{rlll}\\\\\n\\hline\n";
    
    result = result + "Resultset & Wins$-$ & Wins & Losses \\\\\n& Losses & & \\\\\n\\hline\n";
    

    int[] ranking = Utils.sort(m_RankingDiff);
    for (int i = getColCount() - 1; i >= 0; i--) {
      int curr = ranking[i];
      
      if (!getColHidden(curr))
      {

        result = result + "(" + (curr + 1) + ") & " + Utils.padLeft(new StringBuilder().append("").append(m_RankingDiff[curr]).toString(), width) + " & " + Utils.padLeft(new StringBuilder().append("").append(m_RankingWins[curr]).toString(), width) + " & " + Utils.padLeft(new StringBuilder().append("").append(m_RankingLosses[curr]).toString(), width) + "\\\\\n";
      }
    }
    



    result = result + "\\hline\n\\end{tabular} \\footnotesize \\par}\n\\end{table}";
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
  






  public static void main(String[] args)
  {
    ResultMatrix matrix = new ResultMatrixLatex(3, 3);
    

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

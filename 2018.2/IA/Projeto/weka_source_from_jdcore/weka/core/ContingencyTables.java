package weka.core;

import java.io.PrintStream;




























public class ContingencyTables
  implements RevisionHandler
{
  private static double log2 = Math.log(2.0D);
  



  public ContingencyTables() {}
  



  public static double chiSquared(double[][] matrix, boolean yates)
  {
    int df = (matrix.length - 1) * (matrix[0].length - 1);
    
    return Statistics.chiSquaredProbability(chiVal(matrix, yates), df);
  }
  









  public static double chiVal(double[][] matrix, boolean useYates)
  {
    double expect = 0.0D;double chival = 0.0D;double n = 0.0D;
    boolean yates = true;
    
    int nrows = matrix.length;
    int ncols = matrix[0].length;
    double[] rtotal = new double[nrows];
    double[] ctotal = new double[ncols];
    for (int row = 0; row < nrows; row++) {
      for (int col = 0; col < ncols; col++) {
        rtotal[row] += matrix[row][col];
        ctotal[col] += matrix[row][col];
        n += matrix[row][col];
      }
    }
    int df = (nrows - 1) * (ncols - 1);
    if ((df > 1) || (!useYates)) {
      yates = false;
    } else if (df <= 0) {
      return 0.0D;
    }
    chival = 0.0D;
    for (row = 0; row < nrows; row++) {
      if (Utils.gr(rtotal[row], 0.0D)) {
        for (int col = 0; col < ncols; col++) {
          if (Utils.gr(ctotal[col], 0.0D)) {
            expect = ctotal[col] * rtotal[row] / n;
            chival += chiCell(matrix[row][col], expect, yates);
          }
        }
      }
    }
    return chival;
  }
  









  public static boolean cochransCriterion(double[][] matrix)
  {
    double n = 0.0D;double smallfreq = 5.0D;
    int smallcount = 0;int nonZeroRows = 0;int nonZeroColumns = 0;
    

    int nrows = matrix.length;
    int ncols = matrix[0].length;
    
    double[] rtotal = new double[nrows];
    double[] ctotal = new double[ncols];
    for (int row = 0; row < nrows; row++) {
      for (int col = 0; col < ncols; col++) {
        rtotal[row] += matrix[row][col];
        ctotal[col] += matrix[row][col];
        n += matrix[row][col];
      }
    }
    for (row = 0; row < nrows; row++) {
      if (Utils.gr(rtotal[row], 0.0D)) {
        nonZeroRows++;
      }
    }
    for (int col = 0; col < ncols; col++) {
      if (Utils.gr(ctotal[col], 0.0D)) {
        nonZeroColumns++;
      }
    }
    for (row = 0; row < nrows; row++) {
      if (Utils.gr(rtotal[row], 0.0D)) {
        for (col = 0; col < ncols; col++) {
          if (Utils.gr(ctotal[col], 0.0D)) {
            double expect = ctotal[col] * rtotal[row] / n;
            if (Utils.sm(expect, smallfreq)) {
              if (Utils.sm(expect, 1.0D)) {
                return false;
              }
              smallcount++;
              if (smallcount > nonZeroRows * nonZeroColumns / smallfreq) {
                return false;
              }
            }
          }
        }
      }
    }
    
    return true;
  }
  







  public static double CramersV(double[][] matrix)
  {
    double n = 0.0D;
    
    int nrows = matrix.length;
    int ncols = matrix[0].length;
    for (int row = 0; row < nrows; row++) {
      for (int col = 0; col < ncols; col++) {
        n += matrix[row][col];
      }
    }
    int min = nrows < ncols ? nrows - 1 : ncols - 1;
    if ((min == 0) || (Utils.eq(n, 0.0D)))
      return 0.0D;
    return Math.sqrt(chiVal(matrix, false) / (n * min));
  }
  






  public static double entropy(double[] array)
  {
    double returnValue = 0.0D;double sum = 0.0D;
    
    for (int i = 0; i < array.length; i++) {
      returnValue -= lnFunc(array[i]);
      sum += array[i];
    }
    if (Utils.eq(sum, 0.0D)) {
      return 0.0D;
    }
    return (returnValue + lnFunc(sum)) / (sum * log2);
  }
  








  public static double entropyConditionedOnColumns(double[][] matrix)
  {
    double returnValue = 0.0D;double total = 0.0D;
    
    for (int j = 0; j < matrix[0].length; j++) {
      double sumForColumn = 0.0D;
      for (int i = 0; i < matrix.length; i++) {
        returnValue += lnFunc(matrix[i][j]);
        sumForColumn += matrix[i][j];
      }
      returnValue -= lnFunc(sumForColumn);
      total += sumForColumn;
    }
    if (Utils.eq(total, 0.0D)) {
      return 0.0D;
    }
    return -returnValue / (total * log2);
  }
  







  public static double entropyConditionedOnRows(double[][] matrix)
  {
    double returnValue = 0.0D;double total = 0.0D;
    
    for (int i = 0; i < matrix.length; i++) {
      double sumForRow = 0.0D;
      for (int j = 0; j < matrix[0].length; j++) {
        returnValue += lnFunc(matrix[i][j]);
        sumForRow += matrix[i][j];
      }
      returnValue -= lnFunc(sumForRow);
      total += sumForRow;
    }
    if (Utils.eq(total, 0.0D)) {
      return 0.0D;
    }
    return -returnValue / (total * log2);
  }
  












  public static double entropyConditionedOnRows(double[][] train, double[][] test, double numClasses)
  {
    double returnValue = 0.0D;double testSum = 0.0D;
    
    for (int i = 0; i < test.length; i++) {
      double trainSumForRow = 0.0D;
      double testSumForRow = 0.0D;
      for (int j = 0; j < test[0].length; j++) {
        returnValue -= test[i][j] * Math.log(train[i][j] + 1.0D);
        trainSumForRow += train[i][j];
        testSumForRow += test[i][j];
      }
      testSum = testSumForRow;
      returnValue += testSumForRow * Math.log(trainSumForRow + numClasses);
    }
    
    return returnValue / (testSum * log2);
  }
  






  public static double entropyOverRows(double[][] matrix)
  {
    double returnValue = 0.0D;double total = 0.0D;
    
    for (int i = 0; i < matrix.length; i++) {
      double sumForRow = 0.0D;
      for (int j = 0; j < matrix[0].length; j++) {
        sumForRow += matrix[i][j];
      }
      returnValue -= lnFunc(sumForRow);
      total += sumForRow;
    }
    if (Utils.eq(total, 0.0D)) {
      return 0.0D;
    }
    return (returnValue + lnFunc(total)) / (total * log2);
  }
  






  public static double entropyOverColumns(double[][] matrix)
  {
    double returnValue = 0.0D;double total = 0.0D;
    
    for (int j = 0; j < matrix[0].length; j++) {
      double sumForColumn = 0.0D;
      for (int i = 0; i < matrix.length; i++) {
        sumForColumn += matrix[i][j];
      }
      returnValue -= lnFunc(sumForColumn);
      total += sumForColumn;
    }
    if (Utils.eq(total, 0.0D)) {
      return 0.0D;
    }
    return (returnValue + lnFunc(total)) / (total * log2);
  }
  







  public static double gainRatio(double[][] matrix)
  {
    double preSplit = 0.0D;double postSplit = 0.0D;double splitEnt = 0.0D;
    double total = 0.0D;
    

    for (int i = 0; i < matrix[0].length; i++) {
      double sumForColumn = 0.0D;
      for (int j = 0; j < matrix.length; j++)
        sumForColumn += matrix[j][i];
      preSplit += lnFunc(sumForColumn);
      total += sumForColumn;
    }
    preSplit -= lnFunc(total);
    

    for (int i = 0; i < matrix.length; i++) {
      double sumForRow = 0.0D;
      for (int j = 0; j < matrix[0].length; j++) {
        postSplit += lnFunc(matrix[i][j]);
        sumForRow += matrix[i][j];
      }
      splitEnt += lnFunc(sumForRow);
    }
    postSplit -= splitEnt;
    splitEnt -= lnFunc(total);
    
    double infoGain = preSplit - postSplit;
    if (Utils.eq(splitEnt, 0.0D))
      return 0.0D;
    return infoGain / splitEnt;
  }
  







  public static double log2MultipleHypergeometric(double[][] matrix)
  {
    double sum = 0.0D;double total = 0.0D;
    
    for (int i = 0; i < matrix.length; i++) {
      double sumForRow = 0.0D;
      for (int j = 0; j < matrix[i].length; j++) {
        sumForRow += matrix[i][j];
      }
      sum += SpecialFunctions.lnFactorial(sumForRow);
      total += sumForRow;
    }
    for (int j = 0; j < matrix[0].length; j++) {
      double sumForColumn = 0.0D;
      for (int i = 0; i < matrix.length; i++) {
        sumForColumn += matrix[i][j];
      }
      sum += SpecialFunctions.lnFactorial(sumForColumn);
    }
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        sum -= SpecialFunctions.lnFactorial(matrix[i][j]);
      }
    }
    sum -= SpecialFunctions.lnFactorial(total);
    return -sum / log2;
  }
  







  public static double[][] reduceMatrix(double[][] matrix)
  {
    int nonZeroRows = 0;int nonZeroColumns = 0;
    


    int nrows = matrix.length;
    int ncols = matrix[0].length;
    double[] rtotal = new double[nrows];
    double[] ctotal = new double[ncols];
    for (int row = 0; row < nrows; row++) {
      for (int col = 0; col < ncols; col++) {
        rtotal[row] += matrix[row][col];
        ctotal[col] += matrix[row][col];
      }
    }
    for (row = 0; row < nrows; row++) {
      if (Utils.gr(rtotal[row], 0.0D)) {
        nonZeroRows++;
      }
    }
    for (int col = 0; col < ncols; col++) {
      if (Utils.gr(ctotal[col], 0.0D)) {
        nonZeroColumns++;
      }
    }
    double[][] newMatrix = new double[nonZeroRows][nonZeroColumns];
    int currRow = 0;
    for (row = 0; row < nrows; row++) {
      if (Utils.gr(rtotal[row], 0.0D)) {
        int currCol = 0;
        for (col = 0; col < ncols; col++) {
          if (Utils.gr(ctotal[col], 0.0D)) {
            newMatrix[currRow][currCol] = matrix[row][col];
            currCol++;
          }
        }
        currRow++;
      }
    }
    return newMatrix;
  }
  







  public static double symmetricalUncertainty(double[][] matrix)
  {
    double total = 0.0D;double columnEntropy = 0.0D;
    double rowEntropy = 0.0D;double entropyConditionedOnRows = 0.0D;double infoGain = 0.0D;
    

    for (int i = 0; i < matrix[0].length; i++) {
      double sumForColumn = 0.0D;
      for (int j = 0; j < matrix.length; j++) {
        sumForColumn += matrix[j][i];
      }
      columnEntropy += lnFunc(sumForColumn);
      total += sumForColumn;
    }
    columnEntropy -= lnFunc(total);
    

    for (int i = 0; i < matrix.length; i++) {
      double sumForRow = 0.0D;
      for (int j = 0; j < matrix[0].length; j++) {
        sumForRow += matrix[i][j];
        entropyConditionedOnRows += lnFunc(matrix[i][j]);
      }
      rowEntropy += lnFunc(sumForRow);
    }
    entropyConditionedOnRows -= rowEntropy;
    rowEntropy -= lnFunc(total);
    infoGain = columnEntropy - entropyConditionedOnRows;
    if ((Utils.eq(columnEntropy, 0.0D)) || (Utils.eq(rowEntropy, 0.0D)))
      return 0.0D;
    return 2.0D * (infoGain / (columnEntropy + rowEntropy));
  }
  








  public static double tauVal(double[][] matrix)
  {
    double maxcol = 0.0D;double maxtotal = 0.0D;double n = 0.0D;
    
    int nrows = matrix.length;
    int ncols = matrix[0].length;
    double[] ctotal = new double[ncols];
    for (int row = 0; row < nrows; row++) {
      double max = 0.0D;
      for (int col = 0; col < ncols; col++) {
        if (Utils.gr(matrix[row][col], max))
          max = matrix[row][col];
        ctotal[col] += matrix[row][col];
        n += matrix[row][col];
      }
      maxtotal += max;
    }
    if (Utils.eq(n, 0.0D)) {
      return 0.0D;
    }
    maxcol = ctotal[Utils.maxIndex(ctotal)];
    return (maxtotal - maxcol) / (n - maxcol);
  }
  



  private static double lnFunc(double num)
  {
    if (num <= 0.0D) {
      return 0.0D;
    }
    return num * Math.log(num);
  }
  











  private static double chiCell(double freq, double expected, boolean yates)
  {
    if (Utils.smOrEq(expected, 0.0D)) {
      return 0.0D;
    }
    

    double diff = Math.abs(freq - expected);
    if (yates)
    {

      diff -= 0.5D;
      

      if (diff < 0.0D) {
        diff = 0.0D;
      }
    }
    

    return diff * diff / expected;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8923 $");
  }
  



  public static void main(String[] ops)
  {
    double[] firstRow = { 10.0D, 5.0D, 20.0D };
    double[] secondRow = { 2.0D, 10.0D, 6.0D };
    double[] thirdRow = { 5.0D, 10.0D, 10.0D };
    double[][] matrix = new double[3][0];
    
    matrix[0] = firstRow;matrix[1] = secondRow;matrix[2] = thirdRow;
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        System.out.print(matrix[i][j] + " ");
      }
      System.out.println();
    }
    System.out.println("Chi-squared probability: " + chiSquared(matrix, false));
    
    System.out.println("Chi-squared value: " + chiVal(matrix, false));
    
    System.out.println("Cochran's criterion fullfilled: " + cochransCriterion(matrix));
    
    System.out.println("Cramer's V: " + CramersV(matrix));
    
    System.out.println("Entropy of first row: " + entropy(firstRow));
    
    System.out.println("Entropy conditioned on columns: " + entropyConditionedOnColumns(matrix));
    
    System.out.println("Entropy conditioned on rows: " + entropyConditionedOnRows(matrix));
    
    System.out.println("Entropy conditioned on rows (with Laplace): " + entropyConditionedOnRows(matrix, matrix, 3.0D));
    
    System.out.println("Entropy of rows: " + entropyOverRows(matrix));
    
    System.out.println("Entropy of columns: " + entropyOverColumns(matrix));
    
    System.out.println("Gain ratio: " + gainRatio(matrix));
    
    System.out.println("Negative log2 of multiple hypergeometric probability: " + log2MultipleHypergeometric(matrix));
    
    System.out.println("Symmetrical uncertainty: " + symmetricalUncertainty(matrix));
    
    System.out.println("Tau value: " + tauVal(matrix));
    
    double[][] newMatrix = new double[3][3];
    newMatrix[0][0] = 1.0D;newMatrix[0][1] = 0.0D;newMatrix[0][2] = 1.0D;
    newMatrix[1][0] = 0.0D;newMatrix[1][1] = 0.0D;newMatrix[1][2] = 0.0D;
    newMatrix[2][0] = 1.0D;newMatrix[2][1] = 0.0D;newMatrix[2][2] = 1.0D;
    System.out.println("Matrix with empty row and column: ");
    for (int i = 0; i < newMatrix.length; i++) {
      for (int j = 0; j < newMatrix[i].length; j++) {
        System.out.print(newMatrix[i][j] + " ");
      }
      System.out.println();
    }
    System.out.println("Reduced matrix: ");
    newMatrix = reduceMatrix(newMatrix);
    for (int i = 0; i < newMatrix.length; i++) {
      for (int j = 0; j < newMatrix[i].length; j++) {
        System.out.print(newMatrix[i][j] + " ");
      }
      System.out.println();
    }
  }
}

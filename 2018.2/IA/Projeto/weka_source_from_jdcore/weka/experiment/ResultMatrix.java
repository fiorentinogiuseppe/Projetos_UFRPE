package weka.experiment;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.RevisionHandler;
import weka.core.Utils;





















































public abstract class ResultMatrix
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 4487179306428209739L;
  public static final int SIGNIFICANCE_TIE = 0;
  public static final int SIGNIFICANCE_WIN = 1;
  public static final int SIGNIFICANCE_LOSS = 2;
  public String TIE_STRING = " ";
  

  public String WIN_STRING = "v";
  

  public String LOSS_STRING = "*";
  

  public String LEFT_PARENTHESES = "(";
  

  public String RIGHT_PARENTHESES = ")";
  

  protected String[] m_ColNames = null;
  

  protected String[] m_RowNames = null;
  

  protected boolean[] m_ColHidden = null;
  

  protected boolean[] m_RowHidden = null;
  

  protected int[][] m_Significance = (int[][])null;
  

  protected double[][] m_Mean = (double[][])null;
  

  protected double[][] m_StdDev = (double[][])null;
  

  protected double[] m_Counts = null;
  


  protected int m_MeanPrec;
  

  protected int m_StdDevPrec;
  

  protected boolean m_ShowStdDev;
  

  protected boolean m_ShowAverage;
  

  protected boolean m_PrintColNames;
  

  protected boolean m_PrintRowNames;
  

  protected boolean m_EnumerateColNames;
  

  protected boolean m_EnumerateRowNames;
  

  protected int m_ColNameWidth;
  

  protected int m_RowNameWidth;
  

  protected int m_MeanWidth;
  

  protected int m_StdDevWidth;
  

  protected int m_SignificanceWidth;
  

  protected int m_CountWidth;
  

  protected Vector m_HeaderKeys = null;
  

  protected Vector m_HeaderValues = null;
  

  protected int[][] m_NonSigWins = (int[][])null;
  

  protected int[][] m_Wins = (int[][])null;
  

  protected int[] m_RankingWins = null;
  

  protected int[] m_RankingLosses = null;
  

  protected int[] m_RankingDiff = null;
  

  protected int[] m_RowOrder = null;
  

  protected int[] m_ColOrder = null;
  

  protected boolean m_RemoveFilterName = false;
  


  public ResultMatrix()
  {
    this(1, 1);
  }
  


  public ResultMatrix(int cols, int rows)
  {
    setSize(cols, rows);
    clear();
  }
  



  public ResultMatrix(ResultMatrix matrix)
  {
    assign(matrix);
  }
  




  public abstract String getDisplayName();
  




  public void assign(ResultMatrix matrix)
  {
    setSize(matrix.getColCount(), matrix.getRowCount());
    

    TIE_STRING = TIE_STRING;
    WIN_STRING = WIN_STRING;
    LOSS_STRING = LOSS_STRING;
    LEFT_PARENTHESES = LEFT_PARENTHESES;
    RIGHT_PARENTHESES = RIGHT_PARENTHESES;
    m_MeanPrec = m_MeanPrec;
    m_StdDevPrec = m_StdDevPrec;
    m_ShowStdDev = m_ShowStdDev;
    m_ShowAverage = m_ShowAverage;
    m_PrintColNames = m_PrintColNames;
    m_PrintRowNames = m_PrintRowNames;
    m_EnumerateColNames = m_EnumerateColNames;
    m_EnumerateRowNames = m_EnumerateRowNames;
    m_RowNameWidth = m_RowNameWidth;
    m_MeanWidth = m_MeanWidth;
    m_StdDevWidth = m_StdDevWidth;
    m_SignificanceWidth = m_SignificanceWidth;
    m_CountWidth = m_CountWidth;
    m_RemoveFilterName = m_RemoveFilterName;
    

    m_HeaderKeys = ((Vector)m_HeaderKeys.clone());
    m_HeaderValues = ((Vector)m_HeaderValues.clone());
    

    for (int i = 0; i < m_Mean.length; i++) {
      for (int n = 0; n < m_Mean[i].length; n++) {
        m_Mean[i][n] = m_Mean[i][n];
        m_StdDev[i][n] = m_StdDev[i][n];
        m_Significance[i][n] = m_Significance[i][n];
      }
    }
    
    for (i = 0; i < m_ColNames.length; i++) {
      m_ColNames[i] = m_ColNames[i];
      m_ColHidden[i] = m_ColHidden[i];
    }
    
    for (i = 0; i < m_RowNames.length; i++) {
      m_RowNames[i] = m_RowNames[i];
      m_RowHidden[i] = m_RowHidden[i];
    }
    
    for (i = 0; i < m_Counts.length; i++) {
      m_Counts[i] = m_Counts[i];
    }
    
    if (m_NonSigWins != null) {
      m_NonSigWins = new int[m_NonSigWins.length][];
      m_Wins = new int[m_NonSigWins.length][];
      for (i = 0; i < m_NonSigWins.length; i++) {
        m_NonSigWins[i] = new int[m_NonSigWins[i].length];
        m_Wins[i] = new int[m_NonSigWins[i].length];
        
        for (int n = 0; n < m_NonSigWins[i].length; n++) {
          m_NonSigWins[i][n] = m_NonSigWins[i][n];
          m_Wins[i][n] = m_Wins[i][n];
        }
      }
    }
    

    if (m_RankingWins != null) {
      m_RankingWins = new int[m_RankingWins.length];
      m_RankingLosses = new int[m_RankingWins.length];
      m_RankingDiff = new int[m_RankingWins.length];
      for (i = 0; i < m_RankingWins.length; i++) {
        m_RankingWins[i] = m_RankingWins[i];
        m_RankingLosses[i] = m_RankingLosses[i];
        m_RankingDiff[i] = m_RankingDiff[i];
      }
    }
  }
  



  public void clear()
  {
    m_MeanPrec = 2;
    m_StdDevPrec = 2;
    m_ShowStdDev = false;
    m_ShowAverage = false;
    m_PrintColNames = true;
    m_PrintRowNames = true;
    m_EnumerateColNames = true;
    m_EnumerateRowNames = false;
    m_RowNameWidth = 0;
    m_ColNameWidth = 0;
    m_MeanWidth = 0;
    m_StdDevWidth = 0;
    m_SignificanceWidth = 0;
    m_CountWidth = 0;
    
    setSize(getColCount(), getRowCount());
  }
  







  public void setSize(int cols, int rows)
  {
    m_ColNames = new String[cols];
    m_RowNames = new String[rows];
    m_Counts = new double[rows];
    m_ColHidden = new boolean[cols];
    m_RowHidden = new boolean[rows];
    m_Mean = new double[rows][cols];
    m_Significance = new int[rows][cols];
    m_StdDev = new double[rows][cols];
    m_ColOrder = null;
    m_RowOrder = null;
    

    for (int i = 0; i < m_Mean.length; i++) {
      for (int n = 0; n < m_Mean[i].length; n++) {
        m_Mean[i][n] = NaN.0D;
      }
    }
    for (i = 0; i < m_ColNames.length; i++)
      m_ColNames[i] = ("col" + i);
    for (i = 0; i < m_RowNames.length; i++) {
      m_RowNames[i] = ("row" + i);
    }
    clearHeader();
    clearSummary();
    clearRanking();
  }
  


  public void setMeanPrec(int prec)
  {
    if (prec >= 0) {
      m_MeanPrec = prec;
    }
  }
  

  public int getMeanPrec()
  {
    return m_MeanPrec;
  }
  


  public void setStdDevPrec(int prec)
  {
    if (prec >= 0) {
      m_StdDevPrec = prec;
    }
  }
  

  public int getStdDevPrec()
  {
    return m_StdDevPrec;
  }
  


  public void setColNameWidth(int width)
  {
    if (width >= 0) {
      m_ColNameWidth = width;
    }
  }
  

  public int getColNameWidth()
  {
    return m_ColNameWidth;
  }
  


  public void setRowNameWidth(int width)
  {
    if (width >= 0) {
      m_RowNameWidth = width;
    }
  }
  

  public int getRowNameWidth()
  {
    return m_RowNameWidth;
  }
  


  public void setMeanWidth(int width)
  {
    if (width >= 0) {
      m_MeanWidth = width;
    }
  }
  

  public int getMeanWidth()
  {
    return m_MeanWidth;
  }
  


  public void setStdDevWidth(int width)
  {
    if (width >= 0) {
      m_StdDevWidth = width;
    }
  }
  

  public int getStdDevWidth()
  {
    return m_StdDevWidth;
  }
  


  public void setSignificanceWidth(int width)
  {
    if (width >= 0) {
      m_SignificanceWidth = width;
    }
  }
  

  public int getSignificanceWidth()
  {
    return m_SignificanceWidth;
  }
  


  public void setCountWidth(int width)
  {
    if (width >= 0) {
      m_CountWidth = width;
    }
  }
  

  public int getCountWidth()
  {
    return m_CountWidth;
  }
  


  public void setShowStdDev(boolean show)
  {
    m_ShowStdDev = show;
  }
  


  public boolean getShowStdDev()
  {
    return m_ShowStdDev;
  }
  


  public void setShowAverage(boolean show)
  {
    m_ShowAverage = show;
  }
  


  public boolean getShowAverage()
  {
    return m_ShowAverage;
  }
  


  public void setRemoveFilterName(boolean remove)
  {
    m_RemoveFilterName = remove;
  }
  


  public boolean getRemoveFilterName()
  {
    return m_RemoveFilterName;
  }
  




  public void setPrintColNames(boolean print)
  {
    m_PrintColNames = print;
    if (!print) {
      setEnumerateColNames(true);
    }
  }
  

  public boolean getPrintColNames()
  {
    return m_PrintColNames;
  }
  




  public void setPrintRowNames(boolean print)
  {
    m_PrintRowNames = print;
    if (!print) {
      setEnumerateRowNames(true);
    }
  }
  

  public boolean getPrintRowNames()
  {
    return m_PrintRowNames;
  }
  



  public void setEnumerateColNames(boolean enumerate)
  {
    m_EnumerateColNames = enumerate;
  }
  


  public boolean getEnumerateColNames()
  {
    return m_EnumerateColNames;
  }
  


  public void setEnumerateRowNames(boolean enumerate)
  {
    m_EnumerateRowNames = enumerate;
  }
  


  public boolean getEnumerateRowNames()
  {
    return m_EnumerateRowNames;
  }
  


  public int getColCount()
  {
    return m_ColNames.length;
  }
  





  public int getVisibleColCount()
  {
    int cols = 0;
    for (int i = 0; i < getColCount(); i++) {
      if (!getColHidden(i)) {
        cols++;
      }
    }
    return cols;
  }
  


  public int getRowCount()
  {
    return m_RowNames.length;
  }
  





  public int getVisibleRowCount()
  {
    int rows = 0;
    for (int i = 0; i < getRowCount(); i++) {
      if (!getRowHidden(i)) {
        rows++;
      }
    }
    return rows;
  }
  




  public void setColName(int index, String name)
  {
    if ((index >= 0) && (index < getColCount())) {
      m_ColNames[index] = name;
    }
  }
  










  public String getColName(int index)
  {
    String result = null;
    
    if ((index >= 0) && (index < getColCount())) {
      if (getPrintColNames()) {
        result = m_ColNames[index];
      } else {
        result = "";
      }
      if (getEnumerateColNames()) {
        result = LEFT_PARENTHESES + Integer.toString(index + 1) + RIGHT_PARENTHESES + " " + result;
        


        result = result.trim();
      }
    }
    
    return result;
  }
  




  public void setRowName(int index, String name)
  {
    if ((index >= 0) && (index < getRowCount())) {
      m_RowNames[index] = name;
    }
  }
  










  public String getRowName(int index)
  {
    String result = null;
    
    if ((index >= 0) && (index < getRowCount())) {
      if (getPrintRowNames()) {
        result = m_RowNames[index];
      } else {
        result = "";
      }
      if (getEnumerateRowNames()) {
        result = LEFT_PARENTHESES + Integer.toString(index + 1) + RIGHT_PARENTHESES + " " + result;
        


        result = result.trim();
      }
    }
    
    return result;
  }
  




  public void setColHidden(int index, boolean hidden)
  {
    if ((index >= 0) && (index < getColCount())) {
      m_ColHidden[index] = hidden;
    }
  }
  


  public boolean getColHidden(int index)
  {
    if ((index >= 0) && (index < getColCount())) {
      return m_ColHidden[index];
    }
    return false;
  }
  




  public void setRowHidden(int index, boolean hidden)
  {
    if ((index >= 0) && (index < getRowCount())) {
      m_RowHidden[index] = hidden;
    }
  }
  


  public boolean getRowHidden(int index)
  {
    if ((index >= 0) && (index < getRowCount())) {
      return m_RowHidden[index];
    }
    return false;
  }
  




  public void setCount(int index, double count)
  {
    if ((index >= 0) && (index < getRowCount())) {
      m_Counts[index] = count;
    }
  }
  



  public double getCount(int index)
  {
    if ((index >= 0) && (index < getRowCount())) {
      return m_Counts[index];
    }
    return 0.0D;
  }
  





  public void setMean(int col, int row, double value)
  {
    if ((col >= 0) && (col < getColCount()) && (row >= 0) && (row < getRowCount()))
    {
      m_Mean[row][col] = value;
    }
  }
  


  public double getMean(int col, int row)
  {
    if ((col >= 0) && (col < getColCount()) && (row >= 0) && (row < getRowCount()))
    {
      return m_Mean[row][col];
    }
    return 0.0D;
  }
  







  public double getAverage(int col)
  {
    if ((col >= 0) && (col < getColCount())) {
      double avg = 0.0D;
      int count = 0;
      
      for (int i = 0; i < getRowCount(); i++) {
        if (!Double.isNaN(getMean(col, i))) {
          avg += getMean(col, i);
          count++;
        }
      }
      
      return avg / count;
    }
    
    return 0.0D;
  }
  






  public void setStdDev(int col, int row, double value)
  {
    if ((col >= 0) && (col < getColCount()) && (row >= 0) && (row < getRowCount()))
    {
      m_StdDev[row][col] = value;
    }
  }
  


  public double getStdDev(int col, int row)
  {
    if ((col >= 0) && (col < getColCount()) && (row >= 0) && (row < getRowCount()))
    {
      return m_StdDev[row][col];
    }
    return 0.0D;
  }
  





  public void setSignificance(int col, int row, int value)
  {
    if ((col >= 0) && (col < getColCount()) && (row >= 0) && (row < getRowCount()))
    {
      m_Significance[row][col] = value;
    }
  }
  


  public int getSignificance(int col, int row)
  {
    if ((col >= 0) && (col < getColCount()) && (row >= 0) && (row < getRowCount()))
    {
      return m_Significance[row][col];
    }
    return 0;
  }
  








  public int getSignificanceCount(int col, int type)
  {
    int result = 0;
    
    if ((col >= 0) && (col < getColCount())) {
      for (int i = 0; i < getRowCount(); i++) {
        if (!getRowHidden(i))
        {


          if (!Double.isNaN(getMean(col, i)))
          {

            if (getSignificance(col, i) == type)
              result++; }
        }
      }
    }
    return result;
  }
  






  public void setRowOrder(int[] order)
  {
    if (order == null) {
      m_RowOrder = null;
    }
    else {
      if (order.length == getRowCount()) {
        m_RowOrder = new int[order.length];
        for (int i = 0; i < order.length; i++) {
          m_RowOrder[i] = order[i];
        }
      }
      System.err.println("setRowOrder: length does not match (" + order.length + " <> " + getRowCount() + ") - ignored!");
    }
  }
  





  public int[] getRowOrder()
  {
    return m_RowOrder;
  }
  





  public int getDisplayRow(int index)
  {
    if ((index >= 0) && (index < getRowCount())) {
      if (getRowOrder() == null) {
        return index;
      }
      return getRowOrder()[index];
    }
    
    return -1;
  }
  







  public void setColOrder(int[] order)
  {
    if (order == null) {
      m_ColOrder = null;
    }
    else {
      if (order.length == getColCount()) {
        m_ColOrder = new int[order.length];
        for (int i = 0; i < order.length; i++) {
          m_ColOrder[i] = order[i];
        }
      }
      System.err.println("setColOrder: length does not match (" + order.length + " <> " + getColCount() + ") - ignored!");
    }
  }
  





  public int[] getColOrder()
  {
    return m_ColOrder;
  }
  





  public int getDisplayCol(int index)
  {
    if ((index >= 0) && (index < getColCount())) {
      if (getColOrder() == null) {
        return index;
      }
      return getColOrder()[index];
    }
    
    return -1;
  }
  











  protected String doubleToString(double d, int prec)
  {
    String result = Utils.doubleToString(d, prec);
    

    if (result.indexOf(".") == -1) {
      result = result + ".";
    }
    
    int currentPrec = result.length() - result.indexOf(".") - 1;
    for (int i = currentPrec; i < prec; i++) {
      result = result + "0";
    }
    return result;
  }
  







  protected String trimString(String s, int length)
  {
    if ((length > 0) && (s.length() > length)) {
      return s.substring(0, length);
    }
    return s;
  }
  






  protected String padString(String s, int length)
  {
    return padString(s, length, false);
  }
  










  protected String padString(String s, int length, boolean left)
  {
    String result = s;
    

    for (int i = s.length(); i < length; i++) {
      if (left) {
        result = " " + result;
      } else {
        result = result + " ";
      }
    }
    
    if ((length > 0) && (result.length() > length)) {
      result = result.substring(0, length);
    }
    return result;
  }
  





  protected int getColSize(String[][] data, int col)
  {
    return getColSize(data, col, false, false);
  }
  











  protected int getColSize(String[][] data, int col, boolean skipFirst, boolean skipLast)
  {
    int result = 0;
    
    if ((col >= 0) && (col < data[0].length)) {
      for (int i = 0; i < data.length; i++)
      {
        if ((i != 0) || (!skipFirst))
        {


          if ((i != data.length - 1) || (!skipLast))
          {

            if (data[i][col].length() > result)
              result = data[i][col].length(); }
        }
      }
    }
    return result;
  }
  




  protected String removeFilterName(String s)
  {
    if (getRemoveFilterName()) {
      return s.replaceAll("-weka\\.filters\\..*", "").replaceAll("-unsupervised\\..*", "").replaceAll("-supervised\\..*", "");
    }
    

    return s;
  }
  




















  protected String[][] toArray()
  {
    int rows = getVisibleRowCount();
    if (getShowAverage())
      rows++;
    int cols = getVisibleColCount();
    if (getShowStdDev()) {
      cols *= 3;
    } else {
      cols *= 2;
    }
    String[][] result = new String[rows + 2][cols + 1];
    

    result[0][0] = trimString("Dataset", getRowNameWidth());
    int x = 1;
    for (int ii = 0; ii < getColCount(); ii++) {
      int i = getDisplayCol(ii);
      if (!getColHidden(i))
      {

        result[0][x] = trimString(removeFilterName(getColName(i)), getColNameWidth());
        
        x++;
        
        if (getShowStdDev()) {
          result[0][x] = "";
          x++;
        }
        
        result[0][x] = "";
        x++;
      }
    }
    
    int y = 1;
    for (ii = 0; ii < getRowCount(); ii++) {
      int i = getDisplayRow(ii);
      if (!getRowHidden(i)) {
        result[y][0] = trimString(removeFilterName(getRowName(i)), getRowNameWidth());
        
        y++;
      }
    }
    

    y = 1;
    for (ii = 0; ii < getRowCount(); ii++) {
      int i = getDisplayRow(ii);
      if (!getRowHidden(i))
      {

        x = 1;
        for (int nn = 0; nn < getColCount(); nn++) {
          int n = getDisplayCol(nn);
          if (!getColHidden(n))
          {


            boolean valueExists = !Double.isNaN(getMean(n, i));
            

            if (!valueExists) {
              result[y][x] = "";
            } else
              result[y][x] = doubleToString(getMean(n, i), getMeanPrec());
            x++;
            

            if (getShowStdDev()) {
              if (!valueExists) {
                result[y][x] = "";
              } else if (Double.isInfinite(getStdDev(n, i))) {
                result[y][x] = "Inf";
              } else
                result[y][x] = doubleToString(getStdDev(n, i), getStdDevPrec());
              x++;
            }
            

            if (!valueExists) {
              result[y][x] = "";
            }
            else {
              switch (getSignificance(n, i)) {
              case 0: 
                result[y][x] = TIE_STRING;
                break;
              case 1: 
                result[y][x] = WIN_STRING;
                break;
              case 2: 
                result[y][x] = LOSS_STRING;
              }
              
            }
            x++;
          }
        }
        y++;
      }
    }
    
    if (getShowAverage()) {
      y = result.length - 2;
      x = 0;
      result[y][0] = "Average";
      x++;
      for (ii = 0; ii < getColCount(); ii++) {
        int i = getDisplayCol(ii);
        if (!getColHidden(i))
        {


          result[y][x] = doubleToString(getAverage(i), getMeanPrec());
          x++;
          

          if (getShowStdDev()) {
            result[y][x] = "";
            x++;
          }
          

          result[y][x] = "";
          x++;
        }
      }
    }
    
    y = result.length - 1;
    x = 0;
    result[y][0] = (LEFT_PARENTHESES + WIN_STRING + "/" + TIE_STRING + "/" + LOSS_STRING + RIGHT_PARENTHESES);
    



    x++;
    for (ii = 0; ii < getColCount(); ii++) {
      int i = getDisplayCol(ii);
      if (!getColHidden(i))
      {


        result[y][x] = "";
        x++;
        

        if (getShowStdDev()) {
          result[y][x] = "";
          x++;
        }
        

        result[y][x] = (LEFT_PARENTHESES + getSignificanceCount(i, 1) + "/" + getSignificanceCount(i, 0) + "/" + getSignificanceCount(i, 2) + RIGHT_PARENTHESES);
        



        x++;
      }
    }
    
    String[][] tmpResult = new String[result.length][result[0].length - 1];
    
    x = 0;
    for (int i = 0; i < result[0].length; i++)
    {
      if (((i != 3) || (!getShowStdDev())) && ((i != 2) || (getShowStdDev())))
      {


        for (int n = 0; n < result.length; n++) {
          tmpResult[n][x] = result[n][i];
        }
        x++;
      } }
    result = tmpResult;
    
    return result;
  }
  



  protected boolean isRowName(int index)
  {
    return index == 0;
  }
  


  protected boolean isMean(int index)
  {
    
    
    if (index == 0) {
      return true;
    }
    
    index--;
    
    if (index < 0) {
      return false;
    }
    if (getShowStdDev()) {
      return index % 3 == 1;
    }
    return index % 2 == 0;
  }
  




  protected boolean isAverage(int rowIndex)
  {
    if (getShowAverage()) {
      return getVisibleRowCount() + 1 == rowIndex;
    }
    return false;
  }
  



  protected boolean isStdDev(int index)
  {
    index--;
    index--;
    
    if (getShowStdDev()) {
      if (index == 0) {
        return true;
      }
      
      index--;
      
      if (index < 0) {
        return false;
      }
      return index % 3 == 1;
    }
    

    return false;
  }
  



  protected boolean isSignificance(int index)
  {
    index--;
    index--;
    if (getShowStdDev()) {
      index--;
      
      if (index < 0) {
        return false;
      }
      return index % 3 == 2;
    }
    
    if (index < 0) {
      return false;
    }
    return index % 2 == 1;
  }
  




  public abstract String toStringMatrix();
  



  public String toString()
  {
    return toStringMatrix();
  }
  


  public void clearHeader()
  {
    m_HeaderKeys = new Vector();
    m_HeaderValues = new Vector();
  }
  






  public void addHeader(String key, String value)
  {
    int pos = m_HeaderKeys.indexOf(key);
    if (pos > -1) {
      m_HeaderValues.set(pos, value);
    }
    else {
      m_HeaderKeys.add(key);
      m_HeaderValues.add(value);
    }
  }
  







  public String getHeader(String key)
  {
    int pos = m_HeaderKeys.indexOf(key);
    if (pos == 0) {
      return null;
    }
    return (String)m_HeaderKeys.get(pos);
  }
  



  public Enumeration headerKeys()
  {
    return m_HeaderKeys.elements();
  }
  




  public abstract String toStringHeader();
  




  public abstract String toStringKey();
  



  public void clearSummary()
  {
    m_NonSigWins = ((int[][])null);
    m_Wins = ((int[][])null);
  }
  







  public void setSummary(int[][] nonSigWins, int[][] wins)
  {
    m_NonSigWins = new int[nonSigWins.length][nonSigWins[0].length];
    m_Wins = new int[wins.length][wins[0].length];
    
    for (int i = 0; i < m_NonSigWins.length; i++) {
      for (int n = 0; n < m_NonSigWins[i].length; n++) {
        m_NonSigWins[i][n] = nonSigWins[i][n];
        m_Wins[i][n] = wins[i][n];
      }
    }
  }
  


  protected String getSummaryTitle(int col)
  {
    return "" + (char)(97 + col % 26);
  }
  



  public abstract String toStringSummary();
  


  public void clearRanking()
  {
    m_RankingWins = null;
    m_RankingLosses = null;
    m_RankingDiff = null;
  }
  






  public void setRanking(int[][] wins)
  {
    m_RankingWins = new int[wins.length];
    m_RankingLosses = new int[wins.length];
    m_RankingDiff = new int[wins.length];
    
    for (int i = 0; i < wins.length; i++) {
      for (int j = 0; j < wins[i].length; j++) {
        m_RankingWins[j] += wins[i][j];
        m_RankingDiff[j] += wins[i][j];
        m_RankingLosses[i] += wins[i][j];
        m_RankingDiff[i] -= wins[i][j];
      }
    }
  }
  
  public abstract String toStringRanking();
}

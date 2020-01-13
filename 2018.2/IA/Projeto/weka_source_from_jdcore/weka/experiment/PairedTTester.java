package weka.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;

























































public class PairedTTester
  implements OptionHandler, Tester, RevisionHandler
{
  static final long serialVersionUID = 8370014624008728610L;
  protected Instances m_Instances;
  protected int m_RunColumn;
  protected int m_RunColumnSet;
  protected int m_FoldColumn;
  protected int m_SortColumn;
  protected int[] m_SortOrder;
  protected int[] m_ColOrder;
  protected double m_SignificanceLevel;
  protected Range m_DatasetKeyColumnsRange;
  protected int[] m_DatasetKeyColumns;
  protected DatasetSpecifiers m_DatasetSpecifiers;
  protected Range m_ResultsetKeyColumnsRange;
  protected int[] m_ResultsetKeyColumns;
  protected int[] m_DisplayedResultsets;
  protected FastVector m_Resultsets;
  protected boolean m_ResultsetsValid;
  protected boolean m_ShowStdDevs;
  protected ResultMatrix m_ResultMatrix;
  
  public PairedTTester()
  {
    m_RunColumn = 0;
    

    m_RunColumnSet = -1;
    

    m_FoldColumn = -1;
    

    m_SortColumn = -1;
    

    m_SortOrder = null;
    

    m_ColOrder = null;
    

    m_SignificanceLevel = 0.05D;
    




    m_DatasetKeyColumnsRange = new Range();
    




    m_DatasetSpecifiers = new DatasetSpecifiers();
    





    m_ResultsetKeyColumnsRange = new Range();
    




    m_DisplayedResultsets = null;
    

    m_Resultsets = new FastVector();
    




    m_ShowStdDevs = false;
    

    m_ResultMatrix = new ResultMatrixPlainText();
  }
  

  protected class DatasetSpecifiers
    implements RevisionHandler
  {
    FastVector m_Specifiers = new FastVector();
    

    protected DatasetSpecifiers() {}
    
    protected void removeAllSpecifiers()
    {
      m_Specifiers.removeAllElements();
    }
    





    protected void add(Instance inst)
    {
      for (int i = 0; i < m_Specifiers.size(); i++) {
        Instance specifier = (Instance)m_Specifiers.elementAt(i);
        boolean found = true;
        for (int j = 0; j < m_DatasetKeyColumns.length; j++) {
          if (inst.value(m_DatasetKeyColumns[j]) != specifier.value(m_DatasetKeyColumns[j]))
          {
            found = false;
          }
        }
        if (found) {
          return;
        }
      }
      m_Specifiers.addElement(inst);
    }
    






    protected Instance specifier(int i)
    {
      return (Instance)m_Specifiers.elementAt(i);
    }
    





    protected int numSpecifiers()
    {
      return m_Specifiers.size();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6431 $");
    }
  }
  



  protected class Dataset
    implements RevisionHandler
  {
    Instance m_Template;
    


    FastVector m_Dataset;
    



    public Dataset(Instance template)
    {
      m_Template = template;
      m_Dataset = new FastVector();
      add(template);
    }
    







    protected boolean matchesTemplate(Instance first)
    {
      for (int i = 0; i < m_DatasetKeyColumns.length; i++) {
        if (first.value(m_DatasetKeyColumns[i]) != m_Template.value(m_DatasetKeyColumns[i]))
        {
          return false;
        }
      }
      return true;
    }
    





    protected void add(Instance inst)
    {
      m_Dataset.addElement(inst);
    }
    





    protected FastVector contents()
    {
      return m_Dataset;
    }
    





    public void sort(int runColumn)
    {
      double[] runNums = new double[m_Dataset.size()];
      for (int j = 0; j < runNums.length; j++) {
        runNums[j] = ((Instance)m_Dataset.elementAt(j)).value(runColumn);
      }
      int[] index = Utils.stableSort(runNums);
      FastVector newDataset = new FastVector(runNums.length);
      for (int j = 0; j < index.length; j++) {
        newDataset.addElement(m_Dataset.elementAt(index[j]));
      }
      m_Dataset = newDataset;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6431 $");
    }
  }
  



  protected class Resultset
    implements RevisionHandler
  {
    Instance m_Template;
    


    FastVector m_Datasets;
    



    public Resultset(Instance template)
    {
      m_Template = template;
      m_Datasets = new FastVector();
      add(template);
    }
    







    protected boolean matchesTemplate(Instance first)
    {
      for (int i = 0; i < m_ResultsetKeyColumns.length; i++) {
        if (first.value(m_ResultsetKeyColumns[i]) != m_Template.value(m_ResultsetKeyColumns[i]))
        {
          return false;
        }
      }
      return true;
    }
    






    protected String templateString()
    {
      String result = "";
      String tempResult = "";
      for (int i = 0; i < m_ResultsetKeyColumns.length; i++) {
        tempResult = m_Template.toString(m_ResultsetKeyColumns[i]) + ' ';
        

        tempResult = Utils.removeSubstring(tempResult, "weka.classifiers.");
        tempResult = Utils.removeSubstring(tempResult, "weka.filters.");
        tempResult = Utils.removeSubstring(tempResult, "weka.attributeSelection.");
        result = result + tempResult;
      }
      return result.trim();
    }
    






    public FastVector dataset(Instance inst)
    {
      for (int i = 0; i < m_Datasets.size(); i++) {
        if (((PairedTTester.Dataset)m_Datasets.elementAt(i)).matchesTemplate(inst)) {
          return ((PairedTTester.Dataset)m_Datasets.elementAt(i)).contents();
        }
      }
      return null;
    }
    





    public void add(Instance newInst)
    {
      for (int i = 0; i < m_Datasets.size(); i++) {
        if (((PairedTTester.Dataset)m_Datasets.elementAt(i)).matchesTemplate(newInst)) {
          ((PairedTTester.Dataset)m_Datasets.elementAt(i)).add(newInst);
          return;
        }
      }
      PairedTTester.Dataset newDataset = new PairedTTester.Dataset(PairedTTester.this, newInst);
      m_Datasets.addElement(newDataset);
    }
    





    public void sort(int runColumn)
    {
      for (int i = 0; i < m_Datasets.size(); i++) {
        ((PairedTTester.Dataset)m_Datasets.elementAt(i)).sort(runColumn);
      }
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6431 $");
    }
  }
  








  protected String templateString(Instance template)
  {
    String result = "";
    for (int i = 0; i < m_DatasetKeyColumns.length; i++) {
      result = result + template.toString(m_DatasetKeyColumns[i]) + ' ';
    }
    if (result.startsWith("weka.classifiers.")) {
      result = result.substring("weka.classifiers.".length());
    }
    return result.trim();
  }
  




  public void setResultMatrix(ResultMatrix matrix)
  {
    m_ResultMatrix = matrix;
  }
  



  public ResultMatrix getResultMatrix()
  {
    return m_ResultMatrix;
  }
  



  public void setShowStdDevs(boolean s)
  {
    m_ShowStdDevs = s;
  }
  



  public boolean getShowStdDevs()
  {
    return m_ShowStdDevs;
  }
  




  protected void prepareData()
    throws Exception
  {
    if (m_Instances == null) {
      throw new Exception("No instances have been set");
    }
    if (m_RunColumnSet == -1) {
      m_RunColumn = (m_Instances.numAttributes() - 1);
    } else {
      m_RunColumn = m_RunColumnSet;
    }
    
    if (m_ResultsetKeyColumnsRange == null) {
      throw new Exception("No result specifier columns have been set");
    }
    m_ResultsetKeyColumnsRange.setUpper(m_Instances.numAttributes() - 1);
    m_ResultsetKeyColumns = m_ResultsetKeyColumnsRange.getSelection();
    
    if (m_DatasetKeyColumnsRange == null) {
      throw new Exception("No dataset specifier columns have been set");
    }
    m_DatasetKeyColumnsRange.setUpper(m_Instances.numAttributes() - 1);
    m_DatasetKeyColumns = m_DatasetKeyColumnsRange.getSelection();
    

    m_Resultsets.removeAllElements();
    m_DatasetSpecifiers.removeAllSpecifiers();
    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance current = m_Instances.instance(i);
      if (current.isMissing(m_RunColumn)) {
        throw new Exception("Instance has missing value in run column!\n" + current);
      }
      
      for (int j = 0; j < m_ResultsetKeyColumns.length; j++) {
        if (current.isMissing(m_ResultsetKeyColumns[j])) {
          throw new Exception("Instance has missing value in resultset key column " + (m_ResultsetKeyColumns[j] + 1) + "!\n" + current);
        }
      }
      

      for (int j = 0; j < m_DatasetKeyColumns.length; j++) {
        if (current.isMissing(m_DatasetKeyColumns[j])) {
          throw new Exception("Instance has missing value in dataset key column " + (m_DatasetKeyColumns[j] + 1) + "!\n" + current);
        }
      }
      

      boolean found = false;
      for (int j = 0; j < m_Resultsets.size(); j++) {
        Resultset resultset = (Resultset)m_Resultsets.elementAt(j);
        if (resultset.matchesTemplate(current)) {
          resultset.add(current);
          found = true;
          break;
        }
      }
      if (!found) {
        Resultset resultset = new Resultset(current);
        m_Resultsets.addElement(resultset);
      }
      
      m_DatasetSpecifiers.add(current);
    }
    

    for (int j = 0; j < m_Resultsets.size(); j++) {
      Resultset resultset = (Resultset)m_Resultsets.elementAt(j);
      if (m_FoldColumn >= 0)
      {
        resultset.sort(m_FoldColumn);
      }
      resultset.sort(m_RunColumn);
    }
    
    m_ResultsetsValid = true;
  }
  





  public int getNumDatasets()
  {
    if (!m_ResultsetsValid) {
      try {
        prepareData();
      } catch (Exception ex) {
        ex.printStackTrace();
        return 0;
      }
    }
    return m_DatasetSpecifiers.numSpecifiers();
  }
  





  public int getNumResultsets()
  {
    if (!m_ResultsetsValid) {
      try {
        prepareData();
      } catch (Exception ex) {
        ex.printStackTrace();
        return 0;
      }
    }
    return m_Resultsets.size();
  }
  






  public String getResultsetName(int index)
  {
    if (!m_ResultsetsValid) {
      try {
        prepareData();
      } catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    }
    return ((Resultset)m_Resultsets.elementAt(index)).templateString();
  }
  








  public boolean displayResultset(int index)
  {
    boolean result = true;
    
    if (m_DisplayedResultsets != null) {
      result = false;
      for (int i = 0; i < m_DisplayedResultsets.length; i++) {
        if (m_DisplayedResultsets[i] == index) {
          result = true;
          break;
        }
      }
    }
    
    return result;
  }
  













  public PairedStats calculateStatistics(Instance datasetSpecifier, int resultset1Index, int resultset2Index, int comparisonColumn)
    throws Exception
  {
    if (m_Instances.attribute(comparisonColumn).type() != 0)
    {
      throw new Exception("Comparison column " + (comparisonColumn + 1) + " (" + m_Instances.attribute(comparisonColumn).name() + ") is not numeric");
    }
    


    if (!m_ResultsetsValid) {
      prepareData();
    }
    
    Resultset resultset1 = (Resultset)m_Resultsets.elementAt(resultset1Index);
    Resultset resultset2 = (Resultset)m_Resultsets.elementAt(resultset2Index);
    FastVector dataset1 = resultset1.dataset(datasetSpecifier);
    FastVector dataset2 = resultset2.dataset(datasetSpecifier);
    String datasetName = templateString(datasetSpecifier);
    if (dataset1 == null) {
      throw new Exception("No results for dataset=" + datasetName + " for resultset=" + resultset1.templateString());
    }
    if (dataset2 == null) {
      throw new Exception("No results for dataset=" + datasetName + " for resultset=" + resultset2.templateString());
    }
    if (dataset1.size() != dataset2.size()) {
      throw new Exception("Results for dataset=" + datasetName + " differ in size for resultset=" + resultset1.templateString() + " and resultset=" + resultset2.templateString());
    }
    





    PairedStats pairedStats = new PairedStats(m_SignificanceLevel);
    
    for (int k = 0; k < dataset1.size(); k++) {
      Instance current1 = (Instance)dataset1.elementAt(k);
      Instance current2 = (Instance)dataset2.elementAt(k);
      if (current1.isMissing(comparisonColumn)) {
        System.err.println("Instance has missing value in comparison column!\n" + current1);


      }
      else if (current2.isMissing(comparisonColumn)) {
        System.err.println("Instance has missing value in comparison column!\n" + current2);
      }
      else
      {
        if (current1.value(m_RunColumn) != current2.value(m_RunColumn)) {
          System.err.println("Run numbers do not match!\n" + current1 + current2);
        }
        
        if ((m_FoldColumn != -1) && 
          (current1.value(m_FoldColumn) != current2.value(m_FoldColumn))) {
          System.err.println("Fold numbers do not match!\n" + current1 + current2);
        }
        

        double value1 = current1.value(comparisonColumn);
        double value2 = current2.value(comparisonColumn);
        pairedStats.add(value1, value2);
      } }
    pairedStats.calculateDerived();
    
    return pairedStats;
  }
  






  public String resultsetKey()
  {
    if (!m_ResultsetsValid) {
      try {
        prepareData();
      } catch (Exception ex) {
        ex.printStackTrace();
        return ex.getMessage();
      }
    }
    String result = "";
    for (int j = 0; j < getNumResultsets(); j++) {
      result = result + "(" + (j + 1) + ") " + getResultsetName(j) + '\n';
    }
    return result + '\n';
  }
  






  public String header(int comparisonColumn)
  {
    if (!m_ResultsetsValid) {
      try {
        prepareData();
      } catch (Exception ex) {
        ex.printStackTrace();
        return ex.getMessage();
      }
    }
    
    initResultMatrix();
    m_ResultMatrix.addHeader("Tester", getClass().getName());
    m_ResultMatrix.addHeader("Analysing", m_Instances.attribute(comparisonColumn).name());
    m_ResultMatrix.addHeader("Datasets", Integer.toString(getNumDatasets()));
    m_ResultMatrix.addHeader("Resultsets", Integer.toString(getNumResultsets()));
    m_ResultMatrix.addHeader("Confidence", getSignificanceLevel() + " (two tailed)");
    m_ResultMatrix.addHeader("Sorted by", getSortColumnName());
    m_ResultMatrix.addHeader("Date", new SimpleDateFormat().format(new Date()));
    
    return m_ResultMatrix.toStringHeader() + "\n";
  }
  










  public int[][] multiResultsetWins(int comparisonColumn, int[][] nonSigWin)
    throws Exception
  {
    int numResultsets = getNumResultsets();
    int[][] win = new int[numResultsets][numResultsets];
    
    for (int i = 0; i < numResultsets; i++) {
      for (int j = i + 1; j < numResultsets; j++) {
        System.err.print("Comparing (" + (i + 1) + ") with (" + (j + 1) + ")\r");
        
        System.err.flush();
        for (int k = 0; k < getNumDatasets(); k++) {
          try {
            PairedStats pairedStats = calculateStatistics(m_DatasetSpecifiers.specifier(k), i, j, comparisonColumn);
            

            if (differencesSignificance < 0) {
              win[i][j] += 1;
            } else if (differencesSignificance > 0) {
              win[j][i] += 1;
            }
            
            if (differencesStats.mean < 0.0D) {
              nonSigWin[i][j] += 1;
            } else if (differencesStats.mean > 0.0D) {
              nonSigWin[j][i] += 1;
            }
          }
          catch (Exception ex) {
            System.err.println(ex.getMessage());
          }
        }
      }
    }
    return win;
  }
  



  protected void initResultMatrix()
  {
    m_ResultMatrix.setSize(getNumResultsets(), getNumDatasets());
    m_ResultMatrix.setShowStdDev(m_ShowStdDevs);
    
    for (int i = 0; i < getNumDatasets(); i++) {
      m_ResultMatrix.setRowName(i, templateString(m_DatasetSpecifiers.specifier(i)));
    }
    
    for (int j = 0; j < getNumResultsets(); j++) {
      m_ResultMatrix.setColName(j, getResultsetName(j));
      m_ResultMatrix.setColHidden(j, !displayResultset(j));
    }
  }
  









  public String multiResultsetSummary(int comparisonColumn)
    throws Exception
  {
    int[][] nonSigWin = new int[getNumResultsets()][getNumResultsets()];
    int[][] win = multiResultsetWins(comparisonColumn, nonSigWin);
    
    initResultMatrix();
    m_ResultMatrix.setSummary(nonSigWin, win);
    
    return m_ResultMatrix.toStringSummary();
  }
  







  public String multiResultsetRanking(int comparisonColumn)
    throws Exception
  {
    int[][] nonSigWin = new int[getNumResultsets()][getNumResultsets()];
    int[][] win = multiResultsetWins(comparisonColumn, nonSigWin);
    
    initResultMatrix();
    m_ResultMatrix.setRanking(win);
    
    return m_ResultMatrix.toStringRanking();
  }
  









  public String multiResultsetFull(int baseResultset, int comparisonColumn)
    throws Exception
  {
    int maxWidthMean = 2;
    int maxWidthStdDev = 2;
    
    double[] sortValues = new double[getNumDatasets()];
    

    for (int i = 0; i < getNumDatasets(); i++) {
      sortValues[i] = Double.POSITIVE_INFINITY;
      
      for (int j = 0; j < getNumResultsets(); j++) {
        if (displayResultset(j)) {
          try
          {
            PairedStats pairedStats = calculateStatistics(m_DatasetSpecifiers.specifier(i), baseResultset, j, comparisonColumn);
            

            if ((!Double.isInfinite(yStats.mean)) && (!Double.isNaN(yStats.mean)))
            {
              double width = Math.log(Math.abs(yStats.mean)) / Math.log(10.0D) + 1.0D;
              
              if (width > maxWidthMean) {
                maxWidthMean = (int)width;
              }
            }
            
            if (j == baseResultset) {
              if (getSortColumn() != -1) {
                sortValues[i] = calculateStatisticsm_DatasetSpecifiers.specifier(i), baseResultset, j, getSortColumn()).xStats.mean;
              }
              else
              {
                sortValues[i] = i;
              }
            }
            if ((m_ShowStdDevs) && (!Double.isInfinite(yStats.stdDev)) && (!Double.isNaN(yStats.stdDev)))
            {

              double width = Math.log(Math.abs(yStats.stdDev)) / Math.log(10.0D) + 1.0D;
              
              if (width > maxWidthStdDev) {
                maxWidthStdDev = (int)width;
              }
            }
          }
          catch (Exception ex) {
            System.err.println(ex);
          }
        }
      }
    }
    
    m_SortOrder = Utils.sort(sortValues);
    

    m_ColOrder = new int[getNumResultsets()];
    m_ColOrder[0] = baseResultset;
    int index = 1;
    for (int i = 0; i < getNumResultsets(); i++) {
      if (i != baseResultset)
      {
        m_ColOrder[index] = i;
        index++;
      }
    }
    
    initResultMatrix();
    m_ResultMatrix.setRowOrder(m_SortOrder);
    m_ResultMatrix.setColOrder(m_ColOrder);
    m_ResultMatrix.setMeanWidth(maxWidthMean);
    m_ResultMatrix.setStdDevWidth(maxWidthStdDev);
    m_ResultMatrix.setSignificanceWidth(1);
    


    for (int i = 0; i < m_ResultMatrix.getColCount(); i++) {
      if ((i == baseResultset) && (m_ResultMatrix.getColHidden(i)))
      {
        m_ResultMatrix.setColHidden(i, false);
        System.err.println("Note: test base was hidden - set visible!");
      }
    }
    

    for (int i = 0; i < getNumDatasets(); i++) {
      m_ResultMatrix.setRowName(i, templateString(m_DatasetSpecifiers.specifier(i)));
      

      for (int j = 0; j < getNumResultsets(); j++) {
        try
        {
          PairedStats pairedStats = calculateStatistics(m_DatasetSpecifiers.specifier(i), baseResultset, j, comparisonColumn);
          



          m_ResultMatrix.setCount(i, count);
          

          m_ResultMatrix.setMean(j, i, yStats.mean);
          

          m_ResultMatrix.setStdDev(j, i, yStats.stdDev);
          

          if (differencesSignificance < 0) {
            m_ResultMatrix.setSignificance(j, i, 1);
          } else if (differencesSignificance > 0) {
            m_ResultMatrix.setSignificance(j, i, 2);
          } else {
            m_ResultMatrix.setSignificance(j, i, 0);
          }
        }
        catch (Exception e) {
          System.err.println(e);
        }
      }
    }
    

    StringBuffer result = new StringBuffer(1000);
    try {
      result.append(m_ResultMatrix.toStringMatrix());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    


    result.append("\n\n" + m_ResultMatrix.toStringKey());
    
    return result.toString();
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tSpecify list of columns that specify a unique\n\tdataset.\n\tFirst and last are valid indexes. (default none)", "D", 1, "-D <index,index2-index4,...>"));
    



    newVector.addElement(new Option("\tSet the index of the column containing the run number", "R", 1, "-R <index>"));
    

    newVector.addElement(new Option("\tSet the index of the column containing the fold number", "F", 1, "-F <index>"));
    

    newVector.addElement(new Option("\tSpecify list of columns that specify a unique\n\t'result generator' (eg: classifier name and options).\n\tFirst and last are valid indexes. (default none)", "G", 1, "-G <index1,index2-index4,...>"));
    



    newVector.addElement(new Option("\tSet the significance level for comparisons (default 0.05)", "S", 1, "-S <significance level>"));
    

    newVector.addElement(new Option("\tShow standard deviations", "V", 0, "-V"));
    

    newVector.addElement(new Option("\tProduce table comparisons in Latex table format", "L", 0, "-L"));
    

    newVector.addElement(new Option("\tProduce table comparisons in CSV table format", "csv", 0, "-csv"));
    

    newVector.addElement(new Option("\tProduce table comparisons in HTML table format", "html", 0, "-html"));
    

    newVector.addElement(new Option("\tProduce table comparisons with only the significance values", "significance", 0, "-significance"));
    

    newVector.addElement(new Option("\tProduce table comparisons output suitable for GNUPlot", "gnuplot", 0, "-gnuplot"));
    


    return newVector.elements();
  }
  















































  public void setOptions(String[] options)
    throws Exception
  {
    setShowStdDevs(Utils.getFlag('V', options));
    if (Utils.getFlag('L', options))
      setResultMatrix(new ResultMatrixLatex());
    if (Utils.getFlag("csv", options))
      setResultMatrix(new ResultMatrixCSV());
    if (Utils.getFlag("html", options))
      setResultMatrix(new ResultMatrixHTML());
    if (Utils.getFlag("significance", options)) {
      setResultMatrix(new ResultMatrixSignificance());
    }
    String datasetList = Utils.getOption('D', options);
    Range datasetRange = new Range();
    if (datasetList.length() != 0) {
      datasetRange.setRanges(datasetList);
    }
    setDatasetKeyColumns(datasetRange);
    
    String indexStr = Utils.getOption('R', options);
    if (indexStr.length() != 0) {
      if (indexStr.equals("first")) {
        setRunColumn(0);
      } else if (indexStr.equals("last")) {
        setRunColumn(-1);
      } else {
        setRunColumn(Integer.parseInt(indexStr) - 1);
      }
    } else {
      setRunColumn(-1);
    }
    
    String foldStr = Utils.getOption('F', options);
    if (foldStr.length() != 0) {
      setFoldColumn(Integer.parseInt(foldStr) - 1);
    } else {
      setFoldColumn(-1);
    }
    
    String sigStr = Utils.getOption('S', options);
    if (sigStr.length() != 0) {
      setSignificanceLevel(new Double(sigStr).doubleValue());
    } else {
      setSignificanceLevel(0.05D);
    }
    
    String resultsetList = Utils.getOption('G', options);
    Range generatorRange = new Range();
    if (resultsetList.length() != 0) {
      generatorRange.setRanges(resultsetList);
    }
    setResultsetKeyColumns(generatorRange);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[11];
    int current = 0;
    
    if (!getResultsetKeyColumns().getRanges().equals("")) {
      options[(current++)] = "-G";
      options[(current++)] = getResultsetKeyColumns().getRanges();
    }
    if (!getDatasetKeyColumns().getRanges().equals("")) {
      options[(current++)] = "-D";
      options[(current++)] = getDatasetKeyColumns().getRanges();
    }
    options[(current++)] = "-R";
    options[(current++)] = ("" + (getRunColumn() + 1));
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSignificanceLevel());
    
    if (getShowStdDevs()) {
      options[(current++)] = "-V";
    }
    
    if ((getResultMatrix() instanceof ResultMatrixLatex)) {
      options[(current++)] = "-L";
    }
    if ((getResultMatrix() instanceof ResultMatrixCSV)) {
      options[(current++)] = "-csv";
    }
    if ((getResultMatrix() instanceof ResultMatrixHTML)) {
      options[(current++)] = "-html";
    }
    if ((getResultMatrix() instanceof ResultMatrixSignificance)) {
      options[(current++)] = "-significance";
    }
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public Range getResultsetKeyColumns()
  {
    return m_ResultsetKeyColumnsRange;
  }
  





  public void setResultsetKeyColumns(Range newResultsetKeyColumns)
  {
    m_ResultsetKeyColumnsRange = newResultsetKeyColumns;
    m_ResultsetsValid = false;
  }
  





  public int[] getDisplayedResultsets()
  {
    return m_DisplayedResultsets;
  }
  





  public void setDisplayedResultsets(int[] cols)
  {
    m_DisplayedResultsets = cols;
  }
  





  public double getSignificanceLevel()
  {
    return m_SignificanceLevel;
  }
  





  public void setSignificanceLevel(double newSignificanceLevel)
  {
    m_SignificanceLevel = newSignificanceLevel;
  }
  





  public Range getDatasetKeyColumns()
  {
    return m_DatasetKeyColumnsRange;
  }
  





  public void setDatasetKeyColumns(Range newDatasetKeyColumns)
  {
    m_DatasetKeyColumnsRange = newDatasetKeyColumns;
    m_ResultsetsValid = false;
  }
  





  public int getRunColumn()
  {
    return m_RunColumnSet;
  }
  





  public void setRunColumn(int newRunColumn)
  {
    m_RunColumnSet = newRunColumn;
    m_ResultsetsValid = false;
  }
  





  public int getFoldColumn()
  {
    return m_FoldColumn;
  }
  





  public void setFoldColumn(int newFoldColumn)
  {
    m_FoldColumn = newFoldColumn;
    m_ResultsetsValid = false;
  }
  




  public String getSortColumnName()
  {
    if (getSortColumn() == -1) {
      return "-";
    }
    return m_Instances.attribute(getSortColumn()).name();
  }
  




  public int getSortColumn()
  {
    return m_SortColumn;
  }
  




  public void setSortColumn(int newSortColumn)
  {
    if (newSortColumn >= -1) {
      m_SortColumn = newSortColumn;
    }
  }
  




  public Instances getInstances()
  {
    return m_Instances;
  }
  





  public void setInstances(Instances newInstances)
  {
    m_Instances = newInstances;
    m_ResultsetsValid = false;
  }
  




  public void assign(Tester tester)
  {
    setInstances(tester.getInstances());
    setResultMatrix(tester.getResultMatrix());
    setShowStdDevs(tester.getShowStdDevs());
    setResultsetKeyColumns(tester.getResultsetKeyColumns());
    setDisplayedResultsets(tester.getDisplayedResultsets());
    setSignificanceLevel(tester.getSignificanceLevel());
    setDatasetKeyColumns(tester.getDatasetKeyColumns());
    setRunColumn(tester.getRunColumn());
    setFoldColumn(tester.getFoldColumn());
    setSortColumn(tester.getSortColumn());
  }
  





  public String getToolTipText()
  {
    return "Performs test using t-test statistic";
  }
  




  public String getDisplayName()
  {
    return "Paired T-Tester";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6431 $");
  }
  




  public static void main(String[] args)
  {
    try
    {
      PairedTTester tt = new PairedTTester();
      String datasetName = Utils.getOption('t', args);
      String compareColStr = Utils.getOption('c', args);
      String baseColStr = Utils.getOption('b', args);
      boolean summaryOnly = Utils.getFlag('s', args);
      boolean rankingOnly = Utils.getFlag('r', args);
      try {
        if ((datasetName.length() == 0) || (compareColStr.length() == 0))
        {
          throw new Exception("-t and -c options are required");
        }
        tt.setOptions(args);
        Utils.checkForRemainingOptions(args);
      } catch (Exception ex) {
        String result = "";
        Enumeration enu = tt.listOptions();
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          result = result + option.synopsis() + '\n' + option.description() + '\n';
        }
        
        throw new Exception("Usage:\n\n-t <file>\n\tSet the dataset containing data to evaluate\n-b <index>\n\tSet the resultset to base comparisons against (optional)\n-c <index>\n\tSet the column to perform a comparison on\n-s\n\tSummarize wins over all resultset pairs\n\n-r\n\tGenerate a resultset ranking\n\n" + result);
      }
      











      Instances data = new Instances(new BufferedReader(new FileReader(datasetName)));
      
      tt.setInstances(data);
      
      int compareCol = Integer.parseInt(compareColStr) - 1;
      System.out.println(tt.header(compareCol));
      if (rankingOnly) {
        System.out.println(tt.multiResultsetRanking(compareCol));
      } else if (summaryOnly) {
        System.out.println(tt.multiResultsetSummary(compareCol));
      } else {
        System.out.println(tt.resultsetKey());
        if (baseColStr.length() == 0) {
          for (int i = 0; i < tt.getNumResultsets(); i++) {
            if (tt.displayResultset(i))
            {
              System.out.println(tt.multiResultsetFull(i, compareCol)); }
          }
        } else {
          int baseCol = Integer.parseInt(baseColStr) - 1;
          System.out.println(tt.multiResultsetFull(baseCol, compareCol));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
  }
}

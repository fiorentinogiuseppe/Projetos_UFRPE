package weka.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;






































































































public class PairedCorrectedTTester
  extends PairedTTester
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -3105268939845653323L;
  
  public PairedCorrectedTTester() {}
  
  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Claude Nadeau and Yoshua Bengio");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.TITLE, "Inference for the Generalization Error");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.PDF, "http://www.iro.umontreal.ca/~lisa/bib/pub_subject/comparative/pointeurs/nadeau_MLJ1597.pdf");
    
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
    
    PairedTTester.Resultset resultset1 = (PairedTTester.Resultset)m_Resultsets.elementAt(resultset1Index);
    PairedTTester.Resultset resultset2 = (PairedTTester.Resultset)m_Resultsets.elementAt(resultset2Index);
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
    






    double testTrainRatio = 0.0D;
    int trainSizeIndex = -1;
    int testSizeIndex = -1;
    
    for (int i = 0; i < m_Instances.numAttributes(); i++) {
      if (m_Instances.attribute(i).name().toLowerCase().equals("number_of_training_instances")) {
        trainSizeIndex = i;
      } else if (m_Instances.attribute(i).name().toLowerCase().equals("number_of_testing_instances")) {
        testSizeIndex = i;
      }
    }
    if ((trainSizeIndex >= 0) && (testSizeIndex >= 0)) {
      double totalTrainSize = 0.0D;
      double totalTestSize = 0.0D;
      for (int k = 0; k < dataset1.size(); k++) {
        Instance current = (Instance)dataset1.elementAt(k);
        totalTrainSize += current.value(trainSizeIndex);
        totalTestSize += current.value(testSizeIndex);
      }
      testTrainRatio = totalTestSize / totalTrainSize;
    }
    PairedStats pairedStats = new PairedStatsCorrected(m_SignificanceLevel, testTrainRatio);
    

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
  




  public static void main(String[] args)
  {
    try
    {
      PairedCorrectedTTester tt = new PairedCorrectedTTester();
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
            System.out.println(tt.multiResultsetFull(i, compareCol));
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
  




  public String getDisplayName()
  {
    return "Paired T-Tester (corrected)";
  }
  





  public String getToolTipText()
  {
    return "Performs test using corrected resampled t-test statistic (Nadeau and Bengio)";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}

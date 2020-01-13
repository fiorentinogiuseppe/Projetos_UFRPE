package weka.classifiers.functions;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.pace.ChisqMixture;
import weka.classifiers.functions.pace.NormalMixture;
import weka.classifiers.functions.pace.PaceMatrix;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NoSupportForMissingValuesException;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.WekaException;
import weka.core.matrix.DoubleVector;
import weka.core.matrix.IntVector;



































































































public class PaceRegression
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 7230266976059115435L;
  Instances m_Model = null;
  

  private double[] m_Coefficients;
  

  private int m_ClassIndex;
  

  private boolean m_Debug;
  

  private static final int olsEstimator = 0;
  
  private static final int ebEstimator = 1;
  
  private static final int nestedEstimator = 2;
  
  private static final int subsetEstimator = 3;
  
  private static final int pace2Estimator = 4;
  
  private static final int pace4Estimator = 5;
  
  private static final int pace6Estimator = 6;
  
  private static final int olscEstimator = 7;
  
  private static final int aicEstimator = 8;
  
  private static final int bicEstimator = 9;
  
  private static final int ricEstimator = 10;
  
  public static final Tag[] TAGS_ESTIMATOR = { new Tag(0, "Ordinary least squares"), new Tag(1, "Empirical Bayes"), new Tag(2, "Nested model selector"), new Tag(3, "Subset selector"), new Tag(4, "PACE2"), new Tag(5, "PACE4"), new Tag(6, "PACE6"), new Tag(7, "Ordinary least squares selection"), new Tag(8, "AIC"), new Tag(9, "BIC"), new Tag(10, "RIC") };
  













  private int paceEstimator = 1;
  
  private double olscThreshold = 2.0D;
  

  public PaceRegression() {}
  

  public String globalInfo()
  {
    return "Class for building pace regression linear models and using them for prediction. \n\nUnder regularity conditions, pace regression is provably optimal when the number of coefficients tends to infinity. It consists of a group of estimators that are either overall optimal or optimal under certain conditions.\n\nThe current work of the pace regression theory, and therefore also this implementation, do not handle: \n\n- missing values \n- non-binary nominal attributes \n- the case that n - k is small where n is the number of instances and k is the number of coefficients (the threshold used in this implmentation is 20)\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  






















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PHDTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Wang, Y");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.TITLE, "A new approach to fitting linear models in high dimensional spaces");
    result.setValue(TechnicalInformation.Field.SCHOOL, "Department of Computer Science, University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Wang, Y. and Witten, I. H.");
    additional.setValue(TechnicalInformation.Field.YEAR, "2002");
    additional.setValue(TechnicalInformation.Field.TITLE, "Modeling for optimal probability prediction");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the Nineteenth International Conference in Machine Learning");
    additional.setValue(TechnicalInformation.Field.YEAR, "2002");
    additional.setValue(TechnicalInformation.Field.PAGES, "650-657");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Sydney, Australia");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.BINARY_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    Capabilities cap = getCapabilities();
    cap.setMinimumNumberInstances(20 + data.numAttributes());
    cap.testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    



    m_Model = new Instances(data, 0);
    m_ClassIndex = data.classIndex();
    double[][] transformedDataMatrix = getTransformedDataMatrix(data, m_ClassIndex);
    
    double[] classValueVector = data.attributeToDoubleArray(m_ClassIndex);
    
    m_Coefficients = null;
    



    m_Coefficients = pace(transformedDataMatrix, classValueVector);
  }
  







  private double[] pace(double[][] matrix_X, double[] vector_Y)
  {
    PaceMatrix X = new PaceMatrix(matrix_X);
    PaceMatrix Y = new PaceMatrix(vector_Y, vector_Y.length);
    IntVector pvt = IntVector.seq(0, X.getColumnDimension() - 1);
    int n = X.getRowDimension();
    int kr = X.getColumnDimension();
    
    X.lsqrSelection(Y, pvt, 1);
    X.positiveDiagonal(Y, pvt);
    
    PaceMatrix sol = (PaceMatrix)Y.clone();
    X.rsolve(sol, pvt, pvt.size());
    DoubleVector r = Y.getColumn(pvt.size(), n - 1, 0);
    double sde = Math.sqrt(r.sum2() / r.size());
    
    DoubleVector aHat = Y.getColumn(0, pvt.size() - 1, 0).times(1.0D / sde);
    
    DoubleVector aTilde = null;
    switch (paceEstimator) {
    case 1: 
    case 2: 
    case 3: 
      NormalMixture d = new NormalMixture();
      d.fit(aHat, 1);
      if (paceEstimator == 1) {
        aTilde = d.empiricalBayesEstimate(aHat);
      } else if (paceEstimator == 1)
        aTilde = d.subsetEstimate(aHat); else
        aTilde = d.nestedEstimate(aHat);
      break;
    case 4: 
    case 5: 
    case 6: 
      DoubleVector AHat = aHat.square();
      ChisqMixture dc = new ChisqMixture();
      dc.fit(AHat, 1);
      DoubleVector ATilde;
      DoubleVector ATilde; if (paceEstimator == 6) {
        ATilde = dc.pace6(AHat); } else { DoubleVector ATilde;
        if (paceEstimator == 4)
          ATilde = dc.pace2(AHat); else
          ATilde = dc.pace4(AHat); }
      aTilde = ATilde.sqrt().times(aHat.sign());
      break;
    case 0: 
      aTilde = aHat.copy();
      break;
    case 7: 
    case 8: 
    case 9: 
    case 10: 
      if (paceEstimator == 8) { olscThreshold = 2.0D;
      } else if (paceEstimator == 9) { olscThreshold = Math.log(n);
      } else if (paceEstimator == 10) olscThreshold = (2.0D * Math.log(kr));
      aTilde = aHat.copy();
      for (int i = 0; i < aTilde.size(); i++)
        if (Math.abs(aTilde.get(i)) < Math.sqrt(olscThreshold))
          aTilde.set(i, 0.0D);
    }
    PaceMatrix YTilde = new PaceMatrix(new PaceMatrix(aTilde).times(sde));
    X.rsolve(YTilde, pvt, pvt.size());
    DoubleVector betaTilde = YTilde.getColumn(0).unpivoting(pvt, kr);
    
    return betaTilde.getArrayCopy();
  }
  






  public boolean checkForMissing(Instance instance, Instances model)
  {
    for (int j = 0; j < instance.numAttributes(); j++) {
      if ((j != model.classIndex()) && 
        (instance.isMissing(j))) {
        return true;
      }
    }
    
    return false;
  }
  







  private double[][] getTransformedDataMatrix(Instances data, int classIndex)
  {
    int numInstances = data.numInstances();
    int numAttributes = data.numAttributes();
    int middle = classIndex;
    if (middle < 0) {
      middle = numAttributes;
    }
    
    double[][] result = new double[numInstances][numAttributes];
    
    for (int i = 0; i < numInstances; i++) {
      Instance inst = data.instance(i);
      
      result[i][0] = 1.0D;
      

      for (int j = 0; j < middle; j++) {
        result[i][(j + 1)] = inst.value(j);
      }
      for (int j = middle + 1; j < numAttributes; j++) {
        result[i][j] = inst.value(j);
      }
    }
    return result;
  }
  







  public double classifyInstance(Instance instance)
    throws Exception
  {
    if (m_Coefficients == null) {
      throw new Exception("Pace Regression: No model built yet.");
    }
    

    if (checkForMissing(instance, m_Model)) {
      throw new NoSupportForMissingValuesException("Can't handle missing values!");
    }
    

    return regressionPrediction(instance, m_Coefficients);
  }
  






  public String toString()
  {
    if (m_Coefficients == null) {
      return "Pace Regression: No model built yet.";
    }
    
    StringBuffer text = new StringBuffer();
    
    text.append("\nPace Regression Model\n\n");
    
    text.append(m_Model.classAttribute().name() + " =\n\n");
    int index = 0;
    
    text.append(Utils.doubleToString(m_Coefficients[0], 12, 4));
    

    for (int i = 1; i < m_Coefficients.length; i++)
    {

      if (index == m_ClassIndex) { index++;
      }
      if (m_Coefficients[i] != 0.0D)
      {
        text.append(" +\n");
        text.append(Utils.doubleToString(m_Coefficients[i], 12, 4) + " * ");
        
        text.append(m_Model.attribute(index).name());
      }
      index++;
    }
    
    return text.toString();
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    newVector.addElement(new Option("\tProduce debugging output.\n\t(default no debugging output)", "D", 0, "-D"));
    

    newVector.addElement(new Option("\tThe estimator can be one of the following:\n\t\teb -- Empirical Bayes estimator for noraml mixture (default)\n\t\tnested -- Optimal nested model selector for normal mixture\n\t\tsubset -- Optimal subset selector for normal mixture\n\t\tpace2 -- PACE2 for Chi-square mixture\n\t\tpace4 -- PACE4 for Chi-square mixture\n\t\tpace6 -- PACE6 for Chi-square mixture\n\n\t\tols -- Ordinary least squares estimator\n\t\taic -- AIC estimator\n\t\tbic -- BIC estimator\n\t\tric -- RIC estimator\n\t\tolsc -- Ordinary least squares subset selector with a threshold", "E", 0, "-E <estimator>"));
    











    newVector.addElement(new Option("\tThreshold value for the OLSC estimator", "S", 0, "-S <threshold value>"));
    
    return newVector.elements();
  }
  
































  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String estimator = Utils.getOption('E', options);
    if (estimator.equals("ols")) { paceEstimator = 0;
    } else if (estimator.equals("olsc")) { paceEstimator = 7;
    } else if ((estimator.equals("eb")) || (estimator.equals(""))) {
      paceEstimator = 1;
    } else if (estimator.equals("nested")) { paceEstimator = 2;
    } else if (estimator.equals("subset")) { paceEstimator = 3;
    } else if (estimator.equals("pace2")) { paceEstimator = 4;
    } else if (estimator.equals("pace4")) { paceEstimator = 5;
    } else if (estimator.equals("pace6")) { paceEstimator = 6;
    } else if (estimator.equals("aic")) { paceEstimator = 8;
    } else if (estimator.equals("bic")) { paceEstimator = 9;
    } else if (estimator.equals("ric")) paceEstimator = 10; else {
      throw new WekaException("unknown estimator " + estimator + " for -E option");
    }
    
    String string = Utils.getOption('S', options);
    if (!string.equals("")) { olscThreshold = Double.parseDouble(string);
    }
  }
  





  public double[] coefficients()
  {
    double[] coefficients = new double[m_Coefficients.length];
    for (int i = 0; i < coefficients.length; i++) {
      coefficients[i] = m_Coefficients[i];
    }
    return coefficients;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[6];
    int current = 0;
    
    if (getDebug()) {
      options[(current++)] = "-D";
    }
    
    options[(current++)] = "-E";
    switch (paceEstimator) {
    case 0:  options[(current++)] = "ols";
      break;
    case 7:  options[(current++)] = "olsc";
      options[(current++)] = "-S";
      options[(current++)] = ("" + olscThreshold);
      break;
    case 1:  options[(current++)] = "eb";
      break;
    case 2:  options[(current++)] = "nested";
      break;
    case 3:  options[(current++)] = "subset";
      break;
    case 4:  options[(current++)] = "pace2";
      break;
    case 5:  options[(current++)] = "pace4";
      break;
    case 6:  options[(current++)] = "pace6";
      break;
    case 8:  options[(current++)] = "aic";
      break;
    case 9:  options[(current++)] = "bic";
      break;
    case 10:  options[(current++)] = "ric";
    }
    
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public int numParameters()
  {
    return m_Coefficients.length - 1;
  }
  




  public String debugTipText()
  {
    return "Output debug information to the console.";
  }
  





  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  





  public boolean getDebug()
  {
    return m_Debug;
  }
  




  public String estimatorTipText()
  {
    return "The estimator to use.\n\neb -- Empirical Bayes estimator for noraml mixture (default)\nnested -- Optimal nested model selector for normal mixture\nsubset -- Optimal subset selector for normal mixture\npace2 -- PACE2 for Chi-square mixture\npace4 -- PACE4 for Chi-square mixture\npace6 -- PACE6 for Chi-square mixture\nols -- Ordinary least squares estimator\naic -- AIC estimator\nbic -- BIC estimator\nric -- RIC estimator\nolsc -- Ordinary least squares subset selector with a threshold";
  }
  
















  public SelectedTag getEstimator()
  {
    return new SelectedTag(paceEstimator, TAGS_ESTIMATOR);
  }
  





  public void setEstimator(SelectedTag estimator)
  {
    if (estimator.getTags() == TAGS_ESTIMATOR) {
      paceEstimator = estimator.getSelectedTag().getID();
    }
  }
  




  public String thresholdTipText()
  {
    return "Threshold for the olsc estimator.";
  }
  





  public void setThreshold(double newThreshold)
  {
    olscThreshold = newThreshold;
  }
  





  public double getThreshold()
  {
    return olscThreshold;
  }
  













  private double regressionPrediction(Instance transformedInstance, double[] coefficients)
    throws Exception
  {
    int column = 0;
    double result = coefficients[column];
    for (int j = 0; j < transformedInstance.numAttributes(); j++) {
      if (m_ClassIndex != j) {
        column++;
        result += coefficients[column] * transformedInstance.value(j);
      }
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new PaceRegression(), argv);
  }
}

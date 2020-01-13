package weka.classifiers.functions;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;



















































































public class LinearRegression
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler
{
  static final long serialVersionUID = -3364580862046573747L;
  private double[] m_Coefficients;
  private boolean[] m_SelectedAttributes;
  private Instances m_TransformedData;
  private ReplaceMissingValues m_MissingFilter;
  private NominalToBinary m_TransformFilter;
  private double m_ClassStdDev;
  private double m_ClassMean;
  private int m_ClassIndex;
  private double[] m_Means;
  private double[] m_StdDevs;
  private boolean b_Debug;
  private int m_AttributeSelection;
  public static final int SELECTION_M5 = 0;
  public static final int SELECTION_NONE = 1;
  public static final int SELECTION_GREEDY = 2;
  public static final Tag[] TAGS_SELECTION = { new Tag(1, "No attribute selection"), new Tag(0, "M5 method"), new Tag(2, "Greedy method") };
  





  private boolean m_EliminateColinearAttributes = true;
  

  private boolean m_checksTurnedOff = false;
  

  private double m_Ridge = 1.0E-8D;
  

  public LinearRegression() {}
  

  public void turnChecksOff()
  {
    m_checksTurnedOff = true;
  }
  




  public void turnChecksOn()
  {
    m_checksTurnedOff = false;
  }
  




  public String globalInfo()
  {
    return "Class for using linear regression for prediction. Uses the Akaike criterion for model selection, and is able to deal with weighted instances.";
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    if (!m_checksTurnedOff)
    {
      getCapabilities().testWithFail(data);
      

      data = new Instances(data);
      data.deleteWithMissingClass();
    }
    

    if (!m_checksTurnedOff) {
      m_TransformFilter = new NominalToBinary();
      m_TransformFilter.setInputFormat(data);
      data = Filter.useFilter(data, m_TransformFilter);
      m_MissingFilter = new ReplaceMissingValues();
      m_MissingFilter.setInputFormat(data);
      data = Filter.useFilter(data, m_MissingFilter);
      data.deleteWithMissingClass();
    } else {
      m_TransformFilter = null;
      m_MissingFilter = null;
    }
    
    m_ClassIndex = data.classIndex();
    m_TransformedData = data;
    

    m_SelectedAttributes = new boolean[data.numAttributes()];
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i != m_ClassIndex) {
        m_SelectedAttributes[i] = true;
      }
    }
    m_Coefficients = null;
    

    m_Means = new double[data.numAttributes()];
    m_StdDevs = new double[data.numAttributes()];
    for (int j = 0; j < data.numAttributes(); j++) {
      if (j != data.classIndex()) {
        m_Means[j] = data.meanOrMode(j);
        m_StdDevs[j] = Math.sqrt(data.variance(j));
        if (m_StdDevs[j] == 0.0D) {
          m_SelectedAttributes[j] = false;
        }
      }
    }
    
    m_ClassStdDev = Math.sqrt(data.variance(m_TransformedData.classIndex()));
    m_ClassMean = data.meanOrMode(m_TransformedData.classIndex());
    

    findBestModel();
    

    m_TransformedData = new Instances(data, 0);
  }
  







  public double classifyInstance(Instance instance)
    throws Exception
  {
    Instance transformedInstance = instance;
    if (!m_checksTurnedOff) {
      m_TransformFilter.input(transformedInstance);
      m_TransformFilter.batchFinished();
      transformedInstance = m_TransformFilter.output();
      m_MissingFilter.input(transformedInstance);
      m_MissingFilter.batchFinished();
      transformedInstance = m_MissingFilter.output();
    }
    

    return regressionPrediction(transformedInstance, m_SelectedAttributes, m_Coefficients);
  }
  







  public String toString()
  {
    if (m_TransformedData == null) {
      return "Linear Regression: No model built yet.";
    }
    try {
      StringBuffer text = new StringBuffer();
      int column = 0;
      boolean first = true;
      
      text.append("\nLinear Regression Model\n\n");
      
      text.append(m_TransformedData.classAttribute().name() + " =\n\n");
      for (int i = 0; i < m_TransformedData.numAttributes(); i++) {
        if ((i != m_ClassIndex) && (m_SelectedAttributes[i] != 0))
        {
          if (!first) {
            text.append(" +\n");
          } else
            first = false;
          text.append(Utils.doubleToString(m_Coefficients[column], 12, 4) + " * ");
          
          text.append(m_TransformedData.attribute(i).name());
          column++;
        }
      }
      text.append(" +\n" + Utils.doubleToString(m_Coefficients[column], 12, 4));
      
      return text.toString();
    } catch (Exception e) {}
    return "Can't print Linear Regression!";
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    newVector.addElement(new Option("\tProduce debugging output.\n\t(default no debugging output)", "D", 0, "-D"));
    

    newVector.addElement(new Option("\tSet the attribute selection method to use. 1 = None, 2 = Greedy.\n\t(default 0 = M5' method)", "S", 1, "-S <number of selection method>"));
    


    newVector.addElement(new Option("\tDo not try to eliminate colinear attributes.\n", "C", 0, "-C"));
    

    newVector.addElement(new Option("\tSet ridge parameter (default 1.0e-8).\n", "R", 1, "-R <double>"));
    
    return newVector.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    String selectionString = Utils.getOption('S', options);
    if (selectionString.length() != 0) {
      setAttributeSelectionMethod(new SelectedTag(Integer.parseInt(selectionString), TAGS_SELECTION));
    }
    else
    {
      setAttributeSelectionMethod(new SelectedTag(0, TAGS_SELECTION));
    }
    
    String ridgeString = Utils.getOption('R', options);
    if (ridgeString.length() != 0) {
      setRidge(new Double(ridgeString).doubleValue());
    } else {
      setRidge(1.0E-8D);
    }
    setDebug(Utils.getFlag('D', options));
    setEliminateColinearAttributes(!Utils.getFlag('C', options));
  }
  





  public double[] coefficients()
  {
    double[] coefficients = new double[m_SelectedAttributes.length + 1];
    int counter = 0;
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      if ((m_SelectedAttributes[i] != 0) && (i != m_ClassIndex)) {
        coefficients[i] = m_Coefficients[(counter++)];
      }
    }
    coefficients[m_SelectedAttributes.length] = m_Coefficients[counter];
    return coefficients;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[6];
    int current = 0;
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + getAttributeSelectionMethod().getSelectedTag().getID());
    
    if (getDebug()) {
      options[(current++)] = "-D";
    }
    if (!getEliminateColinearAttributes()) {
      options[(current++)] = "-C";
    }
    options[(current++)] = "-R";
    options[(current++)] = ("" + getRidge());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String ridgeTipText()
  {
    return "The value of the Ridge parameter.";
  }
  





  public double getRidge()
  {
    return m_Ridge;
  }
  





  public void setRidge(double newRidge)
  {
    m_Ridge = newRidge;
  }
  




  public String eliminateColinearAttributesTipText()
  {
    return "Eliminate colinear attributes.";
  }
  





  public boolean getEliminateColinearAttributes()
  {
    return m_EliminateColinearAttributes;
  }
  





  public void setEliminateColinearAttributes(boolean newEliminateColinearAttributes)
  {
    m_EliminateColinearAttributes = newEliminateColinearAttributes;
  }
  





  public int numParameters()
  {
    return m_Coefficients.length - 1;
  }
  




  public String attributeSelectionMethodTipText()
  {
    return "Set the method used to select attributes for use in the linear regression. Available methods are: no attribute selection, attribute selection using M5's method (step through the attributes removing the one with the smallest standardised coefficient until no improvement is observed in the estimate of the error given by the Akaike information criterion), and a greedy selection using the Akaike information metric.";
  }
  












  public void setAttributeSelectionMethod(SelectedTag method)
  {
    if (method.getTags() == TAGS_SELECTION) {
      m_AttributeSelection = method.getSelectedTag().getID();
    }
  }
  






  public SelectedTag getAttributeSelectionMethod()
  {
    return new SelectedTag(m_AttributeSelection, TAGS_SELECTION);
  }
  




  public String debugTipText()
  {
    return "Outputs debug information to the console.";
  }
  





  public void setDebug(boolean debug)
  {
    b_Debug = debug;
  }
  





  public boolean getDebug()
  {
    return b_Debug;
  }
  











  private boolean deselectColinearAttributes(boolean[] selectedAttributes, double[] coefficients)
  {
    double maxSC = 1.5D;
    int maxAttr = -1;int coeff = 0;
    for (int i = 0; i < selectedAttributes.length; i++) {
      if (selectedAttributes[i] != 0) {
        double SC = Math.abs(coefficients[coeff] * m_StdDevs[i] / m_ClassStdDev);
        
        if (SC > maxSC) {
          maxSC = SC;
          maxAttr = i;
        }
        coeff++;
      }
    }
    if (maxAttr >= 0) {
      selectedAttributes[maxAttr] = false;
      if (b_Debug) {
        System.out.println("Deselected colinear attribute:" + (maxAttr + 1) + " with standardised coefficient: " + maxSC);
      }
      
      return true;
    }
    return false;
  }
  







  private void findBestModel()
    throws Exception
  {
    int numInstances = m_TransformedData.numInstances();
    
    if (b_Debug) {
      System.out.println(new Instances(m_TransformedData, 0).toString());
    }
    
    do
    {
      m_Coefficients = doRegression(m_SelectedAttributes);
    } while ((m_EliminateColinearAttributes) && (deselectColinearAttributes(m_SelectedAttributes, m_Coefficients)));
    



    int numAttributes = 1;
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      if (m_SelectedAttributes[i] != 0) {
        numAttributes++;
      }
    }
    
    double fullMSE = calculateSE(m_SelectedAttributes, m_Coefficients);
    double akaike = numInstances - numAttributes + 2 * numAttributes;
    if (b_Debug) {
      System.out.println("Initial Akaike value: " + akaike);
    }
    

    int currentNumAttributes = numAttributes;
    boolean improved; switch (m_AttributeSelection)
    {

    case 2: 
      do
      {
        boolean[] currentSelected = (boolean[])m_SelectedAttributes.clone();
        improved = false;
        currentNumAttributes--;
        
        for (int i = 0; i < m_SelectedAttributes.length; i++) {
          if (currentSelected[i] != 0)
          {

            currentSelected[i] = false;
            double[] currentCoeffs = doRegression(currentSelected);
            double currentMSE = calculateSE(currentSelected, currentCoeffs);
            double currentAkaike = currentMSE / fullMSE * (numInstances - numAttributes) + 2 * currentNumAttributes;
            

            if (b_Debug) {
              System.out.println("(akaike: " + currentAkaike);
            }
            

            if (currentAkaike < akaike) {
              if (b_Debug) {
                System.err.println("Removing attribute " + (i + 1) + " improved Akaike: " + currentAkaike);
              }
              
              improved = true;
              akaike = currentAkaike;
              System.arraycopy(currentSelected, 0, m_SelectedAttributes, 0, m_SelectedAttributes.length);
              

              m_Coefficients = currentCoeffs;
            }
            currentSelected[i] = true;
          }
        }
      } while (improved);
      break;
    


    case 0: 
      for (;;)
      {
        improved = false;
        currentNumAttributes--;
        

        double minSC = 0.0D;
        int minAttr = -1;int coeff = 0;
        for (int i = 0; i < m_SelectedAttributes.length; i++) {
          if (m_SelectedAttributes[i] != 0) {
            double SC = Math.abs(m_Coefficients[coeff] * m_StdDevs[i] / m_ClassStdDev);
            
            if ((coeff == 0) || (SC < minSC)) {
              minSC = SC;
              minAttr = i;
            }
            coeff++;
          }
        }
        

        if (minAttr >= 0) {
          m_SelectedAttributes[minAttr] = false;
          double[] currentCoeffs = doRegression(m_SelectedAttributes);
          double currentMSE = calculateSE(m_SelectedAttributes, currentCoeffs);
          double currentAkaike = currentMSE / fullMSE * (numInstances - numAttributes) + 2 * currentNumAttributes;
          

          if (b_Debug) {
            System.out.println("(akaike: " + currentAkaike);
          }
          

          if (currentAkaike < akaike) {
            if (b_Debug) {
              System.err.println("Removing attribute " + (minAttr + 1) + " improved Akaike: " + currentAkaike);
            }
            
            improved = true;
            akaike = currentAkaike;
            m_Coefficients = currentCoeffs;
          } else {
            m_SelectedAttributes[minAttr] = true;
          }
        }
        if (!improved) {
          break;
        }
      }
    }
    
  }
  












  private double calculateSE(boolean[] selectedAttributes, double[] coefficients)
    throws Exception
  {
    double mse = 0.0D;
    for (int i = 0; i < m_TransformedData.numInstances(); i++) {
      double prediction = regressionPrediction(m_TransformedData.instance(i), selectedAttributes, coefficients);
      

      double error = prediction - m_TransformedData.instance(i).classValue();
      mse += error * error;
    }
    return mse;
  }
  















  private double regressionPrediction(Instance transformedInstance, boolean[] selectedAttributes, double[] coefficients)
    throws Exception
  {
    double result = 0.0D;
    int column = 0;
    for (int j = 0; j < transformedInstance.numAttributes(); j++) {
      if ((m_ClassIndex != j) && (selectedAttributes[j] != 0))
      {
        result += coefficients[column] * transformedInstance.value(j);
        column++;
      }
    }
    result += coefficients[column];
    
    return result;
  }
  









  private double[] doRegression(boolean[] selectedAttributes)
    throws Exception
  {
    if (b_Debug) {
      System.out.print("doRegression(");
      for (int i = 0; i < selectedAttributes.length; i++) {
        System.out.print(" " + selectedAttributes[i]);
      }
      System.out.println(" )");
    }
    int numAttributes = 0;
    for (int i = 0; i < selectedAttributes.length; i++) {
      if (selectedAttributes[i] != 0) {
        numAttributes++;
      }
    }
    

    Matrix independent = null;Matrix dependent = null;
    if (numAttributes > 0) {
      independent = new Matrix(m_TransformedData.numInstances(), numAttributes);
      
      dependent = new Matrix(m_TransformedData.numInstances(), 1);
      for (int i = 0; i < m_TransformedData.numInstances(); i++) {
        Instance inst = m_TransformedData.instance(i);
        double sqrt_weight = Math.sqrt(inst.weight());
        int column = 0;
        for (int j = 0; j < m_TransformedData.numAttributes(); j++) {
          if (j == m_ClassIndex) {
            dependent.set(i, 0, inst.classValue() * sqrt_weight);
          }
          else if (selectedAttributes[j] != 0) {
            double value = inst.value(j) - m_Means[j];
            


            if (!m_checksTurnedOff) {
              value /= m_StdDevs[j];
            }
            independent.set(i, column, value * sqrt_weight);
            column++;
          }
        }
      }
    }
    




    double[] coefficients = new double[numAttributes + 1];
    if (numAttributes > 0) {
      double[] coeffsWithoutIntercept = independent.regression(dependent, m_Ridge).getCoefficients();
      
      System.arraycopy(coeffsWithoutIntercept, 0, coefficients, 0, numAttributes);
    }
    
    coefficients[numAttributes] = m_ClassMean;
    

    int column = 0;
    for (int i = 0; i < m_TransformedData.numAttributes(); i++) {
      if ((i != m_TransformedData.classIndex()) && (selectedAttributes[i] != 0))
      {



        if (!m_checksTurnedOff) {
          coefficients[column] /= m_StdDevs[i];
        }
        

        coefficients[(coefficients.length - 1)] -= coefficients[column] * m_Means[i];
        
        column++;
      }
    }
    
    return coefficients;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9770 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new LinearRegression(), argv);
  }
}

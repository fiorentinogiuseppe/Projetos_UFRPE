package weka.classifiers.meta;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Summarizable;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;














































































































public class CVParameterSelection
  extends RandomizableSingleClassifierEnhancer
  implements Drawable, Summarizable, TechnicalInformationHandler
{
  static final long serialVersionUID = -6529603380876641265L;
  protected String[] m_ClassifierOptions;
  protected String[] m_BestClassifierOptions;
  protected String[] m_InitOptions;
  protected double m_BestPerformance;
  public CVParameterSelection() {}
  
  protected class CVParameter
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -4668812017709421953L;
    private String m_ParamChar;
    private double m_Lower;
    private double m_Upper;
    private double m_Steps;
    private double m_ParamValue;
    private boolean m_AddAtEnd;
    private boolean m_RoundParam;
    
    public CVParameter(String param)
      throws Exception
    {
      String[] parts = param.split(" ");
      if ((parts.length < 4) || (parts.length > 5)) {
        throw new Exception("CVParameter " + param + ": four or five components expected!");
      }
      
      try
      {
        Double.parseDouble(parts[0]);
        throw new Exception("CVParameter " + param + ": Character parameter identifier expected");
      }
      catch (NumberFormatException n) {
        m_ParamChar = parts[0];
        
        try
        {
          m_Lower = Double.parseDouble(parts[1]);
        } catch (NumberFormatException n) {
          throw new Exception("CVParameter " + param + ": Numeric lower bound expected");
        }
        

        if (parts[2].equals("A")) {
          m_Upper = (m_Lower - 1.0D);
        } else if (parts[2].equals("I")) {
          m_Upper = (m_Lower - 2.0D);
        } else {
          try {
            m_Upper = Double.parseDouble(parts[2]);
            
            if (m_Upper < m_Lower) {
              throw new Exception("CVParameter " + param + ": Upper bound is less than lower bound");
            }
          }
          catch (NumberFormatException n) {
            throw new Exception("CVParameter " + param + ": Upper bound must be numeric, or 'A' or 'N'");
          }
        }
        
        try
        {
          m_Steps = Double.parseDouble(parts[3]);
        } catch (NumberFormatException n) {
          throw new Exception("CVParameter " + param + ": Numeric number of steps expected");
        }
        

        if ((parts.length == 5) && (parts[4].equals("R"))) {
          m_RoundParam = true;
        }
      }
    }
    




    public String toString()
    {
      String result = m_ParamChar + " " + m_Lower + " ";
      switch ((int)(m_Lower - m_Upper + 0.5D)) {
      case 1: 
        result = result + "A";
        break;
      case 2: 
        result = result + "I";
        break;
      default: 
        result = result + m_Upper;
      }
      
      result = result + " " + m_Steps;
      if (m_RoundParam) {
        result = result + " R";
      }
      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 8180 $");
    }
  }
  

















  protected FastVector m_CVParams = new FastVector();
  

  protected int m_NumAttributes;
  

  protected int m_TrainFoldSize;
  

  protected int m_NumFolds = 10;
  







  protected String[] createOptions()
  {
    String[] options = new String[m_ClassifierOptions.length + 2 * m_CVParams.size()];
    
    int start = 0;int end = options.length;
    

    for (int i = 0; i < m_CVParams.size(); i++) {
      CVParameter cvParam = (CVParameter)m_CVParams.elementAt(i);
      double paramValue = m_ParamValue;
      if (m_RoundParam)
      {
        paramValue = Math.rint(paramValue);
      }
      boolean isInt = paramValue - (int)paramValue == 0.0D;
      

      if (m_AddAtEnd) {
        options[(--end)] = ("" + ((m_RoundParam) || (isInt) ? Utils.doubleToString(paramValue, 4) : Double.valueOf(m_ParamValue)));
        

        options[(--end)] = ("-" + m_ParamChar);
      } else {
        options[(start++)] = ("-" + m_ParamChar);
        options[(start++)] = ("" + ((m_RoundParam) || (isInt) ? Utils.doubleToString(paramValue, 4) : Double.valueOf(m_ParamValue)));
      }
    }
    


    System.arraycopy(m_ClassifierOptions, 0, options, start, m_ClassifierOptions.length);
    


    return options;
  }
  










  protected void findParamsByCrossValidation(int depth, Instances trainData, Random random)
    throws Exception
  {
    if (depth < m_CVParams.size()) {
      CVParameter cvParam = (CVParameter)m_CVParams.elementAt(depth);
      
      double upper;
      switch ((int)(m_Lower - m_Upper + 0.5D)) {
      case 1: 
        upper = m_NumAttributes;
        break;
      case 2: 
        upper = m_TrainFoldSize;
        break;
      default: 
        upper = m_Upper;
      }
      
      double increment = (upper - m_Lower) / (m_Steps - 1.0D);
      m_ParamValue = m_Lower;
      for (; m_ParamValue <= upper; 
          CVParameter.access$018(cvParam, increment)) {
        findParamsByCrossValidation(depth + 1, trainData, random);
      }
    }
    else {
      Evaluation evaluation = new Evaluation(trainData);
      

      String[] options = createOptions();
      if (m_Debug) {
        System.err.print("Setting options for " + m_Classifier.getClass().getName() + ":");
        
        for (int i = 0; i < options.length; i++) {
          System.err.print(" " + options[i]);
        }
        System.err.println("");
      }
      m_Classifier.setOptions(options);
      for (int j = 0; j < m_NumFolds; j++)
      {


        Instances train = trainData.trainCV(m_NumFolds, j, new Random(1L));
        Instances test = trainData.testCV(m_NumFolds, j);
        m_Classifier.buildClassifier(train);
        evaluation.setPriors(train);
        evaluation.evaluateModel(m_Classifier, test, new Object[0]);
      }
      double error = evaluation.errorRate();
      if (m_Debug) {
        System.err.println("Cross-validated error rate: " + Utils.doubleToString(error, 6, 4));
      }
      
      if ((m_BestPerformance == -99.0D) || (error < m_BestPerformance))
      {
        m_BestPerformance = error;
        m_BestClassifierOptions = createOptions();
      }
    }
  }
  




  public String globalInfo()
  {
    return "Class for performing parameter selection by cross-validation for any classifier.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PHDTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "R. Kohavi");
    result.setValue(TechnicalInformation.Field.YEAR, "1995");
    result.setValue(TechnicalInformation.Field.TITLE, "Wrappers for Performance Enhancement and Oblivious Decision Graphs");
    result.setValue(TechnicalInformation.Field.SCHOOL, "Stanford University");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Department of Computer Science, Stanford University");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tNumber of folds used for cross validation (default 10).", "X", 1, "-X <number of folds>"));
    

    newVector.addElement(new Option("\tClassifier parameter options.\n\teg: \"N 1 5 10\" Sets an optimisation parameter for the\n\tclassifier with name -N, with lower bound 1, upper bound\n\t5, and 10 optimisation steps. The upper bound may be the\n\tcharacter 'A' or 'I' to substitute the number of\n\tattributes or instances in the training data,\n\trespectively. This parameter may be supplied more than\n\tonce to optimise over several classifier options\n\tsimultaneously.", "P", 1, "-P <classifier parameter>"));
    











    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  















































  public void setOptions(String[] options)
    throws Exception
  {
    String foldsString = Utils.getOption('X', options);
    if (foldsString.length() != 0) {
      setNumFolds(Integer.parseInt(foldsString));
    } else {
      setNumFolds(10);
    }
    

    m_CVParams = new FastVector();
    String cvParam;
    do { cvParam = Utils.getOption('P', options);
      if (cvParam.length() != 0) {
        addCVParameter(cvParam);
      }
    } while (cvParam.length() != 0);
    
    super.setOptions(options);
  }
  



  public String[] getOptions()
  {
    String[] superOptions;
    


    if (m_InitOptions != null) {
      try {
        m_Classifier.setOptions((String[])m_InitOptions.clone());
        superOptions = super.getOptions();
        m_Classifier.setOptions((String[])m_BestClassifierOptions.clone());
      } catch (Exception e) {
        throw new RuntimeException("CVParameterSelection: could not set options in getOptions().");
      }
      
    } else {
      superOptions = super.getOptions();
    }
    String[] options = new String[superOptions.length + m_CVParams.size() * 2 + 2];
    
    int current = 0;
    for (int i = 0; i < m_CVParams.size(); i++) {
      options[(current++)] = "-P";options[(current++)] = ("" + getCVParameter(i));
    }
    options[(current++)] = "-X";options[(current++)] = ("" + getNumFolds());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    return options;
  }
  




  public String[] getBestClassifierOptions()
  {
    return (String[])m_BestClassifierOptions.clone();
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    
    result.setMinimumNumberInstances(m_NumFolds);
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    Instances trainData = new Instances(instances);
    trainData.deleteWithMissingClass();
    
    if (!(m_Classifier instanceof OptionHandler)) {
      throw new IllegalArgumentException("Base classifier should be OptionHandler.");
    }
    m_InitOptions = m_Classifier.getOptions();
    m_BestPerformance = -99.0D;
    m_NumAttributes = trainData.numAttributes();
    Random random = new Random(m_Seed);
    trainData.randomize(random);
    m_TrainFoldSize = trainData.trainCV(m_NumFolds, 0).numInstances();
    

    if (m_CVParams.size() == 0) {
      m_Classifier.buildClassifier(trainData);
      m_BestClassifierOptions = m_InitOptions;
      return;
    }
    
    if (trainData.classAttribute().isNominal()) {
      trainData.stratify(m_NumFolds);
    }
    m_BestClassifierOptions = null;
    


    m_ClassifierOptions = m_Classifier.getOptions();
    for (int i = 0; i < m_CVParams.size(); i++) {
      Utils.getOption(m_CVParams.elementAt(i)).m_ParamChar, m_ClassifierOptions);
    }
    
    findParamsByCrossValidation(0, trainData, random);
    
    String[] options = (String[])m_BestClassifierOptions.clone();
    m_Classifier.setOptions(options);
    m_Classifier.buildClassifier(trainData);
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_Classifier.distributionForInstance(instance);
  }
  










  public void addCVParameter(String cvParam)
    throws Exception
  {
    CVParameter newCV = new CVParameter(cvParam);
    
    m_CVParams.addElement(newCV);
  }
  






  public String getCVParameter(int index)
  {
    if (m_CVParams.size() <= index) {
      return "";
    }
    return ((CVParameter)m_CVParams.elementAt(index)).toString();
  }
  




  public String CVParametersTipText()
  {
    return "Sets the scheme parameters which are to be set by cross-validation.\nThe format for each string should be:\nparam_char lower_bound upper_bound number_of_steps\neg to search a parameter -P from 1 to 10 by increments of 1:\n    \"P 1 10 10\" ";
  }
  










  public Object[] getCVParameters()
  {
    Object[] CVParams = m_CVParams.toArray();
    
    String[] params = new String[CVParams.length];
    
    for (int i = 0; i < CVParams.length; i++) {
      params[i] = CVParams[i].toString();
    }
    return params;
  }
  






  public void setCVParameters(Object[] params)
    throws Exception
  {
    FastVector backup = m_CVParams;
    m_CVParams = new FastVector();
    
    for (int i = 0; i < params.length; i++) {
      try {
        addCVParameter((String)params[i]);
      } catch (Exception ex) {
        m_CVParams = backup;throw ex;
      }
    }
  }
  



  public String numFoldsTipText()
  {
    return "Get the number of folds used for cross-validation.";
  }
  





  public int getNumFolds()
  {
    return m_NumFolds;
  }
  





  public void setNumFolds(int numFolds)
    throws Exception
  {
    if (numFolds < 0) {
      throw new IllegalArgumentException("Stacking: Number of cross-validation folds must be positive.");
    }
    
    m_NumFolds = numFolds;
  }
  






  public int graphType()
  {
    if ((m_Classifier instanceof Drawable)) {
      return ((Drawable)m_Classifier).graphType();
    }
    return 0;
  }
  





  public String graph()
    throws Exception
  {
    if ((m_Classifier instanceof Drawable))
      return ((Drawable)m_Classifier).graph();
    throw new Exception("Classifier: " + m_Classifier.getClass().getName() + " " + Utils.joinOptions(m_BestClassifierOptions) + " cannot be graphed");
  }
  








  public String toString()
  {
    if (m_InitOptions == null) {
      return "CVParameterSelection: No model built yet.";
    }
    String result = "Cross-validated Parameter selection.\nClassifier: " + m_Classifier.getClass().getName() + "\n";
    try
    {
      for (int i = 0; i < m_CVParams.size(); i++) {
        CVParameter cvParam = (CVParameter)m_CVParams.elementAt(i);
        result = result + "Cross-validation Parameter: '-" + m_ParamChar + "'" + " ranged from " + m_Lower + " to ";
        


        switch ((int)(m_Lower - m_Upper + 0.5D)) {
        case 1: 
          result = result + m_NumAttributes;
          break;
        case 2: 
          result = result + m_TrainFoldSize;
          break;
        default: 
          result = result + m_Upper;
        }
        
        result = result + " with " + m_Steps + " steps\n";
      }
    } catch (Exception ex) {
      result = result + ex.getMessage();
    }
    result = result + "Classifier Options: " + Utils.joinOptions(m_BestClassifierOptions) + "\n\n" + m_Classifier.toString();
    

    return result;
  }
  





  public String toSummaryString()
  {
    String result = "Selected values: " + Utils.joinOptions(m_BestClassifierOptions);
    
    return result + '\n';
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8180 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new CVParameterSelection(), argv);
  }
}

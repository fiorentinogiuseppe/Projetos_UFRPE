package weka.classifiers.functions;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.trees.lmt.LogisticBase;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;







































































































public class SimpleLogistic
  extends Classifier
  implements OptionHandler, AdditionalMeasureProducer, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 7397710626304705059L;
  protected LogisticBase m_boostedModel;
  protected NominalToBinary m_NominalToBinary = null;
  

  protected ReplaceMissingValues m_ReplaceMissingValues = null;
  

  protected int m_numBoostingIterations;
  

  protected int m_maxBoostingIterations = 500;
  

  protected int m_heuristicStop = 50;
  


  protected boolean m_useCrossValidation;
  


  protected boolean m_errorOnProbabilities;
  

  protected double m_weightTrimBeta = 0.0D;
  

  private boolean m_useAIC = false;
  


  public SimpleLogistic()
  {
    m_numBoostingIterations = 0;
    m_useCrossValidation = true;
    m_errorOnProbabilities = false;
    m_weightTrimBeta = 0.0D;
    m_useAIC = false;
  }
  






  public SimpleLogistic(int numBoostingIterations, boolean useCrossValidation, boolean errorOnProbabilities)
  {
    m_numBoostingIterations = numBoostingIterations;
    m_useCrossValidation = useCrossValidation;
    m_errorOnProbabilities = errorOnProbabilities;
    m_weightTrimBeta = 0.0D;
    m_useAIC = false;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    

    m_ReplaceMissingValues = new ReplaceMissingValues();
    m_ReplaceMissingValues.setInputFormat(data);
    data = Filter.useFilter(data, m_ReplaceMissingValues);
    

    m_NominalToBinary = new NominalToBinary();
    m_NominalToBinary.setInputFormat(data);
    data = Filter.useFilter(data, m_NominalToBinary);
    

    m_boostedModel = new LogisticBase(m_numBoostingIterations, m_useCrossValidation, m_errorOnProbabilities);
    m_boostedModel.setMaxIterations(m_maxBoostingIterations);
    m_boostedModel.setHeuristicStop(m_heuristicStop);
    m_boostedModel.setWeightTrimBeta(m_weightTrimBeta);
    m_boostedModel.setUseAIC(m_useAIC);
    

    m_boostedModel.buildClassifier(data);
  }
  








  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    m_ReplaceMissingValues.input(inst);
    inst = m_ReplaceMissingValues.output();
    m_NominalToBinary.input(inst);
    inst = m_NominalToBinary.output();
    

    return m_boostedModel.distributionForInstance(inst);
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tSet fixed number of iterations for LogitBoost", "I", 1, "-I <iterations>"));
    


    newVector.addElement(new Option("\tUse stopping criterion on training set (instead of\n\tcross-validation)", "S", 0, "-S"));
    



    newVector.addElement(new Option("\tUse error on probabilities (rmse) instead of\n\tmisclassification error for stopping criterion", "P", 0, "-P"));
    



    newVector.addElement(new Option("\tSet maximum number of boosting iterations", "M", 1, "-M <iterations>"));
    


    newVector.addElement(new Option("\tSet parameter for heuristic for early stopping of\n\tLogitBoost.\n\tIf enabled, the minimum is selected greedily, stopping\n\tif the current minimum has not changed for iter iterations.\n\tBy default, heuristic is enabled with value 50. Set to\n\tzero to disable heuristic.", "H", 1, "-H <iterations>"));
    







    newVector.addElement(new Option("\tSet beta for weight trimming for LogitBoost. Set to 0 for no weight trimming.\n", "W", 1, "-W <beta>"));
    

    newVector.addElement(new Option("\tThe AIC is used to choose the best iteration (instead of CV or training error).\n", "A", 0, "-A"));
    

    return newVector.elements();
  }
  









































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('I', options);
    if (optionString.length() != 0) {
      setNumBoostingIterations(new Integer(optionString).intValue());
    }
    
    setUseCrossValidation(!Utils.getFlag('S', options));
    setErrorOnProbabilities(Utils.getFlag('P', options));
    
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMaxBoostingIterations(new Integer(optionString).intValue());
    }
    
    optionString = Utils.getOption('H', options);
    if (optionString.length() != 0) {
      setHeuristicStop(new Integer(optionString).intValue());
    }
    
    optionString = Utils.getOption('W', options);
    if (optionString.length() != 0) {
      setWeightTrimBeta(new Double(optionString).doubleValue());
    }
    
    setUseAIC(Utils.getFlag('A', options));
    
    Utils.checkForRemainingOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] options = new String[11];
    int current = 0;
    
    options[(current++)] = "-I";
    options[(current++)] = ("" + getNumBoostingIterations());
    
    if (!getUseCrossValidation()) {
      options[(current++)] = "-S";
    }
    
    if (getErrorOnProbabilities()) {
      options[(current++)] = "-P";
    }
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + getMaxBoostingIterations());
    
    options[(current++)] = "-H";
    options[(current++)] = ("" + getHeuristicStop());
    
    options[(current++)] = "-W";
    options[(current++)] = ("" + getWeightTrimBeta());
    
    if (getUseAIC()) {
      options[(current++)] = "-A";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public int getNumBoostingIterations()
  {
    return m_numBoostingIterations;
  }
  



  public boolean getUseCrossValidation()
  {
    return m_useCrossValidation;
  }
  





  public boolean getErrorOnProbabilities()
  {
    return m_errorOnProbabilities;
  }
  




  public int getMaxBoostingIterations()
  {
    return m_maxBoostingIterations;
  }
  




  public int getHeuristicStop()
  {
    return m_heuristicStop;
  }
  


  public double getWeightTrimBeta()
  {
    return m_weightTrimBeta;
  }
  




  public boolean getUseAIC()
  {
    return m_useAIC;
  }
  




  public void setNumBoostingIterations(int n)
  {
    m_numBoostingIterations = n;
  }
  




  public void setUseCrossValidation(boolean l)
  {
    m_useCrossValidation = l;
  }
  





  public void setErrorOnProbabilities(boolean l)
  {
    m_errorOnProbabilities = l;
  }
  




  public void setMaxBoostingIterations(int n)
  {
    m_maxBoostingIterations = n;
  }
  




  public void setHeuristicStop(int n)
  {
    if (n == 0) {
      m_heuristicStop = m_maxBoostingIterations;
    } else {
      m_heuristicStop = n;
    }
  }
  

  public void setWeightTrimBeta(double n)
  {
    m_weightTrimBeta = n;
  }
  




  public void setUseAIC(boolean c)
  {
    m_useAIC = c;
  }
  





  public int getNumRegressions()
  {
    return m_boostedModel.getNumRegressions();
  }
  




  public String toString()
  {
    if (m_boostedModel == null) return "No model built";
    return "SimpleLogistic:\n" + m_boostedModel.toString();
  }
  






  public double measureAttributesUsed()
  {
    return m_boostedModel.percentAttributesUsed();
  }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(3);
    newVector.addElement("measureAttributesUsed");
    newVector.addElement("measureNumIterations");
    return newVector.elements();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureAttributesUsed") == 0)
      return measureAttributesUsed();
    if (additionalMeasureName.compareToIgnoreCase("measureNumIterations") == 0) {
      return getNumRegressions();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (SimpleLogistic)");
  }
  







  public String globalInfo()
  {
    return "Classifier for building linear logistic regression models. LogitBoost with simple regression functions as base learners is used for fitting the logistic models. The optimal number of LogitBoost iterations to perform is cross-validated, which leads to automatic attribute selection. For more information see:\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Niels Landwehr and Mark Hall and Eibe Frank");
    result.setValue(TechnicalInformation.Field.TITLE, "Logistic Model Trees");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "2005");
    result.setValue(TechnicalInformation.Field.VOLUME, "95");
    result.setValue(TechnicalInformation.Field.PAGES, "161-205");
    result.setValue(TechnicalInformation.Field.NUMBER, "1-2");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Marc Sumner and Eibe Frank and Mark Hall");
    additional.setValue(TechnicalInformation.Field.TITLE, "Speeding up Logistic Model Tree Induction");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "9th European Conference on Principles and Practice of Knowledge Discovery in Databases");
    additional.setValue(TechnicalInformation.Field.YEAR, "2005");
    additional.setValue(TechnicalInformation.Field.PAGES, "675-683");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  




  public String numBoostingIterationsTipText()
  {
    return "Set fixed number of iterations for LogitBoost. If >= 0, this sets the number of LogitBoost iterations to perform. If < 0, the number is cross-validated or a stopping criterion on the training set is used (depending on the value of useCrossValidation).";
  }
  






  public String useCrossValidationTipText()
  {
    return "Sets whether the number of LogitBoost iterations is to be cross-validated or the stopping criterion on the training set should be used. If not set (and no fixed number of iterations was given), the number of LogitBoost iterations is used that minimizes the error on the training set (misclassification error or error on probabilities depending on errorOnProbabilities).";
  }
  







  public String errorOnProbabilitiesTipText()
  {
    return "Use error on the probabilties as error measure when determining the best number of LogitBoost iterations. If set, the number of LogitBoost iterations is chosen that minimizes the root mean squared error (either on the training set or in the cross-validation, depending on useCrossValidation).";
  }
  






  public String maxBoostingIterationsTipText()
  {
    return "Sets the maximum number of iterations for LogitBoost. Default value is 500, for very small/large datasets a lower/higher value might be preferable.";
  }
  





  public String heuristicStopTipText()
  {
    return "If heuristicStop > 0, the heuristic for greedy stopping while cross-validating the number of LogitBoost iterations is enabled. This means LogitBoost is stopped if no new error minimum has been reached in the last heuristicStop iterations. It is recommended to use this heuristic, it gives a large speed-up especially on small datasets. The default value is 50.";
  }
  







  public String weightTrimBetaTipText()
  {
    return "Set the beta value used for weight trimming in LogitBoost. Only instances carrying (1 - beta)% of the weight from previous iteration are used in the next iteration. Set to 0 for no weight trimming. The default value is 0.";
  }
  







  public String useAICTipText()
  {
    return "The AIC is used to determine when to stop LogitBoost iterations (instead of cross-validation or training error).";
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new SimpleLogistic(), argv);
  }
}

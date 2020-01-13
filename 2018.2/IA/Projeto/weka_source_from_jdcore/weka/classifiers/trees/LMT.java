package weka.classifiers.trees;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.trees.j48.C45ModelSelection;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.lmt.LMTNode;
import weka.classifiers.trees.lmt.ResidualModelSelection;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
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
import weka.filters.Filter;
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
























































































































public class LMT
  extends Classifier
  implements OptionHandler, AdditionalMeasureProducer, Drawable, TechnicalInformationHandler
{
  static final long serialVersionUID = -1113212459618104943L;
  protected ReplaceMissingValues m_replaceMissing;
  protected NominalToBinary m_nominalToBinary;
  protected LMTNode m_tree;
  protected boolean m_fastRegression;
  protected boolean m_convertNominal;
  protected boolean m_splitOnResiduals;
  protected boolean m_errorOnProbabilities;
  protected int m_minNumInstances;
  protected int m_numBoostingIterations;
  protected double m_weightTrimBeta;
  private boolean m_useAIC = false;
  


  public LMT()
  {
    m_fastRegression = true;
    m_numBoostingIterations = -1;
    m_minNumInstances = 15;
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
    

    Instances filteredData = new Instances(data);
    filteredData.deleteWithMissingClass();
    

    m_replaceMissing = new ReplaceMissingValues();
    m_replaceMissing.setInputFormat(filteredData);
    filteredData = Filter.useFilter(filteredData, m_replaceMissing);
    

    if (m_convertNominal) {
      m_nominalToBinary = new NominalToBinary();
      m_nominalToBinary.setInputFormat(filteredData);
      filteredData = Filter.useFilter(filteredData, m_nominalToBinary);
    }
    
    int minNumInstances = 2;
    
    ModelSelection modSelection;
    ModelSelection modSelection;
    if (m_splitOnResiduals) {
      modSelection = new ResidualModelSelection(minNumInstances);
    } else {
      modSelection = new C45ModelSelection(minNumInstances, filteredData);
    }
    

    m_tree = new LMTNode(modSelection, m_numBoostingIterations, m_fastRegression, m_errorOnProbabilities, m_minNumInstances, m_weightTrimBeta, m_useAIC);
    

    m_tree.buildClassifier(filteredData);
    
    if ((modSelection instanceof C45ModelSelection)) { ((C45ModelSelection)modSelection).cleanup();
    }
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    m_replaceMissing.input(instance);
    instance = m_replaceMissing.output();
    

    if (m_convertNominal) {
      m_nominalToBinary.input(instance);
      instance = m_nominalToBinary.output();
    }
    
    return m_tree.distributionForInstance(instance);
  }
  






  public double classifyInstance(Instance instance)
    throws Exception
  {
    double maxProb = -1.0D;
    int maxIndex = 0;
    

    double[] probs = distributionForInstance(instance);
    for (int j = 0; j < instance.numClasses(); j++) {
      if (Utils.gr(probs[j], maxProb)) {
        maxIndex = j;
        maxProb = probs[j];
      }
    }
    return maxIndex;
  }
  




  public String toString()
  {
    if (m_tree != null) {
      return "Logistic model tree \n------------------\n" + m_tree.toString();
    }
    return "No tree build";
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(8);
    
    newVector.addElement(new Option("\tBinary splits (convert nominal attributes to binary ones)", "B", 0, "-B"));
    

    newVector.addElement(new Option("\tSplit on residuals instead of class values", "R", 0, "-R"));
    

    newVector.addElement(new Option("\tUse cross-validation for boosting at all nodes (i.e., disable heuristic)", "C", 0, "-C"));
    

    newVector.addElement(new Option("\tUse error on probabilities instead of misclassification error for stopping criterion of LogitBoost.", "P", 0, "-P"));
    


    newVector.addElement(new Option("\tSet fixed number of iterations for LogitBoost (instead of using cross-validation)", "I", 1, "-I <numIterations>"));
    


    newVector.addElement(new Option("\tSet minimum number of instances at which a node can be split (default 15)", "M", 1, "-M <numInstances>"));
    

    newVector.addElement(new Option("\tSet beta for weight trimming for LogitBoost. Set to 0 (default) for no weight trimming.", "W", 1, "-W <beta>"));
    

    newVector.addElement(new Option("\tThe AIC is used to choose the best iteration.", "A", 0, "-A"));
    

    return newVector.elements();
  }
  


































  public void setOptions(String[] options)
    throws Exception
  {
    setConvertNominal(Utils.getFlag('B', options));
    setSplitOnResiduals(Utils.getFlag('R', options));
    setFastRegression(!Utils.getFlag('C', options));
    setErrorOnProbabilities(Utils.getFlag('P', options));
    
    String optionString = Utils.getOption('I', options);
    if (optionString.length() != 0) {
      setNumBoostingIterations(new Integer(optionString).intValue());
    }
    
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMinNumInstances(new Integer(optionString).intValue());
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
    
    if (getConvertNominal()) {
      options[(current++)] = "-B";
    }
    
    if (getSplitOnResiduals()) {
      options[(current++)] = "-R";
    }
    
    if (!getFastRegression()) {
      options[(current++)] = "-C";
    }
    
    if (getErrorOnProbabilities()) {
      options[(current++)] = "-P";
    }
    
    options[(current++)] = "-I";
    options[(current++)] = ("" + getNumBoostingIterations());
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + getMinNumInstances());
    
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
  


  public double getWeightTrimBeta()
  {
    return m_weightTrimBeta;
  }
  




  public boolean getUseAIC()
  {
    return m_useAIC;
  }
  


  public void setWeightTrimBeta(double n)
  {
    m_weightTrimBeta = n;
  }
  




  public void setUseAIC(boolean c)
  {
    m_useAIC = c;
  }
  




  public boolean getConvertNominal()
  {
    return m_convertNominal;
  }
  




  public boolean getSplitOnResiduals()
  {
    return m_splitOnResiduals;
  }
  




  public boolean getFastRegression()
  {
    return m_fastRegression;
  }
  




  public boolean getErrorOnProbabilities()
  {
    return m_errorOnProbabilities;
  }
  




  public int getNumBoostingIterations()
  {
    return m_numBoostingIterations;
  }
  




  public int getMinNumInstances()
  {
    return m_minNumInstances;
  }
  




  public void setConvertNominal(boolean c)
  {
    m_convertNominal = c;
  }
  




  public void setSplitOnResiduals(boolean c)
  {
    m_splitOnResiduals = c;
  }
  




  public void setFastRegression(boolean c)
  {
    m_fastRegression = c;
  }
  




  public void setErrorOnProbabilities(boolean c)
  {
    m_errorOnProbabilities = c;
  }
  




  public void setNumBoostingIterations(int c)
  {
    m_numBoostingIterations = c;
  }
  




  public void setMinNumInstances(int c)
  {
    m_minNumInstances = c;
  }
  




  public int graphType()
  {
    return 1;
  }
  





  public String graph()
    throws Exception
  {
    return m_tree.graph();
  }
  



  public int measureTreeSize()
  {
    return m_tree.numNodes();
  }
  



  public int measureNumLeaves()
  {
    return m_tree.numLeaves();
  }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(2);
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    
    return newVector.elements();
  }
  






  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0)
      return measureTreeSize();
    if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0) {
      return measureNumLeaves();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (LMT)");
  }
  






  public String globalInfo()
  {
    return "Classifier for building 'logistic model trees', which are classification trees with logistic regression functions at the leaves. The algorithm can deal with binary and multi-class target variables, numeric and nominal attributes and missing values.\n\nFor more information see: \n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Niels Landwehr and Mark Hall and Eibe Frank");
    result.setValue(TechnicalInformation.Field.TITLE, "Logistic Model Trees");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
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
  




  public String convertNominalTipText()
  {
    return "Convert all nominal attributes to binary ones before building the tree. This means that all splits in the final tree will be binary.";
  }
  





  public String splitOnResidualsTipText()
  {
    return "Set splitting criterion based on the residuals of LogitBoost. There are two possible splitting criteria for LMT: the default is to use the C4.5 splitting criterion that uses information gain on the class variable. The other splitting criterion tries to improve the purity in the residuals produces when fitting the logistic regression functions. The choice of the splitting criterion does not usually affect classification accuracy much, but can produce different trees.";
  }
  









  public String fastRegressionTipText()
  {
    return "Use heuristic that avoids cross-validating the number of Logit-Boost iterations at every node. When fitting the logistic regression functions at a node, LMT has to determine the number of LogitBoost iterations to run. Originally, this number was cross-validated at every node in the tree. To save time, this heuristic cross-validates the number only once and then uses that number at every node in the tree. Usually this does not decrease accuracy but improves runtime considerably.";
  }
  









  public String errorOnProbabilitiesTipText()
  {
    return "Minimize error on probabilities instead of misclassification error when cross-validating the number of LogitBoost iterations. When set, the number of LogitBoost iterations is chosen that minimizes the root mean squared error instead of the misclassification error.";
  }
  






  public String numBoostingIterationsTipText()
  {
    return "Set a fixed number of iterations for LogitBoost. If >= 0, this sets a fixed number of LogitBoost iterations that is used everywhere in the tree. If < 0, the number is cross-validated.";
  }
  





  public String minNumInstancesTipText()
  {
    return "Set the minimum number of instances at which a node is considered for splitting. The default value is 15.";
  }
  





  public String weightTrimBetaTipText()
  {
    return "Set the beta value used for weight trimming in LogitBoost. Only instances carrying (1 - beta)% of the weight from previous iteration are used in the next iteration. Set to 0 for no weight trimming. The default value is 0.";
  }
  







  public String useAICTipText()
  {
    return "The AIC is used to determine when to stop LogitBoost iterations. The default is not to use AIC.";
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5535 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new LMT(), argv);
  }
}

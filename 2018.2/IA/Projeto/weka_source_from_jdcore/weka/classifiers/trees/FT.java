package weka.classifiers.trees;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.trees.ft.FTInnerNode;
import weka.classifiers.trees.ft.FTLeavesNode;
import weka.classifiers.trees.ft.FTNode;
import weka.classifiers.trees.ft.FTtree;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
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
import weka.filters.Filter;
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
























































































































public class FT
  extends Classifier
  implements OptionHandler, AdditionalMeasureProducer, Drawable, TechnicalInformationHandler
{
  static final long serialVersionUID = -1113212459618105000L;
  protected ReplaceMissingValues m_replaceMissing;
  protected NominalToBinary m_nominalToBinary;
  protected FTtree m_tree;
  protected boolean m_convertNominal;
  protected boolean m_errorOnProbabilities;
  protected int m_minNumInstances;
  protected int m_numBoostingIterations;
  protected int m_modelType;
  protected double m_weightTrimBeta;
  protected boolean m_useAIC;
  public static final int MODEL_FT = 0;
  public static final int MODEL_FTLeaves = 1;
  public static final int MODEL_FTInner = 2;
  public static final Tag[] TAGS_MODEL = { new Tag(0, "FT"), new Tag(1, "FTLeaves"), new Tag(2, "FTInner") };
  







  public FT()
  {
    m_numBoostingIterations = 15;
    m_minNumInstances = 15;
    m_weightTrimBeta = 0.0D;
    m_useAIC = false;
    m_modelType = 0;
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
    


    if (m_modelType == 0) {
      m_tree = new FTNode(m_errorOnProbabilities, m_numBoostingIterations, m_minNumInstances, m_weightTrimBeta, m_useAIC);
    }
    

    if (m_modelType == 1) {
      m_tree = new FTLeavesNode(m_errorOnProbabilities, m_numBoostingIterations, m_minNumInstances, m_weightTrimBeta, m_useAIC);
    }
    

    if (m_modelType == 2) {
      m_tree = new FTInnerNode(m_errorOnProbabilities, m_numBoostingIterations, m_minNumInstances, m_weightTrimBeta, m_useAIC);
    }
    

    m_tree.buildClassifier(filteredData);
    
    m_tree.prune();
    m_tree.assignIDs(0);
    m_tree.cleanup();
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
      if (m_modelType == 0) {
        return "FT tree \n------------------\n" + m_tree.toString();
      }
      if (m_modelType == 1) {
        return "FT Leaves tree \n------------------\n" + m_tree.toString();
      }
      return "FT Inner tree \n------------------\n" + m_tree.toString();
    }
    
    return "No tree built";
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(8);
    
    newVector.addElement(new Option("\tBinary splits (convert nominal attributes to binary ones) ", "B", 0, "-B"));
    

    newVector.addElement(new Option("\tUse error on probabilities instead of misclassification error for stopping criterion of LogitBoost.", "P", 0, "-P"));
    


    newVector.addElement(new Option("\tSet fixed number of iterations for LogitBoost (instead of using cross-validation)", "I", 1, "-I <numIterations>"));
    


    newVector.addElement(new Option("\tSet Funtional Tree type to be generate:  0 for FT, 1 for FTLeaves and 2 for FTInner", "F", 1, "-F <modelType>"));
    


    newVector.addElement(new Option("\tSet minimum number of instances at which a node can be split (default 15)", "M", 1, "-M <numInstances>"));
    

    newVector.addElement(new Option("\tSet beta for weight trimming for LogitBoost. Set to 0 (default) for no weight trimming.", "W", 1, "-W <beta>"));
    

    newVector.addElement(new Option("\tThe AIC is used to choose the best iteration.", "A", 0, "-A"));
    

    return newVector.elements();
  }
  































  public void setOptions(String[] options)
    throws Exception
  {
    setBinSplit(Utils.getFlag('B', options));
    setErrorOnProbabilities(Utils.getFlag('P', options));
    
    String optionString = Utils.getOption('I', options);
    if (optionString.length() != 0) {
      setNumBoostingIterations(new Integer(optionString).intValue());
    }
    
    optionString = Utils.getOption('F', options);
    if (optionString.length() != 0) {
      setModelType(new SelectedTag(Integer.parseInt(optionString), TAGS_MODEL));
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
    
    if (getBinSplit()) {
      options[(current++)] = "-B";
    }
    
    if (getErrorOnProbabilities()) {
      options[(current++)] = "-P";
    }
    
    options[(current++)] = "-I";
    options[(current++)] = ("" + getNumBoostingIterations());
    
    options[(current++)] = "-F";
    
    options[(current++)] = ("" + getModelType().getSelectedTag().getID());
    
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
  




  public boolean getBinSplit()
  {
    return m_convertNominal;
  }
  




  public boolean getErrorOnProbabilities()
  {
    return m_errorOnProbabilities;
  }
  




  public int getNumBoostingIterations()
  {
    return m_numBoostingIterations;
  }
  




  public SelectedTag getModelType()
  {
    return new SelectedTag(m_modelType, TAGS_MODEL);
  }
  




  public void setModelType(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_MODEL) {
      int c = newMethod.getSelectedTag().getID();
      if ((c == 0) || (c == 1) || (c == 2)) {
        m_modelType = c;
      } else {
        throw new IllegalArgumentException("Wrong model type, -F value should be: 0, for FT, 1, for FTLeaves, and 2, for FTInner ");
      }
    }
  }
  





  public int getMinNumInstances()
  {
    return m_minNumInstances;
  }
  




  public void setBinSplit(boolean c)
  {
    m_convertNominal = c;
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
    throw new IllegalArgumentException(additionalMeasureName + " not supported (FT)");
  }
  






  public String globalInfo()
  {
    return "Classifier for building 'Functional trees', which are classification trees  that could have logistic regression functions at the inner nodes and/or leaves. The algorithm can deal with binary and multi-class target variables, numeric and nominal attributes and missing values.\n\nFor more information see: \n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Joao Gama");
    result.setValue(TechnicalInformation.Field.TITLE, "Functional Trees");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "2004");
    result.setValue(TechnicalInformation.Field.VOLUME, "55");
    result.setValue(TechnicalInformation.Field.PAGES, "219-250");
    result.setValue(TechnicalInformation.Field.NUMBER, "3");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Niels Landwehr and Mark Hall and Eibe Frank");
    additional.setValue(TechnicalInformation.Field.TITLE, "Logistic Model Trees");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Machine Learning");
    additional.setValue(TechnicalInformation.Field.YEAR, "2005");
    additional.setValue(TechnicalInformation.Field.VOLUME, "95");
    additional.setValue(TechnicalInformation.Field.PAGES, "161-205");
    additional.setValue(TechnicalInformation.Field.NUMBER, "1-2");
    
    return result;
  }
  




  public String modelTypeTipText()
  {
    return "The type of FT model. 0, for FT, 1, for FTLeaves, and 2, for FTInner";
  }
  





  public String binSplitTipText()
  {
    return "Convert all nominal attributes to binary ones before building the tree. This means that all splits in the final tree will be binary.";
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
    runClassifier(new FT(), argv);
  }
}

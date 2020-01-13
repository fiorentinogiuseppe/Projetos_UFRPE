package weka.classifiers.trees.m5;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;





















































































public abstract class M5Base
  extends Classifier
  implements AdditionalMeasureProducer, TechnicalInformationHandler
{
  private static final long serialVersionUID = -4022221950191647679L;
  private Instances m_instances;
  protected FastVector m_ruleSet;
  private boolean m_generateRules;
  private boolean m_unsmoothedPredictions;
  private ReplaceMissingValues m_replaceMissing;
  private NominalToBinary m_nominalToBinary;
  private RemoveUseless m_removeUseless;
  protected boolean m_saveInstances = false;
  



  protected boolean m_regressionTree;
  



  protected boolean m_useUnpruned = false;
  



  protected double m_minNumInstances = 4.0D;
  


  public M5Base()
  {
    m_generateRules = false;
    m_unsmoothedPredictions = false;
    m_useUnpruned = false;
    m_minNumInstances = 4.0D;
  }
  




  public String globalInfo()
  {
    return "M5Base. Implements base routines for generating M5 Model trees and rules\nThe original algorithm M5 was invented by R. Quinlan and Yong Wang made improvements.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ross J. Quinlan");
    result.setValue(TechnicalInformation.Field.TITLE, "Learning with Continuous Classes");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "5th Australian Joint Conference on Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.YEAR, "1992");
    result.setValue(TechnicalInformation.Field.PAGES, "343-348");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "World Scientific");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Singapore");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Y. Wang and I. H. Witten");
    additional.setValue(TechnicalInformation.Field.TITLE, "Induction of model trees for predicting continuous classes");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Poster papers of the 9th European Conference on Machine Learning");
    additional.setValue(TechnicalInformation.Field.YEAR, "1997");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tUse unpruned tree/rules", "N", 0, "-N"));
    

    newVector.addElement(new Option("\tUse unsmoothed predictions", "U", 0, "-U"));
    

    newVector.addElement(new Option("\tBuild regression tree/rule rather than a model tree/rule", "R", 0, "-R"));
    


    newVector.addElement(new Option("\tSet minimum number of instances per leaf\n\t(default 4)", "M", 1, "-M <minimum number of instances>"));
    

    return newVector.elements();
  }
  












  public void setOptions(String[] options)
    throws Exception
  {
    setUnpruned(Utils.getFlag('N', options));
    setUseUnsmoothed(Utils.getFlag('U', options));
    setBuildRegressionTree(Utils.getFlag('R', options));
    String optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMinNumInstances(new Double(optionString).doubleValue());
    }
    Utils.checkForRemainingOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    if (getUnpruned()) {
      options[(current++)] = "-N";
    }
    
    if (getUseUnsmoothed()) {
      options[(current++)] = "-U";
    }
    
    if (getBuildRegressionTree()) {
      options[(current++)] = "-R";
    }
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + getMinNumInstances());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String unprunedTipText()
  {
    return "Whether unpruned tree/rules are to be generated.";
  }
  




  public void setUnpruned(boolean unpruned)
  {
    m_useUnpruned = unpruned;
  }
  




  public boolean getUnpruned()
  {
    return m_useUnpruned;
  }
  





  public String generateRulesTipText()
  {
    return "Whether to generate rules (decision list) rather than a tree.";
  }
  




  protected void setGenerateRules(boolean u)
  {
    m_generateRules = u;
  }
  




  protected boolean getGenerateRules()
  {
    return m_generateRules;
  }
  





  public String useUnsmoothedTipText()
  {
    return "Whether to use unsmoothed predictions.";
  }
  




  public void setUseUnsmoothed(boolean s)
  {
    m_unsmoothedPredictions = s;
  }
  




  public boolean getUseUnsmoothed()
  {
    return m_unsmoothedPredictions;
  }
  





  public String buildRegressionTreeTipText()
  {
    return "Whether to generate a regression tree/rule instead of a model tree/rule.";
  }
  





  public boolean getBuildRegressionTree()
  {
    return m_regressionTree;
  }
  





  public void setBuildRegressionTree(boolean newregressionTree)
  {
    m_regressionTree = newregressionTree;
  }
  





  public String minNumInstancesTipText()
  {
    return "The minimum number of instances to allow at a leaf node.";
  }
  




  public void setMinNumInstances(double minNum)
  {
    m_minNumInstances = minNum;
  }
  




  public double getMinNumInstances()
  {
    return m_minNumInstances;
  }
  




  public Capabilities getCapabilities()
  {
    return new LinearRegression().getCapabilities();
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    m_instances = new Instances(data);
    
    m_replaceMissing = new ReplaceMissingValues();
    m_replaceMissing.setInputFormat(m_instances);
    m_instances = Filter.useFilter(m_instances, m_replaceMissing);
    
    m_nominalToBinary = new NominalToBinary();
    m_nominalToBinary.setInputFormat(m_instances);
    m_instances = Filter.useFilter(m_instances, m_nominalToBinary);
    
    m_removeUseless = new RemoveUseless();
    m_removeUseless.setInputFormat(m_instances);
    m_instances = Filter.useFilter(m_instances, m_removeUseless);
    
    m_instances.randomize(new Random(1L));
    
    m_ruleSet = new FastVector();
    


    if (m_generateRules) {
      Instances tempInst = m_instances;
      do
      {
        Rule tempRule = new Rule();
        tempRule.setSmoothing(!m_unsmoothedPredictions);
        tempRule.setRegressionTree(m_regressionTree);
        tempRule.setUnpruned(m_useUnpruned);
        tempRule.setSaveInstances(false);
        tempRule.setMinNumInstances(m_minNumInstances);
        tempRule.buildClassifier(tempInst);
        m_ruleSet.addElement(tempRule);
        
        tempInst = tempRule.notCoveredInstances();
        tempRule.freeNotCoveredInstances();
      } while (tempInst.numInstances() > 0);
    }
    else {
      Rule tempRule = new Rule();
      
      tempRule.setUseTree(true);
      
      tempRule.setSmoothing(!m_unsmoothedPredictions);
      tempRule.setSaveInstances(m_saveInstances);
      tempRule.setRegressionTree(m_regressionTree);
      tempRule.setUnpruned(m_useUnpruned);
      tempRule.setMinNumInstances(m_minNumInstances);
      


      Instances temp_train = m_instances;
      
      tempRule.buildClassifier(temp_train);
      
      m_ruleSet.addElement(tempRule);
    }
    



    m_instances = new Instances(m_instances, 0);
  }
  







  public double classifyInstance(Instance inst)
    throws Exception
  {
    double prediction = 0.0D;
    boolean success = false;
    
    m_replaceMissing.input(inst);
    inst = m_replaceMissing.output();
    m_nominalToBinary.input(inst);
    inst = m_nominalToBinary.output();
    m_removeUseless.input(inst);
    inst = m_removeUseless.output();
    
    if (m_ruleSet == null) {
      throw new Exception("Classifier has not been built yet!");
    }
    
    if (!m_generateRules) {
      Rule temp = (Rule)m_ruleSet.elementAt(0);
      return temp.classifyInstance(inst);
    }
    



    for (int i = 0; i < m_ruleSet.size(); i++) {
      boolean cont = false;
      Rule temp = (Rule)m_ruleSet.elementAt(i);
      try
      {
        prediction = temp.classifyInstance(inst);
        success = true;
      } catch (Exception e) {
        cont = true;
      }
      
      if (!cont) {
        break;
      }
    }
    
    if (!success) {
      System.out.println("Error in predicting (DecList)");
    }
    return prediction;
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    

    if (m_ruleSet == null) {
      return "Classifier hasn't been built yet!";
    }
    
    if (m_generateRules) {
      text.append("M5 " + (m_useUnpruned == true ? "unpruned " : "pruned ") + (m_regressionTree == true ? "regression " : "model ") + "rules ");
      







      if (!m_unsmoothedPredictions) {
        text.append("\n(using smoothed linear models) ");
      }
      
      text.append(":\n");
      
      text.append("Number of Rules : " + m_ruleSet.size() + "\n\n");
      
      for (int j = 0; j < m_ruleSet.size(); j++) {
        Rule temp = (Rule)m_ruleSet.elementAt(j);
        
        text.append("Rule: " + (j + 1) + "\n");
        text.append(temp.toString());
      }
    } else {
      Rule temp = (Rule)m_ruleSet.elementAt(0);
      text.append(temp.toString());
    }
    return text.toString();
  }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(1);
    newVector.addElement("measureNumRules");
    return newVector.elements();
  }
  






  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0) {
      return measureNumRules();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (M5)");
  }
  






  public double measureNumRules()
  {
    if (m_generateRules) {
      return m_ruleSet.size();
    }
    return m_ruleSet.elementAt(0)).m_topOfTree.numberOfLinearModels();
  }
  
  public RuleNode getM5RootNode() {
    Rule temp = (Rule)m_ruleSet.elementAt(0);
    return temp.getM5RootNode();
  }
}

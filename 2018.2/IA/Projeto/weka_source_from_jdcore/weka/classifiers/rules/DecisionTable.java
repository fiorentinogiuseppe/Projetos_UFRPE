package weka.classifiers.rules;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.SubsetEvaluator;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.AdditionalMeasureProducer;
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
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;





































































































































public class DecisionTable
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, AdditionalMeasureProducer, TechnicalInformationHandler
{
  static final long serialVersionUID = 2888557078165701326L;
  protected Hashtable m_entries;
  protected double[] m_classPriorCounts;
  protected double[] m_classPriors;
  protected int[] m_decisionFeatures;
  protected Filter m_disTransform;
  protected Remove m_delTransform;
  protected IBk m_ibk;
  protected Instances m_theInstances;
  protected Instances m_dtInstances;
  protected int m_numAttributes;
  private int m_numInstances;
  protected boolean m_classIsNominal;
  protected boolean m_useIBk;
  protected boolean m_displayRules;
  private int m_CVFolds;
  private Random m_rr;
  protected double m_majority;
  protected ASSearch m_search = new BestFirst();
  
  protected ASEvaluation m_evaluator;
  
  protected Evaluation m_evaluation;
  
  public static final int EVAL_DEFAULT = 1;
  
  public static final int EVAL_ACCURACY = 2;
  
  public static final int EVAL_RMSE = 3;
  
  public static final int EVAL_MAE = 4;
  
  public static final int EVAL_AUC = 5;
  public static final Tag[] TAGS_EVALUATION = { new Tag(1, "Default: accuracy (discrete class); RMSE (numeric class)"), new Tag(2, "Accuracy (discrete class only"), new Tag(3, "RMSE (of the class probabilities for discrete class)"), new Tag(4, "MAE (of the class probabilities for discrete class)"), new Tag(5, "AUC (area under the ROC curve - discrete class only)") };
  






  protected int m_evaluationMeasure = 1;
  





  public String globalInfo()
  {
    return "Class for building and using a simple decision table majority classifier.\n\nFor more information see: \n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ron Kohavi");
    result.setValue(TechnicalInformation.Field.TITLE, "The Power of Decision Tables");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "8th European Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "1995");
    result.setValue(TechnicalInformation.Field.PAGES, "174-189");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  



  private void insertIntoTable(Instance inst, double[] instA)
    throws Exception
  {
    DecisionTableHashKey thekey;
    


    DecisionTableHashKey thekey;
    


    if (instA != null) {
      thekey = new DecisionTableHashKey(instA);
    } else {
      thekey = new DecisionTableHashKey(inst, inst.numAttributes(), false);
    }
    

    double[] tempClassDist2 = (double[])m_entries.get(thekey);
    if (tempClassDist2 == null) {
      if (m_classIsNominal) {
        double[] newDist = new double[m_theInstances.classAttribute().numValues()];
        

        for (int i = 0; i < m_theInstances.classAttribute().numValues(); i++) {
          newDist[i] = 1.0D;
        }
        
        newDist[((int)inst.classValue())] = inst.weight();
        

        m_entries.put(thekey, newDist);
      } else {
        double[] newDist = new double[2];
        newDist[0] = (inst.classValue() * inst.weight());
        newDist[1] = inst.weight();
        

        m_entries.put(thekey, newDist);
      }
      

    }
    else if (m_classIsNominal) {
      tempClassDist2[((int)inst.classValue())] += inst.weight();
      

      m_entries.put(thekey, tempClassDist2);
    } else {
      tempClassDist2[0] += inst.classValue() * inst.weight();
      tempClassDist2[1] += inst.weight();
      

      m_entries.put(thekey, tempClassDist2);
    }
  }
  














  double evaluateInstanceLeaveOneOut(Instance instance, double[] instA)
    throws Exception
  {
    DecisionTableHashKey thekey = new DecisionTableHashKey(instA);
    if (m_classIsNominal)
    {
      double[] tempDist;
      if ((tempDist = (double[])m_entries.get(thekey)) == null) {
        throw new Error("This should never happen!");
      }
      double[] normDist = new double[tempDist.length];
      System.arraycopy(tempDist, 0, normDist, 0, tempDist.length);
      normDist[((int)instance.classValue())] -= instance.weight();
      


      boolean ok = false;
      for (int i = 0; i < normDist.length; i++) {
        if (Utils.gr(normDist[i], 1.0D)) {
          ok = true;
          break;
        }
      }
      

      m_classPriorCounts[((int)instance.classValue())] -= instance.weight();
      
      double[] classPriors = (double[])m_classPriorCounts.clone();
      Utils.normalize(classPriors);
      if (!ok) {
        normDist = classPriors;
      }
      
      m_classPriorCounts[((int)instance.classValue())] += instance.weight();
      


      Utils.normalize(normDist);
      if (m_evaluationMeasure == 5) {
        m_evaluation.evaluateModelOnceAndRecordPrediction(normDist, instance);
      } else {
        m_evaluation.evaluateModelOnce(normDist, instance);
      }
      return Utils.maxIndex(normDist);
    }
    






    double[] tempDist;
    





    if ((tempDist = (double[])m_entries.get(thekey)) != null) {
      double[] normDist = new double[tempDist.length];
      System.arraycopy(tempDist, 0, normDist, 0, tempDist.length);
      normDist[0] -= instance.classValue() * instance.weight();
      normDist[1] -= instance.weight();
      if (Utils.eq(normDist[1], 0.0D)) {
        double[] temp = new double[1];
        temp[0] = m_majority;
        m_evaluation.evaluateModelOnce(temp, instance);
        return m_majority;
      }
      double[] temp = new double[1];
      normDist[0] /= normDist[1];
      m_evaluation.evaluateModelOnce(temp, instance);
      return temp[0];
    }
    
    throw new Error("This should never happen!");
  }
  














  double evaluateFoldCV(Instances fold, int[] fs)
    throws Exception
  {
    int ruleCount = 0;
    int numFold = fold.numInstances();
    int numCl = m_theInstances.classAttribute().numValues();
    double[][] class_distribs = new double[numFold][numCl];
    double[] instA = new double[fs.length];
    

    double acc = 0.0D;
    int classI = m_theInstances.classIndex();
    double[] normDist;
    double[] normDist;
    if (m_classIsNominal) {
      normDist = new double[numCl];
    } else {
      normDist = new double[2];
    }
    

    for (int i = 0; i < numFold; i++) {
      Instance inst = fold.instance(i);
      for (int j = 0; j < fs.length; j++) {
        if (fs[j] == classI) {
          instA[j] = Double.MAX_VALUE;
        } else if (inst.isMissing(fs[j])) {
          instA[j] = Double.MAX_VALUE;
        } else {
          instA[j] = inst.value(fs[j]);
        }
      }
      DecisionTableHashKey thekey = new DecisionTableHashKey(instA);
      if ((class_distribs[i] =  = (double[])m_entries.get(thekey)) == null) {
        throw new Error("This should never happen!");
      }
      if (m_classIsNominal) {
        class_distribs[i][((int)inst.classValue())] -= inst.weight();
      } else {
        class_distribs[i][0] -= inst.classValue() * inst.weight();
        class_distribs[i][1] -= inst.weight();
      }
      ruleCount++;
      
      m_classPriorCounts[((int)inst.classValue())] -= inst.weight();
    }
    
    double[] classPriors = (double[])m_classPriorCounts.clone();
    Utils.normalize(classPriors);
    

    for (i = 0; i < numFold; i++) {
      Instance inst = fold.instance(i);
      System.arraycopy(class_distribs[i], 0, normDist, 0, normDist.length);
      if (m_classIsNominal) {
        boolean ok = false;
        for (int j = 0; j < normDist.length; j++) {
          if (Utils.gr(normDist[j], 1.0D)) {
            ok = true;
            break;
          }
        }
        
        if (!ok) {
          normDist = (double[])classPriors.clone();
        }
        

        Utils.normalize(normDist);
        if (m_evaluationMeasure == 5) {
          m_evaluation.evaluateModelOnceAndRecordPrediction(normDist, inst);
        } else {
          m_evaluation.evaluateModelOnce(normDist, inst);



        }
        




      }
      else if (Utils.eq(normDist[1], 0.0D)) {
        double[] temp = new double[1];
        temp[0] = m_majority;
        m_evaluation.evaluateModelOnce(temp, inst);
      } else {
        double[] temp = new double[1];
        normDist[0] /= normDist[1];
        m_evaluation.evaluateModelOnce(temp, inst);
      }
    }
    


    for (i = 0; i < numFold; i++) {
      Instance inst = fold.instance(i);
      
      m_classPriorCounts[((int)inst.classValue())] += inst.weight();
      

      if (m_classIsNominal) {
        class_distribs[i][((int)inst.classValue())] += inst.weight();
      } else {
        class_distribs[i][0] += inst.classValue() * inst.weight();
        class_distribs[i][1] += inst.weight();
      }
    }
    return acc;
  }
  









  protected double estimatePerformance(BitSet feature_set, int num_atts)
    throws Exception
  {
    m_evaluation = new Evaluation(m_theInstances);
    
    int[] fs = new int[num_atts];
    
    double[] instA = new double[num_atts];
    int classI = m_theInstances.classIndex();
    
    int index = 0;
    for (int i = 0; i < m_numAttributes; i++) {
      if (feature_set.get(i)) {
        fs[(index++)] = i;
      }
    }
    

    m_entries = new Hashtable((int)(m_theInstances.numInstances() * 1.5D));
    

    for (i = 0; i < m_numInstances; i++)
    {
      Instance inst = m_theInstances.instance(i);
      for (int j = 0; j < fs.length; j++) {
        if (fs[j] == classI) {
          instA[j] = Double.MAX_VALUE;
        } else if (inst.isMissing(fs[j])) {
          instA[j] = Double.MAX_VALUE;
        } else {
          instA[j] = inst.value(fs[j]);
        }
      }
      insertIntoTable(inst, instA);
    }
    

    if (m_CVFolds == 1)
    {

      for (i = 0; i < m_numInstances; i++) {
        Instance inst = m_theInstances.instance(i);
        for (int j = 0; j < fs.length; j++) {
          if (fs[j] == classI) {
            instA[j] = Double.MAX_VALUE;
          } else if (inst.isMissing(fs[j])) {
            instA[j] = Double.MAX_VALUE;
          } else {
            instA[j] = inst.value(fs[j]);
          }
        }
        evaluateInstanceLeaveOneOut(inst, instA);
      }
    }
    m_theInstances.randomize(m_rr);
    m_theInstances.stratify(m_CVFolds);
    

    for (i = 0; i < m_CVFolds; i++) {
      Instances insts = m_theInstances.testCV(m_CVFolds, i);
      evaluateFoldCV(insts, fs);
    }
    

    switch (m_evaluationMeasure) {
    case 1: 
      if (m_classIsNominal) {
        return m_evaluation.pctCorrect();
      }
      return -m_evaluation.rootMeanSquaredError();
    case 2: 
      return m_evaluation.pctCorrect();
    case 3: 
      return -m_evaluation.rootMeanSquaredError();
    case 4: 
      return -m_evaluation.meanAbsoluteError();
    case 5: 
      double[] classPriors = m_evaluation.getClassPriors();
      Utils.normalize(classPriors);
      double weightedAUC = 0.0D;
      for (i = 0; i < m_theInstances.classAttribute().numValues(); i++) {
        double tempAUC = m_evaluation.areaUnderROC(i);
        if (!Instance.isMissingValue(tempAUC)) {
          weightedAUC += classPriors[i] * tempAUC;
        } else {
          System.err.println("Undefined AUC!!");
        }
      }
      return weightedAUC;
    }
    
    return 0.0D;
  }
  






  private String printSub(BitSet sub)
  {
    String s = "";
    for (int jj = 0; jj < m_numAttributes; jj++) {
      if (sub.get(jj)) {
        s = s + " " + (jj + 1);
      }
    }
    return s;
  }
  



  protected void resetOptions()
  {
    m_entries = null;
    m_decisionFeatures = null;
    m_useIBk = false;
    m_CVFolds = 1;
    m_displayRules = false;
    m_evaluationMeasure = 1;
  }
  



  public DecisionTable()
  {
    resetOptions();
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(7);
    
    newVector.addElement(new Option("\tFull class name of search method, followed\n\tby its options.\n\teg: \"weka.attributeSelection.BestFirst -D 1\"\n\t(default weka.attributeSelection.BestFirst)", "S", 1, "-S <search method specification>"));
    





    newVector.addElement(new Option("\tUse cross validation to evaluate features.\n\tUse number of folds = 1 for leave one out CV.\n\t(Default = leave one out CV)", "X", 1, "-X <number of folds>"));
    




    newVector.addElement(new Option("\tPerformance evaluation measure to use for selecting attributes.\n\t(Default = accuracy for discrete class and rmse for numeric class)", "E", 1, "-E <acc | rmse | mae | auc>"));
    



    newVector.addElement(new Option("\tUse nearest neighbour instead of global table majority.", "I", 0, "-I"));
    


    newVector.addElement(new Option("\tDisplay decision table rules.\n", "R", 0, "-R"));
    


    newVector.addElement(new Option("", "", 0, "\nOptions specific to search method " + m_search.getClass().getName() + ":"));
    


    Enumeration enu = ((OptionHandler)m_search).listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  




  public String crossValTipText()
  {
    return "Sets the number of folds for cross validation (1 = leave one out).";
  }
  





  public void setCrossVal(int folds)
  {
    m_CVFolds = folds;
  }
  





  public int getCrossVal()
  {
    return m_CVFolds;
  }
  




  public String useIBkTipText()
  {
    return "Sets whether IBk should be used instead of the majority class.";
  }
  





  public void setUseIBk(boolean ibk)
  {
    m_useIBk = ibk;
  }
  





  public boolean getUseIBk()
  {
    return m_useIBk;
  }
  




  public String displayRulesTipText()
  {
    return "Sets whether rules are to be printed.";
  }
  





  public void setDisplayRules(boolean rules)
  {
    m_displayRules = rules;
  }
  





  public boolean getDisplayRules()
  {
    return m_displayRules;
  }
  




  public String searchTipText()
  {
    return "The search method used to find good attribute combinations for the decision table.";
  }
  




  public void setSearch(ASSearch search)
  {
    m_search = search;
  }
  




  public ASSearch getSearch()
  {
    return m_search;
  }
  




  public String evaluationMeasureTipText()
  {
    return "The measure used to evaluate the performance of attribute combinations used in the decision table.";
  }
  





  public SelectedTag getEvaluationMeasure()
  {
    return new SelectedTag(m_evaluationMeasure, TAGS_EVALUATION);
  }
  





  public void setEvaluationMeasure(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_EVALUATION) {
      m_evaluationMeasure = newMethod.getSelectedTag().getID();
    }
  }
  






















































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    String optionString = Utils.getOption('X', options);
    if (optionString.length() != 0) {
      m_CVFolds = Integer.parseInt(optionString);
    }
    
    m_useIBk = Utils.getFlag('I', options);
    
    m_displayRules = Utils.getFlag('R', options);
    
    optionString = Utils.getOption('E', options);
    if (optionString.length() != 0) {
      if (optionString.equals("acc")) {
        setEvaluationMeasure(new SelectedTag(2, TAGS_EVALUATION));
      } else if (optionString.equals("rmse")) {
        setEvaluationMeasure(new SelectedTag(3, TAGS_EVALUATION));
      } else if (optionString.equals("mae")) {
        setEvaluationMeasure(new SelectedTag(4, TAGS_EVALUATION));
      } else if (optionString.equals("auc")) {
        setEvaluationMeasure(new SelectedTag(5, TAGS_EVALUATION));
      } else {
        throw new IllegalArgumentException("Invalid evaluation measure");
      }
    }
    
    String searchString = Utils.getOption('S', options);
    if (searchString.length() == 0)
      searchString = BestFirst.class.getName();
    String[] searchSpec = Utils.splitOptions(searchString);
    if (searchSpec.length == 0) {
      throw new IllegalArgumentException("Invalid search specification string");
    }
    String searchName = searchSpec[0];
    searchSpec[0] = "";
    setSearch(ASSearch.forName(searchName, searchSpec));
  }
  





  public String[] getOptions()
  {
    String[] options = new String[9];
    int current = 0;
    
    options[(current++)] = "-X";options[(current++)] = ("" + m_CVFolds);
    
    if (m_evaluationMeasure != 1) {
      options[(current++)] = "-E";
      switch (m_evaluationMeasure) {
      case 2: 
        options[(current++)] = "acc";
        break;
      case 3: 
        options[(current++)] = "rmse";
        break;
      case 4: 
        options[(current++)] = "mae";
        break;
      case 5: 
        options[(current++)] = "auc";
      }
      
    }
    if (m_useIBk) {
      options[(current++)] = "-I";
    }
    if (m_displayRules) {
      options[(current++)] = "-R";
    }
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSearchSpec());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  protected String getSearchSpec()
  {
    ASSearch s = getSearch();
    if ((s instanceof OptionHandler)) {
      return s.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)s).getOptions());
    }
    
    return s.getClass().getName();
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
    if ((m_evaluationMeasure != 2) && (m_evaluationMeasure != 5)) {
      result.enable(Capabilities.Capability.NUMERIC_CLASS);
      result.enable(Capabilities.Capability.DATE_CLASS);
    }
    
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  
  private class DummySubsetEvaluator extends ASEvaluation implements SubsetEvaluator {
    private static final long serialVersionUID = 3927442457704974150L;
    
    private DummySubsetEvaluator() {}
    
    public void buildEvaluator(Instances data) throws Exception
    {}
    
    public double evaluateSubset(BitSet subset) throws Exception {
      int fc = 0;
      for (int jj = 0; jj < m_numAttributes; jj++) {
        if (subset.get(jj)) {
          fc++;
        }
      }
      
      return estimatePerformance(subset, fc);
    }
  }
  


  protected void setUpEvaluator()
    throws Exception
  {
    m_evaluator = new DummySubsetEvaluator(null);
  }
  
  protected boolean m_saveMemory = true;
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    m_theInstances = new Instances(data);
    m_theInstances.deleteWithMissingClass();
    
    m_rr = new Random(1L);
    
    if (m_theInstances.classAttribute().isNominal()) {
      m_classPriorCounts = new double[data.classAttribute().numValues()];
      
      Arrays.fill(m_classPriorCounts, 1.0D);
      for (int i = 0; i < data.numInstances(); i++) {
        Instance curr = data.instance(i);
        m_classPriorCounts[((int)curr.classValue())] += curr.weight();
      }
      
      m_classPriors = ((double[])m_classPriorCounts.clone());
      Utils.normalize(m_classPriors);
    }
    
    setUpEvaluator();
    
    if (m_theInstances.classAttribute().isNumeric()) {
      m_disTransform = new weka.filters.unsupervised.attribute.Discretize();
      m_classIsNominal = false;
      

      ((weka.filters.unsupervised.attribute.Discretize)m_disTransform).setBins(10);
      
      ((weka.filters.unsupervised.attribute.Discretize)m_disTransform).setInvertSelection(true);
      


      String rangeList = "";
      rangeList = rangeList + (m_theInstances.classIndex() + 1);
      

      ((weka.filters.unsupervised.attribute.Discretize)m_disTransform).setAttributeIndices(rangeList);
    }
    else {
      m_disTransform = new weka.filters.supervised.attribute.Discretize();
      ((weka.filters.supervised.attribute.Discretize)m_disTransform).setUseBetterEncoding(true);
      m_classIsNominal = true;
    }
    
    m_disTransform.setInputFormat(m_theInstances);
    m_theInstances = Filter.useFilter(m_theInstances, m_disTransform);
    
    m_numAttributes = m_theInstances.numAttributes();
    m_numInstances = m_theInstances.numInstances();
    m_majority = m_theInstances.meanOrMode(m_theInstances.classAttribute());
    

    int[] selected = m_search.search(m_evaluator, m_theInstances);
    
    m_decisionFeatures = new int[selected.length + 1];
    System.arraycopy(selected, 0, m_decisionFeatures, 0, selected.length);
    m_decisionFeatures[(m_decisionFeatures.length - 1)] = m_theInstances.classIndex();
    

    m_delTransform = new Remove();
    m_delTransform.setInvertSelection(true);
    

    m_delTransform.setAttributeIndicesArray(m_decisionFeatures);
    m_delTransform.setInputFormat(m_theInstances);
    m_dtInstances = Filter.useFilter(m_theInstances, m_delTransform);
    

    m_numAttributes = m_dtInstances.numAttributes();
    

    m_entries = new Hashtable((int)(m_dtInstances.numInstances() * 1.5D));
    

    for (int i = 0; i < m_numInstances; i++) {
      Instance inst = m_dtInstances.instance(i);
      insertIntoTable(inst, null);
    }
    

    if (m_useIBk) {
      m_ibk = new IBk();
      m_ibk.buildClassifier(m_theInstances);
    }
    

    if (m_saveMemory) {
      m_theInstances = new Instances(m_theInstances, 0);
      m_dtInstances = new Instances(m_dtInstances, 0);
    }
    m_evaluation = null;
  }
  












  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    m_disTransform.input(instance);
    m_disTransform.batchFinished();
    instance = m_disTransform.output();
    
    m_delTransform.input(instance);
    m_delTransform.batchFinished();
    instance = m_delTransform.output();
    
    DecisionTableHashKey thekey = new DecisionTableHashKey(instance, instance.numAttributes(), false);
    
    double[] tempDist;
    if ((tempDist = (double[])m_entries.get(thekey)) == null) {
      if (m_useIBk) {
        tempDist = m_ibk.distributionForInstance(instance);
      }
      else if (!m_classIsNominal) {
        tempDist = new double[1];
        tempDist[0] = m_majority;
      } else {
        tempDist = (double[])m_classPriors.clone();

      }
      

    }
    else if (!m_classIsNominal) {
      double[] normDist = new double[1];
      tempDist[0] /= tempDist[1];
      tempDist = normDist;
    }
    else
    {
      double[] normDist = new double[tempDist.length];
      System.arraycopy(tempDist, 0, normDist, 0, tempDist.length);
      Utils.normalize(normDist);
      tempDist = normDist;
    }
    
    return tempDist;
  }
  






  public String printFeatures()
  {
    String s = "";
    
    for (int i = 0; i < m_decisionFeatures.length; i++) {
      if (i == 0) {
        s = "" + (m_decisionFeatures[i] + 1);
      } else {
        s = s + "," + (m_decisionFeatures[i] + 1);
      }
    }
    return s;
  }
  



  public double measureNumRules()
  {
    return m_entries.size();
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
    throw new IllegalArgumentException(additionalMeasureName + " not supported (DecisionTable)");
  }
  







  public String toString()
  {
    if (m_entries == null) {
      return "Decision Table: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    
    text.append("Decision Table:\n\nNumber of training instances: " + m_numInstances + "\nNumber of Rules : " + m_entries.size() + "\n");
    


    if (m_useIBk) {
      text.append("Non matches covered by IB1.\n");
    } else {
      text.append("Non matches covered by Majority class.\n");
    }
    
    text.append(m_search.toString());
    


    text.append("Evaluation (for feature selection): CV ");
    if (m_CVFolds > 1) {
      text.append("(" + m_CVFolds + " fold) ");
    } else {
      text.append("(leave one out) ");
    }
    text.append("\nFeature set: " + printFeatures());
    
    if (m_displayRules)
    {

      int maxColWidth = 0;
      for (int i = 0; i < m_dtInstances.numAttributes(); i++) {
        if (m_dtInstances.attribute(i).name().length() > maxColWidth) {
          maxColWidth = m_dtInstances.attribute(i).name().length();
        }
        
        if ((m_classIsNominal) || (i != m_dtInstances.classIndex())) {
          Enumeration e = m_dtInstances.attribute(i).enumerateValues();
          while (e.hasMoreElements()) {
            String ss = (String)e.nextElement();
            if (ss.length() > maxColWidth) {
              maxColWidth = ss.length();
            }
          }
        }
      }
      
      text.append("\n\nRules:\n");
      StringBuffer tm = new StringBuffer();
      for (int i = 0; i < m_dtInstances.numAttributes(); i++) {
        if (m_dtInstances.classIndex() != i) {
          int d = maxColWidth - m_dtInstances.attribute(i).name().length();
          tm.append(m_dtInstances.attribute(i).name());
          for (int j = 0; j < d + 1; j++) {
            tm.append(" ");
          }
        }
      }
      tm.append(m_dtInstances.attribute(m_dtInstances.classIndex()).name() + "  ");
      
      for (int i = 0; i < tm.length() + 10; i++) {
        text.append("=");
      }
      text.append("\n");
      text.append(tm);
      text.append("\n");
      for (int i = 0; i < tm.length() + 10; i++) {
        text.append("=");
      }
      text.append("\n");
      
      Enumeration e = m_entries.keys();
      while (e.hasMoreElements()) {
        DecisionTableHashKey tt = (DecisionTableHashKey)e.nextElement();
        text.append(tt.toString(m_dtInstances, maxColWidth));
        double[] ClassDist = (double[])m_entries.get(tt);
        
        if (m_classIsNominal) {
          int m = Utils.maxIndex(ClassDist);
          try {
            text.append(m_dtInstances.classAttribute().value(m) + "\n");
          } catch (Exception ee) {
            System.out.println(ee.getMessage());
          }
        } else {
          text.append(ClassDist[0] / ClassDist[1] + "\n");
        }
      }
      
      for (int i = 0; i < tm.length() + 10; i++) {
        text.append("=");
      }
      text.append("\n");
      text.append("\n");
    }
    return text.toString();
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5981 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new DecisionTable(), argv);
  }
}

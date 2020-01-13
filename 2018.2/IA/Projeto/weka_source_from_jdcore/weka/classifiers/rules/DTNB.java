package weka.classifiers.rules;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.SubsetEvaluator;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
























































































public class DTNB
  extends DecisionTable
{
  protected NaiveBayes m_NB;
  private int[] m_nbFeatures;
  private double m_percentUsedByDT;
  private double m_percentDeleted;
  static final long serialVersionUID = 2999557077765701326L;
  protected ASSearch m_backwardWithDelete;
  
  public DTNB() {}
  
  public String globalInfo()
  {
    return "Class for building and using a decision table/naive bayes hybrid classifier. At each point in the search, the algorithm evaluates the merit of dividing the attributes into two disjoint subsets: one for the decision table, the other for naive Bayes. A forward selection search is used, where at each step, selected attributes are modeled by naive Bayes and the remainder by the decision table, and all attributes are modelled by the decision table initially. At each step, the algorithm also considers dropping an attribute entirely from the model.\n\nFor more information, see: \n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Mark Hall and Eibe Frank");
    result.setValue(TechnicalInformation.Field.TITLE, "Combining Naive Bayes and Decision Tables");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the 21st Florida Artificial Intelligence Society Conference (FLAIRS)");
    
    result.setValue(TechnicalInformation.Field.YEAR, "2008");
    result.setValue(TechnicalInformation.Field.PAGES, "318-319");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "AAAI press");
    
    return result;
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
        inst.setWeight(-inst.weight());
        m_NB.updateClassifier(inst);
        inst.setWeight(-inst.weight());
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
        } else {
          Utils.normalize(normDist);
        }
        
        double[] nbDist = m_NB.distributionForInstance(inst);
        
        for (int l = 0; l < normDist.length; l++) {
          normDist[l] = (Math.log(normDist[l]) - Math.log(classPriors[l]));
          normDist[l] += Math.log(nbDist[l]);
        }
        normDist = Utils.logs2probs(normDist);
        



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
        m_NB.updateClassifier(inst);
      } else {
        class_distribs[i][0] += inst.classValue() * inst.weight();
        class_distribs[i][1] += inst.weight();
      }
    }
    return acc;
  }
  













  double evaluateInstanceLeaveOneOut(Instance instance, double[] instA)
    throws Exception
  {
    DecisionTableHashKey thekey = new DecisionTableHashKey(instA);
    
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
    } else {
      Utils.normalize(normDist);
    }
    
    m_classPriorCounts[((int)instance.classValue())] += instance.weight();
    

    if (m_NB != null)
    {

      instance.setWeight(-instance.weight());
      m_NB.updateClassifier(instance);
      double[] nbDist = m_NB.distributionForInstance(instance);
      instance.setWeight(-instance.weight());
      m_NB.updateClassifier(instance);
      
      for (int i = 0; i < normDist.length; i++) {
        normDist[i] = (Math.log(normDist[i]) - Math.log(classPriors[i]));
        normDist[i] += Math.log(nbDist[i]);
      }
      normDist = Utils.logs2probs(normDist);
    }
    

    if (m_evaluationMeasure == 5) {
      m_evaluation.evaluateModelOnceAndRecordPrediction(normDist, instance);
    } else {
      m_evaluation.evaluateModelOnce(normDist, instance);
    }
    return Utils.maxIndex(normDist);
  }
  



  protected void setUpEvaluator()
    throws Exception
  {
    m_evaluator = new EvalWithDelete();
    m_evaluator.buildEvaluator(m_theInstances);
  }
  
  protected class EvalWithDelete extends ASEvaluation implements SubsetEvaluator {
    private BitSet m_deletedFromDTNB;
    
    protected EvalWithDelete() {}
    
    public void buildEvaluator(Instances data) throws Exception {
      m_NB = null;
      m_deletedFromDTNB = new BitSet(data.numAttributes());
    }
    
    private int setUpForEval(BitSet subset)
      throws Exception
    {
      int fc = 0;
      for (int jj = 0; jj < m_numAttributes; jj++) {
        if (subset.get(jj)) {
          fc++;
        }
      }
      



      for (int j = 0; j < m_numAttributes; j++) {
        m_theInstances.attribute(j).setWeight(1.0D);
        if ((j != m_theInstances.classIndex()) && 
          (subset.get(j)))
        {
          m_theInstances.attribute(j).setWeight(0.0D);
        }
      }
      


      for (int i = 0; i < m_numAttributes; i++) {
        if (m_deletedFromDTNB.get(i)) {
          m_theInstances.attribute(i).setWeight(0.0D);
        }
      }
      
      if (m_NB == null)
      {
        m_NB = new NaiveBayes();
        m_NB.buildClassifier(m_theInstances);
      }
      return fc;
    }
    
    public double evaluateSubset(BitSet subset) throws Exception {
      int fc = setUpForEval(subset);
      
      return estimatePerformance(subset, fc);
    }
    
    public double evaluateSubsetDelete(BitSet subset, int potentialDelete) throws Exception
    {
      int fc = setUpForEval(subset);
      

      m_theInstances.attribute(potentialDelete).setWeight(0.0D);
      

      return estimatePerformance(subset, fc);
    }
    
    public BitSet getDeletedList() {
      return m_deletedFromDTNB;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6269 $");
    }
  }
  


  protected class BackwardsWithDelete
    extends ASSearch
  {
    protected BackwardsWithDelete() {}
    

    public String globalInfo()
    {
      return "Specialized search that performs a forward selection (naive Bayes)/backward elimination (decision table). Also considers dropping attributes entirely from the combined model.";
    }
    

    public String toString()
    {
      return "";
    }
    
    public int[] search(ASEvaluation eval, Instances data)
      throws Exception
    {
      double best_merit = -1.7976931348623157E308D;
      double temp_best = 0.0D;double temp_merit = 0.0D;double temp_merit_delete = 0.0D;
      int temp_index = 0;
      
      BitSet best_group = null;
      
      int numAttribs = data.numAttributes();
      
      if (best_group == null) {
        best_group = new BitSet(numAttribs);
      }
      

      int classIndex = data.classIndex();
      for (int i = 0; i < numAttribs; i++) {
        if (i != classIndex) {
          best_group.set(i);
        }
      }
      




      best_merit = ((SubsetEvaluator)eval).evaluateSubset(best_group);
      



      boolean done = false;
      boolean addone = false;
      
      boolean deleted = false;
      while (!done) {
        BitSet temp_group = (BitSet)best_group.clone();
        temp_best = best_merit;
        
        done = true;
        addone = false;
        for (i = 0; i < numAttribs; i++) {
          boolean z = (i != classIndex) && (temp_group.get(i));
          
          if (z)
          {
            temp_group.clear(i);
            

            temp_merit = ((SubsetEvaluator)eval).evaluateSubset(temp_group);
            
            temp_merit_delete = ((DTNB.EvalWithDelete)eval).evaluateSubsetDelete(temp_group, i);
            boolean deleteBetter = false;
            
            if (temp_merit_delete >= temp_merit) {
              temp_merit = temp_merit_delete;
              deleteBetter = true;
            }
            
            z = temp_merit >= temp_best;
            
            if (z) {
              temp_best = temp_merit;
              temp_index = i;
              addone = true;
              done = false;
              if (deleteBetter) {
                deleted = true;
              } else {
                deleted = false;
              }
            }
            

            temp_group.set(i);
          }
        }
        if (addone) {
          best_group.clear(temp_index);
          best_merit = temp_best;
          if (deleted)
          {
            ((DTNB.EvalWithDelete)eval).getDeletedList().set(temp_index);
          }
        }
      }
      



      return attributeList(best_group);
    }
    




    protected int[] attributeList(BitSet group)
    {
      int count = 0;
      BitSet copy = (BitSet)group.clone();
      









      for (int i = 0; i < m_numAttributes; i++) {
        if (copy.get(i)) {
          count++;
        }
      }
      
      int[] list = new int[count];
      count = 0;
      
      for (int i = 0; i < m_numAttributes; i++) {
        if (copy.get(i)) {
          list[(count++)] = i;
        }
      }
      
      return list;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6269 $");
    }
  }
  
  private void setUpSearch() {
    m_backwardWithDelete = new BackwardsWithDelete();
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    m_saveMemory = false;
    
    if (data.classAttribute().isNumeric()) {
      throw new Exception("Can only handle nominal class!");
    }
    
    if (m_backwardWithDelete == null) {
      setUpSearch();
      m_search = m_backwardWithDelete;
    }
    



    super.buildClassifier(data);
    



    for (int i = 0; i < m_theInstances.numAttributes(); i++) {
      m_theInstances.attribute(i).setWeight(1.0D);
    }
    
    int count = 0;
    
    for (int i = 0; i < m_decisionFeatures.length; i++) {
      if (m_decisionFeatures[i] != m_theInstances.classIndex()) {
        count++;
        
        m_theInstances.attribute(m_decisionFeatures[i]).setWeight(0.0D);
      }
    }
    
    double numDeleted = 0.0D;
    
    BitSet deleted = ((EvalWithDelete)m_evaluator).getDeletedList();
    for (int i = 0; i < m_theInstances.numAttributes(); i++) {
      if (deleted.get(i)) {
        m_theInstances.attribute(i).setWeight(0.0D);
        
        numDeleted += 1.0D;
      }
    }
    

    m_percentUsedByDT = (count / (m_theInstances.numAttributes() - 1));
    m_percentDeleted = (numDeleted / (m_theInstances.numAttributes() - 1));
    
    m_NB = new NaiveBayes();
    m_NB.buildClassifier(m_theInstances);
    
    m_dtInstances = new Instances(m_dtInstances, 0);
    m_theInstances = new Instances(m_theInstances, 0);
  }
  












  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    m_disTransform.input(instance);
    m_disTransform.batchFinished();
    instance = m_disTransform.output();
    
    m_delTransform.input(instance);
    m_delTransform.batchFinished();
    Instance dtInstance = m_delTransform.output();
    
    DecisionTableHashKey thekey = new DecisionTableHashKey(dtInstance, dtInstance.numAttributes(), false);
    

    if ((tempDist = (double[])m_entries.get(thekey)) == null) {
      if (m_useIBk) {
        tempDist = m_ibk.distributionForInstance(dtInstance);

      }
      else
      {
        tempDist = (double[])m_classPriors.clone();
      }
    }
    else
    {
      double[] normDist = new double[tempDist.length];
      System.arraycopy(tempDist, 0, normDist, 0, tempDist.length);
      Utils.normalize(normDist);
      tempDist = normDist;
    }
    
    double[] nbDist = m_NB.distributionForInstance(instance);
    for (int i = 0; i < nbDist.length; i++) {
      tempDist[i] = (Math.log(tempDist[i]) - Math.log(m_classPriors[i]));
      tempDist[i] += Math.log(nbDist[i]);
    }
    


    double[] tempDist = Utils.logs2probs(tempDist);
    Utils.normalize(tempDist);
    
    return tempDist;
  }
  
  public String toString()
  {
    String sS = super.toString();
    if ((m_displayRules) && (m_NB != null)) {
      sS = sS + m_NB.toString();
    }
    return sS;
  }
  



  public double measurePercentAttsUsedByDT()
  {
    return m_percentUsedByDT;
  }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(2);
    newVector.addElement("measureNumRules");
    newVector.addElement("measurePercentAttsUsedByDT");
    return newVector.elements();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0)
      return measureNumRules();
    if (additionalMeasureName.compareToIgnoreCase("measurePercentAttsUsedByDT") == 0) {
      return measurePercentAttsUsedByDT();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (DecisionTable)");
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    
    result.disable(Capabilities.Capability.NUMERIC_CLASS);
    result.disable(Capabilities.Capability.DATE_CLASS);
    
    return result;
  }
  







  public void setSearch(ASSearch search) {}
  






  public ASSearch getSearch()
  {
    if (m_backwardWithDelete == null) {
      setUpSearch();
      
      m_search = m_backwardWithDelete;
    }
    return m_search;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(7);
    
    newVector.addElement(new Option("\tUse cross validation to evaluate features.\n\tUse number of folds = 1 for leave one out CV.\n\t(Default = leave one out CV)", "X", 1, "-X <number of folds>"));
    




    newVector.addElement(new Option("\tPerformance evaluation measure to use for selecting attributes.\n\t(Default = accuracy for discrete class and rmse for numeric class)", "E", 1, "-E <acc | rmse | mae | auc>"));
    



    newVector.addElement(new Option("\tUse nearest neighbour instead of global table majority.", "I", 0, "-I"));
    


    newVector.addElement(new Option("\tDisplay decision table rules.\n", "R", 0, "-R"));
    


    return newVector.elements();
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    String optionString = Utils.getOption('X', options);
    if (optionString.length() != 0) {
      setCrossVal(Integer.parseInt(optionString));
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
  }
  





  public String[] getOptions()
  {
    String[] options = new String[9];
    int current = 0;
    
    options[(current++)] = "-X";options[(current++)] = ("" + getCrossVal());
    
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
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6269 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new DTNB(), argv);
  }
}

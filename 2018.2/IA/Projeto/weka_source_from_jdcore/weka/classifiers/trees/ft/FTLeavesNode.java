package weka.classifiers.trees.ft;

import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.trees.j48.C45ModelSelection;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.Distribution;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.NoSplit;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;




































public class FTLeavesNode
  extends FTtree
{
  private static final long serialVersionUID = 950601378326259315L;
  
  public FTLeavesNode(boolean errorOnProbabilities, int numBoostingIterations, int minNumInstances, double weightTrimBeta, boolean useAIC)
  {
    m_errorOnProbabilities = errorOnProbabilities;
    m_fixedNumIterations = numBoostingIterations;
    m_minNumInstances = minNumInstances;
    m_maxIterations = 200;
    setWeightTrimBeta(weightTrimBeta);
    setUseAIC(useAIC);
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    buildTree(data, (SimpleLinearRegression[][])null, data.numInstances(), 0.0D);
  }
  














  public void buildTree(Instances data, SimpleLinearRegression[][] higherRegressions, double totalInstanceWeight, double higherNumParameters)
    throws Exception
  {
    m_totalInstanceWeight = totalInstanceWeight;
    m_train = new Instances(data);
    

    m_isLeaf = true;
    m_sons = null;
    
    m_numInstances = m_train.numInstances();
    m_numClasses = m_train.numClasses();
    

    m_numericData = getNumericData(m_train);
    m_numericDataHeader = new Instances(m_numericData, 0);
    
    m_regressions = initRegressions();
    m_numRegressions = 0;
    
    if (higherRegressions != null) m_higherRegressions = higherRegressions; else {
      m_higherRegressions = new SimpleLinearRegression[m_numClasses][0];
    }
    m_numHigherRegressions = m_higherRegressions[0].length;
    
    m_numParameters = higherNumParameters;
    

    if (m_numInstances >= m_numFoldsBoosting) {
      if (m_fixedNumIterations > 0) {
        performBoosting(m_fixedNumIterations);
      } else if (getUseAIC()) {
        performBoostingInfCriterion();
      } else {
        performBoostingCV();
      }
    }
    
    m_numParameters += m_numRegressions;
    

    m_regressions = selectRegressions(m_regressions);
    






    double[][] FsConst = getFs(m_numericData);
    
    for (int j = 0; j < data.numInstances(); j++)
    {
      double[] probsConst = probs(FsConst[j]);
      
      if (data.instance(j).classValue() != getConstError(probsConst)) { m_constError += 1.0D;
      }
    }
    
    m_modelSelection = new C45ModelSelection(m_minNumInstances, data);
    m_localModel = m_modelSelection.selectModel(data);
    boolean grow;
    boolean grow;
    if (m_numInstances > m_minNumInstances) {
      grow = m_localModel.numSubsets() > 1;
    } else {
      grow = false;
    }
    

    m_hasConstr = false;
    if (grow)
    {
      m_isLeaf = false;
      Instances[] localInstances = m_localModel.split(data);
      m_sons = new FTLeavesNode[m_localModel.numSubsets()];
      
      for (int i = 0; i < m_sons.length; i++) {
        m_sons[i] = new FTLeavesNode(m_errorOnProbabilities, m_fixedNumIterations, m_minNumInstances, getWeightTrimBeta(), getUseAIC());
        
        m_sons[i].buildTree(localInstances[i], mergeArrays(m_regressions, m_higherRegressions), m_totalInstanceWeight, m_numParameters);
        
        localInstances[i] = null;
      }
    }
    else {
      m_leafclass = m_localModel.distribution().maxClass();
    }
  }
  








  public double prune()
    throws Exception
  {
    double treeError = 0.0D;
    



    double errorsLeaf = getEstimatedErrorsForDistribution(m_localModel.distribution());
    if (m_isLeaf) {
      return errorsLeaf;
    }
    
    double errorsConstModel = getEtimateConstModel(m_localModel.distribution());
    double errorsTree = 0.0D;
    for (int i = 0; i < m_sons.length; i++) {
      double probBranch = m_localModel.distribution().perBag(i) / m_localModel.distribution().total();
      
      errorsTree += probBranch * m_sons[i].prune();
    }
    

    if ((Utils.smOrEq(errorsLeaf, errorsTree)) && (Utils.smOrEq(errorsLeaf, errorsConstModel)))
    {
      m_sons = null;
      m_isLeaf = true;
      m_hasConstr = false;
      m_leafclass = m_localModel.distribution().maxClass();
      
      m_localModel = new NoSplit(m_localModel.distribution());
      treeError = errorsLeaf;


    }
    else if (Utils.smOrEq(errorsConstModel, errorsTree))
    {
      m_sons = null;
      m_isLeaf = true;
      m_hasConstr = true;
      
      m_localModel = new NoSplit(m_localModel.distribution());
      treeError = errorsConstModel;
    } else {
      treeError = errorsTree;
    }
    
    return treeError;
  }
  

  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probs;
    
    double[] probs;
    
    if ((m_isLeaf) && (m_hasConstr))
    {
      probs = modelDistributionForInstance(instance);
    }
    else if ((m_isLeaf) && (!m_hasConstr))
    {
      double[] probs = new double[instance.numClasses()];
      probs[m_leafclass] = 1.0D;
    }
    else {
      int branch = m_localModel.whichSubset(instance);
      probs = m_sons[branch].distributionForInstance(instance);
    }
    
    return probs;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

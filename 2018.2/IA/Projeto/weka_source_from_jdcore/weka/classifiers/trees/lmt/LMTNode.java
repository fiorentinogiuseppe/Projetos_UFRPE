package weka.classifiers.trees.lmt;

import java.util.Collections;
import java.util.Vector;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.ModelSelection;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.NominalToBinary;























































































public class LMTNode
  extends LogisticBase
{
  static final long serialVersionUID = 1862737145870398755L;
  protected double m_totalInstanceWeight;
  protected int m_id;
  protected int m_leafModelNum;
  public double m_alpha;
  public double m_numIncorrectModel;
  public double m_numIncorrectTree;
  protected int m_minNumInstances;
  protected ModelSelection m_modelSelection;
  protected NominalToBinary m_nominalToBinary;
  protected SimpleLinearRegression[][] m_higherRegressions;
  protected int m_numHigherRegressions = 0;
  

  protected static int m_numFoldsPruning = 5;
  



  protected boolean m_fastRegression;
  



  protected int m_numInstances;
  



  protected ClassifierSplitModel m_localModel;
  


  protected LMTNode[] m_sons;
  


  protected boolean m_isLeaf;
  



  public LMTNode(ModelSelection modelSelection, int numBoostingIterations, boolean fastRegression, boolean errorOnProbabilities, int minNumInstances, double weightTrimBeta, boolean useAIC)
  {
    m_modelSelection = modelSelection;
    m_fixedNumIterations = numBoostingIterations;
    m_fastRegression = fastRegression;
    m_errorOnProbabilities = errorOnProbabilities;
    m_minNumInstances = minNumInstances;
    m_maxIterations = 200;
    setWeightTrimBeta(weightTrimBeta);
    setUseAIC(useAIC);
  }
  









  public void buildClassifier(Instances data)
    throws Exception
  {
    if ((m_fastRegression) && (m_fixedNumIterations < 0)) { m_fixedNumIterations = tryLogistic(data);
    }
    
    Instances cvData = new Instances(data);
    cvData.stratify(m_numFoldsPruning);
    
    double[][] alphas = new double[m_numFoldsPruning][];
    double[][] errors = new double[m_numFoldsPruning][];
    
    for (int i = 0; i < m_numFoldsPruning; i++)
    {
      Instances train = cvData.trainCV(m_numFoldsPruning, i);
      Instances test = cvData.testCV(m_numFoldsPruning, i);
      
      buildTree(train, (SimpleLinearRegression[][])null, train.numInstances(), 0.0D);
      
      int numNodes = getNumInnerNodes();
      alphas[i] = new double[numNodes + 2];
      errors[i] = new double[numNodes + 2];
      

      prune(alphas[i], errors[i], test);
    }
    

    buildTree(data, (SimpleLinearRegression[][])null, data.numInstances(), 0.0D);
    int numNodes = getNumInnerNodes();
    
    double[] treeAlphas = new double[numNodes + 2];
    

    int iterations = prune(treeAlphas, null, null);
    
    double[] treeErrors = new double[numNodes + 2];
    
    for (int i = 0; i <= iterations; i++)
    {
      double alpha = Math.sqrt(treeAlphas[i] * treeAlphas[(i + 1)]);
      double error = 0.0D;
      


      for (int k = 0; k < m_numFoldsPruning; k++) {
        int l = 0;
        while (alphas[k][l] <= alpha) l++;
        error += errors[k][(l - 1)];
      }
      
      treeErrors[i] = error;
    }
    

    int best = -1;
    double bestError = Double.MAX_VALUE;
    for (int i = iterations; i >= 0; i--) {
      if (treeErrors[i] < bestError) {
        bestError = treeErrors[i];
        best = i;
      }
    }
    
    double bestAlpha = Math.sqrt(treeAlphas[best] * treeAlphas[(best + 1)]);
    

    unprune();
    

    prune(bestAlpha);
    cleanup();
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
    
    boolean grow;
    boolean grow;
    if (m_numInstances > m_minNumInstances)
    {
      if ((m_modelSelection instanceof ResidualModelSelection))
      {
        double[][] probs = getProbs(getFs(m_numericData));
        double[][] trainYs = getYs(m_train);
        double[][] dataZs = getZs(probs, trainYs);
        double[][] dataWs = getWs(probs, trainYs);
        m_localModel = ((ResidualModelSelection)m_modelSelection).selectModel(m_train, dataZs, dataWs);
      } else {
        m_localModel = m_modelSelection.selectModel(m_train);
      }
      
      grow = m_localModel.numSubsets() > 1;
    } else {
      grow = false;
    }
    
    if (grow)
    {
      m_isLeaf = false;
      Instances[] localInstances = m_localModel.split(m_train);
      m_sons = new LMTNode[m_localModel.numSubsets()];
      for (int i = 0; i < m_sons.length; i++) {
        m_sons[i] = new LMTNode(m_modelSelection, m_fixedNumIterations, m_fastRegression, m_errorOnProbabilities, m_minNumInstances, getWeightTrimBeta(), getUseAIC());
        





        m_sons[i].buildTree(localInstances[i], mergeArrays(m_regressions, m_higherRegressions), m_totalInstanceWeight, m_numParameters);
        
        localInstances[i] = null;
      }
    }
  }
  







  public void prune(double alpha)
    throws Exception
  {
    CompareNode comparator = new CompareNode();
    

    modelErrors();
    treeErrors();
    calculateAlphas();
    

    Vector nodeList = getNodes();
    
    boolean prune = nodeList.size() > 0;
    
    while (prune)
    {

      LMTNode nodeToPrune = (LMTNode)Collections.min(nodeList, comparator);
      

      if (m_alpha > alpha)
        break;
      m_isLeaf = true;
      m_sons = null;
      

      treeErrors();
      calculateAlphas();
      
      nodeList = getNodes();
      prune = nodeList.size() > 0;
    }
  }
  










  public int prune(double[] alphas, double[] errors, Instances test)
    throws Exception
  {
    CompareNode comparator = new CompareNode();
    

    modelErrors();
    treeErrors();
    calculateAlphas();
    

    Vector nodeList = getNodes();
    
    boolean prune = nodeList.size() > 0;
    

    alphas[0] = 0.0D;
    



    if (errors != null) {
      Evaluation eval = new Evaluation(test);
      eval.evaluateModel(this, test, new Object[0]);
      errors[0] = eval.errorRate();
    }
    
    int iteration = 0;
    while (prune)
    {
      iteration++;
      

      LMTNode nodeToPrune = (LMTNode)Collections.min(nodeList, comparator);
      
      m_isLeaf = true;
      


      alphas[iteration] = m_alpha;
      

      if (errors != null) {
        Evaluation eval = new Evaluation(test);
        eval.evaluateModel(this, test, new Object[0]);
        errors[iteration] = eval.errorRate();
      }
      

      treeErrors();
      calculateAlphas();
      
      nodeList = getNodes();
      prune = nodeList.size() > 0;
    }
    

    alphas[(iteration + 1)] = 1.0D;
    return iteration;
  }
  





  protected void unprune()
  {
    if (m_sons != null) {
      m_isLeaf = false;
      for (int i = 0; i < m_sons.length; i++) { m_sons[i].unprune();
      }
    }
  }
  






  protected int tryLogistic(Instances data)
    throws Exception
  {
    Instances filteredData = new Instances(data);
    NominalToBinary nominalToBinary = new NominalToBinary();
    nominalToBinary.setInputFormat(filteredData);
    filteredData = Filter.useFilter(filteredData, nominalToBinary);
    
    LogisticBase logistic = new LogisticBase(0, true, m_errorOnProbabilities);
    

    logistic.setMaxIterations(200);
    logistic.setWeightTrimBeta(getWeightTrimBeta());
    logistic.setUseAIC(getUseAIC());
    logistic.buildClassifier(filteredData);
    

    return logistic.getNumRegressions();
  }
  



  public int getNumInnerNodes()
  {
    if (m_isLeaf) return 0;
    int numNodes = 1;
    for (int i = 0; i < m_sons.length; i++) numNodes += m_sons[i].getNumInnerNodes();
    return numNodes;
  }
  


  public int getNumLeaves()
  {
    int numLeaves;
    

    if (!m_isLeaf) {
      int numLeaves = 0;
      int numEmptyLeaves = 0;
      for (int i = 0; i < m_sons.length; i++) {
        numLeaves += m_sons[i].getNumLeaves();
        if ((m_sons[i].m_isLeaf) && (!m_sons[i].hasModels())) numEmptyLeaves++;
      }
      if (numEmptyLeaves > 1) {
        numLeaves -= numEmptyLeaves - 1;
      }
    } else {
      numLeaves = 1;
    }
    return numLeaves;
  }
  


  public void modelErrors()
    throws Exception
  {
    Evaluation eval = new Evaluation(m_train);
    
    if (!m_isLeaf) {
      m_isLeaf = true;
      eval.evaluateModel(this, m_train, new Object[0]);
      m_isLeaf = false;
      m_numIncorrectModel = eval.incorrect();
      for (int i = 0; i < m_sons.length; i++) m_sons[i].modelErrors();
    } else {
      eval.evaluateModel(this, m_train, new Object[0]);
      m_numIncorrectModel = eval.incorrect();
    }
  }
  


  public void treeErrors()
  {
    if (m_isLeaf) {
      m_numIncorrectTree = m_numIncorrectModel;
    } else {
      m_numIncorrectTree = 0.0D;
      for (int i = 0; i < m_sons.length; i++) {
        m_sons[i].treeErrors();
        m_numIncorrectTree += m_sons[i].m_numIncorrectTree;
      }
    }
  }
  


  public void calculateAlphas()
    throws Exception
  {
    if (!m_isLeaf) {
      double errorDiff = m_numIncorrectModel - m_numIncorrectTree;
      
      if (errorDiff <= 0.0D)
      {

        m_isLeaf = true;
        m_sons = null;
        m_alpha = Double.MAX_VALUE;
      }
      else {
        errorDiff /= m_totalInstanceWeight;
        m_alpha = (errorDiff / (getNumLeaves() - 1));
        
        for (int i = 0; i < m_sons.length; i++) m_sons[i].calculateAlphas();
      }
    }
    else {
      m_alpha = Double.MAX_VALUE;
    }
  }
  







  protected SimpleLinearRegression[][] mergeArrays(SimpleLinearRegression[][] a1, SimpleLinearRegression[][] a2)
  {
    int numModels1 = a1[0].length;
    int numModels2 = a2[0].length;
    
    SimpleLinearRegression[][] result = new SimpleLinearRegression[m_numClasses][numModels1 + numModels2];
    

    for (int i = 0; i < m_numClasses; i++) {
      for (int j = 0; j < numModels1; j++)
        result[i][j] = a1[i][j];
    }
    for (int i = 0; i < m_numClasses; i++)
      for (int j = 0; j < numModels2; j++) result[i][(j + numModels1)] = a2[i][j];
    return result;
  }
  



  public Vector getNodes()
  {
    Vector nodeList = new Vector();
    getNodes(nodeList);
    return nodeList;
  }
  




  public void getNodes(Vector nodeList)
  {
    if (!m_isLeaf) {
      nodeList.add(this);
      for (int i = 0; i < m_sons.length; i++) { m_sons[i].getNodes(nodeList);
      }
    }
  }
  



  protected Instances getNumericData(Instances train)
    throws Exception
  {
    Instances filteredData = new Instances(train);
    m_nominalToBinary = new NominalToBinary();
    m_nominalToBinary.setInputFormat(filteredData);
    filteredData = Filter.useFilter(filteredData, m_nominalToBinary);
    
    return super.getNumericData(filteredData);
  }
  






  protected double[] getFs(Instance instance)
    throws Exception
  {
    double[] pred = new double[m_numClasses];
    




    double[] instanceFs = super.getFs(instance);
    

    for (int i = 0; i < m_numHigherRegressions; i++) {
      double predSum = 0.0D;
      for (int j = 0; j < m_numClasses; j++) {
        pred[j] = m_higherRegressions[j][i].classifyInstance(instance);
        predSum += pred[j];
      }
      predSum /= m_numClasses;
      for (int j = 0; j < m_numClasses; j++) {
        instanceFs[j] += (pred[j] - predSum) * (m_numClasses - 1) / m_numClasses;
      }
    }
    
    return instanceFs;
  }
  




  public boolean hasModels()
  {
    return m_numRegressions > 0;
  }
  





  public double[] modelDistributionForInstance(Instance instance)
    throws Exception
  {
    instance = (Instance)instance.copy();
    m_nominalToBinary.input(instance);
    instance = m_nominalToBinary.output();
    

    instance.setDataset(m_numericDataHeader);
    
    return probs(getFs(instance));
  }
  


  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] probs;
    
    double[] probs;
    
    if (m_isLeaf)
    {
      probs = modelDistributionForInstance(instance);
    }
    else {
      int branch = m_localModel.whichSubset(instance);
      probs = m_sons[branch].distributionForInstance(instance);
    }
    return probs;
  }
  



  public int numLeaves()
  {
    if (m_isLeaf) return 1;
    int numLeaves = 0;
    for (int i = 0; i < m_sons.length; i++) numLeaves += m_sons[i].numLeaves();
    return numLeaves;
  }
  



  public int numNodes()
  {
    if (m_isLeaf) return 1;
    int numNodes = 1;
    for (int i = 0; i < m_sons.length; i++) numNodes += m_sons[i].numNodes();
    return numNodes;
  }
  




  public String toString()
  {
    assignLeafModelNumbers(0);
    try {
      StringBuffer text = new StringBuffer();
      
      if (m_isLeaf) {
        text.append(": ");
        text.append("LM_" + m_leafModelNum + ":" + getModelParameters());
      } else {
        dumpTree(0, text);
      }
      text.append("\n\nNumber of Leaves  : \t" + numLeaves() + "\n");
      text.append("\nSize of the Tree : \t" + numNodes() + "\n");
      

      text.append(modelsToString());
      return text.toString();
    } catch (Exception e) {}
    return "Can't print logistic model tree";
  }
  









  public String getModelParameters()
  {
    StringBuffer text = new StringBuffer();
    int numModels = m_numRegressions + m_numHigherRegressions;
    text.append(m_numRegressions + "/" + numModels + " (" + m_numInstances + ")");
    return text.toString();
  }
  






  protected void dumpTree(int depth, StringBuffer text)
    throws Exception
  {
    for (int i = 0; i < m_sons.length; i++) {
      text.append("\n");
      for (int j = 0; j < depth; j++)
        text.append("|   ");
      text.append(m_localModel.leftSide(m_train));
      text.append(m_localModel.rightSide(i, m_train));
      if (m_sons[i].m_isLeaf) {
        text.append(": ");
        text.append("LM_" + m_sons[i].m_leafModelNum + ":" + m_sons[i].getModelParameters());
      } else {
        m_sons[i].dumpTree(depth + 1, text);
      }
    }
  }
  


  public int assignIDs(int lastID)
  {
    int currLastID = lastID + 1;
    
    m_id = currLastID;
    if (m_sons != null) {
      for (int i = 0; i < m_sons.length; i++) {
        currLastID = m_sons[i].assignIDs(currLastID);
      }
    }
    return currLastID;
  }
  


  public int assignLeafModelNumbers(int leafCounter)
  {
    if (!m_isLeaf) {
      m_leafModelNum = 0;
      for (int i = 0; i < m_sons.length; i++) {
        leafCounter = m_sons[i].assignLeafModelNumbers(leafCounter);
      }
    } else {
      leafCounter++;
      m_leafModelNum = leafCounter;
    }
    return leafCounter;
  }
  








  protected double[][] getCoefficients()
  {
    double[][] coefficients = super.getCoefficients();
    
    double constFactor = (m_numClasses - 1) / m_numClasses;
    for (int j = 0; j < m_numClasses; j++) {
      for (int i = 0; i < m_numHigherRegressions; i++) {
        double slope = m_higherRegressions[j][i].getSlope();
        double intercept = m_higherRegressions[j][i].getIntercept();
        int attribute = m_higherRegressions[j][i].getAttributeIndex();
        coefficients[j][0] += constFactor * intercept;
        coefficients[j][(attribute + 1)] += constFactor * slope;
      }
    }
    
    return coefficients;
  }
  



  public String modelsToString()
  {
    StringBuffer text = new StringBuffer();
    if (m_isLeaf) {
      text.append("LM_" + m_leafModelNum + ":" + super.toString());
    } else {
      for (int i = 0; i < m_sons.length; i++) {
        text.append("\n" + m_sons[i].modelsToString());
      }
    }
    return text.toString();
  }
  




  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    assignIDs(-1);
    assignLeafModelNumbers(0);
    text.append("digraph LMTree {\n");
    if (m_isLeaf) {
      text.append("N" + m_id + " [label=\"LM_" + m_leafModelNum + ":" + getModelParameters() + "\" " + "shape=box style=filled");
      
      text.append("]\n");
    } else {
      text.append("N" + m_id + " [label=\"" + Utils.backQuoteChars(m_localModel.leftSide(m_train)) + "\" ");
      

      text.append("]\n");
      graphTree(text);
    }
    
    return text.toString() + "}\n";
  }
  




  private void graphTree(StringBuffer text)
    throws Exception
  {
    for (int i = 0; i < m_sons.length; i++) {
      text.append("N" + m_id + "->" + "N" + m_sons[i].m_id + " [label=\"" + Utils.backQuoteChars(m_localModel.rightSide(i, m_train).trim()) + "\"]\n");
      



      if (m_sons[i].m_isLeaf) {
        text.append("N" + m_sons[i].m_id + " [label=\"LM_" + m_sons[i].m_leafModelNum + ":" + m_sons[i].getModelParameters() + "\" " + "shape=box style=filled");
        
        text.append("]\n");
      } else {
        text.append("N" + m_sons[i].m_id + " [label=\"" + Utils.backQuoteChars(m_sons[i].m_localModel.leftSide(m_train)) + "\" ");
        

        text.append("]\n");
        m_sons[i].graphTree(text);
      }
    }
  }
  


  public void cleanup()
  {
    super.cleanup();
    if (!m_isLeaf) {
      for (int i = 0; i < m_sons.length; i++) { m_sons[i].cleanup();
      }
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}

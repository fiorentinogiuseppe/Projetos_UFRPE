package weka.classifiers.trees;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableClassifier;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.matrix.EigenvalueDecomposition;
import weka.core.matrix.Matrix;








































































































public class SimpleCart
  extends RandomizableClassifier
  implements AdditionalMeasureProducer, TechnicalInformationHandler
{
  private static final long serialVersionUID = 4154189200352566053L;
  protected Instances m_train;
  protected SimpleCart[] m_Successors;
  protected Attribute m_Attribute;
  protected double m_SplitValue;
  protected String m_SplitString;
  protected double m_ClassValue;
  protected Attribute m_ClassAttribute;
  protected double m_minNumObj = 2.0D;
  

  protected int m_numFoldsPruning = 5;
  

  protected double m_Alpha;
  

  protected double m_numIncorrectModel;
  

  protected double m_numIncorrectTree;
  

  protected boolean m_isLeaf;
  

  protected boolean m_Prune = true;
  

  protected int m_totalTrainInstances;
  

  protected double[] m_Props;
  

  protected double[] m_ClassProbs = null;
  

  protected double[] m_Distribution;
  

  protected boolean m_Heuristic = true;
  

  protected boolean m_UseOneSE = false;
  

  protected double m_SizePer = 1.0D;
  


  public SimpleCart() {}
  

  public String globalInfo()
  {
    return "Class implementing minimal cost-complexity pruning.\nNote when dealing with missing values, use \"fractional instances\" method instead of surrogate split method.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Leo Breiman and Jerome H. Friedman and Richard A. Olshen and Charles J. Stone");
    result.setValue(TechnicalInformation.Field.YEAR, "1984");
    result.setValue(TechnicalInformation.Field.TITLE, "Classification and Regression Trees");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Wadsworth International Group");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Belmont, California");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    data = new Instances(data);
    data.deleteWithMissingClass();
    

    if (!m_Prune)
    {

      int[][] sortedIndices = new int[data.numAttributes()][0];
      double[][] weights = new double[data.numAttributes()][0];
      double[] classProbs = new double[data.numClasses()];
      double totalWeight = computeSortedInfo(data, sortedIndices, weights, classProbs);
      
      makeTree(data, data.numInstances(), sortedIndices, weights, classProbs, totalWeight, m_minNumObj, m_Heuristic);
      
      return;
    }
    
    Random random = new Random(m_Seed);
    Instances cvData = new Instances(data);
    cvData.randomize(random);
    cvData = new Instances(cvData, 0, (int)(cvData.numInstances() * m_SizePer) - 1);
    cvData.stratify(m_numFoldsPruning);
    
    double[][] alphas = new double[m_numFoldsPruning][];
    double[][] errors = new double[m_numFoldsPruning][];
    

    for (int i = 0; i < m_numFoldsPruning; i++)
    {

      Instances train = cvData.trainCV(m_numFoldsPruning, i);
      Instances test = cvData.testCV(m_numFoldsPruning, i);
      

      int[][] sortedIndices = new int[train.numAttributes()][0];
      double[][] weights = new double[train.numAttributes()][0];
      double[] classProbs = new double[train.numClasses()];
      double totalWeight = computeSortedInfo(train, sortedIndices, weights, classProbs);
      
      makeTree(train, train.numInstances(), sortedIndices, weights, classProbs, totalWeight, m_minNumObj, m_Heuristic);
      

      int numNodes = numInnerNodes();
      alphas[i] = new double[numNodes + 2];
      errors[i] = new double[numNodes + 2];
      

      prune(alphas[i], errors[i], test);
    }
    

    int[][] sortedIndices = new int[data.numAttributes()][0];
    double[][] weights = new double[data.numAttributes()][0];
    double[] classProbs = new double[data.numClasses()];
    double totalWeight = computeSortedInfo(data, sortedIndices, weights, classProbs);
    

    makeTree(data, data.numInstances(), sortedIndices, weights, classProbs, totalWeight, m_minNumObj, m_Heuristic);
    

    int numNodes = numInnerNodes();
    
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
      treeErrors[i] = (error / m_numFoldsPruning);
    }
    

    int best = -1;
    double bestError = Double.MAX_VALUE;
    for (int i = iterations; i >= 0; i--) {
      if (treeErrors[i] < bestError) {
        bestError = treeErrors[i];
        best = i;
      }
    }
    

    if (m_UseOneSE) {
      double oneSE = Math.sqrt(bestError * (1.0D - bestError) / data.numInstances());
      for (int i = iterations; i >= 0; i--) {
        if (treeErrors[i] <= bestError + oneSE) {
          best = i;
          break;
        }
      }
    }
    
    double bestAlpha = Math.sqrt(treeAlphas[best] * treeAlphas[(best + 1)]);
    

    unprune();
    prune(bestAlpha);
  }
  















  protected void makeTree(Instances data, int totalInstances, int[][] sortedIndices, double[][] weights, double[] classProbs, double totalWeight, double minNumObj, boolean useHeuristic)
    throws Exception
  {
    if (totalWeight == 0.0D) {
      m_Attribute = null;
      m_ClassValue = Instance.missingValue();
      m_Distribution = new double[data.numClasses()];
      return;
    }
    
    m_totalTrainInstances = totalInstances;
    m_isLeaf = true;
    m_Successors = null;
    
    m_ClassProbs = new double[classProbs.length];
    m_Distribution = new double[classProbs.length];
    System.arraycopy(classProbs, 0, m_ClassProbs, 0, classProbs.length);
    System.arraycopy(classProbs, 0, m_Distribution, 0, classProbs.length);
    if (Utils.sum(m_ClassProbs) != 0.0D) { Utils.normalize(m_ClassProbs);
    }
    

    double[][][] dists = new double[data.numAttributes()][0][0];
    double[][] props = new double[data.numAttributes()][0];
    double[][] totalSubsetWeights = new double[data.numAttributes()][2];
    double[] splits = new double[data.numAttributes()];
    String[] splitString = new String[data.numAttributes()];
    double[] giniGains = new double[data.numAttributes()];
    

    for (int i = 0; i < data.numAttributes(); i++) {
      Attribute att = data.attribute(i);
      if (i != data.classIndex()) {
        if (att.isNumeric())
        {
          splits[i] = numericDistribution(props, dists, att, sortedIndices[i], weights[i], totalSubsetWeights, giniGains, data);
        }
        else
        {
          splitString[i] = nominalDistribution(props, dists, att, sortedIndices[i], weights[i], totalSubsetWeights, giniGains, data, useHeuristic);
        }
      }
    }
    

    int attIndex = Utils.maxIndex(giniGains);
    m_Attribute = data.attribute(attIndex);
    
    m_train = new Instances(data, sortedIndices[attIndex].length);
    for (int i = 0; i < sortedIndices[attIndex].length; i++) {
      Instance inst = data.instance(sortedIndices[attIndex][i]);
      Instance instCopy = (Instance)inst.copy();
      instCopy.setWeight(weights[attIndex][i]);
      m_train.add(instCopy);
    }
    


    if ((totalWeight < 2.0D * minNumObj) || (giniGains[attIndex] == 0.0D) || (props[attIndex][0] == 0.0D) || (props[attIndex][1] == 0.0D))
    {
      makeLeaf(data);
    }
    else
    {
      m_Props = props[attIndex];
      int[][][] subsetIndices = new int[2][data.numAttributes()][0];
      double[][][] subsetWeights = new double[2][data.numAttributes()][0];
      

      if (m_Attribute.isNumeric()) { m_SplitValue = splits[attIndex];
      }
      else {
        m_SplitString = splitString[attIndex];
      }
      splitData(subsetIndices, subsetWeights, m_Attribute, m_SplitValue, m_SplitString, sortedIndices, weights, data);
      



      if ((subsetIndices[0][attIndex].length < minNumObj) || (subsetIndices[1][attIndex].length < minNumObj))
      {
        makeLeaf(data);
        return;
      }
      

      m_isLeaf = false;
      m_Successors = new SimpleCart[2];
      for (int i = 0; i < 2; i++) {
        m_Successors[i] = new SimpleCart();
        m_Successors[i].makeTree(data, m_totalTrainInstances, subsetIndices[i], subsetWeights[i], dists[attIndex][i], totalSubsetWeights[attIndex][i], minNumObj, useHeuristic);
      }
    }
  }
  












  public void prune(double alpha)
    throws Exception
  {
    modelErrors();
    treeErrors();
    calculateAlphas();
    

    Vector nodeList = getInnerNodes();
    
    boolean prune = nodeList.size() > 0;
    double preAlpha = Double.MAX_VALUE;
    while (prune)
    {

      SimpleCart nodeToPrune = nodeToPrune(nodeList);
      

      if (m_Alpha > alpha) {
        break;
      }
      
      nodeToPrune.makeLeaf(m_train);
      

      if (m_Alpha == preAlpha) {
        nodeToPrune.makeLeaf(m_train);
        treeErrors();
        calculateAlphas();
        nodeList = getInnerNodes();
        prune = nodeList.size() > 0;
      }
      else {
        preAlpha = m_Alpha;
        

        treeErrors();
        calculateAlphas();
        
        nodeList = getInnerNodes();
        prune = nodeList.size() > 0;
      }
    }
  }
  















  public int prune(double[] alphas, double[] errors, Instances test)
    throws Exception
  {
    modelErrors();
    treeErrors();
    calculateAlphas();
    

    Vector nodeList = getInnerNodes();
    
    boolean prune = nodeList.size() > 0;
    

    alphas[0] = 0.0D;
    



    if (errors != null) {
      Evaluation eval = new Evaluation(test);
      eval.evaluateModel(this, test, new Object[0]);
      errors[0] = eval.errorRate();
    }
    
    int iteration = 0;
    double preAlpha = Double.MAX_VALUE;
    while (prune)
    {
      iteration++;
      

      SimpleCart nodeToPrune = nodeToPrune(nodeList);
      

      m_isLeaf = true;
      

      if (m_Alpha == preAlpha) {
        iteration--;
        treeErrors();
        calculateAlphas();
        nodeList = getInnerNodes();
        prune = nodeList.size() > 0;

      }
      else
      {
        alphas[iteration] = m_Alpha;
        

        if (errors != null) {
          Evaluation eval = new Evaluation(test);
          eval.evaluateModel(this, test, new Object[0]);
          errors[iteration] = eval.errorRate();
        }
        preAlpha = m_Alpha;
        

        treeErrors();
        calculateAlphas();
        
        nodeList = getInnerNodes();
        prune = nodeList.size() > 0;
      }
    }
    
    alphas[(iteration + 1)] = 1.0D;
    return iteration;
  }
  



  protected void unprune()
  {
    if (m_Successors != null) {
      m_isLeaf = false;
      for (int i = 0; i < m_Successors.length; i++) { m_Successors[i].unprune();
      }
    }
  }
  
















  protected double numericDistribution(double[][] props, double[][][] dists, Attribute att, int[] sortedIndices, double[] weights, double[][] subsetWeights, double[] giniGains, Instances data)
    throws Exception
  {
    double splitPoint = NaN.0D;
    double[][] dist = (double[][])null;
    int numClasses = data.numClasses();
    

    double[][] currDist = new double[2][numClasses];
    dist = new double[2][numClasses];
    

    double[] parentDist = new double[numClasses];
    int missingStart = 0;
    for (int j = 0; j < sortedIndices.length; j++) {
      Instance inst = data.instance(sortedIndices[j]);
      if (!inst.isMissing(att)) {
        missingStart++;
        currDist[1][((int)inst.classValue())] += weights[j];
      }
      parentDist[((int)inst.classValue())] += weights[j];
    }
    System.arraycopy(currDist[1], 0, dist[1], 0, dist[1].length);
    

    double currSplit = data.instance(sortedIndices[0]).value(att);
    
    double bestGiniGain = -1.7976931348623157E308D;
    
    for (int i = 0; i < sortedIndices.length; i++) {
      Instance inst = data.instance(sortedIndices[i]);
      if (inst.isMissing(att)) {
        break;
      }
      if (inst.value(att) > currSplit)
      {
        double[][] tempDist = new double[2][numClasses];
        for (int k = 0; k < 2; k++)
        {
          System.arraycopy(currDist[k], 0, tempDist[k], 0, tempDist[k].length);
        }
        
        double[] tempProps = new double[2];
        for (int k = 0; k < 2; k++) {
          tempProps[k] = Utils.sum(tempDist[k]);
        }
        
        if (Utils.sum(tempProps) != 0.0D) { Utils.normalize(tempProps);
        }
        
        int index = missingStart;
        while (index < sortedIndices.length) {
          Instance insta = data.instance(sortedIndices[index]);
          for (int j = 0; j < 2; j++) {
            tempDist[j][((int)insta.classValue())] += tempProps[j] * weights[index];
          }
          index++;
        }
        
        double currGiniGain = computeGiniGain(parentDist, tempDist);
        
        if (currGiniGain > bestGiniGain) {
          bestGiniGain = currGiniGain;
          


          splitPoint = (inst.value(att) + currSplit) / 2.0D;
          
          for (int j = 0; j < currDist.length; j++) {
            System.arraycopy(tempDist[j], 0, dist[j], 0, dist[j].length);
          }
        }
      }
      
      currSplit = inst.value(att);
      currDist[0][((int)inst.classValue())] += weights[i];
      currDist[1][((int)inst.classValue())] -= weights[i];
    }
    

    int attIndex = att.index();
    props[attIndex] = new double[2];
    for (int k = 0; k < 2; k++) {
      props[attIndex][k] = Utils.sum(dist[k]);
    }
    if (Utils.sum(props[attIndex]) != 0.0D) { Utils.normalize(props[attIndex]);
    }
    
    subsetWeights[attIndex] = new double[2];
    for (int j = 0; j < 2; j++) {
      subsetWeights[attIndex][j] += Utils.sum(dist[j]);
    }
    


    giniGains[attIndex] = bestGiniGain;
    dists[attIndex] = dist;
    
    return splitPoint;
  }
  


















  protected String nominalDistribution(double[][] props, double[][][] dists, Attribute att, int[] sortedIndices, double[] weights, double[][] subsetWeights, double[] giniGains, Instances data, boolean useHeuristic)
    throws Exception
  {
    String[] values = new String[att.numValues()];
    int numCat = values.length;
    int numClasses = data.numClasses();
    
    String bestSplitString = "";
    double bestGiniGain = -1.7976931348623157E308D;
    

    int[] classFreq = new int[numCat];
    for (int j = 0; j < numCat; j++) { classFreq[j] = 0;
    }
    double[] parentDist = new double[numClasses];
    double[][] currDist = new double[2][numClasses];
    double[][] dist = new double[2][numClasses];
    int missingStart = 0;
    
    for (int i = 0; i < sortedIndices.length; i++) {
      Instance inst = data.instance(sortedIndices[i]);
      if (!inst.isMissing(att)) {
        missingStart++;
        classFreq[((int)inst.value(att))] += 1;
      }
      parentDist[((int)inst.classValue())] += weights[i];
    }
    

    int nonEmpty = 0;
    for (int j = 0; j < numCat; j++) {
      if (classFreq[j] != 0) { nonEmpty++;
      }
    }
    
    String[] nonEmptyValues = new String[nonEmpty];
    int nonEmptyIndex = 0;
    for (int j = 0; j < numCat; j++) {
      if (classFreq[j] != 0) {
        nonEmptyValues[nonEmptyIndex] = att.value(j);
        nonEmptyIndex++;
      }
    }
    

    int empty = numCat - nonEmpty;
    String[] emptyValues = new String[empty];
    int emptyIndex = 0;
    for (int j = 0; j < numCat; j++) {
      if (classFreq[j] == 0) {
        emptyValues[emptyIndex] = att.value(j);
        emptyIndex++;
      }
    }
    
    if (nonEmpty <= 1) {
      giniGains[att.index()] = 0.0D;
      return "";
    }
    

    if (data.numClasses() == 2)
    {



      double[] pClass0 = new double[nonEmpty];
      
      double[][] valDist = new double[nonEmpty][2];
      
      for (int j = 0; j < nonEmpty; j++) {
        for (int k = 0; k < 2; k++) {
          valDist[j][k] = 0.0D;
        }
      }
      
      for (int i = 0; i < sortedIndices.length; i++) {
        Instance inst = data.instance(sortedIndices[i]);
        if (inst.isMissing(att)) {
          break;
        }
        
        for (int j = 0; j < nonEmpty; j++) {
          if (att.value((int)inst.value(att)).compareTo(nonEmptyValues[j]) == 0) {
            valDist[j][((int)inst.classValue())] += inst.weight();
            break;
          }
        }
      }
      
      for (int j = 0; j < nonEmpty; j++) {
        double distSum = Utils.sum(valDist[j]);
        if (distSum == 0.0D) pClass0[j] = 0.0D; else {
          pClass0[j] = (valDist[j][0] / distSum);
        }
      }
      
      String[] sortedValues = new String[nonEmpty];
      for (int j = 0; j < nonEmpty; j++) {
        sortedValues[j] = nonEmptyValues[Utils.minIndex(pClass0)];
        pClass0[Utils.minIndex(pClass0)] = Double.MAX_VALUE;
      }
      



      String tempStr = "";
      
      for (int j = 0; j < nonEmpty - 1; j++) {
        currDist = new double[2][numClasses];
        if (tempStr == "") tempStr = "(" + sortedValues[j] + ")"; else
          tempStr = tempStr + "|(" + sortedValues[j] + ")";
        for (int i = 0; i < sortedIndices.length; i++) {
          Instance inst = data.instance(sortedIndices[i]);
          if (inst.isMissing(att)) {
            break;
          }
          
          if (tempStr.indexOf("(" + att.value((int)inst.value(att)) + ")") != -1)
          {
            currDist[0][((int)inst.classValue())] += weights[i]; } else {
            currDist[1][((int)inst.classValue())] += weights[i];
          }
        }
        double[][] tempDist = new double[2][numClasses];
        for (int kk = 0; kk < 2; kk++) {
          tempDist[kk] = currDist[kk];
        }
        
        double[] tempProps = new double[2];
        for (int kk = 0; kk < 2; kk++) {
          tempProps[kk] = Utils.sum(tempDist[kk]);
        }
        
        if (Utils.sum(tempProps) != 0.0D) { Utils.normalize(tempProps);
        }
        
        int mstart = missingStart;
        while (mstart < sortedIndices.length) {
          Instance insta = data.instance(sortedIndices[mstart]);
          for (int jj = 0; jj < 2; jj++) {
            tempDist[jj][((int)insta.classValue())] += tempProps[jj] * weights[mstart];
          }
          mstart++;
        }
        
        double currGiniGain = computeGiniGain(parentDist, tempDist);
        
        if (currGiniGain > bestGiniGain) {
          bestGiniGain = currGiniGain;
          bestSplitString = tempStr;
          for (int jj = 0; jj < 2; jj++)
          {
            System.arraycopy(tempDist[jj], 0, dist[jj], 0, dist[jj].length);
          }
          
        }
        
      }
      
    }
    else if ((!useHeuristic) || (nonEmpty <= 4))
    {

      for (int i = 0; i < (int)Math.pow(2.0D, nonEmpty - 1); i++) {
        String tempStr = "";
        currDist = new double[2][numClasses];
        
        int bit10 = i;
        for (int j = nonEmpty - 1; j >= 0; j--) {
          int mod = bit10 % 2;
          if (mod == 1) {
            if (tempStr == "") tempStr = "(" + nonEmptyValues[j] + ")"; else
              tempStr = tempStr + "|(" + nonEmptyValues[j] + ")";
          }
          bit10 /= 2;
        }
        for (int j = 0; j < sortedIndices.length; j++) {
          Instance inst = data.instance(sortedIndices[j]);
          if (inst.isMissing(att)) {
            break;
          }
          
          if (tempStr.indexOf("(" + att.value((int)inst.value(att)) + ")") != -1)
            currDist[0][((int)inst.classValue())] += weights[j]; else {
            currDist[1][((int)inst.classValue())] += weights[j];
          }
        }
        double[][] tempDist = new double[2][numClasses];
        for (int k = 0; k < 2; k++) {
          tempDist[k] = currDist[k];
        }
        
        double[] tempProps = new double[2];
        for (int k = 0; k < 2; k++) {
          tempProps[k] = Utils.sum(tempDist[k]);
        }
        
        if (Utils.sum(tempProps) != 0.0D) { Utils.normalize(tempProps);
        }
        
        int index = missingStart;
        while (index < sortedIndices.length) {
          Instance insta = data.instance(sortedIndices[index]);
          for (int j = 0; j < 2; j++) {
            tempDist[j][((int)insta.classValue())] += tempProps[j] * weights[index];
          }
          index++;
        }
        
        double currGiniGain = computeGiniGain(parentDist, tempDist);
        
        if (currGiniGain > bestGiniGain) {
          bestGiniGain = currGiniGain;
          bestSplitString = tempStr;
          for (int j = 0; j < 2; j++)
          {
            System.arraycopy(tempDist[j], 0, dist[j], 0, dist[j].length);
          }
          
        }
        
      }
      
    }
    else
    {
      int n = nonEmpty;
      int k = data.numClasses();
      double[][] P = new double[n][k];
      int[] numInstancesValue = new int[n];
      double[] meanClass = new double[k];
      int numInstances = data.numInstances();
      

      for (int j = 0; j < meanClass.length; j++) { meanClass[j] = 0.0D;
      }
      for (int j = 0; j < numInstances; j++) {
        Instance inst = data.instance(j);
        int valueIndex = 0;
        for (int i = 0; i < nonEmpty; i++) {
          if (att.value((int)inst.value(att)).compareToIgnoreCase(nonEmptyValues[i]) == 0) {
            valueIndex = i;
            break;
          }
        }
        P[valueIndex][((int)inst.classValue())] += 1.0D;
        numInstancesValue[valueIndex] += 1;
        meanClass[((int)inst.classValue())] += 1.0D;
      }
      

      for (int i = 0; i < P.length; i++) {
        for (int j = 0; j < P[0].length; j++) {
          if (numInstancesValue[i] == 0) P[i][j] = 0.0D; else {
            P[i][j] /= numInstancesValue[i];
          }
        }
      }
      
      for (int i = 0; i < meanClass.length; i++) {
        meanClass[i] /= numInstances;
      }
      

      double[][] covariance = new double[k][k];
      for (int i1 = 0; i1 < k; i1++) {
        for (int i2 = 0; i2 < k; i2++) {
          double element = 0.0D;
          for (int j = 0; j < n; j++) {
            element += (P[j][i2] - meanClass[i2]) * (P[j][i1] - meanClass[i1]) * numInstancesValue[j];
          }
          
          covariance[i1][i2] = element;
        }
      }
      
      Matrix matrix = new Matrix(covariance);
      EigenvalueDecomposition eigen = new EigenvalueDecomposition(matrix);
      
      double[] eigenValues = eigen.getRealEigenvalues();
      

      int index = 0;
      double largest = eigenValues[0];
      for (int i = 1; i < eigenValues.length; i++) {
        if (eigenValues[i] > largest) {
          index = i;
          largest = eigenValues[i];
        }
      }
      

      double[] FPC = new double[k];
      Matrix eigenVector = eigen.getV();
      double[][] vectorArray = eigenVector.getArray();
      for (int i = 0; i < FPC.length; i++) {
        FPC[i] = vectorArray[i][index];
      }
      


      double[] Sa = new double[n];
      for (int i = 0; i < Sa.length; i++) {
        Sa[i] = 0.0D;
        for (int j = 0; j < k; j++) {
          Sa[i] += FPC[j] * P[i][j];
        }
      }
      

      double[] pCopy = new double[n];
      System.arraycopy(Sa, 0, pCopy, 0, n);
      String[] sortedValues = new String[n];
      Arrays.sort(Sa);
      
      for (int j = 0; j < n; j++) {
        sortedValues[j] = nonEmptyValues[Utils.minIndex(pCopy)];
        pCopy[Utils.minIndex(pCopy)] = Double.MAX_VALUE;
      }
      

      String tempStr = "";
      
      for (int j = 0; j < nonEmpty - 1; j++) {
        currDist = new double[2][numClasses];
        if (tempStr == "") tempStr = "(" + sortedValues[j] + ")"; else
          tempStr = tempStr + "|(" + sortedValues[j] + ")";
        for (int i = 0; i < sortedIndices.length; i++) {
          Instance inst = data.instance(sortedIndices[i]);
          if (inst.isMissing(att)) {
            break;
          }
          
          if (tempStr.indexOf("(" + att.value((int)inst.value(att)) + ")") != -1)
          {
            currDist[0][((int)inst.classValue())] += weights[i]; } else {
            currDist[1][((int)inst.classValue())] += weights[i];
          }
        }
        double[][] tempDist = new double[2][numClasses];
        for (int kk = 0; kk < 2; kk++) {
          tempDist[kk] = currDist[kk];
        }
        
        double[] tempProps = new double[2];
        for (int kk = 0; kk < 2; kk++) {
          tempProps[kk] = Utils.sum(tempDist[kk]);
        }
        
        if (Utils.sum(tempProps) != 0.0D) { Utils.normalize(tempProps);
        }
        
        int mstart = missingStart;
        while (mstart < sortedIndices.length) {
          Instance insta = data.instance(sortedIndices[mstart]);
          for (int jj = 0; jj < 2; jj++) {
            tempDist[jj][((int)insta.classValue())] += tempProps[jj] * weights[mstart];
          }
          mstart++;
        }
        
        double currGiniGain = computeGiniGain(parentDist, tempDist);
        
        if (currGiniGain > bestGiniGain) {
          bestGiniGain = currGiniGain;
          bestSplitString = tempStr;
          for (int jj = 0; jj < 2; jj++)
          {
            System.arraycopy(tempDist[jj], 0, dist[jj], 0, dist[jj].length);
          }
        }
      }
    }
    


    int attIndex = att.index();
    props[attIndex] = new double[2];
    for (int k = 0; k < 2; k++) {
      props[attIndex][k] = Utils.sum(dist[k]);
    }
    
    if (Utils.sum(props[attIndex]) <= 0.0D) {
      for (int k = 0; k < props[attIndex].length; k++) {
        props[attIndex][k] = (1.0D / props[attIndex].length);
      }
    } else {
      Utils.normalize(props[attIndex]);
    }
    


    subsetWeights[attIndex] = new double[2];
    for (int j = 0; j < 2; j++) {
      subsetWeights[attIndex][j] += Utils.sum(dist[j]);
    }
    


    for (int j = 0; j < empty; j++) {
      if (props[attIndex][0] >= props[attIndex][1]) {
        if (bestSplitString == "") bestSplitString = "(" + emptyValues[j] + ")"; else {
          bestSplitString = bestSplitString + "|(" + emptyValues[j] + ")";
        }
      }
    }
    

    giniGains[attIndex] = bestGiniGain;
    
    dists[attIndex] = dist;
    return bestSplitString;
  }
  




















  protected void splitData(int[][][] subsetIndices, double[][][] subsetWeights, Attribute att, double splitPoint, String splitStr, int[][] sortedIndices, double[][] weights, Instances data)
    throws Exception
  {
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i != data.classIndex()) {
        int[] num = new int[2];
        for (int k = 0; k < 2; k++) {
          subsetIndices[k][i] = new int[sortedIndices[i].length];
          subsetWeights[k][i] = new double[weights[i].length];
        }
        
        for (int j = 0; j < sortedIndices[i].length; j++) {
          Instance inst = data.instance(sortedIndices[i][j]);
          if (inst.isMissing(att))
          {
            for (int k = 0; k < 2; k++)
              if (m_Props[k] > 0.0D) {
                subsetIndices[k][i][num[k]] = sortedIndices[i][j];
                subsetWeights[k][i][num[k]] = (m_Props[k] * weights[i][j]);
                num[k] += 1;
              }
          } else {
            int subset;
            int subset;
            if (att.isNumeric()) {
              subset = inst.value(att) < splitPoint ? 0 : 1;
            } else { int subset;
              if (splitStr.indexOf("(" + att.value((int)inst.value(att.index())) + ")") != -1)
              {
                subset = 0; } else
                subset = 1;
            }
            subsetIndices[subset][i][num[subset]] = sortedIndices[i][j];
            subsetWeights[subset][i][num[subset]] = weights[i][j];
            num[subset] += 1;
          }
        }
        

        for (int k = 0; k < 2; k++) {
          int[] copy = new int[num[k]];
          System.arraycopy(subsetIndices[k][i], 0, copy, 0, num[k]);
          subsetIndices[k][i] = copy;
          double[] copyWeights = new double[num[k]];
          System.arraycopy(subsetWeights[k][i], 0, copyWeights, 0, num[k]);
          subsetWeights[k][i] = copyWeights;
        }
      }
    }
  }
  



  public void modelErrors()
    throws Exception
  {
    Evaluation eval = new Evaluation(m_train);
    
    if (!m_isLeaf) {
      m_isLeaf = true;
      

      eval.evaluateModel(this, m_train, new Object[0]);
      m_numIncorrectModel = eval.incorrect();
      
      m_isLeaf = false;
      
      for (int i = 0; i < m_Successors.length; i++) {
        m_Successors[i].modelErrors();
      }
    } else {
      eval.evaluateModel(this, m_train, new Object[0]);
      m_numIncorrectModel = eval.incorrect();
    }
  }
  




  public void treeErrors()
    throws Exception
  {
    if (m_isLeaf) {
      m_numIncorrectTree = m_numIncorrectModel;
    } else {
      m_numIncorrectTree = 0.0D;
      for (int i = 0; i < m_Successors.length; i++) {
        m_Successors[i].treeErrors();
        m_numIncorrectTree += m_Successors[i].m_numIncorrectTree;
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

        makeLeaf(m_train);
        m_Alpha = Double.MAX_VALUE;
      }
      else {
        errorDiff /= m_totalTrainInstances;
        m_Alpha = (errorDiff / (numLeaves() - 1));
        long alphaLong = Math.round(m_Alpha * Math.pow(10.0D, 10.0D));
        m_Alpha = (alphaLong / Math.pow(10.0D, 10.0D));
        for (int i = 0; i < m_Successors.length; i++) {
          m_Successors[i].calculateAlphas();
        }
      }
    }
    else {
      m_Alpha = Double.MAX_VALUE;
    }
  }
  






  protected SimpleCart nodeToPrune(Vector nodeList)
  {
    if (nodeList.size() == 0) return null;
    if (nodeList.size() == 1) return (SimpleCart)nodeList.elementAt(0);
    SimpleCart returnNode = (SimpleCart)nodeList.elementAt(0);
    double baseAlpha = m_Alpha;
    for (int i = 1; i < nodeList.size(); i++) {
      SimpleCart node = (SimpleCart)nodeList.elementAt(i);
      if (m_Alpha < baseAlpha) {
        baseAlpha = m_Alpha;
        returnNode = node;
      } else if ((m_Alpha == baseAlpha) && 
        (node.numLeaves() > returnNode.numLeaves())) {
        returnNode = node;
      }
    }
    
    return returnNode;
  }
  












  protected double computeSortedInfo(Instances data, int[][] sortedIndices, double[][] weights, double[] classProbs)
    throws Exception
  {
    double[] vals = new double[data.numInstances()];
    for (int j = 0; j < data.numAttributes(); j++) {
      if (j != data.classIndex()) {
        weights[j] = new double[data.numInstances()];
        
        if (data.attribute(j).isNominal())
        {


          sortedIndices[j] = new int[data.numInstances()];
          int count = 0;
          for (int i = 0; i < data.numInstances(); i++) {
            Instance inst = data.instance(i);
            if (!inst.isMissing(j)) {
              sortedIndices[j][count] = i;
              weights[j][count] = inst.weight();
              count++;
            }
          }
          for (int i = 0; i < data.numInstances(); i++) {
            Instance inst = data.instance(i);
            if (inst.isMissing(j)) {
              sortedIndices[j][count] = i;
              weights[j][count] = inst.weight();
              count++;
            }
            
          }
        }
        else
        {
          for (int i = 0; i < data.numInstances(); i++) {
            Instance inst = data.instance(i);
            vals[i] = inst.value(j);
          }
          sortedIndices[j] = Utils.sort(vals);
          for (int i = 0; i < data.numInstances(); i++) {
            weights[j][i] = data.instance(sortedIndices[j][i]).weight();
          }
        }
      }
    }
    
    double totalWeight = 0.0D;
    for (int i = 0; i < data.numInstances(); i++) {
      Instance inst = data.instance(i);
      classProbs[((int)inst.classValue())] += inst.weight();
      totalWeight += inst.weight();
    }
    
    return totalWeight;
  }
  







  protected double computeGiniGain(double[] parentDist, double[][] childDist)
  {
    double totalWeight = Utils.sum(parentDist);
    if (totalWeight == 0.0D) { return 0.0D;
    }
    double leftWeight = Utils.sum(childDist[0]);
    double rightWeight = Utils.sum(childDist[1]);
    
    double parentGini = computeGini(parentDist, totalWeight);
    double leftGini = computeGini(childDist[0], leftWeight);
    double rightGini = computeGini(childDist[1], rightWeight);
    
    return parentGini - leftWeight / totalWeight * leftGini - rightWeight / totalWeight * rightGini;
  }
  







  protected double computeGini(double[] dist, double total)
  {
    if (total == 0.0D) return 0.0D;
    double val = 0.0D;
    for (int i = 0; i < dist.length; i++) {
      val += dist[i] / total * (dist[i] / total);
    }
    return 1.0D - val;
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (!m_isLeaf)
    {
      if (instance.isMissing(m_Attribute)) {
        double[] returnedDist = new double[m_ClassProbs.length];
        
        for (int i = 0; i < m_Successors.length; i++) {
          double[] help = m_Successors[i].distributionForInstance(instance);
          
          if (help != null) {
            for (int j = 0; j < help.length; j++) {
              returnedDist[j] += m_Props[i] * help[j];
            }
          }
        }
        return returnedDist;
      }
      

      if (m_Attribute.isNominal()) {
        if (m_SplitString.indexOf("(" + m_Attribute.value((int)instance.value(m_Attribute)) + ")") != -1)
        {
          return m_Successors[0].distributionForInstance(instance); }
        return m_Successors[1].distributionForInstance(instance);
      }
      


      if (instance.value(m_Attribute) < m_SplitValue) {
        return m_Successors[0].distributionForInstance(instance);
      }
      return m_Successors[1].distributionForInstance(instance);
    }
    


    return m_ClassProbs;
  }
  




  protected void makeLeaf(Instances data)
  {
    m_Attribute = null;
    m_isLeaf = true;
    m_ClassValue = Utils.maxIndex(m_ClassProbs);
    m_ClassAttribute = data.classAttribute();
  }
  




  public String toString()
  {
    if ((m_ClassProbs == null) && (m_Successors == null)) {
      return "CART Tree: No model built yet.";
    }
    
    return "CART Decision Tree\n" + toString(0) + "\n\n" + "Number of Leaf Nodes: " + numLeaves() + "\n\n" + "Size of the Tree: " + numNodes();
  }
  








  protected String toString(int level)
  {
    StringBuffer text = new StringBuffer();
    
    if (m_Attribute == null) {
      if (Instance.isMissingValue(m_ClassValue)) {
        text.append(": null");
      } else {
        double correctNum = (int)(m_Distribution[Utils.maxIndex(m_Distribution)] * 100.0D) / 100.0D;
        
        double wrongNum = (int)((Utils.sum(m_Distribution) - m_Distribution[Utils.maxIndex(m_Distribution)]) * 100.0D) / 100.0D;
        
        String str = "(" + correctNum + "/" + wrongNum + ")";
        text.append(": " + m_ClassAttribute.value((int)m_ClassValue) + str);
      }
    } else {
      for (int j = 0; j < 2; j++) {
        text.append("\n");
        for (int i = 0; i < level; i++) {
          text.append("|  ");
        }
        if (j == 0) {
          if (m_Attribute.isNumeric()) {
            text.append(m_Attribute.name() + " < " + m_SplitValue);
          } else {
            text.append(m_Attribute.name() + "=" + m_SplitString);
          }
        } else if (m_Attribute.isNumeric()) {
          text.append(m_Attribute.name() + " >= " + m_SplitValue);
        } else {
          text.append(m_Attribute.name() + "!=" + m_SplitString);
        }
        text.append(m_Successors[j].toString(level + 1));
      }
    }
    return text.toString();
  }
  




  public int numNodes()
  {
    if (m_isLeaf) {
      return 1;
    }
    int size = 1;
    for (int i = 0; i < m_Successors.length; i++) {
      size += m_Successors[i].numNodes();
    }
    return size;
  }
  





  public int numInnerNodes()
  {
    if (m_Attribute == null) return 0;
    int numNodes = 1;
    for (int i = 0; i < m_Successors.length; i++)
      numNodes += m_Successors[i].numInnerNodes();
    return numNodes;
  }
  




  protected Vector getInnerNodes()
  {
    Vector nodeList = new Vector();
    fillInnerNodes(nodeList);
    return nodeList;
  }
  




  protected void fillInnerNodes(Vector nodeList)
  {
    if (!m_isLeaf) {
      nodeList.add(this);
      for (int i = 0; i < m_Successors.length; i++) {
        m_Successors[i].fillInnerNodes(nodeList);
      }
    }
  }
  



  public int numLeaves()
  {
    if (m_isLeaf) { return 1;
    }
    int size = 0;
    for (int i = 0; i < m_Successors.length; i++) {
      size += m_Successors[i].numLeaves();
    }
    return size;
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe minimal number of instances at the terminal nodes.\n\t(default 2)", "M", 1, "-M <min no>"));
    



    result.addElement(new Option("\tThe number of folds used in the minimal cost-complexity pruning.\n\t(default 5)", "N", 1, "-N <num folds>"));
    



    result.addElement(new Option("\tDon't use the minimal cost-complexity pruning.\n\t(default yes).", "U", 0, "-U"));
    



    result.addElement(new Option("\tDon't use the heuristic method for binary split.\n\t(default true).", "H", 0, "-H"));
    



    result.addElement(new Option("\tUse 1 SE rule to make pruning decision.\n\t(default no).", "A", 0, "-A"));
    



    result.addElement(new Option("\tPercentage of training data size (0-1].\n\t(default 1).", "C", 1, "-C"));
    



    return result.elements();
  }
  











































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      setMinNumObj(Double.parseDouble(tmpStr));
    } else {
      setMinNumObj(2.0D);
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNumFoldsPruning(Integer.parseInt(tmpStr));
    } else {
      setNumFoldsPruning(5);
    }
    setUsePrune(!Utils.getFlag('U', options));
    setHeuristic(!Utils.getFlag('H', options));
    setUseOneSE(Utils.getFlag('A', options));
    
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setSizePer(Double.parseDouble(tmpStr));
    } else {
      setSizePer(1.0D);
    }
    Utils.checkForRemainingOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-M");
    result.add("" + getMinNumObj());
    
    result.add("-N");
    result.add("" + getNumFoldsPruning());
    
    if (!getUsePrune()) {
      result.add("-U");
    }
    if (!getHeuristic()) {
      result.add("-H");
    }
    if (getUseOneSE()) {
      result.add("-A");
    }
    result.add("-C");
    result.add("" + getSizePer());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public Enumeration enumerateMeasures()
  {
    Vector result = new Vector();
    
    result.addElement("measureTreeSize");
    
    return result.elements();
  }
  




  public double measureTreeSize()
  {
    return numNodes();
  }
  






  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0) {
      return measureTreeSize();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (Cart pruning)");
  }
  







  public String minNumObjTipText()
  {
    return "The minimal number of observations at the terminal nodes (default 2).";
  }
  




  public void setMinNumObj(double value)
  {
    m_minNumObj = value;
  }
  




  public double getMinNumObj()
  {
    return m_minNumObj;
  }
  





  public String numFoldsPruningTipText()
  {
    return "The number of folds in the internal cross-validation (default 5).";
  }
  




  public void setNumFoldsPruning(int value)
  {
    m_numFoldsPruning = value;
  }
  




  public int getNumFoldsPruning()
  {
    return m_numFoldsPruning;
  }
  





  public String usePruneTipText()
  {
    return "Use minimal cost-complexity pruning (default yes).";
  }
  




  public void setUsePrune(boolean value)
  {
    m_Prune = value;
  }
  




  public boolean getUsePrune()
  {
    return m_Prune;
  }
  





  public String heuristicTipText()
  {
    return "If heuristic search is used for binary split for nominal attributes in multi-class problems (default yes).";
  }
  







  public void setHeuristic(boolean value)
  {
    m_Heuristic = value;
  }
  




  public boolean getHeuristic()
  {
    return m_Heuristic;
  }
  




  public String useOneSETipText()
  {
    return "Use the 1SE rule to make pruning decisoin.";
  }
  




  public void setUseOneSE(boolean value)
  {
    m_UseOneSE = value;
  }
  




  public boolean getUseOneSE()
  {
    return m_UseOneSE;
  }
  





  public String sizePerTipText()
  {
    return "The percentage of the training set size (0-1, 0 not included).";
  }
  




  public void setSizePer(double value)
  {
    if ((value <= 0.0D) || (value > 1.0D)) {
      System.err.println("The percentage of the training set size must be in range 0 to 1 (0 not included) - ignored!");
    }
    else
    {
      m_SizePer = value;
    }
  }
  



  public double getSizePer()
  {
    return m_SizePer;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10491 $");
  }
  



  public static void main(String[] args)
  {
    runClassifier(new SimpleCart(), args);
  }
}

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
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.matrix.EigenvalueDecomposition;
import weka.core.matrix.Matrix;



















































































































public class BFTree
  extends RandomizableClassifier
  implements AdditionalMeasureProducer, TechnicalInformationHandler
{
  private static final long serialVersionUID = -7035607375962528217L;
  public static final int PRUNING_UNPRUNED = 0;
  public static final int PRUNING_POSTPRUNING = 1;
  public static final int PRUNING_PREPRUNING = 2;
  public static final Tag[] TAGS_PRUNING = { new Tag(0, "unpruned", "Un-pruned"), new Tag(1, "postpruned", "Post-pruning"), new Tag(2, "prepruned", "Pre-pruning") };
  





  protected int m_PruningStrategy = 1;
  

  protected BFTree[] m_Successors;
  

  protected Attribute m_Attribute;
  

  protected double m_SplitValue;
  

  protected String m_SplitString;
  

  protected double m_ClassValue;
  

  protected Attribute m_ClassAttribute;
  

  protected int m_minNumObj = 2;
  

  protected int m_numFoldsPruning = 5;
  


  protected boolean m_isLeaf;
  

  protected static int m_Expansion;
  

  protected int m_FixedExpansion = -1;
  


  protected boolean m_Heuristic = true;
  

  protected boolean m_UseGini = true;
  


  protected boolean m_UseErrorRate = true;
  

  protected boolean m_UseOneSE = false;
  

  protected double[] m_Distribution;
  

  protected double[] m_Props;
  

  protected int[][] m_SortedIndices;
  

  protected double[][] m_Weights;
  

  protected double[][][] m_Dists;
  

  protected double[] m_ClassProbs;
  

  protected double m_TotalWeight;
  

  protected double m_SizePer = 1.0D;
  


  public BFTree() {}
  

  public String globalInfo()
  {
    return "Class for building a best-first decision tree classifier. This class uses binary split for both nominal and numeric attributes. For missing values, the method of 'fractional' instances is used.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MASTERSTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Haijian Shi");
    result.setValue(TechnicalInformation.Field.YEAR, "2007");
    result.setValue(TechnicalInformation.Field.TITLE, "Best-first decision tree learning");
    result.setValue(TechnicalInformation.Field.SCHOOL, "University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, NZ");
    result.setValue(TechnicalInformation.Field.NOTE, "COMP594");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Jerome Friedman and Trevor Hastie and Robert Tibshirani");
    additional.setValue(TechnicalInformation.Field.YEAR, "2000");
    additional.setValue(TechnicalInformation.Field.TITLE, "Additive logistic regression : A statistical view of boosting");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Annals of statistics");
    additional.setValue(TechnicalInformation.Field.VOLUME, "28");
    additional.setValue(TechnicalInformation.Field.NUMBER, "2");
    additional.setValue(TechnicalInformation.Field.PAGES, "337-407");
    additional.setValue(TechnicalInformation.Field.ISSN, "0090-5364");
    
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
    

    if (m_PruningStrategy == 0)
    {

      int[][] sortedIndices = new int[data.numAttributes()][0];
      double[][] weights = new double[data.numAttributes()][0];
      double[] classProbs = new double[data.numClasses()];
      double totalWeight = computeSortedInfo(data, sortedIndices, weights, classProbs);
      



      double[][][] dists = new double[data.numAttributes()][2][data.numClasses()];
      double[][] props = new double[data.numAttributes()][2];
      double[][] totalSubsetWeights = new double[data.numAttributes()][2];
      FastVector nodeInfo = computeSplitInfo(this, data, sortedIndices, weights, dists, props, totalSubsetWeights, m_Heuristic, m_UseGini);
      


      FastVector BestFirstElements = new FastVector();
      BestFirstElements.addElement(nodeInfo);
      

      int attIndex = ((Attribute)nodeInfo.elementAt(1)).index();
      m_Expansion = 0;
      makeTree(BestFirstElements, data, sortedIndices, weights, dists, classProbs, totalWeight, props[attIndex], m_minNumObj, m_Heuristic, m_UseGini, m_FixedExpansion);
      

      return;
    }
    





    int expansion = 0;
    
    Random random = new Random(m_Seed);
    Instances cvData = new Instances(data);
    cvData.randomize(random);
    cvData = new Instances(cvData, 0, (int)(cvData.numInstances() * m_SizePer) - 1);
    cvData.stratify(m_numFoldsPruning);
    
    Instances[] train = new Instances[m_numFoldsPruning];
    Instances[] test = new Instances[m_numFoldsPruning];
    FastVector[] parallelBFElements = new FastVector[m_numFoldsPruning];
    BFTree[] m_roots = new BFTree[m_numFoldsPruning];
    
    int[][][] sortedIndices = new int[m_numFoldsPruning][data.numAttributes()][0];
    double[][][] weights = new double[m_numFoldsPruning][data.numAttributes()][0];
    double[][] classProbs = new double[m_numFoldsPruning][data.numClasses()];
    double[] totalWeight = new double[m_numFoldsPruning];
    
    double[][][][] dists = new double[m_numFoldsPruning][data.numAttributes()][2][data.numClasses()];
    
    double[][][] props = new double[m_numFoldsPruning][data.numAttributes()][2];
    
    double[][][] totalSubsetWeights = new double[m_numFoldsPruning][data.numAttributes()][2];
    
    FastVector[] nodeInfo = new FastVector[m_numFoldsPruning];
    
    for (int i = 0; i < m_numFoldsPruning; i++) {
      train[i] = cvData.trainCV(m_numFoldsPruning, i);
      test[i] = cvData.testCV(m_numFoldsPruning, i);
      parallelBFElements[i] = new FastVector();
      m_roots[i] = new BFTree();
      

      totalWeight[i] = computeSortedInfo(train[i], sortedIndices[i], weights[i], classProbs[i]);
      



      nodeInfo[i] = computeSplitInfo(m_roots[i], train[i], sortedIndices[i], weights[i], dists[i], props[i], totalSubsetWeights[i], m_Heuristic, m_UseGini);
      



      int attIndex = ((Attribute)nodeInfo[i].elementAt(1)).index();
      
      m_SortedIndices = new int[sortedIndices[i].length][0];
      m_Weights = new double[weights[i].length][0];
      m_Dists = new double[dists[i].length][0][0];
      m_ClassProbs = new double[classProbs[i].length];
      m_Distribution = new double[classProbs[i].length];
      m_Props = new double[2];
      
      for (int j = 0; j < m_SortedIndices.length; j++) {
        m_SortedIndices[j] = sortedIndices[i][j];
        m_Weights[j] = weights[i][j];
        m_Dists[j] = dists[i][j];
      }
      
      System.arraycopy(classProbs[i], 0, m_ClassProbs, 0, classProbs[i].length);
      
      if (Utils.sum(m_ClassProbs) != 0.0D) {
        Utils.normalize(m_ClassProbs);
      }
      System.arraycopy(classProbs[i], 0, m_Distribution, 0, classProbs[i].length);
      
      System.arraycopy(props[i][attIndex], 0, m_Props, 0, props[i][attIndex].length);
      

      m_TotalWeight = totalWeight[i];
      
      parallelBFElements[i].addElement(nodeInfo[i]);
    }
    

    if (m_PruningStrategy == 2)
    {
      double previousError = Double.MAX_VALUE;
      double currentError = previousError;
      double minError = Double.MAX_VALUE;
      int minExpansion = 0;
      FastVector errorList = new FastVector();
      for (;;)
      {
        double expansionError = 0.0D;
        int count = 0;
        
        for (int i = 0; i < m_numFoldsPruning; i++)
        {


          if (expansion == 0) {
            m_isLeaf = true;
            Evaluation eval = new Evaluation(test[i]);
            eval.evaluateModel(m_roots[i], test[i], new Object[0]);
            if (m_UseErrorRate) expansionError += eval.errorRate(); else
              expansionError += eval.rootMeanSquaredError();
            count++;



          }
          else if (m_roots[i] != null) {
            m_isLeaf = false;
            BFTree nodeToSplit = (BFTree)((FastVector)parallelBFElements[i].elementAt(0)).elementAt(0);
            
            if (!m_roots[i].makeTree(parallelBFElements[i], m_roots[i], train[i], m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, m_minNumObj, m_Heuristic, m_UseGini))
            {



              m_roots[i] = null;
            }
            else {
              Evaluation eval = new Evaluation(test[i]);
              eval.evaluateModel(m_roots[i], test[i], new Object[0]);
              if (m_UseErrorRate) expansionError += eval.errorRate(); else
                expansionError += eval.rootMeanSquaredError();
              count++;
            }
          }
        }
        
        if (count == 0)
          break;
        expansionError /= count;
        errorList.addElement(new Double(expansionError));
        currentError = expansionError;
        
        if (!m_UseOneSE) {
          if (currentError > previousError) {
            break;
          }
        }
        else {
          if (expansionError < minError) {
            minError = expansionError;
            minExpansion = expansion;
          }
          
          if (currentError > previousError) {
            double oneSE = Math.sqrt(minError * (1.0D - minError) / data.numInstances());
            
            if (currentError > minError + oneSE) {
              break;
            }
          }
        }
        
        expansion++;
        previousError = currentError;
      }
      
      if (!m_UseOneSE) { expansion -= 1;
      } else {
        double oneSE = Math.sqrt(minError * (1.0D - minError) / data.numInstances());
        for (int i = 0; i < errorList.size(); i++) {
          double error = ((Double)errorList.elementAt(i)).doubleValue();
          if (error <= minError + oneSE) {
            expansion = i;
            break;
          }
          
        }
      }
    }
    else
    {
      FastVector[] modelError = new FastVector[m_numFoldsPruning];
      

      for (int i = 0; i < m_numFoldsPruning; i++) {
        modelError[i] = new FastVector();
        
        m_isLeaf = true;
        Evaluation eval = new Evaluation(test[i]);
        eval.evaluateModel(m_roots[i], test[i], new Object[0]);
        double error;
        double error; if (m_UseErrorRate) error = eval.errorRate(); else
          error = eval.rootMeanSquaredError();
        modelError[i].addElement(new Double(error));
        
        m_isLeaf = false;
        BFTree nodeToSplit = (BFTree)((FastVector)parallelBFElements[i].elementAt(0)).elementAt(0);
        

        m_roots[i].makeTree(parallelBFElements[i], m_roots[i], train[i], test[i], modelError[i], m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, m_minNumObj, m_Heuristic, m_UseGini, m_UseErrorRate);
        



        m_roots[i] = null;
      }
      

      double minError = Double.MAX_VALUE;
      
      int maxExpansion = modelError[0].size();
      for (int i = 1; i < modelError.length; i++) {
        if (modelError[i].size() > maxExpansion) {
          maxExpansion = modelError[i].size();
        }
      }
      double[] error = new double[maxExpansion];
      int[] counts = new int[maxExpansion];
      for (int i = 0; i < maxExpansion; i++) {
        counts[i] = 0;
        error[i] = 0.0D;
        for (int j = 0; j < m_numFoldsPruning; j++) {
          if (i < modelError[j].size()) {
            error[i] += ((Double)modelError[j].elementAt(i)).doubleValue();
            counts[i] += 1;
          }
        }
        error[i] /= counts[i];
        
        if (error[i] < minError) {
          minError = error[i];
          expansion = i;
        }
      }
      

      if (m_UseOneSE) {
        double oneSE = Math.sqrt(minError * (1.0D - minError) / data.numInstances());
        
        for (int i = 0; i < maxExpansion; i++) {
          if (error[i] <= minError + oneSE) {
            expansion = i;
            break;
          }
        }
      }
    }
    




    int[][] prune_sortedIndices = new int[data.numAttributes()][0];
    double[][] prune_weights = new double[data.numAttributes()][0];
    double[] prune_classProbs = new double[data.numClasses()];
    double prune_totalWeight = computeSortedInfo(data, prune_sortedIndices, prune_weights, prune_classProbs);
    



    double[][][] prune_dists = new double[data.numAttributes()][2][data.numClasses()];
    double[][] prune_props = new double[data.numAttributes()][2];
    double[][] prune_totalSubsetWeights = new double[data.numAttributes()][2];
    FastVector prune_nodeInfo = computeSplitInfo(this, data, prune_sortedIndices, prune_weights, prune_dists, prune_props, prune_totalSubsetWeights, m_Heuristic, m_UseGini);
    


    FastVector BestFirstElements = new FastVector();
    BestFirstElements.addElement(prune_nodeInfo);
    
    int attIndex = ((Attribute)prune_nodeInfo.elementAt(1)).index();
    m_Expansion = 0;
    makeTree(BestFirstElements, data, prune_sortedIndices, prune_weights, prune_dists, prune_classProbs, prune_totalWeight, prune_props[attIndex], m_minNumObj, m_Heuristic, m_UseGini, expansion);
  }
  





























  protected void makeTree(FastVector BestFirstElements, Instances data, int[][] sortedIndices, double[][] weights, double[][][] dists, double[] classProbs, double totalWeight, double[] branchProps, int minNumObj, boolean useHeuristic, boolean useGini, int preExpansion)
    throws Exception
  {
    if (BestFirstElements.size() == 0) { return;
    }
    


    FastVector firstElement = (FastVector)BestFirstElements.elementAt(0);
    

    Attribute att = (Attribute)firstElement.elementAt(1);
    

    double splitValue = NaN.0D;
    String splitStr = null;
    if (att.isNumeric()) {
      splitValue = ((Double)firstElement.elementAt(2)).doubleValue();
    } else {
      splitStr = ((String)firstElement.elementAt(2)).toString();
    }
    

    double gain = ((Double)firstElement.elementAt(3)).doubleValue();
    

    if (m_ClassProbs == null) {
      m_SortedIndices = new int[sortedIndices.length][0];
      m_Weights = new double[weights.length][0];
      m_Dists = new double[dists.length][0][0];
      m_ClassProbs = new double[classProbs.length];
      m_Distribution = new double[classProbs.length];
      m_Props = new double[2];
      
      for (int i = 0; i < m_SortedIndices.length; i++) {
        m_SortedIndices[i] = sortedIndices[i];
        m_Weights[i] = weights[i];
        m_Dists[i] = dists[i];
      }
      
      System.arraycopy(classProbs, 0, m_ClassProbs, 0, classProbs.length);
      System.arraycopy(classProbs, 0, m_Distribution, 0, classProbs.length);
      System.arraycopy(branchProps, 0, m_Props, 0, m_Props.length);
      m_TotalWeight = totalWeight;
      if (Utils.sum(m_ClassProbs) != 0.0D) { Utils.normalize(m_ClassProbs);
      }
    }
    
    if ((totalWeight < 2 * minNumObj) || (branchProps[0] == 0.0D) || (branchProps[1] == 0.0D))
    {

      BestFirstElements.removeElementAt(0);
      
      makeLeaf(data);
      if (BestFirstElements.size() != 0) {
        FastVector nextSplitElement = (FastVector)BestFirstElements.elementAt(0);
        BFTree nextSplitNode = (BFTree)nextSplitElement.elementAt(0);
        nextSplitNode.makeTree(BestFirstElements, data, m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, minNumObj, useHeuristic, useGini, preExpansion);
      }
      



      return;
    }
    



    if ((gain == 0.0D) || (preExpansion == m_Expansion)) {
      for (int i = 0; i < BestFirstElements.size(); i++) {
        FastVector element = (FastVector)BestFirstElements.elementAt(i);
        BFTree node = (BFTree)element.elementAt(0);
        node.makeLeaf(data);
      }
      BestFirstElements.removeAllElements();

    }
    else
    {

      BestFirstElements.removeElementAt(0);
      
      m_Attribute = att;
      if (m_Attribute.isNumeric()) m_SplitValue = splitValue; else {
        m_SplitString = splitStr;
      }
      int[][][] subsetIndices = new int[2][data.numAttributes()][0];
      double[][][] subsetWeights = new double[2][data.numAttributes()][0];
      
      splitData(subsetIndices, subsetWeights, m_Attribute, m_SplitValue, m_SplitString, sortedIndices, weights, data);
      



      int attIndex = att.index();
      if ((subsetIndices[0][attIndex].length < minNumObj) || (subsetIndices[1][attIndex].length < minNumObj))
      {
        makeLeaf(data);

      }
      else
      {
        m_isLeaf = false;
        m_Attribute = att;
        

        if ((m_PruningStrategy == 2) || (m_PruningStrategy == 1) || (preExpansion != -1))
        {

          m_Expansion += 1;
        }
        makeSuccessors(BestFirstElements, data, subsetIndices, subsetWeights, dists, att, useHeuristic, useGini);
      }
      


      if (BestFirstElements.size() != 0) {
        FastVector nextSplitElement = (FastVector)BestFirstElements.elementAt(0);
        BFTree nextSplitNode = (BFTree)nextSplitElement.elementAt(0);
        nextSplitNode.makeTree(BestFirstElements, data, m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, minNumObj, useHeuristic, useGini, preExpansion);
      }
    }
  }
  


































  protected boolean makeTree(FastVector BestFirstElements, BFTree root, Instances train, int[][] sortedIndices, double[][] weights, double[][][] dists, double[] classProbs, double totalWeight, double[] branchProps, int minNumObj, boolean useHeuristic, boolean useGini)
    throws Exception
  {
    if (BestFirstElements.size() == 0) { return false;
    }
    


    FastVector firstElement = (FastVector)BestFirstElements.elementAt(0);
    

    BFTree nodeToSplit = (BFTree)firstElement.elementAt(0);
    

    Attribute att = (Attribute)firstElement.elementAt(1);
    

    double splitValue = NaN.0D;
    String splitStr = null;
    if (att.isNumeric()) {
      splitValue = ((Double)firstElement.elementAt(2)).doubleValue();
    } else {
      splitStr = ((String)firstElement.elementAt(2)).toString();
    }
    

    double gain = ((Double)firstElement.elementAt(3)).doubleValue();
    


    if ((totalWeight < 2 * minNumObj) || (branchProps[0] == 0.0D) || (branchProps[1] == 0.0D))
    {

      BestFirstElements.removeElementAt(0);
      nodeToSplit.makeLeaf(train);
      if (BestFirstElements.size() == 0) {
        return false;
      }
      BFTree nextNode = (BFTree)((FastVector)BestFirstElements.elementAt(0)).elementAt(0);
      
      return root.makeTree(BestFirstElements, root, train, m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, minNumObj, useHeuristic, useGini);
    }
    






    if (gain == 0.0D) {
      for (int i = 0; i < BestFirstElements.size(); i++) {
        FastVector element = (FastVector)BestFirstElements.elementAt(i);
        BFTree node = (BFTree)element.elementAt(0);
        node.makeLeaf(train);
      }
      BestFirstElements.removeAllElements();
      return false;
    }
    


    BestFirstElements.removeElementAt(0);
    m_Attribute = att;
    if (att.isNumeric()) m_SplitValue = splitValue; else {
      m_SplitString = splitStr;
    }
    int[][][] subsetIndices = new int[2][train.numAttributes()][0];
    double[][][] subsetWeights = new double[2][train.numAttributes()][0];
    
    splitData(subsetIndices, subsetWeights, m_Attribute, m_SplitValue, m_SplitString, m_SortedIndices, m_Weights, train);
    




    int attIndex = att.index();
    if ((subsetIndices[0][attIndex].length < minNumObj) || (subsetIndices[1][attIndex].length < minNumObj))
    {

      nodeToSplit.makeLeaf(train);
      BFTree nextNode = (BFTree)((FastVector)BestFirstElements.elementAt(0)).elementAt(0);
      
      return root.makeTree(BestFirstElements, root, train, m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, minNumObj, useHeuristic, useGini);
    }
    





    m_isLeaf = false;
    m_Attribute = att;
    
    nodeToSplit.makeSuccessors(BestFirstElements, train, subsetIndices, subsetWeights, dists, m_Attribute, useHeuristic, useGini);
    

    for (int i = 0; i < 2; i++) {
      m_Successors[i].makeLeaf(train);
    }
    
    return true;
  }
  































  protected void makeTree(FastVector BestFirstElements, BFTree root, Instances train, Instances test, FastVector modelError, int[][] sortedIndices, double[][] weights, double[][][] dists, double[] classProbs, double totalWeight, double[] branchProps, int minNumObj, boolean useHeuristic, boolean useGini, boolean useErrorRate)
    throws Exception
  {
    if (BestFirstElements.size() == 0) { return;
    }
    


    FastVector firstElement = (FastVector)BestFirstElements.elementAt(0);
    




    Attribute att = (Attribute)firstElement.elementAt(1);
    

    double splitValue = NaN.0D;
    String splitStr = null;
    if (att.isNumeric()) {
      splitValue = ((Double)firstElement.elementAt(2)).doubleValue();
    } else {
      splitStr = ((String)firstElement.elementAt(2)).toString();
    }
    

    double gain = ((Double)firstElement.elementAt(3)).doubleValue();
    

    if ((totalWeight < 2 * minNumObj) || (branchProps[0] == 0.0D) || (branchProps[1] == 0.0D))
    {

      BestFirstElements.removeElementAt(0);
      makeLeaf(train);
      if (BestFirstElements.size() == 0) {
        return;
      }
      
      BFTree nextSplitNode = (BFTree)((FastVector)BestFirstElements.elementAt(0)).elementAt(0);
      
      nextSplitNode.makeTree(BestFirstElements, root, train, test, modelError, m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, minNumObj, useHeuristic, useGini, useErrorRate);
      



      return;
    }
    




    if (gain == 0.0D) {
      for (int i = 0; i < BestFirstElements.size(); i++) {
        FastVector element = (FastVector)BestFirstElements.elementAt(i);
        BFTree node = (BFTree)element.elementAt(0);
        node.makeLeaf(train);
      }
      BestFirstElements.removeAllElements();

    }
    else
    {

      BestFirstElements.removeElementAt(0);
      m_Attribute = att;
      if (att.isNumeric()) m_SplitValue = splitValue; else {
        m_SplitString = splitStr;
      }
      int[][][] subsetIndices = new int[2][train.numAttributes()][0];
      double[][][] subsetWeights = new double[2][train.numAttributes()][0];
      
      splitData(subsetIndices, subsetWeights, m_Attribute, m_SplitValue, m_SplitString, sortedIndices, weights, train);
      




      int attIndex = att.index();
      if ((subsetIndices[0][attIndex].length < minNumObj) || (subsetIndices[1][attIndex].length < minNumObj))
      {
        makeLeaf(train);

      }
      else
      {
        m_isLeaf = false;
        m_Attribute = att;
        
        makeSuccessors(BestFirstElements, train, subsetIndices, subsetWeights, dists, m_Attribute, useHeuristic, useGini);
        
        for (int i = 0; i < 2; i++) {
          m_Successors[i].makeLeaf(train);
        }
        
        Evaluation eval = new Evaluation(test);
        eval.evaluateModel(root, test, new Object[0]);
        double error;
        double error; if (useErrorRate) error = eval.errorRate(); else
          error = eval.rootMeanSquaredError();
        modelError.addElement(new Double(error));
      }
      
      if (BestFirstElements.size() != 0) {
        FastVector nextSplitElement = (FastVector)BestFirstElements.elementAt(0);
        BFTree nextSplitNode = (BFTree)nextSplitElement.elementAt(0);
        nextSplitNode.makeTree(BestFirstElements, root, train, test, modelError, m_SortedIndices, m_Weights, m_Dists, m_ClassProbs, m_TotalWeight, m_Props, minNumObj, useHeuristic, useGini, useErrorRate);
      }
    }
  }
  





















  protected void makeSuccessors(FastVector BestFirstElements, Instances data, int[][][] subsetSortedIndices, double[][][] subsetWeights, double[][][] dists, Attribute att, boolean useHeuristic, boolean useGini)
    throws Exception
  {
    m_Successors = new BFTree[2];
    
    for (int i = 0; i < 2; i++) {
      m_Successors[i] = new BFTree();
      m_Successors[i].m_isLeaf = true;
      

      m_Successors[i].m_ClassProbs = new double[data.numClasses()];
      m_Successors[i].m_Distribution = new double[data.numClasses()];
      System.arraycopy(dists[att.index()][i], 0, m_Successors[i].m_ClassProbs, 0, m_Successors[i].m_ClassProbs.length);
      
      System.arraycopy(dists[att.index()][i], 0, m_Successors[i].m_Distribution, 0, m_Successors[i].m_Distribution.length);
      
      if (Utils.sum(m_Successors[i].m_ClassProbs) != 0.0D) {
        Utils.normalize(m_Successors[i].m_ClassProbs);
      }
      
      double[][] props = new double[data.numAttributes()][2];
      double[][][] subDists = new double[data.numAttributes()][2][data.numClasses()];
      double[][] totalSubsetWeights = new double[data.numAttributes()][2];
      FastVector splitInfo = m_Successors[i].computeSplitInfo(m_Successors[i], data, subsetSortedIndices[i], subsetWeights[i], subDists, props, totalSubsetWeights, useHeuristic, useGini);
      



      int splitIndex = ((Attribute)splitInfo.elementAt(1)).index();
      m_Successors[i].m_Props = new double[2];
      System.arraycopy(props[splitIndex], 0, m_Successors[i].m_Props, 0, m_Successors[i].m_Props.length);
      


      m_Successors[i].m_SortedIndices = new int[data.numAttributes()][0];
      m_Successors[i].m_Weights = new double[data.numAttributes()][0];
      for (int j = 0; j < m_Successors[i].m_SortedIndices.length; j++) {
        m_Successors[i].m_SortedIndices[j] = subsetSortedIndices[i][j];
        m_Successors[i].m_Weights[j] = subsetWeights[i][j];
      }
      

      m_Successors[i].m_Dists = new double[data.numAttributes()][2][data.numClasses()];
      for (int j = 0; j < subDists.length; j++) {
        m_Successors[i].m_Dists[j] = subDists[j];
      }
      

      m_Successors[i].m_TotalWeight = Utils.sum(totalSubsetWeights[splitIndex]);
      


      if (BestFirstElements.size() == 0) {
        BestFirstElements.addElement(splitInfo);
      } else {
        double gGain = ((Double)splitInfo.elementAt(3)).doubleValue();
        int vectorSize = BestFirstElements.size();
        FastVector lastNode = (FastVector)BestFirstElements.elementAt(vectorSize - 1);
        

        if (gGain < ((Double)lastNode.elementAt(3)).doubleValue()) {
          BestFirstElements.insertElementAt(splitInfo, vectorSize);
        } else {
          for (int j = 0; j < vectorSize; j++) {
            FastVector node = (FastVector)BestFirstElements.elementAt(j);
            double nodeGain = ((Double)node.elementAt(3)).doubleValue();
            if (gGain >= nodeGain) {
              BestFirstElements.insertElementAt(splitInfo, j);
              break;
            }
          }
        }
      }
    }
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
  


















  protected FastVector computeSplitInfo(BFTree node, Instances data, int[][] sortedIndices, double[][] weights, double[][][] dists, double[][] props, double[][] totalSubsetWeights, boolean useHeuristic, boolean useGini)
    throws Exception
  {
    double[] splits = new double[data.numAttributes()];
    String[] splitString = new String[data.numAttributes()];
    double[] gains = new double[data.numAttributes()];
    
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i != data.classIndex()) {
        Attribute att = data.attribute(i);
        if (att.isNumeric())
        {
          splits[i] = numericDistribution(props, dists, att, sortedIndices[i], weights[i], totalSubsetWeights, gains, data, useGini);
        }
        else
        {
          splitString[i] = nominalDistribution(props, dists, att, sortedIndices[i], weights[i], totalSubsetWeights, gains, data, useHeuristic, useGini);
        }
      }
    }
    
    int index = Utils.maxIndex(gains);
    double mBestGain = gains[index];
    
    Attribute att = data.attribute(index);
    double mValue = NaN.0D;
    String mString = null;
    if (att.isNumeric()) { mValue = splits[index];
    } else {
      mString = splitString[index];
      if (mString == null) { mString = "";
      }
    }
    
    FastVector splitInfo = new FastVector();
    splitInfo.addElement(node);
    splitInfo.addElement(att);
    if (att.isNumeric()) splitInfo.addElement(new Double(mValue)); else
      splitInfo.addElement(mString);
    splitInfo.addElement(new Double(mBestGain));
    
    return splitInfo;
  }
  


















  protected double numericDistribution(double[][] props, double[][][] dists, Attribute att, int[] sortedIndices, double[] weights, double[][] subsetWeights, double[] gains, Instances data, boolean useGini)
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
    
    double bestGain = -1.7976931348623157E308D;
    
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
          index++; }
        double currGain;
        double currGain;
        if (useGini) currGain = computeGiniGain(parentDist, tempDist); else {
          currGain = computeInfoGain(parentDist, tempDist);
        }
        if (currGain > bestGain) {
          bestGain = currGain;
          
          splitPoint = Math.rint((inst.value(att) + currSplit) / 2.0D * 100000.0D) / 100000.0D;
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
    

    gains[attIndex] = (Math.rint(bestGain * 1.0E7D) / 1.0E7D);
    dists[attIndex] = dist;
    return splitPoint;
  }
  



















  protected String nominalDistribution(double[][] props, double[][][] dists, Attribute att, int[] sortedIndices, double[] weights, double[][] subsetWeights, double[] gains, Instances data, boolean useHeuristic, boolean useGini)
    throws Exception
  {
    String[] values = new String[att.numValues()];
    int numCat = values.length;
    int numClasses = data.numClasses();
    
    String bestSplitString = "";
    double bestGain = -1.7976931348623157E308D;
    

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
      gains[att.index()] = 0.0D;
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
        if (tempStr == "") tempStr = "(" + sortedValues[j] + ")"; else {
          tempStr = tempStr + "|(" + sortedValues[j] + ")";
        }
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
        double currGain;
        double currGain;
        if (useGini) currGain = computeGiniGain(parentDist, tempDist); else {
          currGain = computeInfoGain(parentDist, tempDist);
        }
        if (currGain > bestGain) {
          bestGain = currGain;
          bestSplitString = tempStr;
          for (int jj = 0; jj < 2; jj++) {
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
        double currGain;
        double currGain;
        if (useGini) currGain = computeGiniGain(parentDist, tempDist); else {
          currGain = computeInfoGain(parentDist, tempDist);
        }
        if (currGain > bestGain) {
          bestGain = currGain;
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
        double currGain;
        double currGain;
        if (useGini) currGain = computeGiniGain(parentDist, tempDist); else {
          currGain = computeInfoGain(parentDist, tempDist);
        }
        if (currGain > bestGain) {
          bestGain = currGain;
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
    
    gains[attIndex] = (Math.rint(bestGain * 1.0E7D) / 1.0E7D);
    
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
  







  protected double computeInfoGain(double[] parentDist, double[][] childDist)
  {
    double totalWeight = Utils.sum(parentDist);
    if (totalWeight == 0.0D) { return 0.0D;
    }
    double leftWeight = Utils.sum(childDist[0]);
    double rightWeight = Utils.sum(childDist[1]);
    
    double parentInfo = computeEntropy(parentDist, totalWeight);
    double leftInfo = computeEntropy(childDist[0], leftWeight);
    double rightInfo = computeEntropy(childDist[1], rightWeight);
    
    return parentInfo - leftWeight / totalWeight * leftInfo - rightWeight / totalWeight * rightInfo;
  }
  







  protected double computeEntropy(double[] dist, double total)
  {
    if (total == 0.0D) return 0.0D;
    double entropy = 0.0D;
    for (int i = 0; i < dist.length; i++) {
      if (dist[i] != 0.0D) entropy -= dist[i] / total * Utils.log2(dist[i] / total);
    }
    return entropy;
  }
  




  protected void makeLeaf(Instances data)
  {
    m_Attribute = null;
    m_isLeaf = true;
    m_ClassValue = Utils.maxIndex(m_ClassProbs);
    m_ClassAttribute = data.classAttribute();
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
  




  public String toString()
  {
    if ((m_Distribution == null) && (m_Successors == null)) {
      return "Best-First: No model built yet.";
    }
    return "Best-First Decision Tree\n" + toString(0) + "\n\n" + "Size of the Tree: " + numNodes() + "\n\n" + "Number of Leaf Nodes: " + numLeaves();
  }
  







  protected String toString(int level)
  {
    StringBuffer text = new StringBuffer();
    
    if (m_Attribute == null) {
      if (Instance.isMissingValue(m_ClassValue)) {
        text.append(": null");
      } else {
        double correctNum = Math.rint(m_Distribution[Utils.maxIndex(m_Distribution)] * 100.0D) / 100.0D;
        
        double wrongNum = Math.rint((Utils.sum(m_Distribution) - m_Distribution[Utils.maxIndex(m_Distribution)]) * 100.0D) / 100.0D;
        
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
    result.addElement(new Option("\tThe pruning strategy.\n\t(default: " + new SelectedTag(1, TAGS_PRUNING) + ")", "P", 1, "-P " + Tag.toOptionList(TAGS_PRUNING)));
    



    result.addElement(new Option("\tThe minimal number of instances at the terminal nodes.\n\t(default 2)", "M", 1, "-M <min no>"));
    



    result.addElement(new Option("\tThe number of folds used in the pruning.\n\t(default 5)", "N", 5, "-N <num folds>"));
    



    result.addElement(new Option("\tDon't use heuristic search for nominal attributes in multi-class\n\tproblem (default yes).\n", "H", 0, "-H"));
    



    result.addElement(new Option("\tDon't use Gini index for splitting (default yes),\n\tif not information is used.", "G", 0, "-G"));
    



    result.addElement(new Option("\tDon't use error rate in internal cross-validation (default yes), \n\tbut root mean squared error.", "R", 0, "-R"));
    



    result.addElement(new Option("\tUse the 1 SE rule to make pruning decision.\n\t(default no).", "A", 0, "-A"));
    



    result.addElement(new Option("\tPercentage of training data size (0-1]\n\t(default 1).", "C", 0, "-C"));
    



    return result.elements();
  }
  




















































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      setMinNumObj(Integer.parseInt(tmpStr));
    } else {
      setMinNumObj(2);
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNumFoldsPruning(Integer.parseInt(tmpStr));
    } else {
      setNumFoldsPruning(5);
    }
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setSizePer(Double.parseDouble(tmpStr));
    } else {
      setSizePer(1.0D);
    }
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setPruningStrategy(new SelectedTag(tmpStr, TAGS_PRUNING));
    } else {
      setPruningStrategy(new SelectedTag(1, TAGS_PRUNING));
    }
    setHeuristic(!Utils.getFlag('H', options));
    
    setUseGini(!Utils.getFlag('G', options));
    
    setUseErrorRate(!Utils.getFlag('R', options));
    
    setUseOneSE(Utils.getFlag('A', options));
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
    
    if (!getHeuristic()) {
      result.add("-H");
    }
    if (!getUseGini()) {
      result.add("-G");
    }
    if (!getUseErrorRate()) {
      result.add("-R");
    }
    if (getUseOneSE()) {
      result.add("-A");
    }
    result.add("-C");
    result.add("" + getSizePer());
    
    result.add("-P");
    result.add("" + getPruningStrategy());
    
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
    throw new IllegalArgumentException(additionalMeasureName + " not supported (Best-First)");
  }
  







  public String pruningStrategyTipText()
  {
    return "Sets the pruning strategy.";
  }
  




  public void setPruningStrategy(SelectedTag value)
  {
    if (value.getTags() == TAGS_PRUNING) {
      m_PruningStrategy = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getPruningStrategy()
  {
    return new SelectedTag(m_PruningStrategy, TAGS_PRUNING);
  }
  





  public String minNumObjTipText()
  {
    return "Set minimal number of instances at the terminal nodes.";
  }
  




  public void setMinNumObj(int value)
  {
    m_minNumObj = value;
  }
  




  public int getMinNumObj()
  {
    return m_minNumObj;
  }
  





  public String numFoldsPruningTipText()
  {
    return "Number of folds in internal cross-validation.";
  }
  




  public void setNumFoldsPruning(int value)
  {
    m_numFoldsPruning = value;
  }
  




  public int getNumFoldsPruning()
  {
    return m_numFoldsPruning;
  }
  





  public String heuristicTipText()
  {
    return "If heuristic search is used for binary split for nominal attributes.";
  }
  





  public void setHeuristic(boolean value)
  {
    m_Heuristic = value;
  }
  





  public boolean getHeuristic()
  {
    return m_Heuristic;
  }
  





  public String useGiniTipText()
  {
    return "If true the Gini index is used for splitting criterion, otherwise the information is used.";
  }
  




  public void setUseGini(boolean value)
  {
    m_UseGini = value;
  }
  




  public boolean getUseGini()
  {
    return m_UseGini;
  }
  





  public String useErrorRateTipText()
  {
    return "If error rate is used as error estimate. if not, root mean squared error is used.";
  }
  




  public void setUseErrorRate(boolean value)
  {
    m_UseErrorRate = value;
  }
  




  public boolean getUseErrorRate()
  {
    return m_UseErrorRate;
  }
  





  public String useOneSETipText()
  {
    return "Use the 1SE rule to make pruning decision.";
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
    return RevisionUtils.extract("$Revision: 6947 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new BFTree(), args);
  }
}

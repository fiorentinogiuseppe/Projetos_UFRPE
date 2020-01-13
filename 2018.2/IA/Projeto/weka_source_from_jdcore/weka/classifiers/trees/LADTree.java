package weka.classifiers.trees;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.trees.adtree.ReferenceInstances;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;









































public class LADTree
  extends Classifier
  implements Drawable, AdditionalMeasureProducer, TechnicalInformationHandler
{
  private static final long serialVersionUID = -4940716114518300302L;
  protected double Z_MAX;
  protected int m_numOfClasses;
  protected ReferenceInstances m_trainInstances;
  protected PredictionNode m_root;
  protected int m_lastAddedSplitNum;
  protected int[] m_numericAttIndices;
  protected double m_search_smallestLeastSquares;
  protected PredictionNode m_search_bestInsertionNode;
  protected Splitter m_search_bestSplitter;
  protected Instances m_search_bestPathInstances;
  protected FastVector m_staticPotentialSplitters2way;
  protected int m_nodesExpanded;
  protected int m_examplesCounted;
  protected int m_boostingIterations;
  
  public LADTree()
  {
    Z_MAX = 4.0D;
    







    m_root = null;
    

    m_lastAddedSplitNum = 0;
    













    m_nodesExpanded = 0;
    m_examplesCounted = 0;
    

    m_boostingIterations = 10;
  }
  




  public String globalInfo()
  {
    return "Class for generating a multi-class alternating decision tree using the LogitBoost strategy. For more info, see\n\n" + getTechnicalInformation().toString();
  }
  










  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Geoffrey Holmes and Bernhard Pfahringer and Richard Kirkby and Eibe Frank and Mark Hall");
    result.setValue(TechnicalInformation.Field.TITLE, "Multiclass alternating decision trees");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "ECML");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.PAGES, "161-172");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  
  protected class LADInstance extends Instance
  {
    public double[] fVector;
    public double[] wVector;
    public double[] pVector;
    public double[] zVector;
    
    public LADInstance(Instance instance) {
      super();
      
      setDataset(instance.dataset());
      

      fVector = new double[m_numOfClasses];
      wVector = new double[m_numOfClasses];
      pVector = new double[m_numOfClasses];
      zVector = new double[m_numOfClasses];
      

      double initProb = 1.0D / m_numOfClasses;
      for (int i = 0; i < m_numOfClasses; i++) {
        pVector[i] = initProb;
      }
      updateZVector();
      updateWVector();
    }
    
    public void updateWeights(double[] fVectorIncrement) { for (int i = 0; i < fVector.length; i++) {
        fVector[i] += fVectorIncrement[i];
      }
      updateVectors(fVector);
    }
    
    public void updateVectors(double[] newFVector) { updatePVector(newFVector);
      updateZVector();
      updateWVector();
    }
    
    public void updatePVector(double[] newFVector) { double max = newFVector[Utils.maxIndex(newFVector)];
      for (int i = 0; i < pVector.length; i++) {
        pVector[i] = Math.exp(newFVector[i] - max);
      }
      Utils.normalize(pVector);
    }
    
    public void updateWVector() { for (int i = 0; i < wVector.length; i++) {
        wVector[i] = ((yVector(i) - pVector[i]) / zVector[i]);
      }
    }
    
    public void updateZVector() {
      for (int i = 0; i < zVector.length; i++) {
        if (yVector(i) == 1.0D) {
          zVector[i] = (1.0D / pVector[i]);
          if (zVector[i] > Z_MAX) {
            zVector[i] = Z_MAX;
          }
        } else {
          zVector[i] = (-1.0D / (1.0D - pVector[i]));
          if (zVector[i] < -Z_MAX) {
            zVector[i] = (-Z_MAX);
          }
        }
      }
    }
    
    public double yVector(int index) { return index == (int)classValue() ? 1.0D : 0.0D; }
    
    public Object copy() {
      LADInstance copy = new LADInstance(LADTree.this, (Instance)super.copy());
      System.arraycopy(fVector, 0, fVector, 0, fVector.length);
      System.arraycopy(wVector, 0, wVector, 0, wVector.length);
      System.arraycopy(pVector, 0, pVector, 0, pVector.length);
      System.arraycopy(zVector, 0, zVector, 0, zVector.length);
      return copy;
    }
    
    public String toString() {
      StringBuffer text = new StringBuffer();
      text.append(" * F(");
      for (int i = 0; i < fVector.length; i++) {
        text.append(Utils.doubleToString(fVector[i], 3));
        if (i < fVector.length - 1) text.append(",");
      }
      text.append(") P(");
      for (int i = 0; i < pVector.length; i++) {
        text.append(Utils.doubleToString(pVector[i], 3));
        if (i < pVector.length - 1) text.append(",");
      }
      text.append(") W(");
      for (int i = 0; i < wVector.length; i++) {
        text.append(Utils.doubleToString(wVector[i], 3));
        if (i < wVector.length - 1) text.append(",");
      }
      text.append(")");
      return super.toString() + text.toString();
    }
  }
  
  protected class PredictionNode implements Serializable, Cloneable
  {
    private double[] values;
    private FastVector children;
    
    public PredictionNode(double[] newValues) {
      values = new double[m_numOfClasses];
      setValues(newValues);
      children = new FastVector();
    }
    
    public void setValues(double[] newValues) { System.arraycopy(newValues, 0, values, 0, m_numOfClasses); }
    

    public double[] getValues() { return values; }
    
    public FastVector getChildren() { return children; }
    public Enumeration children() { return children.elements(); }
    
    public void addChild(LADTree.Splitter newChild) { LADTree.Splitter oldEqual = null;
      for (Enumeration e = children(); e.hasMoreElements();) {
        LADTree.Splitter split = (LADTree.Splitter)e.nextElement();
        if (newChild.equalTo(split)) { oldEqual = split; break;
        } }
      if (oldEqual == null) {
        LADTree.Splitter addChild = (LADTree.Splitter)newChild.clone();
        orderAdded = (++m_lastAddedSplitNum);
        children.addElement(addChild);
      }
      else {
        for (int i = 0; i < newChild.getNumOfBranches(); i++) {
          PredictionNode oldPred = oldEqual.getChildForBranch(i);
          PredictionNode newPred = newChild.getChildForBranch(i);
          if ((oldPred != null) && (newPred != null))
            oldPred.merge(newPred);
        }
      }
    }
    
    public Object clone() { PredictionNode clone = new PredictionNode(LADTree.this, values);
      
      for (Enumeration e = children.elements(); e.hasMoreElements();)
        children.addElement((LADTree.Splitter)((LADTree.Splitter)e.nextElement()).clone());
      return clone;
    }
    
    public void merge(PredictionNode merger) {
      for (int i = 0; i < m_numOfClasses; i++) values[i] += values[i];
      for (Enumeration e = merger.children(); e.hasMoreElements();)
        addChild((LADTree.Splitter)e.nextElement()); } }
  
  protected abstract class Splitter implements Serializable, Cloneable { protected int attIndex;
    public int orderAdded;
    
    protected Splitter() {}
    
    public abstract int getNumOfBranches();
    
    public abstract int branchInstanceGoesDown(Instance paramInstance);
    
    public abstract Instances instancesDownBranch(int paramInt, Instances paramInstances);
    
    public abstract String attributeString();
    
    public abstract String comparisonString(int paramInt);
    
    public abstract boolean equalTo(Splitter paramSplitter);
    
    public abstract void setChildForBranch(int paramInt, LADTree.PredictionNode paramPredictionNode);
    
    public abstract LADTree.PredictionNode getChildForBranch(int paramInt);
    
    public abstract Object clone(); }
  
  protected class TwoWayNominalSplit extends LADTree.Splitter { public TwoWayNominalSplit(int _attIndex, int _trueSplitValue) { super();
      attIndex = _attIndex;trueSplitValue = _trueSplitValue;
      children = new LADTree.PredictionNode[2]; }
    
    public int getNumOfBranches() { return 2; }
    
    public int branchInstanceGoesDown(Instance inst) { if (inst.isMissing(attIndex)) return -1;
      if (inst.value(attIndex) == trueSplitValue) return 0;
      return 1; }
    
    private int trueSplitValue;
    public Instances instancesDownBranch(int branch, Instances instances) { ReferenceInstances filteredInstances = new ReferenceInstances(instances, 1);
      Enumeration e; Enumeration e; if (branch == -1) {
        for (e = instances.enumerateInstances(); e.hasMoreElements();) {
          Instance inst = (Instance)e.nextElement();
          if (inst.isMissing(attIndex)) filteredInstances.addReference(inst);
        } } else { Enumeration e;
        if (branch == 0) {
          for (e = instances.enumerateInstances(); e.hasMoreElements();) {
            Instance inst = (Instance)e.nextElement();
            if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) == trueSplitValue))
              filteredInstances.addReference(inst);
          }
        } else
          for (e = instances.enumerateInstances(); e.hasMoreElements();) {
            Instance inst = (Instance)e.nextElement();
            if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) != trueSplitValue))
              filteredInstances.addReference(inst);
          }
      }
      return filteredInstances;
    }
    
    public String attributeString() { return m_trainInstances.attribute(attIndex).name(); }
    
    public String comparisonString(int branchNum) {
      Attribute att = m_trainInstances.attribute(attIndex);
      if (att.numValues() != 2)
        return (branchNum == 0 ? "= " : "!= ") + att.value(trueSplitValue);
      return "= " + (branchNum == 0 ? att.value(trueSplitValue) : att.value(trueSplitValue == 0 ? 1 : 0));
    }
    
    public boolean equalTo(LADTree.Splitter compare)
    {
      if ((compare instanceof TwoWayNominalSplit)) {
        TwoWayNominalSplit compareSame = (TwoWayNominalSplit)compare;
        return (attIndex == attIndex) && (trueSplitValue == trueSplitValue);
      }
      return false;
    }
    
    public void setChildForBranch(int branchNum, LADTree.PredictionNode childPredictor) { children[branchNum] = childPredictor; }
    
    private LADTree.PredictionNode[] children;
    public LADTree.PredictionNode getChildForBranch(int branchNum) { return children[branchNum]; }
    
    public Object clone() {
      TwoWayNominalSplit clone = new TwoWayNominalSplit(LADTree.this, attIndex, trueSplitValue);
      if (children[0] != null)
        clone.setChildForBranch(0, (LADTree.PredictionNode)children[0].clone());
      if (children[1] != null)
        clone.setChildForBranch(1, (LADTree.PredictionNode)children[1].clone());
      return clone;
    }
  }
  
  protected class TwoWayNumericSplit extends LADTree.Splitter implements Cloneable {
    private double splitPoint;
    private LADTree.PredictionNode[] children;
    
    public TwoWayNumericSplit(int _attIndex, double _splitPoint) { super();
      attIndex = _attIndex;
      splitPoint = _splitPoint;
      children = new LADTree.PredictionNode[2]; }
    
    public TwoWayNumericSplit(int _attIndex, Instances instances) throws Exception { super();
      attIndex = _attIndex;
      splitPoint = findSplit(instances, attIndex);
      children = new LADTree.PredictionNode[2]; }
    
    public int getNumOfBranches() { return 2; }
    
    public int branchInstanceGoesDown(Instance inst) { if (inst.isMissing(attIndex)) return -1;
      if (inst.value(attIndex) < splitPoint) return 0;
      return 1;
    }
    
    public Instances instancesDownBranch(int branch, Instances instances) { ReferenceInstances filteredInstances = new ReferenceInstances(instances, 1);
      Enumeration e; Enumeration e; if (branch == -1) {
        for (e = instances.enumerateInstances(); e.hasMoreElements();) {
          Instance inst = (Instance)e.nextElement();
          if (inst.isMissing(attIndex)) filteredInstances.addReference(inst);
        } } else { Enumeration e;
        if (branch == 0) {
          for (e = instances.enumerateInstances(); e.hasMoreElements();) {
            Instance inst = (Instance)e.nextElement();
            if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) < splitPoint))
              filteredInstances.addReference(inst);
          }
        } else
          for (e = instances.enumerateInstances(); e.hasMoreElements();) {
            Instance inst = (Instance)e.nextElement();
            if ((!inst.isMissing(attIndex)) && (inst.value(attIndex) >= splitPoint))
              filteredInstances.addReference(inst);
          }
      }
      return filteredInstances;
    }
    
    public String attributeString() { return m_trainInstances.attribute(attIndex).name(); }
    

    public String comparisonString(int branchNum) { return (branchNum == 0 ? "< " : ">= ") + Utils.doubleToString(splitPoint, 3); }
    
    public boolean equalTo(LADTree.Splitter compare) {
      if ((compare instanceof TwoWayNumericSplit)) {
        TwoWayNumericSplit compareSame = (TwoWayNumericSplit)compare;
        return (attIndex == attIndex) && (splitPoint == splitPoint);
      }
      return false;
    }
    
    public void setChildForBranch(int branchNum, LADTree.PredictionNode childPredictor) { children[branchNum] = childPredictor; }
    

    public LADTree.PredictionNode getChildForBranch(int branchNum) { return children[branchNum]; }
    
    public Object clone() {
      TwoWayNumericSplit clone = new TwoWayNumericSplit(LADTree.this, attIndex, splitPoint);
      if (children[0] != null)
        clone.setChildForBranch(0, (LADTree.PredictionNode)children[0].clone());
      if (children[1] != null)
        clone.setChildForBranch(1, (LADTree.PredictionNode)children[1].clone());
      return clone;
    }
    
    private double findSplit(Instances instances, int index) throws Exception { double splitPoint = 0.0D;
      double bestVal = Double.MAX_VALUE;
      int numMissing = 0;
      double[][] distribution = new double[3][instances.numClasses()];
      

      for (int i = 0; i < instances.numInstances(); i++) {
        Instance inst = instances.instance(i);
        if (!inst.isMissing(index)) {
          distribution[1][((int)inst.classValue())] += 1.0D;
        } else {
          distribution[2][((int)inst.classValue())] += 1.0D;
          numMissing++;
        }
      }
      

      instances.sort(index);
      

      for (int i = 0; i < instances.numInstances() - (numMissing + 1); i++) {
        Instance inst = instances.instance(i);
        Instance instPlusOne = instances.instance(i + 1);
        distribution[0][((int)inst.classValue())] += inst.weight();
        distribution[1][((int)inst.classValue())] -= inst.weight();
        if (Utils.sm(inst.value(index), instPlusOne.value(index))) {
          double currCutPoint = (inst.value(index) + instPlusOne.value(index)) / 2.0D;
          double currVal = ContingencyTables.entropyConditionedOnRows(distribution);
          if (Utils.sm(currVal, bestVal)) {
            splitPoint = currCutPoint;
            bestVal = currVal;
          }
        }
      }
      
      return splitPoint;
    }
  }
  






  public void initClassifier(Instances instances)
    throws Exception
  {
    m_nodesExpanded = 0;
    m_examplesCounted = 0;
    m_lastAddedSplitNum = 0;
    
    m_numOfClasses = instances.numClasses();
    

    if (instances.checkForStringAttributes()) {
      throw new Exception("Can't handle string attributes!");
    }
    if (!instances.classAttribute().isNominal()) {
      throw new Exception("Class must be nominal!");
    }
    

    m_trainInstances = new ReferenceInstances(instances, instances.numInstances());
    
    for (Enumeration e = instances.enumerateInstances(); e.hasMoreElements();) {
      Instance inst = (Instance)e.nextElement();
      if (!inst.classIsMissing()) {
        LADInstance adtInst = new LADInstance(inst);
        m_trainInstances.addReference(adtInst);
        adtInst.setDataset(m_trainInstances);
      }
    }
    

    m_root = new PredictionNode(new double[m_numOfClasses]);
    

    generateStaticPotentialSplittersAndNumericIndices();
  }
  
  public void next(int iteration) throws Exception {
    boost();
  }
  


  public void done()
    throws Exception
  {}
  


  private void boost()
    throws Exception
  {
    if (m_trainInstances == null) {
      throw new Exception("Trying to boost with no training data");
    }
    
    searchForBestTest();
    
    if (m_Debug) {
      System.out.println("Best split found: " + m_search_bestSplitter.getNumOfBranches() + "-way split on " + m_search_bestSplitter.attributeString() + "\nBestGain = " + m_search_smallestLeastSquares);
    }
    




    if (m_search_bestSplitter == null) { return;
    }
    
    for (int i = 0; i < m_search_bestSplitter.getNumOfBranches(); i++) {
      Instances applicableInstances = m_search_bestSplitter.instancesDownBranch(i, m_search_bestPathInstances);
      
      double[] predictionValues = calcPredictionValues(applicableInstances);
      PredictionNode newPredictor = new PredictionNode(predictionValues);
      updateWeights(applicableInstances, predictionValues);
      m_search_bestSplitter.setChildForBranch(i, newPredictor);
    }
    

    m_search_bestInsertionNode.addChild(m_search_bestSplitter);
    
    if (m_Debug) {
      System.out.println("Tree is now:\n" + toString(m_root, 1) + "\n");
    }
    


    m_search_bestPathInstances = null;
  }
  
  private void updateWeights(Instances instances, double[] newPredictionValues)
  {
    for (int i = 0; i < instances.numInstances(); i++) {
      ((LADInstance)instances.instance(i)).updateWeights(newPredictionValues);
    }
  }
  





  private void generateStaticPotentialSplittersAndNumericIndices()
  {
    m_staticPotentialSplitters2way = new FastVector();
    FastVector numericIndices = new FastVector();
    
    for (int i = 0; i < m_trainInstances.numAttributes(); i++) {
      if (i != m_trainInstances.classIndex()) {
        if (m_trainInstances.attribute(i).isNumeric()) {
          numericIndices.addElement(new Integer(i));
        } else {
          int numValues = m_trainInstances.attribute(i).numValues();
          if (numValues == 2)
            m_staticPotentialSplitters2way.addElement(new TwoWayNominalSplit(i, 0)); else
            for (int j = 0; j < numValues; j++)
              m_staticPotentialSplitters2way.addElement(new TwoWayNominalSplit(i, j));
        }
      }
    }
    m_numericAttIndices = new int[numericIndices.size()];
    for (int i = 0; i < numericIndices.size(); i++) {
      m_numericAttIndices[i] = ((Integer)numericIndices.elementAt(i)).intValue();
    }
  }
  




  private void searchForBestTest()
    throws Exception
  {
    if (m_Debug) {
      System.out.println("Searching for best split...");
    }
    
    m_search_smallestLeastSquares = 0.0D;
    searchForBestTest(m_root, m_trainInstances);
  }
  












  private void searchForBestTest(PredictionNode currentNode, Instances instances)
    throws Exception
  {
    m_nodesExpanded += 1;
    m_examplesCounted += instances.numInstances();
    

    Enumeration e = m_staticPotentialSplitters2way.elements();
    while (e.hasMoreElements()) {
      evaluateSplitter((Splitter)e.nextElement(), currentNode, instances);
    }
    
    if (m_Debug) {}
    



    for (int i = 0; i < m_numericAttIndices.length; i++) {
      evaluateNumericSplit(currentNode, instances, m_numericAttIndices[i]);
    }
    
    if (currentNode.getChildren().size() == 0) { return;
    }
    
    goDownAllPaths(currentNode, instances);
  }
  









  private void goDownAllPaths(PredictionNode currentNode, Instances instances)
    throws Exception
  {
    for (Enumeration e = currentNode.children(); e.hasMoreElements();) {
      Splitter split = (Splitter)e.nextElement();
      for (int i = 0; i < split.getNumOfBranches(); i++) {
        searchForBestTest(split.getChildForBranch(i), split.instancesDownBranch(i, instances));
      }
    }
  }
  












  private void evaluateSplitter(Splitter split, PredictionNode currentNode, Instances instances)
    throws Exception
  {
    double leastSquares = leastSquaresNonMissing(instances, attIndex);
    
    for (int i = 0; i < split.getNumOfBranches(); i++) {
      leastSquares -= leastSquares(split.instancesDownBranch(i, instances));
    }
    if (m_Debug)
    {
      System.out.print(split.getNumOfBranches() + "-way split on " + split.attributeString() + " has leastSquares value of " + Utils.doubleToString(leastSquares, 3));
    }
    


    if (leastSquares > m_search_smallestLeastSquares) {
      if (m_Debug) {
        System.out.print(" (best so far)");
      }
      m_search_smallestLeastSquares = leastSquares;
      m_search_bestInsertionNode = currentNode;
      m_search_bestSplitter = split;
      m_search_bestPathInstances = instances;
    }
    if (m_Debug) {
      System.out.print("\n");
    }
  }
  


  private void evaluateNumericSplit(PredictionNode currentNode, Instances instances, int attIndex)
  {
    double[] splitAndLS = findNumericSplitpointAndLS(instances, attIndex);
    double gain = leastSquaresNonMissing(instances, attIndex) - splitAndLS[1];
    
    if (m_Debug)
    {
      System.out.print("Numeric split on " + instances.attribute(attIndex).name() + " has leastSquares value of " + Utils.doubleToString(gain, 3));
    }
    



    if (gain > m_search_smallestLeastSquares) {
      if (m_Debug) {
        System.out.print(" (best so far)");
      }
      m_search_smallestLeastSquares = gain;
      m_search_bestInsertionNode = currentNode;
      m_search_bestSplitter = new TwoWayNumericSplit(attIndex, splitAndLS[0]);
      m_search_bestPathInstances = instances;
    }
    if (m_Debug) {
      System.out.print("\n");
    }
  }
  
  private double[] findNumericSplitpointAndLS(Instances instances, int attIndex)
  {
    double allLS = leastSquares(instances);
    

    double[] term1L = new double[m_numOfClasses];
    double[] term2L = new double[m_numOfClasses];
    double[] term3L = new double[m_numOfClasses];
    double[] meanNumL = new double[m_numOfClasses];
    double[] meanDenL = new double[m_numOfClasses];
    
    double[] term1R = new double[m_numOfClasses];
    double[] term2R = new double[m_numOfClasses];
    double[] term3R = new double[m_numOfClasses];
    double[] meanNumR = new double[m_numOfClasses];
    double[] meanDenR = new double[m_numOfClasses];
    


    double[] classMeans = new double[m_numOfClasses];
    double[] classTotals = new double[m_numOfClasses];
    

    for (int j = 0; j < m_numOfClasses; j++) {
      for (int i = 0; i < instances.numInstances(); i++) {
        LADInstance inst = (LADInstance)instances.instance(i);
        double temp1 = wVector[j] * zVector[j];
        term1R[j] += temp1 * zVector[j];
        term2R[j] += temp1;
        term3R[j] += wVector[j];
        meanNumR[j] += wVector[j] * zVector[j];
      }
    }
    




    double smallestLeastSquares = Double.POSITIVE_INFINITY;
    double bestSplit = 0.0D;
    

    instances.sort(attIndex);
    
    for (int i = 0; i < instances.numInstances() - 1; i++) {
      if (instances.instance(i + 1).isMissing(attIndex)) break;
      boolean newSplit; boolean newSplit; if (instances.instance(i + 1).value(attIndex) > instances.instance(i).value(attIndex))
        newSplit = true; else
        newSplit = false;
      LADInstance inst = (LADInstance)instances.instance(i);
      double leastSquares = 0.0D;
      for (int j = 0; j < m_numOfClasses; j++) {
        double temp1 = wVector[j] * zVector[j];
        double temp2 = temp1 * zVector[j];
        double temp3 = wVector[j] * zVector[j];
        term1L[j] += temp2;
        term2L[j] += temp1;
        term3L[j] += wVector[j];
        term1R[j] -= temp2;
        term2R[j] -= temp1;
        term3R[j] -= wVector[j];
        meanNumL[j] += temp3;
        meanNumR[j] -= temp3;
        if (newSplit) {
          double meanL = meanNumL[j] / term3L[j];
          double meanR = meanNumR[j] / term3R[j];
          leastSquares += term1L[j] - 2.0D * meanL * term2L[j] + meanL * meanL * term3L[j];
          
          leastSquares += term1R[j] - 2.0D * meanR * term2R[j] + meanR * meanR * term3R[j];
        }
      }
      
      if ((m_Debug) && (newSplit)) {
        System.out.println(attIndex + "/" + (instances.instance(i).value(attIndex) + instances.instance(i + 1).value(attIndex)) / 2.0D + " = " + (allLS - leastSquares));
      }
      


      if ((newSplit) && (leastSquares < smallestLeastSquares)) {
        bestSplit = (instances.instance(i).value(attIndex) + instances.instance(i + 1).value(attIndex)) / 2.0D;
        
        smallestLeastSquares = leastSquares;
      }
    }
    double[] result = new double[2];
    result[0] = bestSplit;
    result[1] = (smallestLeastSquares > 0.0D ? smallestLeastSquares : 0.0D);
    return result;
  }
  
  private double leastSquares(Instances instances)
  {
    double numerator = 0.0D;double denominator = 0.0D;
    double[] classMeans = new double[m_numOfClasses];
    double[] classTotals = new double[m_numOfClasses];
    
    for (int i = 0; i < instances.numInstances(); i++) {
      LADInstance inst = (LADInstance)instances.instance(i);
      for (int j = 0; j < m_numOfClasses; j++) {
        classMeans[j] += zVector[j] * wVector[j];
        classTotals[j] += wVector[j];
      }
    }
    
    double numInstances = instances.numInstances();
    for (int j = 0; j < m_numOfClasses; j++) {
      if (classTotals[j] != 0.0D) { classMeans[j] /= classTotals[j];
      }
    }
    for (int i = 0; i < instances.numInstances(); i++) {
      for (int j = 0; j < m_numOfClasses; j++) {
        LADInstance inst = (LADInstance)instances.instance(i);
        double w = wVector[j];
        double t = zVector[j] - classMeans[j];
        numerator += w * (t * t);
        denominator += w;
      }
    }
    return numerator > 0.0D ? numerator : 0.0D;
  }
  

  private double leastSquaresNonMissing(Instances instances, int attIndex)
  {
    double numerator = 0.0D;double denominator = 0.0D;
    double[] classMeans = new double[m_numOfClasses];
    double[] classTotals = new double[m_numOfClasses];
    
    for (int i = 0; i < instances.numInstances(); i++) {
      LADInstance inst = (LADInstance)instances.instance(i);
      for (int j = 0; j < m_numOfClasses; j++) {
        classMeans[j] += zVector[j] * wVector[j];
        classTotals[j] += wVector[j];
      }
    }
    
    double numInstances = instances.numInstances();
    for (int j = 0; j < m_numOfClasses; j++) {
      if (classTotals[j] != 0.0D) { classMeans[j] /= classTotals[j];
      }
    }
    for (int i = 0; i < instances.numInstances(); i++) {
      for (int j = 0; j < m_numOfClasses; j++) {
        LADInstance inst = (LADInstance)instances.instance(i);
        if (!inst.isMissing(attIndex)) {
          double w = wVector[j];
          double t = zVector[j] - classMeans[j];
          numerator += w * (t * t);
          denominator += w;
        }
      }
    }
    return numerator > 0.0D ? numerator : 0.0D;
  }
  
  private double[] calcPredictionValues(Instances instances)
  {
    double[] classMeans = new double[m_numOfClasses];
    double meansSum = 0.0D;
    double multiplier = (m_numOfClasses - 1) / m_numOfClasses;
    
    double[] classTotals = new double[m_numOfClasses];
    
    for (int i = 0; i < instances.numInstances(); i++) {
      LADInstance inst = (LADInstance)instances.instance(i);
      for (int j = 0; j < m_numOfClasses; j++) {
        classMeans[j] += zVector[j] * wVector[j];
        classTotals[j] += wVector[j];
      }
    }
    double numInstances = instances.numInstances();
    for (int j = 0; j < m_numOfClasses; j++) {
      if (classTotals[j] != 0.0D) classMeans[j] /= classTotals[j];
      meansSum += classMeans[j];
    }
    meansSum /= m_numOfClasses;
    
    for (int j = 0; j < m_numOfClasses; j++) {
      classMeans[j] = (multiplier * (classMeans[j] - meansSum));
    }
    return classMeans;
  }
  






  public double[] distributionForInstance(Instance instance)
  {
    double[] predValues = new double[m_numOfClasses];
    for (int i = 0; i < m_numOfClasses; i++) predValues[i] = 0.0D;
    double[] distribution = predictionValuesForInstance(instance, m_root, predValues);
    double max = distribution[Utils.maxIndex(distribution)];
    for (int i = 0; i < m_numOfClasses; i++) {
      distribution[i] = Math.exp(distribution[i] - max);
    }
    double sum = Utils.sum(distribution);
    if (sum > 0.0D) Utils.normalize(distribution, sum);
    return distribution;
  }
  










  private double[] predictionValuesForInstance(Instance inst, PredictionNode currentNode, double[] currentValues)
  {
    double[] predValues = currentNode.getValues();
    for (int i = 0; i < m_numOfClasses; i++) { currentValues[i] += predValues[i];
    }
    for (Enumeration e = currentNode.children(); e.hasMoreElements();) {
      Splitter split = (Splitter)e.nextElement();
      int branch = split.branchInstanceGoesDown(inst);
      if (branch >= 0) {
        currentValues = predictionValuesForInstance(inst, split.getChildForBranch(branch), currentValues);
      }
    }
    return currentValues;
  }
  









  public String toString()
  {
    String className = getClass().getName();
    if (m_root == null) {
      return className + " not built yet";
    }
    return className + ":\n\n" + toString(m_root, 1) + "\nLegend: " + legend() + "\n#Tree size (total): " + numOfAllNodes(m_root) + "\n#Tree size (number of predictor nodes): " + numOfPredictionNodes(m_root) + "\n#Leaves (number of predictor nodes): " + numOfLeafNodes(m_root) + "\n#Expanded nodes: " + m_nodesExpanded + "\n#Processed examples: " + m_examplesCounted + "\n#Ratio e/n: " + m_examplesCounted / m_nodesExpanded;
  }
  






















  private String toString(PredictionNode currentNode, int level)
  {
    StringBuffer text = new StringBuffer();
    
    text.append(": ");
    double[] predValues = currentNode.getValues();
    for (int i = 0; i < m_numOfClasses; i++) {
      text.append(Utils.doubleToString(predValues[i], 3));
      if (i < m_numOfClasses - 1) text.append(",");
    }
    for (Enumeration e = currentNode.children(); e.hasMoreElements();) {
      Splitter split = (Splitter)e.nextElement();
      
      for (int j = 0; j < split.getNumOfBranches(); j++) {
        PredictionNode child = split.getChildForBranch(j);
        if (child != null) {
          text.append("\n");
          for (int k = 0; k < level; k++) {
            text.append("|  ");
          }
          text.append("(" + orderAdded + ")");
          text.append(split.attributeString() + " " + split.comparisonString(j));
          text.append(toString(child, level + 1));
        }
      }
    }
    return text.toString();
  }
  





  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    text.append("digraph ADTree {\n");
    
    graphTraverse(m_root, text, 0, 0);
    return text.toString() + "}\n";
  }
  












  protected void graphTraverse(PredictionNode currentNode, StringBuffer text, int splitOrder, int predOrder)
    throws Exception
  {
    text.append("S" + splitOrder + "P" + predOrder + " [label=\"");
    double[] predValues = currentNode.getValues();
    for (int i = 0; i < m_numOfClasses; i++) {
      text.append(Utils.doubleToString(predValues[i], 3));
      if (i < m_numOfClasses - 1) text.append(",");
    }
    if (splitOrder == 0)
      text.append(" (" + legend() + ")");
    text.append("\" shape=box style=filled]\n");
    for (Enumeration e = currentNode.children(); e.hasMoreElements();) {
      Splitter split = (Splitter)e.nextElement();
      text.append("S" + splitOrder + "P" + predOrder + "->" + "S" + orderAdded + " [style=dotted]\n");
      
      text.append("S" + orderAdded + " [label=\"" + orderAdded + ": " + split.attributeString() + "\"]\n");
      

      for (int i = 0; i < split.getNumOfBranches(); i++) {
        PredictionNode child = split.getChildForBranch(i);
        if (child != null) {
          text.append("S" + orderAdded + "->" + "S" + orderAdded + "P" + i + " [label=\"" + Utils.backQuoteChars(split.comparisonString(i)) + "\"]\n");
          
          graphTraverse(child, text, orderAdded, i);
        }
      }
    }
  }
  





  public String legend()
  {
    Attribute classAttribute = null;
    if (m_trainInstances == null) return "";
    try { classAttribute = m_trainInstances.classAttribute(); } catch (Exception x) {}
    if (m_numOfClasses == 1) {
      return "-ve = " + classAttribute.value(0) + ", +ve = " + classAttribute.value(1);
    }
    
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < m_numOfClasses; i++) {
      if (i > 0) text.append(", ");
      text.append(classAttribute.value(i));
    }
    return text.toString();
  }
  









  public String numOfBoostingIterationsTipText()
  {
    return "The number of boosting iterations to use, which determines the size of the tree.";
  }
  





  public int getNumOfBoostingIterations()
  {
    return m_boostingIterations;
  }
  





  public void setNumOfBoostingIterations(int b)
  {
    m_boostingIterations = b;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    newVector.addElement(new Option("\tNumber of boosting iterations.\n\t(Default = 10)", "B", 1, "-B <number of boosting iterations>"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  









  public void setOptions(String[] options)
    throws Exception
  {
    String bString = Utils.getOption('B', options);
    if (bString.length() != 0) { setNumOfBoostingIterations(Integer.parseInt(bString));
    }
    super.setOptions(options);
    
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2 + super.getOptions().length];
    
    int current = 0;
    options[(current++)] = "-B";options[(current++)] = ("" + getNumOfBoostingIterations());
    
    System.arraycopy(super.getOptions(), 0, options, current, super.getOptions().length);
    
    while (current < options.length) options[(current++)] = "";
    return options;
  }
  









  public double measureTreeSize()
  {
    return numOfAllNodes(m_root);
  }
  





  public double measureNumLeaves()
  {
    return numOfPredictionNodes(m_root);
  }
  





  public double measureNumPredictionLeaves()
  {
    return numOfLeafNodes(m_root);
  }
  





  public double measureNodesExpanded()
  {
    return m_nodesExpanded;
  }
  





  public double measureExamplesCounted()
  {
    return m_examplesCounted;
  }
  





  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(5);
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    newVector.addElement("measureNumPredictionLeaves");
    newVector.addElement("measureNodesExpanded");
    newVector.addElement("measureExamplesCounted");
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.equalsIgnoreCase("measureTreeSize")) {
      return measureTreeSize();
    }
    if (additionalMeasureName.equalsIgnoreCase("measureNodesExpanded")) {
      return measureNodesExpanded();
    }
    if (additionalMeasureName.equalsIgnoreCase("measureNumLeaves")) {
      return measureNumLeaves();
    }
    if (additionalMeasureName.equalsIgnoreCase("measureNumPredictionLeaves")) {
      return measureNumPredictionLeaves();
    }
    if (additionalMeasureName.equalsIgnoreCase("measureExamplesCounted")) {
      return measureExamplesCounted();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (ADTree)");
  }
  








  protected int numOfPredictionNodes(PredictionNode root)
  {
    int numSoFar = 0;
    Enumeration e; if (root != null) {
      numSoFar++;
      for (e = root.children(); e.hasMoreElements();) {
        Splitter split = (Splitter)e.nextElement();
        for (int i = 0; i < split.getNumOfBranches(); i++)
          numSoFar += numOfPredictionNodes(split.getChildForBranch(i));
      }
    }
    return numSoFar;
  }
  






  protected int numOfLeafNodes(PredictionNode root)
  {
    int numSoFar = 0;
    Enumeration e; if (root.getChildren().size() > 0)
      for (e = root.children(); e.hasMoreElements();) {
        Splitter split = (Splitter)e.nextElement();
        for (int i = 0; i < split.getNumOfBranches(); i++)
          numSoFar += numOfLeafNodes(split.getChildForBranch(i));
      } else
      numSoFar = 1;
    return numSoFar;
  }
  






  protected int numOfAllNodes(PredictionNode root)
  {
    int numSoFar = 0;
    Enumeration e; if (root != null) {
      numSoFar++;
      for (e = root.children(); e.hasMoreElements();) {
        numSoFar++;
        Splitter split = (Splitter)e.nextElement();
        for (int i = 0; i < split.getNumOfBranches(); i++)
          numSoFar += numOfAllNodes(split.getChildForBranch(i));
      }
    }
    return numSoFar;
  }
  








  public void buildClassifier(Instances instances)
    throws Exception
  {
    initClassifier(instances);
    

    for (int T = 0; T < m_boostingIterations; T++) {
      boost();
    }
  }
  
  public int predictiveError(Instances test) {
    int error = 0;
    for (int i = test.numInstances() - 1; i >= 0; i--) {
      Instance inst = test.instance(i);
      try {
        if (classifyInstance(inst) != inst.classValue())
          error++;
      } catch (Exception e) { error++;
      } }
    return error;
  }
  







  public void merge(LADTree mergeWith)
    throws Exception
  {
    if ((m_root == null) || (m_root == null))
      throw new Exception("Trying to merge an uninitialized tree");
    if (m_numOfClasses != m_numOfClasses) {
      throw new Exception("Trees not suitable for merge - different sized prediction nodes");
    }
    m_root.merge(m_root);
  }
  




  public int graphType()
  {
    return 1;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10279 $");
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
  




  public static void main(String[] argv)
  {
    runClassifier(new LADTree(), argv);
  }
}

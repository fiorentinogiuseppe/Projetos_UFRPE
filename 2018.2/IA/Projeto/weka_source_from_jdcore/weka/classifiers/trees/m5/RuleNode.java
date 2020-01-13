package weka.classifiers.trees.m5;

import java.io.PrintStream;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
























































































public class RuleNode
  extends Classifier
{
  static final long serialVersionUID = 1979807611124337144L;
  private Instances m_instances;
  private int m_classIndex;
  protected int m_numInstances;
  private int m_numAttributes;
  private boolean m_isLeaf;
  private int m_splitAtt;
  private double m_splitValue;
  private PreConstructedLinearModel m_nodeModel;
  public int m_numParameters;
  private double m_rootMeanSquaredError;
  protected RuleNode m_left;
  protected RuleNode m_right;
  private RuleNode m_parent;
  private double m_splitNum = 4.0D;
  




  private double m_devFraction = 0.05D;
  private double m_pruningMultiplier = 2.0D;
  




  private int m_leafModelNum;
  




  private double m_globalDeviation;
  




  private double m_globalAbsDeviation;
  




  private int[] m_indices;
  




  private static final double SMOOTHING_CONSTANT = 15.0D;
  



  private int m_id;
  



  private boolean m_saveInstances = false;
  




  private boolean m_regressionTree;
  





  public RuleNode(double globalDev, double globalAbsDev, RuleNode parent)
  {
    m_nodeModel = null;
    m_right = null;
    m_left = null;
    m_parent = parent;
    m_globalDeviation = globalDev;
    m_globalAbsDeviation = globalAbsDev;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    m_rootMeanSquaredError = Double.MAX_VALUE;
    
    m_instances = data;
    m_classIndex = m_instances.classIndex();
    m_numInstances = m_instances.numInstances();
    m_numAttributes = m_instances.numAttributes();
    m_nodeModel = null;
    m_right = null;
    m_left = null;
    
    if ((m_numInstances < m_splitNum) || (Rule.stdDev(m_classIndex, m_instances) < m_globalDeviation * m_devFraction))
    {

      m_isLeaf = true;
    } else {
      m_isLeaf = false;
    }
    
    split();
  }
  






  public double classifyInstance(Instance inst)
    throws Exception
  {
    if (m_isLeaf) {
      if (m_nodeModel == null) {
        throw new Exception("Classifier has not been built correctly.");
      }
      
      return m_nodeModel.classifyInstance(inst);
    }
    
    if (inst.value(m_splitAtt) <= m_splitValue) {
      return m_left.classifyInstance(inst);
    }
    return m_right.classifyInstance(inst);
  }
  













  protected static double smoothingOriginal(double n, double pred, double supportPred)
    throws Exception
  {
    double smoothed = (n * pred + 15.0D * supportPred) / (n + 15.0D);
    


    return smoothed;
  }
  









  public void split()
    throws Exception
  {
    if (!m_isLeaf)
    {
      SplitEvaluate bestSplit = new YongSplitInfo(0, m_numInstances - 1, -1);
      SplitEvaluate currentSplit = new YongSplitInfo(0, m_numInstances - 1, -1);
      

      for (int i = 0; i < m_numAttributes; i++) {
        if (i != m_classIndex)
        {

          m_instances.sort(i);
          currentSplit.attrSplit(i, m_instances);
          
          if ((Math.abs(currentSplit.maxImpurity() - bestSplit.maxImpurity()) > 1.0E-6D) && (currentSplit.maxImpurity() > bestSplit.maxImpurity() + 1.0E-6D))
          {


            bestSplit = currentSplit.copy();
          }
        }
      }
      

      if ((bestSplit.splitAttr() < 0) || (bestSplit.position() < 1) || (bestSplit.position() > m_numInstances - 1))
      {
        m_isLeaf = true;
      } else {
        m_splitAtt = bestSplit.splitAttr();
        m_splitValue = bestSplit.splitValue();
        Instances leftSubset = new Instances(m_instances, m_numInstances);
        Instances rightSubset = new Instances(m_instances, m_numInstances);
        
        for (i = 0; i < m_numInstances; i++) {
          if (m_instances.instance(i).value(m_splitAtt) <= m_splitValue) {
            leftSubset.add(m_instances.instance(i));
          } else {
            rightSubset.add(m_instances.instance(i));
          }
        }
        
        leftSubset.compactify();
        rightSubset.compactify();
        

        m_left = new RuleNode(m_globalDeviation, m_globalAbsDeviation, this);
        m_left.setMinNumInstances(m_splitNum);
        m_left.setRegressionTree(m_regressionTree);
        m_left.setSaveInstances(m_saveInstances);
        m_left.buildClassifier(leftSubset);
        
        m_right = new RuleNode(m_globalDeviation, m_globalAbsDeviation, this);
        m_right.setMinNumInstances(m_splitNum);
        m_right.setRegressionTree(m_regressionTree);
        m_right.setSaveInstances(m_saveInstances);
        m_right.buildClassifier(rightSubset);
        


        if (!m_regressionTree) {
          boolean[] attsBelow = attsTestedBelow();
          attsBelow[m_classIndex] = true;
          int count = 0;
          
          for (int j = 0; j < m_numAttributes; j++) {
            if (attsBelow[j] != 0) {
              count++;
            }
          }
          
          int[] indices = new int[count];
          
          count = 0;
          
          for (j = 0; j < m_numAttributes; j++) {
            if ((attsBelow[j] != 0) && (j != m_classIndex)) {
              indices[(count++)] = j;
            }
          }
          
          indices[count] = m_classIndex;
          m_indices = indices;
        } else {
          m_indices = new int[1];
          m_indices[0] = m_classIndex;
          m_numParameters = 1;
        }
      }
    }
    
    if (m_isLeaf) {
      int[] indices = new int[1];
      indices[0] = m_classIndex;
      m_indices = indices;
      m_numParameters = 1;
    }
  }
  











  private void buildLinearModel(int[] indices)
    throws Exception
  {
    Instances reducedInst = new Instances(m_instances);
    Remove attributeFilter = new Remove();
    
    attributeFilter.setInvertSelection(true);
    attributeFilter.setAttributeIndicesArray(indices);
    attributeFilter.setInputFormat(reducedInst);
    
    reducedInst = Filter.useFilter(reducedInst, attributeFilter);
    


    LinearRegression temp = new LinearRegression();
    temp.buildClassifier(reducedInst);
    
    double[] lmCoeffs = temp.coefficients();
    double[] coeffs = new double[m_instances.numAttributes()];
    
    for (int i = 0; i < lmCoeffs.length - 1; i++) {
      if (indices[i] != m_classIndex) {
        coeffs[indices[i]] = lmCoeffs[i];
      }
    }
    m_nodeModel = new PreConstructedLinearModel(coeffs, lmCoeffs[(lmCoeffs.length - 1)]);
    m_nodeModel.buildClassifier(m_instances);
  }
  





  private boolean[] attsTestedAbove()
  {
    boolean[] atts = new boolean[m_numAttributes];
    boolean[] attsAbove = null;
    
    if (m_parent != null) {
      attsAbove = m_parent.attsTestedAbove();
    }
    
    if (attsAbove != null) {
      for (int i = 0; i < m_numAttributes; i++) {
        atts[i] = attsAbove[i];
      }
    }
    
    atts[m_splitAtt] = true;
    return atts;
  }
  





  private boolean[] attsTestedBelow()
  {
    boolean[] attsBelow = new boolean[m_numAttributes];
    boolean[] attsBelowLeft = null;
    boolean[] attsBelowRight = null;
    
    if (m_right != null) {
      attsBelowRight = m_right.attsTestedBelow();
    }
    
    if (m_left != null) {
      attsBelowLeft = m_left.attsTestedBelow();
    }
    
    for (int i = 0; i < m_numAttributes; i++) {
      if (attsBelowLeft != null) {
        attsBelow[i] = ((attsBelow[i] != 0) || (attsBelowLeft[i] != 0) ? 1 : false);
      }
      
      if (attsBelowRight != null) {
        attsBelow[i] = ((attsBelow[i] != 0) || (attsBelowRight[i] != 0) ? 1 : false);
      }
    }
    
    if (!m_isLeaf) {
      attsBelow[m_splitAtt] = true;
    }
    return attsBelow;
  }
  





  public int numLeaves(int leafCounter)
  {
    if (!m_isLeaf)
    {
      m_leafModelNum = 0;
      
      if (m_left != null) {
        leafCounter = m_left.numLeaves(leafCounter);
      }
      
      if (m_right != null) {
        leafCounter = m_right.numLeaves(leafCounter);
      }
    }
    else {
      leafCounter++;
      m_leafModelNum = leafCounter;
    }
    return leafCounter;
  }
  




  public String toString()
  {
    return printNodeLinearModel();
  }
  




  public String printNodeLinearModel()
  {
    return m_nodeModel.toString();
  }
  




  public String printLeafModels()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_isLeaf) {
      text.append("\nLM num: " + m_leafModelNum);
      text.append(m_nodeModel.toString());
      text.append("\n");
    } else {
      text.append(m_left.printLeafModels());
      text.append(m_right.printLeafModels());
    }
    return text.toString();
  }
  




  public String nodeToString()
  {
    StringBuffer text = new StringBuffer();
    
    System.out.println("In to string");
    text.append("Node:\n\tnum inst: " + m_numInstances);
    
    if (m_isLeaf) {
      text.append("\n\tleaf");
    } else {
      text.append("\tnode");
    }
    
    text.append("\n\tSplit att: " + m_instances.attribute(m_splitAtt).name());
    text.append("\n\tSplit val: " + Utils.doubleToString(m_splitValue, 1, 3));
    text.append("\n\tLM num: " + m_leafModelNum);
    text.append("\n\tLinear model\n" + m_nodeModel.toString());
    text.append("\n\n");
    
    if (m_left != null) {
      text.append(m_left.nodeToString());
    }
    
    if (m_right != null) {
      text.append(m_right.nodeToString());
    }
    
    return text.toString();
  }
  






  public String treeToString(int level)
  {
    StringBuffer text = new StringBuffer();
    
    if (!m_isLeaf) {
      text.append("\n");
      
      for (int i = 1; i <= level; i++) {
        text.append("|   ");
      }
      
      if (m_instances.attribute(m_splitAtt).name().charAt(0) != '[') {
        text.append(m_instances.attribute(m_splitAtt).name() + " <= " + Utils.doubleToString(m_splitValue, 1, 3) + " : ");
      }
      else {
        text.append(m_instances.attribute(m_splitAtt).name() + " false : ");
      }
      
      if (m_left != null) {
        text.append(m_left.treeToString(level + 1));
      } else {
        text.append("NULL\n");
      }
      
      for (i = 1; i <= level; i++) {
        text.append("|   ");
      }
      
      if (m_instances.attribute(m_splitAtt).name().charAt(0) != '[') {
        text.append(m_instances.attribute(m_splitAtt).name() + " >  " + Utils.doubleToString(m_splitValue, 1, 3) + " : ");
      }
      else {
        text.append(m_instances.attribute(m_splitAtt).name() + " true : ");
      }
      
      if (m_right != null) {
        text.append(m_right.treeToString(level + 1));
      } else {
        text.append("NULL\n");
      }
    } else {
      text.append("LM" + m_leafModelNum);
      
      if (m_globalDeviation > 0.0D) {
        text.append(" (" + m_numInstances + "/" + Utils.doubleToString(100.0D * m_rootMeanSquaredError / m_globalDeviation, 1, 3) + "%)\n");

      }
      else
      {

        text.append(" (" + m_numInstances + ")\n");
      }
    }
    return text.toString();
  }
  





  public void installLinearModels()
    throws Exception
  {
    if (m_isLeaf) {
      buildLinearModel(m_indices);
    } else {
      if (m_left != null) {
        m_left.installLinearModels();
      }
      
      if (m_right != null) {
        m_right.installLinearModels();
      }
      buildLinearModel(m_indices);
    }
    Evaluation nodeModelEval = new Evaluation(m_instances);
    nodeModelEval.evaluateModel(m_nodeModel, m_instances, new Object[0]);
    m_rootMeanSquaredError = nodeModelEval.rootMeanSquaredError();
    
    if (!m_saveInstances) {
      m_instances = new Instances(m_instances, 0);
    }
  }
  



  public void installSmoothedModels()
    throws Exception
  {
    if (m_isLeaf) {
      double[] coefficients = new double[m_numAttributes];
      
      double[] coeffsUsedByLinearModel = m_nodeModel.coefficients();
      RuleNode current = this;
      

      for (int i = 0; i < coeffsUsedByLinearModel.length; i++) {
        if (i != m_classIndex) {
          coefficients[i] = coeffsUsedByLinearModel[i];
        }
      }
      
      double intercept = m_nodeModel.intercept();
      do
      {
        if (m_parent != null) {
          double n = m_numInstances;
          
          for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = (coefficients[i] * n / (n + 15.0D));
          }
          intercept = intercept * n / (n + 15.0D);
          

          coeffsUsedByLinearModel = m_parent.getModel().coefficients();
          for (int i = 0; i < coeffsUsedByLinearModel.length; i++) {
            if (i != m_classIndex)
            {
              coefficients[i] += 15.0D * coeffsUsedByLinearModel[i] / (n + 15.0D);
            }
          }
          


          intercept += 15.0D * m_parent.getModel().intercept() / (n + 15.0D);
          


          current = m_parent;
        }
      } while (m_parent != null);
      m_nodeModel = new PreConstructedLinearModel(coefficients, intercept);
      
      m_nodeModel.buildClassifier(m_instances);
    }
    if (m_left != null) {
      m_left.installSmoothedModels();
    }
    if (m_right != null) {
      m_right.installSmoothedModels();
    }
  }
  



  public void prune()
    throws Exception
  {
    Evaluation nodeModelEval = null;
    
    if (m_isLeaf) {
      buildLinearModel(m_indices);
      nodeModelEval = new Evaluation(m_instances);
      


      nodeModelEval.evaluateModel(m_nodeModel, m_instances, new Object[0]);
      
      m_rootMeanSquaredError = nodeModelEval.rootMeanSquaredError();
    }
    else
    {
      if (m_left != null) {
        m_left.prune();
      }
      
      if (m_right != null) {
        m_right.prune();
      }
      
      buildLinearModel(m_indices);
      nodeModelEval = new Evaluation(m_instances);
      



      nodeModelEval.evaluateModel(m_nodeModel, m_instances, new Object[0]);
      
      double rmsModel = nodeModelEval.rootMeanSquaredError();
      double adjustedErrorModel = rmsModel * pruningFactor(m_numInstances, m_nodeModel.numParameters() + 1);
      



      Evaluation nodeEval = new Evaluation(m_instances);
      

      int l_params = 0;int r_params = 0;
      
      nodeEval.evaluateModel(this, m_instances, new Object[0]);
      
      double rmsSubTree = nodeEval.rootMeanSquaredError();
      
      if (m_left != null) {
        l_params = m_left.numParameters();
      }
      
      if (m_right != null) {
        r_params = m_right.numParameters();
      }
      
      double adjustedErrorNode = rmsSubTree * pruningFactor(m_numInstances, l_params + r_params + 1);
      


      if ((adjustedErrorModel <= adjustedErrorNode) || (adjustedErrorModel < m_globalDeviation * 1.0E-5D))
      {


        m_isLeaf = true;
        m_right = null;
        m_left = null;
        m_numParameters = (m_nodeModel.numParameters() + 1);
        m_rootMeanSquaredError = rmsModel;
      } else {
        m_numParameters = (l_params + r_params + 1);
        m_rootMeanSquaredError = rmsSubTree;
      }
    }
    
    if (!m_saveInstances) {
      m_instances = new Instances(m_instances, 0);
    }
  }
  







  private double pruningFactor(int num_instances, int num_params)
  {
    if (num_instances <= num_params) {
      return 10.0D;
    }
    
    return (num_instances + m_pruningMultiplier * num_params) / (num_instances - num_params);
  }
  






  public void findBestLeaf(double[] maxCoverage, RuleNode[] bestLeaf)
  {
    if (!m_isLeaf) {
      if (m_left != null) {
        m_left.findBestLeaf(maxCoverage, bestLeaf);
      }
      
      if (m_right != null) {
        m_right.findBestLeaf(maxCoverage, bestLeaf);
      }
    }
    else if (m_numInstances > maxCoverage[0]) {
      maxCoverage[0] = m_numInstances;
      bestLeaf[0] = this;
    }
  }
  





  public void returnLeaves(FastVector[] v)
  {
    if (m_isLeaf) {
      v[0].addElement(this);
    } else {
      if (m_left != null) {
        m_left.returnLeaves(v);
      }
      
      if (m_right != null) {
        m_right.returnLeaves(v);
      }
    }
  }
  




  public RuleNode parentNode()
  {
    return m_parent;
  }
  




  public RuleNode leftNode()
  {
    return m_left;
  }
  




  public RuleNode rightNode()
  {
    return m_right;
  }
  




  public int splitAtt()
  {
    return m_splitAtt;
  }
  




  public double splitVal()
  {
    return m_splitValue;
  }
  




  public int numberOfLinearModels()
  {
    if (m_isLeaf) {
      return 1;
    }
    return m_left.numberOfLinearModels() + m_right.numberOfLinearModels();
  }
  





  public boolean isLeaf()
  {
    return m_isLeaf;
  }
  




  protected double rootMeanSquaredError()
  {
    return m_rootMeanSquaredError;
  }
  




  public PreConstructedLinearModel getModel()
  {
    return m_nodeModel;
  }
  




  public int getNumInstances()
  {
    return m_numInstances;
  }
  




  private int numParameters()
  {
    return m_numParameters;
  }
  





  public boolean getRegressionTree()
  {
    return m_regressionTree;
  }
  




  public void setMinNumInstances(double minNum)
  {
    m_splitNum = minNum;
  }
  




  public double getMinNumInstances()
  {
    return m_splitNum;
  }
  





  public void setRegressionTree(boolean newregressionTree)
  {
    m_regressionTree = newregressionTree;
  }
  


  public void printAllModels()
  {
    if (m_isLeaf) {
      System.out.println(m_nodeModel.toString());
    } else {
      System.out.println(m_nodeModel.toString());
      m_left.printAllModels();
      m_right.printAllModels();
    }
  }
  





  protected int assignIDs(int lastID)
  {
    int currLastID = lastID + 1;
    m_id = currLastID;
    
    if (m_left != null) {
      currLastID = m_left.assignIDs(currLastID);
    }
    
    if (m_right != null) {
      currLastID = m_right.assignIDs(currLastID);
    }
    return currLastID;
  }
  





  public void graph(StringBuffer text)
  {
    assignIDs(-1);
    graphTree(text);
  }
  




  protected void graphTree(StringBuffer text)
  {
    text.append("N" + m_id + (m_isLeaf ? " [label=\"LM " + m_leafModelNum : new StringBuilder().append(" [label=\"").append(Utils.backQuoteChars(m_instances.attribute(m_splitAtt).name())).toString()) + (m_isLeaf ? " (" + (m_globalDeviation > 0.0D ? m_numInstances + "/" + Utils.doubleToString(100.0D * m_rootMeanSquaredError / m_globalDeviation, 1, 3) + "%)" : new StringBuilder().append(m_numInstances).append(")").toString()) + "\" shape=box style=filled " : "\"") + (m_saveInstances ? "data=\n" + m_instances + "\n,\n" : "") + "]\n");
    


















    if (m_left != null) {
      text.append("N" + m_id + "->" + "N" + m_left.m_id + " [label=\"<=" + Utils.doubleToString(m_splitValue, 1, 3) + "\"]\n");
      

      m_left.graphTree(text);
    }
    
    if (m_right != null) {
      text.append("N" + m_id + "->" + "N" + m_right.m_id + " [label=\">" + Utils.doubleToString(m_splitValue, 1, 3) + "\"]\n");
      

      m_right.graphTree(text);
    }
  }
  





  protected void setSaveInstances(boolean save)
  {
    m_saveInstances = save;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}

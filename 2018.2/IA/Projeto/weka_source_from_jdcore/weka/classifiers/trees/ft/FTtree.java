package weka.classifiers.trees.ft;

import java.util.Vector;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.trees.j48.BinC45ModelSelection;
import weka.classifiers.trees.j48.BinC45Split;
import weka.classifiers.trees.j48.C45Split;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.Distribution;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.Stats;
import weka.classifiers.trees.lmt.LogisticBase;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.NominalToBinary;

















































public abstract class FTtree
  extends LogisticBase
{
  static final long serialVersionUID = 1862737145870398755L;
  protected double m_totalInstanceWeight;
  protected int m_id;
  protected int m_leafModelNum;
  protected int m_minNumInstances;
  protected ModelSelection m_modelSelection;
  protected NominalToBinary m_nominalToBinary;
  protected SimpleLinearRegression[][] m_higherRegressions;
  protected int m_numHigherRegressions = 0;
  

  protected int m_numInstances;
  

  protected ClassifierSplitModel m_localModel;
  

  protected ClassifierSplitModel m_auxLocalModel;
  

  protected FTtree[] m_sons;
  

  protected int m_leafclass;
  

  protected boolean m_isLeaf;
  

  protected boolean m_hasConstr = true;
  

  protected double m_constError = 0.0D;
  

  protected float m_CF = 0.1F;
  





  public FTtree() {}
  





  public abstract void buildClassifier(Instances paramInstances)
    throws Exception;
  





  public abstract void buildTree(Instances paramInstances, SimpleLinearRegression[][] paramArrayOfSimpleLinearRegression, double paramDouble1, double paramDouble2)
    throws Exception;
  





  public abstract double prune()
    throws Exception;
  




  protected Instances insertNewAttr(Instances data)
    throws Exception
  {
    for (int i = 0; i < data.classAttribute().numValues(); i++)
    {
      data.insertAttributeAt(new Attribute("N" + i), i);
    }
    return data;
  }
  



  protected Instances removeExtAttributes(Instances data)
    throws Exception
  {
    for (int i = 0; i < data.classAttribute().numValues(); i++)
    {
      data.deleteAttributeAt(0);
    }
    return data;
  }
  



  protected double getEstimatedErrors()
  {
    double errors = 0.0D;
    

    if (m_isLeaf) {
      return getEstimatedErrorsForDistribution(m_localModel.distribution());
    }
    for (int i = 0; i < m_sons.length; i++) {
      errors += m_sons[i].getEstimatedErrors();
    }
    return errors;
  }
  







  protected double getEstimatedErrorsForBranch(Instances data)
    throws Exception
  {
    double errors = 0.0D;
    

    if (m_isLeaf) {
      return getEstimatedErrorsForDistribution(new Distribution(data));
    }
    Distribution savedDist = m_localModel.distribution();
    m_localModel.resetDistribution(data);
    Instances[] localInstances = (Instances[])m_localModel.split(data);
    
    for (int i = 0; i < m_sons.length; i++) {
      errors += m_sons[i].getEstimatedErrorsForBranch(localInstances[i]);
    }
    return errors;
  }
  






  protected double getEstimatedErrorsForDistribution(Distribution theDistribution)
  {
    if (Utils.eq(theDistribution.total(), 0.0D)) {
      return 0.0D;
    }
    
    double numInc = theDistribution.numIncorrect();
    double numTotal = theDistribution.total();
    return (Stats.addErrs(numTotal, numInc, m_CF) + numInc) / numTotal;
  }
  






  protected double getEtimateConstModel(Distribution theDistribution)
  {
    if (Utils.eq(theDistribution.total(), 0.0D)) {
      return 0.0D;
    }
    
    double numTotal = theDistribution.total();
    return (Stats.addErrs(numTotal, m_constError, m_CF) + m_constError) / numTotal;
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
  




  public int getConstError(double[] probsConst)
  {
    return Utils.maxIndex(probsConst);
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
  





  public abstract double[] distributionForInstance(Instance paramInstance)
    throws Exception;
  





  public String toString()
  {
    assignLeafModelNumbers(0);
    try {
      StringBuffer text = new StringBuffer();
      
      if ((m_isLeaf) && (!m_hasConstr)) {
        text.append(": ");
        text.append("Class=" + m_leafclass);


      }
      else if ((m_isLeaf) && (m_hasConstr)) {
        text.append(": ");
        text.append("FT_" + m_leafModelNum + ":" + getModelParameters());
      }
      else {
        dumpTree(0, text);
      }
      
      text.append("\n\nNumber of Leaves  : \t" + numLeaves() + "\n");
      text.append("\nSize of the Tree : \t" + numNodes() + "\n");
      

      text.append(modelsToString());
      return text.toString();
    } catch (Exception e) {}
    return "Can't print logistic model tree";
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
      if (m_hasConstr) {
        text.append(m_localModel.leftSide(m_train) + "#" + m_id);
      } else
        text.append(m_localModel.leftSide(m_train));
      text.append(m_localModel.rightSide(i, m_train));
      if ((m_sons[i].m_isLeaf) && (m_sons[i].m_hasConstr)) {
        text.append(": ");
        text.append("FT_" + m_sons[i].m_leafModelNum + ":" + m_sons[i].getModelParameters());
      }
      else if ((m_sons[i].m_isLeaf) && (!m_sons[i].m_hasConstr))
      {
        text.append(": ");
        text.append("Class=" + m_sons[i].m_leafclass);
      }
      else
      {
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
    if ((m_isLeaf) && (m_hasConstr)) {
      text.append("FT_" + m_leafModelNum + ":" + super.toString());

    }
    else if ((!m_isLeaf) && (m_hasConstr)) {
      if ((m_modelSelection instanceof BinC45ModelSelection)) {
        text.append("FT_N" + ((BinC45Split)m_localModel).attIndex() + "#" + m_id + ":" + super.toString());
      } else {
        text.append("FT_N" + ((C45Split)m_localModel).attIndex() + "#" + m_id + ":" + super.toString());
      }
      for (int i = 0; i < m_sons.length; i++) {
        text.append("\n" + m_sons[i].modelsToString());
      }
    }
    else if ((!m_isLeaf) && (!m_hasConstr))
    {
      for (int i = 0; i < m_sons.length; i++) {
        text.append("\n" + m_sons[i].modelsToString());
      }
    }
    else if ((m_isLeaf) && (!m_hasConstr))
    {
      text.append("");
    }
    




    return text.toString();
  }
  




  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    assignIDs(-1);
    assignLeafModelNumbers(0);
    text.append("digraph FTree {\n");
    if ((m_isLeaf) && (m_hasConstr)) {
      text.append("N" + m_id + " [label=\"FT_" + m_leafModelNum + ":" + getModelParameters() + "\" " + "shape=box style=filled");
      
      text.append("]\n");
    }
    else if ((m_isLeaf) && (!m_hasConstr)) {
      text.append("N" + m_id + " [label=\"Class=" + m_leafclass + "\" " + "shape=box style=filled");
      
      text.append("]\n");
    }
    else {
      text.append("N" + m_id + " [label=\"" + Utils.backQuoteChars(m_localModel.leftSide(m_train)) + "\" ");
      

      text.append("]\n");
      graphTree(text);
    }
    
    return text.toString() + "}\n";
  }
  




  protected void graphTree(StringBuffer text)
    throws Exception
  {
    for (int i = 0; i < m_sons.length; i++) {
      text.append("N" + m_id + "->" + "N" + m_sons[i].m_id + " [label=\"" + Utils.backQuoteChars(m_localModel.rightSide(i, m_train).trim()) + "\"]\n");
      



      if ((m_sons[i].m_isLeaf) && (m_sons[i].m_hasConstr)) {
        text.append("N" + m_sons[i].m_id + " [label=\"FT_" + m_sons[i].m_leafModelNum + ":" + m_sons[i].getModelParameters() + "\" " + "shape=box style=filled");
        
        text.append("]\n");
      }
      else if ((m_sons[i].m_isLeaf) && (!m_sons[i].m_hasConstr)) {
        text.append("N" + m_sons[i].m_id + " [label=\"Class=" + m_sons[i].m_leafclass + "\" " + "shape=box style=filled");
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
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}

package weka.classifiers.trees.j48;

import java.io.Serializable;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;





















































public class ClassifierTree
  implements Drawable, Serializable, CapabilitiesHandler, RevisionHandler
{
  static final long serialVersionUID = -8722249377542734193L;
  protected ModelSelection m_toSelectModel;
  protected ClassifierSplitModel m_localModel;
  protected ClassifierTree[] m_sons;
  protected boolean m_isLeaf;
  protected boolean m_isEmpty;
  protected Instances m_train;
  protected Distribution m_test;
  protected int m_id;
  private static long PRINTED_NODES = 0L;
  





  protected static long nextID()
  {
    return PRINTED_NODES++;
  }
  




  protected static void resetID()
  {
    PRINTED_NODES = 0L;
  }
  



  public ClassifierTree(ModelSelection toSelectLocModel)
  {
    m_toSelectModel = toSelectLocModel;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    buildTree(data, false);
  }
  









  public void buildTree(Instances data, boolean keepData)
    throws Exception
  {
    if (keepData) {
      m_train = data;
    }
    m_test = null;
    m_isLeaf = false;
    m_isEmpty = false;
    m_sons = null;
    m_localModel = m_toSelectModel.selectModel(data);
    if (m_localModel.numSubsets() > 1) {
      Instances[] localInstances = m_localModel.split(data);
      data = null;
      m_sons = new ClassifierTree[m_localModel.numSubsets()];
      for (int i = 0; i < m_sons.length; i++) {
        m_sons[i] = getNewTree(localInstances[i]);
        localInstances[i] = null;
      }
    } else {
      m_isLeaf = true;
      if (Utils.eq(data.sumOfWeights(), 0.0D))
        m_isEmpty = true;
      data = null;
    }
  }
  












  public void buildTree(Instances train, Instances test, boolean keepData)
    throws Exception
  {
    if (keepData) {
      m_train = train;
    }
    m_isLeaf = false;
    m_isEmpty = false;
    m_sons = null;
    m_localModel = m_toSelectModel.selectModel(train, test);
    m_test = new Distribution(test, m_localModel);
    if (m_localModel.numSubsets() > 1) {
      Instances[] localTrain = m_localModel.split(train);
      Instances[] localTest = m_localModel.split(test);
      train = test = null;
      m_sons = new ClassifierTree[m_localModel.numSubsets()];
      for (int i = 0; i < m_sons.length; i++) {
        m_sons[i] = getNewTree(localTrain[i], localTest[i]);
        localTrain[i] = null;
        localTest[i] = null;
      }
    }
    m_isLeaf = true;
    if (Utils.eq(train.sumOfWeights(), 0.0D))
      m_isEmpty = true;
    train = test = null;
  }
  








  public double classifyInstance(Instance instance)
    throws Exception
  {
    double maxProb = -1.0D;
    
    int maxIndex = 0;
    

    for (int j = 0; j < instance.numClasses(); j++) {
      double currentProb = getProbs(j, instance, 1.0D);
      if (Utils.gr(currentProb, maxProb)) {
        maxIndex = j;
        maxProb = currentProb;
      }
    }
    
    return maxIndex;
  }
  





  public final void cleanup(Instances justHeaderInfo)
  {
    m_train = justHeaderInfo;
    m_test = null;
    if (!m_isLeaf) {
      for (int i = 0; i < m_sons.length; i++) {
        m_sons[i].cleanup(justHeaderInfo);
      }
    }
  }
  







  public final double[] distributionForInstance(Instance instance, boolean useLaplace)
    throws Exception
  {
    double[] doubles = new double[instance.numClasses()];
    
    for (int i = 0; i < doubles.length; i++) {
      if (!useLaplace) {
        doubles[i] = getProbs(i, instance, 1.0D);
      } else {
        doubles[i] = getProbsLaplace(i, instance, 1.0D);
      }
    }
    
    return doubles;
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
  




  public int graphType()
  {
    return 1;
  }
  





  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    assignIDs(-1);
    text.append("digraph J48Tree {\n");
    if (m_isLeaf) {
      text.append("N" + m_id + " [label=\"" + Utils.quote(m_localModel.dumpLabel(0, m_train)) + "\" " + "shape=box style=filled ");
      


      if ((m_train != null) && (m_train.numInstances() > 0)) {
        text.append("data =\n" + m_train + "\n");
        text.append(",\n");
      }
      
      text.append("]\n");
    } else {
      text.append("N" + m_id + " [label=\"" + Utils.quote(m_localModel.leftSide(m_train)) + "\" ");
      

      if ((m_train != null) && (m_train.numInstances() > 0)) {
        text.append("data =\n" + m_train + "\n");
        text.append(",\n");
      }
      text.append("]\n");
      graphTree(text);
    }
    
    return text.toString() + "}\n";
  }
  







  public String prefix()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    if (m_isLeaf) {
      text.append("[" + m_localModel.dumpLabel(0, m_train) + "]");
    } else {
      prefixTree(text);
    }
    
    return text.toString();
  }
  











  public StringBuffer[] toSource(String className)
    throws Exception
  {
    StringBuffer[] result = new StringBuffer[2];
    if (m_isLeaf) {
      result[0] = new StringBuffer("    p = " + m_localModel.distribution().maxClass(0) + ";\n");
      
      result[1] = new StringBuffer("");
    } else {
      StringBuffer text = new StringBuffer();
      StringBuffer atEnd = new StringBuffer();
      
      long printID = nextID();
      
      text.append("  static double N").append(Integer.toHexString(m_localModel.hashCode()) + printID).append("(Object []i) {\n").append("    double p = Double.NaN;\n");
      



      text.append("    if (").append(m_localModel.sourceExpression(-1, m_train)).append(") {\n");
      

      text.append("      p = ").append(m_localModel.distribution().maxClass(0)).append(";\n");
      

      text.append("    } ");
      for (int i = 0; i < m_sons.length; i++) {
        text.append("else if (" + m_localModel.sourceExpression(i, m_train) + ") {\n");
        
        if (m_sons[i].m_isLeaf) {
          text.append("      p = " + m_localModel.distribution().maxClass(i) + ";\n");
        }
        else {
          StringBuffer[] sub = m_sons[i].toSource(className);
          text.append(sub[0]);
          atEnd.append(sub[1]);
        }
        text.append("    } ");
        if (i == m_sons.length - 1) {
          text.append('\n');
        }
      }
      
      text.append("    return p;\n  }\n");
      
      result[0] = new StringBuffer("    p = " + className + ".N");
      result[0].append(Integer.toHexString(m_localModel.hashCode()) + printID).append("(i);\n");
      
      result[1] = text.append(atEnd);
    }
    return result;
  }
  





  public int numLeaves()
  {
    int num = 0;
    

    if (m_isLeaf) {
      return 1;
    }
    for (int i = 0; i < m_sons.length; i++) {
      num += m_sons[i].numLeaves();
    }
    return num;
  }
  





  public int numNodes()
  {
    int no = 1;
    

    if (!m_isLeaf) {
      for (int i = 0; i < m_sons.length; i++)
        no += m_sons[i].numNodes();
    }
    return no;
  }
  




  public String toString()
  {
    try
    {
      StringBuffer text = new StringBuffer();
      
      if (m_isLeaf) {
        text.append(": ");
        text.append(m_localModel.dumpLabel(0, m_train));
      } else {
        dumpTree(0, text); }
      text.append("\n\nNumber of Leaves  : \t" + numLeaves() + "\n");
      text.append("\nSize of the tree : \t" + numNodes() + "\n");
      
      return text.toString();
    } catch (Exception e) {}
    return "Can't print classification tree.";
  }
  







  protected ClassifierTree getNewTree(Instances data)
    throws Exception
  {
    ClassifierTree newTree = new ClassifierTree(m_toSelectModel);
    newTree.buildTree(data, false);
    
    return newTree;
  }
  








  protected ClassifierTree getNewTree(Instances train, Instances test)
    throws Exception
  {
    ClassifierTree newTree = new ClassifierTree(m_toSelectModel);
    newTree.buildTree(train, test, false);
    
    return newTree;
  }
  









  private void dumpTree(int depth, StringBuffer text)
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
        text.append(m_localModel.dumpLabel(i, m_train));
      } else {
        m_sons[i].dumpTree(depth + 1, text);
      }
    }
  }
  




  private void graphTree(StringBuffer text)
    throws Exception
  {
    for (int i = 0; i < m_sons.length; i++) {
      text.append("N" + m_id + "->" + "N" + m_sons[i].m_id + " [label=\"" + Utils.quote(m_localModel.rightSide(i, m_train).trim()) + "\"]\n");
      



      if (m_sons[i].m_isLeaf) {
        text.append("N" + m_sons[i].m_id + " [label=\"" + Utils.quote(m_localModel.dumpLabel(i, m_train)) + "\" " + "shape=box style=filled ");
        

        if ((m_train != null) && (m_train.numInstances() > 0)) {
          text.append("data =\n" + m_sons[i].m_train + "\n");
          text.append(",\n");
        }
        text.append("]\n");
      } else {
        text.append("N" + m_sons[i].m_id + " [label=\"" + Utils.quote(m_sons[i].m_localModel.leftSide(m_train)) + "\" ");
        

        if ((m_train != null) && (m_train.numInstances() > 0)) {
          text.append("data =\n" + m_sons[i].m_train + "\n");
          text.append(",\n");
        }
        text.append("]\n");
        m_sons[i].graphTree(text);
      }
    }
  }
  





  private void prefixTree(StringBuffer text)
    throws Exception
  {
    text.append("[");
    text.append(m_localModel.leftSide(m_train) + ":");
    for (int i = 0; i < m_sons.length; i++) {
      if (i > 0) {
        text.append(",\n");
      }
      text.append(m_localModel.rightSide(i, m_train));
    }
    for (int i = 0; i < m_sons.length; i++) {
      if (m_sons[i].m_isLeaf) {
        text.append("[");
        text.append(m_localModel.dumpLabel(i, m_train));
        text.append("]");
      } else {
        m_sons[i].prefixTree(text);
      }
    }
    text.append("]");
  }
  










  private double getProbsLaplace(int classIndex, Instance instance, double weight)
    throws Exception
  {
    double prob = 0.0D;
    
    if (m_isLeaf) {
      return weight * localModel().classProbLaplace(classIndex, instance, -1);
    }
    int treeIndex = localModel().whichSubset(instance);
    if (treeIndex == -1) {
      double[] weights = localModel().weights(instance);
      for (int i = 0; i < m_sons.length; i++) {
        if (!sonm_isEmpty) {
          prob += son(i).getProbsLaplace(classIndex, instance, weights[i] * weight);
        }
      }
      
      return prob;
    }
    if (sonm_isEmpty) {
      return weight * localModel().classProbLaplace(classIndex, instance, treeIndex);
    }
    
    return son(treeIndex).getProbsLaplace(classIndex, instance, weight);
  }
  













  private double getProbs(int classIndex, Instance instance, double weight)
    throws Exception
  {
    double prob = 0.0D;
    
    if (m_isLeaf) {
      return weight * localModel().classProb(classIndex, instance, -1);
    }
    int treeIndex = localModel().whichSubset(instance);
    if (treeIndex == -1) {
      double[] weights = localModel().weights(instance);
      for (int i = 0; i < m_sons.length; i++) {
        if (!sonm_isEmpty) {
          prob += son(i).getProbs(classIndex, instance, weights[i] * weight);
        }
      }
      
      return prob;
    }
    if (sonm_isEmpty) {
      return weight * localModel().classProb(classIndex, instance, treeIndex);
    }
    
    return son(treeIndex).getProbs(classIndex, instance, weight);
  }
  






  private ClassifierSplitModel localModel()
  {
    return m_localModel;
  }
  



  private ClassifierTree son(int index)
  {
    return m_sons[index];
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10256 $");
  }
}

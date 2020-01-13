package weka.classifiers.rules.part;

import java.io.Serializable;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.Distribution;
import weka.classifiers.trees.j48.EntropySplitCrit;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.NoSplit;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;



































public class ClassifierDecList
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 7284358349711992497L;
  protected int m_minNumObj;
  protected static EntropySplitCrit m_splitCrit = new EntropySplitCrit();
  


  protected ModelSelection m_toSelectModel;
  

  protected ClassifierSplitModel m_localModel;
  

  protected ClassifierDecList[] m_sons;
  

  protected boolean m_isLeaf;
  

  protected boolean m_isEmpty;
  

  protected Instances m_train;
  

  protected Distribution m_test;
  

  protected int indeX;
  


  public ClassifierDecList(ModelSelection toSelectLocModel, int minNum)
  {
    m_toSelectModel = toSelectLocModel;
    m_minNumObj = minNum;
  }
  




  public void buildRule(Instances data)
    throws Exception
  {
    buildDecList(data, false);
    
    cleanup(new Instances(data, 0));
  }
  










  public void buildDecList(Instances data, boolean leaf)
    throws Exception
  {
    m_train = null;
    m_test = null;
    m_isLeaf = false;
    m_isEmpty = false;
    m_sons = null;
    indeX = 0;
    double sumOfWeights = data.sumOfWeights();
    NoSplit noSplit = new NoSplit(new Distribution(data));
    if (leaf) {
      m_localModel = noSplit;
    } else
      m_localModel = m_toSelectModel.selectModel(data);
    if (m_localModel.numSubsets() > 1) {
      Instances[] localInstances = m_localModel.split(data);
      data = null;
      m_sons = new ClassifierDecList[m_localModel.numSubsets()];
      int i = 0;
      int ind;
      do { i++;
        ind = chooseIndex();
        if (ind == -1) {
          for (int j = 0; j < m_sons.length; j++)
            if (m_sons[j] == null)
              m_sons[j] = getNewDecList(localInstances[j], true);
          if (i < 2) {
            m_localModel = noSplit;
            m_isLeaf = true;
            m_sons = null;
            if (Utils.eq(sumOfWeights, 0.0D))
              m_isEmpty = true;
            return;
          }
          ind = 0;
          break;
        }
        m_sons[ind] = getNewDecList(localInstances[ind], false);
      } while ((i < m_sons.length) && (m_sons[ind].m_isLeaf));
      

      indeX = chooseLastIndex();
    } else {
      m_isLeaf = true;
      if (Utils.eq(sumOfWeights, 0.0D)) {
        m_isEmpty = true;
      }
    }
  }
  




  public double classifyInstance(Instance instance)
    throws Exception
  {
    double maxProb = -1.0D;
    
    int maxIndex = 0;
    

    for (int j = 0; j < instance.numClasses(); 
        j++) {
      double currentProb = getProbs(j, instance, 1.0D);
      if (Utils.gr(currentProb, maxProb)) {
        maxIndex = j;
        maxProb = currentProb;
      }
    }
    if (Utils.eq(maxProb, 0.0D)) {
      return -1.0D;
    }
    return maxIndex;
  }
  






  public final double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] doubles = new double[instance.numClasses()];
    

    for (int i = 0; i < doubles.length; i++) {
      doubles[i] = getProbs(i, instance, 1.0D);
    }
    return doubles;
  }
  






  public double weight(Instance instance)
    throws Exception
  {
    if (m_isLeaf)
      return 1.0D;
    int subset = m_localModel.whichSubset(instance);
    if (subset == -1) {
      return m_localModel.weights(instance)[indeX] * m_sons[indeX].weight(instance);
    }
    if (subset == indeX)
      return m_sons[indeX].weight(instance);
    return 0.0D;
  }
  



  public final void cleanup(Instances justHeaderInfo)
  {
    m_train = justHeaderInfo;
    m_test = null;
    if (!m_isLeaf) {
      for (int i = 0; i < m_sons.length; i++) {
        if (m_sons[i] != null) {
          m_sons[i].cleanup(justHeaderInfo);
        }
      }
    }
  }
  

  public String toString()
  {
    try
    {
      StringBuffer text = new StringBuffer();
      if (m_isLeaf) {
        text.append(": ");
        text.append(m_localModel.dumpLabel(0, m_train) + "\n");
      } else {
        dumpDecList(text);
      }
      
      return text.toString();
    } catch (Exception e) {}
    return "Can't print rule.";
  }
  






  protected ClassifierDecList getNewDecList(Instances train, boolean leaf)
    throws Exception
  {
    ClassifierDecList newDecList = new ClassifierDecList(m_toSelectModel, m_minNumObj);
    
    newDecList.buildDecList(train, leaf);
    
    return newDecList;
  }
  



  public final int chooseIndex()
  {
    int minIndex = -1;
    double min = Double.MAX_VALUE;
    

    for (int i = 0; i < m_sons.length; i++) {
      if (son(i) == null) { double estimated;
        double estimated; if (Utils.sm(localModel().distribution().perBag(i), m_minNumObj))
        {
          estimated = Double.MAX_VALUE;
        } else {
          estimated = 0.0D;
          for (int j = 0; j < localModel().distribution().numClasses(); j++) {
            estimated -= m_splitCrit.logFunc(localModel().distribution().perClassPerBag(i, j));
          }
          estimated += m_splitCrit.logFunc(localModel().distribution().perBag(i));
          
          estimated /= localModel().distribution().perBag(i);
        }
        if (Utils.smOrEq(estimated, 0.0D))
          return i;
        if (Utils.sm(estimated, min)) {
          min = estimated;
          minIndex = i;
        }
      }
    }
    return minIndex;
  }
  



  public final int chooseLastIndex()
  {
    int minIndex = 0;
    double min = Double.MAX_VALUE;
    
    if (!m_isLeaf) {
      for (int i = 0; i < m_sons.length; i++) {
        if ((son(i) != null) && 
          (Utils.grOrEq(localModel().distribution().perBag(i), m_minNumObj)))
        {
          double estimated = son(i).getSizeOfBranch();
          if (Utils.sm(estimated, min)) {
            min = estimated;
            minIndex = i;
          }
        }
      }
    }
    return minIndex;
  }
  



  protected double getSizeOfBranch()
  {
    if (m_isLeaf) {
      return -localModel().distribution().total();
    }
    return son(indeX).getSizeOfBranch();
  }
  


  private void dumpDecList(StringBuffer text)
    throws Exception
  {
    text.append(m_localModel.leftSide(m_train));
    text.append(m_localModel.rightSide(indeX, m_train));
    if (m_sons[indeX].m_isLeaf) {
      text.append(": ");
      text.append(m_localModel.dumpLabel(indeX, m_train) + "\n");
    } else {
      text.append(" AND\n");
      m_sons[indeX].dumpDecList(text);
    }
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
      if (m_sons[i] == null) {
        text.append("null");
      } else if (m_sons[i].m_isLeaf) {
        text.append(": ");
        text.append(m_localModel.dumpLabel(i, m_train));
      } else {
        m_sons[i].dumpTree(depth + 1, text);
      }
    }
  }
  








  private double getProbs(int classIndex, Instance instance, double weight)
    throws Exception
  {
    if (m_isLeaf) {
      return weight * localModel().classProb(classIndex, instance, -1);
    }
    int treeIndex = localModel().whichSubset(instance);
    if (treeIndex == -1) {
      double[] weights = localModel().weights(instance);
      return son(indeX).getProbs(classIndex, instance, weights[indeX] * weight);
    }
    
    if (treeIndex == indeX) {
      return son(indeX).getProbs(classIndex, instance, weight);
    }
    return 0.0D;
  }
  






  protected ClassifierSplitModel localModel()
  {
    return m_localModel;
  }
  



  protected ClassifierDecList son(int index)
  {
    return m_sons[index];
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}

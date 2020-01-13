package weka.classifiers.trees.m5;

import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;































public class Rule
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -4458627451682483204L;
  protected static int LEFT = 0;
  protected static int RIGHT = 1;
  



  private Instances m_instances;
  



  private int m_classIndex;
  



  private int m_numAttributes;
  



  private int m_numInstances;
  



  private int[] m_splitAtts;
  



  private double[] m_splitVals;
  



  private RuleNode[] m_internalNodes;
  



  private int[] m_relOps;
  



  private RuleNode m_ruleModel;
  



  protected RuleNode m_topOfTree;
  



  private double m_globalStdDev;
  



  private double m_globalAbsDev;
  



  private Instances m_covered;
  



  private int m_numCovered;
  



  private Instances m_notCovered;
  



  private boolean m_useTree;
  



  private boolean m_smoothPredictions;
  



  private boolean m_saveInstances;
  



  private boolean m_regressionTree;
  



  private boolean m_useUnpruned;
  



  private double m_minNumInstances;
  



  public Rule()
  {
    m_useTree = false;
    m_smoothPredictions = false;
    m_useUnpruned = false;
    m_minNumInstances = 4.0D;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    m_instances = null;
    m_topOfTree = null;
    m_covered = null;
    m_notCovered = null;
    m_ruleModel = null;
    m_splitAtts = null;
    m_splitVals = null;
    m_relOps = null;
    m_internalNodes = null;
    m_instances = data;
    m_classIndex = m_instances.classIndex();
    m_numAttributes = m_instances.numAttributes();
    m_numInstances = m_instances.numInstances();
    

    m_globalStdDev = stdDev(m_classIndex, m_instances);
    m_globalAbsDev = absDev(m_classIndex, m_instances);
    
    m_topOfTree = new RuleNode(m_globalStdDev, m_globalAbsDev, null);
    m_topOfTree.setSaveInstances(m_saveInstances);
    m_topOfTree.setRegressionTree(m_regressionTree);
    m_topOfTree.setMinNumInstances(m_minNumInstances);
    m_topOfTree.buildClassifier(m_instances);
    

    if (!m_useUnpruned) {
      m_topOfTree.prune();
    } else {
      m_topOfTree.installLinearModels();
    }
    
    if (m_smoothPredictions) {
      m_topOfTree.installSmoothedModels();
    }
    
    m_topOfTree.numLeaves(0);
    
    if (!m_useTree) {
      makeRule();
    }
    



    m_instances = new Instances(m_instances, 0);
  }
  







  public double classifyInstance(Instance instance)
    throws Exception
  {
    if (m_useTree) {
      return m_topOfTree.classifyInstance(instance);
    }
    

    if (m_splitAtts.length > 0) {
      for (int i = 0; i < m_relOps.length; i++) {
        if (m_relOps[i] == LEFT)
        {
          if (instance.value(m_splitAtts[i]) > m_splitVals[i]) {
            throw new Exception("Rule does not classify instance");
          }
        }
        else if (instance.value(m_splitAtts[i]) <= m_splitVals[i]) {
          throw new Exception("Rule does not classify instance");
        }
      }
    }
    


    return m_ruleModel.classifyInstance(instance);
  }
  



  public RuleNode topOfTree()
  {
    return m_topOfTree;
  }
  



  private void makeRule()
    throws Exception
  {
    RuleNode[] best_leaf = new RuleNode[1];
    double[] best_cov = new double[1];
    

    m_notCovered = new Instances(m_instances, 0);
    m_covered = new Instances(m_instances, 0);
    best_cov[0] = -1.0D;
    best_leaf[0] = null;
    
    m_topOfTree.findBestLeaf(best_cov, best_leaf);
    
    RuleNode temp = best_leaf[0];
    
    if (temp == null) {
      throw new Exception("Unable to generate rule!");
    }
    

    m_ruleModel = temp;
    
    int count = 0;
    
    while (temp.parentNode() != null) {
      count++;
      temp = temp.parentNode();
    }
    
    temp = best_leaf[0];
    m_relOps = new int[count];
    m_splitAtts = new int[count];
    m_splitVals = new double[count];
    if (m_smoothPredictions) {
      m_internalNodes = new RuleNode[count];
    }
    

    int i = 0;
    
    while (temp.parentNode() != null) {
      m_splitAtts[i] = temp.parentNode().splitAtt();
      m_splitVals[i] = temp.parentNode().splitVal();
      
      if (temp.parentNode().leftNode() == temp) {
        m_relOps[i] = LEFT;
        parentNodem_right = null;
      } else {
        m_relOps[i] = RIGHT;
        parentNodem_left = null;
      }
      
      if (m_smoothPredictions) {
        m_internalNodes[i] = temp.parentNode();
      }
      
      temp = temp.parentNode();
      i++;
    }
    



    for (i = 0; i < m_numInstances; i++) {
      boolean ok = true;
      
      for (int j = 0; j < m_relOps.length; j++) {
        if (m_relOps[j] == LEFT)
        {
          if (m_instances.instance(i).value(m_splitAtts[j]) > m_splitVals[j])
          {
            m_notCovered.add(m_instances.instance(i));
            ok = false;
            break;
          }
        }
        else if (m_instances.instance(i).value(m_splitAtts[j]) <= m_splitVals[j])
        {
          m_notCovered.add(m_instances.instance(i));
          ok = false;
          break;
        }
      }
      

      if (ok) {
        m_numCovered += 1;
      }
    }
  }
  





  public String toString()
  {
    if (m_useTree) {
      return treeToString();
    }
    return ruleToString();
  }
  





  private String treeToString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_topOfTree == null) {
      return "Tree/Rule has not been built yet!";
    }
    
    text.append("M5 " + (m_useUnpruned ? "unpruned " : "pruned ") + (m_regressionTree ? "regression " : "model ") + "tree:\n");
    







    if (m_smoothPredictions == true) {
      text.append("(using smoothed linear models)\n");
    }
    
    text.append(m_topOfTree.treeToString(0));
    text.append(m_topOfTree.printLeafModels());
    text.append("\nNumber of Rules : " + m_topOfTree.numberOfLinearModels());
    
    return text.toString();
  }
  




  private String ruleToString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_splitAtts.length > 0) {
      text.append("IF\n");
      
      for (int i = m_splitAtts.length - 1; i >= 0; i--) {
        text.append("\t" + m_covered.attribute(m_splitAtts[i]).name() + " ");
        
        if (m_relOps[i] == 0) {
          text.append("<= ");
        } else {
          text.append("> ");
        }
        
        text.append(Utils.doubleToString(m_splitVals[i], 1, 3) + "\n");
      }
      
      text.append("THEN\n");
    }
    
    if (m_ruleModel != null) {
      try {
        text.append(m_ruleModel.printNodeLinearModel());
        text.append(" [" + m_numCovered);
        
        if (m_globalAbsDev > 0.0D) {
          text.append("/" + Utils.doubleToString(100.0D * m_ruleModel.rootMeanSquaredError() / m_globalStdDev, 1, 3) + "%]\n\n");

        }
        else
        {

          text.append("]\n\n");
        }
      } catch (Exception e) {
        return "Can't print rule";
      }
    }
    

    return text.toString();
  }
  




  public void setUnpruned(boolean unpruned)
  {
    m_useUnpruned = unpruned;
  }
  




  public boolean getUnpruned()
  {
    return m_useUnpruned;
  }
  




  public void setUseTree(boolean u)
  {
    m_useTree = u;
  }
  




  public boolean getUseTree()
  {
    return m_useTree;
  }
  




  public void setSmoothing(boolean s)
  {
    m_smoothPredictions = s;
  }
  




  public boolean getSmoothing()
  {
    return m_smoothPredictions;
  }
  




  public Instances notCoveredInstances()
  {
    return m_notCovered;
  }
  



  public void freeNotCoveredInstances()
  {
    m_notCovered = null;
  }
  















  protected static final double stdDev(int attr, Instances inst)
  {
    int count = 0;
    double sum = 0.0D;double sqrSum = 0.0D;
    
    for (int i = 0; i <= inst.numInstances() - 1; i++) {
      count++;
      double value = inst.instance(i).value(attr);
      sum += value;
      sqrSum += value * value; }
    double sd;
    double sd;
    if (count > 1) {
      double va = (sqrSum - sum * sum / count) / count;
      va = Math.abs(va);
      sd = Math.sqrt(va);
    } else {
      sd = 0.0D;
    }
    
    return sd;
  }
  







  protected static final double absDev(int attr, Instances inst)
  {
    double average = 0.0D;double absdiff = 0.0D;
    
    for (int i = 0; i <= inst.numInstances() - 1; i++)
      average += inst.instance(i).value(attr);
    double absDev;
    double absDev; if (inst.numInstances() > 1) {
      average /= inst.numInstances();
      for (i = 0; i <= inst.numInstances() - 1; i++) {
        absdiff += Math.abs(inst.instance(i).value(attr) - average);
      }
      absDev = absdiff / inst.numInstances();
    } else {
      absDev = 0.0D;
    }
    
    return absDev;
  }
  





  protected void setSaveInstances(boolean save)
  {
    m_saveInstances = save;
  }
  





  public boolean getRegressionTree()
  {
    return m_regressionTree;
  }
  





  public void setRegressionTree(boolean newregressionTree)
  {
    m_regressionTree = newregressionTree;
  }
  




  public void setMinNumInstances(double minNum)
  {
    m_minNumInstances = minNum;
  }
  




  public double getMinNumInstances()
  {
    return m_minNumInstances;
  }
  
  public RuleNode getM5RootNode() {
    return m_topOfTree;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6260 $");
  }
}

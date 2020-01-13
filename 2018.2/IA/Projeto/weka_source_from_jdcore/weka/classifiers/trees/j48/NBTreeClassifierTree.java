package weka.classifiers.trees.j48;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
































public class NBTreeClassifierTree
  extends ClassifierTree
{
  private static final long serialVersionUID = -4472639447877404786L;
  
  public NBTreeClassifierTree(ModelSelection toSelectLocModel)
  {
    super(toSelectLocModel);
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
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  



  public void buildClassifier(Instances data)
    throws Exception
  {
    super.buildClassifier(data);
    cleanup(new Instances(data, 0));
    assignIDs(-1);
  }
  





















  protected ClassifierTree getNewTree(Instances data)
    throws Exception
  {
    ClassifierTree newTree = new NBTreeClassifierTree(m_toSelectModel);
    newTree.buildTree(data, false);
    
    return newTree;
  }
  







  protected ClassifierTree getNewTree(Instances train, Instances test)
    throws Exception
  {
    ClassifierTree newTree = new NBTreeClassifierTree(m_toSelectModel);
    newTree.buildTree(train, test, false);
    
    return newTree;
  }
  




  public String printLeafModels()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_isLeaf) {
      text.append("\nLeaf number: " + m_id + " ");
      text.append(m_localModel.toString());
      text.append("\n");
    } else {
      for (int i = 0; i < m_sons.length; i++) {
        text.append(((NBTreeClassifierTree)m_sons[i]).printLeafModels());
      }
    }
    return text.toString();
  }
  


  public String toString()
  {
    try
    {
      StringBuffer text = new StringBuffer();
      
      if (m_isLeaf) {
        text.append(": NB");
        text.append(m_id);
      } else {
        dumpTreeNB(0, text);
      }
      text.append("\n" + printLeafModels());
      text.append("\n\nNumber of Leaves  : \t" + numLeaves() + "\n");
      text.append("\nSize of the tree : \t" + numNodes() + "\n");
      
      return text.toString();
    } catch (Exception e) {
      e.printStackTrace(); }
    return "Can't print nb tree.";
  }
  








  private void dumpTreeNB(int depth, StringBuffer text)
    throws Exception
  {
    for (int i = 0; i < m_sons.length; i++) {
      text.append("\n");
      for (int j = 0; j < depth; j++)
        text.append("|   ");
      text.append(m_localModel.leftSide(m_train));
      text.append(m_localModel.rightSide(i, m_train));
      if (m_sons[i].m_isLeaf) {
        text.append(": NB ");
        text.append(m_sons[i].m_id);
      } else {
        ((NBTreeClassifierTree)m_sons[i]).dumpTreeNB(depth + 1, text);
      }
    }
  }
  



  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    text.append("digraph J48Tree {\n");
    if (m_isLeaf) {
      text.append("N" + m_id + " [label=\"" + "NB model" + "\" " + "shape=box style=filled ");
      


      if ((m_train != null) && (m_train.numInstances() > 0)) {
        text.append("data =\n" + m_train + "\n");
        text.append(",\n");
      }
      
      text.append("]\n");
    } else {
      text.append("N" + m_id + " [label=\"" + Utils.backQuoteChars(m_localModel.leftSide(m_train)) + "\" ");
      

      if ((m_train != null) && (m_train.numInstances() > 0)) {
        text.append("data =\n" + m_train + "\n");
        text.append(",\n");
      }
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
        text.append("N" + m_sons[i].m_id + " [label=\"" + "NB Model" + "\" " + "shape=box style=filled ");
        

        if ((m_train != null) && (m_train.numInstances() > 0)) {
          text.append("data =\n" + m_sons[i].m_train + "\n");
          text.append(",\n");
        }
        text.append("]\n");
      } else {
        text.append("N" + m_sons[i].m_id + " [label=\"" + Utils.backQuoteChars(m_sons[i].m_localModel.leftSide(m_train)) + "\" ");
        

        if ((m_train != null) && (m_train.numInstances() > 0)) {
          text.append("data =\n" + m_sons[i].m_train + "\n");
          text.append(",\n");
        }
        text.append("]\n");
        ((NBTreeClassifierTree)m_sons[i]).graphTree(text);
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10286 $");
  }
}

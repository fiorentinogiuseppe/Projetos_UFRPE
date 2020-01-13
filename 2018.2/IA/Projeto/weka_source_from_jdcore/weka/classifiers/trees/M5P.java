package weka.classifiers.trees;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.trees.m5.M5Base;
import weka.classifiers.trees.m5.Rule;
import weka.classifiers.trees.m5.RuleNode;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
























































































public class M5P
  extends M5Base
  implements Drawable
{
  static final long serialVersionUID = -6118439039768244417L;
  
  public M5P()
  {
    setGenerateRules(false);
  }
  




  public int graphType()
  {
    return 1;
  }
  




  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    text.append("digraph M5Tree {\n");
    Rule temp = (Rule)m_ruleSet.elementAt(0);
    temp.topOfTree().graph(text);
    text.append("}\n");
    return text.toString();
  }
  





  public String saveInstancesTipText()
  {
    return "Whether to save instance data at each node in the tree for visualization purposes.";
  }
  







  public void setSaveInstances(boolean save)
  {
    m_saveInstances = save;
  }
  




  public boolean getSaveInstances()
  {
    return m_saveInstances;
  }
  




  public Enumeration listOptions()
  {
    Enumeration superOpts = super.listOptions();
    
    Vector newVector = new Vector();
    while (superOpts.hasMoreElements()) {
      newVector.addElement((Option)superOpts.nextElement());
    }
    
    newVector.addElement(new Option("\tSave instances at the nodes in\n\tthe tree (for visualization purposes)", "L", 0, "-L"));
    

    return newVector.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    setSaveInstances(Utils.getFlag('L', options));
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOpts = super.getOptions();
    String[] options = new String[superOpts.length + 1];
    int current = superOpts.length;
    for (int i = 0; i < current; i++) {
      options[i] = superOpts[i];
    }
    
    if (getSaveInstances()) {
      options[(current++)] = "-L";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new M5P(), args);
  }
}

package weka.classifiers.trees;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.trees.j48.NBTreeClassifierTree;
import weka.classifiers.trees.j48.NBTreeModelSelection;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Summarizable;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.WeightedInstancesHandler;
































































public class NBTree
  extends Classifier
  implements WeightedInstancesHandler, Drawable, Summarizable, AdditionalMeasureProducer, TechnicalInformationHandler
{
  static final long serialVersionUID = -4716005707058256086L;
  private int m_minNumObj = 30;
  

  private NBTreeClassifierTree m_root;
  

  public NBTree() {}
  

  public String globalInfo()
  {
    return "Class for generating a decision tree with naive Bayes classifiers at the leaves.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ron Kohavi");
    result.setValue(TechnicalInformation.Field.TITLE, "Scaling Up the Accuracy of Naive-Bayes Classifiers: A Decision-Tree Hybrid");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Second International Conference on Knoledge Discovery and Data Mining");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.PAGES, "202-207");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    return new NBTreeClassifierTree(null).getCapabilities();
  }
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    NBTreeModelSelection modSelection = new NBTreeModelSelection(m_minNumObj, instances);
    

    m_root = new NBTreeClassifierTree(modSelection);
    m_root.buildClassifier(instances);
  }
  






  public double classifyInstance(Instance instance)
    throws Exception
  {
    return m_root.classifyInstance(instance);
  }
  







  public final double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_root.distributionForInstance(instance, false);
  }
  





  public String toString()
  {
    if (m_root == null) {
      return "No classifier built";
    }
    return "NBTree\n------------------\n" + m_root.toString();
  }
  




  public int graphType()
  {
    return 1;
  }
  





  public String graph()
    throws Exception
  {
    return m_root.graph();
  }
  





  public String toSummaryString()
  {
    return "Number of leaves: " + m_root.numLeaves() + "\n" + "Size of the tree: " + m_root.numNodes() + "\n";
  }
  




  public double measureTreeSize()
  {
    return m_root.numNodes();
  }
  



  public double measureNumLeaves()
  {
    return m_root.numLeaves();
  }
  



  public double measureNumRules()
  {
    return m_root.numLeaves();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0)
      return measureNumRules();
    if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0)
      return measureTreeSize();
    if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0) {
      return measureNumLeaves();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (j48)");
  }
  





  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(3);
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    newVector.addElement("measureNumRules");
    return newVector.elements();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new NBTree(), argv);
  }
}

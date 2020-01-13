package weka.classifiers.trees;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.classifiers.trees.j48.BinC45ModelSelection;
import weka.classifiers.trees.j48.C45ModelSelection;
import weka.classifiers.trees.j48.C45PruneableClassifierTreeG;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.classifiers.trees.j48.ModelSelection;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Matchable;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Summarizable;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
























































































public class J48graft
  extends Classifier
  implements OptionHandler, Drawable, Matchable, Sourcable, WeightedInstancesHandler, Summarizable, AdditionalMeasureProducer, TechnicalInformationHandler
{
  static final long serialVersionUID = 8823716098042427799L;
  private ClassifierTree m_root;
  private boolean m_unpruned = false;
  

  private float m_CF = 0.25F;
  

  private int m_minNumObj = 2;
  


  private boolean m_useLaplace = false;
  

  private int m_numFolds = 3;
  

  private boolean m_binarySplits = false;
  

  private boolean m_subtreeRaising = true;
  

  private boolean m_noCleanup = false;
  

  private boolean m_relabel = false;
  

  public J48graft() {}
  

  public String globalInfo()
  {
    return "Class for generating a grafted (pruned or unpruned) C4.5 decision tree. For more information, see\n\n" + getTechnicalInformation().toString();
  }
  










  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Geoff Webb");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.TITLE, "Decision Tree Grafting From the All-Tests-But-One Partition");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    result.setValue(TechnicalInformation.Field.ADDRESS, "San Francisco, CA");
    
    return result;
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    

    try
    {
      result = new C45PruneableClassifierTreeG(null, !m_unpruned, m_CF, m_subtreeRaising, m_relabel, !m_noCleanup).getCapabilities();
    }
    catch (Exception e) {
      result = new Capabilities(this);
      result.disableAll();
    }
    
    result.setOwner(this);
    
    return result;
  }
  


  public void buildClassifier(Instances instances)
    throws Exception
  {
    ModelSelection modSelection;
    

    ModelSelection modSelection;
    

    if (m_binarySplits) {
      modSelection = new BinC45ModelSelection(m_minNumObj, instances);
    } else
      modSelection = new C45ModelSelection(m_minNumObj, instances);
    m_root = new C45PruneableClassifierTreeG(modSelection, !m_unpruned, m_CF, m_subtreeRaising, m_relabel, !m_noCleanup);
    

    m_root.buildClassifier(instances);
    
    if (m_binarySplits) {
      ((BinC45ModelSelection)modSelection).cleanup();
    } else {
      ((C45ModelSelection)modSelection).cleanup();
    }
  }
  






  public double classifyInstance(Instance instance)
    throws Exception
  {
    return m_root.classifyInstance(instance);
  }
  







  public final double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_root.distributionForInstance(instance, m_useLaplace);
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
  





  public String prefix()
    throws Exception
  {
    return m_root.prefix();
  }
  







  public String toSource(String className)
    throws Exception
  {
    StringBuffer[] source = m_root.toSource(className);
    return "class " + className + " {\n\n" + "  public static double classify(Object [] i)\n" + "    throws Exception {\n\n" + "    double p = Double.NaN;\n" + source[0] + "    return p;\n" + "  }\n" + source[1] + "}\n";
  }
  









































  public Enumeration listOptions()
  {
    Vector newVector = new Vector(9);
    
    newVector.addElement(new Option("\tUse unpruned tree.", "U", 0, "-U"));
    

    newVector.addElement(new Option("\tSet confidence threshold for pruning.\n\t(default 0.25)", "C", 1, "-C <pruning confidence>"));
    


    newVector.addElement(new Option("\tSet minimum number of instances per leaf.\n\t(default 2)", "M", 1, "-M <minimum number of instances>"));
    


    newVector.addElement(new Option("\tUse binary splits only.", "B", 0, "-B"));
    

    newVector.addElement(new Option("\tDon't perform subtree raising.", "S", 0, "-S"));
    

    newVector.addElement(new Option("\tDo not clean up after the tree has been built.", "L", 0, "-L"));
    

    newVector.addElement(new Option("\tLaplace smoothing for predicted probabilities.  (note: this option only affects initial tree; grafting process always uses laplace).", "A", 0, "-A"));
    


    newVector.addElement(new Option("\tRelabel when grafting.", "E", 0, "-E"));
    

    return newVector.elements();
  }
  





































  public void setOptions(String[] options)
    throws Exception
  {
    String minNumString = Utils.getOption('M', options);
    if (minNumString.length() != 0) {
      m_minNumObj = Integer.parseInt(minNumString);
    } else {
      m_minNumObj = 2;
    }
    m_binarySplits = Utils.getFlag('B', options);
    m_useLaplace = Utils.getFlag('A', options);
    

    m_unpruned = Utils.getFlag('U', options);
    m_subtreeRaising = (!Utils.getFlag('S', options));
    m_noCleanup = Utils.getFlag('L', options);
    if ((m_unpruned) && (!m_subtreeRaising)) {
      throw new Exception("Subtree raising doesn't need to be unset for unpruned tree!");
    }
    m_relabel = Utils.getFlag('E', options);
    String confidenceString = Utils.getOption('C', options);
    if (confidenceString.length() != 0) {
      if (m_unpruned) {
        throw new Exception("Doesn't make sense to change confidence for unpruned tree!");
      }
      
      m_CF = new Float(confidenceString).floatValue();
      if ((m_CF <= 0.0F) || (m_CF >= 1.0F)) {
        throw new Exception("Confidence has to be greater than zero and smaller than one!");
      }
    }
    else
    {
      m_CF = 0.25F;
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[10];
    int current = 0;
    
    if (m_noCleanup) {
      options[(current++)] = "-L";
    }
    if (m_unpruned) {
      options[(current++)] = "-U";
    } else {
      if (!m_subtreeRaising) {
        options[(current++)] = "-S";
      }
      options[(current++)] = "-C";options[(current++)] = ("" + m_CF);
    }
    if (m_binarySplits) {
      options[(current++)] = "-B";
    }
    options[(current++)] = "-M";options[(current++)] = ("" + m_minNumObj);
    if (m_useLaplace) {
      options[(current++)] = "-A";
    }
    
    if (m_relabel) {
      options[(current++)] = "-E";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String useLaplaceTipText()
  {
    return "Whether counts at leaves are smoothed based on Laplace.";
  }
  





  public boolean getUseLaplace()
  {
    return m_useLaplace;
  }
  





  public void setUseLaplace(boolean newuseLaplace)
  {
    m_useLaplace = newuseLaplace;
  }
  





  public String toString()
  {
    if (m_root == null) {
      return "No classifier built";
    }
    if (m_unpruned) {
      return "J48graft unpruned tree\n------------------\n" + m_root.toString();
    }
    return "J48graft pruned tree\n------------------\n" + m_root.toString();
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
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(3);
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    newVector.addElement("measureNumRules");
    return newVector.elements();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareTo("measureNumRules") == 0)
      return measureNumRules();
    if (additionalMeasureName.compareTo("measureTreeSize") == 0)
      return measureTreeSize();
    if (additionalMeasureName.compareTo("measureNumLeaves") == 0) {
      return measureNumLeaves();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (j48)");
  }
  






  public String unprunedTipText()
  {
    return "Whether pruning is performed.";
  }
  





  public boolean getUnpruned()
  {
    return m_unpruned;
  }
  




  public void setUnpruned(boolean v)
  {
    m_unpruned = v;
  }
  




  public String relabelTipText()
  {
    return "Whether relabelling is allowed during grafting.";
  }
  





  public boolean getRelabel()
  {
    return m_relabel;
  }
  




  public void setRelabel(boolean v)
  {
    m_relabel = v;
  }
  




  public String confidenceFactorTipText()
  {
    return "The confidence factor used for pruning (smaller values incur more pruning).";
  }
  






  public float getConfidenceFactor()
  {
    return m_CF;
  }
  





  public void setConfidenceFactor(float v)
  {
    m_CF = v;
  }
  




  public String minNumObjTipText()
  {
    return "The minimum number of instances per leaf.";
  }
  





  public int getMinNumObj()
  {
    return m_minNumObj;
  }
  





  public void setMinNumObj(int v)
  {
    m_minNumObj = v;
  }
  




  public String binarySplitsTipText()
  {
    return "Whether to use binary splits on nominal attributes when building the trees.";
  }
  






  public boolean getBinarySplits()
  {
    return m_binarySplits;
  }
  





  public void setBinarySplits(boolean v)
  {
    m_binarySplits = v;
  }
  




  public String subtreeRaisingTipText()
  {
    return "Whether to consider the subtree raising operation when pruning.";
  }
  





  public boolean getSubtreeRaising()
  {
    return m_subtreeRaising;
  }
  





  public void setSubtreeRaising(boolean v)
  {
    m_subtreeRaising = v;
  }
  




  public String saveInstanceDataTipText()
  {
    return "Whether to save the training data for visualization.";
  }
  





  public boolean getSaveInstanceData()
  {
    return m_noCleanup;
  }
  




  public void setSaveInstanceData(boolean v)
  {
    m_noCleanup = v;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5535 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new J48graft(), argv);
  }
}

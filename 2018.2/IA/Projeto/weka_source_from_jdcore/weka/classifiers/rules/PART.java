package weka.classifiers.rules;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.part.MakeDecList;
import weka.classifiers.trees.j48.BinC45ModelSelection;
import weka.classifiers.trees.j48.C45ModelSelection;
import weka.classifiers.trees.j48.ModelSelection;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
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


























































































public class PART
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, Summarizable, AdditionalMeasureProducer, TechnicalInformationHandler
{
  static final long serialVersionUID = 8121455039782598361L;
  private MakeDecList m_root;
  private float m_CF = 0.25F;
  

  private int m_minNumObj = 2;
  

  private boolean m_reducedErrorPruning = false;
  

  private int m_numFolds = 3;
  

  private boolean m_binarySplits = false;
  

  private boolean m_unpruned = false;
  

  private int m_Seed = 1;
  


  public PART() {}
  

  public String globalInfo()
  {
    return "Class for generating a PART decision list. Uses separate-and-conquer. Builds a partial C4.5 decision tree in each iteration and makes the \"best\" leaf into a rule.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Eibe Frank and Ian H. Witten");
    result.setValue(TechnicalInformation.Field.TITLE, "Generating Accurate Rule Sets Without Global Optimization");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Fifteenth International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.EDITOR, "J. Shavlik");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.PAGES, "144-151");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    result.setValue(TechnicalInformation.Field.PS, "http://www.cs.waikato.ac.nz/~eibe/pubs/ML98-57.ps.gz");
    
    return result;
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    Capabilities result;
    
    if (m_unpruned) {
      result = new MakeDecList(null, m_minNumObj).getCapabilities(); } else { Capabilities result;
      if (m_reducedErrorPruning) {
        result = new MakeDecList(null, m_numFolds, m_minNumObj, m_Seed).getCapabilities();
      } else
        result = new MakeDecList(null, m_CF, m_minNumObj).getCapabilities();
    }
    return result;
  }
  







  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    ModelSelection modSelection;
    ModelSelection modSelection;
    if (m_binarySplits) {
      modSelection = new BinC45ModelSelection(m_minNumObj, instances);
    } else
      modSelection = new C45ModelSelection(m_minNumObj, instances);
    if (m_unpruned) {
      m_root = new MakeDecList(modSelection, m_minNumObj);
    } else if (m_reducedErrorPruning) {
      m_root = new MakeDecList(modSelection, m_numFolds, m_minNumObj, m_Seed);
    } else
      m_root = new MakeDecList(modSelection, m_CF, m_minNumObj);
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
    return m_root.distributionForInstance(instance);
  }
  





























  public Enumeration listOptions()
  {
    Vector newVector = new Vector(7);
    
    newVector.addElement(new Option("\tSet confidence threshold for pruning.\n\t(default 0.25)", "C", 1, "-C <pruning confidence>"));
    


    newVector.addElement(new Option("\tSet minimum number of objects per leaf.\n\t(default 2)", "M", 1, "-M <minimum number of objects>"));
    


    newVector.addElement(new Option("\tUse reduced error pruning.", "R", 0, "-R"));
    

    newVector.addElement(new Option("\tSet number of folds for reduced error\n\tpruning. One fold is used as pruning set.\n\t(default 3)", "N", 1, "-N <number of folds>"));
    



    newVector.addElement(new Option("\tUse binary splits only.", "B", 0, "-B"));
    

    newVector.addElement(new Option("\tGenerate unpruned decision list.", "U", 0, "-U"));
    

    newVector.addElement(new Option("\tSeed for random data shuffling (default 1).", "Q", 1, "-Q <seed>"));
    


    return newVector.elements();
  }
  




































  public void setOptions(String[] options)
    throws Exception
  {
    m_unpruned = Utils.getFlag('U', options);
    m_reducedErrorPruning = Utils.getFlag('R', options);
    m_binarySplits = Utils.getFlag('B', options);
    String confidenceString = Utils.getOption('C', options);
    if (confidenceString.length() != 0) {
      if (m_reducedErrorPruning) {
        throw new Exception("Setting CF doesn't make sense for reduced error pruning.");
      }
      
      m_CF = new Float(confidenceString).floatValue();
      if ((m_CF <= 0.0F) || (m_CF >= 1.0F)) {
        throw new Exception("CF has to be greater than zero and smaller than one!");
      }
    }
    else {
      m_CF = 0.25F;
    }
    String numFoldsString = Utils.getOption('N', options);
    if (numFoldsString.length() != 0) {
      if (!m_reducedErrorPruning) {
        throw new Exception("Setting the number of folds does only make sense for reduced error pruning.");
      }
      

      m_numFolds = Integer.parseInt(numFoldsString);
    }
    else {
      m_numFolds = 3;
    }
    

    String minNumString = Utils.getOption('M', options);
    if (minNumString.length() != 0) {
      m_minNumObj = Integer.parseInt(minNumString);
    } else {
      m_minNumObj = 2;
    }
    String seedString = Utils.getOption('Q', options);
    if (seedString.length() != 0) {
      m_Seed = Integer.parseInt(seedString);
    } else {
      m_Seed = 1;
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[11];
    int current = 0;
    
    if (m_unpruned) {
      options[(current++)] = "-U";
    }
    if (m_reducedErrorPruning) {
      options[(current++)] = "-R";
    }
    if (m_binarySplits) {
      options[(current++)] = "-B";
    }
    options[(current++)] = "-M";options[(current++)] = ("" + m_minNumObj);
    if (!m_reducedErrorPruning) {
      options[(current++)] = "-C";options[(current++)] = ("" + m_CF);
    }
    if (m_reducedErrorPruning) {
      options[(current++)] = "-N";options[(current++)] = ("" + m_numFolds);
    }
    options[(current++)] = "-Q";options[(current++)] = ("" + m_Seed);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String toString()
  {
    if (m_root == null) {
      return "No classifier built";
    }
    return "PART decision list\n------------------\n\n" + m_root.toString();
  }
  





  public String toSummaryString()
  {
    return "Number of rules: " + m_root.numRules() + "\n";
  }
  



  public double measureNumRules()
  {
    return m_root.numRules();
  }
  



  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(1);
    newVector.addElement("measureNumRules");
    return newVector.elements();
  }
  





  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0) {
      return measureNumRules();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (PART)");
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
    return "The minimum number of instances per rule.";
  }
  





  public int getMinNumObj()
  {
    return m_minNumObj;
  }
  





  public void setMinNumObj(int v)
  {
    m_minNumObj = v;
  }
  




  public String reducedErrorPruningTipText()
  {
    return "Whether reduced-error pruning is used instead of C.4.5 pruning.";
  }
  





  public boolean getReducedErrorPruning()
  {
    return m_reducedErrorPruning;
  }
  





  public void setReducedErrorPruning(boolean v)
  {
    m_reducedErrorPruning = v;
  }
  




  public String unprunedTipText()
  {
    return "Whether pruning is performed.";
  }
  





  public boolean getUnpruned()
  {
    return m_unpruned;
  }
  





  public void setUnpruned(boolean newunpruned)
  {
    m_unpruned = newunpruned;
  }
  




  public String numFoldsTipText()
  {
    return "Determines the amount of data used for reduced-error pruning.  One fold is used for pruning, the rest for growing the rules.";
  }
  






  public int getNumFolds()
  {
    return m_numFolds;
  }
  





  public void setNumFolds(int v)
  {
    m_numFolds = v;
  }
  




  public String seedTipText()
  {
    return "The seed used for randomizing the data when reduced-error pruning is used.";
  }
  






  public int getSeed()
  {
    return m_Seed;
  }
  





  public void setSeed(int newSeed)
  {
    m_Seed = newSeed;
  }
  




  public String binarySplitsTipText()
  {
    return "Whether to use binary splits on nominal attributes when building the partial trees.";
  }
  






  public boolean getBinarySplits()
  {
    return m_binarySplits;
  }
  





  public void setBinarySplits(boolean v)
  {
    m_binarySplits = v;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new PART(), argv);
  }
}

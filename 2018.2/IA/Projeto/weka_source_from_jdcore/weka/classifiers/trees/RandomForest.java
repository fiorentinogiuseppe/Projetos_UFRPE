package weka.classifiers.trees;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;



























































































public class RandomForest
  extends Classifier
  implements OptionHandler, Randomizable, WeightedInstancesHandler, AdditionalMeasureProducer, TechnicalInformationHandler
{
  private static final long serialVersionUID = -2260823972777004705L;
  protected int m_numTrees = 100;
  




  protected int m_numFeatures = 0;
  

  protected int m_randomSeed = 1;
  

  protected int m_KValue = 0;
  

  protected Bagging m_bagger = null;
  

  protected int m_MaxDepth = 0;
  


  public RandomForest() {}
  


  public String globalInfo()
  {
    return "Class for constructing a forest of random trees.\n\nFor more information see: \n\n" + getTechnicalInformation().toString();
  }
  










  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Leo Breiman");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.TITLE, "Random Forests");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "45");
    result.setValue(TechnicalInformation.Field.NUMBER, "1");
    result.setValue(TechnicalInformation.Field.PAGES, "5-32");
    
    return result;
  }
  





  public String numTreesTipText()
  {
    return "The number of trees to be generated.";
  }
  





  public int getNumTrees()
  {
    return m_numTrees;
  }
  





  public void setNumTrees(int newNumTrees)
  {
    m_numTrees = newNumTrees;
  }
  





  public String numFeaturesTipText()
  {
    return "The number of attributes to be used in random selection (see RandomTree).";
  }
  





  public int getNumFeatures()
  {
    return m_numFeatures;
  }
  





  public void setNumFeatures(int newNumFeatures)
  {
    m_numFeatures = newNumFeatures;
  }
  





  public String seedTipText()
  {
    return "The random number seed to be used.";
  }
  





  public void setSeed(int seed)
  {
    m_randomSeed = seed;
  }
  





  public int getSeed()
  {
    return m_randomSeed;
  }
  





  public String maxDepthTipText()
  {
    return "The maximum depth of the trees, 0 for unlimited.";
  }
  




  public int getMaxDepth()
  {
    return m_MaxDepth;
  }
  




  public void setMaxDepth(int value)
  {
    m_MaxDepth = value;
  }
  





  public double measureOutOfBagError()
  {
    if (m_bagger != null) {
      return m_bagger.measureOutOfBagError();
    }
    return NaN.0D;
  }
  





  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(1);
    newVector.addElement("measureOutOfBagError");
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.equalsIgnoreCase("measureOutOfBagError")) {
      return measureOutOfBagError();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (RandomForest)");
  }
  








  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tNumber of trees to build.\n\t(default 100)", "I", 1, "-I <number of trees>"));
    

    newVector.addElement(new Option("\tNumber of features to consider (<1=int(log_2(#predictors)+1)).\n\t(default 0)", "K", 1, "-K <number of features>"));
    


    newVector.addElement(new Option("\tSeed for random number generator.\n\t(default 1)", "S", 1, "-S"));
    

    newVector.addElement(new Option("\tThe maximum depth of the trees, 0 for unlimited.\n\t(default 0)", "depth", 1, "-depth <num>"));
    


    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-I");
    result.add("" + getNumTrees());
    
    result.add("-K");
    result.add("" + getNumFeatures());
    
    result.add("-S");
    result.add("" + getSeed());
    
    if (getMaxDepth() > 0) {
      result.add("-depth");
      result.add("" + getMaxDepth());
    }
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  










































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('I', options);
    if (tmpStr.length() != 0) {
      m_numTrees = Integer.parseInt(tmpStr);
    } else {
      m_numTrees = 100;
    }
    
    tmpStr = Utils.getOption('K', options);
    if (tmpStr.length() != 0) {
      m_numFeatures = Integer.parseInt(tmpStr);
    } else {
      m_numFeatures = 0;
    }
    
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(1);
    }
    
    tmpStr = Utils.getOption("depth", options);
    if (tmpStr.length() != 0) {
      setMaxDepth(Integer.parseInt(tmpStr));
    } else {
      setMaxDepth(0);
    }
    
    super.setOptions(options);
    
    Utils.checkForRemainingOptions(options);
  }
  





  public Capabilities getCapabilities()
  {
    return new RandomTree().getCapabilities();
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    m_bagger = new Bagging();
    RandomTree rTree = new RandomTree();
    

    m_KValue = m_numFeatures;
    if (m_KValue < 1)
      m_KValue = ((int)Utils.log2(data.numAttributes() - 1) + 1);
    rTree.setKValue(m_KValue);
    rTree.setMaxDepth(getMaxDepth());
    

    m_bagger.setClassifier(rTree);
    m_bagger.setSeed(m_randomSeed);
    m_bagger.setNumIterations(m_numTrees);
    m_bagger.setCalcOutOfBag(true);
    m_bagger.buildClassifier(data);
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_bagger.distributionForInstance(instance);
  }
  






  public String toString()
  {
    if (m_bagger == null) {
      return "Random forest not built yet";
    }
    return "Random forest of " + m_numTrees + " trees, each constructed while considering " + m_KValue + " random feature" + (m_KValue == 1 ? "" : "s") + ".\n" + "Out of bag error: " + Utils.doubleToString(m_bagger.measureOutOfBagError(), 4) + "\n" + (getMaxDepth() > 0 ? "Max. depth of trees: " + getMaxDepth() + "\n" : "") + "\n";
  }
  
















  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new RandomForest(), argv);
  }
}

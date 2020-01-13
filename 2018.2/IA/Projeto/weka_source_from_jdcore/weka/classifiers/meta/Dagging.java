package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.functions.SMO;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

























































































































































public class Dagging
  extends RandomizableSingleClassifierEnhancer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 4560165876570074309L;
  protected int m_NumFolds = 10;
  

  protected Vote m_Vote = null;
  

  protected boolean m_Verbose = false;
  




  public String globalInfo()
  {
    return "This meta classifier creates a number of disjoint, stratified folds out of the data and feeds each chunk of data to a copy of the supplied base classifier. Predictions are made via averaging, since all the generated base classifiers are put into the Vote meta classifier. \nUseful for base classifiers that are quadratic or worse in time behavior, regarding number of instances in the training data. \n\nFor more information, see: \n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ting, K. M. and Witten, I. H.");
    result.setValue(TechnicalInformation.Field.TITLE, "Stacking Bagged and Dagged Models");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Fourteenth international Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.EDITOR, "D. H. Fisher");
    result.setValue(TechnicalInformation.Field.YEAR, "1997");
    result.setValue(TechnicalInformation.Field.PAGES, "367-375");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann Publishers");
    result.setValue(TechnicalInformation.Field.ADDRESS, "San Francisco, CA");
    
    return result;
  }
  


  public Dagging()
  {
    m_Classifier = new SMO();
  }
  




  protected String defaultClassifierString()
  {
    return SMO.class.getName();
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe number of folds for splitting the training set into\n\tsmaller chunks for the base classifier.\n\t(default 10)", "F", 1, "-F <folds>"));
    




    result.addElement(new Option("\tWhether to print some more information during building the\n\tclassifier.\n\t(default is off)", "verbose", 0, "-verbose"));
    




    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    return result.elements();
  }
  








































































































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('F', options);
    if (tmpStr.length() != 0) {
      setNumFolds(Integer.parseInt(tmpStr));
    } else {
      setNumFolds(10);
    }
    setVerbose(Utils.getFlag("verbose", options));
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-F");
    result.add("" + getNumFolds());
    
    if (getVerbose()) {
      result.add("-verbose");
    }
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public int getNumFolds()
  {
    return m_NumFolds;
  }
  




  public void setNumFolds(int value)
  {
    if (value > 0) {
      m_NumFolds = value;
    } else {
      System.out.println("At least 1 fold is necessary (provided: " + value + ")!");
    }
  }
  





  public String numFoldsTipText()
  {
    return "The number of folds to use for splitting the training set into smaller chunks for the base classifier.";
  }
  




  public void setVerbose(boolean value)
  {
    m_Verbose = value;
  }
  




  public boolean getVerbose()
  {
    return m_Verbose;
  }
  




  public String verboseTipText()
  {
    return "Whether to ouput some additional information during building.";
  }
  














  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    m_Vote = new Vote();
    Classifier[] base = new Classifier[getNumFolds()];
    double chunkSize = data.numInstances() / getNumFolds();
    

    if (getNumFolds() > 1) {
      data.randomize(data.getRandomNumberGenerator(getSeed()));
      data.stratify(getNumFolds());
    }
    

    for (int i = 0; i < getNumFolds(); i++) {
      base[i] = makeCopy(getClassifier());
      Instances train;
      Instances train;
      if (getNumFolds() > 1)
      {
        if (getVerbose()) {
          System.out.print(".");
        }
        train = data.testCV(getNumFolds(), i);
      }
      else {
        train = data;
      }
      

      base[i].buildClassifier(train);
    }
    

    m_Vote.setClassifiers(base);
    
    if (getVerbose()) {
      System.out.println();
    }
  }
  





  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_Vote.distributionForInstance(instance);
  }
  




  public String toString()
  {
    if (m_Vote == null) {
      return getClass().getName().replaceAll(".*\\.", "") + ": No model built yet.";
    }
    
    return m_Vote.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5306 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new Dagging(), args);
  }
}

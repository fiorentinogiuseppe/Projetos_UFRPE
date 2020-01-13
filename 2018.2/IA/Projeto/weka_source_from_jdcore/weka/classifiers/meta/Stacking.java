package weka.classifiers.meta;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

















































































public class Stacking
  extends RandomizableMultipleClassifiersCombiner
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 5134738557155845452L;
  protected Classifier m_MetaClassifier = new ZeroR();
  

  protected Instances m_MetaFormat = null;
  

  protected Instances m_BaseFormat = null;
  

  protected int m_NumFolds = 10;
  


  public Stacking() {}
  

  public String globalInfo()
  {
    return "Combines several classifiers using the stacking method. Can do classification or regression.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "David H. Wolpert");
    result.setValue(TechnicalInformation.Field.YEAR, "1992");
    result.setValue(TechnicalInformation.Field.TITLE, "Stacked generalization");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Neural Networks");
    result.setValue(TechnicalInformation.Field.VOLUME, "5");
    result.setValue(TechnicalInformation.Field.PAGES, "241-259");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Pergamon Press");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    newVector.addElement(new Option(metaOption(), "M", 0, "-M <scheme specification>"));
    

    newVector.addElement(new Option("\tSets the number of cross-validation folds.", "X", 1, "-X <number of folds>"));
    


    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  





  protected String metaOption()
  {
    return "\tFull name of meta classifier, followed by options.\n\t(default: \"weka.classifiers.rules.Zero\")";
  }
  































  public void setOptions(String[] options)
    throws Exception
  {
    String numFoldsString = Utils.getOption('X', options);
    if (numFoldsString.length() != 0) {
      setNumFolds(Integer.parseInt(numFoldsString));
    } else {
      setNumFolds(10);
    }
    processMetaOptions(options);
    super.setOptions(options);
  }
  





  protected void processMetaOptions(String[] options)
    throws Exception
  {
    String classifierString = Utils.getOption('M', options);
    String[] classifierSpec = Utils.splitOptions(classifierString);
    String classifierName;
    String classifierName; if (classifierSpec.length == 0) {
      classifierName = "weka.classifiers.rules.ZeroR";
    } else {
      classifierName = classifierSpec[0];
      classifierSpec[0] = "";
    }
    setMetaClassifier(Classifier.forName(classifierName, classifierSpec));
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 4];
    
    int current = 0;
    options[(current++)] = "-X";options[(current++)] = ("" + getNumFolds());
    options[(current++)] = "-M";
    options[(current++)] = (getMetaClassifier().getClass().getName() + " " + Utils.joinOptions(getMetaClassifier().getOptions()));
    

    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    return options;
  }
  




  public String numFoldsTipText()
  {
    return "The number of folds used for cross-validation.";
  }
  





  public int getNumFolds()
  {
    return m_NumFolds;
  }
  





  public void setNumFolds(int numFolds)
    throws Exception
  {
    if (numFolds < 0) {
      throw new IllegalArgumentException("Stacking: Number of cross-validation folds must be positive.");
    }
    
    m_NumFolds = numFolds;
  }
  




  public String metaClassifierTipText()
  {
    return "The meta classifiers to be used.";
  }
  





  public void setMetaClassifier(Classifier classifier)
  {
    m_MetaClassifier = classifier;
  }
  





  public Classifier getMetaClassifier()
  {
    return m_MetaClassifier;
  }
  







  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.setMinimumNumberInstances(getNumFolds());
    
    return result;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    if (m_MetaClassifier == null) {
      throw new IllegalArgumentException("No meta classifier has been set");
    }
    

    getCapabilities().testWithFail(data);
    

    Instances newData = new Instances(data);
    m_BaseFormat = new Instances(data, 0);
    newData.deleteWithMissingClass();
    
    Random random = new Random(m_Seed);
    newData.randomize(random);
    if (newData.classAttribute().isNominal()) {
      newData.stratify(m_NumFolds);
    }
    

    generateMetaLevel(newData, random);
    

    for (int i = 0; i < m_Classifiers.length; i++) {
      getClassifier(i).buildClassifier(newData);
    }
  }
  







  protected void generateMetaLevel(Instances newData, Random random)
    throws Exception
  {
    Instances metaData = metaFormat(newData);
    m_MetaFormat = new Instances(metaData, 0);
    for (int j = 0; j < m_NumFolds; j++) {
      Instances train = newData.trainCV(m_NumFolds, j, random);
      

      for (int i = 0; i < m_Classifiers.length; i++) {
        getClassifier(i).buildClassifier(train);
      }
      

      Instances test = newData.testCV(m_NumFolds, j);
      for (int i = 0; i < test.numInstances(); i++) {
        metaData.add(metaInstance(test.instance(i)));
      }
    }
    
    m_MetaClassifier.buildClassifier(metaData);
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return m_MetaClassifier.distributionForInstance(metaInstance(instance));
  }
  





  public String toString()
  {
    if (m_Classifiers.length == 0) {
      return "Stacking: No base schemes entered.";
    }
    if (m_MetaClassifier == null) {
      return "Stacking: No meta scheme selected.";
    }
    if (m_MetaFormat == null) {
      return "Stacking: No model built yet.";
    }
    String result = "Stacking\n\nBase classifiers\n\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result = result + getClassifier(i).toString() + "\n\n";
    }
    
    result = result + "\n\nMeta classifier\n\n";
    result = result + m_MetaClassifier.toString();
    
    return result;
  }
  






  protected Instances metaFormat(Instances instances)
    throws Exception
  {
    FastVector attributes = new FastVector();
    

    for (int k = 0; k < m_Classifiers.length; k++) {
      Classifier classifier = getClassifier(k);
      String name = classifier.getClass().getName() + "-" + (k + 1);
      if (m_BaseFormat.classAttribute().isNumeric()) {
        attributes.addElement(new Attribute(name));
      } else {
        for (int j = 0; j < m_BaseFormat.classAttribute().numValues(); j++) {
          attributes.addElement(new Attribute(name + ":" + m_BaseFormat.classAttribute().value(j)));
        }
      }
    }
    

    attributes.addElement(m_BaseFormat.classAttribute().copy());
    Instances metaFormat = new Instances("Meta format", attributes, 0);
    metaFormat.setClassIndex(metaFormat.numAttributes() - 1);
    return metaFormat;
  }
  






  protected Instance metaInstance(Instance instance)
    throws Exception
  {
    double[] values = new double[m_MetaFormat.numAttributes()];
    
    int i = 0;
    for (int k = 0; k < m_Classifiers.length; k++) {
      Classifier classifier = getClassifier(k);
      if (m_BaseFormat.classAttribute().isNumeric()) {
        values[(i++)] = classifier.classifyInstance(instance);
      } else {
        double[] dist = classifier.distributionForInstance(instance);
        for (int j = 0; j < dist.length; j++) {
          values[(i++)] = dist[j];
        }
      }
    }
    values[i] = instance.classValue();
    Instance metaInstance = new Instance(1.0D, values);
    metaInstance.setDataset(m_MetaFormat);
    return metaInstance;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6996 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new Stacking(), argv);
  }
}

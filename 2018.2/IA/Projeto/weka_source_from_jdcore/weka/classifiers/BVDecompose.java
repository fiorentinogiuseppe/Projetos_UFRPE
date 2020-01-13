package weka.classifiers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;





























































































public class BVDecompose
  implements OptionHandler, TechnicalInformationHandler, RevisionHandler
{
  protected boolean m_Debug;
  protected Classifier m_Classifier = new ZeroR();
  

  protected String[] m_ClassifierOptions;
  

  protected int m_TrainIterations = 50;
  

  protected String m_DataFileName;
  

  protected int m_ClassIndex = -1;
  

  protected int m_Seed = 1;
  

  protected double m_Bias;
  

  protected double m_Variance;
  

  protected double m_Sigma;
  

  protected double m_Error;
  

  protected int m_TrainPoolSize = 100;
  


  public BVDecompose() {}
  

  public String globalInfo()
  {
    return "Class for performing a Bias-Variance decomposition on any classifier using the method specified in:\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ron Kohavi and David H. Wolpert");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.TITLE, "Bias Plus Variance Decomposition for Zero-One Loss Functions");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Machine Learning: Proceedings of the Thirteenth International Conference");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    result.setValue(TechnicalInformation.Field.EDITOR, "Lorenza Saitta");
    result.setValue(TechnicalInformation.Field.PAGES, "275-283");
    result.setValue(TechnicalInformation.Field.PS, "http://robotics.stanford.edu/~ronnyk/biasVar.ps");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(7);
    
    newVector.addElement(new Option("\tThe index of the class attribute.\n\t(default last)", "c", 1, "-c <class index>"));
    


    newVector.addElement(new Option("\tThe name of the arff file used for the decomposition.", "t", 1, "-t <name of arff file>"));
    

    newVector.addElement(new Option("\tThe number of instances placed in the training pool.\n\tThe remainder will be used for testing. (default 100)", "T", 1, "-T <training pool size>"));
    


    newVector.addElement(new Option("\tThe random number seed used.", "s", 1, "-s <seed>"));
    

    newVector.addElement(new Option("\tThe number of training repetitions used.\n\t(default 50)", "x", 1, "-x <num>"));
    


    newVector.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    

    newVector.addElement(new Option("\tFull class name of the learner used in the decomposition.\n\teg: weka.classifiers.bayes.NaiveBayes", "W", 1, "-W <classifier class name>"));
    



    if ((m_Classifier != null) && ((m_Classifier instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to learner " + m_Classifier.getClass().getName() + ":"));
      



      Enumeration enu = m_Classifier.listOptions();
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    return newVector.elements();
  }
  













































  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String classIndex = Utils.getOption('c', options);
    if (classIndex.length() != 0) {
      if (classIndex.toLowerCase().equals("last")) {
        setClassIndex(0);
      } else if (classIndex.toLowerCase().equals("first")) {
        setClassIndex(1);
      } else {
        setClassIndex(Integer.parseInt(classIndex));
      }
    } else {
      setClassIndex(0);
    }
    
    String trainIterations = Utils.getOption('x', options);
    if (trainIterations.length() != 0) {
      setTrainIterations(Integer.parseInt(trainIterations));
    } else {
      setTrainIterations(50);
    }
    
    String trainPoolSize = Utils.getOption('T', options);
    if (trainPoolSize.length() != 0) {
      setTrainPoolSize(Integer.parseInt(trainPoolSize));
    } else {
      setTrainPoolSize(100);
    }
    
    String seedString = Utils.getOption('s', options);
    if (seedString.length() != 0) {
      setSeed(Integer.parseInt(seedString));
    } else {
      setSeed(1);
    }
    
    String dataFile = Utils.getOption('t', options);
    if (dataFile.length() == 0) {
      throw new Exception("An arff file must be specified with the -t option.");
    }
    
    setDataFileName(dataFile);
    
    String classifierName = Utils.getOption('W', options);
    if (classifierName.length() == 0) {
      throw new Exception("A learner must be specified with the -W option.");
    }
    setClassifier(Classifier.forName(classifierName, Utils.partitionOptions(options)));
  }
  






  public String[] getOptions()
  {
    String[] classifierOptions = new String[0];
    if ((m_Classifier != null) && ((m_Classifier instanceof OptionHandler)))
    {
      classifierOptions = m_Classifier.getOptions();
    }
    String[] options = new String[classifierOptions.length + 14];
    int current = 0;
    if (getDebug()) {
      options[(current++)] = "-D";
    }
    options[(current++)] = "-c";options[(current++)] = ("" + getClassIndex());
    options[(current++)] = "-x";options[(current++)] = ("" + getTrainIterations());
    options[(current++)] = "-T";options[(current++)] = ("" + getTrainPoolSize());
    options[(current++)] = "-s";options[(current++)] = ("" + getSeed());
    if (getDataFileName() != null) {
      options[(current++)] = "-t";options[(current++)] = ("" + getDataFileName());
    }
    if (getClassifier() != null) {
      options[(current++)] = "-W";
      options[(current++)] = getClassifier().getClass().getName();
    }
    options[(current++)] = "--";
    System.arraycopy(classifierOptions, 0, options, current, classifierOptions.length);
    
    current += classifierOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public int getTrainPoolSize()
  {
    return m_TrainPoolSize;
  }
  





  public void setTrainPoolSize(int numTrain)
  {
    m_TrainPoolSize = numTrain;
  }
  





  public void setClassifier(Classifier newClassifier)
  {
    m_Classifier = newClassifier;
  }
  





  public Classifier getClassifier()
  {
    return m_Classifier;
  }
  





  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  





  public boolean getDebug()
  {
    return m_Debug;
  }
  





  public void setSeed(int seed)
  {
    m_Seed = seed;
  }
  





  public int getSeed()
  {
    return m_Seed;
  }
  





  public void setTrainIterations(int trainIterations)
  {
    m_TrainIterations = trainIterations;
  }
  





  public int getTrainIterations()
  {
    return m_TrainIterations;
  }
  





  public void setDataFileName(String dataFileName)
  {
    m_DataFileName = dataFileName;
  }
  





  public String getDataFileName()
  {
    return m_DataFileName;
  }
  





  public int getClassIndex()
  {
    return m_ClassIndex + 1;
  }
  





  public void setClassIndex(int classIndex)
  {
    m_ClassIndex = (classIndex - 1);
  }
  





  public double getBias()
  {
    return m_Bias;
  }
  





  public double getVariance()
  {
    return m_Variance;
  }
  





  public double getSigma()
  {
    return m_Sigma;
  }
  





  public double getError()
  {
    return m_Error;
  }
  




  public void decompose()
    throws Exception
  {
    Reader dataReader = new BufferedReader(new FileReader(m_DataFileName));
    Instances data = new Instances(dataReader);
    
    if (m_ClassIndex < 0) {
      data.setClassIndex(data.numAttributes() - 1);
    } else {
      data.setClassIndex(m_ClassIndex);
    }
    if (data.classAttribute().type() != 1) {
      throw new Exception("Class attribute must be nominal");
    }
    int numClasses = data.numClasses();
    
    data.deleteWithMissingClass();
    if (data.checkForStringAttributes()) {
      throw new Exception("Can't handle string attributes!");
    }
    
    if (data.numInstances() < 2 * m_TrainPoolSize) {
      throw new Exception("The dataset must contain at least " + 2 * m_TrainPoolSize + " instances");
    }
    
    Random random = new Random(m_Seed);
    data.randomize(random);
    Instances trainPool = new Instances(data, 0, m_TrainPoolSize);
    Instances test = new Instances(data, m_TrainPoolSize, data.numInstances() - m_TrainPoolSize);
    
    int numTest = test.numInstances();
    double[][] instanceProbs = new double[numTest][numClasses];
    
    m_Error = 0.0D;
    for (int i = 0; i < m_TrainIterations; i++) {
      if (m_Debug) {
        System.err.println("Iteration " + (i + 1));
      }
      trainPool.randomize(random);
      Instances train = new Instances(trainPool, 0, m_TrainPoolSize / 2);
      
      Classifier current = Classifier.makeCopy(m_Classifier);
      current.buildClassifier(train);
      

      for (int j = 0; j < numTest; j++) {
        int pred = (int)current.classifyInstance(test.instance(j));
        if (pred != test.instance(j).classValue()) {
          m_Error += 1.0D;
        }
        instanceProbs[j][pred] += 1.0D;
      }
    }
    m_Error /= m_TrainIterations * numTest;
    

    m_Bias = 0.0D;
    m_Variance = 0.0D;
    m_Sigma = 0.0D;
    for (int i = 0; i < numTest; i++) {
      Instance current = test.instance(i);
      double[] predProbs = instanceProbs[i];
      
      double bsum = 0.0D;double vsum = 0.0D;double ssum = 0.0D;
      for (int j = 0; j < numClasses; j++) {
        double pActual = current.classValue() == j ? 1.0D : 0.0D;
        double pPred = predProbs[j] / m_TrainIterations;
        bsum += (pActual - pPred) * (pActual - pPred) - pPred * (1.0D - pPred) / (m_TrainIterations - 1);
        
        vsum += pPred * pPred;
        ssum += pActual * pActual;
      }
      m_Bias += bsum;
      m_Variance += 1.0D - vsum;
      m_Sigma += 1.0D - ssum;
    }
    m_Bias /= 2 * numTest;
    m_Variance /= 2 * numTest;
    m_Sigma /= 2 * numTest;
    
    if (m_Debug) {
      System.err.println("Decomposition finished");
    }
  }
  






  public String toString()
  {
    String result = "\nBias-Variance Decomposition\n";
    
    if (getClassifier() == null) {
      return "Invalid setup";
    }
    
    result = result + "\nClassifier   : " + getClassifier().getClass().getName();
    if ((getClassifier() instanceof OptionHandler)) {
      result = result + Utils.joinOptions(m_Classifier.getOptions());
    }
    result = result + "\nData File    : " + getDataFileName();
    result = result + "\nClass Index  : ";
    if (getClassIndex() == 0) {
      result = result + "last";
    } else {
      result = result + getClassIndex();
    }
    result = result + "\nTraining Pool: " + getTrainPoolSize();
    result = result + "\nIterations   : " + getTrainIterations();
    result = result + "\nSeed         : " + getSeed();
    result = result + "\nError        : " + Utils.doubleToString(getError(), 6, 4);
    result = result + "\nSigma^2      : " + Utils.doubleToString(getSigma(), 6, 4);
    result = result + "\nBias^2       : " + Utils.doubleToString(getBias(), 6, 4);
    result = result + "\nVariance     : " + Utils.doubleToString(getVariance(), 6, 4);
    
    return result + "\n";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.15 $");
  }
  




  public static void main(String[] args)
  {
    try
    {
      BVDecompose bvd = new BVDecompose();
      try
      {
        bvd.setOptions(args);
        Utils.checkForRemainingOptions(args);
      } catch (Exception ex) {
        String result = ex.getMessage() + "\nBVDecompose Options:\n\n";
        Enumeration enu = bvd.listOptions();
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          result = result + option.synopsis() + "\n" + option.description() + "\n";
        }
        throw new Exception(result);
      }
      
      bvd.decompose();
      System.out.println(bvd.toString());
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
}

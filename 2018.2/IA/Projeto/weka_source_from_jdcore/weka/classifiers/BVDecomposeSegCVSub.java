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










































































































































public class BVDecomposeSegCVSub
  implements OptionHandler, TechnicalInformationHandler, RevisionHandler
{
  protected boolean m_Debug;
  protected Classifier m_Classifier = new ZeroR();
  

  protected String[] m_ClassifierOptions;
  

  protected int m_ClassifyIterations;
  

  protected String m_DataFileName;
  

  protected int m_ClassIndex = -1;
  

  protected int m_Seed = 1;
  

  protected double m_KWBias;
  

  protected double m_KWVariance;
  

  protected double m_KWSigma;
  

  protected double m_WBias;
  

  protected double m_WVariance;
  

  protected double m_Error;
  

  protected int m_TrainSize;
  

  protected double m_P;
  

  public BVDecomposeSegCVSub() {}
  

  public String globalInfo()
  {
    return "This class performs Bias-Variance decomposion on any classifier using the sub-sampled cross-validation procedure as specified in (1).\nThe Kohavi and Wolpert definition of bias and variance is specified in (2).\nThe Webb definition of bias and variance is specified in (3).\n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Geoffrey I. Webb and Paul Conilione");
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.TITLE, "Estimating bias and variance from data");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "Monash University");
    result.setValue(TechnicalInformation.Field.ADDRESS, "School of Computer Science and Software Engineering, Victoria, Australia");
    result.setValue(TechnicalInformation.Field.PDF, "http://www.csse.monash.edu.au/~webb/Files/WebbConilione04.pdf");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Ron Kohavi and David H. Wolpert");
    additional.setValue(TechnicalInformation.Field.YEAR, "1996");
    additional.setValue(TechnicalInformation.Field.TITLE, "Bias Plus Variance Decomposition for Zero-One Loss Functions");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Machine Learning: Proceedings of the Thirteenth International Conference");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    additional.setValue(TechnicalInformation.Field.EDITOR, "Lorenza Saitta");
    additional.setValue(TechnicalInformation.Field.PAGES, "275-283");
    additional.setValue(TechnicalInformation.Field.PS, "http://robotics.stanford.edu/~ronnyk/biasVar.ps");
    
    additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Geoffrey I. Webb");
    additional.setValue(TechnicalInformation.Field.YEAR, "2000");
    additional.setValue(TechnicalInformation.Field.TITLE, "MultiBoosting: A Technique for Combining Boosting and Wagging");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    additional.setValue(TechnicalInformation.Field.VOLUME, "40");
    additional.setValue(TechnicalInformation.Field.NUMBER, "2");
    additional.setValue(TechnicalInformation.Field.PAGES, "159-196");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(8);
    
    newVector.addElement(new Option("\tThe index of the class attribute.\n\t(default last)", "c", 1, "-c <class index>"));
    


    newVector.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    

    newVector.addElement(new Option("\tThe number of times each instance is classified.\n\t(default 10)", "l", 1, "-l <num>"));
    


    newVector.addElement(new Option("\tThe average proportion of instances common between any two training sets", "p", 1, "-p <proportion of objects in common>"));
    

    newVector.addElement(new Option("\tThe random number seed used.", "s", 1, "-s <seed>"));
    

    newVector.addElement(new Option("\tThe name of the arff file used for the decomposition.", "t", 1, "-t <name of arff file>"));
    

    newVector.addElement(new Option("\tThe number of instances in the training set.", "T", 1, "-T <number of instances in training set>"));
    

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
    
    String classifyIterations = Utils.getOption('l', options);
    if (classifyIterations.length() != 0) {
      setClassifyIterations(Integer.parseInt(classifyIterations));
    } else {
      setClassifyIterations(10);
    }
    
    String prob = Utils.getOption('p', options);
    if (prob.length() != 0) {
      setP(Double.parseDouble(prob));
    } else {
      setP(-1.0D);
    }
    

    String seedString = Utils.getOption('s', options);
    if (seedString.length() != 0) {
      setSeed(Integer.parseInt(seedString));
    } else {
      setSeed(1);
    }
    
    String dataFile = Utils.getOption('t', options);
    if (dataFile.length() != 0) {
      setDataFileName(dataFile);
    } else {
      throw new Exception("An arff file must be specified with the -t option.");
    }
    

    String trainSize = Utils.getOption('T', options);
    if (trainSize.length() != 0) {
      setTrainSize(Integer.parseInt(trainSize));
    } else {
      setTrainSize(-1);
    }
    

    String classifierName = Utils.getOption('W', options);
    if (classifierName.length() != 0) {
      setClassifier(Classifier.forName(classifierName, Utils.partitionOptions(options)));
    } else {
      throw new Exception("A learner must be specified with the -W option.");
    }
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
    options[(current++)] = "-l";options[(current++)] = ("" + getClassifyIterations());
    options[(current++)] = "-p";options[(current++)] = ("" + getP());
    options[(current++)] = "-s";options[(current++)] = ("" + getSeed());
    if (getDataFileName() != null) {
      options[(current++)] = "-t";options[(current++)] = ("" + getDataFileName());
    }
    options[(current++)] = "-T";options[(current++)] = ("" + getTrainSize());
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
  





  public void setClassifyIterations(int classifyIterations)
  {
    m_ClassifyIterations = classifyIterations;
  }
  





  public int getClassifyIterations()
  {
    return m_ClassifyIterations;
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
  





  public double getKWBias()
  {
    return m_KWBias;
  }
  






  public double getWBias()
  {
    return m_WBias;
  }
  






  public double getKWVariance()
  {
    return m_KWVariance;
  }
  






  public double getWVariance()
  {
    return m_WVariance;
  }
  






  public double getKWSigma()
  {
    return m_KWSigma;
  }
  






  public void setTrainSize(int size)
  {
    m_TrainSize = size;
  }
  






  public int getTrainSize()
  {
    return m_TrainSize;
  }
  








  public void setP(double proportion)
  {
    m_P = proportion;
  }
  






  public double getP()
  {
    return m_P;
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
    

    if (data.numInstances() <= 2) {
      throw new Exception("Dataset size must be greater than 2.");
    }
    
    if (m_TrainSize == -1) {
      m_TrainSize = ((int)Math.floor(data.numInstances() / 2.0D));
    } else if ((m_TrainSize < 0) || (m_TrainSize >= data.numInstances() - 1)) {
      throw new Exception("Training set size of " + m_TrainSize + " is invalid.");
    }
    
    if (m_P == -1.0D) {
      m_P = (m_TrainSize / (data.numInstances() - 1.0D));
    } else if ((m_P < m_TrainSize / (data.numInstances() - 1.0D)) || (m_P >= 1.0D)) {
      throw new Exception("Proportion is not in range: " + m_TrainSize / (data.numInstances() - 1.0D) + " <= p < 1.0 ");
    }
    

    int tps = (int)Math.ceil(m_TrainSize / m_P + 1.0D);
    int k = (int)Math.ceil(tps / (tps - m_TrainSize));
    

    if (k > tps) {
      throw new Exception("The required number of folds is too many.Change p or the size of the training set.");
    }
    


    int q = (int)Math.floor(data.numInstances() / tps);
    

    double[][] instanceProbs = new double[data.numInstances()][numClasses];
    int[][] foldIndex = new int[k][2];
    Vector segmentList = new Vector(q + 1);
    

    Random random = new Random(m_Seed);
    
    data.randomize(random);
    


    int currentDataIndex = 0;
    
    for (int count = 1; count <= q + 1; count++) {
      if (count > q) {
        int[] segmentIndex = new int[data.numInstances() - q * tps];
        for (int index = 0; index < segmentIndex.length; currentDataIndex++)
        {
          segmentIndex[index] = currentDataIndex;index++;
        }
        segmentList.add(segmentIndex);
      } else {
        int[] segmentIndex = new int[tps];
        
        for (int index = 0; index < segmentIndex.length; currentDataIndex++) {
          segmentIndex[index] = currentDataIndex;index++;
        }
        segmentList.add(segmentIndex);
      }
    }
    
    int remainder = tps % k;
    

    int foldSize = (int)Math.ceil(tps / k);
    int index = 0;
    

    for (int count = 0; count < k; count++) {
      if ((remainder != 0) && (count == remainder)) {
        foldSize--;
      }
      foldIndex[count][0] = index;
      foldIndex[count][1] = foldSize;
      index += foldSize;
    }
    
    for (int l = 0; l < m_ClassifyIterations; l++)
    {
      for (int i = 1; i <= q; i++)
      {
        int[] currentSegment = (int[])segmentList.get(i - 1);
        
        randomize(currentSegment, random);
        

        for (int j = 1; j <= k; j++)
        {
          Instances TP = null;
          for (int foldNum = 1; foldNum <= k; foldNum++) {
            if (foldNum != j)
            {
              int startFoldIndex = foldIndex[(foldNum - 1)][0];
              foldSize = foldIndex[(foldNum - 1)][1];
              int endFoldIndex = startFoldIndex + foldSize - 1;
              
              for (int currentFoldIndex = startFoldIndex; currentFoldIndex <= endFoldIndex; currentFoldIndex++)
              {
                if (TP == null) {
                  TP = new Instances(data, currentSegment[currentFoldIndex], 1);
                } else {
                  TP.add(data.instance(currentSegment[currentFoldIndex]));
                }
              }
            }
          }
          
          TP.randomize(random);
          
          if (getTrainSize() > TP.numInstances()) {
            throw new Exception("The training set size of " + getTrainSize() + ", is greater than the training pool " + TP.numInstances());
          }
          

          Instances train = new Instances(TP, 0, m_TrainSize);
          
          Classifier current = Classifier.makeCopy(m_Classifier);
          current.buildClassifier(train);
          
          int currentTestIndex = foldIndex[(j - 1)][0];
          int testFoldSize = foldIndex[(j - 1)][1];
          int endTestIndex = currentTestIndex + testFoldSize - 1;
          
          while (currentTestIndex <= endTestIndex)
          {
            Instance testInst = data.instance(currentSegment[currentTestIndex]);
            int pred = (int)current.classifyInstance(testInst);
            

            if (pred != testInst.classValue()) {
              m_Error += 1.0D;
            }
            instanceProbs[currentSegment[currentTestIndex]][pred] += 1.0D;
            currentTestIndex++;
          }
          
          if ((i == 1) && (j == 1)) {
            int[] segmentElast = (int[])segmentList.lastElement();
            for (int currentIndex = 0; currentIndex < segmentElast.length; currentIndex++) {
              Instance testInst = data.instance(segmentElast[currentIndex]);
              int pred = (int)current.classifyInstance(testInst);
              if (pred != testInst.classValue()) {
                m_Error += 1.0D;
              }
              
              instanceProbs[segmentElast[currentIndex]][pred] += 1.0D;
            }
          }
        }
      }
    }
    
    m_Error /= m_ClassifyIterations * data.numInstances();
    
    m_KWBias = 0.0D;
    m_KWVariance = 0.0D;
    m_KWSigma = 0.0D;
    
    m_WBias = 0.0D;
    m_WVariance = 0.0D;
    
    for (int i = 0; i < data.numInstances(); i++)
    {
      Instance current = data.instance(i);
      
      double[] predProbs = instanceProbs[i];
      
      double bsum = 0.0D;double vsum = 0.0D;double ssum = 0.0D;
      double wBSum = 0.0D;double wVSum = 0.0D;
      
      Vector centralTendencies = findCentralTendencies(predProbs);
      
      if (centralTendencies == null) {
        throw new Exception("Central tendency was null.");
      }
      
      for (int j = 0; j < numClasses; j++) {
        double pActual = current.classValue() == j ? 1.0D : 0.0D;
        double pPred = predProbs[j] / m_ClassifyIterations;
        bsum += (pActual - pPred) * (pActual - pPred) - pPred * (1.0D - pPred) / (m_ClassifyIterations - 1);
        vsum += pPred * pPred;
        ssum += pActual * pActual;
      }
      
      m_KWBias += bsum;
      m_KWVariance += 1.0D - vsum;
      m_KWSigma += 1.0D - ssum;
      
      for (int count = 0; count < centralTendencies.size(); count++)
      {
        int wB = 0;int wV = 0;
        int centralTendency = ((Integer)centralTendencies.get(count)).intValue();
        

        for (int j = 0; j < numClasses; j++)
        {

          if ((j != (int)current.classValue()) && (j == centralTendency)) {
            wB = (int)(wB + predProbs[j]);
          }
          if ((j != (int)current.classValue()) && (j != centralTendency)) {
            wV = (int)(wV + predProbs[j]);
          }
        }
        
        wBSum += wB;
        wVSum += wV;
      }
      




      m_WBias += wBSum / (centralTendencies.size() * m_ClassifyIterations);
      
      m_WVariance += wVSum / (centralTendencies.size() * m_ClassifyIterations);
    }
    

    m_KWBias /= 2.0D * data.numInstances();
    m_KWVariance /= 2.0D * data.numInstances();
    m_KWSigma /= 2.0D * data.numInstances();
    

    m_WBias /= data.numInstances();
    
    m_WVariance /= data.numInstances();
    
    if (m_Debug) {
      System.err.println("Decomposition finished");
    }
  }
  






















  public Vector findCentralTendencies(double[] predProbs)
  {
    int centralTValue = 0;
    int currentValue = 0;
    


    Vector centralTClasses = new Vector();
    

    for (int i = 0; i < predProbs.length; i++) {
      currentValue = (int)predProbs[i];
      

      if (currentValue > centralTValue) {
        centralTClasses.clear();
        centralTClasses.addElement(new Integer(i));
        centralTValue = currentValue;
      } else if ((currentValue != 0) && (currentValue == centralTValue)) {
        centralTClasses.addElement(new Integer(i));
      }
    }
    
    if (centralTValue != 0) {
      return centralTClasses;
    }
    return null;
  }
  







  public String toString()
  {
    String result = "\nBias-Variance Decomposition Segmentation, Cross Validation\nwith subsampling.\n";
    

    if (getClassifier() == null) {
      return "Invalid setup";
    }
    
    result = result + "\nClassifier    : " + getClassifier().getClass().getName();
    if ((getClassifier() instanceof OptionHandler)) {
      result = result + Utils.joinOptions(m_Classifier.getOptions());
    }
    result = result + "\nData File     : " + getDataFileName();
    result = result + "\nClass Index   : ";
    if (getClassIndex() == 0) {
      result = result + "last";
    } else {
      result = result + getClassIndex();
    }
    result = result + "\nIterations    : " + getClassifyIterations();
    result = result + "\np             : " + getP();
    result = result + "\nTraining Size : " + getTrainSize();
    result = result + "\nSeed          : " + getSeed();
    
    result = result + "\n\nDefinition   : Kohavi and Wolpert";
    result = result + "\nError         :" + Utils.doubleToString(getError(), 4);
    result = result + "\nBias^2        :" + Utils.doubleToString(getKWBias(), 4);
    result = result + "\nVariance      :" + Utils.doubleToString(getKWVariance(), 4);
    result = result + "\nSigma^2       :" + Utils.doubleToString(getKWSigma(), 4);
    
    result = result + "\n\nDefinition   : Webb";
    result = result + "\nError         :" + Utils.doubleToString(getError(), 4);
    result = result + "\nBias          :" + Utils.doubleToString(getWBias(), 4);
    result = result + "\nVariance      :" + Utils.doubleToString(getWVariance(), 4);
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
  




  public static void main(String[] args)
  {
    try
    {
      BVDecomposeSegCVSub bvd = new BVDecomposeSegCVSub();
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
    }
    catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
  







  public final void randomize(int[] index, Random random)
  {
    for (int j = index.length - 1; j > 0; j--) {
      int k = random.nextInt(j + 1);
      int temp = index[j];
      index[j] = index[k];
      index[k] = temp;
    }
  }
}

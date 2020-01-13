package weka.classifiers.functions;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;





































































































public class Winnow
  extends Classifier
  implements UpdateableClassifier, TechnicalInformationHandler
{
  static final long serialVersionUID = 3543770107994321324L;
  protected boolean m_Balanced;
  protected int m_numIterations = 1;
  

  protected double m_Alpha = 2.0D;
  

  protected double m_Beta = 0.5D;
  

  protected double m_Threshold = -1.0D;
  

  protected int m_Seed = 1;
  

  protected int m_Mistakes;
  

  protected double m_defaultWeight = 2.0D;
  

  private double[] m_predPosVector = null;
  

  private double[] m_predNegVector = null;
  

  private double m_actualThreshold;
  

  private Instances m_Train = null;
  

  private NominalToBinary m_NominalToBinary;
  

  private ReplaceMissingValues m_ReplaceMissingValues;
  


  public Winnow() {}
  

  public String globalInfo()
  {
    return "Implements Winnow and Balanced Winnow algorithms by Littlestone.\n\nFor more information, see\n\n" + getTechnicalInformation().toString() + "\n\n" + "Does classification for problems with nominal attributes " + "(which it converts into binary attributes).";
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "N. Littlestone");
    result.setValue(TechnicalInformation.Field.YEAR, "1988");
    result.setValue(TechnicalInformation.Field.TITLE, "Learning quickly when irrelevant attributes are abound: A new linear threshold algorithm");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "2");
    result.setValue(TechnicalInformation.Field.PAGES, "285-318");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.TECHREPORT);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "N. Littlestone");
    additional.setValue(TechnicalInformation.Field.YEAR, "1989");
    additional.setValue(TechnicalInformation.Field.TITLE, "Mistake bounds and logarithmic linear-threshold learning algorithms");
    additional.setValue(TechnicalInformation.Field.INSTITUTION, "University of California");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "University of California, Santa Cruz");
    additional.setValue(TechnicalInformation.Field.NOTE, "Technical Report UCSC-CRL-89-11");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(7);
    
    newVector.addElement(new Option("\tUse the baLanced version\n\t(default false)", "L", 0, "-L"));
    

    newVector.addElement(new Option("\tThe number of iterations to be performed.\n\t(default 1)", "I", 1, "-I <int>"));
    

    newVector.addElement(new Option("\tPromotion coefficient alpha.\n\t(default 2.0)", "A", 1, "-A <double>"));
    

    newVector.addElement(new Option("\tDemotion coefficient beta.\n\t(default 0.5)", "B", 1, "-B <double>"));
    

    newVector.addElement(new Option("\tPrediction threshold.\n\t(default -1.0 == number of attributes)", "H", 1, "-H <double>"));
    

    newVector.addElement(new Option("\tStarting weights.\n\t(default 2.0)", "W", 1, "-W <double>"));
    

    newVector.addElement(new Option("\tDefault random seed.\n\t(default 1)", "S", 1, "-S <int>"));
    


    return newVector.elements();
  }
  






































  public void setOptions(String[] options)
    throws Exception
  {
    m_Balanced = Utils.getFlag('L', options);
    
    String iterationsString = Utils.getOption('I', options);
    if (iterationsString.length() != 0) {
      m_numIterations = Integer.parseInt(iterationsString);
    }
    String alphaString = Utils.getOption('A', options);
    if (alphaString.length() != 0) {
      m_Alpha = new Double(alphaString).doubleValue();
    }
    String betaString = Utils.getOption('B', options);
    if (betaString.length() != 0) {
      m_Beta = new Double(betaString).doubleValue();
    }
    String tString = Utils.getOption('H', options);
    if (tString.length() != 0) {
      m_Threshold = new Double(tString).doubleValue();
    }
    String wString = Utils.getOption('W', options);
    if (wString.length() != 0) {
      m_defaultWeight = new Double(wString).doubleValue();
    }
    String rString = Utils.getOption('S', options);
    if (rString.length() != 0) {
      m_Seed = Integer.parseInt(rString);
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[20];
    int current = 0;
    
    if (m_Balanced) {
      options[(current++)] = "-L";
    }
    
    options[(current++)] = "-I";options[(current++)] = ("" + m_numIterations);
    options[(current++)] = "-A";options[(current++)] = ("" + m_Alpha);
    options[(current++)] = "-B";options[(current++)] = ("" + m_Beta);
    options[(current++)] = "-H";options[(current++)] = ("" + m_Threshold);
    options[(current++)] = "-W";options[(current++)] = ("" + m_defaultWeight);
    options[(current++)] = "-S";options[(current++)] = ("" + m_Seed);
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  






  public void buildClassifier(Instances insts)
    throws Exception
  {
    getCapabilities().testWithFail(insts);
    

    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    

    m_Train = new Instances(insts);
    
    m_ReplaceMissingValues = new ReplaceMissingValues();
    m_ReplaceMissingValues.setInputFormat(m_Train);
    m_Train = Filter.useFilter(m_Train, m_ReplaceMissingValues);
    m_NominalToBinary = new NominalToBinary();
    m_NominalToBinary.setInputFormat(m_Train);
    m_Train = Filter.useFilter(m_Train, m_NominalToBinary);
    

    if (m_Seed != -1) {
      m_Train.randomize(new Random(m_Seed));
    }
    

    m_predPosVector = new double[m_Train.numAttributes()];
    
    if (m_Balanced) {
      m_predNegVector = new double[m_Train.numAttributes()];
    }
    

    for (int i = 0; i < m_Train.numAttributes(); i++) {
      m_predPosVector[i] = m_defaultWeight;
    }
    if (m_Balanced) {
      for (int i = 0; i < m_Train.numAttributes(); i++) {
        m_predNegVector[i] = m_defaultWeight;
      }
    }
    

    if (m_Threshold < 0.0D) {
      m_actualThreshold = (m_Train.numAttributes() - 1.0D);
    } else {
      m_actualThreshold = m_Threshold;
    }
    
    m_Mistakes = 0;
    

    if (m_Balanced) {
      for (int it = 0; it < m_numIterations; it++) {
        for (int i = 0; i < m_Train.numInstances(); i++) {
          actualUpdateClassifierBalanced(m_Train.instance(i));
        }
      }
    } else {
      for (int it = 0; it < m_numIterations; it++) {
        for (int i = 0; i < m_Train.numInstances(); i++) {
          actualUpdateClassifier(m_Train.instance(i));
        }
      }
    }
  }
  





  public void updateClassifier(Instance instance)
    throws Exception
  {
    m_ReplaceMissingValues.input(instance);
    m_ReplaceMissingValues.batchFinished();
    Instance filtered = m_ReplaceMissingValues.output();
    m_NominalToBinary.input(filtered);
    m_NominalToBinary.batchFinished();
    filtered = m_NominalToBinary.output();
    
    if (m_Balanced) {
      actualUpdateClassifierBalanced(filtered);
    } else {
      actualUpdateClassifier(filtered);
    }
  }
  







  private void actualUpdateClassifier(Instance inst)
    throws Exception
  {
    if (!inst.classIsMissing()) {
      double prediction = makePrediction(inst);
      
      if (prediction != inst.classValue()) {
        m_Mistakes += 1;
        double posmultiplier;
        double posmultiplier; if (prediction == 0.0D)
        {
          posmultiplier = m_Alpha;
        }
        else {
          posmultiplier = m_Beta;
        }
        int n1 = inst.numValues();int classIndex = m_Train.classIndex();
        for (int l = 0; l < n1; l++) {
          if ((inst.index(l) != classIndex) && (inst.valueSparse(l) == 1.0D)) {
            m_predPosVector[inst.index(l)] *= posmultiplier;
          }
        }
      }
    }
    else
    {
      System.out.println("CLASS MISSING");
    }
  }
  







  private void actualUpdateClassifierBalanced(Instance inst)
    throws Exception
  {
    if (!inst.classIsMissing()) {
      double prediction = makePredictionBalanced(inst);
      
      if (prediction != inst.classValue()) {
        m_Mistakes += 1;
        double negmultiplier;
        double posmultiplier; double negmultiplier; if (prediction == 0.0D)
        {
          double posmultiplier = m_Alpha;
          negmultiplier = m_Beta;
        }
        else {
          posmultiplier = m_Beta;
          negmultiplier = m_Alpha;
        }
        int n1 = inst.numValues();int classIndex = m_Train.classIndex();
        for (int l = 0; l < n1; l++) {
          if ((inst.index(l) != classIndex) && (inst.valueSparse(l) == 1.0D)) {
            m_predPosVector[inst.index(l)] *= posmultiplier;
            m_predNegVector[inst.index(l)] *= negmultiplier;
          }
          
        }
      }
    }
    else
    {
      System.out.println("CLASS MISSING");
    }
  }
  






  public double classifyInstance(Instance inst)
    throws Exception
  {
    m_ReplaceMissingValues.input(inst);
    m_ReplaceMissingValues.batchFinished();
    Instance filtered = m_ReplaceMissingValues.output();
    m_NominalToBinary.input(filtered);
    m_NominalToBinary.batchFinished();
    filtered = m_NominalToBinary.output();
    
    if (m_Balanced) {
      return makePredictionBalanced(filtered);
    }
    return makePrediction(filtered);
  }
  







  private double makePrediction(Instance inst)
    throws Exception
  {
    double total = 0.0D;
    
    int n1 = inst.numValues();int classIndex = m_Train.classIndex();
    
    for (int i = 0; i < n1; i++) {
      if ((inst.index(i) != classIndex) && (inst.valueSparse(i) == 1.0D)) {
        total += m_predPosVector[inst.index(i)];
      }
    }
    
    if (total > m_actualThreshold) {
      return 1.0D;
    }
    return 0.0D;
  }
  






  private double makePredictionBalanced(Instance inst)
    throws Exception
  {
    double total = 0.0D;
    
    int n1 = inst.numValues();int classIndex = m_Train.classIndex();
    for (int i = 0; i < n1; i++) {
      if ((inst.index(i) != classIndex) && (inst.valueSparse(i) == 1.0D)) {
        total += m_predPosVector[inst.index(i)] - m_predNegVector[inst.index(i)];
      }
    }
    
    if (total > m_actualThreshold) {
      return 1.0D;
    }
    return 0.0D;
  }
  






  public String toString()
  {
    if (m_predPosVector == null) {
      return "Winnow: No model built yet.";
    }
    String result = "Winnow\n\nAttribute weights\n\n";
    
    int classIndex = m_Train.classIndex();
    
    if (!m_Balanced) {
      for (int i = 0; i < m_Train.numAttributes(); i++) {
        if (i != classIndex)
          result = result + "w" + i + " " + m_predPosVector[i] + "\n";
      }
    } else {
      for (int i = 0; i < m_Train.numAttributes(); i++) {
        if (i != classIndex) {
          result = result + "w" + i + " p " + m_predPosVector[i];
          result = result + " n " + m_predNegVector[i];
          
          double wdiff = m_predPosVector[i] - m_predNegVector[i];
          
          result = result + " d " + wdiff + "\n";
        }
      }
    }
    result = result + "\nCumulated mistake count: " + m_Mistakes + "\n\n";
    
    return result;
  }
  




  public String balancedTipText()
  {
    return "Whether to use the balanced version of the algorithm.";
  }
  





  public boolean getBalanced()
  {
    return m_Balanced;
  }
  





  public void setBalanced(boolean b)
  {
    m_Balanced = b;
  }
  




  public String alphaTipText()
  {
    return "Promotion coefficient alpha.";
  }
  





  public double getAlpha()
  {
    return m_Alpha;
  }
  





  public void setAlpha(double a)
  {
    m_Alpha = a;
  }
  




  public String betaTipText()
  {
    return "Demotion coefficient beta.";
  }
  





  public double getBeta()
  {
    return m_Beta;
  }
  





  public void setBeta(double b)
  {
    m_Beta = b;
  }
  




  public String thresholdTipText()
  {
    return "Prediction threshold (-1 means: set to number of attributes).";
  }
  





  public double getThreshold()
  {
    return m_Threshold;
  }
  





  public void setThreshold(double t)
  {
    m_Threshold = t;
  }
  




  public String defaultWeightTipText()
  {
    return "Initial value of weights/coefficients.";
  }
  





  public double getDefaultWeight()
  {
    return m_defaultWeight;
  }
  





  public void setDefaultWeight(double w)
  {
    m_defaultWeight = w;
  }
  




  public String numIterationsTipText()
  {
    return "The number of iterations to be performed.";
  }
  





  public int getNumIterations()
  {
    return m_numIterations;
  }
  





  public void setNumIterations(int v)
  {
    m_numIterations = v;
  }
  




  public String seedTipText()
  {
    return "Random number seed used for data shuffling (-1 means no randomization).";
  }
  






  public int getSeed()
  {
    return m_Seed;
  }
  





  public void setSeed(int v)
  {
    m_Seed = v;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new Winnow(), argv);
  }
}

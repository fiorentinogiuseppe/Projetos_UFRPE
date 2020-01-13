package weka.classifiers.functions;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;














































































public class VotedPerceptron
  extends Classifier
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -1072429260104568698L;
  private int m_MaxK = 10000;
  

  private int m_NumIterations = 1;
  

  private double m_Exponent = 1.0D;
  

  private int m_K = 0;
  

  private int[] m_Additions = null;
  

  private boolean[] m_IsAddition = null;
  

  private int[] m_Weights = null;
  

  private Instances m_Train = null;
  

  private int m_Seed = 1;
  

  private NominalToBinary m_NominalToBinary;
  

  private ReplaceMissingValues m_ReplaceMissingValues;
  

  public VotedPerceptron() {}
  

  public String globalInfo()
  {
    return "Implementation of the voted perceptron algorithm by Freund and Schapire. Globally replaces all missing values, and transforms nominal attributes into binary ones.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Y. Freund and R. E. Schapire");
    result.setValue(TechnicalInformation.Field.TITLE, "Large margin classification using the perceptron algorithm");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "11th Annual Conference on Computational Learning Theory");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.PAGES, "209-217");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "ACM Press");
    result.setValue(TechnicalInformation.Field.ADDRESS, "New York, NY");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tThe number of iterations to be performed.\n\t(default 1)", "I", 1, "-I <int>"));
    

    newVector.addElement(new Option("\tThe exponent for the polynomial kernel.\n\t(default 1)", "E", 1, "-E <double>"));
    

    newVector.addElement(new Option("\tThe seed for the random number generation.\n\t(default 1)", "S", 1, "-S <int>"));
    

    newVector.addElement(new Option("\tThe maximum number of alterations allowed.\n\t(default 10000)", "M", 1, "-M <int>"));
    


    return newVector.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    String iterationsString = Utils.getOption('I', options);
    if (iterationsString.length() != 0) {
      m_NumIterations = Integer.parseInt(iterationsString);
    } else {
      m_NumIterations = 1;
    }
    String exponentsString = Utils.getOption('E', options);
    if (exponentsString.length() != 0) {
      m_Exponent = new Double(exponentsString).doubleValue();
    } else {
      m_Exponent = 1.0D;
    }
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      m_Seed = Integer.parseInt(seedString);
    } else {
      m_Seed = 1;
    }
    String alterationsString = Utils.getOption('M', options);
    if (alterationsString.length() != 0) {
      m_MaxK = Integer.parseInt(alterationsString);
    } else {
      m_MaxK = 10000;
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[8];
    int current = 0;
    
    options[(current++)] = "-I";options[(current++)] = ("" + m_NumIterations);
    options[(current++)] = "-E";options[(current++)] = ("" + m_Exponent);
    options[(current++)] = "-S";options[(current++)] = ("" + m_Seed);
    options[(current++)] = "-M";options[(current++)] = ("" + m_MaxK);
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
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
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
    

    m_Train.randomize(new Random(m_Seed));
    

    m_Additions = new int[m_MaxK + 1];
    m_IsAddition = new boolean[m_MaxK + 1];
    m_Weights = new int[m_MaxK + 1];
    

    m_K = 0;
    
    for (int it = 0; it < m_NumIterations; it++) {
      for (int i = 0; i < m_Train.numInstances(); i++) {
        Instance inst = m_Train.instance(i);
        if (!inst.classIsMissing()) {
          int prediction = makePrediction(m_K, inst);
          int classValue = (int)inst.classValue();
          if (prediction == classValue) {
            m_Weights[m_K] += 1;
          } else {
            m_IsAddition[m_K] = (classValue == 1 ? 1 : false);
            m_Additions[m_K] = i;
            m_K += 1;
            m_Weights[m_K] += 1;
          }
          if (m_K == m_MaxK) {
            return;
          }
        }
      }
    }
  }
  








  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    m_ReplaceMissingValues.input(inst);
    m_ReplaceMissingValues.batchFinished();
    inst = m_ReplaceMissingValues.output();
    
    m_NominalToBinary.input(inst);
    m_NominalToBinary.batchFinished();
    inst = m_NominalToBinary.output();
    

    double output = 0.0D;double sumSoFar = 0.0D;
    if (m_K > 0) {
      for (int i = 0; i <= m_K; i++) {
        if (sumSoFar < 0.0D) {
          output -= m_Weights[i];
        } else {
          output += m_Weights[i];
        }
        if (m_IsAddition[i] != 0) {
          sumSoFar += innerProduct(m_Train.instance(m_Additions[i]), inst);
        } else {
          sumSoFar -= innerProduct(m_Train.instance(m_Additions[i]), inst);
        }
      }
    }
    double[] result = new double[2];
    result[1] = (1.0D / (1.0D + Math.exp(-output)));
    result[0] = (1.0D - result[1]);
    
    return result;
  }
  





  public String toString()
  {
    return "VotedPerceptron: Number of perceptrons=" + m_K;
  }
  




  public String maxKTipText()
  {
    return "The maximum number of alterations to the perceptron.";
  }
  





  public int getMaxK()
  {
    return m_MaxK;
  }
  





  public void setMaxK(int v)
  {
    m_MaxK = v;
  }
  




  public String numIterationsTipText()
  {
    return "Number of iterations to be performed.";
  }
  





  public int getNumIterations()
  {
    return m_NumIterations;
  }
  





  public void setNumIterations(int v)
  {
    m_NumIterations = v;
  }
  




  public String exponentTipText()
  {
    return "Exponent for the polynomial kernel.";
  }
  





  public double getExponent()
  {
    return m_Exponent;
  }
  





  public void setExponent(double v)
  {
    m_Exponent = v;
  }
  




  public String seedTipText()
  {
    return "Seed for the random number generator.";
  }
  





  public int getSeed()
  {
    return m_Seed;
  }
  





  public void setSeed(int v)
  {
    m_Seed = v;
  }
  








  private double innerProduct(Instance i1, Instance i2)
    throws Exception
  {
    double result = 0.0D;
    int n1 = i1.numValues();int n2 = i2.numValues();
    int classIndex = m_Train.classIndex();
    int p1 = 0; for (int p2 = 0; (p1 < n1) && (p2 < n2);) {
      int ind1 = i1.index(p1);
      int ind2 = i2.index(p2);
      if (ind1 == ind2) {
        if (ind1 != classIndex) {
          result += i1.valueSparse(p1) * i2.valueSparse(p2);
        }
        
        p1++;p2++;
      } else if (ind1 > ind2) {
        p2++;
      } else {
        p1++;
      }
    }
    result += 1.0D;
    
    if (m_Exponent != 1.0D) {
      return Math.pow(result, m_Exponent);
    }
    return result;
  }
  








  private int makePrediction(int k, Instance inst)
    throws Exception
  {
    double result = 0.0D;
    for (int i = 0; i < k; i++) {
      if (m_IsAddition[i] != 0) {
        result += innerProduct(m_Train.instance(m_Additions[i]), inst);
      } else {
        result -= innerProduct(m_Train.instance(m_Additions[i]), inst);
      }
    }
    if (result < 0.0D) {
      return 0;
    }
    return 1;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new VotedPerceptron(), argv);
  }
}

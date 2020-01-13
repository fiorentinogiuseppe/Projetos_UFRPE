package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;











































































public class MINND
  extends Classifier
  implements OptionHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -4512599203273864994L;
  protected int m_Neighbour = 1;
  

  protected double[][] m_Mean = (double[][])null;
  

  protected double[][] m_Variance = (double[][])null;
  

  protected int m_Dimension = 0;
  

  protected Instances m_Attributes;
  

  protected double[] m_Class = null;
  

  protected int m_NumClasses = 0;
  

  protected double[] m_Weights = null;
  

  private static double m_ZERO = 1.0E-45D;
  

  protected double m_Rate = -1.0D;
  

  private double[] m_MinArray = null;
  

  private double[] m_MaxArray = null;
  

  private double m_STOP = 1.0E-45D;
  

  private double[][] m_Change = (double[][])null;
  

  private double[][] m_NoiseM = (double[][])null; private double[][] m_NoiseV = (double[][])null; private double[][] m_ValidM = (double[][])null; private double[][] m_ValidV = (double[][])null;
  



  private int m_Select = 1;
  


  private int m_Choose = 1;
  

  private double m_Decay = 0.5D;
  


  public MINND() {}
  

  public String globalInfo()
  {
    return "Multiple-Instance Nearest Neighbour with Distribution learner.\n\nIt uses gradient descent to find the weight for each dimension of each exeamplar from the starting point of 1.0. In order to avoid overfitting, it uses mean-square function (i.e. the Euclidean distance) to search for the weights.\n It then uses the weights to cleanse the training data. After that it searches for the weights again from the starting points of the weights searched before.\n Finally it uses the most updated weights to cleanse the test exemplar and then finds the nearest neighbour of the test exemplar using partly-weighted Kullback distance. But the variances in the Kullback distance are the ones before cleansing.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  






















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Xin Xu");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.TITLE, "A nearest distribution approach to multiple-instance learning");
    result.setValue(TechnicalInformation.Field.SCHOOL, "University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, NZ");
    result.setValue(TechnicalInformation.Field.NOTE, "0657.591B");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  








  public void buildClassifier(Instances exs)
    throws Exception
  {
    getCapabilities().testWithFail(exs);
    

    Instances newData = new Instances(exs);
    newData.deleteWithMissingClass();
    
    int numegs = newData.numInstances();
    m_Dimension = newData.attribute(1).relation().numAttributes();
    m_Attributes = newData.stringFreeStructure();
    m_Change = new double[numegs][m_Dimension];
    m_NumClasses = exs.numClasses();
    m_Mean = new double[numegs][m_Dimension];
    m_Variance = new double[numegs][m_Dimension];
    m_Class = new double[numegs];
    m_Weights = new double[numegs];
    m_NoiseM = new double[numegs][m_Dimension];
    m_NoiseV = new double[numegs][m_Dimension];
    m_ValidM = new double[numegs][m_Dimension];
    m_ValidV = new double[numegs][m_Dimension];
    m_MinArray = new double[m_Dimension];
    m_MaxArray = new double[m_Dimension];
    for (int v = 0; v < m_Dimension; v++) {
      double tmp209_206 = NaN.0D;m_MaxArray[v] = tmp209_206;m_MinArray[v] = tmp209_206;
    }
    for (int w = 0; w < numegs; w++) {
      updateMinMax(newData.instance(w));
    }
    

    Instances data = m_Attributes;
    
    for (int x = 0; x < numegs; x++) {
      Instance example = newData.instance(x);
      example = scale(example);
      for (int i = 0; i < m_Dimension; i++) {
        m_Mean[x][i] = example.relationalValue(1).meanOrMode(i);
        m_Variance[x][i] = example.relationalValue(1).variance(i);
        if (Utils.eq(m_Variance[x][i], 0.0D))
          m_Variance[x][i] = m_ZERO;
        m_Change[x][i] = 1.0D;
      }
      





      data.add(example);
      m_Class[x] = example.classValue();
      m_Weights[x] = example.weight();
    }
    
    for (int z = 0; z < numegs; z++) {
      findWeights(z, m_Mean);
    }
    
    for (int x = 0; x < numegs; x++) {
      Instance example = preprocess(data, x);
      if (getDebug()) {
        System.out.println("???Exemplar " + x + " has been pre-processed:" + data.instance(x).relationalValue(1).sumOfWeights() + "|" + example.relationalValue(1).sumOfWeights() + "; class:" + m_Class[x]);
      }
      

      if (Utils.gr(example.relationalValue(1).sumOfWeights(), 0.0D)) {
        for (int i = 0; i < m_Dimension; i++) {
          m_ValidM[x][i] = example.relationalValue(1).meanOrMode(i);
          m_ValidV[x][i] = example.relationalValue(1).variance(i);
          if (Utils.eq(m_ValidV[x][i], 0.0D)) {
            m_ValidV[x][i] = m_ZERO;
          }
          
        }
        
      }
      else
      {
        m_ValidM[x] = null;
        m_ValidV[x] = null;
      }
    }
    
    for (int z = 0; z < numegs; z++) {
      if (m_ValidM[z] != null) {
        findWeights(z, m_ValidM);
      }
    }
  }
  







  public Instance preprocess(Instances data, int pos)
    throws Exception
  {
    Instance before = data.instance(pos);
    if ((int)before.classValue() == 0) {
      m_NoiseM[pos] = null;
      m_NoiseV[pos] = null;
      return before;
    }
    
    Instances after_relationInsts = before.attribute(1).relation().stringFreeStructure();
    Instances noises_relationInsts = before.attribute(1).relation().stringFreeStructure();
    
    Instances newData = m_Attributes;
    Instance after = new Instance(before.numAttributes());
    Instance noises = new Instance(before.numAttributes());
    after.setDataset(newData);
    noises.setDataset(newData);
    
    for (int g = 0; g < before.relationalValue(1).numInstances(); g++) {
      Instance datum = before.relationalValue(1).instance(g);
      double[] dists = new double[data.numInstances()];
      
      for (int i = 0; i < data.numInstances(); i++) {
        if (i != pos) {
          dists[i] = distance(datum, m_Mean[i], m_Variance[i], i);
        } else {
          dists[i] = Double.POSITIVE_INFINITY;
        }
      }
      int[] pred = new int[m_NumClasses];
      for (int n = 0; n < pred.length; n++) {
        pred[n] = 0;
      }
      for (int o = 0; o < m_Select; o++) {
        int index = Utils.minIndex(dists);
        pred[((int)m_Class[index])] += 1;
        dists[index] = Double.POSITIVE_INFINITY;
      }
      
      int clas = Utils.maxIndex(pred);
      if ((int)before.classValue() != clas) {
        noises_relationInsts.add(datum);
      } else {
        after_relationInsts.add(datum);
      }
    }
    
    int relationValue = noises.attribute(1).addRelation(noises_relationInsts);
    noises.setValue(0, before.value(0));
    noises.setValue(1, relationValue);
    noises.setValue(2, before.classValue());
    
    relationValue = after.attribute(1).addRelation(after_relationInsts);
    after.setValue(0, before.value(0));
    after.setValue(1, relationValue);
    after.setValue(2, before.classValue());
    

    if (Utils.gr(noises.relationalValue(1).sumOfWeights(), 0.0D)) {
      for (int i = 0; i < m_Dimension; i++) {
        m_NoiseM[pos][i] = noises.relationalValue(1).meanOrMode(i);
        m_NoiseV[pos][i] = noises.relationalValue(1).variance(i);
        if (Utils.eq(m_NoiseV[pos][i], 0.0D)) {
          m_NoiseV[pos][i] = m_ZERO;
        }
        
      }
      
    }
    else
    {
      m_NoiseM[pos] = null;
      m_NoiseV[pos] = null;
    }
    
    return after;
  }
  







  private double distance(Instance first, double[] mean, double[] var, int pos)
  {
    double distance = 0.0D;
    
    for (int i = 0; i < m_Dimension; i++)
    {
      if (first.attribute(i).isNumeric()) {
        if (!first.isMissing(i)) {
          double diff = first.value(i) - mean[i];
          if (Utils.gr(var[i], m_ZERO)) {
            distance += m_Change[pos][i] * var[i] * diff * diff;
          } else {
            distance += m_Change[pos][i] * diff * diff;
          }
        }
        else if (Utils.gr(var[i], m_ZERO)) {
          distance += m_Change[pos][i] * var[i];
        } else {
          distance += m_Change[pos][i] * 1.0D;
        }
      }
    }
    

    return distance;
  }
  





  private void updateMinMax(Instance ex)
  {
    Instances insts = ex.relationalValue(1);
    for (int j = 0; j < m_Dimension; j++) {
      if (insts.attribute(j).isNumeric()) {
        for (int k = 0; k < insts.numInstances(); k++) {
          Instance ins = insts.instance(k);
          if (!ins.isMissing(j)) {
            if (Double.isNaN(m_MinArray[j])) {
              m_MinArray[j] = ins.value(j);
              m_MaxArray[j] = ins.value(j);
            }
            else if (ins.value(j) < m_MinArray[j]) {
              m_MinArray[j] = ins.value(j);
            } else if (ins.value(j) > m_MaxArray[j]) {
              m_MaxArray[j] = ins.value(j);
            }
          }
        }
      }
    }
  }
  







  private Instance scale(Instance before)
    throws Exception
  {
    Instances afterInsts = before.relationalValue(1).stringFreeStructure();
    Instance after = new Instance(before.numAttributes());
    after.setDataset(m_Attributes);
    
    for (int i = 0; i < before.relationalValue(1).numInstances(); i++) {
      Instance datum = before.relationalValue(1).instance(i);
      Instance inst = (Instance)datum.copy();
      
      for (int j = 0; j < m_Dimension; j++) {
        if (before.relationalValue(1).attribute(j).isNumeric())
          inst.setValue(j, (datum.value(j) - m_MinArray[j]) / (m_MaxArray[j] - m_MinArray[j]));
      }
      afterInsts.add(inst);
    }
    
    int attValue = after.attribute(1).addRelation(afterInsts);
    after.setValue(0, before.value(0));
    after.setValue(1, attValue);
    after.setValue(2, before.value(2));
    
    return after;
  }
  









  public void findWeights(int row, double[][] mean)
  {
    double[] neww = new double[m_Dimension];
    double[] oldw = new double[m_Dimension];
    System.arraycopy(m_Change[row], 0, neww, 0, m_Dimension);
    

    double newresult = target(neww, mean, row, m_Class);
    double result = Double.POSITIVE_INFINITY;
    double rate = 0.05D;
    if (m_Rate != -1.0D) {
      rate = m_Rate;
    }
    
    while (Utils.gr(result - newresult, m_STOP)) {
      oldw = neww;
      neww = new double[m_Dimension];
      
      double[] delta = delta(oldw, mean, row, m_Class);
      
      for (int i = 0; i < m_Dimension; i++) {
        if (Utils.gr(m_Variance[row][i], 0.0D))
          oldw[i] += rate * delta[i];
      }
      result = newresult;
      newresult = target(neww, mean, row, m_Class);
      

      while (Utils.gr(newresult, result))
      {
        if (m_Rate == -1.0D) {
          rate *= m_Decay;
          for (int i = 0; i < m_Dimension; i++)
            if (Utils.gr(m_Variance[row][i], 0.0D))
              oldw[i] += rate * delta[i];
          newresult = target(neww, mean, row, m_Class);
        }
        else {
          for (int i = 0; i < m_Dimension; i++)
            neww[i] = oldw[i];
          break label310;
        }
      }
    }
    label310:
    m_Change[row] = neww;
  }
  










  private double[] delta(double[] x, double[][] X, int rowpos, double[] Y)
  {
    double y = Y[rowpos];
    
    double[] delta = new double[m_Dimension];
    for (int h = 0; h < m_Dimension; h++) {
      delta[h] = 0.0D;
    }
    for (int i = 0; i < X.length; i++) {
      if ((i != rowpos) && (X[i] != null)) {
        double var = y == Y[i] ? 0.0D : Math.sqrt(m_Dimension - 1.0D);
        double distance = 0.0D;
        for (int j = 0; j < m_Dimension; j++)
          if (Utils.gr(m_Variance[rowpos][j], 0.0D))
            distance += x[j] * (X[rowpos][j] - X[i][j]) * (X[rowpos][j] - X[i][j]);
        distance = Math.sqrt(distance);
        if (distance != 0.0D) {
          for (int k = 0; k < m_Dimension; k++) {
            if (m_Variance[rowpos][k] > 0.0D) {
              delta[k] += (var / distance - 1.0D) * 0.5D * (X[rowpos][k] - X[i][k]) * (X[rowpos][k] - X[i][k]);
            }
          }
        }
      }
    }
    return delta;
  }
  














  public double target(double[] x, double[][] X, int rowpos, double[] Y)
  {
    double y = Y[rowpos];double result = 0.0D;
    
    for (int i = 0; i < X.length; i++) {
      if ((i != rowpos) && (X[i] != null)) {
        double var = y == Y[i] ? 0.0D : Math.sqrt(m_Dimension - 1.0D);
        double f = 0.0D;
        for (int j = 0; j < m_Dimension; j++) {
          if (Utils.gr(m_Variance[rowpos][j], 0.0D)) {
            f += x[j] * (X[rowpos][j] - X[i][j]) * (X[rowpos][j] - X[i][j]);
          }
        }
        f = Math.sqrt(f);
        
        if (Double.isInfinite(f))
          System.exit(1);
        result += 0.5D * (f - var) * (f - var);
      }
    }
    
    return result;
  }
  










  public double classifyInstance(Instance ex)
    throws Exception
  {
    ex = scale(ex);
    
    double[] var = new double[m_Dimension];
    for (int i = 0; i < m_Dimension; i++) {
      var[i] = ex.relationalValue(1).variance(i);
    }
    
    double[] kullback = new double[m_Class.length];
    

    double[] predict = new double[m_NumClasses];
    for (int h = 0; h < predict.length; h++)
      predict[h] = 0.0D;
    ex = cleanse(ex);
    
    if (ex.relationalValue(1).numInstances() == 0) {
      if (getDebug())
        System.out.println("???Whole exemplar falls into ambiguous area!");
      return 1.0D;
    }
    
    double[] mean = new double[m_Dimension];
    for (int i = 0; i < m_Dimension; i++) {
      mean[i] = ex.relationalValue(1).meanOrMode(i);
    }
    
    for (int h = 0; h < var.length; h++) {
      if (Utils.eq(var[h], 0.0D)) {
        var[h] = m_ZERO;
      }
    }
    for (int i = 0; i < m_Class.length; i++) {
      if (m_ValidM[i] != null) {
        kullback[i] = kullback(mean, m_ValidM[i], var, m_Variance[i], i);
      } else {
        kullback[i] = Double.POSITIVE_INFINITY;
      }
    }
    for (int j = 0; j < m_Neighbour; j++) {
      int pos = Utils.minIndex(kullback);
      predict[((int)m_Class[pos])] += m_Weights[pos];
      kullback[pos] = Double.POSITIVE_INFINITY;
    }
    
    if (getDebug())
      System.out.println("???There are still some unambiguous instances in this exemplar! Predicted as: " + Utils.maxIndex(predict));
    return Utils.maxIndex(predict);
  }
  







  public Instance cleanse(Instance before)
    throws Exception
  {
    Instances insts = before.relationalValue(1).stringFreeStructure();
    Instance after = new Instance(before.numAttributes());
    after.setDataset(m_Attributes);
    
    for (int g = 0; g < before.relationalValue(1).numInstances(); g++) {
      Instance datum = before.relationalValue(1).instance(g);
      double[] minNoiDists = new double[m_Choose];
      double[] minValDists = new double[m_Choose];
      int noiseCount = 0;int validCount = 0;
      double[] nDist = new double[m_Mean.length];
      double[] vDist = new double[m_Mean.length];
      
      for (int h = 0; h < m_Mean.length; h++) {
        if (m_ValidM[h] == null) {
          vDist[h] = Double.POSITIVE_INFINITY;
        } else {
          vDist[h] = distance(datum, m_ValidM[h], m_ValidV[h], h);
        }
        if (m_NoiseM[h] == null) {
          nDist[h] = Double.POSITIVE_INFINITY;
        } else {
          nDist[h] = distance(datum, m_NoiseM[h], m_NoiseV[h], h);
        }
      }
      for (int k = 0; k < m_Choose; k++) {
        int pos = Utils.minIndex(vDist);
        minValDists[k] = vDist[pos];
        vDist[pos] = Double.POSITIVE_INFINITY;
        pos = Utils.minIndex(nDist);
        minNoiDists[k] = nDist[pos];
        nDist[pos] = Double.POSITIVE_INFINITY;
      }
      
      int x = 0;int y = 0;
      while (x + y < m_Choose) {
        if (minValDists[x] <= minNoiDists[y]) {
          validCount++;
          x++;
        }
        else {
          noiseCount++;
          y++;
        }
      }
      if (x >= y) {
        insts.add(datum);
      }
    }
    
    after.setValue(0, before.value(0));
    after.setValue(1, after.attribute(1).addRelation(insts));
    after.setValue(2, before.value(2));
    
    return after;
  }
  























  public double kullback(double[] mu1, double[] mu2, double[] var1, double[] var2, int pos)
  {
    int p = mu1.length;
    double result = 0.0D;
    
    for (int y = 0; y < p; y++) {
      if ((Utils.gr(var1[y], 0.0D)) && (Utils.gr(var2[y], 0.0D))) {
        result += Math.log(Math.sqrt(var2[y] / var1[y])) + var1[y] / (2.0D * var2[y]) + m_Change[pos][y] * (mu1[y] - mu2[y]) * (mu1[y] - mu2[y]) / (2.0D * var2[y]) - 0.5D;
      }
    }
    




    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSet number of nearest neighbour for prediction\n\t(default 1)", "K", 1, "-K <number of neighbours>"));
    



    result.addElement(new Option("\tSet number of nearest neighbour for cleansing the training data\n\t(default 1)", "S", 1, "-S <number of neighbours>"));
    



    result.addElement(new Option("\tSet number of nearest neighbour for cleansing the testing data\n\t(default 1)", "E", 1, "-E <number of neighbours>"));
    



    return result.elements();
  }
  






















  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String numNeighbourString = Utils.getOption('K', options);
    if (numNeighbourString.length() != 0) {
      setNumNeighbours(Integer.parseInt(numNeighbourString));
    } else {
      setNumNeighbours(1);
    }
    numNeighbourString = Utils.getOption('S', options);
    if (numNeighbourString.length() != 0) {
      setNumTrainingNoises(Integer.parseInt(numNeighbourString));
    } else {
      setNumTrainingNoises(1);
    }
    numNeighbourString = Utils.getOption('E', options);
    if (numNeighbourString.length() != 0) {
      setNumTestingNoises(Integer.parseInt(numNeighbourString));
    } else {
      setNumTestingNoises(1);
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    result.add("-K");
    result.add("" + getNumNeighbours());
    
    result.add("-S");
    result.add("" + getNumTrainingNoises());
    
    result.add("-E");
    result.add("" + getNumTestingNoises());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String numNeighboursTipText()
  {
    return "The number of nearest neighbours to the estimate the class prediction of test bags.";
  }
  




  public void setNumNeighbours(int numNeighbour)
  {
    m_Neighbour = numNeighbour;
  }
  




  public int getNumNeighbours()
  {
    return m_Neighbour;
  }
  





  public String numTrainingNoisesTipText()
  {
    return "The number of nearest neighbour instances in the selection of noises in the training data.";
  }
  





  public void setNumTrainingNoises(int numTraining)
  {
    m_Select = numTraining;
  }
  





  public int getNumTrainingNoises()
  {
    return m_Select;
  }
  





  public String numTestingNoisesTipText()
  {
    return "The number of nearest neighbour instances in the selection of noises in the test data.";
  }
  




  public int getNumTestingNoises()
  {
    return m_Choose;
  }
  




  public void setNumTestingNoises(int numTesting)
  {
    m_Choose = numTesting;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new MINND(), args);
  }
}

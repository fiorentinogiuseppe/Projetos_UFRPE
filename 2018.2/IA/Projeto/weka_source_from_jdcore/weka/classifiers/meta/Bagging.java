package weka.classifiers.meta;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.trees.REPTree;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;






































































































































public class Bagging
  extends RandomizableIteratedSingleClassifierEnhancer
  implements WeightedInstancesHandler, AdditionalMeasureProducer, TechnicalInformationHandler
{
  private static final long serialVersionUID = -5178288489778728847L;
  protected int m_BagSizePercent = 100;
  

  protected boolean m_CalcOutOfBag = false;
  


  protected double m_OutOfBagError;
  


  public Bagging()
  {
    m_Classifier = new REPTree();
  }
  






  public String globalInfo()
  {
    return "Class for bagging a classifier to reduce variance. Can do classification and regression depending on the base learner. \n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Leo Breiman");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.TITLE, "Bagging predictors");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "24");
    result.setValue(TechnicalInformation.Field.NUMBER, "2");
    result.setValue(TechnicalInformation.Field.PAGES, "123-140");
    
    return result;
  }
  






  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.REPTree";
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tSize of each bag, as a percentage of the\n\ttraining set size. (default 100)", "P", 1, "-P"));
    

    newVector.addElement(new Option("\tCalculate the out of bag error.", "O", 0, "-O"));
    

    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  






















































































  public void setOptions(String[] options)
    throws Exception
  {
    String bagSize = Utils.getOption('P', options);
    if (bagSize.length() != 0) {
      setBagSizePercent(Integer.parseInt(bagSize));
    } else {
      setBagSizePercent(100);
    }
    
    setCalcOutOfBag(Utils.getFlag('O', options));
    
    super.setOptions(options);
  }
  






  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 3];
    
    int current = 0;
    options[(current++)] = "-P";
    options[(current++)] = ("" + getBagSizePercent());
    
    if (getCalcOutOfBag()) {
      options[(current++)] = "-O";
    }
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String bagSizePercentTipText()
  {
    return "Size of each bag, as a percentage of the training set size.";
  }
  





  public int getBagSizePercent()
  {
    return m_BagSizePercent;
  }
  





  public void setBagSizePercent(int newBagSizePercent)
  {
    m_BagSizePercent = newBagSizePercent;
  }
  





  public String calcOutOfBagTipText()
  {
    return "Whether the out-of-bag error is calculated.";
  }
  





  public void setCalcOutOfBag(boolean calcOutOfBag)
  {
    m_CalcOutOfBag = calcOutOfBag;
  }
  





  public boolean getCalcOutOfBag()
  {
    return m_CalcOutOfBag;
  }
  





  public double measureOutOfBagError()
  {
    return m_OutOfBagError;
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
    throw new IllegalArgumentException(additionalMeasureName + " not supported (Bagging)");
  }
  










  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    super.buildClassifier(data);
    
    if ((m_CalcOutOfBag) && (m_BagSizePercent != 100)) {
      throw new IllegalArgumentException("Bag size needs to be 100% if out-of-bag error is to be calculated!");
    }
    

    int bagSize = (int)(data.numInstances() * (m_BagSizePercent / 100.0D));
    Random random = new Random(m_Seed);
    
    boolean[][] inBag = (boolean[][])null;
    if (m_CalcOutOfBag) {
      inBag = new boolean[m_Classifiers.length][];
    }
    for (int j = 0; j < m_Classifiers.length; j++) {
      Instances bagData = null;
      

      if (m_CalcOutOfBag) {
        inBag[j] = new boolean[data.numInstances()];
        
        bagData = data.resampleWithWeights(random, inBag[j]);
      } else {
        bagData = data.resampleWithWeights(random);
        if (bagSize < data.numInstances()) {
          bagData.randomize(random);
          Instances newBagData = new Instances(bagData, 0, bagSize);
          bagData = newBagData;
        }
      }
      
      if ((m_Classifier instanceof Randomizable)) {
        ((Randomizable)m_Classifiers[j]).setSeed(random.nextInt());
      }
      

      m_Classifiers[j].buildClassifier(bagData);
    }
    

    if (getCalcOutOfBag()) {
      double outOfBagCount = 0.0D;
      double errorSum = 0.0D;
      boolean numeric = data.classAttribute().isNumeric();
      
      for (int i = 0; i < data.numInstances(); i++) {
        double[] votes;
        double[] votes;
        if (numeric) {
          votes = new double[1];
        } else {
          votes = new double[data.numClasses()];
        }
        
        int voteCount = 0;
        for (int j = 0; j < m_Classifiers.length; j++) {
          if (inBag[j][i] == 0)
          {

            voteCount++;
            
            if (numeric)
            {
              votes[0] += m_Classifiers[j].classifyInstance(data.instance(i));
            }
            else {
              double[] newProbs = m_Classifiers[j].distributionForInstance(data.instance(i));
              

              for (int k = 0; k < newProbs.length; k++) {
                votes[k] += newProbs[k];
              }
            }
          }
        }
        double vote;
        if (numeric) {
          double vote = votes[0];
          if (voteCount > 0) {
            vote /= voteCount;
          }
        } else {
          if (!Utils.eq(Utils.sum(votes), 0.0D))
          {
            Utils.normalize(votes);
          }
          vote = Utils.maxIndex(votes);
        }
        

        outOfBagCount += data.instance(i).weight();
        if (numeric) {
          errorSum += StrictMath.abs(vote - data.instance(i).classValue()) * data.instance(i).weight();

        }
        else if (vote != data.instance(i).classValue()) {
          errorSum += data.instance(i).weight();
        }
      }
      
      m_OutOfBagError = (errorSum / outOfBagCount);
    } else {
      m_OutOfBagError = 0.0D;
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] sums = new double[instance.numClasses()];
    
    for (int i = 0; i < m_NumIterations; i++) {
      if (instance.classAttribute().isNumeric() == true) {
        sums[0] += m_Classifiers[i].classifyInstance(instance);
      } else {
        double[] newProbs = m_Classifiers[i].distributionForInstance(instance);
        for (int j = 0; j < newProbs.length; j++)
          sums[j] += newProbs[j];
      }
    }
    if (instance.classAttribute().isNumeric() == true) {
      sums[0] /= m_NumIterations;
      return sums; }
    if (Utils.eq(Utils.sum(sums), 0.0D)) {
      return sums;
    }
    Utils.normalize(sums);
    return sums;
  }
  







  public String toString()
  {
    if (m_Classifiers == null) {
      return "Bagging: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("All the base classifiers: \n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append(m_Classifiers[i].toString() + "\n\n");
    }
    if (m_CalcOutOfBag) {
      text.append("Out of bag error: " + Utils.doubleToString(m_OutOfBagError, 4) + "\n\n");
    }
    

    return text.toString();
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11572 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new Bagging(), argv);
  }
}

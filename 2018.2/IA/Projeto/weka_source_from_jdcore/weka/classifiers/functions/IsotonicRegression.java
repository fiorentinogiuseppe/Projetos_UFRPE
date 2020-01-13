package weka.classifiers.functions;

import java.io.PrintStream;
import java.util.Arrays;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;



















































public class IsotonicRegression
  extends Classifier
  implements WeightedInstancesHandler
{
  static final long serialVersionUID = 1679336022835454137L;
  private Attribute m_attribute;
  private double[] m_cuts;
  private double[] m_values;
  private double m_minMsq;
  private Classifier m_ZeroR;
  
  public IsotonicRegression() {}
  
  public String globalInfo()
  {
    return "Learns an isotonic regression model. Picks the attribute that results in the lowest squared error. Missing values are not allowed. Can only deal with numeric attributes.Considers the monotonically increasing case as well as the monotonicallydecreasing case";
  }
  











  public double classifyInstance(Instance inst)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.classifyInstance(inst);
    }
    
    if (inst.isMissing(m_attribute.index())) {
      throw new Exception("IsotonicRegression: No missing values!");
    }
    int index = Arrays.binarySearch(m_cuts, inst.value(m_attribute));
    if (index < 0) {
      return m_values[(-index - 1)];
    }
    return m_values[(index + 1)];
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  




  protected void regress(Attribute attribute, Instances insts, boolean ascending)
    throws Exception
  {
    insts.sort(attribute);
    

    double[] values = new double[insts.numInstances()];
    double[] weights = new double[insts.numInstances()];
    double[] cuts = new double[insts.numInstances() - 1];
    int size = 0;
    values[0] = insts.instance(0).classValue();
    weights[0] = insts.instance(0).weight();
    for (int i = 1; i < insts.numInstances(); i++) {
      if (insts.instance(i).value(attribute) > insts.instance(i - 1).value(attribute))
      {
        cuts[size] = ((insts.instance(i).value(attribute) + insts.instance(i - 1).value(attribute)) / 2.0D);
        
        size++;
      }
      values[size] += insts.instance(i).classValue();
      weights[size] += insts.instance(i).weight();
    }
    size++;
    
    boolean violators;
    do
    {
      violators = false;
      

      double[] tempValues = new double[size];
      double[] tempWeights = new double[size];
      double[] tempCuts = new double[size - 1];
      

      int newSize = 0;
      tempValues[0] = values[0];
      tempWeights[0] = weights[0];
      for (int j = 1; j < size; j++) {
        if (((ascending) && (values[j] / weights[j] > tempValues[newSize] / tempWeights[newSize])) || ((!ascending) && (values[j] / weights[j] < tempValues[newSize] / tempWeights[newSize])))
        {


          tempCuts[newSize] = cuts[(j - 1)];
          newSize++;
          tempValues[newSize] = values[j];
          tempWeights[newSize] = weights[j];
        } else {
          tempWeights[newSize] += weights[j];
          tempValues[newSize] += values[j];
          violators = true;
        }
      }
      newSize++;
      

      values = tempValues;
      weights = tempWeights;
      cuts = tempCuts;
      size = newSize;
    } while (violators);
    

    for (int i = 0; i < size; i++) {
      values[i] /= weights[i];
    }
    

    Attribute attributeBackedup = m_attribute;
    double[] cutsBackedup = m_cuts;
    double[] valuesBackedup = m_values;
    

    m_attribute = attribute;
    m_cuts = cuts;
    m_values = values;
    

    Evaluation eval = new Evaluation(insts);
    eval.evaluateModel(this, insts, new Object[0]);
    double msq = eval.rootMeanSquaredError();
    

    if (msq < m_minMsq) {
      m_minMsq = msq;
    } else {
      m_attribute = attributeBackedup;
      m_cuts = cutsBackedup;
      m_values = valuesBackedup;
    }
  }
  






  public void buildClassifier(Instances insts)
    throws Exception
  {
    getCapabilities().testWithFail(insts);
    

    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    

    if (insts.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(insts);
      return;
    }
    
    m_ZeroR = null;
    


    m_minMsq = Double.MAX_VALUE;
    m_attribute = null;
    for (int a = 0; a < insts.numAttributes(); a++) {
      if (a != insts.classIndex()) {
        regress(insts.attribute(a), insts, true);
        regress(insts.attribute(a), insts, false);
      }
    }
  }
  






  public String toString()
  {
    if (m_ZeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    StringBuffer text = new StringBuffer();
    text.append("Isotonic regression\n\n");
    if (m_attribute == null) {
      text.append("No model built yet!");
    }
    else {
      text.append("Based on attribute: " + m_attribute.name() + "\n\n");
      for (int i = 0; i < m_values.length; i++) {
        text.append("prediction: " + Utils.doubleToString(m_values[i], 10, 2));
        if (i < m_cuts.length) {
          text.append("\t\tcut point: " + Utils.doubleToString(m_cuts[i], 10, 2) + "\n");
        }
      }
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new IsotonicRegression(), argv);
  }
}

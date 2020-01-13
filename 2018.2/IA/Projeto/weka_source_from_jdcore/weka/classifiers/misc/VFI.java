package weka.classifiers.misc;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
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
import weka.core.WeightedInstancesHandler;





















































































































public class VFI
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 8081692166331321866L;
  protected int m_ClassIndex;
  protected int m_NumClasses;
  protected Instances m_Instances = null;
  

  protected double[][][] m_counts;
  

  protected double[] m_globalCounts;
  

  protected double[][] m_intervalBounds;
  

  protected double m_maxEntrop;
  

  protected boolean m_weightByConfidence = true;
  

  protected double m_bias = -0.6D;
  
  private double TINY = 1.0E-11D;
  

  public VFI() {}
  

  public String globalInfo()
  {
    return "Classification by voting feature intervals. Intervals are constucted around each class for each attribute (basically discretization). Class counts are recorded for each interval on each attribute. Classification is by voting. For more info see:\n\n" + getTechnicalInformation().toString() + "\n\n" + "Have added a simple attribute weighting scheme. Higher weight is " + "assigned to more confident intervals, where confidence is a function " + "of entropy:\nweight (att_i) = (entropy of class distrib att_i / " + "max uncertainty)^-bias";
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "G. Demiroz and A. Guvenir");
    result.setValue(TechnicalInformation.Field.TITLE, "Classification by voting feature intervals");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "9th European Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "1997");
    result.setValue(TechnicalInformation.Field.PAGES, "85-92");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tDon't weight voting intervals by confidence", "C", 0, "-C"));
    

    newVector.addElement(new Option("\tSet exponential bias towards confident intervals\n\t(default = 0.6)", "B", 1, "-B <bias>"));
    



    return newVector.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    setWeightByConfidence(!Utils.getFlag('C', options));
    
    String optionString = Utils.getOption('B', options);
    if (optionString.length() != 0) {
      Double temp = new Double(optionString);
      setBias(temp.doubleValue());
    }
    
    Utils.checkForRemainingOptions(options);
  }
  




  public String weightByConfidenceTipText()
  {
    return "Weight feature intervals by confidence";
  }
  



  public void setWeightByConfidence(boolean c)
  {
    m_weightByConfidence = c;
  }
  



  public boolean getWeightByConfidence()
  {
    return m_weightByConfidence;
  }
  




  public String biasTipText()
  {
    return "Strength of bias towards more confident features";
  }
  



  public void setBias(double b)
  {
    m_bias = (-b);
  }
  



  public double getBias()
  {
    return -m_bias;
  }
  




  public String[] getOptions()
  {
    String[] options = new String[3];
    int current = 0;
    
    if (!getWeightByConfidence()) {
      options[(current++)] = "-C";
    }
    
    options[(current++)] = "-B";options[(current++)] = ("" + getBias());
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
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    if (!m_weightByConfidence) {
      TINY = 0.0D;
    }
    

    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    m_ClassIndex = instances.classIndex();
    m_NumClasses = instances.numClasses();
    m_globalCounts = new double[m_NumClasses];
    m_maxEntrop = (Math.log(m_NumClasses) / Math.log(2.0D));
    
    m_Instances = new Instances(instances, 0);
    
    m_intervalBounds = new double[instances.numAttributes()][2 + 2 * m_NumClasses];
    

    for (int j = 0; j < instances.numAttributes(); j++) {
      boolean alt = false;
      for (int i = 0; i < m_NumClasses * 2 + 2; i++) {
        if (i == 0) {
          m_intervalBounds[j][i] = Double.NEGATIVE_INFINITY;
        } else if (i == m_NumClasses * 2 + 1) {
          m_intervalBounds[j][i] = Double.POSITIVE_INFINITY;
        }
        else if (alt) {
          m_intervalBounds[j][i] = Double.NEGATIVE_INFINITY;
          alt = false;
        } else {
          m_intervalBounds[j][i] = Double.POSITIVE_INFINITY;
          alt = true;
        }
      }
    }
    


    for (int j = 0; j < instances.numAttributes(); j++) {
      if ((j != m_ClassIndex) && (instances.attribute(j).isNumeric())) {
        for (int i = 0; i < instances.numInstances(); i++) {
          Instance inst = instances.instance(i);
          if (!inst.isMissing(j)) {
            if (inst.value(j) < m_intervalBounds[j][((int)inst.classValue() * 2 + 1)])
            {
              m_intervalBounds[j][((int)inst.classValue() * 2 + 1)] = inst.value(j);
            }
            
            if (inst.value(j) > m_intervalBounds[j][((int)inst.classValue() * 2 + 2)])
            {
              m_intervalBounds[j][((int)inst.classValue() * 2 + 2)] = inst.value(j);
            }
          }
        }
      }
    }
    

    m_counts = new double[instances.numAttributes()][][];
    

    for (int i = 0; i < instances.numAttributes(); i++) {
      if (instances.attribute(i).isNumeric()) {
        int[] sortedIntervals = Utils.sort(m_intervalBounds[i]);
        
        int count = 1;
        for (int j = 1; j < sortedIntervals.length; j++) {
          if (m_intervalBounds[i][sortedIntervals[j]] != m_intervalBounds[i][sortedIntervals[(j - 1)]])
          {
            count++;
          }
        }
        double[] reordered = new double[count];
        count = 1;
        reordered[0] = m_intervalBounds[i][sortedIntervals[0]];
        for (int j = 1; j < sortedIntervals.length; j++) {
          if (m_intervalBounds[i][sortedIntervals[j]] != m_intervalBounds[i][sortedIntervals[(j - 1)]])
          {
            reordered[count] = m_intervalBounds[i][sortedIntervals[j]];
            count++;
          }
        }
        m_intervalBounds[i] = reordered;
        m_counts[i] = new double[count][m_NumClasses];
      } else if (i != m_ClassIndex) {
        m_counts[i] = new double[instances.attribute(i).numValues()][m_NumClasses];
      }
    }
    


    for (int i = 0; i < instances.numInstances(); i++) {
      Instance inst = instances.instance(i);
      m_globalCounts[((int)instances.instance(i).classValue())] += inst.weight();
      for (int j = 0; j < instances.numAttributes(); j++) {
        if ((!inst.isMissing(j)) && (j != m_ClassIndex)) {
          if (instances.attribute(j).isNumeric()) {
            double val = inst.value(j);
            

            for (int k = m_intervalBounds[j].length - 1; k >= 0; k--) {
              if (val > m_intervalBounds[j][k]) {
                m_counts[j][k][((int)inst.classValue())] += inst.weight();
                break; }
              if (val == m_intervalBounds[j][k]) {
                m_counts[j][k][((int)inst.classValue())] += inst.weight() / 2.0D;
                
                m_counts[j][(k - 1)][((int)inst.classValue())] += inst.weight() / 2.0D;
                
                break;
              }
            }
          }
          else
          {
            m_counts[j][((int)inst.value(j))][((int)inst.classValue())] += inst.weight();
          }
        }
      }
    }
  }
  





  public String toString()
  {
    if (m_Instances == null) {
      return "FVI: Classifier not built yet!";
    }
    StringBuffer sb = new StringBuffer("Voting feature intervals classifier\n");
    


























    return sb.toString();
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] dist = new double[m_NumClasses];
    double[] temp = new double[m_NumClasses];
    double weight = 1.0D;
    

    for (int i = 0; i < instance.numAttributes(); i++) {
      if ((i != m_ClassIndex) && (!instance.isMissing(i))) {
        double val = instance.value(i);
        boolean ok = false;
        if (instance.attribute(i).isNumeric())
        {
          for (int k = m_intervalBounds[i].length - 1; k >= 0; k--) {
            if (val > m_intervalBounds[i][k]) {
              for (int j = 0; j < m_NumClasses; j++) {
                if (m_globalCounts[j] > 0.0D) {
                  temp[j] = ((m_counts[i][k][j] + TINY) / (m_globalCounts[j] + TINY));
                }
              }
              
              ok = true;
              break; }
            if (val == m_intervalBounds[i][k]) {
              for (int j = 0; j < m_NumClasses; j++) {
                if (m_globalCounts[j] > 0.0D) {
                  temp[j] = ((m_counts[i][k][j] + m_counts[i][(k - 1)][j]) / 2.0D + TINY);
                  
                  temp[j] /= (m_globalCounts[j] + TINY);
                }
              }
              ok = true;
              break;
            }
          }
          if (!ok) {
            throw new Exception("This shouldn't happen");
          }
        } else {
          ok = true;
          for (int j = 0; j < m_NumClasses; j++) {
            if (m_globalCounts[j] > 0.0D) {
              temp[j] = ((m_counts[i][((int)val)][j] + TINY) / (m_globalCounts[j] + TINY));
            }
          }
        }
        

        double sum = Utils.sum(temp);
        if (sum <= 0.0D) {
          for (int j = 0; j < temp.length; j++) {
            temp[j] = (1.0D / temp.length);
          }
        } else {
          Utils.normalize(temp, sum);
        }
        
        if (m_weightByConfidence) {
          weight = ContingencyTables.entropy(temp);
          weight = Math.pow(weight, m_bias);
          if (weight < 1.0D) {
            weight = 1.0D;
          }
        }
        
        for (int j = 0; j < m_NumClasses; j++) {
          dist[j] += temp[j] * weight;
        }
      }
    }
    
    double sum = Utils.sum(dist);
    if (sum <= 0.0D) {
      for (int j = 0; j < dist.length; j++) {
        dist[j] = (1.0D / dist.length);
      }
      return dist;
    }
    Utils.normalize(dist, sum);
    return dist;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7180 $");
  }
  





  public static void main(String[] args)
  {
    runClassifier(new VFI(), args);
  }
}

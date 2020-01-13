package weka.classifiers.bayes;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Attribute;
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
import weka.core.WeightedInstancesHandler;
import weka.estimators.DiscreteEstimator;
import weka.estimators.Estimator;
import weka.estimators.KernelEstimator;
import weka.estimators.NormalEstimator;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;















































































public class NaiveBayes
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 5995231201785697655L;
  protected Estimator[][] m_Distributions;
  protected Estimator m_ClassDistribution;
  protected boolean m_UseKernelEstimator = false;
  




  protected boolean m_UseDiscretization = false;
  



  protected int m_NumClasses;
  


  protected Instances m_Instances;
  


  protected static final double DEFAULT_NUM_PRECISION = 0.01D;
  


  protected Discretize m_Disc = null;
  
  protected boolean m_displayModelInOldFormat = false;
  

  public NaiveBayes() {}
  

  public String globalInfo()
  {
    return "Class for a Naive Bayes classifier using estimator classes. Numeric estimator precision values are chosen based on analysis of the  training data. For this reason, the classifier is not an UpdateableClassifier (which in typical usage are initialized with zero training instances) -- if you need the UpdateableClassifier functionality, use the NaiveBayesUpdateable classifier. The NaiveBayesUpdateable classifier will  use a default precision of 0.1 for numeric attributes when buildClassifier is called with zero training instances.\n\nFor more information on Naive Bayes classifiers, see\n\n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "George H. John and Pat Langley");
    result.setValue(TechnicalInformation.Field.TITLE, "Estimating Continuous Distributions in Bayesian Classifiers");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Eleventh Conference on Uncertainty in Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.YEAR, "1995");
    result.setValue(TechnicalInformation.Field.PAGES, "338-345");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    result.setValue(TechnicalInformation.Field.ADDRESS, "San Mateo");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  







  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    m_NumClasses = instances.numClasses();
    

    m_Instances = new Instances(instances);
    

    if (m_UseDiscretization) {
      m_Disc = new Discretize();
      m_Disc.setInputFormat(m_Instances);
      m_Instances = Filter.useFilter(m_Instances, m_Disc);
    } else {
      m_Disc = null;
    }
    

    m_Distributions = new Estimator[m_Instances.numAttributes() - 1][m_Instances.numClasses()];
    
    m_ClassDistribution = new DiscreteEstimator(m_Instances.numClasses(), true);
    
    int attIndex = 0;
    Enumeration enu = m_Instances.enumerateAttributes();
    while (enu.hasMoreElements()) {
      Attribute attribute = (Attribute)enu.nextElement();
      


      double numPrecision = 0.01D;
      if (attribute.type() == 0) {
        m_Instances.sort(attribute);
        if ((m_Instances.numInstances() > 0) && (!m_Instances.instance(0).isMissing(attribute)))
        {
          double lastVal = m_Instances.instance(0).value(attribute);
          double deltaSum = 0.0D;
          int distinct = 0;
          for (int i = 1; i < m_Instances.numInstances(); i++) {
            Instance currentInst = m_Instances.instance(i);
            if (currentInst.isMissing(attribute)) {
              break;
            }
            double currentVal = currentInst.value(attribute);
            if (currentVal != lastVal) {
              deltaSum += currentVal - lastVal;
              lastVal = currentVal;
              distinct++;
            }
          }
          if (distinct > 0) {
            numPrecision = deltaSum / distinct;
          }
        }
      }
      

      for (int j = 0; j < m_Instances.numClasses(); j++) {
        switch (attribute.type()) {
        case 0: 
          if (m_UseKernelEstimator) {
            m_Distributions[attIndex][j] = new KernelEstimator(numPrecision);
          }
          else {
            m_Distributions[attIndex][j] = new NormalEstimator(numPrecision);
          }
          
          break;
        case 1: 
          m_Distributions[attIndex][j] = new DiscreteEstimator(attribute.numValues(), true);
          
          break;
        default: 
          throw new Exception("Attribute type unknown to NaiveBayes");
        }
      }
      attIndex++;
    }
    

    Enumeration enumInsts = m_Instances.enumerateInstances();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance)enumInsts.nextElement();
      
      updateClassifier(instance);
    }
    

    m_Instances = new Instances(m_Instances, 0);
  }
  







  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (!instance.classIsMissing()) {
      Enumeration enumAtts = m_Instances.enumerateAttributes();
      int attIndex = 0;
      while (enumAtts.hasMoreElements()) {
        Attribute attribute = (Attribute)enumAtts.nextElement();
        if (!instance.isMissing(attribute)) {
          m_Distributions[attIndex][((int)instance.classValue())].addValue(instance.value(attribute), instance.weight());
        }
        
        attIndex++;
      }
      m_ClassDistribution.addValue(instance.classValue(), instance.weight());
    }
  }
  










  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_UseDiscretization) {
      m_Disc.input(instance);
      instance = m_Disc.output();
    }
    double[] probs = new double[m_NumClasses];
    for (int j = 0; j < m_NumClasses; j++) {
      probs[j] = m_ClassDistribution.getProbability(j);
    }
    Enumeration enumAtts = instance.enumerateAttributes();
    int attIndex = 0;
    while (enumAtts.hasMoreElements()) {
      Attribute attribute = (Attribute)enumAtts.nextElement();
      if (!instance.isMissing(attribute)) {
        double max = 0.0D;
        for (int j = 0; j < m_NumClasses; j++) {
          double temp = Math.max(1.0E-75D, Math.pow(m_Distributions[attIndex][j].getProbability(instance.value(attribute)), m_Instances.attribute(attIndex).weight()));
          

          probs[j] *= temp;
          if (probs[j] > max) {
            max = probs[j];
          }
          if (Double.isNaN(probs[j])) {
            throw new Exception("NaN returned from estimator for attribute " + attribute.name() + ":\n" + m_Distributions[attIndex][j].toString());
          }
        }
        

        if ((max > 0.0D) && (max < 1.0E-75D)) {
          for (int j = 0; j < m_NumClasses; j++) {
            probs[j] *= 1.0E75D;
          }
        }
      }
      attIndex++;
    }
    

    Utils.normalize(probs);
    return probs;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tUse kernel density estimator rather than normal\n\tdistribution for numeric attributes", "K", 0, "-K"));
    


    newVector.addElement(new Option("\tUse supervised discretization to process numeric attributes\n", "D", 0, "-D"));
    


    newVector.addElement(new Option("\tDisplay model in old format (good when there are many classes)\n", "O", 0, "-O"));
    



    return newVector.elements();
  }
  






















  public void setOptions(String[] options)
    throws Exception
  {
    boolean k = Utils.getFlag('K', options);
    boolean d = Utils.getFlag('D', options);
    if ((k) && (d)) {
      throw new IllegalArgumentException("Can't use both kernel density estimation and discretization!");
    }
    
    setUseSupervisedDiscretization(d);
    setUseKernelEstimator(k);
    setDisplayModelInOldFormat(Utils.getFlag('O', options));
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[3];
    int current = 0;
    
    if (m_UseKernelEstimator) {
      options[(current++)] = "-K";
    }
    
    if (m_UseDiscretization) {
      options[(current++)] = "-D";
    }
    
    if (m_displayModelInOldFormat) {
      options[(current++)] = "-O";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String toString()
  {
    if (m_displayModelInOldFormat) {
      return toStringOriginal();
    }
    
    StringBuffer temp = new StringBuffer();
    temp.append("Naive Bayes Classifier");
    if (m_Instances == null) {
      temp.append(": No model built yet.");
    }
    else {
      int maxWidth = 0;
      int maxAttWidth = 0;
      boolean containsKernel = false;
      


      for (int i = 0; i < m_Instances.numClasses(); i++) {
        if (m_Instances.classAttribute().value(i).length() > maxWidth) {
          maxWidth = m_Instances.classAttribute().value(i).length();
        }
      }
      
      for (int i = 0; i < m_Instances.numAttributes(); i++) {
        if (i != m_Instances.classIndex()) {
          Attribute a = m_Instances.attribute(i);
          if (a.name().length() > maxAttWidth) {
            maxAttWidth = m_Instances.attribute(i).name().length();
          }
          if (a.isNominal())
          {
            for (int j = 0; j < a.numValues(); j++) {
              String val = a.value(j) + "  ";
              if (val.length() > maxAttWidth) {
                maxAttWidth = val.length();
              }
            }
          }
        }
      }
      
      for (int i = 0; i < m_Distributions.length; i++) {
        for (int j = 0; j < m_Instances.numClasses(); j++) {
          if ((m_Distributions[i][0] instanceof NormalEstimator))
          {
            NormalEstimator n = (NormalEstimator)m_Distributions[i][j];
            double mean = Math.log(Math.abs(n.getMean())) / Math.log(10.0D);
            double precision = Math.log(Math.abs(n.getPrecision())) / Math.log(10.0D);
            double width = mean > precision ? mean : precision;
            

            if (width < 0.0D) {
              width = 1.0D;
            }
            
            width += 6.0D;
            if ((int)width > maxWidth) {
              maxWidth = (int)width;
            }
          } else if ((m_Distributions[i][0] instanceof KernelEstimator)) {
            containsKernel = true;
            KernelEstimator ke = (KernelEstimator)m_Distributions[i][j];
            int numK = ke.getNumKernels();
            String temps = "K" + numK + ": mean (weight)";
            if (maxAttWidth < temps.length()) {
              maxAttWidth = temps.length();
            }
            
            if (ke.getNumKernels() > 0) {
              double[] means = ke.getMeans();
              double[] weights = ke.getWeights();
              for (int k = 0; k < ke.getNumKernels(); k++) {
                String m = Utils.doubleToString(means[k], maxWidth, 4).trim();
                m = m + " (" + Utils.doubleToString(weights[k], maxWidth, 1).trim() + ")";
                if (maxWidth < m.length()) {
                  maxWidth = m.length();
                }
              }
            }
          } else if ((m_Distributions[i][0] instanceof DiscreteEstimator)) {
            DiscreteEstimator d = (DiscreteEstimator)m_Distributions[i][j];
            for (int k = 0; k < d.getNumSymbols(); k++) {
              String size = "" + d.getCount(k);
              if (size.length() > maxWidth) {
                maxWidth = size.length();
              }
            }
            int sum = ("" + d.getSumOfCounts()).length();
            if (sum > maxWidth) {
              maxWidth = sum;
            }
          }
        }
      }
      

      for (int i = 0; i < m_Instances.numClasses(); i++) {
        String cSize = m_Instances.classAttribute().value(i);
        if (cSize.length() > maxWidth) {
          maxWidth = cSize.length();
        }
      }
      

      for (int i = 0; i < m_Instances.numClasses(); i++) {
        String priorP = Utils.doubleToString(((DiscreteEstimator)m_ClassDistribution).getProbability(i), maxWidth, 2).trim();
        

        priorP = "(" + priorP + ")";
        if (priorP.length() > maxWidth) {
          maxWidth = priorP.length();
        }
      }
      
      if (maxAttWidth < "Attribute".length()) {
        maxAttWidth = "Attribute".length();
      }
      
      if (maxAttWidth < "  weight sum".length()) {
        maxAttWidth = "  weight sum".length();
      }
      
      if ((containsKernel) && 
        (maxAttWidth < "  [precision]".length())) {
        maxAttWidth = "  [precision]".length();
      }
      

      maxAttWidth += 2;
      


      temp.append("\n\n");
      temp.append(pad("Class", " ", maxAttWidth + maxWidth + 1 - "Class".length(), true));
      


      temp.append("\n");
      temp.append(pad("Attribute", " ", maxAttWidth - "Attribute".length(), false));
      
      for (int i = 0; i < m_Instances.numClasses(); i++) {
        String classL = m_Instances.classAttribute().value(i);
        temp.append(pad(classL, " ", maxWidth + 1 - classL.length(), true));
      }
      temp.append("\n");
      
      temp.append(pad("", " ", maxAttWidth, true));
      for (int i = 0; i < m_Instances.numClasses(); i++) {
        String priorP = Utils.doubleToString(((DiscreteEstimator)m_ClassDistribution).getProbability(i), maxWidth, 2).trim();
        

        priorP = "(" + priorP + ")";
        temp.append(pad(priorP, " ", maxWidth + 1 - priorP.length(), true));
      }
      temp.append("\n");
      temp.append(pad("", "=", maxAttWidth + maxWidth * m_Instances.numClasses() + m_Instances.numClasses() + 1, true));
      

      temp.append("\n");
      

      int counter = 0;
      for (int i = 0; i < m_Instances.numAttributes(); i++) {
        if (i != m_Instances.classIndex())
        {

          String attName = m_Instances.attribute(i).name();
          temp.append(attName + "\n");
          
          if ((m_Distributions[counter][0] instanceof NormalEstimator)) {
            String meanL = "  mean";
            temp.append(pad(meanL, " ", maxAttWidth + 1 - meanL.length(), false));
            for (int j = 0; j < m_Instances.numClasses(); j++)
            {
              NormalEstimator n = (NormalEstimator)m_Distributions[counter][j];
              String mean = Utils.doubleToString(n.getMean(), maxWidth, 4).trim();
              
              temp.append(pad(mean, " ", maxWidth + 1 - mean.length(), true));
            }
            temp.append("\n");
            
            String stdDevL = "  std. dev.";
            temp.append(pad(stdDevL, " ", maxAttWidth + 1 - stdDevL.length(), false));
            for (int j = 0; j < m_Instances.numClasses(); j++) {
              NormalEstimator n = (NormalEstimator)m_Distributions[counter][j];
              String stdDev = Utils.doubleToString(n.getStdDev(), maxWidth, 4).trim();
              
              temp.append(pad(stdDev, " ", maxWidth + 1 - stdDev.length(), true));
            }
            temp.append("\n");
            
            String weightL = "  weight sum";
            temp.append(pad(weightL, " ", maxAttWidth + 1 - weightL.length(), false));
            for (int j = 0; j < m_Instances.numClasses(); j++) {
              NormalEstimator n = (NormalEstimator)m_Distributions[counter][j];
              String weight = Utils.doubleToString(n.getSumOfWeights(), maxWidth, 4).trim();
              
              temp.append(pad(weight, " ", maxWidth + 1 - weight.length(), true));
            }
            temp.append("\n");
            
            String precisionL = "  precision";
            temp.append(pad(precisionL, " ", maxAttWidth + 1 - precisionL.length(), false));
            for (int j = 0; j < m_Instances.numClasses(); j++) {
              NormalEstimator n = (NormalEstimator)m_Distributions[counter][j];
              String precision = Utils.doubleToString(n.getPrecision(), maxWidth, 4).trim();
              
              temp.append(pad(precision, " ", maxWidth + 1 - precision.length(), true));
            }
            temp.append("\n\n");
          }
          else if ((m_Distributions[counter][0] instanceof DiscreteEstimator)) {
            Attribute a = m_Instances.attribute(i);
            for (int j = 0; j < a.numValues(); j++) {
              String val = "  " + a.value(j);
              temp.append(pad(val, " ", maxAttWidth + 1 - val.length(), false));
              for (int k = 0; k < m_Instances.numClasses(); k++) {
                DiscreteEstimator d = (DiscreteEstimator)m_Distributions[counter][k];
                String count = "" + d.getCount(j);
                temp.append(pad(count, " ", maxWidth + 1 - count.length(), true));
              }
              temp.append("\n");
            }
            
            String total = "  [total]";
            temp.append(pad(total, " ", maxAttWidth + 1 - total.length(), false));
            for (int k = 0; k < m_Instances.numClasses(); k++) {
              DiscreteEstimator d = (DiscreteEstimator)m_Distributions[counter][k];
              String count = "" + d.getSumOfCounts();
              temp.append(pad(count, " ", maxWidth + 1 - count.length(), true));
            }
            temp.append("\n\n");
          } else if ((m_Distributions[counter][0] instanceof KernelEstimator)) {
            String kL = "  [# kernels]";
            temp.append(pad(kL, " ", maxAttWidth + 1 - kL.length(), false));
            for (int k = 0; k < m_Instances.numClasses(); k++) {
              KernelEstimator ke = (KernelEstimator)m_Distributions[counter][k];
              String nk = "" + ke.getNumKernels();
              temp.append(pad(nk, " ", maxWidth + 1 - nk.length(), true));
            }
            temp.append("\n");
            
            String stdDevL = "  [std. dev]";
            temp.append(pad(stdDevL, " ", maxAttWidth + 1 - stdDevL.length(), false));
            for (int k = 0; k < m_Instances.numClasses(); k++) {
              KernelEstimator ke = (KernelEstimator)m_Distributions[counter][k];
              String stdD = Utils.doubleToString(ke.getStdDev(), maxWidth, 4).trim();
              temp.append(pad(stdD, " ", maxWidth + 1 - stdD.length(), true));
            }
            temp.append("\n");
            String precL = "  [precision]";
            temp.append(pad(precL, " ", maxAttWidth + 1 - precL.length(), false));
            for (int k = 0; k < m_Instances.numClasses(); k++) {
              KernelEstimator ke = (KernelEstimator)m_Distributions[counter][k];
              String prec = Utils.doubleToString(ke.getPrecision(), maxWidth, 4).trim();
              temp.append(pad(prec, " ", maxWidth + 1 - prec.length(), true));
            }
            temp.append("\n");
            
            int maxK = 0;
            for (int k = 0; k < m_Instances.numClasses(); k++) {
              KernelEstimator ke = (KernelEstimator)m_Distributions[counter][k];
              if (ke.getNumKernels() > maxK) {
                maxK = ke.getNumKernels();
              }
            }
            for (int j = 0; j < maxK; j++)
            {
              String meanL = "  K" + (j + 1) + ": mean (weight)";
              temp.append(pad(meanL, " ", maxAttWidth + 1 - meanL.length(), false));
              for (int k = 0; k < m_Instances.numClasses(); k++) {
                KernelEstimator ke = (KernelEstimator)m_Distributions[counter][k];
                double[] means = ke.getMeans();
                double[] weights = ke.getWeights();
                String m = "--";
                if (ke.getNumKernels() == 0) {
                  m = "0";
                } else if (j < ke.getNumKernels()) {
                  m = Utils.doubleToString(means[j], maxWidth, 4).trim();
                  m = m + " (" + Utils.doubleToString(weights[j], maxWidth, 1).trim() + ")";
                }
                temp.append(pad(m, " ", maxWidth + 1 - m.length(), true));
              }
              temp.append("\n");
            }
            temp.append("\n");
          }
          

          counter++;
        }
      }
    }
    return temp.toString();
  }
  





  protected String toStringOriginal()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("Naive Bayes Classifier");
    if (m_Instances == null) {
      text.append(": No model built yet.");
    } else {
      try {
        for (int i = 0; i < m_Distributions[0].length; i++) {
          text.append("\n\nClass " + m_Instances.classAttribute().value(i) + ": Prior probability = " + Utils.doubleToString(m_ClassDistribution.getProbability(i), 4, 2) + "\n\n");
          


          Enumeration enumAtts = m_Instances.enumerateAttributes();
          int attIndex = 0;
          while (enumAtts.hasMoreElements()) {
            Attribute attribute = (Attribute)enumAtts.nextElement();
            if (attribute.weight() > 0.0D) {
              text.append(attribute.name() + ":  " + m_Distributions[attIndex][i]);
            }
            
            attIndex++;
          }
        }
      } catch (Exception ex) {
        text.append(ex.getMessage());
      }
    }
    
    return text.toString();
  }
  
  private String pad(String source, String padChar, int length, boolean leftPad)
  {
    StringBuffer temp = new StringBuffer();
    
    if (leftPad) {
      for (int i = 0; i < length; i++) {
        temp.append(padChar);
      }
      temp.append(source);
    } else {
      temp.append(source);
      for (int i = 0; i < length; i++) {
        temp.append(padChar);
      }
    }
    return temp.toString();
  }
  




  public String useKernelEstimatorTipText()
  {
    return "Use a kernel estimator for numeric attributes rather than a normal distribution.";
  }
  





  public boolean getUseKernelEstimator()
  {
    return m_UseKernelEstimator;
  }
  





  public void setUseKernelEstimator(boolean v)
  {
    m_UseKernelEstimator = v;
    if (v) {
      setUseSupervisedDiscretization(false);
    }
  }
  




  public String useSupervisedDiscretizationTipText()
  {
    return "Use supervised discretization to convert numeric attributes to nominal ones.";
  }
  






  public boolean getUseSupervisedDiscretization()
  {
    return m_UseDiscretization;
  }
  





  public void setUseSupervisedDiscretization(boolean newblah)
  {
    m_UseDiscretization = newblah;
    if (newblah) {
      setUseKernelEstimator(false);
    }
  }
  




  public String displayModelInOldFormatTipText()
  {
    return "Use old format for model output. The old format is better when there are many class values. The new format is better when there are fewer classes and many attributes.";
  }
  







  public void setDisplayModelInOldFormat(boolean d)
  {
    m_displayModelInOldFormat = d;
  }
  





  public boolean getDisplayModelInOldFormat()
  {
    return m_displayModelInOldFormat;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5516 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new NaiveBayes(), argv);
  }
}

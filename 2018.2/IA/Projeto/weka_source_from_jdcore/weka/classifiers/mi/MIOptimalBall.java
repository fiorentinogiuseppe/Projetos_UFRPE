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
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.matrix.DoubleVector;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MultiInstanceToPropositional;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.PropositionalToMultiInstance;
import weka.filters.unsupervised.attribute.Standardize;






































































public class MIOptimalBall
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -6465750129576777254L;
  protected double[] m_Center;
  protected double m_Radius;
  protected double[][][] m_Distance;
  protected Filter m_Filter = null;
  

  protected int m_filterType = 0;
  

  public static final int FILTER_NORMALIZE = 0;
  
  public static final int FILTER_STANDARDIZE = 1;
  
  public static final int FILTER_NONE = 2;
  
  public static final Tag[] TAGS_FILTER = { new Tag(0, "Normalize training data"), new Tag(1, "Standardize training data"), new Tag(2, "No normalization/standardization") };
  





  protected MultiInstanceToPropositional m_ConvertToSI = new MultiInstanceToPropositional();
  

  protected PropositionalToMultiInstance m_ConvertToMI = new PropositionalToMultiInstance();
  


  public MIOptimalBall() {}
  

  public String globalInfo()
  {
    return "This classifier tries to find a suitable ball in the multiple-instance space, with a certain data point in the instance space as a ball center. The possible ball center is a certain instance in a positive bag. The possible radiuses are those which can achieve the highest classification accuracy. The model selects the maximum radius as the radius of the optimal ball.\n\nFor more information about this algorithm, see:\n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Peter Auer and Ronald Ortner");
    result.setValue(TechnicalInformation.Field.TITLE, "A Boosting Approach to Multiple Instance Learning");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "15th European Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "2004");
    result.setValue(TechnicalInformation.Field.PAGES, "63-74");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    result.setValue(TechnicalInformation.Field.NOTE, "LNAI 3201");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    Instances train = new Instances(data);
    train.deleteWithMissingClass();
    
    int numAttributes = train.attribute(1).relation().numAttributes();
    m_Center = new double[numAttributes];
    
    if (getDebug()) {
      System.out.println("Start training ...");
    }
    
    m_ConvertToSI.setInputFormat(train);
    train = Filter.useFilter(train, m_ConvertToSI);
    
    if (m_filterType == 1) {
      m_Filter = new Standardize();
    } else if (m_filterType == 0) {
      m_Filter = new Normalize();
    } else {
      m_Filter = null;
    }
    if (m_Filter != null)
    {
      m_Filter.setInputFormat(train);
      train = Filter.useFilter(train, m_Filter);
    }
    

    m_ConvertToMI.setInputFormat(train);
    train = Filter.useFilter(train, m_ConvertToMI);
    


    calculateDistance(train);
    

    findRadius(train);
    
    if (getDebug()) {
      System.out.println("Finish building optimal ball model");
    }
  }
  








  public void calculateDistance(Instances train)
  {
    int numBags = train.numInstances();
    


    m_Distance = new double[numBags][][];
    for (int i = 0; i < numBags; i++) {
      if (train.instance(i).classValue() == 1.0D) {
        int numInstances = train.instance(i).relationalValue(1).numInstances();
        m_Distance[i] = new double[numInstances][];
        for (int j = 0; j < numInstances; j++) {
          Instance tempCenter = train.instance(i).relationalValue(1).instance(j);
          m_Distance[i][j] = new double[numBags];
          for (int k = 0; k < numBags; k++) {
            if (i == k) {
              m_Distance[i][j][k] = 0.0D;
            } else {
              m_Distance[i][j][k] = minBagDistance(tempCenter, train.instance(k));
            }
          }
        }
      }
    }
  }
  






  public double minBagDistance(Instance center, Instance bag)
  {
    double minDistance = Double.MAX_VALUE;
    Instances temp = bag.relationalValue(1);
    
    for (int i = 0; i < temp.numInstances(); i++) {
      double distance = 0.0D;
      for (int j = 0; j < center.numAttributes(); j++) {
        distance += (center.value(j) - temp.instance(i).value(j)) * (center.value(j) - temp.instance(i).value(j));
      }
      if (minDistance > distance)
        minDistance = distance;
    }
    return Math.sqrt(minDistance);
  }
  






  public void findRadius(Instances train)
  {
    int highestCount = 0;
    
    int numBags = train.numInstances();
    
    for (int i = 0; i < numBags; i++) {
      if (train.instance(i).classValue() == 1.0D) {
        int numInstances = train.instance(i).relationalValue(1).numInstances();
        for (int j = 0; j < numInstances; j++) {
          Instance tempCenter = train.instance(i).relationalValue(1).instance(j);
          

          double[] sortedDistance = sortArray(m_Distance[i][j]);
          for (int k = 1; k < sortedDistance.length; k++) {
            double radius = sortedDistance[k] - (sortedDistance[k] - sortedDistance[(k - 1)]) / 2.0D;
            


            int correctCount = 0;
            for (int n = 0; n < numBags; n++) {
              double bagDistance = m_Distance[i][j][n];
              if (((bagDistance <= radius) && (train.instance(n).classValue() == 1.0D)) || ((bagDistance > radius) && (train.instance(n).classValue() == 0.0D)))
              {
                correctCount = (int)(correctCount + train.instance(n).weight());
              }
            }
            

            if ((correctCount > highestCount) || ((correctCount == highestCount) && (radius > m_Radius))) {
              highestCount = correctCount;
              m_Radius = radius;
              for (int p = 0; p < tempCenter.numAttributes(); p++) {
                m_Center[p] = tempCenter.value(p);
              }
            }
          }
        }
      }
    }
  }
  




  public double[] sortArray(double[] distance)
  {
    double[] sorted = new double[distance.length];
    

    double[] disCopy = new double[distance.length];
    for (int i = 0; i < distance.length; i++) {
      disCopy[i] = distance[i];
    }
    DoubleVector sortVector = new DoubleVector(disCopy);
    sortVector.sort();
    sorted = sortVector.getArrayCopy();
    return sorted;
  }
  








  public double[] distributionForInstance(Instance newBag)
    throws Exception
  {
    double[] distribution = new double[2];
    
    distribution[0] = 0.0D;
    distribution[1] = 0.0D;
    
    Instances insts = new Instances(newBag.dataset(), 0);
    insts.add(newBag);
    

    insts = Filter.useFilter(insts, m_ConvertToSI);
    if (m_Filter != null) {
      insts = Filter.useFilter(insts, m_Filter);
    }
    
    int numInsts = insts.numInstances();
    insts.deleteAttributeAt(0);
    
    for (int i = 0; i < numInsts; i++) {
      double distance = 0.0D;
      for (int j = 0; j < insts.numAttributes() - 1; j++) {
        distance += (insts.instance(i).value(j) - m_Center[j]) * (insts.instance(i).value(j) - m_Center[j]);
      }
      if (distance <= m_Radius * m_Radius) {
        distribution[1] = 1.0D;
        break;
      }
    }
    
    distribution[0] = (1.0D - distribution[1]);
    
    return distribution;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tWhether to 0=normalize/1=standardize/2=neither. \n\t(default 0=normalize)", "N", 1, "-N <num>"));
    



    return result.elements();
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    result.add("-N");
    result.add("" + m_filterType);
    
    return (String[])result.toArray(new String[result.size()]);
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String nString = Utils.getOption('N', options);
    if (nString.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(nString), TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(0, TAGS_FILTER));
    }
  }
  





  public String filterTypeTipText()
  {
    return "The filter type for transforming the training data.";
  }
  






  public void setFilterType(SelectedTag newType)
  {
    if (newType.getTags() == TAGS_FILTER) {
      m_filterType = newType.getSelectedTag().getID();
    }
  }
  






  public SelectedTag getFilterType()
  {
    return new SelectedTag(m_filterType, TAGS_FILTER);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MIOptimalBall(), argv);
  }
}

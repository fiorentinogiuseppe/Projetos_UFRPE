package weka.classifiers.meta;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;





























































































public class Vote
  extends RandomizableMultipleClassifiersCombiner
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -637891196294399624L;
  public static final int AVERAGE_RULE = 1;
  public static final int PRODUCT_RULE = 2;
  public static final int MAJORITY_VOTING_RULE = 3;
  public static final int MIN_RULE = 4;
  public static final int MAX_RULE = 5;
  public static final int MEDIAN_RULE = 6;
  public static final Tag[] TAGS_RULES = { new Tag(1, "AVG", "Average of Probabilities"), new Tag(2, "PROD", "Product of Probabilities"), new Tag(3, "MAJ", "Majority Voting"), new Tag(4, "MIN", "Minimum Probability"), new Tag(5, "MAX", "Maximum Probability"), new Tag(6, "MED", "Median") };
  








  protected int m_CombinationRule = 1;
  

  protected Random m_Random;
  


  public Vote() {}
  

  public String globalInfo()
  {
    return "Class for combining classifiers. Different combinations of probability estimates for classification are available.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  











  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tThe combination rule to use\n\t(default: AVG)", "R", 1, "-R " + Tag.toOptionList(TAGS_RULES)));
    



    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-R");
    result.add("" + getCombinationRule());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setCombinationRule(new SelectedTag(tmpStr, TAGS_RULES));
    } else {
      setCombinationRule(new SelectedTag(1, TAGS_RULES));
    }
    super.setOptions(options);
  }
  









  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ludmila I. Kuncheva");
    result.setValue(TechnicalInformation.Field.TITLE, "Combining Pattern Classifiers: Methods and Algorithms");
    result.setValue(TechnicalInformation.Field.YEAR, "2004");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "John Wiley and Sons, Inc.");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "J. Kittler and M. Hatef and Robert P.W. Duin and J. Matas");
    additional.setValue(TechnicalInformation.Field.YEAR, "1998");
    additional.setValue(TechnicalInformation.Field.TITLE, "On combining classifiers");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "IEEE Transactions on Pattern Analysis and Machine Intelligence");
    additional.setValue(TechnicalInformation.Field.VOLUME, "20");
    additional.setValue(TechnicalInformation.Field.NUMBER, "3");
    additional.setValue(TechnicalInformation.Field.PAGES, "226-239");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    if ((m_CombinationRule == 2) || (m_CombinationRule == 3))
    {
      result.disableAllClasses();
      result.disableAllClassDependencies();
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
      result.enableDependency(Capabilities.Capability.NOMINAL_CLASS);
    }
    else if (m_CombinationRule == 6) {
      result.disableAllClasses();
      result.disableAllClassDependencies();
      result.enable(Capabilities.Capability.NUMERIC_CLASS);
      result.enableDependency(Capabilities.Capability.NUMERIC_CLASS);
    }
    
    return result;
  }
  








  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    Instances newData = new Instances(data);
    newData.deleteWithMissingClass();
    
    m_Random = new Random(getSeed());
    
    for (int i = 0; i < m_Classifiers.length; i++) {
      getClassifier(i).buildClassifier(newData);
    }
  }
  





  public double classifyInstance(Instance instance)
    throws Exception
  {
    double result;
    



    switch (m_CombinationRule) {
    case 1: 
    case 2: 
    case 3: 
    case 4: 
    case 5: 
      double[] dist = distributionForInstance(instance);
      double result; if (instance.classAttribute().isNominal()) {
        int index = Utils.maxIndex(dist);
        double result; if (dist[index] == 0.0D) {
          result = Instance.missingValue();
        } else
          result = index;
      } else { double result;
        if (instance.classAttribute().isNumeric()) {
          result = dist[0];
        }
        else
          result = Instance.missingValue();
      }
      break;
    case 6: 
      result = classifyInstanceMedian(instance);
      break;
    default: 
      throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
    }
    
    return result;
  }
  







  protected double classifyInstanceMedian(Instance instance)
    throws Exception
  {
    double[] results = new double[m_Classifiers.length];
    

    for (int i = 0; i < results.length; i++)
      results[i] = m_Classifiers[i].classifyInstance(instance);
    double result;
    double result; if (results.length == 0) {
      result = 0.0D; } else { double result;
      if (results.length == 1) {
        result = results[0];
      } else
        result = Utils.kthSmallestValue(results, results.length / 2);
    }
    return result;
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] result = new double[instance.numClasses()];
    
    switch (m_CombinationRule) {
    case 1: 
      result = distributionForInstanceAverage(instance);
      break;
    case 2: 
      result = distributionForInstanceProduct(instance);
      break;
    case 3: 
      result = distributionForInstanceMajorityVoting(instance);
      break;
    case 4: 
      result = distributionForInstanceMin(instance);
      break;
    case 5: 
      result = distributionForInstanceMax(instance);
      break;
    case 6: 
      result[0] = classifyInstance(instance);
      break;
    default: 
      throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
    }
    
    if ((!instance.classAttribute().isNumeric()) && (Utils.sum(result) > 0.0D)) {
      Utils.normalize(result);
    }
    return result;
  }
  








  protected double[] distributionForInstanceAverage(Instance instance)
    throws Exception
  {
    double[] probs = getClassifier(0).distributionForInstance(instance);
    probs = (double[])probs.clone();
    
    for (int i = 1; i < m_Classifiers.length; i++) {
      double[] dist = getClassifier(i).distributionForInstance(instance);
      for (int j = 0; j < dist.length; j++) {
        probs[j] += dist[j];
      }
    }
    for (int j = 0; j < probs.length; j++) {
      probs[j] /= m_Classifiers.length;
    }
    return probs;
  }
  








  protected double[] distributionForInstanceProduct(Instance instance)
    throws Exception
  {
    double[] probs = getClassifier(0).distributionForInstance(instance);
    probs = (double[])probs.clone();
    
    for (int i = 1; i < m_Classifiers.length; i++) {
      double[] dist = getClassifier(i).distributionForInstance(instance);
      for (int j = 0; j < dist.length; j++) {
        probs[j] *= dist[j];
      }
    }
    
    return probs;
  }
  







  protected double[] distributionForInstanceMajorityVoting(Instance instance)
    throws Exception
  {
    double[] probs = new double[instance.classAttribute().numValues()];
    double[] votes = new double[probs.length];
    
    for (int i = 0; i < m_Classifiers.length; i++) {
      probs = getClassifier(i).distributionForInstance(instance);
      int maxIndex = 0;
      for (int j = 0; j < probs.length; j++) {
        if (probs[j] > probs[maxIndex]) {
          maxIndex = j;
        }
      }
      
      for (int j = 0; j < probs.length; j++) {
        if (probs[j] == probs[maxIndex]) {
          votes[j] += 1.0D;
        }
      }
    }
    int tmpMajorityIndex = 0;
    for (int k = 1; k < votes.length; k++) {
      if (votes[k] > votes[tmpMajorityIndex]) {
        tmpMajorityIndex = k;
      }
    }
    
    Vector<Integer> majorityIndexes = new Vector();
    for (int k = 0; k < votes.length; k++) {
      if (votes[k] == votes[tmpMajorityIndex]) {
        majorityIndexes.add(Integer.valueOf(k));
      }
    }
    int majorityIndex = ((Integer)majorityIndexes.get(m_Random.nextInt(majorityIndexes.size()))).intValue();
    

    probs = new double[probs.length];
    probs[majorityIndex] = 1.0D;
    
    return probs;
  }
  







  protected double[] distributionForInstanceMax(Instance instance)
    throws Exception
  {
    double[] max = getClassifier(0).distributionForInstance(instance);
    max = (double[])max.clone();
    
    for (int i = 1; i < m_Classifiers.length; i++) {
      double[] dist = getClassifier(i).distributionForInstance(instance);
      for (int j = 0; j < dist.length; j++) {
        if (max[j] < dist[j]) {
          max[j] = dist[j];
        }
      }
    }
    return max;
  }
  







  protected double[] distributionForInstanceMin(Instance instance)
    throws Exception
  {
    double[] min = getClassifier(0).distributionForInstance(instance);
    
    min = (double[])min.clone();
    
    for (int i = 1; i < m_Classifiers.length; i++) {
      double[] dist = getClassifier(i).distributionForInstance(instance);
      for (int j = 0; j < dist.length; j++) {
        if (dist[j] < min[j]) {
          min[j] = dist[j];
        }
      }
    }
    return min;
  }
  





  public String combinationRuleTipText()
  {
    return "The combination rule used.";
  }
  




  public SelectedTag getCombinationRule()
  {
    return new SelectedTag(m_CombinationRule, TAGS_RULES);
  }
  




  public void setCombinationRule(SelectedTag newRule)
  {
    if (newRule.getTags() == TAGS_RULES) {
      m_CombinationRule = newRule.getSelectedTag().getID();
    }
  }
  




  public String toString()
  {
    if (m_Classifiers == null) {
      return "Vote: No model built yet.";
    }
    
    String result = "Vote combines";
    result = result + " the probability distributions of these base learners:\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result = result + '\t' + getClassifierSpec(i) + '\n';
    }
    result = result + "using the '";
    
    switch (m_CombinationRule) {
    case 1: 
      result = result + "Average of Probabilities";
      break;
    
    case 2: 
      result = result + "Product of Probabilities";
      break;
    
    case 3: 
      result = result + "Majority Voting";
      break;
    
    case 4: 
      result = result + "Minimum Probability";
      break;
    
    case 5: 
      result = result + "Maximum Probability";
      break;
    
    case 6: 
      result = result + "Median Probability";
      break;
    
    default: 
      throw new IllegalStateException("Unknown combination rule '" + m_CombinationRule + "'!");
    }
    
    result = result + "' combination rule \n";
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9093 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new Vote(), argv);
  }
}

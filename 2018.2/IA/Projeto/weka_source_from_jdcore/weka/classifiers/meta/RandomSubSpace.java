package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.REPTree;
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
import weka.filters.unsupervised.attribute.Remove;










































































































public class RandomSubSpace
  extends RandomizableIteratedSingleClassifierEnhancer
  implements WeightedInstancesHandler, TechnicalInformationHandler
{
  private static final long serialVersionUID = 1278172513912424947L;
  protected double m_SubSpaceSize = 0.5D;
  


  protected Classifier m_ZeroR;
  



  public RandomSubSpace()
  {
    m_Classifier = new REPTree();
  }
  





  public String globalInfo()
  {
    return "This method constructs a decision tree based classifier that maintains highest accuracy on training data and improves on generalization accuracy as it grows in complexity. The classifier consists of multiple trees constructed systematically by pseudorandomly selecting subsets of components of the feature vector, that is, trees constructed in randomly chosen subspaces.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Tin Kam Ho");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "The Random Subspace Method for Constructing Decision Forests");
    result.setValue(TechnicalInformation.Field.JOURNAL, "IEEE Transactions on Pattern Analysis and Machine Intelligence");
    result.setValue(TechnicalInformation.Field.VOLUME, "20");
    result.setValue(TechnicalInformation.Field.NUMBER, "8");
    result.setValue(TechnicalInformation.Field.PAGES, "832-844");
    result.setValue(TechnicalInformation.Field.URL, "http://citeseer.ist.psu.edu/ho98random.html");
    result.setValue(TechnicalInformation.Field.ISSN, "0162-8828");
    
    return result;
  }
  




  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.REPTree";
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSize of each subspace:\n\t\t< 1: percentage of the number of attributes\n\t\t>=1: absolute number of attributes\n", "P", 1, "-P"));
    




    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      result.addElement(enu.nextElement());
    }
    
    return result.elements();
  }
  


























































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setSubSpaceSize(Double.parseDouble(tmpStr));
    } else {
      setSubSpaceSize(0.5D);
    }
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-P");
    result.add("" + getSubSpaceSize());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String subSpaceSizeTipText()
  {
    return "Size of each subSpace: if less than 1 as a percentage of the number of attributes, otherwise the absolute number of attributes.";
  }
  






  public double getSubSpaceSize()
  {
    return m_SubSpaceSize;
  }
  




  public void setSubSpaceSize(double value)
  {
    m_SubSpaceSize = value;
  }
  







  protected int numberOfAttributes(int total, double fraction)
  {
    int k = (int)Math.round(fraction < 1.0D ? total * fraction : fraction);
    
    if (k > total)
      k = total;
    if (k < 1) {
      k = 1;
    }
    return k;
  }
  









  protected String randomSubSpace(Integer[] indices, int subSpaceSize, int classIndex, Random random)
  {
    Collections.shuffle(Arrays.asList(indices), random);
    StringBuffer sb = new StringBuffer("");
    for (int i = 0; i < subSpaceSize; i++) {
      sb.append(indices[i] + ",");
    }
    sb.append(classIndex);
    
    if (getDebug()) {
      System.out.println("subSPACE = " + sb);
    }
    return sb.toString();
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    

    if (data.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(data);
      return;
    }
    
    m_ZeroR = null;
    

    super.buildClassifier(data);
    
    Integer[] indices = new Integer[data.numAttributes() - 1];
    int classIndex = data.classIndex();
    int offset = 0;
    for (int i = 0; i < indices.length + 1; i++) {
      if (i != classIndex) {
        indices[(offset++)] = Integer.valueOf(i + 1);
      }
    }
    int subSpaceSize = numberOfAttributes(indices.length, getSubSpaceSize());
    Random random = data.getRandomNumberGenerator(m_Seed);
    
    for (int j = 0; j < m_Classifiers.length; j++) {
      if ((m_Classifier instanceof Randomizable)) {
        ((Randomizable)m_Classifiers[j]).setSeed(random.nextInt());
      }
      FilteredClassifier fc = new FilteredClassifier();
      fc.setClassifier(m_Classifiers[j]);
      m_Classifiers[j] = fc;
      Remove rm = new Remove();
      rm.setOptions(new String[] { "-V", "-R", randomSubSpace(indices, subSpaceSize, classIndex + 1, random) });
      fc.setFilter(rm);
      

      m_Classifiers[j].buildClassifier(data);
    }
  }
  









  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    
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
    if (m_ZeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    if (m_Classifiers == null) {
      return "RandomSubSpace: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("All the base classifiers: \n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append(m_Classifiers[i].toString() + "\n\n");
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new RandomSubSpace(), args);
  }
}

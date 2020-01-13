package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.AttributeStats;
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
import weka.core.UnsupportedClassTypeException;
import weka.core.Utils;


































































































































public class Decorate
  extends RandomizableIteratedSingleClassifierEnhancer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -6020193348750269931L;
  protected Vector m_Committee = null;
  

  protected int m_DesiredSize = 15;
  


  protected double m_ArtSize = 1.0D;
  

  protected Random m_Random = new Random(0L);
  

  protected Vector m_AttributeStats = null;
  




  public Decorate()
  {
    m_Classifier = new J48();
    m_NumIterations = 50;
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.J48";
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(8);
    
    newVector.addElement(new Option("\tDesired size of ensemble.\n\t(default 15)", "E", 1, "-E"));
    


    newVector.addElement(new Option("\tNumber of iterations.\n\t(default 50)", "I", 1, "-I <num>"));
    


    newVector.addElement(new Option("\tFactor that determines number of artificial examples to generate.\n\tSpecified proportional to training set size.\n\t(default 1.0)", "R", 1, "-R"));
    




    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    


    newVector.remove(4);
    
    return newVector.elements();
  }
  












































































  public void setOptions(String[] options)
    throws Exception
  {
    String desiredSize = Utils.getOption('E', options);
    if (desiredSize.length() != 0) {
      setDesiredSize(Integer.parseInt(desiredSize));
    } else {
      setDesiredSize(15);
    }
    
    String artSize = Utils.getOption('R', options);
    if (artSize.length() != 0) {
      setArtificialSize(Double.parseDouble(artSize));
    } else {
      setArtificialSize(1.0D);
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 4];
    
    int current = 0;
    options[(current++)] = "-E";options[(current++)] = ("" + getDesiredSize());
    options[(current++)] = "-R";options[(current++)] = ("" + getArtificialSize());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String desiredSizeTipText()
  {
    return "the desired number of member classifiers in the Decorate ensemble. Decorate may terminate before this size is reached (depending on the value of numIterations). Larger ensemble sizes usually lead to more accurate models, but increases training time and model complexity.";
  }
  







  public String numIterationsTipText()
  {
    return "the maximum number of Decorate iterations to run. Each iteration generates a classifier, but does not necessarily add it to the ensemble. Decorate stops when the desired ensemble size is reached. This parameter should be greater than equal to the desiredSize. If the desiredSize is not being reached it may help to increase this value.";
  }
  








  public String artificialSizeTipText()
  {
    return "determines the number of artificial examples to use during training. Specified as a proportion of the training data. Higher values can increase ensemble diversity.";
  }
  





  public String globalInfo()
  {
    return "DECORATE is a meta-learner for building diverse ensembles of classifiers by using specially constructed artificial training examples. Comprehensive experiments have demonstrated that this technique is consistently more accurate than the base classifier, Bagging and Random Forests.Decorate also obtains higher accuracy than Boosting on small training sets, and achieves comparable performance on larger training sets. \n\nFor more details see: \n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "P. Melville and R. J. Mooney");
    result.setValue(TechnicalInformation.Field.TITLE, "Constructing Diverse Classifier Ensembles Using Artificial Training Examples");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Eighteenth International Joint Conference on Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.PAGES, "505-510");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "P. Melville and R. J. Mooney");
    additional.setValue(TechnicalInformation.Field.TITLE, "Creating Diversity in Ensembles Using Artificial Data");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Information Fusion: Special Issue on Diversity in Multiclassifier Systems");
    additional.setValue(TechnicalInformation.Field.YEAR, "2004");
    additional.setValue(TechnicalInformation.Field.NOTE, "submitted");
    
    return result;
  }
  




  public double getArtificialSize()
  {
    return m_ArtSize;
  }
  




  public void setArtificialSize(double newArtSize)
  {
    m_ArtSize = newArtSize;
  }
  




  public int getDesiredSize()
  {
    return m_DesiredSize;
  }
  




  public void setDesiredSize(int newDesiredSize)
  {
    m_DesiredSize = newDesiredSize;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    

    result.setMinimumNumberInstances(m_DesiredSize);
    
    return result;
  }
  




  public void buildClassifier(Instances data)
    throws Exception
  {
    if (m_Classifier == null) {
      throw new Exception("A base classifier has not been specified!");
    }
    

    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    

    if (m_Seed == -1) m_Random = new Random(); else {
      m_Random = new Random(m_Seed);
    }
    int i = 1;
    int numTrials = 1;
    Instances divData = new Instances(data);
    Instances artData = null;
    

    int artSize = (int)(Math.abs(m_ArtSize) * divData.numInstances());
    if (artSize == 0) artSize = 1;
    computeStats(data);
    

    m_Committee = new Vector();
    Classifier newClassifier = m_Classifier;
    newClassifier.buildClassifier(divData);
    m_Committee.add(newClassifier);
    double eComm = computeError(divData);
    if (m_Debug) { System.out.println("Initialize:\tClassifier " + i + " added to ensemble. Ensemble error = " + eComm);
    }
    
    while ((i < m_DesiredSize) && (numTrials < m_NumIterations))
    {
      artData = generateArtificialData(artSize, data);
      

      labelData(artData);
      addInstances(divData, artData);
      

      Classifier[] tmp = Classifier.makeCopies(m_Classifier, 1);
      newClassifier = tmp[0];
      newClassifier.buildClassifier(divData);
      
      removeInstances(divData, artSize);
      

      m_Committee.add(newClassifier);
      double currError = computeError(divData);
      if (currError <= eComm) {
        i++;
        eComm = currError;
        if (m_Debug) System.out.println("Iteration: " + (1 + numTrials) + "\tClassifier " + i + " added to ensemble. Ensemble error = " + eComm);
      } else {
        m_Committee.removeElementAt(m_Committee.size() - 1);
      }
      numTrials++;
    }
  }
  




  protected void computeStats(Instances data)
    throws Exception
  {
    int numAttributes = data.numAttributes();
    m_AttributeStats = new Vector(numAttributes);
    
    for (int j = 0; j < numAttributes; j++) {
      if (data.attribute(j).isNominal())
      {
        int[] nomCounts = attributeStatsnominalCounts;
        double[] counts = new double[nomCounts.length];
        if (counts.length < 2) { throw new Exception("Nominal attribute has less than two distinct values!");
        }
        for (int i = 0; i < counts.length; i++)
          counts[i] = (nomCounts[i] + 1);
        Utils.normalize(counts);
        double[] stats = new double[counts.length - 1];
        stats[0] = counts[0];
        
        for (int i = 1; i < stats.length; i++)
          stats[i] = (stats[(i - 1)] + counts[i]);
        m_AttributeStats.add(j, stats);
      } else if (data.attribute(j).isNumeric())
      {
        double[] stats = new double[2];
        stats[0] = data.meanOrMode(j);
        stats[1] = Math.sqrt(data.variance(j));
        m_AttributeStats.add(j, stats);
      } else { System.err.println("Decorate can only handle numeric and nominal values.");
      }
    }
  }
  




  protected Instances generateArtificialData(int artSize, Instances data)
  {
    int numAttributes = data.numAttributes();
    Instances artData = new Instances(data, artSize);
    


    for (int i = 0; i < artSize; i++) {
      double[] att = new double[numAttributes];
      for (int j = 0; j < numAttributes; j++)
        if (data.attribute(j).isNominal())
        {
          double[] stats = (double[])m_AttributeStats.get(j);
          att[j] = selectIndexProbabilistically(stats);
        }
        else if (data.attribute(j).isNumeric())
        {

          double[] stats = (double[])m_AttributeStats.get(j);
          att[j] = (m_Random.nextGaussian() * stats[1] + stats[0]);
        } else { System.err.println("Decorate can only handle numeric and nominal values.");
        }
      Instance artInstance = new Instance(1.0D, att);
      artData.add(artInstance);
    }
    return artData;
  }
  








  protected void labelData(Instances artData)
    throws Exception
  {
    for (int i = 0; i < artData.numInstances(); i++) {
      Instance curr = artData.instance(i);
      
      double[] probs = distributionForInstance(curr);
      
      curr.setClassValue(inverseLabel(probs));
    }
  }
  







  protected int inverseLabel(double[] probs)
    throws Exception
  {
    double[] invProbs = new double[probs.length];
    
    for (int i = 0; i < probs.length; i++) {
      if (probs[i] == 0.0D) {
        invProbs[i] = (Double.MAX_VALUE / probs.length);
      }
      else
      {
        invProbs[i] = (1.0D / probs[i]);
      }
    }
    Utils.normalize(invProbs);
    double[] cdf = new double[invProbs.length];
    
    cdf[0] = invProbs[0];
    for (int i = 1; i < invProbs.length; i++) {
      invProbs[i] += cdf[(i - 1)];
    }
    
    if (Double.isNaN(cdf[(invProbs.length - 1)]))
      System.err.println("Cumulative class membership probability is NaN!");
    return selectIndexProbabilistically(cdf);
  }
  





  protected int selectIndexProbabilistically(double[] cdf)
  {
    double rnd = m_Random.nextDouble();
    int index = 0;
    while ((index < cdf.length) && (rnd > cdf[index])) {
      index++;
    }
    return index;
  }
  





  protected void removeInstances(Instances data, int numRemove)
  {
    int num = data.numInstances();
    for (int i = num - 1; i > num - 1 - numRemove; i--) {
      data.delete(i);
    }
  }
  





  protected void addInstances(Instances data, Instances newData)
  {
    for (int i = 0; i < newData.numInstances(); i++) {
      data.add(newData.instance(i));
    }
  }
  




  protected double computeError(Instances data)
    throws Exception
  {
    double error = 0.0D;
    int numInstances = data.numInstances();
    

    for (int i = 0; i < numInstances; i++) {
      Instance curr = data.instance(i);
      
      if (curr.classValue() != (int)classifyInstance(curr)) error += 1.0D;
    }
    return error / numInstances;
  }
  





  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (instance.classAttribute().isNumeric()) {
      throw new UnsupportedClassTypeException("Decorate can't handle a numeric class!");
    }
    double[] sums = new double[instance.numClasses()];
    

    for (int i = 0; i < m_Committee.size(); i++) {
      Classifier curr = (Classifier)m_Committee.get(i);
      double[] newProbs = curr.distributionForInstance(instance);
      for (int j = 0; j < newProbs.length; j++)
        sums[j] += newProbs[j];
    }
    if (Utils.eq(Utils.sum(sums), 0.0D)) {
      return sums;
    }
    Utils.normalize(sums);
    return sums;
  }
  






  public String toString()
  {
    if (m_Committee == null) {
      return "Decorate: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("Decorate base classifiers: \n\n");
    for (int i = 0; i < m_Committee.size(); i++)
      text.append(((Classifier)m_Committee.get(i)).toString() + "\n\n");
    text.append("Number of classifier in the ensemble: " + m_Committee.size() + "\n");
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8037 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new Decorate(), argv);
  }
}

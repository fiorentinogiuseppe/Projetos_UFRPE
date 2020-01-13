package weka.classifiers.lazy;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
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
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;





































































































public class LWL
  extends SingleClassifierEnhancer
  implements UpdateableClassifier, WeightedInstancesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 1979797405383665815L;
  protected Instances m_Train;
  protected int m_kNN = -1;
  

  protected int m_WeightKernel = 0;
  

  protected boolean m_UseAllK = true;
  



  protected NearestNeighbourSearch m_NNSearch = new LinearNNSearch();
  
  protected static final int LINEAR = 0;
  
  protected static final int EPANECHNIKOV = 1;
  
  protected static final int TRICUBE = 2;
  
  protected static final int INVERSE = 3;
  
  protected static final int GAUSS = 4;
  
  protected static final int CONSTANT = 5;
  
  protected Classifier m_ZeroR;
  

  public String globalInfo()
  {
    return "Locally weighted learning. Uses an instance-based algorithm to assign instance weights which are then used by a specified WeightedInstancesHandler.\nCan do classification (e.g. using naive Bayes) or regression (e.g. using linear regression).\n\nFor more info, see\n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Eibe Frank and Mark Hall and Bernhard Pfahringer");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.TITLE, "Locally Weighted Naive Bayes");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "19th Conference in Uncertainty in Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.PAGES, "249-256");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "C. Atkeson and A. Moore and S. Schaal");
    additional.setValue(TechnicalInformation.Field.YEAR, "1996");
    additional.setValue(TechnicalInformation.Field.TITLE, "Locally weighted learning");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "AI Review");
    
    return result;
  }
  


  public LWL()
  {
    m_Classifier = new DecisionStump();
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.DecisionStump";
  }
  




  public Enumeration enumerateMeasures()
  {
    return m_NNSearch.enumerateMeasures();
  }
  






  public double getMeasure(String additionalMeasureName)
  {
    return m_NNSearch.getMeasure(additionalMeasureName);
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    newVector.addElement(new Option("\tThe nearest neighbour search algorithm to use (default: weka.core.neighboursearch.LinearNNSearch).\n", "A", 0, "-A"));
    


    newVector.addElement(new Option("\tSet the number of neighbours used to set the kernel bandwidth.\n\t(default all)", "K", 1, "-K <number of neighbours>"));
    


    newVector.addElement(new Option("\tSet the weighting kernel shape to use. 0=Linear, 1=Epanechnikov,\n\t2=Tricube, 3=Inverse, 4=Gaussian.\n\t(default 0 = Linear)", "U", 1, "-U <number of weighting method>"));
    




    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  







































  public void setOptions(String[] options)
    throws Exception
  {
    String knnString = Utils.getOption('K', options);
    if (knnString.length() != 0) {
      setKNN(Integer.parseInt(knnString));
    } else {
      setKNN(-1);
    }
    
    String weightString = Utils.getOption('U', options);
    if (weightString.length() != 0) {
      setWeightingKernel(Integer.parseInt(weightString));
    } else {
      setWeightingKernel(0);
    }
    
    String nnSearchClass = Utils.getOption('A', options);
    if (nnSearchClass.length() != 0) {
      String[] nnSearchClassSpec = Utils.splitOptions(nnSearchClass);
      if (nnSearchClassSpec.length == 0) {
        throw new Exception("Invalid NearestNeighbourSearch algorithm specification string.");
      }
      
      String className = nnSearchClassSpec[0];
      nnSearchClassSpec[0] = "";
      
      setNearestNeighbourSearchAlgorithm((NearestNeighbourSearch)Utils.forName(NearestNeighbourSearch.class, className, nnSearchClassSpec));


    }
    else
    {

      setNearestNeighbourSearchAlgorithm(new LinearNNSearch());
    }
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 6];
    
    int current = 0;
    
    options[(current++)] = "-U";options[(current++)] = ("" + getWeightingKernel());
    if ((getKNN() == 0) && (m_UseAllK)) {
      options[(current++)] = "-K";options[(current++)] = "-1";
    }
    else {
      options[(current++)] = "-K";options[(current++)] = ("" + getKNN());
    }
    options[(current++)] = "-A";
    options[(current++)] = (m_NNSearch.getClass().getName() + " " + Utils.joinOptions(m_NNSearch.getOptions()));
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    return options;
  }
  




  public String KNNTipText()
  {
    return "How many neighbours are used to determine the width of the weighting function (<= 0 means all neighbours).";
  }
  








  public void setKNN(int knn)
  {
    m_kNN = knn;
    if (knn <= 0) {
      m_kNN = 0;
      m_UseAllK = true;
    } else {
      m_UseAllK = false;
    }
  }
  







  public int getKNN()
  {
    return m_kNN;
  }
  




  public String weightingKernelTipText()
  {
    return "Determines weighting function. [0 = Linear, 1 = Epnechnikov,2 = Tricube, 3 = Inverse, 4 = Gaussian and 5 = Constant. (default 0 = Linear)].";
  }
  










  public void setWeightingKernel(int kernel)
  {
    if ((kernel != 0) && (kernel != 1) && (kernel != 2) && (kernel != 3) && (kernel != 4) && (kernel != 5))
    {




      return;
    }
    m_WeightKernel = kernel;
  }
  






  public int getWeightingKernel()
  {
    return m_WeightKernel;
  }
  




  public String nearestNeighbourSearchAlgorithmTipText()
  {
    return "The nearest neighbour search algorithm to use (Default: LinearNN).";
  }
  



  public NearestNeighbourSearch getNearestNeighbourSearchAlgorithm()
  {
    return m_NNSearch;
  }
  




  public void setNearestNeighbourSearchAlgorithm(NearestNeighbourSearch nearestNeighbourSearchAlgorithm)
  {
    m_NNSearch = nearestNeighbourSearchAlgorithm;
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    Capabilities result;
    
    if (m_Classifier != null) {
      result = m_Classifier.getCapabilities();
    } else {
      result = super.getCapabilities();
    }
    result.setMinimumNumberInstances(0);
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    return result;
  }
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    if (!(m_Classifier instanceof WeightedInstancesHandler)) {
      throw new IllegalArgumentException("Classifier must be a WeightedInstancesHandler!");
    }
    


    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    

    if (instances.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(instances);
      return;
    }
    
    m_ZeroR = null;
    

    m_Train = new Instances(instances, 0, instances.numInstances());
    
    m_NNSearch.setInstances(m_Train);
  }
  






  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (m_Train == null) {
      throw new Exception("No training instance structure set!");
    }
    if (!m_Train.equalHeaders(instance.dataset())) {
      throw new Exception("Incompatible instance types");
    }
    if (!instance.classIsMissing()) {
      m_NNSearch.update(instance);
      m_Train.add(instance);
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    
    if (m_Train.numInstances() == 0) {
      throw new Exception("No training instances!");
    }
    
    m_NNSearch.addInstanceInfo(instance);
    
    int k = m_Train.numInstances();
    if ((!m_UseAllK) && (m_kNN < k) && (m_WeightKernel != 3) && (m_WeightKernel != 4))
    {

      k = m_kNN;
    }
    
    Instances neighbours = m_NNSearch.kNearestNeighbours(instance, k);
    double[] distances = m_NNSearch.getDistances();
    
    if (m_Debug) {
      System.out.println("Test Instance: " + instance);
      System.out.println("For " + k + " kept " + neighbours.numInstances() + " out of " + m_Train.numInstances() + " instances.");
    }
    


    if (k > distances.length) {
      k = distances.length;
    }
    if (m_Debug) {
      System.out.println("Instance Distances");
      for (int i = 0; i < distances.length; i++) {
        System.out.println("" + distances[i]);
      }
    }
    

    double bandwidth = distances[(k - 1)];
    

    if (bandwidth <= 0.0D)
    {
      for (int i = 0; i < distances.length; i++) {
        distances[i] = 1.0D;
      }
    } else {
      for (int i = 0; i < distances.length; i++) {
        distances[i] /= bandwidth;
      }
    }
    
    for (int i = 0; i < distances.length; i++) {
      switch (m_WeightKernel) {
      case 0: 
        distances[i] = (1.0001D - distances[i]);
        break;
      case 1: 
        distances[i] = (0.75D * (1.0001D - distances[i] * distances[i]));
        break;
      case 2: 
        distances[i] = Math.pow(1.0001D - Math.pow(distances[i], 3.0D), 3.0D);
        break;
      
      case 5: 
        distances[i] = 1.0D;
        break;
      case 3: 
        distances[i] = (1.0D / (1.0D + distances[i]));
        break;
      case 4: 
        distances[i] = Math.exp(-distances[i] * distances[i]);
      }
      
    }
    
    if (m_Debug) {
      System.out.println("Instance Weights");
      for (int i = 0; i < distances.length; i++) {
        System.out.println("" + distances[i]);
      }
    }
    

    double sumOfWeights = 0.0D;double newSumOfWeights = 0.0D;
    for (int i = 0; i < distances.length; i++) {
      double weight = distances[i];
      Instance inst = neighbours.instance(i);
      sumOfWeights += inst.weight();
      newSumOfWeights += inst.weight() * weight;
      inst.setWeight(inst.weight() * weight);
    }
    


    for (int i = 0; i < neighbours.numInstances(); i++) {
      Instance inst = neighbours.instance(i);
      inst.setWeight(inst.weight() * sumOfWeights / newSumOfWeights);
    }
    

    m_Classifier.buildClassifier(neighbours);
    
    if (m_Debug) {
      System.out.println("Classifying test instance: " + instance);
      System.out.println("Built base classifier:\n" + m_Classifier.toString());
    }
    


    return m_Classifier.distributionForInstance(instance);
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
    
    if (m_Train == null) {
      return "Locally weighted learning: No model built yet.";
    }
    String result = "Locally weighted learning\n===========================\n";
    

    result = result + "Using classifier: " + m_Classifier.getClass().getName() + "\n";
    
    switch (m_WeightKernel) {
    case 0: 
      result = result + "Using linear weighting kernels\n";
      break;
    case 1: 
      result = result + "Using epanechnikov weighting kernels\n";
      break;
    case 2: 
      result = result + "Using tricube weighting kernels\n";
      break;
    case 3: 
      result = result + "Using inverse-distance weighting kernels\n";
      break;
    case 4: 
      result = result + "Using gaussian weighting kernels\n";
      break;
    case 5: 
      result = result + "Using constant weighting kernels\n";
    }
    
    result = result + "Using " + (m_UseAllK ? "all" : new StringBuilder().append("").append(m_kNN).toString()) + " neighbours";
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5011 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new LWL(), argv);
  }
}

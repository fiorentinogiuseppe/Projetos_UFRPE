package weka.classifiers.lazy;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.rules.ZeroR;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
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
import weka.core.neighboursearch.CoverTree;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;































































































































public class IBk
  extends Classifier
  implements OptionHandler, UpdateableClassifier, WeightedInstancesHandler, TechnicalInformationHandler, AdditionalMeasureProducer
{
  static final long serialVersionUID = -3080186098777067172L;
  protected Instances m_Train;
  protected int m_NumClasses;
  protected int m_ClassType;
  protected int m_kNN;
  protected int m_kNNUpper;
  protected boolean m_kNNValid;
  protected int m_WindowSize;
  protected int m_DistanceWeighting;
  protected boolean m_CrossValidate;
  protected boolean m_MeanSquared;
  public static final int WEIGHT_NONE = 1;
  public static final int WEIGHT_INVERSE = 2;
  public static final int WEIGHT_SIMILARITY = 4;
  public static final Tag[] TAGS_WEIGHTING = { new Tag(1, "No distance weighting"), new Tag(2, "Weight by 1/distance"), new Tag(4, "Weight by 1-distance") };
  





  protected NearestNeighbourSearch m_NNSearch = new LinearNNSearch();
  



  protected double m_NumAttributesUsed;
  



  protected ZeroR m_defaultModel;
  



  public IBk(int k)
  {
    init();
    setKNN(k);
  }
  




  public IBk()
  {
    init();
  }
  





  public String globalInfo()
  {
    return "K-nearest neighbours classifier. Can select appropriate value of K based on cross-validation. Can also do distance weighting.\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "D. Aha and D. Kibler");
    result.setValue(TechnicalInformation.Field.YEAR, "1991");
    result.setValue(TechnicalInformation.Field.TITLE, "Instance-based learning algorithms");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "6");
    result.setValue(TechnicalInformation.Field.PAGES, "37-66");
    
    return result;
  }
  




  public String KNNTipText()
  {
    return "The number of neighbours to use.";
  }
  




  public void setKNN(int k)
  {
    m_kNN = k;
    m_kNNUpper = k;
    m_kNNValid = false;
  }
  





  public int getKNN()
  {
    return m_kNN;
  }
  




  public String windowSizeTipText()
  {
    return "Gets the maximum number of instances allowed in the training pool. The addition of new instances above this value will result in old instances being removed. A value of 0 signifies no limit to the number of training instances.";
  }
  











  public int getWindowSize()
  {
    return m_WindowSize;
  }
  








  public void setWindowSize(int newWindowSize)
  {
    m_WindowSize = newWindowSize;
  }
  





  public String distanceWeightingTipText()
  {
    return "Gets the distance weighting method used.";
  }
  






  public SelectedTag getDistanceWeighting()
  {
    return new SelectedTag(m_DistanceWeighting, TAGS_WEIGHTING);
  }
  






  public void setDistanceWeighting(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_WEIGHTING) {
      m_DistanceWeighting = newMethod.getSelectedTag().getID();
    }
  }
  





  public String meanSquaredTipText()
  {
    return "Whether the mean squared error is used rather than mean absolute error when doing cross-validation for regression problems.";
  }
  







  public boolean getMeanSquared()
  {
    return m_MeanSquared;
  }
  






  public void setMeanSquared(boolean newMeanSquared)
  {
    m_MeanSquared = newMeanSquared;
  }
  





  public String crossValidateTipText()
  {
    return "Whether hold-one-out cross-validation will be used to select the best k value between 1 and the value specified as the KNN parameter.";
  }
  








  public boolean getCrossValidate()
  {
    return m_CrossValidate;
  }
  






  public void setCrossValidate(boolean newCrossValidate)
  {
    m_CrossValidate = newCrossValidate;
  }
  




  public String nearestNeighbourSearchAlgorithmTipText()
  {
    return "The nearest neighbour search algorithm to use (Default: weka.core.neighboursearch.LinearNNSearch).";
  }
  




  public NearestNeighbourSearch getNearestNeighbourSearchAlgorithm()
  {
    return m_NNSearch;
  }
  




  public void setNearestNeighbourSearchAlgorithm(NearestNeighbourSearch nearestNeighbourSearchAlgorithm)
  {
    m_NNSearch = nearestNeighbourSearchAlgorithm;
  }
  





  public int getNumTraining()
  {
    return m_Train.numInstances();
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
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
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
    m_ClassType = instances.classAttribute().type();
    m_Train = new Instances(instances, 0, instances.numInstances());
    

    if ((m_WindowSize > 0) && (instances.numInstances() > m_WindowSize)) {
      m_Train = new Instances(m_Train, m_Train.numInstances() - m_WindowSize, m_WindowSize);
    }
    


    m_NumAttributesUsed = 0.0D;
    for (int i = 0; i < m_Train.numAttributes(); i++) {
      if ((i != m_Train.classIndex()) && ((m_Train.attribute(i).isNominal()) || (m_Train.attribute(i).isNumeric())))
      {

        m_NumAttributesUsed += 1.0D;
      }
    }
    
    m_NNSearch.setInstances(m_Train);
    

    m_kNNValid = false;
    
    m_defaultModel = new ZeroR();
    m_defaultModel.buildClassifier(instances);
  }
  






  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (!m_Train.equalHeaders(instance.dataset())) {
      throw new Exception("Incompatible instance types");
    }
    if (instance.classIsMissing()) {
      return;
    }
    
    m_Train.add(instance);
    m_NNSearch.update(instance);
    m_kNNValid = false;
    if ((m_WindowSize > 0) && (m_Train.numInstances() > m_WindowSize)) {
      boolean deletedInstance = false;
      while (m_Train.numInstances() > m_WindowSize) {
        m_Train.delete(0);
        deletedInstance = true;
      }
      
      if (deletedInstance == true) {
        m_NNSearch.setInstances(m_Train);
      }
    }
  }
  





  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_Train.numInstances() == 0)
    {
      return m_defaultModel.distributionForInstance(instance);
    }
    if ((m_WindowSize > 0) && (m_Train.numInstances() > m_WindowSize)) {
      m_kNNValid = false;
      boolean deletedInstance = false;
      while (m_Train.numInstances() > m_WindowSize) {
        m_Train.delete(0);
      }
      
      if (deletedInstance == true) {
        m_NNSearch.setInstances(m_Train);
      }
    }
    
    if ((!m_kNNValid) && (m_CrossValidate) && (m_kNNUpper >= 1)) {
      crossValidate();
    }
    
    m_NNSearch.addInstanceInfo(instance);
    
    Instances neighbours = m_NNSearch.kNearestNeighbours(instance, m_kNN);
    double[] distances = m_NNSearch.getDistances();
    double[] distribution = makeDistribution(neighbours, distances);
    
    return distribution;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(8);
    
    newVector.addElement(new Option("\tWeight neighbours by the inverse of their distance\n\t(use when k > 1)", "I", 0, "-I"));
    


    newVector.addElement(new Option("\tWeight neighbours by 1 - their distance\n\t(use when k > 1)", "F", 0, "-F"));
    


    newVector.addElement(new Option("\tNumber of nearest neighbours (k) used in classification.\n\t(Default = 1)", "K", 1, "-K <number of neighbors>"));
    


    newVector.addElement(new Option("\tMinimise mean squared error rather than mean absolute\n\terror when using -X option with numeric prediction.", "E", 0, "-E"));
    


    newVector.addElement(new Option("\tMaximum number of training instances maintained.\n\tTraining instances are dropped FIFO. (Default = no window)", "W", 1, "-W <window size>"));
    


    newVector.addElement(new Option("\tSelect the number of nearest neighbours between 1\n\tand the k value specified using hold-one-out evaluation\n\ton the training data (use when k > 1)", "X", 0, "-X"));
    



    newVector.addElement(new Option("\tThe nearest neighbour search algorithm to use (default: weka.core.neighboursearch.LinearNNSearch).\n", "A", 0, "-A"));
    



    return newVector.elements();
  }
  







































  public void setOptions(String[] options)
    throws Exception
  {
    String knnString = Utils.getOption('K', options);
    if (knnString.length() != 0) {
      setKNN(Integer.parseInt(knnString));
    } else {
      setKNN(1);
    }
    String windowString = Utils.getOption('W', options);
    if (windowString.length() != 0) {
      setWindowSize(Integer.parseInt(windowString));
    } else {
      setWindowSize(0);
    }
    if (Utils.getFlag('I', options)) {
      setDistanceWeighting(new SelectedTag(2, TAGS_WEIGHTING));
    } else if (Utils.getFlag('F', options)) {
      setDistanceWeighting(new SelectedTag(4, TAGS_WEIGHTING));
    } else {
      setDistanceWeighting(new SelectedTag(1, TAGS_WEIGHTING));
    }
    setCrossValidate(Utils.getFlag('X', options));
    setMeanSquared(Utils.getFlag('E', options));
    
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
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[11];
    int current = 0;
    options[(current++)] = "-K";options[(current++)] = ("" + getKNN());
    options[(current++)] = "-W";options[(current++)] = ("" + m_WindowSize);
    if (getCrossValidate()) {
      options[(current++)] = "-X";
    }
    if (getMeanSquared()) {
      options[(current++)] = "-E";
    }
    if (m_DistanceWeighting == 2) {
      options[(current++)] = "-I";
    } else if (m_DistanceWeighting == 4) {
      options[(current++)] = "-F";
    }
    
    options[(current++)] = "-A";
    options[(current++)] = (m_NNSearch.getClass().getName() + " " + Utils.joinOptions(m_NNSearch.getOptions()));
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  






  public Enumeration enumerateMeasures()
  {
    if (m_CrossValidate) {
      Enumeration enm = m_NNSearch.enumerateMeasures();
      Vector measures = new Vector();
      while (enm.hasMoreElements())
        measures.add(enm.nextElement());
      measures.add("measureKNN");
      return measures.elements();
    }
    
    return m_NNSearch.enumerateMeasures();
  }
  









  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.equals("measureKNN")) {
      return m_kNN;
    }
    return m_NNSearch.getMeasure(additionalMeasureName);
  }
  






  public String toString()
  {
    if (m_Train == null) {
      return "IBk: No model built yet.";
    }
    
    if (m_Train.numInstances() == 0) {
      return "Warning: no training instances - ZeroR model used.";
    }
    
    if ((!m_kNNValid) && (m_CrossValidate)) {
      crossValidate();
    }
    


    String result = "IB1 instance-based classifier\nusing " + m_kNN;
    

    switch (m_DistanceWeighting) {
    case 2: 
      result = result + " inverse-distance-weighted";
      break;
    case 4: 
      result = result + " similarity-weighted";
    }
    
    result = result + " nearest neighbour(s) for classification\n";
    
    if (m_WindowSize != 0) {
      result = result + "using a maximum of " + m_WindowSize + " (windowed) training instances\n";
    }
    
    return result;
  }
  



  protected void init()
  {
    setKNN(1);
    m_WindowSize = 0;
    m_DistanceWeighting = 1;
    m_CrossValidate = false;
    m_MeanSquared = false;
  }
  








  protected double[] makeDistribution(Instances neighbours, double[] distances)
    throws Exception
  {
    double total = 0.0D;
    double[] distribution = new double[m_NumClasses];
    

    if (m_ClassType == 1) {
      for (int i = 0; i < m_NumClasses; i++) {
        distribution[i] = (1.0D / Math.max(1, m_Train.numInstances()));
      }
      total = m_NumClasses / Math.max(1, m_Train.numInstances());
    }
    
    for (int i = 0; i < neighbours.numInstances(); i++)
    {
      Instance current = neighbours.instance(i);
      distances[i] *= distances[i];
      distances[i] = Math.sqrt(distances[i] / m_NumAttributesUsed);
      double weight; switch (m_DistanceWeighting) {
      case 2: 
        weight = 1.0D / (distances[i] + 0.001D);
        break;
      case 4: 
        weight = 1.0D - distances[i];
        break;
      default: 
        weight = 1.0D;
      }
      
      weight *= current.weight();
      try {
        switch (m_ClassType) {
        case 1: 
          distribution[((int)current.classValue())] += weight;
          break;
        case 0: 
          distribution[0] += current.classValue() * weight;
        }
      }
      catch (Exception ex) {
        throw new Error("Data has no class attribute!");
      }
      total += weight;
    }
    

    if (total > 0.0D) {
      Utils.normalize(distribution, total);
    }
    return distribution;
  }
  





  protected void crossValidate()
  {
    try
    {
      if ((m_NNSearch instanceof CoverTree)) {
        throw new Exception("CoverTree doesn't support hold-one-out cross-validation. Use some other NN method.");
      }
      

      double[] performanceStats = new double[m_kNNUpper];
      double[] performanceStatsSq = new double[m_kNNUpper];
      
      for (int i = 0; i < m_kNNUpper; i++) {
        performanceStats[i] = 0.0D;
        performanceStatsSq[i] = 0.0D;
      }
      

      m_kNN = m_kNNUpper;
      


      for (int i = 0; i < m_Train.numInstances(); i++) {
        if ((m_Debug) && (i % 50 == 0)) {
          System.err.print("Cross validating " + i + "/" + m_Train.numInstances() + "\r");
        }
        
        Instance instance = m_Train.instance(i);
        Instances neighbours = m_NNSearch.kNearestNeighbours(instance, m_kNN);
        double[] origDistances = m_NNSearch.getDistances();
        
        for (int j = m_kNNUpper - 1; j >= 0; j--)
        {
          double[] convertedDistances = new double[origDistances.length];
          System.arraycopy(origDistances, 0, convertedDistances, 0, origDistances.length);
          
          double[] distribution = makeDistribution(neighbours, convertedDistances);
          
          double thisPrediction = Utils.maxIndex(distribution);
          if (m_Train.classAttribute().isNumeric()) {
            thisPrediction = distribution[0];
            double err = thisPrediction - instance.classValue();
            performanceStatsSq[j] += err * err;
            performanceStats[j] += Math.abs(err);
          }
          else if (thisPrediction != instance.classValue()) {
            performanceStats[j] += 1.0D;
          }
          
          if (j >= 1) {
            neighbours = pruneToK(neighbours, convertedDistances, j);
          }
        }
      }
      

      for (int i = 0; i < m_kNNUpper; i++) {
        if (m_Debug) {
          System.err.print("Hold-one-out performance of " + (i + 1) + " neighbors ");
        }
        
        if (m_Train.classAttribute().isNumeric()) {
          if (m_Debug) {
            if (m_MeanSquared) {
              System.err.println("(RMSE) = " + Math.sqrt(performanceStatsSq[i] / m_Train.numInstances()));
            }
            else
            {
              System.err.println("(MAE) = " + performanceStats[i] / m_Train.numInstances());
            }
            
          }
          
        }
        else if (m_Debug) {
          System.err.println("(%ERR) = " + 100.0D * performanceStats[i] / m_Train.numInstances());
        }
      }
      






      double[] searchStats = performanceStats;
      if ((m_Train.classAttribute().isNumeric()) && (m_MeanSquared)) {
        searchStats = performanceStatsSq;
      }
      double bestPerformance = NaN.0D;
      int bestK = 1;
      for (int i = 0; i < m_kNNUpper; i++) {
        if ((Double.isNaN(bestPerformance)) || (bestPerformance > searchStats[i]))
        {
          bestPerformance = searchStats[i];
          bestK = i + 1;
        }
      }
      m_kNN = bestK;
      if (m_Debug) {
        System.err.println("Selected k = " + bestK);
      }
      
      m_kNNValid = true;
    } catch (Exception ex) {
      throw new Error("Couldn't optimize by cross-validation: " + ex.getMessage());
    }
  }
  










  public Instances pruneToK(Instances neighbours, double[] distances, int k)
  {
    if ((neighbours == null) || (distances == null) || (neighbours.numInstances() == 0)) {
      return null;
    }
    if (k < 1) {
      k = 1;
    }
    
    int currentK = 0;
    
    for (int i = 0; i < neighbours.numInstances(); i++) {
      currentK++;
      double currentDist = distances[i];
      if ((currentK > k) && (currentDist != distances[(i - 1)])) {
        currentK--;
        neighbours = new Instances(neighbours, 0, currentK);
        break;
      }
    }
    
    return neighbours;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10069 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new IBk(), argv);
  }
}

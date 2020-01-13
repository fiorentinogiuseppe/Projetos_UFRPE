package weka.classifiers.meta;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;






































































































public class RacedIncrementalLogitBoost
  extends RandomizableSingleClassifierEnhancer
  implements UpdateableClassifier, TechnicalInformationHandler
{
  static final long serialVersionUID = 908598343772170052L;
  public static final int PRUNETYPE_NONE = 0;
  public static final int PRUNETYPE_LOGLIKELIHOOD = 1;
  public static final Tag[] TAGS_PRUNETYPE = { new Tag(0, "No pruning"), new Tag(1, "Log likelihood pruning") };
  



  protected FastVector m_committees;
  


  protected int m_PruningType = 1;
  

  protected boolean m_UseResampling = false;
  

  protected int m_NumClasses;
  

  protected static final double Z_MAX = 4.0D;
  

  protected Instances m_NumericClassData;
  

  protected Attribute m_ClassAttribute;
  

  protected int m_minChunkSize = 500;
  

  protected int m_maxChunkSize = 2000;
  

  protected int m_validationChunkSize = 1000;
  

  protected int m_numInstancesConsumed;
  

  protected Instances m_validationSet;
  

  protected Instances m_currentSet;
  

  protected Committee m_bestCommittee;
  

  protected ZeroR m_zeroR = null;
  

  protected boolean m_validationSetChanged;
  

  protected int m_maxBatchSizeRequired;
  

  protected Random m_RandomInstance = null;
  




  public RacedIncrementalLogitBoost()
  {
    m_Classifier = new DecisionStump();
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.DecisionStump";
  }
  


  protected class Committee
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 5559880306684082199L;
    

    protected int m_chunkSize;
    

    protected int m_instancesConsumed;
    
    protected FastVector m_models;
    
    protected double m_lastValidationError;
    
    protected double m_lastLogLikelihood;
    
    protected boolean m_modelHasChanged;
    
    protected boolean m_modelHasChangedLL;
    
    protected double[][] m_validationFs;
    
    protected double[][] m_newValidationFs;
    

    public Committee(int chunkSize)
    {
      m_chunkSize = chunkSize;
      m_instancesConsumed = 0;
      m_models = new FastVector();
      m_lastValidationError = 1.0D;
      m_lastLogLikelihood = Double.MAX_VALUE;
      m_modelHasChanged = true;
      m_modelHasChangedLL = true;
      m_validationFs = new double[m_validationChunkSize][m_NumClasses];
      m_newValidationFs = new double[m_validationChunkSize][m_NumClasses];
    }
    





    public boolean update()
      throws Exception
    {
      boolean hasChanged = false;
      while (m_currentSet.numInstances() - m_instancesConsumed >= m_chunkSize) {
        Classifier[] newModel = boost(new Instances(m_currentSet, m_instancesConsumed, m_chunkSize));
        for (int i = 0; i < m_validationSet.numInstances(); i++) {
          m_newValidationFs[i] = updateFS(m_validationSet.instance(i), newModel, m_validationFs[i]);
        }
        m_models.addElement(newModel);
        m_instancesConsumed += m_chunkSize;
        hasChanged = true;
      }
      if (hasChanged) {
        m_modelHasChanged = true;
        m_modelHasChangedLL = true;
      }
      return hasChanged;
    }
    

    public void resetConsumed()
    {
      m_instancesConsumed = 0;
    }
    

    public void pruneLastModel()
    {
      if (m_models.size() > 0) {
        m_models.removeElementAt(m_models.size() - 1);
        m_modelHasChanged = true;
        m_modelHasChangedLL = true;
      }
    }
    



    public void keepLastModel()
      throws Exception
    {
      m_validationFs = m_newValidationFs;
      m_newValidationFs = new double[m_validationChunkSize][m_NumClasses];
      m_modelHasChanged = true;
      m_modelHasChangedLL = true;
    }
    




    public double logLikelihood()
      throws Exception
    {
      if (m_modelHasChangedLL)
      {

        double llsum = 0.0D;
        for (int i = 0; i < m_validationSet.numInstances(); i++) {
          Instance inst = m_validationSet.instance(i);
          llsum += logLikelihood(m_validationFs[i], (int)inst.classValue());
        }
        m_lastLogLikelihood = (llsum / m_validationSet.numInstances());
        m_modelHasChangedLL = false;
      }
      return m_lastLogLikelihood;
    }
    





    public double logLikelihoodAfter()
      throws Exception
    {
      double llsum = 0.0D;
      for (int i = 0; i < m_validationSet.numInstances(); i++) {
        Instance inst = m_validationSet.instance(i);
        llsum += logLikelihood(m_newValidationFs[i], (int)inst.classValue());
      }
      return llsum / m_validationSet.numInstances();
    }
    







    private double logLikelihood(double[] Fs, int classIndex)
      throws Exception
    {
      return -Math.log(distributionForInstance(Fs)[classIndex]);
    }
    




    public double validationError()
      throws Exception
    {
      if (m_modelHasChanged)
      {

        int numIncorrect = 0;
        for (int i = 0; i < m_validationSet.numInstances(); i++) {
          Instance inst = m_validationSet.instance(i);
          if (classifyInstance(m_validationFs[i]) != inst.classValue())
            numIncorrect++;
        }
        m_lastValidationError = (numIncorrect / m_validationSet.numInstances());
        m_modelHasChanged = false;
      }
      return m_lastValidationError;
    }
    





    public int chunkSize()
    {
      return m_chunkSize;
    }
    





    public int committeeSize()
    {
      return m_models.size();
    }
    







    public double classifyInstance(double[] Fs)
      throws Exception
    {
      double[] dist = distributionForInstance(Fs);
      
      double max = 0.0D;
      int maxIndex = 0;
      
      for (int i = 0; i < dist.length; i++) {
        if (dist[i] > max) {
          maxIndex = i;
          max = dist[i];
        }
      }
      if (max > 0.0D) {
        return maxIndex;
      }
      return Instance.missingValue();
    }
    







    public double classifyInstance(Instance instance)
      throws Exception
    {
      double[] dist = distributionForInstance(instance);
      switch (instance.classAttribute().type()) {
      case 1: 
        double max = 0.0D;
        int maxIndex = 0;
        
        for (int i = 0; i < dist.length; i++) {
          if (dist[i] > max) {
            maxIndex = i;
            max = dist[i];
          }
        }
        if (max > 0.0D) {
          return maxIndex;
        }
        return Instance.missingValue();
      
      case 0: 
        return dist[0];
      }
      return Instance.missingValue();
    }
    







    public double[] distributionForInstance(double[] Fs)
      throws Exception
    {
      double[] distribution = new double[m_NumClasses];
      for (int j = 0; j < m_NumClasses; j++) {
        distribution[j] = RacedIncrementalLogitBoost.RtoP(Fs, j);
      }
      return distribution;
    }
    








    public double[] updateFS(Instance instance, Classifier[] newModel, double[] Fs)
      throws Exception
    {
      instance = (Instance)instance.copy();
      instance.setDataset(m_NumericClassData);
      
      double[] Fi = new double[m_NumClasses];
      double Fsum = 0.0D;
      for (int j = 0; j < m_NumClasses; j++) {
        Fi[j] = newModel[j].classifyInstance(instance);
        Fsum += Fi[j];
      }
      Fsum /= m_NumClasses;
      
      double[] newFs = new double[Fs.length];
      for (int j = 0; j < m_NumClasses; j++) {
        Fs[j] += (Fi[j] - Fsum) * (m_NumClasses - 1) / m_NumClasses;
      }
      return newFs;
    }
    






    public double[] distributionForInstance(Instance instance)
      throws Exception
    {
      instance = (Instance)instance.copy();
      instance.setDataset(m_NumericClassData);
      double[] Fs = new double[m_NumClasses];
      for (int i = 0; i < m_models.size(); i++) {
        double[] Fi = new double[m_NumClasses];
        double Fsum = 0.0D;
        Classifier[] model = (Classifier[])m_models.elementAt(i);
        for (int j = 0; j < m_NumClasses; j++) {
          Fi[j] = model[j].classifyInstance(instance);
          Fsum += Fi[j];
        }
        Fsum /= m_NumClasses;
        for (int j = 0; j < m_NumClasses; j++) {
          Fs[j] += (Fi[j] - Fsum) * (m_NumClasses - 1) / m_NumClasses;
        }
      }
      double[] distribution = new double[m_NumClasses];
      for (int j = 0; j < m_NumClasses; j++) {
        distribution[j] = RacedIncrementalLogitBoost.RtoP(Fs, j);
      }
      return distribution;
    }
    






    protected Classifier[] boost(Instances data)
      throws Exception
    {
      Classifier[] newModel = Classifier.makeCopies(m_Classifier, m_NumClasses);
      

      Instances boostData = new Instances(data);
      boostData.deleteWithMissingClass();
      int numInstances = boostData.numInstances();
      

      int classIndex = data.classIndex();
      boostData.setClassIndex(-1);
      boostData.deleteAttributeAt(classIndex);
      boostData.insertAttributeAt(new Attribute("'pseudo class'"), classIndex);
      boostData.setClassIndex(classIndex);
      double[][] trainFs = new double[numInstances][m_NumClasses];
      double[][] trainYs = new double[numInstances][m_NumClasses];
      for (int j = 0; j < m_NumClasses; j++) {
        int i = 0; for (int k = 0; i < numInstances; k++) {
          while (data.instance(k).classIsMissing()) k++;
          trainYs[i][j] = (data.instance(k).classValue() == j ? 1.0D : 0.0D);i++;
        }
      }
      

      for (int x = 0; x < m_models.size(); x++) {
        for (int i = 0; i < numInstances; i++) {
          double[] pred = new double[m_NumClasses];
          double predSum = 0.0D;
          Classifier[] model = (Classifier[])m_models.elementAt(x);
          for (int j = 0; j < m_NumClasses; j++) {
            pred[j] = model[j].classifyInstance(boostData.instance(i));
            predSum += pred[j];
          }
          predSum /= m_NumClasses;
          for (int j = 0; j < m_NumClasses; j++) {
            trainFs[i][j] += (pred[j] - predSum) * (m_NumClasses - 1) / m_NumClasses;
          }
        }
      }
      

      for (int j = 0; j < m_NumClasses; j++)
      {

        for (int i = 0; i < numInstances; i++) {
          double p = RacedIncrementalLogitBoost.RtoP(trainFs[i], j);
          Instance current = boostData.instance(i);
          double actual = trainYs[i][j];
          double z; if (actual == 1.0D) {
            double z = 1.0D / p;
            if (z > 4.0D) {
              z = 4.0D;
            }
          } else if (actual == 0.0D) {
            double z = -1.0D / (1.0D - p);
            if (z < -4.0D) {
              z = -4.0D;
            }
          } else {
            z = (actual - p) / (p * (1.0D - p));
          }
          
          double w = (actual - p) / z;
          current.setValue(classIndex, z);
          current.setWeight(numInstances * w);
        }
        
        Instances trainData = boostData;
        if (m_UseResampling) {
          double[] weights = new double[boostData.numInstances()];
          for (int kk = 0; kk < weights.length; kk++) {
            weights[kk] = boostData.instance(kk).weight();
          }
          trainData = boostData.resampleWithWeights(m_RandomInstance, weights);
        }
        


        newModel[j].buildClassifier(trainData);
      }
      
      return newModel;
    }
    





    public String toString()
    {
      StringBuffer text = new StringBuffer();
      
      text.append("RacedIncrementalLogitBoost: Best committee on validation data\n");
      text.append("Base classifiers: \n");
      
      for (int i = 0; i < m_models.size(); i++) {
        text.append("\nModel " + (i + 1));
        Classifier[] cModels = (Classifier[])m_models.elementAt(i);
        for (int j = 0; j < m_NumClasses; j++) {
          text.append("\n\tClass " + (j + 1) + " (" + m_ClassAttribute.name() + "=" + m_ClassAttribute.value(j) + ")\n\n" + cModels[j].toString() + "\n");
        }
      }
      


      text.append("Number of models: " + m_models.size() + "\n");
      
      text.append("Chunk size per model: " + m_chunkSize + "\n");
      
      return text.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6477 $");
    }
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    m_RandomInstance = new Random(m_Seed);
    

    int classIndex = data.classIndex();
    

    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    if (m_Classifier == null) {
      throw new Exception("A base classifier has not been specified!");
    }
    
    if ((!(m_Classifier instanceof WeightedInstancesHandler)) && (!m_UseResampling))
    {
      m_UseResampling = true;
    }
    
    m_NumClasses = data.numClasses();
    m_ClassAttribute = data.classAttribute();
    

    Instances boostData = new Instances(data);
    

    boostData.setClassIndex(-1);
    boostData.deleteAttributeAt(classIndex);
    boostData.insertAttributeAt(new Attribute("'pseudo class'"), classIndex);
    boostData.setClassIndex(classIndex);
    m_NumericClassData = new Instances(boostData, 0);
    
    data.randomize(m_RandomInstance);
    

    int cSize = m_minChunkSize;
    m_committees = new FastVector();
    while (cSize <= m_maxChunkSize) {
      m_committees.addElement(new Committee(cSize));
      m_maxBatchSizeRequired = cSize;
      cSize *= 2;
    }
    

    m_validationSet = new Instances(data, m_validationChunkSize);
    m_currentSet = new Instances(data, m_maxBatchSizeRequired);
    m_bestCommittee = null;
    m_numInstancesConsumed = 0;
    

    for (int i = 0; i < data.numInstances(); i++) { updateClassifier(data.instance(i));
    }
  }
  




  public void updateClassifier(Instance instance)
    throws Exception
  {
    m_numInstancesConsumed += 1;
    
    if (m_validationSet.numInstances() < m_validationChunkSize) {
      m_validationSet.add(instance);
      m_validationSetChanged = true;
    } else {
      m_currentSet.add(instance);
      boolean hasChanged = false;
      

      for (int i = 0; i < m_committees.size(); i++) {
        Committee c = (Committee)m_committees.elementAt(i);
        if (c.update())
        {
          hasChanged = true;
          
          if (m_PruningType == 1) {
            double oldLL = c.logLikelihood();
            double newLL = c.logLikelihoodAfter();
            if ((newLL >= oldLL) && (c.committeeSize() > 1)) {
              c.pruneLastModel();
              if (m_Debug) System.out.println("Pruning " + c.chunkSize() + " committee (" + oldLL + " < " + newLL + ")");
            } else {
              c.keepLastModel();
            } } else { c.keepLastModel();
          }
        } }
      if (hasChanged)
      {
        if (m_Debug) { System.out.println("After consuming " + m_numInstancesConsumed + " instances... (" + m_validationSet.numInstances() + " + " + m_currentSet.numInstances() + " instances currently in memory)");
        }
        



        double lowestError = 1.0D;
        for (int i = 0; i < m_committees.size(); i++) {
          Committee c = (Committee)m_committees.elementAt(i);
          
          if (c.committeeSize() > 0)
          {
            double err = c.validationError();
            double ll = c.logLikelihood();
            
            if (m_Debug) { System.out.println("Chunk size " + c.chunkSize() + " with " + c.committeeSize() + " models, has validation error of " + err + ", log likelihood of " + ll);
            }
            
            if (err < lowestError) {
              lowestError = err;
              m_bestCommittee = c;
            }
          }
        }
      }
      if (m_currentSet.numInstances() >= m_maxBatchSizeRequired) {
        m_currentSet = new Instances(m_currentSet, m_maxBatchSizeRequired);
        

        for (int i = 0; i < m_committees.size(); i++) {
          Committee c = (Committee)m_committees.elementAt(i);
          c.resetConsumed();
        }
      }
    }
  }
  








  protected static double RtoP(double[] Fs, int j)
    throws Exception
  {
    double maxF = -1.7976931348623157E308D;
    for (int i = 0; i < Fs.length; i++) {
      if (Fs[i] > maxF) {
        maxF = Fs[i];
      }
    }
    double sum = 0.0D;
    double[] probs = new double[Fs.length];
    for (int i = 0; i < Fs.length; i++) {
      probs[i] = Math.exp(Fs[i] - maxF);
      sum += probs[i];
    }
    if (sum == 0.0D) {
      throw new Exception("Can't normalize");
    }
    return probs[j] / sum;
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_bestCommittee != null) { return m_bestCommittee.distributionForInstance(instance);
    }
    if ((m_validationSetChanged) || (m_zeroR == null)) {
      m_zeroR = new ZeroR();
      m_zeroR.buildClassifier(m_validationSet);
      m_validationSetChanged = false;
    }
    return m_zeroR.distributionForInstance(instance);
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(9);
    
    newVector.addElement(new Option("\tMinimum size of chunks.\n\t(default 500)", "C", 1, "-C <num>"));
    



    newVector.addElement(new Option("\tMaximum size of chunks.\n\t(default 2000)", "M", 1, "-M <num>"));
    



    newVector.addElement(new Option("\tSize of validation set.\n\t(default 1000)", "V", 1, "-V <num>"));
    



    newVector.addElement(new Option("\tCommittee pruning to perform.\n\t0=none, 1=log likelihood (default)", "P", 1, "-P <pruning type>"));
    



    newVector.addElement(new Option("\tUse resampling for boosting.", "Q", 0, "-Q"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  


















































  public void setOptions(String[] options)
    throws Exception
  {
    String minChunkSize = Utils.getOption('C', options);
    if (minChunkSize.length() != 0) {
      setMinChunkSize(Integer.parseInt(minChunkSize));
    } else {
      setMinChunkSize(500);
    }
    
    String maxChunkSize = Utils.getOption('M', options);
    if (maxChunkSize.length() != 0) {
      setMaxChunkSize(Integer.parseInt(maxChunkSize));
    } else {
      setMaxChunkSize(2000);
    }
    
    String validationChunkSize = Utils.getOption('V', options);
    if (validationChunkSize.length() != 0) {
      setValidationChunkSize(Integer.parseInt(validationChunkSize));
    } else {
      setValidationChunkSize(1000);
    }
    
    String pruneType = Utils.getOption('P', options);
    if (pruneType.length() != 0) {
      setPruningType(new SelectedTag(Integer.parseInt(pruneType), TAGS_PRUNETYPE));
    } else {
      setPruningType(new SelectedTag(1, TAGS_PRUNETYPE));
    }
    
    setUseResampling(Utils.getFlag('Q', options));
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 9];
    
    int current = 0;
    
    if (getUseResampling()) {
      options[(current++)] = "-Q";
    }
    options[(current++)] = "-C";options[(current++)] = ("" + getMinChunkSize());
    
    options[(current++)] = "-M";options[(current++)] = ("" + getMaxChunkSize());
    
    options[(current++)] = "-V";options[(current++)] = ("" + getValidationChunkSize());
    
    options[(current++)] = "-P";options[(current++)] = ("" + m_PruningType);
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String globalInfo()
  {
    return "Classifier for incremental learning of large datasets by way of racing logit-boosted committees.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  










  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Eibe Frank and Geoffrey Holmes and Richard Kirkby and Mark Hall");
    
    result.setValue(TechnicalInformation.Field.TITLE, " Racing committees for large datasets");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the 5th International Conferenceon Discovery Science");
    
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.PAGES, "153-164");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  






  public void setClassifier(Classifier newClassifier)
  {
    Capabilities cap = newClassifier.getCapabilities();
    
    if (!cap.handles(Capabilities.Capability.NUMERIC_CLASS)) {
      throw new IllegalArgumentException("Base classifier cannot handle numeric class!");
    }
    super.setClassifier(newClassifier);
  }
  




  public String minChunkSizeTipText()
  {
    return "The minimum number of instances to train the base learner with.";
  }
  





  public void setMinChunkSize(int chunkSize)
  {
    m_minChunkSize = chunkSize;
  }
  





  public int getMinChunkSize()
  {
    return m_minChunkSize;
  }
  




  public String maxChunkSizeTipText()
  {
    return "The maximum number of instances to train the base learner with. The chunk sizes used will start at minChunkSize and grow twice as large for as many times as they are less than or equal to the maximum size.";
  }
  





  public void setMaxChunkSize(int chunkSize)
  {
    m_maxChunkSize = chunkSize;
  }
  





  public int getMaxChunkSize()
  {
    return m_maxChunkSize;
  }
  




  public String validationChunkSizeTipText()
  {
    return "The number of instances to hold out for validation. These instances will be taken from the beginning of the stream, so learning will not start until these instances have been consumed first.";
  }
  





  public void setValidationChunkSize(int chunkSize)
  {
    m_validationChunkSize = chunkSize;
  }
  





  public int getValidationChunkSize()
  {
    return m_validationChunkSize;
  }
  




  public String pruningTypeTipText()
  {
    return "The pruning method to use within each committee. Log likelihood pruning will discard new models if they have a negative effect on the log likelihood of the validation data.";
  }
  





  public void setPruningType(SelectedTag pruneType)
  {
    if (pruneType.getTags() == TAGS_PRUNETYPE) {
      m_PruningType = pruneType.getSelectedTag().getID();
    }
  }
  





  public SelectedTag getPruningType()
  {
    return new SelectedTag(m_PruningType, TAGS_PRUNETYPE);
  }
  




  public String useResamplingTipText()
  {
    return "Force the use of resampling data rather than using the weight-handling capabilities of the base classifier. Resampling is always used if the base classifier cannot handle weighted instances.";
  }
  





  public void setUseResampling(boolean r)
  {
    m_UseResampling = r;
  }
  





  public boolean getUseResampling()
  {
    return m_UseResampling;
  }
  





  public int getBestCommitteeChunkSize()
  {
    if (m_bestCommittee != null) {
      return m_bestCommittee.chunkSize();
    }
    return 0;
  }
  





  public int getBestCommitteeSize()
  {
    if (m_bestCommittee != null) {
      return m_bestCommittee.committeeSize();
    }
    return 0;
  }
  





  public double getBestCommitteeErrorEstimate()
  {
    if (m_bestCommittee != null) {
      try {
        return m_bestCommittee.validationError() * 100.0D;
      } catch (Exception e) {
        System.err.println(e.getMessage());
        return 100.0D;
      }
    }
    return 100.0D;
  }
  





  public double getBestCommitteeLLEstimate()
  {
    if (m_bestCommittee != null) {
      try {
        return m_bestCommittee.logLikelihood();
      } catch (Exception e) {
        System.err.println(e.getMessage());
        return Double.MAX_VALUE;
      }
    }
    return Double.MAX_VALUE;
  }
  





  public String toString()
  {
    if (m_bestCommittee != null) {
      return m_bestCommittee.toString();
    }
    if (((m_validationSetChanged) || (m_zeroR == null)) && (m_validationSet != null) && (m_validationSet.numInstances() > 0))
    {
      m_zeroR = new ZeroR();
      try {
        m_zeroR.buildClassifier(m_validationSet);
      } catch (Exception e) {}
      m_validationSetChanged = false;
    }
    if (m_zeroR != null) {
      return "RacedIncrementalLogitBoost: insufficient data to build model, resorting to ZeroR:\n\n" + m_zeroR.toString();
    }
    
    return "RacedIncrementalLogitBoost: no model built yet.";
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6477 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new RacedIncrementalLogitBoost(), argv);
  }
}

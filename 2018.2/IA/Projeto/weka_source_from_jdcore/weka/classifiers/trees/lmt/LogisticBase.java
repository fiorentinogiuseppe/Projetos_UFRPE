package weka.classifiers.trees.lmt;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;




























































public class LogisticBase
  extends Classifier
  implements WeightedInstancesHandler
{
  static final long serialVersionUID = 168765678097825064L;
  protected Instances m_numericDataHeader;
  protected Instances m_numericData;
  protected Instances m_train;
  protected boolean m_useCrossValidation;
  protected boolean m_errorOnProbabilities;
  protected int m_fixedNumIterations;
  protected int m_heuristicStop = 50;
  

  protected int m_numRegressions = 0;
  

  protected int m_maxIterations;
  

  protected int m_numClasses;
  

  protected SimpleLinearRegression[][] m_regressions;
  

  protected static int m_numFoldsBoosting = 5;
  

  protected static final double Z_MAX = 3.0D;
  

  private boolean m_useAIC = false;
  

  protected double m_numParameters = 0.0D;
  



  protected double m_weightTrimBeta = 0.0D;
  


  public LogisticBase()
  {
    m_fixedNumIterations = -1;
    m_useCrossValidation = true;
    m_errorOnProbabilities = false;
    m_maxIterations = 500;
    m_useAIC = false;
    m_numParameters = 0.0D;
  }
  








  public LogisticBase(int numBoostingIterations, boolean useCrossValidation, boolean errorOnProbabilities)
  {
    m_fixedNumIterations = numBoostingIterations;
    m_useCrossValidation = useCrossValidation;
    m_errorOnProbabilities = errorOnProbabilities;
    m_maxIterations = 500;
    m_useAIC = false;
    m_numParameters = 0.0D;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    m_train = new Instances(data);
    
    m_numClasses = m_train.numClasses();
    

    m_regressions = initRegressions();
    m_numRegressions = 0;
    

    m_numericData = getNumericData(m_train);
    

    m_numericDataHeader = new Instances(m_numericData, 0);
    

    if (m_fixedNumIterations > 0)
    {
      performBoosting(m_fixedNumIterations);
    } else if (m_useAIC)
    {
      performBoostingInfCriterion();
    } else if (m_useCrossValidation)
    {
      performBoostingCV();
    }
    else {
      performBoosting();
    }
    

    m_regressions = selectRegressions(m_regressions);
  }
  







  protected void performBoostingCV()
    throws Exception
  {
    int completedIterations = m_maxIterations;
    
    Instances allData = new Instances(m_train);
    
    allData.stratify(m_numFoldsBoosting);
    
    double[] error = new double[m_maxIterations + 1];
    
    for (int i = 0; i < m_numFoldsBoosting; i++)
    {
      Instances train = allData.trainCV(m_numFoldsBoosting, i);
      Instances test = allData.testCV(m_numFoldsBoosting, i);
      

      m_numRegressions = 0;
      m_regressions = initRegressions();
      

      int iterations = performBoosting(train, test, error, completedIterations);
      if (iterations < completedIterations) { completedIterations = iterations;
      }
    }
    
    int bestIteration = getBestIteration(error, completedIterations);
    

    m_numRegressions = 0;
    performBoosting(bestIteration);
  }
  


  protected void performBoostingInfCriterion()
    throws Exception
  {
    double criterion = 0.0D;
    double bestCriterion = Double.MAX_VALUE;
    int bestIteration = 0;
    int noMin = 0;
    

    double criterionValue = Double.MAX_VALUE;
    

    double[][] trainYs = getYs(m_train);
    double[][] trainFs = getFs(m_numericData);
    double[][] probs = getProbs(trainFs);
    

    boolean[][] attributes = new boolean[m_numClasses][m_numericDataHeader.numAttributes()];
    
    int iteration = 0;
    while (iteration < m_maxIterations)
    {

      boolean foundAttribute = performIteration(iteration, trainYs, trainFs, probs, m_numericData);
      if (!foundAttribute) break;
      iteration++;
      m_numRegressions = iteration;
      




      double numberOfAttributes = m_numParameters + iteration;
      

      criterionValue = 2.0D * negativeLogLikelihood(trainYs, probs) + 2.0D * numberOfAttributes;
      


      if (noMin > m_heuristicStop) break;
      if (criterionValue < bestCriterion) {
        bestCriterion = criterionValue;
        bestIteration = iteration;
        noMin = 0;
      } else {
        noMin++;
      }
    }
    
    m_numRegressions = 0;
    performBoosting(bestIteration);
  }
  













  protected int performBoosting(Instances train, Instances test, double[] error, int maxIterations)
    throws Exception
  {
    Instances numericTrain = getNumericData(train);
    

    double[][] trainYs = getYs(train);
    double[][] trainFs = getFs(numericTrain);
    double[][] probs = getProbs(trainFs);
    
    int iteration = 0;
    
    int noMin = 0;
    double lastMin = Double.MAX_VALUE;
    
    if (m_errorOnProbabilities) error[0] += getMeanAbsoluteError(test); else {
      error[0] += getErrorRate(test);
    }
    while (iteration < maxIterations)
    {

      boolean foundAttribute = performIteration(iteration, trainYs, trainFs, probs, numericTrain);
      if (!foundAttribute) break;
      iteration++;
      m_numRegressions = iteration;
      




      if (m_errorOnProbabilities) error[iteration] += getMeanAbsoluteError(test); else {
        error[iteration] += getErrorRate(test);
      }
      
      if (noMin > m_heuristicStop) break;
      if (error[iteration] < lastMin) {
        lastMin = error[iteration];
        noMin = 0;
      } else {
        noMin++;
      }
    }
    
    return iteration;
  }
  





  protected void performBoosting(int numIterations)
    throws Exception
  {
    double[][] trainYs = getYs(m_train);
    double[][] trainFs = getFs(m_numericData);
    double[][] probs = getProbs(trainFs);
    
    for (int iteration = 0; 
        

        iteration < numIterations; 
        
        iteration++)
    {
      boolean foundAttribute = performIteration(iteration, trainYs, trainFs, probs, m_numericData);
      if (!foundAttribute) {
        break;
      }
    }
    m_numRegressions = iteration;
  }
  






  protected void performBoosting()
    throws Exception
  {
    double[][] trainYs = getYs(m_train);
    double[][] trainFs = getFs(m_numericData);
    double[][] probs = getProbs(trainFs);
    
    int iteration = 0;
    
    double[] trainErrors = new double[m_maxIterations + 1];
    trainErrors[0] = getErrorRate(m_train);
    
    int noMin = 0;
    double lastMin = Double.MAX_VALUE;
    
    while (iteration < m_maxIterations) {
      boolean foundAttribute = performIteration(iteration, trainYs, trainFs, probs, m_numericData);
      if (!foundAttribute) break;
      iteration++;
      m_numRegressions = iteration;
      




      trainErrors[iteration] = getErrorRate(m_train);
      

      if (noMin > m_heuristicStop) break;
      if (trainErrors[iteration] < lastMin) {
        lastMin = trainErrors[iteration];
        noMin = 0;
      } else {
        noMin++;
      }
    }
    

    m_numRegressions = getBestIteration(trainErrors, iteration);
  }
  




  protected double getErrorRate(Instances data)
    throws Exception
  {
    Evaluation eval = new Evaluation(data);
    eval.evaluateModel(this, data, new Object[0]);
    return eval.errorRate();
  }
  




  protected double getMeanAbsoluteError(Instances data)
    throws Exception
  {
    Evaluation eval = new Evaluation(data);
    eval.evaluateModel(this, data, new Object[0]);
    return eval.meanAbsoluteError();
  }
  






  protected int getBestIteration(double[] errors, int maxIteration)
  {
    double bestError = errors[0];
    int bestIteration = 0;
    for (int i = 1; i <= maxIteration; i++) {
      if (errors[i] < bestError) {
        bestError = errors[i];
        bestIteration = i;
      }
    }
    return bestIteration;
  }
  















  protected boolean performIteration(int iteration, double[][] trainYs, double[][] trainFs, double[][] probs, Instances trainNumeric)
    throws Exception
  {
    for (int j = 0; j < m_numClasses; j++)
    {
      double[] weights = new double[trainNumeric.numInstances()];
      double weightSum = 0.0D;
      

      Instances boostData = new Instances(trainNumeric);
      
      for (int i = 0; i < trainNumeric.numInstances(); i++)
      {

        double p = probs[i][j];
        double actual = trainYs[i][j];
        double z = getZ(actual, p);
        double w = (actual - p) / z;
        

        Instance current = boostData.instance(i);
        current.setValue(boostData.classIndex(), z);
        current.setWeight(current.weight() * w);
        
        weights[i] = current.weight();
        weightSum += current.weight();
      }
      
      Instances instancesCopy = new Instances(boostData);
      
      if (weightSum > 0.0D)
      {
        if (m_weightTrimBeta > 0.0D) {
          double weightPercentage = 0.0D;
          int[] weightsOrder = new int[trainNumeric.numInstances()];
          weightsOrder = Utils.sort(weights);
          instancesCopy.delete();
          

          for (int i = weightsOrder.length - 1; (i >= 0) && (weightPercentage < 1.0D - m_weightTrimBeta); i--) {
            instancesCopy.add(boostData.instance(weightsOrder[i]));
            weightPercentage += weights[weightsOrder[i]] / weightSum;
          }
        }
        


        weightSum = instancesCopy.sumOfWeights();
        for (int i = 0; i < instancesCopy.numInstances(); i++) {
          Instance current = instancesCopy.instance(i);
          current.setWeight(current.weight() * instancesCopy.numInstances() / weightSum);
        }
      }
      

      m_regressions[j][iteration].buildClassifier(instancesCopy);
      
      boolean foundAttribute = m_regressions[j][iteration].foundUsefulAttribute();
      if (!foundAttribute)
      {
        return false;
      }
    }
    


    for (int i = 0; i < trainFs.length; i++) {
      double[] pred = new double[m_numClasses];
      double predSum = 0.0D;
      for (int j = 0; j < m_numClasses; j++) {
        pred[j] = m_regressions[j][iteration].classifyInstance(trainNumeric.instance(i));
        
        predSum += pred[j];
      }
      predSum /= m_numClasses;
      for (int j = 0; j < m_numClasses; j++) {
        trainFs[i][j] += (pred[j] - predSum) * (m_numClasses - 1) / m_numClasses;
      }
    }
    


    for (int i = 0; i < trainYs.length; i++) {
      probs[i] = probs(trainFs[i]);
    }
    return true;
  }
  




  protected SimpleLinearRegression[][] initRegressions()
  {
    SimpleLinearRegression[][] classifiers = new SimpleLinearRegression[m_numClasses][m_maxIterations];
    
    for (int j = 0; j < m_numClasses; j++) {
      for (int i = 0; i < m_maxIterations; i++) {
        classifiers[j][i] = new SimpleLinearRegression();
        classifiers[j][i].setSuppressErrorMessage(true);
      }
    }
    return classifiers;
  }
  






  protected Instances getNumericData(Instances data)
    throws Exception
  {
    Instances numericData = new Instances(data);
    
    int classIndex = numericData.classIndex();
    numericData.setClassIndex(-1);
    numericData.deleteAttributeAt(classIndex);
    numericData.insertAttributeAt(new Attribute("'pseudo class'"), classIndex);
    numericData.setClassIndex(classIndex);
    return numericData;
  }
  







  protected SimpleLinearRegression[][] selectRegressions(SimpleLinearRegression[][] classifiers)
  {
    SimpleLinearRegression[][] goodClassifiers = new SimpleLinearRegression[m_numClasses][m_numRegressions];
    

    for (int j = 0; j < m_numClasses; j++) {
      for (int i = 0; i < m_numRegressions; i++) {
        goodClassifiers[j][i] = classifiers[j][i];
      }
    }
    return goodClassifiers;
  }
  




  protected double getZ(double actual, double p)
  {
    double z;
    


    if (actual == 1.0D) {
      double z = 1.0D / p;
      if (z > 3.0D) {
        z = 3.0D;
      }
    } else {
      z = -1.0D / (1.0D - p);
      if (z < -3.0D) {
        z = -3.0D;
      }
    }
    return z;
  }
  








  protected double[][] getZs(double[][] probs, double[][] dataYs)
  {
    double[][] dataZs = new double[probs.length][m_numClasses];
    for (int j = 0; j < m_numClasses; j++)
      for (int i = 0; i < probs.length; i++) dataZs[i][j] = getZ(dataYs[i][j], probs[i][j]);
    return dataZs;
  }
  








  protected double[][] getWs(double[][] probs, double[][] dataYs)
  {
    double[][] dataWs = new double[probs.length][m_numClasses];
    for (int j = 0; j < m_numClasses; j++)
      for (int i = 0; i < probs.length; i++) {
        double z = getZ(dataYs[i][j], probs[i][j]);
        dataWs[i][j] = ((dataYs[i][j] - probs[i][j]) / z);
      }
    return dataWs;
  }
  







  protected double[] probs(double[] Fs)
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
    
    Utils.normalize(probs, sum);
    return probs;
  }
  






  protected double[][] getYs(Instances data)
  {
    double[][] dataYs = new double[data.numInstances()][m_numClasses];
    for (int j = 0; j < m_numClasses; j++) {
      for (int k = 0; k < data.numInstances(); k++) {
        dataYs[k][j] = (data.instance(k).classValue() == j ? 1.0D : 0.0D);
      }
    }
    
    return dataYs;
  }
  






  protected double[] getFs(Instance instance)
    throws Exception
  {
    double[] pred = new double[m_numClasses];
    double[] instanceFs = new double[m_numClasses];
    

    for (int i = 0; i < m_numRegressions; i++) {
      double predSum = 0.0D;
      for (int j = 0; j < m_numClasses; j++) {
        pred[j] = m_regressions[j][i].classifyInstance(instance);
        predSum += pred[j];
      }
      predSum /= m_numClasses;
      for (int j = 0; j < m_numClasses; j++) {
        instanceFs[j] += (pred[j] - predSum) * (m_numClasses - 1) / m_numClasses;
      }
    }
    

    return instanceFs;
  }
  






  protected double[][] getFs(Instances data)
    throws Exception
  {
    double[][] dataFs = new double[data.numInstances()][];
    
    for (int k = 0; k < data.numInstances(); k++) {
      dataFs[k] = getFs(data.instance(k));
    }
    
    return dataFs;
  }
  







  protected double[][] getProbs(double[][] dataFs)
  {
    int numInstances = dataFs.length;
    double[][] probs = new double[numInstances][];
    
    for (int k = 0; k < numInstances; k++) {
      probs[k] = probs(dataFs[k]);
    }
    return probs;
  }
  








  protected double negativeLogLikelihood(double[][] dataYs, double[][] probs)
  {
    double logLikelihood = 0.0D;
    for (int i = 0; i < dataYs.length; i++) {
      for (int j = 0; j < m_numClasses; j++) {
        if (dataYs[i][j] == 1.0D) {
          logLikelihood -= Math.log(probs[i][j]);
        }
      }
    }
    return logLikelihood;
  }
  






  public int[][] getUsedAttributes()
  {
    int[][] usedAttributes = new int[m_numClasses][];
    

    double[][] coefficients = getCoefficients();
    
    for (int j = 0; j < m_numClasses; j++)
    {

      boolean[] attributes = new boolean[m_numericDataHeader.numAttributes()];
      for (int i = 0; i < attributes.length; i++)
      {
        if (!Utils.eq(coefficients[j][(i + 1)], 0.0D)) { attributes[i] = true;
        }
      }
      int numAttributes = 0;
      for (int i = 0; i < m_numericDataHeader.numAttributes(); i++) { if (attributes[i] != 0) { numAttributes++;
        }
      }
      int[] usedAttributesClass = new int[numAttributes];
      int count = 0;
      for (int i = 0; i < m_numericDataHeader.numAttributes(); i++) {
        if (attributes[i] != 0) {
          usedAttributesClass[count] = i;
          count++;
        }
      }
      
      usedAttributes[j] = usedAttributesClass;
    }
    
    return usedAttributes;
  }
  





  public int getNumRegressions()
  {
    return m_numRegressions;
  }
  




  public double getWeightTrimBeta()
  {
    return m_weightTrimBeta;
  }
  




  public boolean getUseAIC()
  {
    return m_useAIC;
  }
  




  public void setMaxIterations(int maxIterations)
  {
    m_maxIterations = maxIterations;
  }
  




  public void setHeuristicStop(int heuristicStop)
  {
    m_heuristicStop = heuristicStop;
  }
  


  public void setWeightTrimBeta(double w)
  {
    m_weightTrimBeta = w;
  }
  




  public void setUseAIC(boolean c)
  {
    m_useAIC = c;
  }
  




  public int getMaxIterations()
  {
    return m_maxIterations;
  }
  






  protected double[][] getCoefficients()
  {
    double[][] coefficients = new double[m_numClasses][m_numericDataHeader.numAttributes() + 1];
    for (int j = 0; j < m_numClasses; j++)
    {

      for (int i = 0; i < m_numRegressions; i++)
      {
        double slope = m_regressions[j][i].getSlope();
        double intercept = m_regressions[j][i].getIntercept();
        int attribute = m_regressions[j][i].getAttributeIndex();
        
        coefficients[j][0] += intercept;
        coefficients[j][(attribute + 1)] += slope;
      }
    }
    

    for (int j = 0; j < coefficients.length; j++) {
      for (int i = 0; i < coefficients[0].length; i++) {
        coefficients[j][i] *= (m_numClasses - 1) / m_numClasses;
      }
    }
    
    return coefficients;
  }
  







  public double percentAttributesUsed()
  {
    boolean[] attributes = new boolean[m_numericDataHeader.numAttributes()];
    
    double[][] coefficients = getCoefficients();
    for (int j = 0; j < m_numClasses; j++) {
      for (int i = 1; i < m_numericDataHeader.numAttributes() + 1; i++)
      {

        if (!Utils.eq(coefficients[j][i], 0.0D)) { attributes[(i - 1)] = true;
        }
      }
    }
    
    double count = 0.0D;
    for (int i = 0; i < attributes.length; i++) if (attributes[i] != 0) count += 1.0D;
    return count / (m_numericDataHeader.numAttributes() - 1) * 100.0D;
  }
  






  public String toString()
  {
    StringBuffer s = new StringBuffer();
    

    int[][] attributes = getUsedAttributes();
    

    double[][] coefficients = getCoefficients();
    
    for (int j = 0; j < m_numClasses; j++) {
      s.append("\nClass " + j + " :\n");
      
      s.append(Utils.doubleToString(coefficients[j][0], 4, 2) + " + \n");
      for (int i = 0; i < attributes[j].length; i++)
      {
        s.append("[" + m_numericDataHeader.attribute(attributes[j][i]).name() + "]");
        s.append(" * " + Utils.doubleToString(coefficients[j][(attributes[j][i] + 1)], 4, 2));
        if (i != attributes[j].length - 1) s.append(" +");
        s.append("\n");
      }
    }
    return new String(s);
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    instance = (Instance)instance.copy();
    

    instance.setDataset(m_numericDataHeader);
    

    return probs(getFs(instance));
  }
  



  public void cleanup()
  {
    m_train = new Instances(m_train, 0);
    m_numericData = null;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}

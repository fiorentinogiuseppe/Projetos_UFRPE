package weka.gui.boundaryvisualizer;

import java.io.PrintStream;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.experiment.Task;
import weka.experiment.TaskStatusInfo;
































public class RemoteBoundaryVisualizerSubTask
  implements Task
{
  private TaskStatusInfo m_status = new TaskStatusInfo();
  

  private RemoteResult m_result;
  

  private int m_rowNumber;
  

  private int m_panelHeight;
  

  private int m_panelWidth;
  
  private Classifier m_classifier;
  
  private DataGenerator m_dataGenerator;
  
  private Instances m_trainingData;
  
  private int m_xAttribute;
  
  private int m_yAttribute;
  
  private double m_pixHeight;
  
  private double m_pixWidth;
  
  private double m_minX;
  
  private double m_minY;
  
  private double m_maxX;
  
  private double m_maxY;
  
  private int m_numOfSamplesPerRegion = 2;
  
  private int m_numOfSamplesPerGenerator;
  
  private double m_samplesBase = 2.0D;
  
  private Random m_random;
  
  private double[] m_weightingAttsValues;
  
  private boolean[] m_attsToWeightOn;
  
  private double[] m_vals;
  
  private double[] m_dist;
  private Instance m_predInst;
  
  public RemoteBoundaryVisualizerSubTask() {}
  
  public void setRowNumber(int rn)
  {
    m_rowNumber = rn;
  }
  




  public void setPanelWidth(int pw)
  {
    m_panelWidth = pw;
  }
  




  public void setPanelHeight(int ph)
  {
    m_panelHeight = ph;
  }
  




  public void setPixHeight(double ph)
  {
    m_pixHeight = ph;
  }
  




  public void setPixWidth(double pw)
  {
    m_pixWidth = pw;
  }
  




  public void setClassifier(Classifier dc)
  {
    m_classifier = dc;
  }
  




  public void setDataGenerator(DataGenerator dg)
  {
    m_dataGenerator = dg;
  }
  




  public void setInstances(Instances i)
  {
    m_trainingData = i;
  }
  





  public void setMinMaxX(double minx, double maxx)
  {
    m_minX = minx;m_maxX = maxx;
  }
  





  public void setMinMaxY(double miny, double maxy)
  {
    m_minY = miny;m_maxY = maxy;
  }
  




  public void setXAttribute(int xatt)
  {
    m_xAttribute = xatt;
  }
  




  public void setYAttribute(int yatt)
  {
    m_yAttribute = yatt;
  }
  





  public void setNumSamplesPerRegion(int num)
  {
    m_numOfSamplesPerRegion = num;
  }
  





  public void setGeneratorSamplesBase(double ksb)
  {
    m_samplesBase = ksb;
  }
  



  public void execute()
  {
    m_random = new Random(m_rowNumber * 11);
    m_dataGenerator.setSeed(m_rowNumber * 11);
    m_result = new RemoteResult(m_rowNumber, m_panelWidth);
    m_status.setTaskResult(m_result);
    m_status.setExecutionStatus(1);
    try
    {
      m_numOfSamplesPerGenerator = ((int)Math.pow(m_samplesBase, m_trainingData.numAttributes() - 3));
      
      if (m_trainingData == null) {
        Messages.getInstance();throw new Exception(Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_TrainingData_Error_Text_First"));
      }
      if (m_classifier == null) {
        Messages.getInstance();throw new Exception(Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_Classifier_Error_Text"));
      }
      if (m_dataGenerator == null) {
        Messages.getInstance();throw new Exception(Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_DataGenerator_Error_Text"));
      }
      if ((m_trainingData.attribute(m_xAttribute).isNominal()) || (m_trainingData.attribute(m_yAttribute).isNominal()))
      {
        Messages.getInstance();throw new Exception(Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_TrainingData_Error_Text_Second"));
      }
      
      m_attsToWeightOn = new boolean[m_trainingData.numAttributes()];
      m_attsToWeightOn[m_xAttribute] = true;
      m_attsToWeightOn[m_yAttribute] = true;
      

      m_weightingAttsValues = new double[m_attsToWeightOn.length];
      m_vals = new double[m_trainingData.numAttributes()];
      m_predInst = new Instance(1.0D, m_vals);
      m_predInst.setDataset(m_trainingData);
      
      Messages.getInstance();System.err.println(Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_Error_Text") + m_rowNumber);
      for (int j = 0; j < m_panelWidth; j++) {
        double[] preds = calculateRegionProbs(j, m_rowNumber);
        m_result.setLocationProbs(j, preds);
        m_result.setPercentCompleted((int)(100.0D * (j / m_panelWidth)));
      }
    }
    catch (Exception ex) {
      m_status.setExecutionStatus(2);
      Messages.getInstance();Messages.getInstance();m_status.setStatusMessage(Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_StatusMessage_Text_Front_First") + m_rowNumber + Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_StatusMessage_Text_End_First"));
      System.err.print(ex);
      return;
    }
    

    m_status.setExecutionStatus(3);
    Messages.getInstance();Messages.getInstance();m_status.setStatusMessage(Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_StatusMessage_Text_Front") + m_rowNumber + Messages.getString("RemoteBoundaryVisualizerSubTask_Execute_StatusMessage_Text_End"));
  }
  
  private double[] calculateRegionProbs(int j, int i) throws Exception
  {
    double[] sumOfProbsForRegion = new double[m_trainingData.classAttribute().numValues()];
    

    for (int u = 0; u < m_numOfSamplesPerRegion; u++)
    {
      double[] sumOfProbsForLocation = new double[m_trainingData.classAttribute().numValues()];
      

      m_weightingAttsValues[m_xAttribute] = getRandomX(j);
      m_weightingAttsValues[m_yAttribute] = getRandomY(m_panelHeight - i - 1);
      
      m_dataGenerator.setWeightingValues(m_weightingAttsValues);
      
      double[] weights = m_dataGenerator.getWeights();
      double sumOfWeights = Utils.sum(weights);
      int[] indices = Utils.sort(weights);
      

      int[] newIndices = new int[indices.length];
      double sumSoFar = 0.0D;
      double criticalMass = 0.99D * sumOfWeights;
      int index = weights.length - 1;int counter = 0;
      for (int z = weights.length - 1; z >= 0; z--) {
        newIndices[(index--)] = indices[z];
        sumSoFar += weights[indices[z]];
        counter++;
        if (sumSoFar > criticalMass) {
          break;
        }
      }
      indices = new int[counter];
      System.arraycopy(newIndices, index + 1, indices, 0, counter);
      
      for (int z = 0; z < m_numOfSamplesPerGenerator; z++)
      {
        m_dataGenerator.setWeightingValues(m_weightingAttsValues);
        double[][] values = m_dataGenerator.generateInstances(indices);
        
        for (int q = 0; q < values.length; q++) {
          if (values[q] != null) {
            System.arraycopy(values[q], 0, m_vals, 0, m_vals.length);
            m_vals[m_xAttribute] = m_weightingAttsValues[m_xAttribute];
            m_vals[m_yAttribute] = m_weightingAttsValues[m_yAttribute];
            

            m_dist = m_classifier.distributionForInstance(m_predInst);
            
            for (int k = 0; k < sumOfProbsForLocation.length; k++) {
              sumOfProbsForLocation[k] += m_dist[k] * weights[q];
            }
          }
        }
      }
      
      for (int k = 0; k < sumOfProbsForRegion.length; k++) {
        sumOfProbsForRegion[k] += sumOfProbsForLocation[k] * sumOfWeights;
      }
    }
    

    Utils.normalize(sumOfProbsForRegion);
    

    double[] tempDist = new double[sumOfProbsForRegion.length];
    System.arraycopy(sumOfProbsForRegion, 0, tempDist, 0, sumOfProbsForRegion.length);
    

    return tempDist;
  }
  







  private double getRandomX(int pix)
  {
    double minPix = m_minX + pix * m_pixWidth;
    
    return minPix + m_random.nextDouble() * m_pixWidth;
  }
  







  private double getRandomY(int pix)
  {
    double minPix = m_minY + pix * m_pixHeight;
    
    return minPix + m_random.nextDouble() * m_pixHeight;
  }
  




  public TaskStatusInfo getTaskStatus()
  {
    return m_status;
  }
}

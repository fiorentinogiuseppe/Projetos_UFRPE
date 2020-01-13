package weka.gui.boundaryvisualizer;

import java.io.Serializable;
import java.util.Random;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;












































public class KDDataGenerator
  implements DataGenerator, Serializable
{
  private static final long serialVersionUID = -958573275606402792L;
  private Instances m_instances;
  private double[] m_standardDeviations;
  private double[] m_globalMeansOrModes;
  private double m_minStdDev = 1.0E-5D;
  

  private double m_laplaceConst = 1.0D;
  

  private int m_seed = 1;
  


  private Random m_random;
  

  private boolean[] m_weightingDimensions;
  

  private double[] m_weightingValues;
  

  private static double m_normConst = Math.sqrt(6.283185307179586D);
  

  private int m_kernelBandwidth = 3;
  

  private double[][] m_kernelParams;
  

  protected double[] m_Min;
  

  protected double[] m_Max;
  


  public KDDataGenerator() {}
  

  public void buildGenerator(Instances inputInstances)
    throws Exception
  {
    m_random = new Random(m_seed);
    
    m_instances = inputInstances;
    m_standardDeviations = new double[m_instances.numAttributes()];
    m_globalMeansOrModes = new double[m_instances.numAttributes()];
    if (m_weightingDimensions == null) {
      m_weightingDimensions = new boolean[m_instances.numAttributes()];
    }
    




















    for (int i = 0; i < m_instances.numAttributes(); i++) {
      if (i != m_instances.classIndex()) {
        m_globalMeansOrModes[i] = m_instances.meanOrMode(i);
      }
    }
    
    m_kernelParams = new double[m_instances.numInstances()][m_instances.numAttributes()];
    
    computeParams();
  }
  
  public double[] getWeights()
  {
    double[] weights = new double[m_instances.numInstances()];
    
    for (int k = 0; k < m_instances.numInstances(); k++) {
      double weight = 1.0D;
      for (int i = 0; i < m_instances.numAttributes(); i++) {
        if (m_weightingDimensions[i] != 0) {
          double mean = 0.0D;
          if (!m_instances.instance(k).isMissing(i)) {
            mean = m_instances.instance(k).value(i);
          } else {
            mean = m_globalMeansOrModes[i];
          }
          double wm = 1.0D;
          

          wm = normalDens(m_weightingValues[i], mean, m_kernelParams[k][i]);
          

          weight *= wm;
        }
      }
      weights[k] = weight;
    }
    return weights;
  }
  






  private double[] computeCumulativeDistribution(double[] dist)
  {
    double[] cumDist = new double[dist.length];
    double sum = 0.0D;
    for (int i = 0; i < dist.length; i++) {
      sum += dist[i];
      cumDist[i] = sum;
    }
    
    return cumDist;
  }
  






  public double[][] generateInstances(int[] indices)
    throws Exception
  {
    double[][] values = new double[m_instances.numInstances()][];
    
    for (int k = 0; k < indices.length; k++) {
      values[indices[k]] = new double[m_instances.numAttributes()];
      for (int i = 0; i < m_instances.numAttributes(); i++) {
        if ((m_weightingDimensions[i] == 0) && (i != m_instances.classIndex())) {
          if (m_instances.attribute(i).isNumeric()) {
            double mean = 0.0D;
            double val = m_random.nextGaussian();
            if (!m_instances.instance(indices[k]).isMissing(i)) {
              mean = m_instances.instance(indices[k]).value(i);
            } else {
              mean = m_globalMeansOrModes[i];
            }
            
            val *= m_kernelParams[indices[k]][i];
            val += mean;
            
            values[indices[k]][i] = val;
          }
          else {
            double[] dist = new double[m_instances.attribute(i).numValues()];
            for (int j = 0; j < dist.length; j++) {
              dist[j] = m_laplaceConst;
            }
            if (!m_instances.instance(indices[k]).isMissing(i)) {
              dist[((int)m_instances.instance(indices[k]).value(i))] += 1.0D;
            } else {
              dist[((int)m_globalMeansOrModes[i])] += 1.0D;
            }
            Utils.normalize(dist);
            double[] cumDist = computeCumulativeDistribution(dist);
            double randomVal = m_random.nextDouble();
            int instVal = 0;
            for (int j = 0; j < cumDist.length; j++) {
              if (randomVal <= cumDist[j]) {
                instVal = j;
                break;
              }
            }
            values[indices[k]][i] = instVal;
          }
        }
      }
    }
    return values;
  }
  





  private double normalDens(double x, double mean, double stdDev)
  {
    double diff = x - mean;
    
    return 1.0D / (m_normConst * stdDev) * Math.exp(-(diff * diff / (2.0D * stdDev * stdDev)));
  }
  





  public void setWeightingDimensions(boolean[] dims)
  {
    m_weightingDimensions = dims;
  }
  







  public void setWeightingValues(double[] vals)
  {
    m_weightingValues = vals;
  }
  




  public int getNumGeneratingModels()
  {
    if (m_instances != null) {
      return m_instances.numInstances();
    }
    return 0;
  }
  




  public void setKernelBandwidth(int kb)
  {
    m_kernelBandwidth = kb;
  }
  




  public int getKernelBandwidth()
  {
    return m_kernelBandwidth;
  }
  





  public void setSeed(int seed)
  {
    m_seed = seed;
    m_random = new Random(m_seed);
  }
  







  private double distance(Instance first, Instance second)
  {
    double distance = 0.0D;
    
    for (int i = 0; i < m_instances.numAttributes(); i++)
      if (i != m_instances.classIndex())
      {

        double firstVal = m_globalMeansOrModes[i];
        double secondVal = m_globalMeansOrModes[i];
        double diff;
        switch (m_instances.attribute(i).type())
        {
        case 0: 
          if (!first.isMissing(i)) {
            firstVal = first.value(i);
          }
          
          if (!second.isMissing(i)) {
            secondVal = second.value(i);
          }
          
          diff = norm(firstVal, i) - norm(secondVal, i);
          
          break;
        default: 
          diff = 0.0D;
        }
        
        distance += diff * diff;
      }
    return Math.sqrt(distance);
  }
  






  private double norm(double x, int i)
  {
    if ((Double.isNaN(m_Min[i])) || (Utils.eq(m_Max[i], m_Min[i]))) {
      return 0.0D;
    }
    return (x - m_Min[i]) / (m_Max[i] - m_Min[i]);
  }
  







  private void updateMinMax(Instance instance)
  {
    for (int j = 0; j < m_instances.numAttributes(); j++) {
      if (!instance.isMissing(j)) {
        if (Double.isNaN(m_Min[j])) {
          m_Min[j] = instance.value(j);
          m_Max[j] = instance.value(j);
        } else if (instance.value(j) < m_Min[j]) {
          m_Min[j] = instance.value(j);
        } else if (instance.value(j) > m_Max[j]) {
          m_Max[j] = instance.value(j);
        }
      }
    }
  }
  
  private void computeParams() throws Exception
  {
    m_Min = new double[m_instances.numAttributes()];
    m_Max = new double[m_instances.numAttributes()];
    for (int i = 0; i < m_instances.numAttributes(); i++) {
      double tmp52_49 = NaN.0D;m_Max[i] = tmp52_49;m_Min[i] = tmp52_49;
    }
    for (int i = 0; i < m_instances.numInstances(); i++) {
      updateMinMax(m_instances.instance(i));
    }
    
    double[] distances = new double[m_instances.numInstances()];
    for (int i = 0; i < m_instances.numInstances(); i++) {
      Instance current = m_instances.instance(i);
      for (int j = 0; j < m_instances.numInstances(); j++) {
        distances[j] = distance(current, m_instances.instance(j));
      }
      int[] sorted = Utils.sort(distances);
      int k = m_kernelBandwidth;
      double bandwidth = distances[sorted[k]];
      

      if (bandwidth <= 0.0D) {
        for (int j = k + 1; j < sorted.length; j++) {
          if (distances[sorted[j]] > bandwidth) {
            bandwidth = distances[sorted[j]];
            break;
          }
        }
        if (bandwidth <= 0.0D) {
          Messages.getInstance();throw new Exception(Messages.getString("KDDataGenerator_UpdateMinMax_Error_Text"));
        }
      }
      for (int j = 0; j < m_instances.numAttributes(); j++) {
        if (m_Max[j] - m_Min[j] > 0.0D) {
          m_kernelParams[i][j] = (bandwidth * (m_Max[j] - m_Min[j]));
        }
      }
    }
  }
}

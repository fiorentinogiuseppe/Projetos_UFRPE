package weka.classifiers.functions.neural;

import java.util.Random;
import weka.core.RevisionUtils;












































public class NeuralNode
  extends NeuralConnection
{
  private static final long serialVersionUID = -1085750607680839163L;
  private double[] m_weights;
  private double[] m_bestWeights;
  private double[] m_changeInWeights;
  private Random m_random;
  private NeuralMethod m_methods;
  
  public NeuralNode(String id, Random r, NeuralMethod m)
  {
    super(id);
    m_weights = new double[1];
    m_bestWeights = new double[1];
    m_changeInWeights = new double[1];
    
    m_random = r;
    
    m_weights[0] = (m_random.nextDouble() * 0.1D - 0.05D);
    m_changeInWeights[0] = 0.0D;
    
    m_methods = m;
  }
  




  public void setMethod(NeuralMethod m)
  {
    m_methods = m;
  }
  
  public NeuralMethod getMethod() {
    return m_methods;
  }
  






  public double outputValue(boolean calculate)
  {
    if ((Double.isNaN(m_unitValue)) && (calculate))
    {
      m_unitValue = m_methods.outputValue(this);
    }
    
    return m_unitValue;
  }
  







  public double errorValue(boolean calculate)
  {
    if ((!Double.isNaN(m_unitValue)) && (Double.isNaN(m_unitError)) && (calculate))
    {
      m_unitError = m_methods.errorValue(this);
    }
    return m_unitError;
  }
  






  public void reset()
  {
    if ((!Double.isNaN(m_unitValue)) || (!Double.isNaN(m_unitError))) {
      m_unitValue = NaN.0D;
      m_unitError = NaN.0D;
      m_weightsUpdated = false;
      for (int noa = 0; noa < m_numInputs; noa++) {
        m_inputList[noa].reset();
      }
    }
  }
  




  public void saveWeights()
  {
    System.arraycopy(m_weights, 0, m_bestWeights, 0, m_weights.length);
    

    for (int i = 0; i < m_numInputs; i++) {
      m_inputList[i].saveWeights();
    }
  }
  




  public void restoreWeights()
  {
    System.arraycopy(m_bestWeights, 0, m_weights, 0, m_weights.length);
    

    for (int i = 0; i < m_numInputs; i++) {
      m_inputList[i].restoreWeights();
    }
  }
  







  public double weightValue(int n)
  {
    if ((n >= m_numInputs) || (n < -1)) {
      return NaN.0D;
    }
    return m_weights[(n + 1)];
  }
  




  public double[] getWeights()
  {
    return m_weights;
  }
  




  public double[] getChangeInWeights()
  {
    return m_changeInWeights;
  }
  








  public void updateWeights(double l, double m)
  {
    if ((!m_weightsUpdated) && (!Double.isNaN(m_unitError))) {
      m_methods.updateWeights(this, l, m);
      




      super.updateWeights(l, m);
    }
  }
  








  protected boolean connectInput(NeuralConnection i, int n)
  {
    if (!super.connectInput(i, n)) {
      return false;
    }
    


    m_weights[m_numInputs] = (m_random.nextDouble() * 0.1D - 0.05D);
    m_changeInWeights[m_numInputs] = 0.0D;
    
    return true;
  }
  




  protected void allocateInputs()
  {
    NeuralConnection[] temp1 = new NeuralConnection[m_inputList.length + 15];
    int[] temp2 = new int[m_inputNums.length + 15];
    double[] temp4 = new double[m_weights.length + 15];
    double[] temp5 = new double[m_changeInWeights.length + 15];
    double[] temp6 = new double[m_bestWeights.length + 15];
    
    temp4[0] = m_weights[0];
    temp5[0] = m_changeInWeights[0];
    temp6[0] = m_bestWeights[0];
    for (int noa = 0; noa < m_numInputs; noa++) {
      temp1[noa] = m_inputList[noa];
      temp2[noa] = m_inputNums[noa];
      temp4[(noa + 1)] = m_weights[(noa + 1)];
      temp5[(noa + 1)] = m_changeInWeights[(noa + 1)];
      temp6[(noa + 1)] = m_bestWeights[(noa + 1)];
    }
    
    m_inputList = temp1;
    m_inputNums = temp2;
    m_weights = temp4;
    m_changeInWeights = temp5;
    m_bestWeights = temp6;
  }
  












  protected boolean disconnectInput(NeuralConnection i, int n)
  {
    int loc = -1;
    boolean removed = false;
    do {
      loc = -1;
      for (int noa = 0; noa < m_numInputs; noa++) {
        if ((i == m_inputList[noa]) && ((n == -1) || (n == m_inputNums[noa]))) {
          loc = noa;
          break;
        }
      }
      
      if (loc >= 0) {
        for (int noa = loc + 1; noa < m_numInputs; noa++) {
          m_inputList[(noa - 1)] = m_inputList[noa];
          m_inputNums[(noa - 1)] = m_inputNums[noa];
          
          m_weights[noa] = m_weights[(noa + 1)];
          m_changeInWeights[noa] = m_changeInWeights[(noa + 1)];
          
          m_inputList[(noa - 1)].changeOutputNum(m_inputNums[(noa - 1)], noa - 1);
        }
        m_numInputs -= 1;
        removed = true;
      }
    } while ((n == -1) && (loc != -1));
    return removed;
  }
  



  public void removeAllInputs()
  {
    super.removeAllInputs();
    
    double temp1 = m_weights[0];
    double temp2 = m_changeInWeights[0];
    
    m_weights = new double[1];
    m_changeInWeights = new double[1];
    
    m_weights[0] = temp1;
    m_changeInWeights[0] = temp2;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5403 $");
  }
}

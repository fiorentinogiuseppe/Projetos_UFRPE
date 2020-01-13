package weka.gui.boundaryvisualizer;

import java.io.Serializable;

















































public class RemoteResult
  implements Serializable
{
  private static final long serialVersionUID = 1873271280044633808L;
  private int m_rowNumber;
  private int m_rowLength;
  private double[][] m_probabilities;
  private int m_percentCompleted;
  
  public RemoteResult(int rowNum, int rowLength)
  {
    m_probabilities = new double[rowLength][0];
  }
  






  public void setLocationProbs(int index, double[] distribution)
  {
    m_probabilities[index] = distribution;
  }
  




  public double[][] getProbabilities()
  {
    return m_probabilities;
  }
  




  public void setPercentCompleted(int pc)
  {
    m_percentCompleted = pc;
  }
  




  public int getPercentCompleted()
  {
    return m_percentCompleted;
  }
}

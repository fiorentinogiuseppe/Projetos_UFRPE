package weka.classifiers.functions.neural;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import weka.core.RevisionHandler;

























































































public abstract class NeuralConnection
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -286208828571059163L;
  public static final int UNCONNECTED = 0;
  public static final int PURE_INPUT = 1;
  public static final int PURE_OUTPUT = 2;
  public static final int INPUT = 4;
  public static final int OUTPUT = 8;
  public static final int CONNECTED = 16;
  protected NeuralConnection[] m_inputList;
  protected NeuralConnection[] m_outputList;
  protected int[] m_inputNums;
  protected int[] m_outputNums;
  protected int m_numInputs;
  protected int m_numOutputs;
  protected double m_unitValue;
  protected double m_unitError;
  protected boolean m_weightsUpdated;
  protected String m_id;
  protected int m_type;
  protected double m_x;
  protected double m_y;
  
  public NeuralConnection(String id)
  {
    m_id = id;
    m_inputList = new NeuralConnection[0];
    m_outputList = new NeuralConnection[0];
    m_inputNums = new int[0];
    m_outputNums = new int[0];
    
    m_numInputs = 0;
    m_numOutputs = 0;
    
    m_unitValue = NaN.0D;
    m_unitError = NaN.0D;
    
    m_weightsUpdated = false;
    m_x = 0.0D;
    m_y = 0.0D;
    m_type = 0;
  }
  



  public String getId()
  {
    return m_id;
  }
  


  public int getType()
  {
    return m_type;
  }
  


  public void setType(int t)
  {
    m_type = t;
  }
  






  public abstract void reset();
  






  public abstract double outputValue(boolean paramBoolean);
  






  public abstract double errorValue(boolean paramBoolean);
  






  public abstract void saveWeights();
  





  public abstract void restoreWeights();
  





  public double weightValue(int n)
  {
    return 1.0D;
  }
  












  public void updateWeights(double l, double m)
  {
    if (!m_weightsUpdated) {
      for (int noa = 0; noa < m_numInputs; noa++) {
        m_inputList[noa].updateWeights(l, m);
      }
      m_weightsUpdated = true;
    }
  }
  






  public NeuralConnection[] getInputs()
  {
    return m_inputList;
  }
  





  public NeuralConnection[] getOutputs()
  {
    return m_outputList;
  }
  





  public int[] getInputNums()
  {
    return m_inputNums;
  }
  





  public int[] getOutputNums()
  {
    return m_outputNums;
  }
  


  public double getX()
  {
    return m_x;
  }
  


  public double getY()
  {
    return m_y;
  }
  


  public void setX(double x)
  {
    m_x = x;
  }
  


  public void setY(double y)
  {
    m_y = y;
  }
  










  public boolean onUnit(Graphics g, int x, int y, int w, int h)
  {
    int m = (int)(m_x * w);
    int c = (int)(m_y * h);
    if ((x > m + 10) || (x < m - 10) || (y > c + 10) || (y < c - 10)) {
      return false;
    }
    return true;
  }
  







  public void drawNode(Graphics g, int w, int h)
  {
    if ((m_type & 0x8) == 8) {
      g.setColor(Color.orange);
    }
    else {
      g.setColor(Color.red);
    }
    g.fillOval((int)(m_x * w) - 9, (int)(m_y * h) - 9, 19, 19);
    g.setColor(Color.gray);
    g.fillOval((int)(m_x * w) - 5, (int)(m_y * h) - 5, 11, 11);
  }
  






  public void drawHighlight(Graphics g, int w, int h)
  {
    drawNode(g, w, h);
    g.setColor(Color.yellow);
    g.fillOval((int)(m_x * w) - 5, (int)(m_y * h) - 5, 11, 11);
  }
  






  public void drawInputLines(Graphics g, int w, int h)
  {
    g.setColor(Color.black);
    
    int px = (int)(m_x * w);
    int py = (int)(m_y * h);
    for (int noa = 0; noa < m_numInputs; noa++) {
      g.drawLine((int)(m_inputList[noa].getX() * w), (int)(m_inputList[noa].getY() * h), px, py);
    }
  }
  








  public void drawOutputLines(Graphics g, int w, int h)
  {
    g.setColor(Color.black);
    
    int px = (int)(m_x * w);
    int py = (int)(m_y * h);
    for (int noa = 0; noa < m_numOutputs; noa++) {
      g.drawLine(px, py, (int)(m_outputList[noa].getX() * w), (int)(m_outputList[noa].getY() * h));
    }
  }
  









  protected boolean connectInput(NeuralConnection i, int n)
  {
    for (int noa = 0; noa < m_numInputs; noa++) {
      if (i == m_inputList[noa]) {
        return false;
      }
    }
    if (m_numInputs >= m_inputList.length)
    {
      allocateInputs();
    }
    m_inputList[m_numInputs] = i;
    m_inputNums[m_numInputs] = n;
    m_numInputs += 1;
    return true;
  }
  




  protected void allocateInputs()
  {
    NeuralConnection[] temp1 = new NeuralConnection[m_inputList.length + 15];
    int[] temp2 = new int[m_inputNums.length + 15];
    
    for (int noa = 0; noa < m_numInputs; noa++) {
      temp1[noa] = m_inputList[noa];
      temp2[noa] = m_inputNums[noa];
    }
    m_inputList = temp1;
    m_inputNums = temp2;
  }
  






  protected boolean connectOutput(NeuralConnection o, int n)
  {
    for (int noa = 0; noa < m_numOutputs; noa++) {
      if (o == m_outputList[noa]) {
        return false;
      }
    }
    if (m_numOutputs >= m_outputList.length)
    {
      allocateOutputs();
    }
    m_outputList[m_numOutputs] = o;
    m_outputNums[m_numOutputs] = n;
    m_numOutputs += 1;
    return true;
  }
  




  protected void allocateOutputs()
  {
    NeuralConnection[] temp1 = new NeuralConnection[m_outputList.length + 15];
    

    int[] temp2 = new int[m_outputNums.length + 15];
    
    for (int noa = 0; noa < m_numOutputs; noa++) {
      temp1[noa] = m_outputList[noa];
      temp2[noa] = m_outputNums[noa];
    }
    m_outputList = temp1;
    m_outputNums = temp2;
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
    for (int noa = 0; noa < m_numInputs; noa++)
    {

      m_inputList[noa].disconnectOutput(this, -1);
    }
    

    m_inputList = new NeuralConnection[0];
    setType(getType() & 0xFFFFFFFB);
    if (getNumOutputs() == 0) {
      setType(getType() & 0xFFFFFFEF);
    }
    m_inputNums = new int[0];
    m_numInputs = 0;
  }
  








  protected void changeInputNum(int n, int v)
  {
    if ((n >= m_numInputs) || (n < 0)) {
      return;
    }
    
    m_inputNums[n] = v;
  }
  









  protected boolean disconnectOutput(NeuralConnection o, int n)
  {
    int loc = -1;
    boolean removed = false;
    do {
      loc = -1;
      for (int noa = 0; noa < m_numOutputs; noa++) {
        if ((o == m_outputList[noa]) && ((n == -1) || (n == m_outputNums[noa]))) {
          loc = noa;
          break;
        }
      }
      
      if (loc >= 0) {
        for (int noa = loc + 1; noa < m_numOutputs; noa++) {
          m_outputList[(noa - 1)] = m_outputList[noa];
          m_outputNums[(noa - 1)] = m_outputNums[noa];
          

          m_outputList[(noa - 1)].changeInputNum(m_outputNums[(noa - 1)], noa - 1);
        }
        m_numOutputs -= 1;
        removed = true;
      }
    } while ((n == -1) && (loc != -1));
    
    return removed;
  }
  




  public void removeAllOutputs()
  {
    for (int noa = 0; noa < m_numOutputs; noa++)
    {

      m_outputList[noa].disconnectInput(this, -1);
    }
    

    m_outputList = new NeuralConnection[0];
    m_outputNums = new int[0];
    setType(getType() & 0xFFFFFFF7);
    if (getNumInputs() == 0) {
      setType(getType() & 0xFFFFFFEF);
    }
    m_numOutputs = 0;
  }
  






  protected void changeOutputNum(int n, int v)
  {
    if ((n >= m_numOutputs) || (n < 0)) {
      return;
    }
    
    m_outputNums[n] = v;
  }
  


  public int getNumInputs()
  {
    return m_numInputs;
  }
  


  public int getNumOutputs()
  {
    return m_numOutputs;
  }
  







  public static boolean connect(NeuralConnection s, NeuralConnection t)
  {
    if ((s == null) || (t == null)) {
      return false;
    }
    



    disconnect(s, t);
    if (s == t) {
      return false;
    }
    if ((t.getType() & 0x1) == 1) {
      return false;
    }
    if ((s.getType() & 0x2) == 2) {
      return false;
    }
    if (((s.getType() & 0x1) == 1) && ((t.getType() & 0x2) == 2))
    {
      return false;
    }
    if (((t.getType() & 0x2) == 2) && (t.getNumInputs() > 0)) {
      return false;
    }
    
    if (((t.getType() & 0x2) == 2) && ((s.getType() & 0x8) == 8))
    {
      return false;
    }
    
    if (!s.connectOutput(t, t.getNumInputs())) {
      return false;
    }
    if (!t.connectInput(s, s.getNumOutputs() - 1))
    {
      s.disconnectOutput(t, t.getNumInputs());
      return false;
    }
    


    if ((s.getType() & 0x1) == 1) {
      t.setType(t.getType() | 0x4);
    }
    else if ((t.getType() & 0x2) == 2) {
      s.setType(s.getType() | 0x8);
    }
    t.setType(t.getType() | 0x10);
    s.setType(s.getType() | 0x10);
    return true;
  }
  







  public static boolean disconnect(NeuralConnection s, NeuralConnection t)
  {
    if ((s == null) || (t == null)) {
      return false;
    }
    
    boolean stat1 = s.disconnectOutput(t, -1);
    boolean stat2 = t.disconnectInput(s, -1);
    if ((stat1) && (stat2)) {
      if ((s.getType() & 0x1) == 1) {
        t.setType(t.getType() & 0xFFFFFFFB);
      }
      else if ((t.getType() & 0x2) == 2) {
        s.setType(s.getType() & 0xFFFFFFF7);
      }
      if ((s.getNumInputs() == 0) && (s.getNumOutputs() == 0)) {
        s.setType(s.getType() & 0xFFFFFFEF);
      }
      if ((t.getNumInputs() == 0) && (t.getNumOutputs() == 0)) {
        t.setType(t.getType() & 0xFFFFFFEF);
      }
    }
    return (stat1) && (stat2);
  }
}

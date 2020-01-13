package weka.classifiers;

import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.util.Random;
import java.util.StringTokenizer;
import weka.core.Attribute;
import weka.core.AttributeExpression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;







































public class CostMatrix
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -1973792250544554965L;
  private int m_size;
  protected Object[][] m_matrix;
  public static String FILE_EXTENSION = ".cost";
  





  public CostMatrix(int numOfClasses)
  {
    m_size = numOfClasses;
    initialize();
  }
  




  public CostMatrix(CostMatrix toCopy)
  {
    this(toCopy.size());
    
    for (int i = 0; i < m_size; i++) {
      for (int j = 0; j < m_size; j++) {
        setCell(i, j, toCopy.getCell(i, j));
      }
    }
  }
  


  public void initialize()
  {
    m_matrix = new Object[m_size][m_size];
    for (int i = 0; i < m_size; i++) {
      for (int j = 0; j < m_size; j++) {
        setCell(i, j, i == j ? new Double(0.0D) : new Double(1.0D));
      }
    }
  }
  




  public int size()
  {
    return m_size;
  }
  




  public int numColumns()
  {
    return size();
  }
  




  public int numRows()
  {
    return size();
  }
  
  private boolean replaceStrings() throws Exception {
    boolean nonDouble = false;
    
    for (int i = 0; i < m_size; i++) {
      for (int j = 0; j < m_size; j++) {
        if ((getCell(i, j) instanceof String)) {
          AttributeExpression temp = new AttributeExpression();
          temp.convertInfixToPostfix((String)getCell(i, j));
          setCell(i, j, temp);
          nonDouble = true;
        } else if ((getCell(i, j) instanceof AttributeExpression)) {
          nonDouble = true;
        }
      }
    }
    
    return nonDouble;
  }
  












  public Instances applyCostMatrix(Instances data, Random random)
    throws Exception
  {
    double sumOfWeightFactors = 0.0D;
    


    if (data.classIndex() < 0) {
      throw new Exception("Class index is not set!");
    }
    
    if (size() != data.numClasses()) {
      throw new Exception("Misclassification cost matrix has wrong format!");
    }
    


    if (replaceStrings())
    {
      if (data.classAttribute().numValues() > 2) {
        throw new Exception("Can't resample/reweight instances using non-fixed cost values when there are more than two classes!");
      }
      


      double[] weightOfInstances = new double[data.numInstances()];
      for (int i = 0; i < data.numInstances(); i++) {
        Instance inst = data.instance(i);
        int classValIndex = (int)inst.classValue();
        double factor = 1.0D;
        Object element = classValIndex == 0 ? getCell(classValIndex, 1) : getCell(classValIndex, 0);
        
        if ((element instanceof Double)) {
          factor = ((Double)element).doubleValue();
        } else {
          factor = ((AttributeExpression)element).evaluateExpression(inst);
        }
        weightOfInstances[i] = (inst.weight() * factor);
      }
      






      if (random != null) {
        return data.resampleWithWeights(random, weightOfInstances);
      }
      Instances instances = new Instances(data);
      for (int i = 0; i < data.numInstances(); i++) {
        instances.instance(i).setWeight(weightOfInstances[i]);
      }
      return instances;
    }
    


    double[] weightFactor = new double[data.numClasses()];
    double[] weightOfInstancesInClass = new double[data.numClasses()];
    for (int j = 0; j < data.numInstances(); j++) {
      weightOfInstancesInClass[((int)data.instance(j).classValue())] += data.instance(j).weight();
    }
    
    double sumOfWeights = Utils.sum(weightOfInstancesInClass);
    

    for (int i = 0; i < m_size; i++) {
      if (!Utils.eq(((Double)getCell(i, i)).doubleValue(), 0.0D)) {
        CostMatrix normMatrix = new CostMatrix(this);
        normMatrix.normalize();
        return normMatrix.applyCostMatrix(data, random);
      }
    }
    
    for (int i = 0; i < data.numClasses(); i++)
    {



      double sumOfMissClassWeights = 0.0D;
      for (int j = 0; j < data.numClasses(); j++) {
        if (Utils.sm(((Double)getCell(i, j)).doubleValue(), 0.0D)) {
          throw new Exception("Neg. weights in misclassification cost matrix!");
        }
        
        sumOfMissClassWeights += ((Double)getCell(i, j)).doubleValue();
      }
      weightFactor[i] = (sumOfMissClassWeights * sumOfWeights);
      sumOfWeightFactors += sumOfMissClassWeights * weightOfInstancesInClass[i];
    }
    for (int i = 0; i < data.numClasses(); i++) {
      weightFactor[i] /= sumOfWeightFactors;
    }
    

    double[] weightOfInstances = new double[data.numInstances()];
    for (int i = 0; i < data.numInstances(); i++) {
      weightOfInstances[i] = (data.instance(i).weight() * weightFactor[((int)data.instance(i).classValue())]);
    }
    


    if (random != null) {
      return data.resampleWithWeights(random, weightOfInstances);
    }
    Instances instances = new Instances(data);
    for (int i = 0; i < data.numInstances(); i++) {
      instances.instance(i).setWeight(weightOfInstances[i]);
    }
    return instances;
  }
  









  public double[] expectedCosts(double[] classProbs)
    throws Exception
  {
    if (classProbs.length != m_size) {
      throw new Exception("Length of probability estimates don't match cost matrix");
    }
    

    double[] costs = new double[m_size];
    
    for (int x = 0; x < m_size; x++) {
      for (int y = 0; y < m_size; y++) {
        Object element = getCell(y, x);
        if (!(element instanceof Double)) {
          throw new Exception("Can't use non-fixed costs in computing expected costs.");
        }
        
        costs[x] += classProbs[y] * ((Double)element).doubleValue();
      }
    }
    
    return costs;
  }
  










  public double[] expectedCosts(double[] classProbs, Instance inst)
    throws Exception
  {
    if (classProbs.length != m_size) {
      throw new Exception("Length of probability estimates don't match cost matrix");
    }
    

    if (!replaceStrings()) {
      return expectedCosts(classProbs);
    }
    
    double[] costs = new double[m_size];
    
    for (int x = 0; x < m_size; x++) {
      for (int y = 0; y < m_size; y++) {
        Object element = getCell(y, x);
        double costVal;
        double costVal; if (!(element instanceof Double)) {
          costVal = ((AttributeExpression)element).evaluateExpression(inst);
        } else {
          costVal = ((Double)element).doubleValue();
        }
        costs[x] += classProbs[y] * costVal;
      }
    }
    
    return costs;
  }
  






  public double getMaxCost(int classVal)
    throws Exception
  {
    double maxCost = Double.NEGATIVE_INFINITY;
    
    for (int i = 0; i < m_size; i++) {
      Object element = getCell(classVal, i);
      if (!(element instanceof Double)) {
        throw new Exception("Can't use non-fixed costs when getting max cost.");
      }
      
      double cost = ((Double)element).doubleValue();
      if (cost > maxCost) {
        maxCost = cost;
      }
    }
    return maxCost;
  }
  






  public double getMaxCost(int classVal, Instance inst)
    throws Exception
  {
    if (!replaceStrings()) {
      return getMaxCost(classVal);
    }
    
    double maxCost = Double.NEGATIVE_INFINITY;
    
    for (int i = 0; i < m_size; i++) {
      Object element = getCell(classVal, i);
      double cost; double cost; if (!(element instanceof Double)) {
        cost = ((AttributeExpression)element).evaluateExpression(inst);
      } else {
        cost = ((Double)element).doubleValue();
      }
      if (cost > maxCost) {
        maxCost = cost;
      }
    }
    return maxCost;
  }
  




  public void normalize()
  {
    for (int y = 0; y < m_size; y++) {
      double diag = ((Double)getCell(y, y)).doubleValue();
      for (int x = 0; x < m_size; x++) {
        setCell(x, y, new Double(((Double)getCell(x, y)).doubleValue() - diag));
      }
    }
  }
  










  public void readOldFormat(Reader reader)
    throws Exception
  {
    StreamTokenizer tokenizer = new StreamTokenizer(reader);
    
    initialize();
    
    tokenizer.commentChar(37);
    tokenizer.eolIsSignificant(true);
    int currentToken; while (-1 != (currentToken = tokenizer.nextToken()))
    {

      if (currentToken != 10)
      {



        if (currentToken != -2) {
          throw new Exception("Only numbers and comments allowed in cost file!");
        }
        
        double firstIndex = nval;
        if (!Utils.eq((int)firstIndex, firstIndex)) {
          throw new Exception("First number in line has to be index of a class!");
        }
        
        if ((int)firstIndex >= size()) {
          throw new Exception("Class index out of range!");
        }
        

        if (-1 == (currentToken = tokenizer.nextToken())) {
          throw new Exception("Premature end of file!");
        }
        if (currentToken == 10) {
          throw new Exception("Premature end of line!");
        }
        if (currentToken != -2) {
          throw new Exception("Only numbers and comments allowed in cost file!");
        }
        
        double secondIndex = nval;
        if (!Utils.eq((int)secondIndex, secondIndex)) {
          throw new Exception("Second number in line has to be index of a class!");
        }
        
        if ((int)secondIndex >= size()) {
          throw new Exception("Class index out of range!");
        }
        if ((int)secondIndex == (int)firstIndex) {
          throw new Exception("Diagonal of cost matrix non-zero!");
        }
        

        if (-1 == (currentToken = tokenizer.nextToken())) {
          throw new Exception("Premature end of file!");
        }
        if (currentToken == 10) {
          throw new Exception("Premature end of line!");
        }
        if (currentToken != -2) {
          throw new Exception("Only numbers and comments allowed in cost file!");
        }
        
        double weight = nval;
        if (!Utils.gr(weight, 0.0D)) {
          throw new Exception("Only positive weights allowed!");
        }
        setCell((int)firstIndex, (int)secondIndex, new Double(weight));
      }
    }
  }
  






  public CostMatrix(Reader reader)
    throws Exception
  {
    LineNumberReader lnr = new LineNumberReader(reader);
    
    int currentRow = -1;
    String line;
    while ((line = lnr.readLine()) != null)
    {

      if (!line.startsWith("%"))
      {


        StringTokenizer st = new StringTokenizer(line);
        
        if (st.hasMoreTokens())
        {


          if (currentRow < 0) {
            int rows = Integer.parseInt(st.nextToken());
            if (!st.hasMoreTokens()) {
              throw new Exception("Line " + lnr.getLineNumber() + ": expected number of columns");
            }
            

            int cols = Integer.parseInt(st.nextToken());
            if (rows != cols) {
              throw new Exception("Trying to create a non-square cost matrix");
            }
            
            m_size = rows;
            initialize();
            currentRow++;
          }
          else
          {
            if (currentRow == m_size) {
              throw new Exception("Line " + lnr.getLineNumber() + ": too many rows provided");
            }
            

            for (int i = 0; i < m_size; i++) {
              if (!st.hasMoreTokens()) {
                throw new Exception("Line " + lnr.getLineNumber() + ": too few matrix elements provided");
              }
              

              String nextTok = st.nextToken();
              
              Double val = null;
              try {
                val = new Double(nextTok);
                value = val.doubleValue();
              } catch (Exception ex) { double value;
                val = null;
              }
              if (val == null) {
                setCell(currentRow, i, nextTok);
              } else {
                setCell(currentRow, i, val);
              }
            }
            currentRow++;
          } }
      }
    }
    if (currentRow == -1) {
      throw new Exception("Line " + lnr.getLineNumber() + ": expected number of rows");
    }
    if (currentRow != m_size) {
      throw new Exception("Line " + lnr.getLineNumber() + ": too few rows provided");
    }
  }
  






  public void write(Writer w)
    throws Exception
  {
    w.write("% Rows\tColumns\n");
    w.write("" + m_size + "\t" + m_size + "\n");
    w.write("% Matrix elements\n");
    for (int i = 0; i < m_size; i++) {
      for (int j = 0; j < m_size; j++) {
        w.write("" + getCell(i, j) + "\t");
      }
      w.write("\n");
    }
    w.flush();
  }
  










  public String toMatlab()
  {
    StringBuffer result = new StringBuffer();
    
    result.append("[");
    
    for (int i = 0; i < m_size; i++) {
      if (i > 0) {
        result.append("; ");
      }
      
      for (int n = 0; n < m_size; n++) {
        if (n > 0) {
          result.append(" ");
        }
        result.append(getCell(i, n));
      }
    }
    
    result.append("]");
    
    return result.toString();
  }
  






  public final void setCell(int rowIndex, int columnIndex, Object value)
  {
    m_matrix[rowIndex][columnIndex] = value;
  }
  







  public final Object getCell(int rowIndex, int columnIndex)
  {
    return m_matrix[rowIndex][columnIndex];
  }
  







  public final double getElement(int rowIndex, int columnIndex)
    throws Exception
  {
    if (!(m_matrix[rowIndex][columnIndex] instanceof Double)) {
      throw new Exception("Cost matrix contains non-fixed costs!");
    }
    return ((Double)m_matrix[rowIndex][columnIndex]).doubleValue();
  }
  









  public final double getElement(int rowIndex, int columnIndex, Instance inst)
    throws Exception
  {
    if ((m_matrix[rowIndex][columnIndex] instanceof Double))
      return ((Double)m_matrix[rowIndex][columnIndex]).doubleValue();
    if ((m_matrix[rowIndex][columnIndex] instanceof String)) {
      replaceStrings();
    }
    
    return ((AttributeExpression)m_matrix[rowIndex][columnIndex]).evaluateExpression(inst);
  }
  







  public final void setElement(int rowIndex, int columnIndex, double value)
  {
    m_matrix[rowIndex][columnIndex] = new Double(value);
  }
  













  public static CostMatrix parseMatlab(String matlab)
    throws Exception
  {
    String cells = matlab.substring(matlab.indexOf("[") + 1, matlab.indexOf("]")).trim();
    


    StringTokenizer tokRow = new StringTokenizer(cells, ";");
    int rows = tokRow.countTokens();
    StringTokenizer tokCol = new StringTokenizer(tokRow.nextToken(), " ");
    int cols = tokCol.countTokens();
    

    CostMatrix result = new CostMatrix(rows);
    tokRow = new StringTokenizer(cells, ";");
    rows = 0;
    while (tokRow.hasMoreTokens()) {
      tokCol = new StringTokenizer(tokRow.nextToken(), " ");
      cols = 0;
      while (tokCol.hasMoreTokens())
      {
        String current = tokCol.nextToken();
        try {
          double val = Double.parseDouble(current);
          result.setCell(rows, cols, new Double(val));
        }
        catch (NumberFormatException e) {
          result.setCell(rows, cols, current);
        }
        cols++;
      }
      rows++;
    }
    
    return result;
  }
  








  public String toString()
  {
    double maxval = 0.0D;
    boolean fractional = false;
    Object element = null;
    int widthNumber = 0;
    int widthExpression = 0;
    for (int i = 0; i < size(); i++) {
      for (int j = 0; j < size(); j++) {
        element = getCell(i, j);
        if ((element instanceof Double)) {
          double current = ((Double)element).doubleValue();
          
          if (current < 0.0D)
            current *= -11.0D;
          if (current > maxval)
            maxval = current;
          double fract = Math.abs(current - Math.rint(current));
          if ((!fractional) && (Math.log(fract) / Math.log(10.0D) >= -2.0D)) {
            fractional = true;
          }
        }
        else if (element.toString().length() > widthExpression) {
          widthExpression = element.toString().length();
        }
      }
    }
    
    if (maxval > 0.0D) {
      widthNumber = (int)(Math.log(maxval) / Math.log(10.0D) + (fractional ? 4 : 1));
    }
    

    int width = widthNumber > widthExpression ? widthNumber : widthExpression;
    
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < size(); i++) {
      for (int j = 0; j < size(); j++) {
        element = getCell(i, j);
        if ((element instanceof Double)) {
          text.append(" ").append(Utils.doubleToString(((Double)element).doubleValue(), width, fractional ? 2 : 0));
        }
        else
        {
          int diff = width - element.toString().length();
          if (diff > 0) {
            int left = diff % 2;
            left += diff / 2;
            String temp = Utils.padLeft(element.toString(), element.toString().length() + left);
            
            temp = Utils.padRight(temp, width);
            text.append(" ").append(temp);
          } else {
            text.append(" ").append(element.toString());
          }
        }
      }
      text.append("\n");
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9048 $");
  }
}

package weka.filters.unsupervised.attribute;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java_cup.runtime.DefaultSymbolFactory;
import java_cup.runtime.SymbolFactory;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.mathematicalexpression.Parser;
import weka.core.mathematicalexpression.Scanner;
import weka.experiment.Stats;
import weka.filters.UnsupervisedFilter;



























































public class MathExpression
  extends PotentialClassIgnorer
  implements UnsupervisedFilter
{
  static final long serialVersionUID = -3713222714671997901L;
  protected Range m_SelectCols = new Range();
  

  public static final String m_defaultExpression = "(A-MIN)/(MAX-MIN)";
  

  private String m_expression = "(A-MIN)/(MAX-MIN)";
  


  private AttributeStats[] m_attStats;
  


  public MathExpression()
  {
    setInvertSelection(false);
  }
  






  public String globalInfo()
  {
    return "Modify numeric attributes according to a given expression ";
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  









  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    m_SelectCols.setUpper(instanceInfo.numAttributes() - 1);
    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    m_attStats = null;
    return true;
  }
  








  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (m_attStats == null) {
      bufferInput(instance);
      return false;
    }
    convertInstance(instance);
    return true;
  }
  








  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_attStats == null) {
      Instances input = getInputFormat();
      
      m_attStats = new AttributeStats[input.numAttributes()];
      
      for (int i = 0; i < input.numAttributes(); i++) {
        if ((input.attribute(i).isNumeric()) && (input.classIndex() != i))
        {
          m_attStats[i] = input.attributeStats(i);
        }
      }
      

      for (int i = 0; i < input.numInstances(); i++) {
        convertInstance(input.instance(i));
      }
    }
    
    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  




  protected double eval(HashMap symbols)
  {
    double result;
    



    try
    {
      SymbolFactory sf = new DefaultSymbolFactory();
      ByteArrayInputStream parserInput = new ByteArrayInputStream(m_expression.getBytes());
      Parser parser = new Parser(new Scanner(parserInput, sf), sf);
      parser.setSymbols(symbols);
      parser.parse();
      result = parser.getResult().doubleValue();
    }
    catch (Exception e) {
      result = NaN.0D;
      e.printStackTrace();
    }
    
    return result;
  }
  






  private void convertInstance(Instance instance)
    throws Exception
  {
    Instance inst = null;
    HashMap symbols = new HashMap(5);
    if ((instance instanceof SparseInstance)) {
      double[] newVals = new double[instance.numAttributes()];
      int[] newIndices = new int[instance.numAttributes()];
      double[] vals = instance.toDoubleArray();
      int ind = 0;
      
      for (int j = 0; j < instance.numAttributes(); j++) {
        if (m_SelectCols.isInRange(j)) {
          if ((instance.attribute(j).isNumeric()) && (!Instance.isMissingValue(vals[j])) && (getInputFormat().classIndex() != j))
          {

            symbols.put("A", new Double(vals[j]));
            symbols.put("MAX", new Double(m_attStats[j].numericStats.max));
            symbols.put("MIN", new Double(m_attStats[j].numericStats.min));
            symbols.put("MEAN", new Double(m_attStats[j].numericStats.mean));
            symbols.put("SD", new Double(m_attStats[j].numericStats.stdDev));
            symbols.put("COUNT", new Double(m_attStats[j].numericStats.count));
            symbols.put("SUM", new Double(m_attStats[j].numericStats.sum));
            symbols.put("SUMSQUARED", new Double(m_attStats[j].numericStats.sumSq));
            double value = eval(symbols);
            if ((Double.isNaN(value)) || (Double.isInfinite(value))) {
              System.err.println("WARNING:Error in evaluating the expression: missing value set");
              value = Instance.missingValue();
            }
            if (value != 0.0D) {
              newVals[ind] = value;
              newIndices[ind] = j;
              ind++;
            }
          }
        }
        else {
          double value = vals[j];
          if (value != 0.0D) {
            newVals[ind] = value;
            newIndices[ind] = j;
            ind++;
          }
        }
      }
      double[] tempVals = new double[ind];
      int[] tempInd = new int[ind];
      System.arraycopy(newVals, 0, tempVals, 0, ind);
      System.arraycopy(newIndices, 0, tempInd, 0, ind);
      inst = new SparseInstance(instance.weight(), tempVals, tempInd, instance.numAttributes());
    }
    else {
      double[] vals = instance.toDoubleArray();
      for (int j = 0; j < getInputFormat().numAttributes(); j++) {
        if ((m_SelectCols.isInRange(j)) && 
          (instance.attribute(j).isNumeric()) && (!Instance.isMissingValue(vals[j])) && (getInputFormat().classIndex() != j))
        {

          symbols.put("A", new Double(vals[j]));
          symbols.put("MAX", new Double(m_attStats[j].numericStats.max));
          symbols.put("MIN", new Double(m_attStats[j].numericStats.min));
          symbols.put("MEAN", new Double(m_attStats[j].numericStats.mean));
          symbols.put("SD", new Double(m_attStats[j].numericStats.stdDev));
          symbols.put("COUNT", new Double(m_attStats[j].numericStats.count));
          symbols.put("SUM", new Double(m_attStats[j].numericStats.sum));
          symbols.put("SUMSQUARED", new Double(m_attStats[j].numericStats.sumSq));
          vals[j] = eval(symbols);
          if ((Double.isNaN(vals[j])) || (Double.isInfinite(vals[j]))) {
            System.err.println("WARNING:Error in Evaluation the Expression: missing value set");
            vals[j] = Instance.missingValue();
          }
        }
      }
      
      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(instance.dataset());
    push(inst);
  }
  



























  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String expString = Utils.getOption('E', options);
    if (expString.length() != 0) {
      setExpression(expString);
    } else {
      setExpression("(A-MIN)/(MAX-MIN)");
    }
    
    String ignoreList = Utils.getOption('R', options);
    if (ignoreList.length() != 0) {
      setIgnoreRange(ignoreList);
    }
    
    setInvertSelection(Utils.getFlag('V', options));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-E");
    result.add(getExpression());
    
    if (getInvertSelection()) {
      result.add("-V");
    }
    if (!getIgnoreRange().equals("")) {
      result.add("-R");
      result.add(getIgnoreRange());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    result.addElement(new Option("\tSpecify the expression to apply. Eg. pow(A,6)/(MEAN+MAX)\n\tSupported operators are +, -, *, /, pow, log,\n\tabs, cos, exp, sqrt, tan, sin, ceil, floor, rint, (, ), \n\tMEAN, MAX, MIN, SD, COUNT, SUM, SUMSQUARED, ifelse", "E", 1, "-E <expression>"));
    





    result.addElement(new Option("\tSpecify list of columns to ignore. First and last are valid\n\tindexes. (default none)", "R", 1, "-R <index1,index2-index4,...>"));
    



    result.addElement(new Option("\tInvert matching sense (i.e. only modify specified columns)", "V", 0, "-V"));
    


    return result.elements();
  }
  





  public String expressionTipText()
  {
    return "Specify the expression to apply. The 'A' letterrefers to the attribute value. MIN,MAX,MEAN,SDrefer respectively to minimum, maximum, mean andstandard deviation of the attribute.\n\tSupported operators are +, -, *, /, pow, log,abs, cos, exp, sqrt, tan, sin, ceil, floor, rint, (, ),A,MEAN, MAX, MIN, SD, COUNT, SUM, SUMSQUARED, ifelse\n\tEg. pow(A,6)/(MEAN+MAX)*ifelse(A<0,0,sqrt(A))+ifelse(![A>9 && A<15])";
  }
  










  public void setExpression(String expr)
  {
    m_expression = expr;
  }
  



  public String getExpression()
  {
    return m_expression;
  }
  






  public String invertSelectionTipText()
  {
    return "Determines whether action is to select or unselect. If set to true, only the specified attributes will be modified; If set to false, specified attributes will not be modified.";
  }
  







  public boolean getInvertSelection()
  {
    return !m_SelectCols.getInvert();
  }
  







  public void setInvertSelection(boolean invert)
  {
    m_SelectCols.setInvert(!invert);
  }
  






  public String ignoreRangeTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  








  public String getIgnoreRange()
  {
    return m_SelectCols.getRanges();
  }
  








  public void setIgnoreRange(String rangeList)
  {
    m_SelectCols.setRanges(rangeList);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new MathExpression(), argv);
  }
}

package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.SimpleBatchFilter;
import weka.filters.unsupervised.instance.subsetbyexpression.Parser;























































































































public class SubsetByExpression
  extends SimpleBatchFilter
{
  private static final long serialVersionUID = 5628686110979589602L;
  protected String m_Expression = "true";
  

  protected boolean m_filterAfterFirstBatch = false;
  


  public SubsetByExpression() {}
  

  public String globalInfo()
  {
    return "Filters instances according to a user-specified expression.\n\nGrammar:\n\nboolexpr_list ::= boolexpr_list boolexpr_part | boolexpr_part;\n\nboolexpr_part ::= boolexpr:e {: parser.setResult(e); :} ;\n\nboolexpr ::=    BOOLEAN \n              | true\n              | false\n              | expr < expr\n              | expr <= expr\n              | expr > expr\n              | expr >= expr\n              | expr = expr\n              | ( boolexpr )\n              | not boolexpr\n              | boolexpr and boolexpr\n              | boolexpr or boolexpr\n              | ATTRIBUTE is STRING\n              ;\n\nexpr      ::=   NUMBER\n              | ATTRIBUTE\n              | ( expr )\n              | opexpr\n              | funcexpr\n              ;\n\nopexpr    ::=   expr + expr\n              | expr - expr\n              | expr * expr\n              | expr / expr\n              ;\n\nfuncexpr ::=    abs ( expr )\n              | sqrt ( expr )\n              | log ( expr )\n              | exp ( expr )\n              | sin ( expr )\n              | cos ( expr )\n              | tan ( expr )\n              | rint ( expr )\n              | floor ( expr )\n              | pow ( expr for base , expr for exponent )\n              | ceil ( expr )\n              ;\n\nNotes:\n- NUMBER\n  any integer or floating point number \n  (but not in scientific notation!)\n- STRING\n  any string surrounded by single quotes; \n  the string may not contain a single quote though.\n- ATTRIBUTE\n  the following placeholders are recognized for \n  attribute values:\n  - CLASS for the class value in case a class attribute is set.\n  - ATTxyz with xyz a number from 1 to # of attributes in the\n    dataset, representing the value of indexed attribute.\n\nExamples:\n- extracting only mammals and birds from the 'zoo' UCI dataset:\n  (CLASS is 'mammal') or (CLASS is 'bird')\n- extracting only animals with at least 2 legs from the 'zoo' UCI dataset:\n  (ATT14 >= 2)\n- extracting only instances with non-missing 'wage-increase-second-year'\n  from the 'labor' UCI dataset:\n  not ismissing(ATT3)\n";
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
    
    bufferInput(instance);
    
    int numReturnedFromParser = 0;
    if (isFirstBatchDone()) {
      Instances inst = new Instances(getInputFormat());
      inst = process(inst);
      numReturnedFromParser = inst.numInstances();
      for (int i = 0; i < inst.numInstances(); i++)
        push(inst.instance(i));
      flushInput();
    }
    
    return numReturnedFromParser > 0;
  }
  






  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe expression to use for filtering\n\t(default: true).", "E", 1, "-E <expr>"));
    



    result.addElement(new Option("\tApply the filter to instances that arrive after the first\n\t(training) batch. The default is to not apply the filter (i.e.\n\talways return the instance)", "F", 0, "-F"));
    




    return result.elements();
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('E', options);
    if (tmpStr.length() != 0) {
      setExpression(tmpStr);
    } else {
      setExpression("true");
    }
    m_filterAfterFirstBatch = Utils.getFlag('F', options);
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-E");
    result.add("" + getExpression());
    
    if (m_filterAfterFirstBatch) {
      result.add("-F");
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.STRING_CLASS);
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  




  public void setExpression(String value)
  {
    m_Expression = value;
  }
  




  public String getExpression()
  {
    return m_Expression;
  }
  





  public String expressionTipText()
  {
    return "The expression to used for filtering the dataset.";
  }
  










  public void setFilterAfterFirstBatch(boolean b)
  {
    m_filterAfterFirstBatch = b;
  }
  










  public boolean getFilterAfterFirstBatch()
  {
    return m_filterAfterFirstBatch;
  }
  





  public String filterAfterFirstBatchTipText()
  {
    return "Whether to apply the filtering process to instances that are input after the first (training) batch. The default is false so that, when used in a FilteredClassifier, test instances do not potentially get 'consumed' by the filter an a prediction is always made.";
  }
  












  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    return new Instances(inputFormat, 0);
  }
  







  protected Instances process(Instances instances)
    throws Exception
  {
    if ((!isFirstBatchDone()) || (m_filterAfterFirstBatch)) {
      return Parser.filter(m_Expression, instances);
    }
    return instances;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9804 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new SubsetByExpression(), args);
  }
}
